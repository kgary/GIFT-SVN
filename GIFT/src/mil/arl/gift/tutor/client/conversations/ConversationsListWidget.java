/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client.conversations;

import gwtquery.plugins.draggable.client.DraggableOptions;
import gwtquery.plugins.draggable.client.DraggableOptions.AxisOption;
import gwtquery.plugins.draggable.client.DraggableOptions.HelperType;
import gwtquery.plugins.draggable.client.events.DragStartEvent;
import gwtquery.plugins.draggable.client.events.DragStartEvent.DragStartEventHandler;
import gwtquery.plugins.draggable.client.gwt.DraggableWidget;
import gwtquery.plugins.droppable.client.events.DropEvent;
import gwtquery.plugins.droppable.client.events.DropEvent.DropEventHandler;
import gwtquery.plugins.droppable.client.gwt.DroppableWidget;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.tutor.client.BrowserSession;
import mil.arl.gift.tutor.client.widgets.TutorActionsWidget;
import mil.arl.gift.tutor.shared.WidgetInstance;
import mil.arl.gift.tutor.shared.properties.ChatWindowWidgetProperties;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A container widget to manage collapsible ConversationWidgets.
 * 
 * @author bzahid
 */
public class ConversationsListWidget extends AbstractUpdateQueue {

	interface ConversationsListWidgetUiBinder extends UiBinder<Widget, ConversationsListWidget> {
	}
	
	private static ConversationsListWidgetUiBinder uiBinder = GWT.create(ConversationsListWidgetUiBinder.class);
	
	private static Logger logger = Logger.getLogger(ConversationsListWidget.class.getName());
	
	/** A map of chat ids to conversation widgets. Used to determine which conversation widget should be updated. */
    private Map<Integer, ConversationWidget> idToChatMap = new HashMap<Integer, ConversationWidget>();
        
    private UpdateCounterWidget updateCounter = new UpdateCounterWidget();
    
    @UiField
    protected ScrollPanel scrollPanel;
    
    protected DroppableWidget<FlowPanel> dropPanel;
    
    protected DraggableWidget<ConversationWidget> selectedChat;
    
    private String incomingActiveChat = null;
    
    /**
     * Class constructor
     */
    public ConversationsListWidget() {
    	super(false);
    	setUpdateCounter(updateCounter);
    	initWidget(uiBinder.createAndBindUi(this));
    	
    	dropPanel = new DroppableWidget<FlowPanel>(new FlowPanel());
    	dropPanel.getElement().setId("dragContainer");
    	scrollPanel.add(dropPanel);
    	
    	dropPanel.addDropHandler(new DropEventHandler() {

			@Override
			public void onDrop(DropEvent event) {
				
				boolean inserted = false;
				int clientY = event.getDragDropContext().getHelperPosition().top;
				int offset = dropPanel.getOriginalWidget().getWidget(0).getAbsoluteTop();
				
				dropPanel.getOriginalWidget().remove(selectedChat);
				
				if(clientY < 20) {
					dropPanel.getOriginalWidget().insert(selectedChat, 0);
					inserted = true; 
					
				} else {
					
					for(int i = 0; i < dropPanel.getOriginalWidget().getWidgetCount(); i++) {
						int beforeWidget = dropPanel.getOriginalWidget().getWidget(i).getAbsoluteTop();
						if((offset + clientY) < beforeWidget) {
							dropPanel.getOriginalWidget().insert(selectedChat, i);
							inserted = true;
							break;
						}
					}
				}
				
				if(!inserted) {
					dropPanel.getOriginalWidget().add(selectedChat);
				}
				
			}
    		
    	});
    }

    /**
     * Creates a new conversation widget and adds it to the list.
     * 
     * @param widgetInstance The widget instance
     */
    private ConversationWidget createNewConversationWidget(WidgetInstance widgetInstance) {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("Creating new conversation widget");
        }
    	
    	ConversationWidget conversationWidget = new ConversationWidget(widgetInstance, new ConversationListActionHandler());    	
    	idToChatMap.put(ChatWindowWidgetProperties.getChatId(widgetInstance.getWidgetProperties()), conversationWidget);
    	
    	final DraggableWidget<ConversationWidget> draggableChat = new DraggableWidget<ConversationWidget>(conversationWidget);
    	dropPanel.getOriginalWidget().insert(draggableChat, 0);
    	
    	//
    	// Setup drag behavior
    	//
    	DraggableOptions dragOptions = new DraggableOptions();
		dragOptions.setAxis(AxisOption.Y_AXIS);
		dragOptions.setZIndex(5);
		dragOptions.setOpacity((float) 0.8);
		dragOptions.setContainment("#dragContainer");
		dragOptions.setHelper(HelperType.CLONE); 
    	
