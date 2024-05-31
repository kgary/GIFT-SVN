/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.file;

import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.FileSelectionCallback;
import mil.arl.gift.common.io.FileTreeModel;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.HasData;

/**
 * The view interface for a file selector.
 */
public interface FileSelectionView extends IsWidget, IsSerializable{
	
	public static enum Mode{
		UPLOAD,
		MY_WORKSPACE;
	}

	/**
	 * Gets the file name data display.
	 *
	 * @return the file name data display
	 */
	HasData<FileTreeModel> getFileNameDataDisplay();

	/**
	 * Sets the up arrow command.
	 *
	 * @param command the new up arrow command
	 */
	void setUpArrowCommand(ScheduledCommand command);

	/**
	 * Gets the up arrow.
	 *
	 * @return the up arrow
	 */
	HasEnabled getUpArrow();

	/**
	 * Gets the directory name.
	 *
	 * @return the directory name
	 */
	HasText getDirectoryName();

	/**
	 * Gets the file name input.
	 *
	 * @return the file name input
	 */
	HasValue<String> getFileNameInput();

	/**
	 * Submits the user's file choice. A similar method exists in FileSelectionPresenter. The reason this method also exists for the view is 
	 * because a HTML form MUST be used to handle a file upload, and this can only be done from the view.
	 * 
	 * @param callback The callback that should handle the result
	 */
	void submitFileChoice(FileSelectionCallback callback);

	/**
	 * Sets the file extensions to allow for file selection
	 * 
	 * @param extensions the file extensions
	 */
	void setAllowedFileExtensions(String[] extensions);

	/**
	 * Uploads the selected file using a form element
	 * 
	 * @param callback The callback that should handle the result
	 */
	void uploadChosenFile(FileSelectionCallback callback);

	/**
	 * Resets the scrollbars in the file selection table
	 */
	void resetScroll();

	/**
	 * Adds a cell preview handler to the file selection table
	 * 
	 * @param handler the handler to add
	 */
	void addCellPreviewHandler(Handler<FileTreeModel> handler);

	/**
	 * Gets whether or not this widget is currently in upload mode
	 * 
	 * @return whether or not this widget is currently in upload mode
	 */
	boolean isUploadingFile();
	
	/**
	 * Sets the callback to be invoked when this file selection view changes modes
	 * 
	 * @param callback the callback to be invoked when this file selection view changes modes
	 */
	void setModeChangedCallback(ModeChangedCallback callback);
	
	/**
	 * A callback to be invoked when a file selection view changes modes
	 * 
	 * @author nroberts
	 */
	public interface ModeChangedCallback{
		
		public void onModeChanged(Mode mode);
	}
}
