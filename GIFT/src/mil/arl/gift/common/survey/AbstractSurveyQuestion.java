/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;

import mil.arl.gift.common.enums.QuestionTypeEnum;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.util.StringUtils;

/**
 * A question in a survey
 *
 * @param <T> The type of question this survey question holds
 * @author jleonard
 */
public abstract class AbstractSurveyQuestion<T extends AbstractQuestion> extends AbstractSurveyElement implements Serializable {

    private static final long serialVersionUID = 1L;

    private T question;
    
    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public AbstractSurveyQuestion(){

    }

    /**
     * Constructor
     *
     * @param id unique id of the survey question
     * @param pageId unique page id that contains the survey question
     * @param question The question to be asked
     * @param properties question properties.  Can't be null.
     */
    public AbstractSurveyQuestion(int id, int pageId, T question, SurveyItemProperties properties) {
        super(id, pageId, properties);

        this.question = question;

    }
    
    @Override
    public SurveyElementTypeEnum getSurveyElementType() {
        
        return SurveyElementTypeEnum.QUESTION_ELEMENT;
    }

    /**
     * Gets the question to be asked
     *
     * @return T The question to be asked
     */
    public T getQuestion() {

        return question;
    }

    /**
     * Gets if a response is required for the question
     *
     * @return boolean If a response is required for a question
     */
    public boolean getIsRequired() {

        // Check to the abstract survey question property first.  If that is not used, then fallback to the
        // abstract question property.
        Boolean isRequired = getBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED);
        if (isRequired == null) {
            isRequired = false;
        }
        
