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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.course.BooleanEnum;
import mil.arl.gift.common.AbstractAfterActionReviewEvent;
import mil.arl.gift.common.AbstractPedagogicalRequest;
import mil.arl.gift.common.AfterActionReviewRemediationEvent;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest;
import mil.arl.gift.common.GetKnowledgeAssessmentSurveyRequest.ConceptParameters;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.RequestBranchAdaptation;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AbstractRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.AdvancementInfo.AdvancementConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.BranchAdpatationStrategyTypeInterface;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.MetadataAttributeItem;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.PassiveRemediationConcept;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.ProgressionInfo;
import mil.arl.gift.common.course.strategy.BranchAdaptationStrategy.RemediationInfo;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.domain.course.CourseObjectWrapper.CourseObjectNameRef;

/**
 * This class has the logic to handle the details of a Merrill Branch Point course transition.
 * 
 * @author mhoffman
 *
 */
public class MerrillsBranchPointHandler extends DynamicContentHandler implements TransitionHandler {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MerrillsBranchPointHandler.class);
    
    /**
     * The number of successive remediation needed from the same recall quadrant in this
     * branch point.  This is used as an indicator that the learner is not comprehending
     * the content being taught and may need additional support or try re-taking the course.
     */
    public static int DEFAULT_RECALL_BAILOUT_CNT = 3;
    
    /**
     * the number of allowed recall attempts before prematurely but gracefully ending the course
     */
    private int allowedRecallAttempts = NO_DEFAULT_SET;
    
    /** heading for the structured review page that is shown after a recall is taken and the learner needs remediation */
    public static final String AFTER_RECALL_REMEDIATION_HEADING = "Review concepts needing remediation";
    
    /** heading for the structured review page that is shown after a recall is taken and the learner does NOT need remediation */
    public static final String AFTER_RECALL_PASSED_HEADING = "You passed the Check on Learning";
    
    /** course element containing information about the branch point */
    private generated.course.MerrillsBranchPoint branch;
    
    /** contains all of the initial course objects of an authored adaptive courseflow course object */
    private List<CourseObjectWrapper> expandedObjects = new ArrayList<>();
    
    /** the next course object that should be used in the course flow */
    private CourseObjectWrapper nextCourseObject = null;
    
    /**
     * This collections contain the question ids for questions that were either answered
     * correctly or incorrectly the last time that question was asked in a recall survey 
     * for this branch point transition.  
     * A question id will not be in both lists because you can't get a question correct and incorrect
     * the last time you answered that question.
     */
    private Set<Integer> questionsCorrectLastTimeAnswered = new HashSet<>();
    private Set<Integer> questionsIncorrectLastTimeAnswered = new HashSet<>();
    
    /**
     * The current count of successive executions of this branch point's recall quadrant.
     * If the learner moves to the next transition past the recall or practice quadrant, the relative counter
     * is reset.  In that scenario the counter can be used again if the recall or practice quadrant is
     * reached later on as a result of remediation somewhere further on in the course execution.
     */
    private int successiveRecallCount;
    
    /** contains the latest knowledge advancement information for concepts provided by pedagogical request(s) */
    private Set<AdvancementConcept> knowledgeAdvancementConcepts;
    
    /** contains the latest skill advancement information for concepts provided by pedagogical request(s) */
    private Set<AdvancementConcept> skillAdvancementConcepts;
    
    /** flag used to indicate whether the learner has shown knowledge based comprehension of all concepts taught in this course object */
    private boolean knowledgeAdvancementEnabled = false;
    
    /** flag used to indicate if some logic, i.e. user selected to take the knowledge phases again, is over-riding the pedagogical model (ICAP) of
     * being able to advance past the knowledge phases.  True means the logic has been over-ridden and skipping knowledge phases should not happen. */
    private boolean knowledgeAdvancementIgnored = false;
    
    /** flag used to indicate whether the learner has show skill based comprehension of all concepts taught in this course object */
    private boolean skillAdvancementEnabled = false;
    
    /** this contains the next quadrant type to execute */
    private MerrillQuadrantEnum nextQuadrant = null;
    
    /**
     * whether this handler has been initialized
     * true if the {@link #initialize(boolean, boolean)} method has been called at least once.
     */
    private boolean initialized = false;
    
    /**
     * The current course of action for this branch point
     */
    private COA currentCOA = null;    
    
    /**
     * Enumerated courses of action (COA) that are based on pedagogical requests during an adaptive courseflow course object.<br/>
     * The order matters.  Enum entries with lower ordinal values have higher precedence when it comes to selecting
     * the most important COA.  E.g. Progression can't override Remediation-After-Practice.
     */
    enum COA{
        REMEDIATION_AFTER_PRACTICE,
        REMEDIATION_AFTER_RECALL,
        ADVANCEMENT,
        PROGRESSION,
        FINISHED
    }

    /**
     * Class constructor - set attribute
     * 
     * @param branch - course element containing information about the branch point
     * @param knowledgeAdvancementConcepts - contains information about concepts that the learner has proven to have expert knowledge
     *                              Can be null or empty.
     * @param skillAdvancementConcepts - contains information about concepts that the learner has proven to have expert skill
     *                              Can be null or empty.
     * @param runtimeCourseDirectory the Domain descendant directory the course is executed from and that the course XML file is in 
     * @param authoredCourseDirectory the directory where the authored course resides, useful for updating persistent files like paradata
     * @throws IOException - if there was a problem retrieving a file reference using the authored course folder
     */
    public MerrillsBranchPointHandler(generated.course.MerrillsBranchPoint branch, Set<AdvancementConcept> knowledgeAdvancementConcepts, 
            Set<AdvancementConcept> skillAdvancementConcepts, DesktopFolderProxy runtimeCourseDirectory, AbstractFolderProxy authoredCourseDirectory) throws IOException{
        super(branch.getTransitionName(), runtimeCourseDirectory, authoredCourseDirectory , null);
        
        this.branch = branch;
        setKnowledgeAdvancementInfo(knowledgeAdvancementConcepts);
        setSkillAdvancementInfo(skillAdvancementConcepts);
        getPracticeConceptsRelationship();
        
        //
        // set allowed attempts value (optional)
        //
        generated.course.Recall recall = getRecallQuadrant();
        if(recall.getAllowedAttempts() != null){
            allowedRecallAttempts = recall.getAllowedAttempts().intValue();
        }
        
        generated.course.Practice practice = getPracticeQuadrant();
        if(practice != null && practice.getAllowedAttempts() != null){
            setAllowedPracticeAttempts(practice.getAllowedAttempts().intValue());
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Setting allowed remediation attempts for "+branch.getTransitionName()+" : recall=" + allowedRecallAttempts + ", practice=" + allowedPracticeAttempts);
        }
    }
    
    /**
     * Initialize this adaptive courseflow handler by expanding the phases to execute (e.g. Rule) into course
     * objects.  If already called before this method will do nothing.
     * @param learnerWantsSkipKnowledge whether to allow skipping of the knowledge phases of this adaptive courseflow (e.g. Rule).  True
     * only allows for skipping to happen but it can only happen if the learner has expert knowledge on all the concepts covered
     * in this adaptive courseflow.
     * @throws IOException if there was a problem retrieving a file reference using the authored course folder
     */
    public void initialize(boolean learnerWantsSkipKnowledge) throws IOException{
        
        if(initialized){
            return;
        }
        
        // knowledge skipping will happen if the learner has the knowledge and wants to take the knowledge phases (i.e. not skip)
        this.knowledgeAdvancementIgnored = willKnowledgeBeSkipped() && !learnerWantsSkipKnowledge;
        
        expandTransitions(learnerWantsSkipKnowledge);
        initialized = true;
    }
    
    /**
     * Whether this handler has been initialized
     * @return true if the {@link #initialize(boolean, boolean)} method has been called at least once.
     */
    public boolean isInitialized(){
        return initialized;
    }
    
    /**
     * Return teh flag used to indicate if some logic, i.e. user selected to take the knowledge phases again, is over-riding the pedagogical model (ICAP) of
     * being able to advance past the knowledge phases. 
     * @return  True means the logic has been over-ridden and skipping knowledge phases should not happen.
     */
    public boolean knowledgeAdvancementIgnored(){
        return knowledgeAdvancementIgnored;
    }
    
    /**
     * Whether this adaptive courseflow has started by setting that the next course of action is 
     * to enter one of the phases (e.g. Rule)
     * @return true if the next course of action is set.
     */
    public boolean hasEntered(){
        return currentCOA != null;
    }
    
    /**
     * Retrieve the generated Quadrant information authored for this branch point course element.
     * 
     * @param generatedQuadrantType the generated class to search for (e.g. generated.course.Rule.class)
     * @return the instance found.  Can be null if the quadrant requested wasn't found to be authored in this branch point.
     */
    @SuppressWarnings("unchecked")
    private <E> E getQuadrant(Class<E> generatedQuadrantType){
        
        Serializable elementFound = null;
        for(Serializable element : branch.getQuadrants().getContent()){
            
            if(generatedQuadrantType.isAssignableFrom(generated.course.Rule.class) && element instanceof generated.course.Rule){
                elementFound = element;
                break;
            }else if(generatedQuadrantType.isAssignableFrom(generated.course.Example.class) && element instanceof generated.course.Example){
                elementFound = element;
                break;
            }else if(generatedQuadrantType.isAssignableFrom(generated.course.Recall.class) && element instanceof generated.course.Recall){
                elementFound = element;
                break;
            }else if(generatedQuadrantType.isAssignableFrom(generated.course.Practice.class) && element instanceof generated.course.Practice){
                elementFound = element;
                break;
            }
            
        }
        
        return (E) elementFound;
    }
    
    /**
     * Return the recall quadrant information.
     * 
     * @return contains information about the types of questions that should be requested
     * from the question bank and the rules to determine whether the learner's knowledge
     * level on the concepts being taught.
     */
    private generated.course.Recall getRecallQuadrant(){
        return getQuadrant(generated.course.Recall.class);
    }
    
    /**
     * Return the practice quadrant information.
     * 
     * @return contains information about the concepts being practice, will be null if not 
     * practicing in an adaptive courseflow course object.  Will be null until {@link #setPracticeQuadrant(generated.course.Practice)}
     * is called. 
     */
    @Override
    protected generated.course.Practice getPracticeQuadrant(){
        return getQuadrant(generated.course.Practice.class);
    }
    
    @Override
    protected Collection<String> getConceptList(){
        return branch.getConcepts().getConcept();
    }
    
    /**
     * Return the enumerated next Merrill Quadrant in the current course flow.
     * 
     * @return MerrillQuadrantEnum the next merrill's quadrant needing to be executed in the course.  Null if the end of this merrill's
     *          branch point has been reached.
     * @throws CourseComprehensionException if the learner failed to pass the Recall test or the Practice scenario given a pre-defined maximum number
     * of attempts for each.
     */
    public MerrillQuadrantEnum getNextQuadrant() throws CourseComprehensionException{
        
        if(nextQuadrant != null){
            previousQuadrant = nextQuadrant;
        }
        nextQuadrant = null;
        
        if(nextCourseObject != null){
            if(nextCourseObject.getCourseObject() instanceof RuleCourseObject){
                nextQuadrant = MerrillQuadrantEnum.RULE;
            }else if(nextCourseObject.getCourseObject() instanceof ExampleCourseObject){
                nextQuadrant = MerrillQuadrantEnum.EXAMPLE;
            }else if(nextCourseObject.getCourseObject() instanceof RecallCourseObject){
                nextQuadrant = MerrillQuadrantEnum.RECALL;
            }else if(nextCourseObject.getCourseObject() instanceof PracticeCourseObject){
                nextQuadrant = MerrillQuadrantEnum.PRACTICE;
            }else if(nextCourseObject.getCourseObject() instanceof RemediationCourseObject){
                nextQuadrant = MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL;
            }
        }
        
        if(hasPracticeAttemptLimit() && successivePracticeCount >= allowedPracticeAttempts){
            //the learner is not increasing their comprehension of this branch points concepts at a pace the course author
            //wants, therefore bounce the learner out of the course (BAIL OUT!!!)
            
            if(logger.isInfoEnabled()){
                logger.info("The learner has failed to proceed past the practice quadrant after "+successivePracticeCount+
                        " attempts for the current branch point.  Therefore GIFT is gracefully stopping the exeuction of this course.");
            }
            throw new CourseComprehensionException("The learner has failed to proceed past the practice quadrant after "+successivePracticeCount+
                    " attempts for the current branch point.");
        }
        
        return nextQuadrant;
    }
    
    /**
     * Returns true if there is a recall attempt limit that has been configured for the course.
     * 
     * @return True if there is a recall attempt limit that has been configured for the course.
     */
    private boolean hasRecallAttemptLimit() {
        return allowedRecallAttempts > 0;
    }
    
    /**
     * Return any transitions (e.g. Guidance) after the Recall quadrant and before either the Practice
     * quadrant or the end of the MBP course element since Practice is optional (i.e. this will not
     * return the optional Practice quadrant only the transitions between Recall and Practice).
     * 
     * @return can be empty but not null.
     */
    public List<CourseObjectWrapper> getAfterRecallInterQuadrantTransitions(){
        
        List<CourseObjectWrapper> interTransitions = new ArrayList<>(); 
        int currentIndex = expandedObjects.indexOf(nextCourseObject);
        int index = currentIndex;  //start at the next MBP course transition that needs to be displayed to the user
        for(; index >= 0 && index < expandedObjects.size(); index++){
            
            CourseObjectWrapper contentObj = expandedObjects.get(index);
            if(!(contentObj.getCourseObject() instanceof AbstractExpandedCourseObject) && !knowledgeAdvancementEnabled){
                
                interTransitions.add(contentObj);
                
            }else{
                //set the next quadrant course transition for use when calling getNextQuadrant()
                nextCourseObject = expandedObjects.get(index);
                break;
            }
        }
        
        return interTransitions;
    }
    
    /**
     * Build the mapping of this branch point's practice quadrant (if authored since it is optional)
     * concepts to the course concepts they represent.  This mapping is useful for when a pedagogical
     * request is received and it references DKF concepts while this class is looking to handle course
     * level concepts.  The relationship is defined in the branch point course element in the course file.
     * @return mapping of DKF concepts used in practice to course level concepts.  Can be empty but not null. 
     */
    public Map<String, String> getPracticeConceptsRelationship(){
        
        if(practiceConceptToCourseConcept == null || practiceConceptToCourseConcept.isEmpty()){
            //attempt to build it
            
            generated.course.Practice practice = getPracticeQuadrant();
            if(practice != null){
                
                //MH: ticket #1100 (look for other comments in this class)
//                for(ConceptPair cPair : practice.getPracticeConcepts().getConceptPair()){
//                    
//                    practiceConceptToCourseConcept.put(cPair.getDKFConcept(), cPair.getCourseConcept());
//                }
                for(String courseConcept : practice.getPracticeConcepts().getCourseConcept()){
                    practiceConceptToCourseConcept.put(courseConcept, courseConcept);
                }
            }

        }
        
        return practiceConceptToCourseConcept;
    }
    
    /**
     * Set the information about concepts that the learner has proven to have expert knowledge
     *                              
     * @param knowledgeAdvancementConcepts Can be null or empty.
     */
    private void setKnowledgeAdvancementInfo(Set<AdvancementConcept> knowledgeAdvancementConcepts) {
        this.knowledgeAdvancementConcepts = knowledgeAdvancementConcepts;
    }
    
    /**
     * Set the information about concepts that the learner has proven to have expert skill
     *                              
     * @param skillAdvancmentConcepts Can be null or empty.
     */
    private void setSkillAdvancementInfo(Set<AdvancementConcept> skillAdvancmentConcepts) {
        this.skillAdvancementConcepts = skillAdvancmentConcepts;
    }
    
    /**
     * Expand on the Merrill branch transitions authored in this part of the course.
     * This should only be called the first time this branch point class is constructed.
     * @param allowSkipKnowledge whether to allow skipping of the knowledge phases of this adaptive courseflow (e.g. Rule).  True
     * only allows for skipping to happen but it can only happen if the learner has expert knowledge on all the concepts covered
     * in this adaptive courseflow.
     * @throws IOException - if there was a problem retrieving a file reference using the authored course folder
     */
    private void expandTransitions(boolean allowSkipKnowledge) throws IOException{ 
        
        //determine if any quadrants (plus intermediate transitions) should be skipped based
        //on known advancement information thus far
        if(knowledgeAdvancementConcepts != null && allowSkipKnowledge){
                    
            knowledgeAdvancementEnabled = willKnowledgeBeSkipped();
            if(logger.isInfoEnabled()){
                logger.info("Based on the advancement information known thus far this merrills branch point course element will "+ (knowledgeAdvancementEnabled ? "" : "NOT") +" have portions skipped.");
            }
        }
        
        // NOTE: currently practice phase is not allowed to be skipped, we want experts to be able to practice
        boolean allowSkipPractice = false;
        
        // determine whether the practice phase of this adaptive courseflow should be skipped based on
        // the up-to-date advancement information provided by pedagogical request up to this point in the course.
        // i.e. if the learner has expert skill on the concepts covered in practice than skip practice
        if(skillAdvancementConcepts != null && getPracticeQuadrant() != null && allowSkipPractice) {
            int expertConceptCnt = 0;
            for(String practiceConcept : getPracticeQuadrant().getPracticeConcepts().getCourseConcept()) {
                for(AdvancementConcept advancementConcept : skillAdvancementConcepts) {
                    if(practiceConcept.equalsIgnoreCase(advancementConcept.getConcept())) {
                        expertConceptCnt++;
                    }
                }
            }
            
            skillAdvancementEnabled = expertConceptCnt == getPracticeQuadrant().getPracticeConcepts().getCourseConcept().size();
        }
      
        for(Object contentObj : branch.getQuadrants().getContent()){
            
            // Rule        = MerrillQuadrantEnum.RULE
            // Example     = MerrillQuadrantEnum.EXAMPLE
            // Recall      = generated.course.Recall.PresentSurvey (course transition from course.xml)
            //             (plus) Remediation
            // Practice    = MerrillQuadrantEnum.PRACTICE
            
            if(contentObj instanceof generated.course.Transitions && !knowledgeAdvancementEnabled){
                //optional course object(s) between phases
                
                generated.course.Transitions transitions = (generated.course.Transitions)contentObj;
                for(Serializable subCourseObject : transitions.getTransitionType()){
                    
                    CourseObjectWrapper wrapper = CourseObjectWrapper.generateCourseObjectWrapper(subCourseObject, authoredCourseDirectory);
                    expandedObjects.add(wrapper);
                }
                
            }else if(contentObj instanceof generated.course.Rule && !knowledgeAdvancementEnabled){
                
                //the course object is just the enumeration which is used for metadata matching later
                RuleCourseObject ruleCourseObject = new RuleCourseObject(branch.getTransitionName(), MerrillQuadrantEnum.RULE, branch.getConcepts().getConcept());
                expandedObjects.add(new CourseObjectWrapper(ruleCourseObject, new CourseObjectNameRef(branch.getTransitionName())));
                
            }else if(contentObj instanceof generated.course.Example && !knowledgeAdvancementEnabled){
                
                //the course object is just the enumeration which is used for metadata matching later
                ExampleCourseObject exampleCourseObject = new ExampleCourseObject(branch.getTransitionName(), MerrillQuadrantEnum.EXAMPLE, branch.getConcepts().getConcept());
                expandedObjects.add(new CourseObjectWrapper(exampleCourseObject, new CourseObjectNameRef(branch.getTransitionName())));
                
            }else if(contentObj instanceof generated.course.Recall && !knowledgeAdvancementEnabled){                                
                
                //the course object info is the Recall Survey transition
                RecallCourseObject recallCourseObject = new RecallCourseObject(branch.getTransitionName(), ((generated.course.Recall)contentObj).getPresentSurvey(), branch.getConcepts().getConcept());
                expandedObjects.add(new CourseObjectWrapper(recallCourseObject, new CourseObjectNameRef(branch.getTransitionName())));
                
                RemediationCourseObject remediationCourseObject = new RemediationCourseObject(branch.getTransitionName());
                expandedObjects.add(new CourseObjectWrapper(remediationCourseObject, new CourseObjectNameRef(branch.getTransitionName())));
                
            }else if(contentObj instanceof generated.course.Practice && !skillAdvancementEnabled) {
                
                generated.course.Practice practice = (generated.course.Practice)contentObj;
                
                //the transition info is just the enumeration which is used for metadata matching later on
                PracticeCourseObject practiceCourseObject = new PracticeCourseObject(branch.getTransitionName(), MerrillQuadrantEnum.PRACTICE, practice.getPracticeConcepts().getCourseConcept());
                expandedObjects.add(new CourseObjectWrapper(practiceCourseObject, new CourseObjectNameRef(branch.getTransitionName())));
                
            }else if(contentObj instanceof generated.course.Remediation){
                
                generated.course.Remediation remediation = (generated.course.Remediation)contentObj;
                               
                setExcludeRuleExampleContent(remediation.getExcludeRuleExampleContent() != null && 
                        remediation.getExcludeRuleExampleContent() == BooleanEnum.TRUE);
            }
        }
        
        //the first transition based on the course.xml for this branch point course transition
        //By default this is the Rule quadrant, but it can be disabled in authoring.
        if(!expandedObjects.isEmpty()){
            nextCourseObject = expandedObjects.get(0);
        }
    } 
    
    /**
     * Check whether all of the concepts covered in the knowledge phases (e.g. Rule) of this adaptive courseflow
     * are included in the {@link #knowledgeAdvancementConcepts}.  The {@link #knowledgeAdvancementConcepts}
     * contain the course concepts that the learner has expert knowledge on. 
     * @return true if the learner has expert knowledge on all the course concepts covered in this knowledge phases
     */
    public boolean willKnowledgeBeSkipped(){
        
        //for now just do a simple count comparison between the number of concepts taught in this branch point
        //that the user is an expert on and the total number of concepts taught in this branch point
        //TODO: future work would involve possibly trimming the concepts taught by this branch point to a subset    
        int expertConceptCnt = 0;
        for(String branchPtConcept : branch.getConcepts().getConcept()){
            
            for(AdvancementConcept advancementConcept : knowledgeAdvancementConcepts){
                
                if(branchPtConcept.equalsIgnoreCase(advancementConcept.getConcept())){
                    //found a concept that is taught by this branch point and needs to be skipped
                    //because the advancement information contains the concept as well.
                    expertConceptCnt++;
                }
            }
        }
        
        return expertConceptCnt == branch.getConcepts().getConcept().size();
    }
    
    
    /**
     * Update this branch points history of which questions the learner correctly versus incorrectly
     * answered for a recall survey.
     * 
     * @param correctQuestions collection of question ids the learner just answered correctly for a recall survey
     * @param incorrectQuestions collection of question ids the learner just answered incorrectly for a recall survey
     */
    public void updateLastTimeAnswered(Set<Integer> correctQuestions, Set<Integer> incorrectQuestions){
        
        //add the correct questions to the correct set and remove them (if present) 
        //from the incorrect set for this class
        //
        for(Integer qId : correctQuestions){            
            questionsCorrectLastTimeAnswered.add(qId);
            questionsIncorrectLastTimeAnswered.remove(qId);
        }
        
        //add the incorrect questions to the incorrect set and remove them (if present) 
        //from the correct set for this class
        //
        for(Integer qId : incorrectQuestions){            
            questionsIncorrectLastTimeAnswered.add(qId);
            questionsCorrectLastTimeAnswered.remove(qId);
        }
    }
    
    /**
     * Update the recall survey request instance by adding adding question priority
     * information based on the question's that were previously answered correct versus
     * incorrect.  The goal of which to prefer the questions that were answered incorrect
     * so the learner has a chance to answer them correctly in the next recall survey.
     * 
     * @param request the get survey request to update
     */
    public void prioritizeRecallSurveyRequest(GetKnowledgeAssessmentSurveyRequest request){

        for(String concept : request.getConcepts().keySet()){
            
            ConceptParameters params = request.getConcepts().get(concept);
            for(Integer qId : questionsCorrectLastTimeAnswered){
                params.addAvoidQuestion(qId);
            }
            
            for(Integer qId : questionsIncorrectLastTimeAnswered){
                params.addPreferredQuestion(qId);
            }
        }
    }
    
    /**
     * Handle the progression course of action for this branch point by moving onto the next course element defined in
     * this branch point course element.  This could either be a quadrant or a set of intermediate course elements (e.g. guidance).
     * 
     * @param strategies the collection of pedagogical request strategies that specifically define progression of the branch point.  This
     * would include metadata to use to select the content to deliver for a Rule or Example quadrant.
     * @return contains any intermediate (i.e. between quadrant) course elements to execute on until reaching the next
     * quadrant in the branch point course element.  If this list is empty that means there are no intermediate course elements.
     * @throws Exception if there was a problem building a Rule/Example content course element
     */
    private List<CourseObjectWrapper> handleProgression(List<BranchAdpatationStrategyTypeInterface> strategies) throws Exception {
        
        //TODO: for now just using the first in the list
        BranchAdpatationStrategyTypeInterface type = strategies.get(0);
        if(!(type instanceof ProgressionInfo || type instanceof RemediationInfo)){
            //progression happens when moving forward in the adaptive course course object
            //rule->(progression strategy)->example->(progression strategy)->recall 
            //recall->(remediationinfo strategy)->AAR->(progression strategy)->remediation->(progression strategy)->recall
            throw new IllegalArgumentException("The strateiges must all be of type "+ProgressionInfo.class.getName()+" or "+RemediationInfo.class.getName()+".  Found one of type "+type);
        }
        
        // flag used to indicate whether a structured review (AAR) should be shown next that contains information
        // about how the learner passed the check on learning (Recall) that was just finished.  This will only happen
        // if the next phase is remediation-after-recall and there is nothing to remediate on in this adaptive courseflow.
        boolean needsSuccessAAR = nextQuadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL;
        
        List<CourseObjectWrapper> transitions = new ArrayList<>(); 
        int currentIndex = expandedObjects.indexOf(nextCourseObject);
        int index = currentIndex;
        for(; index < expandedObjects.size(); index++){
            
            CourseObjectWrapper contentObj = expandedObjects.get(index);
            
            if(contentObj.getCourseObject() instanceof AbstractExpandedCourseObject){
                //found an adaptive courseflow phase
                
                AbstractExpandedCourseObject expandedCourseObject = (AbstractExpandedCourseObject)contentObj.getCourseObject();
                
                if(index == currentIndex){
                    //found the next course object to execute
                    
                    if(!(expandedCourseObject instanceof RecallCourseObject)){                        
                        //the Rule/Example/Remediation/Practice course objects will come here... and they need an actual
                        //course object to be created
                        
                        if(expandedCourseObject instanceof AbstractCDTCourseObject){
                            //Rule/Example/Practice need the metadata in the progression info object
                                                        
                            AbstractCDTCourseObject cdtCourseObject = (AbstractCDTCourseObject)expandedCourseObject;
                            if(type instanceof ProgressionInfo){
                                //retrieve the metadata attributes and apply them to all concepts for this 
                                //adaptive courseflow course object
                                
                                ProgressionInfo progressionInfo = (ProgressionInfo)type;
                                List<MetadataAttributeItem> metadataItems = ((ProgressionInfo)type).getAttributes();
                                for(String concept : cdtCourseObject.concepts){
                                    
                                    PassiveRemediationConcept passiveConcept = new PassiveRemediationConcept(concept, metadataItems, progressionInfo.getQuadrant());
                                    
                                    List<AbstractRemediationConcept> remediationConcepts = new ArrayList<>();
                                    remediationConcepts.add(passiveConcept);
                                    cdtCourseObject.setConceptRemediation(concept, remediationConcepts);
                                }

                            }else if(type instanceof RemediationInfo){
                                //retrieve the metadata attributes specific to course concepts needing remediation
                                
                                RemediationInfo remediationInfo = (RemediationInfo)type;
                                cdtCourseObject.setConceptRemediationMap(remediationInfo.getRemediationMap());
                                
                                // this is not a successful pass of recall phase
                                needsSuccessAAR = false;
                            }
                            
                        }else if(expandedCourseObject instanceof RemediationCourseObject){
                            
                            if(((RemediationCourseObject)expandedCourseObject).getRemediationInfo() == null){
                                //skip the after recall and after remediation phases if a pedagogical strategy request wasn't received
                                continue;
                            }
                            
                            // this is not a successful pass of recall phase
                            needsSuccessAAR = false;
                            
                        }
                        
                        buildContentDeliveryPhase(expandedCourseObject, transitions, false);                        
                        
                    }else if(currentCOA == COA.REMEDIATION_AFTER_PRACTICE){                        
                        //don't add Recall transition if current course of action is REMEDIATION_AFTER_PRACTICE because we 
                        //don't want to do the Recall quadrant after the remediation Example quadrant
                        if(logger.isInfoEnabled()){
                            logger.info("Skipping Recall quadrant because this branch point is in "+COA.REMEDIATION_AFTER_PRACTICE+" mode.");
                        }
                        continue;
                        
                    }else{
                    
                        if(logger.isDebugEnabled()){
                            logger.debug("Adding the quadrant transition of "+expandedCourseObject+".");
                        }
                    
                        //add the transition info to the transitions to inject into the course flow
                        transitions.add(contentObj);
                    }
                    
                    
                    //don't add any course objects after Recall phase because there could be after recall remediation
                    //that should come before the next progression course element
                    if(expandedCourseObject instanceof RecallCourseObject){
                        index++;
                        break;
                    }

                }else{
                    //don't build the following quadrant, only the next/upcoming quadrant
                    break;
                }
                
            }else{
                
                //don't add intermediate transitions between Rule-Example, Example-Recall if
                //the user has already seen them because this is currently handling an after Recall
                //remediation path.
                //TODO: handle if the author wants to present the transition again using the course.xsd RemediationBypass value.
                if(successiveRecallCount == 0){
                    transitions.add(contentObj);
                }
            }
        }
        
        // Add a structured review (AAR) to see the survey responses of the recall that the learner has passed with Expert assessment
        // across all concepts.
        // The next phase is remediation after recall, however this method is for progression so remediation
        // will not be given.  Only want the AAR to be added if the remediation is NOT going to happen.
        if(needsSuccessAAR){
            notifyAssessmentEvent(true);
            generated.course.AAR aar = buildRemediationAARTransition(AFTER_RECALL_PASSED_HEADING);
            transitions.add(0, CourseObjectWrapper.generateCourseObjectWrapper(aar, authoredCourseDirectory));
        }
        
        if(nextCourseObject != null && 
                nextCourseObject.getCourseObject() instanceof RemediationCourseObject && 
                ((RemediationCourseObject)nextCourseObject.getCourseObject()).getRemediationInfo() != null){
            //go back to recall which is right before the current remediation course object index
            index = currentIndex - 1;
            
            //reset remediation info as it was just used above in this method to build remediation content for the learner
            ((RemediationCourseObject)nextCourseObject.getCourseObject()).setRemediationInfo(null);
        }
        
        if(index < expandedObjects.size()){  
            //set the next transition for this branch point to the next adaptive courseflow course object to execute (quadrant or inter-quadrant transition)
            //if the index is beyond the size of the branch point's transition list (i.e. the index is
            //past where the Practice course element, the last element in a branch point), than this is not done.
            
            nextCourseObject = expandedObjects.get(index);
            if(logger.isInfoEnabled()){
                logger.info("The next transition, as part of Progression course of action, to execute for this branch point will be "+nextCourseObject+" (index = "+index+", size = "+expandedObjects.size()+").");
            }
        }else{
            nextCourseObject = null;  //all done
        }
        
        return transitions;
    }
    
    /**
     * Handle the remediation after recall course of action by determining what the next quadrant to execute should be (Rule or Example)
     * and what concept(s) should be presented in the ideal content.  The ideal content is also chosen by the metadata in the strateiges.
     * 
     * An After Action Review (AAR) course element will also be generated and populated with a generic message based on the concept(s) and
     * which quadrant(s) are needed for remediation.  This AAR will always be the first course element in the returned course elements.
     * 
     * @param strategies the collection of pedagogical request strategies that specifically define after recall remediation of the branch point.  This
     * would include metadata to use to select the content to deliver for a Rule followed by example or Example quadrant.
     * @param courseEvents used to help populate the generated AAR course element with prior experiences
     * @return the first element will be the AAR course element followed by any intermediate (i.e. between quadrant) 
     * course elements to execute on until reaching the next quadrant in the branch point course element.
     * @throws IOException - if there was a problem retrieving a file reference using the authored course folder
     */
    private List<CourseObjectWrapper> handleRemediationAfterRecall(List<BranchAdpatationStrategyTypeInterface> strategies, Map<Date, List<AbstractAfterActionReviewEvent>> courseEvents) throws IOException{
        
        //TODO: for now just using the first in the list
        BranchAdpatationStrategyTypeInterface type = strategies.get(0);
        if(!(type instanceof RemediationInfo)){
            throw new IllegalArgumentException("The strateiges must all be of type "+RemediationInfo.class.getName()+".  Found one of type "+type);
        }
        
        RemediationInfo strategy = (RemediationInfo) type;
        Map<String, List<AbstractRemediationConcept>> remediationMap = strategy.getRemediationMap();
        
        if(logger.isInfoEnabled()){
            logger.info("Handling course concept, after recall remediation based on: "+strategy);
        }
        
        // update current paradata beans of failed Recall
        notifyAssessmentEvent(false);
        
        AfterActionReviewRemediationEvent aarEvent = new AfterActionReviewRemediationEvent(branch.getTransitionName());
        aarEvent.setAdaptiveCourseflowEvent(true);
        
        for(String concept : remediationMap.keySet()){
            
            //the remediation must be for a concept being taught in this branch point
            //note: no need to provide the concept mapping since this is not a practice remediation handler method, its recall which
            //      references course level concepts and not a practice that uses DKF concepts that need to be translated to course concepts.
            if(hasBranchPointConcept(concept, null, branch.getConcepts().getConcept())){
            
                aarEvent.addRemediationInfo("'"+concept+"'");
            }
        }//end for
        
        List<AbstractAfterActionReviewEvent> aarDatedEvent = new ArrayList<>(1);
        aarDatedEvent.add(aarEvent);
        Date now = new Date();
        courseEvents.put(now, aarDatedEvent);
        
        if(logger.isInfoEnabled()){
            logger.info("At "+now+" added after Recall remediation AAR event of "+aarEvent+" for "+this+".");
        }

        
        //find the index of the remediation course object created by this handler instance.       
        boolean remediationQuadrantFound = false;
        for(int index = 0; index < expandedObjects.size(); index++){
            
            CourseObjectWrapper expandedCourseObject = expandedObjects.get(index);
            
            if(expandedCourseObject.getCourseObject() instanceof RemediationCourseObject){
                
                RemediationCourseObject remediationCourseObject = (RemediationCourseObject)expandedCourseObject.getCourseObject();
                remediationCourseObject.setRemediationInfo(strategy);
             
                if(logger.isInfoEnabled()){
                    logger.info("Due to the pedagogical request containing remediation, the next course object will no longer be "+nextCourseObject+
                            " but instead the remediation course object of "+remediationCourseObject);
                }
                nextCourseObject = expandedCourseObject;
                remediationQuadrantFound = true;
            }
        }//end for
        
        //build a new AAR transition with a remediation event
        generated.course.AAR aar = null;
        aar = buildRemediationAARTransition(AFTER_RECALL_REMEDIATION_HEADING);
        
        if(!remediationQuadrantFound){
            logger.error("Unable to find the remediation course object for the remediation phase of the '"+this.branch.getTransitionName()+"' adaptive courseflow course object.");
            return null;
        }
        
        //add the AAR as the first transition to visit next
        List<CourseObjectWrapper> transitions = new ArrayList<>(); 
        if(aar != null){
            transitions.add(CourseObjectWrapper.generateCourseObjectWrapper(aar, authoredCourseDirectory));
        }
        
        return transitions;
    }
    
    /**
     * Handle the remediation after practice course of action by adding the Example quadrant for this branch point
     * to the returned list of course elements.  If this branch point contains the Practice quadrant that spurred the remediation,
     * than it will be identified as the next quadrant to execute after all the example quadrants have been completed.  
     * 
     * @param strategies the collection of pedagogical request strategies that specifically define after practice remediation of the branch point.  This
     * would include metadata to use to select the content to deliver for a Example quadrant.
     * @param practiceToCourseConcept mapping of DKF concepts used in practice to course level concepts.  Can't be null or empty. 
     * @param courseEvents used to help populate the generated AAR course element with prior experiences
     * @return will contain only 1 element, this branch point's example quadrant.
     * @throws Exception if there was a problem building content transitions for remediation
     */
    public List<CourseObjectWrapper> handleRemediationAfterPractice(List<RemediationInfo> strategies, Map<String, String> practiceToCourseConcept,  
            Map<Date, List<AbstractAfterActionReviewEvent>> courseEvents) throws Exception{
        
        if(practiceToCourseConcept == null || practiceToCourseConcept.isEmpty()){
            throw new IllegalArgumentException("The practice concept to course concept map must contain at least 1 entry, otherwise how else can practice concepts be associated with their course concept counterpart.");
        }
        
        //TODO: for now just using the first in the list
        RemediationInfo strategy = strategies.get(0);
        Map<String, List<AbstractRemediationConcept>> remediationMap = strategy.getRemediationMap();
        
        if(logger.isInfoEnabled()){
            logger.info("Handling course concept, after practice remediation based on: "+strategy);
        }
        
        //get concept(s) for the remediation quadrant to help narrow the content delivered in Example
        RemediationInfo remediationInfo = new RemediationInfo();
        
        AfterActionReviewRemediationEvent aarEvent = new AfterActionReviewRemediationEvent(branch.getTransitionName());
        aarEvent.setAdaptiveCourseflowEvent(true);
        
        for(String concept : remediationMap.keySet()){
            
            //the remediation must be for a concept being taught in this branch point
            if(hasBranchPointConcept(concept, practiceToCourseConcept, branch.getConcepts().getConcept())){
                    
                String courseConcept = practiceToCourseConcept.get(concept);
                if(courseConcept == null){
                    throw new RuntimeException("Unable to find the remediation concept named '"+concept+"' as a key in the practice concept relationship map of "+practiceToCourseConcept+".");
                }
                
                if(logger.isInfoEnabled()){
                    logger.info("Adding AAR entry for Practice DKF concept of '"+concept+"' (course concept of '"+courseConcept+"') mentioning remediation is needed at the Example level.");
                }
                
                // the strategy remediation map for practice/skill is built using the dkf task/concept node names and not the course.xml course concept strings,
                // therefore need to use the same concept name string to look up in the map provided by the pedagogical request
                remediationInfo.getRemediationMap().put(courseConcept, strategy.getRemediationMap().get(concept));
                
                aarEvent.addRemediationInfo("'"+courseConcept+"'");
            }
        }
        
        List<CourseObjectWrapper> courseObjects = new ArrayList<>(); 
        if(!remediationInfo.getRemediationMap().isEmpty()){
            
            if(!hasPracticeAttemptLimit() || successivePracticeCount < allowedPracticeAttempts){
            
                RemediationCourseObject remediationCourseObject = new RemediationCourseObject(branch.getTransitionName());
                remediationCourseObject.setRemediationInfo(remediationInfo);
                
                //need to update the quadrant transition info with latest metadata!!!
                buildContentDeliveryPhase(remediationCourseObject, courseObjects, true);

            }else{
                if(logger.isInfoEnabled()){
                    logger.info("Not adding after practice remediation on "+remediationInfo.getRemediationMap().keySet()+" because the maximum number of practice attempts of "+allowedPracticeAttempts+" has been reached.");
                }
            }
                        
        }else{
            logger.error("Unable to find the course object for the remediation phase of "+MerrillQuadrantEnum.EXAMPLE+".");
            return null;
        }
        
        List<AbstractAfterActionReviewEvent> aarDatedEvent = new ArrayList<>(1);
        aarDatedEvent.add(aarEvent);
        Date now = new Date();
        courseEvents.put(now, aarDatedEvent);

        if(logger.isInfoEnabled()){
            logger.info("At "+now+" added after Practice remediation AAR event of "+aarEvent+" for "+this+".");
        }
        
        return courseObjects;
    }
    
    /**
     * Handle the remediation after practice course of action by setting the next quadrant to Practice, if this branch point contains 
     * the Practice quadrant that spurred the remediation.  This way is will be the next quadrant to execute after all the remediation 
     * example quadrants have been completed.  
     * 
     * @param strategies the collection of pedagogical request strategies that specifically define after practice remediation of the branch point. 
     * @return will always contain 0 elements.
     */
    private List<CourseObjectWrapper> handleRemediationAfterPractice(List<BranchAdpatationStrategyTypeInterface> strategies){
        
        // update current paradata beans of failed practice
        notifyAssessmentEvent(false);
        
        for(int index = 0; index < expandedObjects.size(); index++){
            
            CourseObjectWrapper courseObject = expandedObjects.get(index);
            
            if(courseObject.getCourseObject() instanceof AbstractExpandedCourseObject){
                
                AbstractExpandedCourseObject expandedCourseObject = (AbstractExpandedCourseObject)courseObject.getCourseObject();
                
                if(expandedCourseObject instanceof PracticeCourseObject){
                    //this branch point is the one with the practice quadrant, therefore the next time
                    //it is called upon the Practice quadrant should be executed
                    if(logger.isInfoEnabled()){
                        logger.info("This branch point of "+this+" is the one with the Practice quadrant, therefore setting the next transition to " +
                            "execute in the branch point to this found Practice quadrant for the remediation after practice course of action."); 
                    }
                    nextCourseObject = courseObject;
                    break;
                }
            }
        }//end for
        
        return new ArrayList<>();
    }
    
    /**
     * Build a list of course transitions based on the next quadrant to enter and any other transitions authored between the current
     * quadrant and the next quadrant.  The next quadrant to enter is based on the provided pedagogical request as well as the current
     * state of this branch point.
     * 
     * If remediation after Recall is warranted based on the pedagogical request, an AAR course transition will created.  That AAR course transition
     * will be the next course transition shown to the user.
     * 
     * If remediation after Practice is warranted based on the pedagogical request, an AAR course transition should be created by the branch point manager.
     * 
     * @param request - a pedagogical request that should indicate a branch adaptation instructional strategy. Can't be null.
     * @param courseEvents - contains the current running list of AAR course events for the course which is used by this method to
     * add a remediation AAR event (if warranted) which will then be displayed in any upcoming AAR course transition.
     * @return new course transitions for the domain module to handle.  The list can be empty to indicate
     * this branch point has finished with the learner comprehending the concepts it taught.
     * Note: null will be returned in the case where the course can no longer continue because the learner
     * has failed to comprehend the concepts in this branch point in a timely manner. 
     * @throws CourseComprehensionException if the learner failed to pass the Recall test or the Practice scenario given a pre-defined maximum number
     * of attempts for each.
     * @throws Exception if there was an issue building any transitions based on the Pedagogical request
     */
    public List<CourseObjectWrapper> handleRequest(PedagogicalRequest request, Map<Date, List<AbstractAfterActionReviewEvent>> courseEvents) throws CourseComprehensionException, Exception{
        
        List<BranchAdpatationStrategyTypeInterface> strategies = new ArrayList<>();
        
        //retrieve the course of action to take and the strategies for that type
        COA action = determineAllowedCOA(request, strategies);
        if(logger.isInfoEnabled()){
            logger.info("The identified course of action is "+action+".");
        }
        
        if(action == null){
            //didn't find a course of action to take
            //ERROR
            logger.error("Unable to find a branch adaptation request in the ped request received, therefore unable to determine which quadrant to transition too or what content should be used.\n"+request);
            throw new IllegalArgumentException("The pedagogical request doesn't contain a branch adaptation request needed to execute the next piece of logic in this branch point.");
        }else if(action == COA.FINISHED){
            //this can happen in one of two cases:
            //1) the branch point is done and the request received doesn't indicate another quadrant to go to (i.e. no remediation)
            //2) the previous transition was the recall quadrant and the next is an inter-quadrant transition (e.g. guidance), therefore
            //   need to return those inter-quadrant transitions in order to keep this MBP course element going.
            return getAfterRecallInterQuadrantTransitions();
        }       
        
        //
        //update the number of times this Recall quadrant has been reach w/o advancing past this branch point transition
        //
        if(action == COA.REMEDIATION_AFTER_RECALL){

            successiveRecallCount++;
            
            if(hasRecallAttemptLimit() && successiveRecallCount >= allowedRecallAttempts){
                //the learner is not increasing their comprehension of this branch points concepts at a pace the course author
                //wants, therefore bounce the learner out of the course (BAIL OUT!!!)
                
                if(logger.isInfoEnabled()){
                    logger.info("The learner has failed to proceed past the recall phase after "+successiveRecallCount+
                        " attempts for the current adaptive courseflow course object named '"+branch.getTransitionName()+"'.  Therefore GIFT is gracefully stopping the exeuction of this course.");
                }
                throw new CourseComprehensionException("The learner has failed to proceed past the recall phase after "+successiveRecallCount+
                        " attempts for the current adaptive courseflow course object.");
            }
        }
        
        //
        //update the number of times this Recall quadrant has been reach w/o advancing past this branch point transition
        //
        if(action == COA.REMEDIATION_AFTER_PRACTICE){
            successivePracticeCount++;
            
        }else if(hasPracticeAttemptLimit() && successivePracticeCount >= allowedPracticeAttempts){
            //the learner is not increasing their comprehension of this branch points concepts at a pace the course author
            //wants, therefore bounce the learner out of the course (BAIL OUT!!!)
            
            if(logger.isInfoEnabled()){
                logger.info("The learner has failed to proceed past the practice quadrant after "+successivePracticeCount+
                    " attempts for the current branch point.  Therefore GIFT is gracefully stopping the exeuction of this course.");
            }
            throw new CourseComprehensionException("The learner has failed to proceed past the practice quadrant after "+successivePracticeCount+
                    " attempts for the current branch point.");
        }
        
        List<CourseObjectWrapper> transitions = null;
        switch(action){
        
        case REMEDIATION_AFTER_RECALL:
            //handle remediation after a recall test by setting up the necessary Rule and Example quadrants in this
            //branch point to execute.
            transitions = handleRemediationAfterRecall(strategies, courseEvents);
            break;
        case REMEDIATION_AFTER_PRACTICE:
            //handle remediation after a practice scenario by setting up the necessary Rule and Example quadrants in this
            //branch point to execute.
            transitions = handleRemediationAfterPractice(strategies);
            break;
        case PROGRESSION:
            //handle progressing to the next quadrant for this branch point.  The next quadrant maybe the end of
            //the branch point.
            transitions = handleProgression(strategies);
            break;
        case ADVANCEMENT:
            //advancement (i.e. skipping MBP Rule/Example/Recall based on Cognitive Knowledge) is handled when expanding
            //this branch point course element during initialization.  Checking it here is used to determine if the ped
            //request only contains an advancement type branch adaptation strategy.
            //This could be reached after passing this MBP Recall test, therefore need to add any remaining course transitions 
            //authored for this MBP
            transitions = getAfterRecallInterQuadrantTransitions();
            break;
        default:
            logger.error("Unhandled action of "+action+" given the pedagogical request of "+request+".");
        }
        
        currentCOA = action;
        
        return transitions;
        
    }

    /**
     * Determine what the appropriate course of action (COA) is based on the pedagogical request received.
     *   
     * The priorities from highest to lowest are:
     *  1) Remediation
     *     i. After Recall
     *    ii. After Practice         
     *  2) Progression - next quadrant in this MBP
     *  3) Advancement - advancement past the MBP
     *    
     * 
     * @param request the pedagogical request to analyze
     * @param strategies the strategies that define the highest priority course of action
     * @return COA the course of action type determined to be appropriate based on the request provided.
     *         If null, then this method was unable to determine what course of action to take which is a huge problem.
     */
    private COA determineAllowedCOA(PedagogicalRequest request, List<BranchAdpatationStrategyTypeInterface> strategies){

        COA highestPriority = null;
        
        if(logger.isInfoEnabled()){
            logger.info("Determining course of action:  previousQuadrant = "+previousQuadrant+", nextQuadrant = "+nextQuadrant+".");
        }
        
        for(List<AbstractPedagogicalRequest> requestList : request.getRequests().values()){
            for(AbstractPedagogicalRequest aRequest : requestList){
                
                if(aRequest instanceof RequestBranchAdaptation){
                    //found a branch adaptation request
                    
                    RequestBranchAdaptation adaptationRequest = (RequestBranchAdaptation)aRequest;
                    BranchAdaptationStrategy strategy = adaptationRequest.getStrategy();
                    BranchAdpatationStrategyTypeInterface type = strategy.getStrategyType();
                    
                    if(type instanceof AdvancementInfo){
                        //found advancement information 
                        
                        if(hasBranchPointConcept(type, true, practiceConceptToCourseConcept, branch.getConcepts().getConcept())){
                            // the strategy says that all of the course concepts taught in this branch should be skipped
                            // because they are in the advancement info object
                            // -- Otherwise if at least one course concepts taught in this branch is missing from the strategy
                            //    the COA can't be to ADVANCE to next phase in this branch
                        
                            if((highestPriority == null || highestPriority.ordinal() > COA.ADVANCEMENT.ordinal())){
                                // don't allow Advancement to over-ride a previously found higher priority COA
                                highestPriority = COA.ADVANCEMENT;
                                
                                if(logger.isInfoEnabled()){
                                    logger.info("Setting the current course of action to "+COA.ADVANCEMENT+" beause there is advancement information in the pedagogical request that contains all of this branch points concepts.");
                                }
                            }
                        }
                        
                    }else if(type instanceof RemediationInfo){
                        //found remediation information
                        
                        RemediationInfo info = (RemediationInfo)type;
                        boolean remediationRelevantToBranchConcepts = hasBranchPointConcept(info, false, practiceConceptToCourseConcept, branch.getConcepts().getConcept());
                        boolean remediationRelevantToPracticeConcepts = getPracticeQuadrant() != null ? 
                                hasBranchPointConcept(info, false, practiceConceptToCourseConcept, getPracticeQuadrant().getPracticeConcepts().getCourseConcept()) :
                                false;
                        
                        //the remediation information mentions at least 1 concept covered by this branch point, 
                        //determine if this remediation if after a recall or practice
                        
                        if(logger.isInfoEnabled()){
                            logger.info("Found one or more concepts for this merrill branch in the remediation info.");
                        }
                        
                        if((nextQuadrant == MerrillQuadrantEnum.REMEDIATION_AFTER_RECALL || nextQuadrant == MerrillQuadrantEnum.PRACTICE) && previousQuadrant == MerrillQuadrantEnum.RECALL){  
                            //its remediation after recall
                            //$ - nextQuadrant = null, previousQuadrant = Recall - after failing Recall in MBP w/o a Practice
                            //$ nextQuadrant = Practice, previousQuadrant = Recall - after failing Recall in MBP with a Practice
                            if(remediationRelevantToBranchConcepts) {
                                if(highestPriority != COA.REMEDIATION_AFTER_RECALL){
                                    //clear any strategies not of this COA
                                    if(logger.isInfoEnabled()){
                                        logger.info("The new course of action is "+COA.REMEDIATION_AFTER_RECALL+" because there is remediation information in the pedagogical request" +
                                          " and this branch point has just finished a Recall quadrant.");
                                    }
                                    strategies.clear();
                                }
                                
                                strategies.add(type);
                                highestPriority = COA.REMEDIATION_AFTER_RECALL;
                            }
                        }else if((nextQuadrant == null && previousQuadrant == MerrillQuadrantEnum.PRACTICE) ||
                                (nextQuadrant == null && previousQuadrant == null)){
                            //its remediation after practice
                            //$ if practice is NOT in this MBP - nextQuadrant = Rule or Example, previousQuadrant = Recall
                            //$ if practice is in this MBP - nextQuadrant = null, previousQuadrant = Practice
                            if(remediationRelevantToPracticeConcepts) {
                                if(highestPriority != COA.REMEDIATION_AFTER_PRACTICE){
                                    //clear any strategies not of this COA
                                    if(logger.isInfoEnabled()){
                                        logger.info("The new course of action is "+COA.REMEDIATION_AFTER_PRACTICE+" because there is remediation information in the pedagogical request" +
                                            "and this branch point has just finished a Practice quadrant.");
                                    }
                                    strategies.clear();
                                }
                                
                                strategies.add(type);
                                highestPriority = COA.REMEDIATION_AFTER_PRACTICE;
                            }
                        }else if(((nextQuadrant == MerrillQuadrantEnum.RULE && previousQuadrant == null) ||
                                ((nextQuadrant == MerrillQuadrantEnum.RULE || nextQuadrant == MerrillQuadrantEnum.EXAMPLE) && previousQuadrant == MerrillQuadrantEnum.RECALL) ||
                                ((nextQuadrant == MerrillQuadrantEnum.RULE || nextQuadrant == MerrillQuadrantEnum.EXAMPLE) && previousQuadrant == MerrillQuadrantEnum.PRACTICE)) 
                                && highestPriority == null){
                            //this covers the case when there is remediation in the strategy, however it is pre-branch point meaning
                            //it might have come from the result of a pre-test before this branch point course element
                            //$ previous = null, next = rule -first entering MBP having failed the pre-test
                            //$ previous = recall, next = rule or example -after presenting the created AAR after failing recall in MBP w/o Practice                            
                            //previous = Practice, next = rule -after presenting the created AAR after failing recall in MBP w/ Practice
                            
                            if(remediationRelevantToBranchConcepts) {
                                if(logger.isInfoEnabled()){
                                    logger.info("Setting the course of action to "+COA.PROGRESSION+" because remediation on this branch's concepts was provided and this" +
                                		" branch point course element has yet to be executed.  Adding "+type+".");
                                }
                                
                                highestPriority = COA.PROGRESSION;
                                strategies.add(type);
                            }
                        }else if(!(nextQuadrant == MerrillQuadrantEnum.PRACTICE && previousQuadrant == MerrillQuadrantEnum.PRACTICE)){
                            //MH: noticed this state happens after failed practice in Simple EMAP course during 9/25/15 test event.  Not 
                            //sure why but the course behaves as it should so no need to log an error
                            if(remediationRelevantToBranchConcepts) {
                                logger.error("Found remediation information in a pedagogical request but not sure how to handle since the previous quadrant is "+previousQuadrant+" and the next quadrant is "+nextQuadrant+".");
                            }
                        }
                        
                    }else if(type instanceof ProgressionInfo){
                        
                        //the strategy is not for remediation or advancement (at least that can be addressed now and with this branch point)                    
                        
                        ProgressionInfo progression = (ProgressionInfo)type;
                        if(progression.getQuadrant() == null && progression.getAttributes() == null){ 
                            //finished branch point course element
                            if(logger.isInfoEnabled()){
                                logger.info("The new course of action (found thus far) is "+COA.FINISHED+" because there is no remediation nor advancement information nor metadata attributes in the pedagogical request" +
                                    "and this branch point has finished all quadrants.");
                            }
                            highestPriority = COA.FINISHED;
                            
                        }else{                        
    
                            if((highestPriority == null || highestPriority == COA.PROGRESSION || highestPriority == COA.FINISHED || highestPriority == COA.ADVANCEMENT)){
                                //the strategies list is either empty or has PROGRESSION strategies in it  
                                //null = a COA hasn't been selected yet
                                //Progression = another progression(s) have been selected, add this one
                                //Finished = until this strategy was found in the list, the COA was going to be finish this branch point
                                //Advancement = until this strategy was found in the list, the COA was going to be to skip the new quadrant in this branch point
                                if(logger.isInfoEnabled()){
                                    logger.info("The new course of action (found thus far) is "+COA.PROGRESSION+" because there is no remediation nor advancement information in the pedagogical request" +
                                        " but there are metadata attributes.");
                                }
                                highestPriority = COA.PROGRESSION;
                                strategies.add(type);
                            }
                        }
                        
                    }else{
                        logger.error("Found unhandled branch adaptation strategy type of "+type+".  Is the concept in the strategy part of this branch course concepts of "+branch.getConcepts().getConcept());
                    }
                    
                }//end if on branch adaptation
                    
                
            }//end for
        }//end for
        
        return highestPriority;
    }
    
    /**
     * Return whether or not the advancement information mentions at least one of the concepts
     * covered by this branch point.
     * 
     * @param concept the advancement information from a pedagogical strategy to search
     * @return boolean true iff the advancement information references at least one of the concepts in
     * this branch point course element.
     */
    public boolean hasBranchPointConcept(AdvancementConcept concept){
        
        String advancementConcept = concept.getConcept();
        for(String name : branch.getConcepts().getConcept()){
            
            if(advancementConcept.equalsIgnoreCase(name)){
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Return true if the strategy type information provided includes at least one of this branch
     * point's concepts, i.e. the knowledge concepts (not the practice or skill concept list).
     * 
     * @param strategyType the strategy type to check. 
     * @param hasAllConcepts whether or not to check that all concepts referenced by this branch point are in the strategy type provided.
     * @param courseConceptMap mapping of DKF concepts used in practice to course level concepts.  Can be null or empty. 
     * @return boolean true if the concepts are found.
     */
    public boolean hasBranchPointConcept(BranchAdpatationStrategyTypeInterface strategyType, boolean hasAllConcepts, Map<String, String> courseConceptMap) {
        return hasBranchPointConcept(strategyType, hasAllConcepts, courseConceptMap, branch.getConcepts().getConcept());
    }
    
    /**
     * Return true if the strategy type information provided includes at least one of this branch
     * point's concepts.
     * 
     * @param strategyType the strategy type to check. 
     * @param hasAllConcepts whether or not to check that all concepts referenced by this branch point are in the strategy type provided.
     * @param courseConceptMap mapping of DKF concepts used in practice to course level concepts.  Can be null or empty. 
     * @param conceptList the course concepts to check (could be knowledge or skill concepts in an adaptive courseflow).  Can't be null.
     * If empty this method just returns false.
     * @return boolean true if the concepts are found.
     */
    public boolean hasBranchPointConcept(BranchAdpatationStrategyTypeInterface strategyType, boolean hasAllConcepts, Map<String, String> courseConceptMap, List<String> conceptList){
        
        int cnt = 0;
        if(strategyType instanceof RemediationInfo){
        
            Map<String, List<AbstractRemediationConcept>> remediationConcepts = ((RemediationInfo)strategyType).getRemediationMap();
            if(remediationConcepts != null && !remediationConcepts.isEmpty()){
            
                for(String concept : remediationConcepts.keySet()){
                    
                    //note: provide the concept map in case this is the branch point with practice (i.e. the map will
                    //be populated) and the caller to this method might be determining if the remediation concepts match
                    //this branch points course concepts - therefore a DKF concept to course concept translation is needed.
                    if(hasBranchPointConcept(concept, courseConceptMap, conceptList)){
                        
                        if(hasAllConcepts){
                            cnt++;
                        }else{
                            return true;
                        }
                    }
    
                }
                
                if(cnt == branch.getConcepts().getConcept().size()){
                    return true;
                }
            }
            
        }else if(strategyType instanceof AdvancementInfo){        
            
            List<AdvancementConcept> advancementConcepts = ((AdvancementInfo)strategyType).getConcepts();
            if(advancementConcepts != null && !advancementConcepts.isEmpty()){
            
                for(AdvancementConcept concept : advancementConcepts){
                    
                    if(hasBranchPointConcept(concept)){
                        
                        if(hasAllConcepts){
                            cnt++;
                        }else{
                            return true;
                        }
                    }
    
                }
                
                if(cnt == branch.getConcepts().getConcept().size()){
                    return true;
                }
            }
        }
        
        
        return false;
    }    

    
    /**
     * Populate the metadata files collection with any metadata file found that list all the concepts covered by 
     * this MBP.  In addition applying the search criteria provided.
     * 
     * @param metadataFiles collection of metadata files found for the MBP concepts and search criteria
     * @param strategy contains the metadata attributes that will be used as search criteria
     * @param criteria metadata search criteria used to filter the superset of all metadata files found
     */
//    private void gatherMetadataForAllBranchPointConcepts(Map<File, generated.metadata.Metadata> metadataFiles, QuadrantInfo strategy, MetadataSearchCriteria criteria){
//     
//        //add the concepts being taught in this branch which will be used as a search filter
//        boolean foundConcept;
//        for(String quadrantConcept : branch.getConcepts().getConcept()){
//            
//            foundConcept = false;
//            for(generated.metadata.Concept criteriaConcept : criteria.getConcepts()){
//                
//                if(quadrantConcept.equals(criteriaConcept.getName())){
//                    foundConcept = true;
//                    break;
//                }                    
//                
//            }
//            
//            //this branch concept was not found in the existing search criteria,
//            //therefore it needs to be added in order to search for metadata that covers all the
//            //concepts in this branch point.
//            if(!foundConcept){
//                
//                generated.metadata.Concept concept = new generated.metadata.Concept();
//                concept.setName(quadrantConcept);
//                
//                List<generated.metadata.Attribute> attributes = new ArrayList<>();
//                for(MetadataAttributeItem item : strategy.getAttributes()){
//                    generated.metadata.Attribute attribute = new generated.metadata.Attribute();
//                    
//                    attribute.setValue(item.getAttribute().getValue());
//                    attributes.add(attribute);
//                }
//                
//                if(concept.getAttributes() == null){
//                    concept.setAttributes(new generated.metadata.Attributes());
//                }
//                
//                concept.getAttributes().getAttribute().addAll(attributes);
//                criteria.addConcept(concept);
//            }                
//            
//        }
//        
//        metadataFiles.putAll(MetadataFileFinder.findFiles(courseDirectory, DomainModuleProperties.getInstance().getDomainDirectory(), criteria));
//    }
    
    
//MH: 09-22-14 - may need this logic in the future
    /**
     * Choose a single metadata reference content file to present from the list provided.  The choice will first be based 
     * on paradata found for the files. Then the content delivered list will be used to help choose content 
     * that hasn't been presented yet.  
     * 
     * @param resourceFiles list of resource files to choose from.  Can't be null or empty.
     * @param quadrantContentDelivered list of resource files that have been delivered to the user already
     * @return File the file chosen as the next to be delivered to the user.  Will not be null.
     */
//    private File chooseContentsByParadata(List<File> resourceFiles, List<File> quadrantContentDelivered){
//        
//        if(resourceFiles == null || resourceFiles.isEmpty()){
//            throw new IllegalArgumentException("The list of files to choose from can't be null or empty.");
//        }
//        
//        //
//        //choose best resource file in list...
//        //
//        File resourceFile = ParadataUtil.selectBest(resourceFiles);
//        
//        logger.info("The metadata file chosen based on the best paradata is "+resourceFile+".");

//        // choose content not delivered yet 
//        // -OR- 
//        // oldest delivered content 
//        // *** (that also best adheres to the metadata attributes)
//        if(quadrantContentDelivered != null && quadrantContentDelivered.contains(resourceFile)){
//            
//            logger.info("The metadata file referenced of "+resourceFile+" has already been delivered to the learner.  Looking for a different file that matches the metadata to present...");
//            
//            //contains the index of the earliest delivered content that also best adheres to the metadata attributes
//            int earliestIndex = Integer.MAX_VALUE;
//            for(File potentialFile : resourceFiles){
//                
//                int index = quadrantContentDelivered.indexOf(potentialFile);
//                if(index == -1){
//                    //found a different piece of content to show, update the reference
//                    resourceFile = potentialFile;
//                    logger.info("Found a content file that hasn't been used yet of "+resourceFile+".");
//                    break;
//                }else if(earliestIndex > index){
//                    earliestIndex = index;
//                }
//            }
//            
//            if(earliestIndex != Integer.MAX_VALUE){
//                //all of the content files have been used, therefore select the earliest delivered file
//                //based on the index in the content delivered list
//                
//                logger.info("Re-using a content file that has been presented already of "+resourceFile+" based on the content that presented the longest ago.");
//            }
//        }
//        
//        logger.info("The metadata file chosen based on what was previously delivered in this course execution is "+resourceFile+".");
//        
//        return resourceFile;
//
//    }
    
    /**
     * Return a new AAR course transition.
     * 
     * @param aarTransitionName the name to give the new AAR course transition
     * @return generated.course.AAR the new AAR course transition
     */
    public static generated.course.AAR buildRemediationAARTransition(String aarTransitionName){
        
        generated.course.AAR aar = new generated.course.AAR();
//        aar.setRemediationBypass(BooleanEnum.TRUE);
        aar.setTransitionName(aarTransitionName);
     
        return aar;
    }
    
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[MerrillsBranchPointHandler: ");
        
        sb.append("concepts = {");
        for(String concept : branch.getConcepts().getConcept()){
            sb.append(" ").append(concept).append(",");
        }
        sb.append("}");
        sb.append(", currentCOA = ").append(currentCOA);
        sb.append(", nextQuadrant = ").append(nextQuadrant);
        sb.append(", previousQuadrant = ").append(previousQuadrant);
        sb.append(", successiveRecallCount = ").append(successiveRecallCount);
        sb.append(", successivePracticeCount = ").append(successivePracticeCount);
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * Defines the parameters needed for building rule phase based course object(s) in this adaptive courseflow
     * course object
     * 
     * @author mhoffman
     *
     */
    public static class RuleCourseObject extends AbstractCDTCourseObject{
        
        private static final long serialVersionUID = 1L;

        /**
         * Set attributes 
         * 
         * @param courseObjectName the name of the adaptive courseflow course object given by the author.  Can't be null or empty.
         * @param courseObject information useful for presenting content for the phase (e.g. MerrillQuadrantEnum.RULE, {@link generated.course.Recall.PresentSurvey} )
         * @param concepts the concepts this phase should cover. Can't be null or empty.
         */
        public RuleCourseObject(String courseObjectName, Serializable courseObjectInfo, List<String> concepts){
            super(courseObjectName, courseObjectInfo, concepts);
            
        }
        
        @Override
        public AbstractExpandedCourseObject deepCopy() {
            RuleCourseObject clone = new RuleCourseObject(this.getCourseObjectName(), this.getCourseObject(), this.getConcepts());
            return clone;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[RuleCourseObject: ");
            sb.append(super.toString());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Defines the parameters needed for building example phase based course object(s) in this adaptive courseflow
     * course object
     * 
     * @author mhoffman
     *
     */
    public static class ExampleCourseObject extends AbstractCDTCourseObject{
        
        private static final long serialVersionUID = 1L;

        /**
         * Set attributes 
         * 
         * @param courseObjectName the name of the adaptive courseflow course object given by the author.  Can't be null or empty.
         * @param courseObjectInfo information useful for presenting content for the phase (e.g. MerrillQuadrantEnum.RULE, {@link generated.course.Recall.PresentSurvey} )
         * @param concepts the concepts this phase should cover. Can't be null or empty.
         */
        public ExampleCourseObject(String courseObjectName, Serializable courseObjectInfo, List<String> concepts){
            super(courseObjectName, courseObjectInfo, concepts);
            
        }
        
        @Override
        public AbstractExpandedCourseObject deepCopy() {
            ExampleCourseObject clone = new ExampleCourseObject(this.getCourseObjectName(), this.getCourseObject(), this.getConcepts());
            return clone;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[ExampleCourseObject: ");
            sb.append(super.toString());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Defines the parameters needed for building recall phase based course object(s) in this adaptive courseflow
     * course object
     * 
     * @author mhoffman
     *
     */
    public static class RecallCourseObject extends AbstractCDTCourseObject{
        
        private static final long serialVersionUID = 1L;

        /**
         * Set attributes 
         * 
         * @param courseObjectName the name of the adaptive courseflow course object given by the author.  Can't be null or empty.
         * @param courseObjectInfo information useful for presenting content for the phase (e.g. MerrillQuadrantEnum.RULE, {@link generated.course.Recall.PresentSurvey} )
         * @param concepts the concepts this phase should cover. Can't be null or empty.
         */
        public RecallCourseObject(String courseObjectName, Serializable courseObjectInfo, List<String> concepts){
            super(courseObjectName, courseObjectInfo, concepts);
            
        }
        
        @Override
        public AbstractExpandedCourseObject deepCopy() {
            RecallCourseObject clone = new RecallCourseObject(this.getCourseObjectName(), this.getCourseObject(), this.getConcepts());
            return clone;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[RecallCourseObject: ");
            sb.append(super.toString());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Defines the parameters needed for building practice phase based course object(s) in this adaptive courseflow
     * course object
     * 
     * @author mhoffman
     *
     */
    public static class PracticeCourseObject extends AbstractCDTCourseObject{
        
        private static final long serialVersionUID = 1L;

        /**
         * Set attributes 
         * 
         * @param courseObjectName the name of the adaptive courseflow course object given by the author.  Can't be null or empty.
         * @param courseObjectInfo information useful for presenting content for the phase (e.g. MerrillQuadrantEnum.RULE, {@link generated.course.Recall.PresentSurvey} )
         * @param concepts the concepts this phase should cover. Can't be null or empty.
         */
        public PracticeCourseObject(String courseObjectName, Serializable courseObjectInfo, List<String> concepts){
            super(courseObjectName, courseObjectInfo, concepts);
            
        }

        @Override
        public AbstractExpandedCourseObject deepCopy() {
            PracticeCourseObject clone = new PracticeCourseObject(this.getCourseObjectName(), this.getCourseObject(), this.getConcepts());
            return clone;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[PracticeCourseObject: ");
            sb.append(super.toString());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Defines the parameters needed for building remediation phase based course object(s) in this adaptive courseflow
     * course object
     * 
     * @author mhoffman
     *
     */
    public static class RemediationCourseObject extends AbstractExpandedCourseObject{
        
        private static final long serialVersionUID = 1L;
        
        /** contains information needed to provide remediation */
        private RemediationInfo remediationInfo = null;
        
        /**
         * Set attribute(s)
         * 
         * @param courseObjectName the name of the adaptive courseflow course object given by the author.  Can't be null or empty.
         */
        public RemediationCourseObject(String courseObjectName){
            super(courseObjectName);            
            
        }
        
        /**
         * Set the information needed to provide remediation
         * 
         * @param remediationInfo can be null
         */
        public void setRemediationInfo(RemediationInfo remediationInfo){
            this.remediationInfo = remediationInfo;
        }
        
        /**
         * Return the information needed to provided remediation
         * 
         * @return can be null if no pedagogical request was received for remediation
         */
        public RemediationInfo getRemediationInfo(){
            return this.remediationInfo;
        }

        @Override
        public AbstractExpandedCourseObject deepCopy() {
            RemediationCourseObject clone = new RemediationCourseObject(this.getCourseObjectName());
            clone.setRemediationInfo(remediationInfo);
            return clone;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[RemediationCourseObject: ");
            sb.append(super.toString());
            sb.append(", remediationInfo = ").append(getRemediationInfo());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * The common information needed to create any course object from a phase of the adaptive courseflow
     * course object.
     * 
     * @author mhoffman
     *
     */
    public abstract static class AbstractExpandedCourseObject implements Serializable{
        
        private static final long serialVersionUID = 1L;

        /** the authored adaptive courseflow course object name */
        private String courseObjectName;
        
        /** 
         * information useful for presenting content  
         * (e.g. {@link generated.course.Recall.PresentSurvey} )
         */
        private Serializable courseObject;
        
        /**
         * Class constructor - set attributes
         * 
         * @param courseObjectName the name of the adaptive courseflow course object given by the author.  Can't be null or empty.
         */
        public AbstractExpandedCourseObject(String courseObjectName){
            
            if(courseObjectName == null || courseObjectName.isEmpty()){
                throw new IllegalArgumentException("The course object name can't be null.");
            }
            
            this.courseObjectName = courseObjectName;            
        }
        
        /**
         * Return the name given to the adaptive courseflow course object being processed.
         * 
         * @return won't be null or empty.
         */
        public String getCourseObjectName(){
            return courseObjectName;
        }
        
        /**
         * Return the course object associated with this phase.  This is useful for
         * presenting content to the learner.
         * 
         * @return can be null if not set (e.g. {@link generated.course.Recall.PresentSurvey} )
         */
        public Serializable getCourseObject(){
            return courseObject;
        }
        
        /**
         * Set the course object information needed to render this in the tutor.
         * 
         * @param courseObject information useful for presenting content ({@link generated.course.Recall.PresentSurvey} )
         * Can't be null.
         */
        public void setCourseObject(Serializable courseObject){
            
            if(courseObject == null){
                throw new IllegalArgumentException("The course object can't be null.");
            }
            
            this.courseObject = courseObject;
        }
        
        /**
         * Create a deep copy of this object.
         * 
         * @return a new object with the same attribute values
         */
        public abstract AbstractExpandedCourseObject deepCopy();
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("courseObjectName = ").append(getCourseObjectName());
            sb.append(", courseObject = ").append(getCourseObject());
            return sb.toString();
        }
    }
    
    /**
     * Contains the common information needed to create a CDT phase into one or more
     * course objects.
     * 
     * @author mhoffman
     *
     */
    public abstract static class AbstractCDTCourseObject extends AbstractExpandedCourseObject{

        private static final long serialVersionUID = 1L;

        /** the concepts this quadrant should cover */
        private List<String> concepts;
        
        /** 
         * map of assessed concept name to a descending prioritized list of remediation types.
         * The list won't be null or empty. 
         */
        private Map<String, List<AbstractRemediationConcept>> conceptRemediationPriorityMap = new HashMap<>();
        
        /**
         * Class constructor - set attributes
         * 
         * @param courseObjectName the name of the adaptive courseflow course object given by the author.  Can't be null or empty.
         * @param courseObject information useful for presenting content for the phase (e.g. MerrillQuadrantEnum.RULE, {@link generated.course.Recall.PresentSurvey} )
         * @param concepts the concepts this phase should cover. Can't be null or empty.
         */
        public AbstractCDTCourseObject(String courseObjectName, Serializable courseObject, List<String> concepts){
            super(courseObjectName);
            
            setConcepts(concepts);
            setCourseObject(courseObject);
        }

        /**
         * Return the list of concepts this phase should cover.
         * 
         * @return won't be null or empty.
         */
        public List<String> getConcepts(){
            return concepts;
        }
        
        /**
         * Set the concepts to cover.
         * 
         * @param concepts the concepts this phase should cover. Can't be null or empty.
         */
        public void setConcepts(List<String> concepts){
            
            if(concepts == null || concepts.isEmpty()){
                throw new IllegalArgumentException("The concept list must contain at least 1 value.");
            }
            
            this.concepts = concepts;
        }
        
        /**
         * Set/Replace the remediation map.
         * 
         * @param remediationMap map of assessed concept name to a descending prioritized list of remediation types.
         * The map can't have null key strings, empty key strings, null lists, empty lists.
         */
        public void setConceptRemediationMap(Map<String, List<AbstractRemediationConcept>> remediationMap){
            
            if(remediationMap == null){
                throw new IllegalArgumentException("The remediation map can't be null.");
            }
            
            //check the map
            //i. no null key strings
            //ii. no empty key strings
            //iii. no null lists
            //iv. no empty lists
            for(String concept : remediationMap.keySet()){
                
                List<AbstractRemediationConcept> prioritizedList = remediationMap.get(concept);
                
                if(concept == null || concept.isEmpty()){
                    throw new IllegalArgumentException("Found a null or empty concept in the remediation map.");
                }else if(prioritizedList == null || prioritizedList.isEmpty()){
                    throw new IllegalArgumentException("The prioritized list for concept '"+concept+"' can't be null or empty.");
                }
            }
            
            this.conceptRemediationPriorityMap = remediationMap;
        }

        /**
         * Set the descending prioritized list of remediation for the concept.
         * 
         * @param concept an assessed concept name. Can't be null or empty.
         * @param prioritizedList descending prioritized list of remediation for the concept.  Can't be null or empty.
         */
        public void setConceptRemediation(String concept, List<AbstractRemediationConcept> prioritizedList){
            
            if(concept == null || concept.isEmpty()){
                throw new IllegalArgumentException("The concept can't be null or empty.");
            }else if(prioritizedList == null || prioritizedList.isEmpty()){
                throw new IllegalArgumentException("The prioritized list can't be null or empty.");
            }
            
            conceptRemediationPriorityMap.put(concept, prioritizedList);
        }
        
        /**
         * Return the map of assessed concept name to a descending prioritized list of remediation types.
         * 
         * @return can be empty. The map won't have null key strings, empty key strings, null lists, empty lists.
         */
        public Map<String, List<AbstractRemediationConcept>> getRemediationMap(){
            return conceptRemediationPriorityMap;
        }

        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            
            sb.append("concepts = {");
            for(String concept : concepts){
                sb.append(" ").append(concept);
                if(conceptRemediationPriorityMap.containsKey(concept)){
                    
                    sb.append(" : {");
                    for(AbstractRemediationConcept remediation : conceptRemediationPriorityMap.get(concept)){
                        sb.append("  ").append(remediation).append(",\n");
                    }
                    sb.append("}");
                }
                
                sb.append(",\n");
            }
            sb.append("}");
            
            return sb.toString();
        }
    }

}
