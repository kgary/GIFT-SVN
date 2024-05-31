/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.TabShowEvent;
import org.gwtbootstrap3.client.shared.event.TabShowHandler;
import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.Tooltip;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions;
import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Actions.StateTransitions;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Assessment;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.CheckpointPaceCondition;
import generated.dkf.CheckpointProgressCondition;
import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.CorridorBoundaryCondition;
import generated.dkf.CorridorPostureCondition;
import generated.dkf.EnterAreaCondition;
import generated.dkf.IdentifyPOIsCondition;
import generated.dkf.LearnerAction;
import generated.dkf.LifeformTargetAccuracyCondition;
import generated.dkf.Objects;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Resources;
import generated.dkf.RulesOfEngagementCondition;
import generated.dkf.Scenario;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Scenario.EndTriggers;
import generated.dkf.Strategy;
import generated.dkf.Task;
import generated.dkf.Tasks;
import generated.dkf.Team;
import generated.dkf.TeamMember;
import generated.dkf.TeamOrganization;
import mil.arl.gift.common.gwt.client.widgets.DynamicHeaderScrollPanel;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility.ConceptNodeRef;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.JumpToEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.LearnerStartLocationUpdatedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PopulateScenarioTreesEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.ConceptTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.ConditionTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.LearnerActionsTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.MiscellaneousTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.ScenarioEndTriggersTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.ScenarioObjectTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.StateTransitionTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.StrategyTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.TaskTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.TeamOrganizationTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.tree.WaypointsTreeItem;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ContextMenu;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ImportCourseConceptDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ImportCourseConceptDialog.ImportHandler;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that displays the overall structure of a {@link Scenario} using an interactive tree that is used to 
 * add, edit, rename, delete, and reorder a scenario's {@link Task tasks}, {@link Concept concepts}, {@link Condition conditions}, 
 * {@link StateTransition state transitions}, and {@link InstructionalIntervention strategies}.
 * 
 * @author nroberts
 */
public class ScenarioOutlineEditor extends AbstractScenarioObjectEditor<Scenario> implements HasSelectionHandlers<Serializable> {
    
    /** The logger for the class */
    private static Logger logger = Logger.getLogger(ScenarioOutlineEditor.class.getName());

    /** The UiBinder that combines this java class with the ui.xml */
    private static ScenarioOutlineEditorUiBinder uiBinder = GWT.create(ScenarioOutlineEditorUiBinder.class);

    /** Defines the UiBinder used to combine the java class with the ui.xml */
    interface ScenarioOutlineEditorUiBinder extends UiBinder<Widget, ScenarioOutlineEditor> {
    }

    /** The tool tip for the add button when adding a new Task */
    private static final String ADD_TASK_TOOLTIP = "Add New Task";

    /** The tool tip for the add button when adding a new Strategy */
    private static final String ADD_STRATEGY_TOOLTIP = "Add New Strategy";

    /** The tool tip for the add button when adding a new State Transition */
    private static final String ADD_STATE_TRANSITION_TOOLTIP = "Add New State Transition";
    
