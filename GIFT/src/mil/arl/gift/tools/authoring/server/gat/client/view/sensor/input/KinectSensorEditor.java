/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.BooleanEnum;
import generated.sensor.KinectColorResolutionEnum;
import generated.sensor.KinectDepthResolutionEnum;
import generated.sensor.KinectSensor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.BooleanEnumListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.KinectColorResolutionValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.KinectDepthResolutionValueListBox;

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
 * The Class KinectSensorEditor.
 */
public class KinectSensorEditor extends Composite {
	
	/** The ui binder. */
    interface KinectSensorEditorUiBinder extends UiBinder<Widget, KinectSensorEditor> {} 
	private static KinectSensorEditorUiBinder uiBinder = GWT.create(KinectSensorEditorUiBinder.class);
	
	@UiField
	protected DoubleBox trackingIntervalDoubleBox;
	
	@UiField
	protected DoubleBox colorSampleIntervalDoubleBox;
	
	@UiField
	protected KinectColorResolutionValueListBox colorFrameFormatListBox;
	
	@UiField
	protected DoubleBox depthSampleIntervalDoubleBox;
	
	@UiField
	protected KinectDepthResolutionValueListBox depthFrameFormatListBox;
	
	@UiField
	protected BooleanEnumListBox nearModeListBox;
	
	private KinectSensor kinectSensor;
	
    public KinectSensorEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Double> trackingHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double interval = changeEvent.getValue();
				if(interval == null) {
					//Allow the user to delete the text since it is an optional field.
					if(trackingIntervalDoubleBox.getText().equals("")) {
						kinectSensor.setSkeletonAndFaceTrackingSampleInterval(null);
					} else {
						populateTrackingIntervalDoubleBox();
					}
				} else {
					kinectSensor.setSkeletonAndFaceTrackingSampleInterval(BigDecimal.valueOf(interval));
				}
			}
		};
		trackingIntervalDoubleBox.addValueChangeHandler(trackingHandler);
        
        ValueChangeHandler<Double> colorSampleHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double interval = changeEvent.getValue();
				if(interval == null) {
					//Allow the user to delete the text since it is an optional field.
					if(colorSampleIntervalDoubleBox.getText().equals("")) {
						kinectSensor.setColorSampleInterval(null);
					} else {
						populateColorSampleDoubleBox();
					}
				} else {
					kinectSensor.setColorSampleInterval(BigDecimal.valueOf(interval));
				}
			}
		};
		colorSampleIntervalDoubleBox.addValueChangeHandler(colorSampleHandler);

		ValueChangeHandler<KinectColorResolutionEnum> colorFrameFormatHandler = new ValueChangeHandler<KinectColorResolutionEnum>(){
			@Override
			public void onValueChange(ValueChangeEvent<KinectColorResolutionEnum> changeEvent) {
				KinectColorResolutionEnum kinectColorResolutionEnum = changeEvent.getValue();
				kinectSensor.setColorFrameFormat(kinectColorResolutionEnum);
			}
		};
		colorFrameFormatListBox.addValueChangeHandler(colorFrameFormatHandler);
        
        ValueChangeHandler<Double> depthSampleHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double interval = changeEvent.getValue();
				if(interval == null) {
					//Allow the user to delete the text since it is an optional field.
					if(depthSampleIntervalDoubleBox.getText().equals("")) {
						kinectSensor.setDepthSampleInterval(null);
					} else {
						populateDepthSampleDoubleBox();
					}
				} else {
					kinectSensor.setDepthSampleInterval(BigDecimal.valueOf(interval));
				}
			}
		};
		depthSampleIntervalDoubleBox.addValueChangeHandler(depthSampleHandler);

		ValueChangeHandler<KinectDepthResolutionEnum> depthFrameFormatHandler = new ValueChangeHandler<KinectDepthResolutionEnum>(){
			@Override
			public void onValueChange(ValueChangeEvent<KinectDepthResolutionEnum> changeEvent) {
				KinectDepthResolutionEnum depthEnum = changeEvent.getValue();
				kinectSensor.setDepthFrameFormat(depthEnum);
			}
		};
		depthFrameFormatListBox.addValueChangeHandler(depthFrameFormatHandler);
		
		ValueChangeHandler<BooleanEnum> nearModeHandler = new ValueChangeHandler<BooleanEnum>(){
			@Override
			public void onValueChange(ValueChangeEvent<BooleanEnum> changeEvent) {
				BooleanEnum nearMode = changeEvent.getValue();
				kinectSensor.setNearMode(nearMode);
			}
		};
		nearModeListBox.addValueChangeHandler(nearModeHandler);
    }
    
    public void setKinectSensor(KinectSensor kinectSensor) {
    	this.kinectSensor = kinectSensor;
    	populateTrackingIntervalDoubleBox();
    	populateColorSampleDoubleBox();
    	populateColorFrameFormatListBox();
    	populateDepthSampleDoubleBox();
    	populateDepthFrameFormatListBox();
    	populateNearModeListBox();
    }
    
    private void populateTrackingIntervalDoubleBox() {
    	BigDecimal interval = kinectSensor.getSkeletonAndFaceTrackingSampleInterval();
    	if(interval == null) {
    		trackingIntervalDoubleBox.setText("");
    	} else {
    		trackingIntervalDoubleBox.setValue(interval.doubleValue());
    	}
    }
    
    private void populateColorSampleDoubleBox() {
    	BigDecimal interval = kinectSensor.getColorSampleInterval();
    	if(interval == null) {
    		colorSampleIntervalDoubleBox.setText("");
    	} else {
    		colorSampleIntervalDoubleBox.setValue(interval.doubleValue());
    	}
    }
    
    private void populateDepthSampleDoubleBox() {
    	BigDecimal interval = kinectSensor.getDepthSampleInterval();
    	if(interval == null) {
    		depthSampleIntervalDoubleBox.setText("");
    	} else {
    		depthSampleIntervalDoubleBox.setValue(interval.doubleValue());
    	}
    }
    
    private void populateColorFrameFormatListBox() {
    	KinectColorResolutionEnum kinectColorResolutionEnum = kinectSensor.getColorFrameFormat();
    	colorFrameFormatListBox.setValue(kinectColorResolutionEnum);
    }
    
    private void populateDepthFrameFormatListBox() {
    	KinectDepthResolutionEnum kinectDepthResolutionEnum = kinectSensor.getDepthFrameFormat();
    	depthFrameFormatListBox.setValue(kinectDepthResolutionEnum);
    }
    
    private void populateNearModeListBox() {
    	BooleanEnum nearMode = kinectSensor.getNearMode();
    	nearModeListBox.setValue(nearMode);
    }
    
}
