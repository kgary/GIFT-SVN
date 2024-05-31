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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;

import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.CurrentQuestionAnsweredCallback;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.SurveyPage;
import mil.arl.gift.common.survey.SurveyPageResponse;
import mil.arl.gift.common.survey.SurveyPageResponseMetadata;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * A widget for displaying a survey page that the user can fill out
 *
 * @author jleonard
 */
public class SurveyPageWidget extends Composite implements RequiresResize {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(SurveyPageWidget.class.getName());

    private final SurveyPageResponse surveyPageResponse = new SurveyPageResponse();
    
    private final SurveyProperties surveyProperties;

    private final ArrayList<AbstractSurveyElementWidget<? extends AbstractSurveyElement>> surveyElementWidgets = new ArrayList<AbstractSurveyElementWidget<? extends AbstractSurveyElement>>();

    private final ScrollPanel questionScrollPanel = new ScrollPanel();
    
    /**
     * Constructor, creates a widget for reviewing the answer to a page
     *
     * @param surveyProperties The properties of the survey this page is in
     * @param surveyPage The survey page to be displayed
     * @param pageResponse The response to the page 
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     */
    @SuppressWarnings("unchecked")
	public SurveyPageWidget(SurveyProperties surveyProperties, SurveyPage surveyPage, SurveyPageResponseMetadata pageResponse, boolean isDebug) {
        
        this.surveyProperties = surveyProperties;

        surveyPageResponse.setSurveyPage(surveyPage);

        FlowPanel containerPanel = new FlowPanel();
        this.initWidget(containerPanel);

        if (!surveyProperties.getHideSurveyPageNumbers()) {

            Label surveyPageNameLabel = new Label(surveyPage.getName());
            surveyPageNameLabel.addStyleName(SurveyCssStyles.SURVEY_PAGE_NAME_STYLE);
            containerPanel.add(surveyPageNameLabel);
            questionScrollPanel.getElement().getStyle().setProperty("margin", "-15px -20px 0px");
        } else {
            questionScrollPanel.getElement().getStyle().setProperty("margin", "15px -20px 0px");
        }
        
        FlowPanel questionPanel = new FlowPanel();
        
        questionScrollPanel.setHeight("100%");
        questionScrollPanel.getElement().getStyle().setProperty("paddingLeft", "20px");
        questionScrollPanel.getElement().getStyle().setProperty("borderBottom", "solid 1px rgb(166, 175, 179)");
        questionScrollPanel.add(questionPanel);
        containerPanel.add(questionScrollPanel);
        
        QuestionNumberGenerator questionNumber = new QuestionNumberGenerator();

        for (AbstractSurveyElement element : surveyPage.getElements()) {

            AbstractSurveyElementWidget<? extends AbstractSurveyElement> elementWidget = null;

            if (element.getSurveyElementType() == SurveyElementTypeEnum.QUESTION_ELEMENT && pageResponse != null) {

                AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;

                for (AbstractQuestionResponseMetadata questionResponseMetadata : pageResponse.getQuestionResponses()) {
                    logger.info("question response survey question id = "+questionResponseMetadata.getSurveyQuestionId()+", checking against "+surveyQuestion.getId()+" from "+pageResponse.getQuestionResponses().size()+" responses for this question.");
                    if (Integer.compare(questionResponseMetadata.getSurveyQuestionId(), surveyQuestion.getId()) == 0) {

                        logger.info("creating question widget");
                        elementWidget = AbstractSurveyQuestionWidget.createQuestionWidget(this.surveyProperties, surveyQuestion, questionNumber.getNextQuestionNumber(), questionResponseMetadata);
                        break;
                    }
                }
                
                if(elementWidget == null){
                    throw new RuntimeException("Failed to match the survey question with id "+surveyQuestion.getId()+" to any of the "+pageResponse.getQuestionResponses().size()+" question response survey questions.");
                }

            } else {

                elementWidget = AbstractSurveyElementWidget.createSurveyElementWidget(surveyProperties, element, questionNumber, false, isDebug);
            }

            if (elementWidget != null) {

                surveyElementWidgets.add(elementWidget);
                questionPanel.add(elementWidget);

            } else {

                throw new NullPointerException("A survey element widget could not be constructed for element number " + questionNumber.getCurrentQuestionNumber());
            }
        }
    }

