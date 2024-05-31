/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.guidance;

import org.gwtbootstrap3.extras.summernote.client.event.SummernoteBlurHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * The Interface AarView.
 */
public interface GuidanceView extends IsWidget, IsSerializable{
	
	/**
	 * The Interface Presenter.
	 */
	public interface Presenter{

		/**
		 * Start.
		 */
		void start();
		
		/**
		 * Stop.
		 */
		void stop();
	}

	
	/**
	 * Gets the display time input.
	 *
	 * @return the display time input
	 */
	HasValue<String> getDisplayTimeInput();

	/**
	 * Gets the full screen input.
	 *
	 * @return the full screen input
	 */
	HasValue<Boolean> getFullScreenInput();
	
	/**
     * Gets the disabled input.
     *
     * @return the disabled input
     */
    HasValue<Boolean> getDisabledInput();

	/**
	 * Gets the use file input.
	 *
	 * @return the use file input
	 */
	HasClickHandlers getUseFileInput();

	/**
	 * Gets the use url input.
	 *
	 * @return the use url input
	 */
	HasClickHandlers getUseUrlInput();

	/**
	 * Gets the use message content input.
	 *
	 * @return the use message content input
	 */
	HasClickHandlers getUseMessageContentInput();

	/**
	 * Shows the guidance message editor.
	 */
	void showGuidanceMessageEditor();

	/**
	 * Shows the guidance url editor.
	 */
	void showGuidanceUrlEditor();

	/**
	 * Shows the guidance file editor.
	 */
	void showGuidanceFileEditor();
	
	/**
	 * Gets the remove guidance file component.
	 * 
	 * @return the remove guidance file component
	 */
    HasClickHandlers getRemoveFileInput();
	
    /**
     * Updates the guidance file attribute UI components
     * with the provided guidance file value.
     * 
     * @param value the selected guidance file name or null if no
     * file is selected
     */
	void setGuidanceFileAttributes(String value);

	/**
	 * Adds a blur handler to the message content input.
	 * 
	 * @param handler the handler to add.
	 */
	void addMessageContentInputBlurHandler(SummernoteBlurHandler handler);

	/**
	 * Gets the url address input.
	 *
	 * @return the url address input
	 */
	HasValue<String> getUrlAddressInput();

	/**
	 * Gets the url message input.
	 *
	 * @return the url message input
	 */
	Summernote getUrlMessageInput();
	
	/**
	 * Gets the url preview button.
	 * 
	 * @return the url preview button.
	 */
	HasClickHandlers getUrlPreviewButton();

	/**
	 * Gets the file browse input.
	 *
	 * @return the file browse input
	 */
	HasClickHandlers getFileBrowseInput();

	/**
	 * Gets the file selection dialog.
	 *
	 * @return the file selection dialog
	 */
	HasValue<String> getFileSelectionDialog();

	/**
	 * Gets the url message input.
	 *
	 * @return the url message input
	 */
	Summernote getFileMessageInput();
	
	/**
     * Gets the file preview button.
     * 
     * @return the file preview button.
     */
    HasClickHandlers getFilePreviewButton();

	/**
	 * Gets the file name label
	 * 
	 * @return the file name label
	 */
	HasText getFileNameLabel();
	
	/**
     * Sets whether or not the disabled option is visible
     * 
     * @param visible whether or not the disabled option in the options panel is visible
     */
    public void hideDisabledOption(boolean hide);
    
    /**
     * Sets whether or not the display time panel is visible
     * 
     * @param hide whether or not the display time panel in the options panel is visible
     */
    public void hideDisplayTimePanel(boolean hide);
    
    /**
     * Sets whether or not the display full screen option is visible
     * 
     * @param hide whether or not the display full screen option in the options panel is visible
     */
    public void hideDisplayFullScreenOption(boolean hide);
	
	/**
	 * Sets whether or not the info message editor is visible
	 * 
	 * @param visible whether or not the info message editor is visible
	 */
	public void hideInfoMessage(boolean hide);
	
	/**
	 * Gets the tooltip for the display time field
	 * 
	 * @return the tooltip for the display time field
	 */
	HasHTML getDisplayTimeTooltip();

	/**
	 * Sets the message content.
	 * 
	 * @param html The html to display in the text editor
	 */
	void setMessageContent(String html);

	/**
	 * Sets the message content.
	 * 
	 * @return the message content
	 */
	String getMessageContent();

	/**
	 * Shows the panel used to decide the type of information object
	 */
	void showChoicePanel();
	
}
