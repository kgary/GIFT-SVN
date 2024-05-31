/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.AlertType;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.QuestionResponseElementMetadata;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;

/**
 * A widget for displaying a rating scale survey question
 *
 * @author jleonard
 */
public class RatingScaleQuestionWidget extends AbstractSurveyQuestionWidget<RatingScaleSurveyQuestion> {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(RatingScaleQuestionWidget.class.getName());

    /**
     * The default width of the columns when none is specified
     */
    public static final String DEFAULT_SCALE_COLUMN_WIDTH = "50";

    /**
     * The default left margin when none is specified
     */
    public static final String DEFAULT_SCALE_LEFT_MARGIN = "0";

    /** The selected answer */
    private ListOption listOptionAnswer = null;
    
    /** The index of the selected answer */
    private Integer answerIndex = null;

    /** The time the question was answered */
    private Date timeAnswered;

    /** The index of the row containing the options */
    private int optionsRow = 0;
    
    /** The list of answer buttons */
    private List<RadioButton> answerButtons = new ArrayList<RadioButton>();
    
    /** The table containing the answers*/
    private FlexTable containerTable;
    
    /** THe list of event handlers when clicking on the answer tables */
    private List<HandlerRegistration> answerTableHandlers = new ArrayList<HandlerRegistration>();
    
    /** Click handler for disabled radio button */
    private class DisabledRadioButtonClickHandler implements ClickHandler {

        /** The value of the radio button */
        private boolean value;

        /**
         * Constructor
         * 
         * @param value the value of the radio button
         */
        public DisabledRadioButtonClickHandler(boolean value) {

            this.value = value;
        }

        @Override
        public void onClick(ClickEvent event) {

            RadioButton button = (RadioButton) event.getSource();

            button.setValue(value, false);
        }
    }

    /**
     * Constructor, creates a widget for answering a rating scale question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The question to ask
     * @param questionNumber The number of the question on the survey page
     * @param isBeingEdited If this widget is for a question being modified
     * @param isDebug whether the widget should be rendered in debug mode (e.g. color code question choices based on points)
     */
    public RatingScaleQuestionWidget(SurveyProperties surveyProperties, RatingScaleSurveyQuestion surveyQuestion, int questionNumber, boolean isBeingEdited, boolean isDebug) {

        super(surveyProperties, surveyQuestion, questionNumber, isBeingEdited);
        
        logger.info("creating rating scale for "+surveyQuestion.getId()+", text = "+surveyQuestion.getQuestion().getText());

        VerticalPanel container = new VerticalPanel();

        if (!isBeingEdited && getSurveyElement().getQuestion().getReplyOptionSet() == null) {

            container.add(generateSmallErrorPanel("This rating scale question has no responses to select from."));

        } else if (isBeingEdited && getSurveyElement().getQuestion().getReplyOptionSet() == null) {

            container.add(generateSmallErrorPanel("Answer set has not been selected."));

        } else if (!isBeingEdited && getSurveyElement().getQuestion().getReplyOptionSet().getListOptions().isEmpty()) {

            container.add(generateSmallErrorPanel("This rating scale question has no responses to select from."));

        } else if (isBeingEdited && getSurveyElement().getQuestion().getReplyOptionSet().getListOptions().isEmpty()) {

            container.add(generateSmallErrorPanel("Answer set has no options."));

        } else {

            containerTable = new FlexTable();

            String answerGroupName = "answerGroup-" + questionNumber + "-" +surveyQuestion.getId();
            
            String scaleColumnWidth = getSurveyElement().getQuestion().getColumnWidth();
            
            Boolean useCustomAlignment = getSurveyElement().getQuestion().getUseCustomAlignment();
            
            if (scaleColumnWidth == null || !useCustomAlignment) {

                scaleColumnWidth = DEFAULT_SCALE_COLUMN_WIDTH;
            }

            scaleColumnWidth = scaleColumnWidth.concat("px");

            int index = 0;
            
            // apply logic for bar layout that is not dependent on the choice column (index)
            if(getSurveyElement().getQuestion().usesBarLayout()){
                
                /** stores the answer selected for this question and changes the style of the selected answers widget*/
                ClickHandler clickHandler = new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
    
                        Cell clickedCell = containerTable.getCellForEvent(event);
    
                        if (clickedCell.getRowIndex() == optionsRow) {
    
                            for (int cellColumn = 0; cellColumn < containerTable.getCellCount(optionsRow); cellColumn += 1) {
    
                                containerTable.getCellFormatter().setStyleName(optionsRow, cellColumn, SurveyCssStyles.SURVEY_QUESTION_BAR_LAYOUT_CELL_STYLE);
                            }
    
                            ListOption listOption = getSurveyElement().getQuestion().getReplyOptionSet().getListOptions().get(clickedCell.getCellIndex());
                            setAnswer(clickedCell.getCellIndex(), listOption);
    
                            containerTable.getCellFormatter().setStyleName(
                                    clickedCell.getRowIndex(),
                                    clickedCell.getCellIndex(),
                                    SurveyCssStyles.SURVEY_QUESTION_BAR_LAYOUT_CELL_SELECTED_STYLE);
    
                        }
                    }
                };
             
                answerTableHandlers.add(containerTable.addClickHandler(clickHandler));                

                containerTable.addStyleName(SurveyCssStyles.SURVEY_QUESTION_BAR_LAYOUT_STYLE);

                containerTable.getRowFormatter().addStyleName(0, SurveyCssStyles.SURVEY_QUESTION_BAR_LAYOUT_OPTION_ROW_STYLE);

            }

