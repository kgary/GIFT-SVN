/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Label;

import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DownloadGatErrors;

/**
 * An extension of {@link ErrorDetailsDialog} with the ability to download validation errors. 
 * 
 * @author bzahid
 */
public class FileValidationDialog extends ErrorDetailsDialog {
		
    /**
     * Creates a validation error dialog box with expandable details and a stack trace.<br/>
     * The contents can be written to a file and downloaded.<br/>
     * Note: used to display a single error
     * 
     * @param fileName The name of the file that was validated.
     * @param reason The user-friendly message about the error.  Can't be null or empty.
     * @param details The developer-friendly message about the error.  Can't be null or empty.
     * @param stackTrace The stack trace of the exception thrown. Can be null but not empty.
     */
    public FileValidationDialog(String fileName, String reason, String details, ArrayList<String> stackTrace) {
        super(reason, details, stackTrace);
        
        if(fileName != null) {
            // display the filename
            
            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            Label fileNameLabel = new Label("File: " + fileName);
            fileNameLabel.getElement().getStyle().setProperty("fontWeight", "bold");
            fileNameLabel.getElement().getStyle().setProperty("fontSize", "14px");
            
            setContentHeader(fileNameLabel);
        }
        
        ArrayList<ErrorDetails> errorDetails = new ArrayList<ErrorDetails>(1);
        errorDetails.add(new ErrorDetails(details, stackTrace));
        
        setDownloadHandler(fileName, reason, errorDetails);
    }
	
	/**
     * Creates a validation error dialog box with expandable details and a stack trace.
     * The contents can be written to a file and downloaded.
     * 
     * @param fileName The name of the file that was validated.
     * @param reason The user-friendly message about the error. 
     * @param courseValidationResults the course validation results.  Can't be null.
     * @param isSingleObject whether the validation issue is for a course object (true) or the entire course/file (false)
     */
    public FileValidationDialog(String fileName, String reason, CourseValidationResults courseValidationResults, boolean isSingleObject) {
        super(reason, courseValidationResults, isSingleObject);

        if (fileName != null) {
            // display the filename

            fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
            Label fileNameLabel = new Label("File: " + fileName);
            fileNameLabel.getElement().getStyle().setProperty("fontWeight", "bold");
            fileNameLabel.getElement().getStyle().setProperty("fontSize", "14px");

            setContentHeader(fileNameLabel);
        }
        
        setDownloadHandler(fileName, reason, buildCourseErrorDetails(courseValidationResults));
    }

	/** 
	 * Sets the click handler that allows validation errors to be downloaded.
	 * 
	 * @param fileName The name of the file that was validated.
	 * @param reason The user-friendly message about the error. 
	 * @param errorDetails collection of issues to be able to download.  These issues contain details and optional stack traces.
	 */
	private void setDownloadHandler(final String fileName, 
			final String reason, final List<ErrorDetails> errorDetails) {
		
		setNewDownloadHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent e) {
								
				DownloadGatErrors action = new DownloadGatErrors(GatClientUtility.getUserName(), fileName, reason, errorDetails, getDate());
				action.setCourseName(getCourseName());
				downloadButton.setEnabled(false);
				
				SharedResources.getInstance().getDispatchService().execute(action, callback);
			}
		});
	}
	
}
