/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.ImageCompressionFormat;
import generated.sensor.KinectSensorWriter;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.ImageCompressionFormatValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class KinectSensorWriterEditor.
 */
public class KinectSensorWriterEditor extends Composite {
	
	/** The ui binder. */
    interface KinectSensorWriterEditorUiBinder extends UiBinder<Widget, KinectSensorWriterEditor> {} 
	private static KinectSensorWriterEditorUiBinder uiBinder = GWT.create(KinectSensorWriterEditorUiBinder.class);
	
	@UiField
	protected TextBox filePrefixTextBox;
	
	@UiField
	protected ImageCompressionFormatValueListBox colorCompressionListBox;
	
	@UiField
	protected ImageCompressionFormatValueListBox depthCompressionListBox;
	
	private KinectSensorWriter kinectSensorWriter;
	
    public KinectSensorWriterEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
		
		
		ValueChangeHandler<String> prefixHandler = new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> changeEvent) {
				String prefix = changeEvent.getValue();
				if(prefix.equals("")) {
					kinectSensorWriter.setFilePrefix(null);
				} else {
					kinectSensorWriter.setFilePrefix(prefix);
				}
			}
		};
		filePrefixTextBox.addValueChangeHandler(prefixHandler);
		
		ValueChangeHandler<ImageCompressionFormat> colorHandler = new ValueChangeHandler<ImageCompressionFormat>(){
			@Override
			public void onValueChange(ValueChangeEvent<ImageCompressionFormat> changeEvent) {
				ImageCompressionFormat colorEnum = changeEvent.getValue();
				kinectSensorWriter.setColorCompression(colorEnum);

			}
		};
		colorCompressionListBox.addValueChangeHandler(colorHandler);
		
		ValueChangeHandler<ImageCompressionFormat> depthHandler = new ValueChangeHandler<ImageCompressionFormat>(){
			@Override
			public void onValueChange(ValueChangeEvent<ImageCompressionFormat> changeEvent) {
				ImageCompressionFormat depthEnum = changeEvent.getValue();
				kinectSensorWriter.setDepthCompression(depthEnum);
			}
		};
		depthCompressionListBox.addValueChangeHandler(depthHandler);
    }
    
    public void setKinectSensorWriter(KinectSensorWriter kinectSensorWriter) {
    	this.kinectSensorWriter = kinectSensorWriter;
    	filePrefixTextBox.setText(kinectSensorWriter.getFilePrefix());
    	
    	ImageCompressionFormat colorEnum = kinectSensorWriter.getColorCompression();
    	colorCompressionListBox.setValue(colorEnum);

    	
    	ImageCompressionFormat depthEnum = kinectSensorWriter.getDepthCompression();
    	depthCompressionListBox.setValue(depthEnum);
    }
}
