/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.DomainSessionList;
import mil.arl.gift.common.ModuleConnectionConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.UserSessionList;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.io.HelpDocuments;
import mil.arl.gift.common.io.ImageUtil;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.UserSessionMessage;

/**
 * Not really a standard module, merely takes all messages in the Message_Queue
 * to display the statuses of all the available modules.
 *
 * @author jleonard
 */
public class MonitorModule extends AbstractModule {

    private static Logger logger = LoggerFactory.getLogger(MonitorModule.class);

    private static final String DEFAULT_MODULE_NAME  = "MonitorModule";
    
    /** preferred size pixel gap to add to the selected panels size to have a buffer around the selected panels components*/
    private static final int SELECTED_PANEL_WIDTH_GAP = 40;
    private static final int SELECTED_PANEL_HEIGHT_GAP = 80;

    private ModuleStatusMonitor moduleStatusMonitor = ModuleStatusMonitor.getInstance();

    private UserStatusModel userStatusModel = new UserStatusModel();

    private DomainSessionStatusModel domainSessionStatusModel = new DomainSessionStatusModel();

    private int nextId = 0;

    private final Map<Integer, JComponent> idToComponentMap = new HashMap<Integer, JComponent>();

    /* Only the tab that is selected contains content. All other tabs will be empty. This is because the tab
     * with the largest sized content panel defines the size of the universal scroll pane. This implementation
     * allows the scroll pane to be sized properly. */
    /** 
     * This map is used as a reference for what content goes inside which tab. The key is used
     * for tab identification and the value is that tab's content.
     */
    private final Map<Component, JComponent> tabToComponentMap = new HashMap<Component, JComponent>(); 

    private final JFrame monitorModuleFrame = new JFrame();
    private final JTabbedPane tabContainer;
    private int lastTabIndex = 0;

    private final int MAIN_TAB_INDEX = 0;
    
    private final Set<Integer> monitoredDomainSessions = new HashSet<Integer>();

    private final List<MonitorMessageListener> messageListeners = new ArrayList<MonitorMessageListener>();

    private final List<DomainSessionMonitorListener> activeDomainSessionListeners = new ArrayList<DomainSessionMonitorListener>();
    
    /** map of domain module (unique address) to their active domain session ids */
    private final Map<String, List<Integer>> globalDModuleAddrToDSessionId = new HashMap<>();
    
    /** map of tutor module (unique address) to their active user session ids */
    private final Map<String, List<UserSession>> globalTModuleAddrToUSessionId = new HashMap<>();

    /** the help button's icon shown on the monitor */
    private ImageIcon helpIcon = createImageIcon("images/help.png", "Help");

    /** the monitor documentation file containing help */
    private URI helpFile = HelpDocuments.getMonitorDoc();
    
    /** 
     * array used that indicates whether a timer used to delay the removal of a domain session from 
     * the monitor to allow the domain session to be closed first is active 
     */
    private ArrayList<Integer> domainSessionsPendingRemoval = new ArrayList<Integer>();
    
    /** Used to delay removal of the user sessions from the monitor.  This is so that messages that may
     * be incoming can still be received before the monitor closed out the user session.  This is similar
     * to the domain session removal logic.
     */
    private ArrayList<UserSession> userSessionsPendingRemoval = new ArrayList<UserSession>();

    static {
        PropertyConfigurator.configureAndWatch(PackageUtil.getConfiguration() + "/tools/monitor/monitor.log4j.properties");
    }
    private static MonitorModule instance = null;

    /**
     * Gets the frame the monitor module tabs are displayed in
     *
     * @return JFrame The frame the monitor module tabs are displayed in
     */
    public JFrame getFrame() {

        return monitorModuleFrame;
    }

    /**
     * Gets the singleton instance of the monitor module
     *
     * @return MonitorModule The singleton instance of the monitor module
     */
    public static MonitorModule getInstance() {

        if (instance == null) {

            instance = new MonitorModule();
            instance.init();
        }

        return instance;
    }

