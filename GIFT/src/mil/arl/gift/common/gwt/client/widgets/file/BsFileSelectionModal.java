/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.FileSelectionCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.Mode;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.ModeChangedCallback;
import mil.arl.gift.common.io.FileTreeModel;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A modal that gives the client the option to select a file from either their own machine or the server. This class implements 
 * HasValue<String>, allowing other classes to handle when the client hits the 'Select' button and retrieve the file name they have 
 * selected.
 * 
 * @author nroberts
 */
public class BsFileSelectionModal extends Composite implements HasValue<String>{

	private static BsFileSelectionModalUiBinder uiBinder = GWT
			.create(BsFileSelectionModalUiBinder.class);

	interface BsFileSelectionModalUiBinder extends
			UiBinder<Widget, BsFileSelectionModal> {
	}
	
	@UiField
	protected Modal fileSelectionDialog;
	
	@UiField(provided=true)
	protected BsFileSelectionWidget fileSelection;
	
	@UiField
	protected Modal fileUploadingDialog;
	
	@UiField
	protected Button okButton;
	
	@UiField
	protected Button cancelButton;
	
	@UiField
	protected Label instructionsLabel;
	
	@UiField
	protected Label uploadingLabel;
	
	@UiField
	protected BsLoadingIcon loadingIcon;
	
	private String selectedFileName = null;
	
	private DisplaysMessage messageDisplay = null;
	
	private CanHandleUploadedFile uploadHandler = null;
	
