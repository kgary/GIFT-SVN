/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation.preview;

import java.util.Date;
import java.util.List;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.UpdateConversation;

/**
 * A widget that presents a conversation.
 *
 * @author bzahid
 */
public class ConversationWidget extends Composite {

	private static ConversationWidgetUiBinder uiBinder = GWT.create(ConversationWidgetUiBinder.class);

	interface ConversationWidgetUiBinder extends UiBinder<Widget, ConversationWidget> {
	}

	public static interface ConversationUpdateCallback {
		public void getUpdate(UpdateConversation action);
	}

	@UiField
	protected HorizontalPanel header;

	@UiField
	protected Icon headerIcon;

	@UiField
	protected Button infoButton;

	@UiField
	protected Label chatName;

	@UiField
	protected FlowPanel contentPanel;

	@UiField
	protected FlowPanel chatPanel;

	@UiField
    protected FlowPanel footerPanel;

    @UiField
    protected HTML completedHtml;

    @UiField
    protected Button continueButton;

	private ModalDialogBox detailsDialog = new ModalDialogBox(){

		@Override
		public void show() {

			super.show();

			//need to append this modal to the base course editor when it is shown so it shows above the preview dialog.
			Element body = getBaseWindowBody();
			com.google.gwt.user.client.ui.HTMLPanel bodyPanel = com.google.gwt.user.client.ui.HTMLPanel.wrap(body);

			if (bodyPanel != null) {
			    bodyPanel.add(this);

			} else {
//			    logger.severe("Unable to find the body panel for this modal.");
			}
		}

		@Override
		public void hide() {

			//need to remove this modal from the base course editor when it is hidden
			this.removeFromParent();

			super.hide();
		}
	};

	private BsLoadingIcon loadingIcon = new BsLoadingIcon();

	private ConversationUpdateCallback submitTextCallback = null;

	private int chatId = -1;

