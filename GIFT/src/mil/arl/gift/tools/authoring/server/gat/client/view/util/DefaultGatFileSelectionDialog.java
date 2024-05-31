/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.rpc.AsyncCallback;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.CanGetRootDirectory;
import mil.arl.gift.common.gwt.client.widgets.file.CanHandleUploadedFile;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileCallback;
import mil.arl.gift.common.gwt.client.widgets.file.CopyFileRequest;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionDialog;
import mil.arl.gift.common.gwt.client.widgets.file.GetRootDirectoryCallback;
import mil.arl.gift.common.gwt.client.widgets.file.HandleUploadedFileCallback;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.DefaultMessageDisplay;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.ErrorDetailsDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelDialog;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;
import mil.arl.gift.tools.authoring.server.gat.shared.SharedGatSystemProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFiles;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.CopyWorkspaceFilesResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchRootDirectoryModelResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.MoveDomainFileToWorkspaceLocation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.MoveDomainFileToWorkspaceLocationResult;

/**
 * An extension of {@link FileSelectionDialog} that sends all uploaded files to the workspace folder and gets its list of files in the 
 * MyWorkspace tab from the workspace folder as well.
 * 
 * @author nroberts
 */
public class DefaultGatFileSelectionDialog extends FileSelectionDialog{

    /** The logger. */
    private static Logger logger = Logger.getLogger(DefaultGatFileSelectionDialog.class.getName());
    
    /**guidance displayed at the top of the file selection dialog for DKF file selection */
    public static final String CHOOSE_DKF_FILE = "<html><font size=\"3\"><b>Select the GIFT domain assessment (dkf.xml) that references the AutoTutor Conversation to use for this Survey course object.</b></font></html>";
    
    /**guidance displayed at the top of the file selection dialog for conversation tree file selection for a course object */
    public static final String CHOOSE_CONVERSATION_TREE_FILE_OBJECT = "<html><font size=\"3\"><b>Select the Conversation Tree (conversationTree.xml) to use for this course object.</b></font></html>";
    
    /**guidance displayed at the top of the file selection dialog for conversation tree file selection for metadata editor */
    public static final String CHOOSE_CONVERSATION_TREE_FILE_METADATA = "<html><font size=\"3\"><b>Select the Conversation Tree (conversationTree.xml) to use as this content.</b></font></html>";
    
    /**guidance displayed at the top of the file selection dialog for AutoTutor script file selection */
    public static final String CHOOSE_SKO_FILE = "<html><font size=\"3\"><b>Select the AutoTutor Conversation (sko.xml) to use for this course object.</b></font></html>";
    
    /** Workspace relative path of the course folder location */
    public static String courseFolderPath;
    
    /** Whether or not file selection is read-only*/
    private static boolean readOnly = false;
    
    /**
     * a flag used to indicate whether the file being selected should by copied to
     * the root of the course folder even if that file is a descendant of the course
     * folder already. Set to true if the file should NOT be copied if already a descendant
     * file of the course folder.
     */
    private boolean bypassCopyLogicForFilesInCurrentCourseFolder = true;
    
    /** Whether or not the next upload submission should overwrite any existing files that cause name conflicts */
    private boolean shouldUploadOverwrite = false;
    