    /** The tree of {@link Task tasks} */
    @UiField(provided=true)
    protected Tree tasksTree = new Tree() {
        
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            return false; //prevent keyboard navigation so that arrow keys and special characters do not change selection
        }
    };

    /** The tree of {@link Strategy strategies} */
    @UiField(provided = true)
    protected Tree strategiesTree = new Tree() {
        
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            return false; //prevent keyboard navigation so that arrow keys and special characters do not change selection
        }
    };

    /** The tree of {@link StateTransition state transitions} */
    @UiField(provided=true)
    protected Tree transitionsTree = new Tree() {
        
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            return false; //prevent keyboard navigation so that arrow keys and special characters do not change selection
        }
    };

    /** The tree for containing scenario properties */
    @UiField(provided=true)
    protected Tree propertiesTree = new Tree() {
        
        @Override
        protected boolean isKeyboardNavigationEnabled(TreeItem currentItem) {
            return false; //prevent keyboard navigation so that arrow keys and special characters do not change selection
        }
    };

    /** The tab that contains the {@link #tasksTree} */
    @UiField
    protected TabListItem tasksTab;

    /** The tab that contains the {@link #strategiesTree} */
    @UiField
    protected TabListItem strategiesTab;

    /** The tab that contains the {@link #transitionsTree} */
    @UiField
    protected TabListItem transitionsTab;

    /** The tab that contains the {@link #propertiesTree} */
    @UiField
    protected TabListItem propertiesTab;
    
    /** The add button to create new scenario objects for the current tree/list displayed in the outline */
    @UiField
    protected TabListItem addButton;
    
    /** The tooltip for the {@link #addButton} */
    @UiField
    protected Tooltip addTooltip;
    
    /** The area within the outline tab */
    @UiField
    protected Widget treeArea;
    
    /** The widget below the tree area */
    @UiField
    protected Widget belowTreeArea;
    
    /** The panel that contains the various tabbed content */
    @UiField
    protected DynamicHeaderScrollPanel scenarioHeaderScrollPanel;

    /** The last scenario object that was selected */
    private ScenarioObjectTreeItem<?> lastSelectedItem = null;
    
    /** A context menu that lets the author pick whether they want to add a task or import a course concept */
    private ContextMenu addObjectMenu = new ContextMenu();

    /** Handles when a {@link TreeItem} is clicked within any {@link Tree} */
    private final SelectionHandler<TreeItem> treeSelectionHandler = new SelectionHandler<TreeItem>() {
    
        @Override
        public void onSelection(SelectionEvent<TreeItem> event) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("treeSelectionHandler.onSelection(" + event.getSelectedItem() + ")");
            }
            
            if (event.getSelectedItem() instanceof ScenarioObjectTreeItem) {
                ScenarioObjectTreeItem<?> treeItem = (ScenarioObjectTreeItem<?>) event.getSelectedItem();
                Serializable selectedObject = treeItem.getScenarioObject();
                

                if (lastSelectedItem == null || lastSelectedItem.getScenarioObject() != selectedObject) {
                    updateSelectedItemState(treeItem);

                    /* If the selected object is different than the object
                     * selected beforehand, notify this widget's listeners */
                    SelectionEvent.fire(ScenarioOutlineEditor.this, selectedObject);
                }
            } else if (event.getSelectedItem() == null && lastSelectedItem != null) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.info("Selected object: null");
                }

                /* If the selected object is different than the object
                 * selected beforehand, notify this widget's listeners */
                SelectionEvent.fire(ScenarioOutlineEditor.this, null);
            }
        }
    };

    /**
     * Creates a new scenario outline editor and sets up its event handlers
     */
    public ScenarioOutlineEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ScenarioOutlineEditor()");
        }

        setWidget(uiBinder.createAndBindUi(this));

        tasksTree.addSelectionHandler(treeSelectionHandler);
        transitionsTree.addSelectionHandler(treeSelectionHandler);
        strategiesTree.addSelectionHandler(treeSelectionHandler);
        propertiesTree.addSelectionHandler(treeSelectionHandler);
        final boolean isReadOnly = ScenarioClientUtility.isReadOnly();
        addButton.setVisible(!isReadOnly);

        tasksTab.addShowHandler(new TabShowHandler() {
            
            @Override
            public void onShow(TabShowEvent event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("tasksTab.onShow()");
                }
                addButton.setVisible(!isReadOnly);
                addTooltip.setTitle(ADD_TASK_TOOLTIP);
                scenarioHeaderScrollPanel.scrollToBegin();  // reset horizontal scroll to left most position
            }
        });
        strategiesTab.addShowHandler(new TabShowHandler() {

            @Override
            public void onShow(TabShowEvent event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("strategiesTab.onShow()");
                }
                addButton.setVisible(!isReadOnly);
                addTooltip.setTitle(ADD_STRATEGY_TOOLTIP);
                scenarioHeaderScrollPanel.scrollToBegin(); // reset horizontal scroll to left most position
            }
        });
        transitionsTab.addShowHandler(new TabShowHandler() {

            @Override
            public void onShow(TabShowEvent event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("transitionsTab.onShow()");
                }
                addButton.setVisible(!isReadOnly);
                addTooltip.setTitle(ADD_STATE_TRANSITION_TOOLTIP);
                scenarioHeaderScrollPanel.scrollToBegin(); // reset horizontal scroll to left most position
            }
        });
        propertiesTab.addShowHandler(new TabShowHandler() {

            @Override
            public void onShow(TabShowEvent event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("propertiesTab.onShow()");
                }
                addButton.setVisible(false);
                scenarioHeaderScrollPanel.scrollToBegin(); // reset horizontal scroll to left most position
            }
        });

        addButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Add button clicked.");
                }
                
                if (tasksTab.isActive()) {
                    addObjectMenu.showAtCurrentMousePosition(event);

                } else if (strategiesTab.isActive()) {
                    ScenarioEventUtility.fireCreateScenarioObjectEvent(ScenarioClientUtility.generateNewStrategy());

                } else if (transitionsTab.isActive()) {
                    ScenarioEventUtility
                            .fireCreateScenarioObjectEvent(ScenarioClientUtility.generateNewStateTransition());
                }

                event.stopPropagation();
                event.preventDefault();
            }
            
        });
        
        belowTreeArea.addDomHandler(new MouseOverHandler() {
            
            @Override
            public void onMouseOver(MouseOverEvent event) {
                
                ScenarioObjectTreeItem<?> dragged = ScenarioObjectTreeItem.getItemBeingDragged();
                
                if(dragged != null) {
                    
                    if(!treeArea.getElement().hasClassName("scenarioTreeDropOut")) {
                        
                        //place a black horizontal line after the tree when the area below it is dragged over
                        treeArea.addStyleName("scenarioTreeDropOut");
                    }
                    
                    if(dragged instanceof ConceptTreeItem || dragged instanceof ConditionTreeItem) {
                        
                        //indicate to users that concepts and conditions cannot be added to the end of the task tree
                        if(!belowTreeArea.getElement().hasClassName("scenarioTreeNoDropBelow")) {
                            belowTreeArea.addStyleName("scenarioTreeNoDropBelow");
                        }
                        
                    } else if(!belowTreeArea.getElement().hasClassName("scenarioTreeDropBelow")) {
                        
                        //change the cursor when dragging over the area below the tree to indicate that users can drop items there
                        belowTreeArea.addStyleName("scenarioTreeDropBelow");
                    }
                }
            }
            
        }, MouseOverEvent.getType());
        
        belowTreeArea.addDomHandler(new MouseOutHandler() {
            
            @Override
            public void onMouseOut(MouseOutEvent event) {
                
                //remove black horizontal line after the tree when the area below it is dragged out of
                treeArea.removeStyleName("scenarioTreeDropOut");
                belowTreeArea.removeStyleName("scenarioTreeDropBelow");
                belowTreeArea.removeStyleName("scenarioTreeNoDropBelow");
            }
            
        }, MouseOutEvent.getType());
        
        belowTreeArea.addDomHandler(new MouseUpHandler() {
            
            @Override
            public void onMouseUp(MouseUpEvent event) {
                
                treeArea.removeStyleName("scenarioTreeDropOut");
                belowTreeArea.removeStyleName("scenarioTreeDropBelow");
                belowTreeArea.removeStyleName("scenarioTreeNoDropBelow");
                
                ScenarioObjectTreeItem<?> dragged = ScenarioObjectTreeItem.getItemBeingDragged();
                
                if(dragged != null) {
                    
                    //if a task, strategy, or transition is dragged below the tree, add the item to the end of the tree
                    if (dragged.getScenarioObject() instanceof Task) {
                        
                        Task dragTask = (Task) dragged.getScenarioObject();
                        
                        //if a task is dragged on top of this task, place the dragged task after this task
                        Tasks tasks = ScenarioClientUtility.getTasks();
                        
                        if(tasks != null) {
                            
                            tasks.getTask().remove(dragTask);
                            tasks.getTask().add(dragTask);
                            
                            boolean wasDragItemSelected = dragged.isSelected();
                            
                            dragged.remove();
                            
                            //move the tree item accordingly
                            tasksTree.addItem(dragged);
                            
                            if(wasDragItemSelected) {
                                
                                //if the dragged item was selected, we need to re-select it
                                dragged.setSelected(true);
                            }
                        }
                        
                    } else if (dragged.getScenarioObject() instanceof Strategy) {
                        
                        Strategy dragStrategy = (Strategy) dragged.getScenarioObject();
                        
                        //if a strategy is dragged on top of this strategy, place the dragged strategy after this strategy
                        InstructionalStrategies strategies = ScenarioClientUtility.getStrategies();
                        
                        if(strategies != null) {
                            
                            strategies.getStrategy().remove(dragStrategy);
                            strategies.getStrategy().add(dragStrategy);
                            
                            boolean wasDragItemSelected = dragged.isSelected();
                            
                            dragged.remove();
                            
                            //move the tree item accordingly
                            strategiesTree.addItem(dragged);
                            
                            if(wasDragItemSelected) {
                                
                                //if the dragged item was selected, we need to re-select it
                                dragged.setSelected(true);
                            }
                        }
                        
                    } else if (dragged.getScenarioObject() instanceof StateTransition) {
                        
                        StateTransition dragTransition = (StateTransition) dragged.getScenarioObject();
                        
                        //if a Transition is dragged on top of this Transition, place the dragged Transition after this Transition
                        StateTransitions transitions = ScenarioClientUtility.getStateTransitions();
                        
                        if(transitions != null) {
                            
                            transitions.getStateTransition().remove(dragTransition);
                            transitions.getStateTransition().add(dragTransition);
                            
                            boolean wasDragItemSelected = dragged.isSelected();
                            
                            dragged.remove();
                            
                            //move the tree item accordingly
                            transitionsTree.addItem(dragged);
                            
                            if(wasDragItemSelected) {
                                
                                //if the dragged item was selected, we need to re-select it
                                dragged.setSelected(true);
                            }
                        }
                    }
                }
            }
        }, MouseUpEvent.getType());
        
        final ScheduledCommand addTaskCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {
                
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("Adding new task");
                }
                
                addObjectMenu.hide();

                ScenarioEventUtility.fireCreateScenarioObjectEvent(ScenarioClientUtility.generateNewTask());

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished adding task");
                }
            }
        };
        
        addObjectMenu.getMenu().addItem(
                "<i class='fa fa-lightbulb-o scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Add task"
            +   "</span>", true, addTaskCommand);
        
        final ScheduledCommand importCourseConceptCommand = new ScheduledCommand() {
            
            @Override
            public void execute() {

                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Importing course concept to new task");
                }
                
                addObjectMenu.hide();
                
                ImportCourseConceptDialog.display(new ImportHandler() {
                    
                    @Override
                    public void onImport(ConceptNodeRef nodeToImport) {
                        
                        ScenarioEventUtility.fireCreateScenarioObjectEvent(
                                ScenarioClientUtility.importTaskFromCourse(nodeToImport)
                        );
                    }
                });
                
                if(logger.isLoggable(Level.FINE)){
                    logger.fine("Finished importing course concept to task");
                }
            }
        };
        
        addObjectMenu.getMenu().addItem(
                "<i class='fa fa-external-link-square scenarioTreeContextItem'></i>"
            +   "<span style='vertical-align: middle;'>"
            +       "Import course concept"
            +   "</span>", true, importCourseConceptCommand);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished constructing scenario outline");
        }
    }

    @Override
    protected void editObject(Scenario scenario) {

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editObject(" + scenario + ")");
        }

        clearTrees();

        if (scenario.getAssessment() == null) {
            scenario.setAssessment(new Assessment());
        }

        if (scenario.getAssessment().getObjects() == null) {
            scenario.getAssessment().setObjects(new Objects());
        }

        if (scenario.getAssessment().getTasks() == null) {
            scenario.getAssessment().setTasks(new Tasks());
        }

        if (scenario.getActions() == null) {
            scenario.setActions(new Actions());
        }

        if (scenario.getActions().getStateTransitions() == null) {
            scenario.getActions().setStateTransitions(new StateTransitions());
        }

        if (scenario.getActions().getInstructionalStrategies() == null) {
            scenario.getActions().setInstructionalStrategies(new InstructionalStrategies());
        }

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Creating base tree items");
        }

        populateTrees(scenario);

        // There should always be one task in the , so select it
        if (!ScenarioClientUtility.getUnmodifiableTaskList().isEmpty()) {
            tasksTab.showTab();
            tasksTree.setSelectedItem(tasksTree.getItem(0));
        } else {
            logger.warning("Failed to select the first task in the tree because there were no tasks to select!");
        }
        
        // reset horizontal scroll to left most position so that long labels (e.g. task names) don't make
        // horizontal scroll all the way to the right upon rendering
        scenarioHeaderScrollPanel.scrollToBegin();

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Finished creating base tree items");
        }
    }

    /**
     * Populates the trees within the {@link ScenarioOutlineEditor} with the
     * data from the provided {@link Scenario}
     * 
     * @param scenario The {@link Scenario} from which to read the data that is
     *        used to populate the trees.
     */
    private void populateTrees(Scenario scenario) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("populateTrees(" + scenario + ")");
        }

        // Populate task tree
        final List<Task> tasks = ScenarioClientUtility.getUnmodifiableTaskList();
        for (Task task : tasks) {
            TaskTreeItem taskTreeItem = new TaskTreeItem(task);
            tasksTree.addItem(taskTreeItem);
        }
        updateTreeTabBadge(tasksTab);

        // Populate transition tree
        for (StateTransition transition : ScenarioClientUtility.getUnmodifiableStateTransitionList()) {
            StateTransitionTreeItem transitionTreeItem = new StateTransitionTreeItem(transition);
            transitionsTree.addItem(transitionTreeItem);
        }
        updateTreeTabBadge(transitionsTab);

        // Populate the strategies tree
        for (Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {
            StrategyTreeItem strategyTreeItem = new StrategyTreeItem(strategy);
            strategiesTree.addItem(strategyTreeItem);
        }
        updateTreeTabBadge(strategiesTab);

        Assessment assessment = scenario.getAssessment();
        if (assessment == null) {
            scenario.setAssessment(assessment = new Assessment());
        }

        Objects objects = assessment.getObjects();
        if (objects == null) {
            assessment.setObjects(objects = new Objects());
        }

        PlacesOfInterest placesOfInterest = objects.getPlacesOfInterest();
        if (placesOfInterest == null) {
            objects.setPlacesOfInterest(placesOfInterest = new PlacesOfInterest());
        }
        
        //waypoint references have been updated, so gather the global references
        ScenarioClientUtility.gatherPlacesOfInterestReferences();

        Resources resources = scenario.getResources();
        if (resources == null) {
            scenario.setResources(resources = new Resources());
        }

        AvailableLearnerActions availableLearnerActions = resources.getAvailableLearnerActions();
        if (availableLearnerActions == null) {
            resources.setAvailableLearnerActions(availableLearnerActions = new AvailableLearnerActions());
        }

        EndTriggers endTriggers = scenario.getEndTriggers();
        if (endTriggers == null) {
            scenario.setEndTriggers(endTriggers = new EndTriggers());
        }

        // Populate the property tree
        propertiesTree.addItem(new ScenarioEndTriggersTreeItem(endTriggers));
        propertiesTree.addItem(new LearnerActionsTreeItem(availableLearnerActions));
        propertiesTree.addItem(new WaypointsTreeItem(placesOfInterest));
        
        if(ScenarioClientUtility.isLearnerIdRequiredByApplication()){
            
            //only show the team picker when authoring a scenario that requires learner IDs
            propertiesTree.addItem(new TeamOrganizationTreeItem(ScenarioClientUtility.getTeamOrganization()));
            
            ScenarioClientUtility.gatherTeamReferences();
        }
        propertiesTree.addItem(new MiscellaneousTreeItem(scenario));
        updateTreeTabBadge(propertiesTab);

        // There should always be one task in the tree, so select it
        if (!tasks.isEmpty()) {
            tasksTab.showTab();
            addTooltip.setTitle("Add New Task");
            tasksTree.setSelectedItem(tasksTree.getItem(0));
        } else {
            propertiesTab.showTab();
            addButton.setVisible(false);
            propertiesTree.setSelectedItem(propertiesTree.getItem(0));
        }
    }
    
    /**
     * Re-populates the tasks tree as necessary when something changes that
     * requires the tree to be refreshed.
     * @param scenario The DKF scenario to use as the data behind the repopulated tasks tree.
     */
    private void repopulateTasksTree(Scenario scenario, Serializable sourceScenarioObject) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("repopulateTrees(" + scenario + ")");
        }
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

			@Override
			public void execute() {
				// Get a list of all the opened TaskTreeItems.
		        Map<String, TreeItem> openTasksMap = new HashMap<String, TreeItem>(); 
		        for (int i = 0; i < tasksTree.getItemCount(); i++) {
		        	openTasksMap.putAll(getOpenTaskTreeItems(tasksTree.getItem(i)));
		        }
		        				
				// Clear, then repopulate task tree
		        tasksTree.clear();
		        final List<Task> tasks = ScenarioClientUtility.getUnmodifiableTaskList();
		        for (Task task : tasks) {
		            TaskTreeItem taskTreeItem = new TaskTreeItem(task);
		            tasksTree.addItem(taskTreeItem);
		            if (openTasksMap.containsKey(taskTreeItem.getText())) {
		            	taskTreeItem.setState(true);
		            }
		        }
		        
		        for (int i=0; i < tasksTree.getItemCount(); i++) {
		        	restoreOpenTaskTreeItems(tasksTree.getItem(i), openTasksMap);
		        }
		        
		        updateTreeTabBadge(tasksTab);
		        
		        if (sourceScenarioObject != null) {
		        	selectItem(sourceScenarioObject);
		        } else if (!tasks.isEmpty()) {
		            tasksTab.showTab();
		            addTooltip.setTitle("Add New Task");
		            tasksTree.setSelectedItem(tasksTree.getItem(0));
		        } else {
		            propertiesTab.showTab();
		            addButton.setVisible(false);
		            propertiesTree.setSelectedItem(propertiesTree.getItem(0));
		        }
			}
        });
        
    }
    
    /**
     * Gets a map of the task tree items that are currently opened, indexed by their text.
     * Recursively loops through the contents of child tree items.
     * @param item The item currently being checked.
     * @return A map of the task tree items that are currently opened, indexed by their text. Represents all
     * 		open TreeItems in item or its child TreeItems, etc.
     */
    private Map<String, TreeItem> getOpenTaskTreeItems(TreeItem item) {
		Map<String, TreeItem> itemsToReturn = new HashMap<String, TreeItem>();
		
		if (item.getState()) {
			itemsToReturn.put(item.getText(), item);
		}
		
		for (int i=0; i < item.getChildCount(); i++) {
			itemsToReturn.putAll(getOpenTaskTreeItems(item.getChild(i)));
		}
		
		return itemsToReturn;
	}
    
    /**
     * Using a map of task tree items that were previously open, this opens all the equivalent
     * tree items in the current task tree. Recursively traverses the current tree.
     * @param item The TreeItem currently being checked and either opened or remaining closed.
     * @param openTreeItemsMap A map of all previously open task tree items, indexed by their text.
     */
    private void restoreOpenTaskTreeItems(TreeItem item, Map<String, TreeItem> openTreeItemsMap) {
    	if (openTreeItemsMap.containsKey(item.getText())) {
    		item.setState(true);
    	}
    	
    	for (int i=0; i < item.getChildCount(); i++) {
			restoreOpenTaskTreeItems(item.getChild(i), openTreeItemsMap);
		}
    }

	/**
     * Selects the {@link TreeItem} that contain the provided scenario object.
     * 
     * @param scenarioObject The scenario object to select within the
     *        {@link ScenarioOutlineEditor}
     */
    private void selectItem(Serializable scenarioObject) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("selectItem(" + scenarioObject + ")");
        }

        ScenarioObjectTreeItem<?> treeItem = findTreeItem(scenarioObject);
        if (treeItem != null) {
            selectItem(treeItem);
        }
    }

    /**
     * Select the TreeItem contained in the outline tree
     * 
     * @param item the item to select. Can't be null.
     * @throws IllegalArgumentException if the item is null
     * @throws UnsupportedOperationException if the item is an unknown type
     */
    private void selectItem(TreeItem item) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("selectItem(" + item + ")");
        }
        
        if (item == null) {
            throw new IllegalArgumentException("The parameter 'item' cannot be null.");
        }

        // Expand the path to the item
        TreeItem thisItem = item;
        while (thisItem != null) {
            thisItem.setState(true);
            thisItem = thisItem.getParentItem();
        }

        if (item instanceof TaskTreeItem || item instanceof ConceptTreeItem || item instanceof ConditionTreeItem) {
            // don't fire selection event because selectItem is called for 'jump to' events and not when the user
            // clicks on the item about to be shown.  We don't want the selection handler to be called which would fire
            // its own event that leads to editing the item for a second time (cause the logic before this method also edits the object)
            updateSelectedItemState((ScenarioObjectTreeItem<?>)item);
            tasksTree.setSelectedItem(item, false);
            addTooltip.setTitle("Add New Task");
        } else if (item instanceof StrategyTreeItem) {
            // don't fire selection event because selectItem is called for 'jump to' events and not when the user
            // clicks on the item about to be shown.  We don't want the selection handler to be called which would fire
            // its own event that leads to editing the item for a second time (cause the logic before this method also edits the object)
            updateSelectedItemState((ScenarioObjectTreeItem<?>)item);
            strategiesTree.setSelectedItem(item, false);
            addTooltip.setTitle("Add New Strategy");
        } else if (item instanceof StateTransitionTreeItem) {
            // don't fire selection event because selectItem is called for 'jump to' events and not when the user
            // clicks on the item about to be shown.  We don't want the selection handler to be called which would fire
            // its own event that leads to editing the item for a second time (cause the logic before this method also edits the object)
            updateSelectedItemState((ScenarioObjectTreeItem<?>)item);
            transitionsTree.setSelectedItem(item, false);
            addTooltip.setTitle("Add New State Transition");
        } else if (item instanceof ScenarioEndTriggersTreeItem || item instanceof LearnerActionsTreeItem
                || item instanceof WaypointsTreeItem || item instanceof MiscellaneousTreeItem) {
            // don't fire selection event because selectItem is called for 'jump to' events and not when the user
            // clicks on the item about to be shown.  We don't want the selection handler to be called which would fire
            // its own event that leads to editing the item for a second time (cause the logic before this method also edits the object)
            updateSelectedItemState((ScenarioObjectTreeItem<?>)item);
            propertiesTree.setSelectedItem(item, false);
        } else {
            final String itemClassName = item.getClass().getSimpleName();
            final String message = "The tree item of type '" + itemClassName + "' was unexpected for the method";
            throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Deselects any selected
     */
    private void deselectCurrentSelection() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deselectCurrentSelection()");
        }
        
        if (tasksTree.getSelectedItem() != null) {
            tasksTree.getSelectedItem().setSelected(false);
        }
        if (strategiesTree.getSelectedItem() != null) {
            strategiesTree.getSelectedItem().setSelected(false);
        }
        if (transitionsTree.getSelectedItem() != null) {
            transitionsTree.getSelectedItem().setSelected(false);
        }
        if (propertiesTree.getSelectedItem() != null) {
            propertiesTree.getSelectedItem().setSelected(false);
        }
        
        lastSelectedItem = null;
    }
    
    /**
     * Adds the provided {@link Task} to the scenario outline
     * 
     * @param task The task to add to the scenario outline. Cannot be null.
     */
    private void addTask(Task task) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addTask(" + task + ")");
        }

        if (task == null) {
            throw new IllegalArgumentException("The parameter 'task' cannot be null.");
        }

        TreeItem item = new TaskTreeItem(task);
        tasksTree.addItem(item);
        
        // expand the selected item
        if (item.getParentItem() != null) {
            item.getParentItem().setState(true);
        }

        tasksTree.setSelectedItem(item);
    }

    /**
     * Adds the provided {@link Concept} to the scenario outline
     * 
     * @param concept The concept to add to the scenario outline. Cannot be
     *        null.
     * @param task The task to which to add the concept. Cannot be null.
     * @throws UnsupportedOperationException if the task cannot be found in the outline
     */
    private void addConcept(Concept concept, Task task) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addConcept(" + concept + ", " + task + ")");
        }

        if (concept == null) {
            throw new IllegalArgumentException("The parameter 'concept' cannot be null.");
        } else if (task == null) {
            throw new IllegalArgumentException("The parameter 'task' cannot be null.");
        }

        ScenarioObjectTreeItem<?> taskTreeItem = findTreeItem(task);

        // Guard against a node that was unable to be found
        if (taskTreeItem == null) {
            throw new UnsupportedOperationException(
                    "The task '" + task.getNodeId() + " - " + task.getName() + "' could not be found in the outline.");
        } else if (!(taskTreeItem instanceof TaskTreeItem)) {
            throw new UnsupportedOperationException("The task '" + task.getNodeId() + " - " + task.getName()
                    + "' was found but it wasn't of type 'TaskTreeItem'");
        }

        // Add the new tree item to the found task item
        ConceptTreeItem newConceptItem = new ConceptTreeItem(concept);
        taskTreeItem.addItem(newConceptItem);

        // expand the selected item
        TreeItem parent = taskTreeItem;
        parent.setState(true);
        while((parent = parent.getParentItem()) != null) {
            parent.setState(true);
        }

        tasksTree.setSelectedItem(newConceptItem);
    }
    
    /**
     * Adds the provided {@link Concept} to the scenario outline
     * 
     * @param concept The concept to add to the scenario outline. Cannot be null.
     * @param parentConcept The parent concept to which to add the concept. Cannot be null.
     * @throws UnsupportedOperationException if the parentConcept does not contain concepts or if
     *         the concept is not found in the outline
     */
    private void addConcept(Concept concept, Concept parentConcept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addConcept(" + concept + ", " + parentConcept + ")");
        }

        if (concept == null) {
            throw new IllegalArgumentException("The parameter 'concept' cannot be null.");
        } else if (parentConcept == null) {
            throw new IllegalArgumentException("The parameter 'parentConcept' cannot be null.");
        } else if (parentConcept.getConditionsOrConcepts() != null
                && !(parentConcept.getConditionsOrConcepts() instanceof Concepts)) {
            throw new UnsupportedOperationException("The parameter 'parentConcept' may only have a list of 'subconcepts'");
        }

        // Find the task tree item
        ScenarioObjectTreeItem<?> selectedTaskItem = findTreeItem(parentConcept);
        
        // Guard against a node that was unable to be found
        if (selectedTaskItem == null) {
            throw new UnsupportedOperationException(
                    "The concept '" + parentConcept.getNodeId() + " - " + parentConcept.getName() + "' could not be found in the outline.");
        }

        // Add the new tree item to the found task item
        ConceptTreeItem newConceptItem = new ConceptTreeItem(concept);
        selectedTaskItem.addItem(newConceptItem);

        // expand the selected item
        TreeItem parent = selectedTaskItem;
        parent.setState(true);
        while((parent = parent.getParentItem()) != null) {
            parent.setState(true);
        }

        tasksTree.setSelectedItem(newConceptItem);
    }
    
    /**
     * Adds the provided {@link Condition} to the scenario outline
     * 
     * @param condition The condition to add to the scenario outline. Cannot be
     *        null.
     * @param parentConcept The parent concept to which to add the condition. Cannot be null.
     * @throws UnsupportedOperationException if the parentConcept does not contain conditions or if
     *         the parentConcept is not found in the outline
     */
    private void addCondition(Condition condition, Concept parentConcept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addCondition(" + condition + ", " + parentConcept + ")");
        }

        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        } else if (parentConcept == null) {
            throw new IllegalArgumentException("The parameter 'parentConcept' cannot be null.");
        } else if (parentConcept.getConditionsOrConcepts() != null
                && !(parentConcept.getConditionsOrConcepts() instanceof Conditions)) {
            throw new UnsupportedOperationException("The parameter 'parentConcept' may only have a list of 'conditions'");
        }

        // Find the task tree item
        ScenarioObjectTreeItem<?> selectedTaskItem = findTreeItem(parentConcept);
        
        // Guard against a node that was unable to be found
        if (selectedTaskItem == null) {
            throw new UnsupportedOperationException(
                    "The concept '" + parentConcept.getNodeId() + " - " + parentConcept.getName() + "' could not be found in the outline.");
        }

        // Add the new tree item to the found task item
        ConditionTreeItem newConditionItem = new ConditionTreeItem(condition);
        selectedTaskItem.addItem(newConditionItem);

        // expand the selected item
        TreeItem parent = selectedTaskItem;
        parent.setState(true);
        while((parent = parent.getParentItem()) != null) {
            parent.setState(true);
        }

        tasksTree.setSelectedItem(newConditionItem);
    }

    /**
     * Adds the provided {@link Strategy} to the scenario outline
     * 
     * @param strategy The strategy to add to the scenario outline. Cannot be
     *        null.
     */
    private void addStrategy(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addStrategy(" + strategy + ")");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        TreeItem item = new StrategyTreeItem(strategy);
        strategiesTree.addItem(item);

        // expand the selected item
        if (item.getParentItem() != null) {
            item.getParentItem().setState(true);
        }

        strategiesTree.setSelectedItem(item);
    }

    /**
     * Adds the provided {@link StateTransition} to the scenario outline
     * 
     * @param stateTransition The stateTransition to add to the scenario outline. Cannot be null.
     */
    private void addStateTransition(StateTransition stateTransition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addStateTransition(" + stateTransition + ")");
        }

        if (stateTransition == null) {
            throw new IllegalArgumentException("The parameter 'stateTransition' cannot be null.");
        }

        TreeItem item = new StateTransitionTreeItem(stateTransition);
        transitionsTree.addItem(item);

        // expand the selected item
        if (item.getParentItem() != null) {
            item.getParentItem().setState(true);
        }

        transitionsTree.setSelectedItem(item);
    }

    /**
     * Find the TreeItem that corresponds to the Serializable item it represents
     * 
     * @param item The Serializable item to look for. Cannot be null.
     * @return the ScenarioObjectTreeItem that corresponds to the Serializable item. Returns null if
     *         not found.
     */
    private ScenarioObjectTreeItem<?> findTreeItem(Serializable item) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("findTreeItem(" + item + ")");
        }

        if (item == null) {
            throw new IllegalArgumentException("The parameter 'item' cannot be null.");
        }

        ScenarioObjectTreeItem<?> node = null;

        /* Elevate the team or team member to the entire organization since that
         * is what is actually in the tree */
        if (item instanceof Team || item instanceof TeamMember) {
            item = ScenarioClientUtility.getTeamOrganization();
        }

        Tree outlineTree = null;
        if (item instanceof Task || item instanceof Concept || item instanceof Condition) {
            outlineTree = tasksTree;
        } else if (item instanceof StateTransition) {
            outlineTree = transitionsTree;
        } else if (item instanceof Strategy) {
            outlineTree = strategiesTree;
        } else if (item instanceof PlacesOfInterest || item instanceof AvailableLearnerActions
                || item instanceof EndTriggers || item instanceof Scenario || item instanceof TeamOrganization ||
                item instanceof LearnerAction) {
            outlineTree = propertiesTree;
        } else {
            return null;
        }

        for (int i = 0; i < outlineTree.getItemCount(); i++) {
            node = findTreeItem((ScenarioObjectTreeItem<?>) outlineTree.getItem(i), item);
            if (node != null) {
                return node;
            }
        }

        return node;
    }

    /**
     * Recursive method to search through the tree for the Serializable item.
     * 
     * @param node the root node containing the ScenarioObjectTreeItems we want to search through.
     *        Cannot be null.
     * @param item the Serializable item we want to find. Cannot be null.
     * @return the ScenarioObjectTreeItem that corresponds to the Serializable item. Returns null if
     *         not found.
     */
    private ScenarioObjectTreeItem<?> findTreeItem(ScenarioObjectTreeItem<?> node, Serializable item) {
        if (node == null) {
            throw new IllegalArgumentException("The parameter 'node' cannot be null.");
        } else if (item == null) {
            throw new IllegalArgumentException("The parameter 'item' cannot be null.");
        }

        if (node.getScenarioObject() == item) {
            return node;
        }

        ScenarioObjectTreeItem<?> currNode = null;

        for (int i = 0; i < node.getChildCount(); i++) {
            if (node.getChild(i) instanceof ScenarioObjectTreeItem) {
                currNode = findTreeItem((ScenarioObjectTreeItem<?>) node.getChild(i), item);
                if (currNode != null) {
                    return currNode;
                }
            }
        }

        return currNode;
    }

    @Override
    public HandlerRegistration addSelectionHandler(SelectionHandler<Serializable> handler) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addSelectionHandler(" + handler + ")");
        }

        return addHandler(handler, SelectionEvent.getType());
    }

    /**
     * Clears the {@link Tree} widgets of any previous state
     */
    private void clearTrees() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("clearTrees()");
        }

        tasksTree.clear();
        transitionsTree.clear();
        strategiesTree.clear();
        propertiesTree.clear();
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processEvent(" + event + ")");
        }

        if (event instanceof CreateScenarioObjectEvent) {
            handleCreate((CreateScenarioObjectEvent) event);
        } else if (event instanceof DeleteScenarioObjectEvent) {
            handleDelete((DeleteScenarioObjectEvent) event);
        } else if (event instanceof RenameScenarioObjectEvent) {
            handleRename((RenameScenarioObjectEvent) event);
        } else if (event instanceof JumpToEvent) {
            handleJump((JumpToEvent) event);
        } else if (event instanceof ScenarioEditorDirtyEvent) {
            handleDirtyEvent((ScenarioEditorDirtyEvent) event);
        } else if (event instanceof LearnerStartLocationUpdatedEvent) {
            handleLearnerStartLocationUpdatedEvent((LearnerStartLocationUpdatedEvent) event);
        } else if (event instanceof PopulateScenarioTreesEvent) {
        	handlePopulateScenarioTreesEvent((PopulateScenarioTreesEvent) event);
        } else if (event instanceof PlaceOfInterestEditedEvent) {
            handlePlaceOfInterestEditedEvent((PlaceOfInterestEditedEvent) event);
        }
    }

    /**
     * Handles the processing of a {@link CreateScenarioObjectEvent} by adding
     * the provided scenario object to the {@link ScenarioOutlineEditor}
     * 
     * @param event The event that contains the scenario object ot add to the
     *        {@link ScenarioOutlineEditor}.
     */
    private void handleCreate(CreateScenarioObjectEvent event) {
        Serializable scenarioObj = event.getScenarioObject();
        if (scenarioObj instanceof Task) {
            Task task = (Task) scenarioObj;
            addTask(task);
        } else if (scenarioObj instanceof Concept) {
            Concept concept = (Concept) scenarioObj;
            Serializable parent = event.getParent();
            if (parent instanceof Task) {
                Task parentTask = (Task) parent;
                addConcept(concept, parentTask);
            } else if (parent instanceof Concept) {
                Concept parentConcept = (Concept) parent;
                addConcept(concept, parentConcept);
            }
        } else if (scenarioObj instanceof Condition) {
            Condition condition = (Condition) scenarioObj;
            addCondition(condition, (Concept) event.getParent());
        } else if (scenarioObj instanceof Strategy) {
            Strategy strategy = (Strategy) scenarioObj;
            addStrategy(strategy);
        } else if (scenarioObj instanceof StateTransition) {
            StateTransition transition = (StateTransition) scenarioObj;
            addStateTransition(transition);
        }
        
        //show the appropriate tab for the created item
        TabListItem tab = getScenarioObjectTab(scenarioObj);
        
        if(tab != null) {
            tab.showTab();
        }

    }

    /**
     * Handles the processing of a {@link DeleteScenarioObjectEvent} by removing the provided
     * scenario object from the {@link ScenarioOutlineEditor}.
     * 
     * @param event The event that contains scenario object to remove from the
     *        {@link ScenarioOutlineEditor}.
     */
    private void handleDelete(DeleteScenarioObjectEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleDelete(" + event + ")");
        }

        ScenarioObjectTreeItem<?> treeItem = findTreeItem(event.getScenarioObject());
        if (treeItem != null) {
            // get tab before we remove the item from the tree
            TabListItem treeTab = getScenarioObjectTab(treeItem.getScenarioObject());

            // get parent before we remove the item from the tree
            ScenarioObjectTreeItem<?> parent = treeItem.getParentItem();

            treeItem.remove();

            if (parent == null) {
                /* deleting the tree item might cause the tab to become valid */
                updateTreeTabBadge(treeTab);
            } else {
                walkTreeAndValidate(parent);
            }
        }
    }
    
    /**
     * Handles when the name of the object has been changed. Changes the name
     * label for this tree item to use the new name.
     * 
     * @param event The {@link RenameScenarioObjectEvent} containing the
     *        details of the rename including what the new name is.
     */
    private void handleRename(RenameScenarioObjectEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleRename(" + event + ")");
        }

        ScenarioObjectTreeItem<?> treeItem = findTreeItem(event.getScenarioObject());
        if (treeItem != null) {
            String newName = event.getNewName();
            String oldName = treeItem.getNameLabel().getValue();

            if (event.getScenarioObject().equals(treeItem.getScenarioObject())) {
            
                if (!StringUtils.equals(newName, oldName)) {
                    treeItem.getNameLabel().setValue(newName); 
                }
                
                //return the name label to normal if the name is no longer associated with a course concept
                if(treeItem.getScenarioObject() instanceof Task || treeItem.getScenarioObject() instanceof Concept) {
          
                   if(CourseConceptUtility.getConceptWithName(newName) == null) {
                       treeItem.getNameLabel().removeStyleName("associatedWithConcept");
                   }else {
                       treeItem.getNameLabel().addStyleName("associatedWithConcept");
                   }
            
               }
              
            }
        
        }
    }    
    
    /**
     * Update the last selected item with the item provided if the item provided
     * is not the same as the last selected item.  This will also set the selected state
     * of the provided item to true and the last selected item selected state to false.
     * @param treeItem the item being selected and needs its selected state updated.
     */
    private void updateSelectedItemState(ScenarioObjectTreeItem<?> treeItem){
        
        Serializable selectedObject = treeItem.getScenarioObject();
        
        if (lastSelectedItem != null && lastSelectedItem.getScenarioObject() == selectedObject) {
            return;
        }
            
        if (logger.isLoggable(Level.INFO)) {
            logger.info("Selected object: " + selectedObject.toString());
        }
        
        if(lastSelectedItem != null) {
            
            // If another item was selected before, deselect it. This is necessary if the previous item was selected
            // in a different tab, since selecting an item in a different tab's tree won't update the previous one.
            lastSelectedItem.setSelected(false);
        }
        
        if(!treeItem.getState()) {
            
            //if the selected object's tree item is collapsed, expand it
            treeItem.setState(true);
        }
        
        lastSelectedItem = treeItem;

    }

    /**
     * Handles when a request has been made to select a specific scenario object
     * within the {@link ScenarioOutlineEditor}.
     * 
     * @param event The event containing details about which scenario object to
     *        display within the {@link ScenarioOutlineEditor}.
     */
    private void handleJump(JumpToEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleJump(" + event + ")");
        }
        
        Serializable scenarioObject = event.getScenarioObject();
        
        //show the appropriate tab for the item being jumped to
        TabListItem tab = getScenarioObjectTab(scenarioObject);
        
        if(tab != null) {
            tab.showTab();
        }
        
        if (scenarioObject != null) {            
            selectItem(scenarioObject);
        } else {
            // deselect current item if scenario object is null
            deselectCurrentSelection();
        }
    }
    
    /**
     * Handles when a dirty event has been fired. Revalidates any necessary tree items.
     * 
     * @param event The dirty event containing details about the source scenario object.
     */
    private void handleDirtyEvent(ScenarioEditorDirtyEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleDirtyEvent(" + event.getSourceScenarioObject() + ")");
        }

        Serializable scenarioObject = event.getSourceScenarioObject();
        if (scenarioObject == null) {
            return;
        }

        // walk tree
        walkTreeAndValidate(findTreeItem(scenarioObject));
    }
    
    /**
     * Handles when the learner start location update event has been fires. Revalidates any
     * necessary tree items.
     * 
     * @param event the event that was fired.
     */
    private void handleLearnerStartLocationUpdatedEvent(LearnerStartLocationUpdatedEvent event) {
        // scenario end triggers might contain a learner start location
        final Scenario.EndTriggers scenarioEndTriggers = ScenarioClientUtility.getScenario().getEndTriggers();
        if (scenarioEndTriggers != null) {
            walkTreeAndValidate(findTreeItem(scenarioEndTriggers));
        }

        // specific conditions might contain a learner start location
        for (Condition condition : ScenarioClientUtility.getUnmodifiableConditionList()) {
            if (condition.getInput() != null && condition.getInput().getType() != null) {
                Serializable type = condition.getInput().getType();
                boolean revalidate = false;
                if (type instanceof AvoidLocationCondition || type instanceof CheckpointPaceCondition
                        || type instanceof CheckpointProgressCondition || type instanceof CorridorBoundaryCondition
                        || type instanceof CorridorPostureCondition || type instanceof EnterAreaCondition
                        || type instanceof IdentifyPOIsCondition || type instanceof LifeformTargetAccuracyCondition
                        || type instanceof RulesOfEngagementCondition) {
                    revalidate = true;
                }

                if (revalidate) {
                    walkTreeAndValidate(findTreeItem(condition));
                }
            }
        }

        // task triggers might contain a learner start location
        for (Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {
            walkTreeAndValidate(findTreeItem(task));
        }
    }
    
    /**
     * Handles when the populate scenario trees event has been fire.
     * Re-populates any trees that need to be updated due to a change in underlying data.
     */
    private void handlePopulateScenarioTreesEvent(PopulateScenarioTreesEvent event) {
    	repopulateTasksTree(ScenarioClientUtility.getScenario(), event.getSourceScenarioObject());
    }
    
    /**
     * Handles when the place of interest edited event has been fired. Revalidates any
     * necessary tree items.
     * 
     * @param event the event that was fired.
     */
    private void handlePlaceOfInterestEditedEvent(PlaceOfInterestEditedEvent event) {
        // scenario end triggers might contain a learner start location
        final Scenario.EndTriggers scenarioEndTriggers = ScenarioClientUtility.getScenario().getEndTriggers();
        if (scenarioEndTriggers != null) {
            walkTreeAndValidate(findTreeItem(scenarioEndTriggers));
        }

        // specific strategy activities might contain a place of interest
        for (Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {
            boolean revalidate = false;
            for(Serializable activity : strategy.getStrategyActivities()) {
                if (activity instanceof ScenarioAdaptation) {
                    
                    ScenarioAdaptation sAdaptation = (ScenarioAdaptation)activity;
                    generated.dkf.EnvironmentAdaptation eAdaptation = sAdaptation.getEnvironmentAdaptation();
                    if(eAdaptation == null){
                        continue;
                    } else if(eAdaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs){
                        generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs breadcrumbs = 
                                (generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs)eAdaptation.getType();
                        
                        generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = breadcrumbs.getLocationInfo();
                        if(locationInfo != null){
                            revalidate = true;
                            break;
                        }
                        
                    }else if(eAdaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects){
                        generated.dkf.EnvironmentAdaptation.HighlightObjects highlight = 
                                (generated.dkf.EnvironmentAdaptation.HighlightObjects)eAdaptation.getType();
                        
                        if(highlight.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo){
                            
                            generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = 
                                    (generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo)highlight.getType();
                            
                            if(locationInfo != null){
                                revalidate = true;
                                break;
                            }
                        }
                    }
                }

            }            

            if (revalidate) {
                walkTreeAndValidate(findTreeItem(strategy));
            }
        }
    }
    
    /**
     * Walks the tree (from the bottom up) and performs validation for each item found.
     * 
     * @param treeItem the bottom node of the tree from which to start walking. If null, no validation occurs.
     */
    private void walkTreeAndValidate(ScenarioObjectTreeItem<?> treeItem) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("walkTreeAndValidate(" + treeItem + ")");
        }

        if (treeItem == null) {
            return;
        }

        TabListItem treeTab = getScenarioObjectTab(treeItem.getScenarioObject());
        
        while (treeItem != null) {
            // force re-validation for scenario object
            ScenarioClientUtility.getValidationCache().isValid(treeItem.getScenarioObject(), true);
            
            // update icon to reflect validity of the object
            treeItem.updateIcon();

            // move up the tree
            treeItem = treeItem.getParentItem();
        }
        
        updateTreeTabBadge(treeTab);
    }
    
    /**
     * Updates the tree tab badge to show if it contains invalid items.
     * 
     * @param treeTab the tree tab to update.
     * @throws UnsupportedOperationException if the tree tab type is unknown
     */
    private void updateTreeTabBadge(TabListItem treeTab) {
        /* these should all be cached already, so the operations will be very quick */
        boolean allValid = true;
        if (treeTab == tasksTab) {
            for (Task task : ScenarioClientUtility.getUnmodifiableTaskList()) {
                allValid &= ScenarioClientUtility.getValidationCache().isValid(task);
            }
        } else if (treeTab == transitionsTab) {
            for (StateTransition transition : ScenarioClientUtility.getUnmodifiableStateTransitionList()) {
                allValid &= ScenarioClientUtility.getValidationCache().isValid(transition);
            }
        } else if (treeTab == strategiesTab) {
            for (Strategy strategy : ScenarioClientUtility.getUnmodifiableStrategyList()) {
                allValid &= ScenarioClientUtility.getValidationCache().isValid(strategy);
            }
        } else if (treeTab == propertiesTab) {
            Scenario scenario = ScenarioClientUtility.getScenario();

            final EndTriggers endTriggers = scenario.getEndTriggers();
            if (endTriggers != null) {
                allValid &= ScenarioClientUtility.getValidationCache().isValid(endTriggers);
            }

            final Resources resources = scenario.getResources();
            if (resources != null && resources.getAvailableLearnerActions() != null) {
                allValid &= ScenarioClientUtility.getValidationCache().isValid(resources.getAvailableLearnerActions());
            }

            final Assessment assessment = scenario.getAssessment();
            if (assessment != null && assessment.getObjects() != null
                    && assessment.getObjects().getPlacesOfInterest() != null) {
                allValid &= ScenarioClientUtility.getValidationCache().isValid(assessment.getObjects().getPlacesOfInterest());
            }

            final TeamOrganization teamOrg = scenario.getTeamOrganization();
            if (teamOrg != null) {
                allValid &= ScenarioClientUtility.getValidationCache().isValid(teamOrg);
            }

            allValid &= ScenarioClientUtility.getValidationCache().isValid(scenario);

        } else {
            throw new UnsupportedOperationException("The tree tab '" + treeTab + "' is unknown.");
        }
        
        treeTab.setBadgeText(allValid ? null : "!");
    }
    
    /**
     * Revalidates the provided scenario object.
     * 
     * @param scenarioObject the scenario object to validate
     */
    public void revalidateScenarioObject(Serializable scenarioObject) {
        // walk tree
        walkTreeAndValidate(findTreeItem(scenarioObject));
    }
    
    /**
     * Revalidates the provided scenario objects.
     * 
     * @param scenarioObjectItr the iterable collection of scenario objects to validate
     */
    public void revalidateScenarioObjects(Iterable<? extends Serializable> scenarioObjectItr) {
        if (scenarioObjectItr == null) {
            return;
        }
        
        // walk tree for each item
        for (Serializable scenarioObject : scenarioObjectItr) {
            walkTreeAndValidate(findTreeItem(scenarioObject));
        }
    }
    
    /**
     * Gets the appropriate tab that is capable of showing the given scenario object
     * 
     * @param scenarioObject the scenario object that a tab is needed for
     * @return the appropriate tab for the given scenario object
     */
    private TabListItem getScenarioObjectTab(Serializable scenarioObject) {
        
        if (scenarioObject instanceof Task || scenarioObject instanceof Concept
                || scenarioObject instanceof Condition) {
            return tasksTab;

        } else if (scenarioObject instanceof StateTransition) {
            return transitionsTab;

        } else if (scenarioObject instanceof Strategy) {
            return strategiesTab;

        } else if (scenarioObject instanceof PlacesOfInterest || scenarioObject instanceof AvailableLearnerActions
                || scenarioObject instanceof EndTriggers || scenarioObject instanceof Scenario
                || scenarioObject instanceof TeamOrganization || scenarioObject instanceof LearnerAction) {
            return propertiesTab;

        } else {
            return null;
        }
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return null;
    }
}
