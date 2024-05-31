/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.file;

import java.util.ArrayList;
import java.util.logging.Logger;

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
 * The Class FileSelectionPresenter.
 */
public class FileSelectionPresenter{

    /** The Constant logger. */
    private static final Logger logger = Logger.getLogger(FileSelectionPresenter.class.getName());
    
    /** The view. */
    private FileSelectionView view;
    
	/** The file name data provider. */
	private ListDataProvider<FileTreeModel> fileNameDataProvider = new ListDataProvider<FileTreeModel>(new ArrayList<FileTreeModel>());
	
	/** The file name selection model. */
	private SingleSelectionModel<FileTreeModel> fileNameSelectionModel = new SingleSelectionModel<FileTreeModel>();
	
	/** The domain directory model. */
	private FileTreeModel domainDirectoryModel = null;
	
	/** The current directory model. */
	private FileTreeModel currentDirectoryModel = null;
	
	/** The file extensions to include. */
	private String[] fileExtensionsToInclude;
	
	private String selectedWorkspaceFile = null;
	
	/**
     * Instantiates a new file browser presenter.
     *
     * @param view the view
     */
    public FileSelectionPresenter(FileSelectionView view) {
        
    	this.view = view;
    	
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
					
					if(!selectedFileModel.isDirectory()){
						
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

                    	FileTreeModel selectedFileModel = fileNameSelectionModel.getSelectedObject();
    					
    					if(selectedFileModel.isDirectory()){
    						
    						fileNameDataProvider.getList().clear();
    						fileNameDataProvider.getList().addAll(selectedFileModel.getSubFilesAndDirectories());
    						fileNameDataProvider.refresh();
    						
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
                    lastClick = System.currentTimeMillis();
                }
			}
			
		});
		
		view.setUpArrowCommand(new Scheduler.ScheduledCommand() {
			
			@Override
			public void execute() {
				
				if(currentDirectoryModel != null && currentDirectoryModel.getParentTreeModel() != null){
					
					fileNameDataProvider.getList().clear();
					fileNameDataProvider.getList().addAll(currentDirectoryModel.getParentTreeModel().getSubFilesAndDirectories());
					fileNameDataProvider.refresh();
					
					view.resetScroll();
					
					currentDirectoryModel = currentDirectoryModel.getParentTreeModel(); 

					if(currentDirectoryModel.equals(domainDirectoryModel)){
						view.getUpArrow().setEnabled(false);
					}
					
					view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
							? currentDirectoryModel.getFileOrDirectoryName()
							: null
					);
				}
			}
		});
		
		refresh();
    }
	
    /**
     * Alters the appearance of the cursor to indicate waiting state.
     * 
     * @param waiting whether or not we are waiting
     */
	@SuppressWarnings("unused")
    private void showWaiting(boolean waiting){
		
		if(waiting) {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "wait");
		} else {
			RootPanel.getBodyElement().getStyle().setProperty("cursor", "default");
		}
	}
	
	/**
	 * Refreshes the content in the view using the Domain folder on the server.
	 */
	public void refresh(){
		
		final String msg = "fileSelectionPresenter";
		logger.info(msg);
		
		//TODO: Reimplement when selecting files from the server is enabled
//		AsyncCallback<FetchRootDirectoryModelResult> callback = new AsyncCallback<FetchRootDirectoryModelResult>(){
//
//			@Override
//			public void onFailure(Throwable t) {
//				
//				showWaiting(false);
//				//handleCallbackFailure(logger, msg, t);
//			}
//
//			@Override
//			public void onSuccess(FetchRootDirectoryModelResult result) {
//				
//				if(result != null){
//					
//					showWaiting(false);
//					
//					if(result.getDomainDirectoryModel() != null){
//						
//						domainDirectoryModel = result.getDomainDirectoryModel();
//						
//						boolean foundFile = false;
//						
//						if(selectedWorkspaceFile != null){
//							
//							//If a file location is already specified, search through the file tree model for it and show it in the file selection dialog
//							FileTreeModel currentFileModel = domainDirectoryModel;
//							
//							String[] directories = selectedWorkspaceFile.split("/");
//							
//							for(int i = 0; i < directories.length; i++){
//								
//								if(currentFileModel.getSubFilesAndDirectories() != null){
//									
//									boolean foundDirectory = false;
//								
//									for(FileTreeModel fileModel : currentFileModel.getSubFilesAndDirectories()){
//										
//										if(fileModel.getFileOrDirectoryName().equals(directories[i])){										
//											
//											if(i == (directories.length - 1)){											
//												foundFile = true;
//												
//												currentDirectoryModel = currentFileModel;
//												
//												view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
//														? currentDirectoryModel.getFileOrDirectoryName()
//														: null
//												);
//												
//												fileNameDataProvider.getList().clear();
//												fileNameDataProvider.getList().addAll(currentDirectoryModel.getSubFilesAndDirectories());
//												fileNameDataProvider.refresh();
//												
//												view.resetScroll();
//											
//												if(currentDirectoryModel.equals(domainDirectoryModel)){													
//													view.getUpArrow().setEnabled(false);
//													
//												} else {
//													view.getUpArrow().setEnabled(true);
//												}
//												
//												//Need to set to another value first to make sure a ValueChangeEvent is fired
//												view.getFileNameInput().setValue(null);												
//												
//												view.getFileNameInput().setValue(directories[directories.length - 1], true);
//												
//											} else {											
//												currentFileModel = fileModel;
//												foundDirectory = true;
//											}
//											
//											break;
//										}
//									}
//									
//									if(!foundDirectory){
//										break;
//									}
//									
//								} else {
//									break;
//								}
//							}
//						}
//						
//						if(!foundFile){
//							
//							currentDirectoryModel = domainDirectoryModel;
//							
//							view.getDirectoryName().setText(currentDirectoryModel.getFileOrDirectoryName() != null 
//									? currentDirectoryModel.getFileOrDirectoryName()
//									: null
//							);
//							
//							fileNameDataProvider.getList().clear();
//							fileNameDataProvider.getList().addAll(currentDirectoryModel.getSubFilesAndDirectories());
//							fileNameDataProvider.refresh();
//							
//							view.resetScroll();
//						
//							view.getUpArrow().setEnabled(false);
//							
//							if(selectedWorkspaceFile != null){
//								
//								view.getFileNameInput().setValue(selectedWorkspaceFile, true);					
//								
//							} else {
//								view.getFileNameInput().setValue(null, true);
//							}							
//						}							
//											
//					} else {
//						logger.warning("The domain directory could not be found while attempting to get its file tree model.");
//					}
//				}			
//			}		
//		};
//		
//		logger.info("Getting domain directory model for file selection.");
		
//		FetchRootDirectoryModel action = new FetchRootDirectoryModel(fileExtensionsToInclude);
//		showWaiting(true);
//		
//		SharedResources.getInstance().getDispatchService().execute(action, callback);
	}
	
	/**
	 * Sets the file extensions allowed for selection.
	 * 
	 * @param extensions the file extensions
	 */
	public void setAllowedFileExtensions(String[] extensions){
		fileExtensionsToInclude = extensions;		
		refresh();
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
	 * Submits the user's file choice and invokes the given callback once their choice has been processed. In Upload mode, this method will 
	 * call a file servlet in order to upload the file.
	 * 
	 * @param callback The file selection callback.
	 */
	public void submitFileChoice(FileSelectionCallback callback, FileSelectionView.Mode mode){
		
		if(mode.equals(FileSelectionView.Mode.UPLOAD)){
		
			//file upload needs to be handled in the view since we need to use the form element to submit the file to the server
			view.uploadChosenFile(callback);
			
		
		}else if(mode.equals(FileSelectionView.Mode.MY_WORKSPACE)){

			if(fileNameSelectionModel.getSelectedObject() != null){
			
				StringBuilder sb = new StringBuilder();
				
				FileTreeModel currentFileModel = fileNameSelectionModel.getSelectedObject();
				
				sb.append(currentFileModel.getFileOrDirectoryName());
				
				while(currentFileModel.getParentTreeModel() != null 
						&& !currentFileModel.getParentTreeModel().equals(domainDirectoryModel)){
					
					currentFileModel = currentFileModel.getParentTreeModel();
					
					sb.insert(0, currentFileModel.getFileOrDirectoryName() + "/");
				}
				
				selectedWorkspaceFile = sb.toString();
				
				if(callback != null){
					callback.onClientFileUploaded(selectedWorkspaceFile, null);
				}
				
			} else {
				
				String failureReason;
				
				if(view.getFileNameInput().getValue() != null){
					failureReason = "No file was found matching the file name '" + selectedWorkspaceFile +"'.";
				
				} else {
					failureReason = "Please enter a file name or select a file.";
				}
				
				if(callback != null){
					callback.onFailure(failureReason);
				}
			}
		}
	}
}
