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

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.DetailsDialogBox;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DeleteExportFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DownloadGatErrors;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.DownloadGatErrorsResult;

/**
 * An extension of {@link DetailsDialogBox} with ability to download error details. 
 * 
 * @author bzahid
 */
public class ErrorDetailsDialog extends DetailsDialogBox {
	
	/** The callback for the download operation. */
	protected AsyncCallback<DownloadGatErrorsResult> callback;
    
	/**
     * Creates a error details dialog box with expandable details and a stack trace.<br/>
     * The contents can be written to a file and downloaded.<br/>
     * Note: used to display a single error
     * 
     * @param reason The user-friendly message about the error. 
     * @param details The developer-friendly message about the error.
     * @param stackTrace The stack trace of the exceptions thrown.
     */
    public ErrorDetailsDialog(String reason, String details, List<String> stackTrace) {
        super(reason, new ErrorDetails(details, stackTrace), GatClientUtility.getCourseName());
        
        initCallback();
    }
	
    /**
     * Creates a error details dialog box with expandable details and a stack
     * trace. The contents can be written to a file and downloaded.
     * 
     * @param reason The user-friendly message about the error.
     * @param courseValidationResults the course validation results.  Can't be null.
     * @param isSingleObject whether the validation issue is for a course object (true) or the entire course/file (false)
     */
    public ErrorDetailsDialog(String reason, CourseValidationResults courseValidationResults, boolean isSingleObject) {
        this(reason, courseValidationResults, isSingleObject, null);
    }
    
    /**
     * Creates a error details dialog box with expandable details and a stack
     * trace. The contents can be written to a file and downloaded.
     * 
     * @param reason The user-friendly message about the error.
     * @param courseValidationResults the course validation results.  Can't be null.
     * @param isSingleObject whether the validation issue is for a course object (true) or the entire course/file (false)
     * @param helpMessage custom opening help text for the dialog. If null, a default message will be used.
     */
    public ErrorDetailsDialog(String reason, CourseValidationResults courseValidationResults, boolean isSingleObject, String helpMessage) {
        super(reason, courseValidationResults, GatClientUtility.getCourseName(), isSingleObject, helpMessage);

        initCallback();
    }
	
	private void initCallback() {
	    callback = new AsyncCallback<DownloadGatErrorsResult> () {

            @Override
            public void onFailure(Throwable cause) {
                
                downloadButton.setEnabled(true);
                
                // show a dialog indicating the file creation failed                        
                
                // format the stack trace
                ArrayList<String> stackTrace = new ArrayList<String>();                                                                     
                if(cause.getStackTrace() != null) {
                    stackTrace.add(cause.toString());
                    
                    for(StackTraceElement e : cause.getStackTrace()) {
                        stackTrace.add("at " + e.toString());
                    }
                }
                
                String details;
                if(cause.getMessage() != null){
                    details = cause.getMessage();
                }else{
                    details = "An exception was thrown while trying to create the file containing the errors.";
                }
                
                ErrorDetailsDialog errorDialog = new ErrorDetailsDialog("Failed to write errors to a downloadable file.", details, stackTrace);
                
                errorDialog.setText("Download Failed");
                errorDialog.setCloseable(true);
                errorDialog.setGlassEnabled(true);
                errorDialog.center();
            }

            @Override
            public void onSuccess(DownloadGatErrorsResult result) {
                
                downloadButton.setEnabled(true);
                
                if(result.isSuccess()) {

                    final DeleteExportFile action = new DeleteExportFile();
                    action.setUserName(GatClientUtility.getUserName());
                    action.setDownloadUrl(result.getDownloadUrl());
                    action.setLocationOnServer(result.getRelativeFilePath());

                    // show a dialog indicating the file is ready for download
                    // delete the file when the dialog is closed
                    showSuccessDialog(result.getDownloadUrl(), new CloseHandler<PopupPanel>() {

                        @Override
                        public void onClose(CloseEvent<PopupPanel> arg0) {
                            
                            SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GatServiceResult>() {

                                @Override
                                public void onFailure(Throwable arg0) {
                                    // Nothing to do                                
                                }

                                @Override
                                public void onSuccess(GatServiceResult arg0) {
                                    // Nothing to do
                                }
                                
                            });
                        }
                        
                    });
                    
                } else {
                    // show a dialog indicating the file creation failed
                    showFailureDialog(result.getErrorMsg());
                }
            }                   
        };
	}
	
	@Override
	public void startDownload() {
		DownloadGatErrors action = new DownloadGatErrors(GatClientUtility.getUserName(), null, reason, courseErrorDetails, getDate());	
		action.setCourseName(getCourseName());
		SharedResources.getInstance().getDispatchService().execute(action, callback);
	}

}
