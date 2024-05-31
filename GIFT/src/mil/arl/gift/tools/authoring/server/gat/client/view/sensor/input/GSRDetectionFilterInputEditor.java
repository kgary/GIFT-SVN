/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.GSRDetectionFilterInput;

import java.math.BigInteger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class GSRDetectionFilterInput.
 */
public class GSRDetectionFilterInputEditor extends Composite {
	
	/** The ui binder. */
    interface GSRDetectionFilterInputEditorUiBinder extends UiBinder<Widget, GSRDetectionFilterInputEditor> {} 
	private static GSRDetectionFilterInputEditorUiBinder uiBinder = GWT.create(GSRDetectionFilterInputEditorUiBinder.class);
	
	@UiField
	protected IntegerBox sampleRateIntegerBox;
	
	@UiField
	protected DoubleBox windowSizeDoubleBox;
	
	private GSRDetectionFilterInput gsrDetectionFilterInput;
	
    public GSRDetectionFilterInputEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Integer> sampleRateHandler = new ValueChangeHandler<Integer>() {
			@Override
			public void onValueChange(ValueChangeEvent<Integer> changeEvent) {
				Integer sampleRate = changeEvent.getValue();
				if(sampleRate != null && sampleRate > 0) {
					gsrDetectionFilterInput.setSamplingRateHz(BigInteger.valueOf(sampleRate));
				}
				populateSampleRateIntegerBox();
			}
		};
		sampleRateIntegerBox.addValueChangeHandler(sampleRateHandler);
        
        ValueChangeHandler<Double> windowSizeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double windowSize = changeEvent.getValue();
				if(windowSize == null || windowSize <= 0) {
					populateWindowSizeDoubleBox();
				} else { 
					gsrDetectionFilterInput.setWindowSize(windowSize.floatValue());
				}
			}
		};
		windowSizeDoubleBox.addValueChangeHandler(windowSizeHandler);
    }
    
    public void setGSRDetectionFilterInput(GSRDetectionFilterInput gsrDetectionFilterInput) {
    	this.gsrDetectionFilterInput = gsrDetectionFilterInput;
    	populateSampleRateIntegerBox();
    	populateWindowSizeDoubleBox();
    }
    
    private void populateSampleRateIntegerBox() {
    	BigInteger sampleRate = gsrDetectionFilterInput.getSamplingRateHz();
    	sampleRateIntegerBox.setValue(sampleRate.intValue());
    }
    
    private void populateWindowSizeDoubleBox() {
    	double windowSize = gsrDetectionFilterInput.getWindowSize();
    	windowSizeDoubleBox.setValue(windowSize);
    }
}
