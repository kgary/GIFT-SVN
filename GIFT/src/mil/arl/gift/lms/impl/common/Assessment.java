/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This class represents an assessment made on a course concept.
 * @author cnucci
 *
 */
public class Assessment{
    
    /**
     * The name of the concept
     */
    private String concept;
    
    /**
     * The assessment of the concept
     */
    private AssessmentLevelEnum assessmentLevel;
    
    /**
     * Creates a new Assessment
     * @param concept  The name of the concept
     * @param assessmentLevel  The assessment of the concept
     */
    public Assessment(String concept, AssessmentLevelEnum assessmentLevel) {
        this.concept = concept;
        this.assessmentLevel = assessmentLevel;
    }
    
    /**
     * Gets the concept name
     * @return String - The name of the concept
     */
    public String getConcept() {
        return concept;
    }

    /**
     * Gets the assessment of the concept
     * @return AssessmentLevelEnum - The assessment of the concept
     */
    public AssessmentLevelEnum getAssessmentLevel() {
        return assessmentLevel;
    }
}
