/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps;

import generated.sensor.BooleanEnum;
import generated.sensor.Filter;
import generated.sensor.Sensor;
import generated.sensor.SensorsConfiguration;
import generated.sensor.Writer;

import java.math.BigDecimal;
import java.math.BigInteger;

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationUtility;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class TransmitSensorDataEditor.
 */
public class TransmitSensorDataEditor extends Composite {
	
	/** The ui binder. */
    interface TransmitSensorDataEditorUiBinder extends UiBinder<Widget, TransmitSensorDataEditor> {} 
	private static TransmitSensorDataEditorUiBinder uiBinder = GWT.create(TransmitSensorDataEditorUiBinder.class);
	
	@UiField
	protected HTML caption;
	
	@UiField
	protected DoubleBox intervalDoubleBox;
	
	@UiField
	protected CheckBox distributeExternallyCheckBox;
	
	@UiField
	protected CheckBox writerCheckBox;
	
	@UiField
	protected CheckBox filterCheckBox;
	
	private Sensor sensor;
	
	private SensorsConfiguration sensorsConfiguration;
	
    public TransmitSensorDataEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Double> intervalHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double interval = changeEvent.getValue();
				String text = intervalDoubleBox.getText();
				if(interval == null) {
					//If the user deleted the value from the field.
					if(text.equals("")) {
						sensor.setInterval(null);
					}
					//If the user entered something other than a double then we
					//have to undo what they've entered.
					else {
						populateIntervalDoubleBox();
					}
				} else {
					sensor.setInterval(BigDecimal.valueOf(interval));
				}
			}
		};
		intervalDoubleBox.addValueChangeHandler(intervalHandler);
		
		ValueChangeHandler<Boolean> distributeExternallyHandler = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
				if(changeEvent.getValue()) {
					sensor.setDistributeExternally(BooleanEnum.TRUE);
				} else {
					sensor.setDistributeExternally(BooleanEnum.FALSE);
				}
			}
		};
		distributeExternallyCheckBox.addValueChangeHandler(distributeExternallyHandler);
		
		ValueChangeHandler<Boolean> writerHandler = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
				if(changeEvent.getValue()) {
					//Construct a new Writer appropriate for the selected SensorType.
					SensorTypeEnum sensorTypeEnum = SensorsConfigurationMaps.getInstance().getSensorImplToTypeMap().get(sensor.getSensorImpl());
					Writer writer = SensorsConfigurationFactory.createWriter(sensorTypeEnum);
					
					//Add the writer to the sensors configuration.
					sensorsConfiguration.getWriters().getWriter().add(writer);
					sensor.setWriterInstance(writer.getId());
				} else {
					//Remove the sensor's reference to its writer.
					sensor.setWriterInstance(null);
					
					//As a result the writer may become a dead node and
					//therefore need to be removed from the sensors
					//configuration.
					SensorsConfigurationUtility.removeUnreferencedWriters(sensorsConfiguration);
				}
			}
		};
		writerCheckBox.addValueChangeHandler(writerHandler);
		
		ValueChangeHandler<Boolean> filterHandler = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
				if(changeEvent.getValue()) {
					//Construct a new Writer appropriate for the selected SensorType
					SensorTypeEnum sensorTypeEnum = SensorsConfigurationMaps.getInstance().getSensorImplToTypeMap().get(sensor.getSensorImpl());
					Writer writer = SensorsConfigurationFactory.createWriter(sensorTypeEnum);
					sensorsConfiguration.getWriters().getWriter().add(writer);
					
					//Construct a new Filter appropriate for the selected SensorType.
					Filter filter = SensorsConfigurationFactory.createFilter(sensorTypeEnum, writer);
					sensorsConfiguration.getFilters().getFilter().add(filter);

					//Add the filter to the sensor.
					sensor.setFilterInstance(filter.getId());
				} else {
					//Remove the sensor's reference to its filter.
					sensor.setFilterInstance(null);
					
					//As a result there may now be filters/writers that aren't
					//referenced by any of the reference chains that begin with
					//the sensors...remove these dead nodes.
					SensorsConfigurationUtility.removeUnreferencedFiltersAndWriters(sensorsConfiguration);
				}
			}
		};
		filterCheckBox.addValueChangeHandler(filterHandler);
        
        
    }
    
    public void setSensor(Sensor sensor, SensorsConfiguration sensorsConfiguration) {
    	this.sensor = sensor;
    	this.sensorsConfiguration = sensorsConfiguration;
    	
    	BigDecimal interval = sensor.getInterval();
    	if(interval == null) {
    		intervalDoubleBox.setText("");
    	} else {
    		intervalDoubleBox.setText(interval.toString());
    	}
    	    	BooleanEnum distributeExternally = sensor.getDistributeExternally();
    	if(distributeExternally == null || distributeExternally == BooleanEnum.FALSE) {
    		distributeExternallyCheckBox.setValue(false);
    	} else {
    		distributeExternallyCheckBox.setValue(true);
    	}
    	
    	BigInteger writerInstance = sensor.getWriterInstance();
    	if(writerInstance == null) {
    		writerCheckBox.setValue(false);
    	} else {
    		writerCheckBox.setValue(true);
    	}
    	
    	BigInteger filterInstance = sensor.getFilterInstance();
    	if(filterInstance == null) {
    		filterCheckBox.setValue(false);
    	} else {
    		filterCheckBox.setValue(true);
    	}
    }
    
    public void addWriterValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    	writerCheckBox.addValueChangeHandler(handler);
    }
    
    public void addFilterValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    	filterCheckBox.addValueChangeHandler(handler);
    }
    
    private void populateIntervalDoubleBox() {
    	BigDecimal interval = sensor.getInterval();
    	if(interval == null) {
    		intervalDoubleBox.setText("");
    	} else {
    		intervalDoubleBox.setText(interval.toString());
    	}
    }
    
    public void setTitle() {
    	caption.setText("Transmit Sensor Data");
    }
}
