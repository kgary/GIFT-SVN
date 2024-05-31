/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.DetailsDialogBox;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.tools.dashboard.client.DashboardService;
import mil.arl.gift.tools.dashboard.client.DashboardServiceAsync;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.shared.rpcs.ExportResponse;

/**
 * Bootstrap widget used to display error dialogs with detailed information to the user.  These are
 * a simple modal class that can be dismissed by the user.  An optional callback
 * can be passed in to be used to respond to the dialog confirmation.  
 * The modal can be an informational type of modal versus
 * an error type of modal.
 * 
 * @author nroberts
 */
public class ErrorDetailsDialogWidget extends DetailsDialogBox {
	
	/**
     * Create a remote service proxy to talk to the server-side dashboard service.
     */
    private final DashboardServiceAsync dashboardService = GWT
            .create(DashboardService.class);
    
	/**
	 * Creates an error dialog box with expandable details and a stack trace.
	 */
	public ErrorDetailsDialogWidget() {
		super();
	}
	
	/**
	 * Creates UI components<br/>
	 * Note: this is for a single error and will clear out any errors that were set by the constructors
	 * or other calls to this method.
	 * 
	 * @param title the title of the dialog.  Can't be null or empty.
	 * @param reason The user-friendly message about the error. Can't be null or empty.
	 * @param errorDetails contains the details and optional stack trace.
	 */
	public void setData(String title, final String reason, final ErrorDetails errorDetails) {
		
	    if(title == null || title.isEmpty()){
	        throw new IllegalArgumentException("The title can't be null or empty.");
	    }else if(reason == null || reason.isEmpty()){
	        throw new IllegalArgumentException("The reason can't be null or empty.");
	    }
        
		this.reason = reason;
		
        if(errorDetails != null){
            this.courseErrorDetails.clear();
            this.courseErrorDetails.add(errorDetails);
        }
		
		setDialogTitle(title);		
		setWidget(createErrorContent(reason, errorDetails));
	}
	
	@Override
	public void startDownload() {

		dashboardService.exportErrorFile(UiManager.getInstance().getUserName(), reason, courseErrorDetails, getDate(), getCourseName(), new AsyncCallback<ExportResponse>(){

			@Override
			public void onFailure(Throwable thrown) {
				showFailureDialog(thrown.toString());
			}

			@Override
			public void onSuccess(ExportResponse response) {
				if(response.isSuccess() && response.getExportResult() != null){

					final DownloadableFileRef result = response.getExportResult();										
					showSuccessDialog(result.getDownloadUrl(), new CloseHandler<PopupPanel>() {

						@Override
						public void onClose(CloseEvent<PopupPanel> event) {

							dashboardService.deleteExportFile(result, new AsyncCallback<RpcResponse>(){

								@Override
								public void onFailure(Throwable e) {

									/* 
									 * Do Nothing. File deletion is handled silently, therefore errors 
									 * should be handled on the server.
									 */														
								}

								@Override
								public void onSuccess(RpcResponse response) {

									//Do Nothing. File deletion is handled silently, so nothing should happen here.
								}
							});
						}
					});

				} else {
					showFailureDialog(response.getErrorMessage());
				}
			}

		});
			
	}
	
}
