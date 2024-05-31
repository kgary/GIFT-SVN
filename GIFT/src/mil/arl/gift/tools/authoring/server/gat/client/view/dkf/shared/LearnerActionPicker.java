/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.HasDirection.Direction;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.LearnerAction;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EnforcedButton;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * A widget that is used to select an existing {@link LearnerAction} within the
 * {@link Scenario}.
 * 
 * @author tflowers
 *
 */
public class LearnerActionPicker extends ScenarioValidationComposite implements HasValue<String> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LearnerActionPicker.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static final LearnerActionPickerUiBinder uiBinder = GWT.create(LearnerActionPickerUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface LearnerActionPickerUiBinder extends UiBinder<Widget, LearnerActionPicker> {
    }
    
    /** The binder that routes events on the bus appropriate methods */
    private static final LearnerActionPickerEventBinder eventBinder = GWT.create(LearnerActionPickerEventBinder.class);

    /** Defines the binder for routing events to methods */
    interface LearnerActionPickerEventBinder extends EventBinder<LearnerActionPicker> {
    }

    /** A text box used to search for learner actions to select */
    @UiField
    protected TextBox learnerActionSelect;

    /** The button used to jump to the selected task/concept page */
    @UiField(provided = true)
    protected EnforcedButton nodeJumpButton = new EnforcedButton(IconType.EXTERNAL_LINK, "", "Navigates to the learner actions page",
            new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    
                    ScenarioEventUtility.fireJumpToLearnerActions();
                    
                    /* hide the learner action selector so that it doesn't block
                     * the editor that the author jumps to */
                    LearnerActionSelectionPanel.hideSelector();
                }
            });
    
    /** The collapse containing a panel for selecting a {@link LearnerAction} */
    @UiField
    protected Collapse selectorPanel;
    
    /** The label that is used to annotate the picker field */
    @UiField
    protected HTML label;

    /**
     * Validation container for when the {@link LearnerActionPicker} has no
     * learner action selected.
     */
    private final WidgetValidationStatus learnerActionSelectedValidation;
    
    /**
     * The last value that was entered. Used to revert changes when the author
     * enters an invalid learner action reference.
     */
    private String lastValue = null;
    
    /** The event binder registration */
    private com.google.web.bindery.event.shared.HandlerRegistration eventBinderRegistration;

    /**
     * Creates a new learner action picker and initializes its event handling
     * logic
     */
    public LearnerActionPicker() {
        initWidget(uiBinder.createAndBindUi(this));
        eventBinderRegistration = eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        learnerActionSelectedValidation = new WidgetValidationStatus(learnerActionSelect,
                "You must have a learner action selected. Please select a learner action. If none exist, you need to create one.");

        addAttachHandler(new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                final boolean attached = event.isAttached();
                final boolean hasHandler = eventBinderRegistration != null;

                /* add the event binder to this picker if it is attached and does not have the event
                 * binder already */
                if (attached && !hasHandler) {
                    eventBinderRegistration = eventBinder.bindEventHandlers(LearnerActionPicker.this,
                            SharedResources.getInstance().getEventBus());
                } else if (!attached && hasHandler) {
                    /* remove the event binder from this picker if it is detached */
                    eventBinderRegistration.removeHandler();
                }
            }
        });
        
        learnerActionSelect.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                
                /* show the learner action selection dropdown whenever the
                 * search box is selected */
                LearnerActionSelectionPanel.showSelector(LearnerActionPicker.this);
            }
        });
        learnerActionSelect.addDomHandler(new InputHandler() {
            
            @Override
            public void onInput(InputEvent event) {
                
                //update the filter for the selection dropdown
                LearnerActionSelectionPanel.loadAndFilterLearnerActions();
            }
            
        }, InputEvent.getType());
        learnerActionSelect.addFocusHandler(new FocusHandler() {
            
            @Override
            public void onFocus(FocusEvent event) {
                
                //select all of the search box's text when it gains focus so that it's easier for the author to clear out
                learnerActionSelect.selectAll();
            }
        });
    }

    /**
     * Handles the selection changes for the learner actions.
     * 
     * @param event the value change event containing the new selection
     */
    @UiHandler("learnerActionSelect")
    protected void onLearnerActionSelectChanged(ValueChangeEvent<String> event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onLearnerActionSelectChanged.onValueChange(" + event.getValue() + ")");
        }

        LearnerAction referencedAction = ScenarioClientUtility.getLearnerActionWithName(event.getValue());
        
        if(referencedAction != null) {
            
            /* the author has chosen a valid learner action, so update this
             * widget's value */
            setValue(event.getValue(), true);
            
        } else {
            
            /* the author has not chosen a valid learner action, so revert to
             * the last entered value */
            learnerActionSelect.setValue(lastValue);
        }
    }

    /**
     * Updates the learner action name on the learner action picker
     * 
     * @param event an event indicating that a learner action has been renamed
     */
    @EventHandler
    protected void onLearnerActionRenamedEvent(RenameScenarioObjectEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onLearnerActionRenamedEvent(" + event + ")");
        }

        if(event.getScenarioObject() instanceof LearnerAction) {
            String oldName = event.getOldName();
            String newName = event.getNewName();
            if (StringUtils.isNotBlank(newName) && StringUtils.equals(oldName, getValue())) {
                setValue(newName);
            }
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return this.addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public String getValue() {
        return learnerActionSelect.getValue();
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        
        lastValue = value;
        
        learnerActionSelect.setValue(StringUtils.isBlank(value) ? null : value);
        
        requestValidation(learnerActionSelectedValidation);

        if (fireEvents) {
            ValueChangeEvent.fire(this, getValue());
        }
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(learnerActionSelectedValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (learnerActionSelectedValidation.equals(validationStatus)) {
            final String selectedValue = getValue();
            if (StringUtils.isBlank(selectedValue)) {
                learnerActionSelectedValidation.setInvalid();
                return;
            }

            // must reference a learner action
            boolean foundLearnerAction = false;
            for (LearnerAction actions : ScenarioClientUtility.getUnmodifiableLearnerActionList()) {
                if (StringUtils.equals(actions.getDisplayName(), selectedValue)) {
                    foundLearnerAction = true;
                    break;
                }
            }

            learnerActionSelectedValidation.setValidity(foundLearnerAction);
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(LearnerActionSelectionPanel.getListValidation());
    }

    /**
     * Sets the components to read only mode which prevents users from making changes.
     * 
     * @param isReadonly True to prevent editing, false to allow editing.
     */
    public void setReadonly(boolean isReadonly) {
        learnerActionSelect.setEnabled(!isReadonly);
    }
    
    /**
     * Gets the text box used to enter search text
     * 
     * @return the search text box
     */
    ValueBoxBase<String> getTextBox() {
        return learnerActionSelect;
    }
    
    /**
     * Gets the collapseable panel that the selector should be placed in when it is shown
     * 
     * @return the panel where the selector should be placed
     */
    Collapse getSelectorPanel() {
        return selectorPanel;
    }
    
    /**
     * Sets the label text for this learner action picker
     * 
     * @param text the plain text to use as the label
     */
    public void setLabel(String text) {
        label.setText(text);
    }
    
    /**
     * Sets the label text for this learner action picker and renders it as HTML
     * 
     * @param html the HTML to use as the label
     */
    public void setLabel(SafeHtml html) {
        label.setHTML(html, Direction.LTR);
    }
}
