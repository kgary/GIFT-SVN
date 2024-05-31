/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.validation;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;
import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.color;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.ShownEvent;
import org.gwtbootstrap3.client.shared.event.ShownHandler;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.animate.client.ui.Animate;
import org.gwtbootstrap3.extras.animate.client.ui.constants.Animation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.GenericListEditor;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ItemAction;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier; 

/**
 * The widget that displays the validation messages.
 * 
 * @author sharrison
 */
public class ValidationWidget extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ValidationWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ScenarioValidationWidgetUiBinder uiBinder = GWT.create(ScenarioValidationWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ScenarioValidationWidgetUiBinder extends UiBinder<Widget, ValidationWidget> {
    }

    /** The list editor for the validation error messages */
    @UiField(provided = true)
    protected GenericListEditor<ValidationStatus> validationErrorListEditor = new GenericListEditor<ValidationStatus>(
            new Stringifier<ValidationStatus>() {
                @Override
                public String stringify(ValidationStatus container) {
                    SafeHtmlBuilder htmlBuilder = new SafeHtmlBuilder();
                    htmlBuilder.append(bold(container.getErrorMsg()));

                    if (container instanceof ModelValidationStatus) {
                        ModelValidationStatus modelStatus = (ModelValidationStatus) container;
                        if (modelStatus.isShowAdditionalInstructions()
                                && StringUtils.isNotBlank(modelStatus.getAdditionalInstructions())) {
                            htmlBuilder.appendHtmlConstant("<br/>");
                            htmlBuilder.append(bold(color(modelStatus.getAdditionalInstructions(), "red")));
                        }
                    }

                    return htmlBuilder.toSafeHtml().asString();
                }
            });

    /**
     * The parent widget that indicates we should stop moving up the widget tree. This is used when
     * the user wants to scroll to the invalid widget. If that widget is hidden (by a collapse panel
     * for example), we need to move up the parent tree opening the panels. This widget indicates
     * when we should stop traversing the tree.
     */
    private final Widget expandToStopWidget;

    /**
     * Action to scroll to the location of the validation error on the page. This will be visible
     * for each item in the {@link GenericListEditor} table.
     */
    private ItemAction<ValidationStatus> validationAction = new ItemAction<ValidationStatus>() {

        @Override
        public boolean isEnabled(ValidationStatus item) {
            if (item instanceof WidgetValidationStatus) {
                WidgetValidationStatus status = (WidgetValidationStatus) item;
                return status.getWidget() != null || !status.getElements().isEmpty();
            } else if (item instanceof ModelValidationStatus) {
                ModelValidationStatus status = (ModelValidationStatus) item;
                if (status.getModelObject() != null || StringUtils.isNotBlank(status.getAdditionalInstructions())) {
                    return true;
                }
                return false;
            }

            return false;
        }

        @Override
        public String getTooltip(ValidationStatus item) {
            if (item instanceof ModelValidationStatus) {
                ModelValidationStatus status = (ModelValidationStatus) item;
                if (status.getModelObject() == null) {
                    return StringUtils.isNotBlank(status.getAdditionalInstructions())
                            ? "Click for additional information"
                            : "No additional information was provided for this error.";
                }
            }
            return "Go to the location of the issue";
        }

        @Override
        public IconType getIconType(ValidationStatus item) {
            return IconType.EXTERNAL_LINK;
        }

        @Override
        public void execute(final ValidationStatus item) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("validationAction.execute(" + item + ")");
            }

            if (item instanceof WidgetValidationStatus) {
                final WidgetValidationStatus status = (WidgetValidationStatus) item;
                // sanity check
                if (status.getWidget() == null && status.getElements().isEmpty()) {
                    return;
                }

                Command command = new Command() {
                    @Override
                    public void execute() {
                        // calling the native scroll into view
                        Element scrollToElement;
                        if (!status.getElements().isEmpty()) {
                            scrollToElement = status.getElements().iterator().next();
                        } else {
                            scrollToElement = status.getWidget().getElement();
                        }
                        scrollIntoView(scrollToElement);

                        // can only animate on widgets, not elements
                        String animation = Animate.animate(status.getWidget(), Animation.RUBBER_BAND, 2, 2000);
                        Animate.removeAnimationOnEnd(status.getWidget(), animation);
                    }
                };

                expandToWidget(status.getWidget(), expandToStopWidget, command);
            } else if (item instanceof ModelValidationStatus) {
                ModelValidationStatus status = (ModelValidationStatus) item;
                status.setShowAdditionalInstructions(true);
                validationErrorListEditor.redrawItem(status);

                status.jumpToEvent();
            }
        }
    };

    /**
     * The command that is invoked when any {@link WidgetValidationStatus}
     * changes its error message while it's part of this
     * {@link ValidationWidget}.
     */
    private final Command onErrorMsgChangedCommand = new Command() {

        @Override
        public void execute() {
            validationErrorListEditor.refresh();
        }
    };

    /**
     * Constructor.
     * 
     * @param expandToStopWidget the parent widget that indicates we should stop moving up the
     *        widget tree. This is used when the user wants to scroll to the invalid widget. If that
     *        widget is hidden (by a collapse panel for example), we need to move up the parent tree
     *        opening the panels. This widget indicates when we should stop traversing the tree.
     *        Can't be null.
     */
    public ValidationWidget(Widget expandToStopWidget) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        if (expandToStopWidget == null) {
            throw new IllegalArgumentException("The parameter 'expandToStopWidget' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        this.expandToStopWidget = expandToStopWidget;

        // Populate the list widget
        validationErrorListEditor.setTableLabel("Please fix the following issues:");
        validationErrorListEditor.setRowAction(validationAction);

        // hidden until forced to show
        this.setVisible(false);
    }

    /**
     * Updates the list of validation messages with the provided status container. If the widget is
     * valid, it will be removed if it exists. Otherwise, it will be added to the list.
     * 
     * @param validationStatus the container for the widget and error message. Will do nothing if
     *        null.
     */
    public void updateValidationStatus(ValidationStatus validationStatus) {
        if (validationStatus == null) {
            return;
        }

        if (validationStatus.isValid()) {
            removeValidationError(validationStatus);
        } else {
            addValidationError(validationStatus);
        }
    }

    /**
     * Adds a new validation error to the table.
     * 
     * @param validationStatus the container for the widget and error message.
     */
    private void addValidationError(ValidationStatus validationStatus) {
        // check if the widget is already in the list
        if (findValidationStatus(validationStatus) != null) {
            return;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addValidationError(" + validationStatus + ")");
        }

        validationStatus.addErrorMessageChangeCommand(onErrorMsgChangedCommand);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("validation status is new... adding '" + validationStatus.getErrorMsg()
                    + "' to the validation list.");
        }

        validationErrorListEditor.addItem(validationStatus);

        if (!isVisible()) {
            setVisible(true);
        }
    }

    /**
     * Removes a validation error from the table. Will hide the widget if there are no more
     * validation errors.
     * 
     * @param validationStatus will check the provided container against existing containers. Will
     *        remove the first match it finds.
     */
    private void removeValidationError(ValidationStatus validationStatus) {
        ValidationStatus foundContainer = findValidationStatus(validationStatus);
        
        if (foundContainer == null) {
            return;
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeValidationError(" + validationStatus + ")");
        }

        validationStatus.removeErrorMessageChangeCommand(onErrorMsgChangedCommand);
        validationErrorListEditor.removeItem(foundContainer);

        int errorListSize = validationErrorListEditor.size();
        if (errorListSize == 0) {
            setVisible(false);
        }
    }

    /**
     * Checks if the provided validation status container is already being displayed to the user.
     * Checks by reference.
     * 
     * @param validationStatus the container for the widget and error message.
     * @return the validationStatus if it has already been added to the list; null otherwise.
     */
    private ValidationStatus findValidationStatus(ValidationStatus validationStatus) {
        for (ValidationStatus container : validationErrorListEditor.getItems()) {
            if (container.equals(validationStatus)) {
                return container;
            }
        }

        return null;
    }

    /**
     * Makes sure the child widget is visible on the page. Opens any collapse
     * panels that contains the child widget.
     * 
     * @param childWidget the widget we want visible. Can't be null.
     * @param parentStopWidget the parent widget that indicates we should stop
     *        moving up the widget tree. Can't be null.
     * @param command the command that will fire when all animations have
     *        completed. Can't be null.
     */
    private void expandToWidget(final Widget childWidget, final Widget parentStopWidget, final Command command) {
        if (childWidget == null) {
            throw new IllegalArgumentException("The parameter 'childWidget' cannot be null.");
        } else if (parentStopWidget == null) {
            throw new IllegalArgumentException("The parameter 'parentStopWidget' cannot be null.");
        } else if (command == null) {
            throw new IllegalArgumentException("The parameter 'command' cannot be null.");
        }

        if (childWidget == parentStopWidget) {
            command.execute();
            return;
        }

        if (childWidget instanceof Collapse) {
            Collapse collapseWidget = (Collapse) childWidget;

            if (collapseWidget.isShown()) {
                // if location is already open, keep moving up the tree
                expandToWidget(childWidget.getParent(), parentStopWidget, command);
            } else {
                /* need to have an 'on complete' handler because collapse.show() returns before
                 * completing */
                final HandlerRegistration[] registrations = new HandlerRegistration[1];
                registrations[0] = collapseWidget.addShownHandler(new ShownHandler() {
                    @Override
                    public void onShown(ShownEvent event) {
                        registrations[0].removeHandler();
                        expandToWidget(childWidget.getParent(), parentStopWidget, command);
                    }
                });

                collapseWidget.show();
            }
        } else {

            if (childWidget.getParent() != null) {
                expandToWidget(childWidget.getParent(), parentStopWidget, command);

            } else {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine(
                            "expandToWidget() - childWidget contains no parent. Some widgets may not be directly attached to a parent widget, particularly if they are added directly through the DOM.");
                }

                /* some widgets may not be directly attached to a parent widget, particularly if
                 * they are added directly through the DOM */
                command.execute();
            }
        }
    }

    /**
     * Scrolls the element into view. Will perform the bare minimum amount of scrolling (e.g. will
     * appear at bottom).
     * 
     * The false flag prevents the object from scrolling to the top. We do not want this because if
     * the component is on the bottom of the page, the entire page UI will shift upward to make the
     * component display at the top of the page. The default is true, which is why we have to
     * override the scroll method.
     * 
     * @param element the element to scroll to.
     */
    private native void scrollIntoView(Element element) /*-{
		element.scrollIntoView(false);
    }-*/;
}
