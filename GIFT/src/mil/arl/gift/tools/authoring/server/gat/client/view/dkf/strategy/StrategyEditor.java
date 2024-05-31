/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import java.io.Serializable;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.EnvironmentAdaptation.HighlightObjects;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionEnumType;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.StrategyRef;
import generated.dkf.Task;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.PlaceOfInterestEditedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ReferencesChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchStrategyHandlerClassNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchStrategyHandlerClassNamesResult;

/**
 * An editor that modifies {@link Strategy} scenario objects.
 * 
 * @author nroberts
 */
public class StrategyEditor extends AbstractScenarioObjectEditor<Strategy> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyEditor.class.getName());

    interface StrategyEditorEventBinder extends EventBinder<StrategyEditor> {}

    /**
     * An event binder used to allow this widget to receive events from the shared event bus
     */
    private static final StrategyEditorEventBinder eventBinder = 
            GWT.create(StrategyEditorEventBinder.class);
    

    /** The view being modified by this editor's presenter */
    private StrategyPanel strategyPanel = new StrategyPanel();

    /**
     * Creates a new editor
     */
    public StrategyEditor() {
        
        eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

    }

    @Override
    protected void editObject(Strategy scenarioObject) {
        // ensure that the panel has the handlers before it is displayed
        FetchStrategyHandlerClassNames action = new FetchStrategyHandlerClassNames();
        SharedResources.getInstance().getDispatchService().execute(action,
                new AsyncCallback<FetchStrategyHandlerClassNamesResult>() {
                    @Override
                    public void onSuccess(FetchStrategyHandlerClassNamesResult result) {
                        if (result.isSuccess()) {
                            strategyPanel.setAvailableStrategyHandlers(result.getStrategyHandlerClassNames());
                        } else {
                            // nothing to do but continue
                            logger.severe(
                                    "The TaskEditor failed to retrieve the course surveys. Proceeding without them.");
                        }
                        setWidget(strategyPanel);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.severe(
                                "The TaskEditor failed to retrieve the course surveys. Proceeding without them. Reason: "
                                        + t);

                        // nothing to do but continue
                        setWidget(strategyPanel);
                    }

                });

        strategyPanel.edit(scenarioObject);
        strategyPanel.validateAll();
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        // create event
        if (event instanceof CreateScenarioObjectEvent) {
            CreateScenarioObjectEvent createEvent = (CreateScenarioObjectEvent) event;
            Serializable obj = createEvent.getScenarioObject();
            if (obj instanceof Task || obj instanceof Concept) {
                strategyPanel.addPerformanceNode(obj);
            }else if(obj instanceof LearnerAction){
                
                LearnerAction learnerAction = (LearnerAction)obj;
                if (doesEventValueAffectEditor(learnerAction)) {
                    // this new learner action does reference this strategy
                    strategyPanel.updateReferencedLearnerActions(learnerAction, true);
                }
                
            }else if(obj instanceof StateTransition){
                
                StateTransition stateTransition = (StateTransition)obj;
                for(StrategyRef strategRef : stateTransition.getStrategyChoices().getStrategies()){
                    if (doesEventValueAffectEditor(strategRef)) {
                        // this new state transition does reference this strategy
                        strategyPanel.updateReferencedStateTransitions(stateTransition, true);
                        break;
                    }
                }
            }else if(obj instanceof Task) {
                
            	Task task = (Task)obj;
                strategyPanel.updateTasks(task, true);
            }

            // delete event
        } else if (event instanceof DeleteScenarioObjectEvent) {
            DeleteScenarioObjectEvent deleteEvent = (DeleteScenarioObjectEvent) event;
            Serializable obj = deleteEvent.getScenarioObject();
            if (obj instanceof StateTransition) {
                strategyPanel.removeStateTransition((StateTransition) obj);
            } else if (obj instanceof Task) {
                strategyPanel.removeTasks((Task) obj);
            } else if (obj instanceof Concept) {
                strategyPanel.removePerformanceNode(((Concept) obj).getNodeId());
            } else if(obj instanceof LearnerAction){
                strategyPanel.removeLearnerAction((LearnerAction)obj);
            }

            // rename event
        } else if (event instanceof RenameScenarioObjectEvent) {
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent) event;
            Serializable scenarioObject = renameEvent.getScenarioObject();
            if (scenarioObject instanceof Task) {
            	strategyPanel.refreshReferencedTasks();
            } else if (scenarioObject instanceof Concept) {
                Concept concept = (Concept) scenarioObject;
                strategyPanel.handleTaskOrConceptRename(concept.getNodeId(), renameEvent.getNewName());
            } else if (scenarioObject instanceof StateTransition) {
                strategyPanel.refreshReferencedStateTransitions();
            } else if(scenarioObject instanceof LearnerAction){
                strategyPanel.refreshReferencedLearnerActions();
            }else if(scenarioObject instanceof HighlightObjects){
                strategyPanel.handleHighlightObjectRename(((HighlightObjects)scenarioObject).getName(), renameEvent.getNewName());
            }

            // reference changed event
        } else if (event instanceof ReferencesChangedEvent) {
            ReferencesChangedEvent refChangedEvent = (ReferencesChangedEvent) event;
            Serializable source = refChangedEvent.getReferenceChangedSource();

            if (source instanceof StateTransition) {
                StateTransition transition = (StateTransition) source;
                
                // test old value
                if (doesEventValueAffectEditor(refChangedEvent.getOldValue())) {
                    strategyPanel.updateReferencedStateTransitions(transition, false);
                }

                // test new value
                if (doesEventValueAffectEditor(refChangedEvent.getNewValue())) {
                    strategyPanel.updateReferencedStateTransitions(transition, true);
                }
            }else if (source instanceof LearnerAction) {
                LearnerAction learnerAction = (LearnerAction) source;
                
                // test old value
                if (doesEventValueAffectEditor(refChangedEvent.getOldValue())) {
                    strategyPanel.updateReferencedLearnerActions(learnerAction, false);
                }

                // test new value
                if (doesEventValueAffectEditor(refChangedEvent.getNewValue())) {
                    strategyPanel.updateReferencedLearnerActions(learnerAction, true);
                }
            }else if (source instanceof Task) {
            	Task task = (Task) source;
            	
                // test old value
                if (doesEventValueAffectEditor(refChangedEvent.getOldValue())) {
                    strategyPanel.updateTasks(task, false);
                }

                // test new value
                if (doesEventValueAffectEditor(refChangedEvent.getNewValue())) {
                    strategyPanel.updateTasks(task, true);            
                }
            }
        }
    }

    /**
     * Handles when the author has finished editing a place of interest
     * 
     * @param event the place of interest that was edited
     */
    @EventHandler
    protected void onPlaceOfInterestEdited(PlaceOfInterestEditedEvent event) {
        
        if(event.getPlace() != null) {
            
            Serializable eventObj = event.getPlace();
            strategyPanel.handlePlaceOfInterestChange(eventObj);
        }
    }

    /**
     * Determines if the provided value affects this editor.
     * 
     * @param value the value to check.
     * @return true if the value can match the {@link Strategy} name being edited; false otherwise.
     */
    private boolean doesEventValueAffectEditor(Serializable value) {
        String name = null;
        if (value instanceof StrategyRef) {
            name = ((StrategyRef) value).getName();
        } else if (value instanceof Strategy) {
            name = ((Strategy) value).getName();
        } else if(value instanceof LearnerAction){
            LearnerAction learnerAction = (LearnerAction)value;
            if(LearnerActionEnumType.APPLY_STRATEGY.equals(learnerAction.getType())){
                Serializable actionParams = learnerAction.getLearnerActionParams();
                if(actionParams instanceof generated.dkf.LearnerAction.StrategyReference){
                    name = ((generated.dkf.LearnerAction.StrategyReference)actionParams).getName();
                }
            }
        }else if(value instanceof String){
            name = (String) value;
        }

        if (StringUtils.isNotBlank(name) && StringUtils.equals(name, strategyPanel.getSelectedStrategyName())) {
            return true;
        }
        
        return false;
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return strategyPanel;
    }
}
