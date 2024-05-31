/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.conversations;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.RpcResponse;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingIcon;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.Document;
import mil.arl.gift.tutor.client.conversations.ConversationsListWidget.ConversationListActionHandler;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.ChatWindowWidgetProperties;
import mil.arl.gift.tutor.shared.properties.WidgetProperties;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.constants.IconSize;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.gwt.HTMLPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
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
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

/**
 * A collapsible widget that presents a conversation.
 * 
 * @author bzahid
 */
public class ConversationWidget extends AbstractUpdateQueue {
		
	private static ConversationWidgetUiBinder uiBinder = GWT.create(ConversationWidgetUiBinder.class);
	
	interface ConversationWidgetUiBinder extends UiBinder<Widget, ConversationWidget> {
	}
	
	private static Logger logger = Logger.getLogger(ConversationWidget.class.getName());
	
	@UiField
	protected FocusPanel focusPanelHeader;
	
	@UiField
	protected HorizontalPanel header;
	
	@UiField
	protected Icon headerIcon;
	
	@UiField
	protected Button infoButton;
		
	@UiField
	protected Label chatName;
	
	@UiField
	protected Label inputHint;
		
	@UiField
	protected FlowPanel contentPanel;
	
	@UiField
	protected HorizontalPanel loadingPanel;
	
	@UiField
	protected BsLoadingIcon loadingIcon;
	
	@UiField
	protected FlowPanel chatPanel;
	
	@UiField
	protected TextArea textArea;
	
	@UiField
	protected UpdateCounterWidget updateCounter;
	
	private WidgetProperties properties;
	
	protected FocusPanel footerPanel;
	
	private ChatWidget chatWidget;
	
	private int chatId = -1;
	
	/**
	 * Used to handle conversation widget requests to collapse all other
     * conversations, making that widget the visible one to interact with.
	 */
	private ConversationListActionHandler conversationListActionHandler;
	
	/**
	 * Callback used then the chat widget used by this class is being closed.  This class needs a different 
	 * implementation than ChatWidget because this class doesn't want to clear the document article or avatar data
	 * since both are being used by the parent to this widget (i.e. TutorActionsWidget)
	 */
    private static final AsyncCallback<RpcResponse> conversationCompletedCallback = new AsyncCallback<RpcResponse>() {
        @Override
        public void onFailure(Throwable caught) {
            Document.getInstance().displayError("Finishing chat session", "RPC Failure", caught);
        }

        @Override
        public void onSuccess(RpcResponse result) {
            if (result == null || !result.isSuccess()) {
                Document.getInstance().displayError("Finishing chat session", "Action failed on the server");
            }
        }
    };
	
