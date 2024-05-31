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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.extras.select.client.ui.Option;
import org.gwtbootstrap3.extras.select.client.ui.Select;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.dom.client.Style.TextOverflow;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.ChildConceptEnded;
import generated.dkf.Concept;
import generated.dkf.ConceptAssessment;
import generated.dkf.ConceptEnded;
import generated.dkf.Condition;
import generated.dkf.Conversation;
import generated.dkf.Coordinate;
import generated.dkf.EntityLocation;
import generated.dkf.EntityLocation.EntityId;
import generated.dkf.InstructionalIntervention;
import generated.dkf.LearnerActionReference;
import generated.dkf.MidLessonMedia;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceAssessment.PerformanceNode;
import generated.dkf.PointRef;
import generated.dkf.ScenarioAdaptation;
import generated.dkf.ScenarioStarted;
import generated.dkf.StartLocation;
import generated.dkf.StartTriggers;
import generated.dkf.Strategy;
import generated.dkf.StrategyApplied;
import generated.dkf.Task;
import generated.dkf.TaskEnded;
import generated.dkf.Tasks;
import generated.dkf.TriggerLocation;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil;
import mil.arl.gift.common.gwt.client.util.StrategyActivityUtil.StrategyActivityIcon;
import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.shared.LearnerActionPicker;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy.StrategyActivityEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.strategy.StrategyActivityEditor.StrategyActivityWrapper;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.PerfNodeSelectorImpl;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.ScenarioEventUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.CreateListAction;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemField;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ItemListEditor.ListChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.ListChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;
import mil.arl.gift.tools.authoring.server.gat.shared.model.dkf.PerfNodeIdNamePair;

/**
 * A widget used to add and edit start and end triggers
 * 
 * @author sharrison
 * 
 * @param <T> The type of the trigger elements
 */
