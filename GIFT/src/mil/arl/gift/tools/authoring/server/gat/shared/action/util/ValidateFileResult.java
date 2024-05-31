/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.ArrayList;
import java.util.Date;

import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The result of a {@link ValidateFile} action.
 * 
 * @author bzahid
 */
public class ValidateFileResult extends GatServiceResult {
		 
	/** The file that caused the exception. */
	private String fileName;
	
	/** User-friendly information about the exception. */
	private String reason;
	
	/** Developer-friendly information about the exception. */
	private String details;
	
	/** The cause of the exception. */
	private ArrayList<String> stackTrace;
	
	/** The course validation results containing specific errors about the course. */
	private CourseValidationResults courseValidationResults;
	
    /**
     * the date of the last successful validation
     * Will be null if the validation failed.
     * Currently will also be null if the file being validated is not the course.
     */
    private Date lastSuccessfulValidationDate = null;
	
	/**
	 * Default public constructor.
	 */
	public ValidateFileResult() {
		super();
	}
	
	/**
	 * Constructor
	 * 
	 * @param filename - the file name of the file being validated that caused the exception. Can be null.
	 * @param reason - user friendly message about the exception. Can't be null.
     * @param details - developer friendly message about the exception. Can be null.
     * @param stackTrace - the stack trace of the exception thrown.
     */
	public ValidateFileResult(String fileName, String reason, String details, ArrayList<String> stackTrace) {
		this.fileName = fileName;
		this.reason = reason;
		this.details = details;
		this.stackTrace = stackTrace;
	}

	/**
	 * Gets the name of the file being validated that caused the exception.
	 * 
	 * @return the file name that caused the exception. Can be null.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the name of the file being validated that caused the exception.
	 * 
	 * @param fileName - the file name that caused the exception. Can be null.
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Gets the user-friendly information about the exception.
	 * 
	 * @return the user-friendly message about the exception.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Sets the user-friendly information about the exception.
	 * 
	 * @param reason - the user-friendly message about the exception.
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}

	/**
	 * Gets the developer-friendly information about the exception.
	 * 
	 * @return the developer-friendly message about the exception. Can be null.
	 */
	public String getDetails() {
		return details;
	}

	/**
	 * Sets the developer-friendly information about the exception.
	 * 
	 * @param details - the developer-friendly message about the exception. Can be null.
	 */
	public void setDetails(String details) {
		this.details = details;
	}

	/**
	 * Gets the stack trace of the exception thrown.
	 * 
	 * @return the stack trace of the reported exception.
	 */
	public ArrayList<String> getStackTrace() {
		return stackTrace;
	}

	/**
	 * Sets the stack trace of the exception thrown.
	 * 
	 * @param stackTrace - the stack trace of the reported exception
	 */
	public void setStackTrace(ArrayList<String> stackTrace) {
		this.stackTrace = stackTrace;
	}

	/**
	 * Sets the course validation results to be used by the client 
	 * @param courseValidationResults The course validation results
	 */
	public void setCourseValidationResults(CourseValidationResults courseValidationResults) {
		this.courseValidationResults = courseValidationResults;
	}
	
	/**
	 * Gets the course validation results
	 * @return The course validation results. Can be null.
	 */
	public CourseValidationResults getCourseValidationResults() {
		return courseValidationResults;
	}
	
    /**
     * Return the date of the last successful validation.
     * 
     * @return will be null if any of the following are true:</br>
     * 1. this validation result contains issues
     * 2. this validation result is for a GIFT XML file other than a course.xml
     */
    public Date getLastSuccessfulValidationDate() {
        return lastSuccessfulValidationDate;
    }

    /**
     * Set the date of the last successful validation of the course.
     * 
     * @param lastSuccessfulValidationDate the date of the last successful validation.  Use null to indicate
     * the course was not successfully validated.
     */
    public void setLastSuccessfulValidationDate(Date lastSuccessfulValidationDate) {
        this.lastSuccessfulValidationDate = lastSuccessfulValidationDate;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ValidateFileResult: ");
        builder.append(super.toString());
        builder.append(", fileName=");
        builder.append(fileName);
        builder.append(", reason=");
        builder.append(reason);
        builder.append(", details=");
        builder.append(details);
        builder.append(", stackTrace=");
        builder.append(stackTrace);
        builder.append(", courseValidationResults=");
        builder.append(courseValidationResults);
        builder.append(", lastSuccessfulValidationDate=");
        builder.append(lastSuccessfulValidationDate);
        builder.append("]");
        return builder.toString();
    }
    
    

}
