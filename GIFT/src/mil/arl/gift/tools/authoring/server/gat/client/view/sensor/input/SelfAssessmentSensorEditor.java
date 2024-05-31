/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.SelfAssessmentSensor;

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
 * The Class SelfAssessmentSensorEditor.
 */
public class SelfAssessmentSensorEditor extends Composite {
	
	/** The ui binder. */
    interface SelfAssessmentSensorEditorUiBinder extends UiBinder<Widget, SelfAssessmentSensorEditor> {} 
	private static SelfAssessmentSensorEditorUiBinder uiBinder = GWT.create(SelfAssessmentSensorEditorUiBinder.class);
	
	@UiField
	protected DoubleBox rateChangeAmountDoubleBox;
	
	private SelfAssessmentSensor selfAssessmentSensor;
	
    public SelfAssessmentSensorEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Double> intervalHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double interval = changeEvent.getValue();
				if(interval == null) {
					populateRateChangeAmountDoubleBox();
				} else {
					selfAssessmentSensor.setRateChangeAmount(BigDecimal.valueOf(interval));
				}
			}
		};
		rateChangeAmountDoubleBox.addValueChangeHandler(intervalHandler);
    }
    
    public void setSelfAssessmentSensor(SelfAssessmentSensor selfAssessmentSensor) {
    	this.selfAssessmentSensor = selfAssessmentSensor;
    	populateRateChangeAmountDoubleBox();
    }
    
    private void populateRateChangeAmountDoubleBox() {
    	BigDecimal rateChangeAmount = selfAssessmentSensor.getRateChangeAmount();
    	if(rateChangeAmount == null) {
    		rateChangeAmountDoubleBox.setText("");
    	} else {
    		rateChangeAmountDoubleBox.setValue(rateChangeAmount.doubleValue());
    	}
    }
}
