/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps;

import generated.sensor.KinectSensor;
import generated.sensor.MouseTempHumiditySurrogateSensor;
import generated.sensor.SelfAssessmentSensor;
import generated.sensor.Sensor;
import generated.sensor.SineWaveSensor;
import generated.sensor.VhtMultisenseSensor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.SensorNameChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.KinectSensorEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.MouseTempHumiditySurrogateSensorEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.SelfAssessmentSensorEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.SineWaveSensorEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.VhtMultisenseSensorEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.SensorTypeValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class SensorNameAndInputEditor.
 */
public class SensorNameAndInputEditor extends Composite {
	
	/** The ui binder. */
    interface SensorNameAndInputEditorUiBinder extends UiBinder<Widget, SensorNameAndInputEditor> {} 
	private static SensorNameAndInputEditorUiBinder uiBinder = GWT.create(SensorNameAndInputEditorUiBinder.class);
	
	
	@UiField
	protected HTML caption;
	
	@UiField
	protected TextBox sensorNameTextBox;
	
	@UiField
	protected SensorTypeValueListBox sensorTypeListBox;
	
	@UiField
	protected SelfAssessmentSensorEditor selfAssessmentSensorEditor;
	
	@UiField
	protected MouseTempHumiditySurrogateSensorEditor mouseTempHumiditySurrogateSensorEditor;
	
	@UiField
	protected SineWaveSensorEditor sineWaveSensorEditor;
	
	@UiField
	protected VhtMultisenseSensorEditor vhtMultisenseSensorEditor;
	
	@UiField
	protected KinectSensorEditor kinectSensorEditor;
	
	@UiField
	protected Label warningLabel;
	
	private Sensor sensor;
	
	private List<SensorNameChangedCallback> nameChangedCallbacks = new ArrayList<SensorNameChangedCallback>();
	
    public SensorNameAndInputEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        warningLabel.setVisible(false);
        
