/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.survey;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Date;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.gwtbootstrap3.extras.slider.client.ui.Slider;
import org.gwtbootstrap3.extras.slider.client.ui.base.constants.ScaleType;
import org.gwtbootstrap3.extras.slider.client.ui.base.constants.TooltipType;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.survey.AbstractQuestionResponse;
import mil.arl.gift.common.survey.AbstractQuestionResponseMetadata;
import mil.arl.gift.common.survey.QuestionResponseElement;
import mil.arl.gift.common.survey.SliderRange;
import mil.arl.gift.common.survey.SliderSurveyQuestion;
import mil.arl.gift.common.survey.SurveyProperties;

/**
 * A widget for a slider survey question
 *
 * @author jleonard
 */
public class SliderQuestionWidget extends AbstractSurveyQuestionWidget<SliderSurveyQuestion> {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(SliderQuestionWidget.class.getName());

    private HorizontalPanel answerArea = new HorizontalPanel();

    private Date timeAnswered = null;
        
    private final Slider slider = new Slider(0, 100, 50) {

    	Double sliderValue;
    	
    	@Override 
    	public void setValue(Double value, boolean fireEvents) {
    		super.setValue(value, fireEvents);
    		sliderValue = value;
    	}
    	
    	@Override
    	public Double getValue() {
    		return sliderValue;
    	}
    };
    
    /**
     * Constructor, creates a widget for answering a slider question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The slider question to ask
     * @param questionNumber The number of the question on the page
     * @param isBeingEdited If this widget is for a question being modified 
     */
    public SliderQuestionWidget(SurveyProperties surveyProperties, SliderSurveyQuestion surveyQuestion, int questionNumber, boolean isBeingEdited) {
        super(surveyProperties, surveyQuestion, questionNumber, isBeingEdited);

        answerArea.setSpacing(10);
        answerArea.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);

        slider.setTooltip(TooltipType.HIDE);
        slider.setPrecision(0);
 
        /* Default to linear scale */
        slider.setScale(ScaleType.LINEAR);
        slider.setStep(1.0);
        
        /* If different slider range values have been provided for the slider, update the slider with the values. */
        if (surveyQuestion.getSliderRange() != null) {
            slider.setMin(surveyQuestion.getSliderRange().getMinValue());
            slider.setMax(surveyQuestion.getSliderRange().getMaxValue()); 
            slider.setStep(surveyQuestion.getSliderRange().getStepSize() > 1.0 
                    ? surveyQuestion.getSliderRange().getStepSize() : 1.0);
            slider.setScale(surveyQuestion.getSliderRange().getScaleType().equals(SliderRange.ScaleType.LOGARITHMIC)
                    ? ScaleType.LOGARITHMIC
                    : ScaleType.LINEAR);
        }