	/**
	 * Creates a widget that contains a conversation.
	 * 
	 * @param widgetInstance contains information about the conversation being started
	 * @param conversationListActionHandler Used to handle conversation widget requests to collapse all other
     * conversations, making that widget the visible one to interact with.
	 */
	public ConversationWidget(WidgetInstance widgetInstance, ConversationListActionHandler conversationListActionHandler) {
		super(false);
		initWidget(uiBinder.createAndBindUi(this));		
		setUpdateCounter(updateCounter);
		
		this.conversationListActionHandler = conversationListActionHandler;
		
		chatName.setText(ChatWindowWidgetProperties.getChatName(widgetInstance.getWidgetProperties()));
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
		
		footerPanel = new FocusPanel();
		footerPanel.setWidth("100%");
		footerPanel.getElement().getStyle().setProperty("color", "#EFFFF3");
		footerPanel.getElement().getStyle().setProperty("backgroundColor", "#374C46");
		footerPanel.getElement().getStyle().setProperty("borderRadius", "0 0 8px 8px");
		footerPanel.getElement().getStyle().setProperty("lineHeight", "33px");
		footerPanel.getElement().getStyle().setProperty("cursor", "pointer");
		footerPanel.add(new HTML("You have completed this conversation."));
		
		loadingIcon.setType(IconType.SPINNER);
		loadingIcon.setSize(IconSize.TIMES2);
		loadingIcon.startLoading();
		contentPanel.add(footerPanel);
		footerPanel.setVisible(false);
				
		focusPanelHeader.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				collapseOrExpandChatPanel();
			}
			
		});
		
		footerPanel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				collapseOrExpandChatPanel();
			}
			
		});
		
		infoButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				detailsDialog.center();
			}
			
		});
		
		textArea.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                    String text = textArea.getText();
                    if (text != null && !text.isEmpty()) {

                    	textArea.setEnabled(false);
                    	textArea.setText("");
                        chatWidget.submitText(ChatWindowWidgetProperties.getChatId(properties), text);
                    }
                }
            }
        });
		
		collapseOrExpandChatPanel();
		chatWidget = new ChatWidget(widgetInstance);
		chatPanel.add(chatWidget.getChatPanel());
		properties = widgetInstance.getWidgetProperties();
		chatId = ChatWindowWidgetProperties.getChatId(properties);
		
		textArea.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(textArea.isVisible() && textArea.isEnabled()) {
					textArea.setFocus(true);
				}
			}
			
		});
	}
	
	/**
	 * Updates the chat entries.
	 * 
	 * @param properties The widget properties.
	 */
	public void updateChat(WidgetInstance widgetInstance) {
		properties = widgetInstance.getWidgetProperties();
		
		if(isActive() || ChatWindowWidgetProperties.getEnteredText(properties) != null) {
			chatWidget.updateChat(properties);
			textArea.setEnabled(true);
			textArea.setVisible(ChatWindowWidgetProperties.getAllowFreeResponse(properties));
			inputHint.setVisible(textArea.isVisible());
			focusTextArea();
			
			if(ChatWindowWidgetProperties.getEnteredText(properties)!= null 
					&& !ChatWindowWidgetProperties.getEnteredText(properties).isEmpty()) {
				loadingPanel.setVisible(false);
				loadingIcon.stopLoading();
			}
			
			decrementCounter();
			
			if(ChatWindowWidgetProperties.isFinished(properties)) {
				completeConversation();
			}
			
			if(dequeuing) {
				dequeueUpdates();
			}
			
		} else {
			incrementCounter();
		}
	}
	
	/**
	 * Sets the focus on the text area if it is visible.
	 */
	private void focusTextArea() {
		if(textArea.isVisible()) {
			Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
	            @Override
	            public void execute() {
	            	textArea.setFocus(true);
	            }
	        });
		}
	}
	
	/**
	 * Collapses the chat panel to only display the conversation header, or 
	 * expands the chat panel if it is already collapsed.
	 */
	public void collapseOrExpandChatPanel() {
		
		if(!contentPanel.isVisible() && !isComplete()) {
			if(conversationListActionHandler != null) {
				// If this chat widget is being opened, close all other ones first
			    conversationListActionHandler.collapseOtherWidgetsRequest(chatId);
			}
			
			setIsActive(true);
			focusTextArea();
			
		} else if(contentPanel.isVisible() && !isComplete()) {
			// If this chat widget is being closed, update the server status
		    logger.info("Setting isactive to false because panel is visible and is not complete for\n"+this);
			setIsActive(false);
		}
		
		contentPanel.setVisible(!contentPanel.isVisible());
		
		if(contentPanel.isVisible()) {
			header.getElement().getStyle().setProperty("borderRadius", "8px 8px 0 0");
		} else {
			header.getElement().getStyle().setProperty("borderRadius", "8px");
		}
	}
	
	/**
	 * Collapses the chat panel.
	 */
	public void collapseChatPanel() {
		contentPanel.setVisible(false);
		header.getElement().getStyle().setProperty("borderRadius", "8px");
		
		if(isActive){
			setIsActive(false);
		}
	}
		
	/**
	 * Gets whether or not the conversation widget is collapsed.
	 * 
	 * @return True if the widget is collapsed, false otherwise.
	 */
	public boolean isCollapsed() {
		return !contentPanel.isVisible();
	}
	
	/**
	 * Gets whether or not the conversation has been completed
	 * 
	 * @return True if the conversation is complete, false otherwise.
	 */
	public boolean isComplete() {
		return footerPanel.isVisible();
	}
	
	/**
	 * Adjusts the conversation widget to indicate that the conversation has been completed.
	 */
	public void completeConversation() {
	    
	    if(logger.isLoggable(Level.INFO)){
	        logger.info("Conversation completed : "+chatWidget);
	    }
		headerIcon.setType(IconType.CHECK_CIRCLE);
		header.getElement().getStyle().setProperty("background", "#374C46");
		contentPanel.getElement().getStyle().setProperty("borderRadius", "0 0 8px 8px");
		footerPanel.setVisible(true);
		inputHint.setVisible(false);
		textArea.setVisible(false);
		
		//when the conversation is during an external application course object there will be no continue
        //button shown to the learner, therefore need to make sure the chat widget is closed which notifies
        //the tutor server to respond to the domain's original start chat request message allowing the domain
        //module to continue its logic that was halted for this conversation.
        chatWidget.closeWidget(conversationCompletedCallback);
	}
	
	@Override
	public void updateServerStatus() {
		if(isActive()) {
            if(logger.isLoggable(Level.INFO)){
                logger.info("Notifying server that this conversation is now active\n"+toString());
            }
			BrowserSession.setActiveConversationWidget(chatId, new AsyncCallback<RpcResponse>() {
	
				@Override
				public void onFailure(Throwable thrown) {
					logger.warning("Failed to update ConversationWidget status on the server: " + thrown.toString());
				}
	
				@Override
				public void onSuccess(RpcResponse response) {
					if(response.isSuccess() && !dequeuing) {
					    //if already dequeuing don't ask server to dequeue again.  This prevents duplicate
					    //dequeued conversation elements from being received which happened when collapsing
					    //and expanding the conversation repeatedly.
						dequeueUpdates();
					}else{
					    
                        if(logger.isLoggable(Level.INFO)){
                            logger.info("Server has been notified of new active conversation but not dequeue because already dequeuing\n"+toString());
                        }
					}
				}
				
			});
		} else {
		    if(logger.isLoggable(Level.INFO)){
		        logger.info("Requesting server to set conversation widget inactive for\n"+this);
		    }
			BrowserSession.setConversationWidgetInactive(chatId);
			dequeuing = false;
		}
	}
	
	@Override
	public void dequeueUpdates() {
		if(isActive()) {
		    
		    if(logger.isLoggable(Level.INFO)){
		        logger.info("Requesting conversation elements be dequeued on the server for\n"+toString());
		    }
			dequeuing = true;
			
			BrowserSession.dequeueChatWidgetUpdate(new AsyncCallback<RpcResponse>() {

				@Override
				public void onFailure(Throwable thrown) {
					logger.warning("Failed to retrieve a chat request update from the server for\n"+this+"\nbecause '" + thrown.toString()+"'.");
				}

				@Override
				public void onSuccess(RpcResponse result) {
					if(result.isSuccess()) {
						// Nothing to do
					} else {
						// No more updates
						dequeuing = false;
					}
				}
				
			});
		} else {
            if(logger.isLoggable(Level.INFO)){
                logger.info("Unable to request conversation elements be dequeued on the server for a NON ACTIVE\n"+toString());
            }
			dequeuing = false;
		}
	}
	
	@Override
    public String toString(){
	    
	    StringBuilder sb = new StringBuilder();
	    sb.append("[ConversationWidget: ");
	    sb.append("chatId = ").append(chatId);
	    sb.append(", chatName = ").append(chatName);
	    sb.append(",\nchatWidget = ").append(chatWidget);
	    sb.append(",\nproperties = ").append(properties);
	    sb.append("]");
	    return sb.toString();
	}

}
