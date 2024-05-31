/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.RequestDoNothingTactic;
import mil.arl.gift.common.RequestInstructionalIntervention;
import mil.arl.gift.common.RequestMidLessonMedia;
import mil.arl.gift.common.RequestPerformanceAssessment;
import mil.arl.gift.common.RequestScenarioAdaptation;
import mil.arl.gift.common.course.BasicCourseHandler;
import mil.arl.gift.common.course.CourseActionKnowledge;
import mil.arl.gift.common.course.dkf.DomainActionKnowledge;
import mil.arl.gift.common.course.dkf.strategy.PerformanceAssessmentStrategy;
import mil.arl.gift.common.course.dkf.transition.PerformanceNodeTransition;
import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.course.strategy.DoNothingStrategy;
import mil.arl.gift.common.course.strategy.InstructionalInterventionStrategy;
import mil.arl.gift.common.course.strategy.MidLessonMediaStrategy;
import mil.arl.gift.common.course.strategy.ScenarioAdaptationStrategy;
import mil.arl.gift.common.course.transition.AbstractTransition;
import mil.arl.gift.common.course.transition.LearnerStateTransition;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.IntermediateConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.ped.DKFActionsHandler;
import mil.arl.gift.ped.PedagogicalModel;
import mil.arl.gift.ped.common.HighestPriorityTransitionSelection;
import mil.arl.gift.ped.common.TransitionSelectionInterface;


/**
 * A pedagogical model that uses learner state transitions to select from instructional
 * strategies to request.
 *
 * @author cragusa
 */

public class TransitionsAgent implements PedagogicalModel {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(TransitionsAgent.class);

    /** The name to use when creating a {@link RequestDoNothingTactic} */
    private static final String DO_NOTHING_TACTIC_NAME = "Do Nothing Tactic";

    /** contains macro level Ped information (e.g. course level actions) */
    private CourseActionKnowledge macroActionKnowledge;

    /** contains micro level Ped information (e.g. DKF level actions) */
    private DomainActionKnowledge microActionKnowledge;

    /** the last learner state received.  Will be null until the first learner state has been handled. */
    private LearnerState prevLearnerState;

    /** handler for unmarshalling "Actions" XML content for a Course */
    private BasicCourseHandler courseHandler = new BasicCourseHandler();

    /** handler for unmarshalling "Actions" XML content for a DKF */
    private DKFActionsHandler dkfActionsHandler = new DKFActionsHandler();

    /**
     * The transition selection algorithm to use to help filter the list of transitions satisfied by
     * the latest learner state.
     */
    private static TransitionSelectionInterface transitionSelection = new HighestPriorityTransitionSelection();

    /**
     * Handle the learner state performance assessment by determining the appropriate ped actions.
     *
     * @param performance - the latest learner performance state
     * @param request - the pedagogical request being built which contains pedagogical actions to take on the latest learner state message
     */
    private void handlePerformanceAssessmentState(PerformanceState performance, PedagogicalRequest request){

        if(microActionKnowledge == null){
            return;
        }

        Map<Integer, TaskPerformanceState> tasks = performance.getTasks();

        //get previous tasks performance
        Map<Integer, TaskPerformanceState> prevTasks = null;
        if(prevLearnerState != null){
            prevTasks = prevLearnerState.getPerformance().getTasks();
        }

        //iterate over tasks
        if(tasks.isEmpty() && prevTasks != null) {
        	
        	/* 
        	 * A lesson may have just started after just ending a previous one, in which 
        	 * case the map of tasks would be empty. We need to handle this as a special case, 
        	 * otherwise the "satisfied" state of any performance-based logical expressions 
        	 * will not reset, so the current learner state could re-trigger already fired state 
        	 * transitions because they think they are still satisfied
        	 * 
        	 * To make sure none of the transitions fire again, we can just feed the last received
        	 * performance data through the updating logic so that it thinks the state has not changed.
        	 * This will set the "safisfied" flags off the transitions to false, thereby
        	 * preventing the accidental triggering of strategies.
        	 */
        	for(LearnerStateTransition transition : microActionKnowledge.getTransitions()) {
        	    for(AbstractTransition subTransition : transition.getTransitions()) {
        	        subTransition.isSatisfiedAndReset();
        	    }
        	}
        }
        
        for( Integer taskKey : tasks.keySet() ) {

            TaskPerformanceState taskPerformance = tasks.get(taskKey);
            TaskPerformanceState prevTaskPerformance = null;
            if(prevTasks != null){
                prevTaskPerformance = prevTasks.get(taskKey);
            }

            //get the top level performance for each task
            PerformanceStateAttribute taskPerformanceAttribute = taskPerformance.getState();

            //get task transition knowledge, if available
            List<LearnerStateTransition> transitions = microActionKnowledge.getTransitions(taskKey);
            if(transitions != null){
                //provide each transition with the current and previous attribute values to determine if a transition has been satisfied

                PerformanceStateAttribute prevTaskPeftAttr = null;
                if(prevTaskPerformance != null){
                    prevTaskPeftAttr = prevTaskPerformance.getState();
                }

                for(LearnerStateTransition transition : transitions){

                    transition.update(taskKey,
                                    null,
                                    prevTaskPeftAttr != null ? prevTaskPeftAttr.getShortTerm() : null,
                                    prevTaskPeftAttr != null ? prevTaskPeftAttr.getShortTermTimestamp() : 0,
                                    taskPerformanceAttribute.getShortTerm(),
                                    taskPerformanceAttribute.getShortTermTimestamp());

                }
            }

            handleConceptPerformanceAssessmentState(taskPerformance.getConcepts(),
                    prevTaskPerformance != null ? prevTaskPerformance.getConcepts() : null);

        }//end for taskKey
    }

