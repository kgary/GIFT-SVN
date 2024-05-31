/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageDisplayData;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageEntryMetadata;
import mil.arl.gift.tools.monitor.DomainSessionMonitorListener;
import mil.arl.gift.tools.monitor.DomainSessionStatusListener;

/**
 * A manager that maintains the message data used by message panels in the client and pushes
 * updates to them as messages are received.
 * 
 * @author nroberts
 */
public class PanelManager implements ModuleStatusListener, 
                                     DomainSessionStatusListener,
                                     MonitorMessageListener,
                                     DomainSessionMonitorListener {
    
    /** The logger to use to log information for this class */
    private static Logger logger = LoggerFactory.getLogger(PanelManager.class);
    
    /** The list model of messages that are not part of domain sessions (i.e. global/system messages) */
    private final MessageListModel globalMessageListModel;
    
    /** A mapping from each domain session's ID to the server-side panel used to represent it */
    private final Map<Integer, MessageViewModel> domainSessionIdToMessagePanel = new HashMap<>();
    
    /** A mapping from each domain session's ID to the model used to track its list of messages */
    private final Map<Integer, MessageListModel> domainSessionIdToMessageList  = new HashMap<>();

    /** A handler used to push message updates to the appropriate clients */
    private MessageUpdateHandler updateHandler;
    
    /**
     * Creates a new server-side manager for message panels that uses the given handler
     * to push updates to them
     * 
     * @param updateHandler the handler to use to send updates.
     */
    public PanelManager(MessageUpdateHandler updateHandler) {
        
        this.updateHandler = updateHandler;
        
        globalMessageListModel = new MessageListModel(updateHandler);
        
        setupGlobalMessageViewPanel();
    }
    
    /**
     * Create a new panel for the domain session ID
     * @param domainSessionId is the ID for the domain session we're creating the panel for.
     */
    void createPanel(final int domainSessionId) {
        
        if(domainSessionIdToMessagePanel.get(domainSessionId) != null){
            
            /* A panel already exists for this domain session, so return*/
            return;
        }
                
        MessageListModel  messageListModel = domainSessionIdToMessageList.get(domainSessionId);
        if(messageListModel == null){
            //MH: preventing null pointer here that needs to be debugged further
            throw new IllegalStateException("Unable to find the message list model for domain session "+domainSessionId+".");
        }
        MessageViewFilter messageViewFilter = new DomainSessionMessageViewFilter();
        messageListModel.setMessageDisplayFilter(messageViewFilter);
        
        MessageViewModel messageViewPanel = new MessageViewModel();              
        
        //Setting filter causes others to be notified, so set the filter last
        messageViewPanel.setFilterListModel(messageViewFilter);
        
        //save panel in a hashmap in case I need to remove it later
        domainSessionIdToMessagePanel.put(domainSessionId, messageViewPanel);
        
        logger.info("Created domain session message panel for domain session id "+domainSessionId);
    }
    
    private void setupGlobalMessageViewPanel() {
        
        //filters system messages to populate message type list
        MessageViewFilter messageViewFilter = new SystemMessageViewFilter();
        
        //maintains list of system messages because of the filter
        globalMessageListModel.setMessageDisplayFilter(messageViewFilter);
    }
    
    @Override
    public void handleMessage(final Message msg) {
        
        if(msg == null) {
        
            System.err.println("WARNING: PanelManager::handleMessage got a null Message");
            
        } else if(msg instanceof DomainSessionMessage) {
        
            final DomainSessionMessage domainSessionMsg = (DomainSessionMessage)msg;
            
            final Integer domainSessionId = domainSessionMsg.getDomainSessionId();
            
            MessageListModel messageList;
        
            synchronized(domainSessionIdToMessageList) {
                
                messageList = domainSessionIdToMessageList.get(domainSessionId);
            }
            
            //post the message to the message list for domain session ID
            if(messageList != null) {
                messageList.postMessage(domainSessionMsg);
                
            } else {
                //don't care because this could mean that this message came in after the domain session was closed
                //Leaving empty else here and this comment just in case someone thinks we should log this event in the monitor log file.
            }
            
        }else { //it's not a Domain Session Message therefore it's a System Message
               //so use the globalModuleListModel
            
            globalMessageListModel.postMessage(msg);
        }
    }
    
    /**
     * Removes the panel for the specified domain session (NOT YET IMPLEMENTED!)
     * @param domainSessionId the id of the domain session for which we are to remove the panel.
     */
    void removePanel(final int domainSessionId) {       
        domainSessionIdToMessagePanel.remove(domainSessionId);
        synchronized(domainSessionIdToMessageList) {
            if( domainSessionIdToMessageList.containsKey(domainSessionId) ) {
                
                logger.info("Removing inactive domain session with id = "+domainSessionId+" from message list map.");
                domainSessionIdToMessageList.remove(domainSessionId);
            }
        }
    }   

    @Override
    public void monitorDomainSession(int domainSessionId) {
        createPanel(domainSessionId);   
    }

    @Override
    public void ignoreDomainSession(int domainSessionId) {
        removePanel(domainSessionId);
    }

    @Override
    public void domainSessionActive(DomainSession domainSession) {
                  
        synchronized(domainSessionIdToMessageList) {
            
            if( !domainSessionIdToMessageList.containsKey(domainSession.getDomainSessionId()) ) {
                
                logger.info("Adding active domain session with id = "+domainSession.getDomainSessionId()+" with a new message list model.");
                domainSessionIdToMessageList.put(domainSession.getDomainSessionId(), new MessageListModel(updateHandler));
            }
        }
    }

    @Override
    public void domainSessionInactive(DomainSession domainSession) {
       synchronized(domainSessionIdToMessageList) {
            
            if( domainSessionIdToMessageList.containsKey(domainSession.getDomainSessionId()) ) {
                
                logger.info("Removing inactive domain session with id = "+domainSession.getDomainSessionId()+" from message list map.");
                domainSessionIdToMessageList.remove(domainSession.getDomainSessionId());
            }
        }
    }

    @Override
    public void moduleStatusAdded(long sentTime, ModuleStatus status) {
        //ignore for now
    }

    @Override
    public void moduleStatusChanged(long sentTime, ModuleStatus status) {
        //ignore for now
    }

    @Override
    public void moduleStatusRemoved(StatusReceivedInfo status) {
        //ignore for now
    }

    /**
     * Extracts all of the information needed to display a message's contents on the client
     * 
     * @param message the metadata of the message whose data is being displayed. Cannot be null.
     * @return the display data for the message. Cannot be null.
     */
    public MessageDisplayData getDisplayData(Integer domainSessionId, MessageEntryMetadata message) {
        
        if(domainSessionId == null) {
            return globalMessageListModel.getDisplayData(message);
            
        } else {
            MessageListModel model = domainSessionIdToMessageList.get(domainSessionId);
            if(model != null) {
                return model.getDisplayData(message);
            }
        }
        
        return null;
    }
    
    /**
     * Sets the listening state of the MessageListModel
     * 
     * @param domainSessionId the domain session id for the active session
     * @param listening the listening state
     */
    public void setListening(Integer domainSessionId, boolean listening) {
        
        /* If listening state was associated with a domain session, get the associated message list model 
         * and update there. */
        if (domainSessionId != null) {
            MessageListModel messageListModel = domainSessionIdToMessageList.get(domainSessionId);
            messageListModel.setListening(listening);
            messageListModel.sendListeningStatusUpdate(domainSessionId, listening);
            domainSessionIdToMessageList.put(domainSessionId, messageListModel);
        } else {
            /* System message listening update, use the global list model */
            globalMessageListModel.setListening(listening);       
            globalMessageListModel.sendListeningStatusUpdate(domainSessionId, listening);
        } 
    }
    
    /**
     * Sets the header state of the MessageListModel
     * 
     * @param domainSessionId the domain session id for the active session
     * @param advancedHeader the header state 
     */
    public void setAdvancedHeader(Integer domainSessionId, boolean advancedHeader) {
        
        /* If advanced header state was associated with a domain session, get the associated message list model 
         * and update there. */
        if (domainSessionId != null) {
            MessageListModel messageListModel = domainSessionIdToMessageList.get(domainSessionId);
            messageListModel.setAdvancedHeader(advancedHeader);  
            messageListModel.sendMessageHeaderStatusUpdate(domainSessionId, advancedHeader);
            domainSessionIdToMessageList.put(domainSessionId, messageListModel);
        } else {
            /* System message listening update, use the global list model */
            globalMessageListModel.setAdvancedHeader(advancedHeader);       
            globalMessageListModel.sendMessageHeaderStatusUpdate(domainSessionId, advancedHeader);
        }  
    }

    /**
     * Updates the filter for the given domain session to display entity state messages containing
     * the given entity marking text
     * 
     * @param domainSessionId the ID of the domain session whose messages should be filtered.
     * @param entityMarking the entity marking to filter by. Cannot be null.
     */
    public void filterEntity(int domainSessionId, String entityMarking) {
        MessageListModel view = domainSessionIdToMessageList.get(domainSessionId);
        
        if(view != null && view.getMessageDisplayFilter()!= null) {
            view.getMessageDisplayFilter().setEntityStateURNMarkingFilter(entityMarking);
        }
    }
    
    /**
     * Gets whether there is a client displaying the domain session with the given ID
     * that should receive messages for that domain session
     * 
     * @param domainSessionId the ID of the domain session to check for. Cannot be null.
     * @return whether a client is displaying the domain session
     */
    public boolean hasPanel(Integer domainSessionId) {
        
        if(domainSessionId == null) {
            return globalMessageListModel != null;
            
        } else {
            return domainSessionIdToMessagePanel.containsKey(domainSessionId);
        }
    }
    
    /**
     * Removes the panel from panel manager when a user attempts to stop watching the panel
     * @param domainSessionId the id of the domain session associated with the panel to remove
     */
    public void removePanel(Integer domainSessionId) {
        if(domainSessionId != null) {
            domainSessionIdToMessagePanel.remove(domainSessionId);
        }
    }

    /**
     * Sets the message types that the filter for the given domain session should display
     * 
     * @param domainSessionId the ID of the domain session whose filter is being modified. Can be null.
     * @param selectedChoices the selected message type choices. Cannot be null.
     */
    public void setFilterChoices(Integer domainSessionId, Set<MessageTypeEnum> selectedChoices) {
        
        if (domainSessionId != null) {
            MessageListModel messageListModel = domainSessionIdToMessageList.get(domainSessionId);
            if(messageListModel.getMessageDisplayFilter() != null) {
                messageListModel.getMessageDisplayFilter().setFilterChoices(selectedChoices);
            }
            
        } else {
            if(globalMessageListModel.getMessageDisplayFilter() != null) {
                globalMessageListModel.getMessageDisplayFilter().setFilterChoices(selectedChoices);
            }
        }  
    }

    /**
     * Re-sends all message updates currently stored in memory for the given 
     * domain session to the client so that the client can completely refresh 
     * its information
     * 
     * @param domainSessionId the ID of the domain session to refresh. Can be null.
     */
    public void refresh(Integer domainSessionId) {
        
        if (domainSessionId != null) {
            MessageListModel messageListModel = domainSessionIdToMessageList.get(domainSessionId);
            if(messageListModel.getMessageDisplayFilter() != null) {
                messageListModel.refresh();
            }
            
        } else {
            if(globalMessageListModel.getMessageDisplayFilter() != null) {
                globalMessageListModel.refresh();
            }
        }  
    }
}
