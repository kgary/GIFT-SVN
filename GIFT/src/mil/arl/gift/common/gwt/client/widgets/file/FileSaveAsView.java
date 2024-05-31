/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import java.util.List;

import mil.arl.gift.common.io.FileTreeModel;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
import com.google.gwt.view.client.HasData;

/**
 * The view interface for a file save-as widget.
 */
public interface FileSaveAsView extends IsWidget, IsSerializable{
	
	public static enum Mode{
		MY_WORKSPACE;
	}
	
	/**
	 * Sets whether or not the user can create a new folder.
	 * 
	 * @param value True if the user can create a new folder, false otherwise.
	 */
	public void setAllowFolderCreation(boolean value);
	
	/**
	 * Sets whether or not the user can navigate into subfolders.
	 * 
	 * @param value True if the user can navigate into subfolders, false
	 * otherwise.
	 */
	public void setAllowNavigationToSubfolders(boolean value);

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
	 * Interface for handling file uploads
	 * 
	 * @author nroberts
	 */
	public static interface FileSaveAsCallback{
		
		public void onSuccess(String filename);
		
		public void onFailure(String reason);
	}

	/**
	 * Submits the user's file choice. A similar method exists in FileSaveAsPresenter. The reason this method also exists for the view is 
	 * because a HTML form MUST be used to handle a file upload, and this can only be done from the view.
	 * 
	 * @param callback The callback that should handle the result
	 */
	void submitFileChoice(FileSaveAsCallback callback);

	/**
	 * Sets the file extensions to allow for file selection
	 * 
	 * @param extensions the file extensions
	 */
	void setAllowedFileExtensions(String[] extensions);

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

	/**
	 * Filters out files from the MyWorkspaces tab that match any of the given relative file paths. Any files with the given paths 
	 * will be hidden from the user.
	 * 
	 * @param relativePathsOfFiles the file extensions to filter out
	 */
	void filterOutFiles(List<String> relativePathsOfFiles);

	/**
	 * Sets whether or not only folders should be shown in the MyWorkspaces tab
	 * 
	 * @param showFoldersOnly whether or not only folders should be shown in the MyWorkspaces tab
	 */
	void setShowFoldersOnly(boolean showFoldersOnly);

	/**
	 * Filters out the given file extensions from the MyWorkspaces tab. Any files with the given extensions will be hidden from the user.
	 * 
	 * @param extensions the file extensions to filter out
	 */
	void filterOutExtensions(List<String> extensions);

	/**
	 * Sets whether or not folders should be selectable in the MyWorkspaces tab
	 * 
	 * @param foldersSelectable whether or not folders should be selectable in the MyWorkspaces tab
	 */
	void setFoldersSelectable(boolean foldersSelectable);

	/**
	 * Gets whether or not folders are selectable in the MyWorkspaces tab
	 * 
	 * @return whether or not folders are selectable in the MyWorkspaces tab
	 */
	boolean getFoldersSelectable();

	/**
	 * Clears the file name and refreshes the file list
	 */
	void clearFileName();
	
	/**
	 * Sets whether or not the "Create a Folder" dialog is visible
	 * 
	 * @param visible whether or not the "Create a Folder" dialog is visible
	 */
	void setCreateFolderDialogVisible(boolean visible);

	/**
	 * Sets whether or not the loading icon for the workspace tab should be visible
	 * 
	 * @param visible whether or not the loading icon for the workspace tab should be visible
	 */
	void setWorkspaceLoadingIconVisible(boolean visible);

	/**
	 * @return the textbox on the save as widget
	 */
	HasKeyUpHandlers getFileNameKeyUpInput();
	
	Focusable getFileNameFocusInput();
}
