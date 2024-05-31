/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.util.Date;
import java.util.List;

import mil.arl.gift.common.gwt.client.ErrorDetails;
import net.customware.gwt.dispatch.shared.Action;

/**
 * Action that allows the user to download error details.
 * 
 * @author bzahid
 */
public class DownloadGatErrors implements Action<DownloadGatErrorsResult> {

	/** The username */
	private String userName;
	
	/** The name of the file being validated. Optional */
	private String fileName;
	
	/** The user-friendly message about the error. */
	private String reason;
	
	/** The list of developer-friendly issues. */
	private List<ErrorDetails> errorDetails;
	
	/** time at which the error was created (not when this class was created or when the error was downloaded)*/
	private Date atTime;
	
	/** name of the course where the error happened.  Optional */
	private String courseName;
	
	/** 
	 * Instantiates a new action. 
	 * Should not be explicitly called.  Needed for GWT serialization.
	 */
	public DownloadGatErrors() {
		super();
	}
	
	/** 
	 * Instantiates a new action.
	 * 
	 * @param userName The username. Cannot be null or empty.
	 * @param fileName The name of the file that was validated (optional).
	 * @param reason The user-friendly message about the error. Cannot be null.
	 * @param errorDetails contains a list of one or more errors with details and optional stack traces for each. Can't be null or empty.
	 * @param atTime time at which the error was created (not when this class was created or when the error was downloaded)
	 */
	public DownloadGatErrors(String userName, String fileName, String reason, List<ErrorDetails> errorDetails, Date atTime) {
		setUserName(userName);
		setFileName(fileName);
		setReason(reason);
		setDetails(errorDetails);
		setDate(atTime);
	}
	
	/**
	 * Get the name of the course where the error happened.
	 * 
	 * @return can be null (but not empty) if the error is not associated with a course.
	 */
    public String getCourseName() {
        return courseName;
    }

    /**
     * Set the name of the course the error happened in.
     * 
     * @param courseName can be null (but not empty) if the error is not associated with a course.
     */
    public void setCourseName(String courseName) {
        
        if(courseName != null && courseName.isEmpty()){
            throw new IllegalArgumentException("The course name can't be empty.");
        }
        
        this.courseName = courseName;
    }
	
	/**
	 * Gets the username
	 * 
	 * @return the username
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Sets the username 
	 * 
	 * @param userName
	 */
	public void setUserName(String userName) {
		
		if(userName == null || userName.isEmpty()) {
			throw new IllegalArgumentException("Username cannot be null or empty.");
		}
		
		this.userName = userName;
	}
	
	/**
	 * Gets the name of the file that was validated. 
	 * 
	 * @return the name of the file that was validated.
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Sets the name of the file that was validated. 
	 * 
	 * @param fileName the name of the file that was validated. Cannot be null or empty.
	 */
	public void setFileName(String fileName) {
		
		this.fileName = fileName;
	}

	/**
	 * Gets the user-friendly message about the error. 
	 * 
	 * @return the user-friendly message about the error.  Cannot be null.
	 */
	public String getReason() {
		return reason;
	}

	/**
	 * Sets the user-friendly message about the error. 
	 * 
	 * @param reason the user-friendly message about the error. Cannot be null.
	 */
	public void setReason(String reason) {
		if(reason == null) {
			throw new IllegalArgumentException("Reason cannot be null.");
		}
		
		this.reason = reason;
	}

	/**
	 * Gets the collection of developer-friendly issues.
	 * 
	 * @return collection of developer-friendly issues.  Won't be null.
	 */
	public List<ErrorDetails> getDetails() {
		return errorDetails;
	}

	/**
	 * Sets the developer-friendly message about the error.
	 * 
	 * @param errorDetails collection of developer-friendly issues Cannot be null.
	 */
	public void setDetails(List<ErrorDetails> errorDetails) {
		
		if(errorDetails == null) {
			throw new IllegalArgumentException("Details cannot be null.");
		}
		
        // remove any html formatting
        for (ErrorDetails errorDetail : errorDetails) {
            
            String detail = errorDetail.getDetails();
            detail = detail.replaceAll("<br/>", "\n");
            detail = detail.replaceAll("<img(.)*?>", "(image unavailable)");
            detail = detail.replaceAll("<(.)*?>", "");
        }
		
		this.errorDetails = errorDetails;
	}
	
	/**
	 * Get the time at which this error happened.
	 * 
	 * @return the time
	 */
	public Date getDate() {
        return atTime;
    }

	/**
	 * Set the time at which this error happened.
	 * 
	 * @param atTime time when the error happened.  Can't be null.
	 */
    private void setDate(Date atTime) {
        
        if(atTime == null){
            throw new IllegalArgumentException("Date cannot be null.");
        }
        
        this.atTime = atTime;
    }

    @Override
	public String toString() {
		
		StringBuilder sb = new StringBuilder();
		sb.append("[DownloadValidationErrors: ");
		sb.append("userName = ").append(userName);
		sb.append(", date = ").append(atTime);
		sb.append(", course = ").append(courseName);
		sb.append(", fileName = ").append(fileName);
		sb.append(", reason = ").append(reason);
		sb.append(", details = {\n");
		for(ErrorDetails details : errorDetails){
		    sb.append(details).append(",\n");
		}
		sb.append("}");
		sb.append("]");
		
		return sb.toString();
	}
	
}
