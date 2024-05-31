/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HeaderPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;

import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Actions.StateTransitions;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.LearnerAction;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Scenario;
import generated.dkf.Scenario.EndTriggers;
import generated.dkf.Strategy;
import generated.dkf.Task;
import generated.dkf.Tasks;
import generated.dkf.TeamOrganization;
import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.gwt.client.util.ScenarioElementUtil;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.JumpToEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.LearnerActionsEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.MiscellaneousEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.PlacesOfInterestEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.ScenarioEndTriggersEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor.TeamOrganizationEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy.StrategyEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.ConceptEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.ConditionEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.TaskEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.transition.StateTransitionEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ContextMenu;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;

/**
 * A widget used to create, manage, and display editors for course objects.
 * 
 * @author nroberts
 */
public class ScenarioObjectEditorPanel extends SimpleLayoutPanel implements ProvidesResize, RequiresResize {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioObjectEditorPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static ScenarioObjectEditorPanelUiBinder uiBinder = GWT.create(ScenarioObjectEditorPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface ScenarioObjectEditorPanelUiBinder extends UiBinder<Widget, ScenarioObjectEditorPanel> {
    }

    /**
     * An event binder used to allow this class to handle events on the shared message bus
     * 
     * @author nroberts
     */
    interface MyEventBinder extends EventBinder<ScenarioObjectEditorPanel> {
    }

    /** The event binder associated with this instance */
    private static final MyEventBinder eventBinder = GWT.create(MyEventBinder.class);

    /** The button containing the tabs that do not fit on the screen */
    @UiField
    protected Button tabButton;

    /** The deck panel containing the tab editors */
    @UiField
    protected DeckPanel editorDeck;

    /** The panel containing containing all the UI contents of this class */
    @UiField
    protected HeaderPanel mainPanel;

    /** The panel containing all the open tabs */
    @UiField
    protected HorizontalPanel tabsPanel;

    /** Loading indcator widget */
    @UiField
    protected Widget loadingIndicator;

    /** Text displayed during loading */
    @UiField
    protected HTML loadingText;

    /** Icon displayed during loading */
    @UiField
    protected BsLoadingIcon loadingIcon;

    /** The button used to launch GIFT Wrap */
    private Button giftWrapButton = new Button("Edit with GIFT Wrap");

    /** The panel containing the button used to launch GIFT Wrap */
    private FlowPanel giftWrapPanel = new FlowPanel();

    /** A mapping from each opened course object to the editor tab object */
    private Map<Serializable, EditorTab> objectToTabItem = new HashMap<>();

    /**
     * The context menu used to allow the user to select a course object to display from the list of
     * currently opened course objects
     */
    private ContextMenu tabMenu = new ContextMenu();

    /**
     * Gets the tabMenu that is shown only when there are more pinned tabs than can be displayed
     * within the tab panel's width
     * 
     * @return The {@link ContextMenu} which can't be null.
     */
    public ContextMenu getTabMenu() {
        return tabMenu;
    }

    /** The currently loaded course */
    private Scenario scenario = null;

    /**
     * Creates a new panel with no editors
     */
    public ScenarioObjectEditorPanel() {
        setWidget(uiBinder.createAndBindUi(this));

        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        tabButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                tabMenu.showAtCurrentMousePosition(event);
            }
        });

        giftWrapPanel.getElement().getStyle().setPadding(5, Unit.PX);
        giftWrapPanel.getElement().getStyle().setTextAlign(TextAlign.CENTER);
        giftWrapPanel.add(giftWrapButton);

        Window.addResizeHandler(new ResizeHandler() {

            @Override
            public void onResize(ResizeEvent arg0) {
                ScenarioObjectEditorPanel.this.onResize();
            }
        });
    }

    /**
     * Sets the scenario that this editor should handle scenario objects for
     * 
     * @param scenario the scenario that this editor should use
     */
    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    /**
     * Displays an editor modifying the given scenario object. If the given scenario object has not
     * been opened for editing before, then this method will also create an editor modifying the
     * scenario object. Otherwise, this method simply displays the editor that was already created
     * for this scenario object the first time it was shown.
     * 
     * @param scenarioObject the course object to edit
     */
    public void startEditing(final Serializable scenarioObject) {
        startEditing(scenarioObject, false, false);
    }

    /**
     * Displays an editor modifying the given scenario object. If the given scenario object has not
     * been opened for editing before, then this method will also create an editor modifying the
     * scenario object. Otherwise, this method simply displays the editor that was already created
     * for this scenario object the first time it was shown.
     * 
     * @param scenarioObject the course object to edit
     * @param editName whether the tab name should be focused for editing after creation
     * @param fromJump whether the scenario object is being edited as a result of a jump to action from another
     * part of the dkf editor. E.g. jumping from the strategy panel to a specific learner action.
     */
    public void startEditing(final Serializable scenarioObject, final boolean editName, final boolean fromJump) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("startEditing(" + scenarioObject + ", " + editName + ")");
        }

        if (scenarioObject == null) {
            logger.severe("Failed to load a scenario object for editing. The scenario object to edit is null.");
            throw new IllegalArgumentException("The parameter 'scenarioObject' cannot be null.");
        }

        if (scenarioObject instanceof Tasks || scenarioObject instanceof StateTransitions
                || scenarioObject instanceof InstructionalStrategies) {

            // TODO: Nick remove this block if we decide to implement help text
            // for the tasks, transitions, and strategies tree items
            logger.fine("No editor exists for this type of course object, so it will not be loaded.");
            return;
        }
        
        // when editing a sub object of a panel, make sure to get the appropriated tabbed editor
        // e.g. a learner action is edited using AvailableLearnerActions object on the LearnerActionsEditor 
        Serializable tabObject = scenarioObject;
        if(scenarioObject instanceof LearnerAction){
            tabObject = ScenarioClientUtility.getAvailableLearnerActions();
        }

        /* Get or create the tab used to edit the scenario object */
        EditorTab initialTab = objectToTabItem.get(tabObject);
        if (initialTab == null) {
            initialTab = new EditorTab(tabObject, scenario, this, !editName);
        }

        final EditorTab tab = initialTab;

        tab.setActive(true);

        if (tab.isCollapsed()) {

            // if this tab was collapsed, expand it
            removeFromTabMenu(tab);
            updateTabCount(!editName);
        }

        if (tabsPanel.getWidgetIndex(tab) == -1) {

            // add this object's tab to the editor panel if it hasn't been added
            // already
            tabsPanel.add(tab);
        }

        objectToTabItem.put(tabObject, tab);

        if (tab.getEditor() == null) {

            // get a list of tabs to unpin first to prevent concurrent
            // modification
            ArrayList<EditorTab> tabsToUnpin = new ArrayList<>();
            for (EditorTab unpinnedTab : objectToTabItem.values()) {
                if (!unpinnedTab.equals(EditorTab.getActiveTab()) && !unpinnedTab.isPinned()) {
                    tabsToUnpin.add(unpinnedTab);
                }
            }

            // stop editing any unpinned scenario objects
            for (EditorTab unpinnedTab : tabsToUnpin) {
                if (!unpinnedTab.equals(EditorTab.getActiveTab())) {
                    stopEditing(unpinnedTab.getScenarioObject(), false, false);
                }
            }
        }

        // notify the scenario presenter when a scenario object is being edited
        // so it can select it in the scenario outline
        // SharedResources.getInstance().getEventBus().fireEvent(new
        // CourseObjectOpenedForEditingEvent(scenarioObject));

        // allow the editor panel to be attached to the page before continuing
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

            @Override
            public void execute() {

                updateTabCount(!editName);

                // allow the loading indicator to be displayed before continuing
                loadingText.setText("Loading...");
                loadingIndicator.setVisible(true);

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                    @Override
                    public void execute() {

                        if (tab.getEditor() == null) {

                            if (scenarioObject instanceof Task) {

                                TaskEditor myEditor = new TaskEditor();
                                myEditor.edit((Task) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for Task object");

                            } else if (scenarioObject instanceof Concept) {

                                ConceptEditor myEditor = new ConceptEditor();
                                myEditor.edit((Concept) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for Concept object");

                            } else if (scenarioObject instanceof Condition) {

                                final Condition condition = (Condition) scenarioObject;
                                final String conditionImpl = condition.getConditionImpl();
                                final String oldName = tab.getText();

                                ConditionEditor myEditor = new ConditionEditor();
                                myEditor.edit(condition);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                if (StringUtils.isBlank(conditionImpl)) {
                                    SharedResources.getInstance().getEventBus()
                                            .fireEvent(new RenameScenarioObjectEvent(condition, oldName, "Unknown"));
                                } else if (ScenarioClientUtility.isConditionExcluded(condition) != null) {
                                    /* If the condition was excluded, try to
                                     * find some useful name to display other
                                     * than 'Unknown'. I've chosen to set the
                                     * class name. */
                                    String[] split = conditionImpl.split("\\.");
                                    SharedResources.getInstance().getEventBus().fireEvent(
                                            new RenameScenarioObjectEvent(condition, oldName, split[split.length - 1]));
                                } else {

                                    //
                                    // Retrieve the condition display name then fire a rename event
                                    // so the condition editor tab label will
                                    // be updated.
                                    //
                                    ScenarioClientUtility.getConditionInfoForConditionImpl(conditionImpl,
                                            new AsyncCallback<InteropsInfo.ConditionInfo>() {

                                                @Override
                                                public void onSuccess(InteropsInfo.ConditionInfo conditionInfo) {

                                                    if (conditionInfo != null && conditionInfo.getDisplayName() != null
                                                            && !conditionInfo.getDisplayName().isEmpty()) {
                                                        if (logger.isLoggable(Level.INFO)) {
                                                            logger.info(
                                                                    "Updating the name of the condition editor tab for '"
                                                                            + conditionImpl + "' to "
                                                                            + conditionInfo.getDisplayName());
                                                        }

                                                        SharedResources.getInstance().getEventBus()
                                                                .fireEvent(new RenameScenarioObjectEvent(condition,
                                                                        oldName, conditionInfo.getDisplayName()));
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Throwable thrown) {
                                                    logger.log(Level.SEVERE,
                                                            "The server failed to retrieve the condition information for the condition impl of '"
                                                                    + conditionImpl + "'.",
                                                            thrown);
                                                }
                                            });
                                }

                                logger.info("Finished creating editor for Task object");

                            } else if (scenarioObject instanceof StateTransition) {

                                StateTransitionEditor myEditor = new StateTransitionEditor();
                                myEditor.edit((StateTransition) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for State Transition object");

                            } else if (scenarioObject instanceof Strategy) {

                                StrategyEditor myEditor = new StrategyEditor();
                                myEditor.edit((Strategy) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for State Transition object");

                            } else if (scenarioObject instanceof PlacesOfInterest) {

                                PlacesOfInterestEditor myEditor = new PlacesOfInterestEditor();
                                myEditor.edit((PlacesOfInterest) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for places of interest object");

                            } else if (scenarioObject instanceof AvailableLearnerActions) {

                                LearnerActionsEditor myEditor = new LearnerActionsEditor();
                                myEditor.edit((AvailableLearnerActions) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for AvailableLearnerActions object");
                                
                            } else if (scenarioObject instanceof LearnerAction){
                                
                                // trying to jump to a specific learner action
                                LearnerActionsEditor myEditor = new LearnerActionsEditor();
                                myEditor.editObject((LearnerAction) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for AvailableLearnerActions object on LearnerAction");

                            } else if (scenarioObject instanceof EndTriggers) {

                                ScenarioEndTriggersEditor myEditor = new ScenarioEndTriggersEditor();
                                myEditor.edit((EndTriggers) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for EndTriggers object");
                                
                            } else if (scenarioObject instanceof TeamOrganization) {

                                TeamOrganizationEditor myEditor = new TeamOrganizationEditor();
                                myEditor.edit((TeamOrganization) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for TeamOrganization object");

                            } else if (scenarioObject instanceof Scenario) {

                                MiscellaneousEditor myEditor = new MiscellaneousEditor();
                                myEditor.edit((Scenario) scenarioObject);
                                myEditor.setSize("100%", "100%");

                                tab.setEditor(myEditor);

                                logger.info("Finished creating editor for MiscellaneousEditor object");
                            }

                            if (tab.getEditor() != null) {

                                // add the editor to this widget
                                editorDeck.add(tab.getEditor());

                            } else {

                                logger.severe("Failed to load a scenario object for editing. The class '"
                                        + scenarioObject.getClass().getName()
                                        + "' does not have an accompanying editor.");

                                return;
                            }
                        }else if(fromJump){
                            // the editor tab is already opened and pinned
                            // set any sub-object that needs editing
                            
                            if (tab.getEditor() instanceof LearnerActionsEditor &&
                                    scenarioObject instanceof LearnerAction){
                            
                                // trying to jump to a specific learner action
                                LearnerActionsEditor myEditor = (LearnerActionsEditor) tab.getEditor();
                                myEditor.editObject((LearnerAction) scenarioObject);

                                logger.info("Finished creating editor for AvailableLearnerActions object on LearnerAction");
                            }
                        }

                        /* add the editor to the editor deck if it isn't there already */
                        if (editorDeck.getWidgetIndex(tab.getEditor()) == -1) {
                            editorDeck.add(tab.getEditor());
                        }

                        // show the editor for this scenario object
                        editorDeck.showWidget(editorDeck.getWidgetIndex(tab.getEditor()));

                        if (tab.getEditor() instanceof RequiresResize) {

                            // resize the editor once it is shown, if necessary
                            ((RequiresResize) tab.getEditor()).onResize();
                        }

                        loadingIndicator.setVisible(false);

                        /* Set focus on and edit the name label if this is a new scenario object */
                        if (editName && !(scenarioObject instanceof Condition)) {
                            tab.nameLabel.startEditing();
                        }
                    }
                });
            }
        });
    }

    /**
     * Removes the editor modifying the given scenario object. If other scenario objects have been
     * opened for editing, then this method will switch to the editor for the next available
     * scenario object. Otherwise, this editor will remove itself from its parent once no more
     * scenario objects are opened for editing.
     * 
     * @param scenarioObject the scenario object whose editor should be removed
     */
    public void stopEditing(Serializable scenarioObject) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("stopEditing(" + scenarioObject + ")");
        }

        stopEditing(scenarioObject, true, false);
    }

    /**
     * Removes the editor modifying the given scenario object, and optionally starts editing the
     * next available scenario object if one exists. If no other scenario objects are available or
     * if editing the next scenario object is disabled, this editor will simply remove itself from
     * its parent.
     * 
     * @param scenarioObject the scenario object whose editor should be removed
     * @param shouldEditNext whether or not the next available scenario object should be opened
     * @param closeIfPinned true to close the tab even if it is pinned; false to only close the tab
     *        if it is unpinned.
     */
    public void stopEditing(Serializable scenarioObject, boolean shouldEditNext, boolean closeIfPinned) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("stopEditing(" + scenarioObject + ", " + shouldEditNext + ")");
        }

        EditorTab tab = objectToTabItem.get(scenarioObject);

        if (tab != null) {

            tab.setActive(false);

            AbstractCourseObjectEditor<?> editor = tab.getEditor();

            if (editor != null) {

                // stop any presenter this scenario object's editor may be using
                logger.info("Stop editing, called stopEditing() on current editor");
                editor.stopEditing();

                if (editorDeck.getWidgetCount() > 1 && shouldEditNext) {

                    /* if there are more scenario objects open for editing, find the next available
                     * one */
                    int index = editorDeck.getWidgetIndex(editor);

                    Serializable nextObject = null;

                    if (index < editorDeck.getWidgetCount() - 1) {

                        Widget widget = editorDeck.getWidget(index + 1);

                        if (widget instanceof AbstractCourseObjectEditor<?>) {
                            nextObject = ((AbstractCourseObjectEditor<?>) widget).getCourseObject();
                        }

                    } else {

                        Widget widget = editorDeck.getWidget(index - 1);

                        if (widget instanceof AbstractCourseObjectEditor<?>) {
                            nextObject = ((AbstractCourseObjectEditor<?>) widget).getCourseObject();
                        }
                    }

                    // start editing the next available scenario object
                    ScenarioEventUtility.fireJumpToEvent(nextObject);

                    // remove the editor for the scenario object being removed
                    editorDeck.remove(editor);

                } else {

                    if (shouldEditNext) {
                        ScenarioEventUtility.fireJumpToEvent(null);
                    }

                    // remove the editor for the scenario object being removed
                    editorDeck.remove(editor);
                }
            }

            // if the tab is not pinned, remove it from the editor
            if (closeIfPinned || !tab.isPinned()) {
                objectToTabItem.remove(scenarioObject);
                tabsPanel.remove(tab);
                removeFromTabMenu(tab);
            }
        }

        updateTabCount(true);
    }

    @Override
    public void onResize() {
        super.onResize();
        updateTabCount(true);
    }

    /**
     * Removes a tab from the editor panel and collapses it to the tab menu
     * 
     * @param tab - the tab to be collapsed
     * @param fireEvents - whether events should be fired
     * @throws IllegalArgumentException - if the tab is null
     */
    private void addToTabMenu(EditorTab tab, boolean fireEvents) {

        if (tab == null) {
            throw new IllegalArgumentException("tab cannot be null");
        }

        tab.setActive(false);
        tab.setVisible(false);
        tab.setCollapsed(true);

        if (tab.getMenuItem() != null) {

            if (tabMenu.getMenu().getItemIndex(tab.getMenuItem()) == -1) {

                // add this tab if the tab menu does not contain it
                tabMenu.getMenu().addItem(tab.getMenuItem());
                EditorTab.setTabsCollapsed(EditorTab.getTabsCollapsed() + 1);
            }

        } else {
            logger.warning("tab menuItem was null");
        }
    }

    /**
     * Removes a tab from the tab menu and displays it on the editor panel
     * 
     * @param tab - tab to be removed from the tab menu
     * @throws IllegalArgumentException - if the tab is null
     */
    private void removeFromTabMenu(EditorTab tab) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeFromTabMenu(" + tab + ")");
        }

        if (tab == null) {
            throw new IllegalArgumentException("tab cannot be null");
        }

        tab.setVisible(true);
        tab.setCollapsed(false);

        if (tab.getMenuItem() != null) {

            if (tabMenu.getMenu().getItemIndex(tab.getMenuItem()) != -1) {

                // remove this tab if the tab menu contains it
                tabMenu.getMenu().removeItem(tab.getMenuItem());
                EditorTab.setTabsCollapsed(EditorTab.getTabsCollapsed() - 1);
            }

        } else {
            logger.warning("tab menuItem was null");
        }
    }

    /**
     * Updates the counter for the button used to open the tab menu and displays tabs based on space
     * available
     * 
     * @param fireEvents - whether events should be fired
     */
    private void updateTabCount(boolean fireEvents) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateTabCount()");
        }

        int tabCount = objectToTabItem.size();
        int tabsInMenu = EditorTab.getTabsCollapsed();

        // Calculate tabRoom based on space available
        int offsetWidth = editorDeck.getOffsetWidth() - 6; // 3px padding on each side
        // assume 244 tab width
        int tabWidth = (EditorTab.getActiveTab() != null ? EditorTab.getActiveTab().getOffsetWidth() : 244);
        int tabButtonWidth = tabButton.getOffsetWidth();
        int tabRoom = offsetWidth / tabWidth;
        if (tabCount > tabRoom && tabCount > 1) {
            tabRoom = (offsetWidth - tabButtonWidth) / tabWidth;
        }

        int displayedTabs = tabCount - tabsInMenu;

        // if we need to add a tab...
        if (displayedTabs < tabRoom && displayedTabs < tabCount) {

            // display the active tab if it is not already, or the next
            // available tab
            EditorTab tabToDisplay = null;

            if (EditorTab.getActiveTab().isCollapsed()) {
                tabToDisplay = EditorTab.getActiveTab();
            } else {
                for (int i = 0; i < tabsPanel.getWidgetCount(); i++) {
                    EditorTab tab = (EditorTab) tabsPanel.getWidget(i);
                    if (!tab.equals(EditorTab.getActiveTab()) && tab.isCollapsed()) {
                        tabToDisplay = tab;
                        break;
                    }
                }
            }

            if (tabToDisplay != null) {
                removeFromTabMenu(tabToDisplay);
                updateTabCount(fireEvents);
            }

        } else if (displayedTabs > tabRoom && displayedTabs > 1) {
            // if we need to remove a tab
            EditorTab tabToHide = null;

            for (int i = tabsPanel.getWidgetCount() - 1; i >= 0; i--) {
                EditorTab tab = (EditorTab) tabsPanel.getWidget(i);
                if (!tab.equals(EditorTab.getActiveTab()) && !tab.isCollapsed()) {
                    tabToHide = tab;
                    break;
                }
            }

            if (tabToHide != null) {
                addToTabMenu(tabToHide, fireEvents);
                updateTabCount(fireEvents);
            }
        }

        if (tabCount <= tabRoom || EditorTab.getTabsCollapsed() == 0) {
            tabButton.setVisible(false);
        } else {
            tabButton.setVisible(true);
        }

        tabButton.setText(Integer.toString(EditorTab.getTabsCollapsed()));
    }

    /**
     * Stops editing all the scenario object that have been opened for editing by this widget and
     * returns this widget to its initial state. Only display information, such as the current
     * orientation, will be retained after invoking this method.
     */
    public void stopAllEditing() {

        Set<EditorTab> toClose = new HashSet<EditorTab>(objectToTabItem.values());

        for (EditorTab tab : toClose) {
            stopEditing(tab.getScenarioObject());
        }
    }

    /**
     * Handles routing an event generated from within the Scenario Editor to all the open tabs so
     * they can properly update their UI.
     * 
     * @param event The {@link ScenarioEditorEvent} to route to all open tabs.
     */
    public void dispatchEvent(ScenarioEditorEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("dispatchEvent(" + event + ")");
        }

        /* Let each of the visible tabs handle the event if necessary. This must happen before we
         * handle the event locally. */
        for (EditorTab tab : objectToTabItem.values()) {
            if (tab.getEditor() != null) {
                tab.getEditor().handleEvent(event);
            }
        }

        /* Handle the event for each type */
        if (event instanceof CreateScenarioObjectEvent) {
            handleCreate((CreateScenarioObjectEvent) event);
        } else if (event instanceof DeleteScenarioObjectEvent) {
            handleDelete((DeleteScenarioObjectEvent) event);
        } else if (event instanceof RenameScenarioObjectEvent) {
            handleRename((RenameScenarioObjectEvent) event);
        } else if (event instanceof JumpToEvent) {
            handleJump((JumpToEvent) event);
        }
    }

    /**
     * Show the appropriate EditorTab and then make the name editable
     * 
     * @param event The event containing the details about the scenario object that was created.
     */
    private void handleCreate(CreateScenarioObjectEvent event) {
        if (ScenarioElementUtil.isObjectAnAssessmentObject(event.getScenarioObject())) {
            startEditing(event.getScenarioObject(), true, false);
        }
    }

    /**
     * Removes the {@link EditorTab} coresponding to a scenario object that was just deleted.
     * 
     * @param event The event that contains information concerning the scenario object that was
     *        deleted.
     */
    private void handleDelete(DeleteScenarioObjectEvent event) {
        if (ScenarioElementUtil.isObjectAnAssessmentObject(event.getScenarioObject())) {
            final EditorTab activeTab = EditorTab.getActiveTab();
            boolean isObjectActive = activeTab != null && activeTab.getScenarioObject() == event.getScenarioObject();
            stopEditing(event.getScenarioObject(), isObjectActive, true);
        }
    }

    /**
     * Notify the visible tabs and editors that the rename has occurred.
     * 
     * @param event The event that contains information concerning the scenario object that is being
     *        renamed.
     */
    private void handleRename(RenameScenarioObjectEvent event) {
        if (ScenarioElementUtil.isObjectAnAssessmentObject(event.getScenarioObject())) {
            for (EditorTab tab : objectToTabItem.values()) {
                tab.handleRename(event);
            }
        }
    }

    /**
     * Show the EditorTab for the requested scenario object
     * 
     * @param event The event that contains information concerning the scenario object that is being
     *        shown.
     */
    private void handleJump(JumpToEvent event) {
        if (event.isPinTabOnJump()) {
            EditorTab activeTab = EditorTab.getActiveTab();
            if (activeTab != null) {
                activeTab.setPinned(true);
            }
        }
        
        if(event.getChildScenarioObject() != null){
            startEditing(event.getChildScenarioObject(), false, true);
        }else{
            startEditing(event.getScenarioObject());
        }
    }
}