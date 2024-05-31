/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.sessionfilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

import generated.dkf.Concept;
import generated.dkf.Concepts;
import generated.dkf.Condition;
import generated.dkf.Conditions;
import generated.dkf.Task;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.CourseConceptProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.OverallAssessmentBar;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.PerformanceNodeDataProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.TaskDataProvider;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.TaskDataPanel.ViewMode;
import mil.arl.gift.tools.dashboard.client.gamemaster.RegisteredSessionProvider;
import mil.arl.gift.tools.dashboard.client.gamemaster.TimelineProvider;
import mil.arl.gift.tools.dashboard.shared.RecorderParams;
import mil.arl.gift.tools.dashboard.shared.messages.TaskStateCache;

/**
 * A dialog that allows the observer controller to manually provide overall assessments
 * for concepts in a past session playback
 * 
 * @author nroberts
 */
public class OverallAssessmentDialog extends ModalDialogBox {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(OverallAssessmentDialog.class.getName());

    private static OverallAssessmentDialogUiBinder uiBinder = GWT.create(OverallAssessmentDialogUiBinder.class);

    interface OverallAssessmentDialogUiBinder extends UiBinder<Widget, OverallAssessmentDialog> {
    }
    
    /** Interface to allow CSS style name access */
    interface Style extends CssResource {
        
        String barData();
        
        String itemContainer();
        
        String condition();
        
        String task();
    }
    
    /** An accessor for this widget's CSS styling rules */
    @UiField
    protected Style style;
    
    /** The tree used to display the performance node structure */
    @UiField
    protected Tree overallAssessmentTree;
    
    /** The confirm button */
    private Button confirmButton = new Button("Ok");

    /** The cancel button */
    private Button cancelButton = new Button("Cancel");
    
    /** The provider of task assessment information used to populate this widget */
    private TaskDataProvider dataProvider;
    
    /** The root of the concept hierarchy, which represents the task itself */
    private TaskPerformanceItem taskItem;
    
    /** The singleton instance of this class */
    private static OverallAssessmentDialog instance;
    
    /** A mapping from each concept name to all of the conditions inside of it. Needed
     * to render the conditions under each concept. */
    private Map<String, List<Condition>> conceptNameToConditions = new HashMap<>();
    
    /** A mapping from each performance node ID to its graded assessment score */
    private Map<Integer, AssessmentLevelEnum> nodeIdToAssessment = new HashMap<>();
    
    /** The performance node IDs of all the performance nodes that match course concepts */
    private Set<Integer> courseConceptNodeIds = new HashSet<>();
    
    /** A mapping from each leaf concept ID to the assessment scores for all the conditions immediately below it */
    private Map<Integer, List<ScoreNodeUpdate>> conceptIdToConditionAssessments = new HashMap<>();
    
    /** The scenario information that was loaded from the server */
    private SessionScenarioInfo scenario;

    /** A set of score editors that have media recordings that need to be saved before publishing scores */
    private Set<OverallAssessmentBar> scoreMediaToSave = new HashSet<>();

    /** The knowledge session that is currently being monitored */
    private AbstractKnowledgeSession session;
    
