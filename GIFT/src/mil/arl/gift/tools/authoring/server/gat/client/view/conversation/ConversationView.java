/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation;

import java.io.Serializable;
import java.util.HashMap;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.RichTextArea;

import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.ConversationWidget.ConversationUpdateCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.OkayCancelCallback;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;

/**
 * The interface to the conversation editor view.
 */
public interface ConversationView extends IsWidget, Serializable {			
	
	/**
	 * The Interface Presenter.
	 */
	public interface Presenter {

		/**
		 * Start.
		 *
		 * @param containerWidget the container widget
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 */
		void start(AcceptsOneWidget containerWidget, HashMap<String, String> startParams);		
		
		/**
		 * Confirm stop.
		 *
		 * @return the string
		 */
		String confirmStop();
		
		/**
		 * Stop.
		 */
		void stop();

		/** 
		 * Sets whether or not this editor should be Read-Only
		 * 
		 * @param readOnly whether or not this editor should be Read-Only
		 */
		void setReadOnly(boolean readOnly);
	}    
	
    /**
	 * Show confirm dialog.
	 *
	 * @param msgHtml the msg html
	 * @param confirmMsg the confirm button text 
	 * @param callback the callback
	 */
	void showConfirmDialog(String msgHtml, String confirmMsg, OkayCancelCallback callback);
	
	/**
	 * Gets the conversation tree JSON string.
	 * 
	 * @return the conversation tree JSON string.
	 */
	String getTreeJSONStr();
	
	/**
	 * Loads a conversation tree from a JSON string.
	 * 
	 * @param the conversation tree JSON string.
	 */
	void loadTree(String treeJSONStr);
		
	/**
	 * Creates a new tree and refreshes the view.
	 */
	void newTree();
	
	/**
	 * Gets the id of the first node in the conversation tree.
	 * 
	 * @return The id of the first node in the conversation tree.
	 */
	public int getStartNodeId();
	
	/** 
	 * Sets whether or not this editor should be Read-Only
	 * 
	 * @param readOnly whether or not this editor should be Read-Only
	 */
	void setReadOnly(boolean readOnly);
	
	/**
	 * Gets the name input.
	 *
	 * @return the name input
	 */
	public HasValue<String> getNameInput();
	
	/**
	 * Gets the value change handler of the name input.
	 * 
	 * @return value change handler of the name input
	 */
	public HasValueChangeHandlers<String> getNameInputValueChange();

	/**
	 * Gets the author's description input.
	 *
	 * @return the author's description input
	 */
	public RichTextArea getAuthorsDescriptionInput();
	
	/**
	 * Gets the learner's description input.
	 *
	 * @return the learner's description input
	 */
	public RichTextArea getLearnersDescriptionInput();
	
	/**
	 * Sets the callback to execute when the user selects a choice node in the preview window.
	 * 
	 * @param callback The callback to execute.
	 */
	public void setPreviewSubmitTextCallback(ConversationUpdateCallback callback);
	
	/**
	 * Queues a conversation update to present to the user in the preview window.
	 * 
	 * @param result The update result from the server for this conversation.
	 */
	public void updateConversation(UpdateConversationResult result);

	/**
	 * Gets the preview button.
	 * 
	 * @return The button used to display the conversation tree preview.
	 */
	public HasClickHandlers getPreviewButton();
	
	/**
	 * Gets the close button.
	 * 
	 * @return The close button for the preview panel.
	 */
	public HasClickHandlers getClosePreviewButton();
	
	/**
	 * Sets the chat id of the conversation in the preview window.
	 * 
	 * @param chatId The chat id.
	 */
	public void setPreviewChatId(int id);
	
	/**
	 * Gets the chat id of the conversation in the preview window.
	 * 
	 * @return The chat id.
	 */
	public int getPreviewChatId();

	/**
	 * Closes the conversation preview window.
	 */
	void closePreview();

	/**
     * Sets whether or not the conversation should be shown in full screen when previewed
     * 
     * @param fullScreen whether or not to preview in full screen
     */
    void setPreviewFullScreen(boolean fullScreen);
    
    /**
      * Set whether to allow question answer node concept assessments to be authored
      * @param allow true if assessment authoring should be allowed.  A reason for not allowing
      * might be that the tree is presented during remediation or to deliver content.
      */
    void setAllowConceptAssessments(boolean allow);
}
