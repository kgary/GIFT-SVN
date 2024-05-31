/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to assemble MessageViewPanels and their underlying data stores.
 * @author cragusa
 *
 */
public class PanelManager implements ModuleStatusListener, 
                                     DomainSessionStatusListener,
                                     MonitorMessageListener,
                                     DomainSessionMonitorListener {
    
    private static Logger logger = LoggerFactory.getLogger(PanelManager.class);
	
	//TODO: maybe I can simply use the ModuleStatusModel?
	private final ModuleListModel  globalModuleListModel  = new ModuleListModel();	
	private final MessageListModel globalMessageListModel = new MessageListModel();
	private final MessageViewPanel globalMessageViewPanel = new MessageViewPanel(-1);  //-1 denotes not a domain session panel
    private final MessageStatsModel globalMessageStatsModel = new MessageStatsModel();
    private final MessagePanelContainer globalMessagePanelContainer = new MessagePanelContainer(globalMessageViewPanel);
	
	private final Map<Integer, Integer> domainSessionIdToPanelId = new HashMap<>();
	
	private void setupGlobalMessageViewPanel() {
		
	    //filters system messages to populate message type list
	    MessageViewFilter messageViewFilter = new SystemMessageViewFilter();
	    
	    //maintains list of system messages because of the filter
		globalMessageListModel.setMessageDisplayFilter(messageViewFilter);
		
		//provide panel access to the list of message types
		globalMessageViewPanel.setMessageListModel(globalMessageListModel);	
		
		//if a message type is selected, allow the filter to update (which then updates the ???)
		globalMessageViewPanel.setListSelectionListener(messageViewFilter);
		
		//provide panel access to the list of modules (message sender and receivers discovered)
		globalMessageViewPanel.setModuleListModel(globalModuleListModel);	
		
		//provide panel access to the list of message types
		globalMessageViewPanel.setFilterListModel(messageViewFilter);
		
		//allow the panel to receive message statistic updates - so the panel can display values
        globalMessageStatsModel.addMessageStatisticsUpdateListener(globalMessagePanelContainer);
        
		MonitorModule.getInstance().addPanel("System Msgs", globalMessagePanelContainer);	
		
		//allow the panel to be notified of filter selection changes
		messageViewFilter.addFilterChangeListener(globalMessageViewPanel);
	}
	
		
	/**
	 * Private constructor for singleton pattern.
	 */
	private PanelManager() {		
		setupGlobalMessageViewPanel();
	}
	
	/** The one and only PanelManager instance */
	private static final PanelManager instance = new PanelManager();
	
	private final Map<Integer, MessageViewPanel> domainSessionIdToMessagePanel = new HashMap<>();
	private final Map<Integer, MessageListModel> domainSessionIdToMessageList  = new HashMap<>();
	private final Map<Integer, ModuleListModel>  domainSessionIdToModuleList   = new HashMap<>();
    private final Map<Integer, MessageStatsModel> domainSessionIdToMessageStatsModel = new HashMap<>();
    private final Map<Integer, MessagePanelContainer> domainSessionIdToMessagePanelContainer = new HashMap<>();

	/**
	 * Singleton pattern
	 * @return the one-and-only instance of PanelManager
	 */
	static PanelManager getInstance() {
		return instance;		
	}
	
	/**
	 * Create a new panel for the domain session ID
	 * @param domainSessionId is the ID for the domain session we're creating the panel for.
	 */
	void createPanel(final int domainSessionId) {
				
		MessageListModel  messageListModel = domainSessionIdToMessageList.get(domainSessionId);
		if(messageListModel == null){
		    //MH: preventing null pointer here that needs to be debugged further
		    JOptionPaneUtil.showConfirmDialog("Unable to find the message list model for domain session "+domainSessionId+".  Please debug the state of the Monitor module to determine how this might have happened.", 
		            "Unable to monitor session", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
		    return;
		}
		ModuleListModel   moduleListModel  = domainSessionIdToModuleList.get(domainSessionId);
		MessageViewFilter messageViewFilter = new DomainSessionMessageViewFilter();
		messageListModel.setMessageDisplayFilter(messageViewFilter);		
		
		MessageViewPanel messageViewPanel = new MessageViewPanel(domainSessionId);		
		messageViewPanel.setMessageListModel(messageListModel);			
		messageViewPanel.setListSelectionListener(messageViewFilter);
		messageViewPanel.setModuleListModel(moduleListModel);
		
		messageViewFilter.addFilterChangeListener(messageViewPanel);
		
		//Setting filter causes others to be notified, so set the filter last
		messageViewPanel.setFilterListModel(messageViewFilter);
		
		//save panel in a hashmap in case I need to remove it later
		domainSessionIdToMessagePanel.put(domainSessionId, messageViewPanel);
        
        MessageStatsModel messageStatsModel;
        
        synchronized (domainSessionIdToMessageStatsModel) {
            
            messageStatsModel = domainSessionIdToMessageStatsModel.get(domainSessionId);
        }
        
        MessagePanelContainer messagePanelContainer = new MessagePanelContainer(messageViewPanel);
        
        synchronized(domainSessionIdToMessagePanelContainer) {
            
            domainSessionIdToMessagePanelContainer.put(domainSessionId, messagePanelContainer);
        }
        
        if(messageStatsModel != null) {
            messageStatsModel.addMessageStatisticsUpdateListener(messagePanelContainer);
        }
		
		int panelId = MonitorModule.getInstance().addPanel("Domain Session " + Integer.toString(domainSessionId) + " Msgs", messagePanelContainer);
		
		domainSessionIdToPanelId.put(domainSessionId, panelId);
		
		logger.info("Created domain session message panel for domain session id "+domainSessionId);
	}
	
	/**
	 * Removes the panel for the specified domain session (NOT YET IMPLEMENTED!)
	 * @param domainSessionId the id of the domain session for which we are to remove the panel.
	 */
	void removePanel(final int domainSessionId) {		
       
        Integer panelId = domainSessionIdToPanelId.get(domainSessionId);
        
        if(panelId != null) {
            
            //TODO: What else do I need to do to kill the panel?
            //maybe there are timers and/or listeners that need to be destroyed?
            domainSessionIdToMessagePanel.remove(domainSessionId);
            
            MessageStatsModel messageStatsModel;

            synchronized (domainSessionIdToMessageStatsModel) {

                messageStatsModel = domainSessionIdToMessageStatsModel.get(domainSessionId);
            }

            synchronized (domainSessionIdToMessagePanelContainer) {

                if (messageStatsModel != null) {
                    messageStatsModel.removeMessageStatisticsUpdateListener(domainSessionIdToMessagePanelContainer.get(domainSessionId));
                }

                domainSessionIdToMessagePanelContainer.remove(domainSessionId);
            }

            MonitorModule.getInstance().removePanel(panelId.intValue());
            domainSessionIdToPanelId.remove(domainSessionId);
        }
        else {
            
            System.out.println("PanelID was null");
        }
	}	

	/** self-described helper method */
	private void ensureListModelContainsElement(final ModuleListModel listModel, final String moduleName) {

	    Runnable runnable = new Runnable() {

            @Override
	        public void run() {
	            
	            if(listModel != null ) {

	                if(moduleName != null) {

	                    if(!listModel.contains(moduleName)) {

	                        listModel.addElement(moduleName);
	                    }
	                }
	                else {
	                    System.err.println("WARNING: PanelManager::ensureListModelContainsElement tried to add null moduleName to listModel");
	                }
	            }
	            else {		
	                System.err.println("WARNING: PanelManager::ensureListModelContainsElement tried to add moduleName to null listModel");
	            }
	        }
	    };
	    
	    SwingUtilities.invokeLater(runnable);
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
				
				ModuleListModel moduleListModel;
				
				synchronized(domainSessionIdToModuleList) {
					
					moduleListModel = domainSessionIdToModuleList.get(domainSessionId);
				}
				
				String destQueueName = domainSessionMsg.getDestinationQueueName();
				ensureListModelContainsElement(moduleListModel, destQueueName);
				
				String senderQueueName = domainSessionMsg.getSenderAddress();
				ensureListModelContainsElement(moduleListModel, senderQueueName);
				
				//IMPORTANT:  must post message *after* taking care of modules
				messageList.postMessage(domainSessionMsg);
				
			} else {
				//don't care because this could mean that this message came in after the domain session was closed
			    //Leaving empty else here and this comment just in case someone thinks we should log this event in the monitor log file.
            }

            MessageStatsModel moduleStatsModel;

            synchronized (domainSessionIdToMessageStatsModel) {

                moduleStatsModel = domainSessionIdToMessageStatsModel.get(domainSessionId);
            }
            
            if (moduleStatsModel != null) {
                
                moduleStatsModel.handleMessage(msg);
            }
            
        }else { //it's not a Domain Session Message therefore it's a System Message
			   //so use the globalModuleListModel
			
			//TODO: keep the globalModuleListModel up-to-date by using the AggregateStatusMessage?
			ensureListModelContainsElement(globalModuleListModel, msg.getDestinationQueueName());
			ensureListModelContainsElement(globalModuleListModel, msg.getSenderAddress());

			globalMessageListModel.postMessage(msg);
			
            
            globalMessageStatsModel.handleMessage(msg);
		}
    }
	
	
    //------------------------------------------------------------------------------------------
    //           DomainSessionStatusListener
    //------------------------------------------------------------------------------------------
    @Override
    public void domainSessionActive(DomainSession domainSession) {
	   		
		//TODO: in the future only add the MessageLists when needed (lazily)		
		synchronized(domainSessionIdToMessageList) {
			
			if( !domainSessionIdToMessageList.containsKey(domainSession.getDomainSessionId()) ) {
				
			    logger.info("Adding active domain session with id = "+domainSession.getDomainSessionId()+" with a new message list model.");
				domainSessionIdToMessageList.put(domainSession.getDomainSessionId(), new MessageListModel());
			}
		}
		
		synchronized(domainSessionIdToModuleList) {
			
			if(!domainSessionIdToModuleList.containsKey(domainSession.getDomainSessionId())) {
				
	             logger.info("Adding active domain session with id = "+domainSession.getDomainSessionId()+" with a new module list model.");
				domainSessionIdToModuleList.put(domainSession.getDomainSessionId(), new ModuleListModel());
			}
		}

        synchronized (domainSessionIdToMessageStatsModel) {
            
            if (!domainSessionIdToMessageStatsModel.containsKey(domainSession.getDomainSessionId())) {

                domainSessionIdToMessageStatsModel.put(domainSession.getDomainSessionId(), new MessageStatsModel());
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
        
        synchronized(domainSessionIdToModuleList) {
            
            if(domainSessionIdToModuleList.containsKey(domainSession.getDomainSessionId())) {
                
                logger.info("Removing inactive domain session with id = "+domainSession.getDomainSessionId()+" from module list map.");
                domainSessionIdToModuleList.remove(domainSession.getDomainSessionId());
            }
        }
        
        synchronized (domainSessionIdToMessageStatsModel) {
            
            if (!domainSessionIdToMessageStatsModel.containsKey(domainSession.getDomainSessionId())) {

                domainSessionIdToMessageStatsModel.remove(domainSession.getDomainSessionId());
            }
        }
    }
    //------------------------------------------------------------------------------------------
	
	
	
	//------------------------------------------------------------------------------------------
	//           ModuleStatusModelListener
	//------------------------------------------------------------------------------------------
	@Override
	public void moduleStatusAdded(long sentTime, final ModuleStatus status) {
		//ignore for now
	}

	@Override
	public void moduleStatusChanged(long sentTime, final ModuleStatus status) {
		//ignore for now
	}

	@Override
	public void moduleStatusRemoved(final StatusReceivedInfo status) {
		//ignore for now
	}
	//------------------------------------------------------------------------------------------
	
	
	//------------------------------------------------------------------------------------------
    //           DomainSessionMonitorListener
    //------------------------------------------------------------------------------------------	
	@Override
	public void monitorDomainSession(final int domainSessionId) {
		createPanel(domainSessionId);		
	}

	@Override
	public void ignoreDomainSession(final int domainSessionId) {
		removePanel(domainSessionId);
	}
    //------------------------------------------------------------------------------------------   
	
	
	protected class ModuleListModel extends DefaultListModel<String> {	
    	private static final long serialVersionUID = 1L;	
    }
}
