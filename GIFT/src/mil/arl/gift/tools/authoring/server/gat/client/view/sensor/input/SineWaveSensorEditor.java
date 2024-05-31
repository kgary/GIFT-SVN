/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.SineWaveSensor;

import java.math.BigDecimal;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class SineWaveSensorEditor.
 */
public class SineWaveSensorEditor extends Composite {
	
	/** The ui binder. */
    interface SineWaveSensorEditorUiBinder extends UiBinder<Widget, SineWaveSensorEditor> {} 
	private static SineWaveSensorEditorUiBinder uiBinder = GWT.create(SineWaveSensorEditorUiBinder.class);
	
	@UiField
	protected DoubleBox amplitudeDoubleBox;
	
	@UiField
	protected DoubleBox periodDoubleBox;
	
	private SineWaveSensor sineWaveSensor;
	
    public SineWaveSensorEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Double> amplitudeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double amplitude = changeEvent.getValue();
				if(amplitude == null) {
					populateAmplitudeDoubleBox();
				} else {
					sineWaveSensor.setAmplitude(BigDecimal.valueOf(amplitude));
				}
			}
		};
		amplitudeDoubleBox.addValueChangeHandler(amplitudeHandler);
        
        ValueChangeHandler<Double> periodHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double period = changeEvent.getValue();
				if(period == null) {
					populatePeriodDoubleBox();
				} else {
					sineWaveSensor.setPeriod(BigDecimal.valueOf(period));
				}
			}
		};
		periodDoubleBox.addValueChangeHandler(periodHandler);
    }
    
    public void setSineWaveSensor(SineWaveSensor sineWaveSensor) {
    	this.sineWaveSensor = sineWaveSensor;
    	populateAmplitudeDoubleBox();
    	populatePeriodDoubleBox();
    }
    
    private void populateAmplitudeDoubleBox() {
    	BigDecimal amplitude = sineWaveSensor.getAmplitude();
    	if(amplitude == null) {
    		amplitudeDoubleBox.setText("");
    	} else {
    		amplitudeDoubleBox.setValue(amplitude.doubleValue());
    	}
    }
    
    private void populatePeriodDoubleBox() {
    	BigDecimal period = sineWaveSensor.getPeriod();
    	if(period == null) {
    		periodDoubleBox.setText("");
    	} else {
    		periodDoubleBox.setValue(period.doubleValue());
    	}
    }
}
