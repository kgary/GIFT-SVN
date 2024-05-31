/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JOptionPane;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.CloseDomainSessionRequest;
import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.DomainOptionsList;
import mil.arl.gift.common.DomainOptionsRequest;
import mil.arl.gift.common.DomainSelectionRequest;
import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.DomainSessionList;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.InitializeInteropConnections;
import mil.arl.gift.common.InteropConnectionsInfo;
import mil.arl.gift.common.ModuleConnectionConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.UserData;
import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsReply;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.enums.ErrorEnum;
import mil.arl.gift.common.enums.LessonLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.ModuleModeEnum;
import mil.arl.gift.common.enums.ModuleTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileFinderUtil;
import mil.arl.gift.common.module.AbstractModule;
import mil.arl.gift.common.module.GatewayModuleStatus;
import mil.arl.gift.common.module.ModuleStatus;
import mil.arl.gift.common.module.ModuleStatusListener;
import mil.arl.gift.common.module.ModuleStatusMonitor;
import mil.arl.gift.common.module.ModuleStatusMonitor.StatusReceivedInfo;
import mil.arl.gift.common.ta.state.TrainingAppState;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.gateway.desktop.GatewayTray;
import mil.arl.gift.gateway.installer.InstallMain;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.gateway.interop.AbstractInteropInterface.HandlesDisDialect;
import mil.arl.gift.gateway.interop.InteropConfigFileHandler;
import mil.arl.gift.net.api.MessageCollectionCallback;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.ACK;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.api.message.NACK;
import mil.arl.gift.net.api.message.UserSessionMessage;
import mil.arl.gift.net.dis.DISToGIFTConverter.DISDialect;
import mil.arl.gift.net.util.Util;

/**
 * The gateway module provides a gateway between simulation messages (DIS/HLA/...) and GIFT messages.
 *
 * @author mhoffman
 *
 */
