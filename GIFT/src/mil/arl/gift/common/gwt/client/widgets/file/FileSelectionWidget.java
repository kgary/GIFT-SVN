/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.common.io.FileTreeModel;

import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.HasData;

/**
 * A widget that allows the user to either choose a file to upload to the server or select a file from the server.
 * 
 * This widget will not enact any file uploading or file selection logic until submitFileChoice(FileSelectionCallback) is called. 
 * This allows such logic to be delayed until another encompassing widget determines that such operations should proceed, giving us the 
 * option of canceling a file upload or file selection if a validation check fails. The callback provided will be given the appropriate 
 * file name uploaded or selected once the corresponding operation has completed.
 * 
 * By default, no height is initially declared for the list of server files, meaning the list will be dynamically sized depending on the number 
 * of files in the currently selected directory. In order to set this list to a specific size and enable scrolling, a call must be made to 
 * setFileTableHeight(String height). 
 * 
 * !IMPORTANT!: When uploading a file, this widget MUST be attached to the DOM when submitFileChoice(FileSelectionCallback) is called, 
 * otherwise the upload request will not be sent. The reason for this is that the 'form' element used to upload will not be able 
 * to send the appropriate request to the its servlet unless it is attached to the DOM. Any logic that may cause this widget to be 
 * detached from the DOM (e.g. PopupPanel.hide(),  DialogBox.hide(), Modal.hide()) should only be called after the upload request has been 
 * processed. 
 * 
 * @author nroberts
 */
public class FileSelectionWidget extends Composite implements FileSelectionView {

    /** The logger. */
    private static Logger logger = Logger.getLogger(FileSelectionWidget.class.getName());
    
    /** The loading icon url. */
	private static String LOADING_ICON_URL = "images/loading.gif";
    
	private static FileSelectionWidgetUiBinder uiBinder = GWT
			.create(FileSelectionWidgetUiBinder.class);
	
	/** The directory icon url. */
	private static String DIRECTORY_ICON_URL = "images/folder.png";
	
	/** The file icon url. */
	private static String FILE_ICON_URL = "images/file.png";
	
	/** Additional information to display if the user selects a file with the wrong extension. */
	private String additionalFileExtInfo = null;

	interface FileSelectionWidgetUiBinder extends
			UiBinder<Widget, FileSelectionWidget> {
	}
	
	@UiField
	protected FormPanel form;

	@UiField
	protected FileUpload fileUpload;
	
	/** The up arrow. */
	@UiField
	protected MenuItem upArrow;
	
	/** The up arrow. */
	@UiField
	protected MenuItem directoryName;
	
	/** The file name data grid. */
	@UiField
	protected CellTable<FileTreeModel> fileNameTable;
	
	/** The file name input. */
	@UiField
	protected TextBox fileNameInput;
	
	@UiField
	protected TabPanel tabPanel;
	
	@UiField
	protected Widget uploadTab;
	
	@UiField
	protected ScrollPanel fileTableScrollPanel;
	
	@UiField
	protected Image workspaceLoadImage;
	
	@UiField
	protected DeckPanel workspaceDeck;
	
	@UiField
	protected Widget workspaceLoadPanel;
	
	@UiField
	protected Widget loadedContentPanel;
	
	/**
	 * The file upload callback that will be used to handle when the user has made a file selection.
	 */
	private FileSelectionCallback fileUploadCallback = null;
	
	/**
	 * The copy file request is used to house the logic to perform the copy file operation.  A copy file
	 * operation means that the dialog can be setup to copy the selected item from the my workspaces panel/tab
	 * to a specified destination.  The copyfilerequest object holds the logic to perform the copy operation.
	 */
	private CopyFileRequest copyFileRequest = null;
	
	/**
	 * The presenter associated with this file selector. Ordinarily, we wouldn't give the view direct access to its presenter, but in this case, 
	 * we plan to use this widget in a number of places, so we want to keep the process of setting up the widget as simple as possible. Because 
	 * of this, most of the logic used to set up the presenter is handled in the view's constructor, making handling file uploads as simple as 
	 * calling the constructor and then calling submitFileChoice(FileSelectionCallback)
	 */
	private FileSelectionPresenter presenter;
	
	public ModeChangedCallback modeChangedCallback;
	
