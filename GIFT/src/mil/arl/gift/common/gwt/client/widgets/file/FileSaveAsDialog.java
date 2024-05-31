/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import java.util.List;

import org.gwtbootstrap3.client.ui.Button;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSaveAsView.FileSaveAsCallback;
import mil.arl.gift.common.gwt.client.widgets.file.FileSaveAsView.Mode;
import mil.arl.gift.common.gwt.client.widgets.file.FileSaveAsView.ModeChangedCallback;
import mil.arl.gift.common.io.FileTreeModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A dialog box that gives the client the option to save a file to the server. This class implements {@literal HasValue<String>}, allowing other 
 * classes to handle when the client hits the 'Select' button and retrieve the server-side path to the file selected.
 * 
 * @author nroberts
 */
public class FileSaveAsDialog extends ModalDialogBox implements HasValue<String>{

	private static FileSaveAsDialogUiBinder uiBinder = GWT
			.create(FileSaveAsDialogUiBinder.class);

	interface FileSaveAsDialogUiBinder extends
			UiBinder<Widget, FileSaveAsDialog> {
	}
	
	@UiField(provided=true)
	protected FileSaveAsWidget fileSelection;
	
	@UiField
	protected FlowPanel buttonPanel;
	
	@UiField
	protected Button okButton;
	
	@UiField
	protected Button cancelButton;
	
	@UiField
	protected HTML introMessage;
	
	private String selectedFileName = null;
	
	private boolean shouldCloseOnConfirm = true;
	
	private boolean disallowConfirm = false;
	
	private boolean disallowCancel = false;
	
	private DisplaysMessage messageDisplay = null;
	
	private CancelCallback cancelCallback = null;
	