    /**
     * Check the new concept performance states against the previous concept performance states and update accordingly.
     * This will recursively walk sub-concepts if found.
     *
     * @param newConceptPerformances incoming performance states to compare against any previous states.  Can't be null.
     * @param previousConceptPerformances previous states.  Can be null or empty.
     */
    private void handleConceptPerformanceAssessmentState(List<ConceptPerformanceState> newConceptPerformances, List<ConceptPerformanceState> previousConceptPerformances){

        List<LearnerStateTransition> transitions;

        //iterate over the performance for the concepts
        for( ConceptPerformanceState conceptPerformance : newConceptPerformances) {

            ConceptPerformanceState prevConceptPerformance = null;
            if(previousConceptPerformances != null){
                for(ConceptPerformanceState prevConceptPerformanceState : previousConceptPerformances){
                    if(conceptPerformance.getState().getNodeId() == prevConceptPerformanceState.getState().getNodeId()){
                        prevConceptPerformance = prevConceptPerformanceState;
                        break;
                    }
                }
            }

            //get task transition knowledge, if available
            transitions = microActionKnowledge.getTransitions(conceptPerformance.getState().getNodeId());
            if(transitions != null){
                //compare current concept performance to previous performance

                PerformanceStateAttribute prevConceptPeftAttr = null;
                if(prevConceptPerformance != null){
                    prevConceptPeftAttr = prevConceptPerformance.getState();
                }

                for(LearnerStateTransition transition : transitions){

                    //TODO: need to handle on short term, long term, and predicted values
                    transition.update(conceptPerformance.getState().getNodeId(),
                                    null,
                                    prevConceptPeftAttr != null ? prevConceptPeftAttr.getShortTerm() : null,
                                    prevConceptPeftAttr != null ? prevConceptPeftAttr.getShortTermTimestamp() : 0,
                                    conceptPerformance.getState().getShortTerm(),
                                    conceptPerformance.getState().getShortTermTimestamp());

                }
            }

            // recursively walk the nesting of concepts
            if(conceptPerformance instanceof IntermediateConceptPerformanceState){
                IntermediateConceptPerformanceState newIntermediateConceptPerfState = (IntermediateConceptPerformanceState)conceptPerformance;
                IntermediateConceptPerformanceState prevIntermediateConceptPerfState = (IntermediateConceptPerformanceState)prevConceptPerformance;
                handleConceptPerformanceAssessmentState(newIntermediateConceptPerfState.getConcepts(),
                        prevIntermediateConceptPerfState != null ? prevIntermediateConceptPerfState.getConcepts() : null);
            }

        }//end for conceptKey
    }

