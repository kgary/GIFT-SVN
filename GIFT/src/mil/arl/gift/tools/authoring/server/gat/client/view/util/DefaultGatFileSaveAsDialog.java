/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import mil.arl.gift.common.gwt.client.widgets.file.CanCreateFolder;
import mil.arl.gift.common.gwt.client.widgets.file.CanGetRootDirectory;
import mil.arl.gift.common.gwt.client.widgets.file.CreateFolderCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSaveAsDialog;
import mil.arl.gift.common.gwt.client.widgets.file.GetRootDirectoryCallback;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CreateWorkspaceFolder;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModelResult;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An extension of {@link FileSaveAsDialog} that  gets its list of files in the MyWorkspaces tab from the workspace folder.
 * 
 * @author nroberts
 */
public class DefaultGatFileSaveAsDialog extends FileSaveAsDialog{

	/**
	 * Creates a new file selection dialog.
	 * 
	 * @param useUserFolder whether or not to start the file saving dialog in the current user's folder. If set to true, users will not be able 
	 * to navigate outside their own folders when saving files.
	 */
	public DefaultGatFileSaveAsDialog(final boolean useUserFolder) {
		
		super(			
				new CanGetRootDirectory() {
			
					@Override
					public void getRootDirectory(final GetRootDirectoryCallback callback) {
						
						final String userName = GatClientUtility.getUserName();
						
						AsyncCallback<FetchRootDirectoryModelResult> asyncCallback = new AsyncCallback<FetchRootDirectoryModelResult>(){

							@Override
							public void onFailure(Throwable thrown) {
								callback.onFailure(thrown);
							}

							@Override
							public void onSuccess(FetchRootDirectoryModelResult result) {
								if(result.isSuccess()) {
									FileTreeModel root = result.getDomainDirectoryModel();
									if(useUserFolder) {
										FileTreeModel userDirectory = root.getChildByName(userName);
										callback.onSuccess(root, userDirectory);
									} else {
										callback.onSuccess(root);
									}
								} else {
									
									if(result.getErrorMsg() != null){
										callback.onFailure(result.getErrorMsg());
										
									} else {
										callback.onFailure("An error occurred while getting the root directory.");
									}
								}
							}
							
						};											
						
						FetchRootDirectoryModel action = new FetchRootDirectoryModel();
						action.setUserName(userName);		
						
						SharedResources.getInstance().getDispatchService().execute(action, asyncCallback);
					}
				}, 
				 
				new CanCreateFolder() {
					
					@Override
					public void createFolder(String parentRelativePath, String folderName,
							final CreateFolderCallback callback) {
						
						final String userName = GatClientUtility.getUserName();
						
						AsyncCallback<GatServiceResult> asyncCallback = new AsyncCallback<GatServiceResult>() {
							
							@Override
							public void onSuccess(GatServiceResult result) {
								
								if(result.isSuccess()){
									callback.onFolderCreated();
									
								} else {
									callback.onFailure(result.getErrorMsg());
								}
							}
							
							@Override
							public void onFailure(Throwable thrown) {
								callback.onFailure(thrown);
							}
						};
						
						CreateWorkspaceFolder action = new CreateWorkspaceFolder(userName, parentRelativePath, folderName, false);
						
						SharedResources.getInstance().getDispatchService().execute(action, asyncCallback);
					}					
				},
		
				DefaultMessageDisplay.includeAllMessages
		);
	}
}
