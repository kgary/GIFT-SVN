/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import static mil.arl.gift.common.util.StringUtils.isNotBlank;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Strong;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.experiment.CourseCollection;
import mil.arl.gift.common.experiment.DataCollectionItem;
import mil.arl.gift.common.experiment.ExperimentUtil.DataCollectionUserRole;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.JsniUtility;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ManagedTooltip;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogConfirmWidget.ConfirmationDialogCallback;
import mil.arl.gift.tools.dashboard.client.bootstrap.BsDialogWidget.DialogCallback;
import mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection.EditCollectionProperties;
import mil.arl.gift.tools.dashboard.shared.rpcs.coursecollection.ReorderAction;

/**
 * A widget used to represent a course collection and update its data
 *
 * @author sharrison
 */
public class CourseCollectionWidget extends Composite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(CourseCollectionWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static CourseCollectionWidgetUiBinder uiBinder = GWT.create(CourseCollectionWidgetUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface CourseCollectionWidgetUiBinder extends UiBinder<Widget, CourseCollectionWidget> {
    }

    /** Placeholder text for a collection with no description */
    private final static String NO_DESCRIPTION = "<No description available>";

    /**
     * Create a remote service proxy to talk to the server-side dashboard
     * service
     */
    private final DashboardServiceAsync dashboardService = GWT.create(DashboardService.class);

    /** The main container panel that contains the contents of this widget */
    @UiField
    protected FlowPanel rootPanel;

    /** The header of the collapse panel */
    @UiField
    protected FocusPanel header;

    /** Icon used to indicate if the header is collapsed */
    @UiField
    protected Icon headerCollapseIcon;

    /** The header text */
    @UiField
    protected Label headerText;

    /** The button the user presses to edit the properties */
    @UiField
    protected Button editCollectionButton;

    /** The button the user presses to add to the collection */
    @UiField
    protected Button addNewCourseButton;

    /**
     * The button used to refresh the display of the metadata of this collection
     */
    @UiField
    protected Button refreshCollectionButton;

    /** The button the user presses to delete the collection */
    @UiField
    protected Button deleteCollectionButton;

    /** The panel that shows the content of the container */
    @UiField
    protected DeckPanel contentDeck;

    /** The empty panel to show when collapsed */
    @UiField
    protected SimplePanel noContentPanel;

    /** The non-empty panel to shown when expanded */
    @UiField
    protected FlowPanel contentPanel;

    /** Authored description for the collection */
    @UiField
    protected Paragraph descriptionText;

    /** The label for the collection URL */
    @UiField
    protected Label urlLabel;

    /**
     * The container for the {@link #experimentListPanel} or
     * {@link #noExperimentsLabel}
     */
    @UiField
    protected SimplePanel experimentSimplePanel;

    /** Contains the icons allowing the user to collapse */
    @UiField
    protected FocusPanel footerPanel;

    /** Icon used to indicate if the footer is collapsed */
    @UiField
    protected Icon footerCollapseIcon;

    /** The edit dialog to show to modify the collection's metadata */
    @UiField
    protected Modal editDialog;

    /** The text box used to edit the collection's name */
    @UiField
    protected TextBox editNameTextBox;

    /** The text box used to edit the collection's description */
    @UiField
    protected TextArea editDescriptionTextBox;

    /** The {@link CourseCollection} that is being represented */
    private CourseCollection collection;

    /** The parent widget of this collection */
    private final ExperimentToolWidget parent;

    /** The panel containing the collection's experiments */
    private FlowPanel experimentListPanel = new FlowPanel();

    /** The widget to show when the collection is empty */
    private Alert noExperimentsLabel = new Alert();

    /** The name of the user */
    private final String username = UiManager.getInstance().getUserName();

    /**
     * Constructor.
     *
     * @param collection the collection data used to populate this widget. Can't be null.
     * @param parent the parent of this widget. Can't be null.
     */
    public CourseCollectionWidget(CourseCollection collection, ExperimentToolWidget parent) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("CourseCollectionWidget(" + collection + ")");
        }

        if (collection == null) {
            throw new IllegalArgumentException("The parameter 'collection' cannot be null.");
        } else if (parent == null) {
            throw new IllegalArgumentException("The parameter 'parent' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));

        /* Initialize the data fields */
        this.collection = collection;
        this.parent = parent;

        setContentVisible(false);
        refreshUi();

        header.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                toggleContentVisible();
            }
        });

        footerPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                toggleContentVisible();
            }
        });
    }

    /**
     * Perform action on url copy button click
     *
     * @param event the click event
     */
    @UiHandler("copyUrlButton")
    protected void onCopyUrlButton(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onCopyUrlButton(" + event.toDebugString() + ")");
        }

        event.stopPropagation();

        JsniUtility.copyTextToClipboard(urlLabel.getElement());
    }

    /**
     * Perform action on edit collection button click
     *
     * @param event the click event
     */
    @UiHandler("editCollectionButton")
    protected void onEditCollectionButton(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onEditCollectionButton(" + event.toDebugString() + ")");
        }

        /* Prevent the event from bubbling up to the header and causing the
         * widget to collapse */
        event.stopPropagation();

        showEditDialog();
    }

    /**
     * Perform action on add new course button click
     *
     * @param event the click event
     */
    @UiHandler("addNewCourseButton")
    protected void onAddNewCourse(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onAddNewCourse(" + event.toDebugString() + ")");
        }

        /* Prevent the event from bubbling up to the header and causing the
         * widget to collapse */
        event.stopPropagation();

        parent.showCreateDialog(this);
    }

    /**
     * Perform action on refresh collection button click
     *
     * @param event the click event
     */
    @UiHandler("refreshCollectionButton")
    protected void onRefreshCollection(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onRefreshCollection(" + event.toDebugString() + ")");
        }

        event.stopPropagation();
        refreshData();
    }

    /**
     * Perform action on delete collection button click
     *
     * @param event the click event
     */
    @UiHandler("deleteCollectionButton")
    protected void onDeleteCollection(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onDeleteCollection(" + event.toDebugString() + ")");
        }

        /* Prevent the event from bubbling up to the header and causing the
         * widget to collapse */
        event.stopPropagation();

        /* Ask the user if they are sure they want to delete the collection */
        UiManager.getInstance().displayConfirmDialog("Delete Course Collection",
                "Are you sure you want to delete the course collection? <br/><br/><b>WARNING:</b> This will permanately delete this collection and all the contained experiments.",
                "Yes, delete the collection and all content", "No", new ConfirmationDialogCallback() {

                    @Override
                    public void onDecline() {
                        /* Nothing to do */
                    }

                    @Override
                    public void onAccept() {
                        // Delete the course and all its children courses
                        dashboardService.deleteCourseCollection(UiManager.getInstance().getUserName(), collection.getId(), new AsyncCallback<Void>() {

                            @Override
                            public void onSuccess(Void result) {
                                removeFromParent();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                /* Log the error details */
                                logger.log(Level.SEVERE,
                                        "There was a problem deleting the course collection " + collection);

                                /* Inform the user that there was an error */
                                UiManager.getInstance().displayErrorDialog("Delete Course Collection Error",
                                        "There was a problem deleting the course collection", null);
                            }
                        });
                    }
                });
    }

    /**
     * Populate the collection's children experiments
     */
    private void populateCourses() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateCourses()");
        }

        final DataCollectionUserRole collectionPermissionForUser = collection.getCollectionPermissionForUser(username);
        boolean isOwner = StringUtils.equals(collection.getOwner(), username);
        boolean isManager = collectionPermissionForUser == DataCollectionUserRole.MANAGER;

        experimentListPanel.clear();
        final List<DataCollectionItem> courses = collection.getCourses();

        final Style simplePanelStyle = experimentSimplePanel.getElement().getStyle();
        if (courses.isEmpty()) {
            experimentSimplePanel.setWidget(noExperimentsLabel);
            
            // allow clicking the panel to add a new experiment to the collection
            ClickHandler noExperimentsPanelClickHandler = new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    addNewCourseButton.click();
                }
            };
            experimentSimplePanel.getElement().getStyle().setCursor(Cursor.POINTER);
            experimentSimplePanel.addDomHandler(noExperimentsPanelClickHandler, ClickEvent.getType());
            simplePanelStyle.setMarginBottom(10, Unit.PX);
            simplePanelStyle.setTextAlign(TextAlign.CENTER);
            return;
        }

        experimentSimplePanel.setWidget(experimentListPanel);
        simplePanelStyle.setMarginBottom(0, Unit.PX);
        simplePanelStyle.setTextAlign(TextAlign.LEFT);

        for (int i = 0; i < courses.size(); i++) {
            DataCollectionItem course = courses.get(i);

            /** NOTE: if you change outerPanel type than you need to update expandPublishCourses method */
            final FlowPanel outerPanel = new FlowPanel();
            experimentListPanel.add(outerPanel);

            Style panelStyle = outerPanel.getElement().getStyle();
            panelStyle.setProperty("display", "flex");
            panelStyle.setProperty("flexFlow", "row nowrap");
            panelStyle.setProperty("alignItems", "center");
            panelStyle.setProperty("justifyContent", "center");

            if (isOwner || isManager) {
                final ButtonGroup moveButtons = new ButtonGroup();
                outerPanel.add(moveButtons);

                Button moveUpBtn = new Button("", IconType.CHEVRON_UP, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final int startIndex = experimentListPanel.getWidgetIndex(outerPanel);
                        final int moveToIndex = startIndex - 1;

                        swapCollectionItems(startIndex, moveToIndex);
                    }
                });
                moveUpBtn.setSize(ButtonSize.SMALL);
                moveUpBtn.setMarginBottom(15);
                ManagedTooltip moveUpTooltip = new ManagedTooltip(moveUpBtn,
                        "Move this experiment up in the collection");
                moveButtons.add(moveUpTooltip);
                if (i == 0) {
                    moveUpTooltip.setVisible(false);
                }

                Button moveDownBtn = new Button("", IconType.CHEVRON_DOWN, new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        final int startIndex = experimentListPanel.getWidgetIndex(outerPanel);
                        final int moveToIndex = startIndex + 1;

                        swapCollectionItems(startIndex, moveToIndex);
                    }
                });
                moveDownBtn.setSize(ButtonSize.SMALL);
                moveDownBtn.setMarginBottom(15);
                ManagedTooltip moveDownTooltip = new ManagedTooltip(moveDownBtn,
                        "Move this experiment down in the collection");
                moveButtons.add(moveDownTooltip);
                if (i == courses.size() - 1) {
                    moveDownTooltip.setVisible(false);
                }
            }

            final ExperimentWidget experimentWidget = new ExperimentWidget(course, parent);
            experimentWidget.getElement().getStyle().setProperty("flex", "1");
            experimentWidget.getElement().getStyle().setMarginTop(0, Unit.PX);
            experimentWidget.getElement().getStyle().setMarginLeft(8, Unit.PX);
            outerPanel.add(experimentWidget);
        }
    }

    /**
     * Swaps the two experiments within the collection
     *
     * @param startIndex the first experiment location to swap from
     * @param endIndex the second experiment location to swap to
     */
    private void swapCollectionItems(final int startIndex, final int endIndex) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(startIndex, endIndex);
            logger.fine("swapCollectionItems(" + StringUtils.join(", ", params) + ")");
        }

        /* Sanity check */
        final int widgetCount = experimentListPanel.getWidgetCount();
        if (startIndex == endIndex) {
            /* Same position; do nothing */
            return;
        }else        if (startIndex < 0 || startIndex >= widgetCount) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("moveDownBtn.onClick() - The start index [" + startIndex
                        + "] is out of bounds. Experiment size is " + widgetCount + ".");
            }
            return;
        } else if (endIndex < 0 || endIndex >= widgetCount) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("moveDownBtn.onClick() - The end index [" + endIndex
                        + "] is out of bounds. Experiment size is " + widgetCount + ".");
            }
            return;
        }

        final ReorderAction action = new ReorderAction(username, collection.getId(), startIndex, endIndex, collection.getCourses());
        dashboardService.updateCourseCollection(action, new AsyncCallback<GenericRpcResponse<DataCollectionItem>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<DataCollectionItem> result) {
                        if (logger.isLoggable(Level.FINE)) {
                            List<Object> params = Arrays.<Object>asList(result);
                            logger.fine("swapCollectionItems.updateCourseCollection.onSuccess(" + StringUtils.join(", ", params) + ")");
                        }

                        if (!result.getWasSuccessful()) {
                            UiManager.getInstance().displayDetailedErrorDialog("Failed to Reorder", result.getException());
                            refreshData();
                            return;
                        }

                        Collections.swap(collection.getCourses(), startIndex, endIndex);
                        /* The only thing in the container are flow panels */
                        FlowPanel panelAtStart = (FlowPanel) experimentListPanel.getWidget(startIndex);
                        FlowPanel panelAtEnd = (FlowPanel) experimentListPanel.getWidget(endIndex);

                        experimentListPanel.remove(panelAtStart);
                        experimentListPanel.insert(panelAtStart, endIndex);

                        /* Button group is always the first widget in the panel
                         * with an up and down in it respectively */
                        ButtonGroup buttonGroup;

                        /* Get the buttons from the first panel. Use the end
                         * index because that is where it was moved to. */
                        buttonGroup = (ButtonGroup) panelAtStart.getWidget(0);
                        /* Set up hidden if it's first; shown otherwise */
                        buttonGroup.getWidget(0).setVisible(endIndex != 0);
                        /* Set down hidden if it's last; shown otherwise */
                        buttonGroup.getWidget(1).setVisible(endIndex != widgetCount - 1);

                        /* Get the buttons from the second panel. Use the start
                         * index because it was swapped to that position. */
                        buttonGroup = (ButtonGroup) panelAtEnd.getWidget(0);
                        /* Set up hidden if it's first; shown otherwise */
                        buttonGroup.getWidget(0).setVisible(startIndex != 0);
                        /* Set down hidden if it's last; shown otherwise */
                        buttonGroup.getWidget(1).setVisible(startIndex != widgetCount - 1);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        if (logger.isLoggable(Level.FINE)) {
                            List<Object> params = Arrays.<Object>asList(caught);
                            logger.fine("swapCollectionItems.updateCourseCollection.onFailure(" + StringUtils.join(", ", params) + ")");
                        }

                        logger.severe("Unable to swap collection experiments because " + caught);

                        UiManager.getInstance().displayErrorDialog(
                                "Reorder Course Error",
                                "There was a problem on the server while reordering the collection",
                                new DialogCallback() {

                                    @Override
                                    public void onAccept() {
                                        refreshData();
                                    }
                                });
                    }
                });
    }

    /**
     * Shows the edit dialog for the course collection.
     */
    public void showEditDialog() {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList();
            logger.fine("showEditDialog(" + StringUtils.join(", ", params) + ")");
        }

        editNameTextBox.setValue(collection.getName());
        editDescriptionTextBox.setValue(collection.getDescription());

        editDialog.show();
    }

    /**
     * Perform action on save edits button click
     *
     * @param event the click event
     */
    @UiHandler("saveEditButton")
    protected void onSaveEditButton(ClickEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onSaveEditButton(" + event.toDebugString() + ")");
        }

        final String name = editNameTextBox.getValue();

        if (StringUtils.isBlank(name)) {
            UiManager.getInstance().displayErrorDialog("No Name Specified", "Please provide a name.", null);
            return;
        }

        final String description = editDescriptionTextBox.getValue();

        collection.setName(name);
        collection.setDescription(description);

        dashboardService.updateCourseCollection(
                new EditCollectionProperties(username, collection.getId(), name, description),
                new AsyncCallback<GenericRpcResponse<DataCollectionItem>>() {
                    @Override
                    public void onSuccess(GenericRpcResponse<DataCollectionItem> result) {
                        headerText.setText(name);
                        descriptionText.setText(
                                isNotBlank(collection.getDescription()) ? collection.getDescription() : NO_DESCRIPTION);
                        editDialog.hide();
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        collection.setName(headerText.getText());
                        collection.setDescription(descriptionText.getText());

                        UiManager.getInstance().displayErrorDialog("Failed to Edit the Collection",
                                "An error occurred while editing the collection.", null);
                    }
                });
    }

    /**
     * Fetches the latest data for the current {@link #collection} and then
     * refreshes the user interface.
     */
    private void refreshData() {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList();
            logger.fine("refreshData(" + StringUtils.join(", ", params) + ")");
        }

        dashboardService.getCourseCollection(collection.getId(), new AsyncCallback<CourseCollection>() {

            @Override
            public void onSuccess(CourseCollection result) {
                if (logger.isLoggable(Level.FINE)) {
                    List<Object> params = Arrays.<Object>asList(result);
                    logger.fine("refreshData.onSuccess(" + StringUtils.join(", ", params) + ")");
                }

                result.removeCoursesByVisibility(username);
                collection = result;
                refreshUi();
            }

            @Override
            public void onFailure(Throwable caught) {
                if (logger.isLoggable(Level.FINE)) {
                    List<Object> params = Arrays.<Object>asList(caught);
                    logger.fine("refreshData.onFailure(" + StringUtils.join(", ", params) + ")");
                }

                /* TODO CHUCK: This method is called as a result of an error
                 * dialog already being shown. Should we show another one? */
            }
        });
    }

    /**
     * Updates the user interface based on the current value of
     * {@link #collection}.
     */
    public void refreshUi() {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList();
            logger.fine("refreshUi(" + StringUtils.join(", ", params) + ")");
        }

        /* Initialize the URL text box */
        urlLabel.setText(collection.getUrl());

        /* Initialize the no experiment warning message */
        noExperimentsLabel.add(new Strong("Click to add to this collection."));
        noExperimentsLabel.setType(AlertType.INFO);

        /* Initialize the header */
        headerText.setText(collection.getName());

        refreshPermissions();

        final String description = collection.getDescription();
        descriptionText.setText(isNotBlank(description) ? description : NO_DESCRIPTION);

        populateCourses();
    }

    /**
     * Method that is called to update whether or not the controls tied to
     * various collection edits are enabled for the current user. Disables
     * controls when the user has no permission to use them.
     */
    private void refreshPermissions() {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList();
            logger.fine("refreshPermissions(" + StringUtils.join(", ", params) + ")");
        }

        final DataCollectionUserRole permission = collection
                .getCollectionPermissionForUser(UiManager.getInstance().getUserName());
        final boolean isOwner = StringUtils.equals(collection.getOwner(), username);
        final boolean isManager = permission == DataCollectionUserRole.MANAGER;

        editCollectionButton.setVisible(isOwner || isManager);
        deleteCollectionButton.setVisible(isOwner);
        addNewCourseButton.setVisible(isOwner);
    }

    /**
     * Gets whether or not this collection's content is showing
     * 
     * @return whether or not this collection's content is showing
     */
    private boolean isContentShowing() {
        return contentDeck.getVisibleWidget() == contentDeck.getWidgetIndex(contentPanel);
    }

    /**
     * Toggles whether or not this collection's content should be visible
     */
    private void toggleContentVisible() {
        setContentVisible(!isContentShowing());
    }

    /**
     * Sets whether or not this collection's content should be visible
     * 
     * @param visible true to show the content; false otherwise
     */
    public void setContentVisible(boolean visible) {
        if (visible) {
            contentDeck.showWidget(contentDeck.getWidgetIndex(contentPanel));

            headerCollapseIcon.setType(IconType.CHEVRON_CIRCLE_DOWN);
            footerCollapseIcon.setType(IconType.ANGLE_DOUBLE_UP);

        } else {
            contentDeck.showWidget(contentDeck.getWidgetIndex(noContentPanel));

            headerCollapseIcon.setType(IconType.CHEVRON_CIRCLE_RIGHT);
            footerCollapseIcon.setType(IconType.ANGLE_DOUBLE_DOWN);
        }
    }

    /**
     * Show any published courses that reference the course specified. This currently expand the panel(s) for any
     * matching published courses found.  
     * @param domainOption the course to search for in the published courses.  If null this method does nothing.
     * @param dataSetType optional data set type to ignore when searching published course types.  E.g. ignore
     * experiment published courses.
     */
    public void expandPublishCourses(DomainOption domainOption, DataSetType dataSetType){
        
        boolean expandedChild = false;
        for(int expIndex = 0; expIndex < experimentListPanel.getWidgetCount(); expIndex++){
            
            Widget expListWidget = experimentListPanel.getWidget(expIndex);
            if(expListWidget instanceof FlowPanel){
                FlowPanel panel = (FlowPanel)expListWidget;
                for(int expWidgetIndex = 0; expWidgetIndex < panel.getWidgetCount(); expWidgetIndex++){
                    Widget expInstanceWidget = panel.getWidget(expWidgetIndex);
                    if(expInstanceWidget instanceof ExperimentWidget){
                        
                        ExperimentWidget expWidget = (ExperimentWidget)expInstanceWidget;
                        expandedChild |= expWidget.expandPublishCourses(domainOption, dataSetType);
                    }
                }
            }
        }
        
        if(expandedChild){
            // at least one of this collection's child satisfied the search criteria, therefore this
            // collection also needs to be shown
            setContentVisible(true);
        }
    }

    /**
     * Return the collection that is represented by this widget.
     * 
     * @return the course collection. Can't be null.
     */
    public CourseCollection getCollection() {
        return collection;
    }
}