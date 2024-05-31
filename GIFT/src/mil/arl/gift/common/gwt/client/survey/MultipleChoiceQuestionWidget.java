/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.RadioButton;
import org.gwtbootstrap3.client.ui.constants.AlertType;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MultipleChoiceQuestionResponse;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.QuestionResponseElementMetadata;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * A widget for display multiple choice survey questions
 *
 * @author jleonard
 */
public class MultipleChoiceQuestionWidget extends AbstractSurveyQuestionWidget<MultipleChoiceSurveyQuestion> {

    /** The logger. */
    private static Logger logger = Logger.getLogger(MultipleChoiceQuestionWidget.class.getName());

    /** the choices shown to the learner (matches the {@link #listOptions}) */
    private final ArrayList<CheckBox> answerOptions;

    /** the choices shown to the learner (could be randomized order from the authored choices) */
    private final List<ListOption> listOptions;
    
    /** a map containing the reordered indexes of each question response. Used for recording the correct score */
    private final int[] listOptionsIndexMap;

    /** the container panel with the multiple choices */
    private final FlowPanel containerPanel;

    /** the list of panels containing scoring details for each option */
    private final List<FlowPanel> optionDetails = new ArrayList<FlowPanel>();

    /** the time the question was answered */
    private Date timeAnswered;

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

            CheckBox button = (CheckBox) event.getSource();