    /**
     * Handle the learner cognitive state by determining the appropriate ped actions.
     *
     * @param cognitive - the latest learner cognitive state
     * @param request - the pedagogical request being built which contains pedagogical actions to take on the latest learner state message
     */
    private void handleCognitiveState(CognitiveState cognitive, PedagogicalRequest request){

        if(microActionKnowledge == null && macroActionKnowledge == null){
            return;
        }

        if( cognitive != null ) {

            //get the previous cognitive state values
            Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> prevCognitive = null;
            if(prevLearnerState != null){
                prevCognitive = prevLearnerState.getCognitive().getAttributes();
            }

            for( LearnerStateAttributeNameEnum key : cognitive.getAttributes().keySet() ) {

                LearnerStateAttribute cognitiveStateAttribute = cognitive.getAttributes().get(key);
                LearnerStateAttribute prevCognitiveStateAttribute = null;
                if(prevCognitive != null){
                    prevCognitiveStateAttribute = prevCognitive.get(key);
                }

                updateMicroTransitions(key, cognitiveStateAttribute, prevCognitiveStateAttribute);
                updateMacroTransitions(key, cognitiveStateAttribute, prevCognitiveStateAttribute);

            }//end for key
        }
    }

    /**
     * Update the micro-adaptive transitions with the latest attribute information.
     *
     * @param key unique identifier associated with the state attribute being updated
     * @param currentStateAttribute the current value of the state attribute
     * @param previousStateAttribute the previous value of the state attribute.  Can be null if this is the first update.
     */
    private void updateMicroTransitions(LearnerStateAttributeNameEnum key, LearnerStateAttribute currentStateAttribute, LearnerStateAttribute previousStateAttribute){

        if(microActionKnowledge != null){
            updateTransitions(key, currentStateAttribute, previousStateAttribute, microActionKnowledge.getTransitions());
        }
    }

    /**
     * Update the macro-adaptive transitions with the latest attribute information.
     *
     * @param key unique identifier associated with the state attribute being updated
     * @param currentStateAttribute the current value of the state attribute
     * @param previousStateAttribute the previous value of the state attribute.  Can be null if this is the first update.
     */
    private void updateMacroTransitions(LearnerStateAttributeNameEnum key, LearnerStateAttribute currentStateAttribute, LearnerStateAttribute previousStateAttribute){

        if(macroActionKnowledge != null){
            updateTransitions(key, currentStateAttribute, previousStateAttribute, macroActionKnowledge.getTransitions());
        }
    }

    /**
     * Update the list of learner state transitions with the previous and current attribute value(s).
     *
     * @param key - unique identifier of the state attribute being updated.
     * @param currentStateAttribute - the current values of the state attribute.
     * @param previousStateAttribute - the previous values of the state attribute.  Can be null if this is the first update.
     * @param transitions - list of transitions to update (can be null)
     */
    private void updateTransitions(LearnerStateAttributeNameEnum key, LearnerStateAttribute currentStateAttribute, LearnerStateAttribute previousStateAttribute, List<LearnerStateTransition> transitions){

        if(transitions != null){
            //compare current attribute value to previous value

            AbstractEnum prevST = null;
            long prevSTTimestamp = 0;
            if(previousStateAttribute != null){
                prevST = previousStateAttribute.getShortTerm();
                prevSTTimestamp = previousStateAttribute.getShortTermTimestamp();
            }

            AbstractEnum currST = currentStateAttribute.getShortTerm();
            long currSTTimestamp = currentStateAttribute.getShortTermTimestamp();
            
            boolean isCollection = currentStateAttribute instanceof LearnerStateAttributeCollection;

            for(LearnerStateTransition transition : transitions){

                //TODO: need to handle on short term, long term, and predicted values
                
                if(isCollection) {
                    // check each entry in the collection (but not the root)
                    // e.g. Knowledge (root) with course concepts as descendants
                    LearnerStateAttributeCollection currStateCollection = (LearnerStateAttributeCollection)currentStateAttribute;
                    for(String label : currStateCollection.getAttributes().keySet()) {
                        LearnerStateAttribute currSubAttribute = currStateCollection.getAttributes().get(label);
                        
                        LearnerStateAttribute prevSubAttribute = null;
                        if(previousStateAttribute != null && previousStateAttribute instanceof LearnerStateAttributeCollection) {
                            // get sub attribute's previous state information
                            
                            LearnerStateAttributeCollection prevStateCollection = (LearnerStateAttributeCollection)previousStateAttribute;
                            prevSubAttribute = prevStateCollection.getAttributes().get(label);
                            if(prevSubAttribute != null) {
                                prevST = prevSubAttribute.getShortTerm();
                                prevSTTimestamp = prevSubAttribute.getShortTermTimestamp();
                            }
                        }
                        
                        transition.update(key, label, prevST, prevSTTimestamp, 
                                currSubAttribute.getShortTerm(), currSubAttribute.getShortTermTimestamp());
                        
                        if(currSubAttribute instanceof LearnerStateAttributeCollection) {
                            // keep going down the descendant hierarchy
                            updateTransitions(key, currSubAttribute, prevSubAttribute, transitions);
                        }
                    }
                    
                }else {
                    transition.update(key, null, prevST, prevSTTimestamp, currST, currSTTimestamp);
                }

            }//end for transition
        }
    }

