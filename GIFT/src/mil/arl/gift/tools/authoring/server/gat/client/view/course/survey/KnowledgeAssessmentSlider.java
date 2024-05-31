/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.survey;

import generated.course.ConceptQuestions.AssessmentRules;

import mil.arl.gift.common.gwt.client.widgets.BlockerPanel;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.DescriptionData;
import org.gwtbootstrap3.client.ui.Icon;
import org.gwtbootstrap3.client.ui.Tooltip;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.extras.slider.client.ui.Range;
import org.gwtbootstrap3.extras.slider.client.ui.RangeSlider;
import org.gwtbootstrap3.extras.slider.client.ui.base.constants.TooltipType;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopEvent;
import org.gwtbootstrap3.extras.slider.client.ui.base.event.SlideStopHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * A slider that reflects the criteria needed to meet Novice, Journeyman, and Expert assessment levels
 * 
 * @author bzahid
 */
public class KnowledgeAssessmentSlider extends Composite {
	
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, KnowledgeAssessmentSlider> {
	}
	
	@UiField
	protected FocusPanel headerButton;
	
	@UiField
	protected FlowPanel headerPanel;
	
	@UiField
	protected DeckPanel headerDeckPanel;
	
	@UiField
	protected HTML conceptNameLabel;
	
	@UiField
	protected HTML extraneousConceptNameLabel;
	
	@UiField
	protected Button deleteButton;
	
	@UiField
	protected Icon headerIcon;
	
	@UiField
	protected FlowPanel contentPanel;
	
	@UiField
	protected RangeSlider slider;
	
	@UiField 
	protected BlockerPanel blockerPanel;
	
	@UiField
	protected DescriptionData noviceText;
	
	@UiField
	protected DescriptionData journeymanText;
	
	@UiField
	protected DescriptionData expertText;
	
	@UiField
	protected Tooltip disabledTooltip;
	
	private Range currentValue;
	
	private int totalQuestions = 1;
	
	private String conceptName = null;
		
	@UiHandler("slider")
	void onValueChange(ValueChangeEvent<Range> event) { 
	    if (event.getValue() != null) {
	    	currentValue = event.getValue();
	    	if(currentValue.getMaxValue() != 100) {
	    		updateLabels();
	    	}
	    }
    }
	
	@UiHandler("slider")
	void onSlideStop(SlideStopEvent<Range> event) { 
	    if (event.getValue() != null) {
	    	currentValue = event.getValue();
	    	
	    	if(totalQuestions == 1) {
	    		setValue(50, 50);
	    	} else {
	    		setValue(currentValue.getMinValue(), currentValue.getMaxValue());
	    	}
	    }
    }
	
	@UiHandler("slider")
	void onAttachOrDetach(AttachEvent event) {
		if(event.isAttached()) {
			if(currentValue != null) {
				slider.setValue(currentValue);
			}
		}
	}
	
