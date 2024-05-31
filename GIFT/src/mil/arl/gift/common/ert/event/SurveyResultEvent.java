/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.SubmitSurveyResults;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.ReportProperties;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.FreeResponseReplyWeights;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SliderSurveyQuestion;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyResponse;
import mil.arl.gift.common.survey.score.AbstractScale;
import mil.arl.gift.common.survey.score.ScoreInterface;
import mil.arl.gift.common.survey.score.SurveyAnswerScore;
import mil.arl.gift.common.survey.score.SurveyConceptAssessmentScore;
import mil.arl.gift.common.survey.score.SurveyFeedbackScorer;
import mil.arl.gift.common.survey.score.SurveyScaleScore;
import mil.arl.gift.common.survey.score.SurveyScorerManager;

/**
 * This class represents a survey result state event that can be included in an ERT report.  It has the logic to
 * convert a survey results object into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class SurveyResultEvent extends DomainSessionEvent {
    
    /** collection of columns specific to this event */
    private Map<String, EventReportColumn> columns = new HashMap<String, EventReportColumn>();
    
    private static final String UNDERSCORE = "_";
    private static final String COLON = ":";
    private static final String SEMI_COLON = ";";
    private static final String OPEN_PARENTHESIS = "(";
    private static final String CLOSE_PARENTHESIS = ")";
    private static final String SURVEY_TOKEN = "S";
    private static final String QUESTION_TOKEN = "Q";
    private static final String SQUESTION_TOKEN = "SQ";
    private static final String SCORE_TOKEN = "Score";
    private static final String CORRECT_TOKEN = "_Correct";

    private static final String SUMMARIZE_PASSAGE_COL_NAME = "Learner Summarize Passage";
    private static final String HIGHLIGHT_PASSAGE_COL_NAME = "Learner Highlight Passage";
    private static final String UNKNOWN_RATING_SCALE_COL_NAME = "unknown rating scale question";
    
    private static final String CHOICE_INDEX = "_choice_index";
    private static final String BEST_ANSWER = "_BEST_ANSWER";
    
    /** highlight passage search and extract strings */
    private static final String highlightStart = "<span style=\"background-color: yellow;\">";
    private static final String spanEnd = "</span>";
    private static final String delim = "...";
    
    /** whether to use question text for the header of a survey response column */
    private boolean useQuestionText = ReportProperties.DEFAULT_USE_QUESTION_TEXT_FOR_HEADER;

    /**
     * parse the submit survey results event object and tag with @link {@link MessageTypeEnum.SUBMIT_SURVEY_RESULTS}.
     * This message is usually sent from domain to ums module when a survey is answered.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param results - the survey results for this event
     * @param useQuestionText - whether to use question text for the header of a survey response column 
     */
    public SurveyResultEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, SubmitSurveyResults results, boolean useQuestionText) {
        super(MessageTypeEnum.SUBMIT_SURVEY_RESULTS.getDisplayName(), time, domainSessionMessageEntry, null);
        
        this.useQuestionText = useQuestionText;
        parseEvent(results.getSurveyResponse(), results.getGiftKey());
    }
    
    /**
     * parse the question response event object and tag with @link {@link MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE}.
     * This message is usually sent from tutor to domain module every time a learner answers a question. 
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param questionResponse - a single question response
     * @param useQuestionText - whether to use question text for the header of a survey response column 
     */
    public SurveyResultEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, AbstractQuestionResponse questionResponse, boolean useQuestionText){
        super(MessageTypeEnum.TUTOR_SURVEY_QUESTION_RESPONSE.getDisplayName(), time, domainSessionMessageEntry, null);
        
        this.useQuestionText = useQuestionText;
        parseEvent(questionResponse);
    }
    
    /**
     * parse the display survey tutor reply event object and tag with @link {@link MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY}.
     * This message is usually sent from tutor to domain module when a survey is answered.  This is needed to parse
     * survey questions that aren't stored in the UMS survey db (e.g. highlight/summarize passage)
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param surveyResponse - the survey results for this event
     * @param useQuestionText - whether to use question text for the header of a survey response column 
     */
    public SurveyResultEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, SurveyResponse surveyResponse, boolean useQuestionText) {
        super(MessageTypeEnum.DISPLAY_SURVEY_TUTOR_REPLY.getDisplayName(), time, domainSessionMessageEntry, null);
        
        this.useQuestionText = useQuestionText;
        parseEvent(surveyResponse, null);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return new ArrayList<EventReportColumn>(columns.values());
    }
    
    /**
     * Return the column label for a survey.
     * 
     * @param surveyResponse - the survey responses being parsed 
     * @param giftKey the survey context gift key that points to the survey whose response is being extracted here.  Can 
     * be null.  If null the key won't be used when auto-generating question response column names.
     * @return the column label to use when referencing a survey
     */
    private String getSurveyColName(SurveyResponse surveyResponse, String giftKey){
        
        if(useQuestionText){
            return surveyResponse.getSurveyName();
        }else if(giftKey != null && giftKey.length() < 25){  //just some arbitrary length to make sure not too much text is in column label
            return SURVEY_TOKEN+UNDERSCORE+giftKey;
        }else{
            // fall back solution, use the id.  This is not ideal because the id might not be visible in any authoring UI for the user.
            return SURVEY_TOKEN+UNDERSCORE+surveyResponse.getSurveyId();
        }
    }
    
    /**
     * Return the column label for a survey question.<br/>
     * Precedence:
     * 1. Question tag (if not empty)
     * 2. Question text (if enabled)
     * 3. combination of survey question id and question id
     * 
     * @param question - the question being responded to
     * @return the column label to use when referencing a question
     */
    private String getQuestionColName(AbstractSurveyQuestion<? extends AbstractQuestion> question){
        
        if(question.getTag() != null && question.getTag().length() > 0){
            //the tag is the beginning of the survey question column name
            return QUESTION_TOKEN+question.getQuestion().getQuestionId();
        }else if(useQuestionText){
            // use the question text + some other identifiable id for the survey question
            return question.getQuestion().getText() + UNDERSCORE + Constants.OPEN_PARENTHESIS + SQUESTION_TOKEN+UNDERSCORE+question.getId()+UNDERSCORE+QUESTION_TOKEN+question.getQuestion().getQuestionId() + Constants.CLOSE_PARENTHESIS;
        }else{
            //include some other identifiable id for the survey question
            return SQUESTION_TOKEN+UNDERSCORE+question.getId()+UNDERSCORE+QUESTION_TOKEN+question.getQuestion().getQuestionId();
        }
    }
    
    /**
     * Return the column label for a survey score.
     * 
     * @param surveyResponse - the survey responses being parsed 
     * @param giftKey the survey context gift key that points to the survey whose response is being extracted here.  Can 
     * be null.  If null the key won't be used when auto-generating question response column names.
     * @return the column label
     */
    private String getScoreColName(SurveyResponse surveyResponse, String giftKey){
        return getSurveyColName(surveyResponse, giftKey) + UNDERSCORE+ SCORE_TOKEN;
    }
    
    /**
     * Return the column label for a survey question.  This doesn't include the survey identifier because the question
     * provided is not part of a formal survey.
     * 
     * @param questionResponse - the survey results containing the questions
     * @param question - the question whose answer will be in the column
     * @return the column label to use when referencing a survey question
     */
    private String getSurveyQuestionColName(AbstractQuestionResponse questionResponse, AbstractSurveyQuestion<? extends AbstractQuestion> question){
        
        String name = Constants.EMPTY;
        if(question.getTag() != null && question.getTag().length() > 0){
            //place tag first
            name += question.getTag() + UNDERSCORE;
        }
        
        name += getQuestionColName(question);
        
        return name;
    }
    
    /**
     * Return the column label for a survey question.
     * 
     * @param surveyResponse - the survey responses being parsed 
     * @param giftKey the survey context gift key that points to the survey whose response is being extracted here.  Can 
     * be null.  If null the key won't be used when auto-generating question response column names.
     * @param question - the question whose answer will be in the column
     * @return the column label to use when referencing a survey question
     */
    private String getSurveyQuestionColName(SurveyResponse surveyResponse, String giftKey, AbstractSurveyQuestion<? extends AbstractQuestion> question){
        
        String name = Constants.EMPTY;
        if(question.getTag() != null && question.getTag().length() > 0){
            //place tag first
            name += question.getTag() + UNDERSCORE;
        }
        
        name += getSurveyColName(surveyResponse, giftKey) + UNDERSCORE + getQuestionColName(question);
        
        return name;
    }
    
    /**
     * Return the column label for a matrix of choices question choice.
     * 
     * @param surveyResponse - the survey responses being parsed 
     * @param giftKey the survey context gift key that points to the survey whose response is being extracted here.  Can 
     * be null.  If null the key won't be used when auto-generating question response column names.
     * @param question - the question whose answer will be in the column
     * @param row - a row option in the matrix of choices
     * @param col - a column option in the matrix of choices
     * @return the column label to use when referencing a question
     */
    private String getMatrixColName(SurveyResponse surveyResponse, String giftKey, AbstractSurveyQuestion<? extends AbstractQuestion> question, ListOption row, ListOption col){
        return getMatrixColName(surveyResponse, giftKey, question, row.getText(),col.getText());
    }
    
    /**
     * Return the column label for a matrix of choices question choice.
     * 
     * @param questionResponse - the response to a question
     * @param question - the question whose answer will be in the column
     * @param rowText - the text of a row option in the matrix of choices
     * @param colText - the text of a column option in the matrix of choices
     * @return the column label to use when referencing a question
     */
    private String getMatrixColName(AbstractQuestionResponse questionResponse,  AbstractSurveyQuestion<? extends AbstractQuestion> question, String rowText, String colText){
        return getSurveyQuestionColName(questionResponse, question) + UNDERSCORE + "R"+UNDERSCORE+rowText+ UNDERSCORE +"C"+UNDERSCORE+colText;
    }
    
    /**
     * Return the column label for a matrix of choices question choice.
     * 
     * @param surveyResponse - the survey responses being parsed 
     * @param giftKey the survey context gift key that points to the survey whose response is being extracted here.  Can 
     * be null.  If null the key won't be used when auto-generating question response column names.
     * @param question - the question whose answer will be in the column
     * @param rowText - the text of a row option in the matrix of choices
     * @param colText - the text of a column option in the matrix of choices
     * @return String - the column label to use when referencing a question
     */
    private String getMatrixColName(SurveyResponse surveyResponse, String giftKey, AbstractSurveyQuestion<? extends AbstractQuestion> question, String rowText, String colText){
        return getSurveyQuestionColName(surveyResponse, giftKey, question) + UNDERSCORE + "R"+UNDERSCORE+rowText+ UNDERSCORE +"C"+UNDERSCORE+colText;
    }
    
    /**
     * Return the row label for a matrix of choices question choice.
     * 
     * @param surveyResponse - the survey responses being parsed 
     * @param giftKey the survey context gift key that points to the survey whose response is being extracted here.  Can 
     * be null.  If null the key won't be used when auto-generating question response column names.
     * @param question - the question whose answer will be in the column
     * @param rowText - the text of a row option in the matrix of choices
     * @return String - the row label to use when referencing a question
     */
    private String getMatrixRowName(SurveyResponse surveyResponse, String giftKey, AbstractSurveyQuestion<? extends AbstractQuestion> question, String rowText){
        return getSurveyQuestionColName(surveyResponse, giftKey, question) + UNDERSCORE + "R"+UNDERSCORE+rowText;
    }
    
    /**
     * Create and add a cell to the pre-existing column with the given name.
     * 
     * @param colName - the unique name of the column to create a new cell for
     * @param value - the value to place in the cell of the given column
     */
    private void addCellForColumn(String colName, String value){
        
        EventReportColumn column = columns.get(colName);
        if(column == null){
            throw new IllegalArgumentException("Unable to find the column named "+colName+", therefore the value of "+value+" will not be saved.");
        }else{
            cells.add(new Cell(String.valueOf(value), column));
        }
    }
    
    /**
     * Place the single question's response in a column.
     * 
     * @param questionResponse contains a single question response
     */
    private void parseEvent(AbstractQuestionResponse questionResponse){
        
        /**
         * Format:
         *  (where cn = "column name", row is the value in the row for the column (i.e. cell), prefix = "S<id>_Q<id>")
         * 
         * Free Response:  cn = prefix (will be optional 'tag' property value if specified), row = text
         * Multiple Choice: cn = prefix (will be optional 'tag' property value if specified), row = delimited choice(s) text
         * Rating Scale: cn = prefix (will be optional 'tag' property value if specified), row = choice text 
         * Matrix of Choices: cn = prefix + "_R<text>_C<text>" (will be optional 'tag' property value if specified), row = is selected boolean
         * Slider: cn = prefix (will be optional 'tag' property value if specified), row = text or value 
         */
        String colName = null;
                
        AbstractSurveyQuestion<? extends AbstractQuestion> sQuestion = questionResponse.getSurveyQuestion();                
        
        if (questionResponse.getSurveyQuestion() instanceof FillInTheBlankSurveyQuestion) {

            colName = getSurveyQuestionColName(questionResponse, sQuestion);
            EventReportColumn column = new EventReportColumn(colName, colName);
            columns.put(colName, column);

            if (!questionResponse.getResponses().isEmpty()) {
                
                StringBuilder values = new StringBuilder();
                
                boolean multipleResponses = questionResponse.getResponses().size() > 1;
                for (QuestionResponseElement element : questionResponse.getResponses()) {
                    if (element.getText() != null) {
                        values.append(element.getText());
                    }

                    if (multipleResponses) {
                        values.append(SEMI_COLON);
                    }
                }

                addCellForColumn(colName, values.toString());

            } else {
                // This should never be reached because only questions with
                // response elements should have question responses. Essentially
                // this means there is no response for the question, ignore it
            }

        } else if (questionResponse.getSurveyQuestion() instanceof MultipleChoiceSurveyQuestion) {

            colName = getSurveyQuestionColName(questionResponse, sQuestion);
            EventReportColumn column = new EventReportColumn(colName, colName);
            columns.put(colName, column);

            if (!questionResponse.getResponses().isEmpty()) {
                
                StringBuilder values = new StringBuilder();
                                
                boolean multipleResponses = questionResponse.getResponses().size() > 1;
                for (QuestionResponseElement element : questionResponse.getResponses()) {
                    if (element.getText() != null) {
                        values.append(element.getText());
                    }

                    if (multipleResponses) {
                        values.append(SEMI_COLON);
                    }
                }
                
                addCellForColumn(colName, values.toString());

            } else {
                // This should never be reached because only questions with
                // response elements should have question responses. Essentially
                // this means there is no response for the question, ignore it
            }

        } else if (questionResponse.getSurveyQuestion() instanceof RatingScaleSurveyQuestion) {

            colName = getSurveyQuestionColName(questionResponse, sQuestion);
            EventReportColumn column = new EventReportColumn(colName, colName);
            columns.put(colName, column);

            if (!questionResponse.getResponses().isEmpty()) {

                if (questionResponse.getResponses().size() > 1) {
                    // We shouldn't have more than one element for a rating scale
                    // question, this is unusual but we'll allow it and
                    // use the first response element as the answer
                }

                QuestionResponseElement element = questionResponse.getResponses().get(0);

                if (element.getText() != null) {

                    String value = element.getText();
                    addCellForColumn(colName, value);
                    
                } else {
                    // This is weird
                }

            } else {
                // This should never be reached because only questions with
                // response elements should have question responses. Essentially
                // this means there is no response for the question, ignore it
            }

        } else if (questionResponse.getSurveyQuestion() instanceof MatrixOfChoicesSurveyQuestion) {
                                
            if (!questionResponse.getResponses().isEmpty()) {

                for (QuestionResponseElement element : questionResponse.getResponses()) {
                    if (element.getText() != null) {
                        // This is the header the response is mapped to
                        colName = getMatrixColName(questionResponse, sQuestion, element.getRowChoice().getText(), element.getText());
                        EventReportColumn column = new EventReportColumn(colName, colName);
                        columns.put(colName, column);
                        String value = "1";
                        addCellForColumn(colName, value);
                    }
                }

            } else {
                // This should never be reached because only questions with
                // response elements should have question responses. Essentially
                // this means there is no response for the question, ignore it
            }

        } else if (questionResponse.getSurveyQuestion() instanceof SliderSurveyQuestion) {

            colName = getSurveyQuestionColName(questionResponse, sQuestion);
            EventReportColumn column = new EventReportColumn(colName, colName);
            columns.put(colName, column);

            if (!questionResponse.getResponses().isEmpty()) {

                if (questionResponse.getResponses().size() > 1) {
                    // We shouldn't have more than one element for a slider
                    // question, this is unusual but we'll allow it and
                    // use the first response element as the answer
                }

                QuestionResponseElement element = questionResponse.getResponses().get(0);

                if (element.getText() != null) {

                    String value = element.getText();
                    addCellForColumn(colName, value);

                } else {
                    // This is weird
                }

            } else {
                // This should never be reached because only questions with
                // response elements should have question responses. Essentially
                // this means there is no response for the question, ignore it
            }
        } else{
            //ERROR
            throw new IllegalArgumentException("Found unhandled question type of "+sQuestion);
        }
    }
    
    /**
     * Gather information on the columns and cell content for the content of the survey results
     * 
     * @param sResponse contains survey responses to extract into columns/cells of the report being created
     * @param giftKey the survey context gift key that points to the survey whose response is being extracted here.  Can 
     * be null.  If null the key won't be used when auto-generating question response column names.
     * @throws Exception 
     */
    private void parseEvent(SurveyResponse sResponse, String giftKey){
        
        /**
         * Format:
         *  (where cn = "column name", row is the value in the row for the column (i.e. cell), prefix = "S<id>_Q<id>")
         * 
         * Free Response:  cn = prefix (will be optional 'tag' property value if specified), row = text
         * Multiple Choice: cn = prefix (will be optional 'tag' property value if specified), row = delimited choice(s) text
         * Rating Scale: cn = prefix (will be optional 'tag' property value if specified), row = choice text 
         * Matrix of Choices: cn = prefix + "_R<text>_C<text>" (will be optional 'tag' property value if specified), row = is selected boolean
         * Slider: cn = prefix (will be optional 'tag' property value if specified), row = text or value 
         */
        String colName = null;
        
        //
        //First parse the survey to build the column names (because questions may not be answered)
        //
        
        for(SurveyPageResponse page : sResponse.getSurveyPageResponses()){
            
            for(AbstractSurveyElement element : page.getSurveyPage().getElements()){
                
                if(element instanceof AbstractSurveyQuestion){
                    
                    AbstractSurveyQuestion<? extends AbstractQuestion> sQuestion =(AbstractSurveyQuestion<?>)element;
                    
                    if(sQuestion instanceof FillInTheBlankSurveyQuestion){
                        
                        //check if highlight/summarize passage fill in the blank to parse differently
                        FillInTheBlankSurveyQuestion fillInTheBlankSQuestion = (FillInTheBlankSurveyQuestion)sQuestion;
                        
                        Boolean isRemediationContentSQProp = fillInTheBlankSQuestion.getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT);
                        Boolean isRemediationContentQProp = fillInTheBlankSQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT);
                        boolean isHighlightSummarize = (isRemediationContentSQProp != null && isRemediationContentSQProp) || (isRemediationContentQProp != null && isRemediationContentQProp);
                        if(isHighlightSummarize){
                            
                            Boolean isFieldTextboxSQProp = fillInTheBlankSQuestion.getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY);
                            Boolean isFieldTextboxQProp = fillInTheBlankSQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY);
                            boolean isSummarize = (isFieldTextboxSQProp != null && isFieldTextboxSQProp) || (isFieldTextboxQProp != null && isFieldTextboxQProp);
                            if(isSummarize){
                                
                                //Note: currently if a survey has a summarize or highlight passage it is the only survey question in the survey,
                                //      therefore we can use a static column name here.
                                colName = SUMMARIZE_PASSAGE_COL_NAME; 

                            }else{
                                //highlight passage
                                
                                //Note: currently if a survey has a summarize or highlight passage it is the only survey question in the survey,
                                //      therefore we can use a static column name here.
                                colName = HIGHLIGHT_PASSAGE_COL_NAME;
                            }
                        }else{
                            colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion); 
                        }
                        
                        EventReportColumn column = new EventReportColumn(colName, colName);
                        columns.put(colName, column);
                        
                        if(sQuestion.getHighestPossibleScore() > 0) {
                            String bestColName = colName + BEST_ANSWER;
                            EventReportColumn bestColumn = new EventReportColumn(bestColName, bestColName);
                            columns.put(bestColName, bestColumn);
                        }
                        
                    } else if (sQuestion instanceof MultipleChoiceSurveyQuestion) {

                        colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion); 
                        EventReportColumn column = new EventReportColumn(colName, colName);
                        columns.put(colName, column);
                        
                        //
                        // add column for question choice index
                        //
                        String choiceIndexColName = colName;                        
                        choiceIndexColName += CHOICE_INDEX;
                        EventReportColumn choiceIndexCol = new EventReportColumn(choiceIndexColName, choiceIndexColName);
                        columns.put(choiceIndexColName, choiceIndexCol);
                        
                        if(sQuestion.getHighestPossibleScore() > 0) {
                            String bestColName = colName + BEST_ANSWER;
                            EventReportColumn bestColumn = new EventReportColumn(bestColName, bestColName);
                            columns.put(bestColName, bestColumn);
                        }
                        
                    } else if (sQuestion instanceof RatingScaleSurveyQuestion) {

                        if(sResponse.getSurveyId() == 0 && sQuestion.getId() == 0){
                            
                            colName = UNKNOWN_RATING_SCALE_COL_NAME;
                            EventReportColumn column = new EventReportColumn(colName, colName);
                            columns.put(colName, column);

                        }else{
                            colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion);
                            EventReportColumn column = new EventReportColumn(colName, colName);
                            columns.put(colName, column);
                        }
                        
                        //
                        // add column for question choice index
                        //
                        String choiceIndexColName = colName;                        
                        choiceIndexColName += CHOICE_INDEX;
                        EventReportColumn choiceIndexCol = new EventReportColumn(choiceIndexColName, choiceIndexColName);
                        columns.put(choiceIndexColName, choiceIndexCol);
                        
                        if(sQuestion.getHighestPossibleScore() > 0) {
                            String bestColName = colName + BEST_ANSWER;
                            EventReportColumn bestColumn = new EventReportColumn(bestColName, bestColName);
                            columns.put(bestColName, bestColumn);
                        }

                    } else if (sQuestion instanceof MatrixOfChoicesSurveyQuestion) {
                        
                        MatrixOfChoicesSurveyQuestion surveyQuestion = (MatrixOfChoicesSurveyQuestion) sQuestion;
                        
                        //This gets the list of all columns for a matrix of choices question
                        for(ListOption columnOption : surveyQuestion.getQuestion().getColumnOptions().getListOptions()) {
                            
                            for(ListOption rowOption : surveyQuestion.getQuestion().getRowOptions().getListOptions()) {
                                
                                colName = getMatrixColName(sResponse, giftKey, sQuestion, rowOption, columnOption); 
                                EventReportColumn column = new EventReportColumn(colName, colName);
                                columns.put(colName, column);
                                
                                String rowName = getMatrixRowName(sResponse, giftKey, sQuestion, rowOption.getText());
                                String bestRowName = rowName + BEST_ANSWER;
                                if(!columns.containsKey(rowName) && sQuestion.getHighestPossibleScore() > 0) {
                                    if(sQuestion.getHighestPossibleScore() > 0) {
                                        EventReportColumn bestRow = new EventReportColumn(bestRowName, bestRowName);
                                        columns.put(bestRowName, bestRow);
                                    }
                                }
                                
                            }
                        }
                        
                        if(sQuestion.getHighestPossibleScore() > 0) {
                            String bestColName = sQuestion.getQuestion().getText() + BEST_ANSWER;
                            EventReportColumn bestColumn = new EventReportColumn(bestColName, bestColName);
                            columns.put(bestColName, bestColumn);
                        }

                    } else if (sQuestion instanceof SliderSurveyQuestion) {

                        colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion);
                        EventReportColumn column = new EventReportColumn(colName, colName);
                        columns.put(colName, column);
                        
                        if(sQuestion.getHighestPossibleScore() > 0) {
                            String bestColName = colName + BEST_ANSWER;
                            EventReportColumn bestColumn = new EventReportColumn(bestColName, bestColName);
                            columns.put(bestColName, bestColumn);
                        }
                        
                    } else{
                        //ERROR
                        throw new IllegalArgumentException("Found unhandled question type of "+sQuestion);
                    }
                    
                }//end if AbstractSurveyQuestion
            }
        }
        
        //
        // Then parse the responses and match to columns
        //
        
        // flag used to indicate if the survey is comprised entirely of fill in the blank survey questions that have the
        // default scoring rules - i.e. no possible points earned/defined, always resulting in below expectation scoring once answered.
        boolean containsFillInBlankNoScoringOnly = true;
                
        for(SurveyPageResponse spResponse : sResponse.getSurveyPageResponses()){
            
            for(AbstractQuestionResponse qResponse : spResponse.getQuestionResponses()){
                
                AbstractSurveyQuestion<? extends AbstractQuestion> sQuestion = qResponse.getSurveyQuestion();                
                
                if (qResponse.getSurveyQuestion() instanceof FillInTheBlankSurveyQuestion) {

                    //check if highlight/summarize passage fill in the blank to parse differently
                    FillInTheBlankSurveyQuestion fillInTheBlankSQuestion = (FillInTheBlankSurveyQuestion)sQuestion;
                    Boolean isRemediationContentSQProp = fillInTheBlankSQuestion.getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT);
                    Boolean isRemediationContentQProp = fillInTheBlankSQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_REMEDIATION_CONTENT);
                    boolean isHighlightSummarize = (isRemediationContentSQProp != null && isRemediationContentSQProp) || (isRemediationContentQProp != null && isRemediationContentQProp);
                    boolean isSummarize = false;
                    int pointsEarned = 0;
                    
                    if(isHighlightSummarize){

                        Boolean isFieldTextboxSQProp = fillInTheBlankSQuestion.getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY);
                        Boolean isFieldTextboxQProp = fillInTheBlankSQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY);
                        isSummarize = (isFieldTextboxSQProp != null && isFieldTextboxSQProp) || (isFieldTextboxQProp != null && isFieldTextboxQProp);
                        if(isSummarize){
                            
                            //Note: currently if a survey has a summarize or highlight passage it is the only survey question in the survey,
                            //      therefore we can use a static column name here.
                            colName = SUMMARIZE_PASSAGE_COL_NAME; 

                        }else{
                            //highlight passage - parse passage with learner's highlights to grab the highlighted, possibly disjointed, text.
                            
                            //Note: currently if a survey has a summarize or highlight passage it is the only survey question in the survey,
                            //      therefore we can use a static column name here.
                            colName = HIGHLIGHT_PASSAGE_COL_NAME;
                        }
                        
                    }else{
                        colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion);
                    }
                    
                    String bestAnswerColName = colName + BEST_ANSWER;

                    if (!qResponse.getResponses().isEmpty()) {

                        StringBuilder values = new StringBuilder();

                        boolean multipleResponses = qResponse.getResponses().size() > 1;
                        for (QuestionResponseElement element : qResponse.getResponses()) {
                            
                            if(isHighlightSummarize && !isSummarize){
                                
                                //highlight passage - parse passage with learner's highlights to grab the highlighted, possibly disjointed, text.
                                StringBuilder valueSB = new StringBuilder();
                                appendDisjointedHighlights(element.getText(), valueSB);
                                values.append(valueSB.toString());

                            }else if(element.getText() != null){
                                values.append(element.getText());
                            }                            
                            
                            if (multipleResponses) {
                                values.append(SEMI_COLON);
                            }
                                                        
                            FreeResponseReplyWeights weights = fillInTheBlankSQuestion.getReplyWeights();
                            if(weights != null) {
                                for (int responseIndex = 0; responseIndex < qResponse.getResponses().size(); responseIndex++) {
    
                                    // more responses than scores
                                    if ((responseIndex + 1) > weights.getReplyWeights().size()) {
                                        break;
                                    }
    
                                    QuestionResponseElement resp = qResponse.getResponses().get(responseIndex);
                                    Double responseEarnedPoints = getEarnedPointsForResponseField(weights, responseIndex, resp.getText());
                                    pointsEarned += responseEarnedPoints == null ? 0 : responseEarnedPoints.doubleValue();
    
                                }
                            }

                        addCellForColumn(colName, values.toString());
                        
                        }
                        
                        if(pointsEarned > 0 && fillInTheBlankSQuestion.getHighestPossibleScore() == pointsEarned) {
                            addCellForColumn(bestAnswerColName, "1");
                        } else if(columns.get(bestAnswerColName) != null) {
                            addCellForColumn(bestAnswerColName, "0");
                        }

                    } else {
                        // This should never be reached because only questions with
                        // response elements should have question responses. Essentially
                        // this means there is no response for the question, ignore it
                    }
                    
                    // check if this fill in the blank has no scoring information
                    // This is a work around for having a fill in the blank with text answer score to below expectation always in
                    // order to have a dkf state transition know when the survey is completed.
                    // 
                    FreeResponseReplyWeights weights = fillInTheBlankSQuestion.getReplyWeights();
                    
                    // e.g. List<List<List<Double>>>
                    containsFillInBlankNoScoringOnly &= weights == null || weights.getReplyWeights().isEmpty() || 
                            ((weights.getReplyWeights().size() == 1 && weights.getReplyWeights().get(0).isEmpty()) || 
                            (weights.getReplyWeights().get(0).size() == 1 &&  weights.getReplyWeights().get(0).get(0).isEmpty()));

                } else if (qResponse.getSurveyQuestion() instanceof MultipleChoiceSurveyQuestion) {
                    
                    containsFillInBlankNoScoringOnly = false;

                    MultipleChoiceSurveyQuestion multChoiceSQ = (MultipleChoiceSurveyQuestion)qResponse.getSurveyQuestion();
                    colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion);
                    
                    //
                    // set column value for question's choice index in the authored list of choices (to account for randomized choices)
                    //
                    String choiceIndexColName = colName;                        
                    choiceIndexColName += CHOICE_INDEX;
                    String bestAnswerColName = colName + BEST_ANSWER;
                    int pointsEarned = 0;

                    if (!qResponse.getResponses().isEmpty()) {
                        
                        StringBuilder values = new StringBuilder();                        
                        StringBuilder choiceIndexes = new StringBuilder();

                        boolean multipleResponses = qResponse.getResponses().size() > 1;
                        for (QuestionResponseElement element : qResponse.getResponses()) {
                            if (element.getText() != null) {
                                values.append(element.getText());
                            }
                            
                            //get index of choice in authored option list
                            if(element.getChoice() != null){
                                boolean found = false;
                                for(int index = 0; index < multChoiceSQ.getChoices().getListOptions().size(); index++){
                                    
                                    
                                    if(multChoiceSQ.getChoices().getListOptions().get(index).getId() == element.getChoice().getId()){
                                        //found selected choice in original list of authored choices
                                        int indexOne = index + 1;
                                        choiceIndexes.append(indexOne); // 1-based values
                                        found = true;
                                    }
                                    
                                    if (element.getColumnIndex() != null && element.getColumnIndex() == index) {
                                        List<Double> weights = multChoiceSQ.getReplyWeights();

                                        if ( weights != null) {
                                            //if there are right/wrong answers, calculate points earned and possibly show images for whether the response was correct
                                            pointsEarned += weights.get(index);

                                        }
                                    }
                                    
                                }
                                
                                if(!found){
                                    choiceIndexes.append("-1"); //some value for now found, probably happens if the choices have changed since the question was answered
                                }
                                
                                if (multipleResponses) {
                                    choiceIndexes.append(SEMI_COLON);
                                }
                            }
                            
                            if (multipleResponses) {
                                values.append(SEMI_COLON);
                            }
                                                        
                        }
                        
                        addCellForColumn(colName, values.toString());
                        
                        if(choiceIndexes.length() > 0){
                            addCellForColumn(choiceIndexColName, choiceIndexes.toString());
                        }
                        
                        if(pointsEarned > 0 && multChoiceSQ.getHighestPossibleScore() == pointsEarned) {
                            addCellForColumn(bestAnswerColName, "1");
                        } else if(columns.get(bestAnswerColName) != null) {
                            addCellForColumn(bestAnswerColName, "0");
                        }

                    } else {
                        // This should never be reached because only questions with
                        // response elements should have question responses. Essentially
                        // this means there is no response for the question, ignore it
                    }

                } else if (qResponse.getSurveyQuestion() instanceof RatingScaleSurveyQuestion) {
                    
                    containsFillInBlankNoScoringOnly = false;
                    
                    RatingScaleSurveyQuestion ratingScaleSQ = (RatingScaleSurveyQuestion)qResponse.getSurveyQuestion();

                    if(sResponse.getSurveyId() == 0 && sQuestion.getId() == 0){
                        colName = UNKNOWN_RATING_SCALE_COL_NAME;

                    }else{
                        colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion);
                    }
                    
                    //
                    // set column value for question's choice index (some rating scale questions might have blank choices in between labeled choices)
                    //
                    String choiceIndexColName = colName;                        
                    choiceIndexColName += CHOICE_INDEX;
                    String bestAnswerColName = colName + BEST_ANSWER;
                    int pointsEarned = 0;

                    if (!qResponse.getResponses().isEmpty()) {

                        if (qResponse.getResponses().size() > 1) {
                            // We shouldn't have more than one element for a rating scale
                            // question, this is unusual but we'll allow it and
                            // use the first response element as the answer
                        }

                        QuestionResponseElement element = qResponse.getResponses().get(0);

                        if (element.getText() != null) {

                            String value = element.getText();
                            addCellForColumn(colName, value);
                            
                            //get index of choice in authored option list
                            if(element.getChoice() != null){
                                boolean found = false;
                                for(int index = 0; index < ratingScaleSQ.getChoices().getListOptions().size(); index++){
                                    
                                    if(ratingScaleSQ.getChoices().getListOptions().get(index).getId() == element.getChoice().getId()){
                                        //found selected choice in original list of authored choices
                                        addCellForColumn(choiceIndexColName, String.valueOf(index + 1)); // 1-based values
                                        found = true;
                                    }
                                    
                                    if (element.getColumnIndex() != null && element.getColumnIndex() == index) {
                                        List<Double> weights = ratingScaleSQ.getReplyWeights();

                                        if ( weights != null) {
                                            //if there are right/wrong answers, calculate points earned and possibly show images for whether the response was correct
                                            pointsEarned += weights.get(index);

                                        }
                                    }
                                }
                                
                                if(!found){
                                    addCellForColumn(colName, "-1"); //some value for now found, probably happens if the choices have changed since the question was answered
                                }

                            }
                            
                        } else {
                            // This is weird
                        }
                        
                        if(pointsEarned > 0 && ratingScaleSQ.getHighestPossibleScore() == pointsEarned) {
                            addCellForColumn(bestAnswerColName, "1");
                        } else if(columns.get(bestAnswerColName) != null) {
                            addCellForColumn(bestAnswerColName, "0");
                        }

                    } else {
                        // This should never be reached because only questions with
                        // response elements should have question responses. Essentially
                        // this means there is no response for the question, ignore it
                    }

                } else if (qResponse.getSurveyQuestion() instanceof MatrixOfChoicesSurveyQuestion) {
                    
                    containsFillInBlankNoScoringOnly = false;
                    
                    MatrixOfChoicesSurveyQuestion matrixSQ = (MatrixOfChoicesSurveyQuestion)qResponse.getSurveyQuestion();
                    double pointsEarned = 0;
                    String bestAnswerColName = qResponse.getSurveyQuestion().getQuestion().getText() + BEST_ANSWER;
                                        
                    if (!qResponse.getResponses().isEmpty()) {

                        for (QuestionResponseElement element : qResponse.getResponses()) {
                            if (element.getText() != null) {
                                // This is the header the response is mapped to
                                colName = getMatrixColName(sResponse, giftKey, sQuestion, element.getRowChoice().getText(), element.getText());
                                String rowName = getMatrixRowName(sResponse, giftKey, sQuestion, element.getRowChoice().getText());
                                String bestAnswerRowName = rowName + BEST_ANSWER;
                                String value = "1";
                                addCellForColumn(colName, value);
                                MatrixOfChoicesReplyWeights weights = matrixSQ.getReplyWeights();

                                if ( weights != null) {
                                    //if there are right/wrong answers, calculate points earned and possibly show images for whether the response was correct
                                    double answerWeight = weights.getReplyWeight(element.getRowIndex(), element.getColumnIndex());
                                    pointsEarned += answerWeight;
                                    
                                    if(answerWeight > 0 && answerWeight == weights.getMaxPointsForRow(element.getRowIndex())) {
                                        addCellForColumn(bestAnswerRowName, "1");
                                    } else if(columns.get(bestAnswerRowName) != null) {
                                        addCellForColumn(bestAnswerRowName, "0");
                                    }
                                }
                                
                            }
                        }
                        
                        if(pointsEarned > 0 && matrixSQ.getHighestPossibleScore() == pointsEarned) {
                            addCellForColumn(bestAnswerColName, "1");
                        } else if(columns.get(bestAnswerColName) != null) {
                            addCellForColumn(bestAnswerColName, "0");
                        }

                    } else {
                        // This should never be reached because only questions with
                        // response elements should have question responses. Essentially
                        // this means there is no response for the question, ignore it
                    }

                } else if (qResponse.getSurveyQuestion() instanceof SliderSurveyQuestion) {
                    
                    containsFillInBlankNoScoringOnly = false;
                    
                    SliderSurveyQuestion sliderQuestion = (SliderSurveyQuestion)qResponse.getSurveyQuestion();

                    colName = getSurveyQuestionColName(sResponse, giftKey, sQuestion);
                    String bestAnswerColName = colName + BEST_ANSWER;

                    if (!qResponse.getResponses().isEmpty()) {

                        if (qResponse.getResponses().size() > 1) {
                            // We shouldn't have more than one element for a slider
                            // question, this is unusual but we'll allow it and
                            // use the first response element as the answer
                        }

                        QuestionResponseElement element = qResponse.getResponses().get(0);

                        if (element.getText() != null) {

                            String value = element.getText();
                            addCellForColumn(colName, value);

                        } else {
                            // This is weird
                        }
                        
                        if(columns.get(bestAnswerColName) != null && sliderQuestion.getHighestPossibleScore() == Double.parseDouble(element.getText())) {
                            addCellForColumn(bestAnswerColName, "1");
                        } else if(columns.get(bestAnswerColName) != null) {
                            addCellForColumn(bestAnswerColName, "0");
                        }

                    } else {
                        // This should never be reached because only questions with
                        // response elements should have question responses. Essentially
                        // this means there is no response for the question, ignore it
                    }
                } else{
                    //ERROR
                    throw new IllegalArgumentException("Found unhandled question type of "+sQuestion);
                }                
                
            }//end for on questions in a page
        }//end for on pages

        //
        // Add scoring results
        //
        List<ScoreInterface> scores = SurveyScorerManager.getScores(sResponse);
        if(!scores.isEmpty()){
            //there are score results, add column for each
            
            // the score column is not needed for a fill in the blank only survey that has no scoring rules, other
            // than the default of no possible earned points which results in below expectation
            if(scores.size() == 1 && containsFillInBlankNoScoringOnly){
                return;
            }
            
            String scoreColName = getScoreColName(sResponse, giftKey);
            for(ScoreInterface score : scores){
                
                if(score instanceof SurveyAnswerScore){
                    SurveyAnswerScore sas = (SurveyAnswerScore)score;
                    
                    colName = scoreColName;
                    
                    if(columns.containsKey(colName)){                        
                        //prevent column collisions for this survey when it has multiple survey scorers
                        int cnt = 2;
                        String tempColName = colName + OPEN_PARENTHESIS+cnt+CLOSE_PARENTHESIS;
                        while(columns.containsKey(tempColName)){                            
                            cnt++;
                            tempColName = colName + OPEN_PARENTHESIS+cnt+CLOSE_PARENTHESIS;
                        }
                        
                        colName = tempColName;

                    }
                    
                    EventReportColumn column = new EventReportColumn(colName, colName);
                    columns.put(colName, column);
                    addCellForColumn(colName, String.valueOf(sas.getTotalEarnedPoints()));

                }else if(score instanceof SurveyScaleScore){
                    SurveyScaleScore sss = (SurveyScaleScore)score;                    
                    
                    colName = scoreColName;
                    
                    for(AbstractScale scalePair : sss.getScales()){
                        
                        colName += COLON+scalePair.getAttribute().getName();
                        
                        if(columns.containsKey(colName)){
                            //prevent column collisions for this survey when it has multiple survey scorers
                            int cnt = 2;
                            String tempColName = colName + OPEN_PARENTHESIS+cnt+CLOSE_PARENTHESIS;
                            while(columns.containsKey(tempColName)){                            
                                cnt++;
                                tempColName = colName + OPEN_PARENTHESIS+cnt+CLOSE_PARENTHESIS;
                            }
                            
                            colName = tempColName;
                        }
                        
                        colName = getScoreColName(sResponse, giftKey);
                        EventReportColumn column = new EventReportColumn(colName, colName);
                        columns.put(colName, column);
                        addCellForColumn(colName, String.valueOf(scalePair.getRawValue()));
                    }
                    
                }else if(score instanceof SurveyConceptAssessmentScore){
                    
                    SurveyConceptAssessmentScore scs = (SurveyConceptAssessmentScore)score;                    
                    
                    for(String conceptName : scs.getConceptDetails().keySet()){
                        
                        colName = scoreColName+COLON+conceptName+CORRECT_TOKEN;
                        
                        if(columns.containsKey(colName)){                        
                            //prevent column collisions for this survey when it has multiple survey scorers
                            int cnt = 2;
                            String tempColName = colName + OPEN_PARENTHESIS+cnt+CLOSE_PARENTHESIS;
                            while(columns.containsKey(tempColName)){                            
                                cnt++;
                                tempColName = colName + OPEN_PARENTHESIS+cnt+CLOSE_PARENTHESIS;
                            }
                            
                            colName = tempColName;

                        }
                        
                        //show the total number of questions correct for this concept as the cell value
                        EventReportColumn column = new EventReportColumn(colName, colName);
                        columns.put(colName, column);
                        addCellForColumn(colName, String.valueOf(scs.getConceptDetails().get(conceptName).getCorrectQuestions().size()));
                    }
                    
                }else if(score instanceof SurveyFeedbackScorer){
                    
                    //nothing to add
                    
                }else{
                    throw new IllegalArgumentException("Found unsupported scorer type of " + score);
                }
            }
        }
    }
    
    /**
     * Collect the disjointed highlights from the provided survey response passage.
     * 
     * @param textToSearch contains one or more, possibly disjointed, learner highlights for a highlight passage survey question.
     * Can't be null.
     * @param disjointedHighlights where to place the highlighted text, delimited by {@link #delim}.   Can't be null. 
     * For the first call this should be empty.  
     */
    private void appendDisjointedHighlights(String textToSearch, StringBuilder disjointedHighlights){
        
        if(textToSearch == null){
            return;
        }else if(disjointedHighlights == null){
            throw new IllegalArgumentException("The disjointedHighlights parameter can't be null.");
        }
        
        int startIndex = textToSearch.indexOf(highlightStart);
        if(startIndex != -1){
            //found the start of a highlight
            
            int nextSpanEndIndex = textToSearch.indexOf(spanEnd, startIndex + highlightStart.length());
            
            if(nextSpanEndIndex != -1){
                //found end of a highlight section
                
                if(disjointedHighlights.length() > 0){
                    disjointedHighlights.append(delim);
                }
                
                int length = startIndex + highlightStart.length();
                
                disjointedHighlights.append(textToSearch.substring(length, nextSpanEndIndex));
                
                //Recursively search for the next highlight
                if(textToSearch.length() > (nextSpanEndIndex + spanEnd.length())){
                    appendDisjointedHighlights(textToSearch.substring(nextSpanEndIndex + spanEnd.length()), disjointedHighlights);
            }
        }
    }
    }
    
    /**
     * Gets the scored points for the numeric FillInTheBlank response field.
     * 
     * @param responseFieldIndex the index of the response field.
     * @return the scored value.
     */
    private Double getEarnedPointsForResponseField(FreeResponseReplyWeights weights, int responseFieldIndex, String responseText) {
        double responseWeight = 0;

        List<List<List<Double>>> replyWeights = weights.getReplyWeights();
        if (responseFieldIndex + 1 > replyWeights.size()) {
            return responseWeight;
        }

        Double replyNumber = null;
        // look at the question's responses and gather score attributes
        for (List<Double> rowWeights : replyWeights.get(responseFieldIndex)) {
            
            // is there are no weights, return null. This happens for Free Text response fields.
            if (rowWeights.isEmpty()) {
                return null;
            }
            
            Double scoreValue = rowWeights.get(0);
            if (rowWeights.size() == 1) {
                // this is the default condition
                // always 0 for free text responses
                // authored score for catch-all numeric responses (defaults to 0)
                responseWeight += scoreValue;
                break;
            } else {
                // numeric response, only need to parse value one time
                if (replyNumber == null) {
                    try {
                        replyNumber = Double.valueOf(responseText);
                    } catch (@SuppressWarnings("unused") Exception e) {
                        continue;
                    }
                }

                // check if it is a single value or a range
                if (rowWeights.size() == 2 && Double.compare(replyNumber, rowWeights.get(1)) == 0) {
                    responseWeight += scoreValue;
                    break;
                } else if (rowWeights.size() == 3 && rowWeights.get(1) <= replyNumber && replyNumber <= rowWeights.get(2)) {
                    responseWeight += scoreValue;
                    break;
                }
            }
        }

        return responseWeight;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[SurveyResultEvent: ");
        sb.append(super.toString());
        
        sb.append(", columns = {");
        for(EventReportColumn column : columns.values()){
            sb.append(column.toString()).append(",\n");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }

}
