/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.AlertType;

import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.QuestionResponseElementMetadata;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * A widget for displaying a matrix of choices survey question
 *
 * @author jleonard
 */
public class MatrixOfChoicesQuestionWidget extends AbstractSurveyQuestionWidget<MatrixOfChoicesSurveyQuestion> {

    /** The logger. */
    private static Logger logger = Logger.getLogger(MatrixOfChoicesQuestionWidget.class.getName());

    /**
     * The default width of the columns when none is specified
     */
    public static final String DEFAULT_SCALE_COLUMN_WIDTH = "50";

    /**
     * The default width of the left margin when none is specified
     */
    public static final String DEFAULT_SCALE_LEFT_MARGIN = "0";

    /** The matrix of radio buttons for each answer */
    private final List<List<RadioButton>> answerOptions = new ArrayList<List<RadioButton>>();

    /** The matrix of each answer option */
    private Grid answerOptionsGrid;

    /** The list of row options */
    private final List<ListOption> rowOptions;

    /** The list of column options */
    private final List<ListOption> columnOptions;

    /** The list of times each row was answered */
    private final List<Date> timesAnswered = new ArrayList<Date>();

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
     * Constructor, creates a widget for answering a matrix of choices question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The question to ask
     * @param questionNumber The number of the question on the survey page
     * @param isBeingEdited If this widget is for a question being modified
     * @param isDebug whether the widget should be rendered in debug mode (e.g. color code question choices based on points)
     */
    public MatrixOfChoicesQuestionWidget(SurveyProperties surveyProperties, MatrixOfChoicesSurveyQuestion surveyQuestion,
            int questionNumber, boolean isBeingEdited, boolean isDebug) {
        super(surveyProperties, surveyQuestion, questionNumber, isBeingEdited);

        logger.info("Constructing Matrix of Choices question widget.");

        if (!isBeingEdited && getSurveyElement().getQuestion().getRowOptions() == null) {

            addAnswerArea(generateSmallErrorPanel("This matrix of choices question has no row responses to choose from."));
            rowOptions = null;
            columnOptions = null;

        } else if (!isBeingEdited && getSurveyElement().getQuestion().getColumnOptions() == null) {

            addAnswerArea(generateSmallErrorPanel("This matrix of choices question has no column responses to choose from."));
            rowOptions = null;
            columnOptions = null;

        } else if (isBeingEdited && (getSurveyElement().getQuestion().getRowOptions() == null || getSurveyElement().getQuestion().getColumnOptions() == null)) {

            if (getSurveyElement().getQuestion().getRowOptions() == null) {

                addAnswerArea(generateSmallErrorPanel("No option set for the matrix rows have been selected."));
            }

            if (getSurveyElement().getQuestion().getColumnOptions() == null) {

                addAnswerArea(generateSmallErrorPanel("No option set for the matrix columns have been selected."));
            }

            rowOptions = null;
            columnOptions = null;

        } else if (!isBeingEdited && getSurveyElement().getQuestion().getRowOptions().getListOptions().isEmpty()) {

            addAnswerArea(generateSmallErrorPanel("This matrix of choices question has no row responses to choose from."));
            rowOptions = null;
            columnOptions = null;

        } else if (!isBeingEdited && getSurveyElement().getQuestion().getColumnOptions().getListOptions().isEmpty()) {

            addAnswerArea(generateSmallErrorPanel("This matrix of choices question has no column responses to choose from."));
            rowOptions = null;
            columnOptions = null;

        } else if (isBeingEdited && (getSurveyElement().getQuestion().getRowOptions().getListOptions().isEmpty() || getSurveyElement().getQuestion().getColumnOptions().getListOptions().isEmpty())) {

            if (getSurveyElement().getQuestion().getRowOptions().getListOptions().isEmpty()) {

                addAnswerArea(generateSmallErrorPanel("The option set for the matrix rows has no options."));
            }

            if (getSurveyElement().getQuestion().getColumnOptions().getListOptions().isEmpty()) {

                addAnswerArea(generateSmallErrorPanel("The option set for the matrix columns has no columns."));
            }

            rowOptions = null;
            columnOptions = null;

        } else {

            rowOptions = getSurveyElement().getQuestion().getRowOptions().getListOptions();
            columnOptions = getSurveyElement().getQuestion().getColumnOptions().getListOptions();

            answerOptionsGrid = new Grid(getSurveyElement().getQuestion().getRowOptions().getListOptions().size() + 1, getSurveyElement().getQuestion().getColumnOptions().getListOptions().size() + 1);
            answerOptionsGrid.setCellPadding(5);

            String scaleColumnWidth = getSurveyElement().getQuestion().getColumnWidth();

            Boolean useCustomAlignment = getSurveyElement().getQuestion().getUseCustomAlignment();

            if (scaleColumnWidth == null || !useCustomAlignment) {
                scaleColumnWidth = DEFAULT_SCALE_COLUMN_WIDTH;
            }
            scaleColumnWidth = scaleColumnWidth.concat("px");

            int headerColumn = 1;
            for (ListOption columnOption : getSurveyElement().getQuestion().getColumnOptions().getListOptions()) {
                answerOptionsGrid.setText(0, headerColumn, columnOption.getText());
                answerOptionsGrid.getColumnFormatter().setWidth(headerColumn, scaleColumnWidth);
                // add symmetrical space on either side of the column label so the radio button falls below the center of the text and that
                // each column label is separated from the next column label
                answerOptionsGrid.getCellFormatter().getElement(0, headerColumn).getStyle().setPaddingRight(5, Unit.PX);
                answerOptionsGrid.getCellFormatter().getElement(0, headerColumn).getStyle().setPaddingLeft(5, Unit.PX);
                answerOptionsGrid.getCellFormatter().setHorizontalAlignment(0, headerColumn, HasHorizontalAlignment.ALIGN_CENTER);
                headerColumn += 1;
            }

            int row = 1;
            for (ListOption rowOption : getSurveyElement().getQuestion().getRowOptions().getListOptions()) {
                List<RadioButton> rowAnswerOptions = new ArrayList<RadioButton>();
                answerOptions.add(rowAnswerOptions);
                timesAnswered.add(null);
                answerOptionsGrid.setText(row, 0, rowOption.getText());
                String answerGroupName = "answerGroup-" + questionNumber + "-" + surveyQuestion.getId() + "-" + row;

                int column = 1;
                for (int optionIndex = 0; optionIndex < getSurveyElement().getQuestion().getColumnOptions().getListOptions().size(); optionIndex += 1) {
                    RadioButton answerButton = new RadioButton(answerGroupName);
                    answerButton.setStyleName("btn");
                    final int answerTimeIndex = row - 1;
                    answerButton.addClickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent event) {
                            timesAnswered.set(answerTimeIndex, new Date());
                        }
                    });
                    answerOptionsGrid.setWidget(row, column, answerButton);
                    rowAnswerOptions.add(answerButton);
                    answerOptionsGrid.getCellFormatter().setHorizontalAlignment(row, column, HasHorizontalAlignment.ALIGN_CENTER);
                    column += 1;
                }
                row += 1;
            }

            String leftMargin = getSurveyElement().getQuestion().getLeftMargin();
            if(leftMargin == null || !useCustomAlignment) {
                leftMargin = DEFAULT_SCALE_LEFT_MARGIN;
            }
            leftMargin = leftMargin.concat("px");

            answerOptionsGrid.getElement().getStyle().setProperty("marginLeft", leftMargin);

            //
            // color code the weighted answers for debugging purposes
            //
            if(isDebug &&
                    ((surveyQuestion.getScorerModel() != null && surveyQuestion.getScorerModel().getTotalQuestion()) || surveyProperties.getSurveyType() == SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK)){
                logger.info("Showing the matrix of choices question in debug mode.");
                MatrixOfChoicesReplyWeights weights = surveyQuestion.getReplyWeights();
                double maxRowPoint, minRowPoint;

                if (weights != null) {

                    try{
                        for (int rowIndex = 0; rowIndex < surveyQuestion.getQuestion().getRowOptions().getListOptions().size(); rowIndex++) {

                            //needed to determine the appropriate response scoring image to use
                            maxRowPoint = weights.getMaxPointsForRow(rowIndex);
                            minRowPoint = weights.getMinPointsForRow(rowIndex);

                            for (int columnIndex = 0; columnIndex < surveyQuestion.getQuestion().getColumnOptions().getListOptions().size(); columnIndex++) {

                                double answerWeight = weights.getReplyWeight(rowIndex, columnIndex);
                                logger.info("matrix ["+rowIndex+","+columnIndex+"]:  answerWeight = "+answerWeight+", maxRowPoint = "+maxRowPoint+", minRowPoint = "+minRowPoint);
                                if(answerWeight == maxRowPoint){
                                    answerOptionsGrid.getWidget(rowIndex+1, columnIndex+1).getElement().getStyle().setProperty("background-color", "lightgreen");
                                }else if(answerWeight == minRowPoint){
                                    answerOptionsGrid.getWidget(rowIndex+1, columnIndex+1).getElement().getStyle().setProperty("background-color", "lightcoral");
                                }else{
                                    answerOptionsGrid.getWidget(rowIndex+1, columnIndex+1).getElement().getStyle().setProperty("background-color", "lightblue");
                                }

                            }

                        }
                    }catch(Exception e){
                        logger.log(Level.WARNING,
                                "Failed to color code the weighted answers for question:"+surveyQuestion.getQuestion().getText(),
                                e);
                    }

                }
            }

            this.addAnswerArea(answerOptionsGrid);
        }
    }

    /**
     * Constructor, creates a widget for reviewing the response to a matrix of
     * choices question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The question to ask
     * @param questionNumber The number of the question on the survey page
     * @param responseMetadata The metadata of the response to this question
     */
    public MatrixOfChoicesQuestionWidget(SurveyProperties surveyProperties, MatrixOfChoicesSurveyQuestion surveyQuestion, int questionNumber, AbstractQuestionResponseMetadata responseMetadata) {
        this(surveyProperties, surveyQuestion, questionNumber, true, false);

        MatrixOfChoicesReplyWeights weights = surveyQuestion.getReplyWeights();
        double possiblePoints = surveyQuestion.getHighestPossibleScore();
        double maxRowPoint, minRowPoint, pointsEarned = 0;
        boolean showPoints = canShowPointsEarned();

        if(responseMetadata.getResponses() != null){
            logger.info("Constructing Matrix of Choices question widget with "+responseMetadata.getResponses().size()+" responses.  Possible points = "+possiblePoints);

            for (QuestionResponseElementMetadata responseElementMetadata : responseMetadata.getResponses()) {

                logger.info("looking at response "+responseElementMetadata);

                for (int rowIndex = 0; rowIndex < surveyQuestion.getQuestion().getRowOptions().getListOptions().size(); rowIndex++) {

                    //needed to determine the appropriate response scoring image to use
                    maxRowPoint = weights.getMaxPointsForRow(rowIndex);
                    minRowPoint = weights.getMinPointsForRow(rowIndex);

                    for (int columnIndex = 0; columnIndex < surveyQuestion.getQuestion().getColumnOptions().getListOptions().size(); columnIndex++) {
                        if (responseElementMetadata.getRowIndex() == rowIndex && responseElementMetadata.getColumnIndex() == columnIndex) {

                            if(!showPoints || possiblePoints <= 0){
                                //when not showing points we need to show the choices made for this question -or-
                                //unable to receive positive points on this question, don't replace the response UI element with an image
                                answerOptions.get(rowIndex).get(columnIndex).setValue(true, false);
                            }else{
                                double answerWeight = weights.getReplyWeight(rowIndex, columnIndex);
                                pointsEarned += answerWeight;
                                logger.info("matrix ["+rowIndex+","+columnIndex+"]:  answerWeight = "+answerWeight+", maxRowPoint = "+maxRowPoint+", minRowPoint = "+minRowPoint);
                                if(answerWeight == maxRowPoint){
                                    replaceWidget(rowIndex+1, columnIndex+1, createMaxWeightImage());
                                }else if(answerWeight == minRowPoint){
                                    replaceWidget(rowIndex+1, columnIndex+1, createMinWeightImage());
                                }else{
                                    replaceWidget(rowIndex+1, columnIndex+1, createMidWeightImage());
                                }

                            }
                        }
                    }
                }
            }
        }else{
            logger.info("Constructing Matrix of Choices question widget with NO responses.");
        }

        int id = 0;

        for (List<RadioButton> buttonRow : answerOptions) {

            for (RadioButton rowOption : buttonRow) {

                rowOption.setName("button-" + questionNumber + "-" + id);

                rowOption.addClickHandler(new DisabledRadioButtonClickHandler(rowOption.getValue()));

                id += 1;
            }
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
     * Replace the radio button widget with the image widget provided. The image
     * should be representative of the scoring assessment for that choice.
     *
     * @param rowIndex the row index of the radio button widget to replace
     * @param columnIndex the column index of the radio button widget to replace
     * @param newWidget the image widget to set at the given row and column
     *        index, where the radio button current is at
     */
    private void replaceWidget(int rowIndex, int columnIndex, Widget newWidget){
        answerOptionsGrid.setWidget(rowIndex, columnIndex, newWidget);
    }

    @Override
    public AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException {

        List<QuestionResponseElement> responses = new ArrayList<QuestionResponseElement>();

        for (List<RadioButton> rowAnswerOptions : answerOptions) {
            boolean answered = false;
            for (RadioButton i : rowAnswerOptions) {
                if (i.getValue()) {
                    answered = true;
                    int rowSelected = answerOptions.indexOf(rowAnswerOptions);
                    int columnSelected = rowAnswerOptions.indexOf(i);
                    responses.add(new QuestionResponseElement(
                            columnSelected,
                            columnOptions.get(columnSelected),
                            getSurveyElement().getQuestion().getColumnOptions(),
                            rowSelected,
                            rowOptions.get(rowSelected),
                            getSurveyElement().getQuestion().getRowOptions(),
                            timesAnswered.get(rowSelected)));
                }
            }
            if (!answered && getSurveyElement().getIsRequired()) {

                throw new MalformedAnswerException("Missing an answer", getSurveyElement().getIsRequired());
            }
        }

        if(!responses.isEmpty() && answerOptions.size() != responses.size()) {

            throw new MalformedAnswerException("Missing an answer", true);

        } else if (responses.isEmpty()) {

            throw new MalformedAnswerException("Missing an answer", getSurveyElement().getIsRequired());
        }

        return AbstractQuestionResponse.createResponse(getSurveyElement(), responses);
    }

    @Override
    public void setExternalQuestionResponse(AbstractQuestionResponse questionResponse) {

        //currently not supported
        throw new DetailedException("Unable to apply an external question response to a slider question type.", "This logic has not been implemented yet.", null);
    }
}
