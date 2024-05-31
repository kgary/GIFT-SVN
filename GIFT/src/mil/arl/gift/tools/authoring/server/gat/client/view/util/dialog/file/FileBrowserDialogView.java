/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.file;

import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.Showable;

import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HasEnabled;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.HasData;

/**
 * The Interface FileSelectionView.
 */
public interface FileBrowserDialogView extends IsWidget, IsSerializable, HasValue<String>, Showable {

	/**
	 * Gets the confirm button.
	 *
	 * @return the confirm button
	 */
	HasClickHandlers getConfirmButton();

	/**
	 * Gets the confirm button's HasEnabled interface.
	 *
	 * @return the confirm button's HasEnabled interface.
	 */
	HasEnabled getConfirmButtonHasEnabled();

	/**
	 * Gets the cancel button.
	 *
	 * @return the cancel button
	 */
	HasClickHandlers getCancelButton();

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

}
