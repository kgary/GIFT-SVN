/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Label;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.MultipleChoiceResponseWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AnswerSetPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.DifficultyAndConceptsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.HiddenAnswerWeightsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MultipleChoiceQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

/**
 * The TrueFalseWidget allows the survey author to create a true false question in a survey page.
 * 
 * @author nblomberg
 *
 */
public class TrueFalseWidget extends AbstractQuestionWidget  {

    private static Logger logger = Logger.getLogger(TrueFalseWidget.class.getName());
    
    /** The ID of the last group of radio buttons added by an instance of this widget*/
    private static int lastRadioGroupId = 0;
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, TrueFalseWidget> {
	}
	

	@UiField
	protected Container responseContainer;
	
	@UiField
	protected Label pointHeader;
	
	/** The ID for this widget's radio button group */
	private int radioGroupId = generateRadioGroupId();
	
	/** List of weights for all the answers */
	private List<Double> answerWeights;
	
	/**
	 * collection of attributes for this question to be scored on
	 */
	private Set<AttributeScorerProperties> scoringAttributes;
	
	/** Whether or not this widget is in read-only mode */
	private boolean readOnly;
	
	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
     * to alter the widget components.
     * @param isScored whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public TrueFalseWidget(SurveyEditMode mode, boolean isScored) {
	    super(mode, isScored);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    onEditorModeChanged(mode);    
	    
	}
	
	/**
	 * Generates a new ID for a radio button group
	 * 
	 * @return the new ID
	 */
	private int generateRadioGroupId() {
		
		lastRadioGroupId = lastRadioGroupId + 1;
		
		return lastRadioGroupId;
	}
	
	/**
	 * Adds a new response widget where the user can enter text to create a new response
	 */
	private MultipleChoiceResponseWidget addResponse(String text){
		final MultipleChoiceResponseWidget response = new MultipleChoiceResponseWidget("TrueFalseQuestionGroup - " + radioGroupId);	
		
		response.setReadOnlyMode(readOnly);
		
		response.getPointBox().addValueChangeHandler(new ValueChangeHandler<Double>(){

			@Override
			public void onValueChange(ValueChangeEvent<Double> event) {
			    //Treat blank/null values as 0
                Double value = event.getValue() != null ?
                        event.getValue() : 
                        0.0;

				answerWeights.set(responseContainer.getWidgetIndex(response), value);
				response.setPointValue(value);
				getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
				
				updateTotalQuestionFlag();
				SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
			}
			
		});
		
		response.getFeedbackBox().addChangeHandler(new ChangeHandler(){

            @Override
            public void onChange(ChangeEvent arg0) {
                ((AnswerSetPropertySet) getPropertySetByType(AnswerSetPropertySet.class.getName())).setReplyFeedbacks(getRawFeedbackStrings());
            }
		    
		});
		
		response.getAddFeedbackButton().addClickHandler(new ClickHandler(){

            @Override
            public void onClick(ClickEvent event) {
                ((AnswerSetPropertySet) getPropertySetByType(AnswerSetPropertySet.class.getName())).setReplyFeedbacks(getRawFeedbackStrings());
            }
            
        });
		
		response.getLabel().setValue(text);
		response.setStaticLabel(true);
		response.setLabelEditable(false);
		
		response.setMode(getEditMode());
		
		responseContainer.add(response);
		
		return response;
	}

    @Override
    protected void addCustomPropertySets() {
    	AnswerSetPropertySet answerSet = new AnswerSetPropertySet();
    	//TODO May need to adjust the IDs assigned for theses answers and optionlist
	    OptionList optionList = new OptionList(0, "True/False", false, Arrays.asList(new ListOption(0, "True", 0), new ListOption(0, "False", 0)), Arrays.asList(GatClientUtility.getUserName()), Arrays.asList(GatClientUtility.getUserName()));
	    answerSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, optionList);
	    addPropertySet(answerSet);
	    
	    HiddenAnswerWeightsPropertySet weightsSet = new HiddenAnswerWeightsPropertySet();
	    addPropertySet(weightsSet);
	    
	    DifficultyAndConceptsPropertySet diffAndConceptSet = new DifficultyAndConceptsPropertySet();
        addPropertySet(diffAndConceptSet);
	    
	    answerWeights = Arrays.asList(0.0, 0.0);
	    scoringAttributes = ((QuestionScorer) weightsSet.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
    }
    
    /**
     * Updates the QuestionScorer totalQuestion flag based on the current answerweights for the question.
     * This method should be called anytime a score event is fired and or anytime an event occurs that could change
     * the weights for the question (such as during the load process).
     */
    private void updateTotalQuestionFlag() {

        AbstractPropertySet propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
        HiddenAnswerWeightsPropertySet answerWeightProps = (HiddenAnswerWeightsPropertySet) propSet;
        updateTotalQuestionFlag(answerWeightProps.getProperties());
    }
    
