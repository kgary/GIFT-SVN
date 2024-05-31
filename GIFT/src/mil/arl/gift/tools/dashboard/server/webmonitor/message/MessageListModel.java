/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.util.JsonFormat;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.net.api.message.codec.proto.ProtoMapper;
import mil.arl.gift.net.proto.ProtoCodec;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageDisplayData;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageEntryMetadata;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageFilterChoicesUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageHeaderStatusUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageListenChangedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageReceivedUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageRemovedUpdate;

/**
 * Maintains a list of messages to be shown in a message panel on the client.
 * 
 * @author nroberts
 */
public class MessageListModel implements FilterChangeListener {
    
    /**  max number of messages to store before removing the oldest messages */
	public static final int MESSAGE_LIST_MAX_ELEMENTS = DashboardProperties.getInstance().getMessageDisplayBufferSize();
	
	/** container for all messages */
	private final List<Message> masterList  = new LinkedList<>();
	
	/** A mapping from each message's metadata to the full message it represents*/
	private Map<MessageEntryMetadata, Message> metadataToMessage = new ConcurrentHashMap<>();
	
	/** container for all messages that are able to be displayed (i.e. available for selection) */
	private final List<MessageEntryMetadata> displayList = new ArrayList<>(); 
	
	/** The filter used to display only specific messages */
	private MessageViewFilter messageDisplayFilter;
	
	/** Whether this model should listen for new messages to add */
	private boolean listening = true;
	
	/** Whether advanced message header data should be shown to the client */
	private boolean advancedHeader = false;

	/** A handler that pushes message updates to any listeners (such as the client)*/
    private MessageUpdateHandler updateHandler;

    /** The ID of the domain session associated with this list model */
    private Integer domainSessionId;
	
	/**
	 * Used to ascertain whether this is listening or not listening.
	 * 
	 * @return whether this model is listening
	 */
	private boolean isListening() {
		
		return listening;
	}
	
	/**
	 * Sets the listening state of this MessageListModel.
	 * 
	 * @param listening boolean indicating listening or not listening.
	 * 
	 */
	void setListening(boolean listening) {
		
		this.listening = listening;
	}
	
	/**
     * Sets the advanced header state of this MessageListModel.
     * 
     * @param advancedHeader boolean indicating if additional message data should be displayed.
     * 
     */
    void setAdvancedHeader(boolean advancedHeader) {
        this.advancedHeader = advancedHeader;
    }
	
	/**
	 * Creates a new message list model that uses the given handler
	 * to push message updates to any listeners (such as the client)
	 * 
	 * @param updateHandler the handler that will push updates to listeners. Cannot be null.
	 */
	public MessageListModel(MessageUpdateHandler updateHandler) {
	    
	    if(updateHandler == null) {
	        throw new IllegalArgumentException("The update handler for a message list model cannot be null");
	    }
	    
        this.updateHandler = updateHandler;
    }
	
	/**
	 * Check message against filter (convenience methods does null check).
	 * @param msg the message to check against the filter
	 * @return returns true if the message passes the filter -OR- if the filter is null.
	 */
    private boolean acceptMessage(Message msg) {
		
		if(messageDisplayFilter != null) {
		
			return messageDisplayFilter.acceptMessage(msg);
		}
		
        return true;
	}
	
    /**
     * Clear the available messages list and re-populate it with the current
     * list of available messages for selection.
     */
	private void rebuildDisplayList() {
	    		
	    synchronized(displayList) {
	        displayList.clear();
	        
	        synchronized(metadataToMessage) {
	            metadataToMessage.clear();
	        }
	    }
		
		synchronized(masterList) {
    		for(Message msg : masterList) {
    			
    			if(acceptMessage(msg) ) {
    			    
    			    synchronized(displayList) {
    			        
    			        MessageEntryMetadata metadata = getMetadata(msg);
    			        displayList.add(metadata);
    			        
    			        synchronized(metadataToMessage) {
    			            metadataToMessage.put(metadata, msg);
    			        }
    			    }
    			}
    		}
		}
        
        updateHandler.onMessage(new MessageFilterChangedUpdate(domainSessionId, displayList));
	}
		
	/**
	 * Used by clients to post a new message into the model.
	 * @param msg the message to post.
	 */
    void postMessage(final Message msg) {
	    
    	if(this.isListening()) {
    	    
    	    MessageEntryMetadata metadata = getMetadata(msg);
    	         	
    	    synchronized(masterList) {
    	        
        	    //assume incoming messages are for the current users
        		while(masterList.size() >= MESSAGE_LIST_MAX_ELEMENTS ) {
        			masterList.remove(0);
        		}
    		    masterList.add(msg);
    		}
    		
    		synchronized(displayList) {
        		while( displayList.size() >= MESSAGE_LIST_MAX_ELEMENTS) {
        		 
        		    MessageEntryMetadata removed = displayList.remove(0);
        		    
        		    synchronized(metadataToMessage) {
        		        metadataToMessage.remove(removed);
        		    }
                    
                    updateHandler.onMessage(new MessageRemovedUpdate(domainSessionId, metadata));
        		}
    		}
    			
    	   if( acceptMessage(msg) ) {
    	       
    	        synchronized(displayList) {
    	            displayList.add(metadata);
    	            
    	            synchronized(metadataToMessage) {
    	                metadataToMessage.put(metadata, msg);
    	            }
    	        }
    			
    			if(msg instanceof DomainSessionMessage) {
    			    domainSessionId = ((DomainSessionMessage) msg).getDomainSessionId();
    			}
    			
    			updateHandler.onMessage(new MessageReceivedUpdate(domainSessionId, metadata));
    		}
    	}
	}
	