    /**
     * Constructor, creates a widget for answering a page
     *
     * @param surveyProperties The properties of the survey this page is in
     * @param surveyPage The survey page to be displayed
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     */
    public SurveyPageWidget(SurveyProperties surveyProperties, SurveyPage surveyPage, boolean isDebug) {
        this(surveyProperties, surveyPage, null, isDebug);
    }

    /**
     * Called when the response to the survey page is started
     */
    public void onSurveyPageStart() {
        surveyPageResponse.setStartTime(new Date());
    }

    /**
     * Called when the response to the survey is finished
     */
    public void onSurveyPageEnd() {
        surveyPageResponse.setEndTime(new Date());
    }

    /**
     * Check whether the survey page has been completely filled out
     *
     * @return the following can be returned:<br/>
     * SurveyCompletionStatus.COMPLETE - if every question was answered on this page, no matter if optional or required<br/>
     * SurveyCompletionStatus.MISSING_REQUIRED - if there is one or more required questions not answered on this page<br/>
     * SurveyCompletionStatus.MISSING_OPTIONAL - if there are NO required questions missing an answer and there are one or more
     * optional questions not answered on this page
     */
    @SuppressWarnings("unchecked")
	public SurveyCompletionStatus isComplete() {

        SurveyCompletionStatus isComplete = SurveyCompletionStatus.COMPLETE;

        for (AbstractSurveyElementWidget<? extends AbstractSurveyElement> surveyElement : surveyElementWidgets) {

            if (surveyElement.getSurveyElement().getSurveyElementType() == SurveyElementTypeEnum.QUESTION_ELEMENT) {

                AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>> surveyQuestionWidget = (AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>>) surveyElement;

                try {

                    surveyQuestionWidget.resetStatus();
                    AbstractQuestionResponse answer = surveyQuestionWidget.getAnswer(true);

                    if (answer == null) {

                        throw new MalformedAnswerException("The answer was null", surveyQuestionWidget.getSurveyElement().getIsRequired());
                    }

                } catch (MalformedAnswerException e) {

                    if (e.getIsCritical()) {

                        //show error message next to this required question
                        surveyQuestionWidget.displayError(e.getWhy());
                        isComplete = SurveyCompletionStatus.MISSING_REQUIRED;

                    } else {
                        
                        if(isComplete == SurveyCompletionStatus.COMPLETE) {
                            
                            isComplete = SurveyCompletionStatus.MISSING_OPTIONAL;
                        }
                        
                        //show warning message next to this optional question
                        surveyQuestionWidget.displayWarning(e.getWhy());
                    }
                }
            }
        }

        return isComplete;
    }

