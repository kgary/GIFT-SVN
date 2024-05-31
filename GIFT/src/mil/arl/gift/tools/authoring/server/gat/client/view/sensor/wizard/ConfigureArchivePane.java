/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard;

import generated.sensor.ImageCompressionFormat;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.ImageCompressionFormatValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class ConfigureArchivePane.
 */
public class ConfigureArchivePane extends Composite {
	
	/** The ui binder. */
    interface ConfigureArchivePaneUiBinder extends UiBinder<Widget, ConfigureArchivePane> {} 
	private static ConfigureArchivePaneUiBinder uiBinder = GWT.create(ConfigureArchivePaneUiBinder.class);
	
	@UiField
	protected TextBox outputDirectoryTextBox;
	
	@UiField
	protected TextBox filePrefixTextBox;
	
	/**
	 * These widgets are used for GenericSensorDelimitedWriter
	 */
	@UiField protected FlowPanel genericSensorDelimitedWriterPanel;
	@UiField protected TextBox delimiterTextBox;
	
	/**
	 * These widgets are used for KinectSensorWriter
	 */
	@UiField protected FlowPanel kinectSensorWriterPanel;
	@UiField protected ImageCompressionFormatValueListBox colorCompressionListBox;
	@UiField protected ImageCompressionFormatValueListBox depthCompressionListBox;
	
    public ConfigureArchivePane() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        reset();
    }
    
    public void reset() {
    	setGenericSensorDelimitedWriterVisibility(true);
    	outputDirectoryTextBox.setText(SensorsConfigurationFactory.DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_DIRECTORY);
    	filePrefixTextBox.setText(SensorsConfigurationFactory.DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_FILE_PREFIX);
    	delimiterTextBox.setText(SensorsConfigurationFactory.DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_REPLACEMENT_CHAR);
    	
    	setKinectSensorWriterVisibility(false);
    	colorCompressionListBox.setValue(SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_WRITER_COLOR_COMPRESSION);
    	depthCompressionListBox.setValue(SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_WRITER_DEPTH_COMPRESSION);
    }
    
    public void setSensorType(SensorTypeEnum sensorType) {
    	if(sensorType.equals(SensorTypeEnum.KINECT)) {
    		setKinectSensorWriterVisibility(true);
    		setGenericSensorDelimitedWriterVisibility(false);
    	} else {
    		setKinectSensorWriterVisibility(false);
    		setGenericSensorDelimitedWriterVisibility(true);
    	}
    	updateFilePrefix(sensorType);
    }
    
    private void setGenericSensorDelimitedWriterVisibility(boolean visible) {
    	genericSensorDelimitedWriterPanel.setVisible(visible);
    }
    
    private void setKinectSensorWriterVisibility(boolean visible) {
    	kinectSensorWriterPanel.setVisible(visible);
    }
    
    public String getOutputDirectory() {
    	String outputDirectory = outputDirectoryTextBox.getText();
    	if(outputDirectory.equals("")) {
    		return null;
    	} else {
    		return outputDirectory;
    	}
    }
    
    public String getFilePrefix() {
    	String filePrefix = filePrefixTextBox.getText();
    	if(filePrefix.equals("")) {
    		return null;
    	} else {
    		return filePrefix;
    	}
    }
    
    public String getDelimiter() {
    	String delimiter = delimiterTextBox.getText();
    	if(delimiter.equals("")) {
    		return null;
    	} else {
    		return delimiter;
    	}
    }
    
    public ImageCompressionFormat getColorCompression() {
    	return colorCompressionListBox.getValue();
    }
    
    public ImageCompressionFormat getDepthCompression() {
    	return depthCompressionListBox.getValue();
    }
    
    public void updateFilePrefix(SensorTypeEnum sensorType) {
    	String filePrefix = sensorType.getDisplayName().replaceAll("[^a-zA-Z]", "") + "Sensor";
    	filePrefixTextBox.setText(filePrefix);
    }
}
