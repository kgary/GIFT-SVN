/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.EnumException;
import mil.arl.gift.common.enums.SurveyElementTypeEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.AbstractQuestionWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.MultipleChoiceWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.SurveyEditMode;
import mil.arl.gift.common.survey.AbstractSurveyElement;
import mil.arl.gift.common.survey.AbstractSurveyQuestion;
import mil.arl.gift.common.survey.FillInTheBlankSurveyQuestion;
import mil.arl.gift.common.survey.MatrixOfChoicesSurveyQuestion;
import mil.arl.gift.common.survey.MultipleChoiceSurveyQuestion;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.RatingScaleSurveyQuestion;
import mil.arl.gift.common.survey.SliderSurveyQuestion;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.AbstractSelectSurveyItemWidget.SurveyItemType;

/**
 * The SurveyWidgetFactor class is responsible for constructing and initializing each type
 * of supported widget for the survey editor panel.
 * 
 * This factory class is not meant to be instantiated and should contain static functions only.
 * 
 * @author nblomberg
 *
 */
public class SurveyWidgetFactory  {

    /** The logger for this class */
    private static Logger logger = Logger.getLogger(SurveyWidgetFactory.class.getName());

    /**
     * Constructor (default)
     */
    private SurveyWidgetFactory() {
    }

	/**
	 * Creates a survey widget based on the item type and mode.
	 * 
	 * @param surveyType - The type of survey item to be created.
	 * @param mode - The mode that the survey item should be initialized with.
	 * @param isScored - If the survey is a scored type. 
	 * @param readOnly - Whether or not the survey item should be read-only.
	 * @param isQuestionBank - Whether or not the survey is a question bank.
	 * @return Widget - The widget that was created (null is returned if the widget cannot be created).
	 */
	static public AbstractQuestionWidget createSurveyWidget(SurveyItemType surveyType, SurveyEditMode mode, boolean isScored, boolean readOnly, boolean isQuestionBank) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("createSurveyWidget(");
            List<Object> params = Arrays.<Object>asList(surveyType, mode, isScored, readOnly);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

	    AbstractQuestionWidget widget = null;
        if (surveyType == SurveyItemType.MULTIPLE_CHOICE) {
            widget = new MultipleChoiceWidget(mode, isScored);
        } else if (surveyType == SurveyItemType.INFORMATIVE_TEXT) {
            widget = new InformativeTextWidget(mode); 
        } else if (surveyType == SurveyItemType.FREE_RESPONSE) {
            widget = new FreeResponseWidget(mode, isScored, isQuestionBank);
        } else if (surveyType == SurveyItemType.MATRIX_OF_CHOICES) {
            widget = new MatrixOfChoicesWidget(mode, isScored);
        } else if (surveyType == SurveyItemType.SLIDER_BAR) {
            widget = new SliderBarWidget(mode, isScored);
        } else if (surveyType == SurveyItemType.ESSAY) {
            widget = new EssayWidget(mode);           
        }  else if (surveyType == SurveyItemType.TRUE_FALSE) {
            widget = new TrueFalseWidget(mode, isScored);
        } else if (surveyType == SurveyItemType.RATING_SCALE) {
            widget = new RatingScaleWidget(mode, isScored);
        }  else if (surveyType == SurveyItemType.COPY_EXISTING_ITEM) {
            
            // TODO - Bring up a dialog to pick from an existing question (set of questions).
        } else if (surveyType == SurveyItemType.CLOSE_ITEM){
            //nothing to do
            
        }  else {
            logger.severe("Widget of type: " + surveyType + " is not implemented. Unable to create the survey item.");
        }
        
        if (widget != null) {
            // Order is to:  
            //  1) Create the widget (constructor above)
            //  2) Initialize the widget as needed.
            //  3) Refresh the widget based on the proper mode.
            widget.initializeWidget();
            widget.refresh();
            widget.setReadOnlyMode(readOnly);
        }
        
