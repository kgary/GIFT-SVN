/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.file;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.FileSelectionCallback;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.dashboard.client.UiManager;
import mil.arl.gift.tools.dashboard.client.bootstrap.file.FileSelectionView.Mode;
import mil.arl.gift.tools.dashboard.client.bootstrap.file.FileSelectionView.ModeChangedCallback;

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
public class FileSelectionModal extends Composite implements HasValue<String>{

	private static FileSelectionDialogUiBinder uiBinder = GWT
			.create(FileSelectionDialogUiBinder.class);

	interface FileSelectionDialogUiBinder extends
			UiBinder<Widget, FileSelectionModal> {
	}
	
	@UiField
	protected Modal fileSelectionDialog;
	
	@UiField
	protected Modal fileUploadingDialog;
	
	@UiField(provided=true)
	protected FileSelectionWidget fileSelection;
	
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
	
	/**
	 * Creates a modal dialog used to upload files from a client machine to the server. The servlet URL passed into this constructor will be 
	 * used to contact the servlet that should handle the upload. This URL passed in can either be the full URL of the servlet or the URL 
	 * of the servlet relative to tits host (i.e. 'http://localhost:8080/servletName' or 'just servletName', assuming http://localhost:8080 is 
	 * the host of the servlet named 'servletName).
	 * 
	 * @param uploadServletUrl the URL of the servlet that should handle the upload.
	 */
	public FileSelectionModal(String uploadServletUrl){
		
		fileSelection = new FileSelectionWidget(uploadServletUrl);
		
		initWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
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
					public void onFailure(String reason) {
						
						okButton.setEnabled(true);
						
						loadingIcon.stopLoading();
						fileUploadingDialog.hide();
						
						if(fileSelection.isUploadingFile()){
							String message = "The file upload failed due to a server error.";
							
							if(reason.contains("HTTP ERROR 500")) {
								message += " This may have happened because the file was too large. You can try selecting "
										+ "a smaller file to upload.";
							}
							
							UiManager.getInstance().displayDetailedErrorDialog("File Upload Error", message, reason, null, null);
							
						} else {
							UiManager.getInstance().displayErrorDialog("File Selection Error", reason, null);
						}
					}

                    @Override
                    public void onServerFileSelected(String filename) {
                        //nothing to do
                    }

                    @Override
                    public void onClientFileUploaded(String filename, String servletPath) {

                        okButton.setEnabled(true);
                        loadingIcon.stopLoading();
                        fileUploadingDialog.hide();
                        
                        setValue(filename, true);
                        
//                      if(fileSelection.isUploadingFile()){
//                          UiManager.getInstance().displayInfoDialog("File Upload Success", ""
//                                  + "<span style='font-weight: bold;'>"
//                                  +       filename
//                                  + "</span>"
//                                  + " has been successfully uploaded"
//                                  + "."
//                          );
//                      }
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
	 * Sets an instructional message to show above the file selector. This message is optional and will not be shown unless set.
	 * 
	 * @param optionalInstructions an instructional message
	 */
	public void setOptionalInstructionsText(String optionalInstructions){
		instructionsLabel.setText(optionalInstructions);
	}
}
