/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.Actions;
import generated.dkf.Actions.InstructionalStrategies;
import generated.dkf.Actions.StateTransitions;
import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Area;
import generated.dkf.Assessment;
import generated.dkf.AvailableLearnerActions;
import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.EndTriggers;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionReference;
import generated.dkf.LearnerActionsList;
import generated.dkf.Objects;
import generated.dkf.Path;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceNode;
import generated.dkf.PlacesOfInterest;
import generated.dkf.Point;
import generated.dkf.Resources;
import generated.dkf.Scenario;
import generated.dkf.StartTriggers;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.StrategyRef;
import generated.dkf.Task;
import generated.dkf.Tasks;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.JumpToEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PopulateScenarioTreesEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ReferencesChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.outline.ScenarioOutlineEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.DefaultGatFileSelectionDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.CourseObjectModal;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;

/**
 * The Class DkfViewImpl.
 */
public class DkfViewImpl extends Composite implements DkfView {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(DkfViewImpl.class.getName());

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** A panel used to load editors for various scenario objects */
    @UiField
    protected ScenarioObjectEditorPanel objectEditorPanel;

    /** The root modal that the DKF editor is shown within */
    @UiField
    protected CourseObjectModal dkfTransitionDialog;

    /**
     * Panel used to separate the {@link #outlineWidget} from the {@link #objectEditorPanel}
     */
    @UiField
    protected SplitLayoutPanel editorPanel;

    /**
     * Shows high level view of the DKF and allows the user to select scenario objects for editing
     */
    @UiField
    protected ScenarioOutlineEditor outlineWidget;

    /** Interface for handling events. */
    interface WidgetEventBinder extends EventBinder<DkfViewImpl> {
    }

    /** Create the instance of the event binder (binds the widget for events. */
    private static final WidgetEventBinder eventBinder = GWT.create(WidgetEventBinder.class);

    /** The ui binder. */
    private static EditDkfViewImplUiBinder uiBinder = GWT.create(EditDkfViewImplUiBinder.class);

    /**
     * The Interface EditDkfViewImplUiBinder.
     */
    interface EditDkfViewImplUiBinder extends UiBinder<Widget, DkfViewImpl> {
    }

    /**
     * Instantiates a new dkf view impl.
     */
    @Inject
    public DkfViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    /**
     * Inits the.
     */
    @Inject
    private void init() {
        eventBinder.bindEventHandlers(DkfViewImpl.this, SharedResources.getInstance().getEventBus());
    }

//    /**
//     * Updates the coordinate editor with the updated learner start location.
//     *
//     * @param event an event indicating that learner start location was updated.
//     */
//    @EventHandler
//    protected void onLearnerStartLocationUpdatedEvent(LearnerStartLocationUpdatedEvent event) {
//        LearnerId learnerId = ScenarioClientUtility.getScenario().getLearnerId();
//        if (learnerId == null) {
//            return;
//        }
//        
//        Serializable learnerIdType = learnerId.getType();
//        if(learnerIdType != null && learnerIdType instanceof StartLocation){
//            
//            StartLocation startLocation = (StartLocation) learnerIdType;
//            if(startLocation.getCoordinate() == null){
//                return;
//            }
//            
//            /* The ordering of the steps are IMPORTANT. Do not re-order without thinking of all the
//             * ramifications (especially for validation). */
//
//            /* Step 1. Notify outline of the change */
//            outlineWidget.handleEvent(event);
//
//            /* Step 2. Notify any open panels of the change */
//            objectEditorPanel.dispatchEvent(event);
//
//            /* Step 3. Mark scenario as dirty */
//            onScenarioEditorDirtyEvent(new ScenarioEditorDirtyEvent());
//            
//        }
//
//
//    }

