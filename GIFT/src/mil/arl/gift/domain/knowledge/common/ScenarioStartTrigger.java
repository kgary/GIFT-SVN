/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import mil.arl.gift.domain.knowledge.Task;

/**
 * A trigger that is activated when the provided task has never started before.
 * 
 * @author mhoffman
 *
 */
public class ScenarioStartTrigger extends AbstractTrigger {
    
    /**
     * set trigger name
     * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
     */
    public ScenarioStartTrigger(String triggerName) {
        super(triggerName);
    }

    @Override
    public boolean shouldActivate(Task changedTask, Task taskToActivate) {
        if (taskToActivate == null) {
            throw new IllegalArgumentException("The parameter 'taskToActivate' cannot be null.");
        }

        /* The 'taskToActivate' has not been completed (i.e. activated before)
         * and is not active now */
        return !taskToActivate.isFinished() && !taskToActivate.isActive();
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder("[ScenarioStartTrigger]");
        return sb.toString();
    }
}
