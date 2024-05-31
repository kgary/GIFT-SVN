/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.conversations;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.WhiteSpace;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.i18n.shared.DateTimeFormat;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;

import mil.arl.gift.common.AvatarData;
import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.Document.ConfirmationDialogCallback;
import mil.arl.gift.tutor.client.widgets.AvatarContainer;
import mil.arl.gift.tutor.shared.ChatWindowEntry;
import mil.arl.gift.tutor.shared.CloseAction;
import mil.arl.gift.tutor.shared.SubmitAction;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.ChatWindowWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

/**
 * Widget for chatting with the user with GIFT
 * 
 * @author jleonard
 */
public class ChatWidget extends Composite {

    private static FeedbackWidgetUiBinder uiBinder = GWT.create(FeedbackWidgetUiBinder.class);
    
    private static Logger logger = Logger.getLogger(ChatWidget.class.getName());

    /** The label to represent the user entering text. */
    private final String user = "me";
    
    @UiField
    ScrollPanel feedbackScrollPanel;

    @UiField
    FlowPanel feedbackPanel;

    @UiField
    Label inputHint;
    
    @UiField
    TextArea inputArea;
    
    @UiField
    Image loadingImage;
    
    @UiField
    InlineLabel loadingText;
    
    @UiField
    Button continueButton;
    
    @UiField
    protected Button infoButton;
        
    @UiField
    protected Label chatName;
    
    @UiField
    protected FlowPanel contentPanel;
    
    @UiField
    protected Icon headerIcon;

    private final WidgetInstance widgetInstance;
    
    private int chatId = -1;
    
