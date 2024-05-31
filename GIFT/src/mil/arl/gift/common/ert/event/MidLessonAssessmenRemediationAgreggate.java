/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DisplayMidLessonMediaRequest;
import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.TutorUserInterfaceFeedback;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.logger.MessageLogReader;
import mil.arl.gift.common.logger.MessageLogReader.TimedEvent;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyScale;
import mil.arl.gift.common.survey.score.SurveyScaleScore;
import mil.arl.gift.common.survey.score.SurveyScorerManager;
import mil.arl.gift.net.api.SubjectUtil;
import mil.arl.gift.net.api.message.Message;

/**
 * Used to analyze all mid lesson assessment and remediation events from a single source (e.g. domain session message log file) 
 * and provide additional information in an ERT report (e.g. how many times was a mid lesson survey or media presented and for how long).
 * 
 * @author mhoffman
 *
 */
public class MidLessonAssessmenRemediationAgreggate extends DomainSessionEvent {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(MidLessonAssessmenRemediationAgreggate.class);

    /** 
     * used as a delimeter between time values in a cell where the event can happen multiple times 
     * (i.e. a mid lesson survey is presented multiple times because the learner answered incorrectly) 
     */
    private static final String SEMI_COLON = ";";
    
    private static final String ONE = "1";
    
    /** the common name of this event, shown in the event type column */
    private static final String eventName = "Mid-Lesson Assessment Remediation Analysis";
    
    /** common column name strings */
    public static final String PRESENTED_COL_NAME = "_Presented";
    public static final String TIME_COL_NAME = "_Time";
    public static final String TIME_TOTAL_COL_NAME = "_TimeTotal";
    public static final String CONTENT_COL_NAME = "_Content";
    public static final String CONTENT_TOTAL_COL_NAME = "_ContentTotal";
    
    private static final MessageTypeEnum FEEDBACK_MSG_TYPE = MessageTypeEnum.DISPLAY_FEEDBACK_TUTOR_REQUEST;
    private static final MessageTypeEnum REMEDIATION_MEDIA_MSG_TYPE = MessageTypeEnum.DISPLAY_MIDLESSON_MEDIA_TUTOR_REQUEST;
    private static final MessageTypeEnum REMEDIATION_OVER_MSG_TYPE = MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REQUEST;
    private static final MessageTypeEnum DONE_WITH_REMEDIATION_MSG_TYPE = MessageTypeEnum.PROCESSED_ACK;
    
    private static final Set<MessageTypeEnum> REMEDIATION_MSG_TYPES;
    static{
        REMEDIATION_MSG_TYPES = new HashSet<>();
        REMEDIATION_MSG_TYPES.add(FEEDBACK_MSG_TYPE);
        REMEDIATION_MSG_TYPES.add(REMEDIATION_MEDIA_MSG_TYPE);
        REMEDIATION_MSG_TYPES.add(REMEDIATION_OVER_MSG_TYPE);
    } 
    
    /**
     * Enum used to map activities that occur in GIFT (e.g. feedback is shown, a video is presented)
     * 
     * @author mhoffman
     *
     */
    enum ContentTypeEnum{
        FEEDBACK("fb"),
        VIDEO("video"),
        SLIDES("slides"),  //gift slideshow
        UNKNOWN("unknown"); //when the type can't be determined
        
        // declaring private variable for getting values
        private String displayName;
     
        // enum constructor - cannot be public or protected
        private ContentTypeEnum(String displayName){
            this.displayName = displayName;
        }        
        
        public String getDisplayName(){
            return displayName;
        }
    }
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /** mapping of unique column names to the cell with data for this participant */
    private Map<String, Cell> columnNameToCell = new HashMap<>();
    
    /**
     * Analyze the mid lesson events for assessment and remediation info.
     * 
     * @param messageLogReader contains the messages from the source (e.g. domain session message log file)
     * @param userId the user id that caused the assessments
     * @param domainSessionId the session id where the events have taken place.
     * @param participantId participant id from an experiment where the user is anonymous.  Can be null if not in an experiment.
     */
    public MidLessonAssessmenRemediationAgreggate(MessageLogReader messageLogReader, int userId, int domainSessionId, Integer participantId){
        super(eventName, 0, 0, userId, domainSessionId, null);
        
        parseEvents(messageLogReader);
        
        setParticipantId(participantId);
    }
    
