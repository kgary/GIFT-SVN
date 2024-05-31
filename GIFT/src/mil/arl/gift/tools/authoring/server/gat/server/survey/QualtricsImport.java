/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.survey;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesQuestion;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.RatingScaleQuestion;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor.QualtricsImportResult;

/**
 * Responsible for converting a Qualtric's Survey file (.qsf) into survey items.
 * 
 * @author mhoffman
 *
 */
public class QualtricsImport {    
    

    /**
     * Parse the Qualtrics survey export json string and create GIFT Survey items.
     * 
     * @param qsfString the Qualtrics survey export json string to parse
     * @return the result of the import including the survey pages with survey items as well
     * as information about items that failed to be imported for various reasons.  The GIFT
     * survey database will not be modified based on this import.
     * @throws DetailedException if there was a severe problem importing the Qualtrics survey items.  Note that
     * questions that don't import because GIFT doesn't support a Qualtrics survey question type will not
     * cause this exception to be thrown.  That information will be represented in the failed items messages.
     */
    public static QualtricsImportResult importQsf(String qsfString) throws DetailedException{
        
        try{
            QualtricsSurvey qSurvey = new QualtricsSurvey(qsfString);
        
            QualtricsImportResult result = new QualtricsImportResult();
            result.setSurveyPages(qSurvey.getSurveyElements().getSurveyPages());
            result.setFailedItems(qSurvey.getFailedItems());
            return result;
        }catch(Exception e){
            throw new DetailedException("Failed to successfully import the Qualtrics export file.",
                    "An exception was thrown while importing the Qualtrics survey export (.qsf) file contents that reads:\n"+e.getMessage(),e);
        }
    }
    
    /**
     * This inner class is the root class responsible for parsing the Qualtrics json text and
     * building the appropriate GIFT Survey items.
     * 
     * @author mhoffman
     *
     */
    private static class QualtricsSurvey{
        
        /**
         * JSON keys
         */
        private static final String SURVEY_ENTRY_KEY = "SurveyEntry";
        private static final String SURVEY_ELEMENTS_KEY = "SurveyElements";
        
        @SuppressWarnings("unused")
        private QualtricsSurveyEntry surveyEntry;
        
        private QualtricsSurveyElements surveyElements;
        
        private List<String> failedItems = new ArrayList<>();
        
        /**
         * Parse the Qualtrics export json string.
         * 
         * @param json from the qsf file
         */
        public QualtricsSurvey(String json){
            
            if(json == null || json.isEmpty()){
                throw new IllegalArgumentException("The survey entry JSON string can't be null or empty.");
            }
            
            parse(json);
        }
        
        /**
         * Parse the JSON string and build the GIFT survey pages with survey items
         * 
         * @param jsonString from the qsf file
         */
        private void parse(String jsonString){
            
            try{
                JSONObject jsonObj = (JSONObject) new JSONParser().parse(jsonString);
                setSurveyEntry(new QualtricsSurveyEntry((JSONObject) jsonObj.get(SURVEY_ENTRY_KEY)));
                setSurveyElements(new QualtricsSurveyElements((JSONArray) jsonObj.get(SURVEY_ELEMENTS_KEY), failedItems));
            }catch(ParseException pe){
                throw new RuntimeException("Unable to parse the Qualtrics Survey because of a JSON parse exception.", pe);
            }
            
        }

        /**
         * Contains the GIFT survey pages with survey items created with the attributes in the 
         * Qualtrics export json string.
         * 
         * @return survey element information
         */
        public QualtricsSurveyElements getSurveyElements() {
            return surveyElements;
        }

        private void setSurveyElements(QualtricsSurveyElements surveyElements) {
            this.surveyElements = surveyElements;
        }

        private void setSurveyEntry(QualtricsSurveyEntry surveyEntry) {
            this.surveyEntry = surveyEntry;
        }

        /**
         * Information about Qualtrics survey items that weren't imported successfully.  This
         * is most likely due to GIFT not supported a Qualtrics survey item type or setting.
         * 
         * @return collection of messages about Qualtrics survey item's that weren't imported.
         * Can be null or empty.
         */
        public List<String> getFailedItems() {
            return failedItems;
        }
    }
    
    /**
     * Contains metadata about the survey being imported from Qualtrics.
     * 
     * @author mhoffman
     *
     */
    private static class QualtricsSurveyEntry{
        
        /**
         * JSON keys
         */
        private static String SURVEY_NAME_KEY = "SurveyName";        
        
        private String surveyName;
        
        /**
         * Parse the Qualtrics Survey Entry json object from the qsf file.
         * 
         * @param json survey entry json object
         */
        public QualtricsSurveyEntry(JSONObject json){
            
            if(json == null){
                throw new IllegalArgumentException("The survey entry JSON string can't be null.");
            }
            
            parse(json);
        }
        
        /**
         * Retrieve important attributes from the survey entry json object.
         * 
         * @param json survey entry json object
         */
        private void parse(JSONObject json){
            
            setSurveyName((String) json.get(SURVEY_NAME_KEY));
        }

        /**
         * The name of the Qualtrics survey as authored in Qualtrics UI.
         * 
         * @return survey name
         */
        public String getSurveyName() {
            return surveyName;
        }

        private void setSurveyName(String surveyName) {
            
            if(surveyName == null || surveyName.isEmpty()){
                throw new IllegalArgumentException("The survey name can't be null.");
            }
            
            this.surveyName = surveyName;
        }
        
