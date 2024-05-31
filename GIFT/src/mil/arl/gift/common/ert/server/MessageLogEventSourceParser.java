/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.io.File;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.DisplayMediaCollectionRequest;
import mil.arl.gift.common.DisplayMediaTutorRequest;
import mil.arl.gift.common.DisplayMessageTutorRequest;
import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails;
import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.event.AbstractEvent;
import mil.arl.gift.common.ert.event.ApplyStrategyEvent;
import mil.arl.gift.common.ert.event.AuthorizeStrategiesRequestEvent;
import mil.arl.gift.common.ert.event.BranchPathHistoryEvent;
import mil.arl.gift.common.ert.event.CourseStateEvent;
import mil.arl.gift.common.ert.event.DefaultEvent;
import mil.arl.gift.common.ert.event.DisplayMediaCollectionEvent;
import mil.arl.gift.common.ert.event.DomainSessionEvent;
import mil.arl.gift.common.ert.event.EvaluatorUpdateRequestEvent;
import mil.arl.gift.common.ert.event.ExecuteOCStrategyEvent;
import mil.arl.gift.common.ert.event.FilteredSensorDataEvent;
import mil.arl.gift.common.ert.event.GeolocationEvent;
import mil.arl.gift.common.ert.event.InitializeDomainSessionRequestEvent;
import mil.arl.gift.common.ert.event.KnowledgeAssessmentDetailsEvent;
import mil.arl.gift.common.ert.event.LearnerStateEvent;
import mil.arl.gift.common.ert.event.LessonCompletedEvent;
import mil.arl.gift.common.ert.event.LessonStartedEvent;
import mil.arl.gift.common.ert.event.PedagogicalRequestEvent;
import mil.arl.gift.common.ert.event.PerformanceAssessmentEvent;
import mil.arl.gift.common.ert.event.PowerPointStateEvent;
import mil.arl.gift.common.ert.event.SensorDataEvent;
import mil.arl.gift.common.ert.event.SurveyResultEvent;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.logger.MessageLogReader;
import mil.arl.gift.common.sensor.FilteredSensorData;
import mil.arl.gift.common.sensor.UnfilteredSensorData;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.ta.state.Geolocation;
import mil.arl.gift.common.ta.state.PowerPointState;
import mil.arl.gift.net.api.message.Message;

/**
 * This class is responsible for converting message log entries into events for the ERT.
 * 
 * @author mhoffman
 *
 */