    /**
     * Creates a new dialog for modifying overall assessments
     */
    private OverallAssessmentDialog() {
        
        setWidget(uiBinder.createAndBindUi(this));
        setGlassEnabled(true);
        
        setText("Calculate Score");
        
        cancelButton.setType(ButtonType.DANGER);
        cancelButton.setWidth("70px");
        confirmButton.setType(ButtonType.PRIMARY);
        confirmButton.setWidth("70px");

        FlowPanel footer = new FlowPanel();
        footer.add(confirmButton);
        footer.add(cancelButton);
        setFooterWidget(footer);
        
        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });
        
        confirmButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                publishOverallAssessments();
            }
        });
    }

    /**
     * Sends the overall assessments that have been provided by the observer controller to the
     * server to be patched in and published.
     * <br/><br/>
     * If the observer controller has recorded any audio associated with the assessments, that
     * audio will be saved before publishing the assessments.
     */
    private void publishOverallAssessments() {
        
        /* Collect the new scores */
        final Map<Integer, List<ScoreNodeUpdate>> newScores = getConceptConditionAssessments();
        
        if(!scoreMediaToSave.isEmpty()) {
            
            /* The OC has media recordings that need to be saved */
            final Set<OverallAssessmentBar> unhandledMedia = new HashSet<>(scoreMediaToSave);
            
            /* Create the recorder parameters needed to save the recordings */
            SessionMember host = session.getHostSessionMember();
            RecorderParams params = new RecorderParams();
            
            if(host != null) {
                params.setUserId(host.getUserSession().getUserId())
                    .setDomainSessionId(host.getDomainSessionId())
                    .setExperimentId(host.getUserSession().getExperimentId());
            }
            
            /* Save any recorded media associated with said scores before publishing*/
            for(final OverallAssessmentBar editor : scoreMediaToSave) {
                
                editor.saveObserverMedia(params, new AsyncCallback<String>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        
                        scoreMediaToSave.clear();
                        
                        UiManager.getInstance().displayErrorDialog(
                                "Failed to Publish Scores", 
                                "An error occurred while saving recordered media for scores: " + caught.toString(), 
                                null);
                        hide();
                    }

                    @Override
                    public void onSuccess(String result) {
                        
                        /* No longer need to wait for this recording to save*/
                        unhandledMedia.remove(editor);
                        
                        if(unhandledMedia.isEmpty()) {
                            
                            /* All recordings have saved, so publish the assessments*/
                            publishFinalOverallAssessments(newScores);
                        }
                    }
                });
            }
        
        } else {
            publishFinalOverallAssessments(newScores);
        }
    }
    
    /**
     * Performs the final steps to send the overall assessments that have been provided by the 
     * observer controller to the server to be patched in and published.
     * 
     * @param newScores the assessment scores to push to the server. Cannot be null.
     */
    private void publishFinalOverallAssessments(Map<Integer, List<ScoreNodeUpdate>> newScores) {
        
        UiManager.getInstance().getDashboardService().publishKnowledgeSessionOverallAssessments(
                UiManager.getInstance().getSessionId(), 
                UiManager.getInstance().getUserName(),
                TimelineProvider.getInstance().getPlaybackTime(),
                newScores, 
                CourseConceptProvider.get().getCourseConceptList(),
                new AsyncCallback<GenericRpcResponse<String>>() {
                    
                    @Override
                    public void onSuccess(GenericRpcResponse<String> result) {
                        if(result.getWasSuccessful()) {
                            
                            /* Update log metadata patch file name */
                            RegisteredSessionProvider.getInstance().updateLogPatchFile(result.getContent());
                            
                            hide();
                            
                        } else {
                            
                            if(result.getContent() != null) {
                                
                                /* Patching still succeeded, so update log metadata patch file name */
                                RegisteredSessionProvider.getInstance().updateLogPatchFile(result.getContent());
                            }
                            
                            boolean isLrsError = result.getContent() != null;
                            String title = isLrsError 
                                    ? "Unable to publish summative assessment to external system"
                                    : "Failed to save summative assessment";
                            
                            UiManager.getInstance().displayDetailedErrorDialog(
                                    title,
                                    result.getException().getReason(), 
                                    result.getException().getDetails(),
                                    result.getException().getErrorStackTrace(),
                                    null);
                            
                            hide();
                        }
                        
                        /* Reload the summative assessments in the timeline */
                        TimelineProvider.getInstance().reloadTimeline();
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        UiManager.getInstance().displayErrorDialog(
                                "Failed to Publish Scores", 
                                "An error occurred while publishing the scores: " + caught.toString(), 
                                null);
                        hide();
                    }
                });
    }

    /**
     * Populates this dialog with the performance node structure of the given task
     * 
     * @param state the task state to load the performance node structure from. 
     */
    public void load(final TaskPerformanceState state, AbstractKnowledgeSession session) {
        
        if(state == null) {
            throw new IllegalArgumentException("The task state to build overall assessments from cannot be null");
        }
        
        if(session == null) {
            throw new IllegalArgumentException("A knowledge session must be provided to save overall assessments");
        }
        
        this.session = session;
        
        this.dataProvider = new TaskDataProvider() {
            
            @Override
            public AbstractKnowledgeSession getKnowledgeSession() {
                return null;
            }
            
            @Override
            public TaskPerformanceState getCurrentState() {
                return state;
            }
            
            @Override
            public void setViewMode(ViewMode view) {
                //Do nothing
            }
            
            @Override
            public TaskStateCache getCachedState() {
                return null;
            }
        };
        
        overallAssessmentTree.clear();
        
        scenario = null;
        conceptNameToConditions.clear();
        nodeIdToAssessment.clear();
        conceptIdToConditionAssessments.clear();
        courseConceptNodeIds.clear();
        
        /* Load the knowledge session's scenario info. This is needed in order to display the 
         * scenario's conditions and their user-friendly display names */
        UiManager.getInstance().getDashboardService().getKnowledgeSessionScenario(
                UiManager.getInstance().getSessionId(), 
                UiManager.getInstance().getUserName(),
                new AsyncCallback<GenericRpcResponse<SessionScenarioInfo>>() {
            
            @Override
            public void onSuccess(GenericRpcResponse<SessionScenarioInfo> result) {
                
                if(result.getWasSuccessful()) {
                    
                    scenario = result.getContent();
                    
                    if(scenario == null || scenario.getScenario() == null) {
                        
                        UiManager.getInstance().displayErrorDialog(
                                "Failed to Load Scenario Info", 
                                "No DKF scenario information was found in this knowledge session's resources. "
                                + "<br/><br/>This can happen if the knowledge session is a legacy session that was created "
                                + "before March 2021, when GIFT started saving DKF information alongside session logs.", 
                        null);
                        
                        return;
                    }
                    
                    if(scenario.getCourseConcepts() != null) {
                        
                        /* Populate the course concepts */
                        CourseConceptProvider.get().updateCourseConcepts(scenario.getCourseConcepts());
                    }
                    
                    if(scenario.getCurrentScore() != null) {
                        
                        /* Look at the most recent published score to determine if any existing overall
                         * assessment scores need to be displayed by this dialog */
                        gatherAssessments(scenario.getCurrentScore());
                    }
                    
                    /* Find all the performance nodes that match the current session's course concepts*/
                    gatherCourseConcepts(state);
                    
                    if(courseConceptNodeIds.isEmpty()) {
                        
                        /* If no course concepts are found, prevent the overall score from being modified */
                        UiManager.getInstance().displayInfoDialog("No Course Concepts Under Task", 
                                "The task <b>" + state.getState().getName() + "</b> does not cover any course concepts, so there is no overall score available "
                                + "to be recalculated.");
                        return;
                    }
                    
                    /* Populate the conditions that need to be rendered */
                    for(Task task : scenario.getScenario().getAssessment().getTasks().getTask()) {
                        
                        for(Concept concept : task.getConcepts().getConcept()) {
                            mapConditions(concept);
                        }
                    }
                
                    /* Generate the tree structure */
                    taskItem = new TaskPerformanceItem();
                    overallAssessmentTree.addItem(taskItem);
                    
                    recalculateRollUpAssessments(true);
                    
                    /* Display this dialog */
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            center();
                        }
                    });
                }
            }
            
            @Override
            public void onFailure(Throwable caught) {
                UiManager.getInstance().displayErrorDialog(
                        "Failed to Load Scenario info", 
                        "An error occurred while loading the scenario info: " + caught.toString(), 
                        null);
            }
        });
    }
    
    /**
     * Look at the given score node and all of its children in order to determine if there are
     * any overall assessments that need to be displayed. As assessments are found they will be
     * stored in maps that will be referenced while building the performance node tree.
     * 
     * @param score the score node to look at. If null, no assessments will be gathered.
     */
    private void gatherAssessments(AbstractScoreNode score) {
        
        if(score instanceof GradedScoreNode) {
            
            GradedScoreNode gradedScore = (GradedScoreNode) score;
            
            for(AbstractScoreNode childScore : gradedScore.getChildren()) {
                
                if(childScore instanceof RawScoreNode) {
                    
                    /* This is a condition with an assessment that needs to be loaded */
                    RawScoreNode rawScore = (RawScoreNode) childScore;
                    
                    List<ScoreNodeUpdate> conditionAssessments = conceptIdToConditionAssessments.get(score.getPerformanceNodeId());
                    if(conditionAssessments == null) {
                        conditionAssessments = new ArrayList<>();
                        conceptIdToConditionAssessments.put(score.getPerformanceNodeId(), conditionAssessments);
                    }
                    
                    /* Load each condition's score into the editor as well as any notes associated with it */
                    ScoreNodeUpdate nodeUpdate = new ScoreNodeUpdate(rawScore.getAssessment());
                    nodeUpdate.setEvaluator(rawScore.getEvaluator());
                    nodeUpdate.setObserverComment(rawScore.getObserverComment());
                    nodeUpdate.setObserverMedia(rawScore.getObserverMedia());
                    
                    conditionAssessments.add(nodeUpdate);
                    
                } else {
                    gatherAssessments(childScore);
                }
            }
        }
    }

    /**
     * Gathers concepts from the given DKF concept and saves it to a map that will
     * be referenced to render the conditions
     * 
     * @param concept
     */
    private void mapConditions(Concept concept) {
        if(concept == null) {
            return;
        }
        
        if(concept.getConditionsOrConcepts() instanceof Concepts) {
            
            for(Concept childConcept : ((Concepts) concept.getConditionsOrConcepts()).getConcept()) {
                mapConditions(childConcept);
            }
            
        } else if(concept.getConditionsOrConcepts() instanceof Conditions) {
            conceptNameToConditions.put(concept.getName(), ((Conditions) concept.getConditionsOrConcepts()).getCondition());
        }
    }
    
    /**
     * Redraws the rendered task
     * 
     * @param updateStateOnRedraw whether assessment states should be updated
     * @param assessmentChanged whether the assessment was changed
     */
    public void redraw(boolean updateStateOnRedraw, boolean assessmentChanged) {
        taskItem.redraw(updateStateOnRedraw, assessmentChanged);
    }
    
    /**
     * Gets the singleton instance of this class
     * 
     * @return the singleton instance
     */
    public static OverallAssessmentDialog get() {
        
        if(instance == null) {
            instance = new OverallAssessmentDialog();
        }
        
        return instance;
    }

    /**
     * A tree item representing a task
     *  
     * @author nroberts
     */
    private class TaskPerformanceItem extends TreeItem {
        
        /** A widget used to display the task's assessment level in a bar to the side*/
        private OverallAssessmentBar barData;
        
        /** the data panels used to display concepts data for this task */
        private List<ConceptPerformanceItem> conceptPanels = new ArrayList<>();
        
        private FlowPanel container = new FlowPanel();
        
        /**
         * Creates a new tree item to represent this widget's task
         */
        public TaskPerformanceItem() {
            
            if(dataProvider == null) {
                throw new IllegalArgumentException("The provider of data to the root task item cannot be null");
            }
            
            container.addStyleName(style.itemContainer());
            container.addStyleName(style.task());
            
            Label name = new Label(dataProvider.getCurrentState().getState().getName());
            
            FlowPanel dataContainer = new FlowPanel();
            
            dataContainer.add(name);
            
            dataContainer.getElement().getStyle().setProperty("flex", "1");
            container.add(dataContainer);
            
            barData = new OverallAssessmentBar(true);
            barData.addStyleName(style.barData());
            
            /* If a score was published for this concept, display the currently published assessment */
            AssessmentLevelEnum assessment = nodeIdToAssessment.get(dataProvider.getCurrentState().getState().getNodeId());
            if(assessment != null) {
                barData.setAssessmentLevel(assessment);
            }
            
            container.add(barData);
            
            container.addDomHandler(new ClickHandler() {
                
                @Override
                public void onClick(ClickEvent event) {
                    
                    /* Toggle whether the task is collapsed on click */
                    setState(!getState());
                }
            }, ClickEvent.getType());
            
            setWidget(container);
            
            redraw(false, false);
            
            setState(true);
        }
        
        /**
         * Redraw this panel using the last state received.
         * 
         * @param updateStateOnRedraw flag determining whether to update the concept
         *        states with the current state.
         * @param assessmentChanged whether the assessment state has just changed
         * @return an enumerated type of sound that needs to be played for this task
         *         and any descendant concepts where
         *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
         *         return null;
         */
        public AssessmentSoundType redraw(boolean updateStateOnRedraw, boolean assessmentChanged) {
            
            if(dataProvider.getCurrentState() == null){
                return null;
            }
            
            /* Get the current assessment, since it may have changed */
            AssessmentLevelEnum assessment = nodeIdToAssessment.get(dataProvider.getCurrentState().getState().getNodeId());
            if(assessment != null) {
                barData.setAssessmentLevel(assessment);
            }
            
            barData.redraw();
            
            AssessmentSoundType requestedSoundType = null;
            
            final PerformanceStateAttribute stateAttr = dataProvider.getCurrentState().getState();

            //begin updating this task's concept panels to match the current state
            List<ConceptPerformanceItem> existingConceptPanels = new ArrayList<>(conceptPanels);
            conceptPanels.clear();
            
            //get the panels corresponding to the concepts found in this state
            for(ConceptPerformanceState concept : dataProvider.getCurrentState().getConcepts()) {
                
                logger.info("Adding concept" + concept.getState().getName());
                Integer conceptId = concept.getState().getNodeId();
                
                // find the previous version of the panel for this concept (if existed previously)
                Iterator<ConceptPerformanceItem> existingConceptPanelItr = existingConceptPanels.iterator();
                ConceptPerformanceItem conceptPanel = null;
                while(existingConceptPanelItr.hasNext()){
                    ConceptPerformanceItem existingConceptPanel = existingConceptPanelItr.next();
                    if(existingConceptPanel.getCurrentState().getState().getNodeId() == conceptId){
                        conceptPanel = existingConceptPanel;
                        existingConceptPanelItr.remove();
                        break;
                    }
                }
                
                if(!shouldRender(concept)) {
                    
                    /* Only render course concepts and their parents/children */
                    continue;
                }

                if(conceptPanel == null) {
                    
                    logger.info("Creating concept" + concept.getState().getName());

                    //a panel does not yet exist for this concept, so create one
                    conceptPanel = new ConceptPerformanceItem(concept);

                } else {
                    logger.info("Updating concept" + concept.getState().getName());
                    boolean stateChanged = false;
                    if (conceptPanel.getCurrentState() != null) {
                        stateChanged = PerformanceStateAttributeDiff
                                .performDiff(conceptPanel.getCurrentState().getState(), concept.getState(), false);
                    }

                    //need to always update intermediate concepts in case their childen have changed
                    if(concept instanceof IntermediateConceptPerformanceState || updateStateOnRedraw || stateChanged){
                        // update the existing concept panel using the new state
                        conceptPanel.update(concept, updateStateOnRedraw); 
                    }else{
                        // just redraw using the existing state
                        conceptPanel.redraw(false);
                    }
                }

                conceptPanels.add(conceptPanel);
            }
            
            //remove the panels for any concepts that are not found in the new state
            for(ConceptPerformanceItem panel : existingConceptPanels) {
                panel.remove();
            }

            //insert concepts' panels into the main panel
            List<ConceptPerformanceItem> sortedPanels = new ArrayList<>(conceptPanels);

            int size = sortedPanels.size();
            for(int i = 0; i < size; i++) {

                ConceptPerformanceItem panel = sortedPanels.get(i);
                    
                TreeItem itemAtIndex = getChild(i);
                if(itemAtIndex == null) {
                    addItem(panel); //only add new data panels when the main panel does not yet contain them
                    
                } else if(!itemAtIndex.equals(panel)) {
                    insertItem(i, panel); //sort the existing data panel to its intended position
                }
            }
            
            if(logger.isLoggable(Level.FINE)) {
                logger.fine("Finished updating state for task: " + stateAttr.getName());
            }
            
            return requestedSoundType;
        }
    }
    
    /**
     * A tree item representing a concept
     *  
     * @author nroberts
     */
    private class ConceptPerformanceItem extends TreeItem implements PerformanceNodeDataProvider<ConceptPerformanceState>{
        
        /** A widget used to display the task's assessment level in a bar to the side*/
        private OverallAssessmentBar barData;
        
        /** the data panels used to display concepts data for this task */
        private List<TreeItem> childPanels = new ArrayList<>();
        
        private FlowPanel container = new FlowPanel();
        
        private ConceptPerformanceState currentState;
        
        /**
         * Creates a new tree item to represent the concept that the given
         * performance state applies to
         * 
         * @param the concept performance state that this item should represent
         */
        public ConceptPerformanceItem(ConceptPerformanceState state) {
            
            if(state == null) {
                throw new IllegalArgumentException("The concept to create a performance tree item for cannot be null");
            }
            
            this.currentState = state;
            
            container.addStyleName(style.itemContainer());
            
            FlowPanel dataContainer = new FlowPanel();
            
            Label name = new Label(getCurrentState().getState().getName());
            dataContainer.add(name);
            
            dataContainer.getElement().getStyle().setProperty("flex", "1");
            container.add(dataContainer);
            
            if(state instanceof IntermediateConceptPerformanceState) {
            
                /* This is a non-leaf concept that needs to be read-only */
                barData = new OverallAssessmentBar(true);
                barData.addStyleName(style.barData());
            
                container.addDomHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        /* Toggle whether the concept is collapsed on click */
                        setState(!getState());
                    }
                }, ClickEvent.getType());
                
            } else {
                
                container.addStyleName(style.condition());
                
                /* This is a leaf concept, so let the OC change its assessment */
                barData = new OverallAssessmentBar();
                barData.addStyleName(style.barData());
                barData.setAssessmentChangedCommand(new Command() {
                    
                    @Override
                    public void execute() {
                        recalculateRollUpAssessments(false);
                    }
                });
                
                container.addDomHandler(new ClickHandler() {
                    
                    @Override
                    public void onClick(ClickEvent event) {
                        
                        /* If this concept is clicked on, show the metadata associated with its score */
                        barData.showScoreMetadata(container.getElement());
                    }
                    
                }, ClickEvent.getType());
            }
            
            /* If a score was published for this concept, display the currently published assessment */
            AssessmentLevelEnum assessment = nodeIdToAssessment.get(state.getState().getNodeId());
            if(assessment != null) {
                barData.setAssessmentLevel(assessment);
            }
            
            container.add(barData);

            setWidget(container);
            
            update(state, false);
            
            setState(true);
        }
        
        /**
         * Updates this panel's labels to reflect the attribute data provided by the given concept state
         * 
         * @param state the concept state from which to derive attribute data. Cannot be null.
         * @param updateStateOnRedraw flag determining whether to update the concept
         *        states with the current state.
         * @return an enumerated type of sound that needs to be played for this task
         *         and any descendant concepts where
         *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
         *         return null;
         */
        public void update(ConceptPerformanceState state, boolean updateStateOnRedraw) {
            
            this.currentState = state;
            
            /* Get the current assessment, since it may have changed */
            AssessmentLevelEnum assessment = nodeIdToAssessment.get(currentState.getState().getNodeId());
            if(assessment != null) {
                barData.setAssessmentLevel(assessment);
            }
            
            barData.redraw();
            
            redrawSubConcepts(updateStateOnRedraw);
        }
        
        /**
         * Redraw this panel using the last state received.
         * 
         * @param updateStateOnRedraw flag determining whether to update the concept
         *        states with the current state.
         * @return an enumerated type of sound that needs to be played for this task
         *         and any descendant concepts where
         *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
         *         return null;
         */
        public void redraw(boolean updateStateOnRedraw) {
            
            /* Get the current assessment, since it may have changed */
            AssessmentLevelEnum assessment = nodeIdToAssessment.get(currentState.getState().getNodeId());
            if(assessment != null) {
                barData.setAssessmentLevel(assessment);
            }
            
            barData.redraw();
            
            redrawSubConcepts(updateStateOnRedraw);
        }
        
        private void redrawSubConcepts(boolean updateStateOnRedraw) {

            //begin updating this task's concept panels to match the current state
            List<TreeItem> existingConceptPanels = new ArrayList<>(childPanels);
            childPanels.clear();
            
            if(getCurrentState() instanceof IntermediateConceptPerformanceState) {
                
                //get the panels corresponding to the concepts found in this state
                for(ConceptPerformanceState concept : ((IntermediateConceptPerformanceState) getCurrentState()).getConcepts()) {
                    
                    Integer conceptId = concept.getState().getNodeId();
                    
                    // find the previous version of the panel for this concept (if existed previously)
                    Iterator<TreeItem> existingConceptPanelItr = existingConceptPanels.iterator();
                    ConceptPerformanceItem conceptPanel = null;
                    while(existingConceptPanelItr.hasNext()){
                        
                        TreeItem existingPanel = existingConceptPanelItr.next();
                        
                        if(existingPanel instanceof ConceptPerformanceItem) {
                            ConceptPerformanceItem existingConceptPanel = (ConceptPerformanceItem) existingPanel;
                            if(existingConceptPanel.getCurrentState().getState().getNodeId() == conceptId){
                                conceptPanel = existingConceptPanel;
                                existingConceptPanelItr.remove();
                                break;
                            }
                        }
                    }
                    
                    if(!shouldRender(concept)) {
                        
                        /* Only render course concepts and their parents/children */
                        continue;
                    }
    
                    if(conceptPanel == null) {
    
                        //a panel does not yet exist for this concept, so create one
                        conceptPanel = new ConceptPerformanceItem(concept);
    
                    } else {
                        boolean stateChanged = false;
                        if (conceptPanel.getCurrentState() != null) {
                            stateChanged = PerformanceStateAttributeDiff
                                    .performDiff(getCurrentState().getState(), concept.getState(), false);
                        }
    
                        //need to always update intermediate concepts in case their children have changed
                        if(concept instanceof IntermediateConceptPerformanceState || updateStateOnRedraw || stateChanged){
                            // update the existing concept panel using the new state
                            conceptPanel.update(concept, updateStateOnRedraw); 
                        }else{
                            // just redraw using the existing state
                            conceptPanel.redraw(false);
                        }
                    }
    
                    childPanels.add(conceptPanel);
                }
                
            }
            
            //remove the panels for any concepts that are not found in the new state
            for(TreeItem panel : existingConceptPanels) {
                panel.remove();
            }

            //insert concepts' panels into the main panel
            List<TreeItem> sortedPanels = new ArrayList<>(childPanels);
            
            int size = sortedPanels.size();
            for(int i = 0; i < size; i++) {

                TreeItem panel = sortedPanels.get(i);
                    
                TreeItem itemAtIndex = getChild(i);
                if(itemAtIndex == null) {
                    addItem(panel); //only add new data panels when the main panel does not yet contain them
                    
                } else if(!itemAtIndex.equals(panel)) {
                    insertItem(i, panel); //sort the existing data panel to its intended position
                }
            }
            
            if(logger.isLoggable(Level.FINE)) {
                logger.fine("Finished updating state for concept: " + getCurrentState().getState().getName());
            }
        }

        @Override
        public AbstractKnowledgeSession getKnowledgeSession() {
            return null;
        }

        @Override
        public ConceptPerformanceState getCurrentState() {
            return currentState;
        }
        
        /**
         * Gets the assessment assigned by the OC for this condition item
         * 
         * @return the widget that contains the assessment. Cannot be null.
         */
        public OverallAssessmentBar getAssessment() {
            return barData;
        }
        
    }
    
    /**
     * Gets all of the assessments that have been provided by the OC for the
     * displayed conditions
     * 
     * @return the condition assessment. Will not be null, but can be empty.
     */
    private Map<Integer, List<ScoreNodeUpdate>> getConceptConditionAssessments(){
        
        scoreMediaToSave.clear();
        
        Map<Integer, List<ScoreNodeUpdate>> assessments = new HashMap<>();
        
        int count = taskItem.getChildCount();
        for(int i = 0; i < count; i++) {
            
            TreeItem child = taskItem.getChild(i);
            if(child instanceof ConceptPerformanceItem) {
                ConceptPerformanceItem concept = (ConceptPerformanceItem) child;
                assessments.putAll(getConceptConditionAssessments(concept));
            }
        }
        
        return assessments;
    }
    
    /**
     * Checks whether the given concept should be rendered. Concepts that are not course concepts and
     * concepts that are scenario support nodes are filtered out.
     * 
     * @param concept the concept to check. Cannot be null.
     * @return whether the given concept should be rendered.
     */
    public boolean shouldRender(ConceptPerformanceState concept) {
        
        if(isCourseConcept(concept.getState().getNodeId()) && !concept.getState().isScenarioSupportNode()) {
            
            if(concept instanceof IntermediateConceptPerformanceState) {
                for(ConceptPerformanceState child : ((IntermediateConceptPerformanceState) concept).getConcepts()) {
                    if(shouldRender(child)) {
                        
                        /* Render this concept if it has at least one rendered child concept*/
                        return true;
                    }
                }
                
            } else {
                return true;
            }
        }
        
        return false;
    }

    /**
     * Gets all of the assessments that have been provided by the OC for the
     * displayed conditions
     * 
     * @param concept the concept to look for condition assessments in. Cannot be null.
     * @return the condition assessment. Will not be null, but can be empty.
     */
    private Map<Integer, List<ScoreNodeUpdate>> getConceptConditionAssessments(ConceptPerformanceItem concept){
        
        Map<Integer, List<ScoreNodeUpdate>> assessments = new HashMap<>();
            
        List<ScoreNodeUpdate> childAssessments = new ArrayList<>();
        
        int count = concept.getChildCount();
        List<Condition> conditions = conceptNameToConditions.get(concept.getCurrentState().getState().getName());
        if(count < 1 && conditions != null){
            
            /* This is a leaf concept, so get the assessment provided by the OC */
            OverallAssessmentBar assessmentEditor = concept.getAssessment();
            if(!assessmentEditor.hasAssessmentChanged()) {
                
                /* This concept's assessment has not changed, so don't push any updates for it*/
                return assessments;
            }
            
            ScoreNodeUpdate scoreState = assessmentEditor.getScoreNodeState();
            if(scoreState != null) {
                
                /* Record the username of the OC */
                scoreState.setEvaluator(UiManager.getInstance().getUserName());
            }
            
            for(int i = 0; i < conditions.size(); i++) {
                
                /* Save the OC's provided assessment to each of the child conditions */
                childAssessments.add(scoreState);
            }
            
            if(assessmentEditor.hasObserverMediaChanged()) {
                
                /* The OC made a recording associated with this assessment that needs to be saved */
                scoreMediaToSave.add(assessmentEditor);
            }
            
        } else {
        
            /* This is a non-leaf concept, so gather assessments for all its children */
            for(int i = 0; i < count; i++) {
            
                TreeItem child = concept.getChild(i);
                if(child instanceof ConceptPerformanceItem){
                    
                    ConceptPerformanceItem childConcept = (ConceptPerformanceItem) child;
                    assessments.putAll(getConceptConditionAssessments(childConcept));
                }
            }
        }
        
        if(!childAssessments.isEmpty()) {
            
            /* Only add an entry to the assessments map if this concept has a changed assessment */
            assessments.put(concept.getCurrentState().getState().getNodeId(), childAssessments);
        }
        
        return assessments;
    }
    
    /**
     * Gets whether the performance node with the given ID is part of the course
     * concept hierarchy
     * 
     * @param conceptId the ID of the node to check
     * @return whether the node is part of the course hierarchy
     */
    private boolean isCourseConcept(int conceptId) {
        return courseConceptNodeIds.contains(conceptId);
    }
    
    /**
     * Parses the given task state to build a hierarchy of all the course concepts
     * found within it
     * 
     * @param task the task to parse for course concepts. If null, no course concepts
     * will be gathered.
     */
    private void gatherCourseConcepts(TaskPerformanceState task) {
        if(task == null) {
            return;
        }
        
        boolean isCourseConcept = CourseConceptProvider.get().isCourseConcept(task.getState().getName());  
        
        for(ConceptPerformanceState child : task.getConcepts()) {
            courseConceptNodeIds.addAll(gatherCourseConcepts(child, isCourseConcept));
        }
        
        if(isCourseConcept || !courseConceptNodeIds.isEmpty()) {
            courseConceptNodeIds.add(task.getState().getNodeId());
        }
    }
    
    /**
     * Parses the given concept state to build a hierarchy of all the course concepts
     * found within it
     * 
     * @param concept the concept to parse for course concepts. If null, no course concepts
     * will be gathered.
     * @param isParentCourseConcept whether the parent node is a course concept. If so,
     * this concept will be added to the course concept hierarchy.
     * @return all of the performance nodes underneath this task that were idenditied
     * as part of the course concept hierarchy
     */
    private Set<Integer> gatherCourseConcepts(ConceptPerformanceState concept, boolean isParentCourseConcept) {
        
        Set<Integer> courseConcepts = new HashSet<Integer>();
        
        if(concept == null) {
            return courseConcepts;
        }
        
        boolean isCourseConcept = CourseConceptProvider.get().isCourseConcept(concept.getState().getName());

        if(concept instanceof IntermediateConceptPerformanceState) {
            for(ConceptPerformanceState child : ((IntermediateConceptPerformanceState) concept).getConcepts()) {
                courseConcepts.addAll(gatherCourseConcepts(child, isCourseConcept || isParentCourseConcept));
            }
        }
          
        if(isCourseConcept || isParentCourseConcept || !courseConcepts.isEmpty()) {
            courseConcepts.add(concept.getState().getNodeId());
        }
        
        return courseConcepts;
    }
    
    /**
     * Gathers the current assessment levels of all the conditions in the dialog and queries the server 
     * for what their parent's assessments should look like after rolling up the assessment. When the 
     * server responds, the UI will be redrawn so that the parent concepts display their rolled-up 
     * assessment levels
     * 
     * @param skipUiScores if true, skips traversing the UI go gather scores entered by the OC and just uses 
     * the scores saved on the server. This can be useful while the UI is still being initialized.
     */
    private void recalculateRollUpAssessments(boolean skipUiScores) {
        
        /* Get the condition assessments */
        Map<Integer, List<ScoreNodeUpdate>> assessments = skipUiScores 
                ? new HashMap<Integer, List<ScoreNodeUpdate>>()
                : getConceptConditionAssessments();
        
        /* Query the server for their rollup assessments */
        UiManager.getInstance().getDashboardService().calculateRollUp(
                UiManager.getInstance().getSessionId(), 
                UiManager.getInstance().getUserName(),
                assessments, new AsyncCallback<GenericRpcResponse<Map<Integer,AssessmentLevelEnum>>>() {
                    
                    @Override
                    public void onSuccess(GenericRpcResponse<Map<Integer, AssessmentLevelEnum>> result) {
                        
                        if(result.getWasSuccessful()) {
                            
                            /* Redraw the UI with all the recalculated assessments */
                            nodeIdToAssessment.putAll(result.getContent());
                            redraw(false, true);
                            
                        } else {
                            UiManager.getInstance().displayDetailedErrorDialog(
                                    "Failed to Recalculate Scores",
                                    result.getException().getReason(), 
                                    result.getException().getDetails(),
                                    result.getException().getErrorStackTrace(),
                                    null);
                        }
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        
                        UiManager.getInstance().displayErrorDialog(
                                "Failed to Recalculate Scores", 
                                "An error occurred while recalculating the scores: " + caught.toString(), 
                                null);
                        hide();
                    }
                });
    }
}
