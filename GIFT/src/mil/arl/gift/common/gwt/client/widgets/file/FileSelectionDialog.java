/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.FileSelectionCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.Mode;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.ModeChangedCallback;
import mil.arl.gift.common.io.FileTreeModel;

/**
 * A dialog box that gives the client the option to select a file from either their own machine or the server. This class implements 
 * {@literal HasValue<String>}, allowing other classes to handle when the client hits the 'Select' button and retrieve the server-side path to 
 * the file selected.
 * 
 * @author nroberts
 */
public class FileSelectionDialog extends DialogBox implements HasValue<String>{

	private static FileSelectionDialogUiBinder uiBinder = GWT
			.create(FileSelectionDialogUiBinder.class);

	interface FileSelectionDialogUiBinder extends
			UiBinder<Widget, FileSelectionDialog> {
	}
	
	@UiField(provided=true)
	protected FileSelectionWidget fileSelection;
	
	@UiField
	protected Button okButton;
	
	@UiField
	protected Button cancelButton;
	
	private String selectedFileName = null;
	
	/** how to display messages to the user, can be null */
	private DisplaysMessage messageDisplay = null;
	
	private CanHandleUploadedFile uploadHandler = null;
	
	private boolean showEnabled = true;
	
	private String disabledShowReason = null;
	
	private boolean disallowConfirm = false;
	
	@UiField
	protected HTML introMessage;
	
	/** A callback used to handle when the user uploads a file from their computer or chooses an existing file from the workspace */
	private FileSelectionCallback selectionCallback = new FileSelectionCallback() {
		
		@Override
		public void onServerFileSelected(String filename) {
			BsLoadingDialogBox.remove();
			setValue(filename, true);
			hide();
		}
		
		@Override
		public void onFailure(String reason) {
			BsLoadingDialogBox.remove();
			
			if(!reason.isEmpty() && messageDisplay != null){
				String message = "The file upload failed due to a server error.";
				
				if(reason.contains("HTTP ERROR 500")) {
					message += " This may have happened because the file was too large. You can try selecting "
							+ "a smaller file to upload.";
				}
				
				messageDisplay.showDetailedErrorMessage(message, reason, null, null);
			}
			
			reallowConfirm();
		}

		@Override
		public void onClientFileUploaded(final String filepath,
				String servletPath) {
			
			BsLoadingDialogBox.remove();
			
			if(uploadHandler != null && servletPath != null){
				
				String uploadFilePath = servletPath.replace("\\", "/") + "/" + filepath;	
				final String filename = (filepath.contains("/") ? filepath.substring(filepath.lastIndexOf("/") + 1) : filepath);
				
				uploadHandler.handleUploadedFile(uploadFilePath, filename, new HandleUploadedFileCallback() {
					
					@Override
					public void onSuccess(FileTreeModel file) {
						
						if(messageDisplay != null){															
							messageDisplay.showInfoMessage("Upload successful", "'" + filename + "' has been successfully uploaded.", null);						
						}
						
						setValue(file.getRelativePathFromRoot(true), true);
						hide();
					}
					
					@Override
					public void onFailure(Throwable thrown) {
						if(messageDisplay != null){										
							messageDisplay.showErrorMessage("Failed to upload", 
							        "An error occurred while uploading '" + filename + "'. " + thrown.toString(), null);						
						}
						
						reallowConfirm();
					}
					
					@Override
					public void onFailure(String reason) {
						if(!reason.isEmpty() && messageDisplay != null){	
							
							messageDisplay.showErrorMessage("Failed to upload", 
							        "An error occurred while uploading '" + filename + "'. " + reason, null);						
						}
						
						reallowConfirm();
					}
				});
				
			} else {
				
				setValue(filepath, true);
				hide();
			}
		}
	};
	