	/**
	 * Creates a new file selection dialog.
	 * 
	 * @param rootGetter object that specifies the root directory of the file selection dialog.
	 */
    public DefaultGatFileSelectionDialog(CanGetRootDirectory rootGetter) {
		
		super(
				SharedGatSystemProperties.COURSE_RESOURCE_UPLOAD_URL, 	
				
				rootGetter, 
		
				DefaultMessageDisplay.includeAllMessages
		);
		
		setUploadHandler(new CanHandleUploadedFile() {
					
			@Override
			public void handleUploadedFile(String uploadFilePath,
			        final String fileName,
					final HandleUploadedFileCallback callback) {
				
				moveUploadedFile(uploadFilePath, callback, shouldUploadOverwrite);
			}

		});
		
		setCopyFileRequest(new CopyFileRequest() {

            @Override
            public void asyncCopy(FileTreeModel source,
                    final CopyFileCallback callback) {

            	String sourcePath = source.getRelativePathFromRoot(true);
            	String destPath = courseFolderPath;
            	
            	if(destPath.startsWith("/")) {
            		destPath = destPath.substring(1);
            	}
            	boolean isWithinCourseFolder = sourcePath.startsWith(destPath);
            	
            	logger.fine("Async copy bypassCopy = " + bypassCopyLogicForFilesInCurrentCourseFolder);
            	logger.fine("isWithinCourseFolder = " + isWithinCourseFolder);
            	if (bypassCopyLogicForFilesInCurrentCourseFolder && isWithinCourseFolder) {
            	    
            	    /*
                     * Don't copy if the file selected already exists under the current course folder. Instead, just return the selected file 
                     * relative to the course folder.
                     */
            	    String courseRelativePath = sourcePath.substring(destPath.length() + 1);
            	    FileTreeModel courseRelativeModel = new FileTreeModel(courseRelativePath);
            	    
            	    logger.info("Copy Requested, but file already exists within the directory.  Returning a course relative path to the file instead.  Course relative path is: " + courseRelativePath);
            	    callback.onSuccess(courseRelativeModel);
            	    return;
            	}
            	
            	copyFile(source, courseFolderPath, callback, false);  
            }
            
        });
    	
    }

	/**
	 * Creates a new file selection dialog.
	 */
	public DefaultGatFileSelectionDialog() {
		
		this(
	    		new CanGetRootDirectory() {

	    			@Override
	    			public void getRootDirectory(final GetRootDirectoryCallback callback) {

	    				AsyncCallback<FetchRootDirectoryModelResult> asyncCallback = new AsyncCallback<FetchRootDirectoryModelResult>(){

	    					@Override
	    					public void onFailure(Throwable thrown) {
	    						callback.onFailure(thrown);
	    					}

	    					@Override
	    					public void onSuccess(FetchRootDirectoryModelResult result) {

	   							if(result.isSuccess()){
	   								callback.onSuccess(result.getDomainDirectoryModel());

	   							} else {

	   								if(result.getErrorMsg() != null){
	   									callback.onFailure(result.getErrorMsg());

	   								} else {
	   									callback.onFailure("An error occurred while getting the root directory.");
	   								}
	   							}
	   						}

	   					};

	   					String userName = GatClientUtility.getUserName();

	   					FetchRootDirectoryModel action = new FetchRootDirectoryModel();
	   					action.setUserName(userName);		

	   					SharedResources.getInstance().getDispatchService().execute(action, asyncCallback);
	   				}
	    		}
	    );
	}
	