    /**
     * Handles a {@link ScenarioEditorDirtyEvent}. Editors will perform validation checks on their
     * components.
     *
     * @param dirtyEvent The event indicating that something in the editor has changed.
     */
    @EventHandler
    protected void onScenarioEditorDirtyEvent(ScenarioEditorDirtyEvent dirtyEvent) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onScenarioEditorDirtyEvent(" + dirtyEvent + ")");
        }

        ScenarioClientUtility.setDirty();

        // perform validation
        Serializable scenarioObject = dirtyEvent.getSourceScenarioObject();
        if (scenarioObject != null) {
            ScenarioClientUtility.getValidationCache().setCacheDirty(scenarioObject);

            /* The ordering of the steps are IMPORTANT. Do not re-order without thinking of all the
             * ramifications (especially for validation). */

            /* Step 1. Notify outline of the change */
            outlineWidget.handleEvent(dirtyEvent);

            /* Step2. Notify any open panels of the change */
            objectEditorPanel.dispatchEvent(dirtyEvent);
        }
    }

    /**
     * Handles the event when a Jump To button is pressed.
     *
     * @param event the {@link SurveyAddPageEvent}
     */
    @EventHandler
    protected void onJumpToEvent(JumpToEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onJumpToEvent(" + event + ")");
        }

        /* MUST call tabs before the outline. */
        Serializable scenarioObj = event.getScenarioObject();
        if (scenarioObj != null) {
            /* Alert the open tabs that a request has been made to jump to a specific tree item. */
            objectEditorPanel.dispatchEvent(event);
        }

        /* Alert the outline that a request has been made to jump to a specific tree item. */
        outlineWidget.handleEvent(event);
    }
    
    /**
     * Handles the event when the scenario trees should be re-populated.
     * 
     * @param event the {@link PopulateScenarioTreesEvent}
     */
    @EventHandler
    protected void onPopulateScenarioTreesEvent(PopulateScenarioTreesEvent event) {
    	if (logger.isLoggable(Level.FINE)) {
    		logger.fine("onPopulateScenarioTreesEvent(" + event + ")");
    		
    		/* Alert the outline to populate its scenario trees. */
    		outlineWidget.handleEvent(event);
    	}
    }
    
    /**
     * Handles the event when a place of interest is edited
     * 
     * @param event the event indicating that a place of interest has been edited
     */
    @EventHandler
    protected void onPlacesOfInterestEditedEvent(PlaceOfInterestEditedEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onJumpToEvent(" + event + ")");
        }

        /* Alert the outline that a place of interest was changed. */
        outlineWidget.handleEvent(event);
    }

    /**
     * Handles a request for the renaming of a scenario object. Handles updating the UI
     * appropriately for all references.
     *
     * @param renameEvent The event containing the scenario object that has been requested for
     *        renaming.
     */
    @EventHandler
    protected void onRenameScenarioObjectEvent(RenameScenarioObjectEvent renameEvent) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onRenameScenarioObjectEvent(" + renameEvent + ")");
        }

        /* Alert the outline that a specific tree item has changed its name. */
        outlineWidget.handleEvent(renameEvent);

        /* Alert the open tabs that a name change has occurred. */
        objectEditorPanel.dispatchEvent(renameEvent);

        /* Update the any scenario objects that are referencing the old name */
        Serializable renamedObject = renameEvent.getScenarioObject();
        final String oldName = renameEvent.getOldName();
        final String newName = renameEvent.getNewName();
        if (renamedObject instanceof Task) {
            Task task = (Task) renamedObject;

            for (Serializable reference : ScenarioClientUtility.getReferencesTo(task)) {
                if (reference instanceof StateTransition) {
                    StateTransition stateTransition = (StateTransition) reference;

                    if (stateTransition.getLogicalExpression() == null) {
                        continue;
                    }

                    for (Serializable stateType : stateTransition.getLogicalExpression().getStateType()) {
                        if (stateType instanceof PerformanceNode) {
                            PerformanceNode perfNode = (PerformanceNode) stateType;
                            /* if the performance node is this Task and the name hasn't been updated
                             * yet, then update name */
                            if (perfNode.getNodeId().equals(task.getNodeId())
                                    && !StringUtils.equals(perfNode.getName(), newName)) {
                                perfNode.setName(newName);
                            }
                        }
                    }
                }

                /* Task can also be referenced by a strategy, but strategies only hold onto the node
                 * id which remains the same during renaming. No need to update strategy. */
            }
        } else if (renamedObject instanceof Concept) {
            Concept concept = (Concept) renamedObject;

            for (Serializable reference : ScenarioClientUtility.getReferencesTo(concept)) {
                if (reference instanceof StateTransition) {
                    StateTransition stateTransition = (StateTransition) reference;

                    if (stateTransition.getLogicalExpression() == null) {
                        continue;
                    }

                    for (Serializable stateType : stateTransition.getLogicalExpression().getStateType()) {
                        if (stateType instanceof PerformanceNode) {
                            PerformanceNode perfNode = (PerformanceNode) stateType;
                            /* if the performance node is this Concept and the name hasn't been
                             * updated yet, then update name */
                            if (perfNode.getNodeId().equals(concept.getNodeId())
                                    && !StringUtils.equals(perfNode.getName(), newName)) {
                                perfNode.setName(newName);
                            }
                        }
                    }
                }

                /* Concept can also be referenced by a strategy, but strategies only hold onto the
                 * node id which remains the same during renaming. No need to update strategy. */

                /* Concept can also be referenced by a Task or Concept, but they hold onto the
                 * reference to this Concept object. No need to update the Task or Concept. */
            }
        } else if (renamedObject instanceof Strategy) {
            // build a temporary Strategy using the old name
            Strategy tempStrategy = new Strategy();
            tempStrategy.setName(oldName);
            for (Serializable reference : ScenarioClientUtility.getReferencesTo(tempStrategy)) {
                
                if(reference instanceof StateTransition){
                    StateTransition stateTransition = (StateTransition)reference;
                    if (stateTransition.getStrategyChoices() == null) {
                        continue;
                    }
    
                    for (StrategyRef strategyRef : stateTransition.getStrategyChoices().getStrategies()) {
                        /* if the strategy ref matches the old name, update it to
                         * the new name */
                        if (StringUtils.equals(strategyRef.getName(), renameEvent.getOldName())) {
                            strategyRef.setName(renameEvent.getNewName());
                        }
                    }
                }else if(reference instanceof LearnerAction){
                    LearnerAction learnerAction = (LearnerAction)reference;
                    Serializable actionParams = learnerAction.getLearnerActionParams();
                    if (actionParams instanceof generated.dkf.LearnerAction.StrategyReference) {
                        generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                        /* if the strategy ref matches the old name, update it to
                         * the new name */
                        if (StringUtils.equals(strategyRef.getName(), renameEvent.getOldName())) {
                            strategyRef.setName(renameEvent.getNewName());
                        }
                    }
                }else if(reference instanceof Task){
                    Task task = (Task)reference;
                    if (task.getStartTriggers() != null) {
                        for (StartTriggers.Trigger trigger : task.getStartTriggers().getTrigger()) {
                            Serializable triggerType = trigger.getTriggerType();
                            if(triggerType instanceof StrategyApplied){
                                StrategyApplied sApplied = (StrategyApplied)triggerType;
                                
                                /* if the strategy ref matches the old name, update it to
                                 * the new name */
                                if(StringUtils.equalsIgnoreCase(sApplied.getStrategyName(), renameEvent.getOldName())){
                                    sApplied.setStrategyName(renameEvent.getNewName());
                                }
                            }
                        }
                    }

                    if (task.getEndTriggers() != null) {
                        for (EndTriggers.Trigger trigger : task.getEndTriggers().getTrigger()) {
                            Serializable triggerType = trigger.getTriggerType();
                            if(triggerType instanceof StrategyApplied){
                                StrategyApplied sApplied = (StrategyApplied)triggerType;
                                
                                /* if the strategy ref matches the old name, update it to
                                 * the new name */
                                if(StringUtils.equalsIgnoreCase(sApplied.getStrategyName(), renameEvent.getOldName())){
                                    sApplied.setStrategyName(renameEvent.getNewName());
                                }
                            }
                        }
                    }                    
                }else if(reference instanceof Scenario.EndTriggers){
                    
                    Scenario.EndTriggers eTriggers = (Scenario.EndTriggers)reference;
                    for(Serializable eTrigger : eTriggers.getTrigger()){
                        if(eTrigger instanceof Scenario.EndTriggers.Trigger){
                            Scenario.EndTriggers.Trigger trigger = (Scenario.EndTriggers.Trigger)eTrigger;
                            Serializable triggerType = trigger.getTriggerType();
                            if(triggerType instanceof StrategyApplied){
                                StrategyApplied sApplied = (StrategyApplied)triggerType;
                                
                                /* if the strategy ref matches the old name, update it to
                                 * the new name */
                                if(StringUtils.equalsIgnoreCase(sApplied.getStrategyName(), renameEvent.getOldName())){
                                    sApplied.setStrategyName(renameEvent.getNewName());
                                }
                            }
                        }
                    }

                }
            }
        } else if (renamedObject instanceof LearnerAction) {
            // build a temporary LearnerAction using the old name
            LearnerAction tempLearnerAction = new LearnerAction();
            tempLearnerAction.setDisplayName(oldName);
            for (Serializable reference : ScenarioClientUtility.getReferencesTo(tempLearnerAction)) {
                if (reference instanceof Task) {
                    Task task = (Task) reference;
                    if (task.getStartTriggers() != null) {
                        for (StartTriggers.Trigger trigger : task.getStartTriggers().getTrigger()) {
                            if (trigger.getTriggerType() instanceof LearnerActionReference) {
                                LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                                if (StringUtils.equals(learnerRef.getName(), oldName)) {
                                    learnerRef.setName(newName);
                                }
                            }
                        }
                    }
                    if (task.getEndTriggers() != null) {
                        for (EndTriggers.Trigger trigger : task.getEndTriggers().getTrigger()) {
                            if (trigger.getTriggerType() instanceof LearnerActionReference) {
                                LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                                if (StringUtils.equals(learnerRef.getName(), oldName)) {
                                    learnerRef.setName(newName);
                                }
                            }
                        }
                    }
                } else if (reference instanceof Scenario.EndTriggers) {
                    Scenario.EndTriggers endTriggers = (Scenario.EndTriggers) reference;
                    for (Scenario.EndTriggers.Trigger trigger : endTriggers.getTrigger()) {
                        if (trigger.getTriggerType() instanceof LearnerActionReference) {
                            LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                            if (StringUtils.equals(learnerRef.getName(), oldName)) {
                                learnerRef.setName(newName);
                            }
                        }
                    }
                }
            }
        } else if (renamedObject instanceof LearnerAction) {
            // build a temporary LearnerAction using the old name
            LearnerAction tempLearnerAction = new LearnerAction();
            tempLearnerAction.setDisplayName(oldName);
            for (Serializable reference : ScenarioClientUtility.getReferencesTo(tempLearnerAction)) {
                if (reference instanceof Task) {
                    Task task = (Task) reference;
                    if (task.getStartTriggers() != null) {
                        for (StartTriggers.Trigger trigger : task.getStartTriggers().getTrigger()) {
                            if (trigger.getTriggerType() instanceof LearnerActionReference) {
                                LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                                if (StringUtils.equals(learnerRef.getName(), oldName)) {
                                    learnerRef.setName(newName);
                                }
                            }
                        }
                    }
                    if (task.getEndTriggers() != null) {
                        for (EndTriggers.Trigger trigger : task.getEndTriggers().getTrigger()) {
                            if (trigger.getTriggerType() instanceof LearnerActionReference) {
                                LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                                if (StringUtils.equals(learnerRef.getName(), oldName)) {
                                    learnerRef.setName(newName);
                                }
                            }
                        }
                    }
                } else if (reference instanceof Scenario.EndTriggers) {
                    Scenario.EndTriggers endTriggers = (Scenario.EndTriggers) reference;
                    for (Scenario.EndTriggers.Trigger trigger : endTriggers.getTrigger()) {
                        if (trigger.getTriggerType() instanceof LearnerActionReference) {
                            LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                            if (StringUtils.equals(learnerRef.getName(), oldName)) {
                                learnerRef.setName(newName);
                            }
                        }
                    }
                }
            }
        }

        /* Note: Condition can be referenced by a Concept, but the Concept holds onto the reference
         * to this Condition object. No need to update the Concept. */

        /* Note: State Transitions do not need to update any referencing data models. All cases are
         * handled by reference so refreshing the transition container will update with the name
         * change. */

        /* Mark scenario as dirty */
        onScenarioEditorDirtyEvent(new ScenarioEditorDirtyEvent());
    }

    /**
     * Handles the event for when a scenario object changes its references values. Handles updating
     * the UI appropriately for all references of the modified scenario object.
     *
     * @param refChangedEvent The event containing the scenario object that changed its references
     *        and the old and new reference values.
     */
    @EventHandler
    protected void onReferencesChangedEvent(ReferencesChangedEvent refChangedEvent) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onReferencesChangedEvent(" + refChangedEvent + ")");
        }

        /* Alert the open tabs that a reference has changed so they can update UIs appropriately */
        objectEditorPanel.dispatchEvent(refChangedEvent);

        /* Note: We do not need to update any closed tabs because when the tab is loaded, it will
         * populate itself */

        /* Mark scenario as dirty */
        onScenarioEditorDirtyEvent(new ScenarioEditorDirtyEvent());
    }

    /**
     * Handles a {@link CreateScenarioObjectEvent}. Calls the correct method based on the event's
     * contents.
     *
     * @param event The event containing the scenario object that should be created.
     */
    @EventHandler
    protected void onCreateScenarioObjectEvent(CreateScenarioObjectEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onCreateScenarioObjectEvent(" + event + ")");
        }

        /* The ordering of the steps are IMPORTANT. Do not re-order without thinking of all the
         * ramifications (especially for validation). */

        /* Step 1. Add the newly created scenario object to the backing data model. */
        Serializable obj = event.getScenarioObject();
        if (obj instanceof Task) {
            addTask((Task) obj);
        } else if (obj instanceof Concept) {
            boolean success = addConcept((Concept)obj, event.getParent());
            if (!success) {
                // we do not want to continue with the next steps if the data model wasn't updated
                return;
            }
        } else if (obj instanceof Condition) {
            boolean success = addCondition((Condition) obj, (Concept) event.getParent());
            if (!success) {
                // we do not want to continue with the next steps if the data model wasn't updated
                return;
            }
        } else if (obj instanceof Strategy) {
            addStrategy((Strategy) obj);
        } else if (obj instanceof StateTransition) {
            addStateTransition((StateTransition) obj);
        } else if (obj instanceof LearnerAction) {
            addLearnerAction((LearnerAction) obj);

            /* overwrite obj to AvailableLearnerActions since that is what is used to validate */
            obj = ScenarioClientUtility.getAvailableLearnerActions();
        } else if (obj instanceof Point || obj instanceof Path || obj instanceof Area) {
            addPlaceOfInterest(obj);

            /* overwrite obj to places of interest since that is what is used to validate */
            obj = ScenarioClientUtility.getPlacesOfInterest();
        }

        /* Step 2. Alert the outline that a scenario object has been created so that it can add it
         * to the outline */
        outlineWidget.handleEvent(event);

        /* Step 3. Alert the open tabs that a scenario object has been created so that the object
         * can be added to any relevant drop down lists */
        objectEditorPanel.dispatchEvent(event);

        /* Step 4. Mark scenario as dirty */
        onScenarioEditorDirtyEvent(new ScenarioEditorDirtyEvent(obj));
    }

    /**
     * Adds a {@link Task} to the data model.
     *
     * @param task The {@link Task} that should be added. If null, nothing happens.
     */
    private void addTask(Task task) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addTask(" + task + ")");
        }

        if (task == null) {
            return;
        }

        Tasks tasks = ScenarioClientUtility.getTasks();
        // build task path
        if (tasks == null) {
            Scenario scenario = ScenarioClientUtility.getScenario();
            Assessment assessment = scenario.getAssessment();
            if (assessment == null) {
                assessment = new Assessment();
                scenario.setAssessment(assessment);
            }

            tasks = assessment.getTasks();
            if (tasks == null) {
                tasks = new Tasks();
                assessment.setTasks(tasks);
            }
        }

        tasks.getTask().add(task);
    }

    /**
     * Adds the concept to the parent {@link Task} or {@link Concept}'s data model.
     *
     * @param concept the concept to add
     * @param parentTaskOrConcept the parent of the concept
     * @return true if the concept was successfully added; false otherwise.
     */
    private boolean addConcept(Concept concept, Serializable parentTaskOrConcept) {
        if (concept == null || parentTaskOrConcept == null) {
            return false;
        }

        if (parentTaskOrConcept instanceof Task) {
            Task parentTask = (Task) parentTaskOrConcept;
            if (parentTask.getConcepts() == null) {
                parentTask.setConcepts(new Concepts());
            }

            // Add the concept to the backing data model
            parentTask.getConcepts().getConcept().add(concept);
        } else if (parentTaskOrConcept instanceof Concept) {
            Concept parentConcept = (Concept) parentTaskOrConcept;
            Serializable conditionsOrConcepts = parentConcept.getConditionsOrConcepts();
            if (conditionsOrConcepts == null) {
                parentConcept.setConditionsOrConcepts(new Concepts());
            } else if (conditionsOrConcepts instanceof Conditions) {
                logger.severe("Cannot add concept \"" + concept.getName() + "\" to parent \"" + parentConcept.getName()
                        + "\" because it contains conditions. This should never have been allowed to get this far.");
                return false;
            }

            // Add the concept to the backing data model
            ((Concepts) parentConcept.getConditionsOrConcepts()).getConcept().add(concept);
        }
        
        ScenarioClientUtility.adjustChildRollupRules(parentTaskOrConcept);

        return true;
    }

    /**
     * Adds the condition to the parent {@link Concept}'s data model.
     *
     * @param condition the condition to add
     * @param parentConcept the parent of the condition
     * @return true if the condition was successfully added; false otherwise.
     */
    private boolean addCondition(Condition condition, Concept parentConcept) {
        if (condition == null || parentConcept == null) {
            return false;
        }

            Serializable conditionsOrConcepts = parentConcept.getConditionsOrConcepts();
            if (conditionsOrConcepts == null) {
                parentConcept.setConditionsOrConcepts(new Conditions());
            } else if (conditionsOrConcepts instanceof Concepts) {
                logger.severe("Cannot add condition to parent \"" + parentConcept.getName()
                        + "\" because it contains concepts. This should never have been allowed to get this far.");
                return false;
            }

            // Add the concept to the backing data model
            ((Conditions) parentConcept.getConditionsOrConcepts()).getCondition().add(condition);
            
        ScenarioClientUtility.adjustChildRollupRules(parentConcept);

        return true;
    }

    /**
     * Adds a {@link Strategy} to the data model.
     *
     * @param strategy The {@link Strategy} that should be added. If null, nothing happens.
     */
    private void addStrategy(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addStrategy(" + strategy + ")");
        }

        if (strategy == null) {
            return;
        }

        InstructionalStrategies strategies = ScenarioClientUtility.getStrategies();
        // build strategy path
        if (strategies == null) {
            Scenario scenario = ScenarioClientUtility.getScenario();
            Actions actions = scenario.getActions();
            if (actions == null) {
                actions = new Actions();
                scenario.setActions(actions);
            }

            strategies = actions.getInstructionalStrategies();
            if (strategies == null) {
                strategies = new InstructionalStrategies();
                actions.setInstructionalStrategies(strategies);
            }
        }

        strategies.getStrategy().add(strategy);
    }

    /**
     * Adds a {@link StateTransition} to the data model.
     *
     * @param stateTransition The {@link StateTransition} that should be added. If null, nothing
     *        happens.
     */
    private void addStateTransition(StateTransition stateTransition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addStateTransition(" + stateTransition + ")");
        }

        if (stateTransition == null) {
            return;
        }

        StateTransitions transitions = ScenarioClientUtility.getStateTransitions();
        // build state transition path
        if (transitions == null) {
            Scenario scenario = ScenarioClientUtility.getScenario();
            Actions actions = scenario.getActions();
            if (actions == null) {
                actions = new Actions();
                scenario.setActions(actions);
            }

            transitions = actions.getStateTransitions();
            if (transitions == null) {
                transitions = new StateTransitions();
                actions.setStateTransitions(transitions);
            }
        }

        transitions.getStateTransition().add(stateTransition);
    }

    /**
     * Adds a {@link LearnerAction} to the data model.
     *
     * @param learnerAction The {@link LearnerAction} that should be added. If null, nothing
     *        happens.
     */
    private void addLearnerAction(LearnerAction learnerAction) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addLearnerAction(" + learnerAction + ")");
        }

        if (learnerAction == null) {
            return;
        }

        LearnerActionsList learnerActionsList = ScenarioClientUtility.getLearnerActions();
        if (learnerActionsList == null) {
            final Scenario scenario = ScenarioClientUtility.getScenario();
            if (scenario.getResources() == null) {
                scenario.setResources(new Resources());
            }
            if (scenario.getResources().getAvailableLearnerActions() == null) {
                scenario.getResources().setAvailableLearnerActions(new AvailableLearnerActions());
            }
            learnerActionsList = new LearnerActionsList();
            scenario.getResources().getAvailableLearnerActions().setLearnerActionsList(learnerActionsList);
        }

        List<LearnerAction> learnerActions = learnerActionsList.getLearnerAction();
        if (!learnerActions.contains(learnerAction)) {
            learnerActions.add(learnerAction);
        }
    }

    /**
     * Adds a place of interest to the data model.
     *
     * @param placeOfInterest The place of interest that should be added. If null, nothing
     *        happens.
     */
    private void addPlaceOfInterest(Serializable placeOfInterest) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addPlaceOfInterest(" + placeOfInterest + ")");
        }

        if (placeOfInterest == null) {
            return;
        }

        PlacesOfInterest placesOfInterest = ScenarioClientUtility.getPlacesOfInterest();
        if (placesOfInterest == null) {
            final Scenario scenario = ScenarioClientUtility.getScenario();
            if (scenario.getAssessment() == null) {
                scenario.setAssessment(new Assessment());
            }
            if (scenario.getAssessment().getObjects() == null) {
                scenario.getAssessment().setObjects(new Objects());
            }
            placesOfInterest = new PlacesOfInterest();
            scenario.getAssessment().getObjects().setPlacesOfInterest(placesOfInterest);
        }

        List<Serializable> placesOfInterestList = placesOfInterest.getPointOrPathOrArea();
        if (!placesOfInterestList.contains(placeOfInterest)) {
            placesOfInterestList.add(placeOfInterest);
        }
    }

    /**
     * Handles a {@link DeleteScenarioObjectEvent}. Calls the correct method based on the event's
     * contents.
     *
     * @param event The event containing the scenario object that should be deleted.
     */
    @EventHandler
    protected void onDeleteScenarioObjectEvent(DeleteScenarioObjectEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("onDeleteScenarioObjectEvent(" + event + ")");
        }

        Serializable obj = event.getScenarioObject();

        /* The ordering of the steps are IMPORTANT. Do not re-order without thinking of all the
         * ramifications (especially for validation). */

        /* Step 1. Delete the scenario object from the backing data model. */
        if (obj instanceof Task) {
            Task task = (Task) obj;

            /* Handle the deletion of child concepts first so that they are deleted before we stop
             * referencing them in the scenario */
            if (task.getConcepts() != null) {
                List<Concept> childConcepts = task.getConcepts().getConcept();
                while (!childConcepts.isEmpty()) {
                    onDeleteScenarioObjectEvent(new DeleteScenarioObjectEvent(childConcepts.get(0), task));
                }
            }

            deleteTask(task);
        } else if (obj instanceof Concept) {
            Concept concept = (Concept) obj;

            /* Handle deletion of the concept's children before we stop referencing them in the
             * scenario */
            if (concept.getConditionsOrConcepts() instanceof Concepts) {
                Concepts concepts = (Concepts) concept.getConditionsOrConcepts();
                while (!concepts.getConcept().isEmpty()) {
                    onDeleteScenarioObjectEvent(new DeleteScenarioObjectEvent(concepts.getConcept().get(0), concept));
                }
            } else if (concept.getConditionsOrConcepts() instanceof Conditions) {
                Conditions conditions = (Conditions) concept.getConditionsOrConcepts();
                while (!conditions.getCondition().isEmpty()) {
                    onDeleteScenarioObjectEvent(new DeleteScenarioObjectEvent(conditions.getCondition().get(0), concept));
                    break;
                }
            }

            deleteConcept(concept);
        } else if (obj instanceof Strategy) {
            deleteStrategy((Strategy) obj);
        } else if (obj instanceof StateTransition) {
            deleteStateTransition((StateTransition) obj);
        } else if (obj instanceof Condition) {
            deleteCondition((Condition) obj);
        } else if (obj instanceof Point || obj instanceof Path || obj instanceof Area) {
            deletePlaceOfInterest(obj);
        } else if (obj instanceof LearnerAction) {
            deleteLearnerAction((LearnerAction) obj);
        }

        /* Step 2. Remove deleted scenario object from validation cache */
        ScenarioClientUtility.getValidationCache().dropFromCache(obj);

        /* Step 3. Alert the outline that a scenario object has been deleted so that it can remove
         * it from the outline */
        outlineWidget.handleEvent(event);

        /* Step 4. Alert the open tabs that a scenario object has been deleted so that the object
         * can be removed from any relevant pages */
        objectEditorPanel.dispatchEvent(event);

        /* Step 5. Mark scenario as dirty */
        onScenarioEditorDirtyEvent(new ScenarioEditorDirtyEvent());
    }

    /**
     * Deletes a {@link Task} when a {@link DeleteScenarioObjectEvent} has been raised by a child
     * widget that contains a {@link Task}. Removes the element from the backing {@link Scenario}
     * object.
     *
     * @param task The {@link Task} that should be deleted. If null, nothing happens.
     */
    private void deleteTask(Task task) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deleteTask(" + task + ")");
        }

        if (task == null || task.getNodeId() == null) {
            return;
        }

        /* Remove the references to the task from all scenario objects who reference the task */
        Iterable<Serializable> taskReferencers = ScenarioClientUtility.getReferencesTo(task);
        for (Serializable referenceObj : taskReferencers) {
            if (referenceObj instanceof StateTransition) {
                StateTransition transition = (StateTransition) referenceObj;
                Iterator<Serializable> stateTypeIter = transition.getLogicalExpression().getStateType().iterator();
                while (stateTypeIter.hasNext()) {
                    Serializable stateType = stateTypeIter.next();
                    if (stateType instanceof PerformanceNode) {
                        PerformanceNode performanceNode = (PerformanceNode) stateType;
                        if (performanceNode.getNodeId().equals(task.getNodeId())) {
                            stateTypeIter.remove();
                            break;
                        }
                    }
                }
            } else if (referenceObj instanceof Strategy) {
                Strategy strategy = (Strategy) referenceObj;
                for (Serializable activity : strategy.getStrategyActivities()) {
                    if (activity instanceof PerformanceAssessment) {
                        PerformanceAssessment perfAssess = (PerformanceAssessment) activity;
                        if (perfAssess.getAssessmentType() instanceof PerformanceAssessment.PerformanceNode) {
                            PerformanceAssessment.PerformanceNode perfNode = (PerformanceAssessment.PerformanceNode) perfAssess.getAssessmentType();
                            if (task.getNodeId().equals(perfNode.getNodeId())) {
                                perfNode.setNodeId(null);
                            }
                        }
                    }
                }
            } else if (referenceObj instanceof Task) {
                Task otherTask = (Task) referenceObj;

                if (otherTask.getStartTriggers() != null) {
                    Iterator<StartTriggers.Trigger> triggerItr = otherTask.getStartTriggers().getTrigger().iterator();
                    while (triggerItr.hasNext()) {
                        Serializable triggerType = triggerItr.next().getTriggerType();
                        if (ScenarioClientUtility.doesTriggerTypeContainNodeId(triggerType, task.getNodeId())) {
                            triggerItr.remove();
                        }
                    }
                }

                if (otherTask.getEndTriggers() != null) {
                    Iterator<EndTriggers.Trigger> triggerItr = otherTask.getEndTriggers().getTrigger().iterator();
                    while (triggerItr.hasNext()) {
                        Serializable triggerType = triggerItr.next().getTriggerType();
                        if (ScenarioClientUtility.doesTriggerTypeContainNodeId(triggerType, task.getNodeId())) {
                            triggerItr.remove();
                        }
                    }
                }
            }
        }

        // Remove the task from the Scenario
        Tasks tasks = ScenarioClientUtility.getTasks();
        if (tasks != null) {
            tasks.getTask().remove(task);
        }

        // revalidate the referenced objects
        outlineWidget.revalidateScenarioObjects(taskReferencers);
    }

    /**
     * Deletes a {@link Concept} when a {@link DeleteScenarioObjectEvent} has been raised by a child
     * widget that contains a {@link Concept}. Removes the element from the backing {@link Scenario}
     * object.
     *
     * @param concept The {@link Concept} that should be deleted. If null, nothing happens.
     */
    private void deleteConcept(Concept concept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deleteConcept(" + concept + ")");
        }

        if (concept == null || concept.getNodeId() == null) {
            return;
        }

        /* Remove the references to the concept from all scenario objects who reference the concept
         * and also remove the concept from the Scenario */
        Iterable<Serializable> conceptReferencers = ScenarioClientUtility.getReferencesTo(concept);
        for (Serializable referenceObj : conceptReferencers) {
            if (referenceObj instanceof StateTransition) {
                StateTransition stateTransition = (StateTransition) referenceObj;
                Iterator<Serializable> stateTypeIter = stateTransition.getLogicalExpression().getStateType().iterator();
                while (stateTypeIter.hasNext()) {
                    Serializable perfNodeOrLearnerState = stateTypeIter.next();
                    if (perfNodeOrLearnerState instanceof PerformanceNode) {
                        PerformanceNode perfNode = (PerformanceNode) perfNodeOrLearnerState;
                        if (concept.getNodeId().equals(perfNode.getNodeId())) {
                            stateTypeIter.remove();
                        }
                    }
                }
            } else if (referenceObj instanceof Task) {
                Task task = (Task) referenceObj;

                if (task.getStartTriggers() != null) {
                    Iterator<StartTriggers.Trigger> triggerItr = task.getStartTriggers().getTrigger().iterator();
                    while (triggerItr.hasNext()) {
                        Serializable triggerType = triggerItr.next().getTriggerType();
                        if (ScenarioClientUtility.doesTriggerTypeContainNodeId(triggerType, concept.getNodeId())) {
                            triggerItr.remove();
                        }
                    }
                }

                if (task.getEndTriggers() != null) {
                    Iterator<EndTriggers.Trigger> triggerItr = task.getEndTriggers().getTrigger().iterator();
                    while (triggerItr.hasNext()) {
                        Serializable triggerType = triggerItr.next().getTriggerType();
                        if (ScenarioClientUtility.doesTriggerTypeContainNodeId(triggerType, concept.getNodeId())) {
                            triggerItr.remove();
                        }
                    }
                }

                if (task.getConcepts() != null) {
                    task.getConcepts().getConcept().remove(concept);
                }
            } else if (referenceObj instanceof Concept) {
                Concept parentConcept = (Concept) referenceObj;
                /* No need to type check since we know parentConcept is referencing the concept to
                 * delete. */
                Concepts childConcepts = (Concepts) parentConcept.getConditionsOrConcepts();
                childConcepts.getConcept().remove(concept);
            } else if (referenceObj instanceof Strategy) {
                Strategy strategy = (Strategy) referenceObj;
                for (Serializable activity : strategy.getStrategyActivities()) {
                    if (activity instanceof PerformanceAssessment) {
                        PerformanceAssessment perfAssess = (PerformanceAssessment) activity;
                        if (perfAssess.getAssessmentType() instanceof PerformanceAssessment.PerformanceNode) {
                            PerformanceAssessment.PerformanceNode perfNode = (PerformanceAssessment.PerformanceNode) perfAssess.getAssessmentType();
                            if (concept.getNodeId().equals(perfNode.getNodeId())) {
                                perfNode.setNodeId(null);
                            }
                        }
                    }
                }
            }
        }

        // revalidate the referenced objects
        outlineWidget.revalidateScenarioObjects(conceptReferencers);
    }

    /**
     * Deletes a {@link StateTransition} when a {@link DeleteScenarioObjectEvent} has been raised by
     * a child widget that contains a {@link StateTransition}. Removes the element from the backing
     * {@link Scenario} object.
     *
     * @param stateTransition The {@link StateTransition} that should be deleted. If null, nothing
     *        happens.
     */
    private void deleteStateTransition(StateTransition stateTransition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deleteStateTransition(" + stateTransition + ")");
        }

        if (stateTransition == null) {
            return;
        }

        // Remove the state transition from the Scenario
        StateTransitions stateTransitions = ScenarioClientUtility.getStateTransitions();
        if (stateTransitions != null) {
            stateTransitions.getStateTransition().remove(stateTransition);
        }
    }

    /**
     * Deletes a {@link Strategy} when a {@link DeleteScenarioObjectEvent} has been raised by a
     * child widget that contains a {@link Strategy}. Removes the element from the backing
     * {@link Scenario} object.
     *
     * @param strategy The {@link Strategy} that should be deleted. If null, nothing happens.
     */
    private void deleteStrategy(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deleteStrategy(" + strategy + ")");
        }

        if (strategy == null) {
            return;
        }

        // Remove the strategy from the Scenario
        InstructionalStrategies instructionalStrategy = ScenarioClientUtility.getStrategies();
        if (instructionalStrategy != null) {
            instructionalStrategy.getStrategy().remove(strategy);
        }

        /* Remove the references to the strategy from all scenario objects who reference the
         * strategy */
        Iterable<Serializable> strategyReferencers = ScenarioClientUtility.getReferencesTo(strategy);
        for (Serializable reference : strategyReferencers) {
            
            if (reference instanceof StateTransition) {
                StateTransition stateTransition = (StateTransition) reference;
                if (stateTransition.getStrategyChoices() != null) {
                    Iterator<StrategyRef> strategyTypeIter = stateTransition.getStrategyChoices().getStrategies().iterator();
                    while (strategyTypeIter.hasNext()) {
                        StrategyRef strategyRef = strategyTypeIter.next();
                        if (StringUtils.equals(strategy.getName(), strategyRef.getName())) {
                            strategyTypeIter.remove();
                            break;
                        }
                    }
                }
            }else if(reference instanceof LearnerAction){
                LearnerAction learnerAction = (LearnerAction)reference;
                Serializable actionParams = learnerAction.getLearnerActionParams();
                if (actionParams instanceof generated.dkf.LearnerAction.StrategyReference) {
                    generated.dkf.LearnerAction.StrategyReference strategyRef = (generated.dkf.LearnerAction.StrategyReference)actionParams;
                    if (StringUtils.equals(strategyRef.getName(), strategy.getName())) {
                        strategyRef.setName(null);
                    }
                }
            }else if(reference instanceof Task){
                
                Task task = (Task)reference;
                if (task.getStartTriggers() != null) {
                    for (StartTriggers.Trigger trigger : task.getStartTriggers().getTrigger()) {
                        Serializable triggerType = trigger.getTriggerType();
                        if(triggerType instanceof StrategyApplied){
                            StrategyApplied sApplied = (StrategyApplied)triggerType;
                            sApplied.setStrategyName(null);
                        }
                    }
                }

                if (task.getEndTriggers() != null) {
                    for (EndTriggers.Trigger trigger : task.getEndTriggers().getTrigger()) {
                        Serializable triggerType = trigger.getTriggerType();
                        if(triggerType instanceof StrategyApplied){
                            StrategyApplied sApplied = (StrategyApplied)triggerType;
                            sApplied.setStrategyName(null);
                        }
                    }
                }
            }else if(reference instanceof Scenario.EndTriggers){
                
                Scenario.EndTriggers eTriggers = (Scenario.EndTriggers)reference;
                for(Serializable eTrigger : eTriggers.getTrigger()){
                    if(eTrigger instanceof Scenario.EndTriggers.Trigger){
                        Scenario.EndTriggers.Trigger trigger = (Scenario.EndTriggers.Trigger)eTrigger;
                        Serializable triggerType = trigger.getTriggerType();
                        if(triggerType instanceof StrategyApplied){
                            StrategyApplied sApplied = (StrategyApplied)triggerType;
                            sApplied.setStrategyName(null);
                        }
                    }
                }

            }
        }

        // revalidate the referenced objects
        outlineWidget.revalidateScenarioObjects(strategyReferencers);
    }

    /**
     * Deletes a {@link Condition} when a {@link DeleteScenarioObjectEvent} has been raised. Removes
     * the element from the backing {@link Scenario} object.
     *
     * @param condition The {@link Condition} that should be deleted. If null, nothing happens.
     */
    private void deleteCondition(Condition condition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deleteCondition(" + condition + ")");
        }

        if (condition == null) {
            return;
        }

        // Remove the condition from the concept
        Concept concept = ScenarioClientUtility.getReferencesTo(condition);
        if (concept != null) {
            Conditions conditions = (Conditions) concept.getConditionsOrConcepts();
            conditions.getCondition().remove(condition);

            // allow the author to add concepts again after all the conditions are removed
            if (conditions.getCondition().isEmpty()) {
                concept.setConditionsOrConcepts(null);
            }
        }

        // revalidate the referenced objects
        outlineWidget.revalidateScenarioObject(concept);
    }

    /**
     * Deletes a place of interest when a {@link DeleteScenarioObjectEvent} has been raised. Removes
     * the element from the backing {@link Scenario} object.
     *
     * @param placeOfInterest The place of interest that should be deleted. If null, nothing happens.
     */
    private void deletePlaceOfInterest(Serializable placeOfInterest) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deletePlaceOfInterest(" + placeOfInterest + ")");
        }

        if (placeOfInterest == null) {
            return;
        }

        PlacesOfInterest placesOfInterest = ScenarioClientUtility.getPlacesOfInterest();
        if (placesOfInterest != null) {
            // Remove the place of interest from the Scenario
            placesOfInterest.getPointOrPathOrArea().remove(placeOfInterest);
        }

        /* Note: revalidating the objects referencing the deleted waypoint is handled in
         * ScenarioClientUtility's updateWaypointReferences() method */
    }

    /**
     * Deletes a {@link LearnerAction} when a {@link DeleteScenarioObjectEvent} has been raised. Removes
     * the element from the backing {@link Scenario} object.
     *
     * @param learnerAction The {@link LearnerAction} that should be deleted. If null, nothing happens.
     */
    private void deleteLearnerAction(LearnerAction learnerAction) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("deleteLearnerAction(" + learnerAction + ")");
        }

        if (learnerAction == null) {
            return;
        }

        LearnerActionsList learnerActionsList = ScenarioClientUtility.getLearnerActions();
        if (learnerActionsList != null) {
            // Remove the learnerAction from the Scenario
            final List<LearnerAction> learnerActions = learnerActionsList.getLearnerAction();
            learnerActions.remove(learnerAction);

            /* If the list is now empty, the list in the list in the data model should be set to
             * null. If the list goes from empty to not empty, the list in the data model should be
             * set to a non-null value again. */
            if (learnerActions.isEmpty()) {
                ScenarioClientUtility.getScenario().getResources().getAvailableLearnerActions()
                        .setLearnerActionsList(null);
            }
        }

        final String actionName = learnerAction.getDisplayName();
        Iterable<Serializable> learnerActionReferencers = ScenarioClientUtility.getReferencesTo(learnerAction);
        for (Serializable reference : learnerActionReferencers) {
            if (reference instanceof Task) {
                Task task = (Task) reference;
                if (task.getStartTriggers() != null) {
                    Iterator<StartTriggers.Trigger> triggerItr = task.getStartTriggers().getTrigger().iterator();
                    while (triggerItr.hasNext()) {
                        StartTriggers.Trigger trigger = triggerItr.next();
                        if (trigger.getTriggerType() instanceof LearnerActionReference) {
                            LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                            if (StringUtils.equals(learnerRef.getName(), actionName)) {
                                triggerItr.remove();
                            }
                        }
                    }
                }
                if (task.getEndTriggers() != null) {
                    Iterator<EndTriggers.Trigger> triggerItr = task.getEndTriggers().getTrigger().iterator();
                    while (triggerItr.hasNext()) {
                        EndTriggers.Trigger trigger = triggerItr.next();
                        if (trigger.getTriggerType() instanceof LearnerActionReference) {
                            LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                            if (StringUtils.equals(learnerRef.getName(), actionName)) {
                                triggerItr.remove();
                            }
                        }
                    }
                }
            } else if (reference instanceof Scenario.EndTriggers) {
                Scenario.EndTriggers endTriggers = (Scenario.EndTriggers) reference;
                Iterator<Scenario.EndTriggers.Trigger> triggerItr = endTriggers.getTrigger().iterator();
                while (triggerItr.hasNext()) {
                    Scenario.EndTriggers.Trigger trigger = triggerItr.next();
                    if (trigger.getTriggerType() instanceof LearnerActionReference) {
                        LearnerActionReference learnerRef = (LearnerActionReference) trigger.getTriggerType();
                        if (StringUtils.equals(learnerRef.getName(), actionName)) {
                            triggerItr.remove();
                        }
                    }
                }
            }
        }

        // revalidate the referenced objects
        outlineWidget.revalidateScenarioObjects(learnerActionReferencers);
    }

    /* (non-Javadoc)
     *
     * @see mil.arl.gift.tools.authoring.gat.client.view.DkfView#showConfirmDialog(java.lang.String,
     * mil.arl.gift.tools.authoring.gat.client.view.dialog.OkayCancelCallback) */
    @Override
    public void showConfirmDialog(String msgHtml, String confirmMsg, final OkayCancelCallback callback) {
        OkayCancelDialog.show("Confirm", msgHtml, confirmMsg, callback);
    }

    @Override
    public void setReadOnly(boolean readOnly) {
        DefaultGatFileSelectionDialog.setReadOnly(readOnly);
    }

    @Override
    public ScenarioOutlineEditor getScenarioOutline() {
        return outlineWidget;
    }

    @Override
    public ScenarioObjectEditorPanel getObjectEditorPanel() {
        return objectEditorPanel;
    }

    @Override
    public void hideDkfObjectModal() {
        dkfTransitionDialog.hide();
    }
}