	/**
	 * Creates a file selection dialog that only allows users to upload files. 
	 * 
	 * Uploaded files will be sent to the servlet at the given URL to be handled on the server. Errors and other information will be reported to 
	 * the given display object.
	 * 
	 * @param uploadServletUrl the URL of the servlet to send the uploaded file to.
	 * @param messageDisplay an object that can display messages to the user
	 */
	public BsFileSelectionModal(String uploadServletUrl, DisplaysMessage messageDisplay){
		
		fileSelection = new BsFileSelectionWidget(uploadServletUrl);
		
		this.messageDisplay = messageDisplay;
		
		initWidget(uiBinder.createAndBindUi(this));
		
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
	public BsFileSelectionModal(String uploadServletUrl, CanHandleUploadedFile uploadHandler, DisplaysMessage messageDisplay){
		
		fileSelection = new BsFileSelectionWidget(uploadServletUrl);
		
		this.messageDisplay = messageDisplay;
		
		this.uploadHandler = uploadHandler;
		
		initWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Initializes event handlers and other components for this widget
	 */
	private void init(){
		
		fileSelection.setFileTableSize("100%", "220px");
		
		//enable Select button when a file is selected for upload
		fileSelection.getFileUpload().addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {				
				checkFilenameBeforeUpload();
			}
		});
		
		//enable Select button when a server file (not a directory is selected)
		fileSelection.getFileNameDataDisplay().getSelectionModel().addSelectionChangeHandler(new Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if(fileSelection.getFileNameDataDisplay().getSelectionModel() instanceof SingleSelectionModel){
					
					@SuppressWarnings("unchecked")
					SingleSelectionModel<FileTreeModel> selectionModel = 
							(SingleSelectionModel<FileTreeModel>) fileSelection.getFileNameDataDisplay().getSelectionModel();
					
					if(selectionModel.getSelectedObject() != null 
							&& !selectionModel.getSelectedObject().isDirectory()){
						
						checkFilenameBeforeSelect();
						
					} else {
						okButton.setEnabled(false);
					}
				}			
			}
		});
		
		//enable Select button when a server file name is specified
		fileSelection.getFileNameInput().addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {				
				checkFilenameBeforeSelect();
			}
		});
		
		fileSelection.setModeChangedCallback(new ModeChangedCallback() {
			
			@Override
			public void onModeChanged(Mode mode) {
				
				if(mode.equals(Mode.UPLOAD)){
					checkFilenameBeforeUpload();
				
				} else if(mode.equals(Mode.MY_WORKSPACE)){
					checkFilenameBeforeSelect();
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
				
				okButton.setEnabled(false);
				
				fileUploadingDialog.show();
				loadingIcon.startLoading();
				
				String filename = fileSelection.getFileUpload().getFilename();
				filename = filename.substring(filename.lastIndexOf("\\") + 1);
				
				uploadingLabel.setText("Uploading \"" + filename + "\" please wait...");
				
				fileSelection.submitFileChoice(new FileSelectionCallback() {
					
					@Override
					public void onServerFileSelected(String filename) {
						okButton.setEnabled(true);
						loadingIcon.stopLoading();
						fileUploadingDialog.hide();
						hide();
					
						setValue(filename, true);
					}
					
					@Override
					public void onFailure(String reason) {
						
						if(!reason.isEmpty() && messageDisplay != null){
							String message = "The file upload failed due to a server error.";
							
							if(reason.contains("HTTP ERROR 500")) {
								message += " This may have happened because the file was too large. You can try selecting "
										+ "a smaller file to upload.";
							}
							
							messageDisplay.showDetailedErrorMessage(message, reason, null, null);
						}
						
						reallowConfirm();
						
						loadingIcon.stopLoading();
						fileUploadingDialog.hide();
					}

					@Override
					public void onClientFileUploaded(final String filepath,
							String servletPath) {
						
						
						if(uploadHandler != null && servletPath != null){
							
							String uploadFilePath = servletPath.replace("\\", "/") + "/" + filepath;	
							final String filename = (filepath.contains("/") ? filepath.substring(filepath.lastIndexOf("/") + 1) : filepath);
							
							uploadHandler.handleUploadedFile(uploadFilePath, filename, new HandleUploadedFileCallback() {
								
								@Override
								public void onSuccess(FileTreeModel file) {
									
									loadingIcon.stopLoading();
									fileUploadingDialog.hide();
									
									if(messageDisplay != null){															
										messageDisplay.showInfoMessage("Upload successful", 
										        "'" + filename + "' has been successfully uploaded.", null);						
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
									
									loadingIcon.stopLoading();
									fileUploadingDialog.hide();
								}
								
								@Override
								public void onFailure(String reason) {
									if(!reason.isEmpty() && messageDisplay != null){										
										
										messageDisplay.showErrorMessage("Failed to upload", 
										        "An error occurred while uploading '" + filename + "'. " + reason, null);					
									}
									
									reallowConfirm();
									
									loadingIcon.stopLoading();
									fileUploadingDialog.hide();
								}
							});
							
						} else {							
							setValue(filepath, true);
							
							loadingIcon.stopLoading();
							fileUploadingDialog.hide();
							hide();
						}
					}
				});
			}
		});
	}
	
	public void show(){
		
		loadingIcon.stopLoading();
		
		selectedFileName = null;
		
		fileSelection.reset();
		checkFilenameBeforeUpload();
		checkFilenameBeforeSelect();
		
		fileSelectionDialog.show();
	}
	
	public void hide(){
		fileSelectionDialog.hide();
	}
	
	private void checkFilenameBeforeUpload(){
		
		if(fileSelection.getFileUpload().getFilename() != null
				&& !fileSelection.getFileUpload().getFilename().isEmpty()){
			
			okButton.setEnabled(true);
			
		} else{
			okButton.setEnabled(false);
		}
	}
	
	private void checkFilenameBeforeSelect(){
		
		if(fileSelection.getFileNameInput().getValue() != null
				&& !fileSelection.getFileNameInput().getValue().isEmpty()){
			
			okButton.setEnabled(true);
			
		} else{
			okButton.setEnabled(false);
		}
	}
	public BsFileSelectionWidget getFileSelector(){
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
	 * Sets an instructional message to show above the file selector. This message is optional and will not be shown unless set.
	 * 
	 * @param optionalInstructions an instructional message
	 */
	public void setOptionalInstructionsText(String optionalInstructions){
		instructionsLabel.setText(optionalInstructions);
	}
	
	/**
	 * Re-enables the confirm button if it was disabled
	 */
	public void reallowConfirm(){
		
		if(fileSelection.isUploadingFile()){
			checkFilenameBeforeUpload();
		
		} else {
			checkFilenameBeforeSelect();
		}
	}
}
