/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * Finds the differences in attributes for an abstract assessment. This had to
 * be broken out of {@link PerformanceStateAttributeDiff} because
 * {@link AbstractAssessment} uses UUID which is not implemented by GWT.
 * 
 * @author sharrison
 */
public class AssessmentDiff extends PerformanceStateAttributeDiff {

    /**
     * Compares a {@link PerformanceStateAttribute} and an
     * {@link AbstractAssessment}.
     * 
     * @param current the current performance state to use to find differences
     *        between this and the other {@link AbstractAssessment}. Can't be
     *        null.
     * @param other the other assessment to use to find differences between this
     *        and the current {@link PerformanceStateAttribute}. Can't be null.
     * @param diffAttrFields the set to append with the
     *        {@link PerformanceStateAttributeDiff.PerformanceStateAttrFields
     *        attribute fields} that changed. If null, the attribute fields that
     *        were changed will not be stored.
     * @param includeTimestamps true to include timestamp fields when
     *        determining if anything has changed; false to exclude them.
     * @return true if any differences were found; false if none were found.
     */
    public static boolean performDiff(PerformanceStateAttribute current, AbstractAssessment other,
            Set<PerformanceStateAttrFields> diffAttrFields, boolean includeTimestamps) {
        if (current == null) {
            throw new IllegalArgumentException("The parameter 'current' cannot be null.");
        } else if (other == null) {
            throw new IllegalArgumentException("The parameter 'other' cannot be null.");
        }

        boolean foundDiff = false;
        for (PerformanceStateAttrFields attrField : PerformanceStateAttrFields.values()) {
            boolean isFieldDiff = false;

            switch (attrField) {
            /* Check and holds */
            case ASSESSMENT_HOLD:
                isFieldDiff = current.isAssessmentHold() != other.isAssessmentHold();
                break;
            case COMPETENCE_HOLD:
                isFieldDiff = current.isCompetenceHold() != other.isCompetenceHold();
                break;
            case CONFIDENCE_HOLD:
                isFieldDiff = current.isConfidenceHold() != other.isConfidenceHold();
                break;
            case PRIORITY_HOLD:
                isFieldDiff = current.isPriorityHold() != other.isPriorityHold();
                break;
            case TREND_HOLD:
                isFieldDiff = current.isTrendHold() != other.isTrendHold();
                break;

            /* Check assessments */
            case SHORT_TERM:
                isFieldDiff = isAssessmentLevelEnumDifferent(current.getShortTerm(), other.getAssessmentLevel());
                break;
            case LONG_TERM:
                /* Long Term is not supported with assessments */
                break;
            case PREDICTED:
                /* Predicted is not supported with assessments */
                break;

            /* Check performance values */
            case PRIORITY:
                isFieldDiff = !Objects.equals(current.getPriority(), other.getPriority());
                break;
            case CONFIDENCE:
                isFieldDiff = isFloatDifferent(current.getConfidence(), other.getConfidence());
                break;
            case COMPETENCE:
                isFieldDiff = isFloatDifferent(current.getCompetence(), other.getCompetence());
                break;
            case TREND:
                isFieldDiff = isFloatDifferent(current.getTrend(), other.getTrend());
                break;

            /* Check the rest */
            case EVALUATOR:
                isFieldDiff = !StringUtils.equalsIgnoreCase(current.getEvaluator(), other.getEvaluator());
                break;
            case OBSERVER_COMMENT:
                isFieldDiff = !StringUtils.equalsIgnoreCase(current.getObserverComment(), other.getObserverComment());
                break;
            case OBSERVER_MEDIA:
                isFieldDiff = !StringUtils.equalsIgnoreCase(current.getObserverMedia(), other.getObserverMedia());
                break;
            case NODE_STATE:
                isFieldDiff = current.getNodeStateEnum() != other.getNodeStateEnum();
                break;
            case AUTHORATATIVE_RESOURCE:
                isFieldDiff =  !StringUtils.equalsIgnoreCase(current.getAuthoritativeResource(), other.getAuthoritativeResource());
                break;
            /* Compare assessment explanation set; null and empty should equate
             * to being equal */
            case ASSESSMENT_EXPLANATION:
                final Set<String> currentExplanation = CollectionUtils.isNotEmpty(current.getAssessmentExplanation())
                        ? current.getAssessmentExplanation()
                        : null;
                final Set<String> otherExplanation = CollectionUtils.isNotEmpty(other.getAssessmentExplanation())
                        ? other.getAssessmentExplanation()
                        : null;
                isFieldDiff = !CollectionUtils.equals(currentExplanation, otherExplanation);
                break;

            /* Compare assessed team org entity set; null and empty should
             * equate to being equal */
            case ASSESSED_TEAM_ORG_ENTITIES:
                final Map<String, AssessmentLevelEnum> currentTeamOrg = CollectionUtils
                        .isNotEmpty(current.getAssessedTeamOrgEntities()) ? current.getAssessedTeamOrgEntities() : null;
                final Map<String, AssessmentLevelEnum> otherTeamOrg = CollectionUtils
                        .isNotEmpty(other.getAssessedTeamOrgEntities()) ? other.getAssessedTeamOrgEntities() : null;
                isFieldDiff = !CollectionUtils.equals(currentTeamOrg, otherTeamOrg);
                break;

            /* Check timestamp fields. These should all exit early if
             * includeTimestamps is false */
            case SHORT_TERM_TIMESTAMP:
                /* Short Term timestamp is not supported with assessments */
                break;
            case LONG_TERM_TIMESTAMP:
                /* Long Term timestamp is not supported with assessments */
                break;
            case PREDICTED_TIMESTAMP:
                /* Predicted timestamp is not supported with assessments */
                break;
            case PERFORMANCE_ASSESSMENT_TIMESTAMP:
                if (includeTimestamps) {
                    isFieldDiff = current.getPerformanceAssessmentTime() != other.getTime();
                }
                break;
            default:
                throw new RuntimeException("Missing logic to compare values for property '" + attrField + "'.");
            }

            if (isFieldDiff) {
                /* Can't store the different fields; so exit early once the
                 * first difference is found */
                if (diffAttrFields == null) {
                    return true;
                }

                foundDiff = true;
                diffAttrFields.add(attrField);
            }
        }

        return foundDiff;
    }

