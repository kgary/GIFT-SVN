/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.task;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.PanelHeader;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.ConceptAssessment;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.EndTriggers;
import generated.dkf.LearnerAction;
import generated.dkf.LearnerActionReference;
import generated.dkf.StartTriggers;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.Task;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.validation.ModelValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ReferencesChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.ScenarioTriggerUtil;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.AdditionalAssessmentWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.AssessmentRollupWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.CourseConceptDisplayWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.MiscAttributesWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.StateTransitionReferenceWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent.ListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.model.dkf.PerfNodeIdNamePair;

/**
 * The Class TaskPanel.
 */
public class TaskPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(TaskPanel.class.getName());

    /** The ui binder. */
    private static TaskPanelUiBinder uiBinder = GWT.create(TaskPanelUiBinder.class);

    /** The Interface DkfTaskPanelUiBinder */
    interface TaskPanelUiBinder extends UiBinder<Widget, TaskPanel> {
    }

    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);

    /** Displays any course concept data related to this task, if applicable */
    @UiField
    protected CourseConceptDisplayWidget courseConcept;

    /** The collapse panel header */
    @UiField
    protected PanelHeader triggerPanelHeader;

    /** The collapse panel for the start and end triggers */
    @UiField
    protected Collapse triggerPanelCollapse;

    /** Widget to allow the user to create a new start trigger */
    private AddStartTriggerWidget addStartTriggerWidget = new AddStartTriggerWidget();

    /** Widget to allow the user to create a new end trigger */
    private AddEndTriggerWidget addEndTriggerWidget = new AddEndTriggerWidget();
    
    /** Stores the name of the old start trigger task that was changed */
	private String oldStartTrigger;
	
    /** Stores the name of the old end trigger task that was changed */
	private String oldEndTrigger;



    /** The list editor for the start triggers */
    @UiField(provided = true)
    protected ItemListEditor<StartTriggers.Trigger> startTriggerListEditor = new ItemListEditor<StartTriggers.Trigger>(
            addStartTriggerWidget) {
    	
    	@Override
		public void editExisting(int index) {
    		super.editExisting(index);
    		if(getItems().get(index).getTriggerType() instanceof StrategyApplied) {
    			oldStartTrigger = ((StrategyApplied) getItems().get(index).getTriggerType()).getStrategyName();
    		} else {
    			oldStartTrigger = null;
    		}
    	};
    };

    /** The list editor for the end triggers */
    @UiField(provided = true)
    protected ItemListEditor<EndTriggers.Trigger> endTriggerListEditor = new ItemListEditor<EndTriggers.Trigger>(
            addEndTriggerWidget) {
    	
    	@Override
    	public void editExisting(int index) {
    		super.editExisting(index);
    		if(getItems().get(index).getTriggerType() instanceof StrategyApplied) {
    			oldEndTrigger = ((StrategyApplied) getItems().get(index).getTriggerType()).getStrategyName();
    		} else {
    			oldEndTrigger = null;
    		}
    	};
    };

    /**
     * Control that displays each of {@link StateTransitions} that references the {@link Task} that
     * this {@link TaskPanel} references
     */
    @UiField
    protected StateTransitionReferenceWidget referencedStateTransitions;

    /** The optional additional assessments for the task */
    @UiField
    protected AdditionalAssessmentWidget additionalAssessments;
    
    /** An editor used to modify the concept's assessment rollup rules */
    @UiField
    protected AssessmentRollupWidget assessmentRollup;
    
    /** The optional misc attributes for the task */
    @UiField
    protected MiscAttributesWidget miscAttributes;

    /** The task that is being edited in this panel */
    private Task selectedTask;

    /** The state transition's table description label */
    private static final String STATE_TRANSITION_TABLE_DESCRIPTION = "The concepts covered by this task "
            + "will be used to assess the learner's performance in this task as they complete it. "
            + "The assessment for this task will be determined based on the "
            + "assessments that the learner received for any of the conditions or concepts covered by this "
            + "task. <br/><br/> Whenever the assessment for this task changes, the "
            + "following state transitions will determine what actions should be taken.";

    /** Message to be displayed when the {@link Task} has no children. */
    private static final String NO_CHILD_VALIDATION_MESSAGE = "The task must contain at least one concept. Please add a concept for this Task.";

    /** The container for showing validation messages for the task not having children. */
    private final ModelValidationStatus childValidationStatus = new ModelValidationStatus(NO_CHILD_VALIDATION_MESSAGE) {
        @Override
        protected void fireJumpToEvent(Serializable modelObject) {
            ScenarioEventUtility.fireJumpToEvent(modelObject);
        }
    };

    /** The container for showing validation messages for the task not having any end triggers. */
    private final WidgetValidationStatus endTriggerValidationStatus;

    /**
     * Instantiates a new dkf task panel.
     */
    public TaskPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        triggerPanelHeader.addDomHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (triggerPanelCollapse.isShown()) {
                    triggerPanelCollapse.hide();
                } else {
                    triggerPanelCollapse.show();
                }
            }
        }, ClickEvent.getType());

        startTriggerListEditor.setFields(Arrays.asList(buildStartTriggerItemField()));
        startTriggerListEditor.addCreateListAction("Click here to add a new start event",
                buildStartTriggerCreateAction());
        startTriggerListEditor.setRemoveItemDialogTitle("Delete Task Start Event");
        startTriggerListEditor.setRemoveItemStringifier(new Stringifier<StartTriggers.Trigger>() {
            @Override
            public String stringify(StartTriggers.Trigger obj) {
                
                String desc = ScenarioTriggerUtil.buildTriggerDescription(obj);

                return "the task start event of "+bold(SafeHtmlUtils.fromTrustedString(desc)).asString();
            }
        });
		startTriggerListEditor.addListChangedCallback(new ListChangedCallback<StartTriggers.Trigger>() {
			@Override
			public void listChanged(ListChangedEvent<StartTriggers.Trigger> event) {

				for (StartTriggers.Trigger trigger : event.getAffectedItems()) {
					Serializable triggerType = trigger.getTriggerType();
					if (triggerType instanceof StrategyApplied) {
						String currStrategyRef = ((StrategyApplied) triggerType).getStrategyName();
						if (event.getActionPerformed() == ListAction.ADD) {
							ScenarioEventUtility.fireReferencesChangedEvent(selectedTask, null, currStrategyRef);
						} else if (event.getActionPerformed() == ListAction.REMOVE) {
							ScenarioEventUtility.fireReferencesChangedEvent(selectedTask, currStrategyRef, null);
						} else if (event.getActionPerformed() == ListAction.EDIT) {
							ScenarioEventUtility.fireReferencesChangedEvent(selectedTask, oldStartTrigger, currStrategyRef);
						}
					}
				}
				// need to revalidate outline
				ScenarioEventUtility.fireDirtyEditorEvent(selectedTask);
			}
		});

        /* Strategies are so nested in this editor that it is best to revalidate
         * even when cancelled */
        startTriggerListEditor.addEditCancelledCallback(new EditCancelledCallback() {
            @Override
            public void editCancelled() {
                /* Need to revalidate outline */
                ScenarioEventUtility.fireDirtyEditorEvent(selectedTask);
            }
        });

        endTriggerListEditor.setFields(Arrays.asList(buildEndTriggerItemField()));
        Widget createEndTriggerButton = endTriggerListEditor.addCreateListAction("Click here to add a new stop event",
                buildEndTriggerCreateAction());
        endTriggerListEditor.addListChangedCallback(new ListChangedCallback<EndTriggers.Trigger>() {
            @Override
            public void listChanged(ListChangedEvent<EndTriggers.Trigger> event) {
            	
				for (EndTriggers.Trigger trigger : event.getAffectedItems()) {
					Serializable triggerType = trigger.getTriggerType();
					if (triggerType instanceof StrategyApplied) {
						String currStrategyRef = ((StrategyApplied) triggerType).getStrategyName();
						if (event.getActionPerformed() == ListAction.ADD) {
							ScenarioEventUtility.fireReferencesChangedEvent(selectedTask, null, currStrategyRef);
						} else if (event.getActionPerformed() == ListAction.REMOVE) {
							ScenarioEventUtility.fireReferencesChangedEvent(selectedTask, currStrategyRef, null);
						} else if (event.getActionPerformed() == ListAction.EDIT) {
							ScenarioEventUtility.fireReferencesChangedEvent(selectedTask, oldEndTrigger, currStrategyRef);
						}
					}
				}
                requestValidationAndFireDirtyEvent(selectedTask, endTriggerValidationStatus);
            }
        });
        endTriggerListEditor.setRemoveItemDialogTitle("Delete Task End Event");
        endTriggerListEditor.setRemoveItemStringifier(new Stringifier<EndTriggers.Trigger>() {
            @Override
            public String stringify(EndTriggers.Trigger obj) {
                
                String desc = ScenarioTriggerUtil.buildTriggerDescription(obj);
                return "the task end event of "+bold(SafeHtmlUtils.fromTrustedString(desc)).asString();
            }
        });

        referencedStateTransitions.setHelpText(STATE_TRANSITION_TABLE_DESCRIPTION);

        updateReadonly(ScenarioClientUtility.isReadOnly());

        endTriggerValidationStatus = new WidgetValidationStatus(createEndTriggerButton,
                "The task must contain at least one stop event.");

        // needs to be called last
        initValidationComposite(validations);
    }

    /**
     * Builds the item field for the start trigger table.
     * 
     * @return the {@link ItemField} for the start trigger description column.
     */
    private ItemField<StartTriggers.Trigger> buildStartTriggerItemField() {
        return new ItemField<StartTriggers.Trigger>(null, "100%") {
            @Override
            public Widget getViewWidget(StartTriggers.Trigger item) {
                return new HTML(ScenarioTriggerUtil.buildTriggerDescription(item));
            }
        };
    }

    /**
     * Builds the create action for the start trigger table.
     * 
     * @return the {@link CreateListAction} for the start trigger table.
     */
    private CreateListAction<StartTriggers.Trigger> buildStartTriggerCreateAction() {
        return new CreateListAction<StartTriggers.Trigger>() {
            @Override
            public StartTriggers.Trigger createDefaultItem() {
                StartTriggers.Trigger startTrigger = new StartTriggers.Trigger();

                // get concept dropdown first item
                PerfNodeIdNamePair conceptPair = addStartTriggerWidget.getFirstConcept();

                // set default if a concept exists
                if (conceptPair != null) {
                    ConceptAssessment assessment = new ConceptAssessment();
                    assessment.setConcept(conceptPair.getId());
                    assessment.setResult(AssessmentLevelEnum.BELOW_EXPECTATION.getName());
                    startTrigger.setTriggerType(assessment);
                }

                return startTrigger;
            }
        };
    }

    /**
     * Builds the item field for the end trigger table.
     * 
     * @return the {@link ItemField} for the end trigger description column.
     */
    private ItemField<EndTriggers.Trigger> buildEndTriggerItemField() {
        return new ItemField<EndTriggers.Trigger>(null, "100%") {
            @Override
            public Widget getViewWidget(EndTriggers.Trigger item) {
                return new HTML(ScenarioTriggerUtil.buildTriggerDescription(item));
            }
        };
    }

    /**
     * Builds the create action for the end trigger table.
     * 
     * @return the {@link CreateListAction} for the end trigger table.
     */
    private CreateListAction<EndTriggers.Trigger> buildEndTriggerCreateAction() {
        return new CreateListAction<EndTriggers.Trigger>() {
            @Override
            public EndTriggers.Trigger createDefaultItem() {
                EndTriggers.Trigger endTrigger = new EndTriggers.Trigger();
                
                if(selectedTask != null){
                    // if the task has been set, then provide that to the task selector
                    // widget so it is filtered out
                    addEndTriggerWidget.setTaskNameToIgnore(selectedTask.getName());
                    addStartTriggerWidget.setTaskNameToIgnore(selectedTask.getName());
                }

                // get concept dropdown first item
                PerfNodeIdNamePair conceptPair = addEndTriggerWidget.getFirstConcept();

                ConceptAssessment assessment = new ConceptAssessment();
                if (conceptPair != null) {
                    assessment.setConcept(conceptPair.getId());
                }
                assessment.setResult(AssessmentLevelEnum.BELOW_EXPECTATION.getName());
                endTrigger.setTriggerType(assessment);

                return endTrigger;
            }
        };
    }

    /**
     * Populates the panel using the data within the given {@link Task}.
     * 
     * @param task the data object that will be used to populate the panel.
     */
    public void edit(Task task) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + task + ")");
        }

        if (task == null) {
            throw new IllegalArgumentException("The 'task' parameter can't be null");
        }

        courseConcept.load(task);

        // load the current survey context into the survey assessment area
        additionalAssessments.loadSurveyContext(ScenarioClientUtility.getSurveyContextId());

        // replace nulls with empty objects.
        if (task.getStartTriggers() == null) {
            task.setStartTriggers(new StartTriggers());
        }
        if (task.getEndTriggers() == null) {
            task.setEndTriggers(new EndTriggers());
        }
        if (task.getConcepts() == null) {
            task.setConcepts(new Concepts());
        }

        this.selectedTask = task;
        startTriggerListEditor.setRemoveItemDialogTitle("Delete "+(selectedTask.getName() != null ? selectedTask.getName() : "Task") + " Start Event");
        endTriggerListEditor.setRemoveItemDialogTitle("Delete "+(selectedTask.getName() != null ? selectedTask.getName() : "Task") + " End Event");

        // set list into the trigger editor
        startTriggerListEditor.setItems(task.getStartTriggers().getTrigger());

        // set list into the trigger editor
        endTriggerListEditor.setItems(task.getEndTriggers().getTrigger());

        referencedStateTransitions.showTransitions(task);
        additionalAssessments.showAdditionalAssessments(task);
        
        miscAttributes.edit(task);
        assessmentRollup.edit(task);

        updateReadonly(ScenarioClientUtility.isReadOnly());
    }

    /**
     * Sets the the local {@link Task} with the new name.
     * 
     * @param newTaskName the new name.
     */
    public void onRename(String newTaskName) {
        selectedTask.setName(newTaskName);
        additionalAssessments.onRename(selectedTask);
        addEndTriggerWidget.setTaskNameToIgnore(newTaskName);
        addStartTriggerWidget.setTaskNameToIgnore(selectedTask.getName());
    }

    /**
     * Revalidates the invalid list editor rows because they could contain the learner start
     * location and cause the row to become valid.
     */
    public void handleLearnerStartLocationUpdate() {
        startTriggerListEditor.revalidateInvalidItems();
        endTriggerListEditor.revalidateInvalidItems();
    }

    /**
     * Handles the deletion of a certain {@link StateTransition} by removing it from
     * {@link #referencedStateTransitions} if it was contained there.
     * 
     * @param transition The transition to remove if it is found within
     *        {@link #referencedStateTransitions}. If it isn't, nothing happens.
     */
    public void removeStateTransition(StateTransition transition) {
        referencedStateTransitions.remove(transition);
    }

    /**
     * Updates the list of referenced state transitions with the selected task.
     * 
     * @param transition the transition to add or remove from the references table.
     * @param add true to add the transition to the refrences table; false to remove it.
     */
    public void updateReferencedStateTransitions(StateTransition transition, boolean add) {
        if (add) {
            referencedStateTransitions.add(transition);
        } else {
            referencedStateTransitions.remove(transition);
        }
    }

    /**
     * Refreshes the referenced state transitions table and redraws the items in the list.
     */
    public void refreshReferencedStateTransitions() {
        referencedStateTransitions.refresh();
    }

    /**
     * Handles the deletion of a certain {@link Strategy} by removing it from
     * {@link #additionalAssessments} if it was contained there.
     * 
     * @param strategy The {@link Strategy} to remove if it is found within
     *        {@link #additionalAssessments}. If it isn't, nothing happens.
     */
    public void removeAction(Strategy strategy) {
        additionalAssessments.removeAction(strategy);
        
        addStartTriggerWidget.handleRemovedStrategy(strategy);
    }

    /**
     * Updates the list of referenced strategies with the selected task.
     * 
     * @param strategy the strategy to add or remove from the references table.
     * @param add true to add the strategy to the refrences table; false to remove it.
     */
    public void updateReferencedActions(Strategy strategy, boolean add) {
        if (add) {
            additionalAssessments.add(strategy);
        } else {
            additionalAssessments.remove(strategy);
        }
    }

    /**
     * Refreshes the referenced action table and redraws the items in the list.
     */
    public void refreshReferencedActions() {
        additionalAssessments.refreshActionReferences();
    }

    /**
     * Refreshes the trigger tables for the renamed {@link Task}, {@link Concept}, or
     * {@link LearnerAction}
     * 
     * @param triggerItem the item that was renamed.
     * @param oldName the old name of the item.
     * @param newName the new name of the item.
     */
    public void handleTriggerItemRename(Serializable triggerItem, String oldName, String newName) {
        if (triggerItem == null) {
            throw new IllegalArgumentException("The parameter 'triggerItem' cannot be null.");
        } else if (!(triggerItem instanceof Task || triggerItem instanceof Concept
                || triggerItem instanceof LearnerAction || triggerItem instanceof Strategy)) {
            throw new IllegalArgumentException(
                    "The parameter 'triggerItem' must be of type Task, Concept, Strategy Applied or Learner Action.");
        }
        
        boolean isLearnerAction = triggerItem instanceof LearnerAction;
        boolean isStrategy = triggerItem instanceof Strategy;
        boolean isTaskOrConcept = triggerItem instanceof Task || triggerItem instanceof Concept;

        BigInteger nodeIdToCheck = null;

        // get node id
        if (isTaskOrConcept) {
            nodeIdToCheck = triggerItem instanceof Task ? ((Task) triggerItem).getNodeId()
                    : ((Concept) triggerItem).getNodeId();
        }

        // apply rename to start triggers that match the node id
        for (StartTriggers.Trigger trigger : startTriggerListEditor.getItems()) {
            /* the editor can have multiple of the same trigger type so we can't break out */
            if (isLearnerAction) {
                LearnerActionReference learnerRef = ScenarioTriggerUtil.doesTriggerMatchLearnerActionName(trigger,
                        oldName);
                if (learnerRef != null) {
                    learnerRef.setName(newName);
                    startTriggerListEditor.refresh(trigger);
                }
            }else if(isStrategy){
                StrategyApplied sApplied = ScenarioTriggerUtil.doesTriggerMatchStrategyName(trigger, oldName);
                if(sApplied != null){
                    sApplied.setStrategyName(newName);
                    startTriggerListEditor.refresh(trigger);
                }
                
            } else if (ScenarioTriggerUtil.doesTriggerMatchNodeId(trigger, nodeIdToCheck)) {
                startTriggerListEditor.refresh(trigger);
            }
        }

        // apply rename to end triggers that match the node id
        for (EndTriggers.Trigger trigger : endTriggerListEditor.getItems()) {
            /* the editor can have multiple of the same trigger type so we can't break out */
            if (isLearnerAction) {
                LearnerActionReference learnerRef = ScenarioTriggerUtil.doesTriggerMatchLearnerActionName(trigger,
                        oldName);
                if (learnerRef != null) {
                    learnerRef.setName(newName);
                    endTriggerListEditor.refresh(trigger);
                }
                
            }else if(isStrategy){
                StrategyApplied sApplied = ScenarioTriggerUtil.doesTriggerMatchStrategyName(trigger, oldName);
                if(sApplied != null){
                    sApplied.setStrategyName(newName);
                    endTriggerListEditor.refresh(trigger);
                }
            } else if (ScenarioTriggerUtil.doesTriggerMatchNodeId(trigger, nodeIdToCheck)) {
                endTriggerListEditor.refresh(trigger);
            }
        }
        
        if(nodeIdToCheck != null) {
        	addStartTriggerWidget.handleTaskOrConceptRename(nodeIdToCheck, newName);
        }
    }
    
    /**
     * Populates the dropdown of strategy handlers with the provided class name list.
     *
     * @param handlerClassNames the list of handlers
     */
    public void setAvailableStrategyHandlers(List<String> handlerClassNames) {
    	addStartTriggerWidget.setAvailableStrategyHandlers(handlerClassNames);
    }
    
    /**
     * Notification that a condition was removed from the DKF.
     * 
     * @param parentConceptNodeId if known, the node id of the concept that has the condition
     * being removed as its direct child.  Can be null if the parent concept, somehow is not known.
     * Keep in mind that this will cause all triggers to be refreshed instead of just the triggers
     * that directly reference the concept.
     */
    public void removeCondition(BigInteger parentConceptNodeId){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeCondition(" + parentConceptNodeId + ")");
        }
        logger.warning("removeCondition - "+parentConceptNodeId);
        
        addEndTriggerWidget.removeCondition(parentConceptNodeId);
        
        handleTriggerItemDeleted();
    }
    
    /**
     * Notification that a condition was changed (e.g. replaced with another condition)
     * 
     * @param condition the condition that was changed
     */
    public void handleChangeInCondition(Condition condition){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleChangeInCondition(" + condition + ")");
        }
        
        addEndTriggerWidget.handleChangeInCondition(condition);
        
        endTriggerListEditor.redrawListEditor(false);

        requestValidation(childValidationStatus);
    }
    
    /**
     * Removes a performance node as an option from the
     * {@link #addStartTriggerWidget} and the trigger editors.
     *
     * @param nodeId The {@link BigInteger} id of the performance node to remove
     *        from the {@link #addStartTriggerWidget}. Can be null.
     */
    public void removePerformanceNode(BigInteger nodeId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removePerformanceNode(" + nodeId + ")");
        }
        
        addStartTriggerWidget.removePerformanceNode(nodeId);
        addEndTriggerWidget.removePerformanceNode(nodeId);
        
        handleTriggerItemDeleted();
    }
    
    /**
     * Removes a {@link LearnerAction} from trigger editors.
     * 
     * @param learnerAction the learner action being removed
     */
    public void removeLearnerAction(LearnerAction learnerAction) {
    	handleTriggerItemDeleted();
    }

    /**
     * Handles when a {@link Task}, {@link Concept}, or {@link LearnerAction} is deleted. Removes
     * the deleted item from the trigger editors.
     */
    private void handleTriggerItemDeleted() {
        /* The deleted item was already removed from the lists by reference. Just rebuild the tables
         * to reflect the change. Do not need to force rebuild. */
        startTriggerListEditor.redrawListEditor(false);
        endTriggerListEditor.redrawListEditor(false);        

        requestValidation(childValidationStatus);
        
    }
    
    /**
     * Adds a new option to the list of tasks and concepts using the provided {@link Task} or
     * {@link Concept}.
     *
     * @param taskOrConcept the {@link Task} or {@link Concept} to add.
     */
    public void addPerformanceNode(Serializable taskOrConcept) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addPerformanceNode(" + taskOrConcept + ")");
        }
        
        addStartTriggerWidget.addPerformanceNode(taskOrConcept);
    }
    
    /**
     * Notification that a condition was added to the specified concept.
     * 
     * @param condition the condition that was added
     * @param parentConcept the concept the condition was added too
     */
    public void addCondition(Condition condition, Concept parentConcept){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addCondition(" + condition + ", " + parentConcept + ")");
        }
        addEndTriggerWidget.addCondition(condition, parentConcept);
    }
    
    /**
     * A new strategy was added.  Notify trigger widgets to update their widgets.
     * 
     * @param strategy the strategy being added
     */
    public void addStrategy(Strategy strategy){
        addStartTriggerWidget.updateStrategyList();
        addEndTriggerWidget.updateStrategyList();
    }

    /**
     * Returns the node id of the {@link Task} being edited in this panel
     * 
     * @return the node id of the edited {@link Task}. Will return null if no {@link Task} is being
     *         edited.
     */
    public BigInteger getSelectedTaskNodeId() {
        return selectedTask == null ? null : selectedTask.getNodeId();
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(childValidationStatus);
        validationStatuses.add(endTriggerValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (selectedTask == null) {
            validationStatus.setValid();
        }
        
        if (childValidationStatus.equals(validationStatus)) {
            // must have a child
            if (selectedTask.getConcepts() == null || selectedTask.getConcepts().getConcept().isEmpty()) {
                childValidationStatus.setModelObject(null);
                childValidationStatus.setErrorMessage(NO_CHILD_VALIDATION_MESSAGE);
                childValidationStatus.setAdditionalInstructions(buildNoChildValidationAdditionalInformation());
                childValidationStatus.setInvalid();
            } else {
                Concept invalidConcept = null;
                for (Concept concept : selectedTask.getConcepts().getConcept()) {
                    if (!ScenarioClientUtility.getValidationCache().isValid(concept)) {
                        invalidConcept = concept;
                        break;
                    }
                }

                if (invalidConcept == null) {
                    childValidationStatus.setValid();
                } else {
                    childValidationStatus.setModelObject(invalidConcept);
                    childValidationStatus.setErrorMessage("The child concept '" + invalidConcept.getName()
                            + "' is invalid. Please resolve the issues within the concept.");
                    childValidationStatus.setAdditionalInstructions(null);
                    childValidationStatus.setInvalid();
                }
            }
        } else if (endTriggerValidationStatus.equals(validationStatus)) {
            // must contain at least 1 end trigger
            endTriggerValidationStatus.setValidity(!endTriggerListEditor.getItems().isEmpty());
        }
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(startTriggerListEditor);
        childValidationComposites.add(endTriggerListEditor);
        childValidationComposites.add(additionalAssessments);
        childValidationComposites.add(assessmentRollup);
    }

    /**
     * Builds the additional information string for the {@link #childValidationStatus}.
     * 
     * @return the string to display to the user to help with adding a {@link Concept} for this
     *         {@link Task}.
     */
    private String buildNoChildValidationAdditionalInformation() {
        StringBuilder sb = new StringBuilder("To add a Concept, click the '+' button next to the Task");
        if (selectedTask != null) {
            sb.append(" '").append(selectedTask.getName()).append("'");
        }
        sb.append(" in the Task list.");
        return sb.toString();
    }

    /**
     * Updates the {@link TaskPanel} to update the usability of all edit controls.
     * 
     * @param isReadOnly If true, disable all edit controls. If false, enable all edit controls.
     */
    private void updateReadonly(boolean isReadOnly) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setReadonly(" + isReadOnly + ")");
        }

        startTriggerListEditor.setReadonly(isReadOnly);
        endTriggerListEditor.setReadonly(isReadOnly);
    }

    /**
     * Checks to see if a strategy with the given name is referenced by either of the trigger
     * lists in this editor and, if so, refreshes the list. <br/><br/>
     * 
     * This is used to remove deleted strategies if the trigger editor is currently showing them.
     * 
     * @param name the name of the strategy to delete. Cannot be null.
     */
	public void checkRefs(String name) {

		for (StartTriggers.Trigger trigger : startTriggerListEditor.getItems()) {
			if (trigger.getTriggerType() instanceof StrategyApplied) {
				startTriggerListEditor.refresh(trigger);
			}
		}

		for (EndTriggers.Trigger trigger : endTriggerListEditor.getItems()) {
			if (trigger.getTriggerType() instanceof StrategyApplied) {
				endTriggerListEditor.refresh(trigger);
			}
		}
	}
}