/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.Set;
import java.util.logging.Logger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.FillInTheBlankQuestion;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props.AnswerFieldTextBoxPropertySet;

/**
 * The EssayWidget allows the survey author to create an essay response item in the survey.
 * 
 * @author nblomberg
 *
 */
public class EssayWidget extends AbstractQuestionWidget  {

    private static Logger logger = Logger.getLogger(EssayWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, EssayWidget> {
	}

	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
     * to alter the widget components.
     * @param isScored whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public EssayWidget(SurveyEditMode mode) {
	    super(mode, false);
	    
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    onEditorModeChanged(mode);
	}

    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {
       
    	super.onPropertySetChange(propSet);
    }

    @Override
    protected void addCustomPropertySets() {
    	
        AnswerFieldTextBoxPropertySet propSet = new AnswerFieldTextBoxPropertySet();
        propSet.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY, false);
        
        addPropertySet(propSet);
    }

    @Override
	public void initializeWidget() {
        // Nothing to do here as widgets are created in the binder.
        
    }

    @Override
    public void refresh() {
        // TODO - update the widget based on which mode (scoring mode/writing mode).
        
    }

    @Override
    public void onEditorModeChanged(SurveyEditMode mode) {
        super.onEditorModeChanged(mode);
        setEditMode(mode);
        if(mode == SurveyEditMode.ScoringMode) {
        	questionHtml.setEditable(false);
        } else {
        	questionHtml.setEditable(true);
        }
    }

    @Override
    public Double getPossibleTotalPoints() {
        return 0.0;
    }

	@Override
	protected void setIsScoredType() {
		isScoredType = false;
	}

    @Override
    public void load(AbstractSurveyElement element) throws LoadSurveyException {
        
        if (element instanceof AbstractSurveyQuestion) {
            @SuppressWarnings("unchecked")
            AbstractSurveyQuestion<? extends AbstractQuestion> surveyQuestion = (AbstractSurveyQuestion<? extends AbstractQuestion>) element;
            
            if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {
                
                FillInTheBlankSurveyQuestion question = (FillInTheBlankSurveyQuestion) surveyQuestion;
                SurveyItemProperties properties = question.getProperties();

                FillInTheBlankQuestion essayQuestion = question.getQuestion();
                
                logger.info("question text: " + essayQuestion.getText());
                questionHtml.setValue(essayQuestion.getText());

                // print the question properties
                debugPrintQuestionProperties(properties);
                
                // Load the properties (if any)
                AbstractPropertySet propSet = getPropertySetByType(AnswerFieldTextBoxPropertySet.class.getName());
                AnswerFieldTextBoxPropertySet answerFieldProps = (AnswerFieldTextBoxPropertySet)propSet;
                answerFieldProps.load(properties);
                answerFieldProps.load(essayQuestion.getProperties());
                
                // Load the Common Properties
                propSet = getPropertySetByType(CommonPropertySet.class.getName());
                CommonPropertySet commonProps = (CommonPropertySet) propSet;
                commonProps.load(properties);
                commonProps.load(essayQuestion.getProperties());
                commonProps.setSurveyQuestion(surveyQuestion);
                
                // Load the Image Display Properties
                propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
                QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
                imageProps.load(properties);
                imageProps.load(essayQuestion.getProperties());
                
                // This should be called after all property sets have been loaded for the abstractsurveyelement.
                addUnsupportedProperties(essayQuestion.getProperties(), properties);
                onLoadNotifyPropertySetChanges();
                
                // print the question properties
                debugPrintQuestionProperties(essayQuestion.getProperties());
                
                refresh();
            } else {
                throw new LoadSurveyException("Trying to load a SliderBar widget, but encountered non slider bar data from the database.", null);
            }
        } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type AbstractSurveyQuestion, but found: " + element.getClass().getName(), null);
        }
        
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
    public void setScorerProperty(Set<AttributeScorerProperties> attributes) {
        // do nothing since this is not scored.
        
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