    /**
     * Handle the learner affective assessment by determining the appropriate ped actions.
     *
     * @param affective - the latest learner affective state
     * @param request - the pedagogical request being built which contains pedagogical actions to take on the latest learner state message
     */
    private void handleAffectiveState(AffectiveState affective, PedagogicalRequest request){

        if(microActionKnowledge == null && macroActionKnowledge == null){
            return;
        }

        if( affective != null) {

            //get the previous affective state values
            Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> prevAffective = null;
            if(prevLearnerState != null){
                prevAffective = prevLearnerState.getAffective().getAttributes();
            }

            for( LearnerStateAttributeNameEnum key : affective.getAttributes().keySet() ) {

                LearnerStateAttribute affectiveStateAttribute = affective.getAttributes().get(key);
                LearnerStateAttribute prevAffectiveStateAttribute = null;
                if(prevAffective != null){
                    prevAffectiveStateAttribute = prevAffective.get(key);
                }

                updateMicroTransitions(key, affectiveStateAttribute, prevAffectiveStateAttribute);
                updateMacroTransitions(key, affectiveStateAttribute, prevAffectiveStateAttribute);

            }
        }
    }

    /**
     * Build Ped action requests based on the transitions that have been satisfied.
     *
     * @param state - the latest learner state
     * @param transitions - transitions to look at for being satisfied at this point in time.
     * @param request - the pedagogical request being built which contains pedagogical actions to possibly added too
     * @param macroAction - whether or not the actions to suggest are macro instructional strategy actions.
     */
    private void populateActions(LearnerState state,
            List<LearnerStateTransition> transitions,
            PedagogicalRequest request,
            boolean macroAction){

        List<LearnerStateTransition> transitionsFired = new ArrayList<>();

        for(LearnerStateTransition transition : transitions){

            if(transition.shouldTransition()){

                if(logger.isInfoEnabled()){
                    logger.info("The transition of "+transition+" has been satisfied.");
                }

                transitionsFired.add(transition);
            }
        }

        //
        // Transition Filter Algorithm
        //
        if(logger.isInfoEnabled()){
            logger.info("Attempting to filter the list of transitions based on "+transitionSelection.getClass().getName()+" logic.");
        }
        transitionSelection.filter(state, transitionsFired);

        for (LearnerStateTransition transitionFired : transitionsFired) {

            List<AbstractStrategy> activities = transitionFired.getNextActivitySet();
            if (activities.isEmpty()) {
                // if no activities exist for the strategy, send a do nothing with a null name
                RequestDoNothingTactic doNothingRequest = new RequestDoNothingTactic(DO_NOTHING_TACTIC_NAME);
                doNothingRequest.setIsMacroRequest(macroAction);
                request.addRequest(transitionFired.getReasonForActivation(), doNothingRequest);
            } else {
                for (AbstractStrategy activity : activities) {
                    AbstractPedagogicalRequest activityRequest = buildNewAbstractPedagogicalRequest(transitionFired, activity);

                    if (activityRequest == null) {
                        logger.error("Received unhandled strategy: " + activity + " for transition of "
                                + transitionFired + ".  This strategy will be skipped.");
                        continue;
                    }

                    if (activity.getDelayAfterStrategy() > 0) {
                        activityRequest.setDelayAfterStrategy(activity.getDelayAfterStrategy());
                    }

                    activityRequest.setIsMacroRequest(macroAction);
                    activityRequest.setReasonForRequest(transitionFired.getReasonForActivation());
                    request.addRequest(transitionFired.getReasonForActivation(), activityRequest);
                }
            }
        }
    }

