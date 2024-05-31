/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.spl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.MessageEncodingTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.TopicMessageClient;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.MessageUtil;
import mil.arl.gift.net.api.message.RawMessageHandler;

/**
 * Keeps track of each module's status
 * 
 * @author cdettmering
 */
public class ModuleStatusManager {
	
	private static Logger logger = LoggerFactory.getLogger(ModuleStatusManager.class);
	
	/** URL to the ActiveMQ broker */
	private String messageBrokerUri;
	
	/** Maps the module to a boolean representing if the module has finished initializing */
	private Map<ModuleTypeEnum, Boolean> moduleReadyMap;
	
	/** Maps the module to its discovery topic */
	private Map<ModuleTypeEnum, String> moduleToDiscoveryTopic;
	
	/** List of topic clients created to listen for module status messages */
	private List<TopicMessageClient> topicClients;
    
	/** maps a module type to the last module status received */
    private final Map<ModuleTypeEnum, ModuleStatus> modules = new HashMap<>();
    
    /** maps a module type to the epoch time at which the monitoring started for that module */
    private static final Map<ModuleTypeEnum, Long> moduleThreadsStartTime = new HashMap<>();
	
	/**
	 * Creates a new ModuleStatusManager
	 */
	public ModuleStatusManager() {
		moduleReadyMap = new HashMap<ModuleTypeEnum, Boolean>();
		moduleToDiscoveryTopic = new HashMap<ModuleTypeEnum, String>();
		topicClients = new ArrayList<TopicMessageClient>();
		
		moduleToDiscoveryTopic.put(ModuleTypeEnum.DOMAIN_MODULE, SubjectUtil.DOMAIN_DISCOVERY_TOPIC);
		moduleToDiscoveryTopic.put(ModuleTypeEnum.GATEWAY_MODULE, SubjectUtil.GATEWAY_DISCOVERY_TOPIC);
		moduleToDiscoveryTopic.put(ModuleTypeEnum.LEARNER_MODULE, SubjectUtil.LEARNER_DISCOVERY_TOPIC);
		moduleToDiscoveryTopic.put(ModuleTypeEnum.LMS_MODULE, SubjectUtil.LMS_DISCOVERY_TOPIC);
		moduleToDiscoveryTopic.put(ModuleTypeEnum.PEDAGOGICAL_MODULE, SubjectUtil.PED_DISCOVERY_TOPIC);
		moduleToDiscoveryTopic.put(ModuleTypeEnum.SENSOR_MODULE, SubjectUtil.SENSOR_DISCOVERY_TOPIC);
		moduleToDiscoveryTopic.put(ModuleTypeEnum.TUTOR_MODULE, SubjectUtil.TUTOR_DISCOVERY_TOPIC);
		moduleToDiscoveryTopic.put(ModuleTypeEnum.UMS_MODULE, SubjectUtil.UMS_DISCOVERY_TOPIC);
	}
	
	/**
	 * Sets the URI to the ActiveMQ message broker.
	 * @param uri URI to the ActiveMQ message broker.
	 */
	public void setMessageBrokerUri(String uri) {
		messageBrokerUri = uri;
	}
    
    /**
     * Gets the collection of modules known to this class
     *
     * @return Collection<ModuleStatus> The collection of modules known to this class
     */
    public Collection<ModuleStatus> getModules() {
        
        return modules.values();
    }
	
	/**
	 * Adds a module to the manager. When the module is added the state of the module
	 * will be tracked.
	 * @param module The module to add.
	 */
	public void addModule(ModuleTypeEnum module) {
	    
	    synchronized (moduleReadyMap) {
            
	        if(!moduleReadyMap.containsKey(module)) {
	            moduleReadyMap.put(module, false);
	            createSubjectTopicClient(module);
	        }
        }

	}
	
	/**
	 * Checks if each module being tracked has been started and initialized.
	 * @return True if each module is ready, false otherwise.
	 */
	public boolean isSystemReady() {
	    
	    synchronized (moduleReadyMap) {
	        
    		Set<ModuleTypeEnum> keySet = moduleReadyMap.keySet();
    		Iterator<ModuleTypeEnum> it = keySet.iterator();
    		while(it.hasNext()) {
    			if(!moduleReadyMap.get(it.next())) {
    				return false;
    			}
    		}
    		return true;
	    }
	}
	
	/**
	 * Return the current list of modules not ready (i.e. they aren't sending module status messages).
	 * 
	 * @return List<ModuleTypeEnum> the module types not ready.  Can be empty but not null.
	 */
	public List<ModuleTypeEnum> getModulesNotReady(){
	    
	    List<ModuleTypeEnum> notReady = new ArrayList<>();
	    
	    synchronized (moduleReadyMap) {
	        
            Set<ModuleTypeEnum> keySet = moduleReadyMap.keySet();
            Iterator<ModuleTypeEnum> it = keySet.iterator();
            while(it.hasNext()) {
                ModuleTypeEnum mType = it.next();
                if(!moduleReadyMap.get(mType)) {
                    notReady.add(mType);
                }
            }
	    }
        
        return notReady;
	}

	/**
	 * Handles ModuleStatus messages.
	 * @param status The ModuleStatus to handle.
	 */
	private void handleModuleStatus(ModuleStatus status) {
	    
	    synchronized (moduleReadyMap) {
	    
    		if(moduleReadyMap.containsKey(status.getModuleType())) {
    		    //this is a module of interest
    		    
    		    if(!moduleReadyMap.get(status.getModuleType())){
    		        //the module was not previously known to have a status
    		        
    		        if(logger.isInfoEnabled()){
    		            logger.info(status.getModuleName() + " is now ready.  It took " + (System.currentTimeMillis() - moduleThreadsStartTime.get(status.getModuleType())) + " ms to start.");
    		        }
    		        
    		        moduleReadyMap.put(status.getModuleType(), true);
    		    }
    		}
		
	    }
		
        modules.put(status.getModuleType(), status);
	}
    
    /**
     * Disconnects the manager from ActiveMQ
     */
    public void close() {

        for (TopicMessageClient topicClient : topicClients) {

            topicClient.disconnect(false);
        }

        synchronized (moduleReadyMap) {
            moduleReadyMap.clear();
        }

        moduleToDiscoveryTopic.clear();

        topicClients.clear();

        modules.clear();
    }
	
	/**
	 * Creates a subject topic client for the given module type. This topic client
	 * will then begin receiving ModuleStatus messages from module.
	 * @param module The module to create the topic client for.
	 */
	private void createSubjectTopicClient(ModuleTypeEnum module) {
	    
	    //used to determine how long it takes to hear from the module the first time
	    moduleThreadsStartTime.put(module, System.currentTimeMillis());
	    
		TopicMessageClient tClient = new TopicMessageClient(messageBrokerUri, moduleToDiscoveryTopic.get(module));
		RawMessageHandler handler = new RawMessageHandler() {

			@Override
			public boolean processMessage(String msg,
					MessageEncodingTypeEnum encodingType) {
				try {
					Message message = MessageUtil.getMessageFromString(msg, encodingType);
					ModuleStatus status = (ModuleStatus) message.getPayload();
					handleModuleStatus(status);
					return true;
				} catch(@SuppressWarnings("unused") ClassCastException e) {
					return false;	
				}
			}
		};
		
		tClient.setMessageHandler(handler);
		try {
			if(tClient.connect()) {
				topicClients.add(tClient);
			}
		} catch (Exception e) {
			logger.error("Caught exception while trying to create topic client to " + module, e);
		}
	}
}
