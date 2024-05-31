/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar.util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.util.Map.Entry;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.PerformanceStateAttributeDiff.PerformanceStateAttrFields;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * The patch for a performance state attribute in a playback log message.
 * 
 * @author sharrison
 */
public class PerformancePatchState extends PatchedState {
    
    /**
     * The name of the performance state attribute that this patch applies to
     */
    private final String name;
    
    /** The patched fields for the performance state attribute */
    private final Map<PerformanceStateAttrFields, Object> patchedFieldsMap = new HashMap<>();
    
    /**
     * Flag indicating if the patch should maintain the original performance
     * timestamps
     */
    private boolean maintainPerformanceTimestamps = false;

    /**
     * Constructor.
     * 
     * @param name the name of the performance state attribute that this patch
     *        applies to. Can't be null or empty.
     * @param time the time that the patch should be applied.
     */
    public PerformancePatchState(String name, long time) {
        
        /* We must ensure that the name is properly included in the initial key, otherwise, patched assessments 
         * for performance nodes with different names will be inadvertently merged together */
        super(time, buildUniquePatchKey(name, time));
        
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("The parameter 'name' cannot be blank.");
        }
        
        this.name = name;
    }

    @Override
    public String buildUniquePatchKey(long time) {
        return buildUniquePatchKey(name, time);
    }
    
    /**
     * Builds the unique identifier for a {@link PatchedState}.
     * 
     * @param name the name of the {@link PerformanceStateAttribute} that the
     *        patch applies to. Can't be null.
     * @param time the time the patch should be applied.
     * @return the unique identifier for the patch. Will never be null.
     */
    public static String buildUniquePatchKey(String name, long time) {
        return name + "_" + time;
    }
    
    /**
     * The name of the performance state attribute that this patch applies to
     * 
     * @return the performance state attribute name. Will never be null.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the patched fields for the performance state attribute.
     * 
     * @param patchedAttr the attribute to pull the patched data from. If null,
     *        no patch data will exist.
     * @param patchedFields the fields in the performance state attribute that
     *        have been patched. If null or empty, no patch data will exist. If
     *        it only contains {@link PerformanceStateAttrFields#EVALUATOR}, it
     *        will be treated as empty.
     */
    public void setPatchedFields(PerformanceStateAttribute patchedAttr, Set<PerformanceStateAttrFields> patchedFields) {
        patchedFieldsMap.clear();
        updatePatchedFields(patchedAttr, patchedFields);
    }

    /**
     * Merges the existing patched fields with the ones provided.
     * 
     * @param patchedAttr the attribute to pull the patched data from. If null,
     *        no patch data will be used from this attribute.
     * @param patchedFields the fields in the performance state attribute that
     *        have been patched. If null or empty, no patch data will be used
     *        from this attribute. If it only contains
     *        {@link PerformanceStateAttrFields#EVALUATOR}, it will be treated
     *        as empty.
     */
    public void updatePatchedFields(PerformanceStateAttribute patchedAttr,
            Set<PerformanceStateAttrFields> patchedFields) {
        if (patchedAttr == null || CollectionUtils.isEmpty(patchedFields)) {
            return;
        } else if (patchedFields.size() == 1 && patchedFields.contains(PerformanceStateAttrFields.EVALUATOR)) {
            /* Only updating the evaluator is the same as doing nothing. */
            return;
        }

        /* Ensure the evaluator is always populated */
        patchedFieldsMap.put(PerformanceStateAttrFields.EVALUATOR, patchedAttr.getEvaluator());

        for (PerformanceStateAttrFields field : patchedFields) {
            switch (field) {
            case ASSESSED_TEAM_ORG_ENTITIES:
                patchedFieldsMap.put(field, patchedAttr.getAssessedTeamOrgEntities());
                break;
            case ASSESSMENT_EXPLANATION:
                /* Don't process if there is an observer comment since that will
                 * handle updating the explanation with the comment */
                if (!patchedFieldsMap.containsKey(PerformanceStateAttrFields.OBSERVER_COMMENT)) {
                    patchedFieldsMap.put(field, patchedAttr.getAssessmentExplanation());
                }
                break;
            case ASSESSMENT_HOLD:
                patchedFieldsMap.put(field, patchedAttr.isAssessmentHold());
                /* If hold becomes true, ensure we add the associated value */
                if (patchedAttr.isAssessmentHold()) {
                    patchedFieldsMap.put(PerformanceStateAttrFields.SHORT_TERM, patchedAttr.getShortTerm());
                }
                break;
            case AUTHORATATIVE_RESOURCE:
                patchedFieldsMap.put(field, patchedAttr.getAuthoritativeResource());
                break;
            case COMPETENCE:
                patchedFieldsMap.put(field, patchedAttr.getCompetence());
                break;
            case COMPETENCE_HOLD:
                patchedFieldsMap.put(field, patchedAttr.isCompetenceHold());
                /* If hold becomes true, ensure we add the associated value */
                if (patchedAttr.isCompetenceHold()) {
                    patchedFieldsMap.put(PerformanceStateAttrFields.COMPETENCE, patchedAttr.getCompetence());
                }
                break;
            case CONFIDENCE:
                patchedFieldsMap.put(field, patchedAttr.getConfidence());
                break;
            case CONFIDENCE_HOLD:
                patchedFieldsMap.put(field, patchedAttr.isConfidenceHold());
                /* If hold becomes true, ensure we add the associated value */
                if (patchedAttr.isConfidenceHold()) {
                    patchedFieldsMap.put(PerformanceStateAttrFields.CONFIDENCE, patchedAttr.getConfidence());
                }
                break;
            case EVALUATOR:
                patchedFieldsMap.put(field, patchedAttr.getEvaluator());
                break;
            case LONG_TERM:
                patchedFieldsMap.put(field, patchedAttr.getLongTerm());
                break;
            case LONG_TERM_TIMESTAMP:
                patchedFieldsMap.put(field, patchedAttr.getLongTermTimestamp());
                break;
            case NODE_STATE:
                patchedFieldsMap.put(field, patchedAttr.getNodeStateEnum());
                break;
            case OBSERVER_COMMENT:
                patchedFieldsMap.put(field, patchedAttr.getObserverComment());
                final Set<String> assessmentExplanation = new HashSet<>();
                assessmentExplanation.add(patchedAttr.getObserverComment());
                patchedFieldsMap.put(PerformanceStateAttrFields.ASSESSMENT_EXPLANATION, assessmentExplanation);
                break;
            case OBSERVER_MEDIA:
                patchedFieldsMap.put(field, patchedAttr.getObserverMedia());
                break;
            case PERFORMANCE_ASSESSMENT_TIMESTAMP:
                patchedFieldsMap.put(field, patchedAttr.getPerformanceAssessmentTime());
                break;
            case PREDICTED:
                patchedFieldsMap.put(field, patchedAttr.getPredicted());
                break;
            case PREDICTED_TIMESTAMP:
                patchedFieldsMap.put(field, patchedAttr.getPredictedTimestamp());
                break;
            case PRIORITY:
                patchedFieldsMap.put(field, patchedAttr.getPriority());
                break;
            case PRIORITY_HOLD:
                patchedFieldsMap.put(field, patchedAttr.isPriorityHold());
                /* If hold becomes true, ensure we add the associated value */
                if (patchedAttr.isPriorityHold()) {
                    patchedFieldsMap.put(PerformanceStateAttrFields.PRIORITY, patchedAttr.getPriority());
                }
                break;
            case SHORT_TERM:
                patchedFieldsMap.put(field, patchedAttr.getShortTerm());
                break;
            case SHORT_TERM_TIMESTAMP:
                patchedFieldsMap.put(field, patchedAttr.getShortTermTimestamp());
                break;
            case TREND:
                patchedFieldsMap.put(field, patchedAttr.getTrend());
                break;
            case TREND_HOLD:
                patchedFieldsMap.put(field, patchedAttr.isTrendHold());
                /* If hold becomes true, ensure we add the associated value */
                if (patchedAttr.isTrendHold()) {
                    patchedFieldsMap.put(PerformanceStateAttrFields.TREND, patchedAttr.getTrend());
                }
                break;
            default:
                throw new UnsupportedOperationException("Found an unknown enum type: " + field);
            }
        }
    }
    
    @Override
    public void applyPatch(MessageManager toApplyMsg) {
        applyPatch(toApplyMsg, null);
    }

    /**
     * Apply this patch to the provided message.
     * 
     * @param toApplyMsg the message to apply the patch to.
     * @param fieldsToUpdate the set of attribute fields to utilize from the
     *        patch. If null or empty, all the patch fields will be used.
     */
    @SuppressWarnings("unchecked")
    public void applyPatch(MessageManager toApplyMsg, Set<PerformanceStateAttrFields> fieldsToUpdate) {
        if (toApplyMsg == null) {
            return;
        }

        final AbstractPerformanceState perfState = MessageManager.findPerformanceStateByName(toApplyMsg.getMessage(),
                name);
        if (perfState == null || perfState.getState() == null) {
            return;
        }

        final PerformanceStateAttribute toApply = perfState.getState();

        /* Only update the fields in this collection. Null or empty means to
         * update everything. */
        final boolean hasLimitedFields = CollectionUtils.isNotEmpty(fieldsToUpdate);
        
        /* If a hold changed, auto-enable it's associated value field */
        if (hasLimitedFields) {
            for (PerformanceStateAttrFields holdField : PerformanceStateAttrFields.HOLD_FIELDS) {
                switch (holdField) {
                case ASSESSMENT_HOLD:
                    fieldsToUpdate.add(PerformanceStateAttrFields.SHORT_TERM);
                    break;
                case COMPETENCE_HOLD:
                    fieldsToUpdate.add(PerformanceStateAttrFields.COMPETENCE);
                    break;
                case TREND_HOLD:
                    fieldsToUpdate.add(PerformanceStateAttrFields.TREND);
                    break;
                case CONFIDENCE_HOLD:
                    fieldsToUpdate.add(PerformanceStateAttrFields.CONFIDENCE);
                    break;
                case PRIORITY_HOLD:
                    fieldsToUpdate.add(PerformanceStateAttrFields.PRIORITY);
                    break;
				default:
					break;
                }
            }
        }

        for (PerformanceStateAttrFields field : patchedFieldsMap.keySet()) {
            /* Skip if the field is not in the fieldsToUpdate collection */
            if (hasLimitedFields && !fieldsToUpdate.contains(field)) {
                continue;
            }

            switch (field) {
            case ASSESSED_TEAM_ORG_ENTITIES:
                toApply.setAssessedTeamOrgEntities((Map<String, AssessmentLevelEnum>) patchedFieldsMap.get(field));
                break;
            case ASSESSMENT_EXPLANATION:
                toApply.setAssessmentExplanation((Set<String>) patchedFieldsMap.get(field), true);
                break;
            case ASSESSMENT_HOLD:
                toApply.setAssessmentHold((boolean) patchedFieldsMap.get(field));
                break;
            case AUTHORATATIVE_RESOURCE:
                toApply.setAuthoritativeResource((String) patchedFieldsMap.get(field));
                break;
            case COMPETENCE:
                toApply.setCompetence((float) patchedFieldsMap.get(field), true);
                break;
            case COMPETENCE_HOLD:
                toApply.setCompetenceHold((boolean) patchedFieldsMap.get(field));
                break;
            case CONFIDENCE:
                toApply.setConfidence((float) patchedFieldsMap.get(field), true);
                break;
            case CONFIDENCE_HOLD:
                toApply.setCompetenceHold((boolean) patchedFieldsMap.get(field));
                break;
            case EVALUATOR:
                toApply.setEvaluator((String) patchedFieldsMap.get(field));
                break;
            case LONG_TERM:
                if (patchedFieldsMap.containsKey(PerformanceStateAttrFields.LONG_TERM_TIMESTAMP)) {
                    /* Ideally this would become like short-term where we can
                     * push the new timestamp */
                    toApply.updateLongTerm((AssessmentLevelEnum) patchedFieldsMap.get(field));
                } else {
                    toApply.updateLongTerm((AssessmentLevelEnum) patchedFieldsMap.get(field));
                }
                break;
            case LONG_TERM_TIMESTAMP:
                /* No way to update currently */
                break;
            case NODE_STATE:
                toApply.setNodeStateEnum((PerformanceNodeStateEnum) patchedFieldsMap.get(field));
                break;
            case OBSERVER_COMMENT:
                toApply.setObserverComment((String) patchedFieldsMap.get(field));
                break;
            case OBSERVER_MEDIA:
                toApply.setObserverMedia((String) patchedFieldsMap.get(field));
                break;
            case PERFORMANCE_ASSESSMENT_TIMESTAMP:
                toApply.setPerformanceAssessmentTime((long) patchedFieldsMap.get(field));
                break;
            case PREDICTED:
                if (patchedFieldsMap.containsKey(PerformanceStateAttrFields.PREDICTED_TIMESTAMP)) {
                    /* Ideally this would become like short-term where we can
                     * push the new timestamp */
                    toApply.updatePredicted((AssessmentLevelEnum) patchedFieldsMap.get(field));
                } else {
                    toApply.updatePredicted((AssessmentLevelEnum) patchedFieldsMap.get(field));
                }
                break;
            case PREDICTED_TIMESTAMP:
                /* No way to update currently */
                break;
            case PRIORITY:
                toApply.setPriority((Integer) patchedFieldsMap.get(field), true);
                break;
            case PRIORITY_HOLD:
                toApply.setPriorityHold((boolean) patchedFieldsMap.get(field));
                break;
            case SHORT_TERM:
                if (maintainPerformanceTimestamps) {
                    toApply.updateShortTerm((AssessmentLevelEnum) patchedFieldsMap.get(field), true,
                            toApplyMsg.getOriginalPerformanceStateWithName(name).getState().getShortTermTimestamp());
                } else if (patchedFieldsMap.containsKey(PerformanceStateAttrFields.SHORT_TERM_TIMESTAMP)) {
                    toApply.updateShortTerm((AssessmentLevelEnum) patchedFieldsMap.get(field), true,
                            (long) patchedFieldsMap.get(PerformanceStateAttrFields.SHORT_TERM_TIMESTAMP));
                } else {
                    toApply.updateShortTerm((AssessmentLevelEnum) patchedFieldsMap.get(field), true, getTime());
                }
                break;
            case SHORT_TERM_TIMESTAMP:
                if (!patchedFieldsMap.containsKey(PerformanceStateAttrFields.SHORT_TERM)) {
                    toApply.updateShortTerm(toApply.getShortTerm(), true, (long) patchedFieldsMap.get(field));
                }
                break;
            case TREND:
                toApply.setTrend((float) patchedFieldsMap.get(field), true);
                break;
            case TREND_HOLD:
                toApply.setTrendHold((boolean) patchedFieldsMap.get(field));
                break;
            default:
                throw new UnsupportedOperationException("Found an unknown enum type: " + field);
            }
        }
    }
    
    /**
     * Gets the hold fields that have been patched (updated).
     * 
     * @return the hold fields that have been patched. Can be empty if no hold
     *         fields were patched. Will never be null.
     */
    public Set<PerformanceStateAttrFields> getPatchedHoldFields() {
        final Set<PerformanceStateAttrFields> patchedHolds = new HashSet<>();
        for (PerformanceStateAttrFields holdField : PerformanceStateAttrFields.HOLD_FIELDS) {
            if (patchedFieldsMap.containsKey(holdField)) {
                patchedHolds.add(holdField);
            }
        }
        return patchedHolds;
    }
    
    /**
     * Flag indicating if the patch should maintain the original performance
     * timestamps when being applied.
     * 
     * @param maintainPerformanceTimestamps true (default) to maintain the
     *        original performance timestamps; false otherwise.
     */
    public void setMaintainPerformanceTimestamps(boolean maintainPerformanceTimestamps) {
        this.maintainPerformanceTimestamps = maintainPerformanceTimestamps;
    }
    
    @Override
    public void updatePatch(PatchedState newPatch) {
        if (newPatch == null || !(newPatch instanceof PerformancePatchState)) {
            return;
        }
        
        PerformancePatchState performancePatch = (PerformancePatchState) newPatch;

        /* Override the current fields with the ones in the new patch */
        for (Entry<PerformanceStateAttrFields, Object> entry : performancePatch.patchedFieldsMap.entrySet()) {
            patchedFieldsMap.put(entry.getKey(), entry.getValue());
        }
    }
}