    /**
     * Look at the entire set of messages provided by the source for mid lesson assessments and remediation.
     * 
     * @param messageLogReader contains the messages from the source (e.g. domain session message log file)
     */
    private void parseEvents(MessageLogReader messageLogReader){
        
        List<Message> allSubmitSurveyResultsMsgs = messageLogReader.getMessagesByType(MessageTypeEnum.SUBMIT_SURVEY_RESULTS);
        List<Message> lessonStartedMsgs = messageLogReader.getMessagesByType(MessageTypeEnum.LESSON_STARTED);
        List<Message> lessonCompletedMsgs = messageLogReader.getMessagesByType(MessageTypeEnum.LESSON_COMPLETED);
        
        // filter and group survey submissions based on if they happened during a real time assessment
        // start of real time assessment message type: LessonStarted
        // end of real time assessment message type: LessonCompleted
        
        List<List<Message>> realTimeAssessmentSurveys = new ArrayList<>();
        
        if(lessonStartedMsgs == null){
            return;
        }else if(allSubmitSurveyResultsMsgs == null || allSubmitSurveyResultsMsgs.isEmpty()){
            return;
        }
        
        //
        // Filter out submit survey events that happened outside of a real time assessment
        //
        long currRealTimeAssessmentStart, currRealTimeAssessmentEnd;
        int lastSubmitSurveyResultMsgIndex = 0;
        for(int lessonStartedIndex = 0; lessonStartedIndex < lessonStartedMsgs.size(); lessonStartedIndex++){
            
            // the next real time assessment started event
            Message lessonStartedMsg = lessonStartedMsgs.get(lessonStartedIndex);
            currRealTimeAssessmentStart = lessonStartedMsg.getTimeStamp();
            if(lessonCompletedMsgs != null && lessonCompletedMsgs.size() < lessonStartedIndex){
                Message lessonCompletedMsg = lessonCompletedMsgs.get(lessonStartedIndex);
                currRealTimeAssessmentEnd = lessonCompletedMsg.getTimeStamp();
            }else{
                // the lesson completed message never happened, 
                // most likely the domain session ended during the real time assessment
                currRealTimeAssessmentEnd = Long.MAX_VALUE;
            }
            
            // gather the submit survey results that happened between the start of this real time assessment and the end of this
            // real time assessment (or the end of the domain session messages if the real time assessment never ended gracefully)
            List<Message> currentRealTimeAssessmentSurveys = new ArrayList<>();            
            for(;lastSubmitSurveyResultMsgIndex < allSubmitSurveyResultsMsgs.size(); lastSubmitSurveyResultMsgIndex++){
                
                Message submitSurveyResultMsg = allSubmitSurveyResultsMsgs.get(lastSubmitSurveyResultMsgIndex);
                
                if(!submitSurveyResultMsg.getDestinationQueueName().contains(SubjectUtil.UMS_QUEUE_PREFIX)){
                    // only use the submit survey messages that go to the UMS, ignore all duplicate messages (e.g. sent to learner module, LMS module)
                    continue;
                }else if(submitSurveyResultMsg.getTimeStamp() < currRealTimeAssessmentStart){
                    // this submit survey message happened before the real time assessment started, ignore it
                    continue;
                }else if(submitSurveyResultMsg.getTimeStamp() > currRealTimeAssessmentEnd){
                    // this submit survey message happened after the current real time assessment, finished capturing this real time assessment surveys
                    lastSubmitSurveyResultMsgIndex--; // make sure to look at this survey again for the next real time assessment, if there is one
                    break;
                }
                
                currentRealTimeAssessmentSurveys.add(submitSurveyResultMsg);
            }
            
            if(!currentRealTimeAssessmentSurveys.isEmpty()){
                // there was at least 1 mid lesson survey during this real time assessment
                realTimeAssessmentSurveys.add(currentRealTimeAssessmentSurveys);
            }
        }

        
        // for each mid lesson survey during a single real time assessment:
        //  - count attempts
        //  - for each attempt
        //  -- get start time
        //  -- get end time
        //  -- Result of survey {Above / At / Below / Unknown}
        //  -- what happened next {Feedback, Video, Slides}
        //  -- how long did they spend in that mid lesson media
        for(List<Message> submitSurveyResultsMsgs : realTimeAssessmentSurveys){
            
            SubmitSurveyResults submitSurveyResults;
            SurveyResponse surveyResponse;
            for(Message submitSurveyResultMsg : submitSurveyResultsMsgs){
                
                submitSurveyResults = (SubmitSurveyResults) submitSurveyResultMsg.getPayload();
                surveyResponse = submitSurveyResults.getSurveyResponse();
//                if(surveyResponse.getSurveyName().equals(timeOnTaskMgr.getParticipantIdSurveyName())){
//                    //this is the participant id survey, looking for all of the other surveys
//                    continue;
//                }
                
                String surveyName = surveyResponse.getSurveyName();
                Date startTime = surveyResponse.getSurveyStartTime();
                Date endTime = surveyResponse.getSurveyEndTime();
                Duration durationOnSurvey = null;
                if(startTime != null && endTime != null){
                    durationOnSurvey = Duration.between(startTime.toInstant(), endTime.toInstant());
                }
                
                //
                // Calculate the assessment value for this survey based on authored scoring rules in the survey
                //
                AssessmentLevelEnum assessment = AssessmentLevelEnum.UNKNOWN;
                List<ScoreInterface> scores = SurveyScorerManager.getScores(surveyResponse);
                for(ScoreInterface score : scores){
                    
                    if(score instanceof SurveyScaleScore) {
                        
                        List<AbstractScale> scales =((SurveyScaleScore) score).getScales();
                        
                        for(AbstractScale scale : scales) {
                            
                            if(scale instanceof SurveyScale) {
                                
                                SurveyScale surveyScale = (SurveyScale) scale;
                                
                                if(LearnerStateAttributeNameEnum.KNOWLEDGE.equals(surveyScale.getAttribute())
                                        && surveyScale.getValue() instanceof ExpertiseLevelEnum){
                                    
                                    //if the learner's expertise was evaluated, use their expertise level to determine their assessment level
                                    ExpertiseLevelEnum expertise = (ExpertiseLevelEnum) surveyScale.getValue();
                                    
                                    if (expertise.equals(ExpertiseLevelEnum.NOVICE)) {
                                        assessment = AssessmentLevelEnum.BELOW_EXPECTATION;
                                        
                                    } else if (expertise.equals(ExpertiseLevelEnum.JOURNEYMAN)) {
                                        assessment = AssessmentLevelEnum.AT_EXPECTATION;
                                        
                                    } else if (expertise.equals(ExpertiseLevelEnum.EXPERT)) {
                                        assessment = AssessmentLevelEnum.ABOVE_EXPECTATION;
                                    }
                                    
                                    if(assessment != AssessmentLevelEnum.UNKNOWN) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if(assessment != AssessmentLevelEnum.UNKNOWN) {
                        break;
                    }
                }//end for
                
                //
                // Get content types and time on each that occurred for remediation (or feedback for answering the survey correctly)
                //
                int indexOfSubmitSurvey = messageLogReader.getIndexOfMessage(submitSurveyResultMsg);
                long secondsOnContent = 0;
                StringBuilder contentTypeColValue = new StringBuilder();
                if(indexOfSubmitSurvey != -1){
                
                    List<TimedEvent> messages = messageLogReader.getMessagesAfterIndexUntilMessageType(indexOfSubmitSurvey, REMEDIATION_OVER_MSG_TYPE, DONE_WITH_REMEDIATION_MSG_TYPE, REMEDIATION_MSG_TYPES);
                    for(TimedEvent event : messages){
                        
                        Message message = event.getMessage();
                        if(message.getMessageType() == FEEDBACK_MSG_TYPE){
                            
                            TutorUserInterfaceFeedback feedback = (TutorUserInterfaceFeedback) message.getPayload();
                            if(!feedback.onlyContainsClearAction()){
                                appendContentTypeInfo(contentTypeColValue, ContentTypeEnum.FEEDBACK, event.getDurationOfEvent());
                            }
                        }else if(message.getMessageType() == REMEDIATION_MEDIA_MSG_TYPE){
                            
                            DisplayMidLessonMediaRequest midLessonMediaRequest = (DisplayMidLessonMediaRequest) message.getPayload();
                            List<generated.dkf.Media> media = midLessonMediaRequest.getMediaList();
                            if(media.get(0).getMediaTypeProperties() instanceof generated.dkf.SlideShowProperties){
                                appendContentTypeInfo(contentTypeColValue, ContentTypeEnum.SLIDES, event.getDurationOfEvent());
                                if(event.getDurationOfEvent() > 0){
                                    secondsOnContent += event.getDurationOfEvent() / 1000;
                                }
                            }else if(media.get(0).getMediaTypeProperties() instanceof generated.dkf.YoutubeVideoProperties){
                                appendContentTypeInfo(contentTypeColValue, ContentTypeEnum.VIDEO, event.getDurationOfEvent());
                                if(event.getDurationOfEvent() > 0){
                                    secondsOnContent += event.getDurationOfEvent() / 1000;
                                }
                            }else{
                                logger.error("Found unhandled remediation media type in message \n"+message);
                            }
                        }
                    }
                    
                    if(contentTypeColValue.length() == 0){
                        //unable to find any of the messages of interest, not sure what situation this is
                        //outputStatus("Unable to find an after survey response message of interest needed to determine the content type that was presented for survey response of\n"+surveyResponse.getSurveyName(), LogLevel.ERROR, null);
                        appendContentTypeInfo(contentTypeColValue, ContentTypeEnum.UNKNOWN, 0.0);
                    }
                }else{
                    //ERROR - unable to find the survey
                    logger.error("Unable to find the survey response message in the list of domain session messages therefore unable to determine the content type that was presented for survey response of\n"+surveyResponse.getSurveyName());
                    appendContentTypeInfo(contentTypeColValue, ContentTypeEnum.FEEDBACK, 0.0);
                }
                
                List<EventReportColumn> surveyHeaderColumns = addSurveyInfo(surveyName, assessment, durationOnSurvey, contentTypeColValue.toString(), secondsOnContent);
                addNewHeaderColumns(surveyHeaderColumns);

            } //end for on survey results
        } // end for on list of survey results for a real time assessment lesson
    }
    
    /**
     * Add columns to the collection of header columns.  If the column already exist
     * it won't be added.  New columns are added to the end of the list.
     * 
     * @param newHeaderColumns columns to add.
     */
    public void addNewHeaderColumns(List<EventReportColumn> newHeaderColumns){
        
        if(newHeaderColumns != null){
            
            for(EventReportColumn newColumn : newHeaderColumns){
                
                if(!columns.contains(newColumn)){
                    columns.add(newColumn);
                }
            }
        }
    }
    
    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }
    
    /**
     * Build a string that contains content type information and append it to the running string
     * 
     * @param sb the buffer to append new information too
     * @param contentTypeEnum the enumerated type of a piece of content or activity that was presented to the learner
     * @param durationMs how long the content was visible to the learner
     */
    private void appendContentTypeInfo(StringBuilder sb, ContentTypeEnum contentTypeEnum, double durationMs){
        
        if(sb.length() != 0){
            sb.append(Constants.COMMA);
        }
        
        sb.append(contentTypeEnum.getDisplayName()).append(Constants.OPEN_PARENTHESIS).append(durationMs / 1000.0).append(Constants.CLOSE_PARENTHESIS);
    }
    
    /**
     * Converts the duration value into a string.
     * 
     * @param seconds contains an amount of time in seconds to convert into a string.
     * @return the string representation
     */
    private String formatDuration(long seconds){
        return String.valueOf(seconds);
    }
    
    /**
     * Converts the string duration value into seconds.
     * 
     * @param durationStr a string formatted in the formatDuration method
     * @return the amount of seconds the duration string represents
     */
    private long getDuration(String durationStr){
        return Long.valueOf(durationStr);
    }
    
    /**
     * Set a survey assessment value for a particular survey in an experiment.
     * 
     * @param colName the name of the assessment column for a survey in an experiment
     * @param assessment the assessment value to use.  Can't be null.
     */
    private void setAssessmentCell(String colName, AssessmentLevelEnum assessment){
        
        if(assessment == null){
            throw new IllegalArgumentException("The assessment can't be null.");
        }
                    
        Cell currCell = columnNameToCell.get(colName);
        
        Cell newCell = new Cell(assessment.getName(), currCell.getColumn());
        cells.set(cells.indexOf(currCell), newCell);
        
        columnNameToCell.put(newCell.getColumn().getColumnName(), newCell);            
    }
    
    /**
     * Increment the number of times the survey was presented to the participant across all attempts
     * for this experiment.
     * 
     * @param colName the survey attempts column for a specific survey in this experiment
     */
    private void incrementTimesPresentedCnt(String colName){
        
        Cell currCell = columnNameToCell.get(colName);
        String currValStr = currCell.getValue();
        Integer currVal = Integer.valueOf(currValStr);
                    
        Cell newCell = new Cell(String.valueOf(currVal + 1), currCell.getColumn());
        
        cells.set(cells.indexOf(currCell), newCell);
        
        columnNameToCell.put(newCell.getColumn().getColumnName(), newCell);            
    }
    
    /**
     * Concatenate the survey duration for a single survey for this participant in an experiment
     * to the cell containing previous delimited duration values.
     *  
     * @param colName the duration column name for a specific survey this participant answered in an experiment
     * @param durationOnSurvey the time the survey was shown to the learner until answering.
     * @param totalColName the duration total column name for a specific survey this participant answered in an experiment  (the total across all attempts)
     */
    private void addToSurveyDurationCell(String colName, Duration durationOnSurvey, String totalColName){
        
        //get the previous cell value and concatenate new value to it
        Cell currCell = columnNameToCell.get(colName);
        String currValue = currCell.getValue();
        currValue += SEMI_COLON + formatDuration(durationOnSurvey.getSeconds());
        
        Cell newCell = new Cell(currValue, currCell.getColumn());
        
        cells.set(cells.indexOf(currCell), newCell);
        
        columnNameToCell.put(newCell.getColumn().getColumnName(), newCell);  
        
        //handle the total time across all attempts on this survey cell
        Cell totalCurrCell = columnNameToCell.get(totalColName);
        long totalCurrValue = getDuration(totalCurrCell.getValue());
        totalCurrValue += durationOnSurvey.getSeconds();
        
        Cell newTotalCell = new Cell(formatDuration(totalCurrValue), totalCurrCell.getColumn());
        
        cells.set(cells.indexOf(totalCurrCell), newTotalCell);
        
        columnNameToCell.put(newTotalCell.getColumn().getColumnName(), newTotalCell); 
    }
    
    /**
     * Concatenate the content type set for the content that was shown after a survey response to
     * the cell containing previous delimited content type set values.
     * 
     * @param colName the content column name for a specific survey this participant answered in an experiment
     * @param contentTypeColValue contains information about the content shown to the subject after the survey
     * @param totalColName the content total time column name for a specific survey this participant answered in an experiment (the total across all attempts)
     * @param secondsOnContent amount of seconds spent on content after a survey for a single survey submission.
     */
    private void addToContentTypeCell(String colName, String contentTypeColValue, String totalColName, long secondsOnContent){
        
        //get the previous cell value and concatenate new value to it
        Cell currCell = columnNameToCell.get(colName);
        String currValue = currCell.getValue();
        currValue += SEMI_COLON + contentTypeColValue;
        
        Cell newCell = new Cell(currValue, currCell.getColumn());
        
        cells.set(cells.indexOf(currCell), newCell);
        
        columnNameToCell.put(newCell.getColumn().getColumnName(), newCell); 
        
        //handle the total time on content cell and its single value
        Cell totalCurrCell = columnNameToCell.get(totalColName);
        long totalCurrValue = getDuration(totalCurrCell.getValue());
        totalCurrValue += secondsOnContent;
        
        Cell newTotalCell = new Cell(formatDuration(totalCurrValue), totalCurrCell.getColumn());
        
        cells.set(cells.indexOf(totalCurrCell), newTotalCell);
        
        columnNameToCell.put(newTotalCell.getColumn().getColumnName(), newTotalCell); 
    }
    
    /**
     * Adds information about a survey response event for a participant in an experiment.
     * This could be another attempt at this survey and therefore data should be appended/updated
     * from the information about previous attempts.
     * 
     * @param surveyName the name of the survey that the learner replied too
     * @param assessment the assessment of the learner's responses in this survey
     * @param durationOnSurvey the amount of time it took from the survey being presented to the survey being answered.
     * @param contentTypeColValue information about the gift content that was presented after the survey.</br>
     * "fb(3.343)", "fb(92.185),slides(86.951),slides(48.702)"
     * @param secondsOnContent amount of seconds spent on content after a survey for a single survey submission.
     * @return collection of new columns that were added because this survey attempt was the first for this participant, 
     * for this experiment, for this survey.  Null will be returned if no new columns were created.
     */
    public List<EventReportColumn> addSurveyInfo(String surveyName, AssessmentLevelEnum assessment, Duration durationOnSurvey, String contentTypeColValue, long secondsOnContent){
        
        List<EventReportColumn> newColumnNames = null;
        
        String surveyNameColName = surveyName;
        String numTimesPresentedColName = surveyName + PRESENTED_COL_NAME;
        String durationPresentedColName = surveyName + TIME_COL_NAME;
        String durationPresentedTotalColName = surveyName + TIME_TOTAL_COL_NAME;
        String contentPresentedColName = surveyName + CONTENT_COL_NAME;
        String contentPresentedTotalColName = surveyName + CONTENT_TOTAL_COL_NAME;
        
        // determine if this is the first attempt or not
        Cell surveyNameCell = columnNameToCell.get(surveyNameColName);
        if(surveyNameCell == null){
            
            //this is the first attempt for this participant in this experiment, create new cells    
            
//            surveyAssessmentColNames.add(surveyNameColName);
            
            EventReportColumn surveyNameCol = new EventReportColumn(surveyNameColName, surveyNameColName);
            surveyNameCell = new Cell(assessment.getName(), surveyNameCol);
            
            EventReportColumn numTimesPresentedCol = new EventReportColumn(numTimesPresentedColName, numTimesPresentedColName);
            Cell numTimesPresentedCell = new Cell(ONE, numTimesPresentedCol);
            
            EventReportColumn durationPresentedCol = new EventReportColumn(durationPresentedColName, durationPresentedColName);
            Cell durationPresentedCell = new Cell(formatDuration(durationOnSurvey.getSeconds()), durationPresentedCol);
            
            EventReportColumn durationPresentedTotalCol = new EventReportColumn(durationPresentedTotalColName, durationPresentedTotalColName);
            Cell durationPresentedTotalCell = new Cell(formatDuration(durationOnSurvey.getSeconds()), durationPresentedTotalCol);
            
            EventReportColumn contentPresentedCol = new EventReportColumn(contentPresentedColName, contentPresentedColName);
            Cell contentPresentedCell = new Cell(contentTypeColValue, contentPresentedCol);
            
            EventReportColumn contentPresentedTotalCol = new EventReportColumn(contentPresentedTotalColName, contentPresentedTotalColName);
            Cell contentPresentedTotalCell = new Cell(formatDuration(secondsOnContent), contentPresentedTotalCol);
            
            columnNameToCell.put(surveyNameCell.getColumn().getColumnName(), surveyNameCell);
            columnNameToCell.put(numTimesPresentedCell.getColumn().getColumnName(), numTimesPresentedCell);
            columnNameToCell.put(durationPresentedCell.getColumn().getColumnName(), durationPresentedCell);
            columnNameToCell.put(durationPresentedTotalCell.getColumn().getColumnName(), durationPresentedTotalCell);
            columnNameToCell.put(contentPresentedCell.getColumn().getColumnName(), contentPresentedCell);
            columnNameToCell.put(contentPresentedTotalCell.getColumn().getColumnName(), contentPresentedTotalCell);
            
            cells.add(surveyNameCell);
            cells.add(numTimesPresentedCell);
            cells.add(durationPresentedCell);
            cells.add(durationPresentedTotalCell);
            cells.add(contentPresentedCell);
            cells.add(contentPresentedTotalCell);
            
            newColumnNames = new ArrayList<>();
            newColumnNames.add(surveyNameCol);
            newColumnNames.add(numTimesPresentedCol);
            newColumnNames.add(durationPresentedCol);
            newColumnNames.add(durationPresentedTotalCol);
            newColumnNames.add(contentPresentedCol);
            newColumnNames.add(contentPresentedTotalCol);
        }else{
            //update existing cell values
            
            setAssessmentCell(surveyNameColName, assessment);
            addToSurveyDurationCell(durationPresentedColName, durationOnSurvey, durationPresentedTotalColName);
            incrementTimesPresentedCnt(numTimesPresentedColName);
            addToContentTypeCell(contentPresentedColName, contentTypeColValue, contentPresentedTotalColName, secondsOnContent);
        }
        
        return newColumnNames;
        
    }
}