		draggableChat.setWidth("100%");
		draggableChat.setDraggableOptions(dragOptions);
		draggableChat.addDragStartHandler(new DragStartEventHandler() {

			@Override
			public void onDragStart(DragStartEvent event) {
				selectedChat = draggableChat;
			}
			
		});
		
		if(!isActive()){
		    conversationWidget.incrementCounter();
		}
		
		if(isActive() 
				&& incomingActiveChat != null 
				&& incomingActiveChat.equals(ChatWindowWidgetProperties.getChatName(widgetInstance.getWidgetProperties()))) {
			conversationWidget.collapseOrExpandChatPanel();
			incomingActiveChat = null;
		}
		
    	return conversationWidget;
    }
    
    /** 
     * Automatically displays the chat when it is available
     * 
     * @param name The name of the chat to display. Can be null.
     */
    public void setIncomingChatActive(String name) {
    	incomingActiveChat = name;
    }
    
    /**
     * Updates an existing conversation or creates a new one
     * 
     * @param widgetInstance The widget instance
     */
    public void updateOrCreateConversation(WidgetInstance widgetInstance) {
    	if(idToChatMap.containsKey(ChatWindowWidgetProperties.getChatId(widgetInstance.getWidgetProperties()))) {
    		idToChatMap.get(ChatWindowWidgetProperties.getChatId(widgetInstance.getWidgetProperties())).updateChat(widgetInstance);
    	
    	} else {
    		final ConversationWidget chatWidget = createNewConversationWidget(widgetInstance);
    		
    		if(!isActive && !TutorActionsWidget.getInstance().feedbackWidgetHasUpdates()) {
    			BrowserSession.getInstance().isAvatarIdle(new AsyncCallback<Boolean>() {

    				@Override
    				public void onFailure(Throwable caught) {
    					logger.warning("Caught exception while checking avatar status: " + caught);
    				}

    				@Override
    				public void onSuccess(Boolean result) {
    					if(result) {
    						if(!isActive && !TutorActionsWidget.getInstance().feedbackWidgetHasUpdates()) {
    							TutorActionsWidget.getInstance().selectConversation(null);
    							chatWidget.collapseOrExpandChatPanel();
    						}
    					}
    				}

    			});
    		}
    	}
    }
    
    /**
     * Collapses all other conversations except the one with the matching id
     * 
     * @param chatId The conversation that should remain open.
     */
    public void collapseOtherWidgets(int chatId) {
        
        if(logger.isLoggable(Level.INFO)){
            logger.info("Collapsing all conversations except the one with id "+chatId);
        }
        
    	for(Integer id : idToChatMap.keySet()) {
    		if(id != chatId) {
    			if(!idToChatMap.get(id).isComplete() && !idToChatMap.get(id).isCollapsed()) {
    				idToChatMap.get(id).collapseChatPanel();
    			}
    		}
    	}
    }
    
    /**
     * Gets the total number of available updates.
     * 
     * @return the total number of available updates.
     */
    @Override
    public int getUpdateCount() {
    	
    	int updates = 0;    	
    	for(Integer id : idToChatMap.keySet()) {
    		updates += idToChatMap.get(id).getUpdateCount();
    	}
    	
    	updateCounter.setCount(updates);
    	return updates;
    }
    
    /**
     * Gets whether or not there are ongoing conversations
     * 
     * @return True if there is an ongoing conversation, false otherwise
     */
    public boolean hasOngoingConversations() {
    	
    	for(Integer id : idToChatMap.keySet()) {
    		// If a conversation is not complete and it is not collapsed, consider it ongoing.
    		if(!idToChatMap.get(id).isComplete() && idToChatMap.get(id).isActive()) {
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    @Override
    public void updateServerStatus() {
    	
    	//the chat map may not be ready if this method is called during construction, so only update if the map is ready
    	if(idToChatMap != null){
    		
    		for(Integer id : idToChatMap.keySet()) {
        		if(!idToChatMap.get(id).isCollapsed() && !idToChatMap.get(id).isComplete()) {
        			idToChatMap.get(id).setIsActive(isActive());
        			break;
        		}
        	}
    	}
    }
    
    @Override
    public void dequeueUpdates() {
    	// nothing to dequeue for this widget
    }

    /**
     * Used to handle conversation widget requests to collapse all other
     * conversations, making that widget the visible one to interact with.
     *  
     * @author mhoffman
     *
     */
    public class ConversationListActionHandler implements ConversationListActionInterface{

        @Override
        public void collapseOtherWidgetsRequest(int ignoreChatId) {
            collapseOtherWidgets(ignoreChatId);
        }        
        
    }
    
    public interface ConversationListActionInterface{
        
        /**
         * Collapses all other conversations except the one with the matching id
         * 
         * @param ignoreChatId The conversation that should remain open.
         */
        public void collapseOtherWidgetsRequest(int ignoreChatId);
    }
}