        //Any changes the user makes to the name field need to cascade to the
        //sensor object.
        ValueChangeHandler<String> nameHandler = new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> changeEvent) {
				String name = changeEvent.getValue();
				
				if(name == null || name.isEmpty()){
					sensorNameTextBox.setValue(sensor.getName());
					
				} else {
					
					sensor.setName(name);
					
					invokeNameChangedCallbacks(sensor);
				}	
			}
		};
        sensorNameTextBox.addValueChangeHandler(nameHandler);
        
        //If the user selects a new SensorImpl then that change needs to
        //cascade to the sensor object.
        ValueChangeHandler<SensorTypeEnum> sensorTypeHandler = new ValueChangeHandler<SensorTypeEnum>(){
			@Override
			public void onValueChange(ValueChangeEvent<SensorTypeEnum> changeEvent) {
				//Convert that SensorType to a sensorImpl and save it in the sensor object.
				SensorTypeEnum sensorTypeEnum = changeEvent.getValue();
				String sensorImpl = SensorsConfigurationMaps.getInstance().getSensorTypeToImplMap().get(sensorTypeEnum);
				sensor.setSensorImpl(sensorImpl);
				
				//Construct an input object that corresponds to that SensorTypeEnum.
				Serializable input = SensorsConfigurationFactory.createSensorInputType(sensorTypeEnum);
				sensor.getSensorInput().setType(input);
				
				//Display an editor for the input object.
				showInputEditor();
				
				//If the warning was visible then make sure we remove it when
				//the user selects a new sensor type and we automatically
				//pair it with a new input.
				warningLabel.setVisible(false);
			}
		};
        sensorTypeListBox.addValueChangeHandler(sensorTypeHandler);
    }
    
    public void setSensor(Sensor sensor) {
    	this.sensor = sensor;
    	
    	sensorNameTextBox.setText(sensor.getName());
    	
    	//Select the SensorTypEnum from the list that corresponds to the SensorImpl. 
    	SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
    	String sensorImpl = sensor.getSensorImpl();
    	SensorTypeEnum sensorTypeEnum = sensorConfigurationMaps.getSensorImplToTypeMap().get(sensorImpl);
    	sensorTypeListBox.setValue(sensorTypeEnum);
    	
    	//Create a default input object that should be used for this type of sensor
    	//and grab the actual input object being used. We'll compare them next.
    	Serializable defaultInput = SensorsConfigurationFactory.createSensorInputType(sensorTypeEnum);
    	
    	Serializable actualInput = null;
    	
    	if(sensor.getSensorInput() != null){
    		actualInput = sensor.getSensorInput().getType();
    	}
    	
    	//If the expected input type matches with the actual input type then we 
    	//just have to make sure the warning label isn't on screen.
    	if( 	(actualInput == null && defaultInput == null) ||
    			(actualInput != null && defaultInput != null && actualInput.getClass() == defaultInput.getClass())) {
    		warningLabel.setVisible(false);
    	}
    	//Otherwise we have to replace the invalid existing input with
    	//something valid and warn the user about the change we've made.
    	else {    		
    		sensor.getSensorInput().setType(defaultInput);
    		actualInput = defaultInput;
    		
    		warningLabel.setVisible(true);
    	}
    	
    	//Show the appropriate editor for the input object.
    	showInputEditor();
    }
    
    private void showInputEditor() {
    	
    	if(sensor.getSensorInput() != null){
    		
	    	Serializable input = sensor.getSensorInput().getType();
	    	
	    	if(input instanceof SelfAssessmentSensor) {
	    		SelfAssessmentSensor selfAssessmentSensor = (SelfAssessmentSensor)input;
	    		selfAssessmentSensorEditor.setSelfAssessmentSensor(selfAssessmentSensor);
	    		selfAssessmentSensorEditor.setVisible(true);
	    	} else {
	    		selfAssessmentSensorEditor.setVisible(false);
	    	}
	
	    	if(input instanceof MouseTempHumiditySurrogateSensor) {
	    		MouseTempHumiditySurrogateSensor mouseTempHumiditySurrogateSensor = (MouseTempHumiditySurrogateSensor)input;
	    		mouseTempHumiditySurrogateSensorEditor.setMouseTempHumiditySurrogateSensor(mouseTempHumiditySurrogateSensor);
	    		mouseTempHumiditySurrogateSensorEditor.setVisible(true);
	    	} else {
	    		mouseTempHumiditySurrogateSensorEditor.setVisible(false);
	    	}
	    	
	    	if(input instanceof SineWaveSensor) {
	    		SineWaveSensor sineWaveSensor = (SineWaveSensor)input;
	    		sineWaveSensorEditor.setSineWaveSensor(sineWaveSensor);
	    		sineWaveSensorEditor.setVisible(true);
	    	} else {
	    		sineWaveSensorEditor.setVisible(false);
	    	}
	    	
	    	if(input instanceof VhtMultisenseSensor) {
	    		VhtMultisenseSensor vhtMultisenseSensor = (VhtMultisenseSensor)input;
	    		vhtMultisenseSensorEditor.setVhtMultisenseSensor(vhtMultisenseSensor);
	    		vhtMultisenseSensorEditor.setVisible(true);
	    	} else {
	    		vhtMultisenseSensorEditor.setVisible(false);
	    	}
	    	
	    	if(input instanceof KinectSensor) {
	    		KinectSensor kinectSensor = (KinectSensor)input;
	    		kinectSensorEditor.setKinectSensor(kinectSensor);
	    		kinectSensorEditor.setVisible(true);
	    	} else {
	    		kinectSensorEditor.setVisible(false);
	    	}
    	}
    }
    
    public void setTitle() {
    	caption.setText("Sensor");
    }
    
    /**
     * Adds a callback that invokes logic when a sensor's name is changed
     * 
     * @param callback a callback that invokes logic when a sensor's name is changed
     * @return whether or not the callback was added
     */
    public boolean addSensorNameChangedCallback(SensorNameChangedCallback callback){
    	return nameChangedCallbacks.add(callback);  	
    }
    
    /**
     * Removes a callback that invokes logic when a sensor's name is changed
     * 
     * @param callback a callback that invokes logic when a sensor's name is changed
     * @return whether or not the callback was removed
     */
    public boolean removeSensorNameChangedCallback(SensorNameChangedCallback callback){
    	return nameChangedCallbacks.remove(callback);  	
    }
    
    /**
     * Invokes all callbacks waiting for a sensor's name to change
     * 
     * @param sensor the sensor whose name changed
     */
    private void invokeNameChangedCallbacks(Sensor sensor){
    	
    	for(SensorNameChangedCallback callback : nameChangedCallbacks){
    		callback.onSensorNameChanged(sensor);
    	}
    }
    
    public SensorTypeValueListBox getSensorTypeListBox() {
    	return sensorTypeListBox;
    }
}