    private KeyDownHandler INPUT_AREA_KEYDOWN_HANDLER = new KeyDownHandler() {
        @Override
        public void onKeyDown(KeyDownEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                String text = inputArea.getText();
                if (text != null && !text.isEmpty()) {
                    inputArea.setEnabled(false);
                    inputArea.setText("");
                    submitText(chatId, text);
                }
            }
        }
    };
    
    protected FocusPanel footerPanel;
    
    /** 
     * flag used to indicate whether the chat widget was closed, 
     * either by pressing the continue button or because the conversation is over 
     */
    private boolean chatClosed = false;
    
    interface FeedbackWidgetUiBinder extends UiBinder<Widget, ChatWidget> {
    }

    /**
     * Constructor
     *
     * @param widgetInstance The abstract instance of the widget
     */
    public ChatWidget(final WidgetInstance widgetInstance) {
    	
        this.widgetInstance = widgetInstance;
        Widget uiBinderWidget = uiBinder.createAndBindUi(this);
        final WidgetProperties properties = widgetInstance.getWidgetProperties();
        
        logger.info("Creating chat widget with properties "+properties);

        if (properties != null) {
            
            chatName.setText(ChatWindowWidgetProperties.getChatName(properties));
            
            Date date = new Date();
            DateTimeFormat format = DateTimeFormat.getFormat("MM/dd - hh:mm a");
            
            HTMLPanel detailsPanel = new HTMLPanel("");
            detailsPanel.add(new HTML(
                    "<b style=\"line-height: 30px\">Name: </b>" + SafeHtmlUtils.htmlEscape(chatName.getText()) +
                    "<br/><b style=\"line-height: 30px\">Start Time: </b>" + format.format(date) +
                    "<br/><b style=\"line-height: 30px\">Description: </b>" + ChatWindowWidgetProperties.getDescription(widgetInstance.getWidgetProperties())));        
            detailsPanel.addStyleName("conversationDetails");
            
            final ModalDialogBox detailsDialog = new ModalDialogBox();
            detailsDialog.setText("Conversation Details");
            detailsDialog.setGlassEnabled(true);
            detailsDialog.setCloseable(true);
            detailsDialog.add(detailsPanel);
            detailsDialog.addStyleName("detailsDialog");
            
            infoButton.addClickHandler(new ClickHandler() {

                @Override
                public void onClick(ClickEvent event) {
                    detailsDialog.center();
                }
                
            });
            
            footerPanel = new FocusPanel();
            footerPanel.setWidth("100%");
            footerPanel.getElement().getStyle().setProperty("color", "#EFFFF3");
            footerPanel.getElement().getStyle().setProperty("backgroundColor", "#374C46");
            footerPanel.getElement().getStyle().setProperty("borderRadius", "0 0 8px 8px");
            footerPanel.getElement().getStyle().setProperty("lineHeight", "33px");
            footerPanel.getElement().getStyle().setProperty("cursor", "pointer");
            footerPanel.add(new HTML("You have completed this conversation."));
            
            contentPanel.add(footerPanel);
            footerPanel.setVisible(false);
        }
        
        updateChat(properties);

        initWidget(uiBinderWidget);

        bringLastChatInToFocus();
        
        inputArea.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(inputArea.isVisible() && inputArea.isEnabled()) {
					inputArea.setFocus(true);
				}
			}
			
		});
    }
    
    /**
     * Show the continue button for this chat if appropriate.  Currently this can be when the conversation
     * is completed or the conversation is allowed to be exited early (both via property values).
     * 
     * @param properties contains the properties for this widget at the current moment.  
     */
    private void enableContinueButton(final WidgetProperties properties){
        
        if (ChatWindowWidgetProperties.getAllowEarlyExit(properties) || ChatWindowWidgetProperties.isFinished(properties)) {
            //enable the continue button
            
            logger.info("Showing the continue button and hiding the input area");
            
            continueButton.setVisible(true);
            continueButton.setEnabled(true);
            
            if(ChatWindowWidgetProperties.isFinished(properties)) {
                footerPanel.setVisible(true);
                headerIcon.setType(IconType.CHECK_CIRCLE);
            }

            continueButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {

                    continueButton.setEnabled(false);
                    
                    final AsyncCallback<RpcResponse> callback = new AsyncCallback<RpcResponse>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            Document.getInstance().displayError("Finishing chat session", "RPC Failure", caught);
                        }
            
                        @Override
                        public void onSuccess(RpcResponse result) {
                            if (result == null || !result.isSuccess()) {
                                Document.getInstance().displayError("Finishing chat session", "Action failed on the server");
                            }
                            
                            if(logger.isLoggable(Level.INFO)){
                                logger.info("Clearing the article widget which should be this chat widget because the chat continue button was pressed.");
                            }
                            
                            // Both of these are necessary in order for:
                            // 1. the chat widget to be created again for subsequent course objects (i.e. don't use the update method for a new conversation)
                            // 2. the avatar to be created again which will cause the avatar idle notification to be sent to the server, thereby triggering
                            //    queued conversation message to be released.
                            Document.getInstance().setArticleWidget(null);
                        }
                    };

                    if (!ChatWindowWidgetProperties.isFinished(properties)) {

                        Document.getInstance();
                        Document.displayConfirmationDialog("End Chat Session", "Are you sure you want to end the chat session?", new ConfirmationDialogCallback() {
                            @Override
                            public void onDecline() {

                                continueButton.setEnabled(true);
                            }

                            @Override
                            public void onAccept() {

                                closeWidget(callback);
                            }
                        });

                    } else {

                        closeWidget(callback);
                    }
                }
            });
        }
    }
    
    /**
     * Updates the conversation.
     * 
     * @param properties The widget properties.
     */
    public void updateChat(final WidgetProperties properties) {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("updating chat with "+properties);
        }

        if (properties != null) {
        	
            chatId = ChatWindowWidgetProperties.getChatId(properties);
            
            List<ChatWindowEntry> chatLog = ChatWindowWidgetProperties.getChatLog(properties);
            String spokenText = ChatWindowWidgetProperties.getEnteredText(properties);
            AvatarData avatar = ChatWindowWidgetProperties.getAvatar(properties);

            if (avatar != null && avatar.getURL() != null) {
                
//                BrowserSession.getInstance().busyAvatarNotification();

                if (AvatarContainer.setAvatarData(avatar) && spokenText != null) {

                    AvatarContainer.defineSayFeedback(stripFeedback(spokenText));
                    
                } else if(spokenText != null) {
                     AvatarContainer.sayFeedback(stripFeedback(spokenText));
                }

            }

            if (chatLog != null) {

                if (!chatLog.isEmpty()) {
                    
        	//hide the loading indicators since a chat entry was received from the server
        	//allow addChatLog method to manage whether these indicators are shown again
                    logger.info("hiding loading text");
        	loadingImage.setVisible(false);
        	loadingText.setVisible(false);
        	
                feedbackPanel.clear();  //only clear the panel if the incoming update has chat entries to re-populate the panel with
                                        //This is needed when the update is merely for updating the queue count overlay (e.g. during external application course objects)
                    
                addChatLog(chatLog, properties);

                } else {

                    logger.info("showing loading text");
                    loadingImage.setVisible(true);
                    loadingText.setVisible(true);
                    
                    inputArea.setEnabled(false);
            }
           
            } else {

                logger.info("showing loading text");
                loadingImage.setVisible(true);
                loadingText.setVisible(true);
                
                inputArea.setEnabled(false);
            }

            if (ChatWindowWidgetProperties.isFinished(properties)) {

                logger.info("Hiding the input area because the chat is over.");
                inputHint.setVisible(false);
                inputArea.setVisible(false);                

            } else {
                
                if(ChatWindowWidgetProperties.getAllowFreeResponse(properties)){

                    logger.info("Showing the input area");
                    inputHint.setVisible(true);
                    inputArea.setVisible(true);
                    inputArea.setEnabled(true);
                    
                    inputArea.addKeyDownHandler(INPUT_AREA_KEYDOWN_HANDLER);
                    
                }else{
                    logger.info("Hiding the input area because free response is not allowed at this time.");
                    inputHint.setVisible(false);
                    inputArea.setVisible(false);
        }
    }
    
            //check to see if the continue button below the conversation should be enabled at this point
            enableContinueButton(properties);
        }
    }
    
    /**
     * Adds the entire chat log to the chat widget panel.  In most cases you want to clear the conversation
     * from the {@link ChatWidget#feedbackPanel} first so duplicate chat entries are not show to the learner.
     * Ideally only the new chat entries would be added here.  The loading components visibility will also 
     * be managed in this method.  If the last chat entry contains a choice for the learner (i.e. a question element),
     * than the loading indicator will be hidden.
     * 
     * @param chatLog contains the entire conversation to show on the chat widget
     * @param properties properties of the widget, including the chat id
     */
    private void addChatLog(List<ChatWindowEntry> chatLog, final WidgetProperties properties){
        
        if(chatLog == null){
            return;
        }
        
        ChatWindowEntry chatEntry = null;
        for (int index = 0; index < chatLog.size(); index++) {
            chatEntry = chatLog.get(index);
            boolean isLastEntry = (chatLog.indexOf(chatEntry) == chatLog.size() - 1);
            addChatEntry(ChatWindowWidgetProperties.getChatId(properties), chatEntry, isLastEntry);
        }
        
        //
        // Manage loading indicator
        //
        if(chatEntry != null && chatEntry.getChoices() != null && !chatEntry.getChoices().isEmpty()){
            //the last chat entry contains a question with choices for the learner, therefore not waiting on 
            //the server to provide more conversation elements and can hide the loading indicators
            logger.info("hiding loading text in addChatLog method");
            loadingImage.setVisible(false);
            loadingText.setVisible(false);
        }else if(!ChatWindowWidgetProperties.isFinished(properties) && !ChatWindowWidgetProperties.getAllowFreeResponse(properties)){
            //this indicates that the chat is not finished and the last conversation element is a 
            //message (not a question cause there are no choices) and the learner is not for a free response input, 
            // therefore the server will be providing another tutor chat element coming up next.
            logger.info("showing loading text in addChatLog method");
            loadingImage.setVisible(true);
            loadingText.setVisible(true);
        }
        
        //
        // Manage continue (to next course object) button
        //
        enableContinueButton(properties);
    }
    
    /**
     * Submits the user's text to the server.
     * 
     * @param chatId the id of the chat being updated.
     * @param text The text to be submitted.
     */
    public void submitText(int chatId, String text) {
    	
        logger.info("showing loading text in submitText method");
        loadingImage.setVisible(true);
        loadingText.setVisible(true);
        inputArea.setEnabled(false);
        
    	WidgetProperties submitProperties = new WidgetProperties();
        ChatWindowWidgetProperties.setEnteredText(submitProperties, text);
        ChatWindowWidgetProperties.setDisplayInConversationPanel(submitProperties, 
        		ChatWindowWidgetProperties.shouldDisplayInConversationPanel(widgetInstance.getWidgetProperties()));
        ChatWindowWidgetProperties.setChatId(submitProperties, chatId);
        
        ChatWindowEntry chatWindowEntry = new ChatWindowEntry(user, text);
        
        addChatEntry(chatId, chatWindowEntry, true);
        
        bringLastChatInToFocus();
        
        continueButton.setEnabled(false);

        final String pageId = widgetInstance.getWidgetId();
        
        BrowserSession.getInstance().sendActionToServer(new SubmitAction(pageId, submitProperties), new AsyncCallback<RpcResponse>() {
            @Override
            public void onFailure(Throwable caught) {
                continueButton.setEnabled(true);
                Document.getInstance().displayError("Sending chat message", "RPC Failure", caught);
            }

            @Override
            public void onSuccess(RpcResponse result) {
                
                logger.info("Received chat action response from server of "+result);
                
                logger.info("hiding loading text onSuccess for sendActionToServer");
                loadingImage.setVisible(false);
                loadingText.setVisible(false);
                inputArea.setEnabled(true);  //re-enable after being disabled when the learner pressed the enter key, it can still be hidden at this point
                if (result == null || !result.isSuccess()) {
                    continueButton.setEnabled(true);
                    Document.getInstance().displayError("Sending chat message", "Action failed on the server");
                }
            }
        });
    }
    
    /**
     * Gets the panel containing messages between the tutor and client.
     * 
     * @return a panel containing messages between the tutor and client.
     */
    public FlowPanel getChatPanel() {
    	return feedbackPanel;
    }
    
    /**
     * Adds an entry to the conversation
     * 
     * @param chatId the id of the chat to update.
     * @param chatEntry The chat entry to add.
     * @param isLastEntry Whether or not this is the last chat entry in the update.
     */
    private void addChatEntry(final int chatId, ChatWindowEntry chatEntry, boolean isLastEntry) {

        if (chatEntry.getSource() != null && chatEntry.getText() != null) {

        	HorizontalPanel wrapper = new HorizontalPanel();
        	FlowPanel textPanel = new FlowPanel();
            Label fromWhomLabel = new Label(chatEntry.getSource() + ": ");
            HTML chatText = new HTML(chatEntry.getText());
            
            fromWhomLabel.addStyleName("chatWhom");
            chatText.addStyleName("chatText");
            
            if(chatEntry.getSource().equals(user)) {
            	wrapper.getElement().getStyle().setColor("#5A2020");
            }
            
            textPanel.add(chatText);
            wrapper.add(fromWhomLabel);
            wrapper.add(textPanel);
            feedbackPanel.add(wrapper);
            
            if(chatEntry.getChoices() != null && !chatEntry.getChoices().isEmpty()) {
            	
            	final VerticalPanel choicePanel = new VerticalPanel();
            	
            	for(final String choice : chatEntry.getChoices()) {
            		
            		final Button choiceButton = new Button();			
    				choiceButton.getElement().setInnerHTML(choice);
    				choiceButton.setDataToggle(Toggle.BUTTON);				
    				choiceButton.addStyleName("tutor-radio-button");
    				choiceButton.setType(ButtonType.PRIMARY);
    				
    				choiceButton.getElement().getStyle().setWhiteSpace(WhiteSpace.NORMAL); 	//allows text to wrap
    				choiceButton.getElement().getStyle().setTextAlign(TextAlign.LEFT);		//aligns text to the left
    				choiceButton.getElement().getStyle().setMarginBottom(3, Unit.PX);	
    				
            		if(!isLastEntry) {
            			// disable any old choices
            			choiceButton.setEnabled(false);
            			
            		} else {
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
								
								submitText(chatId, choice);
							}
	            			
	            		});
            		}
            		
            		choicePanel.add(choiceButton);
            	}
            	
            	textPanel.add(choicePanel);
            }
            
        } else {

            throw new IllegalArgumentException("The source and message must not be null");
        }
    }

    private void bringLastChatInToFocus() {

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                feedbackScrollPanel.setVerticalScrollPosition(feedbackPanel.getOffsetHeight() + 1);
                inputArea.setFocus(true);
            }
        });
    }
    
    /**
     * Notifies the client and server that the chat widget is being closed.
     * In most cases this notification will trigger a reply message being sent from tutor to
     * domain module.  The reply message is in response to the original start chat (conversation) request message
     * from the domain module.
     * If this widget was already closed the method will do nothing.
     * 
     * @param callback  Callback for when the server responds to the close widget action. Can be null if no callback is needed.
     * This can be useful for performing post widget close actions like clearing the article or avatar data.
     */
    public synchronized void closeWidget(AsyncCallback<RpcResponse> callback) {        

        if(!chatClosed){
            final String pageId = widgetInstance.getWidgetId();            
            BrowserSession.getInstance().sendActionToServer(new CloseAction(pageId), callback);
        
            chatClosed = true;  //so the close action doesn't get sent again
        }
    }

    /**
     * Remove feedback of unparsable text before sending to the speech engine
     * 
     * Removes HTML tags and text in parentheses.
     *
     * @param feedback The feedback to clean up
     * @return String The speech engine parsable text
     */
    private String stripFeedback(String feedback) {
        String newFeedback = feedback;
        newFeedback = newFeedback.replaceAll("<(.|\n)*?>", "");
        newFeedback = newFeedback.replaceAll("\\((.|\n)*?\\)", "");
        return newFeedback;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[ChatWidget: ");
        sb.append("widgetInstance = ").append(widgetInstance);
        sb.append(", continueButtonShown = ").append(continueButton.isEnabled() && continueButton.isVisible());
        sb.append("]");
        
        return sb.toString();
    }

}