    public List<String> getRawFeedbackStrings() {
        List<String> feedbacks = new ArrayList<String>();
        
        if (responseContainer.getWidgetCount() > 0) {

            for (int i = 0; i < responseContainer.getWidgetCount(); i++) {

                Widget w = responseContainer.getWidget(i);

                if (w instanceof MultipleChoiceResponseWidget) {
                    
                    String feedback = ((MultipleChoiceResponseWidget) w).getFeedback();
                    
                    if(feedback != null && !feedback.isEmpty()){
                        feedbacks.add(feedback);
                    
                    } else {
                        feedbacks.add("");
                    }
                }
            }
        }
        
        return feedbacks;
    }
    
    public void setRawFeedbackStrings(List<String> feedbacks)
    {
        for(int i = 0; i < responseContainer.getWidgetCount(); i++) {
            if(i < feedbacks.size()) {
                String feedback = feedbacks.get(i);
                Widget w = responseContainer.getWidget(i);
                
                if(w instanceof MultipleChoiceResponseWidget) {
                    if(feedback != null && !feedback.isEmpty()){
                        ((MultipleChoiceResponseWidget) w).setFeedback(feedback);
                    } else {
                        ((MultipleChoiceResponseWidget) w).setFeedback(null);
                    }
                }
            }
        }
    }

    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {

    	super.onPropertySetChange(propSet);
    }

    @Override
	public void initializeWidget() {
        for(ListOption option : ((OptionList) getPropertySetByType(AnswerSetPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY)).getListOptions()){
        	addResponse(option.getText());
        }
        
    }

    @Override
    public void refresh() {
        if (getEditMode() == SurveyEditMode.WritingMode) {
            pointHeader.setVisible(false);
            questionHtml.setEditable(true);
            refreshResponseWidgets();
        } else if (getEditMode() == SurveyEditMode.ScoringMode) {
            pointHeader.setVisible(true);
            questionHtml.setEditable(false);
            refreshResponseWidgets();
        } else {
            logger.severe("Unsupported mode: " + getEditMode());
        }
        
    }
    
    /**
     * Refreshes the response widgets based on the current mode of the editor.
     */
    private void refreshResponseWidgets() {
        for (int x=0; x < responseContainer.getWidgetCount(); x++) {
            Widget widget = responseContainer.getWidget(x);
            
            if (widget instanceof MultipleChoiceResponseWidget) {
                MultipleChoiceResponseWidget mcrWidget = (MultipleChoiceResponseWidget)widget;
                
                mcrWidget.refresh();
            }
            
        }
    }

    @Override
    public void onEditorModeChanged(SurveyEditMode mode) {
        super.onEditorModeChanged(mode);
        setEditMode(mode);
        
        
        for (int x=0; x < responseContainer.getWidgetCount(); x++) {
            Widget widget = responseContainer.getWidget(x);
            
            if (widget instanceof MultipleChoiceResponseWidget) {
                MultipleChoiceResponseWidget mcrWidget = (MultipleChoiceResponseWidget)widget;
                
                mcrWidget.onEditorModeChanged(mode);
            }
            
        }
    }

    @Override
    public Double getPossibleTotalPoints() {
         
        // The total points for true/false is computed the same as multiple choice with max selection of 1.
        Double totalPoints = SurveyScorerUtil.getHighestScoreMultipleChoice(answerWeights, 0, 1);
        
        
        return totalPoints;
    }

	@Override
	protected void setIsScoredType() {
		isScoredType = true;
	}

