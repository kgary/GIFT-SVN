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
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.base.TextBoxBase;
import org.gwtbootstrap3.client.ui.constants.AlertType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.enums.SurveyResponseTypeEnum;
import mil.arl.gift.common.gwt.client.BrowserConsistentTextArea;
import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.FreeResponseReplyWeights;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.QuestionResponseElementMetadata;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * A widget for a free response survey question
 *
 * @author jleonard
 */
public class FreeResponseQuestionWidget extends AbstractSurveyQuestionWidget<FillInTheBlankSurveyQuestion> {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(FreeResponseQuestionWidget.class.getName());
    
    /** List of flow panels that contain the response field and any response icons */
    private final List<FlowPanel> optionDetails = new ArrayList<FlowPanel>();

    /** List of response text box fields */
    private List<TextBoxBase> multiResponseAnswerArea = new ArrayList<TextBoxBase>();

    /** The list of date-times that the questions were answered.  */
    private final List<Date> dateTimeAnswered = new ArrayList<Date>();

    /**
     * Constructor, creates a widget for answering a free response question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The survey question to ask
     * @param questionNumber The number of the question on the page
     * @param isBeingEdited If this widget is for a question being modified
     */
    public FreeResponseQuestionWidget(SurveyProperties surveyProperties, FillInTheBlankSurveyQuestion surveyQuestion, int questionNumber,
            boolean isBeingEdited) {
        super(surveyProperties, surveyQuestion, questionNumber, isBeingEdited);

        Widget answerArea;
        if (surveyQuestion.getQuestion().isAnswerFieldTextBox()) {
            answerArea = generateAnswerArea(surveyQuestion);

        } else {
            BrowserConsistentTextArea textArea = new BrowserConsistentTextArea();
            textArea.addChangeHandler(new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {

                    if (dateTimeAnswered.isEmpty()) {
                        dateTimeAnswered.add(new Date());
                    } else {
                        dateTimeAnswered.set(0, new Date());
                    }
                }
            });

            multiResponseAnswerArea.add(textArea);
            answerArea = textArea;
        }