	/**
	 * Moves an file uploaded to the specified location to the appropriate course folder
	 * 
	 * @param uploadFilePath the location of the uploaded file
	 * @param callback the callback with which to handle failure and success conditions
	 * @param overwriteExisting whether or not to overwrite an existing file with the same name in the course folder
	 */
	private void moveUploadedFile(final String uploadFilePath, final HandleUploadedFileCallback callback, final boolean overwriteExisting) {
		
		AsyncCallback<MoveDomainFileToWorkspaceLocationResult> asyncCallback = 
				new AsyncCallback<MoveDomainFileToWorkspaceLocationResult>() {

			@Override
			public void onFailure(Throwable thrown) {
				BsLoadingDialogBox.remove();
				callback.onFailure("Failed to move uploaded file to destination.<br><br>Reason:<br>" + thrown.getMessage());
			}

			@Override
			public void onSuccess(
					MoveDomainFileToWorkspaceLocationResult result) {
				
				BsLoadingDialogBox.remove();
				
				if(result.isSuccess()){
					
					FileTreeModel movedLocationModel = result.getMovedLocationModel();
					callback.onSuccess(movedLocationModel);
					
				} else {
					
					if(result.getMoveFailures().isEmpty()){						
						ErrorDetailsDialog dialog = new ErrorDetailsDialog(
								result.getErrorMsg(), 
								result.getErrorDetails(), 
								result.getErrorStackTrace());
						dialog.setDialogTitle("Upload Failed");
						dialog.center();
						
						//Passing in an empty string here will skip the messageDisplay.showErrorMessage()
						//preventing 2 error dialogs from appearing
						callback.onFailure("");
						
					} else {
						
						final String targetPath = result.getMoveFailures().get(uploadFilePath);
						
						String uploadFileName = uploadFilePath.substring(uploadFilePath.lastIndexOf(Constants.FORWARD_SLASH) + 1, uploadFilePath.length());
						
						OkayCancelDialog.show(
                				"Overwrite File", 
                				"A file with the name of '" + uploadFileName + "' already exists at " + targetPath + ". <br/><br/>"
                						+ "Do you want to overwrite this file? " , 
                				"Yes, Overwrite File", 
                				new OkayCancelCallback() {
									
									@Override
									public void okay() {
																				
										shouldUploadOverwrite = true;
										
										//we need to resubmit the uploaded file again since it has likely already been deleted on the server
										getFileSelector().submitFileChoice(getSelectionCallback());				
									}
									
									@Override
									public void cancel() {
										
										//return the user to their state before hitting the confirm button
										reallowConfirm();
										
										BsLoadingDialogBox.remove();
									}
                				}
                		);
					}
				}
			}
		};
		
		String courseFolderName = getCourseFolderName();
		BsLoadingDialogBox.display("Moving File", "Moving the uploaded file to the \"" + courseFolderName + "\" course.");
		
		MoveDomainFileToWorkspaceLocation action = new MoveDomainFileToWorkspaceLocation(
				GatClientUtility.getUserName(), 
				uploadFilePath, 
				courseFolderPath,
				overwriteExisting
		);
		
		SharedResources.getInstance().getDispatchService().execute(action, asyncCallback);
	}

	/**
	 * Copies the selected file to the specified course folder path
	 * 
	 * @param source the file selected
	 * @param callback a callback used to handle failure and success conditions
	 * @param courseFolderPath the path to the course folder where the selected file should be copied
	 * @param overwriteExisting whether or not to overwrite an existing file with the same name
	 */
	private void copyFile(final FileTreeModel source, final String courseFolderPath, final CopyFileCallback callback, boolean overwriteExisting) {
		
		final String sourcePath = source.getRelativePathFromRoot(true);
		
		logger.fine("Received async request to copy file with filename: " + sourcePath + " to folder: " + courseFolderPath);
        AsyncCallback<CopyWorkspaceFilesResult> asyncCallback = new AsyncCallback<CopyWorkspaceFilesResult>(){

            @Override
            public void onFailure(Throwable t) {
                callback.onFailure(t.getMessage());               
            }

            @Override
            public void onSuccess(CopyWorkspaceFilesResult result) {
                
                logger.info("Copy file result: "+result);
               
                if (result.isSuccess()) {
                    
                    if (result.getCopiedFiles().isEmpty()) {
                        String errorMsg = "Failure in copying the file to: " + courseFolderPath + ". The server returned 0 results for the copy.";
                        callback.onFailure(errorMsg);
                        
                    } else {
                        logger.fine("CopyFileRequest succeeded with result list of: " + result.getCopiedFiles().size());

                        // We're only expecting a result size of 1 element here.
                        FileTreeModel copiedFile = result.getCopiedFiles().get(0);
                        copiedFile.detatchFromParent();
                        callback.onSuccess(copiedFile);
                    }
                    
                } else {
                	
                	if(result.getCopyFailures().isEmpty()){

                		ErrorDetailsDialog dialog = new ErrorDetailsDialog(
                				result.getErrorMsg(), 
								result.getErrorDetails(), 
								result.getErrorStackTrace());
						dialog.setDialogTitle("Upload Failed");
						dialog.center();

						//Passing in an empty string here will skip the messageDisplay.showErrorMessage()
						//preventing 2 error dialogs from appearing
	                    callback.onFailure("");
                    
                	} else {
                		
                		final String targetPath = result.getCopyFailures().get(sourcePath);                		
                        
                        if(logger.isLoggable(Level.FINE)){
                            logger.fine("Trying to execute a file copy but found an existing file with the same name at " + targetPath + ".");
                        }
                		
                		OkayCancelDialog.show(
                				"Overwrite File", 
                				"A file with the name of '" + source.getFileOrDirectoryName() + "' already exists at " + targetPath + ". <br/><br/>"
                						+ "Do you want to overwrite this file? " , 
                				"Yes, Overwrite File", 
                				new OkayCancelCallback() {
									
									@Override
									public void okay() {
										copyFile(source, courseFolderPath, callback, true);
									}
									
									@Override
									public void cancel() {
										
										//return the user to their state before hitting the confirm button
										reallowConfirm();
										
										BsLoadingDialogBox.remove();
									}
                				}
                		);
                	}
                }
                
            }
        };

        HashMap<String, String> sourcePathsToTargetPaths = new HashMap<String, String>();

        logger.fine("Requesting copy from source: " + sourcePath + " to: " + courseFolderPath);
        sourcePathsToTargetPaths.put(sourcePath, courseFolderPath);
        
        // Setup the server request.
        CopyWorkspaceFiles action = new CopyWorkspaceFiles();
        action.setUsername(GatClientUtility.getUserName());                
        action.setSourcePathsToTargetPaths(sourcePathsToTargetPaths);
        action.setOverwriteExisting(overwriteExisting);

        // Send the request to the server.
        SharedResources.getInstance().getDispatchService().execute(action, asyncCallback);
	}

