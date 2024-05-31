/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.AssignedSectorCondition;
import generated.dkf.AvoidLocationCondition;
import generated.dkf.Condition;
import generated.dkf.Count;
import generated.dkf.EliminateHostilesCondition;
import generated.dkf.HaltConditionInput;
import generated.dkf.HealthConditionInput;
import generated.dkf.IdentifyPOIsCondition;
import generated.dkf.LifeformTargetAccuracyCondition;
import generated.dkf.MuzzleFlaggingCondition;
import generated.dkf.NegligentDischargeCondition;
import generated.dkf.RealTimeAssessmentRules;
import generated.dkf.RulesOfEngagementCondition;
import generated.dkf.SpeedLimitCondition;
import generated.dkf.ViolationTime;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ScoringRuleWidget.ScoringRuleCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ScoringRuleWidget.ScoringRuleType;

/**
 * The widget that displays the optional customized assessment scoring rules for the condition.
 *
 * @author sharrison
 */
public class RealTimeAssessmentScoringRulesWidget extends ScoringRulesWidgetWrapper {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(RealTimeAssessmentScoringRulesWidget.class.getName());

    /**
     * Constructor. Only uses {@link ScoringRuleType#COUNT count} and
     * {@link ScoringRuleType#VIOLATION_TIME violation time}.
     */
    public RealTimeAssessmentScoringRulesWidget() {
        super(ScoringRuleType.COUNT, ScoringRuleType.VIOLATION_TIME);
    }