	/**
	 * Constructor
	 * 
	 * @param conceptName The name of the concept to associate with this slider
	 */
	public KnowledgeAssessmentSlider(String conceptName) {
		initWidget(uiBinder.createAndBindUi(this));
		
		blockerPanel.block();			
		headerDeckPanel.showWidget(0);
	    currentValue = slider.getValue();
	    slider.setTooltip(TooltipType.HIDE);
	    disabledTooltip.setTitle("Add another question to edit rules");
	    
	    headerButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				contentPanel.setVisible(!contentPanel.isVisible());
				if(!contentPanel.isVisible()) {
					contentPanel.getElement().getStyle().setProperty("border-radius", "6px");
					headerPanel.getElement().getStyle().setProperty("border-radius", "6px");
					headerIcon.setType(IconType.PLUS_CIRCLE);
				} else {
					contentPanel.getElement().getStyle().setProperty("border-radius", "4px 4px 0 0");
					headerPanel.getElement().getStyle().setProperty("border-radius", "4px 4px 0 0");
					headerIcon.setType(IconType.MINUS_CIRCLE);
				}
			}
		});
	    
		setConceptName(conceptName);
	}
	
	/**
	 * Applies styling to the slider widget that indicates it is associated with a missing concept.
	 * 
	 * @param extraneous True to apply extraneous styling, false to remove it.
	 */
	public void setExtraneous(boolean extraneous) {
		if(extraneous) {
			this.getElement().getStyle().setProperty("background", "linear-gradient(#fff, #e2dada 97%)");
			this.getElement().getStyle().setProperty("border", "solid 1px #ad9090");
			extraneousConceptNameLabel.setText(conceptName);
			headerDeckPanel.showWidget(1);
			blockerPanel.block();
		} else {
			this.getElement().getStyle().clearProperty("background");
			this.getElement().getStyle().clearProperty("border");
			headerDeckPanel.showWidget(0);
			blockerPanel.unblock();
		}
	}
	
	/**
	 * Sets the click handler to execute when the user clicks the delete button 
	 * on an extraneous concept slider
	 * 
	 * @param removeHandler The click handler to execute
	 */
	public void setRemoveHandler(ClickHandler removeHandler) {
		deleteButton.addClickHandler(removeHandler);
	}
	
	/**
	 * Adds a handler to execute when a slider is stopped
	 * 
	 * @param handler The handler to execute when a slider is stopped
	 */
	public void addSlideStopHandler(SlideStopHandler<Range> handler) {
		slider.addSlideStopHandler(handler);
	}
	
	/**
	 * Sets the range of the slider using the concept question's assessment rules
	 * 
	 * @param assessmentRules The assessment rules of the concept question
	 */
	public void setRange(int totalQuestions, AssessmentRules assessmentRules) {
		double min = 50;
		double max = 50;
		this.totalQuestions = totalQuestions;
		
		if(totalQuestions > 1) {			
            int atExpectation = assessmentRules.getAtExpectation().getNumberCorrect().intValue();
            int aboveExpectation = assessmentRules.getAboveExpectation().getNumberCorrect().intValue();

            // If journeyman is not being used
			if(atExpectation == 0) {
			    // Since belowExpectation is always ZERO, if journeyman is unused, min equals max
                min = (100D * aboveExpectation) / totalQuestions;
			} else {
			    // Checks in setValue ensure min will not equal zero
				min = (100D * atExpectation) / totalQuestions;
			}

            max = (100D * aboveExpectation ) / totalQuestions;
		} 
		
		setValue(min, max);
	}
	
	/**
	 * Sets the value of the slider and updates the labels
	 * 
	 * @param min The min value of the slider
	 * @param max The max value of the slider
	 */
	private void setValue(double min, double max) {

	    // Verify max and min values do not exceed 100
	    if (max >= 100) {
	        max = 100;
	    }
	    if (min >= 100) {
	        min = 100;
	    }
		
		if(slider.getValue().getMinValue() != min || slider.getValue().getMaxValue() != max) {
            Range range = new Range(min, max);
			currentValue = range;
			slider.setValue(range, false);
		}
		
		if(totalQuestions == 1) {
			currentValue = new Range(50, 50);
			slider.setValue(currentValue, false);
		} else {
			boolean setValue = false;
			// Ensure min value does not equal 0 so our math works later
			if(currentValue.getMinValue() == 0) {
				currentValue = new Range(1, currentValue.getMaxValue());
				setValue = true;
			}			
			// Ensure min value does not equal 100 so our math works later
			if(currentValue.getMinValue() == 100) {
				currentValue = new Range(99, currentValue.getMinValue());
				setValue = true;
			}			
			// Ensure max value does not equal 0 so our math works later
			if(currentValue.getMaxValue() == 0) {
				currentValue = new Range(currentValue.getMinValue(), 1);
				setValue = true;
			}
			// Ensure max value does not equal 100 so our math works later
			if(currentValue.getMaxValue() == 100) {
				currentValue = new Range(currentValue.getMinValue(), 99);
				setValue = true;
			}
			if(setValue) {
				slider.setValue(currentValue, false);
			}
		}
		
		if(totalQuestions == 1) {
			disabledTooltip.setTitle("Add another question to edit rules");
			blockerPanel.block();
			
		} else if(!GatClientUtility.isReadOnly()) {
			disabledTooltip.setTitle("");
			blockerPanel.unblock();
		}
		
		updateLabels();
	}
	
	/**
	 * Sets the name of the concept associated with this slider
	 * 
	 * @param conceptName The name of the concept associated with this slider
	 */
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
		conceptNameLabel.setText(conceptName);
	}
	
	/**
	 * Gets the name of the concept associated with this slider
	 * 
	 * @return the name of the concept associated with this slider
	 */
	public String getConceptName() {
		return conceptName;
	}

	/**
	 * Updates the slider descriptions
	 */
	private void updateLabels() {
		
	    int[] level = calculateAllLevels();
	    
		int novicePoints = level[0];
		int journeymanPoints = level[1];
		int expertPoints = level[2];
		
		if(novicePoints > 0) { 
			noviceText.setText("0-" + novicePoints + " correct");
		} else {
			noviceText.setText("0 correct");
		}
		
		if(currentValue.getMinValue() == currentValue.getMaxValue() || journeymanPoints == 0) {
			journeymanText.setText("Unused");
		} else {
			if(journeymanPoints < expertPoints - 1) {
				journeymanText.setText(journeymanPoints + "-" + (expertPoints - 1) + " correct");
			} else {
				journeymanText.setText(journeymanPoints + " correct");
			}
		}
		
		if(expertPoints < totalQuestions) {
			expertText.setText(expertPoints + "-" + totalQuestions + " correct");
		} else {
			expertText.setText(expertPoints + " correct");
		}
	}
	
	/**
	 * Calulates the values of answers to meet each level, representing the following:
	 * 
	 * Novice - the maximum number of questions needed to be considered novice
	 * Journeyman - the minimum number of questions needed to be considered journeyman
	 * Expert - the minimum number of questions needed to be considered expert
	 * 
	 * @return int[] level - an array containing 3 values. 0 = Novice, 1 = Journeyman, 2 = Expert
	 */
	public int[] calculateAllLevels() {
	    
	    int[] level = new int[3];

	    // This should not happen but verify min>0 and max<100 just in case.
        if(currentValue.getMinValue() <= 0) {
            currentValue = new Range(1, currentValue.getMaxValue());
        }			
        if(currentValue.getMaxValue() >= 100) {
            currentValue = new Range(currentValue.getMinValue(), 99);
        }
		
		// Calculate Journeyman
		// 
		// If journeyman is not being used then set it equal to 0.
		if(currentValue.getMinValue() == currentValue.getMaxValue()) {
            level[1] = 0;
		} else {
            Double journeyman = totalQuestions * (currentValue.getMinValue() / 100D);
		
            // Journeyman value cannot be 0 if it's being used; if it is, add 1.
            if(journeyman.intValue() == 0) {
                journeyman += 1;
            }

            level[1] = journeyman.intValue();
		}
		
		// Calculate Expert
		//
		// Previous logic ensures that if currentValue.getMaxValue() = 100, it will be set to 99.
		// This means that expert will be at most totalQuestions * 0.99, in other words
		// expert level will never be equal to totalQuestions when converted to an int.
		// Since the expert level is always used in order to continue, if currentValue.getMaxValue() = 99,
		// add 1 to the int value of expert to guarantee it's always at least 1.
		int addOne = (currentValue.getMaxValue() == 99 ? 1 : 0);
		Double expert = totalQuestions * (currentValue.getMaxValue() / 100D);
		level[2] = expert.intValue() + addOne;

	    // Calculate Novice
		Double novice = 0D;
		// If journeyman is not being used, then novice is expert - 1
		if (currentValue.getMinValue() == currentValue.getMaxValue()) {
		    novice = (double) (level[2] - 1);
		} else { // otherwise novice is journeyman - 1
		    novice = (double) (level[1] - 1);
		}
		
		level[0] = novice.intValue();
		
		// Verify journeyman value does not equal expert value
		if (level[1] == level[2]) {
		    // If it does, add 1 to expert value if it's less than the total number of questions
		    if (level[2] + 1 <= totalQuestions) {
                level[2] += 1;
		    } else { // Otherwise subtract 1 from journeyman value
		        level[1] -= 1;
		    }
		}
		
		// Verify novice value does not equal expert value
		if (level[0] == level[2]) {
		    // If journeyman is being used...
		    if (level[1] > 0) {
		        // Expert value equals journeyman + 1 if it's less than the total number of questions
		        if (level[1] + 1 <= totalQuestions) {
		            level[2] = level[1] + 1;
		        }
		    } else { // Otherwise...
                // Add 1 to the expert value if it's less than the total number of questions
                if (level[2] + 1 <= totalQuestions) {
                    level[2] += 1;
                } else if (level[0] - 1 >= 0) { // Otherwise subtract 1 from the novice value so long as it's greater than or equal to 0
                    level[0] -= 1;
                }
		    }
        }
		
	    return level;
	}
}
