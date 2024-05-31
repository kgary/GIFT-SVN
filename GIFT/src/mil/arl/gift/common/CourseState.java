/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MerrillQuadrantEnum;
import mil.arl.gift.common.util.CollectionUtils;

/**
 * This class contains information about the current state of the course, including but not limited too
 * where in the course the user is.
 * 
 * @author mhoffman
 *
 */
public class CourseState {
    
    /** the type of the next (scheduled - because the Pedagogy might change the flow) transition in the course   */
    private String nextTransitionImplementation;
    
    /** (optional) the next merrill quadrant in the course flow */
    private MerrillQuadrantEnum nextQuadrant;
    
    /** (optional) information about learner state that is needed in the next course object in the course 
     * This can be used to help decide if the next course object should be skipped.  
     * E.g. does the learner have Grit learner state already so he can skip a Grit survey.
     * E.g. does the learner already have knowledge on a course concept so he can skip a question bank pre-test course object.
     */
    private RequiredLearnerStateAttributes requiredLearnerStateAttributes;
    
    /** (optional) specifies (in milliseconds) how recent pre-existing learner state must be in order to be considered */
    private Map<LearnerStateAttributeNameEnum, Serializable> learnerStateShelfLife = null;
    
    /**
     * enumerated states for expandable course objects.
     * 
     * @author mhoffman
     *
     */
    public enum ExpandableCourseObjectStateEnum{
        ADAPTIVE_COURSEFLOW, // indicates the learner is currently in an adaptive course flow course object
        TRAINING_APPLICATION, // indicates the learner is currently in a training app course object
        NONE // indicates the learner is not in a training app, nor adaptive course flow course object
    }
    
    /** (optional) indicates whether the current location in the authored course is a course object that can 
     * be expanded to multiple course objects (e.g. adaptive course flow, training app w/ remediation) */
    private ExpandableCourseObjectStateEnum expandableCourseObjectStateEnum = ExpandableCourseObjectStateEnum.NONE;
    
    /**
     * Default constructor
     * 
     * @param nextTransitionImplementation the class that contains the information for the next course transition
     *                                     the domain module wants to execute.  
     *         Examples: generated.course.PresentSurvey, generated.course.Guidance, generated.course.Recall.PresentSurvey
     */
    public CourseState(String nextTransitionImplementation){
        
        if(nextTransitionImplementation == null){
            throw new IllegalArgumentException("The next transition implementation can't be null.");
        }
        
        this.nextTransitionImplementation = nextTransitionImplementation;
    }
    
    /**
     * Return the type of the next (scheduled - because the Pedagogy might change the flow) transition in the course 
     * 
     * @return e.g. 'generated.course.TrainingApplication'
     */
    public String getNextTransitionImplementation(){
        return nextTransitionImplementation;
    }
    
    /**
     * Set the quadrant the user is scheduled to transition into in the flow of the course.
     * 
     * @param nextQuadrant the next merrill quadrant in the course flow.  Can be null.
     */
    public void setNextQuadrant(MerrillQuadrantEnum nextQuadrant){
        this.nextQuadrant = nextQuadrant;
    }
    
    /**
     * Return the quadrant the user is scheduled to transition into in the flow of the course.
     * 
     * @return MerrillQuadrantEnum - can be null
     */
    public MerrillQuadrantEnum getNextQuadrant(){
        return nextQuadrant;
    }

    /**
     * Returns the information about learner state that is needed in the next course object in the course 
     * This can be used to help decide if the next course object should be skipped.  
     * E.g. does the learner have Grit learner state already so he can skip a Grit survey.
     * E.g. does the learner already have knowledge on a course concept so he can skip a question bank pre-test course object.
     * 
     * @return learner state needed in the next course object. Can be null
     *         if there are no learner state attributes needed in the next course object.
     */
    public RequiredLearnerStateAttributes getRequiredLearnerStateAttributes() {
        return requiredLearnerStateAttributes;
    }

    /**
     * Sets the information about learner state that is needed in the next course object in the course 
     * This can be used to help decide if the next course object should be skipped.  
     * E.g. does the learner have Grit learner state already so he can skip a Grit survey.
     * E.g. does the learner already have knowledge on a course concept so he can skip a question bank pre-test course object.
     * 
     * @param requiredLearnerStateAttributes learner state needed in the next course object.  Can be null.
     */
    public void setRequiredLearnerStateAttributes(RequiredLearnerStateAttributes requiredLearnerStateAttributes) {
        this.requiredLearnerStateAttributes = requiredLearnerStateAttributes;
    }

    /**
     * Getter for the learnerStateShelfLife
     * @return the value of the learnerStateShelfLife, will never be null
     */
    public Map<LearnerStateAttributeNameEnum, Serializable> getLearnerStateShelfLife() {
        return learnerStateShelfLife != null ? learnerStateShelfLife : (learnerStateShelfLife = new HashMap<>());
    }

    /**
     * Setter for the learnerStateShelfLife
     * @param learnerStateShelfLife the new value of the learnerStateShelfLife, can be null
     */
    public void setLearnerStateShelfLife(Map<LearnerStateAttributeNameEnum, Serializable> learnerStateShelfLife) {
        this.learnerStateShelfLife = learnerStateShelfLife;
    }

