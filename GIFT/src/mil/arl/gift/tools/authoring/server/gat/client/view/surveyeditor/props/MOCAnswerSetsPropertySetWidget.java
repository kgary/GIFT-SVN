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

import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.MOCAnswerSetsPropertySet;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor.props.PropertySetListener;
import mil.arl.gift.common.survey.ListOption;
import mil.arl.gift.common.survey.MatrixOfChoicesReplyWeights;
import mil.arl.gift.common.survey.OptionList;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientUtility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;

/**
 * The MOCAnswerSetsPropertySetWidget is responsible for displaying the controls that allow
 * the author to use an existing answer set rather than use custom answer sets.
 * 
 * @author wpearigen
 *
 */
public class MOCAnswerSetsPropertySetWidget extends AbstractPropertySetWidget  {

    private static Logger logger = Logger.getLogger(MOCAnswerSetsPropertySetWidget.class.getName());
    
	private static WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);

	interface WidgetUiBinder extends
			UiBinder<Widget, MOCAnswerSetsPropertySetWidget> {
	}
	
	@UiField
	protected CheckBox existingRowAnswerSetCheckBox;
	
	@UiField
	protected CheckBox existingColumnAnswerSetCheckBox;
	
	@UiField
	protected Collapse columnCollapse;
	
	@UiField
	protected Collapse rowCollapse;
	
	@UiField(provided=true)
	protected ValueListBox<OptionList> rowAnswerSetListBox = new ValueListBox<OptionList>(new AbstractRenderer<OptionList>(){

		@Override
		public String render(OptionList value) {

			if(value != null && value.getName() != null){
				return value.getName();
			}
			
			return null;
		}
		
	});
	
	@UiField(provided=true)
	protected ValueListBox<OptionList> columnAnswerSetListBox = new ValueListBox<OptionList>(new AbstractRenderer<OptionList>(){

		@Override
		public String render(OptionList value) {

			if(value != null && value.getName() != null){
				return value.getName();
			}
			
			return null;
		}
		
	});
	
	/**
	 * Capture the option lists the user has created so they can be used 
	 * if "use existing answer set" is unselected
	 */
	private OptionList userBackupColumnOptionList;
	private OptionList userBackupRowOptionList;
	private MatrixOfChoicesReplyWeights userBackupAnswerWeights;
	private MOCState initialState = null;
	
	/**
	 * Constructor (default)
	 * 
	 * @param propertySet - The propertySet for the widget.
	 * @param listener - The listener that can be used to handle changes in the properties from the widget.
	 */
    public MOCAnswerSetsPropertySetWidget(MOCAnswerSetsPropertySet propertySet, PropertySetListener listener, final List<OptionList> sharedAnswerSets) {
	    super(propertySet, listener);
	    logger.info("constructor()");
	    initWidget(uiBinder.createAndBindUi(this));
	    
	    existingColumnAnswerSetCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
					
				MOCAnswerSetsPropertySet props = (MOCAnswerSetsPropertySet) propSet;
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET, event.getValue());

				MatrixOfChoicesReplyWeights currentAnswerWeights = (MatrixOfChoicesReplyWeights) props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
				// Check whether or not the user entered scores
				boolean emptyReplies = (currentAnswerWeights.getReplyWeights() == null || currentAnswerWeights.getReplyWeights().isEmpty());
				if(!emptyReplies) {
					// Check to make sure the answer weight array isn't empty
					emptyReplies = (currentAnswerWeights.getReplyWeights().size() == 1 && currentAnswerWeights.getReplyWeights().get(0).isEmpty());
				}
				
				if(initialState == null && !emptyReplies) {
					// If the initial state has not been captured yet and the user has entered scoring values, store the previous checkbox values as the initial state.
					initialState = new MOCState(existingRowAnswerSetCheckBox.getValue(), !existingColumnAnswerSetCheckBox.getValue());
				}
				
				if(event.getValue()){
					// If 'Use Existing Column Answer Set' is checked
					columnCollapse.show();
					userBackupColumnOptionList = (OptionList) props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
					
					if(!emptyReplies && initialState.matches(existingRowAnswerSetCheckBox.getValue(), !existingColumnAnswerSetCheckBox.getValue())) {
						// If the user has entered scoring and the previous state of the checkboxes matches the initial state, backup the current scoring values						
						userBackupAnswerWeights = new MatrixOfChoicesReplyWeights(new ArrayList<List<Double>>(currentAnswerWeights.getReplyWeights()));							
						props.getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
						
					} else if (userBackupAnswerWeights != null && initialState.matches(existingRowAnswerSetCheckBox.getValue(), existingColumnAnswerSetCheckBox.getValue())){
						// If backup scores are available and the current state of checkboxes matches the initial state, restore the backed up scores						
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, userBackupAnswerWeights);
					}
					
					if(!sharedAnswerSets.contains(props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY))){
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, columnAnswerSetListBox.getValue() != null ? columnAnswerSetListBox.getValue() : sharedAnswerSets.get(0));
					}
					
				} else {
					// 'Use Existing Column Answer Set' is unchecked
					
					columnCollapse.hide();
					if(userBackupColumnOptionList != null){
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, userBackupColumnOptionList);
						
						if(userBackupAnswerWeights != null && initialState.matches(existingRowAnswerSetCheckBox.getValue(), existingColumnAnswerSetCheckBox.getValue())){
							// If backup scores are available and the current state of checkboxes matches the initial state, restore the backed up scores							
							props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, userBackupAnswerWeights);
							
						} else if(!emptyReplies && initialState.matches(existingRowAnswerSetCheckBox.getValue(), !existingColumnAnswerSetCheckBox.getValue())) {
							// If the user has entered scoring and the previous state of the checkboxes matches the initial state, backup the current scoring values							
							userBackupAnswerWeights = new MatrixOfChoicesReplyWeights(new ArrayList<List<Double>>(currentAnswerWeights.getReplyWeights()));							
							props.getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
							
						}
						
					} else {
						OptionList newOptionList = new OptionList();
						newOptionList.setId(0);
						newOptionList.setIsShared(false);
						newOptionList.setEditableToUserNames(new HashSet<String>(Arrays.asList(GatClientUtility.getUserName())));
						newOptionList.setVisibleToUserNames(new HashSet<String>(Arrays.asList(GatClientUtility.getUserName())));
						newOptionList.setListOptions(new ArrayList<ListOption>());
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, newOptionList);
					}
					logger.info("optionList has been removed, now = " + (props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY)));
				}
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    existingRowAnswerSetCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
					
				MOCAnswerSetsPropertySet props = (MOCAnswerSetsPropertySet) propSet;
				props.getProperties().setBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET, true);								
				
				MatrixOfChoicesReplyWeights currentAnswerWeights = (MatrixOfChoicesReplyWeights) props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
				// Check whether or not the user entered scores
				boolean emptyReplies = (currentAnswerWeights.getReplyWeights() == null || currentAnswerWeights.getReplyWeights().isEmpty());
				if(!emptyReplies) {
					// Check to make sure the answer weight array isn't empty
					emptyReplies = (currentAnswerWeights.getReplyWeights().size() == 1 && currentAnswerWeights.getReplyWeights().get(0).isEmpty());
				}
				
				if(initialState == null && !emptyReplies) {
					// If the initial state has not been captured yet and the user has entered scoring values, store the current checkbox values as the initial state.
					initialState = new MOCState(!existingRowAnswerSetCheckBox.getValue(), existingColumnAnswerSetCheckBox.getValue());
				}
				
				if(event.getValue()){
					// If 'Use Existing Row Answer Set' is checked
					
					rowCollapse.show();
					userBackupRowOptionList = (OptionList) props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
					
					if(!emptyReplies && initialState.matches(!existingRowAnswerSetCheckBox.getValue(), existingColumnAnswerSetCheckBox.getValue())) {
						// If the user has entered scoring and the previous state of the checkboxes matches the initial state, backup the current scoring values						
						userBackupAnswerWeights = new MatrixOfChoicesReplyWeights(new ArrayList<List<Double>>(currentAnswerWeights.getReplyWeights()));
						props.getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);

					} else if(userBackupAnswerWeights != null && initialState.matches(existingRowAnswerSetCheckBox.getValue(), existingColumnAnswerSetCheckBox.getValue())){
						// If backup scores are available and the current state of checkboxes matches the initial state, restore the backed up scores						
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, userBackupAnswerWeights);
					}
					
					if(!sharedAnswerSets.contains(props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY))){
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, rowAnswerSetListBox.getValue() != null ? rowAnswerSetListBox.getValue() : sharedAnswerSets.get(0));
					}
					
				} else {
					// 'Use Existing Row Answer Set' is unchecked
					
					rowCollapse.hide();
					if(userBackupRowOptionList != null){
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, userBackupRowOptionList);
												
						if(userBackupAnswerWeights != null && initialState.matches(existingRowAnswerSetCheckBox.getValue(), existingColumnAnswerSetCheckBox.getValue())){
							// If backup scores are available and the current state of checkboxes matches the initial state, restore the backed up scores							
							props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ANSWER_WEIGHTS, userBackupAnswerWeights);
							
						} else if(!emptyReplies && initialState.matches(!existingRowAnswerSetCheckBox.getValue(), existingColumnAnswerSetCheckBox.getValue())) {
							// If the user has entered scoring and the previous state of the checkboxes matches the initial state, backup the current scoring values							
							userBackupAnswerWeights = new MatrixOfChoicesReplyWeights(new ArrayList<List<Double>>(currentAnswerWeights.getReplyWeights()));							
							props.getProperties().removeProperty(SurveyPropertyKeyEnum.ANSWER_WEIGHTS);
						}
						
					} else {
						OptionList newOptionList = new OptionList();
						newOptionList.setId(0);
						newOptionList.setIsShared(false);
						newOptionList.setEditableToUserNames(new HashSet<String>(Arrays.asList(GatClientUtility.getUserName())));
						newOptionList.setVisibleToUserNames(new HashSet<String>(Arrays.asList(GatClientUtility.getUserName())));
						newOptionList.setListOptions(new ArrayList<ListOption>());
						props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, newOptionList);
					}
					logger.info("optionList has been removed, now = " + (props.getProperties().getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY)));
				}
				
				propListener.onPropertySetChange(propSet);
			}
		});
	    
	    columnAnswerSetListBox.addValueChangeHandler(new ValueChangeHandler<OptionList>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<OptionList> event) {
				if(existingColumnAnswerSetCheckBox.getValue()){
					MOCAnswerSetsPropertySet props = (MOCAnswerSetsPropertySet) propSet;
					
					props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY, event.getValue());
					propListener.onPropertySetChange(propSet);
				}
			}
		});
	    
	    rowAnswerSetListBox.addValueChangeHandler(new ValueChangeHandler<OptionList>() {
			
			@Override
			public void onValueChange(ValueChangeEvent<OptionList> event) {
				if(existingRowAnswerSetCheckBox.getValue()){
					MOCAnswerSetsPropertySet props = (MOCAnswerSetsPropertySet) propSet;
					
					props.getProperties().setPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY, event.getValue());
					propListener.onPropertySetChange(propSet);
				}
			}
		});
	    
	    if(propertySet != null){
	    	
	    	Serializable useExistingColumnAnswerSet = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_COLUMN_ANSWER_SET);
	    	
	    	if(useExistingColumnAnswerSet != null && useExistingColumnAnswerSet instanceof Boolean){
	    		existingColumnAnswerSetCheckBox.setValue((Boolean) useExistingColumnAnswerSet);
	    	} else {
	    		existingColumnAnswerSetCheckBox.setValue(null);
	    	}
	    	
	    	if(existingColumnAnswerSetCheckBox.getValue()) {
	    		columnCollapse.show();
	    	} else {
	    		columnCollapse.hide();
	    	}
	    	
	    	Serializable useExistingRowAnswerSet = propertySet.getBooleanPropertyValue(SurveyPropertyKeyEnum.USE_EXISTING_ROW_ANSWER_SET);
	    	
	    	if(useExistingRowAnswerSet != null && useExistingRowAnswerSet instanceof Boolean){
	    		existingRowAnswerSetCheckBox.setValue((Boolean) useExistingRowAnswerSet);
	    	} else {
	    		existingRowAnswerSetCheckBox.setValue(null);
	    	}
	    	
	    	if(existingRowAnswerSetCheckBox.getValue()) {
	    		rowCollapse.show();
	    	} else {
	    		rowCollapse.hide();
	    	}
	    	
	    	Serializable selectedColumnAnswerSet = propertySet.getPropertyValue(SurveyPropertyKeyEnum.COLUMN_OPTIONS_KEY);
	    	
	    	if(selectedColumnAnswerSet != null && selectedColumnAnswerSet instanceof OptionList && ((OptionList) selectedColumnAnswerSet).getIsShared()){
	    		for(OptionList optionList : sharedAnswerSets){
	    			if(((OptionList) selectedColumnAnswerSet).getName().equals(optionList.getName())){
	    				columnAnswerSetListBox.setValue(optionList);
	    				break;
	    			}
	    		}
	    		columnAnswerSetListBox.setAcceptableValues(sharedAnswerSets);
	    	} else {
	    		columnAnswerSetListBox.setValue(sharedAnswerSets.get(0));
	    		columnAnswerSetListBox.setAcceptableValues(sharedAnswerSets);
	    	}
	    	
	    	Serializable selectedRowAnswerSet = propertySet.getPropertyValue(SurveyPropertyKeyEnum.ROW_OPTIONS_KEY);
	    	
	    	if(selectedRowAnswerSet != null && selectedRowAnswerSet instanceof OptionList && ((OptionList) selectedRowAnswerSet).getIsShared()){
	    		for(OptionList optionList : sharedAnswerSets){
	    			if(((OptionList) selectedRowAnswerSet).getName().equals(optionList.getName())){
	    				rowAnswerSetListBox.setValue(optionList);
	    				break;
	    			}
	    		}
	    		rowAnswerSetListBox.setAcceptableValues(sharedAnswerSets);
	    	} else {
	    		rowAnswerSetListBox.setValue(sharedAnswerSets.get(0));
	    		rowAnswerSetListBox.setAcceptableValues(sharedAnswerSets);
	    	}
	    	
	    	
	    	propListener.onPropertySetChange(propSet);
	    }
	    
	    
	}
    
    /**
     * Class to help keep track of which answer set options the user
     * initially selected. This allows the scores to be backed up 
     * and restored when appropriate.
     */
    private class MOCState {
    	
    	// Whether or not the user initially selected 'Use Existing Row Answer Set'
    	private boolean useExistingRow = false;
    	
    	// Whether or not the user initially selected 'Use Existing Column Answer Set'
    	private boolean useExistingCol = false;
    	
    	/**
    	 * Constructor. Creates a new state that stores the initial options selected by the user
    	 * 
    	 * @param useExistingRow true if the user initially selected 'Use Existing Row Answer Set'.
    	 * @param useExistingCol true if the user initially selected 'Use Existing Column Answer Set'
    	 */
    	public MOCState(boolean useExistingRow, boolean useExistingCol) {
    		this.useExistingRow = useExistingRow;
    		this.useExistingCol = useExistingCol;
    	}
    	
    	/**
    	 * Checks to see whether or not the current answer set options match the intial options
    	 * that were selected by the user.
    	 * 
    	 * @param useExistingRow The value for the 'Use Existing Row Answer Set' checkbox 
    	 * @param useExistingCol The value for the 'Use Existing Column Answer Set' checkbox
    	 * @return true if the options match, false otherwise.
    	 */
    	public boolean matches(boolean useExistingRow, boolean useExistingCol) {
    		return (this.useExistingRow == useExistingRow && this.useExistingCol == useExistingCol);
    	}
    }
    
}