    /**
     * Default Constructor
     */
    private MonitorModule() {
        super(DEFAULT_MODULE_NAME, SubjectUtil.MONITOR_QUEUE + ":" + ipaddr, SubjectUtil.MONITOR_QUEUE + ":" + ipaddr, MonitorModuleProperties.getInstance());
        tabContainer = new JTabbedPane();

        tabContainer.addChangeListener(new ChangeListener() {
        	
            @Override
            public void stateChanged(ChangeEvent e) {
                JTabbedPane pane = (JTabbedPane) e.getSource();
	            
	            // lastTabIndex = -1 means that a tab was just removed.
	            if (lastTabIndex != -1) {
	            	// Remove the panel from the previously viewed tab to allow for proper size adjustment.
	            	pane.setComponentAt(lastTabIndex, null);
	            }
	            
	            lastTabIndex = pane.getSelectedIndex();

	            // Display the appropriate panel for the selected tab.
	            pane.setComponentAt(pane.getSelectedIndex(), tabToComponentMap.get(pane.getTabComponentAt(pane.getSelectedIndex())));
	            
                JComponent paneComponent = (JComponent) pane.getSelectedComponent();
	            
	            if (paneComponent != null) {
                monitorModuleFrame.setSize(paneComponent.getPreferredSize().width + SELECTED_PANEL_WIDTH_GAP, paneComponent.getPreferredSize().height + SELECTED_PANEL_HEIGHT_GAP);
	            }
	            
                tabContainer.invalidate();
            }
        });
    }

