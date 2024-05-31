/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * A representation of the amount of progress made by an ongoing RPC service
 * 
 * @author nroberts
 */
public class ProgressIndicator implements IsSerializable{
	
	/** a message indicating the current status in the overall progress of the service associated with this progress indicator */
	private String overallStatus;
	
	/** a message indicating the current status in an individual step of the service associated with this progress indicator */
	private String status;
	
	/** the percent of the service associated with this progress indicator that has already been completed*/
	private float percent;
	
	/** whether or not the service associated with this progress indicator has finished */
	private boolean finished;
	
	/** contains any additional information about the generated report */
	private String finishedAdditionalDetails = null;
	
	/** an exception thrown by the service associated with this progress indicator if one occurs */
	private Throwable exception;
	
	/** a string representation of the current stack trace at the time of an exception*/
	private String stackTraceMessage;
	
	/**
	 * Class Constructor - creates a new ProgressIndicator
	 */
	public ProgressIndicator(){
		overallStatus = "";
		status = "";
		percent = 0;
		finished = false;
	}
	
	/**
	 * Class Constructor - creates a new ProgressIndicator with the specified overall status message, status message, and starting percent
	 * 
	 * @param startOverallStatus A message indicating the overall starting status
	 * @param startStatus A message indicating the starting status of an individual step
	 * @param startPercent The percent value with which this progress indicator will start
	 */
	public ProgressIndicator(String startOverallStatus, String startStatus, float startPercent){
		overallStatus = startOverallStatus;
		status = startStatus;
		percent = startPercent;
		finished = false;
	}
	
	/**
	 * Updates the data currently stored regarding the service associated with this progress indicator
	 * 
	 * @param newOverallStatus A new overall status message. Set to null if the overall status message should not be updated
	 * @param newStatus A new status message. Set to null if the status message should not be updated
	 * @param percentIncrement A percent value by which the current percent completed should be incremented
	 */
	public void updateProgress(String newOverallStatus, String newStatus, float percentIncrement){
		
		if(newOverallStatus != null){
			overallStatus = newOverallStatus;
		}
		
		if(newStatus != null){
			status = newStatus;
		}
		
		percent += percentIncrement;
	}
	
	/**
	 * Resets the data currently stored regarding the service associated with this progress indicator
	 */
	public void resetProgress(){
		overallStatus = "";
		status = "";
		percent = 0;
		finished = false;
		exception = null;
		stackTraceMessage = null;
	}
	
	/**
	 * Gets the percent completed of the service associated with this progress indicator
	 * 
	 * @return float - The current percent completed
	 */
	public float getPercent(){
		return percent;
	}
	
	/**
	 * Sets the percent completed of the service associated with this progress indicator
	 * 
	 * @param newPercent The percent value to which the percent completed should be set
	 */
	public void setPercent(float newPercent){
		percent = newPercent;
	}
	
	/**
	 * Gets whether or not the service associated with this progress indicator has finished
	 * 
	 * @return Boolean A true or false value indicating whether or not the service has finished
	 */
	public boolean isFinished(){
		return finished;
	}
	
	/**
	 * Sets the completion status of the service associated with this progress indicator
	 * 
	 * @param completed A boolean indicating whether or not the service has finished
	 */
	public void setFinished(boolean completed){
		finished = completed;
	}
	
	/**
	 * Set any additional information about the generated report that will be presented
	 * on the "report finished" dialog on the ERT client.
	 * 
	 * @param details additional information about the generated report
	 */
	public void setFinishedAdditionalDetails(String details){
	    finishedAdditionalDetails = details;
	}
	
	/**
	 * Return any additional information about the generated report.
	 * 
	 * @return String
	 */
	public String getFinishedAdditionalDetails(){
	    return finishedAdditionalDetails;
	}
	
	/**
	 * Gets the exception thrown while executing the service associated with this progress indicator, if such an exception exists.
	 * 
	 * @return Throwable - An exception thrown by the service. Null if such an exception does not exist.
	 */
	public Throwable getException(){
		return exception;
	}
	
	/**
	 * Stores an exception for the service associated with this progress indicator
	 * 
	 * @param ex The exception to be stored
	 */
	public void setException(Throwable ex){
		exception = ex;		
	}
	
	/**
	 * Gets the string representation of the stack trace for the service associated with this progress indicator
	 * 
	 * @return String - A message representing the stack trace of an service.
	 */
	public String getStackTraceMessage(){
		return stackTraceMessage;
	}
	
	/**
	 * Sets the stack trace message for the service associated with this progress indicator
	 * 
	 * @param st A string representing the service's stack trace
	 */
	public void setStackTraceMessage(String st){
		stackTraceMessage = st;		
	}
	
	/**
	 * Gets a HTML message indicating the current progress of the service associated with this progress indicator
	 * 
	 * @return String - A HTML message indicating the current progress of an ongoing service
	 */
	public String getProgressMessage(){
		return "<b>"+  overallStatus + "</b><br>" + status;
	}
	
}