	/**
	 * Sets whether all file selection dialogs in the GAT should be in Read-Only mode or not
	 * 
	 * @param readOnly whether all file selection dialogs in the GAT should be in Read-Only mode or not
	 */
	public static void setReadOnly(boolean readOnly){
		
		DefaultGatFileSelectionDialog.readOnly = readOnly;
	}
	
	@Override
	public void show(){
		
		if(!readOnly){		
			super.show();
		
		} else {
			WarningDialog.error("Read only", "File selection is disabled in Read-Only mode.");
		}
		
		shouldUploadOverwrite = false;
	}

	/**
	 * Set the flag used to indicate whether the file being selected should by copied to
	 * the root of the course folder even if that file is a descendant of the course
	 * folder already.
	 * 
	 * @param bypassCopyLogicForFilesInCurrentCourseFolder set to true if the file should NOT
	 * be copied if already a descendant file of the course folder.
	 */
	public void setBypassCopyLogicForFilesInCurrentCourseFolder(
			boolean bypassCopyLogicForFilesInCurrentCourseFolder) {
		this.bypassCopyLogicForFilesInCurrentCourseFolder = bypassCopyLogicForFilesInCurrentCourseFolder;
	}
	
	/**
	 * Return the course folder name for the gat file selection dialog. 
	 * 
	 * @return String - The name of the course folder.  
	 */
	public static String getCourseFolderName() {
	    String courseFolderName = GatClientUtility.getCourseFolderName(courseFolderPath);
	    
	    return courseFolderName;
	}

    /**
     * Return the workspace relative path of the course folder location. (e.g.
     * Public/courseFolder, userFolder/courseFolder)
     * 
     * @return the workspace relative path of the course folder location.
     */
    public static String getCourseFolderPath() {
        return courseFolderPath;
    }

	/**
	 * Sets additional information to be displayed if the user selects the wrong file type.
	 * 
	 * @param additionalFileExtInfo The message to be displayed if the user selects the wrong file type.
	 */
	public void setAdditionalFileExtensionInfo(String additionalFileExtInfo) {
		if(fileSelection != null) {
			fileSelection.setFileExtensionInfo(additionalFileExtInfo);
		}
	}
}
