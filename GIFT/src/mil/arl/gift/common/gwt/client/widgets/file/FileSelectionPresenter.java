/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.FileSelectionCallback;
import mil.arl.gift.common.io.FileTreeModel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

/**
 * A helper class for FileSelectionWidget that handles the bulk of the behind-the-scenes logic in the MyWorkspaces tab.
 * 
 * @author nroberts
 */
public class FileSelectionPresenter{
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(FileSelectionPresenter.class.getName());
    
    /** The view. */
    private FileSelectionView view;
    
	/** The file name data provider. */
	private ListDataProvider<FileTreeModel> fileNameDataProvider = new ListDataProvider<FileTreeModel>(new ArrayList<FileTreeModel>());
	
	/** The file name selection model. */
	private SingleSelectionModel<FileTreeModel> fileNameSelectionModel = new SingleSelectionModel<FileTreeModel>();
	
	/** The root directory model. */
	private FileTreeModel rootDirectoryModel = null;
	
	/** The starting directory model. */
	private FileTreeModel startDirectoryModel = null;
	
	/** The current directory model. */
	private FileTreeModel currentDirectoryModel = null;
	
	/** The file extensions to include. */
	private String[] fileExtensionsToInclude;
	
	private String selectedWorkspaceFile = null;
	
	private List<String> filteredFiles = new ArrayList<String>();
	
	private List<String> filteredExtensions = new ArrayList<String>();
	
	private boolean showFoldersOnly = false;
	
	private boolean foldersSelectable = false;
	
	private CanGetRootDirectory rootGetter = null;
	
	private DisplaysMessage messageDisplay = null;
	
	/** If the mode is set to MY_WORKSPACE this object allows the user to set whether the selected item
	 *  from the panel should be copied to a specified destination.  The default behavior is to not copy
	 *  and just return the selection.  If this is null, the default behavior is used.
	 */
	private CopyFileRequest copyFileRequest = null;
	
	/**
	 * Creates a new file selection presenter modifying the specified file selection view. The file selection presenter will invoke the logic 
	 * needed to get the MyWorkpaces tab's root directory from the server.
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param view the view that this presenter should modify
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
    public FileSelectionPresenter(FileSelectionView view, CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay) {
        
    	this.view = view; 	
    	this.rootGetter = rootGetter;
    	this.messageDisplay = messageDisplay;
    	
    	init();
    }    
    
    /**
     * Initializes all handlers attached to the view.
     */
    private void init() {
    	
    	view.getFileNameInput().addValueChangeHandler(new ValueChangeHandler<String>(){

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(event.getValue() != null){
					
					for(FileTreeModel fileModel : fileNameDataProvider.getList()){
						
						if(fileModel.getFileOrDirectoryName() != null 
								&& fileModel.getFileOrDirectoryName().equals(
										event.getValue())){
							
							if(fileNameSelectionModel.getSelectedObject() == null
									|| !fileNameSelectionModel.getSelectedObject().equals(fileModel)){
								
								fileNameSelectionModel.setSelected(fileModel, true);
								break;
							
							} 
						}
						
						fileNameSelectionModel.setSelected(fileModel, false);
					}
				}
			}
			
		});
    	
    	fileNameDataProvider.addDataDisplay(view.getFileNameDataDisplay());
		
		view.getFileNameDataDisplay().setSelectionModel(fileNameSelectionModel);
		
		fileNameSelectionModel.addSelectionChangeHandler(new Handler(){

			@Override
			public void onSelectionChange(SelectionChangeEvent event) {
				
				if(fileNameSelectionModel.getSelectedObject() != null){ 
					
					FileTreeModel selectedFileModel = fileNameSelectionModel.getSelectedObject();
					
					if(!selectedFileModel.isDirectory() || foldersSelectable){
						
						if (selectedFileModel.getFileOrDirectoryName() != null){	
							
							view.getFileNameInput().setValue(selectedFileModel.getFileOrDirectoryName());				
						}
					}
				}
			}
			
		});		
		
