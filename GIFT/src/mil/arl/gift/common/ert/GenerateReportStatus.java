/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

import java.io.Serializable;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ProgressIndicator;

/**
 * The status of an ongoing report generation
 * 
 * @author nroberts
 */
public class GenerateReportStatus implements Serializable{
	
	private static final long serialVersionUID = 2254050799203712905L;

	/** The report progress*/
	private ProgressIndicator progress;
	
	/** contains any additional information about the generated report */
	private String finishedAdditionalDetails = null;
	
	/** an exception thrown by the service associated with this progress indicator if one occurs */
	private DetailedException exception;
	
	/** a string representation of the current stack trace at the time of an exception*/
	private String stackTraceMessage;
	
	/** Whether or not report generation has finished*/
	boolean finished = false;
	
	/** A reference to the report file, if one exists*/
	private DownloadableFileRef reportResult;
	
	/**
	 * Class Constructor - creates a new ProgressIndicator
	 */
	public GenerateReportStatus(){
		
	}
	
	/**
	 * Class Constructor - creates a new ProgressIndicator
	 * @param indicator the new value of progress
	 */
	public GenerateReportStatus(ProgressIndicator indicator){
		this.progress = indicator;
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
	 * @return An exception thrown by the service. Null if such an exception does not exist.
	 */
	public DetailedException getException(){
		return exception;
	}
	
	/**
	 * Stores an exception for the service associated with this progress indicator
	 * 
	 * @param ex The exception to be stored
	 */
	public void setException(DetailedException ex){
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
	 * Gets the report progress
	 * 
	 * @return the report progress
	 */
	public ProgressIndicator getProgress() {
		return progress;
	}

	/**
	 * Sets the report progress
	 * 
	 * @param progress the report progress
	 */
	public void setProgress(ProgressIndicator progress) {
		this.progress = progress;
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
	 * Gets a reference to the export file
	 * 
	 * @return a reference to the export file
	 */
	public DownloadableFileRef getReportResult() {
		return reportResult;
	}

	/**
	 * Sets the reference to the export file
	 * 
	 * @param reportResult the reference to the export file
	 */
	public void setReportResult(DownloadableFileRef reportResult) {
		this.reportResult = reportResult;
	}
}