    @Override
    protected void init() {
        ControlJPanel panel = new ControlJPanel();
        registerModuleStatusListener(panel);
        registerUserStatusListener(panel);
        registerDomainSessionStatusListener(panel);
        RemoteClientManager.getInstance().addRemoteClientStatusListener(panel);

        BookmarkPanel bookmarkPanel = new BookmarkPanel();
        registerDomainSessionStatusListener(bookmarkPanel);
       
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        containerPanel.add(panel);

        addTab("Main", containerPanel);        
        addTab("Bookmarks", bookmarkPanel);

        if (MonitorModuleProperties.getInstance().getEnableWebcamViewing()) {
            WebcamJPanel webcamPanel = new WebcamJPanel();
            addTab("Webcams", webcamPanel);
        }

        registerMessageListener(PanelManager.getInstance());
        registerActiveDomainSessionListener(PanelManager.getInstance());
        registerDomainSessionStatusListener(PanelManager.getInstance());

        JPanel sensorPanel = SensorDataManager.getInstance().getGraphPanel();
        registerMessageListener(SensorDataManager.getInstance());
        registerActiveDomainSessionListener(SensorDataManager.getInstance());
        registerDomainSessionStatusListener(SensorDataManager.getInstance());

        addTab("Sensor", sensorPanel);

        LearnerStatePanel learnerStatePanel = new LearnerStatePanel();
        registerMessageListener(learnerStatePanel);
        registerActiveDomainSessionListener(learnerStatePanel);
        registerDomainSessionStatusListener(learnerStatePanel);

        //RemotePanel remotePanel = new RemotePanel();
        //tabContainer.addTab("Remote", remotePanel);

        addTab("Learner State", learnerStatePanel);
        
        JScrollPane scrollPane = new JScrollPane(tabContainer);
        monitorModuleFrame.add(scrollPane);
        
        monitorModuleFrame.setIconImage(ImageUtil.getInstance().getSystemIcon());
        monitorModuleFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        monitorModuleFrame.setTitle(MonitorModuleProperties.getInstance().getApplicationTitle());
        monitorModuleFrame.pack();

        // Only the selected tab will hold a component, all other unselected tabs will be empty.
        // This allows for proper window size adjustment.
        tabContainer.setComponentAt(tabContainer.getSelectedIndex(), containerPanel);
        JPanel currentPanel = (JPanel) tabContainer.getSelectedComponent();
        monitorModuleFrame.setSize(currentPanel.getPreferredSize().width + SELECTED_PANEL_WIDTH_GAP, currentPanel.getPreferredSize().height + SELECTED_PANEL_HEIGHT_GAP);
        monitorModuleFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });


        //
        //create Help button
        //
        Rectangle tabBounds = tabContainer.getBoundsAt(0);

        Container glassPane = (Container) monitorModuleFrame.getRootPane().getGlassPane();
        glassPane.setVisible(true);
        glassPane.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(tabBounds.y, 0, 0, 15);
        gbc.anchor = GridBagConstraints.NORTHEAST;
        JButton helpButton = new JButton(helpIcon);
        helpButton.setPreferredSize(new Dimension(helpIcon.getIconWidth(), helpIcon.getIconHeight()));
        helpButton.setOpaque(false);
        helpButton.setContentAreaFilled(false);
        helpButton.setBorderPainted(false);
        helpButton.setToolTipText("Opens the Monitor Help documentation");
        helpButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {

                Desktop desktop = Desktop.getDesktop();
                try {
                    desktop.browse(helpFile);  // opens application associated with file
                } catch (Exception ex) {
                    logger.error("Caught exception while trying to open the documentation at " + helpFile, ex);
                    JOptionPane.showMessageDialog(monitorModuleFrame, "Unable to open documentation\nPlease refer to the log for more details", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        glassPane.add(helpButton, gbc);

        monitorModuleFrame.setVisible(true);

        
        //look for Domain and Tutor Modules being added/removed to track their active domain and user sessions, respectively
        //Note: part of this requests the active domain sessions from the domain module and then the response is used
        //      to add domain sessions to the monitor module.  This should only be done after the all panels have registered
        //      for listening for domain session status notifications
        registerModuleStatusListener(new ModuleStatusListener() {
            
            @Override
            public void moduleStatusRemoved(StatusReceivedInfo status) {
                
                ModuleStatus moduleStatus = status.getModuleStatus();
                if(moduleStatus.getModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                    
                    //remove domain sessions mapped to this domain module
                    logger.info("Removing the domain sessions mapped to domain module named "+moduleStatus.getModuleName()+" because that module is no longer online after not having heard from it for "+(status.getTimeoutValue()/1000.0)+" seconds.");
                    List<Integer> dsIds = globalDModuleAddrToDSessionId.remove(moduleStatus.getQueueName());
                    domainSessionStatusModel.removeDomainSessions(dsIds);                  
                    
                }else if(moduleStatus.getModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                    
                    //remove user sessions mapped to this tutor module
                    logger.info("Removing the user sessions mapped to tutor module named "+moduleStatus.getModuleName()+" because that module is no longer online after not having heard from it for "+(status.getTimeoutValue()/1000.0)+" seconds.");
                    List<UserSession> usIds = globalTModuleAddrToUSessionId.remove(moduleStatus.getQueueName());
                    userStatusModel.removeUserSessions(usIds);
                }
            }
            
            @Override
            public void moduleStatusChanged(long sentTime, ModuleStatus status) {
                //nothing to do
                
            }
            
            @Override
            public void moduleStatusAdded(long sentTime, final ModuleStatus status) {
                
                if(status.getModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                    
                    //request active domain sessions
                    createSubjectQueueClient(status.getQueueName(), status.getModuleType(), false);
                    sendMessage(null, MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REQUEST, new MessageCollectionCallback() {
                        
                        @Override
                        public void success() {
                            removeClientConnection(status.getQueueName(), false);                            
                        }
                        
                        @Override
                        public void received(Message msg) {
                            
                            if(msg.getPayload() instanceof DomainSessionList){
                                
                                DomainSessionList dsList = (DomainSessionList)msg.getPayload();
                                logger.info("Adding mapping domain module: "+status+" active domain sessions of "+dsList);                            
                                domainSessionStatusModel.addDomainSessions(dsList.getDomainSessions());                              
                                
                            }else{
                                logger.error("Received unhandled message payload when requesting active domain sessions from domain module: "+status+" - "+msg);
                            }               
                        }
                        
                        @Override
                        public void failure(String why) {
                            logger.error("Unable to retrieve active domain sessions from domain module: "+status+" because "+why);
                            removeClientConnection(status.getQueueName(), false);
                        }
                        
                        @Override
                        public void failure(Message msg) {
                            logger.error("Unable to retrieve active domain sessions from domain module: "+status+" because received response message of "+msg);
                            removeClientConnection(status.getQueueName(), false);
                        }
                    });
                    
                }else if(status.getModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                    
                    //request active user sessions
                    createSubjectQueueClient(status.getQueueName(), status.getModuleType(), false);
                    sendMessage(null, MessageTypeEnum.ACTIVE_USER_SESSIONS_REQUEST, new MessageCollectionCallback() {
                        
                        @Override
                        public void success() {
                            removeClientConnection(status.getQueueName(), false);                            
                        }
                        
                        @Override
                        public void received(Message msg) {
                            
                            if(msg.getPayload() instanceof UserSessionList){
                                
                                UserSessionList usList = (UserSessionList)msg.getPayload();
                                logger.info("Adding mapping tutor module: "+status+" active user sessions of "+usList);
                                userStatusModel.addUserSessions(usList.getUserSessions());
                                
                            }else{
                                logger.error("Received unhandled message payload when requesting active user sessions from tutor module: "+status+" - "+msg);
                            }
                        }
                        
                        @Override
                        public void failure(String why) {
                            logger.error("Unable to retrieve active user sessions from tutor module: "+status+" because "+why);
                            removeClientConnection(status.getQueueName(), false);
                        }
                        
                        @Override
                        public void failure(Message msg) {
                            logger.error("Unable to retrieve active user sessions from tutor module: "+status+" because received response message of "+msg);
                            removeClientConnection(status.getQueueName(), false);
                        }
                    });
                    
                }
            }
        });

        //for receiving all messages (on a particular GIFT network of modules)
        createSubjectTopicClient(SubjectUtil.MONITOR_TOPIC);

        logger.info("Monitor initialized");
    }

    @Override
    protected ModuleTypeEnum getModuleType() {
        return ModuleTypeEnum.MONITOR_MODULE;
    }

    @Override
    protected void handleMessage(Message message) {

        if (message instanceof UserSessionMessage) {
            handleAbstractUserSessionMessage((UserSessionMessage) message);
        }

        synchronized (messageListeners) {

            for (MonitorMessageListener i : messageListeners) {

                try {
                    i.handleMessage(message);
                } catch (Exception e) {
                    logger.error("Caught exception from misbehaving message listener", e);
                }
            }
        }
    }

    /**
     * Handle any monitoring of a user session message.
     * 
     * @param message
     */
    private void handleAbstractUserSessionMessage(final UserSessionMessage message) {

        final UserSession user = message.getUserSession();
        if(message.getMessageType() == MessageTypeEnum.PROCESSED_ACK || message.getMessageType() == MessageTypeEnum.PROCESSED_NACK
                || message.getMessageType() == MessageTypeEnum.ACK || message.getMessageType() == MessageTypeEnum.NACK){
                 //Note: have to ignore these ACK/NACK messages which will happen after a logout message is sent.
                 //      Also, these are some of the most commonly sent messages, therefore checking for these first is a good idea.
                 
                 return;
                 
        }else if(message.getMessageType() == MessageTypeEnum.LOGOUT_REQUEST){
            //signals that its time to remove the user session
            
            if(message.getSenderModuleType() == ModuleTypeEnum.TUTOR_MODULE){
               
                // Similar to the domain session logic, the user sessions are not immediately removed.
                // there are cases (such as when a domain session is also being closed for the user session) where incoming
                // messages need to be processed.  In this case, a slight delay (5s) is used to handle the incoming messages
                // before the user session is actually removed from the monitor.
                if (!userSessionsPendingRemoval.contains(user)) {

                    // Delay the user session removal for 5 seconds.
                    Timer delayedDSRemovalTimer = new Timer("Delayed User Session Remove");
                    delayedDSRemovalTimer.schedule(new TimerTask() {
                        
                        @Override
                        public void run() {
                            logger.debug("Removing user session: " + user);

                            userStatusModel.removeUserSession(user);
                            
                            List<UserSession> usIds = globalTModuleAddrToUSessionId.get(message.getSenderAddress());
                            if(usIds != null && !usIds.isEmpty() && usIds.contains(message.getUserSession())){
                                logger.info("Removing user session id of "+message.getUserId()+" from being mapped to tutor module named "+message.getSenderModuleName());
                                usIds.remove(user); 
                            }
                            
                            synchronized (userSessionsPendingRemoval) {
                                userSessionsPendingRemoval.remove(user);
                            }
                            
                        }
                    }, 5000);
                    
                    synchronized (userSessionsPendingRemoval) {
                        userSessionsPendingRemoval.add(user);
                    }
                } 
            }
            
        }else if(message.getMessageType() == MessageTypeEnum.LOGIN_REQUEST){
            //signals that its time to add a user session
            
            if(message.getSenderModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                //add mapping of tutor module to user session
                addTutorModuleUserSession(message.getSenderAddress(), user);
            }
            
        }else{
            //otherwise make sure there is an entry for this user session
        	//if login failed user message will be module allocation request or reply at this point, so don't add to list
            if(message.getMessageType() != MessageTypeEnum.MODULE_ALLOCATION_REQUEST && message.getMessageType() != MessageTypeEnum.MODULE_ALLOCATION_REPLY){
            	userStatusModel.addUserSession(message.getUserSession());
            }
            
            if(message.getSenderModuleType() == ModuleTypeEnum.TUTOR_MODULE){
                //add mapping of tutor module to user session
                addTutorModuleUserSession(message.getSenderAddress(), user);
            }
        }
                
        if (message instanceof DomainSessionMessage) {
            handleDomainSessionMessage((DomainSessionMessage) message);
        }
    }

    /**
     * Handle monitoring a domain session message.
     * 
     * @param msg
     */
    private void handleDomainSessionMessage(final DomainSessionMessage msg) {        
        if(msg.getMessageType() == MessageTypeEnum.PROCESSED_ACK || msg.getMessageType() == MessageTypeEnum.PROCESSED_NACK
           || msg.getMessageType() == MessageTypeEnum.ACK || msg.getMessageType() == MessageTypeEnum.NACK){
            //Note: have to ignore these ACK/NACK messages which will happen after a close domain session message is sent.
            //      Also, these are some of the most commonly sent messages, therefore checking for these first is a good idea.
            
            return;
                    
        }else if(msg.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST){
            //signals that its time to remove the domain session
            if(msg.getSenderModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                //remove domain module domain session id mapping                
                final Integer domainSessionId = msg.getDomainSessionId();
                if(domainSessionStatusModel.containsDomainSession(domainSessionId)){
                    //the domain session needs to be removed from the list
                    if (!domainSessionsPendingRemoval.contains(domainSessionId)) {
                        //there isn't a timer already handling the removal call
                        //delay the removal of the domain session to allow for all domain session messages associated with
                        //closing a domain session to be monitored
                        Timer delayedDSRemovalTimer = new Timer("Delayed Domain Session Remove");
                        delayedDSRemovalTimer.schedule(new TimerTask() {
                            
                            @Override
                            public void run() {
                                logger.debug("Removing domain session: " + domainSessionId);
                                domainSessionStatusModel.removeDomainSession(domainSessionId);  
                                
                                synchronized (domainSessionsPendingRemoval) {
                                    domainSessionsPendingRemoval.remove(domainSessionId);
                                }
                                
                            }
                        }, 5000);
                        
                        synchronized (domainSessionsPendingRemoval) {
                            domainSessionsPendingRemoval.add(domainSessionId);
                        }
                        
                    } else {
                        logger.debug("domainSessionsPendingRemoval already contains the domain session to be removed, NOT scheduling a removal timer.");
                    }
                } else {
                    logger.debug("domainSessionStatusModel cannot find domain session: " + msg.getDomainSessionId());
                }
                
                List<Integer> dsIds = globalDModuleAddrToDSessionId.get(msg.getSenderAddress());
                if(dsIds != null && !dsIds.isEmpty() && dsIds.contains(msg.getDomainSessionId())){
                    logger.info("Removing domain session id of "+msg.getDomainSessionId()+" from being mapped to domain module named "+msg.getSenderModuleName());
                    dsIds.remove(Integer.valueOf(msg.getDomainSessionId()));
                }
            }
                        
        }else{
            //otherwise make sure there is an entry for this domain session
            
            if(!domainSessionStatusModel.containsDomainSession(msg.getDomainSessionId())){
                DomainSession dSession = new DomainSession(msg.getDomainSessionId(), msg.getUserId(), DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
                dSession.copyFromUserSession(msg.getUserSession());
                domainSessionStatusModel.addDomainSession(dSession);
            }
            
            if(msg.getSenderModuleType() == ModuleTypeEnum.DOMAIN_MODULE){
                //add mapping of domain module to domain session
                
                addDomainModuleDomainSession(msg.getSenderAddress(), msg.getDomainSessionId());
            }
        }        

    }
    
    /**
     * Add a new mapping of a domain module's domain session.
     * 
     * @param dmIdentifier - unique identifier of a domain module 
     * @param dsId - unique identifier of a domain session
     */
    private void addDomainModuleDomainSession(String dmIdentifier, int dsId){
        
        List<Integer> dsIds = globalDModuleAddrToDSessionId.get(dmIdentifier);
        if(dsIds == null){
            dsIds = new ArrayList<>();
            globalDModuleAddrToDSessionId.put(dmIdentifier, dsIds);
        }
        
        if(!dsIds.contains(dsId)){
            logger.info("Adding new domain session id of "+dsId+" to current mapping of domain session ids of "+dsId+" to domain module identifier of "+dmIdentifier);
            dsIds.add(dsId);
        }
    }
    
    /**
     * Add a new mapping of a tutor module's user session.
     * 
     * @param tmIdentifier - unique identifier of a tutor module 
     * @param user - the user session to add.
     */
    private void addTutorModuleUserSession(String tmIdentifier, UserSession user){

        List<UserSession> usIds = globalTModuleAddrToUSessionId.get(tmIdentifier);
        if(usIds == null){
            usIds = new ArrayList<UserSession>();
            globalTModuleAddrToUSessionId.put(tmIdentifier, usIds);
        }
        
        if(!usIds.contains(user)){
            logger.info("Adding new user "+user+" to current mapping of user session ids of "+usIds+" to tutor module identifier of "+tmIdentifier);
            usIds.add(user);
        } 
    }

    /**
     * Notify the system to monitor a domain session
     *
     * @param domainSessionId The ID of the domain session to monitor
     */
    public void monitorDomainSession(int domainSessionId) {

        if (!monitoredDomainSessions.contains(domainSessionId)) {
            monitoredDomainSessions.add(domainSessionId);
            synchronized (activeDomainSessionListeners) {
                for (DomainSessionMonitorListener i : activeDomainSessionListeners) {

                    try {
                        i.monitorDomainSession(domainSessionId);
                    } catch (Exception e) {
                        logger.error("Caught exception from misbehaving listener " + i, e);
                    }
                }
            }
        } else {
            logger.warn("Domain Session ID " + domainSessionId + " is already being monitored");
        }
    }

    /**
     * Notify the system to stop monitoring a domain session
     *
     * @param domainSessionId The ID of the domain session to stop monitoring
     */
    public void ignoreDomainSession(int domainSessionId) {

        if (monitoredDomainSessions.contains(domainSessionId)) {
            synchronized (activeDomainSessionListeners) {
                for (DomainSessionMonitorListener i : activeDomainSessionListeners) {

                    try {
                        i.ignoreDomainSession(domainSessionId);
                    } catch (Exception e) {
                        logger.error("Caught exception from misbehaving listener " + i, e);
                    }
                }
            }
            monitoredDomainSessions.remove(Integer.valueOf(domainSessionId));
        } else {
            logger.warn("Domain Session ID " + domainSessionId + " is not being monitored");
        }
    }

    @Override
    public void sendModuleStatus() {
        //the monitor is not a normal GIFT module in that it doesn't need to notify other modules of its existence
    }

    /**
     * Adds a tab to the TabbedPane and stores a reference to the tab's content.
     * 
     * @param title The title of the tab.  A JLabel with this title text will be used to
     * identify the tab.
     * @param component
     */
    private void addTab(final String title, final JComponent component) {
    	
        tabContainer.addTab(null, null);
        
        // The JLabel object has two purposes: To display the tab's title and to identify this tab inside the tabToComponentMap. 
        tabContainer.setTabComponentAt(tabContainer.getTabCount()-1, new JLabel(title));
        synchronized(tabToComponentMap){
            tabToComponentMap.put(tabContainer.getTabComponentAt(tabContainer.getTabCount()-1), component);
        }
    }
    
    /**
     * Adds a component to a new tab in the Monitor module
     *
     * @param title The title of the tab
     * @param component The component to be added to the tab
     * @return The ID of the added panel
     */
    public int addPanel(String title, final JComponent component) {

    	addTab(title, component);
    	
        nextId += 1;
        idToComponentMap.put(nextId, component);
        
        return nextId;
    }

    /**
     * Removes a panel from the monitor module
     *
     * @param id The ID of the panel to remove from the monitor module
     */
    public void removePanel(final int id) {

        if (idToComponentMap.containsKey(id)) {

            JComponent component = idToComponentMap.get(id);

            if (component != null) {

            	lastTabIndex = -1; // -1 will imply the last tab index (this tab) was deleted 
                tabContainer.remove(component);
               
                // Find and remove from tabToComponentMap
                synchronized(tabToComponentMap){
                    Iterator<Component> itr = tabToComponentMap.keySet().iterator();
                    while(itr.hasNext()){
                        
                        Component key = itr.next();
                        if (tabToComponentMap.get(key) == component) {
                            itr.remove();
                        }
                    }
                }
            }

            idToComponentMap.remove(id);
        }
    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     *
     * @param path The absolute path of the image to create an icon from
     * @param description The description of the image
     * @return ImageIcon The image converted into an icon
     */
    protected ImageIcon createImageIcon(String path, String description) {
        java.net.URL imgURL = getClass().getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL, description);
        }

        System.err.println("Couldn't find file: " + path);
        return null;
    }

    /**
     * Registers a listener that is interested in getting all messages in the
     * system
     *
     * @param listener The interested listener
     */
    public void registerMessageListener(final MonitorMessageListener listener) {
        synchronized (messageListeners) {
            messageListeners.add(listener);
        }
    }

    /**
     * Registers a listener that is interested in being notified when a domain
     * session is to be monitored
     *
     * @param listener The interested listener
     */
    public void registerActiveDomainSessionListener(final DomainSessionMonitorListener listener) {
        synchronized (activeDomainSessionListeners) {
            activeDomainSessionListeners.add(listener);
        }
    }

    /**
     * Registers a listener that is interested in being notified when modules
     * come up and go down
     *
     * @param listener The interested listener
     */
    public void registerModuleStatusListener(final ModuleStatusListener listener) {
        moduleStatusMonitor.addListener(listener);
    }

    /**
     * Registers a listener that is interested in being notified when users come
     * online and go offline
     *
     * @param listener The interested listener
     */
    public void registerUserStatusListener(final UserStatusListener listener) {
        userStatusModel.addListener(listener);
    }

    /**
     * Registers a listener that is interested in being notified when domain
     * sessions start and end
     *
     * @param listener The interested listener
     */
    public void registerDomainSessionStatusListener(final DomainSessionStatusListener listener) {
        domainSessionStatusModel.addListener(listener);
    }
    
    /**
     * Requests the start time of the domain session with the specified ID from the UMS
     * 
     * @param domainSessionID The ID of the domain session whose start time is being requested
     * @param callback A MessageCollectionCallback awaiting the results of the request
     */
    public void requestDomainSessionStartTime(final int domainSessionID, final MessageCollectionCallback callback) {  	    	
    	
    	JPanel p = (JPanel) tabToComponentMap.get(tabContainer.getTabComponentAt(MAIN_TAB_INDEX));   	   	
    			
    	// Find the ControlJPanel
    	for (Component c : p.getComponents()) {

    		if (c instanceof ControlJPanel) {
    			// Found the ControlJPanel
    			ControlJPanel panel = (ControlJPanel)c;

				List<Object> umsModuleAddresses = Arrays.asList(panel.getUMSListModel().toArray());
				
				//Note: Currently GIFT only supports one UMS instance at a time, so request start time from the first instance available
		        if (!umsModuleAddresses.isEmpty()) {   	
		        	
		        	logger.info("Requesting start time for domain session with ID: " + domainSessionID + " from the UMS");
		        	
		        	final String subject = (String) umsModuleAddresses.get(0);
		        	
		        	createSubjectQueueClient(subject, ModuleTypeEnum.UMS_MODULE, false);
		          	sendMessage(domainSessionID, MessageTypeEnum.DOMAIN_SESSION_START_TIME_REQUEST, new MessageCollectionCallback() {
		                     		
		                  @Override
		                  public void success() {                                                                             
		                	  callback.success();    
		                	  removeClientConnection(subject, false);
		                  }
		                  
		    			  @Override
		                  public void received(final Message msg) {              	                                                               
		            		  callback.received(msg);      
		                  }
		                  
		                  @Override
		                  public void failure(final String why) {                    
		            		  callback.failure(why);       
		            		  removeClientConnection(subject, false);
		                  }
		                  
		                  @Override
		                  public void failure(final Message msg) {              		              		              		              
			        		  callback.failure(msg);    
			        		  removeClientConnection(subject, false);
				          }
		          	});
		          	
		        }else{
		        	callback.failure("Unable to retrieve the Domain Session start time for "+ domainSessionID + " because there are no UMS modules known to the Monitor.");
		        }
		        
		        break; // found ControlJPanel
    		}
    	}
	}

    /**
     * Notify the domain module to close the domain session.
     *
     * @param domainModuleAddress - address of domain module being ordered to close the respective domain session.
     * @param domainSession - info about the domain session
     */
    public void closeDomainSession(String domainModuleAddress, DomainSession domainSession) {

        createSubjectQueueClient(domainModuleAddress, ModuleTypeEnum.DOMAIN_MODULE, false);
        sendDomainSessionMessage(domainModuleAddress, new CloseDomainSessionRequest(), domainSession, domainSession.getDomainSessionId(), domainSession.getExperimentId(),
                MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, null);
        removeClientConnection(domainModuleAddress, false);
    }

    /**
     * Send a kill module message to the module with the given address.
     *
     * @param queueName name of the module queue to connect too and send the message too
     * @param moduleType the type of module associated with the queue
     */
    public void killModule(final String queueName, ModuleTypeEnum moduleType) {

        logger.info("Sending kill message to " + queueName + " of module type " + moduleType);

        //since this is the only message the monitor sends to modules, 
        // 1) create client
        // 2) send message
        // 3) remove client

        createSubjectQueueClient(queueName, moduleType, false);
        sendMessage(queueName, null, MessageTypeEnum.KILL_MODULE, null);
        removeClientConnection(queueName, false);
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            try {
                UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            } catch (Exception e) {
                logger.error("Caught an exception while setting the look and feel", e);
            }
        }
        
        MonitorModuleProperties.getInstance().setCommandLineArgs(args);

        MonitorModule monitor = null;
        try{
            monitor = MonitorModule.getInstance();
            monitor.showModuleStartedPrompt();
            
        }catch(Throwable e){
        	
        	System.err.println("The Monitor threw an exception.");
        	e.printStackTrace();        

        	if(monitor != null){
        		monitor.cleanup();
        	}

        	if (e instanceof ModuleConnectionConfigurationException) {
        		        	
        		JOptionPane.showMessageDialog(null,
        				"Monitor cannot start because it's unable to establish a connection to the message bus." +
						"\nIs ActiveMQ running? Remember, ActiveMQ must be launched before the Monitor!" +
						"\nIf ActiveMQ is running, then you may have a configuration error." +
						"\nAdjust module properties if necessary then (re)start ActiveMQ and try again.",
						"Monitor Error",
						JOptionPane.ERROR_MESSAGE);     
        		
        	} else {
        		
            	JOptionPane.showMessageDialog(null,
            			"The Monitor had a severe error.  Check the log file and the console window for more information.",
            			"Monitor Error",
            			JOptionPane.ERROR_MESSAGE); 
            	showModuleUnexpectedExitPrompt(ModuleModeEnum.POWER_USER_MODE);        	
        	}
        }

        System.out.println("Good-bye");

        //kill any threads
        System.exit(0);
    }
}