            button.setValue(value, false);
        }
    }

    /**
     * Constructor, creates a widget for answering a multiple choice question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param question The question to ask
     * @param questionNumber The number of the question on the page
     * @param isBeingEdited If this widget is for a question being modified
     * @param isAnswered If the user has already submitted an answer for this question.
     * @param isDebug whether the widget should be rendered in debug mode (e.g. color code question choices based on points)
     */
    public MultipleChoiceQuestionWidget(SurveyProperties surveyProperties, MultipleChoiceSurveyQuestion question, int questionNumber, boolean isBeingEdited, boolean isAnswered, boolean isDebug) {
        this(surveyProperties, question, questionNumber, isBeingEdited, isAnswered, isDebug, null);
    }

    /**
     * Constructor, creates a widget for answering a multiple choice question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param question The question to ask
     * @param questionNumber The number of the question on the page
     * @param isBeingEdited If this widget is for a question being modified
     * @param isAnswered If the user has already submitted an answer for this question.
     * @param isDebug whether the widget should be rendered in debug mode (e.g. color code question choices based on points)
     * @param optionOrder The order that the question's list options should be presented in
     */
    public MultipleChoiceQuestionWidget(SurveyProperties surveyProperties, MultipleChoiceSurveyQuestion question, int questionNumber, boolean isBeingEdited, boolean isAnswered, boolean isDebug, final List<Integer> optionOrder) {
        super(surveyProperties, question, questionNumber, isBeingEdited);

        if (!isSingleSelect() && getSurveyElement().getQuestion().getMinimumSelectionsRequired() == null) {

            timeAnswered = new Date();
        }

        containerPanel = new FlowPanel();

        OptionList optionSet = getSurveyElement().getQuestion().getReplyOptionSet();
        if (!isBeingEdited && optionSet == null) {
            logger.info("Found MC question with no responses to choose from.");
            containerPanel.add(generateSmallErrorPanel("This multiple choice question has no responses to choose from."));

            answerOptions = new ArrayList<CheckBox>(0);
            listOptions = new ArrayList<ListOption>(0);
            listOptionsIndexMap = new int[0];

        } else if (isBeingEdited && optionSet == null) {
            logger.info("Found MC question with no choice set.");
            containerPanel.add(generateSmallErrorPanel("Answer set has not been selected."));

            answerOptions = new ArrayList<CheckBox>(0);
            listOptions = new ArrayList<ListOption>(0);
            listOptionsIndexMap = new int[0];

        } else if(!isBeingEdited && optionSet.getListOptions().isEmpty()) {
            logger.info("Found MC question with no choices.");
            containerPanel.add(generateSmallErrorPanel("This multiple choice question has no responses to choose from."));

            answerOptions = new ArrayList<CheckBox>(0);
            listOptions = new ArrayList<ListOption>(0);
            listOptionsIndexMap = new int[0];

        } else if (isBeingEdited && optionSet.getListOptions().isEmpty()) {
            logger.info("Found MC question with no choices.");
            containerPanel.add(generateSmallErrorPanel("Answer set has no options."));

            answerOptions = new ArrayList<CheckBox>(0);
            listOptions = new ArrayList<ListOption>(0);
            listOptionsIndexMap = new int[0];

        } else {
            logger.info("Found MC question with no choices.");
            answerOptions = new ArrayList<CheckBox>(optionSet.getListOptions().size());
            listOptionsIndexMap = new int[optionSet.getListOptions().size()];
            for (int i =0;i<optionSet.getListOptions().size(); i++) {
                listOptionsIndexMap[i] = i;
            }

            String answerGroupName = "answerGroup-" + questionNumber + "-" + question.getId();


            listOptions = new ArrayList<ListOption>(optionSet.getListOptions());
            List<Double> weights = getSurveyElement().getReplyWeights() != null
                    ? new ArrayList<>(getSurveyElement().getReplyWeights())
                    : null;

            if(optionOrder != null) {
                logger.info("Applying choice ordering...");
                //save the original order of the options to the map before sorting the options
                for (int i=0;i<optionOrder.size(); i++) {
                    listOptionsIndexMap[optionOrder.indexOf(listOptions.get(i).getId())] = i;
                }

                //if list options should be listed in a particular order, sort them by that order
                Collections.sort(listOptions, new Comparator<ListOption>() {

                    @Override
                    public int compare(ListOption o1, ListOption o2) {
                        return Integer.compare(optionOrder.indexOf(o1.getId()), optionOrder.indexOf(o2.getId()));
                    }
                });
            }

            if(!isAnswered && getSurveyElement().getRandomizeReplyOptions()) {
                logger.info("Applying randomization to MC question choices.");
                for(int index = 0; index < listOptions.size(); index += 1) {

                    int randomIndex = Random.nextInt(listOptions.size());
                    Collections.swap(listOptions, index, randomIndex);

                    // swap indexes in map
                    int temp = listOptionsIndexMap[index];
                    listOptionsIndexMap[index] = listOptionsIndexMap[randomIndex];
                    listOptionsIndexMap[randomIndex] = temp;

                    if (weights != null) {
                        Collections.swap(weights, index, randomIndex);
                    }
                }
            }

            for (ListOption i : listOptions) {

                CheckBox answerButton;
                if (isSingleSelect()) {

                    answerButton = new RadioButton(answerGroupName, i.getText());

                } else {

                    answerButton = new CheckBox(i.getText());
                }

                answerButton.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
                answerButton.getElement().getStyle().setProperty("padding", "6px 12px");
                answerButton.getElement().getStyle().setProperty("margin", "0px");
                answerButton.getElement().getStyle().setTextAlign(TextAlign.LEFT);

                answerButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {

                    @Override
                    public void onValueChange(ValueChangeEvent<Boolean> event) {

                        if(event.getValue()){

                            //notify tutor server
                            try{
                                logger.info("Creating notification that the learner selected a multiple choice question choice on the TUI.");
                                answeredQuestion(); //make sure the time answered value is populated the first time (for checkbox ValueChangeHandler gets called before ClickHandler)
                                AbstractQuestionResponse response = getAnswer(true);
                                notifyCurrentQuestionAnswered(response);
                            }catch(Exception e){
                                logger.log(Level.WARNING, "Failed to notify the tutor server of an intermediate survey question response "+
                                        "because an exception was thrown while trying to get the question response information. The multiple choice survey question id is "+getSurveyElement().getId(), e);
                            }
                        }
                    }
                });

                answerButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        answeredQuestion();
                    }
                });

                answerOptions.add(answerButton);

                FlowPanel listOptionDetails = new FlowPanel();

                listOptionDetails.getElement().getStyle().setDisplay(Style.Display.INLINE_BLOCK);

                if (question.getScorerModel() != null && question.getScorerModel().getTotalQuestion()) {

                    listOptionDetails.setSize("16px", "16px");
                }

                optionDetails.add(listOptionDetails);

                HorizontalPanel listOptionPanel = new HorizontalPanel();

                listOptionPanel.add(listOptionDetails);

                listOptionPanel.add(answerButton);

                containerPanel.add(listOptionPanel);
            }

            //
            // color code the weighted answers for debugging purposes
            //
            if(isDebug &&
                    ((question.getScorerModel() != null && question.getScorerModel().getTotalQuestion()) || surveyProperties.getSurveyType() == SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK)){
                logger.info("Showing the multiple choice question in debug mode.");

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
                }

                if (weights != null) {

                    try{
                        for(int index = 0; index < answerOptions.size(); index ++){
                            CheckBox checkbox = answerOptions.get(index);

                            if (checkbox != null) {

                                if (weights.get(index) == maxWeight) {
                                    checkbox.getElement().getStyle().setProperty("background-color", "lightgreen");

                                } else if(weights.get(index) == minWeight){
                                    checkbox.getElement().getStyle().setProperty("background-color", "lightcoral");

                                }else{
                                    checkbox.getElement().getStyle().setProperty("background-color", "lightblue");
                                }
                            }
                        }
                    }catch(Exception e){
                        logger.log(Level.WARNING,
                                "Failed to color code the weighted answers for question:"+question.getQuestionText(),
                                e);
                    }

                }
            }
        }

        this.addAnswerArea(containerPanel);


    }

    /**
     * Constructor, creates a widget for reviewing the response to a multiple
     * choice question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The question to ask
     * @param questionNumber The number of the question on the page
     * @param responseMetadata The metadata of the response to this question
     */
    public MultipleChoiceQuestionWidget(SurveyProperties surveyProperties, MultipleChoiceSurveyQuestion surveyQuestion, int questionNumber, AbstractQuestionResponseMetadata responseMetadata) {
        this(surveyProperties, surveyQuestion, questionNumber, true, true, false, responseMetadata.getOptionOrder());

        List<Double> weights = surveyQuestion.getReplyWeights();
        List<String> feedbacks = surveyQuestion.getReplyFeedbacks();

        FlowPanel feedbackPanel = new FlowPanel();

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
        for (QuestionResponseElementMetadata responseElement : responseMetadata.getResponses()) {
            logger.info("handling next question response element..");
            for (int index=0; index < surveyQuestion.getQuestion().getReplyOptionSet().getListOptions().size(); index++) {

                //find the multiple choice question choice by matching question response index to the question index
                if (responseElement.getColumnIndex() != null && responseElement.getColumnIndex() == index) {

                    logger.info("found question choice index for response at index "+index);
                    //select the choice in the UI component
                	int uiIndex = index;
                	for (int i=0;i<listOptionsIndexMap.length; i++) {
                	    if (listOptionsIndexMap[i] == index) {
                	        uiIndex = i;
                            CheckBox checkbox = answerOptions.get(uiIndex);
                            checkbox.setValue(true, false);
                	        break;
                	    }
                	}

                    if (showPoints && weights != null) {
                        //if there are right/wrong answers, calculate points earned and possibly show images for whether the response was correct

                        FlowPanel checkPanel = optionDetails.get(uiIndex);
                        CheckBox selected = answerOptions.get(uiIndex);
                        if(selected.getValue()){
                           //only show images next to selected answers
                            pointsEarned += weights.get(index);

                            logger.info("A total of "+pointsEarned+" points were earned for this response");
                            if (checkPanel != null) {
                                checkPanel.getElement().getStyle().setProperty("verticalAlign", "top");
                                checkPanel.getElement().getStyle().setProperty("marginTop", "5px");

                                if (weights.get(index) == maxWeight) {
                                    checkPanel.add(createMaxWeightImage());

                                } else if(weights.get(index) == minWeight){
                                    checkPanel.add(createMinWeightImage());

                                }else{
                                    checkPanel.add(createMidWeightImage());
                                }
                            }
                        }

                    }
                    

                    if(feedbacks != null && index < feedbacks.size()){

                        String feedback = feedbacks.get(index);
                        if(feedback != null && !feedback.isEmpty()){

                            logger.info("Adding question choice feedback of '"+feedback+"' to multiple choice question widget.");

                            Alert label = new Alert(feedback);
                            label.getElement().getStyle().setProperty("boxShadow", "2px 2px 7px rgba(0,0,0,0.4)");
                            label.getElement().getStyle().setFontWeight(FontWeight.BOLD);

                            if(feedbackPanel.getWidgetCount() == 0){

                                //add some spacing to the first feedback message
                                label.getElement().getStyle().setMarginTop(10, Unit.PX);
                            }

                            if (weights != null && showPoints) {
                                //if there are right/wrong answers, color the feedback label appropriately

                                if (weights.get(index) > 0) {
                                    label.setType(AlertType.SUCCESS);

                                } else {
                                    label.setType(AlertType.DANGER);
                                }

                            } else {

                                //otherwise, just use the same color for any neutral responses
                                label.setType(AlertType.INFO);
                            }

                            feedbackPanel.add(label);
                        }
                    }
                    // found the answer
                    break;
                }
            }
        }

        if(showPoints){
            logger.info("attempting to add earned points label...");
            //don't bother calculating the possible points if we aren't going to show them
            double possiblePoints = surveyQuestion.getHighestPossibleScore();
            boolean allowsPartialCredit = surveyQuestion.getAllowsPartialCredit();
            if(possiblePoints > 0){
                //show a label with the points earned and the total possible points
                //only show if it is possible to earn positive points on this question
                
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
                feedbackPanel.insert(earnedLabel,0);
                
                logger.info("Earned points label added with text of '"+earnedLabel.getText()+"'.");
            }
        }

        containerPanel.add(feedbackPanel);

        int id = 0;

        for (CheckBox answerOption : answerOptions) {

            boolean value = answerOption.getValue();

            answerOption.setName("button-" + questionNumber + "-" + id);

            answerOption.addClickHandler(new DisabledRadioButtonClickHandler(value));

            answerOption.setValue(value);

            id += 1;
        }
    }

    /**
     * Returns if the multiple choice question is a single select question
     * @return boolean If the multiple choice question is a single select question
     */
    private boolean isSingleSelect() {

        boolean isSingleSelect = false;

        if (getSurveyElement().getQuestion().hasMultiSelectEnabledProperty()) {
            // The new surveys can have the multi select property, so if the multi select property exists, then use
            // that value to determine if the question is single select.
            isSingleSelect = !getSurveyElement().getQuestion().getIsMultiSelectEnabled();
        } else {
            // If the survey doesn't have the multiselect property (old SAS logic), then use the original logic to determine
            // if the survey is single select.
            isSingleSelect = (getSurveyElement().getQuestion().getMinimumSelectionsRequired() == null || getSurveyElement().getQuestion().getMinimumSelectionsRequired() == 1)
            && (getSurveyElement().getQuestion().getMaximumSelectionsAllowed() == null || getSurveyElement().getQuestion().getMaximumSelectionsAllowed() == 1);
        }
        return isSingleSelect;
    }

    @Override
    public AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException {

        List<QuestionResponseElement> answers = new ArrayList<QuestionResponseElement>();

        for (int checkboxIndex = 0; checkboxIndex < answerOptions.size(); checkboxIndex++) {
            CheckBox answerCheckbox = answerOptions.get(checkboxIndex);
            if (answerCheckbox.getValue()) {
                //check box is checked, convert the (possibly randomized) ui index to the actual index
                answers.add(new QuestionResponseElement(
                        listOptionsIndexMap[checkboxIndex],
                        listOptions.get(listOptionsIndexMap[checkboxIndex]),
                        getSurveyElement().getQuestion().getReplyOptionSet(),
                        timeAnswered));
            }
        }

        if(answers.isEmpty()){

        	// if the multiple choice question has had all of its answer options unselected, treat the question as unanswered
        	timeAnswered = null;
        }

        if (timeAnswered != null) {

            if (isSingleSelect()) {

                if (answers.size() != 1) {

                    throw new MalformedAnswerException("Select only one answer", true);
                }

            } else {

            	// Get the min & max selections from the question in the database
            	Integer minRequired = getSurveyElement().getQuestion().getMinimumSelectionsRequired();
            	Integer maxAllowed = getSurveyElement().getQuestion().getMaximumSelectionsAllowed();

            	/* Get the number of answers available.
            	 * B. Zahid: Previously getSurveyElement().getQuestion().getWeights().size was used,
            	 * but throwing a NPE when the question's weights were null. This happens when a
            	 * multi-select question was authored without weights, and then assigned weights in
            	 * the survey editor. */
            	int answerCount = getSurveyElement().getQuestion().getReplyOptionSet().getListOptions().size();

                if (minRequired != null && answers.size() < minRequired && minRequired <= answerCount) {

                    throw new MalformedAnswerException("Not enough answers selected", true);
                }

                if (maxAllowed != null && answers.size() > maxAllowed && maxAllowed <= answerCount) {

                    throw new MalformedAnswerException("Too many answers selected", true);
                }
            }

        } else {

            throw new MalformedAnswerException("No option(s) selected", getSurveyElement().getIsRequired());
        }

        AbstractQuestionResponse response = AbstractQuestionResponse.createResponse(getSurveyElement(), answers);

        //store the order that the list options were shown in so that that order can be preserved in AARs
        List<Integer> optionIds = new ArrayList<>();
        for(ListOption listOption : listOptions) {
            optionIds.add(listOption.getId());
        }
        response.setOptions(optionIds);

        return response;
    }

    /**
     * The question has been answered.  Set the necessary attributes to indicate so.
     */
    private void answeredQuestion(){
        timeAnswered = new Date();
    }

    @Override
    public void setExternalQuestionResponse(
            AbstractQuestionResponse questionResponse) {

        if(questionResponse == null){
            return;
        }

        //select the appropriate answers
        if(questionResponse instanceof MultipleChoiceQuestionResponse){

            MultipleChoiceQuestionResponse multChoiceQResponse = (MultipleChoiceQuestionResponse)questionResponse;
            List<QuestionResponseElement> responses = multChoiceQResponse.getResponses();
            for(QuestionResponseElement response : responses){

                for (int index=0; index<answerOptions.size(); index++) {
                    CheckBox checkBox = answerOptions.get(index);
                    if(response.getColumnIndex() != null && response.getColumnIndex() == index){

                        logger.info("Selecting multiple choice question choice component based on an external request for the choice text of '"+checkBox.getText()+"'.");
                        checkBox.setValue(true);
                        answeredQuestion();
                    }
                }
            }
        }else{
            throw new DetailedException("Failed to apply an external question response to a multiple choice question.",
                    "Found unhandled question response object type of "+questionResponse+".  Expected a "+MultipleChoiceQuestionResponse.class+".", null);
        }
    }
}