	/**
	 * Creates a file selection dialog that only allows users to upload files. 
	 * 
	 * Uploaded files will be sent to the servlet at the given URL to be handled on the server. Errors and other information will be reported to 
	 * the given display object.
	 * 
	 * @param uploadServletUrl the URL of the servlet to send the uploaded file to.
	 * @param messageDisplay an object that can display messages to the user
	 */
	public FileSelectionDialog(String uploadServletUrl, DisplaysMessage messageDisplay){
		
		fileSelection = new FileSelectionWidget(uploadServletUrl);
		
		setMessageDisplay(messageDisplay);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Creates a file selection dialog that only allows users to upload files. 
	 * 
	 * Uploaded files will be sent to the servlet at the given URL to be handled on the server. Errors and other information will be reported to 
	 * the given display object.
	 * 
	 * @param uploadServletUrl the URL of the servlet to send the uploaded file to.
	 * @param uploadHandler an object that can handle uploaded files
	 * @param messageDisplay an object that can display messages to the user
	 */
	public FileSelectionDialog(String uploadServletUrl, CanHandleUploadedFile uploadHandler, DisplaysMessage messageDisplay){
		
		fileSelection = new FileSelectionWidget(uploadServletUrl);
		
		setMessageDisplay(messageDisplay);
		
		this.uploadHandler = uploadHandler;
		
		setWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Creates a file selection dialog that only allows users to select files from a root directory on the server. 
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
	public FileSelectionDialog(CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay){
		
		fileSelection = new FileSelectionWidget(rootGetter, messageDisplay);
		
		setMessageDisplay(messageDisplay);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Creates a file selection dialog that allows users to upload files or select files from a root directory on the server.
	 * 
	 * Uploaded files will be sent to the servlet at the given URL to be handled on the server. The root directory will be retrieved 
	 * using the instance of {@link CanGetRootDirectory} provided.Errors and other information will be reported to 
	 * the given display object.
	 * 
	 * @param uploadServletUrl the URL of the servlet to send the uploaded file to.
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay an object that can display messages to the user
	 */
	public FileSelectionDialog(String uploadServletUrl, CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay){
		
		fileSelection = new FileSelectionWidget(uploadServletUrl, rootGetter, messageDisplay);
		
		setMessageDisplay(messageDisplay);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Creates a file selection dialog that allows users to upload files or select files from a root directory on the server.
	 * 
	 * Uploaded files will be sent to the servlet at the given URL to be handled on the server. The root directory will be retrieved 
	 * using the instance of {@link CanGetRootDirectory} provided.Errors and other information will be reported to 
	 * the given display object.
	 * 
	 * @param uploadServletUrl the URL of the servlet to send the uploaded file to.
	 * @param uploadHandler an object that can handle uploaded files
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay an object that can display messages to the user.  can be null.
	 */
	public FileSelectionDialog(String uploadServletUrl, CanHandleUploadedFile uploadHandler, CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay){
		
		fileSelection = new FileSelectionWidget(uploadServletUrl, rootGetter, messageDisplay);
		
		this.uploadHandler = uploadHandler;
		
		setMessageDisplay(messageDisplay);
		
		setWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * How to display messages related to this dialog to the user.
	 * 
	 * @param messageDisplay an object that can display messages to the user.  can be null.
	 */
	public void setMessageDisplay(DisplaysMessage messageDisplay){
	    this.messageDisplay = messageDisplay;
	}
	
	/**
	 * Initializes event handlers and other components for this widget
	 */
	private void init(){
		
		setText("Select a File");
		setGlassEnabled(true);
		
		fileSelection.setFileTableSize("675px", "220px");
		
		//enable Select button when a file is selected for upload
		fileSelection.getFileUpload().addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {				
				checkFilenameBeforeUpload();
			}
		});
		
		//enable Select button when a server file
		fileSelection.getFileNameDataDisplay().getSelectionModel().addSelectionChangeHandler(new Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if(fileSelection.getFileNameDataDisplay().getSelectionModel() instanceof SingleSelectionModel){
					
					@SuppressWarnings("unchecked")
					SingleSelectionModel<FileTreeModel> selectionModel = 
							(SingleSelectionModel<FileTreeModel>) fileSelection.getFileNameDataDisplay().getSelectionModel();
					
					if(selectionModel.getSelectedObject() != null 
							&& (!selectionModel.getSelectedObject().isDirectory() || fileSelection.getFoldersSelectable())){
						
						okButton.setEnabled(true);
					}
				}			
			}
		});
		
		fileSelection.getFileNameKeyUpInput().addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				
				boolean enterPressed = event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER;
				
				if(enterPressed){
					
					fileSelection.getFileNameFocusInput().setFocus(false);
					
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							okButton.click();
						}
					});
				}
			}
		});
		
		fileSelection.setModeChangedCallback(new ModeChangedCallback() {
			
			@Override
			public void onModeChanged(Mode mode) {
				
				if(mode.equals(Mode.UPLOAD)){
					checkFilenameBeforeUpload();
				
				} else if(mode.equals(Mode.MY_WORKSPACE)){
					okButton.setEnabled(true);
				}
			}
		});
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(!disallowConfirm){
					
					if(fileSelection.getSelectedFile() == null && !fileSelection.getFileNameInputValue().isEmpty()) {
						// if there is no file model selected, check the textbox for a filename to select
						fileSelection.setSelectedFile(fileSelection.getFileNameInputValue());
					}
					
					if(fileSelection.getSelectedFile() != null && fileSelection.getSelectedFile().isDirectory() && !fileSelection.getFoldersSelectable()) {
						// if the selected file is a directory, open it
						fileSelection.openSelectedFolder();
						
					} else {
						// select the file
						
						if(fileSelection.isUploadingFile()){
							
							String filename = fileSelection.getFileUpload().getFilename();
							filename = filename.substring(filename.lastIndexOf("\\") + 1);
							
							BsLoadingDialogBox.display("Uploading File", "Uploading '" + filename + "'. Please wait...");
							
						} else {
                            if (fileSelection.getSelectedFile() == null) {
                                if (messageDisplay != null) {
                                    messageDisplay.showWarningMessage("No File Selected", "Please select a file.",
                                            null);
                                }
                                return;
                            }

							BsLoadingDialogBox.display("Checking File", "Verifying file selection. Please wait...");
						}
						
						okButton.setEnabled(false);
						disallowConfirm = true;
					
						fileSelection.submitFileChoice(selectionCallback);
					}
				}
			}
		});
	}
	
	@Override
	public void show(){
		
		if(showEnabled){
			
			super.show();
			
			selectedFileName = null;
			
			fileSelection.reset();

			reallowConfirm();
			
		} else {
			
			if(messageDisplay != null){
				messageDisplay.showErrorMessage("Selection disabled", 
				        "File selection has been disabled. " + disabledShowReason != null ? disabledShowReason : "", null);
			}
		}
	}
	
	private void checkFilenameBeforeUpload(){
		
		if(fileSelection.getFileUpload().getFilename() != null
				&& !fileSelection.getFileUpload().getFilename().isEmpty()
				&& !disallowConfirm){
			
			okButton.setEnabled(true);
			
		} else{
			okButton.setEnabled(false);
		}
	}
	
	/**
	 * Sets the CopyFileRequest object for the dialog which will determine
	 * how the dialog will perform the copy of the selected item from the my
	 * workspaces tab.
	 * 
	 * @param copyFileReq - The CopyFileRequest object containing the logic to perform the copy.
	 */
	public void setCopyFileRequest(CopyFileRequest copyFileReq) {
	    fileSelection.setCopyFileRequest(copyFileReq);
	}
	
	/**
	 * Sets the CopyFileRequest object for the dialog which will determine
	 * how the dialog will perform the copy of the selected item from the my
	 * workspaces tab.
	 * 
	 * @param copyFileReq - The CopyFileRequest object containing the logic to perform the copy.
	 */
	public CopyFileRequest getCopyFileRequest() {
	    return fileSelection.getCopyFileRequest();
	}
	
	public FileSelectionWidget getFileSelector(){
		return fileSelection;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {
		return selectedFileName;
	}

	@Override
	public void setValue(String value) {
		setValue(value, false);
	}

	@Override
	public void setValue(String value, boolean fireEvents) {
		
		String oldValue = selectedFileName;
		
		selectedFileName = value;
		
		if(fireEvents){
			 ValueChangeEvent.fireIfNotEqual(this, oldValue, value);
		}
	}
	
	/**
	 * Filters out files from the MyWorkspaces tab that match any of the given relative file paths. Any files with the given paths 
	 * will be hidden from the user.
	 * 
	 * @param relativePathsOfFiles the file extensions to filter out
	 */
	public void filterOutFiles(List<String> relativePathsOfFiles){
		
		fileSelection.filterOutFiles(relativePathsOfFiles);
	}
	
	/**
	 * Sets whether or not only folders should be shown in the MyWorkspaces tab
	 * 
	 * @param showFoldersOnly whether or not only folders should be shown in the MyWorkspaces tab
	 */
	public void setShowFoldersOnly(boolean showFoldersOnly){
		
		fileSelection.setShowFoldersOnly(showFoldersOnly);
	}
	
	/**
	 * Filters out the given file extensions from the MyWorkspaces tab. Any files with the given extensions will be hidden from the user.
	 * 
	 * @param extensions the file extensions to filter out
	 */
	public void filterOutExtensions(List<String> extensions){
		
		fileSelection.filterOutExtensions(extensions);
	}
	
	/**
	 * Sets whether or not folders should be selectable in the MyWorkspaces tab
	 * 
	 * @param foldersSelectable whether or not folders should be selectable in the MyWorkspaces tab
	 */
	public void setFoldersSelectable(boolean foldersSelectable){
		
		fileSelection.setFoldersSelectable(foldersSelectable);
	}
	
	/**
	 * Specifies an object that will be used to handle uploaded files
	 * 
	 * @param uploadHandler an object to handle uploaded files with
	 */
	public void setUploadHandler(CanHandleUploadedFile uploadHandler){
		this.uploadHandler = uploadHandler;
	}
	
	/**
	 * Gets the object used to handle uploaded files
	 * 
	 * @return the object used to handle uploaded files
	 */
	public CanHandleUploadedFile getUploadHandler(){
		return this.uploadHandler;
	}
	
	/**
	 * Sets the file extensions to allow
	 * 
	 * @param extensions the file extensions to allow
	 */
	public void setAllowedFileExtensions (String[] extensions){
		fileSelection.setAllowedFileExtensions(extensions);
	}
	
	/**
     * Enables or disables uploading by adding or removing the "Upload" tab if this instance is capable of uploading.
     * 
	 * @param enabled whether or not uploading should be enabled
	 * @param reason an optional message shown if uploading is disabled and no tabs are left in the dialog
     */
	public void setUploadEnabledIfPossible(boolean enabled, String reason) {
		
		if(!fileSelection.setUploadEnabledIfPossible(enabled)){
			
			showEnabled = false;
			
			//need to show a message to the user if no tabs are available in the dialog explaining why
			disabledShowReason = reason;
		
		} else {
			showEnabled = true;
			disabledShowReason = null;
		}
	}
	
	/**
	 * Re-enables the confirm button if it was disabled
	 */
	public void reallowConfirm(){
		
		disallowConfirm = false;
		
		if(fileSelection.isUploadingFile()){
			checkFilenameBeforeUpload();
		
		} else {
			okButton.setEnabled(true);
		}
	}
	
	/**
	 * Gets the callback used to handle file selections
	 * 
	 * @return the callback used to handle file selections
	 */
	protected FileSelectionCallback getSelectionCallback(){
		return selectionCallback;
	}
	
	/**
	 * Sets the HTML to use for the optional introduction message. If not set, no introduction message will appear.
	 * 
	 * @param html the HTML to use
	 */
	public void setIntroMessageHTML(String html){
		introMessage.setHTML(html);
	}
}
