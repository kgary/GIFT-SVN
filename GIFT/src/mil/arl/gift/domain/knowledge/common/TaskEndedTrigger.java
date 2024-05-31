/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.domain.knowledge.Task;

/**
 * This trigger is used to determine if a specified task has finished.
 * 
 * @author mzellars
 *
 */
public class TaskEndedTrigger extends AbstractTrigger {

	/** instance of the logger */
	private static Logger logger = LoggerFactory.getLogger(TaskEndedTrigger.class);
	
	/** the task looking for a finish state */
	private generated.dkf.Task task;

	/**
	 * Class constructor - set attributes
	 * 
	 * @param triggerName - the name of the trigger used for display purposes.  Can't be null or empty.
	 * @param task the task looking for a finish state
	 */
	public TaskEndedTrigger(String triggerName, generated.dkf.Task task) {
	    super(triggerName);
		
		if(task == null){
			throw new IllegalArgumentException("The task can't be null");
		}
		
		this.task = task;
	}

    @Override
    public boolean shouldActivate(Task changedTask, Task taskToActivate) {
        if (changedTask == null) {
            throw new IllegalArgumentException("The parameter 'changedTask' cannot be null.");
        }

        boolean activate = false;
        BigInteger changedTaskNodeId = BigInteger.valueOf(changedTask.getNodeId());
        boolean isNodeIdEqual = changedTaskNodeId.compareTo(this.task.getNodeId()) == 0;
        if (isNodeIdEqual && changedTask.isFinished()) {
            activate = true;
            if (logger.isDebugEnabled()) {
                logger.debug("Activating " + this + " because changedTask finished.");
            }
        }

        return activate;
    }

	@Override
	public String toString(){
		
		StringBuffer sb = new StringBuffer();
		sb.append("[TaskEndedTrigger: ");
		sb.append(super.toString());
		sb.append(", task = ").append(task);
		sb.append("]");
		
		return sb.toString();
	}

}
