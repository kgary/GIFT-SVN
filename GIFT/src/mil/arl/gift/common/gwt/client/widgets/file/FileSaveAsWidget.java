/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import java.util.List;

import mil.arl.gift.common.gwt.client.widgets.DisplaysMessage;
import mil.arl.gift.common.io.FileTreeModel;

import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.HasData;

/**
 * A widget that allows the user to specify a file to save to the server.
 * 
 * This widget will not enact file selection logic until submitFileChoice(FileSaveAsCallback) is called. 
 * This allows such logic to be delayed until another encompassing widget determines that such operations should proceed, giving us the 
 * option of canceling a file selection if a validation check fails. The callback provided will be given the appropriate 
 * file name selected once the corresponding operation has completed.
 * 
 * By default, no height is initially declared for the list of server files, meaning the list will be dynamically sized depending on the number 
 * of files in the currently selected directory. In order to set this list to a specific size and enable scrolling, a call must be made to 
 * setFileTableHeight(String height). 
 * 
 * @author nroberts
 */
public class FileSaveAsWidget extends Composite implements FileSaveAsView {

	private static FileSaveAsWidgetUiBinder uiBinder = GWT
			.create(FileSaveAsWidgetUiBinder.class);
	
	/** The directory icon url. */
	private static String DIRECTORY_ICON_URL = "images/folder.png";
	
	/** The file icon url. */
	private static String FILE_ICON_URL = "images/file.png";
	
	/** The loading icon url. */
	private static String LOADING_ICON_URL = "images/loading.gif";

	interface FileSaveAsWidgetUiBinder extends
			UiBinder<Widget, FileSaveAsWidget> {
	}
	
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
	protected Widget myWorkspacesTab;
	
	@UiField
	protected ScrollPanel fileTableScrollPanel;
	
	@UiField
	protected Button newFolderButton;
	
	@UiField
	protected FlowPanel toolbarPanel;
	
	@UiField
	protected Label fileNameLabel;
	
	@UiField
	protected MenuBar navigationBar;
	
	@UiField
	protected FlowPanel fileTableContainer;
	
	@UiField
	protected Image workspaceLoadImage;
	
	@UiField
	protected DeckPanel workspaceDeck;
	
	@UiField
	protected Widget workspaceLoadPanel;
	
	@UiField
	protected Widget namePanel;
	
	@UiField
	protected Widget loadedContentPanel;
	
	/**
	 * The presenter associated with this file selector. Ordinarily, we wouldn't give the view direct access to its presenter, but in this case, 
	 * we plan to use this widget in a number of places, so we want to keep the process of setting up the widget as simple as possible. Because 
	 * of this, most of the logic used to set up the presenter is handled in the view's constructor, making handling file uploads as simple as 
	 * calling the constructor and then calling submitFileChoice(FileSaveAsCallback)
	 */
	private FileSaveAsPresenter presenter;
	
	public ModeChangedCallback modeChangedCallback;
	
	private FileSaveAsView.Mode currentMode = FileSaveAsView.Mode.MY_WORKSPACE;
	
	private CanCreateFolder folderCreator = null;
	
	private DialogBox createFolderDialog = new DialogBox();
	
	private Button createFolderButton = new Button("Create Folder");
	
	private Button cancelButton = new Button("Cancel");

	private DisplaysMessage messageDisplay;
	
	boolean isFileListVisible = true;
	
	/**
	 * Creates a new file save-as widget with no capability of showing a list of files. This constructor mainly exists to 
	 * provide common functionality to other constructors and should not be used on its own.
	 */
	private FileSaveAsWidget(){
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
		
		tabPanel.addSelectionHandler(new SelectionHandler<Integer>() {
			
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				
				if(event.getSelectedItem() == tabPanel.getWidgetIndex(myWorkspacesTab)){
					
					currentMode = FileSaveAsView.Mode.MY_WORKSPACE;
					
					if(modeChangedCallback != null){
						modeChangedCallback.onModeChanged(currentMode);
					}
					
					namePanel.setVisible(true);
					
				} else {
					namePanel.setVisible(false);
				}
			} 
		});
		
		newFolderButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {				
				createFolderDialog.center();
				createFolderButton.setEnabled(true);
				cancelButton.setEnabled(true);
				
			}
		});
		
		//initialize "Create New Folder" dialog
		createFolderDialog.setText("Create New Folder");
		createFolderDialog.setGlassEnabled(true);
		
		ClickHandler cancelHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {				
				createFolderDialog.hide();
			}
		};
		cancelButton.addClickHandler(cancelHandler);
		
		VerticalPanel vPanel = new VerticalPanel();
		HorizontalPanel hPanel = new HorizontalPanel();
		
		HTML html = new HTML("Please enter the name of the folder you want to create:");
		html.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		vPanel.getElement().getStyle().setProperty("margin", "20px 20px 3px");
		hPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		createFolderButton.setWidth("100px");
		cancelButton.setWidth("100px");
		createFolderButton.getElement().getStyle().setProperty("marginRight", "20px");
				
		final TextBox folderNameTextBox = new TextBox();
		folderNameTextBox.setWidth("100%");
		folderNameTextBox.getElement().getStyle().setProperty("margin", "3px 0px 7px");
		
		ClickHandler okayHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				createFolderButton.setEnabled(false);
				
				if(folderCreator != null){
					
					if(folderNameTextBox.getValue() != null && !folderNameTextBox.getValue().isEmpty()){
						cancelButton.setEnabled(false);
						presenter.createFolder(folderNameTextBox.getValue(), folderCreator);
						
					} else {
						createFolderButton.setEnabled(true);
						messageDisplay.showErrorMessage("Missing name", "Please specify a folder name.", null);
					}
				
				} else {
					messageDisplay.showErrorMessage("Failed to create folder", "Could not create folder. Creating folders is not enabled for this interface.", null);
				}
			}
		};		
		
		folderNameTextBox.addKeyUpHandler(new KeyUpHandler() {

			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					createFolderButton.click();
				}
			}			
		});
		
		createFolderButton.addClickHandler(okayHandler);
		
		vPanel.setHeight("100");
		vPanel.setWidth("300");
		vPanel.setSpacing(10);
		
		vPanel.add(html);
		vPanel.add(folderNameTextBox);
		
		hPanel.setSpacing(10);
		hPanel.add(createFolderButton);
		hPanel.add(cancelButton);
		vPanel.add(hPanel);
		
		createFolderDialog.setWidget(vPanel);
		
		workspaceDeck.showWidget(workspaceDeck.getWidgetIndex(workspaceLoadPanel));
	}
	
	/**
	 * Creates a file save-as widget that only allows users to save files to a root directory on the server. 
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param rootGetter an object that can get the root directory
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
	public FileSaveAsWidget(CanGetRootDirectory rootGetter, DisplaysMessage messageDisplay){
		
		this();
		
		presenter = new FileSaveAsPresenter(this, rootGetter, messageDisplay);
		
		this.messageDisplay = messageDisplay;
		
		tabPanel.selectTab(tabPanel.getWidgetIndex(myWorkspacesTab), true);
		
		currentMode = Mode.MY_WORKSPACE;
		
		toolbarPanel.setVisible(false);
	}
	
	/**
	 * Creates a file save-as widget that allows users to save files to a root directory on the server and create new directories under that
	 * root directory. 
	 * 
	 * The root directory will be retrieved using the instance of {@link CanGetRootDirectory} provided. Errors and other information will be 
	 * reported to the given display object.
	 * 
	 * @param rootGetter an object that can get the root directory
	 * @param folderCreator an object that can create folders
	 * @param messageDisplay messageDisplay an object that can display messages to the user
	 */
	public FileSaveAsWidget(CanGetRootDirectory rootGetter, CanCreateFolder folderCreator, DisplaysMessage messageDisplay){
		
		this();
		
		presenter = new FileSaveAsPresenter(this, rootGetter, messageDisplay);
		
		tabPanel.selectTab(tabPanel.getWidgetIndex(myWorkspacesTab), true);
		
		currentMode = Mode.MY_WORKSPACE;
		
		this.folderCreator = folderCreator;
		
		toolbarPanel.setVisible(true);
	}
	
	@Override
	public void submitFileChoice(final FileSaveAsCallback callback){		
		presenter.submitFileChoice(callback, currentMode);
	}
	
	@Override
	public void setAllowedFileExtensions(String[] extensions){
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
	
	@Override
	public HasKeyUpHandlers getFileNameKeyUpInput() {
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
	 * Resets all fields.
	 */
	public void reset(){
		
		presenter.refresh();
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
	public void clearFileName() {
		presenter.clear();
	}

	@Override
	public void setCreateFolderDialogVisible(boolean visible) {
		
		if(visible){
			createFolderDialog.center();
			createFolderButton.setEnabled(true);
			cancelButton.setEnabled(true);
		
		} else {
			createFolderDialog.hide();
		}
	}

	@Override
	public void setAllowNavigationToSubfolders(boolean value) {
		presenter.setAllowNavigationToSubfolders(value);
	}

	@Override
	public void setAllowFolderCreation(boolean value) {
		toolbarPanel.setVisible(value);
	}
	
	public boolean isFileOrFolderInCurrentDirectory(String fileOrFolderName) {
		return presenter.isFileOrFolderInCurrentDirectory(fileOrFolderName);
	}
	
    /**
     * Finds the child model (file or folder) that has the given name.
     * 
     * @param fileOrFolderName The name of the file or folder to retrieve.
     * @return The file tree model if found, otherwise returns null.
     */
	public FileTreeModel getFileModelInCurrentDirectory(String fileOrFolderName) {
	    return presenter.getFileModelInCurrentDirectory(fileOrFolderName);
	}
	/**
	 * Sets the text on the "File Name:" label
	 * 
	 * @param text the new text to use
	 */
	public void setFileNameLabelText(String text){
		fileNameLabel.setText(text);
	}

	/**
	 * Sets whether or not the list of files and its navigation bar should be visible
	 * 
	 * @param visible whether or not the list of files and its navigation bar should be visible
	 */
	public void setFileListVisible(boolean visible) {
		tabPanel.setVisible(visible);
		
		isFileListVisible = visible;
	}
	
	@Override
	public void setWorkspaceLoadingIconVisible(boolean visible){
		
		if(visible && isFileListVisible){
			workspaceDeck.showWidget(workspaceDeck.getWidgetIndex(workspaceLoadPanel));
			
		} else {
			workspaceDeck.showWidget(workspaceDeck.getWidgetIndex(loadedContentPanel));
		}
	}

	@Override
	public Focusable getFileNameFocusInput() {
		return fileNameInput;
	}

	/**
     * Sets the starting directory for the dialog.  This directory path must be a subfolder of the
     * {@link FileSaveAsPresenter#startDirectoryModel startDirectoryModel} of the widget.  
     * 
     * @param path - The starting directory path (relative to the root).  This should not be null.
     */
    public void setStartingDirectory(String path) {
        presenter.setStartingDirectory(path);
        
    }
}
