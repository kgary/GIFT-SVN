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
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.gwt.client.widgets.file.FileSaveAsView.FileSaveAsCallback;
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
 * A helper class for FileSaveAsWidget that handles the bulk of the behind-the-scenes logic in the MyWorkspaces tab.
 * 
 * @author nroberts
 */
public class FileSaveAsPresenter{
        
    /** instance of the logger */
    private static Logger logger = Logger.getLogger(FileSaveAsPresenter.class.getName());
    
    /** The view. */
    private FileSaveAsView view;
    
	/** The file name data provider. */
	private ListDataProvider<FileTreeModel> fileNameDataProvider = new ListDataProvider<FileTreeModel>(new ArrayList<FileTreeModel>());
	
	/** The file name selection model. */
	private SingleSelectionModel<FileTreeModel> fileNameSelectionModel = new SingleSelectionModel<FileTreeModel>();
	
	/** The root directory model. */
	private FileTreeModel rootDirectoryModel = null;
	
	/** The starting directory model. This model also serves as the top most directory that the user can select from.  It can be the same
	 * as the root direcotry model, but can be a subfolder.  In many cases it is set to the user's workspace directory to prevent the user
	 * from selecting outside of their own workspace.
	 */
	private FileTreeModel startDirectoryModel = null;
	
	/** The current directory model. */
	private FileTreeModel currentDirectoryModel = null;
	
	/** The starting directory path to start the selection dialog with.  This is not the same as the startDirectoryModel, instead
	 *  the startingDirectoryPath is used to set the initial directory that the dialog shows and can be a subfolder within the 
	 *  startDirectoryModel.  
	 */
	private String startingDirectoryPath = null;
	
	/** The file extensions to include. */
	private String[] fileExtensionsToInclude;
	
	private String selectedWorkspaceFile = null;
	
	private List<String> filteredFiles = new ArrayList<String>();
	
	private List<String> filteredExtensions = new ArrayList<String>();
	
	private boolean showFoldersOnly = false;
	
	private boolean foldersSelectable = false;
	
	private boolean allowNavigationToSubfolders = true;
	
	private CanGetRootDirectory rootGetter = null;
	
	private DisplaysMessage messageDisplay = null;
	
	private FileTreeModel folderToOpenOnRefresh = null;
	
	private FileListLoadedCallback listLoadedCallback = null;
	
