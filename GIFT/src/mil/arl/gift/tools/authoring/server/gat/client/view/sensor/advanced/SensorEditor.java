/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced;

import generated.sensor.Filter;
import generated.sensor.Sensor;
import generated.sensor.SensorsConfiguration;
import generated.sensor.Writer;

import java.math.BigInteger;

import mil.arl.gift.common.io.Constants;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps.ArchiveFilteredDataEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps.ArchiveSensorDataEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps.FilterSensorDataEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps.SensorNameAndInputEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps.TransmitFilteredDataEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps.TransmitSensorDataEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class SensorEditor.
 */
public class SensorEditor extends Composite {
	
	/** The ui binder. */
    interface SensorEditorUiBinder extends UiBinder<Widget, SensorEditor> {} 
	private static SensorEditorUiBinder uiBinder = GWT.create(SensorEditorUiBinder.class);
	
	@UiField
	protected DeckPanel deckPanel;
	
	@UiField
	protected SensorNameAndInputEditor sensorNameAndInputEditor;
	
	@UiField
	protected TransmitSensorDataEditor transmitSensorDataEditor;
	
	@UiField
	protected ArchiveSensorDataEditor archiveSensorDataEditor;
	
	@UiField
	protected FilterSensorDataEditor filterSensorDataEditor;
	
	@UiField
	protected TransmitFilteredDataEditor transmitFilteredDataEditor;
	
	@UiField
	protected ArchiveFilteredDataEditor archiveFilteredDataEditor;
	
	@UiField
	protected HTML requiredFieldLabel;
	
	private SensorsConfiguration sensorsConfiguration;
	
	private Sensor sensor;
	
    public SensorEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        requiredFieldLabel.setHTML(Constants.REQUIRED_FIELD_LABEL_HTML);
        
