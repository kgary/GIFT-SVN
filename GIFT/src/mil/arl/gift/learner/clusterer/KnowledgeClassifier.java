/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.IntermediateConceptAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.state.ConceptStateRecord;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.LearnerStateAttributeCollection;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class is responsible for classifying the assessment it receives in order
 * to determine what the current learner state attribute value is for knowledge.
 * 
 * @author mhoffman
 *
 */
public class KnowledgeClassifier extends AbstractClassifier {

	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KnowledgeClassifier.class);

    /** the learner's engagement state. Null until some data is received by this class that can populate the state object */
	private LearnerStateAttributeCollection state;
	
	/**
	 * Mapping of course concept to all assessments associated to it
	 * The reason for this is that a course could have a concept hierarchy and the LRS can provide a flattened
	 * list of course concepts.  From course to course, additional concept hierarchies can be brought in as well.
	 * When a new concept assessment happens, all assessments for that concept should be updated to match.
	 * In the future, this might not be needed if a better approach to merging a unique concept is implemented.
	 * E.g.
	 * Knowledge
	 * - course concepts
	 * -- all concepts
	 * --- pressed button 1 (from course concepts, updated by things like Question Bank in a course)
	 * - pressed button 1 (from LRS xAPI statements when starting a course)
	 */
	private Map<String, Set<LearnerStateAttribute>> courseConceptToAttributes = new HashMap<>();
	
	/** 
	 * used to determine if an incoming task assessment is for a new task or existing 
	 * This assumes that each unique task will have a different UUID even if the task is the same name
	 * across DKFs.  Currently this assumption holds true based on how the Domain module assigns UUIDs to tasks.
	 */
	private List<UUID> taskIds = new ArrayList<UUID>();
	
	/** the last assessment provided to this classifier */
	private AbstractAssessment assessment;
	
	/** the attribute being classified by this classifier */
    private static final LearnerStateAttributeNameEnum STATE_ATTRIBUTE = LearnerStateAttributeNameEnum.KNOWLEDGE; 
    
    /** the enum for low level knowledge */
    private static final AbstractEnum LOW_VALUE = ExpertiseLevelEnum.NOVICE;
    
    /** the enum for middle level knowledge */
    private static final AbstractEnum MED_VALUE = ExpertiseLevelEnum.JOURNEYMAN;
    
    /** the enum for high level knowledge */
    private static final AbstractEnum HIGH_VALUE = ExpertiseLevelEnum.EXPERT;
    
    /** the enum for an unknown level of knowledge */
    private static final AbstractEnum UNKNOWN_VALUE = ExpertiseLevelEnum.UNKNOWN;
	
	/**
	 * Return the  learner {@value #STATE_ATTRIBUTE} state
	 * 
	 * @return the state information for this classifier
	 */
	@Override
	public LearnerStateAttribute getState(){
		return state;
	}
	
	/**
	 * Return the current {@value #STATE_ATTRIBUTE} state data
	 * 
	 * @return the last {@link AbstractAssessment} provided to this classifier
	 */
	@Override
	public Object getCurrentData(){
		return assessment;
	}
	
	@Override
	public boolean updateState(TaskAssessment data){
		
		boolean updated = false;
				
        if(logger.isInfoEnabled()){
            logger.info("Received a Performance Assessment update of "+data);
        }
        
        if(state == null){
            //build new
            state = new LearnerStateAttributeCollection(STATE_ATTRIBUTE);           
        }
        
        if(!taskIds.contains(data.getCourseNodeId())){
            //first time seeing this task, {update OR add} this task and its concepts to the collection
            
            AbstractEnum taskKnowledge = getLevel(((AbstractAssessment)data).getAssessmentLevel());
            LearnerStateAttributeCollection taskCollection = new LearnerStateAttributeCollection(STATE_ATTRIBUTE);
            taskCollection.setShortTerm(taskKnowledge);
            
            if(state.getAttributes().containsKey(data.getName())){
            	//update existing task hierarchy to possible new hierarchy (this is when both have the same task name value)
            	state.updateAttribute(data.getName(), taskCollection);
            }else{
            	//first instance of a task with this name, so add it 
            	state.addAttribute(data.getName(), taskCollection);
            }            

            //either way, the concept hierarchy under this task is most likely new (since the task is a new instance)
            for(ConceptAssessment cAss : data.getConceptAssessments()){                
                handleConcepts(taskCollection, cAss);
            }
            
            taskIds.add(data.getCourseNodeId());
            
            updated = true;
            
            if(logger.isInfoEnabled()){
                logger.info("The concept assessment update caused a change in the current learner state to "+state);
            }
            
        }else{
            //update this task's state, if needed
            
            LearnerStateAttributeCollection taskCollection = (LearnerStateAttributeCollection) state.getAttributes().get(data.getName());
            AbstractEnum taskKnowledge = getLevel(((AbstractAssessment)data).getAssessmentLevel());
            if(taskCollection.getShortTerm() != taskKnowledge){
                //update the task
                taskCollection.setShortTerm(taskKnowledge);                
                
                updated = true;
            }
            
            //check the task's descendant concepts
            for(ConceptAssessment cAss : data.getConceptAssessments()){                
                updated |= updateConcepts(taskCollection, cAss);
            }
            
        }

        assessment = data;
		
		return updated;
	}
	
    @Override
    public boolean updateState(AbstractScale scaleState) {
        
        if (logger.isInfoEnabled()) {
            logger.info("Received scale state of "+scaleState);
        }
        
        boolean updated = false;
        if(STATE_ATTRIBUTE.equals(scaleState.getAttribute()) && scaleState instanceof ConceptStateRecord){
            
            if(state == null){
                //build new
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
            
            // find the course concept's top level state attribute
            LearnerStateAttribute conceptState = state.getAttributes().get(record.getCourseConcept());
            if(conceptState == null){
                // it doesn't exist so create it
                conceptState = createLearnerStateAttribute(record.getCourseConcept(), convertedLevel);
                state.addAttribute(record.getCourseConcept(), conceptState);
                
                // update all references to the course concept known to this classifier
                updateCourseConcept(record.getCourseConcept(), conceptState, true);   
                
                updated = true;
            }else if(!conceptState.getShortTerm().equals(convertedLevel)){
                //need to update state for this node
                
                conceptState.setShortTerm(convertedLevel);
                
                // update all references to the course concept known to this classifier
                updateCourseConcept(record.getCourseConcept(), conceptState, false);   
                
                if(logger.isInfoEnabled()){
                    logger.info("Updated the "+STATE_ATTRIBUTE+" value of "+record.getCourseConcept()+" to "+convertedLevel+".");
                }
                
                updated = true;
            }

        }
        
        return updated;
    }
    
    /**
     * Add a course concept and it's associated assessment instance to the collection of other assessments
     * already mapped to that course concept.
     * @param courseConcept the course concept to map the new assessment instance, can't be null or empty
     * @param updatedConceptState the new assessment to map to the course concept, can't be null
     */
    private void addCourseConcept(String courseConcept, LearnerStateAttribute updatedConceptState) {
        
        if(StringUtils.isBlank(courseConcept)) {
            throw new IllegalArgumentException("The course concept is null or empty");
        }else if(updatedConceptState == null) {
            throw new IllegalArgumentException("The update concept state is null");
        }
        
        Set<LearnerStateAttribute> attributes = courseConceptToAttributes.get(courseConcept);
        if(attributes == null) {
            attributes = new HashSet<>();
            courseConceptToAttributes.put(courseConcept, attributes);
        }
        
        attributes.add(updatedConceptState);
    }
    
    /**
     * Create a new learner state attribute assessment using the attributes provided.
     * @param courseConcept the course concept needing a new learner state attribute to be created.
     * @param providedShortTerm the recommended short term assessment to use in this new learner state attribute.  If 
     * ExpertiseLevelEnum.UNKNOWN, use the current short term assessment, if available, for that course concept.
     * @return the new Learner State Attribute to be associated with that course conept.
     */
    private LearnerStateAttribute createLearnerStateAttribute(String courseConcept, AbstractEnum providedShortTerm) {
        
        if(StringUtils.isBlank(courseConcept)) {
            throw new IllegalArgumentException("The course concept is null or empty");
        }
        
        if(providedShortTerm == null || providedShortTerm.equals(ExpertiseLevelEnum.UNKNOWN)) {
            // see if it can be replaced with the current value for this concept
            
            Set<LearnerStateAttribute> attributes = courseConceptToAttributes.get(courseConcept);
            if(CollectionUtils.isNotEmpty(attributes)) {
                LearnerStateAttribute attribute = attributes.iterator().next();  // random pick - should all have the same assessment values anyway
                providedShortTerm = attribute.getShortTerm();
            }
        }
        
        return new LearnerStateAttribute(STATE_ATTRIBUTE, providedShortTerm, UNKNOWN_VALUE, UNKNOWN_VALUE);
    }
    
    /**
     * Update an existing assessment for a course concept with the learner state attribute value provided.  Apply
     * this new assessment value to all other assessments map to that course concept.
     * @param courseConcept the course concept whose assessment is being updated, if null or empty, nothing happens.
     * @param updatedConceptState the updated assessment to map to the course concept, if null, nothing happens.
     * @param newConceptState true if this course concept assessment is also a new assessment instance that needs
     * to be tracked/mapped to the course concept mapping in this class.
     */
    private void updateCourseConcept(String courseConcept, LearnerStateAttribute updatedConceptState, boolean newConceptState) {
        
        if(StringUtils.isBlank(courseConcept)) {
            return;
        }else if(updatedConceptState == null) {
            return;
        }
        
        if(!courseConceptToAttributes.containsKey(courseConcept)) {
            // this is the first instance of this course concept
            
            addCourseConcept(courseConcept, updatedConceptState);
            return;
        }
        
        for(LearnerStateAttribute attribute : courseConceptToAttributes.get(courseConcept)) {
            attribute.setShortTerm(updatedConceptState.getShortTerm());
            attribute.setLongTerm(updatedConceptState.getLongTerm());
            attribute.setPredicted(updatedConceptState.getPredicted());
        }
        
        if(newConceptState) {
            // this assessment is a new instance for this course concept, add it to the mapping for this course concept
            addCourseConcept(courseConcept, updatedConceptState);
        }
    }
	
	/**
	 * Analyzes the concept assessment for performance in order to update this classifier's learner state attribute collection 
	 * of cognitive knowledge values.  This method will return true if the assessment resulted in updating the cognitive knowledge 
	 * value.
	 * 
	 * @param parentCollection either a task or intermediate concept cognitive knowledge learner state representation that maybe
	 * updated by the provided concept assessment.
	 * @param conceptAssessment a concept assessment from a performance assessment object to analyze
	 * @return boolean whether the assessment updated the parent collection cognitive knowledge state value.
	 */
	private boolean updateConcepts(LearnerStateAttributeCollection parentCollection, ConceptAssessment conceptAssessment){
	    
	    boolean updated = false;
	    
	    LearnerStateAttribute conceptAttribute = parentCollection.getAttributes().get(conceptAssessment.getName()); 
	    if(conceptAttribute instanceof LearnerStateAttributeCollection){
	        //must be an intermediate concept
	        
            AbstractEnum conceptKnowledge = getLevel(((AbstractAssessment)conceptAssessment).getAssessmentLevel());
            if(conceptAttribute.getShortTerm() != conceptKnowledge){
                //update the intermediate concept
                conceptAttribute.setShortTerm(conceptKnowledge);                
                
                updated |= true;
            }
            
            // check the intermediate concept's descendant concepts
            for(ConceptAssessment cAss : ((IntermediateConceptAssessment)conceptAssessment).getConceptAssessments()){                
                updated |= updateConcepts(((LearnerStateAttributeCollection)conceptAttribute), cAss);
            }
	        
	    }else{
            AbstractEnum conceptKnowledge = getLevel(((AbstractAssessment)conceptAssessment).getAssessmentLevel());
            if(conceptAttribute.getShortTerm() != conceptKnowledge){
                //update the concept
                conceptAttribute.setShortTerm(conceptKnowledge);
                
                // update all references to the course concept known to this classifier
                updateCourseConcept(conceptAssessment.getName(), conceptAttribute, false); 
                
                updated |= true;
            }
	    }
	    
	    return updated;
	}
	
	/**
	 * Create cognitive knowledge learner state attribute objects based on the concept assessment provided.
	 * 
	 * @param parentCollection where to add the newly created cognitive knowledge learner state attribute objects.
	 * @param conceptAssessment the assessment to analyze for concept map hierarchy and assessment values
	 */
	private void handleConcepts(LearnerStateAttributeCollection parentCollection, ConceptAssessment conceptAssessment){
	    
	    if(conceptAssessment instanceof IntermediateConceptAssessment){
            
	        //create new collection for this subconcept, add new collection to parent 
            AbstractEnum iConceptKnowledge = getLevel(((IntermediateConceptAssessment)conceptAssessment).getAssessmentLevel());
            LearnerStateAttributeCollection iConceptState = new LearnerStateAttributeCollection(STATE_ATTRIBUTE);
            iConceptState.setShortTerm(iConceptKnowledge);
            parentCollection.addAttribute(conceptAssessment.getName(), iConceptState);            
            
            //add descendants to this collection
            for(ConceptAssessment cAss : ((IntermediateConceptAssessment)conceptAssessment).getConceptAssessments()){
                handleConcepts(iConceptState, cAss);
            }            
	        
	    }else{
            AbstractEnum conceptKnowledge = getLevel(((AbstractAssessment)conceptAssessment).getAssessmentLevel());
            LearnerStateAttribute conceptState = createLearnerStateAttribute(conceptAssessment.getName(), conceptKnowledge);
            parentCollection.addAttribute(conceptAssessment.getName(), conceptState);
            
            // update all references to the course concept known to this classifier
            updateCourseConcept(conceptAssessment.getName(), conceptState, true);  
	    }
	}
	
	/**
	 * Return an enumerated value for the performance assessment result provided.
	 * 
	 * @param currentAssessment a performance assessment result to translate into a different enumeration
	 * @return AbstractEnum the resulting translated enumeration.
	 */
	private AbstractEnum getLevel(AssessmentLevelEnum currentAssessment){
	    
        AbstractEnum uLevel = UNKNOWN_VALUE;
        if(currentAssessment == AssessmentLevelEnum.ABOVE_EXPECTATION){
            uLevel = HIGH_VALUE;
        }else if(currentAssessment == AssessmentLevelEnum.AT_EXPECTATION){
            uLevel = MED_VALUE;
        }else if(currentAssessment != AssessmentLevelEnum.UNKNOWN){
            uLevel = LOW_VALUE;
        }
        
        return uLevel;
	}
	
    
    @Override
    public LearnerStateAttributeNameEnum getAttribute() {
        return STATE_ATTRIBUTE;
    }
	
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[KnowledgeClassifier:");
	    sb.append(" state = ").append(getState());
	    sb.append(", previous assessment = ").append(getCurrentData());
	    sb.append(", ").append(super.toString());
	    sb.append("]");
		return sb.toString();
	}
	
}