		//double click directories to open them
		view.addCellPreviewHandler(new CellPreviewEvent.Handler<FileTreeModel>(){
			
			long lastClick=-1000;

			@Override
			public void onCellPreview(CellPreviewEvent<FileTreeModel> event) {
				
				long clickedAt = System.currentTimeMillis();

                if(event.getNativeEvent().getType().contains("click")){
                	
                    if(clickedAt - lastClick < 300) { // double click on 2 clicks detected within 300 ms
                    	openSelectedFolder();                    	                    	
                    }
                    lastClick = System.currentTimeMillis();
                }
			}
			
		});
		
		view.setUpArrowCommand(new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				
				if(currentDirectoryModel != null && currentDirectoryModel.getParentTreeModel() != null){
					
					refreshCurrentFileList(currentDirectoryModel.getParentTreeModel());
					
					view.resetScroll();
					
					currentDirectoryModel = currentDirectoryModel.getParentTreeModel(); 

					if(currentDirectoryModel.equals(rootDirectoryModel) || currentDirectoryModel.equals(startDirectoryModel)){
						view.getUpArrow().setEnabled(false);
					}
					
					view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
							? currentDirectoryModel.getFileOrDirectoryName()
							: null
					);
				}
			}
		});
    }
	
    /**
     * Alters the appearance of the cursor to indicate waiting state.
     * 
     * @param waiting whether or not we are waiting
     */
	private void showWaiting(boolean waiting){
		
		if(waiting) {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "wait");
			view.setWorkspaceLoadingIconVisible(true);
			
		} else {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "default");
			view.setWorkspaceLoadingIconVisible(false);
		}
	}
	
	/**
	 * Refreshes all the content in the view with the latest file information from the server
	 */
	public void refresh(){
		
		if(rootGetter != null){
		
			GetRootDirectoryCallback callback = new GetRootDirectoryCallback() {
				
				@Override
				public void onSuccess(FileTreeModel rootFile) {
					
					showWaiting(false);
					
					populateFileHierarchy(rootFile, null);
				}
				
				@Override
				public void onSuccess(FileTreeModel rootFile,
						FileTreeModel startFile) {
					
					showWaiting(false);
					
					populateFileHierarchy(rootFile, startFile);
				}
				
				@Override
				public void onFailure(Throwable thrown) {
					showWaiting(false);
				}
				
				@Override
				public void onFailure(String reason) {
					showWaiting(false);
				}
			};
			
			showWaiting(true);
			
			rootGetter.getRootDirectory(callback);
		}
	}
	
	/**
	 * Sets the file extensions allowed for selection.
	 * 
	 * @param extensions the file extensions
	 */
	public void setAllowedFileExtensions(String[] extensions){
		fileExtensionsToInclude = extensions;		
	}
	
	/**
	 * Gets the file extensions allowed for selection
	 * 
	 * @return the file extensions users can select from
	 */
	public String[] getAllowedFileExtensions(){
		return fileExtensionsToInclude;
	}
	
	/**
	 * Gets the file extensions that are now allowed for the dialog.  Note this
	 * doesn't mean that the user cannot selecte them.  For the upload mode, the user
	 * can still pick the file (since it's a windows file selection dialog), but the file will be verified
	 * to see if it is not allowed.  For the workspace selection, the files are filtered by these extensions and
	 * are not shown to the user.
	 * 
	 * @return the list of file extensions that are not allowed.
	 */
	public List<String> getFilteredFileExtensions() {
	    return filteredExtensions;
	}
	
	/**
	 * Submits the user's file choice and invokes the given callback once their choice has been processed. In Upload mode, this method will 
	 * call a file servlet in order to upload the file.
	 * 
	 * @param callback The file selection callback.
	 * @param mode specifies whether the file choice mode is UPLOAD or MY_WORKSPACE
	 */
	public void submitFileChoice(FileSelectionCallback callback, FileSelectionView.Mode mode){
		
		if(mode.equals(FileSelectionView.Mode.UPLOAD)){
		
			//file upload needs to be handled in the view since we need to use the form element to submit the file to the server
			view.uploadChosenFile(callback);
			
		
		}else if(mode.equals(FileSelectionView.Mode.MY_WORKSPACE) && (copyFileRequest == null)) {
		    // The default behavior is to return the selection from the my workspace panel.
		    if(fileNameSelectionModel.getSelectedObject() != null){
	            
                StringBuilder sb = new StringBuilder();
                
                FileTreeModel currentFileModel = fileNameSelectionModel.getSelectedObject();
                
                sb.append(currentFileModel.getFileOrDirectoryName());
                
                while(currentFileModel.getParentTreeModel() != null 
                        && !currentFileModel.getParentTreeModel().equals(rootDirectoryModel)){
                    
                    currentFileModel = currentFileModel.getParentTreeModel();
                    
                    sb.insert(0, currentFileModel.getFileOrDirectoryName() + "/");
                }
                
                selectedWorkspaceFile = sb.toString();
                
                if(callback != null){
                    callback.onServerFileSelected(selectedWorkspaceFile);
                }
                
            } else {
                
                String failureReason;
                
                if(view.getFileNameInput().getValue() != null){
                    failureReason = "No file was found matching the file name '" 
                    		+ currentDirectoryModel.getFileOrDirectoryName() 
                    		+ "/" + view.getFileNameInput().getValue() +"'.";
                
                } else {
                    failureReason = "Please enter a file name or select a file.";
                }
                
                if(callback != null){
                    callback.onFailure(failureReason);
                }
            }
        }else if(mode.equals(FileSelectionView.Mode.MY_WORKSPACE) && (copyFileRequest != null)) {

            // This mode allows the file selection from the my workspace panel to be copied to a 
            // specified destination.
			if(fileNameSelectionModel.getSelectedObject() != null){

				FileTreeModel selectedFileModel = fileNameSelectionModel.getSelectedObject();
				
				selectedWorkspaceFile = selectedFileModel.getRelativePathFromRoot(true);

				logger.fine("Selected Workspace file: " + selectedWorkspaceFile);

				// Send the request to copy the selected item.
				view.copyChosenFile(selectedFileModel, callback);
				
			} else {
				
				String failureReason;
				
				if(view.getFileNameInput().getValue() != null){
					failureReason = "No file was found matching the file name '" 
							+ currentDirectoryModel.getFileOrDirectoryName() 
							+ "/" + view.getFileNameInput().getValue() +"'.";
				
				} else {
					failureReason = "Please enter a file name or select a file.";
				}
				
				if(callback != null){
					callback.onFailure(failureReason);
				}
			}
		} 
	}
	
	/**
	 * Filters out files from the MyWorkspaces tab that match any of the given relative file paths. Any files with the given paths 
	 * will be hidden from the user.
	 * 
	 * @param relativePathsOfFiles the file extensions to filter out
	 */
	public void filterOutFiles(List<String> relativePathsOfFiles){
		
		this.filteredFiles.clear();
		this.filteredFiles.addAll(relativePathsOfFiles);
		
		if(currentDirectoryModel != null){
			refreshCurrentFileList(currentDirectoryModel);
		}
	}
	
	/**
	 * Filters out the given file extensions from the MyWorkspaces tab. Any files with the given extensions will be hidden from the user.
	 * 
	 * @param extensions the file extensions to filter out
	 */
	public void filterOutExtensions(List<String> extensions){
		
		this.filteredExtensions.clear();
		this.filteredExtensions.addAll(extensions);
		
		if(currentDirectoryModel != null){
			refreshCurrentFileList(currentDirectoryModel);
		}
	}
	
	/**
	 * Sets whether or not folders should be selectable in the MyWorkspaces tab
	 * 
	 * @param foldersSelectable whether or not folders should be selectable in the MyWorkspaces tab
	 */
	public void setFoldersSelectable(boolean foldersSelectable){
		
		this.foldersSelectable = foldersSelectable;
	}
	
	/**
	 * Sets whether or not only folders should be shown in the MyWorkspaces tab
	 * 
	 * @param showFoldersOnly whether or not only folders should be shown in the MyWorkspaces tab
	 */
	public void setShowFoldersOnly(boolean showFoldersOnly){
		
		this.showFoldersOnly = showFoldersOnly;
		
		if(showFoldersOnly){
			
			//if only folders are being shown, we also need to make sure they're selectable or else the user can't really do anything with them
			this.foldersSelectable = true;
		}
		
		if(currentDirectoryModel != null){
			refreshCurrentFileList(currentDirectoryModel);
		}
	}
	

	/**
	 * Gets whether or not folders are selectable in the MyWorkspaces tab
	 * 
	 * @return whether or not folders are selectable in the MyWorkspaces tab
	 */
	public boolean getFoldersSelectable() {
		return foldersSelectable;
	}
	
	/**
	 * Gets the currently selected file.
	 * 
	 * @return the currently selected file.
	 */
	public FileTreeModel getSelectedFile() {
		return fileNameSelectionModel.getSelectedObject();
	}
	
	/**
	 * Searches the current directory for the given filename. If found,
	 * the file is selected.
	 * 	
	 * @param filename the file to search for and select.
	 */
	public void setSelectedFile(String filename) {
		for(FileTreeModel fileModel : fileNameDataProvider.getList()){
			
			if(fileModel.getFileOrDirectoryName() != null 
					&& fileModel.getFileOrDirectoryName().equals(
							filename)){
				
				if(fileNameSelectionModel.getSelectedObject() == null
						|| !fileNameSelectionModel.getSelectedObject().equals(fileModel)){
					
					fileNameSelectionModel.setSelected(fileModel, true);
					break;
				
				} 
			}
			
			fileNameSelectionModel.setSelected(fileModel, false);
		}
	}
	
	/**
	 * Opens the selected directory.
	 */
	public void openSelectedFolder() {
		
		FileTreeModel selectedFileModel = fileNameSelectionModel.getSelectedObject();
		
		if(selectedFileModel.isDirectory()){
			
			refreshCurrentFileList(selectedFileModel);
			
			view.resetScroll();
			
			fileNameSelectionModel.setSelected(selectedFileModel, false);
			
			currentDirectoryModel = selectedFileModel;
			
			view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
					? currentDirectoryModel.getFileOrDirectoryName()
					: null
			);
			
			view.getUpArrow().setEnabled(true);
		}
	}
	
	/**
	 * Refreshes the current list of visible files based on the directory selected
	 * 
	 * @param directory
	 */
	private void refreshCurrentFileList(FileTreeModel directory){			
		
		if(directory.isDirectory()){
			
			List<FileTreeModel> currentFileList = new ArrayList<FileTreeModel>(directory.getSubFilesAndDirectories());
				
			Iterator<FileTreeModel> itr = currentFileList.iterator();
			
			while(itr.hasNext()){
			
				FileTreeModel currentFile  = itr.next();
				
				if(!currentFile.isDirectory()){
					
					if(showFoldersOnly){
						itr.remove();
						continue;
						
					} else {
						
						if(filteredExtensions != null && !filteredExtensions.isEmpty()){
							
							boolean hasExcludedExtension = false;
							
							for(String extension : filteredExtensions){
								
								if(currentFile.getFileOrDirectoryName().endsWith(extension)){
									hasExcludedExtension = true;
									break;
								}
							}
							
							if(hasExcludedExtension){
								itr.remove();
								continue;
							}
						}
						
						if(fileExtensionsToInclude != null && fileExtensionsToInclude.length != 0){					
						
							boolean hasIncludedExtension = false;
							
							String lowercaseFilename = currentFile.getFileOrDirectoryName().toLowerCase();
							for(String extension : fileExtensionsToInclude){
							    
							    //perform case insensitive search
							    String lowercaseExtension = extension.toLowerCase();
								
								if(lowercaseFilename.endsWith(lowercaseExtension)){
									hasIncludedExtension = true;
									break;
								}
							}
							
							if(!hasIncludedExtension){
								itr.remove();
								continue;
							}
						}					
					}
				}
				
				boolean isFilteredFile = false;
				
				for(String relativePath : filteredFiles){
					
					if(currentFile.getRelativePathFromRoot().equals(relativePath)){
						isFilteredFile = true;
						break;
					}
					
					//try the path without the root in case the caller of filterOutFiles(List<String>) doesn't know the root file
					String pathWithoutRoot = currentFile.getRelativePathFromRoot().replaceFirst(rootDirectoryModel.getFileOrDirectoryName() + "/", "");
					
					if(pathWithoutRoot.equals(relativePath)){
						isFilteredFile = true;
						break;
					}
				}
				
				if(isFilteredFile){
					itr.remove();
					continue;
				}
			}
			
			fileNameDataProvider.getList().clear();
			fileNameDataProvider.getList().addAll(currentFileList);
			fileNameDataProvider.refresh();
			
		} else {
			
			if(messageDisplay != null){
				messageDisplay.showInfoMessage("Failed to refresh", 
				        "Tried to refresh file list but current file model does not point to a directory", null);
			}
		}
	}
	
	/**
	 * Populates the file hierarchy used to display the list of files and create the file path upon submitting a file choice
	 * 
	 * @param rootFile the directory to which the submitted file path should be made relative
	 * @param startFile the directory to start in when once displayed. If set to null, the root directory will be used as the starting point
	 */
	private void populateFileHierarchy(FileTreeModel rootFile, FileTreeModel startFile){
		
		if(rootFile != null){
			
			rootDirectoryModel = rootFile;
			
			if(startFile != null){
				startDirectoryModel = startFile;
			
			} else {
				startDirectoryModel = rootDirectoryModel;
			}
			
			boolean foundFile = false;
			
			if(selectedWorkspaceFile != null){
				
				//If a file location is already specified, search through the file tree model for it and show it in the file selection dialog
				FileTreeModel currentFileModel = rootDirectoryModel;
				
				String[] directories = selectedWorkspaceFile.split("/");
				
				for(int i = 0; i < directories.length; i++){
					
					if(currentFileModel.getSubFilesAndDirectories() != null){
						
						boolean foundDirectory = false;
					
						for(FileTreeModel fileModel : currentFileModel.getSubFilesAndDirectories()){
							
							if(fileModel.getFileOrDirectoryName().equals(directories[i])){										
								
								if(i == (directories.length - 1)){											
									foundFile = true;
									
									currentDirectoryModel = currentFileModel;
									
									view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
											? currentDirectoryModel.getFileOrDirectoryName()
											: null
									);
									
									refreshCurrentFileList(currentDirectoryModel);
									
									view.resetScroll();
								
									if(currentDirectoryModel.equals(rootDirectoryModel) || currentDirectoryModel.equals(startDirectoryModel)){													
										view.getUpArrow().setEnabled(false);
										
									} else {
										view.getUpArrow().setEnabled(true);
									}
									
									//Need to set to another value first to make sure a ValueChangeEvent is fired
									view.getFileNameInput().setValue(null);												
									
									view.getFileNameInput().setValue(directories[directories.length - 1], true);
									
								} else {											
									currentFileModel = fileModel;
									foundDirectory = true;
								}
								
								break;
							}
						}
						
						if(!foundDirectory){
							break;
						}
						
					} else {
						break;
					}
				}
			}
			
			if(!foundFile){
				
				currentDirectoryModel = startDirectoryModel;
				
				view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
						? currentDirectoryModel.getFileOrDirectoryName()
						: null
				);
				
				refreshCurrentFileList(currentDirectoryModel);
				
				view.resetScroll();
			
				view.getUpArrow().setEnabled(false);
				
				if(selectedWorkspaceFile != null){
					
					view.getFileNameInput().setValue(selectedWorkspaceFile, true);					
					
				} else {
					view.getFileNameInput().setValue(null, true);
				}							
			}							
								
		}
	}

    /**
     * Set the copy file request for the presenter.  If this is not null
     * the presenter will attempt to copy the file based on the selected item in
     * the my workspaces panel.
     * 
     * @param copyFileReq - The CopyFileRequest object used to perform the copy file (can be null if the copy operation is not needed).
     */
    public void setCopyFileRequest(CopyFileRequest copyFileReq) {
        copyFileRequest = copyFileReq;
        
    }
}