        answerArea.setWidth("90%");
        addAnswerArea(answerArea);
    }

    /**
     * Constructor, creates a widget for reviewing the response to a free response question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The survey question to ask
     * @param questionNumber The number of the question on the page
     * @param response The response to this question
     */
    public FreeResponseQuestionWidget(SurveyProperties surveyProperties, FillInTheBlankSurveyQuestion surveyQuestion, int questionNumber,
            AbstractQuestionResponseMetadata response) {
        this(surveyProperties, surveyQuestion, questionNumber, true);

        for (TextBoxBase answerArea : multiResponseAnswerArea) {
            answerArea.setReadOnly(true);
        }

        boolean showPoints = canShowPointsEarned();
        boolean hasCorrectAnswer = surveyQuestion.getQuestion().getProperties().hasProperty(SurveyPropertyKeyEnum.CORRECT_ANSWER);
        double totalPointsEarned = 0; 
        if (!response.getResponses().isEmpty()) {

            if (response.getResponses().size() != multiResponseAnswerArea.size()) {
                GWT.log("Warning: Got a different number of responses than was expected. Received: " + response.getResponses().size() + "; Expected: "
                        + multiResponseAnswerArea.size());
            }

            for (int i = 0; i < response.getResponses().size(); i++) {
                // more responses than text fields
                if ((i + 1) > multiResponseAnswerArea.size()) {
                    break;
                }

                multiResponseAnswerArea.get(i).setText(response.getResponses().get(i).getText());
            }

            // this boolean will only be true for Highlight or Summarize passages which only contain
            // 1 text field.
            if (hasCorrectAnswer) {
                VerticalPanel idealAnswerPanel = new VerticalPanel();
                idealAnswerPanel.setWidth("90%");

                Label idealAnswerLabel = new Label("Ideal Answer:");
                idealAnswerLabel.getElement().getStyle().setProperty("margin-top", "5px");
                idealAnswerLabel.getElement().getStyle().setProperty("padding", "5px");
                idealAnswerPanel.add(idealAnswerLabel);

                HTMLPanel correctAnswerPanel = new HTMLPanel(
                        ((String) surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.CORRECT_ANSWER)));
                idealAnswerPanel.add(correctAnswerPanel);

                addAnswerArea(idealAnswerPanel);

                correctAnswerPanel.setStyleName(SurveyCssStyles.SURVEY_QUESTION_ANSWER_STYLE);
            }
            // Only free response text has weights. Essay does not.
            else if (showPoints && surveyQuestion.getReplyWeights() != null){
                
                FreeResponseReplyWeights weights = surveyQuestion.getReplyWeights();
                for (int responseIndex = 0; responseIndex < response.getResponses().size(); responseIndex++) {

                    // more responses than scores
                    if ((responseIndex + 1) > weights.getReplyWeights().size()) {
                        break;
                    }

                    double minPointsForField = weights.getMinPointsForResponseField(responseIndex);
                    double maxPointsForField = weights.getMaxPointsForResponseField(responseIndex);

                    QuestionResponseElementMetadata resp = response.getResponses().get(responseIndex);
                    Double responseEarnedPoints = getEarnedPointsForResponseField(weights, responseIndex, resp.getText());
                    totalPointsEarned += responseEarnedPoints == null ? 0 : responseEarnedPoints.doubleValue();

                    if (responseEarnedPoints != null && optionDetails.get(responseIndex) != null) {
                        FlowPanel flowPanel = optionDetails.get(responseIndex);
                        Image image;
                        if (responseEarnedPoints == maxPointsForField) {
                            image = createMaxWeightImage();
                        } else if (responseEarnedPoints == minPointsForField) {
                            image = createMinWeightImage();
                        } else {
                            image = createMidWeightImage();
                        }
                        image.getElement().getStyle().setMarginLeft(-4, Style.Unit.PX);
                        flowPanel.add(image);
                    }
                }
            }
        }
        
        if (showPoints && !hasCorrectAnswer && surveyQuestion.getReplyWeights() != null) {
            //don't bother calculating the possible points if we aren't going to show them
            double possiblePoints = surveyQuestion.getHighestPossibleScore();
            if (possiblePoints > 0) {
                // show a label with the points earned and the total possible points
                // only show if it is possible to earn positive points on this question
                
                boolean allowsPartialCredit = surveyQuestion.getAllowsPartialCredit();
                
                // flag used to determine if positive points were erased which then can be used to alter the label shown to the user
                // Decided that removing negative points didn't warrant a change in the label.
                boolean erasedPositivePts = false;
                
                // if partial credit is not allowed and the highest points where not earned, set points to zero
                if(!allowsPartialCredit && totalPointsEarned != 0 && totalPointsEarned < possiblePoints) {
                    logger.info("Setting earned points to 0 for "+surveyQuestion);
                    erasedPositivePts = totalPointsEarned > 0;
                    totalPointsEarned = 0;
                }

                Alert earnedLabel = new Alert("You earned "+totalPointsEarned+" of "+possiblePoints+" possible points "+ (erasedPositivePts ? "(No partial credit)" : "") + ".");
                earnedLabel.getElement().getStyle().setProperty("boxShadow", "2px 2px 7px rgba(0,0,0,0.4)");
                earnedLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                earnedLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
                earnedLabel.setType(AlertType.SUCCESS);
                addEarnedPointsArea(earnedLabel);
            }
        }
    }

    /**
     * Gets the scored points for the response field.
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

    /**
     * Initializes the container that holds the response field widgets. Each line will have
     * "responsesPerLine" number of widgets on it. Each widget will contain a text field and
     * optionally a label.
     * 
     * @param surveyQuestion the survey question object.
     */
    private Widget generateAnswerArea(FillInTheBlankSurveyQuestion surveyQuestion) {
        Integer responsesPerLine = surveyQuestion.getQuestion().getNumberResponseFieldsPerLine();
        List<String> fieldTypes = surveyQuestion.getQuestion().getResponseFieldTypes();
        List<String> responseLabels = surveyQuestion.getQuestion().getResponseFieldLabels();
        List<String> responseLeftAligned = surveyQuestion.getQuestion().getResponseFieldLeftAligned();
        int numberOfResponseFields = fieldTypes.size();
        
        if (responsesPerLine == null || responsesPerLine < 1) {
            // handles legacy courses
            responsesPerLine = 1;
        }

        if (numberOfResponseFields == 0) {
            
            // handles legacy courses
            fieldTypes.add(SurveyResponseTypeEnum.FREE_TEXT.getDisplayName());
            
            // size changed, need to recalculate
            numberOfResponseFields = fieldTypes.size();
        }

        int createdWidgets = 0;

        Container widgetContainer = new Container();
        widgetContainer.setPaddingLeft(0.0);
        widgetContainer.setPaddingRight(0.0);
        
        Label incorrectInputLabel = new Label();
        incorrectInputLabel.setText("You inserted a non-numeric value in a numeric field. It has been removed.");
        incorrectInputLabel.getElement().getStyle().setColor("red");
        incorrectInputLabel.getElement().getStyle().setPaddingLeft(10, Style.Unit.PX);
        incorrectInputLabel.getElement().getStyle().setPaddingBottom(10, Style.Unit.PX);
        incorrectInputLabel.setVisible(false);
        
        widgetContainer.add(incorrectInputLabel);

        // keep looping until we have created all the widgets
        while (numberOfResponseFields > createdWidgets) {
            int numRemaining = numberOfResponseFields - createdWidgets;

            HorizontalPanel row = new HorizontalPanel();
            row.addStyleName(SurveyCssStyles.SURVEY_FREE_RESPONSE_ROW_STYLE);
            row.setWidth("100%");

            // loop for the number of responses that should be on each line
            for (int i = 0; i < responsesPerLine; i++) {

                // reached the number of responses but it didn't fill the whole "responses per
                // line" parameter, so end early.
                if (createdWidgets >= numberOfResponseFields) {
                    break;
                }

                // Calculate column size. If the whole "responses per line" is filled, split by
                // value evenly; otherwise, split by the number remaining evenly.
                int colSize;
                if (numRemaining < responsesPerLine) {
                    colSize = 100 / numRemaining;
                } else {
                    colSize = 100 / responsesPerLine;
                }

                HorizontalPanel responseFieldPanel = new HorizontalPanel();
                responseFieldPanel.setWidth("100%");

                dateTimeAnswered.add(null);
                final int answerTimeIndex = createdWidgets;

                // placeholder for future icons (structured review)
                FlowPanel listOptionDetails = new FlowPanel();
                listOptionDetails.addStyleName(SurveyCssStyles.SURVEY_FREE_RESPONSE_ICON_STYLE);

                optionDetails.add(listOptionDetails);
                
                // response field text box
                final TextBox textBox = new TextBox();
                textBox.getElement().getStyle().setProperty("minWidth", "50px");
                
                if (SurveyResponseTypeEnum.NUMERIC.getDisplayName().equals(fieldTypes.get(createdWidgets))) {
                    addNumericRegex(textBox, incorrectInputLabel);
                    
                    // To make the numeric textbox force the mobile keyboard to the number keyboard
                    // we need to set the attribute type to 'number'. This has some consequences
                    // though, the textboxes will have a number spinner and the letter 'e' seems to
                    // be a valid number character.
                    // textBox.getElement().setAttribute("type", "number");
                }

                textBox.addValueChangeHandler(new ValueChangeHandler<String>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<String> event) {
                        dateTimeAnswered.set(answerTimeIndex, new Date());
                    }
                });

                String label = null;
                if (responseLabels != null && createdWidgets < responseLabels.size()) {
                    label = responseLabels.get(createdWidgets);
                }

                multiResponseAnswerArea.add(textBox);

                boolean leftAligned = false;
                if (responseLeftAligned != null && createdWidgets < responseLeftAligned.size()) {
                    leftAligned = Boolean.parseBoolean(responseLeftAligned.get(createdWidgets));
                }
                
                // if label is not left aligned, add textbox before
                if (!leftAligned) {
                    // always add the 'icon' panel right before the textbox
                    responseFieldPanel.add(listOptionDetails);
                responseFieldPanel.add(textBox);
                responseFieldPanel.setCellWidth(textBox, "100%");
                }

                // if the label exists, add it to the panel
                if (label != null && !label.trim().isEmpty()) {
                    InlineLabel labelWidget = new InlineLabel(label);
                    labelWidget.setWordWrap(false);

                    responseFieldPanel.add(labelWidget);
                    responseFieldPanel.setCellVerticalAlignment(labelWidget, HasVerticalAlignment.ALIGN_MIDDLE);
                }
                
                // if label is left aligned, add textbox after
                if (leftAligned) {
                    // always add the 'icon' panel right before the textbox
                    responseFieldPanel.add(listOptionDetails);
                    responseFieldPanel.add(textBox);
                    responseFieldPanel.setCellWidth(textBox, "100%");
                }

                row.add(responseFieldPanel);
                row.setCellWidth(responseFieldPanel, colSize + "%");

                createdWidgets++;
            }

            widgetContainer.add(row);
        }

        return widgetContainer;
    }

    /**
     * Adds a keydownhandler to the given textbox. This prevents the user from typing alphabetic
     * characters.
     * 
     * @param textBox the text field to add the handler to.
     * @param errorMsgLabel the error message label to display if the user tries entering an invalid character.
     */
    private void addNumericRegex(final TextBox textBox, final Label errorMsgLabel) {
        textBox.addDomHandler(new InputHandler() {

            //the last valid value entered into the text box
            String lastValue = null;

            @Override
            public void onInput(InputEvent event) {

                final String value = textBox.getText();

                if (value != null && !value.isEmpty()) {

                    try {

                        if (value.equals("-")) {
                            // do nothing if they start with a negative sign
                        } else if (value.endsWith(".")
                                && value.indexOf(".") == value.lastIndexOf(".")) {
                            // do nothing if they press the decimal point ONLY if there are
                            // no other decimal points
                        } else if (value.contains(" ")) {
                            // remove space, will be caught be catch block to remove key stroke
                            throw new Exception();
                        } else if (value.contains("f") || value.contains("d")) {
                            // 'f' and 'd' after numbers is a valid double string ("float"
                            // and "double"). Make sure to explicitly remove these characters.
                            // Will be caught be catch block to remove key stroke
                            throw new Exception();
                        } else {
                            // prevent the user from entering values that are invalid
                            Double doubleValue = Double.valueOf(value);
                            
                            // eliminate leading 0's as the user is typing
                            if (value.startsWith("0")) {
                                textBox.setValue(Double.toString(doubleValue));
                            }
                        }
                        
                        errorMsgLabel.setVisible(false);
                        lastValue = value;
                        
                    } catch (@SuppressWarnings("unused") Exception e) {
                        textBox.setText(lastValue);
                        errorMsgLabel.setVisible(true);
                    }
                    
                } else {
                    lastValue = value;
                        }
                    }
        }, InputEvent.getType());
    }

    @Override
    public AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException {

        List<QuestionResponseElement> responses = new ArrayList<QuestionResponseElement>();

        for (int i = 0; i < multiResponseAnswerArea.size(); i++) {
            TextBoxBase textBox = multiResponseAnswerArea.get(i);
            if (textBox.getValue() != null && !textBox.getValue().trim().isEmpty()) {
                responses.add(new QuestionResponseElement(textBox.getValue().trim(), dateTimeAnswered.get(i)));
            } else {
                if (validate && getSurveyElement().getIsRequired()) {
                    throw new MalformedAnswerException("Missing an answer", getSurveyElement().getIsRequired());
                } else if (!validate) {
                    // if we do not care about validating, then add a blank response element for the unanswered response
                    // in order to match up the response to the UI component when viewing the responses to this question
                    // Note: this is not ideal because it infers a response when none was actually given.  Ideally we would
                    //       add null here but that breaks downstream logic right now (codec, scoring, structured review, ERT)
                    responses.add(new QuestionResponseElement(Constants.EMPTY, new Date()));
                }
            }
        }

        if (validate) {
            if (!responses.isEmpty() && multiResponseAnswerArea.size() != responses.size()) {
                throw new MalformedAnswerException("Missing an answer", getSurveyElement().getIsRequired());

            } else if (responses.isEmpty()) {
                throw new MalformedAnswerException("Missing an answer", getSurveyElement().getIsRequired());
            }
        }

        return AbstractQuestionResponse.createResponse(getSurveyElement(), responses);
    }

    @Override
    public void setExternalQuestionResponse(AbstractQuestionResponse questionResponse) {

        // currently not supported
        throw new DetailedException("Unable to apply an external question response to a slider question type.", "This logic has not been implemented yet.", null);
    }
}
