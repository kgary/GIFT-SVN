/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;

/**
 * An alternative version of {@link FileOperationProgressModal} that uses {@link LoadedProgressIndicator LoadedProgressIndicators} to pass
 * the results of operations back asynchronously. This avoids triggering timeouts due to long-running server calls.
 * <br/><br/>
 * Note that the type declared for a particular instance of this class should match the payload type used by the operation that instance
 * will retrieve progress for, otherwise an error will be thrown. Operations that have payload types will reference that type in 
 * their progress mappings in {@link mil.arl.gift.tools.authoring.server.gat.server.FileOperationsManager FileOperationsManager}. If an
 * operation does not use payloads in its progress mappings, then {@link FileOperationProgressModal} should be used instead of this class.
 * 
 * @author nroberts
 */
public class LoadedFileOperationProgressModal<T> extends ModalDialogBox {
	
	/** The type of progress this instance handles */
	private ProgressType progressType = ProgressType.DELETE;
	
	/** A progress indicator that will track the progress of the operation and hold the result once it is complete */
	private LoadedProgressIndicator<T> progressBar;
	
	/** A progress bar used to show the current progress of the main task */
	private ProgressBarListEntry progress = new ProgressBarListEntry();
	
	/** A progress bar used to show the progress of the current subtask */
	private ProgressBarListEntry subtaskProgress = new ProgressBarListEntry();
	
	/** The callback to complete when the asynchronous operation finishes */
	private AsyncCallback<LoadedProgressIndicator<T>> onCompleteCallback;
	
	/**
	 * Initializes the progress dialog
	 * 
	 * @param progressType The type of file progress to display.
	 */
	public LoadedFileOperationProgressModal(ProgressType progressType) {
		
		super();		
		
		this.progressType = progressType;
		
		switch(progressType) {		
		case DELETE: 
			setText("Deleting Files");
			break;
			
		case SLIDE_SHOW:
			setText("Creating Slide Show");
			break;
			
		case UNZIP:
			setText("Unzipping Archive");
			break;
		}
		
		setGlassEnabled(true);
		setCloseable(false);
		
		init();
	}
	
