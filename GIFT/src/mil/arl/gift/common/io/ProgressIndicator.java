/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;

/**
 * An object representing the current progress of a task being executed
 * 
 * @author nroberts
 */
public class ProgressIndicator implements Serializable{

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_TASK_DESC = "Calculating...";
	
	/** The percent of the task that is complete */
	private int percentComplete;
	
	/** A description of the task currently being executed */
	private String taskDescription = DEFAULT_TASK_DESC;
	
	/** Whether or not the task currently being executed should be cancelled */
	private boolean shouldCancel = false;
	
	/** Whether or not the operation being represented is complete*/
	private boolean isComplete = false;
	
	/** An exception that may be thrown by the operation upon failure */
	private DetailedExceptionSerializedWrapper exception;
	
	/** this indicator's subtask indicator (optional) */
	private ProgressIndicator subtaskProgressIndicator = null;
	
	/** used to help determine how long a process took to complete */
	private long creationTime = System.currentTimeMillis();

	/**
	 * Creates a new progress indicated with 0% completion and no task description.
	 */
	public ProgressIndicator(){
		this.setPercentComplete(0);
	}
	
	/**
	 * Creates a new progress indicated with the specified percent completion and no task description.
	 * 
	 * @param percentComplete the percent of the task that is complete
	 */
	public ProgressIndicator(int percentComplete){
		this.setPercentComplete(percentComplete);
	}
	
	/**
	 * Creates a new progress indicated with 0% completion and the specified task description.
	 * 
	 * @param taskDescription a description of the task currently being executed
	 */
	public ProgressIndicator(String taskDescription){
		this.setPercentComplete(0);
		this.setTaskDescription(taskDescription);
	}
	/**
	 * Creates a new progress indicated with the specified percent completion and the specified task description.
	 * 
	 * @param percentComplete the percent of the task that is complete
	 * @param taskDescription a description of the task currently being executed
	 */
	public ProgressIndicator(int percentComplete, String taskDescription){
		this.setPercentComplete(percentComplete);
		this.setTaskDescription(taskDescription);
	}

	/**
	 * Gets the percent of the task that is complete
	 * 
	 * @return the percent of the task that is complete
	 */
	public int getPercentComplete() {
		return percentComplete;
	}

	/**
	 * Sets the percent of the task that is complete
	 * 
	 * @param percentComplete the percent of the task that is complete
	 */
	public void setPercentComplete(int percentComplete) {
		this.percentComplete = percentComplete;
	}
	
	/**
	 * Increases the existing percent complete value by the specified amount.
	 * 
	 * @param percentToIncrease the amount to increase the percent complete
	 */
	public void increasePercentComplete(int percentToIncrease){
	    this.percentComplete += percentToIncrease;
	}

	/**
	 * Gets the description of the task currently being executed
	 * 
	 * @return the description of the task currently being executed
	 */
	public String getTaskDescription() {
		return taskDescription;
	}

	/**
	 * Sets the description of the task currently being executed
	 * 
	 * @param taskDescription the description of the task currently being executed.  If null or empty string
	 * the default description of {@link #DEFAULT_TASK_DESC} is used.
	 */
	public void setTaskDescription(String taskDescription) {
	    
	    if(taskDescription == null){
	        this.taskDescription = DEFAULT_TASK_DESC;
	    }else{
	        this.taskDescription = taskDescription;
	    }
	}
	
	/**
	 * Gets the progress indicator for the current subtask
	 * 
	 * @return the progress indicator for the current subtask.  Can be null.
	 */
	public ProgressIndicator getSubtaskProcessIndicator(){
		return subtaskProgressIndicator;
	}
	
	/**
	 * Sets the progress indicator for the current subtask
	 * 
	 * @param subtaskProgressIndicator the progress indicator for the current subtask
	 */
	public void setSubtaskProgressIndicator(ProgressIndicator subtaskProgressIndicator){
		this.subtaskProgressIndicator = subtaskProgressIndicator;
	}
	