        return isRequired;
        
    }
    
    /**
     * Whether this survey question allows partial credit or not.
     * @return default if the property doesn't exist is true.
     */
    public boolean getAllowsPartialCredit() {
        
        Boolean allowsPartialCredit = getBooleanPropertyValue(SurveyPropertyKeyEnum.ALLOW_PARTIAL_CREDIT);
        if(allowsPartialCredit == null) {
            allowsPartialCredit = true;
        }
        
        return allowsPartialCredit;
    }

    /**
     * Sets if a response is required for the question
     *
     * @param isRequired If a response is required for a question
     */
    public void setIsRequired(boolean isRequired) {

        if (!isRequired) {

            getProperties().removeProperty(SurveyPropertyKeyEnum.REQUIRED);

        } else {

            getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.REQUIRED, isRequired);
        }
    }

    /**
     * Gets the text to display as help to assist the user to answer the
     * question
     *
     * @return String The text to display as help to assist the user
     */
    public String getHelpString() {

        // Check to the abstract survey question property first.  If that is not used, then fallback to the
        // abstract question property.
        String helpString = getStringPropertyValue(SurveyPropertyKeyEnum.HELP_STRING);
        
        if (helpString == null) {
            helpString = "";
        }
        
        return helpString;
        
    }

    /**
     * Sets the text to display as help to assist the user to answer the
     * question
     *
     * @param helpString The text to display as help to assist the user
     */
    public void setHelpString(String helpString) {

        if (helpString.isEmpty()) {

            getProperties().removeProperty(SurveyPropertyKeyEnum.HELP_STRING);

        } else {

            getProperties().setPropertyValue(SurveyPropertyKeyEnum.HELP_STRING, helpString);
        }
    }

    /**
     * Gets the scorer types associated with the survey question
     *
     * @return List<SurveyScorerTypeEnum> The scorer types associated with the
     * survey question
     */
    public QuestionScorer getScorerModel() {

        // Check the abstract survey question first for the value, otherwise fallback to the question itself.
    	if(getPropertyValue(SurveyPropertyKeyEnum.SCORERS) != null){
    		
    		return (QuestionScorer) getPropertyValue(SurveyPropertyKeyEnum.SCORERS);
    	} else{
    		
    		return (QuestionScorer) getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.SCORERS);
    	} 
    }

    /**
     * Sets the scorer types associated with the survey question
     *
     * @param scorerModel The scorer model associated with the survey question
     */
    public void setScorerModel(QuestionScorer scorerModel) {
        
        if (scorerModel != null) {

            getProperties().setPropertyValue(SurveyPropertyKeyEnum.SCORERS, scorerModel);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.SCORERS);
        }
    }

    /**
     * Gets the scorer for the survey question
     *
     * @return String The scorer for the survey question
     */
    public String getTag() {

        // Check to the abstract survey question property first.  If that is not used, then fallback to the
        // abstract question property.
        String tag = getStringPropertyValue(SurveyPropertyKeyEnum.TAG);
        if (tag == null) {
            tag = Constants.EMPTY;
        }
        
        return tag;
    }

    /**
     * Sets the tag for the survey question
     *
     * @param tag The tag for the survey question
    */
    public void setTag(String tag) {

        if (tag != null && !tag.isEmpty()) {

            getProperties().setPropertyValue(SurveyPropertyKeyEnum.TAG, tag);

        } else {

            getProperties().removeProperty(SurveyPropertyKeyEnum.TAG);
        }
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Survey Question: ");
        sb.append("question = ").append(getQuestion());
        sb.append(", element = ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }

    /**
     * Creates a new survey question based on the question type
     *
     * @param id The ID of the survey question
     * @param pageId The ID of the survey page this survey question is in
     * @param question The question of this survey question.  Can't be null.
     * @param properties The properties of the survey question
     * @return GwtSurveyQuestion The constructed survey question 
     */
    public static AbstractSurveyQuestion<? extends AbstractQuestion> createSurveyQuestion(int id, int pageId, AbstractQuestion question, SurveyItemProperties properties) {
        
        if(question == null){
            throw new IllegalArgumentException("Failed to create a new survey question because the question provided is null.");
        }

        QuestionTypeEnum questionType = QuestionTypeEnum.valueOf(question);

        if (questionType.equals(QuestionTypeEnum.FREE_RESPONSE)) {

            return new FillInTheBlankSurveyQuestion(id, pageId, (FillInTheBlankQuestion) question, properties);

        } else if (questionType.equals(QuestionTypeEnum.ESSAY)) { 
        	
        	return new FillInTheBlankSurveyQuestion(id, pageId, (FillInTheBlankQuestion) question, properties);
            
        } else if (questionType.equals(QuestionTypeEnum.MULTIPLE_CHOICE)) {

            return new MultipleChoiceSurveyQuestion(id, pageId, (MultipleChoiceQuestion) question, properties);
            
        } else if (questionType.equals(QuestionTypeEnum.TRUE_FALSE)) {

            return new MultipleChoiceSurveyQuestion(id, pageId, (MultipleChoiceQuestion) question, properties);

        } else if (questionType.equals(QuestionTypeEnum.RATING_SCALE)) {

            return new RatingScaleSurveyQuestion(id, pageId, (RatingScaleQuestion) question, properties);

        } else if (questionType.equals(QuestionTypeEnum.MATRIX_OF_CHOICES)) {

            return new MatrixOfChoicesSurveyQuestion(id, pageId, (MatrixOfChoicesQuestion) question, properties);

        } else if (questionType.equals(QuestionTypeEnum.SLIDER_BAR)) {

            return new SliderSurveyQuestion(id, pageId, (SliderQuestion) question, properties);

        } else {

            throw new IllegalArgumentException("Cannot construct a quetion object of question type :" + questionType);
        }
    }
    

    /**
     * Used to fetch a String property value from an AbstractSurveyQuestion item.  NOTE:  There is an 
     * order precedence here in that the AbstractSurveyQuestion property is checked first.
     * If the property is not found there, then the property for the AbstractQuestion is checked.
     * 
     * @param propertyKey - the property key to get the value for.
     * @return The property value if found, null otherwise.
     */
    public String getStringPropertyValue(SurveyPropertyKeyEnum propertyKey) {
        
        String propertyValue = null;
        if (getProperties().getPropertyValue(propertyKey) != null) {
            propertyValue = (String) getProperties().getPropertyValue(propertyKey);
        } else {
            propertyValue = (String) getQuestion().getProperties().getPropertyValue(propertyKey);
        }
        
        return propertyValue;
    }
    
    /**
     * Used to fetch a Boolean property value from an AbstractSurveyQuestion item.  NOTE:  There is an 
     * order precedence here in that the AbstractSurveyQuestion property is checked first.
     * If the property is not found there, then the property for the AbstractQuestion is checked.
     * 
     * @param propertyKey - the property key to get the value for.
     * @return The property value if found, null otherwise.
     */
    public Boolean getBooleanPropertyValue(SurveyPropertyKeyEnum propertyKey) {
        
        Boolean propertyValue = null;
        if (getProperties().getBooleanPropertyValue(propertyKey) != null) {
            propertyValue = getProperties().getBooleanPropertyValue(propertyKey);
        } else {
            propertyValue = getQuestion().getProperties().getBooleanPropertyValue(propertyKey);
        }
        
        return propertyValue;
    }
    
    /**
     * Used to fetch an Integer property value from an AbstractSurveyQuestion item.  NOTE:  There is an 
     * order precedence here in that the AbstractSurveyQuestion property is checked first.
     * If the property is not found there, then the property for the AbstractQuestion is checked.
     * 
     * @param propertyKey - the property key to get the value for.
     * @return The property value if found, null otherwise.
     */
    public Integer getIntegerPropertyValue(SurveyPropertyKeyEnum propertyKey) {
        Integer propertyValue = null;
        if (getProperties().getIntegerPropertyValue(propertyKey) != null) {
            propertyValue = getProperties().getIntegerPropertyValue(propertyKey);
        } else {
            propertyValue = getQuestion().getProperties().getIntegerPropertyValue(propertyKey);
        }
        
        return propertyValue;
    }
    
    /**
     * Used to fetch a Serializable property value from an AbstractSurveyQuestion item.  NOTE:  There is an 
     * order precedence here in that the AbstractSurveyQuestion property is checked first.
     * If the property is not found there, then the property for the AbstractQuestion is checked.
     * 
     * @param propertyKey - the property key to get the value for.
     * @return The property value if found, null otherwise.
     */
    public Serializable getPropertyValue(SurveyPropertyKeyEnum propertyKey) {
        Serializable propertyValue = null;
        if (getProperties().getPropertyValue(propertyKey) != null) {
            propertyValue = getProperties().getPropertyValue(propertyKey);
        } else {
            propertyValue = getQuestion().getProperties().getPropertyValue(propertyKey);
        }
        
        return propertyValue;
    }
    
    /**
     * Return the highest possible score from the weights provided and the
     * maximum number of selections allowed from those weights.
     *
     * @return the sum of the N highest positive weights, where N =
     * maxSelections.  Return 0.0 if the question type doesn't support
     * scoring (e.g. free response) or the scoring values aren't authored.
     */
    public abstract double getHighestPossibleScore();
    
    @Override
    public String getWidgetId() {
        return (String) getPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID);
    }
    
    @Override
    public void setWidgetId(String widgetId) {
        if(StringUtils.isBlank(widgetId)) {
            question.getProperties().removeProperty(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID);
        } else {
            question.getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_WIDGET_ID, widgetId);
        }
    }
    
    @Override
    public void applySurveyMediaHost(String originUri) {
        applySurveyMediaHost(originUri, getQuestion().getProperties());
    }
}
