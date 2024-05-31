/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.file;

import mil.arl.gift.common.io.FileTreeModel;

import com.google.gwt.cell.client.ImageCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;

// TODO: Auto-generated Javadoc
/**
 * The Class FileBrowserDialogContent.
 *
 * @author nroberts
 */
public class FileBrowserDialogContent extends Composite{

	/** The ui binder. */
	private static FileSelectionViewImplUiBinder uiBinder = GWT
			.create(FileSelectionViewImplUiBinder.class);
	
	/** The directory icon url. */
	private static String DIRECTORY_ICON_URL = "images/transitions/aar-1.png";
	
	/** The file icon url. */
	private static String FILE_ICON_URL = "images/transitions/survey.png";

	/**
	 * The Interface FileSelectionViewImplUiBinder.
	 */
	interface FileSelectionViewImplUiBinder extends
			UiBinder<Widget, FileBrowserDialogContent> {
	}
	
	/** The up arrow. */
	@UiField
	protected MenuItem upArrow;
	
	/** The up arrow. */
	@UiField
	protected MenuItem directoryName;
	
	/** The file name data grid. */
	@UiField
	protected DataGrid<FileTreeModel> fileNameDataGrid;
	
	/** The file name input. */
	@UiField
	protected TextBox fileNameInput;
	
	/** The select button. */
	@UiField
	protected Button selectButton;
	
	/** The cancel button. */
	@UiField
	protected Button cancelButton;
	
	/**
	 * Instantiates a new file selection view impl.
	 */
	FileBrowserDialogContent() {
		initWidget(uiBinder.createAndBindUi(this));
		
		init();
	}
	
	/**
	 * Initialize this widget's components.
	 */
	private void init(){
		
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
		fileNameDataGrid.addColumn(fileIconColumn);
		fileNameDataGrid.setColumnWidth(fileIconColumn, "20%");
		
		Column<FileTreeModel, String> fileNameColumn = new Column<FileTreeModel, String>(new TextCell()){

			@Override
			public String getValue(FileTreeModel object) {
				return object.getFileOrDirectoryName();
			}
			
		};
		
		fileNameDataGrid.addColumn(fileNameColumn);
		fileNameDataGrid.setColumnWidth(fileNameColumn, "80%");
	}

	/**
	 * Gets the file name input.
	 *
	 * @return the file name input
	 */
	HasValue<String> getFileNameInput(){
		return fileNameInput;
	}

	/**
	 * Sets the up arrow command.
	 *
	 * @param command the new up arrow command
	 */
	void setUpArrowCommand(ScheduledCommand command){
		upArrow.setScheduledCommand(command);
	}

	/**
	 * Gets the up arrow.
	 *
	 * @return the up arrow
	 */
	HasEnabled getUpArrow(){
		return upArrow;
	}

	/**
	 * Gets the file name data display.
	 *
	 * @return the file name data display
	 */
	public HasData<FileTreeModel> getFileNameDataDisplay(){
		return fileNameDataGrid;
	}
	
	/**
	 * Gets the confirm button.
	 *
	 * @return the confirm button
	 */
	public HasClickHandlers getConfirmButton(){
		return selectButton;
	}

	/**
	 * Gets the confirm button has enabled.
	 *
	 * @return the confirm button has enabled
	 */
	public HasEnabled getConfirmButtonHasEnabled(){
		return selectButton;
	}

	/**
	 * Gets the cancel button.
	 *
	 * @return the cancel button
	 */
	public HasClickHandlers getCancelButton(){
		return cancelButton;
	}

	/**
	 * Gets the directory name.
	 *
	 * @return the directory name
	 */
	public HasText getDirectoryName(){
		return directoryName;
	}
}