	/**
	 * Updates the task description of this indicator's subtask.  If the subtask
	 * doesn't exist it is created.
	 * 
	 * @param taskDescription the description of the task
	 */
	public void updateSubtaskDescription(String taskDescription){
	    
	    if(subtaskProgressIndicator == null){
	        subtaskProgressIndicator =  new ProgressIndicator();
	    }
	    
	    subtaskProgressIndicator.setTaskDescription(taskDescription);
	}
	
	/**
	 * Updates the percent complete of the progress indicator of this indicator's subtask.  If the subtask
	 * doesn't exist it is created.
	 * 
	 * @param percentToIncrease the amount to increase the subtask percent complete amount by.
	 */
	public void increaseSubtaskProgress(int percentToIncrease){
	    
        if(subtaskProgressIndicator == null){
            subtaskProgressIndicator =  new ProgressIndicator();
        }
	       
	    subtaskProgressIndicator.increasePercentComplete(percentToIncrease);
	}
	
	/**
	 * Set the percent complete of the progress indicator of this indicator's subtask.  If the subtask
	 * doesn't exist it is created.
	 * 
	 * @param percentComplete the value to set the subtask percent complete to.
	 */
	public void setSubtaskProgress(int percentComplete){
	    
        if(subtaskProgressIndicator == null){
            subtaskProgressIndicator =  new ProgressIndicator();
        }
           
        subtaskProgressIndicator.setPercentComplete(percentComplete);
	}
	
	/**
	 * Return the percent complete of the progress indicator of this indicator's subtask.
	 * 
	 * @return the subtask percent complete value.  Zero is returned if the subtask indicator doesn't exist.
	 */
	public int getSubtaskProgress(){
	    return subtaskProgressIndicator == null ? 0 : subtaskProgressIndicator.getPercentComplete();	    
	}

	/**
	 * Gets whether or not the task currently being executed should be cancelled
	 * 
	 * @return  whether or not the task currently being executed should be cancelled
	 */
	public synchronized boolean shouldCancel() {
		return shouldCancel;
	}

	/**
	 * Sets  whether or not the task currently being executed should be cancelled
	 * 
	 * @param shouldCancel  whether or not the task currently being executed should be cancelled
	 */
	public synchronized void setShouldCancel(boolean shouldCancel) {
		this.shouldCancel = shouldCancel;
	}
	
	/**
	 * Gets whether or not the operation being represented is complete
	 * 
	 * @return the isComplete whether or not the operation being represented is complete
	 */
	public boolean isComplete() {
		return isComplete;
	}

	/**
	 * Sets whether or not the operation being represented is complete
	 * 
	 * @param isComplete whether or not the operation being represented is complete
	 */
	public void setComplete(boolean isComplete) {
		this.isComplete = isComplete;
	}

	/**
	 * Gets the exception that was thrown by the operation if the operation failed
	 * 
	 * @return the exception that was thrown by the operation
	 */
	public DetailedExceptionSerializedWrapper getException() {
		return exception;
	}

	/**
	 * Sets the exception that was thrown by the operation upon failure
	 * 
	 * @param exception the exception that was thrown
	 */
	public void setException(DetailedException exception) {
		this.exception = new DetailedExceptionSerializedWrapper(exception);
	}
	
	/**
	 * Return the time at which this indicator instance was created.  This is useful
	 * for determining how long a process took to complete.  Even though this time value
	 * is not a direct correlation of when the process started, we normally create an instance
	 * of this class right before work is started.
	 * 
	 * @return the epoch time when this class was instantiated
	 */
	public long getCreationTime(){
	    return creationTime;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("[ProgressIndicator: ");
		sb.append("percentComplete = " ).append( percentComplete).append( ", ");
		sb.append("taskDescription = " ).append( (taskDescription != null ? taskDescription : "null")).append( ", ");
		sb.append("shouldCancel = " ).append( shouldCancel ).append( ", ");
		sb.append("isComplete = " ).append( isComplete ).append( ", ");
		sb.append("creationTime = ").append(creationTime).append( ", ");
		sb.append("exception = " ).append( (exception != null ? exception.toString() : "null") ).append( ", ");
		sb.append("subtaskProgressIndicator = " ).append( (subtaskProgressIndicator != null ? subtaskProgressIndicator.toString() : "null"));
		sb.append("]");
		
		return sb.toString();
	}
}