    /**
     * Gets the responses of the user for answering the questions on the page
     *
     * @return GwtSurveyPageResponse The response for the page
     * @throws MalformedAnswerException Thrown when the answer is malformed for
     * one of the questions
     */
    @SuppressWarnings("unchecked")
	public SurveyPageResponse getResponse() throws MalformedAnswerException {

        List<AbstractQuestionResponse> surveyResults = new ArrayList<AbstractQuestionResponse>();
        MalformedAnswerException exceptionThrown = null;

        for (AbstractSurveyElementWidget<? extends AbstractSurveyElement> surveyElement : surveyElementWidgets) {

            if (surveyElement.getSurveyElement().getSurveyElementType() == SurveyElementTypeEnum.QUESTION_ELEMENT) {

                AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>> surveyQuestionWidget = (AbstractSurveyQuestionWidget<? extends AbstractSurveyQuestion<? extends AbstractQuestion>>) surveyElement;

                try {

                    surveyQuestionWidget.resetStatus();
                    surveyResults.add(surveyQuestionWidget.getAnswer(false));

                } catch (MalformedAnswerException e) {
                    
                    if(e.getIsCritical()) {
                        
                        surveyQuestionWidget.displayError(e.getWhy());
                        
                    } else {
                        
                        surveyResults.add(AbstractQuestionResponse.createResponse(surveyQuestionWidget.getSurveyElement(), new ArrayList<QuestionResponseElement>()));
                        
                        surveyQuestionWidget.displayWarning(e.getWhy());
                    }

                    if (exceptionThrown == null && e.getIsCritical()) {

                        exceptionThrown = e;
                    }
                }
            }
        }

        if (exceptionThrown != null) {

            throw exceptionThrown;
        }

        surveyPageResponse.getQuestionResponses().clear();
        surveyPageResponse.getQuestionResponses().addAll(surveyResults);

        return surveyPageResponse;
    }
    
    /**
     * Populate the correct widget component with the answers to survey questions provided by some other logic
     * external to the tutor.  For example, with a multiple choice question the choices provided in
     * the response should have their radio/checkbox components selected in the tutor client to indicate
     * the question was answered.
     *  
     * @param surveyPageResponse contains responses to questions on this survey page.  Can be null.  Can also have no actual responses as a no
     * response is a valid question response.
     */
    public void applyExternalSurveyResponse(SurveyPageResponse surveyPageResponse){
        
        if(surveyPageResponse == null){
            return;
        }else if(this.surveyPageResponse.getSurveyPage().getId() != surveyPageResponse.getSurveyPage().getId()){
            //ERROR
            throw new DetailedException("Unable to apply the responses for a specific survey page because they are for a different survey page.", 
                    "Received survey page response for survey page with id "+surveyPageResponse.getSurveyPage().getId()+" but this survey page widget is for the survey page with id "+this.surveyPageResponse.getSurveyPage().getId()+".", null);
        }
        
        boolean responseApplied;
        for(AbstractQuestionResponse questionResponse : surveyPageResponse.getQuestionResponses()){
            
            responseApplied = false;
            for(AbstractSurveyElementWidget<?> surveyElementWidget : surveyElementWidgets){
                
                if(surveyElementWidget instanceof AbstractSurveyQuestionWidget<?>){
                
                    AbstractSurveyElement surveyElement = surveyElementWidget.getSurveyElement();
                    if(questionResponse.getSurveyQuestion().getId() == surveyElement.getId()){
                        ((AbstractSurveyQuestionWidget<?>)surveyElementWidget).setExternalQuestionResponse(questionResponse);
                        responseApplied = true;
                        break;
                    }
                }
            }
            
            if(!responseApplied){
                //ERROR
                throw new DetailedException("Failed to apply the question response on a survey page.", 
                        "Could not find the question id widget of "+questionResponse.getSurveyQuestion().getId()+" from the question response.", null);
            }
        }
    }
    
    /**
     * Set the callback used for notification of a survey question being answered.
     * 
     * @param questionAnsweredCallback can be null.
     */
    public void setCurrentQuestionAnsweredCallback(CurrentQuestionAnsweredCallback questionAnsweredCallback){
        
        for(AbstractSurveyElementWidget<?> surveyElementWidget : surveyElementWidgets){
            surveyElementWidget.setCurrentQuestionAnsweredCallback(questionAnsweredCallback);            
        }
    }
    
    /**
     * Return the unique survey page id for the survey page being represented by this widget.
     * 
     * @return the unique survey page id
     */
    public int getSurveyPageId(){
        return surveyPageResponse.getSurveyPage().getId();
    }
    
    @Override
    protected void onAttach() {
        super.onAttach();
        onResize();
    }


	@Override
	public void onResize() {
		
	}
}
