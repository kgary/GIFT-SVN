/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps;

import generated.sensor.ECGDetectionFilterInput;
import generated.sensor.Filter;
import generated.sensor.GSRDetectionFilterInput;
import generated.sensor.SensorsConfiguration;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.FilterTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.ECGDetectionFilterInputEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.GSRDetectionFilterInputEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.FilterTypeValueListBox;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class FilterSensorDataEditor.
 */
public class FilterSensorDataEditor extends Composite {
	
	/** The ui binder. */
    interface FilterSensorDataEditorUiBinder extends UiBinder<Widget, FilterSensorDataEditor> {} 
	private static FilterSensorDataEditorUiBinder uiBinder = GWT.create(FilterSensorDataEditorUiBinder.class);
	
	@UiField
	protected HTML caption;
	
	@UiField
	protected FilterTypeValueListBox filterTypeListBox;
	
	@UiField
	protected GSRDetectionFilterInputEditor gsrDetectionFilterInputEditor;
	
	@UiField
	protected ECGDetectionFilterInputEditor ecgDetectionFilterInputEditor;
	
	@UiField
	protected Label initializationWarningLabel;
	
	@UiField
	protected Label referenceWarningLabel;
	
	private Filter filter;
	
    public FilterSensorDataEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
  		
  		//If the user selects a new filter impl then that change needs to
  		//cascade to the filter object.
  		ValueChangeHandler<FilterTypeEnum> filterTypeHandler = new ValueChangeHandler<FilterTypeEnum>(){
  			@Override
  			public void onValueChange(ValueChangeEvent<FilterTypeEnum> changeEvent) {
  				//Figure out what filter type the user selected.
  				FilterTypeEnum filterType = changeEvent.getValue();
  				
  				//Convert that filterType to a filterImpl and save it in the filter object.
  				String filterImpl = SensorsConfigurationMaps.getInstance().getFilterTypeToImplMap().get(filterType);
  				filter.setFilterImpl(filterImpl);
  				
  				//Construct an input object that corresponds to that filter type.
  				Serializable filterInput = SensorsConfigurationFactory.createFilterInputType(filterType);
  				filter.getFilterInput().setType(filterInput);
  				
  				//Display an editor for the input object.
  				showInputEditor();
  				
  				//If the warnings were visible then make sure we remove them when
  				//the user selects a new filter type and we automatically
  				//pair it with a new input.
  				initializationWarningLabel.setVisible(false);
  				referenceWarningLabel.setVisible(false);
  			}
  		};
  		filterTypeListBox.addValueChangeHandler(filterTypeHandler);
    }
    
    public void setFilter(Filter filter, SensorsConfiguration sensorsConfiguration) {
    	this.filter = filter;
    	
    	//Select the filter type from the list that corresponds to the FilterImpl. 
    	SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
    	String filterImpl = filter.getFilterImpl();
    	FilterTypeEnum filterType = sensorConfigurationMaps.getFilterImplToTypeMap().get(filterImpl);
    	filterTypeListBox.setValue(filterType);
    	
    	//Create a default input object that should be used for this type of filter
    	//and grab the actual input object being used. We'll compare them next.
    	Serializable defaultInput = SensorsConfigurationFactory.createFilterInputType(filterType);
    	Serializable actualInput = filter.getFilterInput().getType();
    		
    	//If the expected input type matches with the actual input type then we 
    	//just have to make sure the warning label isn't on screen.
    	if( 	(actualInput == null && defaultInput == null) ||
    			(actualInput != null && defaultInput != null && actualInput.getClass() == defaultInput.getClass())) {
    		initializationWarningLabel.setVisible(false);
    	}
    	//Otherwise we have to replace the invalid existing input with
    	//something valid and warn the user about the change we've made.
    	else {    		
    		filter.getFilterInput().setType(defaultInput);
    		actualInput = defaultInput;
    		
    		initializationWarningLabel.setVisible(true);
    	}
    	
    	//Let the user know if this Filter is referenced by more than one Sensor or Filter.
    	if(SensorsConfigurationUtility.isFilterReferencedMoreThanOnce(sensorsConfiguration, filter.getId())) {
    		referenceWarningLabel.setVisible(true);
    	} else {
    		referenceWarningLabel.setVisible(false);
    	}
    	
    	//Show the appropriate editor for the input object.
    	showInputEditor();
    }
    
    public void setTitle() {
    	caption.setText("Filter Sensor Data");
    }
    
    private void showInputEditor() {
    	Serializable input = filter.getFilterInput().getType();

    	if(input instanceof GSRDetectionFilterInput) {
    		GSRDetectionFilterInput gsrDetectionFilterInput = (GSRDetectionFilterInput)input;
    		gsrDetectionFilterInputEditor.setGSRDetectionFilterInput(gsrDetectionFilterInput);
    		gsrDetectionFilterInputEditor.setVisible(true);
    	} else {
    		gsrDetectionFilterInputEditor.setVisible(false);
    	}

    	if(input instanceof ECGDetectionFilterInput) {
    		ECGDetectionFilterInput ecgDetectionFilterInput = (ECGDetectionFilterInput)input;
    		ecgDetectionFilterInputEditor.setECGDetectionFilterInput(ecgDetectionFilterInput);
    		ecgDetectionFilterInputEditor.setVisible(true);
    	} else {
    		ecgDetectionFilterInputEditor.setVisible(false);
    	}
    }
}
