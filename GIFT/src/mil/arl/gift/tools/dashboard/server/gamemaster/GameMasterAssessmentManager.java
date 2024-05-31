/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.gamemaster;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainAssessmentContent;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.SurveyResultListener;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.score.ScoreUtil;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.aar.util.AbstractAarAssessmentManager;
import mil.arl.gift.common.aar.util.MessageManager;
import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainDKFHandler;
import mil.arl.gift.domain.knowledge.Concept;
import mil.arl.gift.domain.knowledge.CourseConceptSearchFilter;
import mil.arl.gift.domain.knowledge.DomainAssessmentKnowledge;
import mil.arl.gift.domain.knowledge.IntermediateConcept;
import mil.arl.gift.domain.knowledge.Scenario;
import mil.arl.gift.domain.knowledge.Task;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;
import mil.arl.gift.domain.knowledge.common.AbstractSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.AssessmentProxy;
import mil.arl.gift.domain.knowledge.common.ProxyPerformanceAssessment;
import mil.arl.gift.domain.knowledge.common.ScenarioActionInterface;
import mil.arl.gift.tools.authoring.common.conversion.AbstractLegacySchemaHandler;
import mil.arl.gift.domain.knowledge.common.metric.grade.GradeMetricInterface;
import mil.arl.gift.domain.knowledge.common.metric.grade.ObserverControllerGradeMetric;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;

/**
 * The assessment manager used by Game Master.
 * <br/><br/>
 * When this assessment manager is provided a DKF file, it will parse that DKF file in a manner similar
 * to the Domain module in order to build a hierarchy of domain assessment knowledge objects that is used
 * to emulate the assessment behavior that's used when running courses.
 * <br/><br/>
 * This used primarily used to allow assessments that are made to specific nodes to roll up to parent
 * nodes further up the hierarchy, just like how assessments propagate during courses.
 * 
 * @author nroberts
 */
public class GameMasterAssessmentManager extends AbstractAarAssessmentManager {

    /** The logger for this class */
    private static final Logger logger = LoggerFactory.getLogger(GameMasterAssessmentManager.class.getName());

    /** The domain assessment knowledge that's used to emulate Domain runtime assessment logic */
    private DomainAssessmentKnowledge domainKnowledge;
    
    /** A mapping from each performance node ID to its associated node object in the domain assessment knowledge */
    private final Map<Integer, AbstractPerformanceAssessmentNode> idToNode = new HashMap<>();
    
    /** A mapping from each peformance node ID to its associated performance state in the state that is 
     * currently being processed */
    private final Map<Integer, AbstractPerformanceState> idToState = new HashMap<>();
    
    /** An assessment proxy involved in emulating runtime assessment logic. Needed to register nodes. */
    private AssessmentProxy assessmentProxy = new AssessmentProxy();

    /** Sharable information about the scenario tracked by this assessment manager */
    private SessionScenarioInfo scenario;
    
    /**
     * Creates a new assessment manager that references the given DKF file and knowledge session
     * to build its assessment model and interacts with the Game Master in the GIFT Dashboard
     * 
     * @param dkfFileName the name of the DKF from which to derive assessment rules. Cannot be null.
     * @param session the knowledge session from a log that is associated with the DKF. Cannot be null.
     */
    public GameMasterAssessmentManager(String dkfFileName, AbstractKnowledgeSession session) {
        super(dkfFileName, session);
    }

