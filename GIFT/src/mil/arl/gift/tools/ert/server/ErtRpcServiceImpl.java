/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.GenerateReportStatus;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.gwt.server.GiftServletUtils;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DownloadableFileRef;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.tools.dashboard.server.DashboardProperties;
import mil.arl.gift.tools.ert.client.ErtRpcService;
import mil.arl.gift.common.ert.event.AbstractEvent;
import mil.arl.gift.common.ert.event.BranchPathHistoryAggregate;
import mil.arl.gift.common.ert.event.BranchPathHistoryEvent;
import mil.arl.gift.common.ert.event.DomainSessionEvent;
import mil.arl.gift.common.ert.event.MidLessonAssessmenRemediationAgreggate;
import mil.arl.gift.common.ert.event.ParticipantAttemptCnt;
import mil.arl.gift.common.ert.event.PerformanceAssessmentEvent;
import mil.arl.gift.common.ert.event.TimeOnTaskAggregate;
import mil.arl.gift.common.ert.server.AbstractEventSourceParser;
import mil.arl.gift.common.ert.server.MessageLogEventSourceParser;
import mil.arl.gift.common.ert.server.ReportGenerationUtil;
import mil.arl.gift.common.ert.server.ReportWriter;
import mil.arl.gift.tools.ert.shared.EventSourceTreeNode;
import mil.arl.gift.tools.ert.shared.EventSourcesTreeModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class ErtRpcServiceImpl extends RemoteServiceServlet implements ErtRpcService {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ErtRpcServiceImpl.class);
    
    /** adding events task description for progress indicator */
    private static final String ADDING_EVENTS_TASK_DESC = "Adding events to the writer";
    
    /** creating an ERT report task description for progress indicator */
    private static final String CREATING_REPORT_TASK_DESC = "Creating report";
    
    /** testing writing to the report file task description for progress indicator */
    private static final String TESTING_WRITER_TASK_DESC = "Testing writer";

    /** used for file queries for ERT report generation */
    private EventReportServer eventReportServer = new EventReportServer();
    
    /** a mapping from each client IP address to its respective progress indicator */
    private final HashMap<String, GenerateReportStatus> ipToProgressIndicator = new HashMap<String, GenerateReportStatus>();
    
    /** where to search for log files */
    private File[] rootDirectories = null;

    /**
     * Default constructor - use the default location for searching for log files
     */
    public ErtRpcServiceImpl() {
        eventReportServer.findEventFiles();
    }
    
    /**
     * Use the specified directories for searching for log files
     * 
     * @param rootDirectories one or more directories to use instead of the default directories
     */
    public ErtRpcServiceImpl(File... rootDirectories){
        this.rootDirectories = rootDirectories;
        eventReportServer.findEventFiles(rootDirectories);
    }
    

    @Override
    public EventSourcesTreeModel getEventSources(boolean refresh) throws IllegalArgumentException {
        
        if(refresh){
            eventReportServer.findEventFiles(this.rootDirectories);
        }
        
        return eventReportServer.getTreeModel();
    }
    
    /**
     * Return the file associated with the event source node id provided.
     * 
     * @param nodeId unique event source node id
     * @return File
     */
    public File getFileForNode(int nodeId){
        return eventReportServer.getFileForNode(nodeId);
    }

    @Override
    public ReportProperties selectEventSource(List<Integer> eventSourceIds) throws Exception {
        return selectEventSource(eventSourceIds, DashboardProperties.getInstance().getDefaultEventTypes());
    }
    
    /**
     * Creates a report properties with information about events found in the event sources (e.g. domain session
     * message log, sensor writer file) being parsed.
     * 
     * @param eventSourceIds ERT created unique identifiers of an event source found
     * @param defaultEventNames list of GIFT event types to include automatically in the collection of selected
     * columns.  If null, all events found in the source(s) will be selected to be included.
     * @return the new report properties instance 
     * @throws Exception if there was a problem parsing an event source or creating the report properties
     */
    public ReportProperties selectEventSource(List<Integer> eventSourceIds, String[] defaultEventNames) throws Exception {
        
        ReportProperties reportProperties = new ReportProperties("anonymous", eventSourceIds, new ArrayList<EventType>(), new ArrayList<EventReportColumn>(), ReportProperties.DEFAULT_EMPTY_CELL, ReportProperties.DEFAULT_FILENAME);
        List<EventReportColumn> eventColumns = reportProperties.getReportColumns(); // this must be done after building report properties
        
        try{
            for(Integer eventSourceId : eventSourceIds){
                
                EventSourceTreeNode eventSourceNode = eventReportServer.getTreeModel().getNode(eventSourceId);
                if (eventSourceNode != null && !eventSourceNode.isFolder()) {

                    File file = eventReportServer.getFileForNode(eventSourceNode.getNodeId());
                    AbstractEventSourceParser parser = EventSourceUtil.getEventParser(file, new ReportProperties());
                    
                    if(parser == null){
                        logger.error("Unable to find an event parser for file named "+file.getName());
                        return null;
                    }

                    //add event types for this source
                    List<EventType> parserEventTypes = parser.getTypesOfEvents();
                    for(EventType eType : parserEventTypes){
                        
                        if(reportProperties.getEventTypeOptions().contains(eType)){
                            //extend existing event type with new columns
                            
                            EventType existingEventType = reportProperties.getEventType(eType);
                            
                            List<EventReportColumn> existingColumns = existingEventType.getEventColumns();
                            for(EventReportColumn possibleNewColumn : eType.getEventColumns()){
                                
                                if(!existingColumns.contains(possibleNewColumn)){
                                    //there isn't a column with that name, therefore add new column to existing event type
                                    
                                    existingColumns.add(possibleNewColumn);
                                }
                            }
                            
                        }else{
                            //don't add duplicates
                            reportProperties.addEventTypeOption(eType);
                        }
                    }
                    
                    //add event columns for this source
                    List<EventReportColumn> parserEventColumns = parser.getDefaultColumns();
                    for(EventReportColumn column : parserEventColumns){
                        
                        if(!eventColumns.contains(column)){
                            //don't add duplicates
                            eventColumns.add(column);
                        }
                    }


                } else if (eventSourceNode == null) {
                    logger.error("The node for nodeId " + eventSourceId + " is null");
                } else if (eventSourceNode.isFolder()) {
                    logger.error("The node for nodeId " + eventSourceId + " is a folder");
                }
            } //end for
            
            if(reportProperties.getEventTypeOptions().isEmpty()){
                throw new Exception("Unable to find any event types in "+eventSourceIds.size()+" selected event sources.");
            }else{
                
                //get default selected event types
                //Note: although the two for loops are not too efficient these two lists are always short
                List<EventType> defaultSelectedEventTypes = new ArrayList<EventType>();
                DashboardProperties.getInstance().refresh();
                if(defaultEventNames != null){
                    for(String eventName : defaultEventNames){
                        
                        for(EventType eventType : reportProperties.getEventTypeOptions()){
                            
                            if(eventType.getName().equals(eventName)){
                                defaultSelectedEventTypes.add(eventType);
                                break;
                            }
                        }                
                    }
                }else{
                    
                    for(EventType eventType : reportProperties.getEventTypeOptions()){                        
                        defaultSelectedEventTypes.add(eventType);
                    } 
                }
                
                if(!defaultSelectedEventTypes.isEmpty()){
                    reportProperties.setDefaultSelectedEventTypes(defaultSelectedEventTypes);
                }
                
                if(logger.isDebugEnabled()){
                    logger.debug("Created report properties for client - "+reportProperties);
                }
            }
        
        }catch(OutOfMemoryError memoryError){
            logger.error("Ran out of memory while parsing event sources", memoryError);
            throw new Exception("Ran out of memory while parsing the selected event sources, check ERT and GAS log for more details.\r\n You can either increase the memory amount of the Java application or select fewer event sources to merge.", memoryError);
        }catch(Throwable e){
            logger.error("Caught exception while parsing event sources", e);
            throw new Exception("There was an error thrown while parsing the selected event sources, check ERT and GAS log for more details.", e);
        }

        return reportProperties;
    }

    @Override
    public String generateEventReport(final ReportProperties reportProperties) throws Exception {
    	
    	String clientIp = GiftServletUtils.getWebClientAddress(getThreadLocalRequest());
        
        try{	    	  	        	

        	if(!ipToProgressIndicator.isEmpty()){        		
        		logger.warn("An ongoing report generation request was detected by the server. If the client issuing "
        				+ "the request had not yet received its finished report, it is possible that report "
        				+ "may have been corrupted when loading the ERT webpage in a separate session."
        				+ "To prevent this issue, please limit the number of ERT sessions to one per GAS instance.");
        	}
        	
        	final ProgressIndicator progressIndicator = new ProgressIndicator("Initializing...");
        	final ProgressIndicator subtaskProgressIndicator = new ProgressIndicator();
            progressIndicator.setSubtaskProgressIndicator(subtaskProgressIndicator);
        	final GenerateReportStatus progressResult = new GenerateReportStatus(subtaskProgressIndicator);
        	
        	ipToProgressIndicator.put(clientIp, progressResult);
        	
            //
            //Create csv writer with column header information
            //
            if(logger.isInfoEnabled()){
                logger.info("Reading report column headers");
            }
            
            final List<EventReportColumn> reportColumns = reportProperties.getReportColumns();            
            reportColumns.removeAll(Collections.singletonList(null));  //remove any null objects in the collection
            
            if(logger.isInfoEnabled()){
                logger.info("Creating report based on "+reportProperties);
            }
            progressIndicator.setPercentComplete(0);
            progressIndicator.setTaskDescription(CREATING_REPORT_TASK_DESC);
    
            final ReportWriter writer = new ReportWriter(reportProperties, true);          
            writer.setEmptyCellValue(reportProperties.getEmptyCellValue());
            
            //
            // Test writer - attempt to detect some issues early, before waiting on report contents to be gathered
            //
            if(logger.isInfoEnabled()){
                logger.info("Testing writer before attempting to build report which could take a long time...");
            }
            progressIndicator.setTaskDescription(TESTING_WRITER_TASK_DESC);
            try {                
                   
                writer.writeTest();                
                if(logger.isInfoEnabled()){
                    logger.info("Finished testing writer for file " + reportProperties.getFileName());
                }
                
            } catch (Exception e) {
                logger.error("Caught exception while testing writer", e);
                throw new Exception(e);
            }
        
            //
            // Thread the call to allow the calling RPC thread to be released to prevent browser timeout issues
            //
            Thread t = new Thread("Report Generation - "+clientIp){
            
                @Override
                public void run(){
                
                        
                    try{
                        //
                        //Add events for each source to writer
                        //
                        int progressStart = 0;
                        int progressEnd = 50;
                        progressIndicator.setTaskDescription(ADDING_EVENTS_TASK_DESC);
                        progressIndicator.setPercentComplete(progressStart);
                        
                        // used to keep track of attempts for each participant 
                        Map<Integer, ParticipantAttemptCnt> participantToAttemptCnt = new HashMap<>();
                        boolean participantAttemptCounted;
                         
//                         float percentCompletePerEventSourceId = 100/((float)reportProperties.getEventSourceIds().size());
//                         float percentCompletePerEventType = percentCompletePerEventSourceId/reportProperties.getEventTypeOptions().size();

                        int numSources = reportProperties.getEventSourceIds().size();
                        int currentSource = 0;
                        for(Integer eventSourceId : reportProperties.getEventSourceIds()){
                             currentSource++;
                             participantAttemptCounted = false;
                             
                             EventSourceTreeNode eventSourceNode = eventReportServer.getTreeModel().getNode(eventSourceId);
                            
                             progressIndicator.setTaskDescription("Getting events for " + eventSourceNode.getName() + ".");
                             progressIndicator.setPercentComplete(progressStart + (((progressEnd - progressStart)*currentSource)/numSources));
                             if (eventSourceNode != null && !eventSourceNode.isFolder()) {
                                 
                                 File file = eventReportServer.getFileForNode(eventSourceNode.getNodeId());
                                 AbstractEventSourceParser parser = EventSourceUtil.getEventParser(file, reportProperties);
                                 
                                 if(parser == null){
                                     logger.error("Unable to find an event parser for "+eventSourceNode+" file named "+file.getName());
                                     throw new Exception("Unable to find an event parser for "+eventSourceNode+" file named "+file.getName());
                                 }
                                 
                                 //
                                 // Search for participant id, increment attempt counter
                                 //
                                 Integer participantId = parser.getParticipantId();                                 
                                 
                                 // capture lesson started events for elapsed DKF time
                                 List<AbstractEvent> lessonStartedEvents = null;
                                 
                                 // capture lesson completed events for elapsed DKF time
                                 List<AbstractEvent> lessonCompletedEvents = null;
                                 
                                 if(parser instanceof MessageLogEventSourceParser){
                                     
                                     MessageLogEventSourceParser messageParser = (MessageLogEventSourceParser)parser;
                                     EventType lessonStartedEventType = messageParser.getEventTypeByMessageType(MessageTypeEnum.LESSON_STARTED);
                                     if(lessonStartedEventType != null){
                                         lessonStartedEvents = parser.getEventsByType(lessonStartedEventType);                                         
                                     }
                                     
                                     EventType lessonCompletedEventType = messageParser.getEventTypeByMessageType(MessageTypeEnum.LESSON_COMPLETED);
                                     if(lessonCompletedEventType != null){
                                         lessonCompletedEvents = parser.getEventsByType(lessonCompletedEventType);                                         
                                     }
                                 }
                                 
                                 if(logger.isInfoEnabled()){
                                     logger.info("Adding events for node named "+eventSourceNode.getName()+" to writer");
                                 }
                                 
                                 boolean conductMidLessonAssessmentAnalysis = false;
                                 int userId = -1, domainSessionId = -1;

                                 //add the event contents for each event type
                                 for (EventType eventType : reportProperties.getEventTypeOptions()) {
                                     
                                     if(reportProperties.isSelected(eventType)){
                                         
                                         if(logger.isInfoEnabled()){
                                             logger.info("Adding event type of "+eventType+" to report");
                                         }
                 
                                         List<AbstractEvent> events = parser.getEventsByType(eventType);
                                         
                                         if(events != null){
                                             
                                             // capture performance assessment events for time on task analysis
                                             List<PerformanceAssessmentEvent> perfAssEvents = null;
                                             
                                             // capture branch path history events for duration analysis
                                             List<BranchPathHistoryEvent> branchPathHistoryEvents = null;
                                             
                                             for (AbstractEvent event : events) {
                                                 
                                                     if(logger.isInfoEnabled()){
                                                         logger.info("Adding event "+event+" to report");
                                                     }
                                                     
                                                     if(event instanceof DomainSessionEvent){
                                                         DomainSessionEvent dsEvent = ((DomainSessionEvent) event);
                                                         
                                                         if(participantId != null){
                                                             // make sure the participant id column is populated for every domain session event
                                                             // in order to facilitate merge by participant id logic if selected to do so
                                                             dsEvent.setParticipantId(participantId);
                                                             writer.addHeaderColumn(EventReportColumn.PARTICIPANT_ID_COL);
                                                             
                                                             if(!participantAttemptCounted){
                                                                 // make sure participant attempt count is set/incremented for each attempt by the same participant
                                                                 ParticipantAttemptCnt participantAttemptCnt = participantToAttemptCnt.get(participantId);
                                                                 if(participantAttemptCnt == null){
                                                                     participantAttemptCnt = new ParticipantAttemptCnt(dsEvent.getUserId(), dsEvent.getDomainSessionId(), participantId);
                                                                     participantToAttemptCnt.put(participantId, participantAttemptCnt);
                                                                     writer.addHeaderColumn(EventReportColumn.ATTEMPT_COL);
                                                                 }else{
                                                                     participantAttemptCnt.incrementAttemptCnt();
                                                                 }
                                                                 
                                                                 participantAttemptCounted = true;
                                                             }
                                                         }
                                                         
                                                         Double elapsedDKFTime = ReportGenerationUtil.getElapsedDKFTime(lessonStartedEvents, lessonCompletedEvents, dsEvent);
                                                         dsEvent.setElapsedDKFTime(elapsedDKFTime);
                                                     }
                                                     
                                                     if(event instanceof PerformanceAssessmentEvent){
                                                         
                                                         if(perfAssEvents == null){
                                                             perfAssEvents = new ArrayList<>();
                                                         }
                                                         
                                                         perfAssEvents.add((PerformanceAssessmentEvent) event);
                                                         
                                                     } else if(event instanceof BranchPathHistoryEvent){
                                                         
                                                         if(branchPathHistoryEvents == null){
                                                             branchPathHistoryEvents = new ArrayList<>();
                                                         }
                                                         
                                                         branchPathHistoryEvents.add((BranchPathHistoryEvent) event);
                                                         
                                                     } 
                                                     
                                                     writer.addRow(event.toRow());
                                                 } // end for on events of the current event type
                                                 
                                                 //
                                                 // Aggregate analysis over events of interest from report properties and that are in the event source
                                                 //
                                                 if(perfAssEvents != null && !perfAssEvents.isEmpty()){
                                                     // conduct time on task analysis
                                                     PerformanceAssessmentEvent firstEvent = perfAssEvents.get(0);
                                                     userId = firstEvent.getUserId();
                                                     domainSessionId = firstEvent.getDomainSessionId();
                                                     
                                                     conductMidLessonAssessmentAnalysis = true;
                                                             
                                                     TimeOnTaskAggregate timeOnTaskAggregate = new TimeOnTaskAggregate(perfAssEvents, userId, domainSessionId, participantId);
                                                     Double elapsedDKFTime = ReportGenerationUtil.getElapsedDKFTime(lessonStartedEvents, lessonCompletedEvents, timeOnTaskAggregate);
                                                     timeOnTaskAggregate.setElapsedDKFTime(elapsedDKFTime);
                                                     writer.addHeaderColumns(timeOnTaskAggregate.getColumns());
                                                     writer.addRow(timeOnTaskAggregate.toRow());
                                                 }
                                                 
                                                 if(branchPathHistoryEvents != null && !branchPathHistoryEvents.isEmpty()){
                                                     // conduct branch path duration analysis
                                                     BranchPathHistoryEvent firstEvent = branchPathHistoryEvents.get(0);
                                                     userId = firstEvent.getUserId();
                                                     domainSessionId = firstEvent.getDomainSessionId();
                                                     BranchPathHistoryAggregate aggregate = new BranchPathHistoryAggregate(branchPathHistoryEvents, userId, domainSessionId, participantId);
                                                     writer.addHeaderColumns(aggregate.getColumns());
                                                     writer.addRow(aggregate.toRow());
                                                  }
                                             }
                                         }
                                     } // end for loop on events to include in this report
                                 
                                 //
                                 // Aggregate analysis on mid lesson assessments (surveys) and remediation content
                                 //
                                 if(conductMidLessonAssessmentAnalysis){
                                     MidLessonAssessmenRemediationAgreggate midLesson = 
                                             new MidLessonAssessmenRemediationAgreggate(((MessageLogEventSourceParser)parser).getMessageLogReader(), userId, domainSessionId, participantId);
                                     writer.addHeaderColumns(midLesson.getColumns());
                                     writer.addRow(midLesson.toRow());
                                 }                 

                             } else if (eventSourceNode.isFolder()) {
                                 logger.error("Cannot generate the event report: The event source node is a folder.");
                             } else {
                                 logger.error("Found unhandled event source of "+eventSourceNode);
                             }
                        } //end for loop on sources to include in this report
                        
                        //
                        // Aggregate info captured across event sources
                        //
                        for(ParticipantAttemptCnt attemptCnt : participantToAttemptCnt.values()){
                                writer.addRow(attemptCnt.toRow());
                        }
                         
                        progressIndicator.setPercentComplete(progressEnd);
                        
                    }catch(Throwable e){
                        
                        if(e instanceof java.lang.NoClassDefFoundError){
                            System.out.println("A NoClassDefFoundError was thrown while parsing the data file. "+
                                    "You need to add the library that contains the class mentioned in the error to the runtime classpath of the ERT.");
                        }
                        
                        logger.error("Caught exception/error while trying to parse the content in the selected event sources (i.e. files).", e);
                        e.printStackTrace();                     
                        
                        progressResult.setException(new DetailedException(
                        		"A problem ocurred while generating the report.", 
                        		"An error was detected while trying to parse the content in the selected files.", 
                        		e));
                        StringBuilder sb = new StringBuilder();
                        for(StackTraceElement s : e.getStackTrace()){
                        	sb.append("     at ").append(s.toString()).append("\n");
                        }
                        progressResult.setStackTraceMessage(sb.toString());
                        return;
                    }

                    if(logger.isInfoEnabled()){
                        logger.info("Writing event report file");
                    }
                    try {
                        //write all the added event contents to file
                    	writer.setProgressIndicator(progressResult);
                    	String fileCreated = writer.write();
                        if(logger.isInfoEnabled()){
                            logger.info("Finished creating report file: " + fileCreated);           
                        }
                        
                        progressIndicator.setPercentComplete(100);
                        progressResult.setFinished(true);
                        
                        // populate the created file name so it can be displayed in the ERT UI
                        DownloadableFileRef reportResult = new DownloadableFileRef(fileCreated, fileCreated);
                        progressResult.setReportResult(reportResult);

                    } catch (Throwable e) {
                        logger.error("Caught exception/error while writing report", e);
                        e.printStackTrace();
                        
                        progressResult.setException(new DetailedException(
                        		"A problem ocurred while generating the report.", 
                        		"An error was detected while writing to the report file.", 
                        		e));
                        StringBuilder sb = new StringBuilder();
                        for(StackTraceElement s : e.getStackTrace()){
                        	sb.append("     at ").append(s.toString()).append("\n");
                        }
                        progressResult.setStackTraceMessage(sb.toString());
                    }
                }
            };
            
            t.start();
		
        
        } catch(Exception e){
            logger.error("Caught exception while generating report", e);
            ipToProgressIndicator.remove(clientIp);
            throw new Exception(e);
        }

		return reportProperties.getFileName();
    }

    @Override
    public boolean saveReportProperties(String fileName, ReportProperties properties) throws IllegalArgumentException {
        
        try {
            
            ReportWriter.saveReportProperties(new File(EventReportServer.DEFAULT_SETTINGS_ROOT_DIR + File.separator + fileName), properties);
            
            return true;
            
        } catch (Exception ex) {
            
            logger.error("Caught exception while saving report properties", ex);
            
            throw new RuntimeException("Caught exception while saving report properties", ex);
        }
    }

    @Override
    public ReportProperties loadReportProperties(String fileName, ReportProperties properties) throws IllegalArgumentException {
        
        try {
            
            File file = new File(fileName);
            if(file.exists()){
                return ReportWriter.loadReportProperties(file, properties);
            }else{
                return EventReportServer.loadReportProperties(fileName, properties);
            }
            
        } catch (Exception ex) {
            
            logger.error("Caught exception while loading report properties", ex);
            
            throw new RuntimeException("Caught exception while loading report properties", ex);
        }
    }

    @Override
    public List<String> getSettingsList(){
        
        try{
            return Arrays.asList(eventReportServer.findSettingsFiles());
        }catch(Exception e){
            logger.error("Caught exception while trying to retrieve settings list.", e);
            throw new RuntimeException("Caught exception while trying to retrieve settings list.", e);
        }
    }
    
    @Override
    public GenerateReportStatus getProgressIndicator(){
    	return ipToProgressIndicator.get(GiftServletUtils.getWebClientAddress(getThreadLocalRequest()));
    }
    
    @Override
    public Boolean removeProgressIndicator(){
        
    	if(ipToProgressIndicator.remove(GiftServletUtils.getWebClientAddress(getThreadLocalRequest())) != null){
    	    if(logger.isInfoEnabled()){
    	        logger.info("Successfully removed progress indicator for client " + GiftServletUtils.getWebClientAddress(getThreadLocalRequest()));
    	    }
    		return true;
    	}
    	
    	return false;
    }
}