	/** 
	 * Initializes the dialog's UI components.
	 */
	private void init() {
		
		progressBar = new LoadedProgressIndicator<T>();
		progressBar.setPercentComplete(0);
		progressBar.setTaskDescription("Initiating request...");
		
		FlowPanel wrapper = new FlowPanel();
		Button closeButton = new Button("Perform in Background");
		
		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				LoadedFileOperationProgressModal.this.hide();
			}
			
		});
		
		progress.updateProgress(progressBar);
		
		wrapper.add(progress);
		wrapper.setWidth("400px");
		wrapper.getElement().getStyle().setProperty("wordBreak", "break-all");
		
		if(progressType == ProgressType.SLIDE_SHOW) {
			subtaskProgress.setVisible(false);
			progressBar.updateSubtaskDescription("Overall Progress");
			subtaskProgress.updateProgress(progressBar.getSubtaskProcessIndicator());
			wrapper.add(subtaskProgress);
		}
		
		if(progressType == ProgressType.UNZIP) {
			subtaskProgress.setVisible(false);
			progressBar.updateSubtaskDescription("Overall Progress");
			subtaskProgress.updateProgress(progressBar.getSubtaskProcessIndicator());
			wrapper.add(subtaskProgress);
		}
		
		setWidget(wrapper);
		setFooterWidget(closeButton);
	}

	/**
	 * Updates the dialog's progress bar. The dialog will close if the progress
	 * bar reaches 100%.
	 * 
	 * @param progressResult A progress indicator retrieved from the server.
	 */
	private void updateProgress(LoadedProgressIndicator<T> progressResult) {

		progressBar = progressResult;
		progress.updateProgress(progressBar);
		if(progressResult.getSubtaskProcessIndicator() != null) {
			subtaskProgress.updateProgress(progressResult.getSubtaskProcessIndicator());
			if(!subtaskProgress.isVisible()) {
				subtaskProgress.setVisible(true);
			}
		}
		
		if (progressBar.getPercentComplete() >= 100) {
			stopPollForProgress(false);
		}
	}
	
	/**
	 * Displays the dialog and begins polling the server for file operation progress asynchronously.
	 * 
	 * @param onCompleteCallback a callback to be invoked when the operation completes or ends in failure
	 */
	public void startPollForProgress(AsyncCallback<LoadedProgressIndicator<T>> onCompleteCallback) {
		
		this.onCompleteCallback = onCompleteCallback;
		
		// Reset the panel
		progressBar = new LoadedProgressIndicator<T>();
		progressBar.setPercentComplete(0);
		progressBar.setTaskDescription("Initiating request...");
		progress.updateProgress(progressBar);
		
		if(progressType == ProgressType.SLIDE_SHOW) {
			subtaskProgress.setVisible(false);
			progressBar.updateSubtaskDescription("Overall Progress");
			subtaskProgress.updateProgress(progressBar.getSubtaskProcessIndicator());
		}
		
		if(progressType == ProgressType.UNZIP) {
			subtaskProgress.setVisible(false);
			progressBar.updateSubtaskDescription("Overall Progress");
			subtaskProgress.updateProgress(progressBar.getSubtaskProcessIndicator());
		}
		
		pollForProgress();
		center();
	}
	
	/**
	 * Stops the dialog from requesting progress updates.
	 * 
	 * @param error Whether or not an error has occurred. If true,
	 * the dialog will hide immediately. Otherwise, the progress bar
	 * will increase to 100% and then close.
	 */
	public void stopPollForProgress(boolean error) {
		
		if(error) {
			this.hide();
			
		} else {
			progressBar.setPercentComplete(100);
			progress.updateProgress(progressBar);
			
			Timer timer = new Timer() {
				
				@Override
				public void run() {
					LoadedFileOperationProgressModal.this.hide();
				}
			};
			
			timer.schedule(1600);
		}
		
	}
	
	/**
     * Periodically polls for the task's progress and invokes any assigned callbacks once the operation finishes.
     */
    private void pollForProgress(){
			
		GetProgress action = new GetProgress(progressType);
		action.setUserName(GatClientUtility.getUserName());
		
		SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GetProgressResult>(){

			@Override
			public void onFailure(Throwable thrown) {
				
				if(onCompleteCallback != null){
					onCompleteCallback.onFailure(thrown);
				}
				
				stopPollForProgress(true);
			}

			@Override
			public void onSuccess(GetProgressResult response) {
				
				if(response != null && response.getProgress() != null){
					
					if(response.getProgress() instanceof LoadedProgressIndicator<?>){
						
						try{
						
							@SuppressWarnings("unchecked")
							LoadedProgressIndicator<T> progress = (LoadedProgressIndicator<T>) response.getProgress();
							
							if(progress.isComplete() || progress.getException() != null){
								
								if(onCompleteCallback != null){
									
									//the operation has completed successfully
									onCompleteCallback.onSuccess(progress);
								}
								
								stopPollForProgress(false);
								
							} else if(progress.getException() != null){
								
								if(onCompleteCallback != null){
									
									//the operation has failed and the caught exception should be passed to the callback
									onCompleteCallback.onSuccess(progress);
								}
								
								stopPollForProgress(true);
								
							} else {
								
								updateProgress(progress);
								
								//schedule another poll for progress 1 second from now
								Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
									
									@Override
									public boolean execute() {
										
										pollForProgress();
										
										return false;
									}
									
								}, 1000);
							}
							
						} catch(ClassCastException e){
							
							if(onCompleteCallback != null){
									
								onCompleteCallback.onFailure(
										new DetailedException(
												"Failed to get progress for this operation because an invalid data type was "
												+ "returned from the server.", 
												"The returned progress information was of the type '" + 
												response.getProgress().getClass().toString() + "' which is not the expected type "
												+ "for the " + progressType.toString() + " operation.", 
												e
										)
								);
							}
							
							stopPollForProgress(true);
						}
					}
					
				} else {
					
					if(onCompleteCallback != null){
						
						onCompleteCallback.onFailure(
								new DetailedException(
										"Failed to get progress for this operation because no progress information was found on the server.", 
										"The returned progress information was empty, indicating that the operation is either already "
										+ "completed or was never started.", 
										null
								)
						);
					}
					
					stopPollForProgress(true);
				}
			}
			
		});
	}
	
}
