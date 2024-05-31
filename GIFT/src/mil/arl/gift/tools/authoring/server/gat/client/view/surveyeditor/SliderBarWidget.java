/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.constants.ScaleType;
import org.gwtbootstrap3.extras.slider.client.ui.base.constants.TooltipType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableInlineLabel;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.DifficultyAndConceptsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.HiddenAnswerWeightsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.SliderQuestion;
import mil.arl.gift.common.survey.SliderRange;
import mil.arl.gift.common.survey.SliderSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.QuestionScorer;
import mil.arl.gift.common.survey.score.SurveyScorerUtil;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.SliderLabelPropertySet;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.SliderRangePropertySet;

/**
 * The SliderBarWidget allows the survey author to create a slider bar item for the survey page.
 * 
 * @author nblomberg
 *
 */
public class SliderBarWidget extends AbstractQuestionWidget  {

    private static Logger logger = Logger.getLogger(SliderBarWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, SliderBarWidget> {
	}
	
	@UiField
	FlowPanel sliderContainer;
	
	@UiField
	Slider slider;
	
	@UiField
	Label pointHeader;
	
	@UiField
	EditableInlineLabel leftLabel;
	
	@UiField
	EditableInlineLabel rightLabel;
	
	@UiHandler("slider")
	void onChange(ValueChangeEvent<Double> event) { 
	    if (event.getValue() != null) {
	        currentValue = event.getValue();
	    }
    }
	
	/** the default value of the slider bar position. */
	private static final Double DEFAULT_VALUE = 50.0;
	
	/** The current value of the slider bar.  This is used only as a display in the survey editor, but is not
	 * stored to any properties.
	 */
	private Double currentValue = DEFAULT_VALUE;
	
	private SliderLabelPropertySet labelProps;
	
	/**
	 * collection of attributes for this question to be scored on
	 */
	private Set<AttributeScorerProperties> scoringAttributes;
	
	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
     * to alter the widget components.
     * @param isScored whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public SliderBarWidget(SurveyEditMode mode, boolean isScored) {
	    super(mode, isScored);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));

