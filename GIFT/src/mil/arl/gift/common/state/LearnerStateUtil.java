/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;

/**
 * Utility class for evaluating entity assessments based on a learner state
 * 
 * @author sharrison
 */
public class LearnerStateUtil {
    /**
     * Evaluate the assessments for each team role using the provided knowledge
     * session state.
     *
     * @param state the knowledge session state used to update the team role
     *        assessments. Can't be null.
     * @param teamStructure the team structure used to identify ancestors. Can't
     *        be null.
     * @return the assessment results for the team roles.
     */
    public static Map<String, EvaluationResult> performEvaluation(LearnerState state, Team teamStructure) {
        /* Initialize the results map to a default assessment state */
        Map<String, EvaluationResult> results = new HashMap<>();

        /* Can't assess the learners without a performance state */
        if (state == null || state.getPerformance() == null) {
            return results;
        }

        /* Assign a default assessment of unknown to each learner until an
         * assessment value is known. This is needed to stop showing roles as
         * below expectation for a task/concept after that task/concept is no
         * longer active. */
        for (String learner : teamStructure.getTeamMemberNames()) {
            results.put(learner, new EvaluationResult(learner));
        }

        /* Determine the learners' performance assessments */
        for (TaskPerformanceState taskState : state.getPerformance().getTasks().values()) {
            
            if (taskState.getState() == null
                    || !PerformanceNodeStateEnum.ACTIVE.equals(taskState.getState().getNodeStateEnum())) {
                continue;
            }

            for (ConceptPerformanceState conceptState : taskState.getConcepts()) {
                /* Update the concept performances */
                updateConceptPerformance(conceptState, results, teamStructure);
            }
        }

        /* Now that we have the team member assessments; update the team
         * assessments */
        updateTeamPerformance(results, teamStructure);

        return results;
    }

    /**
     * Updates the team performance assessments based on their children states.
     * 
     * @param conceptState the concept state used to update the team
     *        assessments. Can't be null.
     * @param teamAssessments the map of team assessments. This is used to
     *        calculate the aggregate assessment state for teams. Can't be null.
     * @param teamStructure the team structure used to identify ancestors. Can't
     *        be null.
     */
    private static void updateConceptPerformance(ConceptPerformanceState conceptState,
            Map<String, EvaluationResult> teamAssessments, Team teamStructure) {
        final PerformanceStateAttribute cStateAttr = conceptState.getState();

        /*- Skip cases:
         * 1. Concept has no performance state attribute.
         *       Reason: Nothing to evaluate.
         * 2. Concept has no specified assessed learners.
         *       Reason: No one to assign the concept's assessment.
         */
        if (cStateAttr != null && CollectionUtils.isNotEmpty(cStateAttr.getAssessedTeamOrgEntities())) {
            /* Update the learners' assessments */
            for (Entry<String, AssessmentLevelEnum> entry : cStateAttr.getAssessedTeamOrgEntities().entrySet()) {
                final String learner = entry.getKey();

                /* Only maintain assessments for this map layer's team
                 * structure */
                if (!teamStructure.getTeamMemberNames().contains(learner)) {
                    continue;
                }

                final EvaluationResult entityEvalResult = teamAssessments.get(learner);
                
                final AssessmentLevelEnum newAssessment = entry.getValue();
                final AssessmentLevelEnum evalAssessment = entityEvalResult.getAssessmentLevel();

                if (evalAssessment == null) {
                    continue;
                } else if (newAssessment == null) {
                    entityEvalResult.setAssessmentLevel(null);
                    entityEvalResult.getConceptPerformances().clear();
                } else if (evalAssessment == newAssessment) {
                    entityEvalResult.getConceptPerformances().add(conceptState);
                } else if (entityEvalResult.isHigherPriority(newAssessment)) {
                    entityEvalResult.setAssessmentLevel(newAssessment);
                    entityEvalResult.getConceptPerformances().clear();
                    entityEvalResult.getConceptPerformances().add(conceptState);
                }
            }
        }

        if (conceptState instanceof IntermediateConceptPerformanceState) {
            /* If this is an intermediate concept, process its children
             * concepts */
            IntermediateConceptPerformanceState intermediate = (IntermediateConceptPerformanceState) conceptState;
            for (ConceptPerformanceState iState : intermediate.getConcepts()) {
                updateConceptPerformance(iState, teamAssessments, teamStructure);
            }
        }
    }

    /**
     * Updates the team performance assessments based on their children states.
     * 
     * @param teamAssessments the map of team assessments. This is used to
     *        calculate the aggregate assessment state for teams. If null, no
     *        update will be performed.
     * @param team the team to update. If null, no update will be performed.
     */
    private static void updateTeamPerformance(Map<String, EvaluationResult> teamAssessments,
            Team team) {
        if (teamAssessments == null || team == null) {
            return;
        }

        EvaluationResult teamEvalResult = teamAssessments.get(team.getName());
        if (teamEvalResult == null) {
            teamEvalResult = new EvaluationResult(team.getName());
            teamAssessments.put(team.getName(), teamEvalResult);
        }

        for (AbstractTeamUnit teamItem : team.getUnits()) {
            final AssessmentLevelEnum teamAssessment = teamEvalResult.getAssessmentLevel();
            if (teamAssessment == null) {
                /* Visual only, no need to keep processing */
                break;
            }

            EvaluationResult newEvalResult = null;
            if (teamItem instanceof TeamMember) {
                TeamMember<?> member = (TeamMember<?>) teamItem;
                final String memberName = member.getName();
                newEvalResult = teamAssessments.get(memberName);
            } else if (teamItem instanceof Team) {
                final Team subTeam = (Team) teamItem;
                updateTeamPerformance(teamAssessments, subTeam);
                newEvalResult = teamAssessments.get(subTeam.getName());
            }

            if (newEvalResult != null) {
                final AssessmentLevelEnum newAssessment = newEvalResult.getAssessmentLevel();
                if (newAssessment == null) {
                    teamEvalResult.setAssessmentLevel(null);
                    teamEvalResult.getConceptPerformances().clear();
                } else if (teamAssessment == newAssessment) {
                    teamEvalResult.getConceptPerformances().addAll(newEvalResult.getConceptPerformances());
                } else if (teamEvalResult.isHigherPriority(newAssessment)) {
                    teamEvalResult.setAssessmentLevel(newAssessment);
                    teamEvalResult.getConceptPerformances().clear();
                    teamEvalResult.getConceptPerformances().addAll(newEvalResult.getConceptPerformances());
                }
            }
        }
    }
}