        ValueChangeHandler<Boolean> updateEditorVisibilityListener = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
				updateEditorVisibility();
			}
		};
		
		deckPanel.showWidget(0);
		
        transmitSensorDataEditor.addFilterValueChangeHandler(updateEditorVisibilityListener);
        transmitSensorDataEditor.addWriterValueChangeHandler(updateEditorVisibilityListener);
        transmitFilteredDataEditor.addWriterValueChangeHandler(updateEditorVisibilityListener);
    }
    
    public void setSensor(Sensor sensor, SensorsConfiguration sensorsConfiguration) {
    	this.sensor = sensor;
    	this.sensorsConfiguration = sensorsConfiguration;
    	
    	sensorNameAndInputEditor.setSensor(sensor);
    	transmitSensorDataEditor.setSensor(sensor, sensorsConfiguration);
    	populateArchiveSensorDataEditor();
    	populateFilterSensorDataEditor();
    	populateFilteredDataEditor();
    	populateArchiveFilteredDataEditor();
    	
    	updateEditorVisibility();
    }
    
    private void updateEditorVisibility() {
    	boolean showStep3 = true;
    	boolean showStep4 = true;
    	boolean showStep5 = true;
    	boolean showStep6 = true;
    	
    	if(sensor.getWriterInstance() == null) {
    		showStep3 = false;
    	}
    	
    	if(sensor.getFilterInstance() == null) {
    		showStep4 = false;
    		showStep5 = false;
    		showStep6 = false;
    	} else {
    		Filter filter = SensorsConfigurationUtility.getFilter(sensorsConfiguration, sensor.getFilterInstance());
    		if(filter.getWriterInstance() == null) {
    			showStep6 = false;
    		}
    	}

    	sensorNameAndInputEditor.setTitle();
    	sensorNameAndInputEditor.setVisible(true);

    	transmitSensorDataEditor.setTitle();
    	transmitSensorDataEditor.setVisible(true);
    	
    	if(showStep3) {
    		if(!archiveSensorDataEditor.isVisible()) {
    			populateArchiveSensorDataEditor();
    			archiveSensorDataEditor.setVisible(true);
    		}
    		archiveSensorDataEditor.setTitlePrefix();
      	} else {
    		archiveSensorDataEditor.setVisible(false);
    	}
    	
    	if(showStep4) {
    		if(!filterSensorDataEditor.isVisible()) {
    			populateFilterSensorDataEditor();
    			filterSensorDataEditor.setVisible(true);
    		}
    		filterSensorDataEditor.setTitle();
    	} else {
    		filterSensorDataEditor.setVisible(false);
    	}
    	
    	if(showStep5) {
    		if(!transmitFilteredDataEditor.isVisible()) {
    			populateTransmitFilteredDataEditor();
        		transmitFilteredDataEditor.setVisible(true);
    		}
    		transmitFilteredDataEditor.setTitle();
    	} else {
    		transmitFilteredDataEditor.setVisible(false);
    	}
    	
    	if(showStep6) {
    		if(!archiveFilteredDataEditor.isVisible()) {
    			populateArchiveFilteredDataEditor();
    			archiveFilteredDataEditor.setVisible(true);
    		}
    		archiveFilteredDataEditor.setTitle();
    	} else {
    		archiveFilteredDataEditor.setVisible(false);
    	}
    }
    
    private void populateArchiveSensorDataEditor() {
    	BigInteger writerId = sensor.getWriterInstance();
    	if(writerId != null) {
    		Writer writer = SensorsConfigurationUtility.getWriter(sensorsConfiguration, writerId);
    		archiveSensorDataEditor.setWriter(writer, sensorsConfiguration);
    	}
    }
    
    private void populateFilterSensorDataEditor() {
    	BigInteger filterId = sensor.getFilterInstance();
    	if(filterId != null) {
    		Filter filter = SensorsConfigurationUtility.getFilter(sensorsConfiguration, filterId);
    		filterSensorDataEditor.setFilter(filter, sensorsConfiguration);
    	}
    }
    
    private void populateFilteredDataEditor() {
    	BigInteger filterId = sensor.getFilterInstance();
    	if(filterId != null) {
    		Filter filter = SensorsConfigurationUtility.getFilter(sensorsConfiguration, filterId);
    		transmitFilteredDataEditor.setFilter(filter, sensorsConfiguration);
    	}
    }
    
    private void populateTransmitFilteredDataEditor() {
    	BigInteger filterId = sensor.getFilterInstance();
    	if(filterId != null) {
    		Filter filter = SensorsConfigurationUtility.getFilter(sensorsConfiguration, filterId);
        	transmitFilteredDataEditor.setFilter(filter, sensorsConfiguration);
    	}
    }
    
    private void populateArchiveFilteredDataEditor() {
    	BigInteger filterId = sensor.getFilterInstance();
		Filter filter = SensorsConfigurationUtility.getFilter(sensorsConfiguration, filterId);
		if(filter != null) {
	    	BigInteger writerId = filter.getWriterInstance();
	    	if(writerId != null) {
	    		Writer writer = SensorsConfigurationUtility.getWriter(sensorsConfiguration, writerId);
	    		archiveFilteredDataEditor.setWriter(writer, sensorsConfiguration);
	    	}
	    }
    }
    
    /**
     * Sets whether or not the sensor editor should be visible
     * 
     * @param visible  whether or not the sensor editor should be visible
     */
    public void setSensorEditorVisible(boolean visible){
    	
		if(visible){
			deckPanel.showWidget(1);
			
		} else {
			deckPanel.showWidget(0);
		}
    }
    
    /**
     * Adds a callback that invokes logic when a sensor's name is changed
     * 
     * @param callback a callback that invokes logic when a sensor's name is changed
     * @return whether or not the callback was added
     */
    public boolean addSensorNameChangedCallback(SensorNameChangedCallback callback){
    	return sensorNameAndInputEditor.addSensorNameChangedCallback(callback);
    }
    
    /**
     * Removes a callback that invokes logic when a sensor's name is changed
     * 
     * @param callback a callback that invokes logic when a sensor's name is changed
     * @return whether or not the callback was removed
     */
    public boolean removeSensorNameChangedCallback(SensorNameChangedCallback callback){
    	return sensorNameAndInputEditor.removeSensorNameChangedCallback(callback);
    }
}
