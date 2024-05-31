/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.LearnerAction;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceNode;
import generated.dkf.Strategy;
import generated.dkf.Task;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.LearnerStartLocationUpdatedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ReferencesChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.task.TaskPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchStrategyHandlerClassNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchStrategyHandlerClassNamesResult;

/**
 * An editor that modifies {@link Task} scenario objects.
 * 
 * @author nroberts
 */
public class TaskEditor extends AbstractScenarioObjectEditor<Task> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TaskEditor.class.getName());

    /** The view being modified by this editor's presenter */
    private TaskPanel taskPanel = new TaskPanel();

    /** Creates a new editor */
    public TaskEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("TaskEditor()");
        }
    }

    @Override
    protected void editObject(Task scenarioObject) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editObject(" + scenarioObject + ")");
        }
        
        // ensure that the panel has the handlers before it is displayed
        // -- needed for the task start trigger optional strategy sets
        FetchStrategyHandlerClassNames action = new FetchStrategyHandlerClassNames();
        SharedResources.getInstance().getDispatchService().execute(action,
                new AsyncCallback<FetchStrategyHandlerClassNamesResult>() {
                    @Override
                    public void onSuccess(FetchStrategyHandlerClassNamesResult result) {
                        if (result.isSuccess()) {
                        	taskPanel.setAvailableStrategyHandlers(result.getStrategyHandlerClassNames());
                        } else {
                            // nothing to do but continue
                            logger.severe(
                                    "The TaskEditor failed to retrieve the course surveys. Proceeding without them.");
                        }
                        setWidget(taskPanel);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        logger.severe(
                                "The TaskEditor failed to retrieve the course surveys. Proceeding without them. Reason: "
                                        + t);

                        // nothing to do but continue
                        setWidget(taskPanel);
                    }

                });

        taskPanel.edit(scenarioObject);
        taskPanel.validateAll();

    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("processEvent(" + event + ")");
        }

        // create event
        if (event instanceof CreateScenarioObjectEvent) {
            CreateScenarioObjectEvent createEvent = (CreateScenarioObjectEvent) event;
            Serializable obj = createEvent.getScenarioObject();
            if (obj instanceof Task || obj instanceof Concept) {
                taskPanel.addPerformanceNode(obj);
            }else if(obj instanceof Condition){
                taskPanel.addCondition((Condition) obj, (Concept) createEvent.getParent());
            }else if(obj instanceof Strategy){
                taskPanel.addStrategy((Strategy)obj);
            }

          // delete event
        } else if (event instanceof DeleteScenarioObjectEvent) {
            DeleteScenarioObjectEvent deleteEvent = (DeleteScenarioObjectEvent) event;
            Serializable obj = deleteEvent.getScenarioObject();
            if (obj instanceof Task) {
            	taskPanel.removePerformanceNode(((Task)obj).getNodeId());
            }else if (obj instanceof Concept) {
            	taskPanel.removePerformanceNode(((Concept)obj).getNodeId());
            }else if(obj instanceof LearnerAction) {
                taskPanel.removeLearnerAction((LearnerAction)obj);
            } else if (obj instanceof Strategy) {
                taskPanel.removeAction((Strategy) obj);
                taskPanel.checkRefs(((Strategy) obj).getName());
            } else if (obj instanceof StateTransition) {
                taskPanel.removeStateTransition((StateTransition) obj);
            } else if(obj instanceof Condition){                
                BigInteger parentConceptNodeId = ((Concept)deleteEvent.getParent()).getNodeId();
                taskPanel.removeCondition(parentConceptNodeId);
            }

            // rename event
        } else if (event instanceof RenameScenarioObjectEvent) {
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent) event;
            Serializable obj = renameEvent.getScenarioObject();                
            if (obj instanceof Task || obj instanceof Concept || obj instanceof LearnerAction || obj instanceof Strategy) {
                taskPanel.handleTriggerItemRename(obj, renameEvent.getOldName(), renameEvent.getNewName());
                if (doesEventValueAffectEditor(obj)) {
                    taskPanel.onRename(renameEvent.getNewName());
                }
                
                if (obj instanceof Strategy) {
                    taskPanel.refreshReferencedActions();
                }
            } else if (obj instanceof StateTransition) {
                taskPanel.refreshReferencedStateTransitions();
            }

            // reference changed event
        } else if (event instanceof ReferencesChangedEvent) {
            ReferencesChangedEvent refChangedEvent = (ReferencesChangedEvent) event;
            Serializable source = refChangedEvent.getReferenceChangedSource();

            if (source instanceof Strategy) {
                // Strategy changed its references
                Strategy strategy = (Strategy) source;

                // test old value
                if (doesEventValueAffectEditor(refChangedEvent.getOldValue())) {
                    taskPanel.updateReferencedActions(strategy, false);
                }

                // test new value
                if (doesEventValueAffectEditor(refChangedEvent.getNewValue())) {
                    taskPanel.updateReferencedActions(strategy, true);
                }

            } else if (source instanceof StateTransition) {
                // State Transition changed its references
                StateTransition transition = (StateTransition) source;

                // test old value
                if (doesEventValueAffectEditor(refChangedEvent.getOldValue())) {
                    taskPanel.updateReferencedStateTransitions(transition, false);
                }

                // test new value
                if (doesEventValueAffectEditor(refChangedEvent.getNewValue())) {
                    taskPanel.updateReferencedStateTransitions(transition, true);
                }
            }

            // learner start location updated event
        } else if (event instanceof LearnerStartLocationUpdatedEvent) {
            taskPanel.handleLearnerStartLocationUpdate();
        } else if(event instanceof ScenarioEditorDirtyEvent){
            
            ScenarioEditorDirtyEvent scenarioEditorDirtyEvent = (ScenarioEditorDirtyEvent)event;
            if(scenarioEditorDirtyEvent.getSourceScenarioObject() instanceof Condition){
                taskPanel.handleChangeInCondition((Condition) scenarioEditorDirtyEvent.getSourceScenarioObject());
            }
        }
    }

    /**
     * Determines if the provided value affects this editor.
     * 
     * @param value the value to check.
     * @return true if the value can match the {@link Task} node id being edited; false otherwise.
     */
    private boolean doesEventValueAffectEditor(Serializable value) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("doesEventValueAffectEditor(");
            List<Object> params = Arrays.<Object>asList(value);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        BigInteger nodeId = null;
        if (value instanceof PerformanceNode) {
            nodeId = ((PerformanceNode) value).getNodeId();
        } else if (value instanceof PerformanceAssessment.PerformanceNode) {
            nodeId = ((PerformanceAssessment.PerformanceNode) value).getNodeId();
        } else if (value instanceof Task) {
            nodeId = ((Task) value).getNodeId();
        }

        if (nodeId != null) {
            if (nodeId.equals(taskPanel.getSelectedTaskNodeId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return taskPanel;
    }
}
