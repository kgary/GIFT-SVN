/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor.props;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.ValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.MultipleChoiceWidget;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.AnswerSetPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.common.survey.SurveyItemProperties;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

/**
 * The AnswerSetPropertySetWidget is responsible for displaying the controls that allow
 * the author to use an existing answer set rather than use custom answer sets.
 * 
 * @author nblomberg
 *
 */
public class AnswerSetPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(AnswerSetPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, AnswerSetPropertySetWidget> {
	}
	
	@UiField
	protected CheckBox existingAnswerSetCheckBox;
	
	@UiField
	protected CheckBox randomizeAnswersCheckBox;
	
	@UiField
	protected Collapse answerSetCollapse;
	
	/**
	 * Boolean to decide whether or not to show the randomize checkbox
	 */
	private boolean isMultipleChoice;
	
	@UiField(provided=true)
	protected ValueListBox<OptionList> answerSetListBox = new ValueListBox<OptionList>(new AbstractRenderer<OptionList>(){

		@Override
		public String render(OptionList value) {

			if(value != null && value.getName() != null){
				return value.getName();
			}
			
			return null;
		}
		
	});
	
	/**
	 * Capture the option list the user has created so it can be used 
	 * if "use existing answer set" is unselected
	 */
	OptionList userBackupOptionList;

	private Serializable userBackupAnswerWeights;
	
	private Boolean initialChoice = null;
	
	/**
	 * Constructor (default)
	 * 
	 * @param propertySet - The propertySet for the widget.
	 * @param listener - The listener that can be used to handle changes in the properties from the widget.
	 */
    public AnswerSetPropertySetWidget(AnswerSetPropertySet propertySet, PropertySetListener listener, final List<OptionList> sharedAnswerSets) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    isMultipleChoice = (listener instanceof MultipleChoiceWidget);
	    
	    existingAnswerSetCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
					
				AnswerSetPropertySet props = (AnswerSetPropertySet) propSet;
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ANSWER_SET, event.getValue());
				
				OptionList currentOptionList = (OptionList) props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
				List<Double> currentAnswerWeights = null;
				if(props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS) != null) {
					currentAnswerWeights = SurveyItemProperties.decodeDoubleListString((String) props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS));
				}
				
				if(initialChoice == null && currentAnswerWeights != null && !currentAnswerWeights.isEmpty() ) {
					// If the initial state hasn't been captured and the user has scoring entered, store the previous checkbox value as the initial state.
					initialChoice = !event.getValue();
				}
				
				if(event.getValue()){
					randomizeAnswersCheckBox.setValue(false, true);
					randomizeAnswersCheckBox.setEnabled(false);
					userBackupOptionList = currentOptionList;
					
					if(initialChoice != event.getValue() && !currentOptionList.getListOptions().isEmpty()) {
						// If the previous choice matches the initial choice, backup the scoring						
						userBackupAnswerWeights = props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
						props.getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
						
					} else if (userBackupAnswerWeights != null && initialChoice == event.getValue()) {
						// If the current choice matches the initial choice and there is backup scoring available, restore it
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, userBackupAnswerWeights);
					}
					
					if(!sharedAnswerSets.contains(currentOptionList)){
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, answerSetListBox.getValue() != null ? answerSetListBox.getValue() : sharedAnswerSets.get(0));
					}
					answerSetCollapse.show();
					
				} else {
					
					answerSetCollapse.hide();
					randomizeAnswersCheckBox.setEnabled(true);
					
					if(userBackupOptionList != null) {
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, userBackupOptionList);
												
						if(initialChoice == event.getValue()) {
							//If there is a backup available and the current choice matches the initial choice, restore the backup
							props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, userBackupAnswerWeights);
							
						} else if(currentAnswerWeights != null && !currentAnswerWeights.isEmpty()){
							// If the user has entered scoring, backup the current answer set and the scoring
							userBackupAnswerWeights = props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
							props.getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
						}
						
					} else {
						OptionList newOptionList = new OptionList();
						newOptionList.setId(0);
						newOptionList.setIsShared(false);
						newOptionList.setEditableToUserNames(new HashSet<String>(Arrays.asList(GatClientUtility.getUserName())));
						newOptionList.setVisibleToUserNames(new HashSet<String>(Arrays.asList(GatClientUtility.getUserName())));
						newOptionList.setListOptions(new ArrayList<ListOption>());
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY, newOptionList);
						props.getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
					}
					logger.info("optionList has been removed, now = " + (props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY)));
				}
				
				notifyPropertySetChanged();
			}
		});
	    
	    answerSetListBox.addValueChangeHandler(new ValueChangeHandler<OptionList>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<OptionList> event) {
				if(existingAnswerSetCheckBox.getValue()){
					AnswerSetPropertySet props = (AnswerSetPropertySet) propSet;
					
					props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY,  event.getValue());
					propListener.onPropertySetChange(propSet);
				}
			}
		});
	    
	    randomizeAnswersCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				AnswerSetPropertySet props = (AnswerSetPropertySet) propSet;
				
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE, event.getValue());
				propListener.onPropertySetChange(propSet);
			}
	    	
	    });
	    
	    //Only show the randomize checkbox for multiple choice questions
    	randomizeAnswersCheckBox.setVisible(isMultipleChoice);
	    
	    if(propertySet != null){
	    	
	        Serializable selectedAnswerSet = propertySet.getPropertyValue(SurveyPropertyKeyEnum.REPLY_OPTION_SET_KEY);
	        Serializable useExistingAnswerSet = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ANSWER_SET);
	        
	        // check option lists
	        boolean isSharedOptionList = false;
            if(selectedAnswerSet != null && selectedAnswerSet instanceof OptionList && ((OptionList) selectedAnswerSet).getIsShared()){
                randomizeAnswersCheckBox.setEnabled(false);
                randomizeAnswersCheckBox.setValue(false);
                for(OptionList optionList : sharedAnswerSets){
                    if(((OptionList) selectedAnswerSet).getName().equals(optionList.getName())){
                        answerSetListBox.setValue(optionList);
                        isSharedOptionList = true;
                        break;
                    }
                }
                answerSetListBox.setAcceptableValues(sharedAnswerSets);
            } else {
                answerSetListBox.setValue(sharedAnswerSets.get(0));
                answerSetListBox.setAcceptableValues(sharedAnswerSets);
            }
            
            // set user existing answer set
            if (useExistingAnswerSet != null && useExistingAnswerSet instanceof Boolean) {
                existingAnswerSetCheckBox.setValue((Boolean) useExistingAnswerSet);
                answerSetCollapse.show();
            } else if (isSharedOptionList) {
                // it is missing the property that indicates that this is a shared option list. Add it now.
                ((AnswerSetPropertySet) propSet).getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ANSWER_SET, true);
                
                existingAnswerSetCheckBox.setValue(true);
                answerSetCollapse.show();
            } else {
                existingAnswerSetCheckBox.setValue(false);
                answerSetCollapse.hide();
            }

            if (isMultipleChoice){
	    		
	    		Serializable randomize = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.RANDOMIZE);
		    	
		    	if(randomize != null && randomize instanceof Boolean){
		    		randomizeAnswersCheckBox.setValue((Boolean) randomize);
		    		
		    	} else {
		    		randomizeAnswersCheckBox.setValue(false);
		    	}
		    	
	    	}
	    	
	    	propListener.onPropertySetChange(propSet);
	    	
	    } else {
	    	answerSetCollapse.hide();
	    }
	    
	    
	}
    
}