public class GatewayModule extends AbstractModule {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GatewayModule.class);

    /** the name of the module */
    private static final String DEFAULT_MODULE_NAME = "Gateway_Module";

    /** a unique token (hopefully) for this gateway module to include in the queue name */
    private static String Unique_Queue_Token;

    /** singleton instance of this class */
    protected static GatewayModule instance = null;

    /** The name of the interop configuration file. */
    private final String interopConfigFileName;

    /** container of interop interfaces created */
    private Map<Integer, AbstractInteropInterface> interops = new HashMap<>();

    /** instance of a currently running GW installer, needed when closing this module during the installation logic */
    private InstallMain runningInstall = null;

    /** flag used to indicate if the JWS GW module instance has presented the interops installer dialog yet */
    private boolean haveDisplayedInstaller = false;

    /** the name of the topic translated simulation messages are sent too */
    private String gatewayTopicName = null;

    /** unique status object for Gateway module to notify other modules (mainly domain) of where simulation messages will be sent too */
    private GatewayModuleStatus gatewayStatus;

    /** The user session this gateway currently belongs to. */
    private final AtomicReference<UserSession> userSession = new AtomicReference<>();

    /**
     * A boolean flag indicating whether or not the creation of a user session
     * is currently in progress.
     */
    private final AtomicBoolean isStartingUserSesssion = new AtomicBoolean();

    /** The domain session this gateway currently belongs to. */
    private final AtomicReference<DomainSession> domainSession = new AtomicReference<>();

    /**
     * A boolean flag indicating whether or not a domain selection action is
     * currently in progress.
     */
    private final AtomicBoolean isStartingDomainSession = new AtomicBoolean();

    static {
        //use Gateway log4j

        String configFilename = PackageUtil.getConfiguration() + "/gateway/gateway.log4j.properties";

        InputStream iStream = FileFinderUtil.getFileByClassLoader(configFilename);
        if(iStream != null){
            Properties prop = new Properties();
            try {
                prop.load(iStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
            PropertyConfigurator.configure(prop);
        }else{
            PropertyConfigurator.configureAndWatch(configFilename);
        }
    }

    /**
     * The {@link Set} of {@link MessageTypeEnum} objects that should not result
     * in an error if they are not handled by currently enabled interops.  Some
     * interops handle these messages types.
     */
    private static final Set<MessageTypeEnum> OPTIONAL_MSG_TYPES = Stream
            .of(MessageTypeEnum.LESSON_STARTED, MessageTypeEnum.LESSON_COMPLETED, MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST)
            .collect(Collectors.toSet());

    /**
     * Return the singleton instance of this class
     *
     * @return the singleton instance of this class
     */
    public static synchronized GatewayModule getInstance() {

        if (instance == null) {
            instance = new GatewayModule();
        }

        return instance;
    }

    /**
     * Default constructor
     */
    private GatewayModule(){
        super(DEFAULT_MODULE_NAME,
                SubjectUtil.GATEWAY_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + Unique_Queue_Token,
                SubjectUtil.GATEWAY_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + Unique_Queue_Token + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX,
                GatewayModuleProperties.getInstance());

        interopConfigFileName = null;
        gatewayTopicName = SubjectUtil.GATEWAY_TOPIC_PREFIX + ADDRESS_TOKEN_DELIM + Unique_Queue_Token;
    }

    /**
     * Class constructor - allow caller to specify the interop configuration file name to use for creating
     * interop interface instance(s).
     * NOTE: this should only be used by classes which extend the gateway module such as the test class because
     * the instance member attribute needs to be set for other classes to use.
     *
     * @param interopConfigFileName - the interop configuration file name to use
     */
    protected GatewayModule(String interopConfigFileName){
        super(DEFAULT_MODULE_NAME, SubjectUtil.GATEWAY_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + Unique_Queue_Token, SubjectUtil.GATEWAY_QUEUE_PREFIX + ADDRESS_TOKEN_DELIM + Unique_Queue_Token + ADDRESS_TOKEN_DELIM + SubjectUtil.INBOX_QUEUE_SUFFIX, GatewayModuleProperties.getInstance());

        this.interopConfigFileName = interopConfigFileName;
        gatewayTopicName = SubjectUtil.GATEWAY_TOPIC_PREFIX + ADDRESS_TOKEN_DELIM + Unique_Queue_Token;
    }

    /**
     * Perform initialization logic for the module
     */
    @Override
    protected void init() {
        if (!GatewayModuleProperties.getInstance().isRemoteMode()) {
            // create and configure interop interfaces now (configure interops
            // later when running JWS)
            configureInterop(null);
        }

        //create client to send gateway status too
        createSubjectTopicClient(SubjectUtil.GATEWAY_DISCOVERY_TOPIC, false);

        //create client to send gift messages that the gateway translated from simulation messages
        createSubjectTopicClient(gatewayTopicName, false);

        // In RTA lesson level the Gateway module is responsible for replacing the tutor module's
        // control over start/stopping sessions.  Therefore the Gateway module needs the same
        // logic for starting a user/domain session which involves connecting to the UMS and LMS
        // modules first.  Only allocate these modules if in this mode.
        if (LessonLevelEnum.RTA.equals(CommonProperties.getInstance().getLessonLevel())) {
            final AtomicBoolean umsModuleSelectionSent = new AtomicBoolean(false);
            final AtomicBoolean lmsModuleSelectionSent = new AtomicBoolean(false);

            final ModuleStatusListener extControlInitListener = new ModuleStatusListener() {               
               
               private void removeExtControlInitListener(){
                   
                   if(umsModuleSelectionSent.get() && lmsModuleSelectionSent.get()){
                       ModuleStatusMonitor.getInstance().removeListener(this);
                   }
               }

                @Override
                public void moduleStatusRemoved(StatusReceivedInfo status) {
                }

                @Override
                public void moduleStatusChanged(long sentTime, ModuleStatus status) {
                    // Note: this logic is in the changed method and not the added method because the added method is only
                    // called on the first heartbeat received by that module type.  Since network session is instantiating
                    // the module status monitor before this init() there is a race condition where the added method won't
                    // be called for this listener.
                    if (logger.isDebugEnabled()) {
                        logger.debug("moduleStatusChanged(" + sentTime + ", " + status + ")");
                    }

                    if (ModuleTypeEnum.UMS_MODULE.equals(status.getModuleType()) && !umsModuleSelectionSent.getAndSet(true)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("UMS Module Status received: " + status);
                        }

                        selectUMSModule(new MessageCollectionCallback() {

                            @Override
                            public void success() {
                                if (logger.isInfoEnabled()) {
                                    logger.info("UMS Module successfully selected.");
                                }
                                
                                removeExtControlInitListener();
                            }

                            @Override
                            public void received(Message msg) {
                            }

                            @Override
                            public void failure(String why) {
                                logger.error("There was a problem selecting the UMS Module: " + why);
                                removeExtControlInitListener();
                            }

                            @Override
                            public void failure(Message msg) {
                                failure(msg.toString());
                                removeExtControlInitListener();
                            }
                        });
                    } else if (ModuleTypeEnum.LMS_MODULE.equals(status.getModuleType()) && !lmsModuleSelectionSent.getAndSet(true)) {
                        if (logger.isInfoEnabled()) {
                            logger.info("LMS Module Status received: " + status);
                        }

                        selectModule(new UserSession(UserSessionMessage.PRE_USER_UNKNOWN_ID), ModuleTypeEnum.LMS_MODULE,
                                new MessageCollectionCallback() {

                                    @Override
                                    public void success() {
                                        if (logger.isInfoEnabled()) {
                                            logger.info("Successfully selected the LMS Module.");
                                        }
                                        removeExtControlInitListener();
                                    }

                                    @Override
                                    public void received(Message msg) {
                                    }

                                    @Override
                                    public void failure(String why) {
                                        logger.error("There was a problem allocating the LMS Module: " + why);
                                        removeExtControlInitListener();
                                    }

                                    @Override
                                    public void failure(Message msg) {
                                        failure(msg.toString());
                                        removeExtControlInitListener();
                                    }
                                });
                    }
                }

                @Override
                public void moduleStatusAdded(long sentTime, ModuleStatus status) {                    
                }
            };
            
            ModuleStatusMonitor.getInstance().addListener(extControlInitListener);
        }

        if(Util.isLocalAddress(Unique_Queue_Token)){
            // the Gateway module is now running through JWS JNLP, provide all the IP addresses of this computer
            // for comparison later
            gatewayStatus = new GatewayModuleStatus(gatewayTopicName, moduleStatus, Util.getLocalAddressesAsStrings());
        }else{
            gatewayStatus = new GatewayModuleStatus(gatewayTopicName, moduleStatus);
        }

        //start the module heartbeat
        initializeHeartbeat();
    }

    /**
     * Return whether or not there is an available interop interface that requires
     * the user's display.
     *
     * @return boolean
     */
    private boolean haveInteropsNeedingUsersDisplay(){

        //if this GW module is started in JWS mode than it is definitely needed by the user
        //and only that user
        if (GatewayModuleProperties.getInstance().isRemoteMode()) {
            return true;
        }

        for(AbstractInteropInterface interop : interops.values()){

            //note: don't test the list of all available interop(s) here - it should be done when a course starts that
            //needs that interop plugin.
            if(interop.isAvailable(false) && interop.requiresUsersDisplay()){
                return true;
            }
        }

        return false;
    }

    /**
     * Configure the interop interfaces specified by the interop config file named.
     * @param selectedInterops - collection of interop implementation class names (e.g. gateway.interop.vbsplugin.VBSPluginInterface)
     * that should be the only interops instantiated and configured.  Can be null if the logic should rely on the "available"
     * value in the interop configuration XML file.
     */
    private void configureInterop(Collection<String> selectedInterops) {

        InteropConfigFileHandler interopConfig;
        if(interopConfigFileName == null){
            interopConfig = new InteropConfigFileHandler(GatewayModuleProperties.getInstance().getInteropConfig());
        }else{
            interopConfig = new InteropConfigFileHandler(interopConfigFileName);
        }

        try{
            interopConfig.configureByImplementationClassName(selectedInterops);
        }catch(DetailedException e){
            throw e;
        }catch(Throwable t){
            throw new RuntimeException("Failed to configure the interops.", t);
        }
        interops = interopConfig.getInterops();

        checkForMissingInterops(interops);
    }

    /**
     * Compares the list of interop interface classes in the gateway package to the list of configured
     * interops.  If there is one or more interop classes not configured a warning log message will be
     * produced.
     *
     * @param interops mapping of configured interops to check against the set of all interop interface classes
     * in the gateway package.
     */
    private void checkForMissingInterops(Map<Integer, AbstractInteropInterface> interops){

        try {
            //find interops in GIFT
            String packageName = "mil.arl.gift.gateway.interop";
            List<Class<?>> supersetClasses = ClassFinderUtil.getSubClassesOf(packageName, AbstractInteropInterface.class);
            boolean found;
            StringBuilder sb = new StringBuilder();

            //compare to configured available interops
            for(Class<?> supersetClass : supersetClasses){

                found = false;

                for(AbstractInteropInterface interop : interops.values()){

                    if(interop.getClass().isAssignableFrom(supersetClass)){
                        found = true;
                        break;
                    }
                }

                if(!found){
                    sb.append(supersetClass.getName()).append("\n");
                }
            }

            if(sb.length() > 0){
                logger.warn("The following gateway interops are not being used in this instance because they are either set to 'not' available or a "+
                        "configuration is missing in "+GatewayModuleProperties.getInstance().getInteropConfig()+":\n"+sb.toString());
            }

        } catch (Exception e) {
            logger.error("Caught exception while trying to check for missing interops.", e);
        }


    }

    @Override
    public ModuleTypeEnum getModuleType(){
        return ModuleTypeEnum.GATEWAY_MODULE;
    }

    /**
     * Return the gateway topic name
     *
     * @return String The gateway topic name
     */
    public String getGatewayTopicName() {
        return gatewayTopicName;
    }

    /**
     * Return the container of interop interfaces that have been instantiated
     *
     * @return Map<Integer, AbstractInteropInterface>
     */
    public Map<Integer, AbstractInteropInterface> getInterops(){
        return interops;
    }

    @Override
    public void updateAllocationStatus(UserSession userSession){

        //
        // (If no interops needing the display) The gateway module can be allocated to unlimited users
        // (If 1 or more interops needing the display) The gateway module can be allocated to 1 user.  Here are the checks for that case:
        //   1. is there a user session for this instance?
        //   2. does the user session match the incoming request?
        //
        boolean allocated = this.getUserSession() != null &&
                userSession != null &&
                this.getUserId() != userSession.getUserId() &&
                haveInteropsNeedingUsersDisplay();
        if(logger.isInfoEnabled()){
            logger.info("Updating allocation status fully allocated to " + allocated + " for based on current user of "
                    + this.getUserSession() + " and requesting allocation for user of " + userSession);
        }
        allocationStatus.setFullyAllocated(allocated);
    }

    @Override
    public void allocateToUser(UserSession userSession) throws Exception {

        /* Confirm that there is not another UserSession that is in the process
         * of starting already. */
        final boolean sessionAlreadyStarting = isStartingUserSesssion.getAndSet(true);
        if (sessionAlreadyStarting) {
            throw new Exception("Unable to allocate " + userSession
                    + " because there is another user session that is already being started.");
        }

        try {
            if (haveInteropsNeedingUsersDisplay()) {
                setUserSession(userSession);
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Skipping gateway module allocation to user " + userSession
                            + " because the gateway module has no available interop interfaces needing the user's display.");
                }
            }
        } finally {
            isStartingUserSesssion.set(false);
        }
    }

    /**
     * Return the user id for the current user of this module that are associated with a learner
     *
     * @return Integer
     */
    public Integer getUserId(){
        return getUserSession().getUserId();
    }

    /**
     * Return the information about the user allocated to this gateway module.
     *
     * @return UserSession can be null.
     */
    public UserSession getUserSession() {
        return userSession.get();
    }

    /**
     * Set the user id for the current user of this module that are associated
     * with a learner
     *
     * @param newUserSession information about the user that is being allocated
     *        to this gateway module.
     * @throws UnsupportedOperationException if the provided {@link UserSession}
     *         is not null and {@link GatewayModule} already has a non-null
     *         value.
     */
    private void setUserSession(UserSession newUserSession) throws UnsupportedOperationException {

        /* Setting to null is always allowed. */
        if (newUserSession == null) {
            userSession.set(null);
            return;
        }

        /* If the sessions are the same, no action is necessary. */
        final boolean sessionsAreTheSame = newUserSession.equals(userSession.get());
        if (sessionsAreTheSame) {
            return;
        }

        /* If there is already a session running, the userSession value cannot
         * be updated. */
        final boolean sessionAlreadyRunning = !userSession.compareAndSet(null, newUserSession);
        if (sessionAlreadyRunning) {
            throw new UnsupportedOperationException("Can't change the user session for the gateway module to " + newUserSession
                    + " without closing the existing domain session first.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Changed userSession from null to " + newUserSession);
        }
    }

    /**
     * Return the domain session for the current use of this gateway module
     *
     * @return the current domain session for this gateway.  Can be null if not being used in a domain session right now.
     */
    public DomainSession getDomainSession(){
        return domainSession.get();
    }

    /**
     * Set the domain session for the current use of this gateway module
     *
     * @param newDomainSession The new value of {@link #domainSession}.
     */
    private void setDomainSession(DomainSession newDomainSession) {
        /* Setting to null is always allowed. */
        if (newDomainSession == null) {
            domainSession.set(null);
            return;
        }

        /* If the sessions are the same, no action is necessary. */
        final boolean sessionsAreTheSame = newDomainSession.equals(domainSession.get());
        if (sessionsAreTheSame) {
            return;
        }

        /* If there is already a session running, the userSession value cannot
         * be updated. */
        final boolean sessionAlreadyRunning = !domainSession.compareAndSet(null, newDomainSession);
        if (sessionAlreadyRunning) {
            throw new UnsupportedOperationException("Can't change the domain session for the gateway module to "
                    + newDomainSession + " without closing the existing domain session first.");
        }

        if (logger.isInfoEnabled()) {
            logger.info("Changed userSession from null to " + newDomainSession);
        }
    }


    @Override
    protected void handleMessage(Message message) {

        if(message.getMessageType() == MessageTypeEnum.KILL_MODULE) {
            handleKillModuleMessage(message);
        } else if(message.getMessageType() == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST) {
            handleInitializeDomainSessionRequestMessage(message);
        } else if(message.getMessageType() == MessageTypeEnum.START_DOMAIN_SESSION) {
            handleStartDomainSessionRequestMessage(message);
        } else if(message.getMessageType() == MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST) {
            handleCloseDomainSessionRequestMessage(message);
        } else if(message.getMessageType() == MessageTypeEnum.INIT_INTEROP_CONNECTIONS) {
            handleInitInteropConnectionsMessage(message);
        } else if(message.getMessageType() == MessageTypeEnum.CONFIGURE_INTEROP_CONNECTIONS){
            handleConfigureInteropConnectionsMessage(message);
        } else if(message.getMessageType() == MessageTypeEnum.LESSON_COMPLETED) {
            handleLessonCompletedMessage(message);
        } else if(message.getMessageType() == MessageTypeEnum.LESSON_STARTED) {
            handleLessonStartedMessage(message);
        } else {
            handleMessageFromGIFT(message);
        }

    }

    /**
     * Handles the message to kill this module
     *
     * @param message - The Kill Module message
     */
    private void handleKillModuleMessage(Message message) {
        Thread killModule = new Thread("Kill Module"){
            @Override
            public void run() {
                killModule();
            }
        };
        killModule.start();
    }

    /**
     * Handle the initialize domain session request message
     *
     * @param msg The {@link Message} to handle.
     */
    private void handleInitializeDomainSessionRequestMessage(Message msg) {

        DomainSession dSession = new DomainSession(((DomainSessionMessage)msg).getDomainSessionId(), ((DomainSessionMessage)msg).getUserId(), DomainSession.UNKNOWN_DOMAIN_NAME, DomainSession.UNKNOWN_DOMAIN_NAME);
        dSession.copyFromUserSession(((DomainSessionMessage)msg).getUserSession());
        final boolean canStartDomainSession = !isStartingDomainSession.getAndSet(true);
        if (canStartDomainSession) {
            setDomainSession(dSession);
            isStartingDomainSession.set(false);
            sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
        } else {
            final NACK nack = new NACK(ErrorEnum.OPERATION_FAILED,
                    "This Gateway Module is already in the process of starting another domain session.");
            sendReply(msg, nack, MessageTypeEnum.NACK);
        }

    }

    /**
     * Handle the lesson completed message by cleaning up any running interop interfaces
     *
     * @param message - the lesson completed message
     */
    private void handleLessonCompletedMessage(Message message) {

        if(logger.isInfoEnabled()){
            logger.info("Disabling available interfaces because the lesson is completed.");
        }

        handleMessageFromGIFT(message);
    }

    /**
     * Handle the lesson started message
     *
     * @param message - the lesson started message
     */
    private void handleLessonStartedMessage(Message message) {

        if(logger.isInfoEnabled()){
            logger.info("Rcvd lesson started message.\n"+message);
        }

        handleMessageFromGIFT(message);
    }

    /**
     * Disable all interop interfaces
     */
    private void disableAllInterops(){

        for(AbstractInteropInterface iFace : interops.values()){

            try{
                if(iFace.isAvailable(false)){
                    iFace.setEnabled(false);
                }
            }catch(Throwable e){
                logger.error("Caught exception from misbehaving interop interface while trying to disable it - "+iFace+".", e);
            }
        }
    }

    /**
     * Handle the configure interop connections request message used to
     * configure the interop interfaces that might be needed in the current
     * course execution.
     *
     * @param msg The {@link Message} to handle.
     */
    private void handleConfigureInteropConnectionsMessage(Message msg){
        handleInterops(msg, GatewayModuleProperties.getInstance().isRemoteMode(), false);
    }

    /**
     * Handle the initialize interop connections request message used to enable
     * interop interfaces for a specific training application course element.
     *
     * @param msg The {@link Message} to handle.
     */
    private void handleInitInteropConnectionsMessage(Message msg) {
        handleInterops(msg, false, true);
    }

    /**
     * Handle the initialize interop connections message by enabling the
     * specified interop plugin classes.
     *
     * @param msg the initialize interop connections message to process
     * @param needInstaller whether the installer dialog should be shown to
     *        allow the user to provide configuration parameters such as local
     *        paths to applications.
     * @param enableInterops whether to enable interop plugins if currently
     *        disabled, i.e. override the disabled setting
     */
    private void handleInterops(Message msg, boolean needInstaller, boolean enableInterops){

        InitializeInteropConnections initializeInteropConnections = (InitializeInteropConnections)msg.getPayload();
        String requestingObserver = initializeInteropConnections.getRequestingObserver();

        //the interops will be empty the first time the configuration file is read however this method
        //is called when the gateway is started and when a training application course element is reached in a course.
        if(interops.isEmpty() && initializeInteropConnections.getRequestingObserver() == null){
            //parse the interop config that was included with the GW JWS instance

            try{
                configureInterop(initializeInteropConnections.getInterops());
            }catch(Throwable e){
                logger.error("Caught exception while trying to configure the Gateway's interops for request of "+msg+".\nReplying with NACK message which will most likely end the domain session.", e);

                String reason = "There was a problem instantiating the Gateway interop plugins to use for this course";
                if(e instanceof DetailedException){
                    reason += " because "+((DetailedException)e).getReason();
                }else{
                    reason += ".";
                }

                NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, reason);
                if(GatewayModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){

                    if(e instanceof DetailedException){
                        nack.setErrorHelp(((DetailedException)e).getDetails() + ".  Check the Java Web Start trace file for more debugging details.");
                    }else{
                        nack.setErrorHelp("An exception occured in the Gateway module (check the Java Web Start log for more details) with the message:\n"+e.getMessage()+
                                "\n\nTechnical Note: the following Gateway module interop plugins where being initialized:\n"+initializeInteropConnections.getInterops());
                    }
                }else{

                    if(e instanceof DetailedException){
                        nack.setErrorHelp(((DetailedException)e).getDetails() + ".  Check the Gateway module log file for more debugging details.");
                    }else{
                        nack.setErrorHelp("An exception occured in the Gateway module (check the Gateway module log file for more details) with the message:\n"+e.getMessage()+
                                "\n\nTechnical Note: the following Gateway module interop plugins where being initialized:\n"+initializeInteropConnections.getInterops());
                    }
                }

                sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
                return;
            }
        }

        // list of interops that exist and can be enabled, populated based on the requested list
        List<AbstractInteropInterface> interfacesToEnable = new ArrayList<>(initializeInteropConnections.getInterops().size());
        Set<MessageTypeEnum> supportedMessages = new HashSet<>();
        try{

            for(String interopName : initializeInteropConnections.getInterops()){
                
                interopName = InteropConfigFileHandler.convertFromLegacyInteropName(interopName);

                //find interop interface by name
                //TODO: use interface map to this searches advantage somehow
                for(AbstractInteropInterface iFace : interops.values()){

                    if(iFace.getClass().getName().contains(interopName)){
                        //found

                        if(iFace.isAvailable(true)){
                            if(logger.isDebugEnabled()){
                                logger.debug("adding interface of '"+iFace.getClass().getName()+"' to be enabled.");
                            }
                            interfacesToEnable.add(iFace);
                            supportedMessages.addAll(iFace.getSupportedMessageTypes());
                        }else{
                            logger.error("The interop interface of "+iFace+" is not available.  Therefore the Gateway module can't enable it or even use it.  " +
                                    "To make it available please make sure the avaiable value for that interface in the "+GatewayModuleProperties.getInstance().getInteropConfig()+" is set to true and that there are no" +
                                    " errors in the Gateway module log related to this interop plugin.");
                        }

                        //ignore name collisions, duplicates, etc.
                        break;
                    }

                }//end for
            }//end for

        }catch(Exception e){
            logger.error("Caught exception while trying to gather the list of available interop connections.", e);

            //to signal that a complete collection of interop interfaces being requested is not available, its an all or nothing situation
            interfacesToEnable.clear();
        }

        if(interfacesToEnable.size() == initializeInteropConnections.getInterops().size()){
            // all requested interops were found to exist and can be enabled

            InteropConnectionsInfo interopConnectionsInfo = new InteropConnectionsInfo();
            interopConnectionsInfo.addSupportedMessageTypes(supportedMessages);

            try{

                if(!interfacesToEnable.isEmpty() || initializeInteropConnections.getRequestingObserver() != null){
                    // handle the requested interops OR allow disabling of all interops if the request is coming from an Observer

                    /*
                     * First disable all interops because the interops that need to be enabled could already be enabled from previous course executions.
                     */
                    if(logger.isInfoEnabled()){
                        logger.info("Attempting to ensure all interop interfaces are disabled.");
                    }
                    for(AbstractInteropInterface iFace : interops.values()){

                        try{
                            if(iFace.isAvailable(false) && iFace.hasObserverControllerPermission(requestingObserver)){
                                if(logger.isInfoEnabled()){
                                    logger.info("Attempting to disable interop interface: "+iFace);
                                }
                                iFace.setEnabled(false, initializeInteropConnections.isPlayback());
                                if(logger.isInfoEnabled()){
                                    logger.info("Disabled interop interface: "+iFace);
                                }
                            }

                        }catch(Exception e){
                            logger.error("There was a problem trying to disable the interop interface of "+iFace+".  This means that this interface could produce training application messages when it shouldn't which could affect Domain module assessments.  The Gateway module is now in a broken state and should be restarted.", e);
                            throw new ConfigurationException("The '"+iFace.getName()+"' interop interface could not be disabled.",
                                    "The interop plugin had an exception - "+ (e.getMessage() != null ? e.getMessage() : e.toString()),
                                    e);
                        }
                    }//end for

                    List<TrainingApplicationEnum> reqTrainingAppConfigs = new ArrayList<>();

                    HandlesDisDialect disHandler = null;
                    DISDialect disDialect = null;

                    //enable the specific interop interfaces
                    for(AbstractInteropInterface iFace : interops.values()){

                        if(interfacesToEnable.contains(iFace)){

                            try{
                                if(iFace.isAvailable(true)){

                                    reqTrainingAppConfigs.addAll(iFace.getReqTrainingAppConfigurations());

                                    if(enableInterops){
                                        if(logger.isInfoEnabled()){
                                            logger.info("Attempting to enable interop interface of "+iFace);
                                        }
                                        iFace.setEnabled(true, initializeInteropConnections.isPlayback());
                                        if(logger.isInfoEnabled()){
                                            logger.info("Enabled interop interface: "+iFace);
                                        }

                                        if(requestingObserver != null) {
                                            iFace.addObserverController(requestingObserver);
                                        }
                                    }

                                    iFace.setDomainContentServerAddress(initializeInteropConnections.getDomainServerAddress());

                                }else{
                                    //This is a big problem because this interop interface is needed for the current part of the course
                                    throw new Exception("The "+iFace.getName()+" interop interface is not available.");
                                }

                            }catch(Throwable e){
                                logger.error("There was a problem trying to enable the interop interface of "+iFace+".  This interface is critical to the current part of the course, therefore the course should not be able to continue.", e);
                                throw new ConfigurationException("The "+iFace.getName()+" interop interface is not available.",
                                        e.getMessage(),
                                        e);
                            }
                        }

                        if(iFace.isEnabled()) {
                            if(iFace instanceof HandlesDisDialect) {
                                disHandler = (HandlesDisDialect) iFace;

                            } else {

                                DISDialect thisDialect = iFace.getDisDialect();

                                if(thisDialect != null) {
                                    disDialect = thisDialect;
                                }
                            }
                        }

                    }//end for

                    if(disHandler != null) {

                        //use the appropriate DIS dialect if an interface requires a modified one for DIS communication
                        disHandler.setDisDialect(disDialect);
                    }

                    if(logger.isInfoEnabled()){
                        logger.info("Enabled "+interfacesToEnable.size()+" interop connections");
                    }

                    if(needInstaller){

                        //Threading this to allow the module to continue to receive messages (specifically close domain session)
                        //while the user is configuring
                        Thread installThread = new Thread(new Runnable() {

                            @Override
                            public void run() {

                                //Display configuration dialog for the training applications specified
                                runningInstall = new InstallMain(reqTrainingAppConfigs);

                                if(logger.isInfoEnabled()){

                                    if(msg instanceof UserSessionMessage) {
                                        logger.info("Starting GW installer for user "+ ((UserSessionMessage) msg).getUserId()+".");

                                    } else {
                                        logger.info("Starting GW installer for system.");
                                    }
                                }

                                haveDisplayedInstaller = true;

                                if(runningInstall.show()){
                                    if(logger.isInfoEnabled()){
                                        logger.info("The installation logic completed.");
                                    }
                                    sendReply(msg, interopConnectionsInfo, MessageTypeEnum.INTEROP_CONNECTIONS_INFO);
                                }else{
                                    logger.error("The installation logic didn't complete, therefore notifying the domain module to terminate the course.");
                                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "The Gateway module installer didn't complete.");
                                    nack.setErrorHelp("If you canceled/closed the Gateway module installer before completing the necessary steps you can ignore this message.  Otherwise, if you have access to the Gateway module log file please check it for additional help.");
                                    sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
                                }

                                runningInstall = null;
                            }
                        }, "GW Install - " + (msg instanceof UserSessionMessage
                                ? "User " + ((UserSessionMessage) msg).getUserId()
                                : "System"));
                        installThread.start();

                    }else{
                        sendReply(msg, interopConnectionsInfo, MessageTypeEnum.INTEROP_CONNECTIONS_INFO);
                    }

                }else{
                    logger.warn("No interop interface connections were enabled because there were none referenced in the init interop connection message");
                    sendReply(msg, interopConnectionsInfo, MessageTypeEnum.INTEROP_CONNECTIONS_INFO);
                }

            }catch(Throwable e){
                //a critical error has happened and the course should stop execution
                logger.error("Caught an exception while trying to initialize interop connections, therefore the course should stop execution.", e);

                if(GatewayModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "There was a problem handling the initialization of the Gateway plugin interop connections needed for this course.");
                    nack.setErrorHelp("An exception occured in the Gateway module (check the Java Web Start log for more details) with the message:\n"+e.getMessage()+
                            "\n\nTechnical Note: the following Gateway module interop plugins where being initialized:\n"+initializeInteropConnections.getInterops());
                    sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
                }else{
                    NACK nack = new NACK(ErrorEnum.OPERATION_FAILED, "There was a problem handling the initialization of the Gateway plugin interop connections needed for this course.");
                    nack.setErrorHelp("An exception occured in the Gateway module (check the log for more details) with the message:\n"+e.getMessage()+
                            "\n\nTechnical Note: the following Gateway module interop plugins where being initialized:\n"+initializeInteropConnections.getInterops());
                    sendReply(msg, nack, MessageTypeEnum.PROCESSED_NACK);
                }
            }

        }else{

            //ERROR - there aren't enough enable interop interfaces in this Gateway module.  Determine the ones that are not
            //available and provide a useful error message.
            StringBuilder missingInterops = new StringBuilder();
            boolean found;
            for(String requestedInterop : initializeInteropConnections.getInterops()){

                found = false;
                for(AbstractInteropInterface availableInterop : interfacesToEnable){

                    if(availableInterop.getClass().getName().contains(requestedInterop)){
                        found = true;
                        break;
                    }
                }

                if(!found){
                    missingInterops.append(requestedInterop).append("\n");
                }
            }

            logger.error("The following Gateway module interop plugins are being requested for the current course but are not available:\n"+
                    missingInterops.toString()+
                    "Please see earlier ERROR messages regarding those plugins because they are most likely disabled.\n"
                    + "Requesting message is\n"+msg+".");


            if(GatewayModuleProperties.getInstance().getDeploymentMode() == DeploymentModeEnum.SERVER){
                sendReply(msg,
                        new NACK(ErrorEnum.MALFORMED_DATA_ERROR,
                                "The following Gateway module interop plugins are being requested for the current course but are not available:\n"+
                                        missingInterops.toString()+
                                        "This means the Gateway module can't enable it or even use them."),
                                MessageTypeEnum.PROCESSED_NACK);
            }else{
                sendReply(msg,
                        new NACK(ErrorEnum.MALFORMED_DATA_ERROR,
                                "The following Gateway module interop plugins are being requested for the current course but are not available:\n"+
                                        missingInterops.toString()+
                                        "This means the Gateway module can't enable it or even use them.  " +
                                        "To make those plugins available please make sure the 'avaiable' value for that interface in the "+GatewayModuleProperties.getInstance().getInteropConfig()+" is set to true and that there are no" +
                                        " errors in the Gateway module log related to this interop plugin."),
                                MessageTypeEnum.PROCESSED_NACK);
            }
        }


    }

    /**
     * Handle the start domain session request message
     *
     * @param msg The {@link Message} to handle.
     */
    private void handleStartDomainSessionRequestMessage(Message msg) {
        this.sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
    }

    /**
     * Handle the close domain session request message
     *
     * @param msg The {@link Message} to handle.
     */
    private void handleCloseDomainSessionRequestMessage(Message msg) {

        if(logger.isInfoEnabled()){
            logger.info("Received close domain session request message.  Cleaning up the Gateway module.");
        }

        try{
            handleMessageFromGIFT(msg);
        }catch(Throwable t){
            logger.error("Caught unhandled error when attempting to notify the active interops about the close domain session request.", t);
        }

        //reset
        UserSessionMessage userSessionMsg = (UserSessionMessage) msg;
        UserSession user = userSessionMsg.getUserSession();
        try{
            releaseDomainSessionModules(user);
            disableAllInterops();
        }catch(Exception e){
            logger.error("Caught exception while trying to release domain session module connections and turning off all active interops.", e);
        }
        try{
            setUserSession(null);
        }catch(Exception e){
            logger.error("Caught exception while trying to reset gateway module user session", e);
        }
        try{
            setDomainSession(null);
        }catch(Exception e){
            logger.error("Caught exception while trying to reset gateway module domain session", e);
        }

        //close any currently executing installer
        if(runningInstall != null){
            runningInstall.close("the course is ending");
        }

        //close the JWS gateway module instance at the end of the course
        if (GatewayModuleProperties.getInstance().isRemoteMode()) {
            if(logger.isInfoEnabled()){
                logger.info("The received close domain session request message will close the Gateway module because it is running in JWS mode.");
            }
            killModule();
        }
    }

    @Override
    public void sendModuleStatus(){
        sendMessage(SubjectUtil.GATEWAY_DISCOVERY_TOPIC, gatewayStatus, MessageTypeEnum.GATEWAY_MODULE_STATUS, null);
    }

    /**
     * Handle the GIFT message by giving it to the appropriate active interop interface's networks
     *
     * @param message - incoming message from GIFT message bus to handle
     */
    public void handleMessageFromGIFT(Message message){

        if(logger.isDebugEnabled()){
            logger.debug("Sending GIFT message through interop interfaces, message = "+message);
        }

        final MessageTypeEnum messageType = message.getMessageType();

        //whether the message has been replied too by one of the interop interfaces
        boolean replied = false;
        //whether at least one interop interface was given the message to handle
        boolean handled = false;
        // whether the sender of the message is waiting for a reply message
        boolean needsReply = message.needsHandlingResponse();

        StringBuilder errorMsg = new StringBuilder();
        for(AbstractInteropInterface interopInterface : interops.values()){

            if(interopInterface.isEnabled()) {
                
                if (interopInterface.getSupportedMessageTypes().contains(messageType)){
                    handled = true;
    
                    try{
                        replied |= interopInterface.handleGIFTMessage(message, errorMsg);
    
                    }catch(ConfigurationException e) {
                        logger.error(e.getReason(), e);
    
                        //build reason/details
                        errorMsg.append(e.getReason()).append(" because ").append(e.getDetails());
    
                    } catch(Throwable t){
                    	logger.error("Caught exception from misbehaving interop interface of "+interopInterface+".", t);
                        errorMsg.append("The interop interface of ").append(interopInterface.getName()).append(" threw an exception when dealing with a ").append(messageType).append(" message.");
                    }
                }
                
                // Attempt to disable the enabled interop now that the lesson was completed (dkf completed)
                // so that messages (e.g. DIS traffic) aren't being received from the external system (e.g. VBS)
                // when GIFT doesn't need them.  (#5061)
                try {
                    if (messageType == MessageTypeEnum.LESSON_COMPLETED) {
                        interopInterface.setEnabled(false);
                    }
                } catch(Throwable t) {
                    logger.error("Caught throwable when setting the enabled value of "+ interopInterface.getName() + " to false in response to a Lesson Completed message.",t);
                    errorMsg.append("The interop interface of ").append(interopInterface.getName()).append(" threw a throwable when dealing with a ").append(messageType).append(" message.");
                }
    
                if(errorMsg.length() > 0){
                    //there was an error, send NACK
                    sendReply(message, new NACK(ErrorEnum.OPERATION_FAILED, errorMsg.toString()), MessageTypeEnum.PROCESSED_NACK);
                    replied = true;
                    break;
                }
            }
        }

        final boolean isOptionalMsg = OPTIONAL_MSG_TYPES.contains(messageType);
        if ((handled || isOptionalMsg) && needsReply && !replied) {
            //at least one interop interface was given the request, but none have responded to the request and there were no errors

            //send an ACK reply to let it
            sendReply(message, new ACK(), MessageTypeEnum.PROCESSED_ACK);

        } else if (!handled && !isOptionalMsg) {
            // no interop interfaces were given the request because none support
            // this message type

            logger.error("Didn't handle message from GIFT of " + message
                    + " with the current set of enabled interop interfaces.  "
                    + "Is the appropriate interface enabled for this part of the course and does that interface implementation support messages of this type (type = "
                    + messageType + ").");

            // send NACK
            sendReply(message,
                    new NACK(ErrorEnum.OPERATION_FAILED,
                            "There are no interop interfaces that can handle this message"),
                    MessageTypeEnum.PROCESSED_NACK);
        }
    }

    /**
     * Send the simulation message that has been translated into a GIFT message over the GIFT network
     *
     * @param payload - the message payload to send
     * @param messageType - the type of message being sent
     * @param interop - plugin requesting the message to be sent
     */
    public synchronized void sendMessageToGIFT(TrainingAppState payload, MessageTypeEnum messageType, AbstractInteropInterface interop){

        if(interop.isEnabled()){
            sendMessageToGIFT(getDomainSession(), getDomainSession().getDomainSessionId(), getDomainSession().getExperimentId(), payload, messageType, interop);

        }else{
            if(logger.isInfoEnabled()){
                logger.info("Not sending message of type "+messageType+" to GIFT because interop interface is not enabled: "+interop);
            }
        }
    }

    /**
     * Send a reply message to a GIFT message over the GIFT network
     *
     * @param messageReplyingToo - the GIFT message being replied too
     * @param payload - the message payload to send
     * @param messageType - the type of message being sent
     * @param interop - plugin requesting the message to be sent
     */
    public synchronized void sendReplyMessageToGIFT(Message messageReplyingToo, Object payload, MessageTypeEnum messageType, AbstractInteropInterface interop){

        if(interop.isEnabled()){
            if(logger.isDebugEnabled()){
                logger.debug("Sending translated simulation message to GIFT with payload = "+payload);
            }

            sendReply(messageReplyingToo, payload, messageType);

        }else{
            if(logger.isInfoEnabled()){
                logger.info("Not sending message of type "+messageType+" to GIFT because interop interface is not enabled: "+interop);
            }
        }
    }

    /**
     * Send the simulation message that has been translated into a GIFT message over the GIFT network
     *
     * @param userSession - information about the user session (including the unique user id of the learner)
     *                      the message is associated with
     * @param domainSessionId - unique domain session id associated with this message being sent
     * @param experimentId unique id for an experiment.  Can be null if this domain session is not an experiment instance.
     * @param payload - the message payload to send
     * @param messageType - the type of message being sent
     * @param interop - plugin requesting the message to be sent
     */
    public synchronized void sendMessageToGIFT(UserSession userSession, int domainSessionId, String experimentId, TrainingAppState payload, MessageTypeEnum messageType, AbstractInteropInterface interop){

        if(interop.isEnabled()){
            if(logger.isDebugEnabled()){
                logger.debug("Sending translated simulation message to GIFT with payload = "+payload);
            }

            sendDomainSessionMessage(gatewayTopicName, payload, userSession, domainSessionId, experimentId, messageType, null);

        }else{
            if(logger.isInfoEnabled()){
                logger.info("Not sending message of type "+messageType+" to GIFT because interop interface is not enabled: "+interop);
            }
        }
    }

    /**
     * Gets a user session for a specified user.
     *
     * @param username The {@link String} name of the user for which the
     *        {@link UserSession} is being created.
     * @return A {@link CompletableFuture} that completes when the
     *         {@link UserSession} has been created.
     */
    private CompletableFuture<UserSession> startUserSession(String username) {
        /* Confirm that another request is not already trying to start a user
         * session. */
        final boolean alreadyStarting = this.isStartingUserSesssion.getAndSet(true);
        if (alreadyStarting) {
            final String exMsg = "Unabled to start a new user session for '" + username
                    + "' because another user session is already being started.";
            final Exception ex = new Exception(exMsg);
            final CompletableFuture<UserSession> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(ex);
            return failedFuture;
        }

        /* Get the user session and then save it as the current user session.
         * Once that is complete flip the start flag to signal that the start
         * has completed. */
        return getUserSessionFor(username).thenApply(userSession -> {
            try {
                setUserSession(userSession);
                return userSession;
            } catch (Exception ex) {
                throw new RuntimeException("There was a problem starting the user session.", ex);
            }
        }).whenComplete((result, err) -> isStartingUserSesssion.set(false));
    }

    /**
     * Sends a request to create a new user to the UMS module.
     *
     * <b>NOTE:</b> Make sure a UMS module has been selected first.
     *
     * @param username The {@link String} value of the username.
     * @return A {@link CompletableFuture} that will complete when the
     *         {@link UserData} in the UMS module.
     */
    private CompletableFuture<UserSession> getUserSessionFor(String username) {
        return sendMessageAsync(username, MessageTypeEnum.USER_ID_REQUEST).thenCompose(msg -> {
            final UserData userData = (UserData) msg.getPayload();
            final int userId = userData.getUserId();

            final UserSession userSession = new UserSession(userId);
            userSession.setUsername(userData.getUsername());

            final CompletableFuture<UserSession> future = new CompletableFuture<>();

            if(haveSubjectClient(userSession, ModuleTypeEnum.DOMAIN_MODULE)) {
                
                if(logger.isDebugEnabled()){
                    logger.debug("Already have a connection to the Domain module for "+userSession);
                }

                /* If the domain module has already been selected for this user session, don't try to select it again.
                 * Technically, NetworkSession already does this for us when selecting a module, but it also has additional
                 * logic that iterates over all the connected modules and their module statuses afterward. The Gateway
                 * inherits the Ped connection from the Domain but doesn't ever allocate it, so this causes an exception
                 * to get thrown because the Gateway isn't listening to the Ped's heartbeats and doesn't have a module
                 * status for it as a result. Since the Gateway doesn't actually need the Ped, the simplest way to avoid the
                 * error is just to avoid trying to re-select the domain module.  */
                future.complete(userSession);

            } else {

                selectModule(userSession, ModuleTypeEnum.DOMAIN_MODULE, new MessageCollectionCallback() {

                    @Override
                    public void success() {
                        
                        if(logger.isDebugEnabled()){
                            logger.debug("Successfully selected Domain module for "+userSession);
                        }
                        future.complete(userSession);
                    }

                    @Override
                    public void received(Message msg) {
                    }

                    @Override
                    public void failure(String why) {
                        future.completeExceptionally(
                                new Exception("There was a problem selecting the domain module: " + why));
                    }

                    @Override
                    public void failure(Message msg) {
                        failure(msg.toString());
                    }
                });
            }

            return future;
        });
    }

    /**
     * Sends a {@link DomainOptionsRequest} to the domain module.
     *
     * @param username The {@link String} name of the user who is making the
     *        request.
     * @param request The {@link DomainOptionsRequest} specifying which user is
     *        making the request.
     * @return A {@link CompletableFuture} that will complete when the
     *         {@link DomainOptionsList} is received from the domain module.
     */
    public CompletableFuture<DomainOptionsList> sendDomainOptionsRequest(String username, DomainOptionsRequest request) {
        request.getWebClientInformation().setClientAddress(ipaddr);
        return getUserSessionFor(username).thenCompose(userSession -> {
            
            if(logger.isDebugEnabled()){
                logger.debug("Sending request for domain options to domain module for "+username);
            }
            return sendUserSessionMessage(request, userSession, MessageTypeEnum.DOMAIN_OPTIONS_REQUEST);
        }).thenApply(msg -> {
            if (msg.getMessageType() == MessageTypeEnum.DOMAIN_OPTIONS_REPLY) {
                
                if(logger.isDebugEnabled()){
                    logger.debug("Received domain options list message from domain module for "+username);
                }
                return (DomainOptionsList) msg.getPayload();
            } else {
                throw new IllegalArgumentException("Received a message of unexpected type: " + msg.getMessageType());
            }
        });
    }

    /**
     * Sends a {@link DomainSelectionRequest} to the domain module.
     *
     * @param username The {@link UserSession} of the user who is making the
     *        request.
     * @param domainSelectionRequest The {@link DomainSelectionRequest} that
     *        specifies which course to start.
     * @return A {@link CompletableFuture} that will complete when the
     *         {@link DomainSession} is received from the domain module.
     */
    public CompletableFuture<DomainSession> sendDomainSelectionRequest(String username, DomainSelectionRequest domainSelectionRequest) {
        final DomainSession domainSession = getDomainSession();
        domainSelectionRequest.getClientInformation().setClientAddress(ipaddr);
        final boolean alreadyStarting = isStartingDomainSession.getAndSet(true);
        if (alreadyStarting) {
            CompletableFuture<DomainSession> failedFuture = new CompletableFuture<>();
            Throwable t = new Exception("A domain session is already in the process of starting. "
                    + "Only one domain session may run in a Gateway module at a time. "
                    + "Another domain session cannot be started until the current one has completed or is stopped.");
            failedFuture.completeExceptionally(t);
            return failedFuture;
        } else if (domainSession != null) {
            CompletableFuture<DomainSession> failedFuture = new CompletableFuture<>();
            Throwable t = new Exception(
                    "A domain session has already started. "
                            + "Another domain session cannot be started until this one has completed. "
                            + "Current domain session is " + domainSession);
            failedFuture.completeExceptionally(t);
            isStartingDomainSession.set(false);
            return failedFuture;
        }

        return startUserSession(username).thenCompose(userSession -> {
            return sendUserSessionMessageAsync(domainSelectionRequest, userSession,
                    MessageTypeEnum.DOMAIN_SELECTION_REQUEST).thenApply(msg -> {
                        final DomainSession startedDomainSession = (DomainSession) msg.getPayload();
                        setDomainSession(startedDomainSession);

                        if (msg.needsHandlingResponse()) {
                            sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
                        }

                        return startedDomainSession;
                    });
        }).whenComplete((result, err) -> isStartingDomainSession.set(false));
    }

    /**
     * Sends an {@link MessageTypeEnum#ACTIVE_DOMAIN_SESSIONS_REQUEST active
     * domain sessions request message} to the domain module.
     *
     * @return A {@link CompletableFuture} that will complete when the
     *         {@link DomainSessionList} is received from the domain module.
     */
    public CompletableFuture<DomainSessionList> sendActiveDomainSessionsRequest() {
        final List<ModuleStatus> lastStatuses = ModuleStatusMonitor.getInstance()
                .getLastStatus(ModuleTypeEnum.DOMAIN_MODULE);
        final ModuleStatus status = lastStatuses == null || lastStatuses.isEmpty() ? null : lastStatuses.get(0);

        if (status == null) {
            CompletableFuture<DomainSessionList> future = new CompletableFuture<>();
            future.completeExceptionally(
                    new Exception("No Domain Module is available to which to send an ACTIVE_DOMAIN_SESSIONS_REQUEST."));
            return future;
        }

        /* Creates the connection to the domain module if it does not already
         * exist. */
        final String queueName = status.getQueueName();
        final ModuleTypeEnum moduleType = status.getModuleType();
        if (!haveSubjectClient(queueName)) {
            createSubjectQueueClient(queueName, moduleType, false);
        }

        // Note: there was code here that removed the DM client connection but it was being executed at the same time
        // that another thread was trying to request the domain options from the DM on behalf of the RTA test harness.
        return sendMessageAsync(null, MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REQUEST).thenApply(msg -> {
            if (msg.needsHandlingResponse()) {
                sendReply(msg, new ACK(), MessageTypeEnum.PROCESSED_ACK);
            }

            if (msg.getMessageType() == MessageTypeEnum.ACTIVE_DOMAIN_SESSIONS_REPLY) {
                return (DomainSessionList) msg.getPayload();
            } else {
                throw new IllegalArgumentException("Received a message of unexpected type: " + msg.getMessageType());
            }
        });
    }

    /**
     * Sends a message to the domain module requesting the creation of a new
     * {@link TeamKnowledgeSession}.
     *
     * @return A {@link CompletableFuture} that completes once a reply is
     *         received to the message. Can't be null.
     */
    public CompletableFuture<DomainSessionMessage> sendCreateTeamKnowledgeSession() {
        final int domainSessionId = getDomainSession().getDomainSessionId();
        ManageTeamMembershipRequest request = ManageTeamMembershipRequest
                .createHostedTeamKnowledgeSessionRequest(domainSessionId, "Session");
        return sendDomainSessionMessageAsync(request, getDomainSession(),
                MessageTypeEnum.MANAGE_MEMBERSHIP_TEAM_KNOWLEDGE_SESSION);
    }

    /**
     * Sends a message to the domain module updating the knowledge sessions
     * there.
     *
     * @param reply The {@link KnowledgeSessionsReply} that is used to update
     *        the sessions in the domain module. Can't be null.
     * @return A {@link CompletableFuture} that completes once a reply is
     *         received to the request.
     */
    public CompletableFuture<DomainSessionMessage> sendKnowledgeSessionUpdateRequest(KnowledgeSessionsReply reply) {
        
        // to the LMS for recording session details and team member assignments
        sendDomainSessionMessage(ModuleTypeEnum.LMS_MODULE, reply, getUserSession(), getDomainSession().getDomainSessionId(), MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST, null);
        
        // to the DM to update the domain module of the knowledge session
        return sendDomainSessionMessageAsync(ModuleTypeEnum.DOMAIN_MODULE, reply, getUserSession(),
                getDomainSession().getDomainSessionId(), MessageTypeEnum.KNOWLEDGE_SESSION_UPDATED_REQUEST);
    }

    /**
     * Sends an {@link ApplyStrategies} message to the domain module.
     *
     * @param applyStrategies The {@link ApplyStrategies} payload that instructs
     *        the domain module on which strategies need to be applied.
     */
    public void sendApplyStrategiesRequest(ApplyStrategies applyStrategies) {
        final DomainSession domainSession = getDomainSession();
        sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, applyStrategies, domainSession,
                domainSession.getDomainSessionId(), MessageTypeEnum.APPLY_STRATEGIES, null);
    }

    /**
     * Sends a {@link CloseDomainSessionRequest} to the domain module.
     *
     * @param closeDomainSessionRequest The {@link CloseDomainSessionRequest}
     *        that contains the reason for closing the session.
     */
    public void sendCloseDomainSessionRequest(CloseDomainSessionRequest closeDomainSessionRequest) {
        final DomainSession domainSession = getDomainSession();

        /* If there is no active domain session, we are unable to close the
         * domain session and no action can be taken. */
        if (domainSession == null) {
            return;
        }

        sendDomainSessionMessage(ModuleTypeEnum.DOMAIN_MODULE, closeDomainSessionRequest, domainSession,
                domainSession.getDomainSessionId(), MessageTypeEnum.CLOSE_DOMAIN_SESSION_REQUEST, null);
    }
    
    /**
     * Sends a {@link EvaluatorUpdateRequest} to the domain module.
     * 
     * @param evaluatorUpdateRequest the {@link EvaluatorUpdateRequest} that contains
     * evaluator updates to apply in the ongoing session (e.g. concept performance assessment, bookmark)
     */
    public void sendEvaluatorUpdateRequest(EvaluatorUpdateRequest evaluatorUpdateRequest){
        
        final DomainSession domainSession = getDomainSession();
        sendDomainSessionMessage(evaluatorUpdateRequest, domainSession,
                domainSession.getDomainSessionId(), MessageTypeEnum.EVALUATOR_UPDATE_REQUEST, null);
    }

    @Override
    protected void cleanup() {
        super.cleanup();

        if(logger.isInfoEnabled()){
            logger.info("cleaning up interop interface(s)");
        }

        for(AbstractInteropInterface interopInterface : interops.values()){

            try{
                if(interopInterface.isAvailable(false)){
                    interopInterface.setEnabled(false);
                    interopInterface.cleanup();
                }
            }catch(Throwable e){
                logger.error("Caught exception from misbehaving interop interface of "+interopInterface+" while trying to cleanup.", e);
            }
        }

        //clear the collection so they can't be cleaned again
        interops.clear();
    }

    /**
     * Used to run the gateway module
     *
     * @param args - launch module arguments
     */
    public static void main(String[] args) {
        ModuleModeEnum mode = checkModuleMode(args);
        GatewayModuleProperties.getInstance().setCommandLineArgs(args);

        String clientId = GatewayModuleProperties.getInstance().getClientId();
        if(clientId != null){
            System.out.println("your client id is "+clientId);
            Unique_Queue_Token = clientId;
        }else{
            Unique_Queue_Token = ipaddr;
        }

        if (GatewayModuleProperties.getInstance().isRemoteMode()) {
            //Note: this thread will end gracefully after instantiating the GW module instance

            GatewayModule gModule = null;
            GatewayTray gwTray = null;
            try{
                gModule = GatewayModule.getInstance();
                gModule.init();
                gwTray = GatewayTray.getInstance();
                gwTray.displayAlert();

                //start the timer thread as we should be hearing from the domain module shortly
                new JWSPresentInstallerTimer().start();
            }catch(ModuleConnectionConfigurationException moduleConnectionException){

                logger.error("Caught exception while running the JWS Gateway module.", moduleConnectionException);

                JOptionPane.showMessageDialog(null,
                            "The Gateway Module had a problem connecting to the GIFT message bus at "+GatewayModuleProperties.getInstance().getBrokerURL()+".\n\n"
                            + "Does that address point to the machine running the GIFT message bus?\n"
                            + "If not please update the 'ActiveMQURL' property in 'GIFT\\config\\module.common.properties' with the\n"
                            + "appropriate network address (i.e. change 'localhost' to '10.1.21.13').  Then restart the GIFT server instance and try again.\n\n"
                            + "Please check the latest Java Web Start logs for more information.  For Windows 7 this can be found at:\n"
                            + "'%APPDATA%\\LocalLow\\Sun\\Java\\Deployment\\log\\'   (e.g. C:\\Users\\jsmith\\AppData\\LocalLow\\Sun\\Java\\Deployment\\log).\n\n"
                            + "Please use the GIFT forums for additional troubleshooting.",
                            "Gateway Module Error",
                            JOptionPane.ERROR_MESSAGE);

            }catch(Exception e){

                logger.error("Caught exception while running the JWS Gateway module.", e);

                JOptionPane.showMessageDialog(null,
                            "The Gateway Module had a severe error and needs to close.\n\n"
                            + "Error message : "+e.getMessage()+"\n\n"
                            + "Please check the latest Java Web Start logs for more information.  For Windows 7 this can be found at:\n"
                            + "'%APPDATA%\\LocalLow\\Sun\\Java\\Deployment\\log\\'    (e.g. C:\\Users\\jsmith\\AppData\\LocalLow\\Sun\\Java\\Deployment\\log).\n\n"
                            + "Please use the GIFT forums for additional troubleshooting.",
                            "Gateway Module Error",
                            JOptionPane.ERROR_MESSAGE);

                if(gModule != null){
                    gModule.cleanup();

                    if(gwTray != null){
                        gwTray.clean();
                    }
                }

            }
        }else{

            //currently it doesn't make sense to have/need a shutdown hook when running the GW module
            //in a Java Web Start (JWS) environment
            //...in case the GW module closes harshly (i.e. not gracefully with a kill message) we
            //still want the cleanup logic to happen
            Runtime.getRuntime().addShutdownHook(new Thread("Gateway module Shutdown Hook"){

                @Override
                public void run(){

                    //Clean up the interops
                    try{
                        GatewayModule gModule = GatewayModule.getInstance();

                        try {
                            gModule.cleanup();

                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }catch(Exception managerException){
                        managerException.printStackTrace();
                    }
                }
            });

            GatewayModule gModule = null;
            try{
                gModule = GatewayModule.getInstance();
                gModule.init();
                gModule.setModuleMode(mode);
                gModule.showModuleStartedPrompt();  //holds onto the thread
                gModule.cleanup();

            }catch(Exception e){

                logger.error("Caught exception while running Gateway module.", e);

                if(mode == ModuleModeEnum.LEARNER_MODE){
                    //this mode doesn't have a console to print too
                    JOptionPane.showMessageDialog(null,
                            "The Gateway Module had a severe error.  Check the latest gateway module log file for more information.",
                            "Gateway Module Error",
                            JOptionPane.ERROR_MESSAGE);
                }else{

                    System.err.println("The Gateway Module threw an exception.");
                    e.printStackTrace();

                    JOptionPane.showMessageDialog(null,
                            "The Gateway Module had a severe error.  Check the log file and the console window for more information.",
                            "Gateway Module Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                if(gModule != null){
                    gModule.cleanup();
                }

                //this allows the SPL to be launched again w/o having to wait for SPL logic to timeout
                showModuleUnexpectedExitPrompt(mode);
            }

            if(mode.equals(ModuleModeEnum.POWER_USER_MODE)) {
                System.out.println("Good-bye");
                //kill any threads
                System.exit(0);
            }
        }  //end else


    }// end main

    /**
     * This inner class is used to kill a Java Web Start Gateway module instance if,
     * after being started by the user, the domain hasn't ordered the Gateway installer
     * dialog to appear via the configure interops message.
     *
     * This is an attempt at trying to solve the issue of JWS GW modules remaining in system tray
     * #2094
     *
     * @author mhoffman
     *
     */
    private static class JWSPresentInstallerTimer extends Thread{

        @Override
        public void run(){

            long sleepTime = GatewayModuleProperties.getRemotePresentInstallerTimeout();

            try {
                sleep(sleepTime);
            } catch (InterruptedException e) {
                logger.error("The wait was interrupted therefore not sure what user experience will happen next.  This JWS GW module may terminate prematurely.", e);
            }

            //Has Gateway been connected to Domain (domain sent module allocation request)
            //Has Gateway display the installer dialog (ordered by domain)
            if(!GatewayModule.getInstance().haveDisplayedInstaller){
                logger.error("Closing the Java Web Start Gateway module application after waiting "+sleepTime+" ms to connect to the GIFT Domain module.");

                JOptionPaneUtil.showConfirmDialog(
                        "The GIFT Communication Application (Gateway module) has not heard from the GIFT server since being started.\n"
                        + "Therefore it will be closing in order to prevent multiple running concurrent instances on your machine.\n"
                        + "If this happened while running a GIFT course than you will most likely need to restart the course.\n\n"
                        + "Please check that your network connection can still reach the GIFT Server.\n\n"
                        + "This can also be caused by your network configuration blocking the port used in this URL:\n"+GatewayModuleProperties.getInstance().getBrokerURL()+"\n\n"
                        + "Another problem could be that your network connection is not fast enough and the Gateway running on your machine\n"
                        + "is timing out.\n\n"
                        + "Developer Note: If you have access to the Domain module log file, please check it for more information.",
                        "GIFT Communication Application Closing", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
                GatewayModule.getInstance().killModule();
            }
        }
    }
}