	private FileSelectionView.Mode currentMode = FileSelectionView.Mode.UPLOAD;
	
	
	/**
	 * Creates a new file selection widget with no capability of uploading or showing a list of files. This constructor mainly exists to 
	 * provide common functionality to other constructors and should not be used on its own.
	 */
	private FileSelectionWidget(){
		initWidget(uiBinder.createAndBindUi(this));
		
		workspaceLoadImage.setUrl(LOADING_ICON_URL);
		
		Column<FileTreeModel, String> fileIconColumn = new Column<FileTreeModel, String>(new ImageCell()){

			@Override
			public String getValue(FileTreeModel object) {

				if(object != null && object.isDirectory()){
					return DIRECTORY_ICON_URL;
					
				} else {
					return FILE_ICON_URL;
				}
			}
			
		};
		fileNameTable.addColumn(fileIconColumn);
		fileNameTable.setColumnWidth(fileIconColumn, "25px");
		
		Column<FileTreeModel, String> fileNameColumn = new Column<FileTreeModel, String>(new TextCell()){

			@Override
			public String getValue(FileTreeModel object) {
				return object.getFileOrDirectoryName();
			}
			
		};
		
		fileNameTable.addColumn(fileNameColumn);
		fileNameTable.setColumnWidth(fileNameColumn, "100%");
		fileNameTable.setPageSize(Integer.MAX_VALUE);
		
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);
		
		tabPanel.selectTab(0, true);
		
