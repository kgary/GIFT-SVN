/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.IntermediateConceptAssessment;
import mil.arl.gift.common.LMSCourseRecord;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.course.CourseConceptsUtil;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.score.AbstractScoreNode;
import mil.arl.gift.common.score.GradedScoreNode;
import mil.arl.gift.common.state.ConceptStateRecord;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.survey.score.AbstractScale;

/**
 * This class is responsible for classifying the assessment it receives in order
 * to determine what the current learner state attribute value is for skill.
 * 
 * @author mhoffman
 *
 */
public class SkillClassifier extends AbstractClassifier {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(SkillClassifier.class);

    /** the learner's engagement state. Null until some data is received by this class that can populate the state object */
	private LearnerStateAttributeCollection state;
	
	/** the last course record provided to this classifier */
	private LMSCourseRecord courseRecord;
	
    private static final LearnerStateAttributeNameEnum STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.SKILL; 
    
    private static final AbstractEnum LOW_VALUE = ExpertiseLevelEnum.NOVICE;
    private static final AbstractEnum MED_VALUE = ExpertiseLevelEnum.JOURNEYMAN;
    private static final AbstractEnum HIGH_VALUE = ExpertiseLevelEnum.EXPERT;
    private static final AbstractEnum UKNOWN_VALUE = ExpertiseLevelEnum.UNKNOWN;
    
    /** 
     * contains course concepts retrieved from a performance assessment (task assessment) 
     * with the unique course concepts task name 
     */
    private Set<String> courseConcepts = new HashSet<>();
	
	/**
	 * Return the  learner understanding state
	 * 
	 * @return LearnerState
	 */
	@Override
	public LearnerStateAttribute getState(){
		return state;
	}
	
    @Override
    public boolean updateState(LMSCourseRecord courseRecord){
        
        if (logger.isInfoEnabled()) {
            logger.info("Received a Course record of "+courseRecord);
        }        
        
        boolean updated = updateState(courseRecord.getRoot());
        
        //update latest course record object
        this.courseRecord = courseRecord;
        
        return updated;
    }
    
    @Override
    public boolean updateState(AbstractScale scaleState) {
        
        if (logger.isInfoEnabled()) {
            logger.info("Received scale state of "+scaleState);
        }
        
        if(STATE_ATTRIBUTE.equals(scaleState.getAttribute()) && scaleState instanceof ConceptStateRecord){
            
            if (state == null) {
                state = new LearnerStateAttributeCollection(STATE_ATTRIBUTE);
            }
            
            ConceptStateRecord record = (ConceptStateRecord)scaleState;
            AbstractEnum convertedLevel = null;
            if(record.getValue() instanceof ExpertiseLevelEnum){
                convertedLevel = record.getValue();
            }else if(record.getValue() instanceof AssessmentLevelEnum){
                convertedLevel = getLevel((AssessmentLevelEnum)record.getValue());
            }else{
                // found unhandled value
                if(logger.isInfoEnabled()){
                    logger.info("Found unhandled state update enum of "+record.getValue()+" when updating "+STATE_ATTRIBUTE+".  Not using this update to change state.");
                }
                return false;
            }
            
            LearnerStateAttribute conceptAttribute = state.getConceptExpertiseLevel(record.getCourseConcept().toLowerCase());
            if(!courseConcepts.contains(record.getCourseConcept())){
                courseConcepts.add(record.getCourseConcept().toLowerCase());
                
                conceptAttribute = new LearnerStateAttribute(STATE_ATTRIBUTE, convertedLevel, UKNOWN_VALUE, UKNOWN_VALUE);
                state.addAttribute(record.getCourseConcept(), conceptAttribute);
                
                return true;
            }else if(conceptAttribute != null){
                
                //check for update of existing node
                
                if(!conceptAttribute.getShortTerm().equals(convertedLevel)){
                    //need to update state for this node
                    
                    conceptAttribute.setShortTerm(convertedLevel);
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Updated the "+STATE_ATTRIBUTE+" value of "+record.getCourseConcept()+" to "+convertedLevel+".");
                    }
                    
                    return true;
                }
            }
        }
        
