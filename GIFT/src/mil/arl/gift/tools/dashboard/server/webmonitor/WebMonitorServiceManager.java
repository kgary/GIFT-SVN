/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.server.webmonitor;

import static mil.arl.gift.common.util.StringUtils.isBlank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.gwt.server.BrowserWebSession;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.util.Util;
import mil.arl.gift.tools.dashboard.server.BrowserSessionListener;
import mil.arl.gift.tools.dashboard.server.DashboardBrowserWebSession;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.tools.dashboard.server.UserSessionManager;
import mil.arl.gift.tools.dashboard.server.WebMonitorModule;
import mil.arl.gift.tools.dashboard.server.webmonitor.message.MessageUpdateHandler;
import mil.arl.gift.tools.dashboard.server.webmonitor.message.MonitorMessageListener;
import mil.arl.gift.tools.dashboard.server.webmonitor.message.PanelManager;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageDisplayData;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.MessageEntryMetadata;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.ModuleStatusUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.UsersStatusUpdate;
import mil.arl.gift.tools.dashboard.shared.messages.monitor.WebMonitorUpdate;
import mil.arl.gift.tools.dashboard.shared.rpcs.WebMonitorStatus;
import mil.arl.gift.tools.monitor.DomainSessionStatusListener;
import mil.arl.gift.tools.monitor.UserStatusListener;
import mil.arl.gift.tools.remote.HostInfo;
import mil.arl.gift.tools.remote.LaunchConstants;

/**
 * A manager that manages the server-side operations needed to interact with the web monitor using
 * its UI controls on the client.
 * 
 * @author nroberts
 */