public abstract class AddTriggerWidget<T> extends ItemEditor<T> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AddTriggerWidget.class.getName());

    /** The UiBinder that combines the ui.xml with this java class */
    private static AddTriggerWidgetUiBinder uiBinder = GWT.create(AddTriggerWidgetUiBinder.class);
    
    /** default name for strategy */
    private static final String TASK_TRIGGER_DEFAULT_STRATEGY_NAME = "TaskTriggerStrategy";
    
    /**
     * the Select widget option for the placeholder strategy reference choice
     */
    private static final String STRATEGY_REF_PLACEHOLDER = "Select a strategy";

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface AddTriggerWidgetUiBinder extends UiBinder<Widget, AddTriggerWidget<?>> {
    }

    /**
     * The type of trigger's condition. Value represents the selected index of
     * the {@link #triggerCondition} list
     */
    public enum TriggerType {
        /** triggered when the scenario starts */
        SCENARIO_STARTS("the scenario starts"),
        /** Triggered based on a concept assessment. */
        CONCEPT_ASSESSMENT("the learner's performance in a concept is assessed"),
        /** Triggered upon a concept ending. */
        CONCEPT_ENDED("the learner has finished all of the conditions covered by a concept"),
        /** Triggered upon a task ending. */
        TASK_ENDED("the learner has completed a task"),
        /** Triggered by the learner executing a {@link LearnerAction} */
        LEARNER_ACTION("a learner action is selected in the Tutor"),
        /** Triggered upon an entity reaching a location. */
        ENTITY_LOCATION("a simulated entity reaches a location in a simulated environment"),
        /** Triggered upon a strategy being applied */
        STRATEGY_APPLIED("a strategy is applied");
        
        /** the text to show the author in the selection component */
        private String typeText;
        
        /**
         * Set attribute. 
         * @param typeText text to show the author in the selection component.  This should
         * be unique among this enum in order to uniquely identify this enum entry.
         */
        private TriggerType(String typeText){
            setDisplayName(typeText);
        }
        
        /**
         * Gets the trigger type enum for the given unique type text.
         * 
         * @param typeText the trigger type enum text to find in the enum set.
         * @return the trigger type enum with the given text, null if not found.
         */
        public static TriggerType getTypeByText(String typeText){
            for (TriggerType triggerType : TriggerType.values()) {
                if (triggerType.getTypeText().equals(typeText)) {
                    return triggerType;
                }
            }

            return null;
        }

        /**
         * Return the text to show the author in the selection component
         * @return will not be null or empty.
         */
        public String getTypeText() {
            return typeText;
        }

        /**
         * Set the type text for this enum.
         * 
         * @param typeText can't be null or empty. This should
         * be unique among this enum in order to uniquely identify this enum entry.
         */
        private void setDisplayName(String typeText) {
            
            if(StringUtils.isBlank(typeText)){
                throw new IllegalArgumentException("The type text can't be null or empty.");
            }

            this.typeText = typeText;
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[TriggerType: ");
            sb.append("name = ").append(name());
            sb.append(", typeText = ").append(typeText);
            sb.append("]");
            return sb.toString();
        }
    }

    /** Drop down used to select the type of trigger */
    @UiField
    protected ListBox triggerCondition;

    /** The panel containing the different trigger condition type panels */
    @UiField
    protected DeckPanel deckPanel;
    
    /** the empty panel to use for the scenario starts editor of start task trigger */
    @UiField
    protected FlowPanel scenarioStartsEditor;
    
    /** The editor displayed for a 'concept assessment' trigger */
    @UiField
    protected PerfNodeSelectorImpl conceptAssessmentEditor;

    /** Warning displayed if there are no tasks or concepts available. */
    @UiField
    protected Label warningLabel;

    /** The editor displayed for a 'concept ended' trigger */
    @UiField
    protected PerfNodeSelectorImpl conceptEndedEditor;

    /** The editor displayed for a 'task ended' trigger */
    @UiField
    protected PerfNodeSelectorImpl taskEndedEditor;
    
    /** The editor displayed for a 'learner action' trigger */
    @UiField
    protected LearnerActionPicker learnerActionEditor;

    /** The editor displayed for a 'entity location' trigger */
    @UiField
    protected EntityTaskTriggerEditor entityLocationEditor;

    /** Checkbox indicating whether a delay should be used. */
    @UiField
    protected CheckBox delayCheckBox;
    
    /** Used to enter the duration of a delay */
    @UiField
    protected TextBox delayBox;

    /** The panel that contains the widgets related to delays. */
    @UiField
    protected FocusPanel delayPanel;

    /** First label for describing the delay. */
    @UiField
    protected HasText delayLabel;

    /** Second label for describing the delay. */
    @UiField
    protected HasText delayLabel2;

    /** The text used to describe the trigger */
    @UiField
    protected HasText ruleLabel;

    /** The checkbox which specifies wheter feedback should be displayed. */
    @UiField
    protected CheckBox feedbackCheckBox;

    /** Labels the {@link #feedbackCheckBox} */
    @UiField
    protected InlineHTML feedbackCheckLabel;

    /** The panel containing widgets for authoring the feedback on trigger */
    @UiField
    protected FocusPanel feedbackPanel;
    
    /** the editor for the strategy applied trigger */
    @UiField
    protected FlowPanel strategyAppliedEditor;
    
    /** where the strategy name is selected for the strategy applied trigger */
    @UiField
    protected Select strategyNameBox;
    
    /** the container around the strategy name selector */
    @UiField
    protected Widget strategyNameContainer;
    
    /** The editor for the activities of this strategy */
    private final StrategyActivityEditor activityEditor = new StrategyActivityEditor();
    
    /** The editor used to author/manage all activities */
    @UiField(provided = true)
    protected ItemListEditor<StrategyActivityWrapper> strategyActivitiesList = new ItemListEditor<>(activityEditor);

    /** Comparator to sort {@link PerfNodeIdNamePair performance node pairs} */
    private final Comparator<PerfNodeIdNamePair> PERF_NODE_SORTER = new Comparator<PerfNodeIdNamePair>() {

        @Override
        public int compare(PerfNodeIdNamePair o1, PerfNodeIdNamePair o2) {

            if (o1.getName() != null && o2.getName() != null) {
                return o1.getName().compareTo(o2.getName());
            }

            return 0;
        }
    };

    /** The list of editor widgets. List is initialized in the constructor. */
    private final List<ScenarioValidationComposite> editorList;

    /**
     * The container for showing validation messages for the delay not having a valid value.
     */
    private final WidgetValidationStatus delayValidation;
    
    /**
     * The container for showing validation messages for the concept ended task trigger for 
     * have a selected concept that has one or more conditions that can't complete (end).
     */
    private final WidgetValidationStatus conceptEndedValidation;
    
    /**
     * Start trigger adding/editing validation - 
     * The container for showing validation messages for the start triggers having more than
     * one scenario started trigger type.
     */
    protected final WidgetValidationStatus startTriggerSelectionValidation;
    
    /**
     * Strategy Applied trigger validation - the strategy name must be valid
     */
    protected final WidgetValidationStatus strategyNameValidation;

    /**
     * The backing coordinate object used to set the start location for an entity location trigger
     */
    private Coordinate startCoordinate;
    
    /** The strategy that the {@link StrategyViewImpl} is editing */
    private Strategy selectedStrategy;

    /** The trigger currently being edited */
    protected T editedTrigger;
    
    /**
     * list of condition classes that call {@link #AbstractConditionconditionCompleted()} to indicate that the
     * condition is no longer assessing.  Will be null until the server call returns with the collection.
     */
    private Set<String> conditionsThatCanComplete = null;

    /**
     * Constructor.
     */
    public AddTriggerWidget() {
        initWidget(uiBinder.createAndBindUi(this));

        delayValidation = new WidgetValidationStatus(delayBox, "The delay value is invalid. Please enter a postive number greater than zero.");
        startTriggerSelectionValidation = new WidgetValidationStatus(triggerCondition, "Only one scenario started event is allowed per task.");
        conceptEndedValidation = new WidgetValidationStatus(conceptEndedEditor, "The concept contains a condition that will never end, meaning this task trigger will never activate.");
        strategyNameValidation = new WidgetValidationStatus(strategyNameBox, "Please provide a valid strategy name");
                
        editorList = new ArrayList<ScenarioValidationComposite>();
        editorList.add(conceptAssessmentEditor);
        editorList.add(conceptEndedEditor);
        editorList.add(taskEndedEditor);
        editorList.add(learnerActionEditor);
        editorList.add(entityLocationEditor);

        feedbackPanel.setVisible(false);
        strategyActivitiesList.setActive(false);
        
        /* we only want to validate on the selected editor, so default all the editors to inactive
         * until changed later */
        for (ScenarioValidationComposite editor : editorList) {
            editor.setActive(false);
        }

        conceptAssessmentEditor.getListBoxTitle().setText("Concept to watch:");
        conceptAssessmentEditor.getResultTitle().setText("Assessment to wait for:");
        conceptEndedEditor.getListBoxTitle().setText("Concept to wait for:");
        taskEndedEditor.getListBoxTitle().setText("Task to wait for:");
        taskEndedEditor.setSelectsTasks(true);

        // programmatically add all the trigger types
        // Trigger types can be removed with {@link #excludeTriggerTypeChoices(TriggerType)}
        for(TriggerType triggerType : TriggerType.values()){
            triggerCondition.addItem(triggerType.getTypeText());
        }
        
        triggerCondition.setTitle("The circumstances that must be met for the event to start.");
        triggerCondition.addChangeHandler(new ChangeHandler() {

            @Override
            public void onChange(ChangeEvent event) {
                String selectedText = triggerCondition.getSelectedItemText();
                TriggerType triggerType = TriggerType.getTypeByText(selectedText);
                switch (triggerType) {
                case SCENARIO_STARTS:
                    showEditor(scenarioStartsEditor);
                    break;
                case CONCEPT_ASSESSMENT:
                    showEditor(conceptAssessmentEditor);
                    break;
                case CONCEPT_ENDED:
                    showEditor(conceptEndedEditor);
                    break;
                case TASK_ENDED:
                    showEditor(taskEndedEditor);
                    break;
                case LEARNER_ACTION:
                    showEditor(learnerActionEditor);
                    break;
                case ENTITY_LOCATION:
                    showEditor(entityLocationEditor);
                    break;
                case STRATEGY_APPLIED:              
                    
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            // make sure the list has the latest strategies, returns with the placeholder being selected
                            updateStrategyList();
                        }
                    });
                    
                    showEditor(strategyAppliedEditor);
                    
                    break;
                default:
                    throw new UnsupportedOperationException("The trigger type '" + triggerType + "' is unknown.");
                }
                
                requestValidation(startTriggerSelectionValidation, strategyNameValidation);
            }
        });
        
        // when a concept is selected make sure to validate the concept ended trigger
        conceptEndedEditor.addValueChangeHandler(new ValueChangeHandler<BigInteger>() {

            @Override
            public void onValueChange(ValueChangeEvent<BigInteger> valueChangeEvent) {
                requestValidation(conceptEndedValidation);
            }
        });

        delayCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {

                delayBox.setEnabled(event.getValue());
                requestValidation(delayValidation);
            }
        });

        delayPanel.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                if (delayCheckBox.isEnabled()) {
                    delayCheckBox.setValue(!delayCheckBox.getValue(), true);
                }
            }
        });

        delayBox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                event.stopPropagation();
            }
        });

        delayBox.addDomHandler(new InputHandler() {

            // the last valid value entered into the text box
            String lastValue = null;

            @Override
            public void onInput(InputEvent event) {
                if (!delayBox.isEnabled()) {
                    return;
                }
                
                final String value = delayBox.getText();

                if (StringUtils.isNotBlank(value)) {

                    try {

                        if (value.equals("-")) {
                            // remove the minus symbol
                            throw new Exception();
                        } else if (value.endsWith(".") && value.indexOf(".") == value.lastIndexOf(".")) {
                            // do nothing if they press the decimal point ONLY
                            // if there are
                            // no other decimal points
                        } else if (value.contains(" ")) {
                            // remove space, will be caught be catch block to
                            // remove key stroke
                            throw new Exception();
                        } else if (value.contains("f") || value.contains("d")) {
                            // 'f' and 'd' after numbers is a valid double
                            // string ("float"
                            // and "double"). Make sure to explicitly remove
                            // these characters.
                            // Will be caught be catch block to remove key
                            // stroke
                            throw new Exception();
                        } else {
                            // prevent the user from entering values that are
                            // invalid
                            Double doubleValue = Double.valueOf(value);

                            // eliminate leading 0's as the user is typing
                            if (value.startsWith("0")) {
                                delayBox.setValue(Double.toString(doubleValue));
                            }
                        }

                        lastValue = value;
                        requestValidation(delayValidation);

                    } catch (@SuppressWarnings("unused") Exception e) {
                        delayBox.setText(lastValue);
                    }

                } else {
                    lastValue = value;
                    requestValidation(delayValidation);
                }
            }
        }, InputEvent.getType());

        feedbackCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                feedbackPanel.setVisible(event.getValue());
                
                strategyActivitiesList.setActive(feedbackPanel.isVisible());
                strategyActivitiesList.validateAll();
            }
        });
        
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

                List<Serializable> activityList = selectedStrategy.getStrategyActivities();
                activityList.clear();
                for (StrategyActivityWrapper wrapper : strategyActivitiesList.getItems()) {
                    Serializable activity = wrapper.getActivity();
                    if (activity != null) {
                        activityList.add(activity);
                    }
                }

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
        
        // get the conditions collections from the server
        ScenarioClientUtility.getConditionsThatCanComplete(new AsyncCallback<Set<String>>() {
            
            @Override
            public void onSuccess(Set<String> result) {
                conditionsThatCanComplete = result;                
            }
            
            @Override
            public void onFailure(Throwable error) {
                logger.warning("Failed to retrieve information about which conditions can complete from the server, "+
                            "therefore that validation check will not be enabled.");
            }
        });
        
        strategyNameBox.addValueChangeHandler(new ValueChangeHandler<String>() {

            @Override
            public void onValueChange(ValueChangeEvent<String> event) {

                if(event.getValue() != null) {

                    // by default the ConceptAssessment is the editedTrigger type when creating a new one,
                    // but editing an existing StrategyApplied object you want to enter this 
                    if(editedTrigger instanceof StrategyApplied){
                        String oldRef = ((StrategyApplied)editedTrigger).getStrategyName();
                        ((StrategyApplied)editedTrigger).setStrategyName(event.getValue());
                        
                        ScenarioEventUtility.fireReferencesChangedEvent((StrategyApplied)editedTrigger, oldRef, event.getValue());  
                    }
                    
                    requestValidation(strategyNameValidation);
                }
            }
        });

    }
    
    /**
     * Remove the specified trigger types from the selection component.
     *  
     * @param triggerTypes if null or empty this method does nothing.
     */
    public void excludeTriggerTypeChoices(TriggerType...triggerTypes){
        
        if(triggerTypes == null || triggerTypes.length == 0){
            return;
        }
        
        for(TriggerType triggerType : triggerTypes){
            
            int index = getIndexOfTriggerType(triggerType);
            if(index != -1){
                triggerCondition.removeItem(index);
            }
        }
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
     * Returns the delay value from the text box.
     * 
     * @return the {@link BigDecimal} value of the delay. Can be null.
     */
    protected BigDecimal getDelayFromInput() {
        if (delayCheckBox.getValue()) {
            return new BigDecimal(delayBox.getValue());
        }

        return null;
    }

    /**
     * Retrieves the trigger type from the user input.
     * 
     * @return the trigger type provided by the user. Can be null.
     */
    protected Serializable getTriggerTypeFromInput() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("getTriggerTypeFromInput()");
        }

        TriggerType triggerType = TriggerType.getTypeByText(triggerCondition.getSelectedItemText());
        if(triggerType != null){
            switch (triggerType) {
            case SCENARIO_STARTS:
                ScenarioStarted scenarioStarted = new ScenarioStarted();
                return scenarioStarted;            
            case CONCEPT_ASSESSMENT:
                BigInteger selectedConcept = conceptAssessmentEditor.getValue();
                String selectedAssessment = conceptAssessmentEditor.getResultChoices().getValue();
    
                ConceptAssessment conceptAssessment = new ConceptAssessment();
                conceptAssessment.setConcept(selectedConcept);
                conceptAssessment.setResult(selectedAssessment);
                return conceptAssessment;
            case CONCEPT_ENDED:
                ConceptEnded conceptEnded = new ConceptEnded();
                conceptEnded.setNodeId(conceptEndedEditor.getValue());
                return conceptEnded;
            case TASK_ENDED:
                TaskEnded taskEnded = new TaskEnded();
                taskEnded.setNodeId(taskEndedEditor.getValue());
                return taskEnded;
            case LEARNER_ACTION:
                LearnerActionReference learnerActionReference = new LearnerActionReference();
                learnerActionReference.setName(learnerActionEditor.getValue());
                return learnerActionReference;
            case ENTITY_LOCATION:
                EntityId entityId = entityLocationEditor.getEntityIdEditor().getEntityId();
    
                StartLocation startLocation = new StartLocation();
                startLocation.setCoordinate(startCoordinate);
    
                TriggerLocation triggerLocation = new TriggerLocation();
                Coordinate inlineCoord = entityLocationEditor.getCoordinate();
                PointRef ptRef = entityLocationEditor.getPlaceOfInterest();
                if(inlineCoord != null) {
                    triggerLocation.setCoordinate(inlineCoord);
                }else {
                    triggerLocation.setPointRef(ptRef);
                }                
    
                EntityLocation entityLocation = new EntityLocation();
                entityLocation.setEntityId(entityId);
                entityLocation.setTriggerLocation(triggerLocation);
                return entityLocation;
            case STRATEGY_APPLIED:
                
                String sName = strategyNameBox.getValue();
                
                StrategyApplied sApplied = new StrategyApplied();
                sApplied.setStrategyName(sName);
                return sApplied;
            }
        }

        return null;
    }
    
    /**
     * Retrieves a strategy containing the activities from the user input.
     * 
     * @return the strategy containing the activities authored by the user. Can be null.
     */
    protected Strategy getStrategy(){
    	
    	if(Boolean.TRUE.equals(feedbackCheckBox.getValue())) {
    		
            List<Serializable> activityList = new ArrayList<>(strategyActivitiesList.size());
            for (StrategyActivityWrapper wrapper : strategyActivitiesList.getItems()) {
                Serializable activity = wrapper.getActivity();
                if (activity != null) {
                    activityList.add(activity);
                }
            }
            
            Strategy strategy = new Strategy();
            strategy.setName(TASK_TRIGGER_DEFAULT_STRATEGY_NAME);
            strategy.getStrategyActivities().addAll(activityList);
            return strategy;

    	}else {
    		return null;
    	}
    }

    /**
     * Retrieves the trigger choice from the provided trigger.
     * 
     * @param trigger the trigger that contains the trigger choice.
     * @return the type of choice. Can be null.
     */
    protected abstract Serializable getTriggerChoice(T trigger);

    /**
     * Retrieves the delay from the provided trigger.
     * 
     * @param trigger the trigger that contains the delay.
     * @return the delay. Can be null.
     */
    protected abstract BigDecimal getDelay(T trigger);

    /**
     * Retrieves the {@link TriggerMessage trigger message} from the provided
     * trigger.
     * 
     * @param trigger the trigger may contain the message.
     * @return the message. Can be null.
     */
    protected abstract StartTriggers.Trigger.TriggerMessage getTriggerMessage(T trigger);

    /**
     * Return the index of the trigger type enum in the current selection component.
     * 
     * @param triggerType the enumerated trigger type to find in the selection component.
     * @return the zero based index of the trigger type enum, if found, in the selection component.  Returns
     * -1 if not found.
     */
    private int getIndexOfTriggerType(TriggerType triggerType){
        
        for(int index = 0; index < triggerCondition.getItemCount(); index++){
            String typeText = triggerCondition.getItemText(index);
            if(typeText.equals(triggerType.getTypeText())){
                return index;
            }
        }
        
        return -1;
    }
    
    @Override
    protected void populateEditor(T trigger) {
        editedTrigger = trigger;

        boolean displayWarning = false;

        final int conceptAssessmentIndex = getIndexOfTriggerType(TriggerType.CONCEPT_ASSESSMENT);
        final int conceptEndedIndex = getIndexOfTriggerType(TriggerType.CONCEPT_ENDED);
        final int taskEndedIndex = getIndexOfTriggerType(TriggerType.TASK_ENDED);

        // populate concept assessment choices
        if (hasAvailableConcepts()) {
            triggerCondition.setSelectedIndex(conceptAssessmentIndex);
            triggerCondition.getElement().<SelectElement>cast().getOptions()
                    .getItem(conceptAssessmentIndex).setDisabled(false);

            triggerCondition.setSelectedIndex(conceptEndedIndex);
            triggerCondition.getElement().<SelectElement>cast().getOptions().getItem(conceptEndedIndex)
                    .setDisabled(false);
        } else {
            displayWarning = true;
            triggerCondition.getElement().<SelectElement>cast().getOptions()
                    .getItem(conceptAssessmentIndex).setDisabled(true);
            triggerCondition.getElement().<SelectElement>cast().getOptions().getItem(conceptEndedIndex)
                    .setDisabled(true);
        }

        if (hasAvailableTasks()) {
            triggerCondition.setSelectedIndex(taskEndedIndex);
            triggerCondition.getElement().<SelectElement>cast().getOptions().getItem(taskEndedIndex)
                    .setDisabled(false);

        } else {
            displayWarning = true;
            triggerCondition.getElement().<SelectElement>cast().getOptions().getItem(taskEndedIndex)
                    .setDisabled(true);
        }

        // display warning label if there were any issues preventing an option from being enabled
        if (displayWarning) {
            warningLabel.setVisible(true);
            warningLabel.setText(
                    "Some options have been disabled because no tasks or concepts were found in this assessment.");
        } else {
            warningLabel.setVisible(false);
            warningLabel.setText("");
        }

        // reset editors
        conceptAssessmentEditor.setValue(null);
        conceptEndedEditor.setValue(null);
        taskEndedEditor.setValue(null);

        entityLocationEditor.getEntityIdEditor().edit(null);
        entityLocationEditor.resetLocationType(true);

        // if no trigger is passed in or an invalid trigger is passed in, use a new trigger
        Serializable triggerChoice = getTriggerChoice(trigger);
        Serializable triggerChoiceToEdit;
        if (!(triggerChoice instanceof ConceptAssessment) && !(triggerChoice instanceof StrategyApplied)
                && !(triggerChoice instanceof ConceptEnded) && !(triggerChoice instanceof TaskEnded)
                && !(triggerChoice instanceof LearnerActionReference) && !(triggerChoice instanceof EntityLocation)
                && !(triggerChoice instanceof ChildConceptEnded) && !(triggerChoice instanceof ScenarioStarted)) {

            if (hasAvailableConcepts()) {
                triggerChoiceToEdit = new ConceptAssessment();

            } else {
                triggerChoiceToEdit = new EntityLocation();
            }
        } else {
            triggerChoiceToEdit = triggerChoice;
        }

        if (triggerChoiceToEdit instanceof ConceptAssessment) {
            triggerCondition.setSelectedIndex(conceptAssessmentIndex);
            showEditor(conceptAssessmentEditor);
            ConceptAssessment conceptAssessment = (ConceptAssessment) triggerChoiceToEdit;

            // find the concept assessment trigger's concept and select it
            conceptAssessmentEditor.setValue(conceptAssessment.getConcept());

            // find the concept assessment trigger's assessment and select it
            if (conceptAssessment.getResult() != null) {
                conceptAssessmentEditor.getResultChoices().setValue(conceptAssessment.getResult());
            }

        } else if (triggerChoiceToEdit instanceof ConceptEnded || triggerChoiceToEdit instanceof ChildConceptEnded) {
            triggerCondition.setSelectedIndex(conceptEndedIndex);
            showEditor(conceptEndedEditor);

            if (triggerChoiceToEdit instanceof ConceptEnded) {
                ConceptEnded conceptEnded = (ConceptEnded) triggerChoiceToEdit;
                conceptEndedEditor.setValue(conceptEnded.getNodeId());
            } else {
                ChildConceptEnded conceptEnded = (ChildConceptEnded) triggerChoiceToEdit;
                conceptEndedEditor.setValue(conceptEnded.getNodeId());
            }
        } else if (triggerChoiceToEdit instanceof TaskEnded) {
            triggerCondition.setSelectedIndex(taskEndedIndex);
            showEditor(taskEndedEditor);
            TaskEnded taskEnded = (TaskEnded) triggerChoiceToEdit;

            taskEndedEditor.setValue(taskEnded.getNodeId());
        } else if (triggerChoiceToEdit instanceof LearnerActionReference) {
            triggerCondition.setSelectedIndex(getIndexOfTriggerType(TriggerType.LEARNER_ACTION));
            showEditor(learnerActionEditor);
            LearnerActionReference learnerActionReference = (LearnerActionReference) triggerChoiceToEdit;
            learnerActionEditor.setValue(learnerActionReference.getName());
        } else if(triggerChoiceToEdit instanceof StrategyApplied){            
            triggerCondition.setSelectedIndex(getIndexOfTriggerType(TriggerType.STRATEGY_APPLIED));
            showEditor(strategyAppliedEditor);            
            
            // make sure the list has the latest strategies, returns with the placeholder being selected
            updateStrategyList();
            
            StrategyApplied sApplied = (StrategyApplied)triggerChoiceToEdit;
            strategyNameBox.setValue(sApplied.getStrategyName());            
        } else if (triggerChoiceToEdit instanceof EntityLocation) {

            final int entityLocationIndex = getIndexOfTriggerType(TriggerType.ENTITY_LOCATION);
            triggerCondition.setSelectedIndex(entityLocationIndex);
            showEditor(entityLocationEditor);

            EntityLocation entityLocation = (EntityLocation) triggerChoiceToEdit;

            /* Populate the start location of the entity to be tracked from the
             * value specified within the trigger */
            EntityId entityId = entityLocation.getEntityId();
            entityLocationEditor.getEntityIdEditor().edit(entityId);

            /* Populate the location the entity will reach at which point the
             * trigger will be fired from the value specified within the
             * trigger */
            TriggerLocation triggerLocation = entityLocation.getTriggerLocation();
            if (triggerLocation != null) {
                if(triggerLocation.getCoordinate() != null) {
                    entityLocationEditor.showInlineCoordinateEditor();
                    entityLocationEditor.getTriggerCoordinateEditor().setCoordinate(triggerLocation.getCoordinate());
                }else if(triggerLocation.getPointRef() != null) {
                    entityLocationEditor.showPlaceOfInterestEditor();
                    String poiName = triggerLocation.getPointRef().getValue();
                    entityLocationEditor.getPlaceOfInterestPicker().setValue(poiName);
                    entityLocationEditor.getMinDistanceSpinner().setValue(triggerLocation.getPointRef().getDistance());
                }
            }
        } else if(triggerChoiceToEdit instanceof ScenarioStarted){
            triggerCondition.setSelectedIndex(getIndexOfTriggerType(TriggerType.SCENARIO_STARTS));
            showEditor(scenarioStartsEditor);
        }

        BigDecimal delay = getDelay(trigger);
        delayCheckBox.setValue(delay != null);
        delayBox.setValue(delay != null ? delay.toString() : null);
        delayBox.setEnabled(delay != null);

        StartTriggers.Trigger.TriggerMessage triggerMessage = getTriggerMessage(trigger);
        feedbackCheckBox.setValue(triggerMessage != null);
        feedbackPanel.setVisible(triggerMessage != null);
        strategyActivitiesList.setActive(feedbackPanel.isVisible());
        
        if(triggerMessage != null && triggerMessage.getStrategy() != null) {
	        editStrategy(triggerMessage.getStrategy());       
        }else {
        	// currently setItems must be called in order for the add row to be shown
        	strategyActivitiesList.setItems(new ArrayList<StrategyActivityWrapper>(0));
        }        

    }
    
    /**
     * Populate the item list editor with the strategy's activities.
     * 
     * @param strategy if null this method does nothing.
     */
    private void editStrategy(Strategy strategy) {
    	
    	if(strategy == null) {
    		return;
    	}
    	
        this.selectedStrategy = strategy;
    	
        List<StrategyActivityWrapper> activityWrappers = new ArrayList<>();
        for(Serializable activity : selectedStrategy.getStrategyActivities()) {
            activityWrappers.add(new StrategyActivityWrapper(activity));
        }

        activityEditor.setStrategy(selectedStrategy);
        strategyActivitiesList.setItems(activityWrappers);	 
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
        validate(conceptEndedValidation);
    }
    
    /**
     * Notification that a condition changed (e.g. was replaced with another condition)
     * 
     * @param condition the condition being replaced.
     */
    public void handleChangeInCondition(Condition condition){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleChangeInCondition(" + condition + ")");
        }
        
        validate(conceptEndedValidation);
    }
    
    /**
     * Notification that a condition was added.
     * 
     * @param condition the condition that was added
     * @param parentConcept the concept the condition was added too.
     */
    public void addCondition(Condition condition, Concept parentConcept){
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addCondition(" + condition + ", "+parentConcept+")");
        }
        validate(conceptEndedValidation);
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
     * Handle notification that a Strategy was removed.  Update the {@link #strategyNameBox} widget list
     * of strategies. 
     * @param strategy the strategy that was deleted
     */
    public void handleRemovedStrategy(Strategy strategy){
        updateStrategyList();
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
    
    /**
     * Set the name of the task to filter out from the task selector widget of the
     * task ended trigger editor.
     * @param taskNameToIgnore can be null to clear out previous filtering.
     */
    public void setTaskNameToIgnore(String taskNameToIgnore){
        taskEndedEditor.setTaskNameToIgnore(taskNameToIgnore);
    }
    
    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {
    	childValidationComposites.add(strategyActivitiesList);
        childValidationComposites.add(conceptAssessmentEditor);
        childValidationComposites.add(conceptEndedEditor);
        childValidationComposites.add(taskEndedEditor);
        childValidationComposites.add(learnerActionEditor);
        childValidationComposites.add(entityLocationEditor);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(delayValidation);
        validationStatuses.add(startTriggerSelectionValidation);
        validationStatuses.add(strategyNameValidation);
        validationStatuses.add(conceptEndedValidation);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (delayValidation.equals(validationStatus)) {
            // validate the delay
            if (delayCheckBox.getValue()) {
                if (StringUtils.isBlank(delayBox.getValue())) {
                    // invalid if no value was entered
                    delayValidation.setInvalid();
                } else {
                    try {
                        BigDecimal triggerDelay = new BigDecimal(delayBox.getValue());

                        delayValidation.setValidity(triggerDelay.compareTo(BigDecimal.ZERO) > 0);
                    } catch (@SuppressWarnings("unused") Exception e) {
                        // invalid if value couldn't be parsed
                        delayValidation.setInvalid();
                    }
                }
            } else {
                // valid if user is opting out of delay
                delayValidation.setValid();
            }
        }else if(conceptEndedValidation.equals(validationStatus)){
            
            if(!CollectionUtils.isEmpty(conditionsThatCanComplete)){
                // the client has received the information from the server about all conditions that can complete
                
                int visibleWidgetIndex = deckPanel.getVisibleWidget();
                if (visibleWidgetIndex != -1) {
                    Widget visibleWidget = deckPanel.getWidget(visibleWidgetIndex);
                    
                    if (visibleWidget != conceptEndedEditor) {
                        // the deck panel is showing another trigger editor, don't validate concept ended then
                        conceptEndedValidation.setValid();
                    }else{
                        // the deck panel is showing the concept ended editor, validate concept ended then
                        Concept concept = ScenarioClientUtility.getConceptWithId(conceptEndedEditor.getValue());
                        if(concept != null){
                            boolean canComplete = ScenarioClientUtility.hasOnlyConditionsThatCanComplete(concept.getConditionsOrConcepts());
                            conceptEndedValidation.setValidity(canComplete);

                        }else{
                            conceptEndedValidation.setValid();
                        }
                    }
                }

            }else{
                conceptEndedValidation.setValid();
            }

        }else if(strategyNameValidation.equals(validationStatus)){
            
            int visibleWidgetIndex = deckPanel.getVisibleWidget();
            if (visibleWidgetIndex != -1) {
                Widget visibleWidget = deckPanel.getWidget(visibleWidgetIndex);
                
                if (visibleWidget == strategyAppliedEditor) {
                    // strategy reference must be valid
                    strategyNameValidation.setValidity(ScenarioClientUtility.getStrategyWithName(strategyNameBox.getValue()) != null);
                }else{
                    // the deck panel is showing another trigger editor, don't validate strategy name then
                    strategyNameValidation.setValid();
                }
            }
            
        }
    }
    
    /**
     * Updates the available strategy names that the author can select from
     */
    public void updateStrategyList() {
        updateStrategyList(null, null);
    }

    /**
     * Updates the available strategy names that the author can select from and, if necessary, replaces the old
     * name of a renamed strategy.
     *
     * @param oldName the old name to update.  Can be null if the list just needs to be updated.
     * @param newName the new value to update the name with. Can be null if the list just needs to be updated
     */
    public void updateStrategyList(String oldName, String newName) {

        String selectedName = strategyNameBox.getValue();

        List<String> strategyNames = ScenarioClientUtility.getAvailableStrategyNames();        

        //
        // update the UI with the most current list of strategy names
        //
        strategyNameBox.clear();
        Option placeholderOption = new Option();
        placeholderOption.setText(STRATEGY_REF_PLACEHOLDER);
        placeholderOption.setValue(STRATEGY_REF_PLACEHOLDER);
        placeholderOption.setSelected(true);
        placeholderOption.setEnabled(false);
        placeholderOption.setHidden(true);
        strategyNameBox.add(placeholderOption);
        
        for (String strategyName : strategyNames) {
            Option option = new Option();
            option.setText(strategyName);
            option.setValue(strategyName);
            strategyNameBox.add(option);
        }

        if(!strategyNames.isEmpty()) {         
            
            if (StringUtils.isNotBlank(oldName)) {
                
                if(StringUtils.equals(oldName, selectedName)) {
                    // the current selected name is being changed to a new name
                    selectedName = newName;
                }
                
                //if a rename is occurring, make sure to replace the old name with the new one in the list
                // (for each occurrence of the old name)
                ListIterator<String> itr = strategyNames.listIterator();
                while (itr.hasNext()) {
                    String strategyName = itr.next();
                    if (StringUtils.equals(oldName, strategyName)) {
                        itr.set(newName);
                    }
                }
            }

            if(oldName != null && selectedName != null && strategyNames.contains(selectedName)) {
                //if a name was already selected and still exists in the updated list, reselect it
                strategyNameBox.setValue(selectedName);
                
                if(editedTrigger instanceof StrategyApplied){
                    ((StrategyApplied)editedTrigger).setStrategyName(selectedName);
                }
            }else{
                strategyNameBox.setValue(STRATEGY_REF_PLACEHOLDER);
            }
        }
                
        strategyNameBox.render();
        strategyNameBox.refresh();

        requestValidation(strategyNameValidation);
    }


    /**
     * Show/Hides the feedback UI
     * 
     * @param show true to show, false to hide
     */
    protected void showFeedbackUI(boolean show) {
        feedbackCheckBox.setVisible(show);
        feedbackCheckLabel.setVisible(show);
        feedbackPanel.setVisible(show ? feedbackCheckBox.getValue() : false);
        strategyActivitiesList.setActive(feedbackPanel.isVisible());
    }
    
    /**
     * Retrieves the first concept in the list (sorted alphabetically by concept name).
     * 
     * @return the first concepts. Can be null.
     */
    public PerfNodeIdNamePair getFirstConcept() {
        List<PerfNodeIdNamePair> conceptPairs = getConceptIdNamePairs();
        return !conceptPairs.isEmpty() ? conceptPairs.get(0) : null;
    }

    /**
     * Gets the list of concept id and name pairs.
     * 
     * @return The list of concept id and name pairs.
     */
    private List<PerfNodeIdNamePair> getConceptIdNamePairs() {
        List<PerfNodeIdNamePair> nameIdPairs = new ArrayList<PerfNodeIdNamePair>();

        for (Concept concept : ScenarioClientUtility.getUnmodifiableConceptList()) {
            
            BigInteger id = concept.getNodeId();
            String name = concept.getName();

            PerfNodeIdNamePair nameIdPair = new PerfNodeIdNamePair();
            nameIdPair.setId(id);
            nameIdPair.setName(name);
            nameIdPairs.add(nameIdPair);
        }

        Collections.sort(nameIdPairs, PERF_NODE_SORTER);
        return nameIdPairs;
    }

    /**
     * Shows the provided editor to the user. Hides all other editors.
     * 
     * @param editorToShow the editor to display.
     */
    private void showEditor(Widget editorToShow) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("showEditor(" + editorToShow + ")");
        }
        
        int visibleWidgetIndex = deckPanel.getVisibleWidget();
        if (visibleWidgetIndex != -1) {
            Widget visibleWidget = deckPanel.getWidget(visibleWidgetIndex);
            
            // trying to show an editor that is already visible
            if (visibleWidget == editorToShow) {
                return;
            }
            
            if (visibleWidget instanceof ScenarioValidationComposite) {
                ScenarioValidationComposite editor = (ScenarioValidationComposite) visibleWidget;
                editor.clearValidations();
                editor.setActive(false);
            }
        }

        int widgetIndex = deckPanel.getWidgetIndex(editorToShow);
        if (widgetIndex != -1) {
            deckPanel.showWidget(widgetIndex);
            if (editorToShow instanceof ScenarioValidationComposite) {
                ScenarioValidationComposite editor = (ScenarioValidationComposite) editorToShow;
                editor.setActive(true);
                editor.validateAll();
            }
        } else {
            logger.severe("Could not show editor '" + editorToShow + "' because it could not be found.");
        }
    }
    
    /**
     * Gets whether or not the scenario has any concepts to select from
     * 
     * @return whether or not there are any concepts
     */
    private boolean hasAvailableConcepts() {
        Tasks tasks = ScenarioClientUtility.getTasks();
        
        if(tasks != null) {
            for(Task task : tasks.getTask()) {
                
                if(task.getConcepts() != null && !task.getConcepts().getConcept().isEmpty()) {
                    return true;
                }
            }  
        }
        
        return false;
    }
    
    /**
     * Gets whether or not the scenario has any tasks to select from
     * 
     * @return whether or not there are any tasks
     */
    private boolean hasAvailableTasks() {
        List<Task> tasks = ScenarioClientUtility.getUnmodifiableTaskList();
        return !tasks.isEmpty();
    }
    
    @Override
    public void setReadonly(boolean isReadonly) {
        triggerCondition.setEnabled(!isReadonly);
        conceptAssessmentEditor.setReadonly(isReadonly);
        conceptEndedEditor.setReadonly(isReadonly);
        taskEndedEditor.setReadonly(isReadonly);
        entityLocationEditor.setReadonly(isReadonly);
        delayCheckBox.setEnabled(!isReadonly);
        delayBox.setEnabled(!isReadonly && delayCheckBox.getValue());
        feedbackCheckBox.setEnabled(!isReadonly);
        strategyActivitiesList.setReadonly(isReadonly);
        strategyNameBox.setEnabled(!isReadonly);
    }
}