	/**
	 * Creates a widget that contains a conversation.
	 */
	public ConversationWidget() {
		initWidget(uiBinder.createAndBindUi(this));

		detailsDialog.setText("Conversation Details");
		detailsDialog.setGlassEnabled(true);
		detailsDialog.setCloseable(true);
		detailsDialog.addStyleName("detailsDialog");

		infoButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				detailsDialog.center();
			}

		});

		FlowPanel iconContainer = new FlowPanel();
		iconContainer.getElement().getStyle().setProperty("margin", "auto");
		iconContainer.setWidth("30px");
		loadingIcon.setType(IconType.SPINNER);
		loadingIcon.setSize(IconSize.TIMES2);
		loadingIcon.startLoading();
		chatPanel.add(iconContainer);

	}

    /**
     * Shows the {@link #continueButton} without showing the other content of
     * the {@link #footerPanel}. This method should be used in the case where
     * the conversation ended prematurely (e.g. an error occurred).
     */
    public void showContinueButton() {
        footerPanel.setVisible(true);
        completedHtml.setVisible(false);
    }

	/**
     * Sets the command to execute when the conversation is complete.
     *
     * @param handler the {@link ClickHandler} to execute when the
     *        {@link #continueButton} is clicked. Can't be null.
     */
    public void addContinueClickHandler(ClickHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("The parameter 'handler' cannot be null.");
        }

        continueButton.addClickHandler(handler);
	}

	/**
     * Sets the conversation details to display in the information dialog.
     *
     * @param chatName The name of the conversation
     * @param learnersDescription The description of the conversation.
     */
	public void setDetails(String chatName, String learnersDescription) {
		this.chatName.setText(chatName);
		Date date = new Date();
		DateTimeFormat format = DateTimeFormat.getFormat("MM/dd - hh:mm a");

		HTMLPanel detailsPanel = new HTMLPanel("");
		detailsPanel.add(new HTML(
				"<b style=\"line-height: 30px\">Name: </b>" + SafeHtmlUtils.htmlEscape(chatName) +
				"<br/><b style=\"line-height: 30px\">Start Time: </b>" + format.format(date) +
				"<br/><b style=\"line-height: 30px\">Description: </b>" + learnersDescription));
		detailsPanel.addStyleName("conversationDetails");

		detailsDialog.setWidget(detailsPanel);

	}

	/**
	 * Sets the chat id of the conversation.
	 *
	 * @param chatId The chat id.
	 */
	public void setChatId(int chatId) {
		this.chatId = chatId;
	}

	/**
	 * Gets the chat id of the conversation.
	 *
	 * @return The chat id.
	 */
	public int getChatId() {
		return chatId;
	}

	/**
	 * Updates the chat text entries.
	 *
	 * @param source The source of the text
	 * @param text The message to display
	 * @param choices The choices to present to the user.
	 * @param endConversation Whether or not the conversation should end
	 */
	public void updateChatText(String source, String text, List<String> choices, boolean endConversation) {

		loadingIcon.stopLoading();
		loadingIcon.setVisible(false);
		FlowPanel textPanel = null;

		if(text != null && !text.isEmpty()) {
			HorizontalPanel wrapper = new HorizontalPanel();
			Label fromWhomLabel = new Label(source + ": ");
			HTML chatText = new HTML(text);
			textPanel = new FlowPanel();

			fromWhomLabel.addStyleName("chatWhom");
			chatText.addStyleName("chatText");

			if(source.equals(GatClientUtility.getUserName())) {
				wrapper.getElement().getStyle().setColor("#5A2020");
			}

			textPanel.add(chatText);
			wrapper.add(fromWhomLabel);
			wrapper.add(textPanel);
			chatPanel.add(wrapper);
		}

		if(choices != null && !choices.isEmpty()) {

			final VerticalPanel choicePanel = new VerticalPanel();

			for(final String choice : choices) {

				final Button choiceButton = new Button();
				choiceButton.getElement().setInnerHTML(choice);
				choiceButton.setDataToggle(Toggle.BUTTON);
				choiceButton.addStyleName("tutor-radio-button");
				choiceButton.setType(ButtonType.PRIMARY);

				choiceButton.getElement().getStyle().setWhiteSpace(WhiteSpace.NORMAL); 	//allows text to wrap
				choiceButton.getElement().getStyle().setTextAlign(TextAlign.LEFT);		//aligns text to the left
				choiceButton.getElement().getStyle().setMarginBottom(3, Unit.PX);

				choiceButton.addClickHandler(new ClickHandler() {

					@Override
					public void onClick(ClickEvent event) {

						choiceButton.setActive(true);

						// disable all buttons
						for(int index = 0; index < choicePanel.getWidgetCount(); index++) {
							if(choicePanel.getWidget(index) instanceof Button) {
								((Button)choicePanel.getWidget(index)).setEnabled(false);
							}
						}
						submitText(choice);
					}

				});

				choicePanel.add(choiceButton);
			}

			if(textPanel != null) {
				textPanel.add(choicePanel);
			}
		}

		if(endConversation) {
			completeConversation();
		}

	}

	/**
	 * Displays text to submit in the chat and submits text to the server
	 *
	 * @param text The text to submit
	 */
	public void submitText(String text) {
		updateChatText("me", text, null, false);

		if(submitTextCallback != null) {
			UpdateConversation action = new UpdateConversation();

			action.setChatId(chatId);
			action.setUserText(text);
			submitTextCallback.getUpdate(action);
		}
	}

	/**
	 * Sets the callback to execute when the user selects a choice node in the preview window.
	 *
	 * @param callback The callback to execute.
	 */
	public void setPreviewSubmitTextCallback(ConversationUpdateCallback callback) {
		this.submitTextCallback = callback;
	}

	/**
	 * Adjusts the conversation widget to indicate that the conversation has been completed.
	 */
	public void completeConversation() {
		headerIcon.setType(IconType.CHECK_CIRCLE);
		header.getElement().getStyle().setProperty("background", "#374C46");
		contentPanel.getElement().getStyle().setProperty("borderRadius", "0 0 8px 8px");
		footerPanel.setVisible(true);
	}

	/**
	 * Clears the chat entries and resets the widget.
	 */
	public void clearChat() {
		chatPanel.clear();
		footerPanel.setVisible(false);
		header.getElement().getStyle().setProperty("background", "rgb(82, 72, 72)");
		contentPanel.getElement().getStyle().setProperty("borderRadius", "0 0 20px 20px");
	}

	/**
	 * Gets the &lt;body&gt; element for the the window containing the base course editor.
	 */
	private native Element getBaseWindowBody()/*-{

		var baseWnd = @mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility::getBaseEditorWindow()();

		return baseWnd.document.body;
    }-*/;
}
