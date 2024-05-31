/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.HashMap;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.client.ui.DescriptionTitle;
import org.gwtbootstrap3.client.ui.InlineCheckBox;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;

/**
 * The ScoringLogicWidgetCuiS is used for the Collect User Info Scored (CuiS) survey to 
 * control the rules of how the survey will be scored.
 * 
 * @author nblomberg
 *
 */
public class ScoringLogicAttributeWidget extends Composite  {

    private static Logger logger = Logger.getLogger(ScoringLogicAttributeWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, ScoringLogicAttributeWidget> {
	}

	/**
	 * contains 2 range buttons for a 3 value slider
	 */
	@UiField
	RangeSlider rangeScoringSlider;
	
	/**
	 * contains 1 range button for a 2 value slider
	 */
	@UiField
	Slider simpleScoringSlider;
	
	@UiField
	DescriptionData lowText;
	
	@UiField
	DescriptionData mediumText;
	
	@UiField
	DescriptionData highText;
	
	@UiField
	DescriptionData possiblePoints;
	
	@UiField
	Container rulesPanel;
	
	@UiField
	InlineCheckBox showPercentages;
	
	@UiField
	Label attributeNameLabel;
	
	@UiField
	ListBox scoredOnList;
	
	@UiField
	DescriptionTitle lowDescription;
	
	@UiField
	DescriptionTitle mediumDescription;
	
	@UiField
	DescriptionTitle highDescription;
	
	@UiField
	Column lowColumn;
	
	@UiField
	Column mediumColumn;
	
	@UiField
	Column highColumn;
	
	@UiHandler("scoredOnList")
    void onChangeScoredOnList(ChangeEvent event) {
	    logger.info("scoredOnList selection changed: " + scoredOnList.getSelectedValue());
	    
	    SharedResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
	}
	
    /** The current range value of the scoring logic widget. */
	private Range currentRange = new Range(DEFAULT_MIN_RANGE, DEFAULT_MAX_RANGE);
	
	/** The current double value of the simple scoring logic widget. */
	private Double currentValue = DEFAULT_VALUE;
	
	/** The possible total points for all scored items in a survey. */
	private Double possibleTotalPoints = 0.0;
	
	/** Default max range value. */
	public static final int DEFAULT_MAX_RANGE = 85;
	/** Default min range value. */
	public static final int DEFAULT_MIN_RANGE = 65;
	/** Default Value for simple slider */
	public static final Double DEFAULT_VALUE = 50.0;
	/** Maximum percentage allowed. */
	private static final int MAX_PERCENT = 100;
	/** Minimum percentage allowed. */
	private static final int MIN_PERCENT = 0;
	
	/** Label to indicate that the scoring level will not be used. */
	private static final String UNUSED_LABEL = "Unused";
	
	/** Label used to indicate that the scoring should count only for questions containing the same attribute. */
	private static final String SAME_ATTRIBUTE_LABEL = "Same Attribute";
	
	private SurveyWidgetId surveyWidgetId = new SurveyWidgetId();
	
	/** the attribute this scoring rule calculates */
	private LearnerStateAttributeNameEnum attribute;
	
	
	@UiHandler("rangeScoringSlider")
    void onRangeChange(ValueChangeEvent<Range> event) {
        logger.info("change, value = " + event.getValue());

        currentRange = event.getValue();
        updateScoringTableText(currentRange);
    }
	
	@UiHandler("simpleScoringSlider")
    void onValueChange(ValueChangeEvent<Double> event) {
        logger.info("change, value = " + event.getValue());

        currentValue = event.getValue();
        updateScoringTableText(currentValue);
    }
	
	@UiHandler("showPercentages")
	void onClickPercentages(ClickEvent event) {
	    logger.info("showPercentages: " + showPercentages.getValue());
	    
        if (attribute.getAttributeAuthoredValues().size() == 2) {
            // these learner state attributes only have 2 states (e.g. low/high), so use a single slider to separate 2 states
            updateScoringTableText(simpleScoringSlider.getValue());
        } else {
	        //use a double slider to separate 3 states for other learner state attributes (e.g. low/medium/high)
	    	updateScoringTableText(rangeScoringSlider.getValue());
	    }
	}

	/**
	 * Constructor (default)
	 */
	public ScoringLogicAttributeWidget(LearnerStateAttributeNameEnum attribute) {
	    
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    this.attribute = attribute;
	    this.attributeNameLabel.setText(attribute.getDisplayName());
	    
	    showPercentages.setValue(true);
	    
	    getElement().setId(surveyWidgetId.getWidgetId());
	    	    
	    refresh();
	}
	
	/**
	 * Return the value of the 'show percentages' check box that is located on the scoring panel.
	 * 
	 * @return true if the check box is checked
	 */
	public boolean isShowPercentages(){
	    return showPercentages.getValue();
	}
	
	/**
	 * Set the 'show percentages' check box value that is located on the scoring panel
	 * 
	 * @param value true to check the check box.
	 */
	public void setShowPercentages(boolean value){
	    showPercentages.setValue(value);
	}

	/**
	 * Updates the possible total point label for the widget.
	 */
    public void updatePossibleTotalPoints() {
	    
	    Double points = getPossibleTotalPoints();
        possiblePoints.setText(points.toString());        
    }

    /**
     * Updates the scoring table text based on the range value.
     * 
     * @param range - The range value of the scoring slider. 
     */
    private void updateScoringTableText(Range range) {
	   
        if (showPercentages.getValue()) {
            // Update Low text
            if (range.getMinValue() > MIN_PERCENT) {
                lowText.setText("score \u003C " + Math.ceil(range.getMinValue()) + " percent");
            } else {
                lowText.setText(UNUSED_LABEL);
            }
            
            if (range.getMinValue() != range.getMaxValue()) {
                
                mediumText.setText(Math.ceil(range.getMinValue()) + " percent \u2264 score \u003C " + Math.ceil(range.getMaxValue()) + " percent");
            } else {
                mediumText.setText(UNUSED_LABEL);
            }
            
            // Update High text
            if (range.getMaxValue() != MAX_PERCENT) {
                highText.setText(Math.ceil(range.getMaxValue()) + " percent \u2264 score \u2264 100 percent");
            } else {
                highText.setText("100 percent");
            }
        } else {
            
            Double totalPoints = getPossibleTotalPoints();
            
            
            // Update Low text
            if (range.getMinValue() > MIN_PERCENT) {
                Double lowPoints = totalPoints * range.getMinValue() / 100.0;
                lowText.setText("score \u003C " + lowPoints + " points");
            } else {
                lowText.setText(UNUSED_LABEL);
            }
            
            if (range.getMinValue() != range.getMaxValue()) {
                
                Double lowPoints = totalPoints * range.getMinValue() / 100.0;
                
                Double mediumPoints = totalPoints * range.getMaxValue() / 100.0;
                
                mediumText.setText(lowPoints + " points \u2264 score \u003C " + mediumPoints + " points");
            } else {
                mediumText.setText(UNUSED_LABEL);
            }
            
            // Update High text
            if (range.getMaxValue() != MAX_PERCENT) {
                Double mediumPoints = totalPoints * range.getMaxValue() / 100.0;
                
                
                highText.setText(mediumPoints + " points \u2264 score \u2264 " + totalPoints + " points");
            } else {
                highText.setText(totalPoints + " points");
            }
        }
	    
	}
    
    /**
     * Updates the scoring table text based on the simple double value.
     * 
     * @param value - The double value of the simple scoring slider. 
     */
    private void updateScoringTableText(Double value) {
    	
        if (showPercentages.getValue()) {
            // Update Low text
            if (value > MIN_PERCENT) {
                lowText.setText("score \u003C " + Math.ceil(value) + " percent");
            } else {
                lowText.setText(UNUSED_LABEL);
            }
            
            // Update High text
            if (value != MAX_PERCENT) {
                highText.setText(Math.ceil(value) + "  percent \u2264 score \u2264 100 percent");
            } else {
                highText.setText("100 percent");
            }
        } else {
            
            Double totalPoints = getPossibleTotalPoints();
            
            // Update Low text
            if (value > MIN_PERCENT) {
                Double lowPoints = totalPoints * value / 100.0;
                lowText.setText("score \u003C " + lowPoints + " points");
            } else {
                lowText.setText(UNUSED_LABEL);
            }
            
            // Update High text
            if (value != MAX_PERCENT) {
                Double mediumPoints = totalPoints * value / 100.0;
                highText.setText(mediumPoints + " points \u2264 score \u2264 " + totalPoints + " points");
            } else {
                highText.setText(totalPoints + " points");
            }
        }
	    
	}

    /**
     * Refreshes the widget based on the current state values.
     */
    public void refresh() {

        if (attribute.getAttributeAuthoredValues().size() == 2) {
            //these learner state attributes only have 2 states (e.g. low/high), so use a single slider to separate 2 states
    		simpleScoringSlider.setValue(currentValue);
    		//The SlideStopEvent is used to force the values from this widget to be written to the 
    		//corresponding attribute's properties. See Issue 3200 for more details.
    		SlideStopEvent.fire(simpleScoringSlider, currentValue);
	    	updateScoringTableText(simpleScoringSlider.getValue());
	    } else {
	        //use a double slider to separate 3 states for other learner state attributes (e.g. low/medium/high)
	    	rangeScoringSlider.setValue(currentRange);
	    	//The SlideStopEvent is used to force the values from this widget to be written to the 
            //corresponding attribute's properties. See Issue 3200 for more details.
            SlideStopEvent.fire(rangeScoringSlider, currentRange);
	    	updateScoringTableText(rangeScoringSlider.getValue());
	    }
        
        updatePossibleTotalPoints();

    }
    
    /**
     * Sets whether the elements in the widget should be enabled
     * 
     * @param enabled - Whether to enabled the elements in the widget
     */
    public void setEnabled(Boolean enabled) {
        scoredOnList.setEnabled(enabled);
        rangeScoringSlider.setEnabled(enabled);
        simpleScoringSlider.setEnabled(enabled);
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
     * Gets the possible total points that is displayed in the scoring header.
     * 
     * @return Double - The possible total points that is displayed in the scoring header.
     */
    public Double getPossibleTotalPoints() {
        return possibleTotalPoints;
    }

    /**
     * Initializes the component back to default values.
     */
    public void initialize() {
        possibleTotalPoints = 0.0;
        
    }

    /**
     * Get the survey widget id associated with this widget.
     * 
     * @return - The survey widget id for this widget.
     */
    public SurveyWidgetId getSurveyWidgetId() {
        return surveyWidgetId;
    }

   
    /**
     * Deletes the logic rule from the survey.
     */
    public void deleteLogicRule() {
        
        // Remove the logic rule widget from the ui.
        this.removeFromParent();
    }

    /**
     * Method used to determine if the logic widget is set to be scored on the same attribute.
     * 
     * @return - Returns true if the logic widget is set to be scored on the same attribute, false otherwise.
     */
    public boolean isScoredOnSameAttribute() {
        boolean isScoredOnSameAttribute = false;
        if (scoredOnList.getSelectedItemText().compareTo(SAME_ATTRIBUTE_LABEL) == 0) {
            isScoredOnSameAttribute = true;
        }
        return isScoredOnSameAttribute;
    }

    /**
     * Sets the total points value (for a specific attribute) on the scoring logic widget.
     * 
     * @param attributeScoreMap - The mapping of possible total points per attribute.
     */
    public void setTotalPointsPerAttribute(HashMap<LearnerStateAttributeNameEnum, Double> attributeScoreMap) {

        Double totalPoints = attributeScoreMap.get(attribute);
        
        if (totalPoints == null) {
            totalPoints = 0.0;
        } 
        setPossibleTotalPoints(totalPoints);      
    } 
    
    /**
     * Updates the descriptions for the scoring labels when certain attributes
     * are selected, i.e. Grit only has Low and High and Learning Style has several options,
     * none of them quantitative.<br/>
     * Note: currently only support 0,1,2, or 3 attribute values.
     *  
     * @param attribute The attribute to be scored on, decides which descriptions to use for scoring widget.
     * Can't be null.
     */
    public void updateScoringLevelDescriptions(LearnerStateAttributeNameEnum attribute){
        
        int valueSize = attribute.getAttributeAuthoredValues().size();
        
        if(valueSize <= 1){
            // the slider is not needed if there is zero or one attribute value possible
            rangeScoringSlider.getParent().setVisible(false);
            simpleScoringSlider.getParent().setVisible(false);
            return;
        }       
        
        if(valueSize > 2){
            // show the range scoring slider and hide the simple slider
            updateScoringTableText(rangeScoringSlider.getValue());
            rangeScoringSlider.getParent().setVisible(true);
            simpleScoringSlider.getParent().setVisible(false);

            lowDescription.setText(attribute.getAttributeAuthoredValues().get(0).getDisplayName());
            lowColumn.setSize("XS_4");
            lowColumn.setVisible(true);
            
            mediumDescription.setText(attribute.getAttributeAuthoredValues().get(1).getDisplayName());
            mediumColumn.setSize("XS_4");
            mediumColumn.setVisible(true);
            
            highDescription.setText(attribute.getAttributeAuthoredValues().get(2).getDisplayName());
            highColumn.setSize("XS_4");
            highColumn.setVisible(true);
            
        }else{
            // show the simple slider and hide the range scoring slider
            updateScoringTableText(simpleScoringSlider.getValue());
            rangeScoringSlider.getParent().setVisible(false);
            simpleScoringSlider.getParent().setVisible(true);
            
            lowDescription.setText(attribute.getAttributeAuthoredValues().get(0).getDisplayName());
            lowColumn.setSize("XS_6");
            lowColumn.setVisible(true);
            
            // hide the middle column because there are only 2 choices, 
            // and the low (left) and high (right) end points are necessary
            mediumColumn.setVisible(false);
            
            highDescription.setText(attribute.getAttributeAuthoredValues().get(1).getDisplayName());
            highColumn.setSize("XS_6");
            highColumn.setVisible(true);
        }

    }
    
    /**
     * Return the learner state attribute this scoring rule calculates.
     * 
     * @return the learner state attribute
     */
    public LearnerStateAttributeNameEnum getAttribute(){
        return attribute;
    }
    
    /**
     * Gets the scoring range slider bar
     * 
     * @return the scoring slider bar
     */
    public RangeSlider getRangeScoringSlider(){
    	return rangeScoringSlider;
    }
    
    /**
     * Gets the scoring range slider bar
     * 
     * @return the scoring slider bar
     */
    public Slider getSimpleScoringSlider(){
    	return simpleScoringSlider;
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
     * Gets the scoredOnList
     * 
     * @return the scoredOnList
     */
    public ListBox getScoredOnList() {
		return scoredOnList;
	}

    /**
     * Sets the current value for the simple scoring sliding bar
     * 
     * @param newValue the value to set the slider bar to
     */
	public void setCurrentValue(Double newValue) {
		currentValue = newValue;
	}

}
