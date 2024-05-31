/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy;

import static mil.arl.gift.common.gwt.client.SafeHtmlUtils.bold;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBoxButton;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.Conversation;
import generated.dkf.InstructionalIntervention;
import generated.dkf.LearnerAction;
import generated.dkf.MidLessonMedia;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceAssessment.PerformanceNode;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.Strategy;
import generated.dkf.StrategyStressCategory;
import generated.dkf.Task;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil.StrategyActivityIcon;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.ValidationWidget;
import mil.arl.gift.common.util.StrategyUtil;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.LearnerActionReferenceWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy.StrategyActivityEditor.StrategyActivityWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.StateTransitionReferenceWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.shared.TaskTransitionReferenceWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.EditCancelledCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The UI used to author instructional strategies.
 */
public class StrategyPanel extends ScenarioValidationComposite {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyPanel.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static StrategyPanelUiBinder uiBinder = GWT.create(StrategyPanelUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface StrategyPanelUiBinder extends UiBinder<Widget, StrategyPanel> {
    }

    /** Displays any validation errors on the page */
    @UiField(provided = true)
    protected ValidationWidget validations = new ValidationWidget(this);

    /** The editor for the activities of this strategy */
    private final StrategyActivityEditor activityEditor = new StrategyActivityEditor();

    /** The editor used to author/manage all activities */
    @UiField(provided = true)
    protected ItemListEditor<StrategyActivityWrapper> strategyActivitiesList = new ItemListEditor<>(activityEditor);

    /**
     * The button indicating if the strategy should always be automatically
     * processed regardless of the OC's 'auto' flag
     */
    @UiField
    protected CheckBoxButton mandatoryStrategyButton;
    
    @UiField
    protected CheckBoxButton resetKnowledgeButton;

    /**
     * A list control which displays each {@link StateTransition} that references this
     * {@link Strategy}
     */
    @UiField
    protected StateTransitionReferenceWidget referencedStateTransitions;
    
    /**
     * A list control which displays each {@link Task} that references this
     * {@link Strategy}
     */
    @UiField
    protected TaskTransitionReferenceWidget referencedTasks;
    
    /**
     * A list control which displays each {@link LearnerAction} that references this
     * {@link Strategy}
     */
    @UiField
    protected LearnerActionReferenceWidget referencedLearnerActions;
    
    /** The slider used to author stress value */
    @UiField
    Slider stressSlider;
    
    /** The slider used to author difficulty value */
    @UiField
    Slider difficultySlider;
    
    /** a label that shows all the stress categories for the tactics in this strategy set */
    @UiField
    Label stressCategoriesLabel;

    /** The strategy that the {@link StrategyViewImpl} is editing */
    private Strategy selectedStrategy;

    /**
     * Constructor.
     */
    public StrategyPanel() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("StrategyPanel()");
        }

        initWidget(uiBinder.createAndBindUi(this));

        strategyActivitiesList.setFields(buildActivityListFields());
        strategyActivitiesList.setDraggable(true);
        strategyActivitiesList.addCreateListAction("Click here to add an activity", new CreateListAction<StrategyActivityWrapper>() {

            @Override
            public StrategyActivityWrapper createDefaultItem() {
                return new StrategyActivityWrapper();
            }
        });

