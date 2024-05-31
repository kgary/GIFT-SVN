/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.Label;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.QuestionDifficultyEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.ResponsesChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.events.SurveyScoreValueChangedEvent;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AbstractPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AnswerSetPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.CommonPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.DifficultyAndConceptsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.HiddenAnswerWeightsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.MultiSelectPropertySet;
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
import mil.arl.gift.common.util.StringUtils;

/**
 * The MultipleChoiceWidget allows the survey author to create a multiple choice
 * question with a set of responses.
 * 
 * @author nblomberg
 *
 */
public class MultipleChoiceWidget extends AbstractQuestionWidget {

	private static Logger logger = Logger.getLogger(MultipleChoiceWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
	
	/** The ID of the last group of radio buttons added by an instance of this widget*/
    private static int lastRadioGroupId = 0;

	interface WidgetUiBinder extends
			UiBinder<Widget, MultipleChoiceWidget> {
	}
	

	@UiField
	Label pointHeader;
	
	@UiField
	protected Container responseContainer;
	
	private boolean multiselectEnabled = false;
	
	/** The ID for this widget's radio button group */
	private int radioGroupId = generateRadioGroupId(); 

	/**
	 * An optional list of predefined responses to provide as choices with every
	 * response
	 */
	private List<PickableObject> pickableResponses = new ArrayList<PickableObject>();
	
	/** A tooltip for the button used to pick from predefined responses */
	private String pickableResponsesTooltip = null;

	/** The option list of the multiple choice question corresponding to this widget */
	private OptionList optionList;
	
	/** List of weights for all the answers */
	private List<Double> answerWeights;
	
	/**
	 * attributes for this question to be scored on
	 */
	private Set<AttributeScorerProperties> scoringAttributes;
	
	/**
	 * Create widget
	 * @param mode whether the widget is being displayed in writing mode or scoring mode.  Can't be null.  Used
     * to alter the widget components.
     * @param isScored whether the survey question associated with these common survey question properties
     * can be scored because the survey is a scored survey type and the question supports scoring answers.
	 */
	public MultipleChoiceWidget(SurveyEditMode mode, boolean isScored) {
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
	 * Adds a new response widget where the user can enter text to create a new
	 * response
	 */
	private MultipleChoiceResponseWidget addResponse(ListOption option, boolean isShared){
		return addResponse(option, "", isShared);
	}
	
	/**
	 * Adds a new response widget where the user can enter text to create a new
	 * response
	 * 
	 * @param option contains properties of the question choice (e.g. text).  If null this response object is
	 * consider a placeholder for the next choice to author.
	 * @param objectId an optional value that associates this question choice with an object in a training application (e.g. ARES).
	 * Can be null or empty string if this choice is NOT associated with an object outside of the survey.
	 * @param isShared whether the choice (list option) is a shared one, meaning the database entry can be 
	 * used in other surveys.  If this is true than the text can't be edited. 
	 */
	private MultipleChoiceResponseWidget addResponse(ListOption option, String objectId, boolean isShared){
		final MultipleChoiceResponseWidget response = new MultipleChoiceResponseWidget(
				"MultipleChoiceQuestionGroup - " + radioGroupId);
		
		if (option != null) {
			response.getLabel().setValue(option.getText(), new PickableObject(option.getText(), objectId));
			
			if(optionList.getListOptions().size() > answerWeights.size()) {
				/* The number of list options should match the number of answer weights, otherwise a scoring 
				 * error will occur when the user runs the course. */
				answerWeights.add(0.0);
			}
		}
		
        boolean isScoringMode = getEditMode().equals(SurveyEditMode.ScoringMode);
        if (StringUtils.isNotBlank(objectId)) {
            response.setStaticLabel(true);
            response.getLabel().setTooltipText("The choice text cannot be edited because it was selected from the list of scenario objecs.");
        } else {
            // set if the label is static or not
            response.setStaticLabel(isShared);
            
            // set if the label is editable or not
            response.setLabelEditable(!(isShared || isReadOnly || isScoringMode));
        }
		
		response.getLabel().addValueChangeHandler(new ValueChangeHandler<String>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				int responseIndex = responseContainer.getWidgetIndex(response);
				
				if (!event.getValue().isEmpty()) {
					response.getPointBox().setEnabled(true);
					
					// add the option to reply set, or update if it already exists
					if (optionList.getListOptions().size() <= responseIndex) {
						optionList.getListOptions().add(null);
					}
					if (optionList.getListOptions().get(responseIndex) == null) {
						logger.info("Adding " + event.getValue() + " to optionList at index " + responseIndex);
						optionList.getListOptions().set(responseIndex, new ListOption(0, event.getValue(), optionList.getId()));
						answerWeights.add(0.0);
						getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(
								SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
						
						updateMultiSelectWidget();
						response.hideWarningIcon();
						
					} else {
						logger.info("Updating index " + responseIndex + " to value = " + event.getValue());
						optionList.getListOptions().get(responseIndex).setText(event.getValue());
					}
					
					// update sort key based on new ordering of choices
					for(int index = 0; index < optionList.getListOptions().size(); index++){
					    optionList.getListOptions().get(index).setSortKey(index);
					}
					
					updateTotalQuestionFlag();
				} 

				if (responseIndex == responseContainer.getWidgetCount() - 1) {

					if (!event.getValue().isEmpty()) {
						
    						// if the user enters text into the last response, add
    						// another response area after it
    						addResponse(null, false);
						
						response.setTemporary(false);
					}

				} else {

					// if the user removes all the text in any other response,
					// remove
					if (event.getValue().isEmpty()) {
						if (optionList.getListOptions().size() > responseIndex) {
							optionList.getListOptions().remove(responseIndex);
							answerWeights.remove(responseIndex);
							responseContainer.remove(response);
							getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
							updateTotalQuestionFlag();
							updateMultiSelectWidget();
						}
					}
				}
			}
		});
		
		response.getLabel().setEnterKeyListener(new Command() {
			
			@Override
			public void execute() {
				
				int responseIndex = responseContainer.getWidgetIndex(response);
				
				if (responseIndex == responseContainer.getWidgetCount() - 1) {	
                	
                	//need to wait until after value change event is processed, so defer remaining logic
                	Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						
						@Override
						public void execute() {
							
							if (!response.getLabel().getValue().isEmpty()
									&& responseContainer.getWidgetIndex(response) + 1 < responseContainer.getWidgetCount()) {

								//a new response was created by hitting the Enter key on the last response, so start editing
								//the new response
								int nextIndex = responseContainer.getWidgetIndex(response) + 1;
								
								Widget nextWidget = responseContainer.getWidget(nextIndex);
								
								if(nextWidget instanceof MultipleChoiceResponseWidget){
									((MultipleChoiceResponseWidget) nextWidget).getLabel().startEditing();
								}
							}
						}
					});
				}
			}
		});