    /**
     * Compares the two {@link AbstractAssessment assessments}.
     * 
     * @param current the current assessment to use to find differences between
     *        this and the other {@link AbstractAssessment}. Can't be null.
     * @param other the other assessment to use to find differences between this
     *        and the current {@link AbstractAssessment}. Can't be null.
     * @param diffAttrFields the set to append with the
     *        {@link PerformanceStateAttributeDiff.PerformanceStateAttrFields
     *        attribute fields} that changed. If null, the attribute fields that
     *        were changed will not be stored.
     * @param includeTimestamps true to include timestamp fields when
     *        determining if anything has changed; false to exclude them.
     * @return true if any differences were found; false if none were found.
     */
    public static boolean performDiff(AbstractAssessment current, AbstractAssessment other,
            Set<PerformanceStateAttrFields> diffAttrFields, boolean includeTimestamps) {
        if (current == null) {
            throw new IllegalArgumentException("The parameter 'current' cannot be null.");
        } else if (other == null) {
            throw new IllegalArgumentException("The parameter 'other' cannot be null.");
        }

        boolean foundDiff = false;
        for (PerformanceStateAttrFields attrField : PerformanceStateAttrFields.values()) {
            boolean isFieldDiff = false;

            switch (attrField) {
            /* Check and holds */
            case ASSESSMENT_HOLD:
                isFieldDiff = current.isAssessmentHold() != other.isAssessmentHold();
                break;
            case COMPETENCE_HOLD:
                isFieldDiff = current.isCompetenceHold() != other.isCompetenceHold();
                break;
            case CONFIDENCE_HOLD:
                isFieldDiff = current.isConfidenceHold() != other.isConfidenceHold();
                break;
            case PRIORITY_HOLD:
                isFieldDiff = current.isPriorityHold() != other.isPriorityHold();
                break;
            case TREND_HOLD:
                isFieldDiff = current.isTrendHold() != other.isTrendHold();
                break;

            /* Check assessments */
            case SHORT_TERM:
                isFieldDiff = isAssessmentLevelEnumDifferent(current.getAssessmentLevel(), other.getAssessmentLevel());
                break;
            case LONG_TERM:
                /* Long Term is not supported with assessments */
                break;
            case PREDICTED:
                /* Predicted is not supported with assessments */
                break;

            /* Check performance values */
            case PRIORITY:
                isFieldDiff = !Objects.equals(current.getPriority(), other.getPriority());
                break;
            case CONFIDENCE:
                isFieldDiff = isFloatDifferent(current.getConfidence(), other.getConfidence());
                break;
            case COMPETENCE:
                isFieldDiff = isFloatDifferent(current.getCompetence(), other.getCompetence());
                break;
            case TREND:
                isFieldDiff = isFloatDifferent(current.getTrend(), other.getTrend());
                break;

            /* Check the rest */
            case EVALUATOR:
                isFieldDiff = !StringUtils.equalsIgnoreCase(current.getEvaluator(), other.getEvaluator());
                break;
            case OBSERVER_COMMENT:
                isFieldDiff = !StringUtils.equalsIgnoreCase(current.getObserverComment(), other.getObserverComment());
                break;
            case OBSERVER_MEDIA:
                isFieldDiff = !StringUtils.equalsIgnoreCase(current.getObserverMedia(), other.getObserverMedia());
                break;
            case NODE_STATE:
                isFieldDiff = current.getNodeStateEnum() != other.getNodeStateEnum();
                break;

            /* Compare assessment explanation set; null and empty should equate
             * to being equal */
            case ASSESSMENT_EXPLANATION:
                final Set<String> currentExplanation = CollectionUtils.isNotEmpty(current.getAssessmentExplanation())
                        ? current.getAssessmentExplanation()
                        : null;
                final Set<String> otherExplanation = CollectionUtils.isNotEmpty(other.getAssessmentExplanation())
                        ? other.getAssessmentExplanation()
                        : null;
                isFieldDiff = !CollectionUtils.equals(currentExplanation, otherExplanation);
                break;

            /* Compare assessed team org entity set; null and empty should
             * equate to being equal */
            case ASSESSED_TEAM_ORG_ENTITIES:
                final Map<String, AssessmentLevelEnum> currentTeamOrg = CollectionUtils
                        .isNotEmpty(current.getAssessedTeamOrgEntities()) ? current.getAssessedTeamOrgEntities() : null;
                final Map<String, AssessmentLevelEnum> otherTeamOrg = CollectionUtils
                        .isNotEmpty(other.getAssessedTeamOrgEntities()) ? other.getAssessedTeamOrgEntities() : null;
                isFieldDiff = !CollectionUtils.equals(currentTeamOrg, otherTeamOrg);
                break;

            /* Check timestamp fields. These should all exit early if
             * includeTimestamps is false */
            case SHORT_TERM_TIMESTAMP:
                /* Short Term timestamp is not supported with assessments */
                break;
            case LONG_TERM_TIMESTAMP:
                /* Long Term timestamp is not supported with assessments */
                break;
            case PREDICTED_TIMESTAMP:
                /* Predicted timestamp is not supported with assessments */
                break;
            case PERFORMANCE_ASSESSMENT_TIMESTAMP:
                if (includeTimestamps) {
                    isFieldDiff = current.getTime() != other.getTime();
                }
                break;
            case AUTHORATATIVE_RESOURCE:
                isFieldDiff = !Objects.equals(current.getAuthoritativeResource(), other.getAuthoritativeResource());
                break;
            default:
                throw new RuntimeException("Missing logic to compare values for property '" + attrField + "'.");
            }

            if (isFieldDiff) {
                /* Can't store the different fields; so exit early once the
                 * first difference is found */
                if (diffAttrFields == null) {
                    return true;
                }

                foundDiff = true;
                diffAttrFields.add(attrField);
            }
        }

        return foundDiff;
    }
}