public class MessageLogEventSourceParser extends AbstractEventSourceParser {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MessageLogEventSourceParser.class);
    
    /** 
     * column labels
     * Note: SPSS requires that the first row contain the column headers and the text contain no spaces, use underscores. 
     */
    public static final String DS_WRITE_TIME_COL_NAME = "DS_Write_Time";
    public static final String DS_WRITE_TIME_COL_DISPLAY_NAME = "Domain Session Write Time";    
    
    /**
     * the survey question tag property value to use as a key to find the participatn id survey question 
     * in a survey found by this parser
     */
    private static final String PARTICIPANT_ID_TAG = "participant_ID";
    
    /**
     * the columns for each type of message log file (system and domain session)
     */
    public static List<EventReportColumn> domainSessionColumns;
    public static List<EventReportColumn> systemColumns;  
    
    /** columns that contain time values */
    public static List<EventReportColumn> timeColumns;
    
    /** the column that contains the elapsed domain session time when the message was written to the log file */
    public static final EventReportColumn DS_WRITE_TIME_COLUMN = new EventReportColumn(DS_WRITE_TIME_COL_DISPLAY_NAME, MessageLogEventSourceParser.DS_WRITE_TIME_COL_NAME);
    
    static{
        
        // don't include domain session write time by default in reports, more of a developer debug column at this point 
        DS_WRITE_TIME_COLUMN.setEnabled(false);
        
        domainSessionColumns = new ArrayList<EventReportColumn>();
        domainSessionColumns.add(EventReportColumn.TIME_COLUMN);
        domainSessionColumns.add(EventReportColumn.DS_TIME_COLUMN);
        domainSessionColumns.add(EventReportColumn.DKF_TIME_COLUMN);
        domainSessionColumns.add(EventReportColumn.EVENT_TYPE_COLUMN);
        domainSessionColumns.add(EventReportColumn.USER_ID_COLUMN);
        domainSessionColumns.add(EventReportColumn.USERNAME_COLUMN);
        domainSessionColumns.add(EventReportColumn.DS_ID_COLUMN);
        domainSessionColumns.add(EventReportColumn.CONTENT_COLUMN);
        domainSessionColumns.add(DS_WRITE_TIME_COLUMN);
        
        systemColumns = new ArrayList<EventReportColumn>();
        systemColumns.add(EventReportColumn.TIME_COLUMN);
        systemColumns.add(EventReportColumn.EVENT_TYPE_COLUMN);
        systemColumns.add(EventReportColumn.USER_ID_COLUMN);
        systemColumns.add(EventReportColumn.CONTENT_COLUMN);
        
        timeColumns = new ArrayList<EventReportColumn>();
        timeColumns.add(EventReportColumn.TIME_COLUMN);
        timeColumns.add(EventReportColumn.DS_TIME_COLUMN);
        timeColumns.add(EventReportColumn.DKF_TIME_COLUMN);
        timeColumns.add(DS_WRITE_TIME_COLUMN);
    }

    /** instance of the reader that parses a message log */
    private MessageLogReader reader;
    
    /** the message log file being read */
    private File file;
    
    /** configuration for the report */
    private ReportProperties reportProperties;
    
    /** mapping of event type to the events of that type, ordered by how they appear in the source of that data (e.g. domain session log, sensor data file) */
    private Map<EventType, List<AbstractEvent>> eventTypeToEvent;
    
    /** mapping of message type enum to the created event type for it */
    private Map<MessageTypeEnum, EventType> messageTypeToEventType;

    /**
     * Class constructor - parses the message log file.
     * 
     * @param file a message log file.  Can't be null and must exist.
     * @param reportProperties contains configuration parameters and other properties for an event report.  Can't be null.
     * @throws Exception if there was a severe problem parsing the message log file
     */
    public MessageLogEventSourceParser(File file, ReportProperties reportProperties) throws Exception{
        
        if(file == null){
            throw new IllegalArgumentException("The file can't be null");
        }else if(!file.exists()){
            throw new IllegalArgumentException("The file "+file+" doesn't exist.");
        }
        this.file = file;
        
        if(reportProperties == null){
            throw new IllegalArgumentException("The report properties can't be null");
        }
        this.reportProperties = reportProperties;
        
        try{
            init();
        }catch(Exception e){
            throw new Exception("Failed to parse the message log file named "+file.getName()+".", e);
        }
    }
    
    /**
     * Parse the message log file
     * @throws Exception if there was a severe problem parsing the message log file
     */
    private void init() throws Exception{
        
        if(logger.isInfoEnabled()){
            logger.info("Initializing message log parser for file = "+file.getName()+" ...");
        }

        reader = MessageLogReader.createMessageLogReader(file.getName());
        reader.parseLog(new FileProxy(new File(file.getAbsolutePath())));
        
        Set<MessageTypeEnum> messageTypes = reader.getTypesOfMessages();
        if(logger.isInfoEnabled()){
            logger.info("Found "+messageTypes.size()+" unique message types to select from in '"+file.getName()+".");
        }
        
        eventTypeToEvent = new HashMap<EventType, List<AbstractEvent>>(messageTypes.size());
        messageTypeToEventType = new HashMap<>();
        for(MessageTypeEnum type : messageTypes){
            
            // Keep track of a map of sourceEventIds to their sender addresses to filter out duplicate messages
            HashMap<SimpleEntry<Integer, String>, MessageSource> duplicateFilter = new HashMap<SimpleEntry<Integer, String>, MessageSource>();
            
            //get the possible set of columns based on the message type
            List<Message> messages = reader.getMessagesByType(type);
            if(messages == null){
                logger.error("Unable to find the messages for message type "+type);
                break;
            }

            List<AbstractEvent> events = new ArrayList<AbstractEvent>(messages.size());
            HashMap<String, EventReportColumn> columns = new HashMap<String, EventReportColumn>();
            
            for(Message message : messages){
                
                // Filter messages if a message has already been added with the same sourceEventId and sender
                MessageSource source = new MessageSource(message.getSourceEventId(), message.getSenderAddress());
                
                // Add the event if it doesn't have a sourceEventId or it does but it hasn't been added to our duplicate filter list
                SimpleEntry<Integer, String> key = new SimpleEntry<Integer, String>(source.sourceEventId, source.senderAddress);
                if (source.sourceEventId == Message.ID_NOT_AVAILABLE || !duplicateFilter.containsKey(key)) {
                    duplicateFilter.put(key, source);
                    
                    AbstractEvent event = getEvent(message);
                    
                    if(event == null){
                        logger.error("Unable to create an event from message =\n"+message+"\n from message file '"+file.getName()+"'.");
                        continue;
                    }
                    
                    //add event for each message of the current message type
                    events.add(event);
                    
                    //add the specific event columns, uniquely by name - don't want duplicate column names
                    for(EventReportColumn column : event.getColumns()){
                        columns.put(column.getColumnName(), column);
                    }
                }
            }
            
            List<EventReportColumn> messageColumns = new ArrayList<EventReportColumn>(columns.values());            
            
            EventType eventType = new EventType(type.getName(), type.getDisplayName(), type.getDescription(), messageColumns);
            messageTypeToEventType.put(type, eventType);
            eventTypeToEvent.put(eventType, events);
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Finished initializing parser");
        }
    }
    
    /**
     * Create an event for the ERT based on the message provided.
     * 
     * @param message
     * @return AbstractEvent
     */
    private AbstractEvent getEvent(Message message){
        
        AbstractEvent event = null;
        
        MessageTypeEnum mType = message.getMessageType();

        if(mType == MessageTypeEnum.LEARNER_STATE){
            event = new LearnerStateEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (LearnerState)message.getPayload());
        
        }else if(mType == MessageTypeEnum.PEDAGOGICAL_REQUEST){
            event = new PedagogicalRequestEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (PedagogicalRequest)message.getPayload());

        }else if(mType == MessageTypeEnum.SENSOR_DATA){
            event = new SensorDataEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (UnfilteredSensorData)message.getPayload());

        }else if(mType == MessageTypeEnum.SENSOR_FILTER_DATA){
            event = new FilteredSensorDataEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (FilteredSensorData)message.getPayload());

        }else if(mType == MessageTypeEnum.PERFORMANCE_ASSESSMENT){
            event = new PerformanceAssessmentEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (PerformanceAssessment)message.getPayload());

        }else if(mType == MessageTypeEnum.SUBMIT_SURVEY_RESULTS){
            event = new SurveyResultEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (SubmitSurveyResults)message.getPayload(),
                    reportProperties.shouldUseSurveyQuestionTextForHeader());
            
        }else if(mType == MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE){
            event = new SurveyResultEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (AbstractQuestionResponse)message.getPayload(),
                    reportProperties.shouldUseSurveyQuestionTextForHeader());
            
        }else if(mType == MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY){
            event = new SurveyResultEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (SurveyResponse)message.getPayload(),
                    reportProperties.shouldUseSurveyQuestionTextForHeader());
            
        }else if(mType == MessageTypeEnum.COURSE_STATE){
            event = new CourseStateEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (CourseState)message.getPayload());
            
        }else if(mType == MessageTypeEnum.LESSON_STARTED){
            event = new LessonStartedEvent(message.getTimeStamp(), ((DomainSessionMessageEntry) message));
            
        }else if(mType == MessageTypeEnum.LESSON_COMPLETED){
            
            if(message.getPayload() instanceof LessonCompleted){
                event = new LessonCompletedEvent(message.getTimeStamp(), 
                        ((DomainSessionMessageEntry) message),
                        (LessonCompleted)message.getPayload());
            }else{
                // legacy JSON message, pre July 2021, no payload
                event = new LessonCompletedEvent(message.getTimeStamp(), 
                        ((DomainSessionMessageEntry) message));
            }
            
        }else if(mType == MessageTypeEnum.INITIALIZE_DOMAIN_SESSION_REQUEST){
            event = new InitializeDomainSessionRequestEvent(message.getTimeStamp(),
                    ((DomainSessionMessageEntry) message),
                    (InitializeDomainSessionRequest)message.getPayload());

        }else if(mType == MessageTypeEnum.BRANCH_PATH_HISTORY_UPDATE){
            event = new BranchPathHistoryEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (BranchPathHistory)message.getPayload());
            
        }else if(mType == MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST){
            event = new AuthorizeStrategiesRequestEvent(message.getTimeStamp(), 
                    (DomainSessionMessageEntry)message, 
                    (AuthorizeStrategiesRequest)message.getPayload());
            
        }else if(mType == MessageTypeEnum.APPLY_STRATEGIES){
            event = new ApplyStrategyEvent(message.getTimeStamp(), 
                    (DomainSessionMessageEntry)message, 
                    (ApplyStrategies)message.getPayload());
            
        }else if(mType == MessageTypeEnum.EXECUTE_OC_STRATEGY){
            event = new ExecuteOCStrategyEvent(message.getTimeStamp(), 
                    (DomainSessionMessageEntry)message, 
                    (ExecuteOCStrategy)message.getPayload());            
            
        }else if(mType == MessageTypeEnum.DISPLAY_LESSON_MATERIAL_TUTOR_REQUEST){
            
            if(message.getPayload() instanceof DisplayMediaCollectionRequest){
                event = new DisplayMediaCollectionEvent(message.getTimeStamp(), 
                        ((DomainSessionMessageEntry) message), 
                        (DisplayMediaCollectionRequest)message.getPayload());
            }
            
        }else if(mType == MessageTypeEnum.DISPLAY_CONTENT_TUTOR_REQUEST){
            
            if(message.getPayload() instanceof DisplayMessageTutorRequest){
                event = new DisplayMediaCollectionEvent(message.getTimeStamp(), 
                        ((DomainSessionMessageEntry) message), 
                        (DisplayMessageTutorRequest)message.getPayload());
            }else if(message.getPayload() instanceof DisplayMediaTutorRequest){
                event = new DisplayMediaCollectionEvent(message.getTimeStamp(),
                        ((DomainSessionMessageEntry) message),
                        (DisplayMediaTutorRequest)message.getPayload());
            }
            
        }else if(mType == MessageTypeEnum.POWERPOINT_STATE){
            event = new PowerPointStateEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (PowerPointState)message.getPayload());
            
        }else if(mType == MessageTypeEnum.EVALUATOR_UPDATE_REQUEST){
            event = new EvaluatorUpdateRequestEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message),
                    (EvaluatorUpdateRequest) message.getPayload());
            
        }else if(mType == MessageTypeEnum.GEOLOCATION){
            event = new GeolocationEvent(message.getTimeStamp(),
                    ((DomainSessionMessageEntry) message),
                    (Geolocation) message.getPayload());

            
        }else if(mType == MessageTypeEnum.KNOWLEDGE_ASSESSMENT_DETAILS){
            event = new KnowledgeAssessmentDetailsEvent(message.getTimeStamp(), 
                    ((DomainSessionMessageEntry) message), 
                    (KnowledgeAssessmentDetails)message.getPayload());
        }else{
            
            String payload = message.getPayload() != null ? message.getPayload().toString() : null;
            if(message instanceof DomainSessionMessageEntry){
              //this domain session message entry has no corresponding event class, use generic event class
                event = new DomainSessionEvent(message.getMessageType().getDisplayName(), 
                        message.getTimeStamp(), 
                        ((DomainSessionMessageEntry) message), 
                        payload);
                
            } else {
                //found an admin/system message event
                event = new DefaultEvent(message.getMessageType().getDisplayName(), message.getTimeStamp(), payload);
            }
        }            
        
        return event;
    }
    
    /**
     * Return whether the message log file is a domain session log file
     * 
     * @return boolean
     */
    public boolean isDomainSessionLog(){
        return reader.isDomainSessionLog();
    }

    @Override
    public List<EventType> getTypesOfEvents() {
        return new ArrayList<EventType>(eventTypeToEvent.keySet());
    }

    @Override
    public List<AbstractEvent> getEventsByType(EventType eventType) {        
        return eventTypeToEvent.get(eventType);
    }
    
    /**
     * Return the event type for the message type enum provided.
     * @param messageType the message type to get an event type for.
     * @return the event type for the message type.  Can be null if the message type was
     * not found in the log.
     */
    public EventType getEventTypeByMessageType(MessageTypeEnum messageType){
        return messageTypeToEventType.get(messageType);
    }
    
    /**
     * Return the message log reader for this source parser.
     * @return the reader which has access to all the messages from the source
     */
    public MessageLogReader getMessageLogReader(){
        return reader;
    }
    
    @Override
    public List<EventReportColumn> getDefaultColumns() {
        
        List<EventReportColumn> reportColumns;
        
        if (reader.isDomainSessionLog()) {
            reportColumns = domainSessionColumns;
        } else {
            reportColumns = systemColumns;
        }
        
        return reportColumns;
    }
    
    /**
     * Return whether the file is a message log file by analyzing its content.
     * Specifically this method checks for the codec delimiters.
     * 
     * @param file the file to check
     * @return boolean
     */
    public static boolean isMessageLog(File file){
        
        boolean checkPassed = false;

        try {
            MessageLogReader logReader = MessageLogReader.createMessageLogReader(file.getName());
            checkPassed = logReader.isMessageLog(new FileProxy(file));
        } catch (Exception e) {
            logger.error("Caught exception while trying to determine if the file: " + file.getName()
                    + " is a message log file", e);
        }
        
        if(logger.isInfoEnabled()){
            logger.info("Check of file named = "+file.getName()+" to be a message log event source file resulted in "+checkPassed);
        }
        
        return checkPassed;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[MessageLogEventSourceParser: ");
        sb.append("file = ").append(file.getName());
        
        sb.append(", eventTypes: {");
        for(EventType type : eventTypeToEvent.keySet()){
            sb.append(type.toString()).append(", ");
        }
        sb.append("}");
        sb.append("]");
        
        return sb.toString();
    }

    /**
     * Custom class to filter messages by their sources by creating hash codes for each source and it's sender
     * 
     * @author cpadilla
     *
     */
    private class MessageSource {
        
        /** The messages source event id */
        public int sourceEventId;
        
        /** The address of the sender */
        public String senderAddress;
        
        /**
         * Constructor
         * 
         * @param sourceEventId - The messages source event Id
         * @param senderAddress - The address of the sender
         */
        public MessageSource(int sourceEventId, String senderAddress) {
            this.sourceEventId = sourceEventId;
            this.senderAddress = senderAddress;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof MessageSource)) {
                return false;
            }

            if (obj == this) {
                return true;
            }

            return this.sourceEventId == ((MessageSource) obj).sourceEventId &&
                    this.senderAddress.equals(((MessageSource) obj).senderAddress);
        }
        
        @Override
        public int hashCode() {
            return sourceEventId + senderAddress.hashCode();
        }
    }

    @Override
    public Integer getParticipantId() {

        List<Message> allSubmitSurveyResultsMsgs = reader.getMessagesByType(MessageTypeEnum.SUBMIT_SURVEY_RESULTS);

        if (allSubmitSurveyResultsMsgs == null) {
            return null;
        }

        for(Message message : allSubmitSurveyResultsMsgs){
            
            // double check this is a survey result event
            if(!(message.getPayload() instanceof SubmitSurveyResults)){
                continue;
            }
            
            SubmitSurveyResults submitSurveyResults = (SubmitSurveyResults) message.getPayload();
            SurveyResponse surveyResponse = submitSurveyResults.getSurveyResponse();
            for(SurveyPageResponse pageResponse : surveyResponse.getSurveyPageResponses()){
                
                for(AbstractQuestionResponse qResponse : pageResponse.getQuestionResponses()){
                    
                    String tag = qResponse.getSurveyQuestion().getTag();
                    if(tag != null && !tag.isEmpty() && tag.equalsIgnoreCase(PARTICIPANT_ID_TAG)){
                        // found a question with the participant id tag
                        
                        if(!qResponse.getResponses().isEmpty()){
                            String answerText = qResponse.getResponses().get(0).getText();
                            if(answerText != null){
                                
                                try{
                                    return Integer.valueOf(answerText);                                    
                                }catch(@SuppressWarnings("unused") Exception e){}
                            }
                        }

                    }
                }
            }
        }
        
        return null;    
    }
    
}