        strategyActivitiesList.addListChangedCallback(new ListChangedCallback<StrategyActivityWrapper>() {

            @Override
            public void listChanged(ListChangedEvent<StrategyActivityWrapper> event) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("strategyActivitiesList.onListChanged(" + event + ")");
                }
                
                Set<StrategyStressCategory> stressCategories = new HashSet<>();

                List<Serializable> activityList = selectedStrategy.getStrategyActivities();
                activityList.clear();
                for (StrategyActivityWrapper wrapper : strategyActivitiesList.getItems()) {
                    Serializable activity = wrapper.getActivity();
                    if (activity != null) {
                        activityList.add(activity);
                    }
                    
                    StrategyStressCategory stressCategory = StrategyUtil.getStrategyStressCategory(activity);
                    if(stressCategory != null) {
                        stressCategories.add(stressCategory);
                    }
                }
                
                stressCategoriesLabel.setText(StringUtils.join(", ", stressCategories));

                activityEditor.validateAllAndFireDirtyEvent(selectedStrategy);
            }
        });

        /* Strategy panel is so nested that it is best to revalidate even when
         * cancelled */
        strategyActivitiesList.addEditCancelledCallback(new EditCancelledCallback() {
            @Override
            public void editCancelled() {
                activityEditor.validateAllAndFireDirtyEvent(selectedStrategy);
            }
        });

        strategyActivitiesList.setRemoveItemDialogTitle("Delete Strategy");
        strategyActivitiesList.setRemoveItemStringifier(new Stringifier<StrategyActivityEditor.StrategyActivityWrapper>() {
            
            @Override
            public String stringify(StrategyActivityWrapper obj) {

                Serializable activity = obj.getActivity();
                if(activity == null){
                    return "this strategy";
                }else if(activity instanceof InstructionalIntervention){
                    return "this feedback strategy";
                }else if(activity instanceof ScenarioAdaptation){
                    return "this scenario adaptation";
                }else if(activity instanceof MidLessonMedia){
                    return "this present media strategy";
                }else if(activity instanceof PerformanceAssessment){
                    
                    PerformanceAssessment pAss = (PerformanceAssessment)activity;
                    Serializable assessmentType = pAss.getAssessmentType();
                    if(assessmentType == null){
                        return "this strategy";
                    }else if(assessmentType instanceof Conversation){
                        return "this conversation strategy";
                    }else if(assessmentType instanceof PerformanceNode){
                        
                        PerformanceNode pNode = (PerformanceNode)assessmentType;
                        Serializable taskOrConcept = ScenarioClientUtility.getTaskOrConceptWithId(pNode.getNodeId());
                        if(taskOrConcept == null){
                            return "this strategy that queries the task/concept for additional assessment (e.g. Survey)";
                        }else if(taskOrConcept instanceof Task){
                            Task task = (Task)taskOrConcept;
                            return "this strategy that queries the task "+bold(task.getName())+" for additional assessment (e.g. Survey)";
                        }else if(taskOrConcept instanceof Concept){
                            Concept concept = (Concept)taskOrConcept;
                            return "this strategy that queries the concept "+bold(concept.getName())+" for additional assessment (e.g. Survey)";
                        }
                    }
                }
                
                return "this strategy";
            }
        });

        mandatoryStrategyButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                /* need to defer reading the value, since it takes a moment for
                 * the value to bubble up */
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        final boolean mandatory = Boolean.TRUE.equals(mandatoryStrategyButton.getValue());
                        mandatoryStrategyButton.setActive(mandatory);

                        List<Serializable> activityList = selectedStrategy.getStrategyActivities();
                        activityList.clear();
                        for (StrategyActivityWrapper wrapper : strategyActivitiesList.getItems()) {
                            Serializable activity = wrapper.getActivity();
                            if (activity != null) {
                                StrategyUtil.setMandatory(activity, mandatory);
                                activityList.add(activity);
                            }
                        }
                    }
                });
            }
        });

        /**
         * Disable click events on the button when the button is disabled. This
         * is to work around the issue with GWT Bootstrap CheckBoxButton class
         * setEnabled() method which doesn't actually do what it's supposed to.
         * It visually disables the element, but it doesn't actually stop it
         * from receiving click events.
         */
        mandatoryStrategyButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (!mandatoryStrategyButton.isEnabled()) {
                    event.preventDefault();
                    event.stopPropagation();
                }
            }
        });
        
        resetKnowledgeButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(final ValueChangeEvent<Boolean> event) {
                /* need to defer reading the value, since it takes a moment for
                 * the value to bubble up */
                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        boolean shouldResetScenario = Boolean.TRUE.equals(resetKnowledgeButton.getValue());
                        resetKnowledgeButton.setActive(shouldResetScenario);
                        
                        selectedStrategy.setShouldResetScenario(shouldResetScenario);
                    }
                });
            }
        });
        
        
        stressSlider.addSlideStopHandler(new SlideStopHandler<Double>() {
            
            @Override
            public void onSlideStop(SlideStopEvent<Double> event) {

                if(selectedStrategy != null) {
                    selectedStrategy.setStress(BigDecimal.valueOf(event.getValue()));
                }
            }
        });
        
        difficultySlider.addSlideStopHandler(new SlideStopHandler<Double>() {
            
            @Override
            public void onSlideStop(SlideStopEvent<Double> event) {

                if(selectedStrategy != null) {
                    selectedStrategy.setDifficulty(BigDecimal.valueOf(event.getValue()));
                }
            }
        });

        // needs to be called last
        initValidationComposite(validations);
        updateReadOnly();
    }

    /**
     * Builds a list containing each {@link ItemField} for the
     * {@link #strategyActivitiesList}.
     *
     * @return The {@link Iterable} containing each {@link ItemField}. Can't be
     *         null.
     * @throws UnsupportedOperationException if the action type is unknown
     */
    private Iterable<ItemField<StrategyActivityWrapper>> buildActivityListFields() {
        ItemField<StrategyActivityWrapper> typeField = new ItemField<StrategyActivityWrapper>("Type", null) {

            @Override
            public Widget getViewWidget(StrategyActivityWrapper item) {
                FlowPanel toRet = new FlowPanel();
                toRet.setWidth("auto");
                toRet.getElement().getStyle().setProperty("whiteSpace", "nowrap");
                toRet.getElement().getStyle().setProperty("maxWidth", "200px");
                toRet.getElement().getStyle().setTextOverflow(TextOverflow.ELLIPSIS);

                StrategyActivityIcon icon = StrategyActivityUtil.getIconFromActivity(item.getActivity());
                icon.applyGATStyle();

                /* Combines the icon and name type into the panel. */
                toRet.add(icon);
                return toRet;
            }
        };

        ItemField<StrategyActivityWrapper> summaryField = new ItemField<StrategyActivityWrapper>("Summary", "85%") {

            @Override
            public Widget getViewWidget(StrategyActivityWrapper wrapper) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("summaryField.getViewWidget(" + wrapper + ")");
                }

                Serializable item = wrapper.getActivity();
                if (item == null) {
                    return new HTML("Unknown");
                }

                FlowPanel flowPanel = new FlowPanel();
                if (item instanceof InstructionalIntervention) {
                    StrategyActivityUtil.summarize((InstructionalIntervention) item, ScenarioClientUtility.getTrainingAppType(), flowPanel);
                } else if (item instanceof MidLessonMedia) {
                    StrategyActivityUtil.summarize((MidLessonMedia) item, flowPanel);
                } else if (item instanceof PerformanceAssessment) {
                    StrategyActivityUtil.summarize((PerformanceAssessment) item, ScenarioClientUtility.getUnmodifiableNodeIdToNameMap(), flowPanel);
                } else if (item instanceof ScenarioAdaptation) {
                    StrategyActivityUtil.summarize((ScenarioAdaptation) item, flowPanel);
                } else {
                    String msg = "The wrapper is unexpectedly wrapping type " + item.getClass().getSimpleName();
                    throw new UnsupportedOperationException(msg);
                }
                
                if(flowPanel.getWidgetCount() < 1) {
                    
                    //if the panel has nothing in it, add some padding so that it can still be dragged
                    flowPanel.getElement().getStyle().setPadding(10, Unit.PX);
                }

                return flowPanel;
            }
        };

        return Arrays.asList(typeField, summaryField);
    }

    /**
     * Populates the panel using the data within the given {@link Strategy}.
     *
     * @param strategy the data object that will be used to populate the panel.
     * @throws UnsupportedOperationException if the action type is unknown
     */
    public void edit(Strategy strategy) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("edit(" + strategy + ")");
        }

        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        this.selectedStrategy = strategy;
        referencedStateTransitions.showTransitions(selectedStrategy);
        referencedLearnerActions.showLearnerActions(selectedStrategy);
        referencedTasks.showTasks(selectedStrategy);
        
        referencedLearnerActions.setVisible(!referencedLearnerActions.isEmpty());

        Set<StrategyStressCategory> stressCategories = new HashSet<>();

        List<StrategyActivityWrapper> activityWrappers = new ArrayList<>();
        boolean mandatory = false;
        for (Serializable activity : selectedStrategy.getStrategyActivities()) {
            activityWrappers.add(new StrategyActivityWrapper(activity));
            mandatory |= StrategyUtil.isMandatory(activity);

            StrategyStressCategory stressCategory = StrategyUtil.getStrategyStressCategory(activity);
            if(stressCategory != null) {
                stressCategories.add(stressCategory);
            }
        }
        
        stressCategoriesLabel.setText(StringUtils.join(", ", stressCategories));

        activityEditor.setStrategy(strategy);
        strategyActivitiesList.setItems(activityWrappers);
        mandatoryStrategyButton.setValue(mandatory, true);
        boolean shouldResetScenario = Boolean.TRUE.equals(selectedStrategy.isShouldResetScenario());
        resetKnowledgeButton.setValue(shouldResetScenario);
        resetKnowledgeButton.setActive(shouldResetScenario);
        
        stressSlider.setValue(strategy.getStress() != null ? strategy.getStress().doubleValue() : 0.0);
        difficultySlider.setValue(strategy.getDifficulty() != null ? strategy.getDifficulty().doubleValue() : 0.0);

        updateReadOnly();
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

        activityEditor.addPerformanceNode(taskOrConcept);
    }

    /**
     * Checks if the node id is selected in the {@link PerformanceAssessmentEditor}. If it is
     * selected, set the selected node to null. Remove the provided node id from the available
     * option list.
     *
     * @param nodeId the node id to check against.
     */
    public void removePerformanceNode(BigInteger nodeId) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removePerformanceNode(" + nodeId + ")");
        }

        activityEditor.removePerformanceNode(nodeId);
    }

    /**
     * Updates the dropdown of {@link Task tasks} and {@link Concept concepts} to reflect the name
     * change.
     *
     * @param nodeId the node id of the {@link Task} or {@link Concept} that was renamed. If null,
     *        nothing will change.
     * @param newName the new name. If blank, nothing will change.
     */
    public void handleTaskOrConceptRename(BigInteger nodeId, String newName) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(nodeId, newName);
            logger.fine("handleTaskOrConceptRename(" + StringUtils.join(", ", params) + ")");
        }

        activityEditor.handleTaskOrConceptRename(nodeId, newName);
    }
    
    /**
     * Updates a {@link HighlightObjects} within the {@link #activityEditor} 
     * that was renamed.
     * 
     * @param oldName the old name of the highlight object.  Can be null.
     * @param newName the new name for the highlight object.  Can't be null.
     */
    public void handleHighlightObjectRename(String oldName, String newName){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleHighlightObjectRename(" + oldName + ", " + newName + ")");
        }
        
        activityEditor.handleHighlightObjectRename(oldName, newName);
    }
    
    /**
     * A place of interest (e.g. generated.dkf.Point) has been changed, request revalidation on certain validation widgets.
     * 
     * @param placeOfInterest the place of interest that has changed. If null or not a point/path, this method does nothing.
     */
    public void handlePlaceOfInterestChange(Serializable placeOfInterest){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handlePlaceOfInterestChange(" + placeOfInterest + ")");
        }
        strategyActivitiesList.revalidateAllItems();
    }

    /**
     * Handles the deletion of a certain {@link StateTransition} by removing it from
     * {@link #referencedStateTransitions} if it was contained there.
     *
     * @param transition The transition to remove if it is found within
     *        {@link #referencedStateTransitions}. If it isn't, nothing happens.
     */
    public void removeStateTransition(StateTransition transition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeStateTransition(" + transition + ")");
        }

        referencedStateTransitions.remove(transition);
    }

    /**
     * Updates the list of referenced state transitions with the selected task.
     *
     * @param transition the transition to add or remove from the references table.
     * @param add true to add the transition to the references table; false to remove it.
     */
    public void updateReferencedStateTransitions(StateTransition transition, boolean add) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(transition, add);
            logger.fine("updateReferencedStateTransitions(" + StringUtils.join(", ", params) + ")");
        }

        if (add) {
            referencedStateTransitions.add(transition);
        } else {
            referencedStateTransitions.remove(transition);
        }
    }
    
    /**
     * Handles the deletion of a certain {@link Task} by removing it from
     * {@link #referencedTasks} if it was contained there.
     *
     * @param task The task to remove if it is found within
     *        {@link #referencedTasks}. If it isn't, nothing happens.
     */
    public void removeTasks(Task task) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeTasks(" + task + ")");
        }

        referencedTasks.remove(task);
    }
    
    /**
     * Updates the list of referenced state transitions with the selected task.
     *
     * @param task the task to add or remove from the references table.
     * @param add true to add the transition to the references table; false to remove it.
     */
    public void updateTasks(Task task, boolean add) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(task, add);
            logger.fine("updateReferencedTasks(" + StringUtils.join(", ", params) + ")");
        }

        if (add) {
            referencedTasks.add(task);
        } else {
        	referencedTasks.remove(task);
        }
    }
    
    /**
     * Handles the deletion of a certain {@link LearnerAction} by removing it from
     * {@link #referencedLearnerActions} if it was contained there.
     *
     * @param learnerAction The learnerAction to remove if it is found within
     *        {@link #referencedLearnerActions}. If it isn't, nothing happens.
     */
    public void removeLearnerAction(LearnerAction learnerAction) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeLearnerAction(" + learnerAction + ")");
        }

        referencedLearnerActions.remove(learnerAction);
        
        referencedLearnerActions.setVisible(!referencedLearnerActions.isEmpty());
    }

    /**
     * Updates the list of referenced learner actions with the selected task.
     *
     * @param learnerAction the learnerAction to add or remove from the references table.
     * @param add true to add the learnerAction to the references table; false to remove it.
     */
    public void updateReferencedLearnerActions(LearnerAction learnerAction, boolean add) {
        if (logger.isLoggable(Level.FINE)) {
            List<Object> params = Arrays.<Object>asList(learnerAction, add);
            logger.fine("updateReferencedLearnerActions(" + StringUtils.join(", ", params) + ")");
        }

        if (add) {
            referencedLearnerActions.add(learnerAction);
        } else {
            referencedLearnerActions.remove(learnerAction);
        }
        
        referencedLearnerActions.setVisible(!referencedLearnerActions.isEmpty());
    }

    /**
     * Refreshes the referenced state transitions table and redraws the items in the list.
     */
    public void refreshReferencedStateTransitions() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refreshReferencedStateTransitions()");
        }

        referencedStateTransitions.refresh();
    }
    
    /**
     * Refreshes the referenced tasks table and redraws the items in the list.
     */
    public void refreshReferencedTasks() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refreshReferencedTasks()");
        }

        referencedTasks.refresh();
    }
    
    /**
     * Refreshes the referenced learner actions table and redraws the items in the list.
     */
    public void refreshReferencedLearnerActions() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("refreshReferencedLearnerActions()");
        }

        referencedLearnerActions.refresh();
    }

    /**
     * Returns the name of the {@link Strategy} being edited in this panel
     *
     * @return the name of the edited {@link Strategy}. Will return null if no {@link Strategy} is
     *         being edited.
     */
    public String getSelectedStrategyName() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getSelectedStrategyName()");
        }

        return selectedStrategy == null ? null : selectedStrategy.getName();
    }

    /**
     * Populates the dropdown of strategy handlers with the provided class name list.
     *
     * @param handlerClassNames the list of handlers
     */
    public void setAvailableStrategyHandlers(List<String> handlerClassNames) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setAvailableStrategyHandlers(" + handlerClassNames + ")");
        }

        activityEditor.setAvailableStrategyHandlers(handlerClassNames);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        /* There are no ValidationStatuses on this ValidationComposite. There
         * are only ValidationStatuses on its children. */
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        /* Since there are no ValidationStatuses on this ValidationComposite.
         * There is nothing that needs to be validated */
    }

    @Override
    public void addValidationCompositeChildren(final Set<ValidationComposite> childValidationComposites) {
        childValidationComposites.add(strategyActivitiesList);
    }

    /**
     * Updates whether or not the controls used for editing {@link #selectedStrategy} are enabled
     * based on the state of the widget and the state of the flag contained within
     * {@link ScenarioClientUtility}
     */
    private void updateReadOnly() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("updateReadOnly()");
        }

        boolean isReadOnly = ScenarioClientUtility.isReadOnly() || selectedStrategy == null;
        strategyActivitiesList.setReadonly(isReadOnly);
        mandatoryStrategyButton.setEnabled(!isReadOnly);
        resetKnowledgeButton.setEnabled(!isReadOnly);
        
        stressSlider.setEnabled(!isReadOnly);
        difficultySlider.setEnabled(!isReadOnly);
    }
}