    @Override
    public void load(AbstractSurveyElement element) throws LoadSurveyException {
        
        if (element instanceof AbstractSurveyQuestion) {
            @SuppressWarnings("unchecked")
            AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;
            if (surveyQuestion instanceof MultipleChoiceSurveyQuestion && ((MultipleChoiceSurveyQuestion) surveyQuestion).getChoices().getName().equals("True/False")) {
                
                MultipleChoiceSurveyQuestion question = (MultipleChoiceSurveyQuestion) surveyQuestion;
                SurveyItemProperties properties = question.getProperties();

                MultipleChoiceQuestion trueFalseQuestion = question.getQuestion();
                
                logger.info("question text: " + trueFalseQuestion.getText());
                questionHtml.setValue(trueFalseQuestion.getText());

                // print the question properties
                debugPrintQuestionProperties(properties);
                
                // Load the properties (if any)
                AbstractPropertySet propSet = getPropertySetByType(CommonPropertySet.class.getName());
                CommonPropertySet commonProps = (CommonPropertySet) propSet;
                commonProps.load(properties);
                commonProps.load(trueFalseQuestion.getProperties());
                commonProps.setSurveyQuestion(surveyQuestion);
                
                // Load the Image Display Properties
                propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
                QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
                imageProps.load(properties);
                imageProps.load(trueFalseQuestion.getProperties());
                
                // Load the Answer Set Properties
                propSet = getPropertySetByType(AnswerSetPropertySet.class.getName());
                AnswerSetPropertySet answerSetProps = (AnswerSetPropertySet) propSet;
                answerSetProps.load(properties);
                answerSetProps.load(trueFalseQuestion.getProperties());
                
                // Load the Answer Weights Properties
                propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
                HiddenAnswerWeightsPropertySet answerWeightProps = (HiddenAnswerWeightsPropertySet) propSet;
                answerWeightProps.load(trueFalseQuestion.getProperties());
                answerWeightProps.load(properties);
                
                // Load the difficulty and concepts Properties
                propSet = getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName());
                DifficultyAndConceptsPropertySet diffAndConceptsSet = (DifficultyAndConceptsPropertySet) propSet;
                diffAndConceptsSet.load(properties);
                diffAndConceptsSet.load(trueFalseQuestion.getProperties());
                
                if(!SurveyItemProperties.decodeDoubleListString((String) answerWeightProps.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS)).isEmpty()){
                	answerWeights = SurveyItemProperties.decodeDoubleListString((String) answerWeightProps.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
				}
                if(answerWeights.size() == 2){
	                ((MultipleChoiceResponseWidget) responseContainer.getWidget(0)).getPointBox().setValue(answerWeights.get(0));
	                ((MultipleChoiceResponseWidget) responseContainer.getWidget(0)).setPointValue(answerWeights.get(0));
	                ((MultipleChoiceResponseWidget) responseContainer.getWidget(1)).getPointBox().setValue(answerWeights.get(1));
	                ((MultipleChoiceResponseWidget) responseContainer.getWidget(1)).setPointValue(answerWeights.get(1));
                }
                
                setRawFeedbackStrings(answerSetProps.getReplyFeedbacks());
                
                scoringAttributes = ((QuestionScorer) answerWeightProps.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
                
                updateTotalQuestionFlag();
                
                // This should be called after all property sets have been loaded for the abstractsurveyelement.
                addUnsupportedProperties(trueFalseQuestion.getProperties(), properties);
                onLoadNotifyPropertySetChanges();
                
                // print the question properties
                debugPrintQuestionProperties(trueFalseQuestion.getProperties());
                
                refresh();
            } else {
                throw new LoadSurveyException("Trying to load a TrueFalse widget, but encountered non multiple choice data from the database.", null);
            }
        } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type AbstractSurveyQuestion, but found: " + element.getClass().getName(), null);
        }
        
    }
    
    /**
     * Sets the collection of attributes for this question to be scored on. This collection comes from
     * the question container widget and is set onValueChange
     * 
     * @param attributes the collection of Attributes
     */
    @Override
    public void setScorerProperty(Set<AttributeScorerProperties> attributes){
    	scoringAttributes = attributes;
    	((QuestionScorer) getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).setAttributeScorers(scoringAttributes);
    }
    
    /**
     * Debug print function used to print the properties for the question.
     * @param properties
     */
    public void debugPrintQuestionProperties(SurveyItemProperties properties) {
        
        if (properties != null) {
            // DEBUG PRINT THE properties
            logger.info("Properties size = " + properties.getPropertyCount());
            for (SurveyPropertyKeyEnum key : properties.getKeys()) {
                logger.info("Key name = " + key + "\nKey value = " + properties.getPropertyValue(key));
            }
        }
        
    }
    
    @Override
    public List<String> getScoringAttributesAsStringList(){
    	List<String> stringList = new ArrayList<String>();
    	for(AttributeScorerProperties attribute : scoringAttributes){
    		stringList.add(attribute.getAttributeType().getName());
    	}
    	return stringList;
    }
    
    @Override
    public void setDifficulty(QuestionDifficultyEnum difficulty) {
		getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY, difficulty.getDisplayName());
	}
	
    @Override
	public String getDifficulty(){
		return getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY).toString();
	}
	
    @Override
	public void setConcepts(ArrayList<String> concepts){
		getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS, SurveyItemProperties.encodeListString(concepts));
	}
	
	@Override
	public List<String> getConcepts(){
		return SurveyItemProperties.decodeListString((String) getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.ASSOCIATED_CONCEPTS));
	}
	
	@Override
	public void setReadOnlyMode(boolean readOnly) {
		this.readOnly = readOnly;

	    questionHtml.setEditable(!readOnly);
	    if (readOnly) {
	        questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
	        questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
	    }
		
		for(int i = 0; i < responseContainer.getWidgetCount(); i++){
			
			Widget widget = responseContainer.getWidget(i);
			
			if(widget instanceof MultipleChoiceResponseWidget){
				
				MultipleChoiceResponseWidget response = (MultipleChoiceResponseWidget) widget;
				
				response.setReadOnlyMode(readOnly);
			}
		}
	}

    @Override
    public void setPlaceholderResponseVisible(boolean visible) {
        //Nothing to do
    }
}
