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

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.ExpertiseLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.OperatorEnum;
import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.common.survey.Survey;
import mil.arl.gift.common.survey.score.AttributeScorerProperties;
import mil.arl.gift.common.survey.score.ReturnValueCondition;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.widgets.CollapseButton;

/**
 * The KasScoringLogicWidget is used for the Knowledge Assessment (Static) survey to 
 * control the rules of how the survey will be scored.
 * 
 * @author nblomberg
 *
 */
public class KasScoringLogicWidget extends Composite  {

    private static Logger logger = Logger.getLogger(KasScoringLogicWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, KasScoringLogicWidget> {
	}

	
	@UiField
	RangeSlider scoringSlider;
	
	@UiField
	DescriptionData noviceText;
	
	@UiField
	DescriptionData journeymanText;
	
	@UiField
	DescriptionData expertText;
	
	@UiField
	DescriptionData possiblePoints;
	
	@UiField
	DescriptionData scoredAttribute;
	
	@UiField
	CollapseButton collapseButton;
	
	@UiField
	Container rulesPanel;
	
	@UiField
	InlineCheckBox showPercentages;
	
	@UiField
	protected BlockerPanel rulesBlocker;
	
	/** The current range value of the scoring logic widget. */
	private Range currentRange;
	
	/** The possible total points for all scored items in a survey. */
	private Double possibleTotalPoints = 0.0;
	
	/** Default max range value. */
	private static final int DEFAULT_MAX_RANGE = 85;
	/** Default min range value. */
	private static final int DEFAULT_MIN_RANGE = 65;
	/** Maximum percentage allowed. */
	private static final int MAX_PERCENT = 100;
	/** Minimum percentage allowed. */
	private static final int MIN_PERCENT = 0;
	
	/** Label to indicate that the scoring level will not be used. */
	private static final String UNUSED_LABEL = "Unused";
	
	/**
	 * The Survey being loaded into the widget (also represents 
	 * the newly created survey object when authoring a new survey)
	 */
	private Survey loadedSurvey;
	
	/** Flag to indicate if the widget has been initialized. */
	private boolean initializedWidget = false;
	
	@UiHandler("scoringSlider")
    void onRangeChange(ValueChangeEvent<Range> event) {
        logger.info("change, value = " + event.getValue());

        currentRange = event.getValue();
        updateReturnConditions();
        updateScoringTableText(event.getValue());
    }
	
	@UiHandler("showPercentages")
	void onClickPercentages(ClickEvent event) {
	    logger.info("showPercentages: " + showPercentages.getValue());
	    updateScoringTableText(scoringSlider.getValue());
	}

	/**
	 * Constructor (default)
	 */
	public KasScoringLogicWidget() {
	    
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
        collapseButton.addMouseDownHandler(new MouseDownHandler() {

            @Override
            public void onMouseDown(MouseDownEvent event) {
                logger.info("scoring logic widget clicked. - set visiblity to "+!collapseButton.isCollapsed());
                
                setRulesPanelVisibility(!collapseButton.isCollapsed());
            }
        });
	    
	    setDefaults();	 
	    updateScoredAttributeLabel();
	}
	
	/**
	 * Sets the visibility of the rules panel.
	 * 
	 * @param visible - true to show the rules panel, false to hide it.
	 */
	private void setRulesPanelVisibility(boolean visible) {
	    rulesPanel.setVisible(visible);
	}
	
	
	/**
	 * Updates the scored attribute label for the widget. 
	 */
	private void updateScoredAttributeLabel() {
	    scoredAttribute.setText(LearnerStateAttributeNameEnum.KNOWLEDGE.getDisplayName());
        
    }

	/**
	 * Updates the possible total point label for the widget.
	 */
    public void updatePossibleTotalPoints() {
	    
	    Double points = getPossibleTotalPoints();
	    
	    if(points != null){
	    	possiblePoints.setText(points.toString());
	    }
        
    }

    /**
     * Updates the scoring table text based on the range value.
     * 
     * @param range - The range value of the scoring slider. 
     */
    private void updateScoringTableText(Range range) {
	   
        if (showPercentages.getValue()) {
            // Update novice text
            if (range.getMinValue() > MIN_PERCENT) {
                noviceText.setText("score \u003C " + range.getMinValue() + " percent");
            } else {
                noviceText.setText(UNUSED_LABEL);
            }
            
            if (range.getMinValue() != range.getMaxValue()) {
                
                journeymanText.setText(range.getMinValue() + " percent \u2264 score \u003C " + range.getMaxValue() + " percent");
            } else {
                journeymanText.setText(UNUSED_LABEL);
            }
            
            // Update Expert text
            if (range.getMaxValue() != MAX_PERCENT) {
                expertText.setText(range.getMaxValue() + " percent \u2264 score \u2264 100 percent");
            } else {
                expertText.setText(UNUSED_LABEL);
            }
        } else {
            
            Double totalPoints = getPossibleTotalPoints();
            
            if(totalPoints != null){
            	
	            // Update novice text
	            if (range.getMinValue() > MIN_PERCENT) {
	                Double novicePoints = totalPoints * range.getMinValue() / 100.0;
	                noviceText.setText("score \u003C " + novicePoints + " points");
	            } else {
	                noviceText.setText(UNUSED_LABEL);
	            }
	            
	            if (range.getMinValue() != range.getMaxValue()) {
	                
	                Double novicePoints = totalPoints * range.getMinValue() / 100.0;
	                
	                Double journeyPoints = totalPoints * range.getMaxValue() / 100.0;
	                
	                journeymanText.setText(novicePoints + " points \u2264 score \u003C " + journeyPoints + " points");
	            } else {
	                journeymanText.setText(UNUSED_LABEL);
	            }
	            
	            // Update Expert text
	            if (range.getMaxValue() != MAX_PERCENT) {
	                Double journeyPoints = totalPoints * range.getMaxValue() / 100.0;
	                
	                
	                expertText.setText(journeyPoints + " points \u2264 score \u2264 " + totalPoints + " points");
	            } else {
	                expertText.setText(UNUSED_LABEL);
	            }
            }
        }
	    
	}

    /**
     * Refreshes the widget based on the current state values.
     * 
     * @param forceRefresh
     *            boolean flag to force a refresh of the point values. True
     *            means the values always refresh; false means they only refresh
     *            if the show percentages check box is selected.
     */
    public void refresh(boolean forceRefresh) {
        
        if (initializedWidget) {
            Set<AttributeScorerProperties> attribList = getAttributeScorerProperties(); 
            AttributeScorerProperties kasScorerProperties = attribList.iterator().next();
            if (kasScorerProperties.getReturnConditions().size() < 2) {
                kasScorerProperties.setReturnConditions(SurveyEditorPanel.createReturnValue(LearnerStateAttributeNameEnum.KNOWLEDGE));
            }

            if (forceRefresh || !showPercentages.getValue()) {
                // need to update point values based on new possible total points
                // Note: percentages shouldn't be changed based on a change in
                // total points rather only when the author changes the sliders

                Double minValue;
                Double maxValue;

                if (getPossibleTotalPoints() != null && getPossibleTotalPoints() != 0) {
                    // convert to percent
                    minValue = Math.rint((kasScorerProperties.getReturnConditions().get(1).getValue() / getPossibleTotalPoints()) * 100);
                    maxValue = Math.rint((kasScorerProperties.getReturnConditions().get(0).getValue() / getPossibleTotalPoints()) * 100);
                } else {
                    minValue = kasScorerProperties.getReturnConditions().get(1).getValue();
                    maxValue = kasScorerProperties.getReturnConditions().get(0).getValue();
                }

                logger.info("Called refresh() for kasScoringLogicWidget with range = [" + minValue + "," + maxValue + "] and total possible points = " + getPossibleTotalPoints());
                currentRange = new Range(minValue, maxValue);
            }

            // update scoring slider data
            scoringSlider.setValue(currentRange);
            updateScoringTableText(scoringSlider.getValue());
            
            // update points and level thresholds
            updateReturnConditions();
            updatePossibleTotalPoints();
            setRulesPanelVisibility(!collapseButton.isCollapsed());
        }
    }

    /**
     * Set the possible total points to be displayed in the scoring header.
     * 
     * @param totalPoints - The total points to be displayed.
     */
    public void setPossibleTotalPoints(Double totalPoints) {
        possibleTotalPoints = totalPoints;
    }
    
    /** 
     * Updates the return value conditions of the attribute scorer properties based on the current range.
     */
    public void updateReturnConditions() {
    	Double max;
    	Double min;

    	if(getPossibleTotalPoints() != null && getPossibleTotalPoints() != 0) {
    		max = (currentRange.getMaxValue() * getPossibleTotalPoints()) / 100;
    		min = (currentRange.getMinValue() * getPossibleTotalPoints()) / 100;
    	} else {
    		max = currentRange.getMaxValue();
    		min = currentRange.getMinValue();
    	}

    	Set<AttributeScorerProperties> attribList = getAttributeScorerProperties();
    	AttributeScorerProperties kasScorerProperties = attribList.iterator().next();
    	kasScorerProperties.getReturnConditions().clear();
    	kasScorerProperties.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, max, ExpertiseLevelEnum.EXPERT));
    	kasScorerProperties.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, min, ExpertiseLevelEnum.JOURNEYMAN));
    	kasScorerProperties.getReturnConditions().add(new ReturnValueCondition(OperatorEnum.GTE, 0, ExpertiseLevelEnum.NOVICE));
    }
    
    /**
     * Gets the possible total points that is displayed in the scoring header.
     * 
     * @return Double - The possible total points that is displayed in the scoring header.
     */
    public Double getPossibleTotalPoints() {
        return possibleTotalPoints;
    }
    
    /**
     * Gets the scoring slider bar
     * 
     * @return the scoring slider bar
     */
    public RangeSlider getScoringSlider() {
        return scoringSlider;
    }

    /**
     * Initializes the component back to default values.
     * @param survey - the survey that is being initialized.
     * @param isNewSurvey - True if the survey being initialized is a new survey, false if it is an existing survey.
     */
    public void initialize(Survey survey, boolean isNewSurvey) {      
        this.loadedSurvey = survey;
                
        initializedWidget = false;

        if(isNewSurvey){
            initializeScoringLogicWidget();
        }
    }
    
    /**
     * Creates and initializes the logic widget. The widget is defaulted to
     * assessing on Total for the attribute of Knowledge. The survey totalScorer
     * property is updated appropriately as well.
     */
    private void initializeScoringLogicWidget() {
        setDefaults();
        
        Set<AttributeScorerProperties> attribList = getAttributeScorerProperties();
        attribList.add(new AttributeScorerProperties(LearnerStateAttributeNameEnum.KNOWLEDGE, SurveyEditorPanel.createReturnValue(LearnerStateAttributeNameEnum.KNOWLEDGE)));
        updateReturnConditions();
        
        initializedWidget = true;
    }
    
    /**
     * Gets the AttributeScorerProperties list for the Static Knowledge Assessment survey.
     * This list is always retreived from the TotalScorer object since this type of survey
     * is always assessed on total with an attribute of knowledge.
     * @return the collection of attribute scorer properties
     */
    public Set<AttributeScorerProperties> getAttributeScorerProperties() {
        return loadedSurvey.getScorerModelForNewSurvey().getTotalScorer().getAttributeScorers();
    }
    
    /**
     * Setting default values.
     */
   private void setDefaults() {
       possibleTotalPoints = 0.0;
       showPercentages.setValue(true);
       currentRange = new Range(DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE);
   }
	
    /**
   	 * Sets whether or not this widget should be read-only
   	 * 
   	 * @param readOnly whether or not this widget should be read-only
   	 */
   	public void setReadOnlyMode(boolean readOnly) {
   		rulesBlocker.setVisible(readOnly);
   	}
   	
   	/**
     * Sets the current range on the scoring slider bar
     * 
     * @param range the range to set the slider bar to
     */
    public void setCurrentRange(Range range){
        this.currentRange = range;
    }
    
    /**
     * Called when an existing survey is loaded up with scoring attributes.
     * creates a scoring widget for each scoring attribute
     * 
     * @param survey the existing survey being loaded
     */
    public void load(Survey survey) {
        logger.info("load()");
        this.loadedSurvey = survey;
        
        if (loadedSurvey != null) {
            if (getAttributeScorerProperties().isEmpty()) {
                // Handles loading surveys created in the old SAS with scoring enabled and no attributes
                logger.info("Scorer Attributes are empty, adding blank Knowledge attribute");
                initializeScoringLogicWidget();
            } else {
                // Now that we have "loaded" the survey, mark initialized as true and refresh.
                initializedWidget = true;
                refresh(true);
            }
        }
    }
}