	/**
	 * Creates a new file save as presenter modifying the specified file selection view. The file selection presenter will invoke the logic 
	 * needed to get the MyWorkpaces tab's root directory from the server.
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param view the view that this presenter should modify
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
    public FileSaveAsPresenter(FileSaveAsView view, CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay) {
        
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
							
							if(!fileModel.isDirectory() || foldersSelectable){
							
								if(fileNameSelectionModel.getSelectedObject() == null
										|| !fileNameSelectionModel.getSelectedObject().equals(fileModel)){
									
									fileNameSelectionModel.setSelected(fileModel, true);
									break;
								
								} 
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
				
				if(!allowNavigationToSubfolders) {
					return;
				}
				
				long clickedAt = System.currentTimeMillis();

                if(event.getNativeEvent().getType().contains("click")){
                	
                    if(clickedAt - lastClick < 300) { // double click on 2 clicks detected within 300 ms

                    	FileTreeModel selectedFileModel = fileNameSelectionModel.getSelectedObject();
    					
    					if(selectedFileModel.isDirectory()){
    						
    						refreshCurrentFileList(selectedFileModel);
    						
    						view.resetScroll();
    						
    						fileNameSelectionModel.setSelected(selectedFileModel, false);

    						setCurrentDirectoryModel(selectedFileModel);
    						
    						view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
    								? currentDirectoryModel.getFileOrDirectoryName()
    								: null
    						);
    						
    						view.getUpArrow().setEnabled(true);
    					}
                    	
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

					setCurrentDirectoryModel(currentDirectoryModel.getParentTreeModel());

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
				public void onSuccess(FileTreeModel rootFile, FileTreeModel startFile) {
					showWaiting(false);
					
					populateFileHierarchy(rootFile, startFile);
				}
				
				@Override
				public void onFailure(Throwable thrown) {
					showWaiting(false);
					
					if(listLoadedCallback != null){
						listLoadedCallback.onFailure();
					}
				}
				
				@Override
				public void onFailure(String reason) {
					showWaiting(false);
					
					if(listLoadedCallback != null){
						listLoadedCallback.onFailure();
					}
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
	 * Submits the user's file choice and invokes the given callback once their choice has been processed.
	 * 
	 * @param callback The file selection callback.
	 * @param mode the current file selection mode
	 */
	public void submitFileChoice(final FileSaveAsCallback callback, FileSaveAsView.Mode mode){
		
		String value = view.getFileNameInput().getValue();
		
		if(value != null && !value.isEmpty()){
			
			value = value.trim();
		
			if(!value.isEmpty()){
				
				if(mode.equals(FileSaveAsView.Mode.MY_WORKSPACE)){
						
					BsLoadingDialogBox.display("Verifying File Name", "Please wait while your file name is verified.");
					
					final String fileName = value;			
					
					setListLoadedCallback(new FileListLoadedCallback() {
						
						@Override
						public void onFileListLoaded() {	
							
							/*if(fileExtensionsToInclude != null && fileExtensionsToInclude.length != 0){
								
								boolean hasAllowedExtension = false;
								
								for(String extension : fileExtensionsToInclude){
									
									if(fileName.endsWith(extension)){
										
										hasAllowedExtension = true;
										break;
									}
								}
								
								if(!hasAllowedExtension){
									
									StringBuilder sb = new StringBuilder();
									sb.append("The file specified must have a file name ending in ");
									
									for(int i = 0; i < fileExtensionsToInclude.length; i++){
										
										if(i == fileExtensionsToInclude.length - 1){
											
											if(fileExtensionsToInclude.length == 1){
												sb.append(fileExtensionsToInclude[i]);
												
											} else {
												sb.append("or ");
												sb.append(fileExtensionsToInclude[i]);
											}
											
										} else if(i == fileExtensionsToInclude.length - 1 && fileExtensionsToInclude.length == 2){
											sb.append(fileExtensionsToInclude[i]);
											sb.append(" ");
											
										} else {
											sb.append(fileExtensionsToInclude[i]);
											sb.append(", ");
										}
									}
									
									sb.append(".");
									
									callback.onFailure(sb.toString());
									return;					
								}
								
							} else if(filteredExtensions != null && !filteredExtensions.isEmpty()){
								
								boolean hasFilteredExtension = true;
								
								for(String extension : filteredExtensions){
									
									if(fileName.endsWith(extension)){
										
										hasFilteredExtension = false;
										break;
									}
								}
								
								if(hasFilteredExtension){
									
									StringBuilder sb = new StringBuilder();
									sb.append("The file specified must not have a file name ending in ");
									
									for(int i = 0; i < filteredExtensions.size(); i++){
										
										if(i == filteredExtensions.size() - 1){
											
											if(filteredExtensions.size() == 1){
												sb.append(filteredExtensions.get(i));
												
											} else {
												sb.append("or ");
												sb.append(filteredExtensions.get(i));
											}
											
										} else if(i == filteredExtensions.size() - 1 && filteredExtensions.size() == 2){
											sb.append(filteredExtensions.get(i));
											sb.append(" ");
											
										} else {
											sb.append(filteredExtensions.get(i));
											sb.append(", ");
										}
									}
									
									sb.append(".");
									
									callback.onFailure(sb.toString());
									return;					
								}
							}*/
						
							StringBuilder sb = new StringBuilder();
							
							FileTreeModel saveDirectoryModel = currentDirectoryModel;
							
							if(!saveDirectoryModel.equals(rootDirectoryModel)){
								
								sb.append(saveDirectoryModel.getFileOrDirectoryName()).append("/").append(fileName);
								
								while(saveDirectoryModel.getParentTreeModel() != null 
										&& !saveDirectoryModel.getParentTreeModel().equals(rootDirectoryModel)){
									
									saveDirectoryModel = saveDirectoryModel.getParentTreeModel();
									
									sb.insert(0, saveDirectoryModel.getFileOrDirectoryName() + "/");
								}
								
							} else {
								sb.append(fileName);
							}
							
							selectedWorkspaceFile = sb.toString();
							
							if(callback != null){
								callback.onSuccess(selectedWorkspaceFile);
							}
							
							BsLoadingDialogBox.remove();
						}
	
						@Override
						public void onFailure() {
							BsLoadingDialogBox.remove();
						}
					});
				}
				
			} else {
				
				String failureReason = "Please enter a file name with at least one non-whitespace character.";

				if(callback != null){
					callback.onFailure(failureReason);
				}
			}
		
		} else {
			
			String failureReason = "Please enter a file name.";

			if(callback != null){
				callback.onFailure(failureReason);
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
		
		this.foldersSelectable = true;
	}
	
	/**
	 * Sets whether or not the user can navigate into subfolders.
	 * 
	 * @param value True if the user can navigate into subfolders, false
	 * otherwise.
	 */
	public void setAllowNavigationToSubfolders(boolean value) {
		this.allowNavigationToSubfolders = value;
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
							
							for(String extension : fileExtensionsToInclude){
								
								if(currentFile.getFileOrDirectoryName().endsWith(extension)){
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
	 * Creates a folder within the currently visible directory with the folder name specified
	 * 
	 * @param folderName the name of the folder to create
	 * @param folderCreator an object capable of creating folders
	 */
	public void createFolder(String folderName, CanCreateFolder folderCreator) {

		// Get a workspace relative path based on the current directory model.
		String currentDirectoryPath = currentDirectoryModel.getRelativePathFromRoot(true);
	
		BsLoadingDialogBox.display("Creating Folder", "Please wait...");
		
		logger.fine("createFolder currentDirectoryPath = " + currentDirectoryPath);
		folderCreator.createFolder(currentDirectoryPath, folderName, new CreateFolderCallback() {
			
			@Override
			public void onFolderCreated() {	
				BsLoadingDialogBox.remove();
				
				folderToOpenOnRefresh = currentDirectoryModel;
				
				refresh();
				
				view.setCreateFolderDialogVisible(false);
			}
			
			@Override
			public void onFailure(Throwable thrown) {
				BsLoadingDialogBox.remove();
				
				messageDisplay.showErrorMessage("Failed to create folder", 
				        "An error occurred while creating the folder: " + thrown.toString(), null);
				
				refresh();
			}
			
			@Override
			public void onFailure(String reason) {
				BsLoadingDialogBox.remove();
				
				messageDisplay.showErrorMessage("Failed to create folder", 
				        "An error occurred while creating the folder: " + reason, null);
				
				refresh();
			}
		});
	}

	/**
	 * Clears the file name field
	 */
	public void clear() {
		
		selectedWorkspaceFile = null;
		view.getFileNameInput().setValue(null, true);
	}
	
	/**
	 * Populates the file hierarchy used to display the list of files and create the file path upon submitting a file choice
	 * 
	 * @param rootFile the directory to which the submitted file path should be made relative
	 * @param startFile the directory to start in when once displayed. If set to null, the root directory will be used as the starting point
	 *        The startFile also serves as the "top most" file/directory that the user can select from.  
	 */
	private void populateFileHierarchy(FileTreeModel rootFile, FileTreeModel startFile){
		
		if(rootFile != null){
			
			rootDirectoryModel = rootFile;

			if(startFile != null){
                startDirectoryModel = startFile;
            
            } else {
                startDirectoryModel = rootDirectoryModel;
            }
			
			FileTreeModel startingDirectoryModel = null;
			if(startingDirectoryPath != null && !startingDirectoryPath.isEmpty()){
			    
			    startingDirectoryModel = rootDirectoryModel.getModelFromRelativePath(startingDirectoryPath);
			    logger.info("startDirectoryModel = " + startingDirectoryModel);
			    if (startingDirectoryModel == null) {
			        startingDirectoryModel = startDirectoryModel;
			    }
			    
			    // Clear the starting directory path
			    startingDirectoryPath = null;
			
			} else {
			    startingDirectoryModel = startDirectoryModel;
			}
			
			
			
			
			boolean foundFile = false;
			
			FileTreeModel fileParentModel = null;
			
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

									setCurrentDirectoryModel(currentFileModel);
									
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
									
								}else {			
									
									if(i == (directories.length - 2)){												
										fileParentModel = fileModel;
									}
									
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
				
				if(fileParentModel != null){

					setCurrentDirectoryModel(fileParentModel);
					
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
					
				} else {

					setCurrentDirectoryModel(startingDirectoryModel);
					
					view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
							? currentDirectoryModel.getFileOrDirectoryName()
							: null
					);
					
					refreshCurrentFileList(currentDirectoryModel);
					
					view.resetScroll();
				
					if(startingDirectoryModel.equals(rootDirectoryModel) || currentDirectoryModel.equals(startDirectoryModel)){     
					    view.getUpArrow().setEnabled(false);
					} else {
					    view.getUpArrow().setEnabled(true);
					}
				}
			}							
		}
		
		if(folderToOpenOnRefresh != null){
			
			openFolder(folderToOpenOnRefresh);
			
			folderToOpenOnRefresh = null;
		}
		
		if(listLoadedCallback != null){
			listLoadedCallback.onFileListLoaded();
		}
	} 
	
	/**
	 * Opens the specified folder in the file hierarchy if it exists. 
	 * 
	 * @param folder the folder to open
	 */
	private void openFolder(FileTreeModel folder){
		
		List<String> foldersInOrder = new ArrayList<String>();
		
		FileTreeModel searchFolder = folder;
		
		while(searchFolder.getParentTreeModel() != null){
			foldersInOrder.add(0, searchFolder.getFileOrDirectoryName());
			searchFolder = searchFolder.getParentTreeModel();
		}
		
		FileTreeModel foundFolder  = rootDirectoryModel;
		
		Iterator<String> itr = foldersInOrder.iterator();
		
		if(!foldersInOrder.isEmpty()){
			while(itr.hasNext()){
				
				String folderName = itr.next();
				
				foundFolder = foundFolder.getChildByName(folderName);
				
				if(foundFolder == null){
					return;
				}
				
				itr.remove();
			}
		}

		setCurrentDirectoryModel(foundFolder);
		
		view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
				? currentDirectoryModel.getFileOrDirectoryName()
				: null
		);
		
		if(currentDirectoryModel.equals(rootDirectoryModel) || currentDirectoryModel.equals(startDirectoryModel)){													
			view.getUpArrow().setEnabled(false);
			
		} else {
			view.getUpArrow().setEnabled(true);
		}

		refreshCurrentFileList(currentDirectoryModel);	
	}
	
	private void setCurrentDirectoryModel(FileTreeModel model) {
		currentDirectoryModel = model;
		if(currentDirectoryModel == startDirectoryModel) {
			view.setAllowFolderCreation(false);
		} else { 
			view.setAllowFolderCreation(true);
		}
	}
	
	public boolean isFileOrFolderInCurrentDirectory(String fileOrFolderName) {
		FileTreeModel fileTreeModel = currentDirectoryModel.getChildByName(fileOrFolderName);
		if(fileTreeModel == null) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Finds the child model (file or folder) that has the given name.
	 * 
	 * @param fileOrFolderName The name of the file or folder to retrieve.
	 * @return The file tree model if found, otherwise returns null.
	 */
	public FileTreeModel getFileModelInCurrentDirectory(String fileOrFolderName) {
	    return currentDirectoryModel.getChildByName(fileOrFolderName);
	}
	
	/**
	 * Sets a callback to execute once the file list is loaded. If the file list is already loaded, then the callback will be executed immediately
	 * 
	 * @param loadedCallback the callback to execute
	 */
	public void setListLoadedCallback(final FileListLoadedCallback loadedCallback) {
		
		if(rootDirectoryModel != null){
			
			loadedCallback.onFileListLoaded();
			
			listLoadedCallback = null;
		
		} else {
			
			listLoadedCallback = new FileListLoadedCallback() {
				
				@Override
				public void onFileListLoaded() {
					
					loadedCallback.onFileListLoaded();
					
					listLoadedCallback = null;
				}

				@Override
				public void onFailure() {
					
					loadedCallback.onFailure();
					
					listLoadedCallback = null;
				}
				
			};
		}
	}

	/**
	 * Sets the starting directory for the dialog.  This directory path must be a subfolder of the
	 * {@link FileSaveAsPresenter#startDirectoryModel startDirectoryModel} of the widget.   The value is cached here
	 * and used later when the dialog is populated in the 
	 * {@link FileSaveAsPresenter#populateFileHierarchy(FileTreeModel, FileTreeModel) populateFileHierarchy()} method.
	 * 
	 * @param path - The starting directory path (relative to the root).  This should not be null.
	 */
    public void setStartingDirectory(String path) {
        
        startingDirectoryPath = path;
    }
}