        slider.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent ae) {

                if (ae.isAttached()) {

                    // Add the handler after the attach since it captures events during initialization
                    slider.addValueChangeHandler(new ValueChangeHandler<Double>() {
                        @Override
                        public void onValueChange(ValueChangeEvent<Double> event) {
                        	slider.setValue(event.getValue(), false);
                            timeAnswered = new Date();
                        }
                    });
                    
                    // Hack to make the slider bar's slider have a neutral z-index
                    NodeList<Element> divNodes = slider.getElement().getElementsByTagName("div");

                    for (int divIndex = 0; divIndex < divNodes.getLength(); ++divIndex) {

                        Element divNode = divNodes.getItem(divIndex);

                        if (divNode != null) {

                            NodeList<Element> imgNodes = slider.getElement().getElementsByTagName("img");

                            for (int imgIndex = 0; imgIndex < imgNodes.getLength(); ++imgIndex) {

                                Element imgNode = imgNodes.getItem(imgIndex);

                                if (imgNode != null) {
                                    
                                    imgNode.getStyle().setZIndex(0);
                                }
                            }
                        }
                    }
                }
            }
        });
      
        if (getSurveyElement().getQuestion().getLeftLabel() != null && !getSurveyElement().getQuestion().getLeftLabel().isEmpty()) {

            answerArea.add(new Label(getSurveyElement().getQuestion().getLeftLabel()));
        }
        
        FlowPanel sliderPanel = new FlowPanel();
        sliderPanel.add(slider);
        sliderPanel.addStyleName(SurveyCssStyles.SURVEY_QUESTION_SLIDER_BAR);

        answerArea.add(sliderPanel);
        

        if (getSurveyElement().getQuestion().getRightLabel() != null && !getSurveyElement().getQuestion().getRightLabel().isEmpty()) {

            answerArea.add(new Label(getSurveyElement().getQuestion().getRightLabel()));
        }

        addAnswerArea(answerArea, true);
    }

    /**
     * Constructor, creates a widget for reviewing the response to a slider
     * question
     *
     * @param surveyProperties The properties of the survey this question is in
     * @param surveyQuestion The slider question to ask
     * @param questionNumber The number of the question on the page
     * @param responseMetadata The metadata of the response to this question
     */
    public SliderQuestionWidget(SurveyProperties surveyProperties, SliderSurveyQuestion surveyQuestion, int questionNumber, AbstractQuestionResponseMetadata responseMetadata) {
        this(surveyProperties, surveyQuestion, questionNumber, true);

        if (!responseMetadata.getResponses().isEmpty()) {
            boolean showPoints = canShowPointsEarned();

            if (responseMetadata.getResponses().size() > 1) {

                GWT.log("Warning: Got multiple responses back for a slider question. Expecting only 1, using the first response by default.");
            }
            
            SliderRange bounds = surveyQuestion.getSliderRange();

            int value;
            
            if (bounds != null) {

                double doubleValue = Double.parseDouble(responseMetadata.getResponses().get(0).getText()) - bounds.getMinValue();

                doubleValue /= (bounds.getMaxValue() - bounds.getMinValue());

                value = (int) (doubleValue * slider.getMax());
                
            } else {
                
                value = (int) Double.parseDouble(responseMetadata.getResponses().get(0).getText());
            }
            
            slider.setValue((double) value);
            
            //add a label showing the total points earned and the total possible points
            double possiblePoints = surveyQuestion.getHighestPossibleScore();
            if(showPoints && possiblePoints > 0){
                //don't bother calculating the possible points if we aren't going to show them                
                //don't show if you can't earn positive points
                
                boolean allowsPartialCredit = surveyQuestion.getAllowsPartialCredit();
                
                // flag used to determine if positive points were erased which then can be used to alter the label shown to the user
                // Decided that removing negative points didn't warrant a change in the label.
                boolean erasedPositivePts = false;
                
                // if partial credit is not allowed and the highest points where not earned, set points to zero
                if(!allowsPartialCredit && value != 0 && value < possiblePoints) {
                    logger.info("Setting earned points to 0 for "+surveyQuestion);
                    erasedPositivePts = value > 0;
                    value = 0;
                }
                
                Alert earnedLabel = new Alert("You earned "+value+" of "+possiblePoints+" possible points "+ (erasedPositivePts ? "(No partial credit)" : "") + ".");
                earnedLabel.getElement().getStyle().setProperty("boxShadow", "2px 2px 7px rgba(0,0,0,0.4)");
                earnedLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);
                earnedLabel.getElement().getStyle().setMarginTop(10, Unit.PX);
                earnedLabel.setType(AlertType.SUCCESS);
                addEarnedPointsArea(earnedLabel);
            }
        }
    }

    @Override
    public AbstractQuestionResponse getAnswer(boolean validate) throws MalformedAnswerException {

        if (timeAnswered != null) {
            
            double value;
            
            SliderRange bounds = getSurveyElement().getSliderRange();
            
            /* Previously, there was logic in place that manually calculated the value returned from the slider 
             * if the range bounds were not set (0 - 100). Because the bounds authored in the survey composer weren't set to 
             * the GWT Slider object created inside this widget class, the formula found inside revision 25784 was 
             * always used, returning a correct value regardless since any value other than the max slider value was just 
             * added to the value field.
             * 
             * Ex: value = (x / max) * (max - min) + min, where x = 300, min = 0, max = 100
             *     value = 3 * 100 + 0 --> 300 = 300
             * 
             * It seemed that the calculated slider value returned the intended number captured from the learner at
             * first, but using a LOGARITHMIC scale required that the slider bounds were explicitly set for those
             * properties to reflect on the TUI. Because of this, with the slider min, max, step size, and scale type fields 
             * now correctly set, the formula worked off bounds different from 0 and 100, resulting in incorrect values being 
             * saved. 
             * 
             * Ex: value = (x / max) * (max - min) + min, where x = 300, min = 200, max = 2000
             *     value = 0.15 * 1800 + 200 --> 470 =/= 300
             * 
             * To fix this, we now use the value returned from the slider since ValueChangeHandlers are in place that already 
             * set the slider value once a new value is selected.
             */
            value = slider.getValue();

            int decimalPlaces = 2;

            if (bounds != null) {

                BigDecimal min = new BigDecimal(bounds.getMinValue());

                if (min.scale() > decimalPlaces) {

                    decimalPlaces = min.scale();
                }

                BigDecimal max = new BigDecimal(bounds.getMaxValue());

                if (max.scale() > decimalPlaces) {

                    decimalPlaces = max.scale();
                }
            }
            
            BigDecimal pointsDecimal = new BigDecimal(value);
            
            pointsDecimal.setScale(decimalPlaces, RoundingMode.DOWN);

            String answerText = Double.toString(value);

            return AbstractQuestionResponse.createResponse(getSurveyElement(), Collections.singletonList(new QuestionResponseElement(answerText, timeAnswered)));

        } else {

            throw new MalformedAnswerException("The question has not been answered", getSurveyElement().getIsRequired());
        }
    }

    @Override
    public void setExternalQuestionResponse(AbstractQuestionResponse questionResponse) {

        //currently not supported
        throw new DetailedException("Unable to apply an external question response to a slider question type.", "This logic has not been implemented yet.", null);
    }
}
