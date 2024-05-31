/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import java.util.Iterator;
import java.util.logging.Logger;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.CurrentQuestionAnsweredCallback;
import mil.arl.gift.common.survey.SurveyProperties;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;

/**
 * An abstract class for representing a survey element as a widget
 *
 * @param <T> The type of survey element this class for rendering
 * @author jleonard
 */
public abstract class AbstractSurveyElementWidget<T extends AbstractSurveyElement> extends Composite {

    /** The logger. */
    private static Logger logger = Logger.getLogger(AbstractSurveyElementWidget.class.getName());
            
    private final T surveyElement;

    protected final SurveyProperties surveyProperties;
    
    private final boolean isPreview;

    private final FlowPanel containerPanel = new FlowPanel();
    
    /** used for notification that this question was answered, can be null */
    private CurrentQuestionAnsweredCallback questionAnsweredCallback = null;

    /**
     * Constructor
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyElement The survey element to display
     * @param isPreview If the element is only being previewed
     */
    public AbstractSurveyElementWidget(SurveyProperties surveyProperties, T surveyElement, boolean isPreview) {

        this.surveyElement = surveyElement;
        this.surveyProperties = surveyProperties;
        this.isPreview = isPreview;

        this.initWidget(containerPanel);
    }
    
    /**
     *  Only calculate total points earned and show right/wrong answer icons and show total points earned for question bank, assess learner and
     *  collect info scored knowledge only survey types.  This means don't do this for collect info not scored and collect info scored with other
     *  attributes besides knowledge (e.g. motivation survey).
     *  This is because the scoring on surveys like the motivation survey are only for GIFT to use and not for learners to see as there most 
     *  likely no right or wrong answer anyway
     * @return true if the points earned should be shown to the learner.
     */
    public boolean canShowPointsEarned(){
        
        if(surveyProperties == null){
            return false;
        }
        
        logger.info("checking if the points earned from survey responses should be shown to the learner...");

        boolean showPoints = surveyProperties.getSurveyType() == SurveyTypeEnum.ASSESSLEARNER_QUESTIONBANK || surveyProperties.getSurveyType() == SurveyTypeEnum.ASSESSLEARNER_STATIC;
        if(!showPoints){
            
            if(surveyProperties.getSurveyType() == null || surveyProperties.getSurveyType() == SurveyTypeEnum.COLLECTINFO_SCORED){
                // either this is a survey that existed before survey types were a property and this survey hasn't been saved since then
                // -OR-
                // it is a collect info scored survey type and therefore need to check if there is only a knowledge scorer because this
                // type of survey can have affective scorers like motivation.

                if(surveyProperties.getSurveyScorer() != null){
                    //this is at least a scored survey

                    if(surveyProperties.getSurveyScorer().getAttributeScorers() != null &&
                        surveyProperties.getSurveyScorer().getAttributeScorers().size() == 1){

                        Iterator<AttributeScorerProperties> itr = surveyProperties.getSurveyScorer().getAttributeScorers().iterator();
                        if(itr.next().getAttributeType().equals(LearnerStateAttributeNameEnum.KNOWLEDGE)){
                            // collect user info only with knowledge scorer
                            logger.info("going to show earned points because the collect user info survey only has knowledge scorer.");
                            showPoints = true;                               
                        }

                    }else if(surveyProperties.getSurveyScorer().getTotalScorer() != null){

                        if(surveyProperties.getSurveyScorer().getTotalScorer().getAttributeScorers().isEmpty()){
                            // legacy survey with no survey type but has no attribute scorer (infers knowledge survey type) 
                            logger.info("going to show earned points because the legacy survey with no survey type has no attribute scorer (can infer knowledge survey type).");
                            showPoints = true; 
                        }else if(surveyProperties.getSurveyScorer().getTotalScorer().getAttributeScorers().size() == 1){

                            Iterator<AttributeScorerProperties> itr = surveyProperties.getSurveyScorer().getTotalScorer().getAttributeScorers().iterator();
                            if(itr.next().getAttributeType().equals(LearnerStateAttributeNameEnum.KNOWLEDGE)){
                                // only knowledge scorer
                                logger.info("going to show earned points because the legacy survey with no survey type has one knowledge scorer.");
                                showPoints = true;
                            }
                        }
                    }                        
                }
            }
        }
        logger.info("Determined that the earned points should "+(showPoints ? "" : "NOT") + " be shown.");
        return showPoints;
    }

