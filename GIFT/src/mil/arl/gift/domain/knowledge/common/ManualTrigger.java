/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

/**
 * Represents a trigger that isn't activated by an event but a user action (e.g.
 * the activate/deactivate task button is clicked in the Game Master).
 *
 * @author tflowers
 *
 */
public class ManualTrigger extends AbstractTrigger {

    /**
     * Constructs a {@link ManualTrigger} without a delay.
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     */
    public ManualTrigger(String triggerName) {
        super(triggerName);
    }

    /**
     * Constructs a {@link ManualTrigger} with a delay.
     *
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     * @param triggerDelayInSeconds The delay to add to the trigger measured in seconds.
     */
    public ManualTrigger(String triggerName, float triggerDelayInSeconds) {
        super(triggerName, triggerDelayInSeconds);
    }

    @Override
    public String toString() {
        return new StringBuilder("[ManualTrigger: ").append(super.toString()).append("]").toString();
    }
}
