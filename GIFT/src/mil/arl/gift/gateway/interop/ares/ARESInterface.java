/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.interop.ares;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dignitas.ares.protobuf.AresMessageAssessment.AssessmentType;
import com.dignitas.ares.protobuf.AresMessageAssessment.AssessmentUpdate;
import com.dignitas.ares.protobuf.AresMessageGiftWrap;
import com.dignitas.ares.protobuf.AresMessageScenario;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.DisplaySurveyTutorRequest;
import mil.arl.gift.common.KnowledgeSessionCreated;
import mil.arl.gift.common.Siman;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SimanTypeEnum;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.UriUtil;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.EvaluationResult;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateUtil;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.MultipleChoiceQuestionResponse;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityMarking;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.SimulationAddress;
import mil.arl.gift.common.ta.util.ExternalMonitorConfig;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.gateway.GatewayModule;
import mil.arl.gift.gateway.GatewayModuleProperties;
import mil.arl.gift.gateway.interop.AbstractInteropInterface;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;
import mil.arl.gift.net.dis.DISToGIFTConverter.DISDialect;
import mil.arl.gift.net.rest.RESTClient;

/**
 * Use to interface with the Augmented Reality Sandtable (ARES) training application.
 *
 * @author mhoffman
 *
 */
public class ARESInterface extends AbstractInteropInterface {

    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ARESInterface.class);

    /**
     * contains the types of GIFT messages this interop interface can consume from GIFT modules
     * and handle or send to some external training application
     */
    private static List<MessageTypeEnum> supportedMsgTypes;
    static{
        supportedMsgTypes = new ArrayList<>();
        supportedMsgTypes.add(MessageTypeEnum.SIMAN);
        supportedMsgTypes.add(MessageTypeEnum.SURVEY_PRESENTED_NOTIFICATION);
        supportedMsgTypes.add(MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE);
        supportedMsgTypes.add(MessageTypeEnum.KNOWLEDGE_SESSION_CREATED);
        supportedMsgTypes.add(MessageTypeEnum.LEARNER_STATE);
        supportedMsgTypes.add(MessageTypeEnum.ENTITY_STATE);
        supportedMsgTypes.add(MessageTypeEnum.EXTERNAL_MONITOR_CONFIG);
    }

    /**
     * contains the training applications that this interop plugin was built for and should connect to
     */
    private static List<TrainingApplicationEnum> REQ_TRAINING_APPS;
    static{
        REQ_TRAINING_APPS = new ArrayList<>();
        REQ_TRAINING_APPS.add(TrainingApplicationEnum.ARES);
    }

    /**
     * contains the list of GIFT messages (e.g. Entity State) that this interop plugin interface
     * can create and send to GIFT modules (e.g. Domain module) after consuming some relevant information from
     * an external training applications (e.g. VBS).
     */
    private static List<MessageTypeEnum> PRODUCED_MSG_TYPES;
    static{
        PRODUCED_MSG_TYPES = new ArrayList<>();
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.TRAINING_APP_SURVEY_RESPONSE);
        PRODUCED_MSG_TYPES.add(MessageTypeEnum.TRAINING_APP_SURVEY_SUBMIT);
    }

    private static final String TEMP_ARES_FILE_PREFIX = "GIFT-ARES-";

    private static final String NAME_KEY = "current_scenario_name";
    private static final String ID_KEY = "current_scenario_id";

    /**
     * REST URLs
     */
    private static final String SNAPSHOT_CMD = "/services/table/scenario/snapshot";
    private static final String LOAD_CMD = "/services/commands/scenario/load";
    private static final String CLEAR_CMD = "/services/commands/viewer/clear";
    private static final String CONFIG_CMD = "/services/gift/assessment/config";
    private static final String SCENARIOS_CMD = "/services/storage/scenario";
    private static final String SCENARIO_OBJECTS_CMD = "/services/table/scenario/objects";
    @SuppressWarnings("unused")
    private static final String CURRENT_SCENARIO_METADATA_CMD = "/services/table/scenario";
    private static final String DISABLE_ALL_SELECTIONS_CMD = "/services/scenario/controller/is-selectable/0";
    //possible future use regarding filtering for specific category of selectable objects
    @SuppressWarnings("unused")
    private static final String SCENARIO_UNITS_CMD = "/services/table/scenario/objects/unit";
    @SuppressWarnings("unused")
    private static final String SCENARIO_TACTICAL_GRAPHICS_CMD = "/services/table/scenario/objects/tactical-graphic";
    @SuppressWarnings("unused")
    private static final String SCENARIO_TACTICAL_GEOMETRY_CMD = "/services/table/scenario/objects/tactical-geometry";

    /** The URL that is informed when the {@link LearnerState} is updated. */
    private static final String UPDATE_LEARNER_STATE = "/services/gift/assessment/update";

    private static final String CONTENT_DISPOSITION_FIELD_NAME = "zipfile";

    /**
     * REST parameters
     */
    private Map<String, String> params = new HashMap<>();
    private static final String GIFT_LEARNER_MODE = "gift-learner";
    private static final String GIFT_WRAP_MODE = "gift-wrap";
    private static final String CONTROLLER_CONTEXT_PARAM = "controller_context";

    /** RabbitMQ */
    private ConnectionFactory amqpConnectionFactory = null;
    private Connection amqpConnection = null;
    private Channel giftChannel = null;
    private Channel scenarioChannel = null;
    private static final String amqpGiftExchangeName = "ares.giftwrap";
    private static final String amqpScenarioExchangeName = "ares.scenario";
    private static final String amqpExchangeType = "fanout";

    /**
     * Get the property value for the assessment notification delay (in
     * milliseconds)
     */
    private static final int ASSESSMENT_NOTIFICATION_DELAY_MS = GatewayModuleProperties.getInstance()
            .getAssessmentNotificationDelayMs();

    /** The time in nano that the last entity state change message was sent */
    private Long lastTimeSentChangedEntities;

    /** contains the REST configuration information (e.g. REST server URL) */
    private generated.gateway.REST restConfiguration;

    /** contains the ActiveMQ configuration information (e.g. AMQP port) */
    private generated.gateway.AMQP amqpConfiguration;

    private RESTClient restClient;

    private DomainSessionMessage displaySurveyTutorRequestMessage = null;
    private MultipleChoiceSurveyQuestion surveyQuestion = null;
    private SurveyResponse surveyResponse = new SurveyResponse();
    private String currentScenarioName = "";
    private String currentScenarioId = "";

    /** The team structure for the session */
    private Team sessionTeamStructure;

    /** Maps role names to entity data */
    // TODO: this does not support multiple sessions
    private final Map<String, EntityAssessmentData> roleToEntityDataMap = new HashMap<>();

    /** TODO: Move to a separate class */
    private class AresGiftMessageHandler extends DefaultConsumer {
    	private ARESInterface aresInterface = null;

    	public AresGiftMessageHandler(Channel channel, ARESInterface aresInterface) {
    		super(channel);
    		this.aresInterface = aresInterface;
    	}

    	@Override
    	public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    		long deliveryTag = envelope.getDeliveryTag();

    		AresMessageGiftWrap.GiftWrapSelectionChanged selectionChangeEvent = AresMessageGiftWrap.GiftWrapSelectionChanged.parseFrom(body);

    		for (AresMessageGiftWrap.GiftWrapSelectableObject selectedObject : selectionChangeEvent.getSelectedObjectListList()) {
    			aresInterface.handleQuestionAnswer(selectedObject.getId());
    		}

    		Boolean selectionConfirmed = selectionChangeEvent.getSelectionConfirmed();
    		if (selectionConfirmed) {
    			sendSurveySubmit(displaySurveyTutorRequestMessage);
    		}

    		giftChannel.basicAck(deliveryTag, false);
    	}
    }

    private class AresScenarioMessageHandler extends DefaultConsumer {


        public AresScenarioMessageHandler(Channel channel, ARESInterface aresInterface) {
            super(channel);
        }

    	@Override
        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
    		long deliveryTag = envelope.getDeliveryTag();
    		AresMessageScenario.TableMessage tableMessage = AresMessageScenario.TableMessage.parseFrom(body);

            if (tableMessage.hasField(AresMessageScenario.TableMessage.getDescriptor().findFieldByName("scenario"))) {
                AresMessageScenario.TableScenario scenario = tableMessage.getScenario();
                currentScenarioName = scenario.getName();
                currentScenarioId = scenario.getId();
                logger.info("notified that ARES Scenario '" + currentScenarioName + "' was loaded");
            }
            scenarioChannel.basicAck(deliveryTag, false);
        }
    }

    /**
     * Class constructor
     *
     * @param name - the display name of the plugin
     */
    public ARESInterface(String name) {
        super(name, true);
    }

    @Override
    public boolean configure(Serializable config)  {

        if(config instanceof generated.gateway.ARES){

            generated.gateway.ARES aresConfig = (generated.gateway.ARES)config;

            restConfiguration = aresConfig.getREST();
            restClient = new RESTClient();

            amqpConfiguration = aresConfig.getAMQP();

        } else {
            throw new ConfigurationException("Unable to configure ARES Gateway module plugin interface.",
                    "The configuration logic supports an input of type "+generated.gateway.REST.class+" but the following was provided " + config,
                    null);
        }

        return false;
    }

    @Override
    public List<MessageTypeEnum> getSupportedMessageTypes() {
        return supportedMsgTypes;
    }

    @Override
    public boolean handleGIFTMessage(Message message, StringBuilder errorMsg) {

        if(message.getMessageType() == MessageTypeEnum.SIMAN){

            Siman siman = (Siman) message.getPayload();

            if (siman.getSimanTypeEnum() == SimanTypeEnum.LOAD) {

                //
                // get appropriate configuration info for loading
                //
                generated.course.InteropInputs interopInputs = getLoadArgsByInteropImpl(this.getClass().getName(), siman.getLoadArgs());

                if(interopInputs == null) {
                    return false; //do not load if there are no load arguments
                }

                generated.course.GenericLoadInteropInputs inputs = (generated.course.GenericLoadInteropInputs) interopInputs.getInteropInput();

                String contentRef = inputs.getLoadArgs().getContentRef();

                if (contentRef != null) {

                    logger.info("Received load request for ARES scenario reference '"+contentRef+"'.");

                    //
                    // Check if the content is a file that needs to be pushed to ARES
                    //

                    //Download reference to the temp location
                    //Note: the temp file will be deleted when finished with it
                    boolean fileRef = false;
                    try{
                        File tempFile = File.createTempFile(TEMP_ARES_FILE_PREFIX, Long.toString(System.nanoTime()), FileUtil.getGIFTTempDirectory());

                        tempFile.deleteOnExit();  //in case this gateway module JVM goes down before any cleanup code can be called
                                                 //In the future this may not be the best solution when the Gateway module instance
                                                 //runs for an extended amount of time and downloads can accumulate.

                        //TODO: find a library that does this URL encoding for us
                        String urlStr;
                        if(siman.getRuntimeCourseFolderPath() != null){
                            urlStr = getDomainContentServerAddress() + Constants.FORWARD_SLASH + UriUtil.makeURICompliant(siman.getRuntimeCourseFolderPath()) +
                                    Constants.FORWARD_SLASH + UriUtil.makeURICompliant(contentRef);
                        }else{
                            urlStr = getDomainContentServerAddress() + Constants.FORWARD_SLASH +
                                    UriUtil.makeURICompliant(contentRef);
                        }

                        logger.debug("Checking if scenario is a file by using URL of '"+urlStr+"'.");

                        //download and save to temp file
                        try (InputStream in = new URL(urlStr).openStream()) {
                            Files.copy(in, Paths.get(tempFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
                        }

                        if(tempFile.exists()){
                            fileRef = true;

                            logger.info("Sending ARES scenario file to ARES to load.  ("+tempFile+")");

                            try{
                                loadScenario(tempFile.getAbsolutePath());
                            }catch(@SuppressWarnings("unused") DetailedException e){
                                //already logged in loadScenario method
                                errorMsg.append("Failed to load the ARES scenario because of a problem sending the ARES scenario file.");
                            }

                        }

                    }catch(@SuppressWarnings("unused") FileNotFoundException fnf){
                        logger.debug("The ARES content reference of '"+contentRef+"' must not be a file because a file not found exception was thrown while checking it.  "
                                + "Moving forward with the assumption that the reference is an ARES scenario name hosted on the ARES table.");

                    }catch(Exception e){
                        logger.debug("The ARES content reference of '"+contentRef+"' must not be a file because an exception was thrown while checking it.  "
                                + "Moving forward with the assumption that the reference is an ARES scenario name hosted on the ARES table.", e);
                    }

                    //
                    // If not a domain server hosted file in the course folder, treat the content reference as the name of an ARES scenario (hosted in ARES)
                    //

                    if(!fileRef){

                        try{
                            loadScenario(contentRef);
                        }catch(@SuppressWarnings("unused") DetailedException e){
                            //already logged in loadScenario method
                            errorMsg.append("Failed to load ARES scenario because of a problem calling the ARES load scenario API.");
                        }
                    }
                }

            }else if(siman.getSimanTypeEnum() == SimanTypeEnum.STOP){

                /* Clear the map since the scenario has ended. */
                roleToEntityDataMap.clear();

                try{

                    /* CHUCK 11/14/2019: We commented this out because it was
                     * causing terrain to be unloaded upon the ending of a
                     * course. This was not desired for our 2019 I/ITSEC
                     * demo. */
                    //call the ARES REST request for clearing a scenario
                    // loadScenario(null);
                }catch(@SuppressWarnings("unused") Exception e){
                    //already logged in loadScenario method

                    errorMsg.append("Failed to load ARES scenario because of a problem calling the ARES clear API.");
                }
            }

        }else if(message.getMessageType() == MessageTypeEnum.SURVEY_PRESENTED_NOTIFICATION){
            //handle the request for the tutor to display a survey by providing ARES the necessary information
            //that will allow the learner to answer the survey in ARES application

            return handleDisplaySurveyRequest((DomainSessionMessage) message, errorMsg);

        }else if(message.getMessageType() == MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE){
            //handle the notification that the learner answered the question in the TUI by notifying
            //ARES that ARES objects were selected

            return handleTutorSurveyQuestionResponse(message, errorMsg);

        } else if (message.getMessageType() == MessageTypeEnum.KNOWLEDGE_SESSION_CREATED) {
            KnowledgeSessionCreated sessionCreatedMsg = (KnowledgeSessionCreated) message.getPayload();
            final AbstractKnowledgeSession knowledgeSession = sessionCreatedMsg.getKnowledgeSession();
            sessionTeamStructure = knowledgeSession.getTeamStructure();
            roleToEntityDataMap.clear();
        } else if (message.getMessageType() == MessageTypeEnum.LEARNER_STATE) {
            if (sessionTeamStructure == null) {
                logger.error("Received a learner state in the ARES plugin but no team structure exists, " + message);
                errorMsg.append("ARES plugin can't handle learner state without a team structure");
            }

            handleLearnerState((LearnerState) message.getPayload());

        } else if (message.getMessageType() == MessageTypeEnum.ENTITY_STATE) {
            if (sessionTeamStructure == null) {
                logger.error("Received an entity state in the ARES plugin but no team structure exists, " + message);
                errorMsg.append("ARES plugin can't handle entity state without a team structure");
            }
            handleEntityState((EntityState) message.getPayload());

        } else if(message.getMessageType() == MessageTypeEnum.EXTERNAL_MONITOR_CONFIG) {

            try {
                configureMonitoring((ExternalMonitorConfig) message.getPayload());

            } catch(@SuppressWarnings("unused") Exception e) {
                //already logged in configureMonitoring method

                errorMsg.append("Failed to modify ARES monitoring behevaior because of a problem calling the ARES API");
            }

        }else {
            logger.error("Received unhandled GIFT message in the ARES plugin, " + message);

            errorMsg.append("ARES plugin can't handle message of type ").append(message.getMessageType());
        }

        return false;
    }

    /**
     * Handle the notification that the learner answered a survey question in the tutor.
     *
     * @param message contains the survey question response
     * @param errorMsg error message produced by the plugin handling this gift message
     * @return whether the survey question response was sent to ARES
     */
    private boolean handleTutorSurveyQuestionResponse(Message message, StringBuilder errorMsg){

        AbstractQuestionResponse questionResponse = (AbstractQuestionResponse) message.getPayload();

        if(questionResponse instanceof MultipleChoiceQuestionResponse){

            //
            // get selected question choices object ids
            //
            List<String> objectIds;
            try{
                objectIds = ((MultipleChoiceQuestionResponse)questionResponse).getResponseObjectIds();
            }catch(Exception e){
                errorMsg.append(e.getMessage());
                return false;
            }

            try{
            	// TODO: Assuming only one ID for now, add support for multiple IDs
            	String selectedObjectId = objectIds.get(0);
            	selectObject(selectedObjectId);
        	}catch(Exception e){
                logger.error("Caught exception while trying to set selected object in ARES", e);
                errorMsg.append("Failed to set selected object in ARES");
                return false;
            }

            return true;

        }else{
            //ERROR - unhandled survey response type
            logger.error("Received unhandled survey response type of "+questionResponse);

            errorMsg.append("Failed to notify ARES of the learner's survey question selections made in the tutor because the survey question is not a multiple choice question.");
        }

        return false;
    }

    /**
     * Handle the display survey request message meant for the tutor by parsing the survey the
     * tutor will display and passing along useful information to ARES in order to allow
     * the learner to answer the survey in ARES.
     *
     * @param message the display survey tutor request message to handle
     * @param errorMsg error message produced by the plugin handling this gift message
     * @return whether the survey request was sent to ARES
     */
    private boolean handleDisplaySurveyRequest(DomainSessionMessage message, StringBuilder errorMsg){

        //just in case a response to a previous question is being handled at this time
        synchronized(surveyResponse){
            displaySurveyTutorRequestMessage = message;
            DisplaySurveyTutorRequest currentSurveyRequest = (DisplaySurveyTutorRequest)displaySurveyTutorRequestMessage.getPayload();
            Survey survey = currentSurveyRequest.getSurvey();

            surveyResponse.setSurveyStartTime(new Date());
            surveyResponse.setSurvey(survey);

            //
            // Extract the information ARES needs in order for ARES user to answer the survey
            // Note: currently ARES only supports answering a survey with a single multiple choice question, hence the for loop breaks and return lines.
            //

            List<SurveyPage> surveyPages = survey.getPages();
            if(surveyPages.size() == 1){

                SurveyPage surveyPage = surveyPages.get(0);
                for(AbstractSurveyElement element : surveyPage.getElements()) {

                    if(element instanceof MultipleChoiceSurveyQuestion){

                        MultipleChoiceSurveyQuestion multChoiceSQ = (MultipleChoiceSurveyQuestion)element;
                        List<String> objectIds;
                        try{
                            objectIds = multChoiceSQ.getAndValidateObjectIds();

                            if(objectIds == null){
                                break;
                            }
                        }catch(Exception e){
                            errorMsg.append("ARES Gateway module interop plugin can't handle the survey named ").append(survey.getName()).append(" because ").append(e.getMessage()).append(".");
                            break;
                        }

                        //save for use when answering
                        surveyQuestion = multChoiceSQ;

                        //send object ids to ARES
                        try{
                            //Disable selection on all objects
                            restClient.post(new URL(restConfiguration.getServerURL() + DISABLE_ALL_SELECTIONS_CMD));

                            for (String objectId : objectIds) {

                                if(objectId != null && !objectId.isEmpty()){
                                    restClient.post(new URL(restConfiguration.getServerURL() + "/services/scenario/controller/" + objectId + "/is-selectable/1"));
                                }
                            }
                    	}catch(Exception e){
                            logger.error("Caught exception while trying to set selectable objects", e);
                            errorMsg.append("Failed to load ARES scenario because of a problem setting selectable objects");
                        }

                        return true;
                    }else{
                        //ERROR - only support a single question and that question must be a multiple choice question
                        logger.error("There is more than 1 question in the survey named "+survey.getName()+".  ARES can only answer a single multiple choice question on a single survey page right now.  The display survey request messages is:\n" + message);

                        errorMsg.append("ARES Gateway module interop plugin can't handle the survey named ").append(survey.getName()).append(" because it has more than 1 survey question.");
                        break;
                    }

                }//end for
            }else{
                //ERROR - more than one page
                logger.error("There is more than 1 survey page in the survey named "+survey.getName()+".  ARES can only answer a single multiple choice question on a single survey page right now.  The display survey request messages is:\n" + message);

                errorMsg.append("ARES Gateway module interop plugin can't handle the survey named ").append(survey.getName()).append(" because it has more than 1 survey page.");
            }
        }

        return false;
    }

    /**
     * Create a survey response to the current survey being presented with the given survey
     * question choice selected by the learner in ARES.
     *
     * @param choiceObjectId the unique id of an ARES course object that can be used to answer the single multiple
     * choice survey question being presented to the learner in the TUI.  Can't be null or empty.
     * @throws DetailedException if there was a problem handling sending a survey response
     */
    private void handleQuestionAnswer(String choiceObjectId) throws DetailedException{

        DisplaySurveyTutorRequest currentSurveyRequest = (DisplaySurveyTutorRequest)displaySurveyTutorRequestMessage.getPayload();
        if(currentSurveyRequest == null){
           throw new DetailedException("Unable to create a survey response for the learner based on the object selected in ARES.",
                   "The survey request information is null, therefore the survey can't be retrieved.", null);
        }else if(surveyQuestion == null){
            throw new DetailedException("Unable to create a survey response for the learner based on the object selected in ARES.",
                    "The survey question information is null, therefore the survey choice text associated with the ARES object can't be retrieved.", null);
        }else if(choiceObjectId == null || choiceObjectId.isEmpty()){
            throw new IllegalArgumentException("The choice object id can't be null or empty.");
        }else{

            synchronized(surveyResponse){
                surveyResponse.setSurveyEndTime(new Date());

                surveyResponse.getSurveyPageResponses().clear();

                //
                // find the response text based on the ARES object id
                //
                List<String> objectIds = surveyQuestion.getQuestionChoicesObjectIds();

                if(objectIds == null){
                    //ERROR - found a multiple choice question but it doesn't have object ids that ARES can use
                    throw new DetailedException("Unable to create a survey response for the learner based on the object selected in ARES.",
                            "There are no object ids associated with the current multiple choice survey question.", null);
                }

                int index = 0;
                for(index = 0; index < objectIds.size(); index++){

                    String objectId = objectIds.get(index);
                    if(!objectId.isEmpty() && objectId.equals(choiceObjectId)){
                        break;
                    }
                }

                if(index >= objectIds.size()){
                    //ERROR
                    throw new DetailedException("Unable to create a survey response for the learner based on the object selected in ARES.",
                            "Unable to find the ARES object id ("+choiceObjectId+") in the list object ids authored for the multiple choice survey question.", null);
                }

                List<String> choices = surveyQuestion.getQuestionChoices();
                if(index >= choices.size()){
                    //ERROR
                    throw new DetailedException("Unable to create a survey response for the learner based on the object selected in ARES.",
                            "Unable to find the choice associated with the ARES object id ("+choiceObjectId+").", null);
                }


                List<SurveyPageResponse> surveyResults = new ArrayList<>();
                SurveyPageResponse surveyPageResponse = new SurveyPageResponse();
                surveyPageResponse.setStartTime(surveyResponse.getSurveyStartTime());
                surveyPageResponse.setEndTime(new Date());
                surveyPageResponse.setSurveyPage(currentSurveyRequest.getSurvey().getPages().get(0));

                List<AbstractQuestionResponse> questionResponses = surveyPageResponse.getQuestionResponses();

                MultipleChoiceQuestionResponse multChoiceQResponse = new MultipleChoiceQuestionResponse();
                multChoiceQResponse.setSurveyQuestion(surveyQuestion);
                List<QuestionResponseElement> responseElements = multChoiceQResponse.getResponses();
                QuestionResponseElement responseElement = new QuestionResponseElement(choices.get(index), new Date());
                responseElements.add(responseElement);

                questionResponses.add(multChoiceQResponse);

                surveyResults.add(surveyPageResponse);
                surveyResponse.getSurveyPageResponses().addAll(surveyResults);

                sendSurveyResponse(surveyResponse, displaySurveyTutorRequestMessage);
            }

        }


    }

    /**
     * Sends a survey response GIFT message through the Gateway module to other GIFT module(s).
     *
     * @param surveyResponse contains the response to a survey
     * @param displaySurveyTutorRequestMessage the original message to display the survey
     */
    private void sendSurveyResponse(SurveyResponse surveyResponse, DomainSessionMessage displaySurveyTutorRequestMessage){
        GatewayModule.getInstance().sendMessageToGIFT(surveyResponse, MessageTypeEnum.TRAINING_APP_SURVEY_RESPONSE, this);
    }

    /**
     * Sends a survey submit GIFT message through the Gateway module to other GIFT module(s).
     *
     * @param displaySurveyTutorRequestMessage the original message to display the survey
     */
    private void sendSurveySubmit(DomainSessionMessage displaySurveyTutorRequestMessage){
        GatewayModule.getInstance().sendMessageToGIFT(null, MessageTypeEnum.TRAINING_APP_SURVEY_SUBMIT, this);
    }

    /**
     * Handle an incoming learner state.
     *
     * @param learnerState the learner state containing the performance
     *        assessments for the entities. Can't be null.
     */
    private void handleLearnerState(LearnerState learnerState) {
        if (learnerState == null) {
            throw new IllegalArgumentException("The parameter 'learnerState' cannot be null.");
        }

        /* Evaluate the team assessments based on learner state */
        Map<String, EvaluationResult> evalResults = LearnerStateUtil.performEvaluation(learnerState,
                sessionTeamStructure);

        /* Maintain collection of entities that changed assessment values */
        Set<EntityAssessmentData> changedEntities = new HashSet<>();

        for (Entry<String, EvaluationResult> entry : evalResults.entrySet()) {
            final String roleName = entry.getKey();
            final EvaluationResult evalResult = entry.getValue();

            /* Retrieve and update entity data */
            EntityAssessmentData entityData = roleToEntityDataMap.get(roleName);
            if (entityData == null) {
                /* Data didn't exist before so it didn't 'change'. Create it now
                 * for next time. */
                entityData = new EntityAssessmentData(evalResult);
                roleToEntityDataMap.put(roleName, entityData);
            } else if (evalResult.getAssessmentLevel() == null) {
                /* Visual only message; don't persist this evaluation but create
                 * a new one to send to ARES. */
                EntityAssessmentData newData = new EntityAssessmentData(evalResult);
                if (entityData.getEntityId() != null) {
                    newData.setEntityId(entityData.getEntityId());
                }
                changedEntities.add(newData);
            } else if (didEvaluationChange(evalResult, entityData.getEvaluationResult())) {
                /* Evaluation changed, push the new assessment to ARES */
                entityData.setEvaluationResult(evalResult);
                changedEntities.add(entityData);
            }
        }

        /* If the previously sent assessments are different than these evaluated
         * assessments, send them to ARES */
        sendChangedEntitiesToAres(changedEntities);
    }

    /**
     * Checks if the evaluation result changed from the previous result to the
     * current one.
     *
     * @param newResult the current result. Can't be null.
     * @param oldResult the previous result.
     * @return true if the evalution changed.
     */
    private boolean didEvaluationChange(EvaluationResult newResult, EvaluationResult oldResult) {
        if (newResult == null) {
            throw new IllegalArgumentException("The parameter 'newResult' cannot be null.");
        } else if (oldResult == null) {
            /* No previous data */
            return true;
        }

        /* Previous state was visual only */
        final AssessmentLevelEnum oldAssessment = oldResult.getAssessmentLevel();
        if (oldAssessment == null) {
            return true;
        }

        /* Assessments are different. Clear indicator that the evaluation
         * changed */
        final AssessmentLevelEnum newAssessment = newResult.getAssessmentLevel();
        if (oldAssessment != newAssessment) {
            return true;
        }

        /* Assessments are the same, for Below Expectation, see if there are any
         * new concepts that caused the result */
        if (newAssessment.equals(AssessmentLevelEnum.BELOW_EXPECTATION)) {
            /* Check if a message was already sent less than the delay ago. If a
             * visual only assessment is present, then the timestamp doesn't
             * matter and the message will be sent to ARES regardless. */
            if (lastTimeSentChangedEntities != null) {
                final long nanoDiff = System.nanoTime() - lastTimeSentChangedEntities;
                final long msDiff = TimeUnit.MILLISECONDS.convert(nanoDiff, TimeUnit.NANOSECONDS);
                if (msDiff <= ASSESSMENT_NOTIFICATION_DELAY_MS) {
                    return false;
                }
            }

            final Set<ConceptPerformanceState> oldResultConcepts = oldResult.getConceptPerformances();
            for (ConceptPerformanceState cState : newResult.getConceptPerformances()) {
                if (!oldResultConcepts.contains(cState)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Handle an incoming entity state.
     *
     * @param entityState the entity state being updated. Can't be null.
     */
    private void handleEntityState(EntityState entityState) {
        if (entityState == null) {
            throw new IllegalArgumentException("The parameter 'entityState' cannot be null.");
        }

        AbstractTeamUnit teamElement = null;

        final EntityMarking entityMarking = entityState.getEntityMarking();
        if (entityMarking != null) {
            teamElement = sessionTeamStructure.getTeamElementByEntityMarking(entityMarking.getEntityMarking());
            if (teamElement == null) {
                teamElement = sessionTeamStructure.getTeamElement(entityMarking.getGiftDisplayName());
            }

            final EntityIdentifier entityId = entityState.getEntityID();

            /* Can't find the team element */
            if (teamElement == null) {
                return;
            } else if (teamElement instanceof TeamMember<?>) {
                /* Set the team member entity id if this is the first time it is
                 * found */
                TeamMember<?> member = (TeamMember<?>) teamElement;
                if (member.getEntityIdentifier() == null) {
                    member.setEntityIdentifier(entityId);
                }
            }

            /* Maintain collection of entities that pre-existed but didn't have
             * an entity id */
            Set<EntityAssessmentData> changedEntities = new HashSet<>();

            /* Retrieve and update entity data */
            final String roleName = teamElement.getName();
            EntityAssessmentData entityData = roleToEntityDataMap.get(roleName);

            if (entityData == null) {
                entityData = new EntityAssessmentData();
                entityData.setEntityId(entityId);
                roleToEntityDataMap.put(roleName, entityData);
            } else if (entityData.getEntityId() == null) {
                entityData.setEntityId(entityId);
                changedEntities.add(entityData);
            }

            /* If any entities existed but without an entity id; send them now
             * to ARES with the entity id populated */
            sendChangedEntitiesToAres(changedEntities);
        }
    }

    /**
     * Send the changed entity data to ARES if it contains an entity id and
     * assessment value.
     *
     * @param changedEntityData the data to send.
     */
    private void sendChangedEntitiesToAres(Set<EntityAssessmentData> changedEntityData) {
        if (CollectionUtils.isEmpty(changedEntityData)) {
            return;
        }

        Map<String, AssessmentType> changedEntities = new HashMap<>();
        for (EntityAssessmentData data : changedEntityData) {
            /* Must contain the entity id and assessment value */
            if (data.getCompoundEntityKey() == null || data.getAssessmentType() == null) {
                continue;
            }

            changedEntities.put(data.getCompoundEntityKey(), data.getAssessmentType());
        }

        /* Send any changed entities to ARES */
        if (!changedEntities.isEmpty()) {
            if (logger.isDebugEnabled()) {
                StringBuilder sb = new StringBuilder("Sending to ARES entity changes: ");
                StringUtils.join(", ", changedEntities.entrySet(), new Stringifier<Entry<String, AssessmentType>>() {
                    @Override
                    public String stringify(Entry<String, AssessmentType> obj) {
                        return "[" + obj.getKey() + ", " + obj.getValue() + "]";
                    }
                }, sb);
                logger.debug(sb.toString());
            }

            AssessmentUpdate.Builder updateBuilder = AssessmentUpdate.newBuilder();
            updateBuilder.putAllAssessment(changedEntities);
            AssessmentUpdate updateMsg = updateBuilder.build();

            try {
                lastTimeSentChangedEntities = System.nanoTime();
                restClient.post(new URL(restConfiguration.getServerURL() + UPDATE_LEARNER_STATE),
                        updateMsg.toByteArray());
            } catch (Exception e) {
                logger.error("Caught exception while trying to call ARES update learner state REST API", e);
                throw new DetailedException("Failed to update the learner state",
                        "There was a problem updating the learner state because an exception was thrown.", e);
            }
        }
    }

    // TODO: Move to a new class
    /**
     * Creates a connection to the ARES RabbitMQ message bus.
     *
     * @throws ConfigurationException if there was a problem connecting
     */
    private void connectToRabbitMq() throws ConfigurationException{
    	amqpConnectionFactory = new ConnectionFactory();
        amqpConnectionFactory.setHost(amqpConfiguration.getNetworkAddress());
        amqpConnectionFactory.setPort(amqpConfiguration.getNetworkPort());
        amqpConnectionFactory.setUsername(amqpConfiguration.getUser());
        amqpConnectionFactory.setPassword(amqpConfiguration.getPassword());

        try {
        	amqpConnection = amqpConnectionFactory.newConnection();
			giftChannel = amqpConnection.createChannel();
			giftChannel.exchangeDeclare(amqpGiftExchangeName, amqpExchangeType, false);
			String giftQueueName = giftChannel.queueDeclare("", false, true, true, null).getQueue();
	        giftChannel.queueBind(giftQueueName, amqpGiftExchangeName, "");
	        giftChannel.basicConsume(giftQueueName, false, "ScenarioConsumer", new AresGiftMessageHandler(giftChannel, this));

	        scenarioChannel = amqpConnection.createChannel();
	        scenarioChannel.exchangeDeclare(amqpScenarioExchangeName, amqpExchangeType, false);
			String scenarioQueueName = giftChannel.queueDeclare("", false, true, true, null).getQueue();
			scenarioChannel.queueBind(scenarioQueueName, amqpScenarioExchangeName, "");
			scenarioChannel.basicConsume(scenarioQueueName, false, "ScenarioConsumer", new AresScenarioMessageHandler(scenarioChannel, this));

	        logger.info("Connected to ARES");

		} catch (IOException | TimeoutException e) {
			logger.error("Error connecting to ARES RabbitMQ server", e);
			throw new ConfigurationException("Failed to connect to the ARES message bus.",
			        "There was an exception when trying to connect to the RabbitMQ server using the following attributes:\n"
			        + "URL = "+amqpConfiguration.getNetworkAddress()+"\n"
			        + "Port = "+amqpConfiguration.getNetworkPort()+"\n"
			        + "User = "+amqpConfiguration.getUser()+"\n"
			        + "Password = "+amqpConfiguration.getPassword()+"\n\n"
			        + "Are you sure that ARES is running with these parameters?\n\nAlso, is there another open GIFT connection to ARES?  GIFT can only have one connection to ARES at a time.",e);
		}
    }

    private void disconnectFromRabbitMq() {
    	if (giftChannel != null) {
            try {
                if (giftChannel.isOpen()) {
                    giftChannel.close();
                }
                giftChannel = null;
                if (amqpConnection.isOpen()) {
                    amqpConnection.close();
                }
                amqpConnection = null;
            } catch (IOException | TimeoutException e) {
            	logger.debug("Error disconnection from ARES RabbitMQ server", e);
            }
        }
    }

    @Override
    public void setEnabled(boolean value) throws ConfigurationException {

        if (isEnabled() && !value){
            //transition from enabled to not enabled
        	disconnectFromRabbitMq();

            /* Clear the roleToEntityDataMap since ARES is no longer monitoring
             * that scenario */
            roleToEntityDataMap.clear();

        } else if(!isEnabled() && value){
            //transition from not enabled to enabled
        	connectToRabbitMq();
        }

        super.setEnabled(value);
    }

    @Override
    public List<TrainingApplicationEnum> getReqTrainingAppConfigurations() {
        return REQ_TRAINING_APPS;
    }

    @Override
    public List<MessageTypeEnum> getProducedMessageTypes(){
        return PRODUCED_MSG_TYPES;
    }

    /**
     * The returned ARES scenarios object is a JSON encoded string with the following format:
     *
     * {"scenario": [
     *     {"description": "", "id": "dc3f24b9c0fe4acd858eba0849a2ab29", "name": "Ft Bliss"},
     *     {"description": "", "id": "85bdff3b3b464d7cb4f27d17d354034a", "name": "Kuzan"}
     * ]}
     *
     */
    @Override
    public Serializable getScenarios() throws DetailedException{

        try {
            byte[] returnValue = restClient.get(new URL(restConfiguration.getServerURL() + SCENARIOS_CMD));
            String scenarios = new String(returnValue);
            logger.info("Received list of ARES scenarios:\n"+scenarios);
            return scenarios;
        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the list of ARES scenarios.",
                    "There was an exception thrown when using the ARES REST API of '"+restConfiguration.getServerURL() + SCENARIOS_CMD+"'.", e);
        }

    }

    /**
     * The returned ARES selectable objects object is a JSON encoded string with the following format:
     *
     * {"tactical_graphic_list": [
     *     {"symbol": {"attributes": {"SIZE": "40"}, "sidc": "GFGPGPP--------"}, "id": "1c3b254c-c40f-4255-bbc5-c0cff99b35ea", "location": [{"latitude": 31.844987803094757, "longitude": -106.4348137211}], "name": "RoutePoint-1"}
     *   ],
     *  "unit_list": [
     *     {"symbol": {"attributes": {"SIZE": "40"}, "sidc": "SHGPUCI----D---"}, "id": "71ead2d9-90c3-4a9e-9738-00fdb9a74db6", "location": {"latitude": 31.841964422408378, "longitude": -106.42444090310346}, "name": "unit5"},
     *     {"symbol": {"attributes": {"SIZE": "40"}, "sidc": "SHGPUCI----D---"}, "id": "4d89654b-f5df-4968-9fc5-589c30a2d03c", "location": {"latitude": 31.84555867358575, "longitude": -106.42457416554134}, "name": "unit4"}
     *   ],
     *  "tactical_geometry_list": [
     *     {"linear": {"color": {"a": 1.0, "g": 1.0}, "width": {"value": 3}}, "id": "dddac15a-18b3-46f5-bc6d-978742d8dbb6", "location": [{"latitude": 31.847277684878044, "longitude": -106.41722857337481}, {"latitude": 31.846534372472895, "longitude": -106.41276329126595}, {"latitude": 31.842410185891225, "longitude": -106.41108255269683}], "name": "dddac15a-18b3-46f5-bc6d-978742d8dbb6"},
     *     {"id": "b441606c-c853-4212-a138-f8d144d2c3b7", "areal": {"lineColor": {"a": 1.0, "g": 1.0}, "fillColor": {"a": 1.0, "g": 1.0}, "lineWidth": {"value": 3}}, "location": [{"latitude": 31.83248336482852, "longitude": -106.42365054202128}, {"latitude": 31.831859941952366, "longitude": -106.4199127567208}, {"latitude": 31.829306304306602, "longitude": -106.41961172197807}], "name": "b441606c-c853-4212-a138-f8d144d2c3b7"}
     *  ]}

     */
    @Override
    public Serializable getSelectableObjects() {

        try {
            byte[] returnValue = restClient.get(new URL(restConfiguration.getServerURL() + SCENARIO_OBJECTS_CMD));
            String objects = new String(returnValue);
            logger.info("Received list of ARES scenario's selectable objects:\n"+objects);
            return objects;
        } catch (Exception e) {
            throw new DetailedException("Failed to retrieve the list of ARES scenario's selectable objects.",
                    "There was an exception thrown when using the ARES REST API of '"+restConfiguration.getServerURL() + SCENARIO_OBJECTS_CMD+"'.", e);
        }
    }

    /**
     * The scenario identifier can either be an ARES scenario ID or the full path to
     * a local ARES scenario zip file.
     */
    @Override
    public void loadScenario(String scenarioIdentifier)
            throws DetailedException {

        currentScenarioName = "";  //reset before loading so the next call to get the currently loaded scenario return null

        if(scenarioIdentifier == null){

            try{
                //call the ARES REST request for clearing a scenario
                restClient.post(new URL(restConfiguration.getServerURL() + CLEAR_CMD));
            }catch(Exception e){
                logger.error("Caught exception while trying to call ARES clear REST API", e);
                throw new DetailedException("Failed to clear the ARES scenario",
                        "There was a problem clearing the ARES scenario because an exception was thrown that reads:\n"+
                                e.getMessage()+"\n\n"
                        + "GIFT is trying to communicate with ARES using the following attributes:\n"
                    + "URL = "+amqpConfiguration.getNetworkAddress()+"\n"
                    + "Port = "+amqpConfiguration.getNetworkPort()+"\n"
                    + "User = "+amqpConfiguration.getUser()+"\n"
                    + "Password = "+amqpConfiguration.getPassword()+"\n\n"
                    + "Are you sure that ARES is running with these parameters?\n\nAlso, is there another open GIFT connection to ARES?  GIFT can only have one connection to ARES at a time.", e);
            }
        }else{

            File file = new File(scenarioIdentifier);
            if(file.exists()){

                logger.info("Send scenario zipfile " + file.getName() + " to ARES.");

                setLoadParams();

                Map<String, File> files = new HashMap<>();
                files.put(CONTENT_DISPOSITION_FIELD_NAME, file);

                try {
                    restClient.post(new URL(restConfiguration.getServerURL() + LOAD_CMD), files, params);
                } catch (Exception e) {
                    logger.error("Caught exception while trying to call ARES load scenario file REST API", e);
                    throw new DetailedException("Failed to load the ARES scenario",
                            "There was a problem loading the ARES scenario referenced by the course file named '"+scenarioIdentifier+" because an exception was thrown.", e);
                }
            } else{

                logger.info("Send ARES the name of the scenario file to load.  ("+scenarioIdentifier+")");

                try{
                    setLoadParams();

                    //call the ARES REST request for loading a scenario
                    restClient.post(new URL(restConfiguration.getServerURL() + LOAD_CMD + Constants.FORWARD_SLASH + scenarioIdentifier), params);
                }catch(Exception e){
                    logger.error("Caught exception while trying to call ARES load scenario REST API", e);
                    throw new DetailedException("Failed to load the ARES scenario",
                            "There was a problem loading the ARES scenario referenced by the id of '"+scenarioIdentifier+" because an exception was thrown.", e);
                }
            }
        }
    }

    /**
     * Requests that ARES modifies how it visualizes its data to match the given configuration
     *
     * @param config the configuration defining how ARES should visualize its data. Cannot be null.
     */
    private void configureMonitoring(ExternalMonitorConfig config) {

        if(config == null) {
            throw new IllegalArgumentException("The monitor configuration to send to ARES cannot be null");
        }

        StringBuilder queryBuilder = new StringBuilder();
        boolean isFirstSetting = true;

        //build a query string containing the settings to configure
        for(Entry<ExternalMonitorConfig.Setting, Boolean> entry : config.getSettings().entrySet()) {

            Boolean value = entry.getValue();

            if(value != null) {

                if(isFirstSetting) {
                    isFirstSetting = false;

                } else {
                    queryBuilder.append(Constants.AND);
                }

                queryBuilder.append(entry.getKey())
                    .append(Constants.EQUALS)
                    .append(value ? 1 : 0);
            }
        }

        try{
            //call the ARES REST request for configuring how it handles visualization
            restClient.post(new URL(restConfiguration.getServerURL() + CONFIG_CMD + Constants.QUESTION_MARK + queryBuilder.toString()));
        }catch(Exception e){
            logger.error("Caught exception while trying to call ARES clear REST API", e);
            throw new DetailedException("Failed to configure the ARES viewer",
                    "There was a problem configuring the ARES viewer because an exception was thrown that reads:\n"+
                            e.getMessage()+"\n\n"
                    + "GIFT is trying to communicate with ARES using the following attributes:\n"
                + "URL = "+amqpConfiguration.getNetworkAddress()+"\n"
                + "Port = "+amqpConfiguration.getNetworkPort()+"\n"
                + "User = "+amqpConfiguration.getUser()+"\n"
                + "Password = "+amqpConfiguration.getPassword()+"\n\n"
                + "Are you sure that ARES is running with these parameters?\n\nAlso, is there another open GIFT connection to ARES?  GIFT can only have one connection to ARES at a time.", e);
        }
    }

    /**
     * Populates the params map with REST API parameters needed when loading an ARES scenario
     */
    private void setLoadParams(){

        String contextMode = mode == InteractionMode.Learner ? GIFT_LEARNER_MODE : GIFT_WRAP_MODE;
        params.put(CONTROLLER_CONTEXT_PARAM, contextMode);
    }

    /**
     * Return a JSON object that contains the ARES scenario data
     *
     * @return the JSON object that contains the currently loaded ARES scenario data:
     *
     * {"current_scenario_name": "Kuzun", "current_scenario_id": "2a51941357184b09bc8c5f1d65b54c56"}
     *
     * When ARES does not have a scenario loaded it will return
     *
     * {"current_scenario_name": "", "current_scenario_id": ""}
     */
    private String getCurrentScenarioData(){
    	return "{\"current_scenario_name\": \""+currentScenarioName+"\", \"current_scenario_id\": \""+currentScenarioId+"\"}";
    }

    @Override
    public void cleanup() {
        //nothing to do
    }

    @Override
    public File exportScenario(File exportFolder) throws DetailedException {

        File tempFile = null;
        try {
            byte[] result = restClient.get(new URL(restConfiguration.getServerURL() + SNAPSHOT_CMD));

            if(result == null || result.length == 0){
                throw new Exception("Failed to receive a valid response to the ARES scenario export request.");
            }

            tempFile = File.createTempFile(TEMP_ARES_FILE_PREFIX, Long.toString(System.nanoTime()) + ".zip", exportFolder);

            FileUtil.registerFileToDeleteOnShutdown(tempFile);   //in case this JVM goes down before any cleanup code can be called
                                                                 //In the future this may not be the best solution when the Gateway module instance
                                                                 //runs for an extended amount of time and downloads can accumulate.

            ZipUtils.zipFromInputStream(new ByteArrayInputStream(result), tempFile);

            return tempFile;

        } catch (Exception e) {
            logger.error("Caught exception while trying to call ARES snapshot REST API", e);

            if(tempFile != null && tempFile.exists()){
                tempFile.delete();
            }

            throw new DetailedException("Failed to export the ARES scenario",
                    "There was a problem exporting the ARES scenario because an exception was thrown.", e);
        }
    }

    @Override
    public void selectObject(Serializable objectIdentifier)
            throws DetailedException {

        try{
            restClient.post(new URL(restConfiguration.getServerURL() + "/services/scenario/controller/" + objectIdentifier + "/select"));
        }catch(Exception e){
            logger.error("Caught exception while trying to set selected object in ARES using the object identifier of '"+objectIdentifier+"'.", e);
            throw new DetailedException("Failed to notify ARES about a selected object.",
                    "There was a problem notifying ARES about the selected object identified by '"+objectIdentifier+"' because an exception was thrown.", e);
        }
    }

    /**
     * The returned ARES scenarios object is a JSON encoded string with the following format:
     *
     * {"current_scenario_name": "Kuzun", "current_scenario_id": "2a51941357184b09bc8c5f1d65b54c56"}
     *
     * When ARES does not have a scenario loaded it will return null
     */
    @Override
    public Serializable getCurrentScenarioMetadata() throws DetailedException {

        try{
            String value = getCurrentScenarioData();

            //Returns null if the restClient returns a field with an empty string value
            //Format will match: {"current_scenario_name": "", "current_scenario_id": ""}
            JSONParser parser = new JSONParser();
            JSONObject metadata = (JSONObject) parser.parse(value);
            String name = (String) metadata.get(NAME_KEY);
            String id = (String) metadata.get(ID_KEY);

            if(name.isEmpty() || id.isEmpty()) {
                return null;
            }

            return value;
        }catch(Exception e){
            logger.error("Caught exception while trying to get the current loaded scenario metadata.", e);
            throw new DetailedException("Failed to retrieve the current ARES scenario metadata.",
                    "There was a problem retrieving the current ARES scenario metadata because an exception was thrown.", e);
        }
    }
    
    @Override
    public DISDialect getDisDialect() {
        return DISDialect.ARES;
    }

    /**
     * Container class to hold the entity data including the identifier and
     * assessment value.
     *
     * @author sharrison
     */
    private class EntityAssessmentData {
        /** The entity identifier */
        private EntityIdentifier entityId;

        /** The result of the performance evaluation for the entity */
        private EvaluationResult evaluationResult;

        /** The compound key used for ARES to identify the entity */
        private String compoundEntityKey;

        /**
         * Default Constructor.
         */
        public EntityAssessmentData() {
        }

        /**
         * Constructor.
         *
         * @param evaluationResult the evaluation result.
         */
        public EntityAssessmentData(EvaluationResult evaluationResult) {
            setEvaluationResult(evaluationResult);
        }

        /**
         * Retrieve the entity identifier.
         *
         * @return the entity identifier. Can be null if it was never set.
         */
        public EntityIdentifier getEntityId() {
            return entityId;
        }

        /**
         * Set the entity identifier. Rebuilds the entity's
         * {@link #compoundEntityKey}.
         *
         * @param entityId the new entity identifier. Can't be null.
         */
        public void setEntityId(EntityIdentifier entityId) {
            if (entityId == null) {
                throw new IllegalArgumentException("The parameter 'entityId' cannot be null.");
            }

            this.entityId = entityId;

            final SimulationAddress simAddress = entityId.getSimulationAddress();
            this.compoundEntityKey = StringUtils.join(".",
                    Arrays.asList(simAddress.getSiteID(), simAddress.getApplicationID(), entityId.getEntityID()));
        }

        /**
         * Retrieve the performance evaluation for the entity.
         *
         * @return the performance evaluation. Can be null if it was never set.
         */
        public EvaluationResult getEvaluationResult() {
            return evaluationResult;
        }

        /**
         * Set the evaluation result.
         *
         * @param evaluationResult the evaluation result to set. Can't be null.
         */
        public void setEvaluationResult(EvaluationResult evaluationResult) {
            if (evaluationResult == null) {
                throw new IllegalArgumentException("The parameter 'evaluationResult' cannot be null.");
            }

            this.evaluationResult = evaluationResult;
        }

        /**
         * Get the assessment type based on the current
         * {@link AssessmentLevelEnum}.
         *
         * @return the {@link AssessmentType}
         */
        public AssessmentType getAssessmentType() {
            if (evaluationResult == null) {
                return null;
            }

            final AssessmentLevelEnum assessment = evaluationResult.getAssessmentLevel();
            if (assessment == null) {
                /* Always process visual only */
                return AssessmentType.VISUAL_ONLY;
            } else if (AssessmentLevelEnum.UNKNOWN.equals(assessment)) {
                return AssessmentType.UNKNOWN_ASSESSMENT;
            } else if (AssessmentLevelEnum.BELOW_EXPECTATION.equals(assessment)) {
                return AssessmentType.BELOW_EXPECTATION;
            } else if (AssessmentLevelEnum.AT_EXPECTATION.equals(assessment)) {
                return AssessmentType.AT_EXPECTATION;
            } else if (AssessmentLevelEnum.ABOVE_EXPECTATION.equals(assessment)) {
                return AssessmentType.ABOVE_EXPECTATION;
            }

            return null;
        }

        /**
         * Retrieve the unique compound key for a given entity.
         *
         * @return the entity's unique compound key.
         */
        public String getCompoundEntityKey() {
            return compoundEntityKey;
        }
    }
}