        return widget;
	}
    
	/**
	 * Gets the question widget type corresponding to the given question widget
	 * 
	 * @param widget the widget from which to get the type
	 * @return the widget's type
	 */
	static public SurveyItemType getQuestionWidgetType(AbstractQuestionWidget widget){
		
		if(widget instanceof MultipleChoiceWidget){
			return SurveyItemType.MULTIPLE_CHOICE;
		
		} else if(widget instanceof InformativeTextWidget){
			return SurveyItemType.INFORMATIVE_TEXT;
			
		} else if(widget instanceof FreeResponseWidget){
			return SurveyItemType.FREE_RESPONSE;
			
		} else if(widget instanceof MatrixOfChoicesWidget){
			return SurveyItemType.MATRIX_OF_CHOICES;
			
		} else if(widget instanceof SliderBarWidget){
			return SurveyItemType.SLIDER_BAR;
			
		} else if(widget instanceof EssayWidget){
			return SurveyItemType.ESSAY;
			
		} else if(widget instanceof TrueFalseWidget){
			return SurveyItemType.TRUE_FALSE;
			
		} else if(widget instanceof RatingScaleWidget){
			return SurveyItemType.RATING_SCALE;
			
		}
		
		return null;
	}

	/**
	 * Gets the question widget type corresponding to the given survey element
	 * 
	 * @param element the survey element from which to get the type
	 * @return the element's type
	 */
	public static SurveyItemType getQuestionWidgetType(AbstractSurveyElement element) {
		
		if(element != null){
			
			SurveyElementTypeEnum type = element.getSurveyElementType();
		
			if (type == SurveyElementTypeEnum.QUESTION_ELEMENT) {

				// Dig deeper in terms of the type of survey.
				AbstractSurveyQuestion<?> surveyQuestion = (AbstractSurveyQuestion<?>) element;

				logger.info("surveyQuestion = " + surveyQuestion.getClass().getName());
				if (surveyQuestion instanceof FillInTheBlankSurveyQuestion) {

					if (surveyQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY) != null
							&& !surveyQuestion.getQuestion().getProperties().getBooleanPropertyValue(SurveyPropertyKeyEnum.IS_ANSWER_FIELD_TEXT_BOX_KEY)) {
						
						return SurveyItemType.ESSAY;
						
					} else {
						return SurveyItemType.FREE_RESPONSE;
					}

				} else if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {

					// MultipleChoiceSurveyQuestion could be a TRUE/FALSE or
					// normal Multiple Choice widget.
					OptionList optionList = null;
					if (surveyQuestion.getQuestion().getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY) instanceof OptionList) {
						
						optionList = (OptionList) surveyQuestion.getQuestion().getProperties()
								.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
					}
					
					if (optionList != null && optionList.getName().equals("True/False")) {
						return SurveyItemType.TRUE_FALSE;
						
					} else {
						return SurveyItemType.MULTIPLE_CHOICE;
					}

				} else if (surveyQuestion instanceof RatingScaleSurveyQuestion) {
					return SurveyItemType.RATING_SCALE;

				} else if (surveyQuestion instanceof MatrixOfChoicesSurveyQuestion) {
					return SurveyItemType.MATRIX_OF_CHOICES;

				} else if (surveyQuestion instanceof SliderSurveyQuestion) {
					return SurveyItemType.SLIDER_BAR;

				}


			} else if (type == SurveyElementTypeEnum.TEXT_ELEMENT) {
				return SurveyItemType.INFORMATIVE_TEXT;
			}
		}
		
		return null;
	}
	
    /**
     * Checks if the survey type is supported by the question bank.
     * 
     * @param surveyType the survey type to check.
     * @return true if the survey type is supported by the question bank; false otherwise.
     */
    public static boolean isSupportedByQuestionBank(SurveyItemType surveyType) {

        // If survey type is null, it isn't supported.
        if (surveyType == null) {
            return false;
        }

        switch (surveyType) {

        // return false block first (intentional fall through)
        case CLOSE_ITEM:
        case COPY_EXISTING_ITEM:
        case ESSAY:
        case INFORMATIVE_TEXT:
            return false;

        // return true block second (intentional fall through)
        case FREE_RESPONSE:
        case MATRIX_OF_CHOICES:
        case MULTIPLE_CHOICE:
        case RATING_SCALE:
        case SLIDER_BAR:
        case TRUE_FALSE:
            return true;

        default:
            throw new EnumException("The survey type [" + surveyType + "] is missing from isSupportedByQuestionBank.", null);
        }
    }
}
