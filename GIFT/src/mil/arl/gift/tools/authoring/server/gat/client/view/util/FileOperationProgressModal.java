/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ProgressBarListEntry;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgressResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.GetProgress.ProgressType;

/**
 * An extension of ModalDialogBox that displays progress for 
 * a file operation such as delete, copy, or move.
 * 
 * @author bzahid
 */
public class FileOperationProgressModal extends ModalDialogBox {
	
	private boolean shouldPollForProgress = false;
	
	private ProgressType progressType = ProgressType.DELETE;
	
	private ProgressIndicator progressBar;
	
	ProgressBarListEntry progress = new ProgressBarListEntry();
	
	ProgressBarListEntry subtaskProgress = new ProgressBarListEntry();
	
	/**
	 * Initializes the progress dialog
	 * 
	 * @param progressType The type of file progress to display.
	 */
	public FileOperationProgressModal(ProgressType progressType) {
		
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
		
		progressBar = new ProgressIndicator(0, "Initiating request...");
		
		FlowPanel wrapper = new FlowPanel();
		Button closeButton = new Button("Perform in Background");
		
		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				FileOperationProgressModal.this.hide();
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
	private void updateProgress(ProgressIndicator progressResult) {

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
	 * Displays the dialog and begins polling the server for file operation progress.
	 */
	public void startPollForProgress() {
		
		// Reset the panel
		progressBar = new ProgressIndicator(0, "Initiating request...");
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
		
		shouldPollForProgress = true;
		maybePollForProgress();
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
		
		shouldPollForProgress = false;
		
		if(error) {
			this.hide();
			
		} else {
			progressBar.setPercentComplete(100);
			progress.updateProgress(progressBar);
			
			Timer timer = new Timer() {
				
				@Override
				public void run() {
					FileOperationProgressModal.this.hide();
				}
			};
			
			timer.schedule(1600);
		}
		
	}
	
	/**
     * If a file operation has been initiated, periodically polls for the task's progress. Otherwise, does nothing.
     */
    private void maybePollForProgress(){
		
		if(shouldPollForProgress){
			
			GetProgress action = new GetProgress(progressType);
			action.setUserName(GatClientUtility.getUserName());
			
			SharedResources.getInstance().getDispatchService().execute(action, new AsyncCallback<GetProgressResult>(){

				@Override
				public void onFailure(Throwable thrown) {
					maybePollForProgress();
				}

				@Override
				public void onSuccess(GetProgressResult response) {
					
					if(response.isSuccess()){
						
						updateProgress(response.getProgress());
					}
	
					maybePollForProgress();			
				}
				
			});
		}
	}
	
}
