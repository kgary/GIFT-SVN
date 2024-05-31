/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;

import generated.conversation.Conversation;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview.ConversationWidget.ConversationUpdateCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.AvatarWidget;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.widgets.AvatarWidget.AvatarIdleCallback;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversationResult;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.ConversationHelper;

/**
 * A popup panel that presents a conversation preview
 *
 * @author bzahid
 */
public class PreviewPanel extends PopupPanel {

	interface PreviewPanelUiBinder extends UiBinder<Widget, PreviewPanel> {
	}

	private static PreviewPanelUiBinder uiBinder = GWT.create(PreviewPanelUiBinder.class);
	
	/** The logger for the class */
    private static Logger logger = Logger.getLogger(PreviewPanel.class.getName());

	@UiField
	protected Button closeButton;

	@UiField
	protected AvatarWidget avatarWidget;

	@UiField
	protected FlowPanel avatarPanel;

	@UiField
	protected SplitLayoutPanel chatPanel;
	
	@UiField
	protected FlowPanel bodyPanel;

	private ConversationWidget conversationWidget = new ConversationWidget();

	/** List of chat updates to be presented to the user. */
	private List<UpdateConversationResult> updateQueue = new ArrayList<UpdateConversationResult>();

	/**
	 * Creates a popup panel containing the conversation preview.
	 */
	public PreviewPanel() {
		setWidget(uiBinder.createAndBindUi(this));
		
		logger.info("Creating conversation preview panel");

		conversationWidget.getElement().getStyle().setProperty("margin", "auto");
		conversationWidget.setWidth("80%");
		avatarPanel.add(conversationWidget);

		addStyleName("previewConversationDialog");

		setGlassEnabled(true);
		setAnimationEnabled(true);
		getElement().getStyle().setProperty("border", "none");
		getElement().getStyle().setProperty("borderRadius", "8px");
		getElement().getStyle().setProperty("boxShadow", "rgba(0, 0, 0, 0.43) 4px 4px 13px");

		avatarWidget.setAvatarIdleCallback(new AvatarIdleCallback(){

			@Override
			public void onIdle() {
				synchronized(updateQueue) {
					if(!updateQueue.isEmpty()) {

						final UpdateConversationResult result = updateQueue.remove(0);

						conversationWidget.updateChatText("GIFT", result.getTutorText().get(0), result.getChoices(), result.endConversation());

						if(!result.getTutorText().get(0).isEmpty()) {
							AvatarWidget.sayFeedback(AvatarWidget.stripFeedback(result.getTutorText().get(0)));
						}
					}
				}
			}

		});

		closeButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				PreviewPanel.this.hide();
				updateQueue.clear();
				conversationWidget.clearChat();
				AvatarWidget.busyAvatarNotification();
			}
		});

		getElement().getStyle().setPosition(Position.ABSOLUTE);
		getElement().getStyle().setTop(20, Unit.PX);
		getElement().getStyle().setLeft(20, Unit.PX);
		getElement().getStyle().setRight(20, Unit.PX);
		getElement().getStyle().setBottom(20, Unit.PX);
	}
	
	   /**
     * Set the background to the image provided.
     * 
     * @param backgroundUrl shouldn't be null or empty
     */
    public void setBackground(String backgroundUrl){
        bodyPanel.getElement().getStyle().setBackgroundImage("url('"+backgroundUrl+"')");
    }

    /**
     * Shows the continue button without showing the other content in the footer
     * This method should be used in the case where the conversation ended
     * prematurely (e.g. an error occurred).
     */
    public void showContinueButton() {
        conversationWidget.showContinueButton();
    }

	/**
     * Set the command to execute when the conversation is complete.
     *
     * @param handler The {@link ClickHandler} to invoke when the continue
     *        button is clicked on the {@link ConversationWidget}. Can't be
     *        null.
     */
    public void addContinueClickHandler(ClickHandler handler) {
        conversationWidget.addContinueClickHandler(handler);
	}

	/**
	 * Sets the conversation details to display in the information dialog.
	 *
	 * @param conversationName The name of the conversation
	 * @param learnersDescription The learner's description
	 */
	public void setDetails(String conversationName, String learnersDescription){
		conversationWidget.setDetails(conversationName, learnersDescription);
	}

	/**
	 * Gets the close button.
	 *
	 * @return The close button for the preview panel.
	 */
	public HasClickHandlers getClosePreviewButton() {
		return closeButton;
	}

	/**
	 * Queues a conversation update to present to the user.
	 *
	 * @param result The update result from the server for this conversation.
	 */
	public void updateConversation(UpdateConversationResult result) {

		boolean pushUpdate = (updateQueue.isEmpty() && avatarWidget.isIdle());

		for(String msg : result.getTutorText()) {

			UpdateConversationResult newResult = new UpdateConversationResult();

			newResult.setTutorText(Arrays.asList(msg));

			if(result.getTutorText().indexOf(msg) == result.getTutorText().size() - 1) {
				newResult.setChoices(result.getChoices());
				newResult.setEndConversation(result.endConversation());
			}

			updateQueue.add(newResult);
		}

		if(pushUpdate && avatarWidget.isIdle()){
			AvatarWidget.idleAvatarNotification();
		}

	}

	/**
	 * Sets the callback to execute when the user selects a choice node in the preview window.
	 *
	 * @param callback The callback to execute.
	 */
	public void setPreviewSubmitTextCallback(ConversationUpdateCallback callback) {
		conversationWidget.setPreviewSubmitTextCallback(callback);
	}

	/**
	 * Sets the chat id of the conversation in the preview window.
	 *
	 * @param chatId The chat id.
	 */
	public void setPreviewChatId(int chatId) {
		conversationWidget.setChatId(chatId);
	}

	/**
	 * Gets the chat id of the conversation in the preview window.
	 *
	 * @return The chat id.
	 */
	public int getPreviewChatId() {
		return conversationWidget.getChatId();
	}

	/**
	 * Closes the preview window.
	 */
	public void closePreview() {
		closeButton.click();
	}

	/**
	 * Gets the widget that contains the conversation preview.
	 */
	public Widget getChatPanel() {
		return chatPanel;
	}

	/**
	 * Constructs the conversation tree as a JSON string so that it can be passed to the server.
	 *
	 *  @param
	 * @return A JSON string representing the conversation tree.
	 */
	public static String buildConversationJSONStr(Conversation currentConversation) {

		JSONObject conversation = new JSONObject();

		conversation.put(ConversationHelper.CONVERSATION_NAME_KEY, new JSONString(currentConversation.getName()));
		if(currentConversation.getVersion() != null) {
			conversation.put(ConversationHelper.VERSION_KEY, new JSONString(currentConversation.getVersion()));
		}
		if(currentConversation.getAuthorsDescription() != null) {
			conversation.put(ConversationHelper.AUTHORS_DESC_KEY, new JSONString(currentConversation.getAuthorsDescription()));
		}
		if(currentConversation.getLearnersDescription() != null) {
			conversation.put(ConversationHelper.LEARNERS_DESC_KEY, new JSONString(currentConversation.getLearnersDescription()));
		}

		return conversation.toString();
	}

	@Override
	public void show() {

		//need to append this modal to the base course editor when it is shown so it takes up the full width and height of the editor
		Element body = getBaseWindowBody();
		HTMLPanel bodyPanel = HTMLPanel.wrap(body);

		if (bodyPanel != null) {
		    bodyPanel.add(this);

		} else {
//		    logger.severe("Unable to find the body panel for this modal.");
		}
	}

	@Override
	public void hide() {

		//Nick: Need to reload the avatar so that any currently running scripts are unloaded properly. If the scripts aren't unloaded,
		//they might execute parts of their logic after this widget has been hidden, causing exceptions to get thrown.
		avatarWidget.reloadAvatar();

		//need to remove this modal from the base course editor when it is hidden
		this.removeFromParent();

	}

	/**
	 * Gets the &lt;body&gt; element for the the window containing the base course editor.
	 */
	private native Element getBaseWindowBody()/*-{

		var baseWnd = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();

		return baseWnd.document.body;
    }-*/;

	/**
	 * Sets whether or not the previewed conversation should be displayed in full screen
	 *
	 * @param fullScreen whether or not to preview in full screen
	 */
	public void setFullScreen(boolean fullScreen) {

	    if(fullScreen) {

	        //move the avatar panel to the center of the chat panel so that it can take up the full screen size
            chatPanel.remove(avatarPanel);
            chatPanel.add(avatarPanel);

	    } else{

	        //move the avatar panel to a side bar so that it doesn't take up the full screen size
	        chatPanel.remove(avatarPanel);
	        chatPanel.addWest(avatarPanel, 417);
        }
	}
}