	    leftLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(event.getValue() != null && !event.getValue().isEmpty()){
					labelProps.getProperties().setPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY, event.getValue());					
				
				} else {
					labelProps.getProperties().removeProperty(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
				}
				
				onPropertySetChange(labelProps);
			}
		});
	    
	    rightLabel.addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(event.getValue() != null && !event.getValue().isEmpty()){
					labelProps.getProperties().setPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY, event.getValue());					
				
				} else {
					labelProps.getProperties().removeProperty(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY);
				}
				
				onPropertySetChange(labelProps);
			}
		});
	    
	    onEditorModeChanged(mode);
	}

    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {
        
    	super.onPropertySetChange(propSet);
        
        if (propSet instanceof SliderRangePropertySet) {
            SliderRangePropertySet rangeProps = (SliderRangePropertySet)propSet;
            
            slider.setMin(rangeProps.getSliderMinValue());
            slider.setMax(rangeProps.getSliderMaxValue());
            slider.setStep(rangeProps.getSliderStepValue());
            slider.setScale(rangeProps.getSliderScaleType().equals(SliderRange.ScaleType.LOGARITHMIC) 
                    ? ScaleType.LOGARITHMIC 
                    : ScaleType.LINEAR);
            
            currentValue = slider.getValue();
            
            logger.info("onPropertySetChange min(" + slider.getMin() + ", max(" + slider.getMax() + ")");
            
            updateTotalQuestionFlag();
            SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());

            
        } else if(propSet instanceof SliderLabelPropertySet){
        	
        	SliderLabelPropertySet labelSet = (SliderLabelPropertySet) propSet;
        	
        	updateSliderLabels(labelSet);
        }
        
    }
    
    /**
     * Updates the slider labels on the widget.
     * 
     * @param propSet - The property set containing the slider label values.
     */
    private void updateSliderLabels(SliderLabelPropertySet propSet) {
        Serializable left = propSet.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY);
        
        if(left != null && left instanceof String){
            leftLabel.setValue((String) left);
            
        } else {
            leftLabel.setValue(null);
        }
        
        Serializable right = propSet.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY);
        
        if(right != null && right instanceof String){
            rightLabel.setValue((String) right);
            
        } else {
            rightLabel.setValue(null);
        }
    }

    @Override
    protected void addCustomPropertySets() {  
    	
    	labelProps = new SliderLabelPropertySet();
        addPropertySet(labelProps);
        
        SliderRangePropertySet rangeProps = new SliderRangePropertySet();
        addPropertySet(rangeProps);
        
        HiddenAnswerWeightsPropertySet answerWeightsProps = new HiddenAnswerWeightsPropertySet();
        addPropertySet(answerWeightsProps);
        
        DifficultyAndConceptsPropertySet diffAndConceptSet = new DifficultyAndConceptsPropertySet();
        addPropertySet(diffAndConceptSet);
        
        scoringAttributes = ((QuestionScorer) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
        
    }

    @Override
	public void initializeWidget() {
        
        logger.info("initializeWidget()");

    }

    @Override
    public void refresh() {
        logger.info("refresh() slider widget");
        
        if (getEditMode() == SurveyEditMode.WritingMode) {
            questionHtml.setEditable(true);
            leftLabel.setEditingEnabled(true);
            rightLabel.setEditingEnabled(true);
            pointHeader.setVisible(false);
        } else {

            questionHtml.setEditable(false);
            leftLabel.setEditingEnabled(false);
            rightLabel.setEditingEnabled(false);
            pointHeader.setVisible(true);
        }
        
        // Update the widget with the last saved values.
        AbstractPropertySet propSet = getPropertySetByType(SliderRangePropertySet.class.getName());
        SliderRangePropertySet rangeProps = (SliderRangePropertySet)propSet;
        slider.setMin(rangeProps.getSliderMinValue());
        slider.setMax(rangeProps.getSliderMaxValue());
        slider.setStep(rangeProps.getSliderStepValue());
        slider.setScale(rangeProps.getSliderScaleType().equals(SliderRange.ScaleType.LOGARITHMIC) 
                ? ScaleType.LOGARITHMIC 
                : ScaleType.LINEAR);
        
        slider.setValue(currentValue);
        
        AbstractPropertySet absLabelSet = getPropertySetByType(SliderLabelPropertySet.class.getName());
        SliderLabelPropertySet labelSet = (SliderLabelPropertySet)absLabelSet;
        updateSliderLabels(labelSet);
        
        updateTotalQuestionFlag();
        SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
        
    }

    @Override
    public void onEditorModeChanged(SurveyEditMode mode) {
        super.onEditorModeChanged(mode);
        setEditMode(mode);
        
        if(SurveyEditMode.ScoringMode.equals(mode)){
	    	slider.setTooltip(TooltipType.SHOW);
	    
	    } else {
	        
	        /* #4453: Hide the slider tooltip in writing mode to better imitate what the learner will see during a course,
	         * particularly since numeric tooltips make no sense for non-numeric labels */
	    	slider.setTooltip(TooltipType.HIDE);
	    }
    }
    
    /**
     * Updates the QuestionScorer totalQuestion flag based on the current answerweights for the question.
     * This method should be called anytime a score event is fired and or anytime an event occurs that could change
     * the weights for the question (such as during the load process).
     */
    public void updateTotalQuestionFlag() {
       
        AbstractPropertySet propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
        HiddenAnswerWeightsPropertySet answerWeightsProps = (HiddenAnswerWeightsPropertySet) propSet;
        updateTotalQuestionFlag(answerWeightsProps.getProperties());
        
    }

    @Override
    public Double getPossibleTotalPoints() {
        
        // The possible total points of the slider bar widget is the max value of the slider.
        AbstractPropertySet propSet = getPropertySetByType(SliderRangePropertySet.class.getName());
        SliderRangePropertySet rangeProps = (SliderRangePropertySet)propSet;
        return SurveyScorerUtil.getHighestScoreSlider(rangeProps.getSliderRange());
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
                logger.info("Key name = " + key);
                
                logger.info("Key value = " + properties.getPropertyValue(key));
            }
        }
        
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
            
            

            if (surveyQuestion instanceof SliderSurveyQuestion) {
                SliderSurveyQuestion question = (SliderSurveyQuestion) surveyQuestion;
                SurveyItemProperties properties = question.getProperties();

                SliderQuestion sliderQuestion = question.getQuestion();
                
                logger.info("question text: " + sliderQuestion.getText());
                questionHtml.setValue(sliderQuestion.getText());

                // Debug print the properties from the abstractsurveyelement
                debugPrintQuestionProperties(properties);
                // print the slider question properties
                debugPrintQuestionProperties(sliderQuestion.getProperties());
                
                // Load the Common Properties
                AbstractPropertySet propSet = getPropertySetByType(CommonPropertySet.class.getName());
                CommonPropertySet commonProps = (CommonPropertySet) propSet;
                commonProps.load(properties);
                commonProps.load(sliderQuestion.getProperties());
                commonProps.setSurveyQuestion(surveyQuestion);
                
                // Load the Image Display Properties
                propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
                QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
                imageProps.load(properties);
                imageProps.load(sliderQuestion.getProperties());
                
                // Load the slider range properties (if any)
                propSet = getPropertySetByType(SliderRangePropertySet.class.getName());
                SliderRangePropertySet rangeProps = (SliderRangePropertySet)propSet;
                rangeProps.load(properties);
                rangeProps.load(sliderQuestion.getProperties());
                
                // Load the Slider label properties).
                labelProps.load(properties);
                labelProps.load(sliderQuestion.getProperties());
                
                // Load the answer weights Properties
                propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
                HiddenAnswerWeightsPropertySet answerWeightsProps = (HiddenAnswerWeightsPropertySet) propSet;
                answerWeightsProps.load(properties);
                answerWeightsProps.load(sliderQuestion.getProperties());
                
                // Load the difficulty and concepts Properties
                propSet = getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName());
                DifficultyAndConceptsPropertySet diffAndConceptsSet = (DifficultyAndConceptsPropertySet) propSet;
                diffAndConceptsSet.load(properties);
                diffAndConceptsSet.load(sliderQuestion.getProperties());
                
                scoringAttributes = ((QuestionScorer) answerWeightsProps.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
                
                populateSliderValues(rangeProps);
                
                updateTotalQuestionFlag();
                
                
                // This should be called after all property sets have been loaded for the abstractsurveyelement.
                addUnsupportedProperties(sliderQuestion.getProperties(), properties);
                onLoadNotifyPropertySetChanges();
                
                
                refresh();
            } else {
                throw new LoadSurveyException("Trying to load a SliderBar widget, but encountered non slider bar data from the database.", null);
            }
        } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type AbstractSurveyQuestion, but found: " + element.getClass().getName(), null);
        }
        
    }
    
    /**
     * Populates the min/max values for the slider and sets the current
     * value to the middle (Average of the min and max). Also populates
     * the left and right labels next to the slider
     * 
     * @param props the slider range property set
     */
    private void populateSliderValues(SliderRangePropertySet props){
    	
    	slider.setMin(props.getSliderMinValue());
    	slider.setMax(props.getSliderMaxValue());
    	slider.setStep(props.getSliderStepValue());
    	slider.setScale(props.getSliderScaleType().equals(SliderRange.ScaleType.LOGARITHMIC) 
                ? ScaleType.LOGARITHMIC 
                : ScaleType.LINEAR);
    	slider.setValue((props.getSliderMinValue() + props.getSliderMaxValue()) / 2.0);
    	
    	leftLabel.setValue((String) labelProps.getPropertyValue(SurveyPropertyKeyEnum.LEFT_EXTREME_LABEL_KEY));
        rightLabel.setValue((String) labelProps.getPropertyValue(SurveyPropertyKeyEnum.RIGHT_EXTREME_LABEL_KEY));
    	
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
    	logger.info("set scoring attributes = " + ((QuestionScorer) getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers());
    }
    
    /**
     * Returns the current list of scoring attributes as a string list so the 
     * question container's multiselect box can be populated on load
     * 
     * @return the string list of the attributes
     */
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
		return (String) getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName()).getProperties().getPropertyValue(SurveyPropertyKeyEnum.QUESTION_DIFFICULTY);
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
		this.isReadOnly = readOnly;

	    questionHtml.setEditable(!readOnly);
	    if (readOnly) {
	        questionHtml.setPlaceholder(READ_ONLY_MODE_PLACEHOLDER);
	        questionHtml.setTooltip(READ_ONLY_MODE_TOOLTIP);
	    }
	}

    @Override
    public void setPlaceholderResponseVisible(boolean visible) {
        //Nothing to do
    }
}