        @Override
        public String toString(){
            
            StringBuffer sb = new StringBuffer();
            sb.append("[QualtricsSurveyEntry: ");
            sb.append("survey name = ").append(getSurveyName());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Responsible for parsing the Qualtrics survey element json object and creating
     * GIFT survey pages with GIFT survey elements.
     * 
     * @author mhoffman
     *
     */
    private static class QualtricsSurveyElements{
        
        /**
         * JSON keys
         */
        private static final String ELEMENT_KEY = "Element";
        private static final String PAYLOAD_KEY = "Payload";
        private static final String PRIMARY_ATTR_KEY = "PrimaryAttribute";
        private static final String QUESTION_TEXT_KEY = "QuestionText";
        private static final String QUESTION_TYPE = "QuestionType";
        private static final String CHOICES_KEY = "Choices";
        private static final String DISPLAY_KEY = "Display";
        private static final String CHOICE_ORDER_KEY = "ChoiceOrder";
        private static final String SELECTOR_KEY = "Selector";
        private static final String SUB_SELECTOR_KEY = "SubSelector";
        private static final String DATA_EXPORT_TAG = "DataExportTag";
        private static final String ANSWERS_KEY = "Answers";
        private static final String ANSWERS_ORDER_KEY = "AnswerOrder";
        private static final String URL_KEY = "URL";
        private static final String GRAPHICS_KEY = "Graphics";
        private static final String GRAPHICS_DESCRIPTION_KEY = "GraphicsDescription";
        private static final String CONFIGURATION_KEY = "Configuration";
        private static final String CSSLIDER_MIN_KEY = "CSSliderMin";
        private static final String CSSLIDER_MAX_KEY = "CSSliderMax";
        private static final String GRID_LINES_KEY = "GridLines";
        private static final String NUM_DECIMALS_KEY = "NumDecimals";
        private static final String STAR_COUNT_KEY = "StarCount";
        private static final String STAR_TYPE_KEY = "StarType";
        
        @SuppressWarnings("unused")
        private static final String DISCRETE_STAR_TYPE = "discrete";
        private static final String HALF_STAR_TYPE = "half";
        private static final String CONTINUOUS_STAR_TYPE = "continuous";
        
        //Required/Optional section
        private static final String FORCE_RESPONSE = "ForceResponse";
        private static final String FORCE_RESPONSE_TYPE_KEY = "ForceResponseType";
        private static final String VALIDATION_KEY = "Validation";
        private static final String SETTINGS_KEY = "Settings";
        private static final String MIN_CHOICES = "MinChoices";
        private static final String MAX_CHOICES = "MaxChoices";
        
        //Randomization section
        private static final String RANDOMIZATION_KEY = "Randomization";
        private static final String TYPE_KEY = "Type";
        
        private static final String ON_VALUE = "ON";
        private static final String ALL_VALUE = "ALL";        
        
        //question types - supported
        private static final String DESCRIPTIVE_BLOCK_QUESTION_TYPE = "DB";
        private static final String MULT_CHOICE_QUESTION_TYPE = "MC";
        private static final String MATRIX_QUESTION_TYPE = "Matrix";
        private static final String TEXT_ENTRY_QUESTION_TYPE = "TE";
        private static final String SLIDER_QUESTION_TYPE = "Slider";
        
        //question types - not supported
        private static final String CONSTANT_SUM_TYPE = "CS";
        private static final String CONSTANT_SUM_TYPE_LABEL = "Constant Sum";
        private static final String RANK_ORDER_TYPE = "RO";
        private static final String RANK_ORDER_TYPE_LABEL = "Rank Order";
        private static final String PICK_GROUP_RANK_TYPE = "PGR";
        private static final String PICK_GROUP_RANK_TYPE_LABEL = "Pick Group and Rank";
        private static final String GAP_TYPE = "GAP";
        private static final String GAP_TYPE_LABEL = "GAP Analysis";
        private static final String GRAPHIC_SLIDER_TYPE = "GS";
        private static final String GRAPHIC_SLIDER_SS_TYPE = "SS";
        private static final String GRAPHIC_SLIDER_TYPE_LABEL = "Graphic Slider";
        private static final String DRILL_DOWN_TYPE = "DD";
        private static final String DRILL_DOWN_TYPE_LABEL = "Drill Down";
        private static final String SIDE_BY_SIDE_TYPE = "SBS";
        private static final String SIDE_BY_SIDE_TYPE_LABEL = "Side By Side";
        
        // Survey elements
        private static final String BLOCK_ELEMENT = "BL";
        private static final String SURVEY_QUESTION_ELEMENT = "SQ";
        
        //Selectors
        private static final String MC_MULTISELECT_BOX = "MSB";
        private static final String MC_MULTISELECT_HORIZONTAL = "MAHR";
        private static final String MC_MULTISELECT_VERTICAL = "MAVR";
        private static final String MC_SIDE_ANSWER_HORIZONTAL = "SAHR";
        private static final String MATRIX_MULTISELECT = "MultipleAnswer";    
        private static final String MULTIPLE_LINE_SELECTOR = "ML";
                
        //block types
        private static final String DEFAULT_BLOCK_TYPE = "Default";
        private static final String STANDARD_BLOCK_TYPE = "Standard";
        
        private List<BlockElement> blockElements = new ArrayList<>();

        /**
         * Parse the survey element json array of json objects.
         * 
         * @param jsonArray the survey element json array from the qsf file.
         * @param failedItems collection of messages related to survey items or attributes that could not
         * be imported into GIFT for various reasons.
         */
        public QualtricsSurveyElements(JSONArray jsonArray, List<String> failedItems){
            
            if(jsonArray == null){
                throw new IllegalArgumentException("The survey elements JSON Array can't be null.");
            }
            
            parse(jsonArray, failedItems);
        }
        
        private void parse(JSONArray jsonArray, List<String> failedItems){
            
            for(int index = 0; index < jsonArray.size(); index++){
                
                JSONObject jsonObj = (JSONObject) jsonArray.get(index);
                
                String element = (String) jsonObj.get(ELEMENT_KEY);
                if(element.equals(BLOCK_ELEMENT)){
                    
                    Object payloadObj = jsonObj.get(PAYLOAD_KEY);
                    if(payloadObj instanceof JSONArray){
                        //the blocks don't have keys, each are JSON objects
                        
                        for(int payloadIndex = 0; payloadIndex < ((JSONArray)payloadObj).size(); payloadIndex++){
                            
                            JSONObject payloadJSONObj = (JSONObject) ((JSONArray)payloadObj).get(payloadIndex);
                            
                            String blockType = (String) payloadJSONObj.get(TYPE_KEY);
                            if(blockType.equals(DEFAULT_BLOCK_TYPE) || blockType.equals(STANDARD_BLOCK_TYPE)){
                                addBlockElement(new BlockElement(payloadJSONObj, failedItems));
                            }
                        }
                        
                            
                    }else{
                        //the blocks have keys
                        
                        for(Object value : ((JSONObject) payloadObj).values()){
                            
                            JSONObject payloadJSONObj = (JSONObject) value;
                            
                            String blockType = (String) payloadJSONObj.get(TYPE_KEY);
                            if(blockType.equals(DEFAULT_BLOCK_TYPE) || blockType.equals(STANDARD_BLOCK_TYPE)){
                                addBlockElement(new BlockElement(payloadJSONObj, failedItems));
                            }
                        }
                    }

                    
                }else if(element.equals(SURVEY_QUESTION_ELEMENT)){
                        parseSurveyQuestion(jsonObj, failedItems);
                }
            }
        }

        /**
         * Return the survey pages for all Qualtric's blocks.
         * 
         * @return the survey pages for all blocks.  Can be empty if there are no blocks.
         */
        public List<SurveyPage> getSurveyPages(){
            
            List<SurveyPage> surveyPages = new ArrayList<>();
            for(BlockElement blockElement : blockElements){
                surveyPages.addAll(blockElement.getSurveyPages());
            }
            
            return surveyPages;
        }

        private void addBlockElement(BlockElement blockElement) {
            this.blockElements.add(blockElement);
        }
        
        /**
         * Parse the Qualtrics survey question json object and create a GIFT survey item from it's attributes.
         * The GIFT survey question will then be set on the appropriate survey page in the appropriate location.
         * 
         * @param jsonObj contains the Qualtrics survey question attributes to import into a GIFT survey item
         * @param failedItems collection of messages about Qualtrics survey items and attributes that couldn't be imported.
         */
        private void parseSurveyQuestion(JSONObject jsonObj, List<String> failedItems){
            
            //qualtrics question id, used to uniquely identify the question in the qsf json string
            String qId = (String) jsonObj.get(PRIMARY_ATTR_KEY);
            
            JSONObject payloadObj = (JSONObject) jsonObj.get(PAYLOAD_KEY);
            
            String questionText = (String) payloadObj.get(QUESTION_TEXT_KEY);
            
            String questionType = (String) payloadObj.get(QUESTION_TYPE);
            
            try {
                if(questionType.equals(DESCRIPTIVE_BLOCK_QUESTION_TYPE)){                
                    //create Text Survey Element
                    
                    TextSurveyElement textSurveyElement = new TextSurveyElement(0, 0, new SurveyItemProperties());
                    
                    if(payloadObj.containsKey(URL_KEY)){
                        //add graphic URL to end of text since Qualtrics shows text followed by image on new line
                        
                        String url = (String) payloadObj.get(URL_KEY);
                        
                        questionText += "<br><img src=\""+url+"\">";
                    }else if(payloadObj.containsKey(GRAPHICS_KEY)){
                        //doesn't contain URL key but does contain Graphics means its a image uploaded to Qualtrics
                        //which Qualtrics doesn't export
                        failedItems.add("Unable to add the image (description: '"+payloadObj.get(GRAPHICS_DESCRIPTION_KEY)+"') shown in question with id '"+qId+"' because Qualtrics doesn't export images that have been uploaded to Qualtrics.");
                        
                        //add message to question to let author know
                        questionText += "<br>[Media import not supported, description: '"+payloadObj.get(GRAPHICS_DESCRIPTION_KEY)+"']"; 
                    }
                    
                    textSurveyElement.getProperties().setPropertyValue(SurveyPropertyKeyEnum.TEXT, questionText);
                    
                    updateCommonProperties(payloadObj, textSurveyElement.getProperties());
                    
                    updateQuestion(textSurveyElement, qId);
                    
                }else if(questionType.equals(MULT_CHOICE_QUESTION_TYPE)){
                    //create multiple choice 
                    
                    handleMultipleChoiceQuestion(qId, questionText, questionType, payloadObj, failedItems);
                    
                }else if(questionType.equals(MATRIX_QUESTION_TYPE)){
                    //matrix of choices
                    
                    handleMatrixOfChoicesQuestion(qId, questionText, questionType, payloadObj, failedItems);
                    
                }else if(questionType.equals(TEXT_ENTRY_QUESTION_TYPE)){
                    
                    FillInTheBlankQuestion fillInQuestion = new FillInTheBlankQuestion(0, questionText, new SurveyItemProperties(), null, null, null);
                    
                    String selector = (String) payloadObj.get(SELECTOR_KEY);
                    if(MULTIPLE_LINE_SELECTOR.equals(selector)){
                        fillInQuestion.setIsAnswerFieldTextBox(false);
                    }
                    
                    updateCommonProperties(payloadObj, fillInQuestion.getProperties());
                    
                    FillInTheBlankSurveyQuestion fillInSurveyQuestion = new FillInTheBlankSurveyQuestion(0, 0, fillInQuestion, fillInQuestion.getProperties());
                    updateQuestion(fillInSurveyQuestion, qId);
                    
                }else if(questionType.equals(SLIDER_QUESTION_TYPE)){
                    //slider question 
                    
                    handleSliderQuestion(qId, questionText, questionType, payloadObj, failedItems);
                    
                }else if(questionType.equals(CONSTANT_SUM_TYPE)){
                    
                    failedItems.add("Unable to import "+CONSTANT_SUM_TYPE_LABEL+" ("+CONSTANT_SUM_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else if(questionType.equals(RANK_ORDER_TYPE)){
                    
                    failedItems.add("Unable to import "+RANK_ORDER_TYPE_LABEL+" ("+RANK_ORDER_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else if(questionType.equals(PICK_GROUP_RANK_TYPE)){
                    
                    failedItems.add("Unable to import "+PICK_GROUP_RANK_TYPE_LABEL+" ("+PICK_GROUP_RANK_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else if(questionType.equals(GAP_TYPE)){
                    
                    failedItems.add("Unable to import "+GAP_TYPE_LABEL+" ("+GAP_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else if(questionType.equals(GRAPHIC_SLIDER_TYPE)){
                    
                    failedItems.add("Unable to import "+GRAPHIC_SLIDER_TYPE_LABEL+" ("+GRAPHIC_SLIDER_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else if(questionType.equals(DRILL_DOWN_TYPE)){
                    
                    failedItems.add("Unable to import "+DRILL_DOWN_TYPE_LABEL+" ("+DRILL_DOWN_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else if(questionType.equals(SIDE_BY_SIDE_TYPE)){
                    
                    failedItems.add("Unable to import "+SIDE_BY_SIDE_TYPE_LABEL+" ("+SIDE_BY_SIDE_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else if(questionType.equals(GRAPHIC_SLIDER_SS_TYPE)){
                    
                    failedItems.add("Unable to import "+GRAPHIC_SLIDER_TYPE_LABEL+" ("+GRAPHIC_SLIDER_SS_TYPE+") survey question with id '"+qId+"' because GIFT doesn't support that question type.");
                    updateQuestion(null, qId);
                    
                }else{
                    
                    failedItems.add("Unable to import survey question with id '"+qId+"' because the Qualtrics question type '"+questionType+"' is not recognized.");
                    updateQuestion(null, qId);
                }
            } catch (Exception e) {
                throw new DetailedException("There was a problem parsing a survey question element.",
                        "An exception was thrown while parsing the survey question with id " + qId +" of type " + questionType + ":\n"+jsonObj+"\n\nThe error reads:\n"+e.getMessage(), e);
            }
            
        }
        
        /**
         * Create a GIFT slider survey question.
         * Note: in Qualtrics a slider question type can have multiple rows and various types of column values
         *       therefore this is a matrix question type.  However the json attributes used are very different
         *       than a qualtrics matrix question type hence using a different method to handle it.
         * 
         * @param qId the Qualtrics unique question id in the json string
         * @param questionText the text of the question
         * @param questionType the type of question
         * @param payloadObj contains other question attribute to use to build the GIFT slider survey question
         * @param failedItems collection of messages about Qualtrics survey items and attributes that couldn't be imported.
         */
        private void handleSliderQuestion(String qId, String questionText, String questionType, JSONObject payloadObj, List<String> failedItems){
            
            MatrixOfChoicesQuestion matrixQuestion = new MatrixOfChoicesQuestion(0, questionText, new SurveyItemProperties(), null, null, null);

            updateCommonProperties(payloadObj, matrixQuestion.getProperties());
            
            //
            // Rows
            //
            
            JSONArray rowOrder = (JSONArray) payloadObj.get(CHOICE_ORDER_KEY);
            
            //this can be a jsonobject or a jsonarray
            //when a jsonarray the choice ids are the text themselves, i.e. numbers as text
            Object rowsObj = payloadObj.get(CHOICES_KEY);
            
            List<ListOption> rowListOptions = new ArrayList<>();

            
            if(rowOrder != null){
                //custom order
                
                Iterator<?> itr = rowOrder.iterator();
                while(itr.hasNext()){
                    
                    String choiceText;
                    if(rowsObj instanceof JSONObject){
                        JSONObject choice  = (JSONObject) ((JSONObject) rowsObj).get(itr.next().toString());
                        choiceText = (String) choice.get(DISPLAY_KEY);
                    }else{
                        choiceText = itr.next().toString();
                    }
                    
                    ListOption listOption = new ListOption(0, choiceText);
                    rowListOptions.add(listOption);
                }
                
            }else{
                                    
                if(rowsObj instanceof JSONObject){
                    for(Object keyObj : ((JSONObject)rowsObj).keySet()){
                        
                        String key = (String) keyObj;
                        JSONObject choice  = (JSONObject) ((JSONObject)rowsObj).get(key);
                        
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        
                        ListOption listOption = new ListOption(0, choiceText);
                        rowListOptions.add(listOption);
                    }
                }else{
                    Iterator<?> itr = ((JSONArray)rowsObj).iterator();
                    while(itr.hasNext()){
                        JSONObject choice = (JSONObject) itr.next();
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        
                        ListOption listOption = new ListOption(0, choiceText);
                        rowListOptions.add(listOption);
                    }
                }
            }
            
            // update sort key based on new ordering of choices
            for(int index = 0; index < rowListOptions.size(); index++){
                rowListOptions.get(index).setSortKey(index);
            }
            
            OptionList rowOptionList = new OptionList(0, "unknown", false, rowListOptions, null, null);
            matrixQuestion.setRowOptions(rowOptionList);
            
            //
            // Columns - explore the Configuration json object
            //
            
            List<ListOption> columnListOptions = new ArrayList<>();
            
            JSONObject configObj = (JSONObject) payloadObj.get(CONFIGURATION_KEY);
            if(configObj.containsKey(STAR_COUNT_KEY)){
                //stars are the selectable values, use the count as column values (e.g. count 5, columns are 1,2,3,4,5)
                
                long count = (Long) configObj.get(STAR_COUNT_KEY);
                
                //check star type: discrete (whole stars), half (allows for half stars), continuous (allows for finer grain filling in of star shape)
                String starType = (String)configObj.get(STAR_TYPE_KEY);
                if(HALF_STAR_TYPE.equals(starType)){
                    //allow half values (0.5, 1.5, ...)
                    
                    //1 + start count in order to handle zero
                    for(int index = 0; index <= count; index++){
                        
                        ListOption wholeNumListOption = new ListOption(0, String.valueOf(index));
                        columnListOptions.add(wholeNumListOption);
                        
                        ListOption decimaleListOption = new ListOption(0, String.valueOf(index + ".5"));
                        columnListOptions.add(decimaleListOption);
                    }
                    
                }else{
                    
                    if(CONTINUOUS_STAR_TYPE.equals(starType)){
                        failedItems.add("GIFT doesn't support continuous selection type that is used in the Slider survey question with id '"+qId+"'.  Using discrete selection instead.");
                    }
                    
                    //1 + start count in order to handle zero
                    for(int index = 0; index <= count; index++){
                        
                        ListOption listOption = new ListOption(0, String.valueOf(index));
                        columnListOptions.add(listOption);
                    }
                }                

            }else{
                //use slider min for first column value
                //use slider max for last column value
                //divide the difference by grid lines value to determine the remaining number of columns
                
                long sliderMin = (Long)configObj.get(CSSLIDER_MIN_KEY);
                long sliderMax = (Long)configObj.get(CSSLIDER_MAX_KEY);
                long gridLines = (Long)configObj.get(GRID_LINES_KEY);
                int numOfDecimals = Integer.valueOf((String)configObj.get(NUM_DECIMALS_KEY));
                
                DecimalFormat df = new DecimalFormat("#.##");
                df.setMaximumFractionDigits(numOfDecimals);
                
                ListOption firstColListOption = new ListOption(0, df.format(sliderMin));
                columnListOptions.add(firstColListOption);
                
                int numOfColumns = (int) ((sliderMax - sliderMin) / gridLines);
                for(int index = 1; index < numOfColumns; index++){
                    
                    double value = sliderMin + (index * ((sliderMax - sliderMin) / numOfColumns));
                    ListOption listOption = new ListOption(0, df.format(value));
                    columnListOptions.add(listOption);
                }
                
                ListOption LastColListOption = new ListOption(0, df.format(sliderMax));
                columnListOptions.add(LastColListOption);
            }
            
            
            OptionList columnOptionList = new OptionList(0, "unknown", false, columnListOptions, null, null);
            matrixQuestion.setColumnOptions(columnOptionList);
            
            MatrixOfChoicesSurveyQuestion matrixSurveyQuestion = new MatrixOfChoicesSurveyQuestion(0, 0, matrixQuestion, matrixQuestion.getProperties());
            updateQuestion(matrixSurveyQuestion, qId);
        }
        
        /**
         * Create a GIFT matrix of choices survey question.
         * 
         * @param qId the Qualtrics unique question id in the json string
         * @param questionText the text of the question
         * @param questionType the type of question
         * @param payloadObj contains other question attribute to use to build the GIFT matrix of choices survey question
         * @param failedItems collection of messages about Qualtrics survey items and attributes that couldn't be imported.
         */
        private void handleMatrixOfChoicesQuestion(String qId, String questionText, String questionType, JSONObject payloadObj, List<String> failedItems){
            
            MatrixOfChoicesQuestion matrixQuestion = new MatrixOfChoicesQuestion(0, questionText, new SurveyItemProperties(), null, null, null);
            
            if(payloadObj.containsKey(SUB_SELECTOR_KEY)) {
                String subselector = (String) payloadObj.get(SUB_SELECTOR_KEY);
                if(subselector.equals(MATRIX_MULTISELECT)) {
                    matrixQuestion.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED, true);
                    failedItems.add("Unable to apply multi-select to survey question with id '"+qId+"' because GIFT doesn't support multi-select for Matrix of Choices questions.");
                }
            }

            updateCommonProperties(payloadObj, matrixQuestion.getProperties());
            
            //
            // Rows
            //
            
            JSONArray rowOrder = (JSONArray) payloadObj.get(CHOICE_ORDER_KEY);
            
            //this can be a jsonobject or a jsonarray
            //when a jsonarray the choice ids are the text themselves, i.e. numbers as text
            Object rowsObj = payloadObj.get(CHOICES_KEY);
            
            List<ListOption> rowListOptions = new ArrayList<>();

            
            if(rowOrder != null){
                //custom order
                
                Iterator<?> itr = rowOrder.iterator();
                while(itr.hasNext()){
                    
                    String choiceText;
                    if(rowsObj instanceof JSONObject){
                        JSONObject choice  = (JSONObject) ((JSONObject) rowsObj).get(itr.next().toString());
                        choiceText = (String) choice.get(DISPLAY_KEY);
                    }else{
                        choiceText = itr.next().toString();
                    }
                    
                    ListOption listOption = new ListOption(0, choiceText);
                    rowListOptions.add(listOption);
                }
                
            }else{
                                    
                if(rowsObj instanceof JSONObject){
                    for(Object keyObj : ((JSONObject)rowsObj).keySet()){
                        
                        String key = (String) keyObj;
                        JSONObject choice  = (JSONObject) ((JSONObject)rowsObj).get(key);
                        
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        
                        ListOption listOption = new ListOption(0, choiceText);
                        rowListOptions.add(listOption);
                    }
                }else{
                    Iterator<?> itr = ((JSONArray)rowsObj).iterator();
                    while(itr.hasNext()){
                        JSONObject choice = (JSONObject) itr.next();
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        
                        ListOption listOption = new ListOption(0, choiceText);
                        rowListOptions.add(listOption);
                    }
                }
            }
            
            // update sort key based on new ordering of choices
            for(int index = 0; index < rowListOptions.size(); index++){
                rowListOptions.get(index).setSortKey(index);
            }
            
            OptionList rowOptionList = new OptionList(0, "unknown", false, rowListOptions, null, null);
            matrixQuestion.setRowOptions(rowOptionList);
            
            //
            // Columns
            //
            
            JSONArray columnOrder = (JSONArray) payloadObj.get(ANSWERS_ORDER_KEY);
            
            //this can be a jsonobject or a jsonarray
            //when a jsonarray the choice ids are the text themselves, i.e. numbers as text
            Object columnsObj = payloadObj.get(ANSWERS_KEY);
            
            List<ListOption> columnListOptions = new ArrayList<>();

            
            if(columnOrder != null){
                //custom order
                
                Iterator<?> itr = columnOrder.iterator();
                while(itr.hasNext()){
                    
                    String choiceText;
                    if(columnsObj instanceof JSONObject){
                        String choiceKey = itr.next().toString();
                        JSONObject choice  = (JSONObject) ((JSONObject) columnsObj).get(choiceKey);
                        choiceText = (String) choice.get(DISPLAY_KEY);
                        if (choiceText == null){
                            choice = (JSONObject)choice.get(choiceKey);
                            choiceText = (String)choice.get(DISPLAY_KEY);
                        }
                    }else{
                        choiceText = itr.next().toString();
                    }
                    
                    ListOption listOption = new ListOption(0, choiceText);
                    columnListOptions.add(listOption);
                }
                
            }else{
                                    
                if(columnsObj instanceof JSONObject){
                    for(Object keyObj : ((JSONObject)columnsObj).keySet()){
                        
                        String key = (String) keyObj;
                        JSONObject choice  = (JSONObject) ((JSONObject)columnsObj).get(key);
                        
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        if (choiceText == null){
                            choice = (JSONObject)choice.get(key);
                            choiceText = (String)choice.get(DISPLAY_KEY);
                        }
                        
                        ListOption listOption = new ListOption(0, choiceText);
                        columnListOptions.add(listOption);
                    }
                }else{
                    Iterator<?> itr = ((JSONArray)columnsObj).iterator();
                    while(itr.hasNext()){
                        JSONObject choice = (JSONObject) itr.next();
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        ListOption listOption = new ListOption(0, choiceText);
                        columnListOptions.add(listOption);
                    }
                }
            }
            
            // update sort key based on new ordering of choices
            for(int index = 0; index < columnListOptions.size(); index++){
                columnListOptions.get(index).setSortKey(index);
            }
        
            OptionList columnOptionList = new OptionList(0, "unknown", false, columnListOptions, null, null);
            matrixQuestion.setColumnOptions(columnOptionList);
            
            MatrixOfChoicesSurveyQuestion matrixSurveyQuestion = new MatrixOfChoicesSurveyQuestion(0, 0, matrixQuestion, matrixQuestion.getProperties());
            updateQuestion(matrixSurveyQuestion, qId);
        }
        
        /**
         * Create a GIFT multiple choice survey question.
         * 
         * @param qId the Qualtrics unique question id in the json string
         * @param questionText the text of the question
         * @param questionType the type of question
         * @param payloadObj contains other question attribute to use to build the GIFT multiple choice survey question
         * @param failedItems collection of messages about Qualtrics survey items and attributes that couldn't be imported.
         */
        private void handleMultipleChoiceQuestion(String qId, String questionText, String questionType, JSONObject payloadObj, List<String> failedItems){
            
            AbstractQuestion mcqElement;
            
            //check for multi select
            boolean isMultiSelect = false;
            boolean isRatingScale = false;
            if(payloadObj.containsKey(SELECTOR_KEY)) {
                String selector = (String) payloadObj.get(SELECTOR_KEY);
                if(selector.equals(MC_MULTISELECT_BOX) || selector.equals(MC_MULTISELECT_VERTICAL)) {
                    isMultiSelect = true;
                } else if (selector.equals(MC_MULTISELECT_HORIZONTAL) || selector.equals(MC_SIDE_ANSWER_HORIZONTAL)) {
                    isRatingScale = true;
                    failedItems.add("Unable to apply multi-select to survey question with id '"+qId+"' because it is being imported as a GIFT Rating Scale question type which doesn't support multi-select.");
                }
            }
            
            if(isRatingScale) {
                mcqElement = new RatingScaleQuestion(0, questionText, new SurveyItemProperties(), null, null, null);
            } else {
                mcqElement = new MultipleChoiceQuestion(0, questionText, new SurveyItemProperties(), null, null, null);
            }
            
            updateCommonProperties(payloadObj, mcqElement.getProperties());
            
            JSONArray choiceOrder = (JSONArray) payloadObj.get(CHOICE_ORDER_KEY);
            
            //this can be a jsonobject or a jsonarray
            //when a jsonarray the choice ids are the text themselves, i.e. numbers as text
            Object choicesObj = payloadObj.get(CHOICES_KEY);
            
            List<ListOption> listOptions = new ArrayList<>();

            
            if(choiceOrder != null){
                //custom order
                
                Iterator<?> itr = choiceOrder.iterator();
                while(itr.hasNext()){
                    
                    String choiceText;
                    if(choicesObj instanceof JSONObject){
                        JSONObject choice  = (JSONObject) ((JSONObject) choicesObj).get(itr.next().toString());
                        choiceText = (String) choice.get(DISPLAY_KEY);
                    }else{
                        choiceText = itr.next().toString();
                    }
                    
                    ListOption listOption = new ListOption(0, choiceText);
                    listOptions.add(listOption);
                }
                
            }else{
                                    
                if(choicesObj instanceof JSONObject){
                    for(Object keyObj : ((JSONObject)choicesObj).keySet()){
                        
                        String key = (String) keyObj;
                        JSONObject choice  = (JSONObject) ((JSONObject)choicesObj).get(key);
                        
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        
                        ListOption listOption = new ListOption(0, choiceText);
                        listOptions.add(listOption);
                    }
                }else{
                    Iterator<?> itr = ((JSONArray)choicesObj).iterator();
                    while(itr.hasNext()){
                        JSONObject choice = (JSONObject) itr.next();
                        String choiceText = (String) choice.get(DISPLAY_KEY);
                        
                        ListOption listOption = new ListOption(0, choiceText);
                        listOptions.add(listOption);
                    }
                }
            }
            
            // update sort key based on new ordering of choices
            for(int index = 0; index < listOptions.size(); index++){
                listOptions.get(index).setSortKey(index);
            }
            
            if(isMultiSelect) {
                mcqElement.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.MULTI_SELECT_ENABLED, true);
                
                // default the max selections to the number of options
                mcqElement.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY, listOptions.size());

                if(payloadObj.containsKey(VALIDATION_KEY)) {
                    JSONObject settings = (JSONObject) ((JSONObject) payloadObj.get(VALIDATION_KEY)).get(SETTINGS_KEY);
                    if(settings != null) {
                        if(settings.containsKey(MIN_CHOICES) && settings.get(MIN_CHOICES) != null) {
                            mcqElement.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY, Integer.parseInt((String) settings.get(MIN_CHOICES)));
                        }
                        if(settings.containsKey(MAX_CHOICES) && settings.get(MAX_CHOICES) != null) {
                            mcqElement.getProperties().setIntegerPropertyValue(SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY, Integer.parseInt((String) settings.get(MAX_CHOICES)));
                        }
                    }
                }
            }
                
            //check for random setting
            try{
                if(isRandom((JSONObject) payloadObj.get(RANDOMIZATION_KEY))){
                    mcqElement.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE, true);
                }
                
            }catch(Exception e){
                failedItems.add("Unable to apply the randomization type for survey question with id "+qId+" because "+e.getMessage());
            }

            
            OptionList optionList = new OptionList(0, "unknown", false, listOptions, null, null);
            if(isRatingScale) {
                ((RatingScaleQuestion) mcqElement).setReplyOptionSet(optionList);
                RatingScaleSurveyQuestion mcsq = new RatingScaleSurveyQuestion(0, 0, (RatingScaleQuestion) mcqElement, mcqElement.getProperties());                
                updateQuestion(mcsq, qId);
                
            } else {
                ((MultipleChoiceQuestion) mcqElement).setReplyOptionSet(optionList);
                MultipleChoiceSurveyQuestion mcsq = new MultipleChoiceSurveyQuestion(0, 0, (MultipleChoiceQuestion) mcqElement, mcqElement.getProperties());                
                updateQuestion(mcsq, qId);
            }
        }
        
        /**
         * Retrieve common question properties from the Qualtrics JSON object and update the survey item
         * properties accordingly.
         * 
         * @param jsonObj contains survey properties to retrieve (e.g. data export tag, is required)
         * @param properties the survey item properties to update
         */
        private void updateCommonProperties(JSONObject jsonObj, SurveyItemProperties properties){
            
            String dataExportTag = (String) jsonObj.get(DATA_EXPORT_TAG);
            
            if(dataExportTag != null){
                properties.setPropertyValue(SurveyPropertyKeyEnum.TAG, dataExportTag);
            }
            
            //Required or Optional?
            if(isRequired((JSONObject) jsonObj.get(VALIDATION_KEY))){
                properties.setBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED, true);
            }
            
        }
        
        /**
         * Return whether the validation JSON object specifies the question element is required or not.
         * 
         * @param validationObj contains the parameters to check if a question element is required
         * @return true if the JSON object contains the necessary parameters to define a question element as required
         */
        private boolean isRequired(JSONObject validationObj){
            
            if(validationObj == null){
                return false;
            }else if(validationObj.containsKey(SETTINGS_KEY)){
                
                JSONObject settingsObj = (JSONObject) validationObj.get(SETTINGS_KEY);
                String forceResponse = (String) settingsObj.get(FORCE_RESPONSE);
                String forceResponseType = (String) settingsObj.get(FORCE_RESPONSE_TYPE_KEY);
                if(ON_VALUE.equals(forceResponse) && ON_VALUE.equals(forceResponseType)){
                    return true;
                }
            }
            
            return false;
        }
        
        private boolean isRandom(JSONObject randomObj) throws Exception{
            
            if(randomObj == null){
                return false;
            }else{
                
                String type = (String) randomObj.get(TYPE_KEY);
                if(ALL_VALUE.equalsIgnoreCase(type)){
                    return true;
                }else{
                    throw new Exception("found unsupported randomization of '"+type+"'.");
                }
            }
        }
        
        /**
         * Replace the placeholder survey element created when setting up the number of elements
         * and ordering on each survey page with the actual GIFT survey question type.
         * 
         * @param surveyElement the gift survey question type to replace the placeholder survey element with
         * on the appropriate survey page.  If null the survey element is removed from the survey page.  This is
         * useful when the survey item couldn't be imported for whatever reason.
         * @param qualtricsQuestionId the Qualtrics unique question id that identifies the question element in the qualtrics
         * JSON export.
         */
        private void updateQuestion(AbstractSurveyElement surveyElement, String qualtricsQuestionId){
            
            for(BlockElement blockElement : blockElements){
                for(SurveyPage surveyPage : blockElement.getSurveyPages()){
                    
                    ListIterator<AbstractSurveyElement> itr = surveyPage.getElements().listIterator();
                    while(itr.hasNext()){
                        
                        AbstractSurveyElement element = itr.next();
                        String tagValue = (String) element.getProperties().getPropertyValue(SurveyPropertyKeyEnum.TAG);
                        if(tagValue != null && tagValue.equals(qualtricsQuestionId)){
                            //replace question on this page
                            
                            if(surveyElement == null){
                                //the survey element is not being imported so remove it from the survey page
                                itr.remove();
                            }else{
                                itr.set(surveyElement);
                            }
                            return;
                        }
                    }
                }
            }
        }

    }
    
    /**
     * Responsible for managing a block in Qualtrics.
     * From Qualtrics: "A block is a group of questions that are displayed as a set within your survey. 
     * Every survey includes at least one block of questions."
     * 
     * @author mhoffman
     *
     */
    private static class BlockElement{
        
        /**
         * JSON keys
         */
        private static final String BLOCK_ELEMENTS_KEY = "BlockElements";
        private static final String BLOCK_ELEMENT_TYPE_KEY = "Type";
        private static final String QUESTION_ID_KEY = "QuestionID";
        private static final String SKIP_LOGIC_KEY = "SkipLogic";
        
        //block element types
        private static final String QUESTION_TYPE = "Question";
        private static final String PAGE_BREAK_TYPE = "Page Break";
        
        private List<SurveyPage> pages = new ArrayList<>();
        
        /**
         * Parse the block element json object
         * 
         * @param jsonArray contains block elements from the qsf file
         * @param failedItems collection of messages about Qualtrics survey items and attributes that couldn't be imported.
         */
        public BlockElement(JSONObject blockObj, List<String> failedItems){
            
            if(blockObj == null){
                throw new IllegalArgumentException("The block element JSON Array can't be null.");
            }
            
            parse(blockObj, failedItems);
        }
        
        private void parse(JSONObject blockObj, List<String> failedItems){
                    
            if(!pages.isEmpty()){
                throw new RuntimeException("Found a second question block element.  Only one question block is currently allowed.");
            }
            
            //the first page
            pages.add(new SurveyPage());
            
            parseQuestionBlock((JSONArray) blockObj.get(BLOCK_ELEMENTS_KEY), failedItems);
        }
        
        private void parseQuestionBlock(JSONArray jsonArray, List<String> failedItems){
            
            for(int index = 0; index < jsonArray.size(); index++){
                
                JSONObject jsonObj = (JSONObject) jsonArray.get(index);
                
                String type = (String) jsonObj.get(BLOCK_ELEMENT_TYPE_KEY);
                if(type.equals(QUESTION_TYPE)){
                    //new question                    
                    
                    //just pick a survey element type to use, will be changed to the correct type later
                    TextSurveyElement newElement = new TextSurveyElement(0, 0, new SurveyItemProperties());
                    
                    String questionId = (String) jsonObj.get(QUESTION_ID_KEY);
                    newElement.getProperties().setPropertyValue(SurveyPropertyKeyEnum.TAG, questionId);
                    
                    pages.get(pages.size()-1).getElements().add(newElement);
                    
                    if(jsonObj.containsKey(SKIP_LOGIC_KEY)){
                        failedItems.add("Unable to import skip logic for question with Id "+ questionId);
                    }
                    
                }else if(type.equals(PAGE_BREAK_TYPE)){
                    //new page
                    
                    pages.add(new SurveyPage());
                }
            }
        }
        
        /**
         * Return the GIFT survey pages created that match the structure of the Qualtrics
         * survey being imported.  In addition placeholder GIFT survey items have been added
         * to each page according to how they are ordered in the qsf file.
         * 
         * @return collection of GIFT survey pages with survey items representative of the qsf file.
         */
        public List<SurveyPage> getSurveyPages(){
            return pages;
        }
    }


}
