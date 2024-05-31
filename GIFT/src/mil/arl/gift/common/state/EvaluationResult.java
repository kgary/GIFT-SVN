/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * Utility class for evaluating entity assessments based on a learner state
 * 
 * @author sharrison
 */
public class EvaluationResult {
    
    /** who is being evaluated */
    private String teamOrgName;

    /** The evaluated assessment level */
    private AssessmentLevelEnum assessmentLevel;

    /** The concepts that were the result of the assessment level */
    private Set<ConceptPerformanceState> conceptPerformances = new HashSet<>();

    /**
     * Default Constructor. Initializes {@link #assessmentLevel} to
     * {@link AssessmentLevelEnum#UNKNOWN}.
     * 
     * @param teamOrgName who is being evaluated here.  Can't be null or empty.
     */
    public EvaluationResult(String teamOrgName) {
        assessmentLevel = AssessmentLevelEnum.UNKNOWN;
        
        if(StringUtils.isBlank(teamOrgName)){
            throw new IllegalArgumentException("The team org name is null or empty");
        }
        
        this.teamOrgName = teamOrgName;
    }
    
    /**
     * Return who is being evaluated.
     * @return a team or team member name from the team organization.  Won't be null or empty.
     */
    public String getTeamOrgName(){
        return teamOrgName;
    }

    /**
     * Retrieve the evaluated assessment level.
     * 
     * @return the evaluated assessment level. Can be null.
     */
    public AssessmentLevelEnum getAssessmentLevel() {
        return assessmentLevel;
    }

    /**
     * Set the evaluated assessment level.
     * 
     * @param assessmentLevel the assessment level to set.
     */
    public void setAssessmentLevel(AssessmentLevelEnum assessmentLevel) {
        this.assessmentLevel = assessmentLevel;
    }

    /**
     * Get the concepts that resulted in the {@link #assessmentLevel}.
     * 
     * @return the set of concepts that caused the {@link #assessmentLevel}.
     *         Will never be null.
     */
    public Set<ConceptPerformanceState> getConceptPerformances() {
        return conceptPerformances;
    }

    /**
     * Check if the new assessment has a higher priority than the currently
     * evaluated assessment.
     *
     * Assessment Priority Level: BELOW > AT > ABOVE > UNKNOWN > null
     * 
     * @param assessmentToTest the assessment to test against the evaluated
     *        assessment.
     * @return true if the new assessment has a higher priority than the old
     *         assessment; false otherwise.
     */
    public boolean isHigherPriority(AssessmentLevelEnum assessmentToTest) {
        if (assessmentLevel == null) {
            return false;
        }
        return assessmentLevel.getValue() > assessmentToTest.getValue();
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[EvaluationResult: ");
        sb.append("teamOrgName = ").append(teamOrgName);
        sb.append(", assessmentLevel = ").append(assessmentLevel);
        sb.append("]");

        return sb.toString();
    }
}