	@Override
	public void filterChanged(final FilterChangeEvent event) {	
		rebuildDisplayList();
	}
	
	/**
	 * Sends a listening changed update that configures the message panel to update the 
	 * listening status
	 * 
	 * @param domainSessionId the domain session id of the monitor 
	 * @param listening the value indicating whether the monitor is listening for messages or not
	 */
	public void sendListeningStatusUpdate(Integer domainSessionId, boolean listening) {
	    updateHandler.onMessage(new MessageListenChangedUpdate(domainSessionId, listening));
	}
	
	/**
     * Sends a header status update that configures the message panel to update the 
     * advanced header value
     * 
     * @param domainSessionId the domain session id of the monitor 
     * @param advancedHeader the value indicating whether the message panel is displaying advanced information
     */
    public void sendMessageHeaderStatusUpdate(Integer domainSessionId, boolean advancedHeader) {
        updateHandler.onMessage(new MessageHeaderStatusUpdate(domainSessionId, advancedHeader));
    }

	/**
	 * Sets the display filter for this list model. 
	 * 
	 * @param filter the filter to apply to the list model. Cannot be null.
	 */
	void setMessageDisplayFilter(MessageViewFilter filter) {
		messageDisplayFilter = filter;
		filter.addFilterChangeListener(this);
		rebuildDisplayList();
	}	
	
	/**
	 * Extracts the minimal amount of metadata needed to shown a message in the message list on the client
	 * 
	 * @param message the message to get the metadata for. Cannot be null.
	 * @return the message's metadata. Will not be null.
	 */
	private MessageEntryMetadata getMetadata(Message message) {
	    
	    if(message == null) {
	        throw new IllegalArgumentException();
	    }
	    
	    return new MessageEntryMetadata(message.getMessageType(), message.getTimeStamp(), message.getSequenceNumber(), message.getSenderAddress());
	}

	/**
	 * Extracts all of the information needed to display a message's contents on the client
	 * 
	 * @param message the metadata of the message whose data is being displayed. Cannot be null.
	 * @return the display data for the message. Can be null if the message has been removed.
	 */
    public MessageDisplayData getDisplayData(MessageEntryMetadata message) {
        
        Message fullMessage = metadataToMessage.get(message);
        if(fullMessage == null) {
            
            /* 
             * The client likely made this request just before the server removed the message, so simply 
             * return null rather than throwing an error, since we can't really prevent delays between 
             * the client and server
             */
            return null;
        }
        
        if (advancedHeader) {
            message.setSenderAddress(fullMessage.getSenderAddress());
            message.setSenderModuleType(fullMessage.getSenderModuleType());
            message.setDestinationAddress(fullMessage.getDestinationQueueName());
            message.setNeedsACK(fullMessage.isReplyMessage());
            message.setSourceEventId(fullMessage.getSourceEventId());
        }
        
        /* Get the user session, if there is one */
        UserSession session = null;
        if(fullMessage instanceof UserSessionMessage) {
            session = ((UserSessionMessage) fullMessage).getUserSession();
        }
        
        /* Get the payload as a JSON string*/
        MessageTypeEnum type = fullMessage.getMessageType();
        Object payload = fullMessage.getPayload();
        
        try {
            Class<?> protoClass = ProtoMapper.getInstance().getObjectClass(type);
            Class<?> codecClass = ProtoMapper.getInstance().getCodecClass(type);

            if (protoClass == null || codecClass == null) {
                throw new Exception(
                        "Unable to find a codec class for message of type " + type + ". Is this a new message type?");
            }

            @SuppressWarnings("unchecked")
            AbstractMessage encodedPayload = ((ProtoCodec<AbstractMessage, Object>) codecClass.getConstructor().newInstance()).map(payload);
            String payloadJson = JsonFormat.printer().print(encodedPayload);
        
            return new MessageDisplayData(message, fullMessage.getDestinationQueueName(), payloadJson, session);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to display message data because it could not be encoded", e);
        }
    }
    
    /**
     * Gets the message display filter used by this message list model
     * @return the currently set message view filter
     */
    public MessageViewFilter getMessageDisplayFilter() {
        return messageDisplayFilter;
    }

    @Override
    public void filterChoicesChanged(List<MessageTypeEnum> allChoices, List<MessageTypeEnum> selectedChoices) {
        updateHandler.onMessage(new MessageFilterChoicesUpdate(domainSessionId, allChoices, selectedChoices));
    }

    /**
     * Re-sends all message updates currently stored in memory to the client so that the
     * client can completely refresh its information
     */
    public void refresh() {
        
        rebuildDisplayList();
        
        messageDisplayFilter.onFilterChoicesChanged();
        
        sendListeningStatusUpdate(domainSessionId, listening);
        
        sendMessageHeaderStatusUpdate(domainSessionId, advancedHeader);
    }
}