    /**
     * Indicates whether the current location in the authored course is a course object that can 
     * be expanded to multiple course objects (e.g. adaptive course flow, training app w/ remediation)
     * @return default if ExpandableCourseObjectStateEnum.NONE
     */
    public ExpandableCourseObjectStateEnum getExpandableCourseObjectState() {
        return expandableCourseObjectStateEnum;
    }

    /**
     * Set whether the current location in the authored course is a course object that can 
     * be expanded to multiple course objects (e.g. adaptive course flow, training app w/ remediation)
     * @param expandableCourseObjectStateEnum if the course state represents an experience in an expandable course object type
     */
    public void setExpandableCourseObjectState(ExpandableCourseObjectStateEnum expandableCourseObjectStateEnum) {
        this.expandableCourseObjectStateEnum = expandableCourseObjectStateEnum;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CourseState: ");
        sb.append("nextTransitionImpl = ").append(getNextTransitionImplementation());
        sb.append(", nextQuadrant = ").append(getNextQuadrant());
        sb.append(", requiredLearnerStateAttributes = ").append(getRequiredLearnerStateAttributes());
        sb.append(", expandableCourseObjectStateEnum = ").append(getExpandableCourseObjectState());
        sb.append("]");
        return sb.toString();
    }
    
    /**
     * Used to indicate whether the next course object requires certain learner state to exist in order for that
     * course object to potentially be skipped in the course flow.
     * @author mhoffman
     *
     */
    public static class RequiredLearnerStateAttributes{
        
        /**
         * contains learner state attributes that are required for the next
         * course object.  The values are optional and can contain additional, more granular information such 
         * as a course concept must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute.
         * Can be null or empty.
         */
        private Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap;
        
        /**
         * Set attribute
         * @param learnerStateAttributesMap contains learner state attributes that are required for the next
         * course object.  The values are optional and can contain additional, more granular information such 
         * as a course concept must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute.
         * Can be null or empty.
         */
        public RequiredLearnerStateAttributes(Map<LearnerStateAttributeNameEnum, AttributeValues> learnerStateAttributesMap){
            this.learnerStateAttributesMap = learnerStateAttributesMap;
        }
        
        /**
         * Return the learner state attributes that are required for the next
         * course object.  The values are optional and can contain additional, more granular information such 
         * as a course concept must have 'Journeyman' Expertise level enum for 'Knowledge' learner state attribute.
         * @return Can be null or empty.
         */
        public Map<LearnerStateAttributeNameEnum, AttributeValues> getLearnerStateAttributesMap(){
            return learnerStateAttributesMap;
        }
        
        /**
         * Base interface to support different types of implementations in the future.
         * @author mhoffman
         *
         */
        public static interface AttributeValues{
            // nothing yet            
        }
        
        /**
         * Contains course concept specific attributes.
         * 
         * @author mhoffman
         *
         */
        public static class ConceptAttributeValues implements AttributeValues{
            
            /**
             * Mapping of unique course concept name to an optional
             * required expertise level associated with that concept.
             */
            private Map<String, ExpertiseLevelEnum> conceptExpertiseLevel;
            
            /**
             * Set attribute.
             * @param conceptExpertiseLevel - Mapping of unique course concept name to an optional
             * required expertise level associated with that concept.  The expertise level is correlated
             * to the learner state attribute name enum this ConceptAttributeValues is mapped too.
             * Can't be null or empty.
             */
            public ConceptAttributeValues(Map<String, ExpertiseLevelEnum> conceptExpertiseLevel){
                if(CollectionUtils.isEmpty(conceptExpertiseLevel)){
                    throw new IllegalArgumentException("The conceptExpertiseLevel can't be null or empty");
                }
                this.conceptExpertiseLevel= conceptExpertiseLevel;
            }
            
            /**
             * Return the mapping of unique course concept name to an optional
             * required expertise level associated with that concept.
             * @return won't be null or empty but values in the map can be null.
             */
            public Map<String, ExpertiseLevelEnum> getConceptExpertiseLevel(){
                return this.conceptExpertiseLevel;
            }
            
            @Override
            public String toString(){
                StringBuffer sb = new StringBuffer("[ConceptAttributeValues:\n{");
                for(String conceptName : conceptExpertiseLevel.keySet()){
                    sb.append(conceptName)
                    .append(" : ")
                    .append(conceptExpertiseLevel.get(conceptName) == null ? "null" : conceptExpertiseLevel.get(conceptName).getName())
                    .append(",\n");
                }
                sb.append("}]");
                return sb.toString();
            }
        }
        
        @Override
        public String toString(){
            StringBuffer sb = new StringBuffer("[RequiredLearnerStateAttributes: ");
            if(learnerStateAttributesMap != null){
                sb.append("learnerStateAttributesMap = {");
                for(LearnerStateAttributeNameEnum attrEnum : learnerStateAttributesMap.keySet()){
                    sb.append(attrEnum.getName());
                    AttributeValues attributeValues = learnerStateAttributesMap.get(attrEnum);
                    if(attributeValues != null){
                        sb.append(" -\n").append(attributeValues);
                    }
                    sb.append("\n");
                }
                sb.append("}");
            }
            sb.append("]");
            return sb.toString();
        }
        
    }
}
