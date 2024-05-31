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

import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.LoadSurveyException;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.QuestionImagePropertySet;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.TextSurveyElement;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;

/**
 * The informative text widget allows the author to write guidance/instructions/etc into the survey.
 * 
 * @author nblomberg
 *
 */
public class InformativeTextWidget extends AbstractQuestionWidget  {

    private static Logger logger = Logger.getLogger(InformativeTextWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	private static final String WRITING_MODE_PLACEHOLDER = "Click here to enter your informative text!";
	private static final String SCORING_MODE_PLACEHOLDER = "Switch to Writing Mode to change your informative text.";
	
	private static final String WRITING_MODE_TOOLTIP = "Click to edit";
	private static final String SCORING_MODE_TOOLTIP = "Switch to Writing Mode to edit informative text";
	
	interface WidgetUiBinder extends
			UiBinder<Widget, InformativeTextWidget> {
	}

	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
     * to alter the widget components.
	 */
	public InformativeTextWidget(SurveyEditMode mode) {
	    super(mode, false);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));

	    // The informative text widget doesn't contain the common properties.
	    removePropertySetByType(CommonPropertySet.class.getName());
	    
	    questionHtml.getElement().getStyle().clearFontWeight();
	    onEditorModeChanged(mode);
	}

    @Override
    public void onPropertySetChange(AbstractPropertySet propSet) {
        super.onPropertySetChange(propSet);
    }

    @Override
    protected void addCustomPropertySets() {
        // Nothing here for now, as this widget uses only the common base properties.
        
    }

    @Override
	public void initializeWidget() {
        // Nothing to do here yet as widgets are created in the binder.
        
    }

    @Override
    public void refresh() {
    	if(getEditMode() == SurveyEditMode.ScoringMode) {
            questionHtml.setEditable(false);
        } else {
            questionHtml.setEditable(true);
        }
    }

    @Override
    public void onEditorModeChanged(SurveyEditMode mode) {
        if(mode == SurveyEditMode.ScoringMode) {
            questionHtml.setPlaceholder(SCORING_MODE_PLACEHOLDER);
            questionHtml.setTooltip(SCORING_MODE_TOOLTIP);
            questionHtml.setEditable(false);
        } else {
            questionHtml.setPlaceholder(WRITING_MODE_PLACEHOLDER);
            questionHtml.setTooltip(WRITING_MODE_TOOLTIP);
            questionHtml.setEditable(true);
        }
        setEditMode(mode);
        
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
        
        if (element instanceof TextSurveyElement) {
            TextSurveyElement textElement = (TextSurveyElement) element;
            questionHtml.setValue(textElement.getText());
            
            // Load the Image Display Properties
            AbstractPropertySet propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
            QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
            imageProps.load(textElement.getProperties());
            onPropertySetChange(imageProps);
        } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type TextSurveyElement, but found: " + element.getClass().getName(), null);
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