	/**
	 * Creates a file save-as dialog that only allows users to save files to a root directory on the server. 
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
	public FileSaveAsDialog(CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay){
		
		fileSelection = new FileSaveAsWidget(rootGetter, messageDisplay);
		
		this.messageDisplay = messageDisplay;
		
		setWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Creates a file save-as dialog that allows users to save files to a root directory on the server and create folders below that directory
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param rootGetter an object that can get the root directory
	 * @param folderCreator an object that can create a folder
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
	public FileSaveAsDialog(CanGetRootDirectory rootGetter, CanCreateFolder folderCreator, DisplaysMessage messageDisplay){
		
		fileSelection = new FileSaveAsWidget(rootGetter, folderCreator, messageDisplay);
		
		this.messageDisplay = messageDisplay;
		
		setWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Initializes event handlers and other components for this widget
	 */
	private void init(){
		
		setText("Save File As");
		setGlassEnabled(true);
		setFooterWidget(buttonPanel);	
				
		fileSelection.setFileTableSize("675px", "220px");
		
		//enable Select button when a server file is specified
		fileSelection.getFileNameDataDisplay().getSelectionModel().addSelectionChangeHandler(new Handler() {
			
			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				if(fileSelection.getFileNameDataDisplay().getSelectionModel() instanceof SingleSelectionModel){
					
					@SuppressWarnings("unchecked")
					SingleSelectionModel<FileTreeModel> selectionModel = 
							(SingleSelectionModel<FileTreeModel>) fileSelection.getFileNameDataDisplay().getSelectionModel();
					
					boolean fileNameIsFolder = selectionModel.getSelectedObject().isDirectory() 
							&& fileSelection.getFileNameInput().getValue() != null
							&& fileSelection.getFileNameInput().getValue().equals(selectionModel.getSelectedObject().getFileOrDirectoryName());
					
					if(!fileNameIsFolder || fileSelection.getFoldersSelectable()){
						
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
				
				if(mode.equals(Mode.MY_WORKSPACE)){
					checkFilenameBeforeSelect();
				}
			}
		});
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    
			    if(!disallowCancel) {
			        
    				hide();
    				
    				if(cancelCallback != null){
    					cancelCallback.onCancel();
    				}
			    }
			}
		});
		
		okButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(!disallowConfirm){
				
					fileSelection.submitFileChoice(new FileSaveAsCallback() {
						
						@Override
						public void onSuccess(String filename) {
							
							if(shouldCloseOnConfirm){
								hide();
								
							} else {
								okButton.setEnabled(false);
								disallowConfirm = true;
							}
							
							setValue(filename, true);
						}
						
						@Override
						public void onFailure(String reason) {
							
							if(messageDisplay != null){
								messageDisplay.showErrorMessage("Unable to save", reason, new ModalDialogCallback() {
                                    
                                    @Override
                                    public void onClose() {
                                        
                                        // set focus to the new course name text field.  Must be after the dialog is shown.
                                        Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                                            @Override
                                            public void execute() {
                                                getFileSelector().getFileNameFocusInput().setFocus(true);
                                            }
                                        });                                        
                                    }
                                });
								

							}
							
							disallowConfirm = false;
							
							checkFilenameBeforeSelect();
						}
					});
				}
			}
		});
		
		//listen for enter and escape keyboard events on this dialog (even if the text field for
		//file name is not in focus) in order to programmatically 'click' the ok or cancel buttons respectfully
		this.addDomHandler(new KeyDownHandler() {

            @Override
            public void onKeyDown(KeyDownEvent e) {

                if(e.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            okButton.click();
                        }
                    });
                }else if(e.getNativeKeyCode() == KeyCodes.KEY_ESCAPE){
                    
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        
                        @Override
                        public void execute() {
                            cancelButton.click();
                        }
                    });
                }
            }
            
        },  KeyDownEvent.getType());

	}
	
	@Override
	public void show(){
		super.show();
		
		disallowConfirm = false;
		
		disallowCancel = false;
		
		selectedFileName = null;
		
		fileSelection.reset();
		
		checkFilenameBeforeSelect();
	}
	
	private void checkFilenameBeforeSelect(){
		
		if(!disallowConfirm){
			
			okButton.setEnabled(true);
			
		} else{
		    
			okButton.setEnabled(false);
		}
		
		if(!disallowCancel) {
		    
		    cancelButton.setEnabled(true);
		    
		} else {
		    
		    cancelButton.setEnabled(false);
		}
	}
	
	public FileSaveAsWidget getFileSelector(){
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
		
		selectedFileName = value;
		
		ValueChangeEvent.fire(this, value);
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
	 * Sets the file extensions to allow
	 * 
	 * @param extensions the file extensions to allow
	 */
	public void setAllowedFileExtensions (String[] extensions){
		fileSelection.setAllowedFileExtensions(extensions);
	}
	
	/**
	 * Clears the file name and refreshes the file list
	 */
	public void clearFileName(){
		fileSelection.clearFileName();
	}
	
	/**
	 * Sets whether or not the user can navigate into subfolders.
	 * 
	 * @param value True if the user can navigate into subfolders, false
	 * otherwise.
	 */
	public void setAllowNavigationToSubfolders(boolean value) {
		fileSelection.setAllowNavigationToSubfolders(value);
	}
	
	public boolean isFileOrFolderInCurrentDirectory(String fileOrFolderName) {
		return fileSelection.isFileOrFolderInCurrentDirectory(fileOrFolderName);
	}
	
    /**
     * Finds the child model (file or folder) that has the given name.
     * 
     * @param fileOrFolderName The name of the file or folder to retrieve.
     * @return The file tree model if found, otherwise returns null.
     */
	 public FileTreeModel getFileModelInCurrentDirectory(String fileOrFolderName) {
	     return fileSelection.getFileModelInCurrentDirectory(fileOrFolderName);
	 }
	 
	/**
	 * Sets the HTML to use for the optional introduction message. If not set, no introduction message will appear.
	 * 
	 * @param html the HTML to use
	 */
	public void setIntroMessageHTML(String html){
		introMessage.setHTML(html);
	}
	
	/**
	 * Sets the text on the "File Name:" label
	 * 
	 * @param text the new text to use
	 */
	public void setFileNameLabelText(String text){
		fileSelection.setFileNameLabelText(text);
	}
	
	/**
	 * Sets the text to use for the confirmation button
	 * 
	 * @param text the text to use
	 */
	public void setConfirmButtonText(String text){
		okButton.setText(text);
	}
	
	/**
	 * Sets whether or not this dialog should close upon hitting the confirmation button.
	 * 
	 * Note: Disabling close on confirm will require this dialog to be closed programmatically by its caller. If the confirm button is 
	 * clicked while close on confirm is disabled, then the confirm button will disable itself until this widget is hidden and shown again, 
	 * to prevent users from accidentally hitting the button repeatedly.
	 * 
	 * @param shouldClose whether or not this dialog should close
	 */
	public void setCloseOnConfirm(boolean shouldClose){
		shouldCloseOnConfirm = shouldClose;
	}
	
	/**
	 * Sets whether or not the list of files and its navigation bar should be visible
	 * 
	 * @param visible whether or not the list of files and its navigation bar should be visible
	 */
	public void setFileListVisible(boolean visible){
		fileSelection.setFileListVisible(visible);
	}
	
	/**
	 * Re-enables the confirm button if it was disabled
	 */
	public void reallowConfirm(){
		
		disallowConfirm = false;
		
		checkFilenameBeforeSelect();
	}
	
	/**
	 * Prevents the cancel button from being clicked
	 */
	public void preventCancel() {
	    
	    disallowCancel = true;
	    
	    cancelButton.setEnabled(false);
	}
	
	/**
	 * Re-enables the cancel button if it was disabled
	 */
	public void reallowCancel() {
	    
	    disallowCancel = false;
        
        cancelButton.setEnabled(true);
	}
	
	/**
	 * Sets a callback to be executed whenever the cancel button is clicked
	 * 
	 * @param callback the callback to execute
	 */
	public void setCancelCallback(CancelCallback callback){
		this.cancelCallback = callback;
	}
	
	
	/**
     * Sets the starting directory for the dialog.  This directory path must be a subfolder of the
     * {@link FileSaveAsPresenter#startDirectoryModel startDirectoryModel} of the widget.  
     * 
     * @param path - The starting directory path (relative to the root).  This should not be null.
     */
	public void setStartingDirectory(String path) {
	    fileSelection.setStartingDirectory(path);
	    
	}
	
	/**
	 * gets the text entered into the text box 
	 * 
	 * @return text in text box
	 */
	public HasValue<String> getFileNameTextBox(){
		return fileSelection.getFileNameInput();
	}
}