		form.addSubmitCompleteHandler(new FormPanel.SubmitCompleteHandler() {

            @Override
            public void onSubmitComplete(SubmitCompleteEvent event) {
            	
            	BsLoadingDialogBox.remove();
                
            	FileSelectionWidgetUtil.handleFileUploadResponse(event.getResults(), fileUploadCallback);
            }
        });
		
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				
				if(event.getSelectedItem() == tabPanel.getWidgetIndex(uploadTab)){
					
					currentMode = FileSelectionView.Mode.UPLOAD;
					
					if(modeChangedCallback != null){
						modeChangedCallback.onModeChanged(currentMode);
					}
					
				} else if(event.getSelectedItem() == tabPanel.getWidgetIndex(workspaceDeck)){
					
					currentMode = FileSelectionView.Mode.MY_WORKSPACE;
					
					if(modeChangedCallback != null){
						modeChangedCallback.onModeChanged(currentMode);
					}
				}
			}
		});
		
		workspaceDeck.showWidget(workspaceDeck.getWidgetIndex(workspaceLoadPanel));
	}
	
	/**
	 * Creates a file selection widget that only allows users to upload files. 
	 * 
	 * Uploaded files will be sent to the servlet at the given URL to be handled on the server. Errors and other information will be reported to 
	 * the given display object.
	 * 
	 * @param uploadServletUrl the URL of the servlet to send the uploaded file to.
	 */
	public FileSelectionWidget(String uploadServletUrl){
		
		this();
		
		String hostURL = GWT.getHostPageBaseURL();
		
		if(uploadServletUrl.contains(hostURL)){
			form.setAction(hostURL + uploadServletUrl);
			
		} else {		
			form.setAction(hostURL + uploadServletUrl);
		}
		
		tabPanel.remove(workspaceDeck);
		tabPanel.selectTab(tabPanel.getWidgetIndex(uploadTab), true);
		
		presenter = new FileSelectionPresenter(this, null, null);
		
		currentMode = Mode.UPLOAD;
	}
	
	/**
	 * Creates a file selection widget that only allows users to select files from a root directory on the server. 
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
	public FileSelectionWidget(CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay){
		
		this();
		
		presenter = new FileSelectionPresenter(this, rootGetter, messageDisplay);
		
		tabPanel.remove(uploadTab);
		tabPanel.selectTab(tabPanel.getWidgetIndex(workspaceDeck), true);
		
		currentMode = Mode.MY_WORKSPACE;
	}
	
	/**
	 * Creates a file selection widget that allows users to upload files or select files from a root directory on the server.
	 * 
	 * Uploaded files will be sent to the servlet at the given URL to be handled on the server. The root directory will be retrieved 
	 * using the instance of {@link CanGetRootDirectory} provided.Errors and other information will be reported to 
	 * the given display object.
	 * 
	 * @param uploadServletUrl the URL of the servlet to send the uploaded file to.
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay an object that can display messages to the user
	 */
	public FileSelectionWidget(String uploadServletUrl, CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay){	
		
		this();
		
		String hostURL = GWT.getHostPageBaseURL();
		
		if(uploadServletUrl.contains(hostURL)){
			form.setAction(hostURL + uploadServletUrl);
			
		} else {		
			form.setAction(hostURL + uploadServletUrl);
		}
		
		presenter = new FileSelectionPresenter(this, rootGetter, messageDisplay);
		
		tabPanel.selectTab(tabPanel.getWidgetIndex(uploadTab), true);
		
		currentMode = Mode.UPLOAD;

	}
	
	@Override
	public void submitFileChoice(final FileSelectionCallback callback){		
		presenter.submitFileChoice(callback, currentMode);
	}
	
	@Override
	public void uploadChosenFile(final FileSelectionCallback callback){
		
	    StringBuilder errorMessage = new StringBuilder("");
	    String fileName = fileUpload.getFilename();
	    if (!verifyFileName(fileName, errorMessage)) {
	        callback.onFailure(errorMessage.toString());
	        return;
	    }

		fileUploadCallback = callback;
		
		form.submit();
		
	}
	
	/**
	 * Gets the value in the filename textbox.
	 * 
	 * @return the value in the filename textbox.
	 */
	public String getFileNameInputValue() {
		return (fileNameInput.getValue() == null) ? "" : fileNameInput.getValue();
	}
	
	/**
	 * Searches the current directory for the given filename. If found,
	 * the file is selected.
	 * 	
	 * @param filename the file to search for and select.
	 */
	public void setSelectedFile(String filename) {
		presenter.setSelectedFile(filename);
	}
	
	/**
	 * Gets the selected file tree model.
	 * 
	 * @return the selected file tree model.
	 */
	public FileTreeModel getSelectedFile() {
		return presenter.getSelectedFile();
	}
	
	/**
	 * Opens the selected folder.
	 */
	public void openSelectedFolder() {
		presenter.openSelectedFolder();
	}
	
	/**
	 * Internal function used to verify the filename against the allowed extensions
	 * that are used in the dialog.
	 * @param fileName - The filename that is selected in the dialog.
	 * @param errorMsg - Contains the returned error message (if any) if the file is not verified.
	 * @return - true if verified, false otherwise.  If false is returned, the errorMsg should contain the reason why it failed.
	 */
	private boolean verifyFileName(String fileName, StringBuilder errorMsg) {
		
	    boolean verified = true;
	    errorMsg.setLength(0);
	    
	    String[] allowedExtensions = presenter.getAllowedFileExtensions();
	    List<String> filteredExtensions = presenter.getFilteredFileExtensions();
	    
	    
	    logger.fine("verifyFileName called for filename: " + fileName + ", extensions to check: " + Arrays.toString(allowedExtensions));
	    
	    
        
        try {
            if(allowedExtensions == null && filteredExtensions.isEmpty()) {
                logger.fine("verifyFileName - no verification needed. Allowed extensions are not specified and there are no filtered extensions specified.");
                return true;
            }
            
            
            // Check allowed extensions if there are any set.
            if (allowedExtensions != null) {
                
                boolean hasAllowedExtension = false;
                
                String lowercaseFilename = fileName.toLowerCase();
                for(String extension : allowedExtensions){
                    
                    //perform case insensitive search
                    String lowercaseExtension = extension.toLowerCase();
                    
                    if(lowercaseFilename.endsWith(lowercaseExtension)){
                        hasAllowedExtension = true;
                        break;
                    }
                }
                if(!hasAllowedExtension){
                    
                    StringBuilder sb = new StringBuilder();
                    sb.append("The file selected must have a file name ending in: <br/> ");
                                
                    sb.append(formatExtensionList(Arrays.asList(allowedExtensions)));
                    sb.append(".");
                    if(additionalFileExtInfo != null) {
                    	sb.append("<br/>");
                    	sb.append(additionalFileExtInfo);
                    }
         
                    errorMsg.append(sb);
                    verified = false;
                }
            }
            
            
            // Only keep validating if the filename is still valid and there are filtered extensions to check against.
            if (verified && !filteredExtensions.isEmpty()) { 
                // Check for filtered extensions.
                boolean hasFilteredExtension = false;
                
                for (String filterExt : filteredExtensions) {
                    if(fileName.endsWith(filterExt)) {
                        hasFilteredExtension = true;
                        break;
                    }
                }
                
                if (hasFilteredExtension) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("The file selected must NOT have a file name ending in ");
                    sb.append(formatExtensionList(filteredExtensions));
                    sb.append(".");
                    
                    errorMsg.append(sb);
                    verified = false;
                    
                }
            }
        } catch (Exception e) {
            logger.severe("Error occurred: " + e.toString());
            errorMsg.append("Error occurred: ").append(e.toString());
            verified = false;
        }
        
	    
        logger.fine("verifyFileName - returning value of: " + verified +  ": reason = " + errorMsg.toString());
	    return verified;
	}
	
	/**
	 * Sets additional information to be displayed if the user selects the wrong file type.
	 * 
	 * @param additionalFileExtInfo The message to be displayed if the user selects the wrong file type.
	 */
	public void setFileExtensionInfo(String additionalFileExtInfo) {
		this.additionalFileExtInfo = additionalFileExtInfo;
	}
	
	/**
	 * Helper function to format a list of extensions to be used in a dialog message.
	 * 
	 * @param extensions - List of extensions to be formatted.
	 * @return formatted string of the extensions.
	 */
	private StringBuilder formatExtensionList(List<String> extensions) {
	    
	    StringBuilder sb = new StringBuilder();
	    for(int i = 0; i < extensions.size(); i++){
            
            if(i == extensions.size() - 1){
                
                if(extensions.size() == 1){
                	sb.append("<b>");
                    sb.append(extensions.get(i));
                    sb.append("</b>");
                    
                } else {
                    sb.append("or ");
                    sb.append("<b>");
                    sb.append(extensions.get(i));
                    sb.append("</b>");
                }
                
            } else if(i == extensions.size() - 1 && extensions.size() == 2){
            	sb.append("<b>");
                sb.append(extensions.get(i));
                sb.append("</b>");
                sb.append(" ");
                
            } else {
            	sb.append("<b>");
                sb.append(extensions.get(i));
                sb.append("</b>");
                sb.append(", ");
            }
        }
	    
	    return sb;
	}
	
	@Override
	public void setAllowedFileExtensions(String[] extensions){
		
		StringBuilder acceptValue = new StringBuilder();
		
		for(int i = 0; i < extensions.length; i++){
			
			int lastDotIndex = extensions[i].lastIndexOf(".");
			
			String metaExtension;
			
			/*
			 * Most browsers only recognize the last period and onwards as an actual file extension, so we can't filter for extensions like 
			 * .course.xml or .dkf.xml during uploads. We can, howerver, use the .xml part to have a little bit of filtering, 
			 */
			
			if(lastDotIndex >= 0 && lastDotIndex < extensions[i].length()){
				metaExtension = extensions[i].substring(lastDotIndex, extensions[i].length());
				
			} else {
				metaExtension = extensions[i];
			}
			
			if(i == 0){
				acceptValue.append(metaExtension);
			
			} else {
				acceptValue.append(", ").append(metaExtension);
			}
		}
		
		fileUpload.getElement().setAttribute("accept", acceptValue.toString());
		
		presenter.setAllowedFileExtensions(extensions);
	}
	
	@Override
	public HasData<FileTreeModel> getFileNameDataDisplay() {
		return fileNameTable;
	}

	@Override
	public void setUpArrowCommand(ScheduledCommand command) {
		upArrow.setScheduledCommand(command);
	}

	@Override
	public HasEnabled getUpArrow() {
		return upArrow;
	}

	@Override
	public HasText getDirectoryName() {
		return directoryName;
	}

	@Override
	public HasValue<String> getFileNameInput() {
		return fileNameInput;
	}
	
	/**
	 * Sets the size of the file selection table
	 * 
	 * @param width the width of the file selection table
	 * @param height the height of the file selection table
	 */
	public void setFileTableSize(String width, String height){
		
		fileTableScrollPanel.setWidth(width);
		fileTableScrollPanel.setHeight(height);
		
		workspaceLoadPanel.setWidth(width);
		workspaceLoadPanel.setHeight(height);
	}
	
	/**
	 * Gets the element used to select a file to upload
	 * 
	 * @return the element used to select a file to upload
	 */
	public FileUpload getFileUpload(){
		return fileUpload;
	}
	
	/**
	 * Resets all fields.
	 */
	public void reset(){
		
		form.reset();
		presenter.refresh();
	}
	
	/**
	 * Resets the upload field and the form used to submit it.
	 */
	public void resetUpload(){
		
		form.reset();
	}
	
	@Override
	public void resetScroll(){
		fileTableScrollPanel.scrollToTop();
	}
	
	@Override
	public void addCellPreviewHandler(Handler<FileTreeModel> handler){
		
		fileNameTable.addCellPreviewHandler(handler);
	}
	
	@Override
	public boolean isUploadingFile(){
		return currentMode.equals(FileSelectionView.Mode.UPLOAD);
	}
	
	@Override
	public void setModeChangedCallback(ModeChangedCallback callback) {
		this.modeChangedCallback = callback;
	}
	
	@Override
	public void filterOutFiles(List<String> relativePathsOfFiles){
		
		if(presenter != null){
			presenter.filterOutFiles(relativePathsOfFiles);
		}
	}
	
	@Override
	public void setShowFoldersOnly(boolean showFoldersOnly){
		
		if(presenter != null){
			presenter.setShowFoldersOnly(showFoldersOnly);
		}
	}
	
	@Override
	public void filterOutExtensions(List<String> extensions){
		
		if(presenter != null){
			presenter.filterOutExtensions(extensions);
		}
	}
	
	@Override
	public void setFoldersSelectable(boolean foldersSelectable){
		
		if(presenter != null){
			presenter.setFoldersSelectable(foldersSelectable);
		}
	}
	
	@Override
	public boolean getFoldersSelectable(){
		
		if(presenter != null){
			return presenter.getFoldersSelectable();
			
		} else {
			return false;
		}
	}
		
    @Override
    public void copyChosenFile(FileTreeModel source, final FileSelectionCallback callback) {
        
        StringBuilder errorMessage = new StringBuilder("");
        logger.fine("copyChosenFile - called with callback object of: " + callback);
        
        String fileName = source.getRelativePathFromRoot(true);
        if (!verifyFileName(fileName, errorMessage)) {
            callback.onFailure(errorMessage.toString());
            return;
        }
        
        
        fileUploadCallback = callback;
        
        // Kick off the copy request on the server.
        if (copyFileRequest != null) {
            copyFileRequest.asyncCopy(source, new CopyFileCallback() {

                @Override
                public void onSuccess(FileTreeModel file) {
                    
                    logger.fine("copyChosenFile - success received");
                    callback.onServerFileSelected(file.getRelativePathFromRoot());
                    
                }

                @Override
                public void onFailure(String reason) {
                    
                    logger.fine("copyChosenFile - onFailure():" + reason);
                    callback.onFailure(reason);
                    
                }

                @Override
                public void onFailure(Throwable thrown) {
                    logger.fine("copyChosenFile - onFailure() throwable exception occurred:" + thrown.getMessage());
                    callback.onFailure(thrown.getMessage());
                    
                }
                
            });
        }
    }

    /**
     * Sets the CopyFileRequest logic for the dialog.
     * 
     * @param copyFileReq - The CopyFileRequest containing the logic to perform the copy file operation.  Can be null to clear the copy file operation.  
     */
    public void setCopyFileRequest(CopyFileRequest copyFileReq) {
        
        copyFileRequest = copyFileReq;
        
        // We need to also signal to the presenter that it has a copy file request.
        presenter.setCopyFileRequest(copyFileReq);
    }
    
    /**
     * Gets the CopyFileRequest logic for the dialog
     * 
     * @return The CopyFileRequest containing the logic to perform the copy file operation.  Can be null if the copy file operation has been cleared.  
     */
    public CopyFileRequest getCopyFileRequest(){
    	return copyFileRequest;
    }

    /**
     * Enables or disables uploading by adding or removing the "Upload" tab if this instance is capable of uploading.
     * 
	 * @param enabled whether or not uploading should be enabled
     * @return true, if any tabs remain. False, otherwise.
     */
	public boolean setUploadEnabledIfPossible(boolean enabled) {
		
		if(enabled){
			
			if(form.getAction() != null && !form.getAction().isEmpty()){
				
				if(tabPanel.getWidgetIndex(uploadTab) == -1){
					tabPanel.insert(uploadTab, "Upload", 0);
				}
				
				return true;
				
			} else {
				
				if(tabPanel.getWidgetIndex(workspaceDeck) != -1){
					
					return true;
					
				} else {
					
					return false;
				}
			}		
			
		} else {
			
			if(tabPanel.getWidgetIndex(uploadTab) != -1){
				tabPanel.remove(uploadTab);
			}
			
			if(tabPanel.getWidgetIndex(workspaceDeck) != -1){
				
				tabPanel.selectTab(tabPanel.getWidgetIndex(workspaceDeck), true);
				
				currentMode = Mode.MY_WORKSPACE;
				
				return true;
			}
			
			return false;
		}
	}   
	
	@Override
	public void setWorkspaceLoadingIconVisible(boolean visible){
		
		if(visible){
			workspaceDeck.showWidget(workspaceDeck.getWidgetIndex(workspaceLoadPanel));
			
		} else {
			workspaceDeck.showWidget(workspaceDeck.getWidgetIndex(loadedContentPanel));
		}
	}

	@Override
	public Focusable getFileNameFocusInput() {
		return fileNameInput;
	}
	
	@Override
	public HasKeyUpHandlers getFileNameKeyUpInput() {
		return fileNameInput;
	}
}
