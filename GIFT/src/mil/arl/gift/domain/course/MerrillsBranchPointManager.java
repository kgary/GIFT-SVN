/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.course;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.MerrillsBranchPoint;
import mil.arl.gift.common.AbstractAfterActionReviewEvent;
import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.AfterActionReviewRemediationEvent;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AbstractRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo.AdvancementConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.util.CollectionUtils;

/**
 * This manager is responsible for managing all Merrill's branch point course transitions 
 * in a single course instance for a single learner.
 * 
 * @author mhoffman
 *
 */
public class MerrillsBranchPointManager {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MerrillsBranchPointManager.class);
    
    /** map of branch point course transition to the handler for that branch point */
    private Map<generated.course.MerrillsBranchPoint, MerrillsBranchPointHandler> branchPtToHandler = new HashMap<>();
    
    /** 
     * map of course object to the handler associated with it 
     * This is used for handling events related to transitions introduced as part of a merrills branch point course transition
     * managed by this manager instance (e.g. assessing survey results for a recall quadrant course transition) 
     */
    private Map<Serializable, MerrillsBranchPointHandler> newTransitionToHandler = new HashMap<>();
    
    /** contains the latest knowldege advancement information for concepts provided by pedagogical request(s) */
    private Set<AdvancementConcept> knowledgeAdvancementConcepts = new HashSet<>();
    
    /** contains the latest skill advancement information for concepts provided by pedagogical request(s) */
    private Set<AdvancementConcept> skillAdvancementConcepts = new HashSet<>();
    
    /** list to keep track of the original authored order of branch points */
    private List<MerrillsBranchPoint> bpTransitionOrder = new ArrayList<>();
    
    /** the Domain descendant directory the course is executed from and where the course XML file is in */
    private DesktopFolderProxy runtimeCourseDirectory;
    
    /** the directory where the authored course resides, useful for updating persistent files like paradata*/
    private AbstractFolderProxy authoredCourseDirectory;
    
    /** the current dynamic content handler being used for a course object that wants to manage remediation, can be null. */
    private DynamicContentHandler currentHandler = null;
    
    /**
     * Default constructor
     * 
     * @param runtimeCourseDirectory the Domain/runtime descendant directory the course is executed from and where the course XML file is in 
     * @param authoredCourseDirectory the directory where the authored course resides, useful for updating persistent files like paradata
     */
    public MerrillsBranchPointManager(DesktopFolderProxy runtimeCourseDirectory, AbstractFolderProxy authoredCourseDirectory) {

        if(runtimeCourseDirectory == null){
            throw new IllegalArgumentException("The runtime course directory can't be null.");
        }else if(authoredCourseDirectory == null){
            throw new IllegalArgumentException("The authored course directory can't be null.");
        }
        
        this.runtimeCourseDirectory = runtimeCourseDirectory;
        this.authoredCourseDirectory = authoredCourseDirectory;
    }
    
    /**
     * Return the handler instance for the branch point course transition.
     * 
     * @param transition - the transition to get the handler for.
     * @return MerrillsBranchPointHandler - the handler for the transition
     * @throws IOException - if there was a problem retrieving a file reference using the authored course folder
     */
    public MerrillsBranchPointHandler getHandler(generated.course.MerrillsBranchPoint transition) throws IOException{
        
        MerrillsBranchPointHandler handler = branchPtToHandler.get(transition);
        if(handler == null){
            handler = new MerrillsBranchPointHandler(transition, knowledgeAdvancementConcepts, skillAdvancementConcepts, runtimeCourseDirectory, authoredCourseDirectory);
            
            branchPtToHandler.put(transition, handler);
            bpTransitionOrder.add(transition);
        }
        
        return handler;
    }
    
    /**
     * Notification that a new course object is about to be presented to the learner.
     * This is used to keep track of the duration the content was presented to the learner.
     * 
     * @param previousCourseObject the previous course object that was presented to the learner
     * @param nextCourseObject the next course object that will be presented to the learner.
     */
    public void notifyNextCourseObject(CourseObjectWrapper previousCourseObject, CourseObjectWrapper nextCourseObject){
        
        if(currentHandler != null){
            
            if(previousCourseObject != null && previousCourseObject.getCourseObject() instanceof MerrillsBranchPointHandler.AbstractExpandedCourseObject){
                
                // remove the adaptive courseflow expanded course object container
                CourseObjectWrapper newCourseObjectWrapper = 
                        new CourseObjectWrapper(((MerrillsBranchPointHandler.AbstractExpandedCourseObject)previousCourseObject.getCourseObject()).getCourseObject(), 
                                previousCourseObject.getCourseObjectReference());
                previousCourseObject = newCourseObjectWrapper;
            }
            
            if(nextCourseObject != null && nextCourseObject.getCourseObject() instanceof MerrillsBranchPointHandler.AbstractExpandedCourseObject){
                
                // remove the adaptive courseflow expanded course object container
                CourseObjectWrapper newCourseObjectWrapper = 
                        new CourseObjectWrapper(((MerrillsBranchPointHandler.AbstractExpandedCourseObject)nextCourseObject.getCourseObject()).getCourseObject(), 
                                nextCourseObject.getCourseObjectReference());
                nextCourseObject = newCourseObjectWrapper;
            }
            
            currentHandler.notifyNextCourseObject(previousCourseObject, nextCourseObject);
        }
    }
    
    /**
     * Notification that an assessment event happened.
     * This is used to keep track of the assessment results after presenting content.
     */
    public void notifyAssessmentEvent(boolean passed){
        
        if(currentHandler != null){
            currentHandler.notifyAssessmentEvent(passed);
        }
    }
    
    /**
     * Return the handler associated with the course transition (e.g. a generated.course object).
     * 
     * @param newTransition the course transition to get the handler for
     * @return MerrillsBranchPointHandler contains information about this transition and the merrills branch point it came from
     */
    public MerrillsBranchPointHandler getHandler(Serializable newTransition){        
        return newTransitionToHandler.get(newTransition);
    }
    
    /**
     * Register the course transition (i.e. a generated.course object) with the provided handler so it
     * can be retrieved at a latter time.
     * 
     * @param newTransition the course transition to register the handler with
     * @param handler contains information about this transition and the merrills branch point it came from
     */
    public void registerHandler(Serializable newTransition, MerrillsBranchPointHandler handler){
        
//        if(newTransitionToHandler.containsKey(newTransition) && newTransitionToHandler.get(newTransition) != handler){
//            
//            if(newTransition instanceof QuadrantTransitionInfoWrapper)
//            //ERROR - there should only be a single handler for the transitions it creates
//            throw new IllegalArgumentException("There is already a mapping between the transition of "+newTransition+" and the handler "+handler+".");
//        }
        
        newTransitionToHandler.put(newTransition, handler);
    }  
    
    /**
     * This instance will no longer be used.  Release references to objects that were created
     * outside of this class, used by inner classes and are inner classes. 
     */
    public void cleanup(){
        bpTransitionOrder.clear();
        branchPtToHandler.clear();
        newTransitionToHandler.clear();
        skillAdvancementConcepts.clear();
        knowledgeAdvancementConcepts.clear();
    }
    
    /**
     * Update the recall survey request instance by adding adding question priority
     * information based on the question's that were previously answered correct versus
     * incorrect.  The goal of which to prefer the questions that were answered incorrect
     * so the learner has a chance to answer them correctly in the next recall survey.
     * 
     * @param recallTransition contains transition information for the recall quadrant and is use to retrieve the appropriate
     * handler for. 
     * @param request the get survey request to update
     */
    public void prioritizeRecallSurveyRequest(MerrillsBranchPointHandler.RecallCourseObject recallTransition, GetKnowledgeAssessmentSurveyRequest request){
        
        MerrillsBranchPointHandler handler = getHandler(recallTransition);
        handler.prioritizeRecallSurveyRequest(request);
    }
    
    /**
     * Set the current dynamic content handler being used for a course object that wants to manage remediation.
     * @param currentHandler can be null
     */
    public void setCurrentHandler(DynamicContentHandler currentHandler){
        this.currentHandler = currentHandler;        
    }
    
    /**
     * Return the current dynamic content handler being used for a course object that wants to manage remediation.
     * @return can be null
     */
    public DynamicContentHandler getCurrentHandler(){
        return this.currentHandler;
    }
    
    /**
     * Process the pedagogical request to determine what course elements are needed next in terms of Merrill's branch point course elements.
     * First the current branch point handler will be called upon and if doesn't add any new course elements to execute the other handlers will
     * be checked.  The other handlers will only be checked if remediation is needed because that is the only case where jumping directly to a different
     * handler is appropriate (e.g. remediation after Practice will cause Example content for other branch points to be presented).
     * 
     * @param currentHandler the current branch point handler being used in the current part of the course.
     * @param request the incoming pedagogical request that should contain information on whether remediation or progression is appropriate.
     * @param courseEvents the current course events known for this user that can be used in an After Action Review course element.
     * @return new course transitions for the domain module to handle.  The list can be empty to indicate
     * this branch point has finished with the learner comprehending the concepts it taught.
     * Note: null will be returned in the case where the course can no longer continue because the learner
     * has failed to comprehend the concepts in this branch point in a timely manner. 
     * @throws Exception if there was a problem handling the pedagogical request to build new course elements
     */
    public List<CourseObjectWrapper> buildTransitions(MerrillsBranchPointHandler currentHandler, PedagogicalRequest request, Map<Date, List<AbstractAfterActionReviewEvent>> courseEvents) throws Exception{

        //see if the current branch point handler will handle the request
        List<CourseObjectWrapper> expandedTransitions = currentHandler.handleRequest(request, courseEvents);
        
        if(expandedTransitions != null && expandedTransitions.isEmpty()){
            //maybe the request needs to be handled across other adaptive courseflow course objects (because the current one didn't add anything)
            //e.g. remediation after practice to a branch point's example quadrant
            if(logger.isInfoEnabled()){
                logger.info("The current branch point handler didn't add any other course elements based on the pedagogical request.  Attempting to analyze the pedagogical request across all branch points.");
            }
            
            // Analyze the request for remediationinfo that only contains Example, the strategy quadrant is Example.  
            // Other info:  the metadata attributes will be empty            
            for(List<AbstractPedagogicalRequest> requestList : request.getRequests().values()){
                for(AbstractPedagogicalRequest aRequest : requestList){
                
                    if(aRequest instanceof RequestBranchAdaptation){
                        
                        BranchAdaptationStrategy strategy = ((RequestBranchAdaptation)aRequest).getStrategy();
                        if(strategy.getStrategyType() instanceof RemediationInfo){
                            
                            RemediationInfo remediation = (RemediationInfo)strategy.getStrategyType();
                            if(remediation.isAfterPractice()){
                                //only look across adaptive courseflow handlers when dealing with after practice remediation
                                
                                Map<String, List<AbstractRemediationConcept>> remediationMap = remediation.getRemediationMap();
                                if(remediationMap != null && !remediationMap.isEmpty()){                       
                                    
                                    if(logger.isInfoEnabled()){
                                        logger.info("Found a pedagogical request strategy of "+strategy+" that references the Example quadrant as well as provides remediation information." +
                                                " Since the current branch point handler wasn't able to process this request, attempting to apply the request to" +
                                                " any handler that references one or more concepts needing remediation.");
                                    }
                                    
                                    //the ordering of handlers should match the authored course flow
                                    List<MerrillsBranchPointHandler> remediationHandlers = new ArrayList<>();
                                    Map<String, String> practiceConceptToCourseConcept = null;
                                    
                                    //First - see if there is a practice concept to course concept mapping
                                    //looking for the first branch point (most likely the last in the ordered list) that has the practice
                                    //quadrant - then retrieve the DKF concept to course concept mapping for translating later on...
                                    for(int index = bpTransitionOrder.size()-1; index >= 0; index--){
                                        
                                        Serializable branchPointTransition = bpTransitionOrder.get(index);
                                        MerrillsBranchPointHandler handler = branchPtToHandler.get(branchPointTransition);
                                        
                                        if(handler.getPracticeConceptsRelationship() != null && !handler.getPracticeConceptsRelationship().isEmpty()){                                        
                                            practiceConceptToCourseConcept = handler.getPracticeConceptsRelationship();
                                            if(logger.isDebugEnabled()){
                                                logger.debug("Found a practice concept relationship map of "+practiceConceptToCourseConcept+".");
                                            }
                                            break;
                                        }
    
                                    } 
                                    
                                    // found no adaptive courseflow course objects that could service a request to practice the concepts requested
                                    // This normally happens when a training app course object comes before an adaptive courseflow course object that has no practice
                                    // and both assess some common concept(s). This is not an issue but an indication that the author most likely choose not
                                    // to provide enough opportunities to re-assess the concept's skill level (e.g. choose 'do not repeat' option in the training app course object)
                                    if(CollectionUtils.isEmpty(practiceConceptToCourseConcept)){
                                        break;
                                    }
                                    
                                    //Create the set of concepts that are covered
                                    @SuppressWarnings("null")
                                    Set<String> conceptsLeftToCover = new HashSet<>(practiceConceptToCourseConcept.values());
                                    
                                    //Second - find the handlers that deal with the concepts needing remediation (using the
                                    //         concept mapping if needed)
                                    //         Note: use the most recent handlers that teach a concept rather than use the older ones
                                    //               currently there is no reason for this but in the future there maybe some attributes
                                    //               in the handler to leverage.
                                    for(int i = bpTransitionOrder.size() - 1; i >= 0; i--) {
                                        MerrillsBranchPoint branchPointTransition = bpTransitionOrder.get(i);
                                        MerrillsBranchPointHandler handler = branchPtToHandler.get(branchPointTransition);
                                        if(handler.hasBranchPointConcept(remediation, false, practiceConceptToCourseConcept)){
                                            
                                            if(!remediationHandlers.contains(handler)){
                                                remediationHandlers.add(handler);
                                                
                                                //Remove all the concepts that the transition covers
                                                conceptsLeftToCover.removeAll(branchPointTransition.getConcepts().getConcept());
                                                
                                                //If there aren't any concepts left to cover, stop adding handlers
                                                //this prevents multiple handlers that teach the same concept to remediation on that concept
                                                if(conceptsLeftToCover.isEmpty()) {
                                                    break;
                                                }
                                            }
                                        }                                     
                                    }
                                    
                                    if(logger.isInfoEnabled()){
                                        logger.info("Found "+remediationHandlers+" handlers that should service this pedagogical request strategy.");
                                    }
                                    
                                    //each handler needs to add an Example phase
                                    List<RemediationInfo> strategyList = new ArrayList<>();
                                    strategyList.add((RemediationInfo) strategy.getStrategyType());
                                    
                                    Map<Date, List<AbstractAfterActionReviewEvent>> addCourseEvents = new HashMap<>();
                                    
                                    for(MerrillsBranchPointHandler handler : remediationHandlers){
                                        List<CourseObjectWrapper> newTransitions = handler.handleRemediationAfterPractice(strategyList, practiceConceptToCourseConcept, addCourseEvents);
                                        
                                        if(newTransitions == null){
                                            //problem
                                            logger.error("The branch point handler of "+handler+" didn't add any remediation course elements even though it is responsible for concept(s) that need remediation at this point.");
                                        }else{
                                            if(logger.isInfoEnabled()){
                                                logger.info("Adding "+newTransitions.size()+" new transition(s) for after practice remediation from "+handler+".");
                                            }
                                            expandedTransitions.addAll(newTransitions);
                                        }
                                    }
                                    
                                    if(!addCourseEvents.isEmpty()){
                                        //build a new AAR transition with a remediation event
                                        
                                        // combine remediation events so that all concepts that need remediation across the remediationHandlers collection
                                        // will appear as one remediation event in the AAR.  After all it was a single practice that caused the remediation.
                                        Iterator<Date> eventDateItr = addCourseEvents.keySet().iterator();
                                        AfterActionReviewRemediationEvent aggregatedRemediationConceptsEvent = null;
                                        while(eventDateItr.hasNext()){
                                            
                                            Date eventDate = eventDateItr.next();
                                            List<AbstractAfterActionReviewEvent> eventDateEvents = addCourseEvents.get(eventDate);
                                            if(eventDateEvents != null && !eventDateEvents.isEmpty()){
                                                
                                                Iterator<AbstractAfterActionReviewEvent> eventItr = eventDateEvents.iterator();
                                                while(eventItr.hasNext()){
                                                    
                                                    AbstractAfterActionReviewEvent event = eventItr.next();
                                                    
                                                    if(event instanceof AfterActionReviewRemediationEvent){
                                                        //found an after practice handler AAR remediation event that should contain
                                                        //one or more concepts that are taught knowledge in that handler
                                                        
                                                        if(aggregatedRemediationConceptsEvent == null){
                                                            aggregatedRemediationConceptsEvent = (AfterActionReviewRemediationEvent) event;
                                                        }else{
                                                            //move this event into the aggregated event that was already capture
                                                            //Note: don't break the while loop because there might be more than just this event in 
                                                            //      the list (in the future)
                                                            
                                                            AfterActionReviewRemediationEvent eventToMove = (AfterActionReviewRemediationEvent) event;
                                                            aggregatedRemediationConceptsEvent.addRemediationInfo(eventToMove.getRemediationInfo());
                                                            
                                                            //remove it from the original list because this event's content was copied to the aggregate event
                                                            eventItr.remove();
                                                        }
                                                    }
                                                }//end while
                                                
                                                if(eventDateEvents.isEmpty()){
                                                    //remove event collection from map, all events were merged into other events in the map
                                                    eventDateItr.remove();
                                                }
                                            }
                                        }//end while
                                        
                                        courseEvents.putAll(addCourseEvents);
                                        
                                        if(logger.isInfoEnabled()){
                                            logger.info("Creating an AAR course element because the branch point handler(s) added "+addCourseEvents.size()+" AAR event(s).");
                                        }
                                        generated.course.AAR aar = MerrillsBranchPointHandler.buildRemediationAARTransition(DynamicContentHandler.AFTER_PRACTICE_REMEDIATION_HEADING);
                                        
                                        //add the AAR as the first transition to visit next
                                        expandedTransitions.add(0, CourseObjectWrapper.generateCourseObjectWrapper(aar, authoredCourseDirectory));
                                    }
                                }
                            }
                        }
                    }//end for                            
                }//end for
            }
        }
        
        return expandedTransitions;
    }
    
    /**
     * A pedagogical request was received and need to be handled by the domain knowledge.
     * For this class it means determining if the request contains 'advancement' information (expertise assessment/state) that can
     * affect the execution of upcoming branch points (i.e. skip a phase such as rule/example/recall or practice).
     * The result of this method can be an update to {@link #knowledgeAdvancementConcepts} or {@link #skillAdvancementConcepts} or
     * nothing at all.
     * 
     * @param request - the request to handle which may or may not contain advancement information for one or more course concepts.
     * Can't be null.
     */
    public void handlePedagogicalRequest(PedagogicalRequest request){
        
        if(logger.isInfoEnabled()){
            logger.info("Branch point manager received a pedagogical request of "+request+".");
        }
        
        // flags used to determine if the corresponding concept list has been reset to be repopulated
        // by the latest pedagogical request information
        boolean haveResetSkillConcepts = false, haveResetKnowledgeConcepts = false;
        
        for(List<AbstractPedagogicalRequest> requestList : request.getRequests().values()){
            for(AbstractPedagogicalRequest aRequest : requestList){
            
                if(aRequest instanceof RequestBranchAdaptation){
                    //found a branch adaptation request
                    
                    RequestBranchAdaptation branchAdaptation = (RequestBranchAdaptation)aRequest;
                    BranchAdaptationStrategy strategy = branchAdaptation.getStrategy();
                    if(strategy.getStrategyType() instanceof AdvancementInfo){
                        //found advancement information in the request
                        if(logger.isInfoEnabled()){
                            logger.info("The request contains a branch adaptation request with advancement information.");
                        }
                        
                        AdvancementInfo advancementInfo = (AdvancementInfo) strategy.getStrategyType();
                        List<AdvancementConcept> newConcepts = advancementInfo.getConcepts();
                        
                        Set<AdvancementConcept> conceptListToManipulate;
                        if(advancementInfo.isSkill()){
                            conceptListToManipulate = skillAdvancementConcepts;
                            if(!haveResetSkillConcepts){
                                // clear the skill concept list to be re-populated next
                                conceptListToManipulate.clear();
                                haveResetSkillConcepts = true;
                            }
                        }else{
                            conceptListToManipulate = knowledgeAdvancementConcepts;
                            if(!haveResetKnowledgeConcepts){
                                // clear the skill concept list to be re-populated next
                                conceptListToManipulate.clear();
                                haveResetKnowledgeConcepts = true;
                            }
                        }
                        
                        conceptListToManipulate.addAll(newConcepts);
                    }
                }
                
            }//end for
        }//end for
    }
}
