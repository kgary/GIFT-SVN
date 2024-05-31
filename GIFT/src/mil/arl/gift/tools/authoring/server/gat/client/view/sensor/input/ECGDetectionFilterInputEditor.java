/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.ECGDetectionFilterInput;

import java.math.BigInteger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class ECGDetectionFilterInputEditor.
 */
public class ECGDetectionFilterInputEditor extends Composite {
	
	/** The ui binder. */
    interface ECGDetectionFilterInputEditorUiBinder extends UiBinder<Widget, ECGDetectionFilterInputEditor> {} 
	private static ECGDetectionFilterInputEditorUiBinder uiBinder = GWT.create(ECGDetectionFilterInputEditorUiBinder.class);
	
	@UiField
	protected IntegerBox sampleRateIntegerBox;
	
	@UiField
	protected IntegerBox windowSizeIntegerBox;
	
	private ECGDetectionFilterInput ecgDetectionFilterInput;
	
    public ECGDetectionFilterInputEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Integer> sampleRateHandler = new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> changeEvent) {
				Integer sampleRate = changeEvent.getValue();
				if(sampleRate != null && sampleRate > 0) {
					ecgDetectionFilterInput.setSamplingRateHz(BigInteger.valueOf(sampleRate));
				}
				populateSampleRateIntegerBox();
			}
		};
		sampleRateIntegerBox.addValueChangeHandler(sampleRateHandler);
        
        ValueChangeHandler<Integer> windowSizeHandler = new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> changeEvent) {
				Integer windowSize = changeEvent.getValue();
				if(windowSize != null && windowSize > 0) {
					ecgDetectionFilterInput.setWindowSize(BigInteger.valueOf(windowSize));
				}
				populateWindowSizeIntegerBox();
			}
		};
		windowSizeIntegerBox.addValueChangeHandler(windowSizeHandler);
    }
    
    public void setECGDetectionFilterInput(ECGDetectionFilterInput ecgDetectionFilterInput) {
    	this.ecgDetectionFilterInput = ecgDetectionFilterInput;
    	populateSampleRateIntegerBox();
    	populateWindowSizeIntegerBox();
    }
    
    private void populateSampleRateIntegerBox() {
    	BigInteger sampleRate = ecgDetectionFilterInput.getSamplingRateHz();
    	sampleRateIntegerBox.setValue(sampleRate.intValue());
    }
    
    private void populateWindowSizeIntegerBox() {
    	BigInteger windowSize = ecgDetectionFilterInput.getWindowSize();
    	windowSizeIntegerBox.setValue(windowSize.intValue());
    }
}
