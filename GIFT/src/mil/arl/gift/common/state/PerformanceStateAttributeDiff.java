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

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * Finds the differences between a performance attribute and another version.
 * 
 * @author sharrison
 */
public class PerformanceStateAttributeDiff {
    /** Epsilon value for comparing floats */
    private static final double EPSILON = .00001;

    /**
     * The fields within the {@link PerformanceStateAttribute} that are checked
     * for differences.
     */
    @SuppressWarnings("javadoc")
    public enum PerformanceStateAttrFields {
        ASSESSED_TEAM_ORG_ENTITIES,
        ASSESSMENT_EXPLANATION,
        EVALUATOR,
        OBSERVER_MEDIA,
        OBSERVER_COMMENT,
        COMPETENCE,
        CONFIDENCE,
        PRIORITY,
        TREND,
        SHORT_TERM,
        LONG_TERM,
        PREDICTED,
        ASSESSMENT_HOLD,
        COMPETENCE_HOLD,
        CONFIDENCE_HOLD,
        PRIORITY_HOLD,
        TREND_HOLD,
        NODE_STATE,
        SHORT_TERM_TIMESTAMP,
        LONG_TERM_TIMESTAMP,
        PREDICTED_TIMESTAMP,
        PERFORMANCE_ASSESSMENT_TIMESTAMP,
        AUTHORATATIVE_RESOURCE;

        /**
         * The collection of {@link PerformanceStateAttrFields} that represent
         * the "hold" fields (e.g. Assessment Hold)
         */
        public static final PerformanceStateAttrFields[] HOLD_FIELDS = { PerformanceStateAttrFields.ASSESSMENT_HOLD,
                PerformanceStateAttrFields.COMPETENCE_HOLD, PerformanceStateAttrFields.CONFIDENCE_HOLD,
                PerformanceStateAttrFields.PRIORITY_HOLD, PerformanceStateAttrFields.TREND_HOLD };
    }

    /** Default Constructor */
    protected PerformanceStateAttributeDiff() {
        /* Do nothing */
    }

    /**
     * Compares the two {@link PerformanceStateAttribute attributes}.
     * 
     * @param current the current performance state to use to find differences
     *        between this and the other {@link PerformanceStateAttribute}.
     *        Can't be null.
     * @param other the other performance state to use to find differences
     *        between this and the current {@link PerformanceStateAttribute}.
     *        Can't be null.
     * @param includeTimestamps true to include timestamp fields when
     *        determining if anything has changed; false to exclude them.
     * @return true if any differences were found; false if none were found.
     */
    public static boolean performDiff(PerformanceStateAttribute current, PerformanceStateAttribute other,
            boolean includeTimestamps) {
        return performDiff(current, other, null, includeTimestamps);
    }

    /**
     * Compares the two {@link PerformanceStateAttribute attributes}.
     * 
     * @param current the current performance state to use to find differences
     *        between this and the other {@link PerformanceStateAttribute}.
     *        Can't be null.
     * @param other the other performance state to use to find differences
     *        between this and the current {@link PerformanceStateAttribute}.
     *        Can't be null.
     * @param diffAttrFields the set to append with the
     *        {@link PerformanceStateAttributeDiff.PerformanceStateAttrFields
     *        attribute fields} that changed. If null, the attribute fields that
     *        were changed will not be stored.
     * @param includeTimestamps true to include timestamp fields when
     *        determining if anything has changed; false to exclude them.
     * @return true if any differences were found; false if none were found.
     */
    public static boolean performDiff(PerformanceStateAttribute current, PerformanceStateAttribute other,
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
                isFieldDiff = isAssessmentLevelEnumDifferent(current.getShortTerm(), other.getShortTerm());
                break;
            case LONG_TERM:
                isFieldDiff = isAssessmentLevelEnumDifferent(current.getLongTerm(), other.getLongTerm());
                break;
            case PREDICTED:
                isFieldDiff = isAssessmentLevelEnumDifferent(current.getPredicted(), other.getPredicted());
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
                if (includeTimestamps) {
                    isFieldDiff = current.getShortTermTimestamp() != other.getShortTermTimestamp();
                }
                break;
            case LONG_TERM_TIMESTAMP:
                if (includeTimestamps) {
                    isFieldDiff = current.getLongTermTimestamp() != other.getLongTermTimestamp();
                }
                break;
            case PREDICTED_TIMESTAMP:
                if (includeTimestamps) {
                    isFieldDiff = current.getPredictedTimestamp() != other.getPredictedTimestamp();
                }
                break;
            case PERFORMANCE_ASSESSMENT_TIMESTAMP:
                if (includeTimestamps) {
                    isFieldDiff = current.getPerformanceAssessmentTime() != other.getPerformanceAssessmentTime();
                }
                break;
            case AUTHORATATIVE_RESOURCE:
                if (includeTimestamps) {
                    isFieldDiff = current.getAuthoritativeResource() != other.getAuthoritativeResource();
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
     * Checks if the two {@link AssessmentLevelEnum}s are different.
     *
     * @param assessment1 the first assessment
     * @param assessment2 the second assessment
     * @return true if the {@link AssessmentLevelEnum}s are different; false if
     *         they are the same.
     */
    protected static boolean isAssessmentLevelEnumDifferent(AssessmentLevelEnum assessment1,
            AssessmentLevelEnum assessment2) {
        if (assessment1 == null) {
            if (assessment2 != null) {
                return true;
            }
        } else if (!assessment1.equals(assessment2)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the values of the floats are different using a small margin of
     * error {@link #EPSILON}.
     * 
     * @param value1 the first float value to compare.
     * @param value2 the second float value to compare.
     * @return true if the float values are numerically different; false
     *         otherwise.
     */
    public static boolean isFloatDifferent(float value1, float value2) {
        return Math.abs(value1 - value2) >= EPSILON;
    }

}
