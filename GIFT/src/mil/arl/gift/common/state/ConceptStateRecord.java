/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.util.StringUtils;

/**
 * Contains the record of a course concept.  This can be used to initialize learner state from a long
 * term learner record on this course concept.  E.g. Knowledge on 'Concept A' is 'Novice'.
 * 
 * @author mhoffman
 *
 */
public class ConceptStateRecord extends AbstractScale {
    
    /** unique name of a course concept this record describes state information about */
    private final String courseConcept;

    /**
     * Set attributes 
     * @param courseConcept unique name of a course concept this record describes state information about.  Can't be empty.
     * @param attribute a learner state attribute enumeration that provides the type of label this record is describing (e.g. Skill)
     * @param attributeLevel a valid level of the attribute as defined by the possible values supported by that attribute enum. E.g.
     * Skill uses ExpertiseLevelEnum as values, therefor for Skill a valid attributeLevel is ExpertiseLevelEnum.Expert.
     */
    public ConceptStateRecord(String courseConcept, LearnerStateAttributeNameEnum attribute, AbstractEnum attributeLevel) {
        super(attribute, 0.0);

        if(StringUtils.isBlank(courseConcept)){
            throw new IllegalArgumentException("The course concept is null or empty");
        }
        
        if(attributeLevel == null){
            throw new IllegalArgumentException("The attribute level is null");
        }else if(!attribute.getAttributeAuthoredValues().contains(attributeLevel)){
            throw new IllegalArgumentException("The attribute level '"+attributeLevel+"' is not a valid level of "+attribute.getDisplayName());
        }
        
        this.courseConcept = courseConcept;
        this.value = attributeLevel;        
    }
    
    /**
     * Return the unique name of a course concept this record describes state information about. 
     * @return won't be empty.
     */
    public String getCourseConcept(){
        return courseConcept;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ConceptStateRecord:  concept = ");
        builder.append(courseConcept);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }

    
}
