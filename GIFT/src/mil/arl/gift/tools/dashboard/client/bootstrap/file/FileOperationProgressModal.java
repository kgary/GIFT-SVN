/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.file;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Text;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.rpcs.CopyCourseResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.ProgressResponse;

/**
 * An extension of ModalDialogBox that displays progress for 
 * a file operation such as delete, copy, or move.
 * 
 * @author bzahid
 */
public class FileOperationProgressModal extends Composite {
    
    private static Logger logger = Logger.getLogger(FileOperationProgressModal.class.getName());
    
    private static FileOperationProgressModalUiBinder uiBinder = GWT.create(FileOperationProgressModalUiBinder.class);
    
    interface FileOperationProgressModalUiBinder extends UiBinder<Widget, FileOperationProgressModal> {
        
    }
    
    @UiField
    protected ProgressBarListEntry progress;

    @UiField
    protected Modal fileProgressDialog;
        
    @UiField 
    protected Text heading;
    
    private static final String DELETE = "delete";
    
    private static final String COPY = "copy";
    
    /** used to indicate the modal is for export process */
    private static final String EXPORT_COURSE_DATA = "export";
    
    private boolean shouldPollForProgress = false;
    
    private ProgressIndicator progressBar;
    
    private String task = null;
    
    /** Adds a 1 second delay between the rpcs to check the progress of copy or delete. */
    private final int CHECK_PROGRESS_DELAY_MS = 1000;
    
    private AsyncCallback<CopyCourseResult> copyCallback;

    /**
     * Create a remote service proxy to talk to the server-side Greeting service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
    /**
     * Initializes the progress dialog
     * 
     * @param task The type of file progress to display.
     */
    private FileOperationProgressModal(String task) {
        
        initWidget(uiBinder.createAndBindUi(this));        
        
        
        copyCallback = null;
        this.task = task;
         
        if(task.equals(DELETE)) {
            heading.setText("Deleting Files");
        }else if(task.equals(EXPORT_COURSE_DATA)){
            heading.setText("Export files");
        } else {
            heading.setText("Copying Files");
        }
        
        progressBar = new ProgressIndicator(0, "Initiating request...");
    }
    
    /**
     * Gets a progress dialog to retrieve progress for delete operations 
     * 
     * @return a progress dialog to retrieve progress for delete operations 
     */
    public static FileOperationProgressModal getDeleteProgressModal() {
        return new FileOperationProgressModal(DELETE);
    }
    
    /**
     * Gets a progress dialog to retrieve progress for copy operations 
     * 
     * @return a progress dialog to retrieve progress for copy operations 
     */
    public static FileOperationProgressModal getCopyProgressModal() {
        return new FileOperationProgressModal(COPY);
    }

    /**
     * Gets a progress dialog to retrieve process for export operations
     * @return a progress dialog to retrieve progress for export operations
     */
    public static FileOperationProgressModal getCourseDataExportProgressModal(){
        return new FileOperationProgressModal(EXPORT_COURSE_DATA);
    }
    
    /**
     * Updates the dialog's progress bar. The dialog will close if the progress
     * bar reaches 100%.
     * 
     * @param progressResult A progress indicator retrieved from the server.
     */
    private void updateProgress(ProgressIndicator progressResult) {

        progressBar = progressResult;
        logger.info("Updating progress indicator with "+progressResult);
        progress.updateProgress(progressBar);
        
        if (progressBar.getPercentComplete() >= 100) {
            stopPollForProgress(false);            
        }
    }
    
    /**
     * Displays the dialog and begins polling the server for file operation progress.
     */
    public void startPollForProgress() {
        shouldPollForProgress = true;
        maybePollForProgress();
        fileProgressDialog.show();
    }
    
    /**
     * Stops the dialog from requesting progress updates.
     * 
     * @param error Whether or not an error has occurred. If true,
     * the dialog will hide immediately. Otherwise, the progress bar
     * will increase to 100% and then close.
     */
    public void stopPollForProgress(boolean error) {
        stopPollForProgress(error, null);
    }
    