    @Override
    public void populateWidget(final Condition parentCondition) {
        if (parentCondition == null) {
            throw new IllegalArgumentException("The parameter 'parentCondition' cannot be null.");
        } else if (!isConditionRTASupported(parentCondition)) {
            throw new IllegalArgumentException(
                    "The parameter 'parentCondition' is not a condition that supports real time assessment rules.");
        }

        for (ScoringRuleWidget widget : widgetsInUse) {
            Serializable scoringRule = findScoringRuleByType(parentCondition, widget.getType());
            widget.populateWidget(scoringRule, parentCondition, new ScoringRuleCallback() {
                @Override
                public void onRemove(Serializable scoringRule) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("onRemove(" + scoringRule + ")");
                    }

                    RealTimeAssessmentRules rtaRules = getRealTimeAssessmentRules(parentCondition);

                    // if no scoring object, nothing to remove
                    if (rtaRules == null) {
                        return;
                    }

                    if (ScoringRuleType.COUNT.isCorrectType(scoringRule)) {
                        rtaRules.setCount(null);
                    } else if (ScoringRuleType.VIOLATION_TIME.isCorrectType(scoringRule)) {
                        rtaRules.setViolationTime(null);
                    }

                    // make schema valid if empty
                    if (rtaRules.getCount() == null && rtaRules.getViolationTime() == null) {
                        setRealTimeAssessmentRules(null, parentCondition);
                    }
                }

                @Override
                public void onAdd(Serializable scoringRule) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.fine("onAdd(" + scoringRule + ")");
                    }

                    RealTimeAssessmentRules rtaRules = getRealTimeAssessmentRules(parentCondition);

                    // create the real time assessment rules if it is null
                    boolean isCount = ScoringRuleType.COUNT.isCorrectType(scoringRule);
                    boolean isViolationTime = ScoringRuleType.VIOLATION_TIME.isCorrectType(scoringRule);
                    if (rtaRules == null) {
                        rtaRules = new RealTimeAssessmentRules();
                        setRealTimeAssessmentRules(rtaRules, parentCondition);
                    } else {
                        if (isCount && rtaRules.getCount() != null) {
                            throw new UnsupportedOperationException("The scoring rule '" + scoringRule
                                    + "' cannot be added to the condition because a Count scoring rule already exists.");
                        } else if (isViolationTime && rtaRules.getViolationTime() != null) {
                            throw new UnsupportedOperationException("The scoring rule '" + scoringRule
                                    + "' cannot be added to the condition because a Violation Time scoring rule already exists.");
                        }
                    }

                    // add it to the real time assessment rules
                    if (isCount) {
                        rtaRules.setCount((Count) scoringRule);
                    } else if (isViolationTime) {
                        rtaRules.setViolationTime((ViolationTime) scoringRule);
                    }
                }
            });
        }
    }

    /**
     * Checks if the provided condition is supported by Real Time Assessment Rules.
     *
     * @param condition the condition to check.
     * @return true if the provided condition contains {@link RealTimeAssessmentRules}.
     */
    private boolean isConditionRTASupported(Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        } else if (condition.getInput() == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot have a null input.");
        } else if (condition.getInput().getType() == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot have a null input type.");
        }

        Serializable conditionInputType = condition.getInput().getType();
        if (conditionInputType instanceof AvoidLocationCondition) {
            return true;
        } else if (conditionInputType instanceof AssignedSectorCondition){
            return true;
        } else if (conditionInputType instanceof EliminateHostilesCondition) {
            return true;
        } else if (conditionInputType instanceof HaltConditionInput) {
            return true;
        } else if (conditionInputType instanceof HealthConditionInput) {
            return true;
        } else if (conditionInputType instanceof IdentifyPOIsCondition) {
            return true;
        } else if (conditionInputType instanceof LifeformTargetAccuracyCondition) {
            return true;
        } else if (conditionInputType instanceof MuzzleFlaggingCondition) {
            return true;
        } else if (conditionInputType instanceof RulesOfEngagementCondition) {
            return true;
        } else if (conditionInputType instanceof SpeedLimitCondition) {
            return true;
        } else if(conditionInputType instanceof NegligentDischargeCondition){
            return true;
        } 

        return false;
    }

    /**
     * Retrieves the {@link RealTimeAssessmentRules} from the provided condition.
     *
     * @param condition the condition that contains the {@link RealTimeAssessmentRules}.
     * @return the real time assessment rules found in the provided condition. Can be null if the
     *         condition doesn't have an input type or the type does not contain
     *         {@link RealTimeAssessmentRules}.
     */
    public RealTimeAssessmentRules getRealTimeAssessmentRules(Condition condition) {
        if (condition == null || condition.getInput() == null || condition.getInput().getType() == null) {
            return null;
        }

        Serializable conditionInputType = condition.getInput().getType();
        if (conditionInputType instanceof AvoidLocationCondition) {
            AvoidLocationCondition avoidLocation = (AvoidLocationCondition) conditionInputType;
            return avoidLocation.getRealTimeAssessmentRules();
        } else if (conditionInputType instanceof EliminateHostilesCondition) {
            EliminateHostilesCondition eliminateHostiles = (EliminateHostilesCondition) conditionInputType;
            return eliminateHostiles.getRealTimeAssessmentRules();
        } else if (conditionInputType instanceof HaltConditionInput) {
            HaltConditionInput haltCondition = (HaltConditionInput) conditionInputType;
            return haltCondition.getRealTimeAssessmentRules();
        } else if (conditionInputType instanceof IdentifyPOIsCondition) {
            IdentifyPOIsCondition identifyPOIs = (IdentifyPOIsCondition) conditionInputType;
            return identifyPOIs.getRealTimeAssessmentRules();
        } else if (conditionInputType instanceof LifeformTargetAccuracyCondition) {
            LifeformTargetAccuracyCondition lifeformTargetAccuracy = (LifeformTargetAccuracyCondition) conditionInputType;
            return lifeformTargetAccuracy.getRealTimeAssessmentRules();
        } else if (conditionInputType instanceof MuzzleFlaggingCondition) {
            MuzzleFlaggingCondition muzzleFlagging = (MuzzleFlaggingCondition) conditionInputType;
            return muzzleFlagging.getRealTimeAssessmentRules();
        } else if (conditionInputType instanceof RulesOfEngagementCondition) {
            RulesOfEngagementCondition rulesOfEngagement = (RulesOfEngagementCondition) conditionInputType;
            return rulesOfEngagement.getRealTimeAssessmentRules();
        } else if (conditionInputType instanceof SpeedLimitCondition) {
            SpeedLimitCondition speedLimit = (SpeedLimitCondition) conditionInputType;
            return speedLimit.getRealTimeAssessmentRules();
        } else if(conditionInputType instanceof NegligentDischargeCondition){
            NegligentDischargeCondition negDischarge = (NegligentDischargeCondition)conditionInputType;
            return negDischarge.getRealTimeAssessmentRules();
        }

        return null;
    }

    /**
     * Sets the {@link RealTimeAssessmentRules} into the provided condition.
     *
     * @param rtaRules the real time assessment rules to set into the condition.
     * @param condition the condition that contains the {@link RealTimeAssessmentRules}.
     */
    private void setRealTimeAssessmentRules(RealTimeAssessmentRules rtaRules, Condition condition) {
        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        } else if (condition.getInput() == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot have a null input.");
        } else if (condition.getInput().getType() == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot have a null input type.");
        }

        Serializable conditionInputType = condition.getInput().getType();
        if (conditionInputType instanceof AvoidLocationCondition) {
            AvoidLocationCondition avoidLocation = (AvoidLocationCondition) conditionInputType;
            avoidLocation.setRealTimeAssessmentRules(rtaRules);
        } else if (conditionInputType instanceof EliminateHostilesCondition) {
            EliminateHostilesCondition eliminateHostiles = (EliminateHostilesCondition) conditionInputType;
            eliminateHostiles.setRealTimeAssessmentRules(rtaRules);
        } else if (conditionInputType instanceof HaltConditionInput) {
            HaltConditionInput haltCondition = (HaltConditionInput) conditionInputType;
            haltCondition.setRealTimeAssessmentRules(rtaRules);
        } else if (conditionInputType instanceof IdentifyPOIsCondition) {
            IdentifyPOIsCondition identifyPOIs = (IdentifyPOIsCondition) conditionInputType;
            identifyPOIs.setRealTimeAssessmentRules(rtaRules);
        } else if (conditionInputType instanceof LifeformTargetAccuracyCondition) {
            LifeformTargetAccuracyCondition lifeformTargetAccuracy = (LifeformTargetAccuracyCondition) conditionInputType;
            lifeformTargetAccuracy.setRealTimeAssessmentRules(rtaRules);
        } else if (conditionInputType instanceof MuzzleFlaggingCondition) {
            MuzzleFlaggingCondition muzzleFlagging = (MuzzleFlaggingCondition) conditionInputType;
            muzzleFlagging.setRealTimeAssessmentRules(rtaRules);
        } else if (conditionInputType instanceof RulesOfEngagementCondition) {
            RulesOfEngagementCondition rulesOfEngagement = (RulesOfEngagementCondition) conditionInputType;
            rulesOfEngagement.setRealTimeAssessmentRules(rtaRules);
        } else if (conditionInputType instanceof SpeedLimitCondition) {
            SpeedLimitCondition speedLimit = (SpeedLimitCondition) conditionInputType;
            speedLimit.setRealTimeAssessmentRules(rtaRules);
        } else if(conditionInputType instanceof NegligentDischargeCondition){
            NegligentDischargeCondition negDischarge = (NegligentDischargeCondition)conditionInputType;
            negDischarge.setRealTimeAssessmentRules(rtaRules);
        }
    }

    /**
     * Retrieves the scoring rule within the provided real time assessment rules object that matches
     * the given type.
     *
     * @param condition the condition that contains the {@link RealTimeAssessmentRules}.
     * @param ruleType the scoring rule type to find within the real time assessment rules object's
     *        rules
     * @return the scoring rule that matches the provided type if it exists; null otherwise.
     */
    private Serializable findScoringRuleByType(Condition condition, ScoringRuleType ruleType) {
        if (ruleType == null) {
            throw new IllegalArgumentException("The parameter 'ruleType' cannot be null.");
        }

        RealTimeAssessmentRules rtaRules = getRealTimeAssessmentRules(condition);
        if (rtaRules == null) {
            return null;
        }

        switch (ruleType) {
        case COUNT:
            return rtaRules.getCount();
        case VIOLATION_TIME:
            return rtaRules.getViolationTime();
        default:
            throw new IllegalArgumentException("The parameter 'ruleType' with value '" + ruleType
                    + "' is not supported by real time assessment rules.");

        }
    }
}