            for (final ListOption listOption : getSurveyElement().getQuestion().getReplyOptionSet().getListOptions()) {

                if (!getSurveyElement().getQuestion().hideReplyOptionLabels()) {

                    containerTable.setText(0, index, listOption.getText());
                } else {
                    containerTable.getCellFormatter().setHeight(0, index, "20px");
                }

                containerTable.getCellFormatter().setHorizontalAlignment(0, index, HasHorizontalAlignment.ALIGN_CENTER);
                    
                final Integer finalIndex = index;

                if (getSurveyElement().getQuestion().usesBarLayout()) {

                    containerTable.getCellFormatter().addStyleName(0, index, SurveyCssStyles.SURVEY_QUESTION_BAR_LAYOUT_CELL_STYLE);

                } else {

                    RadioButton answerButton = new RadioButton(answerGroupName);
                    answerButton.setStyleName("btn");
                    
                    answerButton.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {

                            setAnswer(finalIndex, listOption);
                        }
                    });
                    
                    answerButtons.add(answerButton);

                    containerTable.setWidget(1, index, answerButton);
                    // add symmetrical space on either side of the column label so the radio button falls below the center of the text and that
                    // each column label is separated from the next column label
                    containerTable.getCellFormatter().getElement(0, index).getStyle().setPaddingRight(5, Unit.PX);
                    containerTable.getCellFormatter().getElement(0, index).getStyle().setPaddingLeft(5, Unit.PX);
                    containerTable.getCellFormatter().setHorizontalAlignment(1, index, HasHorizontalAlignment.ALIGN_CENTER);
                }

                containerTable.getColumnFormatter().setWidth(index, scaleColumnWidth);
                index += 1;
            }

            if (getSurveyElement().getQuestion().getScaleImageUri() != null) {

                containerTable.insertRow(0);
                Image scaleImage = generateScaleImage(containerTable);

                containerTable.setWidget(0, 0, scaleImage);

                containerTable.getCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);

                containerTable.getFlexCellFormatter().setColSpan(0, 0, containerTable.getCellCount(1));

                optionsRow = 1;
            }

            if (getSurveyElement().getQuestion().displayScaleLabels()) {

                int labelRow = containerTable.getRowCount();

                containerTable.insertRow(labelRow);

                containerTable.getFlexCellFormatter().setColSpan(labelRow, 0, containerTable.getCellCount(optionsRow));

                FlexTable labelTable = new FlexTable();

                labelTable.setWidth("100%");

                if (getSurveyElement().getQuestion().getLeftExtremeLabel() != null) {

                    labelTable.setWidget(0, 0, new Label(getSurveyElement().getQuestion().getLeftExtremeLabel()));

                } else if (isBeingEdited) {

                    labelTable.setWidget(0, 0, generateSmallErrorPanel("Left Extreme Label has not been set."));

                } else {

                    throw new NullPointerException("An extreme left label has not been set.");
                }
                
                int columnIndex = 1;
                
                List<String> midScaleLabels = getSurveyElement().getQuestion().getMidScaleLabels();
                
                if(midScaleLabels != null && !midScaleLabels.isEmpty()) {

                    for (String midScaleLabel : midScaleLabels) {

                        labelTable.setWidget(0, columnIndex, new Label(midScaleLabel));
                        labelTable.getCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_CENTER);
                        
                         columnIndex += 1;
                    }
                }

                if (getSurveyElement().getQuestion().getRightExtremeLabel() != null) {

                    labelTable.setWidget(0, columnIndex, new Label(getSurveyElement().getQuestion().getRightExtremeLabel()));
                    labelTable.getCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);

                } else if (isBeingEdited) {

                    labelTable.setWidget(0, columnIndex, generateSmallErrorPanel("Right Extreme Label has not been set."));
                    labelTable.getCellFormatter().setHorizontalAlignment(0, columnIndex, HasHorizontalAlignment.ALIGN_RIGHT);

                } else {

                    throw new NullPointerException("An extreme right label has not been set.");
                }
                
                columnIndex += 1;
                
                double percent = (1.0 / columnIndex) * 100.0;
                
                for(int column = 0; column < columnIndex; column += 1) {
                    
                    labelTable.getColumnFormatter().setWidth(column, percent + "%");
                }

                containerTable.setWidget(labelRow, 0, labelTable);
            }

            container.add(containerTable);
            
            String leftMargin = getSurveyElement().getQuestion().getLeftMargin();
            if(leftMargin == null || !useCustomAlignment) {
                leftMargin = DEFAULT_SCALE_LEFT_MARGIN;
            }
            leftMargin = leftMargin.concat("px");
            
            container.getElement().getStyle().setProperty("marginLeft", leftMargin);
            
            //
            // color code the weighted answers for debugging purposes
            //
            if(isDebug &&
                    ((surveyQuestion.getScorerModel() != null && surveyQuestion.getScorerModel().getTotalQuestion()) || surveyProperties.getSurveyType() == SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK)) {

                logger.info("Showing the rating scale question in debug mode.");
                List<Double> weights = surveyQuestion.getReplyWeights();
                double maxWeight = Integer.MIN_VALUE, minWeight = Integer.MAX_VALUE;
                if(weights != null){
                    for(Double weight : weights){
                        
                        if(weight > maxWeight){
                            maxWeight = weight;
                        }
                        
                        if(weight < minWeight){
                            minWeight = weight;
                        }
                    }
                    
                    if(answerButtons.isEmpty()){
                        
                        for(int cellIndex = 0; cellIndex < containerTable.getCellCount(0); cellIndex++){
                            
                            Element element = containerTable.getCellFormatter().getElement(0, cellIndex);
                            if (element != null) {
                                
                                if (weights.get(cellIndex) == maxWeight) {
                                    element.getStyle().setProperty("background-color", "lightgreen");
        
                                } else if(weights.get(cellIndex) == minWeight){
                                    element.getStyle().setProperty("background-color", "lightcoral");
                                    
                                }else{ 
                                    element.getStyle().setProperty("background-color", "lightblue");
                                }
                            }
                        }
                    }else{
                        for(int buttonIndex = 0; buttonIndex < answerButtons.size(); buttonIndex++){
                            RadioButton radioButton = answerButtons.get(buttonIndex);
                                
                            if (radioButton != null) {
                                
                                if (weights.get(buttonIndex) == maxWeight) {
                                    radioButton.getElement().getStyle().setProperty("background-color", "lightgreen");
        
                                } else if(weights.get(buttonIndex) == minWeight){
                                    radioButton.getElement().getStyle().setProperty("background-color", "lightcoral");
                                    
                                }else{ 
                                    radioButton.getElement().getStyle().setProperty("background-color", "lightblue");
                                }
                            }
                        }
                    }
                }
            }
            

        }

        this.addAnswerArea(container);
    }

    /**
     * Constructor, creates a widget for reviewing the response to a rating
     * scale question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The question to ask
     * @param questionNumber The number of the question on the survey page
     * @param responseMetadata The metadata of the response to the question
     */
    public RatingScaleQuestionWidget(SurveyProperties surveyProperties, RatingScaleSurveyQuestion surveyQuestion, int questionNumber, AbstractQuestionResponseMetadata responseMetadata) {
        this(surveyProperties, surveyQuestion, questionNumber, true, false);
        
        //calculate values for use when determining which response scoring image to use
        List<Double> weights = surveyQuestion.getReplyWeights();
        double possiblePoints = surveyQuestion.getHighestPossibleScore();
        double maxWeight = Integer.MIN_VALUE, minWeight = Integer.MAX_VALUE, pointsEarned = 0;
        if(weights != null){
            for(Double weight : weights){
                
                if(weight > maxWeight){
                    maxWeight = weight;
                }
                
                if(weight < minWeight){
                    minWeight = weight;
                }
            }
        }
        
        boolean showPoints = canShowPointsEarned();
        for (QuestionResponseElementMetadata responseElementMetadata : responseMetadata.getResponses()) {

            for (int index = 0; index < surveyQuestion.getQuestion().getReplyOptionSet().getListOptions().size(); index++) {

                if (responseElementMetadata.getColumnIndex() != null && responseElementMetadata.getColumnIndex() == index) {
                    
                    double weight = 0;
                    if(weights != null){
                        weight = weights.get(index); 
                        pointsEarned += weight;
                    }

                    if(!answerButtons.isEmpty()) {
                        //using radio buttons, not bar layout
                        
                        RadioButton radioButton = answerButtons.get(index);
                        if(!showPoints || possiblePoints <= 0){
                            //when not showing points we need to show the choices made for this question -or-
                            //unable to receive positive points, don't show images, just show the selected answer
                            radioButton.setValue(true, false);
                        }else{
                            if(weight == maxWeight){
                                replaceWidget(1, index, createMaxWeightImage());
                            }else if(weight == minWeight){
                                 replaceWidget(1, index, createMinWeightImage());
                            }else{
                                replaceWidget(1, index, createMidWeightImage());
                            }
                        }

                    } else {
                        // The Rating Scale Question uses a cell table, set the selected cell
                        containerTable.getCellFormatter().setStyleName(optionsRow, index, SurveyCssStyles.SURVEY_QUESTION_BAR_LAYOUT_CELL_SELECTED_STYLE);
                    }                   

                }

            }

        }

        int id = 0;

        for (RadioButton answerOption : answerButtons) {

            answerOption.setName("button-" + questionNumber + "-" + id);

            answerOption.addClickHandler(new DisabledRadioButtonClickHandler(answerOption.getValue()));

            id += 1;
        }
        
        for(HandlerRegistration registration : answerTableHandlers) {
            // Detach table click handlers that were added at the start of this method to prevent the user from changing their answer in the review
            registration.removeHandler();
        }
        
        if(showPoints && possiblePoints > 0){
            //don't bother calculating the possible points if we aren't going to show them
            //show a label with the points earned and the total possible points
            //only show if it is possible to earn positive points on this question
            
            boolean allowsPartialCredit = surveyQuestion.getAllowsPartialCredit();
            
            // flag used to determine if positive points were erased which then can be used to alter the label shown to the user
            // Decided that removing negative points didn't warrant a change in the label.
            boolean erasedPositivePts = false;
            
            // if partial credit is not allowed and the highest points where not earned, set points to zero
            if(!allowsPartialCredit && pointsEarned != 0 && pointsEarned < possiblePoints) {
                logger.info("Setting earned points to 0 for "+surveyQuestion);
                erasedPositivePts = pointsEarned > 0;
                pointsEarned = 0;
            }
            
            Alert earnedLabel = new Alert("You earned "+pointsEarned+" of "+possiblePoints+" possible points "+ (erasedPositivePts ? "(No partial credit)" : "") + ".");
            earnedLabel.getElement().getStyle().setProperty("boxShadow", "2px 2px 7px rgba(0,0,0,0.4)");
            earnedLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
            earnedLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
            earnedLabel.setType(AlertType.SUCCESS);
            addEarnedPointsArea(earnedLabel);
        }
    }
    
    /**
     * Replace the radio button widget with the image widget provided.  The image should be
     * representative of the scoring assessment for that choice.
     * 
     * @param rowIndex the row index of the radio button widget to replace
     * @param columnIndex the column index of the radio button widget to replace
     * @param widget the image widget to set at the given row and column index, where the radio button current is at
     */
    private void replaceWidget(int rowIndex, int columnIndex, Widget widget){        
        containerTable.setWidget(rowIndex, columnIndex, widget);
    }

    /**
     * Generates the image for the scale
     *
     * @return Image The image for the question
     */
    private Image generateScaleImage(final Widget container) {

        final Image scaleImage = new Image(getSurveyElement().getQuestion().getScaleImageUri());

        scaleImage.addLoadHandler(new LoadHandler() {
            @Override
            public void onLoad(LoadEvent event) {

                float widthPercentage = (getSurveyElement().getQuestion().getScaleImageWidth() / 100f);

                if (widthPercentage == 0) {

                    widthPercentage = 1f;
                }

                scaleImage.getElement().setAttribute("width", (scaleImage.getWidth() * widthPercentage) + "px");
                container.setWidth(scaleImage.getWidth() + "px");
                scaleImage.getElement().setAttribute("height", (widthPercentage * 100) + "%");
            }
        });

        return scaleImage;
    }

    /**
     * Sets the answer for the rating scale
     * @param index the index of the selected answer
     * @param answer the ListOption selected
     */
    private void setAnswer(Integer index, ListOption answer) {
        timeAnswered = new Date();
        answerIndex = index;
        listOptionAnswer = answer;
    }

    @Override
    public AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException {

        if (listOptionAnswer != null) {

            return AbstractQuestionResponse.createResponse(
                    getSurveyElement(), Collections.singletonList(
                            new QuestionResponseElement(
                                answerIndex,
                                listOptionAnswer,
                                getSurveyElement().getQuestion().getReplyOptionSet(),
                                timeAnswered)));
        }

        throw new MalformedAnswerException("No option selected", getSurveyElement().getIsRequired());
    }

    @Override
    public void setExternalQuestionResponse(AbstractQuestionResponse questionResponse) {

        //currently not supported
        throw new DetailedException("Unable to apply an external question response to a slider question type.", "This logic has not been implemented yet.", null);
    }
}