    /**
     * Builds a new pedagogical request based on the strategy type.
     *
     * @param transitionFired the transition that is being fired that we need to request the action
     *        for.
     * @param activity the activity that is being fired from the transition.
     * @return an {@link AbstractPedagogicalRequest} based on the strategy type and populated with
     *         the strategy name. Will return null if the strategy type is unknown.
     */
    private AbstractPedagogicalRequest buildNewAbstractPedagogicalRequest(LearnerStateTransition transitionFired,
            AbstractStrategy activity) {
        if (transitionFired == null) {
            throw new IllegalArgumentException("The parameter 'transitionFired' cannot be null.");
        }
        
        AbstractPedagogicalRequest request = null;

        final String strategyName = activity.getName();
        if (activity instanceof InstructionalInterventionStrategy) {
            request = new RequestInstructionalIntervention(strategyName);
        } else if (activity instanceof MidLessonMediaStrategy) {
            request = new RequestMidLessonMedia(strategyName);
        } else if (activity instanceof ScenarioAdaptationStrategy) {
            request = new RequestScenarioAdaptation(strategyName);
        } else if (activity instanceof PerformanceAssessmentStrategy) {
            request = new RequestPerformanceAssessment(strategyName);
        } else if (activity instanceof DoNothingStrategy) {
            request = new RequestDoNothingTactic(strategyName);
        } else {
            return null;
        }
        
        Set<Integer> nodeIds = new HashSet<>();
        for(AbstractTransition aTransition : transitionFired.getTransitions()) {
            if(aTransition instanceof PerformanceNodeTransition) {
                PerformanceNodeTransition perfNodeTrans = (PerformanceNodeTransition)aTransition;
                int nodeId = perfNodeTrans.getNodeId();
                nodeIds.add(nodeId);
            }
        }
        
        request.setTaskConceptsAppliedToo(nodeIds);

        return request;
    }

    @Override
    public void getPedagogicalActions(LearnerState state, PedagogicalRequest request) {

        handlePerformanceAssessmentState(state.getPerformance(), request);

        if(logger.isDebugEnabled()){
            logger.debug("After analyzing performance assessment, there are "+request.getRequests().size()+" ped requests in the list");
        }

        handleCognitiveState(state.getCognitive(), request);

        if(logger.isDebugEnabled()){
            logger.debug("After analyzing cognitive state, there are "+request.getRequests().size()+" ped requests in the list");
        }

        handleAffectiveState(state.getAffective(), request);

        if(logger.isDebugEnabled()){
            logger.debug("After analyzing affective state, there are "+request.getRequests().size()+" ped requests in the list");
        }

        if(microActionKnowledge != null){
            //gather micro actions lists (first)
            populateActions(state, microActionKnowledge.getTransitions(), request, false);
        }

        if(macroActionKnowledge != null){
            //gather macro actions lists (second)
            populateActions(state, macroActionKnowledge.getTransitions(), request, true);
        }

        //update previous state for next incoming state
        prevLearnerState = state;

    }

    @Override
    public void initialize(InitializeDomainSessionRequest initDomainSessionRequest) {

        //nothing to do right now...
    }

    @Override
    public void initialize(InitializePedagogicalModelRequest initRequest) throws DetailedException {

        if(logger.isInfoEnabled()){
            logger.info("Initializing "+this);
        }

        if(initRequest.getActions() == null){
            //nothing to do if there is no action information
            return;
        }

        if(initRequest.isCourseActions()){
            //read course "actions"

            try{
                generated.course.Actions actions = courseHandler.getActions(initRequest.getActions());
                macroActionKnowledge = new CourseActionKnowledge(actions);

            }catch(Exception e){
                throw new DetailedException("Unable to initialize default ped model.",
                        "Failed to initialize the pedagogical actions for the course because "+e.getMessage(),
                        e);
            }

        }else{
            //read DKF "actions"

            try{
                generated.dkf.Actions actions = dkfActionsHandler.getActions(initRequest.getActions());
                microActionKnowledge = new DomainActionKnowledge(actions, null);

            }catch(Exception e){
                throw new DetailedException("Unable to initialize default ped model",
                        "Failed to initialize the default pedagogical model because "+e.getMessage(),
                        e);
            }
        }

    }

    @Override
    public PedagogicalRequest handleCourseStateUpdate(CourseState state) {
        // nothing to do right now...
        return null;
    }
    
    @Override
    public void handleLessonStarted() {
        
        // need to clear out the previous learner state when starting a DKF in order for
        // previous state and timestamps to be null
        // E.g. in #5267 Knowledge on a course concept is in an authored state transition (prev = null, curr = Novice)
        // and that knowledge is set prior to the DKF (e.g. LRS or scored survey).  If previous learner state time was not null
        // then the AbstractTransition.shouldTransition logic wouldn't work.
        prevLearnerState = null;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[DefaultPedagogicalModel: ");
        sb.append("previousLearnerState = ").append(prevLearnerState);
        sb.append("]");
        return sb.toString();
    }
}
