/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;

import generated.dkf.Condition;
import generated.dkf.Scoring;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ScoringRuleWidget.ScoringRuleCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.common.ScoringRuleWidget.ScoringRuleType;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;

/**
 * The widget that displays the optional overall assessment scoring rules for the condition.
 * 
 * @author sharrison
 */
public class OverallAssessmentScoringRulesWidget extends ScoringRulesWidgetWrapper {
    
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(OverallAssessmentScoringRulesWidget.class.getName());

    /**
     * Constructor. Uses all scoring rule types.
     */
    public OverallAssessmentScoringRulesWidget() {
        super(ScoringRuleType.values());
    }

    @Override
    public void populateWidget(final Condition parentCondition) {
        if (parentCondition == null) {
            throw new IllegalArgumentException("The parameter 'parentCondition' cannot be null.");
        }
        
        // disable scoring rule widgets that aren't applicable to this condition
        ScenarioClientUtility.getConditionsOverallAssessmentTypes(
                new AsyncCallback<Map<String, Set<String>>>() {

                    @Override
                    public void onSuccess(Map<String, Set<String>> overallAssessmentTypesConditionsMap) {

                        if (overallAssessmentTypesConditionsMap != null && 
                                !overallAssessmentTypesConditionsMap.isEmpty()) {
                            
                            List<ScoringRuleType> ruleTypes = new ArrayList<>(3);
                            String conditionImpl = parentCondition.getConditionImpl();
                            Set<String> overallAssessmentTypes = overallAssessmentTypesConditionsMap.get(conditionImpl);
                            if(overallAssessmentTypes != null){
                                
                                for(String overallAssessmentTypeClazz : overallAssessmentTypes){
                                    
                                    if(overallAssessmentTypeClazz.equals(generated.dkf.Count.class.getCanonicalName())){
                                        ruleTypes.add(ScoringRuleType.COUNT);
                                    }else if(overallAssessmentTypeClazz.equals(generated.dkf.ViolationTime.class.getCanonicalName())){
                                        ruleTypes.add(ScoringRuleType.VIOLATION_TIME);
                                    }else if(overallAssessmentTypeClazz.equals(generated.dkf.CompletionTime.class.getCanonicalName())){
                                        ruleTypes.add(ScoringRuleType.COMPLETION_TIME);
                                    }
                                }
                            }

                            if (logger.isLoggable(Level.INFO)) {
                                logger.info(
                                        "Updating the overall assessment widget based on the types available to the condition '"
                                                + conditionImpl + "' for the types "+ruleTypes+".");
                            }

                            // update widget 
                            enableScoringRuleWidgets(ruleTypes);
                        }
                    }

                    @Override
                    public void onFailure(Throwable thrown) {
                        logger.log(Level.SEVERE,
                                "The server failed to retrieve the conditions overall assessment types.",
                                thrown);
                    }
                });

        final Scoring scoring = parentCondition.getScoring();
        for (ScoringRuleWidget widget : widgetsInUse) {
            Serializable scoringRule = findScoringRuleByType(scoring, widget.getType());
            widget.populateWidget(scoringRule, parentCondition, new ScoringRuleCallback() {
                @Override
                public void onRemove(Serializable scoringRule) {
                    final Scoring scoring = parentCondition.getScoring();

                    // if no scoring object, nothing to remove
                    if (scoring == null) {
                        return;
                    }

                    // check if the removed rule is in the scoring object
                    if (scoring.getType().contains(scoringRule)) {
                        scoring.getType().remove(scoringRule);

                        // make schema valid if empty
                        if (scoring.getType().isEmpty()) {
                            parentCondition.setScoring(null);
                        }
                    }
                }

                @Override
                public void onAdd(Serializable scoringRule) {
                    Scoring scoring = parentCondition.getScoring();

                    // create the scoring object if it is null
                    if (scoring == null) {
                        scoring = new Scoring();
                        parentCondition.setScoring(scoring);
                    } else {
                        // check if the type already is in the scoring object
                        for (Serializable savedRule : scoring.getType()) {
                            if (savedRule.getClass() == scoringRule.getClass()) {
                                throw new UnsupportedOperationException("The scoring rule '" + scoringRule
                                        + "' cannot be added to the condition because one already exists of that type.");
                            }
                        }
                    }

                    // add it to the scoring object
                    scoring.getType().add(scoringRule);
                }
            });
        }
    }

    /**
     * Finds the scoring rule within the provided scoring object that matches the given type.
     * 
     * @param scoring the scoring object that contains the scoring rules
     * @param ruleType the scoring rule type to find within the scoring object's rules
     * @return the scoring rule that matches the provided type if it exists; null otherwise.
     */
    private Serializable findScoringRuleByType(Scoring scoring, ScoringRuleType ruleType) {
        if (ruleType == null) {
            throw new IllegalArgumentException("The parameter 'ruleType' cannot be null.");
        }

        if (scoring == null || scoring.getType() == null) {
            return null;
        }

        for (Serializable scoringRule : scoring.getType()) {
            if (ruleType.isCorrectType(scoringRule)) {
                return scoringRule;
            }
        }

        return null;
    }
}