    /**
     * Gets the element in this widget
     *
     * @return T The element in this widget
     */
    public final T getSurveyElement() {
        return surveyElement;
    }
    
    /**
     * Gets the properties of the survey this survey element is in
     * 
     * @return SurveyProperties The properties of the survey this element is in
     */
    protected final SurveyProperties getSurveyProperties() {
        
        return surveyProperties;
    }
    
    /**
     * Gets if this survey element is only being previewed
     * 
     * @return boolean If the survey element is only being previewed
     */
    public boolean isPreview() {
        
        return isPreview;
    }

    /**
     * Add a widget for displaying the element
     *
     * @param elementWidget The widget for displaying the element
     */
    protected final void addWidget(final Widget elementWidget) {
        containerPanel.clear();
        containerPanel.add(elementWidget);
    }

    /**
     * Generates a small widget for displaying an error
     *
     * @param error The error that occurred
     * @return Widget The widget displaying the error
     */
    protected static Widget generateSmallErrorPanel(String error) {

        HorizontalPanel panel = new HorizontalPanel();
        panel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        panel.getElement().getStyle().setBorderWidth(1, Style.Unit.PX);
        panel.getElement().getStyle().setBorderColor("grey");
        panel.getElement().getStyle().setBorderStyle(Style.BorderStyle.SOLID);
        panel.getElement().getStyle().setPadding(2, Style.Unit.PX);

        Image errorIcon = new Image("images/errorIcon.png");
        errorIcon.setSize("16px", "16px");
        errorIcon.getElement().getStyle().setPadding(3, Style.Unit.PX);
        errorIcon.getElement().getStyle().setVerticalAlign(Style.VerticalAlign.MIDDLE);

        panel.add(errorIcon);

        Label errorLabel = new Label(error);
        errorLabel.getElement().getStyle().setColor("red");
        errorLabel.getElement().getStyle().setPadding(3, Style.Unit.PX);
        errorLabel.getElement().getStyle().setFontWeight(Style.FontWeight.BOLD);

        panel.add(errorLabel);

        return panel;
    }
    
    /**
     * Constructs a widget for displaying a survey element
     * 
     * @param surveyProperties The properties of the survey the element is in
     * @param surveyElement The survey element to make a widget for
     * @param questionNumber The generator for question numbers
     * @param isPreview If the survey element is being previewed
     * @param isDebug if the survey widget should render in debug mode (e.g. color code scored answers)
     * @return AbstractSurveyElementWidget Widget of the survey element
     */
    @SuppressWarnings("unchecked")
	public static AbstractSurveyElementWidget<? extends AbstractSurveyElement> createSurveyElementWidget(SurveyProperties surveyProperties, AbstractSurveyElement surveyElement, QuestionNumberGenerator questionNumber, boolean isPreview, boolean isDebug) {

        if (surveyElement.getSurveyElementType() == SurveyElementTypeEnum.QUESTION_ELEMENT) {

            return AbstractSurveyQuestionWidget.createQuestionWidget(surveyProperties, (AbstractSurveyQuestion<? extends AbstractQuestion>)surveyElement, questionNumber.getNextQuestionNumber(), isPreview, isDebug);

        } else if (surveyElement.getSurveyElementType() == SurveyElementTypeEnum.TEXT_ELEMENT) {

            return new TextBlockElementWidget(surveyProperties, (TextSurveyElement) surveyElement, isPreview);
            
        } else {
            
            throw new RuntimeException("Cannot create survey element widget: Unknown survey item type - " + surveyElement.getSurveyElementType());
        }
    }
    
    /**
     * Set the callback used for notification when this question is answered
     * 
     * @param questionAnsweredCallback can be null.
     */
    public void setCurrentQuestionAnsweredCallback(CurrentQuestionAnsweredCallback questionAnsweredCallback){
        this.questionAnsweredCallback = questionAnsweredCallback;
    }
    
    /**
     * Call upon the question callback for notification that this question was answered.
     * If the callback is not set this method does nothing.
     * 
     * @param response contains the response to this question
     */
    protected void notifyCurrentQuestionAnswered(AbstractQuestionResponse response){
        
        if(questionAnsweredCallback != null){
            questionAnsweredCallback.questionAnswered(response);
        }
    }
}