    @Override
    public List<PerformanceStateAttribute> applyAndRollUp(EvaluatorUpdateRequest request, PerformanceState perfState) {
        
        if(perfState == null) {
            throw new IllegalArgumentException("The performance state to retrieve the current assessments from cannot be null");
        }
        
        /* Copy the assessments from the current performance state into the domain performance nodes */
        pushPerformanceStatesIntoNodes(perfState);
            
        /* 
         * Apply the evaluator update request to the node it is intended for. This will automatically
         * roll up to any parent performance nodes 
         */
        List<Integer> changedNodeIds = applyToNodesAndRollUp(request, perfState);
        
        /* Copy the current state of the changed domain assessment nodes into the current performance state. This pushes
         * the rolled-up assessments to the performance state*/
        List<PerformanceStateAttribute> changedAttributes = new ArrayList<>();
        for(Integer nodeId : changedNodeIds) {
            
            PerformanceStateAttribute newStateAttr = updatePerformanceAssessmentMetrics(
                    idToNode.get(nodeId), idToState.get(nodeId).getState());
            
            changedAttributes.add(newStateAttr);
        }
        
        return changedAttributes;
    }
    
    /**
     * Applies the given evaluator update request to its target node within the given performance state
     * and rolls its assessment up to its parent nodes.
     * 
     * @param request the evaluator update request to apply. Cannot be null.
     * @param perfState the performance state whose assessments should be modified. Cannot be null.
     * @return a list of IDs denoting the performance nodes whose states have changed, either due to
     * the update request itself or assessments that rolled up from it. Can be null if node targeted
     * by the request was never found.
     */
    private List<Integer> applyToNodesAndRollUp(EvaluatorUpdateRequest request, PerformanceState perfState){
        
        if(request == null) {
            throw new IllegalArgumentException("The request to apply to the performance state cannot be null");
        }
        
        if(perfState == null) {
            throw new IllegalArgumentException("The performance state to modify cannot be null");
        }
        
        for(TaskPerformanceState taskState : perfState.getTasks().values()) {
            
            int id = taskState.getState().getNodeId();
            
            if(Objects.equals(request.getNodeName(), taskState.getState().getName())){
                
                /* This is the node that the request is intended for, so apply it and then roll up */
                Task task = (Task) idToNode.get(id);
                task.evaluatorUpdateRequestReceived(request);
                
                if(logger.isInfoEnabled()) {
                    logger.info("Applied and rolled up evaluator update for task " + id);
                }
                
                return Collections.singletonList(id);
                
            } else {
                
                /* Otherwise, look for the node within this task */
                for(ConceptPerformanceState conceptState : taskState.getConcepts()) {
                    
                    List<Integer> foundNodes = applyToNodesAndRollUp(request, conceptState);
                    if(foundNodes != null) {
                        
                        /* Target was found inside this node, so add this node as a parent in the result */
                        List<Integer> toReturn = new ArrayList<>(foundNodes);
                        toReturn.add(id);
                        return toReturn;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Applies the given evaluator update request to its target node within the given conceptperformance state
     * and rolls its assessment up to its parent nodes.
     * 
     * @param request the evaluator update request to apply. Cannot be null.
     * @param conceptState the concept performance state whose assessments should be modified. Cannot be null.
     * @return a list of IDs denoting the performance nodes whose states have changed, either due to
     * the update request itself or assessments that rolled up from it. Can be null if node targeted
     * by the request was never found.
     */
    private List<Integer> applyToNodesAndRollUp(EvaluatorUpdateRequest request, ConceptPerformanceState conceptState){
        
        if(request == null) {
            throw new IllegalArgumentException("The request to apply to the performance state cannot be null");
        }
        
        if(conceptState == null) {
            throw new IllegalArgumentException("The concept performance state to modify cannot be null");
        }
        
        if(conceptState != null && conceptState.getState() != null) {
            
            int id = conceptState.getState().getNodeId();
            
            if(Objects.equals(request.getNodeName(), conceptState.getState().getName())){
            
                /* This is the node that the request is intended for, so apply it and then roll up */
                Concept concept = (Concept) idToNode.get(id);
                concept.evaluatorUpdateRequestReceived(request);
                
                if(logger.isInfoEnabled()) {
                    logger.info("Applied and rolled up evaluator update for concept " + id);
                }
                
                return Collections.singletonList(id);
                
            } else if(conceptState instanceof IntermediateConceptPerformanceState){
                
                /* Otherwise, look for the node within this concept */
                for(ConceptPerformanceState subConceptState : ((IntermediateConceptPerformanceState) conceptState).getConcepts()) {
                    
                    List<Integer> foundNodes = applyToNodesAndRollUp(request, subConceptState);
                    if(foundNodes != null) {
                        
                        /* Target was found inside this node, so add this node as a parent in the result */
                        List<Integer> toReturn = new ArrayList<>(foundNodes);
                        toReturn.add(id);
                        return toReturn;
                    }
                }
            }
        }
        
        return null;
    }
    
    /**
     * Pushes the states of the performance nodes in the given performance state into their equivalent
     * nodes within the domain assessment knowledge hierarchy.
     * <br/><br/>
     * This basically loads performance information obtained from a learner state into the runtime
     * assessment model.
     * 
     * @param perfState the performance state to push state info from. Cannot be null.
     */
    private void pushPerformanceStatesIntoNodes(PerformanceState perfState) {
        
        if(perfState == null) {
            throw new IllegalArgumentException("The performance state to push into the domain assessment knowledge cannot be null");
        }
        
        idToState.clear();
        
        for(TaskPerformanceState taskState : perfState.getTasks().values()) {
            
            AbstractPerformanceAssessmentNode node = idToNode.get(taskState.getState().getNodeId());
            if(node == null || !(node instanceof Task)) {
                throw new IllegalArgumentException("Could not find a task with the ID " + taskState.getState().getNodeId());
            }
            
            Task task = (Task) node;
            
            pushPerformanceStateIntoNode(task, taskState);
            
            for(ConceptPerformanceState conceptState : taskState.getConcepts()) {
                pushPerformanceStatesIntoNodes(conceptState);
            }
        }
    }
    
    /**
     * Pushes the states of the performance nodes in the given concept performance state into their equivalent
     * nodes within the domain assessment knowledge hierarchy.
     * <br/><br/>
     * This basically loads performance information obtained from a learner state into the runtime
     * assessment model.
     * 
     * @param conceptState the concept performance state to push state info from. Cannot be null.
     */
    private void pushPerformanceStatesIntoNodes(ConceptPerformanceState conceptState) {
        
        if(conceptState == null) {
            throw new IllegalArgumentException("The concept performance state to push into the domain assessment knowledge cannot be null");
        }
        
        AbstractPerformanceAssessmentNode node = idToNode.get(conceptState.getState().getNodeId());
        if(node == null || !(node instanceof Concept)) {
            throw new IllegalArgumentException("Could not find a concept with the ID " + conceptState.getState().getNodeId());
        }
        
        Concept concept = (Concept) node;
        
        pushPerformanceStateIntoNode(concept, conceptState);
        
        if(conceptState instanceof IntermediateConceptPerformanceState) {
            
            for(ConceptPerformanceState subConceptState : ((IntermediateConceptPerformanceState) conceptState).getConcepts()) {
                pushPerformanceStatesIntoNodes(subConceptState);
            }
        }
    }

    /**
     * Pushes the given performance node state into the given domain assessment knowledge node
     * 
     * @param node the domain assessment knowledge node that the state is being pushed into. Cannot be null.
     * @param state the performance node state to push in. Cannot be null.
     */
    private void pushPerformanceStateIntoNode(AbstractPerformanceAssessmentNode node, AbstractPerformanceState state) {
        
        if(node == null) {
            throw new IllegalArgumentException("The performance state to push into the domain assessment knowledge cannot be null");
        }
        
        if(state == null) {
            throw new IllegalArgumentException("The domain assessment knowledge node to push into cannot be null");
        }
        
        final Map<String, AssessmentLevelEnum> teamOrgEntities = state.getState().getAssessedTeamOrgEntities();
        boolean hasTeamMembers = CollectionUtils.isNotEmpty(teamOrgEntities);

        /* If the request has team org entities, then add them to the
         * assessment */
        if (hasTeamMembers) {
           node.getAssessment().getAssessedTeamOrgEntities().clear();
           node.getAssessment().getAssessedTeamOrgEntities().putAll(teamOrgEntities);
        }

        // update performance metric
        final AssessmentLevelEnum newMetric = state.getState().getShortTerm();
        if (newMetric != null) {
            node.getAssessment().updateAssessment(newMetric, true);

            /* If the request has no team org entities specified, then apply the
             * assessment update to all previously known entities */
            if (!hasTeamMembers) {
                final Map<String, AssessmentLevelEnum> assessedTeamOrgEntities = node.getAssessment()
                        .getAssessedTeamOrgEntities();
                for (String entity : assessedTeamOrgEntities.keySet()) {
                    assessedTeamOrgEntities.put(entity, newMetric);
                }
            }
        }
        
        node.getAssessment().setObserverComment(state.getState().getObserverComment());
        node.getAssessment().setObserverMedia(state.getState().getObserverMedia());
        node.getAssessment().setEvaluator(state.getState().getEvaluator());
        
        // update competence metric
        node.getAssessment().updateCompetence(state.getState().getCompetence(), true);

        // update confidence metric
        node.getAssessment().updateConfidence(state.getState().getConfidence(), true);

        // update priority metric
        node.getAssessment().updatePriority(state.getState().getPriority(), true);

        // update trend metric
        node.getAssessment().updateTrend(state.getState().getTrend(), true);

        /* Update the hold states */
        node.getAssessment().setAssessmentHold(state.getState().isAssessmentHold());
        node.getAssessment().setPriorityHold(state.getState().isPriorityHold());
        node.getAssessment().setConfidenceHold(state.getState().isConfidenceHold());
        node.getAssessment().setCompetenceHold(state.getState().isCompetenceHold());
        node.getAssessment().setTrendHold(state.getState().isTrendHold());
        
        /* Update the activity state */
        node.getAssessment().setNodeStateEnum(state.getState().getNodeStateEnum());
        
        idToState.put(state.getState().getNodeId(), state);
    }

    @Override
    public void loadDkf(File dkf) throws Exception{
        
        if(dkf != null) {
            
            /* Parse the DKF just like the domain does to obtain a similar model of the scenario
             * - handle legacy schema versions
             */
            FileProxy orig = new FileProxy(dkf);
            UnmarshalledFile newFile = AbstractLegacySchemaHandler.getUnmarshalledFile(orig, FileType.DKF);
            if(newFile.isUpconverted()){
                // back up original dkf to new backup file name and write upconverted dkf to original file name
                
                //backup original file (with non .xml file extension)
                String origFilename = orig.getFileId();
                File backup = new File(origFilename + Constants.BACKUP_SUFFIX);
                FileUtils.copyInputStreamToFile(orig.getInputStream(), backup);
                
                AbstractSchemaHandler.writeToFile(newFile.getUnmarshalled(), new File(orig.getFileId()), true);
            }
            
            // now the current dkf file is the latest schema version
            DomainDKFHandler dkfHandler = new DomainDKFHandler(new FileProxy(dkf), new DesktopFolderProxy(dkf.getParentFile()), null, false);
            dkfHandler.shouldSkipExternalFileLoading(true);
            
            // generate information about this scenario to share with the game master client
            this.scenario = new SessionScenarioInfo(dkfHandler.getScenario());
            
            // populate the scenario info with additional information about all of the concepts in the scenario
            if(scenario != null) {
                for(generated.dkf.Task task : scenario.getScenario().getAssessment().getTasks().getTask()) {
                    for(generated.dkf.Concept concept : task.getConcepts().getConcept()) {
                        populateConditionInfo(scenario, concept);
                    }
                }
            }
            
            /* Build the domain assessment knowledge so that it can be used to roll up assessments */
            this.domainKnowledge = dkfHandler.getDomainAssessmentKnowledge();
            domainKnowledge.registerNodes(assessmentProxy);
            
            /* Provide a scenario action interface that can be used to perform scoring
             * with the user data from the knowledge session */
            domainKnowledge.getScenario().initialize(new ScenarioActionInterface() {
                
                @Override
                public void trainingApplicationRequest(TrainingAppInfoRequest infoRequest) {
                    //Game Master playbacks do not implement this, so ignore
                }
                
                @Override
                public void scenarioStarted() {
                    //Game Master playbacks do not implement this, so ignore
                }
                
                @Override
                public void scenarioEnded(LessonCompletedStatusType status) {
                    //Game Master playbacks do not implement this, so ignore
                }
                
                @Override
                public void performanceAssessmentUpdated(ProxyPerformanceAssessment performanceAssessment) {
                    //Game Master playbacks do not implement this, so ignore
                }
                
                @Override
                public void handleDomainActionWithLearner(DomainAssessmentContent action) {
                    //Game Master playbacks do not implement this, so ignore
                }
                
                @Override
                public SessionMembers getSessionMembers() {
                    
                    /* Use the provided knowledge session to get the team members*/
                    return getKnowledgeSession().getSessionMembers();
                }
                
                @Override
                public List<MessageManager> getPlaybackMessages() {
                    //Game Master playbacks do not implement this, so ignore
                    return null;
                }
                
                @Override
                public void fatalError(String reason, String details) {
                    logger.error("Caught error from emulated scenario. Reason: " + reason +"\nDetails: " + details);
                }
                
                @Override
                public void displayDuringLessonSurvey(AbstractSurveyLessonAssessment surveyAssessment,
                        SurveyResultListener surveyResultListener) {
                    //Game Master playbacks do not implement this, so ignore
                }
            });
            
            gatherPerfNodes();
        }
    }
    
    /**
     * Gathers and maps all the performance assessment nodes from the domain assessment knowledge so 
     * that they can later be quickly referenced by other operations without traversing the entire
     * performance node hierarchy.
     */
    private void gatherPerfNodes() {
        
        if(domainKnowledge == null || domainKnowledge.getScenario() == null) {
            throw new IllegalArgumentException("The domain assessment knowledge to gather nodes from cannot be null");
        }
        
        for(Task task : domainKnowledge.getScenario().getTasks()) {
            idToNode.put(task.getNodeId(), task);
            
            for(Concept concept : task.getConcepts()) {
                gatherPerfNodes(concept);
            }
            
            task.initialize();
        }
    }
    
    /**
     * Gathers and maps all the performance assessment nodes from the domain assessment knowledge so 
     * that they can later be quickly referenced by other operations without traversing the entire
     * performance node hierarchy.
     * 
     * @param concept the concept to gather nodes from. Cannot be null.
     */
    private void gatherPerfNodes(Concept concept) {
        
        if(concept == null) {
            throw new IllegalArgumentException("The concept to gather nodes from cannot be null");
        }
        
        
        idToNode.put(concept.getNodeId(), concept);
        
        if(concept instanceof IntermediateConcept) {
            for(Concept subConcept : ((IntermediateConcept) concept).getConcepts()) {
                gatherPerfNodes(subConcept);
            }
        }
    }
    
    /**
     * Creates an updated version of the given performance state attribute that is populated
     * with modified data from the given domain assessment knowledge node
     * 
     * @param node the domain assessment knowledge node to push data from. Cannot be null.
     * @param perfStateAttr the performance state attribute to update. Cannot be null.
     * @return the updated performance state attribute that was created.
     */
    private PerformanceStateAttribute updatePerformanceAssessmentMetrics(AbstractPerformanceAssessmentNode node,
            PerformanceStateAttribute perfStateAttr) {
        
        if (node == null) {
            throw new IllegalArgumentException("The domain assessment knowledge to provide the update data cannot be null");
            
        } else if (perfStateAttr == null) {
            throw new IllegalArgumentException("The performance state attribute to update cannot be null.");
        }

        PerformanceStateAttribute toReturn = perfStateAttr.deepCopy();
        
        pushNodeIntoPerformanceState(toReturn, node);
        
        return toReturn;
    }

    /**
     * Pushes data from the given domain assessment knowledge node into the given performance node state 
     * 
     * @param state the performance node state to push into. Cannot be null.
     * @param node the domain assessment knowledge node whose data should be pushed in. Cannot be null.
     */
    private void pushNodeIntoPerformanceState(PerformanceStateAttribute state,
            AbstractPerformanceAssessmentNode node) {
        
        if (node == null) {
            throw new IllegalArgumentException("The domain assessment knowledge to push from cannot be null");
            
        } else if (state == null) {
            throw new IllegalArgumentException("The performance state attribute to push into cannot be null.");
        }
        
        final Map<String, AssessmentLevelEnum> teamOrgEntities = node.getAssessment().getAssessedTeamOrgEntities();
        boolean hasTeamMembers = CollectionUtils.isNotEmpty(teamOrgEntities);

        /* If the request has team org entities, then add them to the
         * assessment */
        if (hasTeamMembers) {
            state.setAssessedTeamOrgEntities(teamOrgEntities);
        }

        // update performance metric
        final AssessmentLevelEnum newMetric = node.getAssessment().getAssessmentLevel();
        if (newMetric != null) {
            state.updateShortTerm(newMetric, true);

            /* If the request has no team org entities specified, then apply the
             * assessment update to all previously known entities */
            if (!hasTeamMembers) {
                final Map<String, AssessmentLevelEnum> assessedTeamOrgEntities = state
                        .getAssessedTeamOrgEntities();
                for (String entity : assessedTeamOrgEntities.keySet()) {
                    assessedTeamOrgEntities.put(entity, newMetric);
                }
            }
        }

        state.setObserverComment(node.getAssessment().getObserverComment());
        state.setObserverMedia(node.getAssessment().getObserverMedia());

        // update competence metric
        state.setCompetence(node.getAssessment().getCompetence(), true);

        // update confidence metric
        state.setConfidence(node.getAssessment().getConfidence(), true);

        // update priority metric
        state.setPriority(node.getAssessment().getPriority(), true);

        // update trend metric
        state.setTrend(node.getAssessment().getTrend(), true);

        // update the user that updated the metrics
        if (node.getAssessment().getEvaluator() != null) {
            state.setEvaluator(node.getAssessment().getEvaluator());
        }

        /* Update the hold states */
        state.setAssessmentHold(node.getAssessment().isAssessmentHold());
        state.setPriorityHold(node.getAssessment().isPriorityHold());
        state.setConfidenceHold(node.getAssessment().isConfidenceHold());
        state.setCompetenceHold(node.getAssessment().isCompetenceHold());
        state.setTrendHold(node.getAssessment().isTrendHold());
        
        /* Update the activity state */
        state.setNodeStateEnum(node.getAssessment().getNodeStateEnum());

        if (state.getAssessmentExplanation() == null) {
            state.setAssessmentExplanation(new HashSet<String>(), true);
        }

        state.setPerformanceAssessmentTime(node.getAssessment().getTime());

        /* Set assessment explanation value */
        if (StringUtils.isNotBlank(node.getAssessment().getObserverComment())) {
            /* Use the optional bookmark value provided by the observer */
            state.getAssessmentExplanation().clear();
            state.getAssessmentExplanation().add(node.getAssessment().getObserverComment());
        } else if (hasTeamMembers) {
            /* Create assessment explanation based on team org members
             * selected */
            StringBuilder sb = new StringBuilder("[");
            StringUtils.join(", ", teamOrgEntities.keySet(), sb);
            sb.append("] ").append(teamOrgEntities.size() == 1 ? "has" : "have").append(" been assessed.");
            state.getAssessmentExplanation().clear();
            state.getAssessmentExplanation().add(sb.toString());
        }
    }
    
    @Override
    public SessionScenarioInfo getScenario(){
        return this.scenario;
    }
    
    /**
     * Populates the given session scenario info with condition info about all the conditions
     * found in the given concept
     * 
     * @param scenario the session scenario info to populate. If null, nothing will be populated.
     * @param concept the concept whose conditions should be used to populate the scenario info.
     * If null, nothing will be populated.
     */
    private void populateConditionInfo(SessionScenarioInfo scenario, generated.dkf.Concept concept) {
        
        if(scenario == null || concept == null) {
            return;
        }
        
        if(concept.getConditionsOrConcepts() instanceof generated.dkf.Concepts) {
            generated.dkf.Concepts concepts = (generated.dkf.Concepts) concept.getConditionsOrConcepts();
            for(generated.dkf.Concept childConcept : concepts.getConcept()) {
                populateConditionInfo(scenario, childConcept);
            }
            
        } else if(concept.getConditionsOrConcepts() instanceof generated.dkf.Conditions) {
            generated.dkf.Conditions conditions = (generated.dkf.Conditions) concept.getConditionsOrConcepts();
            for(generated.dkf.Condition condition : conditions.getCondition()) {
                
                String impl = condition.getConditionImpl();
                
                if(impl != null && scenario.getConditonInfo(impl) == null) {
                    scenario.addConditionInfo(
                            impl, 
                            DomainKnowledgeUtil.getConditionInfoForConditionImpl(impl));
                }
            }
        }
    }
    
    @Override
    public GradedScoreNode scoreOverallAsessments(Map<Integer, List<ScoreNodeUpdate>> conceptToConditionAssessments, Collection<String> courseConcepts) {
        
        Scenario knowledgeScenario = domainKnowledge.getScenario();
        knowledgeScenario.getCourseConcepts(new CourseConceptSearchFilter(courseConcepts), new HashSet<String>());
        
        /* Figure out which task scores need to be recalculated based on the provided concept condition assessments */
        Set<Task> affectedTasks = getParentTasks(knowledgeScenario, conceptToConditionAssessments.keySet());
        
        //the root of the tree
        GradedScoreNode node;
        if(scenario.getCurrentScore() != null) {
            
            /* Clone the existing root score so that we can update the performance node scores in a new object. 
             * 
             * We don't want to modify scenario.getCurrentScore() directly, since that will affect the in-memory
             * representation of the original PublicLessonScoreRequest message.*/
            node = GradedScoreNode.deepCopy(scenario.getCurrentScore());
            
        } else {
            
            /* Create a new score from scratch */
            node = new GradedScoreNode(knowledgeScenario.getName());
        }
        
        /* Create a mapping from each leaf concept to its existing score for quick searching */
        Map<Integer, GradedScoreNode> existingConceptAssessments = getExistingLeafConceptScores(node);

        for(Task task : knowledgeScenario.getTasks()){
            
            if(!affectedTasks.contains(task)) {
                
                /* This task was not given a new score by the OC, so skip recalculating the score */
                continue;
            }

            //don't include scores for tasks that have never executed (ticket #1259)
            try{
                GradeMetricInterface metric = new ObserverControllerGradeMetric(conceptToConditionAssessments, existingConceptAssessments);
                task.setGradeMetric(metric);
                
                GradedScoreNode child = task.getScore();
                
                if(child != null){
                    
                    /* See if there is an existing score for this task that needs to be replaced */
                    AbstractScoreNode existing = node.getChildren().stream().filter(otherChild -> {
                        return child.getPerformanceNodeId() == otherChild.getPerformanceNodeId();
                    }).findFirst().orElse(null);
                    
                    if(existing == null) {
                        node.addChild(child); /* Add the new task score */
                        
                    } else {
                        
                        /* Replace the existing score */
                        node.setChild(node.getChildren().indexOf(existing), child);
                    }

                }
                
            }catch(Exception e){
                throw new RuntimeException("An error happened while trying to calculate the score for the task '"+task.getName()+"'.", e);
            }
        }
        
        if(node.isLeaf()){
            //no scoring information was given
            return null;
        }else{
            // Need to recalculate the descendant GradedScoreNodes to ensure that the rollup assessments that are saved
            // properly match when the summative scores dialog displays
            // Note: this is copied from Scenario.getScores()
            ScoreUtil.performAssessmentRollup(node, true);
            return node;
        }
    }
    
    /**
     * Gets all of the tasks that contain the given concept performance node IDs
     * 
     * @param scenario the scenario to search for tasks in. If null, no tasks will be returned.
     * @param conceptIds the performance node IDs of the concepts to look for. If null, no tasks will be retuned
     * @return the tasks that contain concepts with the given performance node IDs. Can be empty.
     */
    private Set<Task> getParentTasks(Scenario scenario, Collection<Integer> conceptIds){
        Set<Task> parentTasks = new HashSet<>();
        
        if(scenario != null && conceptIds != null) {
            for(Task task : scenario.getTasks()) {
                for(Concept concept : task.getConcepts()) {
                    
                    if(containsConcept(concept, conceptIds)) {
                        parentTasks.add(task);
                        continue;
                    }
                }
                
                if(parentTasks.contains(task)) {
                    continue;
                }
            }
        }
        
        return parentTasks;
    }
    
    /**
     * Checks whether the given concept contains a concept with any of the given performance node IDs
     * 
     * @param concept the concept to search for performance node IDs in. If null, false will be returned.
     * @param conceptIds the performance node IDs of the concepts to look for. If null, false will be retured.
     * @return whether the concept contains oneof the concept IDs.
     */
    private boolean containsConcept(Concept concept, Collection<Integer> conceptIds){
        
        if(concept == null || conceptIds == null) {
            return false;
        }
        
        if(conceptIds.contains(concept.getNodeId())){
            return true;
        }
        
        if(concept instanceof IntermediateConcept) {
            for(Concept child : ((IntermediateConcept) concept).getConcepts()) {
                if(containsConcept(child, conceptIds)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public Map<Integer, AssessmentLevelEnum> calculateRollUp(Map<Integer, List<ScoreNodeUpdate>> ocAssessments) {
        
        if(ocAssessments == null) {
            throw new IllegalArgumentException("The observer controllar assessments cannot be null");
        }
        
        ArrayList<String> courseConcepts = new ArrayList<>();
        CourseConceptsUtil.getConceptNamesFromHierarchy(scenario.getCourseConcepts().getConceptNode(), courseConcepts);
        
        GradedScoreNode rootNode = scoreOverallAsessments(ocAssessments, courseConcepts);
        
        // Note: using null because based on the javadoc for calculateRollUp, this call to ScoreUtil shouldn't modify the rootNode
        return ScoreUtil.performAssessmentRollup(rootNode, null);
    }
    
    /**
     * Looks within the given score node for any score nodes representing leaf concepts and then
     * returns those concepts' score nodes
     * 
     * @param node the score node within which to search. If null, the returned map will be empty.
     * @return a mapping from each leaf concept's performance node ID to its score. Will not be null.
     */
    private Map<Integer, GradedScoreNode> getExistingLeafConceptScores(GradedScoreNode node) {
        
        Map<Integer, GradedScoreNode> existingScores = new HashMap<>();
        if(node == null) {
            return existingScores;
        }
        
        for(AbstractScoreNode child : node.getChildren()) {
            
            if(child instanceof GradedScoreNode){
                
                /* This is not a leaf concept, so keep exploring the children */
                existingScores.putAll(getExistingLeafConceptScores((GradedScoreNode)child));
                
            } else {
                
                /* This is a leaf concept, so track its score */
                existingScores.put(node.getPerformanceNodeId(), node);
            }
        }
        
        return existingScores;
    }
}