        return false;
    }
    
    @Override
    public boolean updateState(GradedScoreNode gradedScoreNode) {

        if (logger.isInfoEnabled()) {
            logger.info("Received a GradedScoreNode: " + gradedScoreNode);
        }

        if (state == null) {
            state = new LearnerStateAttributeCollection(STATE_ATTRIBUTE);
        }
        
        return handleScoreNode(gradedScoreNode);
    }
    
    @Override
    public boolean updateState(TaskAssessment data){
        
        if(courseConcepts.isEmpty()){
        
            //need to capture course concepts
            if(data.getName().equals(CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME)){             
                                    
                for(ConceptAssessment conceptAssessment : data.getConceptAssessments()){                      
                    addCourseConcepts(conceptAssessment);
                }               
                
            }
        }
        
        return false;
        
    }
    
    /**
     * Add the provided concept assessment concept name and any subconcept assessment concept names
     * found under this concept to the set known to this class.
     * 
     * @param conceptAssessment contain a concept assessment and possibly subconcept assessments
     */
    private void addCourseConcepts(ConceptAssessment conceptAssessment){
        
        courseConcepts.add(conceptAssessment.getName().toLowerCase());
        
        if(conceptAssessment instanceof IntermediateConceptAssessment){
            
            IntermediateConceptAssessment iConceptAssessment = (IntermediateConceptAssessment)conceptAssessment;
            for(ConceptAssessment subConceptAssessment : iConceptAssessment.getConceptAssessments()){ 
                addCourseConcepts(subConceptAssessment);
            }
        }
    }
    
    /**
     * Initialize the list of course concepts with the ones provided. 
     * 
     * @param concepts the course concepts
     */
    public void initCourseConcepts(List<String> concepts, GradedScoreNode gradedScoreNode) {

        if (concepts != null && CourseConceptsUtil.COURSE_CONCEPTS_CONCEPT_NAME_SKILL.equals(gradedScoreNode.getName())) {

            for (String concept : concepts) {
                courseConcepts.add(concept.toLowerCase());
            }
        }
    }
    
    /**
     * Process the score node and any of it's descendant score nodes by translating the score
     * into a skill state attribute value for each node.
     * 
     * @param node the score node to process
     * @return boolean whether any of the score nodes have changed since the last time a course record was provided
     */
    private boolean handleScoreNode(GradedScoreNode node){
        
        boolean updated = false;
        
        String name = node.getName();
        AbstractEnum gradeLevel = getLevel(node.getAssessment());
        
        LearnerStateAttribute conceptAttribute = state.getConceptExpertiseLevel(name);
        if(conceptAttribute == null){
            //the node name is new to the skill learner state object managed by this class
            
            //only update skill level for course concepts, other concepts are needed for real-time assessment (dkf) logic (i.e. task hierarchy)
            if(courseConcepts.contains(name.toLowerCase())){
                updated = true;           
                
                if(logger.isInfoEnabled()){
                    logger.info("Found a new course record node named "+name+".");
                }
                
                LearnerStateAttribute attribute = new LearnerStateAttribute(STATE_ATTRIBUTE, gradeLevel, UKNOWN_VALUE, UKNOWN_VALUE);
                state.addAttribute(name, attribute);
            }
        }else{
            //check for update of existing node
            
            if(conceptAttribute.getShortTerm() != gradeLevel){
                //need to update state for this node
                
                conceptAttribute.setShortTerm(gradeLevel);
                updated = true;
                
                if(logger.isInfoEnabled()){
                    logger.info("Updated the "+STATE_ATTRIBUTE+" value of "+name+" to "+gradeLevel+".");
                }
            }
        }
        
        //handle child nodes
        for(AbstractScoreNode childNode : node.getChildren()){
            
            if(childNode instanceof GradedScoreNode){
                updated |= handleScoreNode((GradedScoreNode) childNode);
            }
        }
        
        return updated;
    }
    
    /**
     * Return an enumerated value for the performance assessment result provided.
     * 
     * @param currentAssessment a performance assessment result to translate into a different enumeration
     * @return AbstractEnum the resulting translated enumeration.
     */
    private AbstractEnum getLevel(AssessmentLevelEnum currentAssessment){
        
        AbstractEnum uLevel = UKNOWN_VALUE;
        if(currentAssessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
            uLevel = HIGH_VALUE;
        }else if(currentAssessment == AssessmentLevelEnum.AT_EXPECTATION){
            uLevel = MED_VALUE;
        }else if(currentAssessment != AssessmentLevelEnum.UNKNOWN){
            uLevel = LOW_VALUE;
        }
        
        return uLevel;
    }
	
	/**
	 * Return the current skill data
	 * 
	 * @return Object
	 */
	@Override
	public Object getCurrentData(){
		return courseRecord;
	}
    
    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return STATE_ATTRIBUTE;
    }
    
    @Override
    public void domainSessionStarted(){
        // force the course concepts for this classifier to be re-populated,
        // not doing this will prevent new course concepts from being tracked when changing courses
        courseConcepts.clear();
    }
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[SkillClassifier:");
	    sb.append(" state = ").append(getState());
	    sb.append(", previous assessment = ").append(getCurrentData());
	    sb.append(", concepts = ").append(courseConcepts);
	    sb.append(", ").append(super.toString());
	    sb.append("]");
		return sb.toString();
	}
	
}