    /**
     * Stops the dialog from requesting progress updates.
     * 
     * @param error Whether or not an error has occurred. If true, the dialog will hide immediately. 
     * Otherwise, the progress bar will increase to 100% and then close.
     * @param onHideCmd The command to execute after the progress dialog hides. This should be used for
     * displaying subsequent modal dialogs after the progress modal hides to prevent double faded backgrounds. 
     */
    public void stopPollForProgress(boolean error, final ScheduledCommand onHideCmd) {
        
        shouldPollForProgress = false;
        Timer cmdTimer = null;
        
        if(onHideCmd != null) {
            cmdTimer = new Timer() {
            
                @Override
                public void run() {
                    onHideCmd.execute();
                    progress.clear();
                }
            };
        }
        
        if(error) {
            fileProgressDialog.hide();
            
            if(cmdTimer != null) {
            	
                cmdTimer.schedule(300);
                
                //remove the dialog from the DOM so it doesn't get stuck there
                fileProgressDialog.removeFromParent();
            }
            
        } else {
            progressBar.setPercentComplete(100);
            progress.updateProgress(progressBar);
            
            final Timer finalCmdTimer = cmdTimer;
            Timer timer = new Timer() {
                
                @Override
                public void run() {
                	
                    fileProgressDialog.hide();
                    
                    if(finalCmdTimer != null) {
                    	
                        finalCmdTimer.schedule(300);
                        
                        //remove the dialog from the DOM so it doesn't get stuck there
                        fileProgressDialog.removeFromParent();
                    }
                }
            };
            
            timer.schedule(1600);
        }
        
    }
    
    /**
     * Adding a delay before checking the progress.  This is to reduce spam against the server.
     */
    private void schedulePollForProgress() {
        Timer timer = new Timer() {

            @Override
            public void run() {
                maybePollForProgress();
            }
            
        };
        
        timer.schedule(CHECK_PROGRESS_DELAY_MS);
    }
    
    /**
     * If a file operation has been initiated, periodically polls for the task's progress. Otherwise, does nothing.
     */
    private void maybePollForProgress(){
        
        if(shouldPollForProgress){
            
            if(task != null && task.equals(DELETE)) {
            
                dashboardService.getDeleteProgress(UiManager.getInstance().getSessionId(), new AsyncCallback<ProgressResponse>(){
                    
                    @Override
                    public void onFailure(Throwable thrown) {
                        schedulePollForProgress();
                    }
    
                    @Override
                    public void onSuccess(ProgressResponse response) {
                        
                        if(response.isSuccess()){

                            updateProgress(response.getProgress());
                        }
        
                        schedulePollForProgress();            
                    }
                    
                });
            } else {
                
                // Need to make sure the copy callback has been set before proceeding.
                if (copyCallback == null) {
                    logger.severe("The copy callback handler is not set, please set the copy callback handler before calling maybePollForProgress()");
                    return;
                }
                
                dashboardService.getCopyProgress(UiManager.getInstance().getSessionId(), new AsyncCallback<LoadedProgressIndicator<CopyCourseResult>>(){
                    
                    @Override
                    public void onFailure(Throwable thrown) {
                        copyCallback.onFailure(thrown);
                    }
    
                    @Override
                    public void onSuccess(LoadedProgressIndicator<CopyCourseResult> response) {
                        
                        logger.info("getCopyProgress onSuccess() response=" + response);
                        if(response.getException() == null){
                            
                            updateProgress(response);
                            
                            if (response.getPayload() != null) {                                
                                
                            	CopyCourseResult result = response.getPayload();
                                                                
                                logger.info("getCopyProgress onSuccess() result=" + result);
                                copyCallback.onSuccess(result);
                            }
                            
                        } else {
                        	
                        	copyCallback.onFailure(
                        			new DetailedException(
                        					response.getException().getReason(), 
                        					response.getException().getDetails(), 
                        					null
                        			)
                        	);
                        }
        
                       
                        //schedule another poll for progress 1 second from now
						schedulePollForProgress(); 
                    }
                    
                });
            }
        }
    }

    /**
     * Set the callback handler for copy operations.
     * 
     * @param callback The callback that is used during the copy operation.
     */
    public void setCopyCallbackHandler(AsyncCallback<CopyCourseResult> callback) {
        this.copyCallback = callback;
        
    }
    
}
