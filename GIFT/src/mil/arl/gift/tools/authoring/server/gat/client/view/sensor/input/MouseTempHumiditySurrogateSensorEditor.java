/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.MouseTempHumiditySurrogateSensor;

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
 * The Class MouseTempHumiditySurrogateSensorEditor.
 */
public class MouseTempHumiditySurrogateSensorEditor extends Composite {
	
	/** The ui binder. */
    interface MouseTempHumiditySurrogateSensorEditorUiBinder extends UiBinder<Widget, MouseTempHumiditySurrogateSensorEditor> {} 
	private static MouseTempHumiditySurrogateSensorEditorUiBinder uiBinder = GWT.create(MouseTempHumiditySurrogateSensorEditorUiBinder.class);
	
	@UiField
	protected DoubleBox temperatureRateChangeAmountDoubleBox;
	
	@UiField
	protected DoubleBox humidityRateChangeAmountDoubleBox;
	
	private MouseTempHumiditySurrogateSensor mouseTempHumiditySurrogateSensor;
	
    public MouseTempHumiditySurrogateSensorEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Double> temperatureHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double temperatureRateChangeAmount = changeEvent.getValue();
				if(temperatureRateChangeAmount == null) {
					populateTemperatureRateChangeAmountDoubleBox();
				} else {
					mouseTempHumiditySurrogateSensor.setTemperatureRateChangeAmount(BigDecimal.valueOf(temperatureRateChangeAmount));
				}
			}
		};
		temperatureRateChangeAmountDoubleBox.addValueChangeHandler(temperatureHandler);
        
        ValueChangeHandler<Double> humidityHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double humidityRateChangeAmount = changeEvent.getValue();
				if(humidityRateChangeAmount == null) {
					populateHumidityRateChangeAmountDoubleBox();
				} else {
					mouseTempHumiditySurrogateSensor.setHumidityRateChangeAmount(BigDecimal.valueOf(humidityRateChangeAmount));
				}
			}
		};
		humidityRateChangeAmountDoubleBox.addValueChangeHandler(humidityHandler);
    }
    
    public void setMouseTempHumiditySurrogateSensor(MouseTempHumiditySurrogateSensor mouseTempHumiditySurrogateSensor) {
    	this.mouseTempHumiditySurrogateSensor = mouseTempHumiditySurrogateSensor;
    	populateTemperatureRateChangeAmountDoubleBox();
    	populateHumidityRateChangeAmountDoubleBox();
    }
    
    private void populateTemperatureRateChangeAmountDoubleBox() {
    	BigDecimal temperatureRateChangeAmount = mouseTempHumiditySurrogateSensor.getTemperatureRateChangeAmount();
    	if(temperatureRateChangeAmount == null) {
    		temperatureRateChangeAmountDoubleBox.setText("");
    	} else {
    		temperatureRateChangeAmountDoubleBox.setValue(temperatureRateChangeAmount.doubleValue());
    	}
    }
    
    private void populateHumidityRateChangeAmountDoubleBox() {
    	BigDecimal humidityRateChangeAmount = mouseTempHumiditySurrogateSensor.getHumidityRateChangeAmount();
    	if(humidityRateChangeAmount == null) {
    		humidityRateChangeAmountDoubleBox.setText("");
    	} else {
    		humidityRateChangeAmountDoubleBox.setValue(humidityRateChangeAmount.doubleValue());
    	}
    }
}