public class WebMonitorServiceManager implements BrowserSessionListener, ModuleStatusListener, 
        UserStatusListener, DomainSessionStatusListener, MonitorMessageListener, MessageUpdateHandler{
    
    /** The logger for the class */
    private static Logger logger = LoggerFactory.getLogger(WebMonitorServiceManager.class);
    
    /** A mapping from each module type to the queue names associated with it */
    private Map<ModuleTypeEnum, List<String>> moduleToQueueNames = new HashMap<>();

    /** Instance of the manager class used to manage user/browser sessions. */
    private UserSessionManager userSessionManager = null;
    
    /** The singleton instance of this class */
    private static WebMonitorServiceManager instance = null;
    
    /** The IP address to use when making remote service requests */
    private String ipAddress = Util.getLocalHostAddress().getHostAddress();
    
    /** A set of host info describing the local host (i.e. the machine the GAS is running on) */
    private final HostInfo LOCAL_HOST_INFO = new HostInfo(HostInfo.HostType.IOS_EOS, "localhost", "127.0.0.1", 0);
    
    /** The host to use when making remote service requests */
    private HostInfo targetHost = LOCAL_HOST_INFO;
    
    /** A mapping from every active user session's ID to its associated session */
    private Map<Integer, UserSession> userSessions = new HashMap<>();
    
    /** A mapping from every active domain session's ID to its associated session */
    private Map<Integer, DomainSession> activeDomainSessions = new HashMap<>();
    
    /** A server-side manager for the data passed to message panels */
    private PanelManager panelManager = new PanelManager(this);
    
    /** A mapping from each browser session ID to the monitor service associated with it. Used
     * to broadcast updates to multiple browser sessions. */
    private Map<String, WebMonitorService> browserSessionToMonitorService = new ConcurrentHashMap<>();
    
    /**
     * Creates a new web monitor service manager that uses the given user session manager
     * to look up user session information to register browser sessions
     * 
     * @param userSessionManager the user session manager used in registering browser
     * sessions. Cannot be null.
     */
    private WebMonitorServiceManager(UserSessionManager userSessionManager) {
        this.userSessionManager = userSessionManager;

        this.userSessionManager.addStatusListener(this);
        
        /* Start listening for updates from the web monitor module */
        ModuleStatusMonitor.getInstance().addListener(this);
        WebMonitorModule.getInstance().registerUserStatusListener(this);
        WebMonitorModule.getInstance().registerDomainSessionStatusListener(this);
        WebMonitorModule.getInstance().registerMessageListener(this);
    }
    
    /**
     * Create an an instance of the singleton
     *
     * @param userSessionManager The manager responsible for handling user sessions.
     */
    public static void createInstance(UserSessionManager userSessionManager) {
        if (instance == null) {
            instance = new WebMonitorServiceManager(userSessionManager);
        }
    }
    
    /**
     * Get an instance of the singleton.  The user must call createInstance() during initialization or this will return null.
     *
     * @return Instance of the singleton.  Can return null if createInstance() has not been called.
     */
    public static WebMonitorServiceManager getInstance() {
        if (instance == null) {
            logger.error("Singleton instance is null.  Please make sure to call createInstance() during initialization prior to using the class.");
        }

        return instance;
    }
    
    /**
     * Deregisters the browser session with the given key from its web monitor service
     * so that it no longer receives web monitor updates.
     * 
     * @param browserSessionKey the key of the browser session to deregister. Cannot be null.
     */
    public void deregisterBrowser(String browserSessionKey) {
        
        DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        
        /* Unwatch all domain sessions watched by the session. If any domain sessions are 
         * no longer watched by any session, the web monitor will ignore them */
        WebMonitorService service = dashSession.getMonitorService();
        for(Integer domainSessionId : service.getWatchedDomainSessions()) {
            unwatchDomainSession(browserSessionKey, domainSessionId);
        }
        
        dashSession.setMonitorService(null);
        
        synchronized(browserSessionToMonitorService) {
            browserSessionToMonitorService.remove(browserSessionKey);
        }
    }
    
    /**
     * Registers the browser session with the given key from to the web monitor service
     * so that it can receive web monitor updates.
     * 
     * @param browserSessionKey the key of the browser session to deregister. Cannot be null.
     * @return the initial status of the web monitor service once the registration is complete.
     * Will not be null.
     */
    public WebMonitorStatus registerBrowser(String browserSessionKey) {
        
        DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        
        WebMonitorService monitorService = new WebMonitorService(dashSession);
        
        dashSession.setMonitorService(monitorService);
        
        synchronized(browserSessionToMonitorService) {
            browserSessionToMonitorService.put(browserSessionKey, monitorService);
        }
        
        return getStatus();
    }
    
    /**
     * Gets the web monitor service associated with the browser session with the given key
     * 
     * @param browserSessionKey the key of the browser session to get the service for. Cannot be null.
     * @return the web monitor service registered to the browser session. Can be null if the
     * browser session has not registered to the web monitor service.
     */
    public WebMonitorService getMonitorService(String browserSessionKey) {
        DashboardBrowserWebSession dashSession = getDashboardSession(browserSessionKey);
        if(dashSession == null) {
           return null;
        }
        
        return dashSession.getMonitorService();
    }

    @Override
    public void onBrowserSessionEnding(BrowserWebSession webSession) {
        
        if (webSession instanceof DashboardBrowserWebSession) {
            DashboardBrowserWebSession dashSession = (DashboardBrowserWebSession) webSession;
            WebMonitorService monitorProperty = dashSession.getMonitorService();

            /* Deregister monitor service */
            if (monitorProperty != null) {
                deregisterBrowser(webSession.getBrowserSessionKey());
            }
        }
    }
    
    /**
     * Gets the {@link DashboardBrowserWebSession} that is identified by the
     * provided browser session key.
     *
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @return The {@link DashboardBrowserWebSession} associated with the
     *         provided browser session key. Can't be null.
     * @throws IllegalArgumentException if the browser session key is blank or
     *         if there is no {@link DashboardBrowserWebSession} associated with
     *         the provided key value.
     */
    private DashboardBrowserWebSession getDashboardSession(String browserSessionKey) {
        return getDashboardSession(browserSessionKey, true);
    }

    /**
     * Gets the {@link DashboardBrowserWebSession} that is identified by the
     * provided browser session key.
     *
     * @param browserSessionKey The {@link String} value of the browser session
     *        key for the browser to fetch. Can't be blank.
     * @param shouldThrow A flag indicating whether the browser session not
     *        being found should result in a thrown exception or a null return
     *        value. True causes an exception to be thrown, false causes null to
     *        be returned.
     * @return The {@link DashboardBrowserWebSession} associated with the
     *         provided browser session key. Can't be null.
     * @throws IllegalArgumentException if shouldThrow is true and if the
     *         browser session key is blank or if there is no
     *         {@link DashboardBrowserWebSession} associated with the provided
     *         key value.
     */
    private DashboardBrowserWebSession getDashboardSession(String browserSessionKey, boolean shouldThrow) {
        if (isBlank(browserSessionKey)) {
            throw new IllegalArgumentException("The parameter 'browserSessionKey' cannot be blank.");
        }

        final BrowserWebSession browserSession = userSessionManager.getBrowserSession(browserSessionKey);
        if (browserSession instanceof DashboardBrowserWebSession) {
            return (DashboardBrowserWebSession) browserSession;
        } else if (shouldThrow) {
            throw new IllegalArgumentException("There is no browser session with the key " + browserSessionKey);
        } else {
            return null;
        }
    }
    
    /**
     * Gets the current status of this web monitor service. This generally includes static information
     * about the service that doesn't change much over its lifetime, as frequent updates are handled
     * separately.
     * 
     * @return the status of the web monitor service. Will not be null.
     */
    public WebMonitorStatus getStatus() {
        return new WebMonitorStatus(WebMonitorModule.getInstance().getMessageBrokerURI().toString().replace("localhost", ipAddress));
    }

    /**
     * Launches the given GIFT modules, starting the Java processes that contain them
     * 
     * @param modules the modules to launch. Cannot be null.
     */
    public void launchModules(List<ModuleTypeEnum> modules) {
        
        if(modules == null) {
            throw new IllegalArgumentException("A least one module must be provided to launch.");
        }
        
        for(ModuleTypeEnum module : modules) {
            
            String launchCommand = null;
            
            if(ModuleTypeEnum.UMS_MODULE.equals(module)) {
                launchCommand = LaunchConstants.LAUNCH_UMS;
                
            } else if(ModuleTypeEnum.LMS_MODULE.equals(module)) {
                launchCommand = LaunchConstants.LAUNCH_LMS;
                
            } else if(ModuleTypeEnum.DOMAIN_MODULE.equals(module)) {
                launchCommand = LaunchConstants.LAUNCH_DOMAIN;
                
            } else if(ModuleTypeEnum.GATEWAY_MODULE.equals(module)) {
                
                /* Avoid launching the Gateway module in server mode */
                if(DeploymentModeEnum.SERVER.equals(DashboardProperties.getInstance().getDeploymentMode())) {
                    continue;
                }
                
                launchCommand = LaunchConstants.LAUNCH_GATEWAY;
                
            } else if(ModuleTypeEnum.LEARNER_MODULE.equals(module)) {
                launchCommand = LaunchConstants.LAUNCH_LEARNER;
                
            } else if(ModuleTypeEnum.PEDAGOGICAL_MODULE.equals(module)) {
                launchCommand = LaunchConstants.LAUNCH_PED;
                
            } else if(ModuleTypeEnum.SENSOR_MODULE.equals(module)) {
                launchCommand = LaunchConstants.LAUNCH_SENSOR;
                
            } else if(ModuleTypeEnum.TUTOR_MODULE.equals(module)) {
                launchCommand = LaunchConstants.LAUNCH_TUTOR;
                
            } else {
                throw new IllegalArgumentException("The module " + module + "is not a known "
                        + "module that can be launched.");
            }
            
            sendRemoteClientCommand(launchCommand, targetHost);
        }
    }
    
    /**
     * Kills the given GIFT modules, stopping the Java processes that contain them
     * 
     * @param modules the modules to kill. Cannot be null.
     * @param targetQueueName the queue name of the specific module target to kill. If null, all
     * instances of the given modules will be killed.
     */
    public void killModules(List<ModuleTypeEnum> modules, String targetQueueName) {
        
        if(modules == null) {
            throw new IllegalArgumentException("A least one module must be provided to kill.");
        }
        
        for(ModuleTypeEnum module : modules) {
            
            List<String> queueNames = moduleToQueueNames.get(module);
            if(queueNames == null) {
                continue;
            }
            
            for (String queueName : queueNames) {
                
                if(targetQueueName != null && !targetQueueName.equals(queueName)) {
                    
                    /* This is not the queue we're looking for, so skip */
                    continue;
                }
                
                killModule(queueName, module);
            }
        }
    }
    
    /**
     * Send a kill module message to the module with the given address.
     *
     * @param queueName name of the module queue to connect too and send the message too
     * @param module the type of module associated with the queue
     */
    private static synchronized void killModule(String queueName, ModuleTypeEnum module) {
        WebMonitorModule.getInstance().killModule(queueName, module);
    }
    
    /**
     * Adds the queue with the given module type to the tracked
     * module queues
     * 
     * @param module the type of module this queue is associated with. Cannot be null.
     * @param queueName the name of the queue. Cannot be null.
     */
    public void addModule(ModuleTypeEnum module, String queueName) {
        
        List<String> moduleQueues = moduleToQueueNames.get(module);
        if(moduleQueues == null) {
            moduleQueues = new ArrayList<>();
            moduleToQueueNames.put(module, moduleQueues);
        }
        
        if (!moduleQueues.contains(queueName)) {
            moduleQueues.add(queueName);
        }
    }
    
    /**
     * Removed the queue with the given module type from the tracked
     * module queues
     * 
     * @param module the type of module this queue is associated with. Cannot be null.
     * @param queueName the name of the queue. Cannot be null.
     */
    public void removeModule(ModuleTypeEnum module, String queueName) {
        
        List<String> moduleQueues = moduleToQueueNames.get(module);
        if(moduleQueues == null) {
            return; //no queue names to remove
        }
        
        if (moduleQueues.contains(queueName)) {
            moduleQueues.remove(queueName);
        }
    }
    
    /**
     * Sends a command to be executed by a remote client
     *
     * @param command The command to send
     * @param hostInfo The info about the remote client
     */
    private void sendRemoteClientCommand(String command, HostInfo hostInfo) {
        sendRemoteClientCommand(command, hostInfo, ipAddress);
    }
    
    /**
     * Sends a command to be executed by a remote client
     *
     * @param command The command to send
     * @param hostInfo The info about the remote client
     * @param targetIp the IP address of the target host of the request
     */
    private static synchronized void sendRemoteClientCommand(String command, HostInfo hostInfo, String targetIp) {
        try {
            String commandResult;
            if (command.contains("localhost")) {
                commandResult = RemoteClientManager.getInstance().sendCommand(command.replace("localhost", targetIp), hostInfo);
            } else {
                commandResult = RemoteClientManager.getInstance().sendCommand(command, hostInfo);
            }
            
            if(logger.isInfoEnabled()) {
                logger.info("Remote command result: " + commandResult);
            }
            
        } catch (IOException ex) {
            logger.error("Failed to execute command on remote client " + hostInfo.toString(), ex);
        }
    }
    
    /**
     * A module of the given type has come online, update the display components
     * for a module of that type.
     *
     * @param module The type of module online
     * @param queueName The module queue name
     */
    private void setModuleOnline(ModuleTypeEnum module, String queueName) {
        
        addModule(module, queueName);
        
        sendWebMonitorUpdate(new ModuleStatusUpdate(moduleToQueueNames));
    }
    
    /**
     * A module of the given type has gone offline, update the display
     * components for a module of that type.
     *
     * @param module The type of module offline
     * @param queueName The module queue name
     */
    private void setModuleOffline(ModuleTypeEnum module, String queueName) {
        
        removeModule(module, queueName);
        
        sendWebMonitorUpdate(new ModuleStatusUpdate(moduleToQueueNames));
    }
    
    @Override
    public void moduleStatusAdded(long sentTime, ModuleStatus status) {
        ModuleTypeEnum type = status.getModuleType();
        String queueName = status.getQueueName();
        setModuleOnline(type, queueName);
    }
    
    @Override
    public void moduleStatusChanged(long sentTime, ModuleStatus status) {
        ModuleTypeEnum type = status.getModuleType();
        String queueName = status.getQueueName();
        setModuleOnline(type, queueName);
    }

    @Override
    public void moduleStatusRemoved(StatusReceivedInfo status) {
        ModuleStatus moduleStatus = status.getModuleStatus();
        ModuleTypeEnum type = moduleStatus.getModuleType();
        String queueName = moduleStatus.getQueueName();
        setModuleOffline(type, queueName);
    }
    
    /**
     * Add a user to the user list
     *
     * @param userSession The user session of the user to add. If null, no action will be performed.
     */
    private void addUser(UserSession userSession) {
        
        if(userSession == null) {
            return;
        }
        
        userSessions.put(userSession.getUserId(), userSession);
        
        sendWebMonitorUpdate(new UsersStatusUpdate(userSessions, activeDomainSessions));
    }

    /**
     * Removes a user from the user list
     *
     * @param userSession The user session of the user to add. If null, no action will be performed.
     */
    private void removeUser(UserSession userSession) {
        
        if(userSession == null) {
            return;
        }
        
        userSessions.remove(userSession.getUserId());
        
        sendWebMonitorUpdate(new UsersStatusUpdate(userSessions, activeDomainSessions));
    }
    
    /**
     * Adds a domain session to the domain session list
     *
     * @param domainSession The domain session ID. If null, no action will be performed.
     */
    private void addDomainSession(DomainSession domainSession) {
        if(domainSession == null) {
            return;
        }
        
        activeDomainSessions.put(domainSession.getUserId(), domainSession);
        
        sendWebMonitorUpdate(new UsersStatusUpdate(userSessions, activeDomainSessions));
    }

    /**
     * Removes a domain session from the domain session list
     *
     * @param domainSession The domain session ID. If null, no action will be performed.
     */
    private void removeDomainSession(DomainSession domainSession) {
        if(domainSession == null) {
            return;
        }
        
        activeDomainSessions.remove(domainSession.getUserId());
        
        sendWebMonitorUpdate(new UsersStatusUpdate(userSessions, activeDomainSessions));
    }
    
    @Override
    public void userStatusAdded(UserSession userSession) {
        this.addUser(userSession);
    }

    @Override
    public void userStatusRemoved(UserSession userSession) {
        this.removeUser(userSession);
    }

    @Override
    public void domainSessionActive(DomainSession domainSession) {
        this.addDomainSession(domainSession);
        
        panelManager.domainSessionActive(domainSession);
    }

    @Override
    public void domainSessionInactive(DomainSession domainSession) {
        this.removeDomainSession(domainSession);
    }

    @Override
    public void handleMessage(Message msg) {
        panelManager.handleMessage(msg);
    }

    @Override
    public void onMessage(AbstractMessageUpdate update) {
        
        if(panelManager != null 
                && panelManager.hasPanel(update.getDomainSessionId())) {
            
            /* Only send the update to the client if the client is viewing this domain session */
            sendWebMonitorUpdate(update);
        }
    }

    /**
     * Extracts all of the information needed to display a message's contents on the client
     * 
     * @param domainSessionId the ID of the domain session that the message resides in. Can be null
     * if it is a system message.
     * @param message the metadata of the message whose data is being displayed. Cannot be null.
     * @return the display data for the message. Cannot be null.
     */
    public MessageDisplayData getDisplayData(Integer domainSessionId, MessageEntryMetadata message) {
        return panelManager.getDisplayData(domainSessionId, message);
    }
    

    /**
     * Sets the listening state of the MessageListModel
     * 
     * @param domainSessionId the domain session id. Can be null if system message
     * @param listening the listening state
     */
    public void setListening(Integer domainSessionId, boolean listening) {
        panelManager.setListening(domainSessionId, listening);
    }
    
    /**
     * Begins monitoring the domain session with the given ID
     * 
     * @param browserSessionId the ID of the browser session to refresh. Cannot be null.
     * @param domainSessionId the ID of the domain session to monitor
     */
    public void watchDomainSession(String browserSessionId, int domainSessionId) {
        WebMonitorService service = getMonitorService(browserSessionId);
        if(service != null) {
            service.watchDomainSession(domainSessionId);
        }
        
        panelManager.monitorDomainSession(domainSessionId);
    }
    
    /**
     * Sets the header state of the MessageListModel
     * 
     * @param domainSessionId the domain session id. Can be null if system message
     * @param advancedHeader the header state
     */
    public void setAdvancedHeader(Integer domainSessionId, boolean advancedHeader) {
        panelManager.setAdvancedHeader(domainSessionId, advancedHeader);
    }
    
    /**
     * Updates the filter for the given domain session to only display entity states that match the given marking
     * 
     * @param entityMarking the entity marking to filter by. Cannot be null.
     * @param domainSessionId the ID of the domain session whose filter is being changed. Can be null.
     */
    public void entityFilter(String entityMarking, int domainSessionId) {
        panelManager.filterEntity(domainSessionId, entityMarking);
    }

    /**
     * Sets the message types that should be displayed by the filter for the given domain session
     * 
     * @param domainSessionId the ID of the domain session whose filter is being changed. Can be null.
     * @param selectedChoices the selected message type choices. Cannot be null
     */
    public void setFilterChoices(Integer domainSessionId, Set<MessageTypeEnum> selectedChoices) {
        panelManager.setFilterChoices(domainSessionId, selectedChoices);
    }
    
    /**
     * Stops monitoring the domain session with the given ID
     * 
     * @param browserSessionId the ID of the browser session making the request. Cannot be null.
     * @param domainSessionId the ID of the domain session to monitor
     */
    public void unwatchDomainSession(String browserSessionId, Integer domainSessionId) {
        
        /* Prevent the browser session from receiving updates for the domain session*/
        WebMonitorService service = getMonitorService(browserSessionId);
        if(service != null) {
            service.unwatchDomainSession(domainSessionId);
        }
        
        /* See if there are any other browser sessions watching the domain session. If not,
         * remove the server-side representation of the domain session panel */
        boolean isAnySessionWatching = false;
        synchronized(browserSessionToMonitorService) {
            for(WebMonitorService otherService : browserSessionToMonitorService.values()) {
                if(otherService.isWatching(domainSessionId)) {
                    isAnySessionWatching = true;
                    break;
                }
            }
        }
        
        if(!isAnySessionWatching) {
            panelManager.removePanel(domainSessionId);
        }
    }
    
    /**
     * Re-sends all message updates currently stored in memory for the given 
     * domain session to the client so that the client can completely refresh 
     * its information
     * 
     * @param browserSessionId the ID of the browser session to refresh. Cannot be null.
     * @param domainSessionId the ID of the domain session to refresh. Can be null.
     */
    public void refreshPanel(String browserSessionId, Integer domainSessionId) {
        
        WebMonitorService service = getMonitorService(browserSessionId);
        if(service != null) {
            
            /* Refresh the client's user session status */
            service.sendWebMonitorUpdate(new UsersStatusUpdate(userSessions, activeDomainSessions));
            
            /* Refresh the client's watched domain sessions */
            service.sendWatchedDomainSessions();
        }
        
        /* Refresh the client's message status */
        panelManager.refresh(domainSessionId);
    }  
    
    /**
     * Re-sends the current module statuses stored in memory to the client with the given
     * browser session ID so that it can refresh its information to match the server
     * 
     * @param browserSessionId the ID of the browser session to refresh. Cannot be null.
     */
    public void refreshModules(String browserSessionId) {
        WebMonitorService service = getMonitorService(browserSessionId);
        if(service != null) {
            service.sendWebMonitorUpdate(new ModuleStatusUpdate(moduleToQueueNames));
        }
    }
    
    /**
     * Sends an update from the web monitor to the client
     * 
     * @param update the update to send. Cannot be null.
     */
    public void sendWebMonitorUpdate(WebMonitorUpdate update) {
        
        synchronized(browserSessionToMonitorService) {
            for(WebMonitorService service : browserSessionToMonitorService.values()) {
                service.sendWebMonitorUpdate(update);
            }
        }
    }
}
