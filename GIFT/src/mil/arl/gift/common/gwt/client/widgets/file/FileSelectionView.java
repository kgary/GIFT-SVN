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
	 * Interface for handling file uploads
	 * 
	 * @author nroberts
	 */
	public static interface FileSelectionCallback{
		
		public void onServerFileSelected(String filename);
		
		/**
		 * Notification that the file upload succeeded.
		 * 
		 * @param filename the name of the file that was uploaded.  Can include a path from the servlet
		 * path folder.
		 * @param servletPath the location of the folder on the proxy server where the file resides
		 * as a descendant file.  Can be null.
		 */
		public void onClientFileUploaded(String filename, String servletPath);
		
		/**
		 * Notification that the file upload failed on the server.
		 * 
		 * @param reason contains server created details about the failure.
		 */
		public void onFailure(String reason);
	}

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
	 * Copies an existing file in the workspace (user's private or public workspace) into a root folder in the user's workspace.
	 * @param source - The file tree model containing the source file.
	 * @param callback - THe callback used to handle the copy operation events such as success/failure.
	 */
	void copyChosenFile(FileTreeModel source, FileSelectionCallback callback);
	
	/**
	 * Sets whether or not the loading icon for the workspace tab should be visible
	 * 
	 * @param visible whether or not the loading icon for the workspace tab should be visible
	 */
	void setWorkspaceLoadingIconVisible(boolean visible);

	HasKeyUpHandlers getFileNameKeyUpInput();
	
	Focusable getFileNameFocusInput();
}