		response.getMoveUpItem().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				int destIndex = responseContainer.getWidgetIndex(response) - 1;
				List<String> newFeedbacks = getRawFeedbackStrings();

				if (destIndex >= 0) {
					Collections.swap(optionList.getListOptions(), destIndex + 1, destIndex);
					Collections.swap(answerWeights, destIndex + 1, destIndex);
					Collections.swap(newFeedbacks, destIndex + 1, destIndex);
					responseContainer.insert(response, destIndex);
					
					// update sort key based on new ordering of choices
                    for(int index = 0; index < optionList.getListOptions().size(); index++){
                        optionList.getListOptions().get(index).setSortKey(index);
                    }
					
					String answerWeightProps = SurveyItemProperties.encodeDoubleListString(answerWeights);
					getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					getPropertySetByType(AnswerSetPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					((AnswerSetPropertySet) getPropertySetByType(AnswerSetPropertySet.class.getName())).setReplyFeedbacks(newFeedbacks);
				}
			}
		});

		response.getMoveDownItem().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {

				int destIndex = responseContainer.getWidgetIndex(response) + 2;
				List<String> newFeedbacks = getRawFeedbackStrings();

				if (destIndex < responseContainer.getWidgetCount()) {
					Collections.swap(optionList.getListOptions(), destIndex - 2, destIndex - 1);
					Collections.swap(answerWeights, destIndex - 2, destIndex - 1);
					Collections.swap(newFeedbacks, destIndex - 2, destIndex - 1);
					responseContainer.insert(response, destIndex);
					
					// update sort key based on new ordering of choices
                    for(int index = 0; index < optionList.getListOptions().size(); index++){
                        optionList.getListOptions().get(index).setSortKey(index);
                    }
					
					String answerWeightProps = SurveyItemProperties.encodeDoubleListString(answerWeights);
					getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					getPropertySetByType(AnswerSetPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, answerWeightProps);
					((AnswerSetPropertySet) getPropertySetByType(AnswerSetPropertySet.class.getName())).setReplyFeedbacks(newFeedbacks);
				}
			}
		});

		response.getRemoveChoiceItem().addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				logger.info("Removing option at index " + responseContainer.getWidgetIndex(response) + " with value = "
						+ optionList.getListOptions().get(responseContainer.getWidgetIndex(response)).getText());
				optionList.getListOptions().remove(responseContainer.getWidgetIndex(response));
				answerWeights.remove(responseContainer.getWidgetIndex(response));
				responseContainer.remove(response);
				
				// update sort key based on new ordering of choices
                for(int index = 0; index < optionList.getListOptions().size(); index++){
                    optionList.getListOptions().get(index).setSortKey(index);
                }
                
				getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));
				updateTotalQuestionFlag();
				updateMultiSelectWidget();
			}
		});
		
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
				SurveyEditorResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
			}

		});
		
		response.getFeedbackBox().addChangeHandler(new ChangeHandler(){

			@Override
			public void onChange(ChangeEvent event) {
				((AnswerSetPropertySet) getPropertySetByType(AnswerSetPropertySet.class.getName())).setReplyFeedbacks(getRawFeedbackStrings());
			}
			
		});
		
		response.getAddFeedbackButton().addClickHandler(new ClickHandler(){

			@Override
			public void onClick(ClickEvent event) {
				((AnswerSetPropertySet) getPropertySetByType(AnswerSetPropertySet.class.getName())).setReplyFeedbacks(getRawFeedbackStrings());
			}
			
		});
		
		response.setAllowMultiselect(multiselectEnabled);
		response.setTemporary(option == null || isShared);

		response.setMode(getEditMode());
		response.refresh();
		
		response.setPickableResponses(pickableResponsesTooltip, pickableResponses);
		
		response.setVisible(!(response.getLabel().getValue().isEmpty() && isScoringMode));
		
		response.setReadOnlyMode(isReadOnly);

		response.getPointBox().setValue(response.getPointValue() != null ? response.getPointValue() : 0.0);
		
		responseContainer.add(response);

		return response;
	}
	
	/**
	 * Sets the visibility of the placeholder question (last question in responseContainer)
	 * 
	 * @param visible
	 *             Whether or not the placeholder question should be visible
	 */
	@Override
    public void setPlaceholderResponseVisible(boolean visible) {
	    Widget placeholder = null;
	    
        if (responseContainer.getWidgetCount() != 0) {
            placeholder = responseContainer.getWidget(responseContainer.getWidgetCount() - 1);
        }

        if (placeholder != null && isWidgetPlaceholder(placeholder)) {
            if (responseContainer.getWidgetCount() > 1 && isReadOnly) {
                placeholder.setVisible(false);
	        }else if(responseContainer.getWidgetCount() == 1 && !visible) {
	            placeholder.setVisible(true);
	        } else {
	            if(!visible){
	                //hiding placeholder happens no matter what
	                placeholder.setVisible(visible);
	            }else if(visible && getEditMode() == SurveyEditMode.WritingMode){
	                //showing placeholder while in writing mode, nothing else to consider, just do it
                    placeholder.setVisible(visible);
                }else{
                   //this means in scoring mode, trying to show the placeholder... don't show placeholder
                   //if there are already authored choices
                   if(optionList == null || optionList.getListOptions().isEmpty()){
                       //no authored choices so show placeholder
                       placeholder.setVisible(visible);
                   }else{
                       placeholder.setVisible(false);
                   }
                }
	        }
	    }
	}
	
	/**
	 * Checks if the given widget is a placeholder.
	 * @param widget the widget to check. Will return false if null.
	 * @return true if the widget is a MultipleChoiceResponseWidget that contains no label value; false otherwise.
	 */
	private boolean isWidgetPlaceholder(Widget widget) {
        if (widget == null || !(widget instanceof MultipleChoiceResponseWidget)) {
            return false;
        }

        MultipleChoiceResponseWidget respWidget = ((MultipleChoiceResponseWidget) widget);
        if (respWidget.getLabel() == null || StringUtils.isBlank(respWidget.getLabel().getValue())) {
            return true;
        }

        return false;
	}
	
	private void populateAnswerWeights(){
		logger.info("populating answer weights, weights = " + answerWeights);
		for(Widget widget : responseContainer){
			if(widget instanceof MultipleChoiceResponseWidget){
				MultipleChoiceResponseWidget mcWidget = (MultipleChoiceResponseWidget) widget;
				
				if(answerWeights.size() > responseContainer.getWidgetIndex(mcWidget)){
					mcWidget.setPointValue(answerWeights.get(responseContainer.getWidgetIndex(mcWidget)));
					mcWidget.getPointBox().setValue(answerWeights.get(responseContainer.getWidgetIndex(mcWidget)), false);
					mcWidget.refresh();
				}
			}
		}
	}

	/**
	 * Sets whether or not multi-select should be enabled
	 * 
	 * @param enabled
	 *            whether or not multi-select should be enabled
	 */
	public void setMultiselectEnabled(boolean enabled) {

		for (int i = 0; i < responseContainer.getWidgetCount(); i++) {

			Widget widget = responseContainer.getWidget(i);

			if (widget instanceof MultipleChoiceResponseWidget) {

				((MultipleChoiceResponseWidget) widget).setAllowMultiselect(enabled);
			}
		}

		multiselectEnabled = enabled;
	}

	@Override
	protected void addCustomPropertySets() {
		MultiSelectPropertySet propSet = new MultiSelectPropertySet();
		addPropertySet(propSet);

		AnswerSetPropertySet ansSet = new AnswerSetPropertySet();
		addPropertySet(ansSet);
		
		HiddenAnswerWeightsPropertySet weightSet = new HiddenAnswerWeightsPropertySet();
		addPropertySet(weightSet);
		
		DifficultyAndConceptsPropertySet diffAndConceptSet = new DifficultyAndConceptsPropertySet();
        addPropertySet(diffAndConceptSet);

		optionList = (OptionList) ansSet.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
		answerWeights = SurveyItemProperties.decodeDoubleListString((String) weightSet.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
		scoringAttributes = ((QuestionScorer) weightSet.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
		
		propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, optionList);
	}

	@Override
	public void onPropertySetChange(AbstractPropertySet propSet) {

		super.onPropertySetChange(propSet);

		logger.info("onPropertySetChange called: " + propSet.getClass().getName());
		if (propSet instanceof MultiSelectPropertySet) {
			MultiSelectPropertySet msPropSet = (MultiSelectPropertySet) propSet;

			logger.info("multiSelectEnabled = " + msPropSet.getMultiSelectEnabled());
			setMultiselectEnabled(msPropSet.getMultiSelectEnabled());

			updateTotalQuestionFlag();
			// Properties changed can impact the scoring values so signal that a
			// score value was changed.
			SurveyEditorResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
		} else if (propSet instanceof AnswerSetPropertySet) {
			
			AnswerSetPropertySet answerPropSet = (AnswerSetPropertySet) propSet;
			OptionList newOptionList = (OptionList) answerPropSet.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
			List<Double> newAnswerWeights = null;
    		if(answerPropSet.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null) {
    			newAnswerWeights = SurveyItemProperties.decodeDoubleListString((String) answerPropSet.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
    		}
			
    		if(newOptionList != null && optionList != null) { 
    			if(newOptionList.getName() != null && !newOptionList.getName().equals(optionList.getName())) {
    				// set the new option list so that the answer weights can be added correctly
    				optionList = newOptionList;
    				populateAnswers(newOptionList);
    				if(newAnswerWeights != null && !newAnswerWeights.isEmpty()) {
    					answerWeights = newAnswerWeights;
    				}
    			} 
    		}

            /* We have the new answer weights now. Push to properties */
            getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getProperties().setPropertyValue(
                    SurveyPropertyKeyEnum.ANSWER_WEIGHTS, SurveyItemProperties.encodeDoubleListString(answerWeights));

        	optionList = newOptionList; 
			populateAnswerWeights();
			setRawFeedbackStrings(answerPropSet.getReplyFeedbacks());

			updateTotalQuestionFlag();
			SurveyEditorResources.getInstance().getEventBus().fireEvent(new SurveyScoreValueChangedEvent());
		}

	}

	@Override
	public void refresh() {

		logger.info("refresh() called: " + getEditMode());
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
	 * Refreshes the response widgets based on the current editor mode (writing
	 * or scoring mode).
	 */
	private void refreshResponseWidgets() {
		for (int x = 0; x < responseContainer.getWidgetCount(); x++) {
			Widget widget = responseContainer.getWidget(x);

			if (widget instanceof MultipleChoiceResponseWidget) {
				MultipleChoiceResponseWidget mcrWidget = (MultipleChoiceResponseWidget) widget;

				mcrWidget.refresh();
			}

		}
	}

	@Override
	public void initializeWidget() {
		addResponse(null, false);

	}

	@Override
	public void onEditorModeChanged(SurveyEditMode mode) {
	    super.onEditorModeChanged(mode);
        setEditMode(mode);

		for (int x = 0; x < responseContainer.getWidgetCount(); x++) {
			Widget widget = responseContainer.getWidget(x);

			if (widget instanceof MultipleChoiceResponseWidget) {
				MultipleChoiceResponseWidget mcrWidget = (MultipleChoiceResponseWidget) widget;

				mcrWidget.onEditorModeChanged(mode);
				if(mcrWidget.getLabel().getValue().isEmpty() && mode.equals(SurveyEditMode.ScoringMode)){
					mcrWidget.setVisible(false);
				} else if (mode.equals(SurveyEditMode.WritingMode) && !mcrWidget.isVisible()){
					mcrWidget.setVisible(true);
				}
			}

		}
	}

	@Override
	public Double getPossibleTotalPoints() {

		Integer maxResponses = responseContainer.getWidgetCount();

		// Determine how many responses can be selected.
		Integer maxSelections = 1, minSelections = 0;
		if (multiselectEnabled) {
			
			maxSelections = getIntegerValueFromPropertySet(MultiSelectPropertySet.class.getName(),
					SurveyPropertyKeyEnum.MAXIMUM_SELECTIONS_ALLOWED_KEY);
			// Cap the max selections that can be chosen based on the number of
			// actual responses.
			
			if(maxSelections == null){
				
				logger.info("No maximum number of selections allowed was found while calculating the "
						+ "possible total number of points. Assuming the total number of selections.");
				maxSelections = maxResponses;
				
			} else {
				maxSelections = Math.min(maxResponses, maxSelections);
			}
			
	        minSelections = getIntegerValueFromPropertySet(MultiSelectPropertySet.class.getName(),
	                    SurveyPropertyKeyEnum.MINIMUM_SELECTIONS_REQUIRED_KEY);
	        
	        if(minSelections == null){
	        	logger.info("No minimum number of selections allowed was found while calculating the "
						+ "possible total number of points. Assuming the default of 0 selections.");
	        	minSelections = 0;
	        }
	        
	        //make sure the minimum number of selections is less than or equal to the maximum number
	        minSelections = Math.min(maxSelections, minSelections);
		}
		
		Double totalPoints = 0.0;
		
		try {
			totalPoints = SurveyScorerUtil.getHighestScoreMultipleChoice(answerWeights, minSelections, maxSelections);
			logger.info("The total points = "+totalPoints+" - answerWeights = "+answerWeights+", minSelections = "+minSelections+", maxSelections = "+maxSelections);
		} catch (@SuppressWarnings("unused") IllegalArgumentException e) {
			totalPoints = SurveyScorerUtil.getHighestScoreMultipleChoice(answerWeights, 0, 1);
		}

		return totalPoints;
	}

	/**
	 * Sets an optional list of choices that this widget will allow authors to
	 * optionally pick from. This can be helpful if you want to give question
	 * authors the option of either entering text manually or selecting from a
	 * predefined list. <br/>
	 * <br/>
	 * If no choices are defined, then this widget won't provide any options to
	 * pick from, forcing them to type them in manually
	 * 
	 * @param responses
	 */
	public void setPickableChoices(String tooltip, List<PickableObject> choices) {

		pickableResponsesTooltip = tooltip;

		pickableResponses.clear();

		if (choices != null) {

			for (PickableObject response : choices) {
				pickableResponses.add(response);
			}
		}

		for (int i = 0; i < responseContainer.getWidgetCount(); i++) {

			Widget widget = responseContainer.getWidget(i);

			if (widget instanceof MultipleChoiceResponseWidget) {

				((MultipleChoiceResponseWidget) widget).setPickableResponses(pickableResponsesTooltip,
						pickableResponses);
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
	        if (surveyQuestion instanceof MultipleChoiceSurveyQuestion) {

	            MultipleChoiceSurveyQuestion question = (MultipleChoiceSurveyQuestion) surveyQuestion;
	            
	            // TODO - Check for properties at the AbstractQuestion level
	            // then fall back to the AbstractSurveyQuestion level.
	            SurveyItemProperties properties = question.getProperties();

	            MultipleChoiceQuestion multipleChoiceQuestion = question.getQuestion();

	            logger.info("question text: " + multipleChoiceQuestion.getText());
	            questionHtml.setValue(multipleChoiceQuestion.getText());

	            // Load the properties (if any)
	            AbstractPropertySet propSet = getPropertySetByType(MultiSelectPropertySet.class.getName());
	            MultiSelectPropertySet multiSelectProps = (MultiSelectPropertySet) propSet;
	            multiSelectProps.load(properties);
	            multiSelectProps.load(multipleChoiceQuestion.getProperties());
	           
	            // Load the Common Properties
	            propSet = getPropertySetByType(CommonPropertySet.class.getName());
	            CommonPropertySet commonProps = (CommonPropertySet) propSet;
	            commonProps.load(properties);
	            commonProps.load(multipleChoiceQuestion.getProperties());
	            commonProps.setSurveyQuestion(surveyQuestion);

	            // Load the Image Display Properties
	            propSet = getPropertySetByType(QuestionImagePropertySet.class.getName());
	            QuestionImagePropertySet imageProps = (QuestionImagePropertySet) propSet;
	            imageProps.load(properties);
	            imageProps.load(multipleChoiceQuestion.getProperties());

	            // Load the Answer Set Properties
	            propSet = getPropertySetByType(AnswerSetPropertySet.class.getName());
	            AnswerSetPropertySet answerSetProps = (AnswerSetPropertySet) propSet;
	            answerSetProps.load(properties);
	            answerSetProps.load(multipleChoiceQuestion.getProperties());
	            
	            // Load the Answer Weight Properties
	            propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
	            HiddenAnswerWeightsPropertySet answerWeightProps = (HiddenAnswerWeightsPropertySet) propSet;
	            answerWeightProps.load(multipleChoiceQuestion.getProperties());
	            answerWeightProps.load(properties);
	            
	            // Load the difficulty and concepts Properties
                propSet = getPropertySetByType(DifficultyAndConceptsPropertySet.class.getName());
                DifficultyAndConceptsPropertySet diffAndConceptsSet = (DifficultyAndConceptsPropertySet) propSet;
                diffAndConceptsSet.load(properties);
                diffAndConceptsSet.load(multipleChoiceQuestion.getProperties());
	            
	            answerWeights = SurveyItemProperties.decodeDoubleListString((String) answerWeightProps.getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
	            scoringAttributes = ((QuestionScorer) answerWeightProps.getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).getAttributeScorers();
	            optionList = (OptionList) answerSetProps.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
	            populateAnswers(optionList);
	            populateAnswerWeights();
	            setRawFeedbackStrings(answerSetProps.getReplyFeedbacks());

	            updateTotalQuestionFlag();
	            // print the question properties
	            // debugPrintQuestionProperties(multipleChoiceQuestion.getProperties());
	            
	            // This should be called after all property sets have been loaded for the abstractsurveyelement.
	            addUnsupportedProperties(multipleChoiceQuestion.getProperties(), properties);
	            onLoadNotifyPropertySetChanges();
	            
	            refresh();
	        } else {
	            throw new LoadSurveyException(
	                    "Trying to load a MultipleChoice widget, but encountered non multiple choice data from the database.", null);
	        }
	    } else {
            throw new LoadSurveyException("Expected AbstractSurveyElement of type AbstractSurveyQuestion, but found: " + element.getClass().getName(), null);
        }
        

	}

	/**
	 * Will populate the widget with all the existing options and then put a new
	 * option at the end populated with only a new option if optionList is null
	 * 
	 * @param optionList the list of options to put into the question, can be null
	 * 
	 */
	private void populateAnswers(OptionList optionList) {
		responseContainer.clear();
		boolean isShared = optionList.getIsShared();
		if (optionList != null) {
			logger.info("populating answers for multiple choice question with " + optionList.getListOptions().size() + " reply options");
			for (ListOption option : optionList.getListOptions()) {
				addResponse(option, isShared);
			}
		}
		if(!isShared){
			addResponse(null, isShared);
		}

	}

	/**
	 * Debug print function used to print the properties for the question.
	 * 
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

	/**
	 * Shows or hides the warning icon.
	 * 
	 * @param tooltip The tooltip to show for the warning icon.
	 * @param objectId The reply object id, used to retrieve the response.
	 * @param show True to show the warning icon, false to hide it.
	 */
	public void showOrHideWarningIcon(String tooltip, String objectId, boolean show) {

		if (responseContainer.getWidgetCount() > 0 && objectId != null && !objectId.isEmpty()) {
			
			for (int i = 0; i < responseContainer.getWidgetCount() - 1; i++) {

				Widget w = responseContainer.getWidget(i);
				
				if (w instanceof MultipleChoiceResponseWidget) {
					
					PickableObject choice = ((MultipleChoiceResponseWidget) w).getLabel().getValueObject();
					
					if(choice != null && choice.getObjectId() != null) {
						
						if(choice.getObjectId().equals(objectId)) {
							if(show) {
								((MultipleChoiceResponseWidget) w).showWarningIcon(tooltip);
							} else {
								((MultipleChoiceResponseWidget) w).hideWarningIcon();
							}
						}
					}
				} 
			}
		}		
	}
		
	/**
	 * Gets the raw strings used to define the reply options
	 * 
	 * @return the reply strings
	 */
	public List<String> getRawReplyStrings() {

		List<String> replies = new ArrayList<String>();

		if (responseContainer.getWidgetCount() > 0) {

			for (int i = 0; i < responseContainer.getWidgetCount() - 1; i++) {

				Widget w = responseContainer.getWidget(i);

				if (w instanceof MultipleChoiceResponseWidget) {
					replies.add(((MultipleChoiceResponseWidget) w).getLabel().getValue());
				}
			}
		}

		return replies;
	}
	
	/**
	 * Gets the scenario object ids associated with the replies
	 * 
	 * @return the scenario object ids associated with the replies
	 */
	public List<String> getRawReplyIds() {
		
		List<String> replyObjectIds = new ArrayList<String>();
		
		if (responseContainer.getWidgetCount() > 0) {

			for (int i = 0; i < responseContainer.getWidgetCount() - 1; i++) {

				Widget w = responseContainer.getWidget(i);

				if (w instanceof MultipleChoiceResponseWidget) {
					PickableObject choice = ((MultipleChoiceResponseWidget) w).getLabel().getValueObject();
					if(choice != null && choice.getObjectId() != null) {
						replyObjectIds.add(choice.getObjectId());
					} else {
						replyObjectIds.add("");
					}
				}
			}
		}
		
		return replyObjectIds;
	}
	
	/**
	 * Gets the scenario object ids associated with the replies
	 * 
	 * @return the scenario object ids associated with the replies
	 */
	public void updateSelectedReplies(List<PickableObject> choices) {
		
		if (responseContainer.getWidgetCount() > 0) {
			
			List<String> choiceIds = new ArrayList<String>();
			
			for(PickableObject choice : choices) {
				choiceIds.add(choice.getObjectId());		
			}
			
			for (int i = 0; i < responseContainer.getWidgetCount() - 1; i++) {

				Widget w = responseContainer.getWidget(i);
				
				if (w instanceof MultipleChoiceResponseWidget) {
					
					PickableObject choice = ((MultipleChoiceResponseWidget) w).getLabel().getValueObject();
					if(choice != null && choice.getObjectId() != null && !choice.getObjectId().isEmpty()) {
						int index = choiceIds.indexOf(choice.getObjectId());
						if(index != -1) {
							PickableObject object = choices.get(index);
							if(!choice.getResponse().equals(object.getResponse())) {
								((MultipleChoiceResponseWidget) w).getLabel().setValue(object.getResponse(), object);
							} else {
								((MultipleChoiceResponseWidget) w).hideWarningIcon();
							}
						} else {
							((MultipleChoiceResponseWidget) w).showWarningIcon(
									"The scenario object for this choice was not found in the current scenario. Please change this choice.");
						}
					}
				}
			}
		}
	}
		
	/**
	 * Sets the strings used to define the reply options and associates the object ids
	 * with the replies.
	 * 
	 * @param replies The reply strings
	 * @param objectIds The scenario object ids associated with the replies
	 */
	public void setQuestionRepliesAndObjectIds(List<String> replies, List<String> objectIds) {
		
		responseContainer.clear();

		if (replies != null && objectIds != null && !objectIds.isEmpty()) {

			for (int index = 0; index < replies.size(); index++) {
				ListOption option = new ListOption(0, replies.get(index));
				optionList.getListOptions().add(option);
				answerWeights.add(0.0);
				
				if(index < objectIds.size()) {					
					addResponse(option, objectIds.get(index), false);
				} else {
					addResponse(option, false);
				}
			}
			
		} else if (replies != null){
			
			for(String reply : replies) {
				ListOption option = new ListOption(0, reply);
				optionList.getListOptions().add(option);
				answerWeights.add(0.0);				
				addResponse(option, false);
			}
		}
		
		updateTotalQuestionFlag();

            addResponse(null, false);
		
	}
	
	/**
	 * Updates the QuestionScorer totalQuestion flag based on the current {@link #answerWeights} for the question.
	 * This method should be called anytime a score event is fired and or anytime an event occurs that could change
	 * the weights for the question (such as during the load process).
	 */
	private void updateTotalQuestionFlag() {

	    AbstractPropertySet propSet = getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName());
        HiddenAnswerWeightsPropertySet answerWeightProps = (HiddenAnswerWeightsPropertySet) propSet;
        updateTotalQuestionFlag(answerWeightProps.getProperties());
                
		propSet = getPropertySetByType(MultiSelectPropertySet.class.getName());
		propSet.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, optionList);
        
        try {
	        propSet = getPropertySetByType(AnswerSetPropertySet.class.getName());
	        AnswerSetPropertySet answerSetProps = (AnswerSetPropertySet) propSet;
	        answerSetProps.load(answerWeightProps.getProperties());
        } catch (Exception e) {
        	logger.warning("Caught exception while updating answer weight properties: " + e);
        }
	}
	
	/**
	 * Updates the minimum and maximum selections allowed.
	 */
	private void updateMultiSelectWidget() {
		if(multiselectEnabled) {
 			SurveyEditorResources.getInstance().getEventBus().fireEvent(
					new ResponsesChangedEvent(optionList.getListOptions().size()));
		}
	}
		
	/**
	 * Gets the raw strings for each reply option's feedback
	 * 
	 * @return the feedback
	 */
	public List<String> getRawFeedbackStrings() {

		List<String> feedbacks = new ArrayList<String>();

		if (responseContainer.getWidgetCount() > 0) {

			for (int i = 0; i < responseContainer.getWidgetCount() - 1; i++) {

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

	/**
	 * Sets the raw strings for each reply option's feedback
	 * 
	 * @return the feedback
	 */
	public void setRawFeedbackStrings(List<String> feedbacks) {

		if (feedbacks != null && responseContainer.getWidgetCount() > 0) {

			for (int i = 0; i < responseContainer.getWidgetCount() - 1; i++) {
				
				if(i < feedbacks.size()){
					
					String feedback = feedbacks.get(i);
	
					Widget w = responseContainer.getWidget(i);
	
					if (w instanceof MultipleChoiceResponseWidget) {
						
						if(feedback!= null && !feedback.isEmpty()){
							((MultipleChoiceResponseWidget) w).setFeedback(feedback);
						
						} else {
							((MultipleChoiceResponseWidget) w).setFeedback(null);
						}
					}
				}
			}
		}
	}
		
	/**
     * Sets the attributes for this question to be scored on. This comes from
     * the question container widget and is set onValueChange
     * 
     * @param attributes the collection of Attributes
     */
	@Override
    public void setScorerProperty(Set<AttributeScorerProperties> attributes){
    	scoringAttributes = attributes;
    	((QuestionScorer) getPropertySetByType(HiddenAnswerWeightsPropertySet.class.getName()).getPropertyValue(SurveyPropertyKeyEnum.SCORERS)).setAttributeScorers(scoringAttributes);
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
	    
		for(int i = 0; i < responseContainer.getWidgetCount(); i++){
			
			Widget widget = responseContainer.getWidget(i);
			
			if(widget instanceof MultipleChoiceResponseWidget){
				
				MultipleChoiceResponseWidget response = (MultipleChoiceResponseWidget) widget;
				
				response.setReadOnlyMode(readOnly);
			}
		}
		
	}
}
