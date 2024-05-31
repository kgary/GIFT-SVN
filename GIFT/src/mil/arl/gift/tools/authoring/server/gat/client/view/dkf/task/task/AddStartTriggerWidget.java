/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.task;

import java.io.Serializable;
import java.math.BigDecimal;

import generated.dkf.EntityLocation;
import generated.dkf.ScenarioStarted;
import generated.dkf.StartTriggers;
import generated.dkf.StartTriggers.Trigger;
import generated.dkf.StartTriggers.Trigger.TriggerMessage;
import generated.dkf.Strategy;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioValidatorUtility;

/**
 * The inline editor for adding or editing a Start Trigger.
 * 
 * @author sharrison
 */
public class AddStartTriggerWidget extends AddTriggerWidget<StartTriggers.Trigger> {

    /**
     * Constructor.
     */
    public AddStartTriggerWidget() {
        super();

        ruleLabel.setText("This event should start this task when...");
        delayLabel.setText("Wait an additional");
        delayLabel2.setText("second(s) before starting this task.");

        showFeedbackUI(true);
    }

    @Override
    protected void applyEdits(StartTriggers.Trigger trigger) {
        if (trigger == null) {
            throw new IllegalArgumentException("The parameter 'trigger' cannot be null.");
        }

        trigger.setTriggerType(getTriggerTypeFromInput());
        trigger.setTriggerDelay(getDelayFromInput());
        
        Strategy strategy = getStrategy();
        if(strategy != null && !strategy.getStrategyActivities().isEmpty()) {
        	trigger.setTriggerMessage(new StartTriggers.Trigger.TriggerMessage());
        	trigger.getTriggerMessage().setStrategy(strategy);
        }else {
        	trigger.setTriggerMessage(null);
        }

        /* The team references may have changed, so update the global reference
         * map */
        if (trigger.getTriggerType() instanceof EntityLocation) {
            ScenarioClientUtility.gatherTeamReferences();
        }
    }

    @Override
    protected Serializable getTriggerChoice(StartTriggers.Trigger trigger) {
        return trigger != null ? trigger.getTriggerType() : null;
    }

    @Override
    protected BigDecimal getDelay(StartTriggers.Trigger trigger) {
        return trigger != null ? trigger.getTriggerDelay() : null;
    }

    @Override
    protected TriggerMessage getTriggerMessage(StartTriggers.Trigger trigger) {
        return trigger != null ? trigger.getTriggerMessage() : null;
    }
    
    @Override
    public void validate(ValidationStatus validationStatus) {
        if (startTriggerSelectionValidation.equals(validationStatus)) {
            /* Validate when loading start triggers from data model - can only
             * have 1 scenario started task start trigger per task */

            TriggerType type = TriggerType.getTypeByText(triggerCondition.getSelectedItemText());

            /* Only scenario start type cannot have duplicates */
            if (!TriggerType.SCENARIO_STARTS.equals(type)) {
                startTriggerSelectionValidation.setValid();
                return;
            }

            /* Since scenario start is selected, if another one is found then a
             * duplicate exists and the validation fails */
            boolean foundDuplicate = false;
            for (Trigger item : getParentItemListEditor().getItems()) {
                /* Skip the trigger we are currently editing */
                if (editedTrigger == item) {
                    continue;
                }

                if (item.getTriggerType() instanceof ScenarioStarted) {
                    foundDuplicate = true;
                    break;
                }
            }

            startTriggerSelectionValidation.setValidity(!foundDuplicate);
        } else {
            super.validate(validationStatus);
        }
    }

    @Override
    protected boolean validate(Trigger trigger) {
        String errMsg = ScenarioValidatorUtility.validateStartTrigger(trigger);
        if (StringUtils.isNotBlank(errMsg)) {
            return false;
        }

        if (trigger.getTriggerType() instanceof ScenarioStarted) {
            /* Since scenario start is selected, if another one is found then a
             * duplicate exists and the validation fails */
            for (Trigger item : getParentItemListEditor().getItems()) {
                /* Skip the trigger we are currently evaluating */
                if (trigger == item) {
                    continue;
                }

                if (item.getTriggerType() instanceof ScenarioStarted) {
                    return false;
                }
            }
        }

        return true;
    }
}
