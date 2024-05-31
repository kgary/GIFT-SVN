/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition.autotutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.LearnerActionEnumType;
import mil.arl.gift.common.ChatLog;
import mil.arl.gift.common.DisplayChatWindowRequest;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.ConditionDescription.InlineDescription;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.domain.DomainModuleProperties;
import mil.arl.gift.domain.knowledge.common.AutoTutorSurveyLessonAssessment;
import mil.arl.gift.domain.knowledge.common.ConditionActionInterface;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface.ActionsResponse;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface.AssessmentResponse;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterface.Script_Reference_Type;
import mil.arl.gift.net.api.message.Message;

/**
 * This condition communicates with the Auto Tutor Webservice (ATWS) to assess the chat log and
 * get a list of actions to take.
 *
 * @author mhoffman
 *
 */
public class AutoTutorWebServiceInterfaceCondition extends AbstractCondition {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AutoTutorWebServiceInterfaceCondition.class);

    /**
     * contains the types of GIFT messages this condition needs in order to
     * provide assessments
     */
    private static final List<MessageTypeEnum> simulationInterests;

    static {
        simulationInterests = new ArrayList<MessageTypeEnum>();
        simulationInterests.add(MessageTypeEnum.CHAT_LOG);
    }
    
    /**
     * contains the type of overall assessment scorers this condition can populate for an AAR
     */
    private static final Set<Class<?>> overallAssessmentTypes;
    static{
        overallAssessmentTypes = new HashSet<Class<?>>();
    }

    /** information about the purpose of this condition */
    private static final ConditionDescription DESCRIPTION = new InlineDescription("Communicates with AutoTutor webservice to assess chat "
            + "log messages and retrieve a list of actions to take.", "AutoTutor Conversation");

    /** interface to the AT WS */
    private AutoTutorWebServiceInterface autoTutorSession = null;

    /** the name of script */
    private String scriptNameOrURL;

    /** whether the script is remote to the AT WS or not. */
    private Script_Reference_Type scriptRefType;

    /** stores the last chat log to (possibly) use later to retrieve the actions list from the AT WS */
    private ChatLog queuedChatLog = null;

    /** flag used to indicate if a chat message has been received yet */
    private boolean isFirstMessage = true;

    /** whether the autotutor session is active (e.g. has the seed prompt been given) */
    private boolean activeSession = false;

    /** logic for handling chat logs in a different thread */
    private Thread handlerThread;
    private volatile boolean keepHandling = true;
    private List<ChatLog> chatLogQueue = new ArrayList<ChatLog>();

    /**
     * unique id representing a chat session for this AutoTutor condition
     * Note: an unknown id is used when the "Present Survey AT session" is a top level course element because
     *       the system only knows that a DKF exists and there should only be 1 AT condition in it.  Therefore
     *       there is no need to differentiate between conditions using an incoming chat log id as would be done
     *       if there was a DKF with multiple AT conditions that could be used for mid-lesson surveys.  In this situation
     *       each of those conditions would need to uniquely identify the chat entries for it's specific AT session.
     */
    private static final int UNKNOWN_CHAT_ID = -1;
    private int chatId = UNKNOWN_CHAT_ID;

    /**
     * Default constructor - required for authoring logic
     */
    @Deprecated
    public AutoTutorWebServiceInterfaceCondition(){
        throw new UnsupportedOperationException(
                "This condition is no currently supported. An AutoTutor course object or performance assessment should be used instead.");
    }

    /**
     * Class constructor - retrieve script information from the input configuration parameters.
     *
     * @param input configuration parameters to use for this condition
     */
    @Deprecated
    public AutoTutorWebServiceInterfaceCondition(generated.dkf.AutoTutorConditionInput input){
        this();
        Object scriptType = input.getAutoTutorSKO().getScript();

        if(scriptType instanceof generated.dkf.ATRemoteSKO){
            //SKO is NOT local to the ATWS but is already a network available resource
            generated.dkf.ATRemoteSKO remoteSKO = (generated.dkf.ATRemoteSKO)scriptType;
            scriptNameOrURL = remoteSKO.getURL().getAddress();
            scriptRefType = Script_Reference_Type.URL;

            // 8/18 - new AutoTutor ACE server no longer has an API to upload a sko from a GIFT course folder
//        }else if(scriptType instanceof generated.dkf.LocalSKO){
//            //SKO is NOT local to the ATWS but is local to this GIFT instance (i.e. a file on this computer)
//            generated.dkf.LocalSKO localSKO = (generated.dkf.LocalSKO)scriptType;
//            scriptNameOrURL = localSKO.getFile();
//            scriptRefType = Script_Reference_Type.FILE;

//        }else if(scriptType instanceof generated.dkf.AutoTutorConditionInput.ATLocalSKO){
//            //SKO is local to the ATWS
//            generated.dkf.AutoTutorConditionInput.ATLocalSKO localSKO = (generated.dkf.AutoTutorConditionInput.ATLocalSKO)scriptType;
//            scriptNameOrURL = localSKO.getScriptName();
//            isRemote = false;

        }else{
            throw new IllegalArgumentException("Received unhandled script type of "+scriptType);
        }


    }
    
    // team member refs not allowed as an input
    @Override
    public String hasValidTeamMemberRefs(){
        return null;
    }

    @Override
    public void initialize(ConditionActionInterface conditionActionInterface){
        super.initialize(conditionActionInterface);

        autoTutorSession = new AutoTutorWebServiceInterface();
    }

    @Override
    public void stop(){

        if(logger.isInfoEnabled()){
            logger.info("Stopping AT WS interface condition...");
        }

        //stop the handling thread
        keepHandling = false;
        synchronized (chatLogQueue) {
            chatLogQueue.clear();
            chatLogQueue.notifyAll();
        }

        if(logger.isInfoEnabled()){
            logger.info("Waiting for AT WS interface condition handler thread to finish...");
        }

        //wait for the thread to die
        if(Thread.currentThread() != handlerThread){
            while(handlerThread != null && handlerThread.isAlive()){
            	//nothing to do but wait
            }
        }

        if(logger.isInfoEnabled()){
            logger.info("AT WS interface condition is stopped.");
        }

        super.stop();
    }

    @Override
    public void start(){
        super.start();

        //create thread to handle incoming requests - this is to release the handle sim messages calling thread.
        handlerThread = new Thread("Chat Log Handler"){

            @Override
            public void run() {

                if(logger.isInfoEnabled()){
                    logger.info("Starting chat log handler thread");
                }

                while(keepHandling){

                    synchronized (chatLogQueue) {

                        //wait for a message to be added to the queue
                        try {
                            chatLogQueue.wait();
                        } catch (InterruptedException e1) {
                            logger.error("Caught exception while waiting for chat log entries.", e1);
                        }

                        activeSession = true;
                        if(logger.isInfoEnabled()){
                            logger.info("Waking up, there are "+chatLogQueue.size()+" chat logs in the queue.");
                        }

                        //process last chat log
                        if(!chatLogQueue.isEmpty()){
                            ChatLog chatLog = chatLogQueue.remove(chatLogQueue.size()-1);

                            try{

                                //the first message signals that AT WS connection should be established and
                                //the first actions retrieved.
                                if(isFirstMessage && chatLog.getUserEntries().isEmpty()){
                                    if(logger.isInfoEnabled()){
                                        logger.info("Received first chat message from tutor.  Initializing AT session.");
                                    }
                                    isFirstMessage = false;
                                    ActionsResponse response = initAutoTutorSession(scriptNameOrURL, scriptRefType);
                                    response.setChatId(chatId);

                                    if(logger.isInfoEnabled()){
                                        logger.info("Requesting that this response be displayed to the user: "+response+".");
                                    }
                                    conditionActionInterface.handleDomainActionWithLearner(response);
                                    continue;
                                }

                                if(logger.isInfoEnabled()){
                                    logger.info("Process chat log of "+chatLog);
                                }

                                AssessmentResponse response = handleChatLog(chatLog);
                                if(response == null){
                                    //ERROR
                                    throw new Exception("The assessment response can't be null.");
                                }

                                AssessmentLevelEnum expectationAssessment = autoTutorSession.calculateAssessment(response, AutoTutorWebServiceInterface.EXPECTATION);
//                                AssessmentLevelEnum hintAssessment = autoTutorSession.calculateAssessment(response, AutoTutorWebServiceInterface.HINT);
//                                AssessmentLevelEnum promptAssessment = autoTutorSession.calculateAssessment(response, AutoTutorWebServiceInterface.PROMPT);

                                if(logger.isInfoEnabled()){
                                    logger.info("New assessments of chat result - \n"+response);
                                }

                                //save the chat log used for this assessment
                                if(queuedChatLog != null){
                                    synchronized (queuedChatLog) {
                                        queuedChatLog = chatLog;
                                    }
                                }else{
                                    queuedChatLog = chatLog;
                                }

//                                //TESTING - bypasses "GIFT tutor loop"
//                                if(response != null){
//                                    handleRequestForActions();
//                                }

                                // computed for all levels of assessment, for now only send expectation
                                sendAssessment(expectationAssessment);

                            }catch(Exception e){
                                logger.error("Caught exception while trying to handle the chat log of "+chatLog, e);

                                //have to thread this call because it will cause the stop method to be called in this class
                                new Thread("ATWS Fatal Error"){
                                    @Override
                                    public void run() {
                                        conditionActionInterface.fatalError("There was a problem with the AutoTutor Webservice condition.", "The AutoTutor web service caused an exception with the message of '"+e.getMessage()+"'.");
                                    }
                                }.start();
                            }
                        }
                    }//end sync
                }//end while

                if(logger.isInfoEnabled()){
                    logger.info("Handler thread has finished.");
                }
            }
        };

        handlerThread.start();
    }

    /**
     * Notify the concept of the change in assessment value.
     *
     * @param assessment - the new assessment value
     */
    private void sendAssessment(AssessmentLevelEnum assessment){
        updateAssessment(assessment);
        conditionActionInterface.conditionAssessmentCreated(this);
    }

    @Override
    public boolean handleTrainingAppGameState(Message message) {

        if(this.hasCompleted()){
            return false;
        }

        if(message.getMessageType() == MessageTypeEnum.CHAT_LOG){

            ChatLog chatLog = (ChatLog)message.getPayload();
            if(logger.isInfoEnabled()){
                logger.info("Received chat log of "+chatLog+".");
            }

            if(chatId == UNKNOWN_CHAT_ID && AutoTutorChatSessionMgr.getCondition(chatLog.getChatId()) == null){
                if(logger.isInfoEnabled()){
                    logger.info("Setting chat id to incoming chat log chat id of "+chatLog.getChatId()+".");
                }
                chatId = chatLog.getChatId();
                AutoTutorChatSessionMgr.addCondition(chatId, this);
            }

            if(chatLog.getChatId() == chatId){

                if(logger.isInfoEnabled()){
                    logger.info("The chat log is for this chat session.");
                }

                //all interactions should be on another thread to release the handle sim msg thread.
                synchronized(chatLogQueue){
                    chatLogQueue.add(chatLog);
                    chatLogQueue.notifyAll();
                }
            }else{
                if(logger.isInfoEnabled()){
                    logger.info("The chat log is NOT for this chat session.");
                }
            }

        }

        return false;
    }

    @Override
    public List<MessageTypeEnum> getSimulationInterests() {
        return simulationInterests;
    }

    /**
     * Query the AT WS interface for the initial actions.
     *
     * @param scriptReference - the reference to the script content.
     *        For an ATWS local script, the reference is the name of the script.
     *        For a URL reference script, the reference is the URL - Note: the URL must be reachable by the ATWS machine.
     *        For a File referenced script, the reference is the relative path under the GIFT Domain folder.
     * @param scriptType - the type of script reference being provided for initialization.
     *          This dictates the type of handling is done with the reference provided.
     * @return ActionsResponse - response from the AT WS get actions method call
     * @throws IOException
     */
    private ActionsResponse initAutoTutorSession(String scriptReference, Script_Reference_Type scriptType) throws IOException{

        switch(scriptType){

            case ATWS:
                autoTutorSession.initScript(scriptReference);
                break;
            case URL:

                String scriptURL = scriptReference;

                //Check if the URL is a file on this computer
                try{
                    if(this.courseFolder.fileExists(scriptReference)){

                        //Add the network address of the hosted Domain module's domain folder
                        String networkURL;
                        try {
                            networkURL = DomainModuleProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH;

                        } catch (Exception ex) {
                            logger.error("Could not get the host IP address, defaulting to 'localhost'", ex);
                            networkURL = DomainModuleProperties.getInstance().getTransferProtocol() + "localhost:" + DomainModuleProperties.getInstance().getDomainContentServerPort() + "/";
                        }

                        scriptURL = networkURL + scriptReference;

                        if(logger.isDebugEnabled()){
                            logger.debug("The URL provided maps to the file of "+scriptReference+" on this computer, therefore attempting to provide the domain module hosted URL of "+
                                scriptURL+" to the ATWS.  This will only work if the ATWS has access to that file on your machine (i.e. appropriate network and permission configuration).");
                        }
                    }
                }catch(@SuppressWarnings("unused") Exception e){
                    //don't care cause maybe this isn't a local file to begin with
                }

                autoTutorSession.initScriptByURL(scriptURL);
                break;

             // 8/18 - new AutoTutor ACE server no longer has an API to upload a sko from a GIFT course folder
//            case FILE:
//                FileProxy xmlProxy = this.courseFolder.getRelativeFile(scriptReference);
//                autoTutorSession.initScriptByXML(xmlProxy);
//                break;
            default:
                //ERROR
                logger.error("Unhandled script reference type of "+scriptType+".  Therefore not sure what to do with the script reference of "+scriptReference+".");
        }

        return autoTutorSession.getInitialActions();
    }

    /**
     * Query the AT WS interface for the next actions and then handle any actions.
     */
    private void handleRequestForActions(){

        if(!hasCompleted()){
            ActionsResponse actionsResponse = autoTutorSession.getActions(queuedChatLog.getLastUserEntry());
            actionsResponse.setChatId(chatId);

            if(logger.isInfoEnabled()){
                logger.info("Requesting that this response be displayed to the user: "+actionsResponse+".");
            }
            if(conditionActionInterface != null){
                conditionActionInterface.handleDomainActionWithLearner(actionsResponse);
            }

            if(actionsResponse.hasEnded()){
                conditionCompleted();
            }
        }else{
            logger.warn("The condition of "+this+" is receiving a request for action but it has already been completed, therefore no more information will be presented to the user.");
        }
    }

    /**
     * Query the AT WS interface for an assessment based on the chat log.
     *
     * @param chatLog - the current chat log
     * @return AssessmentResponse - response from the AT WS get assessments method call
     * @throws Exception
     */
    private AssessmentResponse handleChatLog(ChatLog chatLog) throws Exception{

        if(autoTutorSession == null){
            throw new Exception("The auto tutor session has not been initialized.");
        }else if(chatLog == null){
            throw new IllegalArgumentException("The chat log can't be null.");
        }else if(chatLog.getUserEntries().isEmpty()){
            throw new IllegalArgumentException("The user's entries can't be empty.");
        }

        String lastUserEntry = chatLog.getLastUserEntry();
        AssessmentResponse assessmentResponse = autoTutorSession.getAssessments(lastUserEntry);

        return assessmentResponse;
    }

    @Override
    public void assessCondition(){

        if(!activeSession){
            //then a request for assessment has been called for this condition before autotutor and the user have interacted,
            //which means this condition is being asked to present the autotutor session

            if(logger.isInfoEnabled()){
                logger.info("The chat session is not active yet, therefore this condition should request the chat window interaction as a way to assess this condition.");
            }

            //generate a unique chat id
            chatId = new DisplayChatWindowRequest(null).getChatId();
            AutoTutorChatSessionMgr.addCondition(chatId, this);
            if(logger.isInfoEnabled()){
                logger.info("Setting chat id to "+chatId+" for new chat window request.");
            }

            AutoTutorSurveyLessonAssessment surveyAssessment = new AutoTutorSurveyLessonAssessment(chatId);
            if(conditionActionInterface != null){
                conditionActionInterface.displayDuringLessonSurvey(surveyAssessment, null);
            }

            //Note: this prevents multiple request for assessments on this condition received in a short amount of time from
            //      causing an autotutor session to be presented for each request.
            activeSession = true;

        }else{

            if(logger.isInfoEnabled()){
                logger.info("Resetting this conditions assessment level to "+AssessmentLevelEnum.UNKNOWN+" to trigger another tutor loop interaction.");
            }

            //first change assessment level to trigger a future state transition
            updateAssessment(AssessmentLevelEnum.UNKNOWN);
            if(conditionActionInterface != null){
                conditionActionInterface.conditionAssessmentCreated(this);
            }

            //then retrieve next set of actions
            handleRequestForActions();
        }
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[AutoTutorWebServiceInterfaceCondition: ");
        sb.append("script = ").append(scriptNameOrURL);
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }

    /**
     * This class is used to manage multiple, possible, AutoTutor sessions within a single domain (i.e. more than
     * one AutoTutor script referenced in a single DKF).
     *
     * @author mhoffman
     *
     */
    private static class AutoTutorChatSessionMgr{

        /** map of tutor chat unique identifiers to an AutoTutor domain condition that handles the interaction */
        private static Map<Integer, AutoTutorWebServiceInterfaceCondition> chatIdToATCondition = new HashMap<>();

        /**
         * Return the AutoTutor condition associated with the chat id specified.
         *
         * @param chatId the unique chat id to lookup an associated AutoTutor condition for.
         * @return AutoTutorWebServiceInterfaceCondition the condition mapped to the chat id.  Can be null.
         */
        public static AutoTutorWebServiceInterfaceCondition getCondition(int chatId){
            return chatIdToATCondition.get(chatId);
        }

        /**
         * Map the condition to the chat id provided.  If a condition is already mapped to that chat id and exception
         * will be thrown by this method.
         *
         * @param chatId the unique chat id to map to an AutoTutor condition.
         * @param condition the condition to map to the chat id
         */
        public static void addCondition(int chatId, AutoTutorWebServiceInterfaceCondition condition){

            if(chatIdToATCondition.containsKey(chatId)){
                throw new IllegalArgumentException("There is already a condition mapped to chat id of "+chatId+", it is "+chatIdToATCondition.get(chatId)+".");
            }else if(condition == null){
                throw new IllegalArgumentException("The condition can't be null.");
            }

            logger.debug("Mapping chat id of "+chatId+" to condition of "+condition+".");

            chatIdToATCondition.put(chatId, condition);
        }
    }

    @Override
    public ConditionDescription getDescription() {
        return DESCRIPTION;
    }

    @Override
    public Set<LearnerActionEnumType> getLearnerActionsNeeded() {
        return null;
    }
    
    @Override
    public Set<Class<?>> getOverallAssessmenTypes() {
        return overallAssessmentTypes;
    }
    
    @Override
    public boolean canComplete() {
        return true;
    }
}
