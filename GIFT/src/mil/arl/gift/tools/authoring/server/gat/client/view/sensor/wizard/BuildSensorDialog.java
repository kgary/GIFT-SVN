/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard;

import generated.sensor.BooleanEnum;
import generated.sensor.Filter;
import generated.sensor.FilterInput;
import generated.sensor.GenericSensorDelimitedWriter;
import generated.sensor.ImageCompressionFormat;
import generated.sensor.KinectColorResolutionEnum;
import generated.sensor.KinectDepthResolutionEnum;
import generated.sensor.KinectSensor;
import generated.sensor.KinectSensorWriter;
import generated.sensor.MouseTempHumiditySurrogateSensor;
import generated.sensor.SelfAssessmentSensor;
import generated.sensor.Sensor;
import generated.sensor.SensorInput;
import generated.sensor.SineWaveSensor;
import generated.sensor.VhtMultisenseSensor;
import generated.sensor.Writer;
import generated.sensor.WriterInput;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.FilterTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.WriterTypeEnum;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class BuildSensorDialog.
 */
public class BuildSensorDialog extends DialogBox {
	
	/** The ui binder. */
    interface BuildSensorDialogUiBinder extends UiBinder<Widget, BuildSensorDialog> {} 
	private static BuildSensorDialogUiBinder uiBinder = GWT.create(BuildSensorDialogUiBinder.class);
	
	@UiField
	protected SelectSensorTypePane selectSensorTypePane;
	
	@UiField
	protected ConfigureSensorPane configureSensorPane;
	
	@UiField
	protected BroadcastRawSensorDataPane broadcastRawSensorDataPane;
	
	@UiField
	protected ArchiveRawSensorDataPane archiveRawSensorDataPane;
	
	@UiField
	protected ConfigureArchivePane configureSensorArchivePane;
	
	@UiField
	protected FilterPane filterPane;
	
	@UiField
	protected BroadcastFilteredDataPane broadcastFilteredDataPane;
	
	@UiField
	protected ArchiveFilteredDataPane archiveFilteredDataPane;
	
	@UiField
	protected ConfigureArchivePane configureFilteredArchivePane;
	
	@UiField
	protected Label finishedPane;
	
	@UiField
	protected Button previousButton;
	
	@UiField
	protected Button nextButton;
	
	@UiField
	protected Button finishButton;
	
	@UiField
	protected Button cancelButton;

	@UiField
	protected Label stepLabel;
	
	int currentStep = 1;
	int totalSteps = 6;
	
    public BuildSensorDialog(final ISensorBuiltHandler callback) {
    	setWidget(uiBinder.createAndBindUi(this));
    	setText("Sensor Configuration Wizard");
    	setGlassEnabled(true);
    	countSteps();
    	
    	ClickHandler previousHandler = new ClickHandler() {
    		@Override
    		public void onClick(ClickEvent arg0) {
    			currentStep--;
    			back();
    		}
    	};
    	previousButton.addClickHandler(previousHandler);
    	
    	ClickHandler nextHandler = new ClickHandler() {
    		@Override
    		public void onClick(ClickEvent arg0) {
    			currentStep++;
    			next();
    		}
    	};
    	nextButton.addClickHandler(nextHandler);
    	
    	ClickHandler finishHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				Writer writerForSensor = constructWriterForSensor();
				Writer writerForFilter = constructWriterForFilter();
				
				ArrayList<Writer> writers = new ArrayList<Writer>();
				if(writerForSensor != null) {
					writers.add(writerForSensor);
				}
				if(writerForFilter != null) {
					writers.add(writerForFilter);
				}
				
				Filter filter = constructFilter(writerForFilter);
				
				Sensor sensor = constructSensor(filter, writerForSensor);
				
				callback.onSensorBuilt(sensor, filter, writers);
				
				// reset step counts in case user creates a new sensor again
				currentStep = 1;
        		totalSteps = 6;
				countSteps();
				
				hide();
			}
    	};
    	
    	finishButton.addClickHandler(finishHandler);
        
        ClickHandler cancelHandler = new ClickHandler(){
        	@Override
			public void onClick(ClickEvent arg0) {
        		hide();
        		
        		// reset step counts in case user creates a new sensor again
        		currentStep = 1;
        		totalSteps = 6;
        		countSteps();
			}
		};
        cancelButton.addClickHandler(cancelHandler);
        
        ValueChangeHandler<Boolean> inputHandler = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue()) {
					// Selecting default config data reduces steps
					totalSteps--;
				} else {
					totalSteps++;
				}
				countSteps();
			}
        };
        
        selectSensorTypePane.useDefaultsListBox.addValueChangeHandler(inputHandler);
        
        archiveRawSensorDataPane.useDefaultArchiveDataListBox.addValueChangeHandler(inputHandler);
        
        filterPane.filterRawSensorDataListBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				// Choosing to send data increases steps
				if(event.getValue()) {
					totalSteps++;
				} else {
					totalSteps--;
				}
				countSteps();
			}
        });
        
        archiveFilteredDataPane.useDefaultArchiveDataListBox.addValueChangeHandler(inputHandler);
    }

	@Override
	public void show() {
		super.show();
		refreshPanes();
		refreshButtons();
		refreshTitle();
	}
	
	private void next() {
		if(selectSensorTypePane.isVisible()) {			
			selectSensorTypePane.setVisible(false);
			boolean useDefaults = selectSensorTypePane.isUsingDefaults();
			if(useDefaults) {
				broadcastRawSensorDataPane.setVisible(true);
			} else {				
				SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
				configureSensorPane.setSensorType(sensorType);
				configureSensorPane.setVisible(true);
			}
		} else if(configureSensorPane.isVisible()) {
			configureSensorPane.setVisible(false);
			broadcastRawSensorDataPane.setVisible(true);
		} else if(broadcastRawSensorDataPane.isVisible()) {
			broadcastRawSensorDataPane.setVisible(false);
			archiveRawSensorDataPane.setVisible(true);
		} else if(archiveRawSensorDataPane.isVisible()) {
			archiveRawSensorDataPane.setVisible(false);
			
			if(archiveRawSensorDataPane.isArchiveRawSensorData() &&
					!archiveRawSensorDataPane.isUseDefaultArchiveData()) {
				SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
				configureSensorArchivePane.setSensorType(sensorType);
				configureSensorArchivePane.setVisible(true);
			} else {
				filterPane.setVisible(true);
			}
		} else if(configureSensorArchivePane.isVisible()) {
			configureSensorArchivePane.setVisible(false);
			filterPane.setVisible(true);
		} else if(filterPane.isVisible()) {
			filterPane.setVisible(false);
			
			if(filterPane.isFilterRawSensorData()) {
				broadcastFilteredDataPane.setVisible(true);
			} else {
				finishedPane.setVisible(true);
			}
		} else if(broadcastFilteredDataPane.isVisible()) {
			broadcastFilteredDataPane.setVisible(false);
			archiveFilteredDataPane.setVisible(true);
		} else if(archiveFilteredDataPane.isVisible()) {
			archiveFilteredDataPane.setVisible(false);
			
			if(archiveFilteredDataPane.isArchiveFilteredData() &&
					!archiveFilteredDataPane.isUseDefaultArchiveData()) {
				SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
				configureFilteredArchivePane.setSensorType(sensorType);
				configureFilteredArchivePane.setVisible(true);
			} else {
				finishedPane.setVisible(true);
			}
		} else if(configureFilteredArchivePane.isVisible()) {
			configureFilteredArchivePane.setVisible(false);
			finishedPane.setVisible(true);
		}
		
		countSteps();
		
		//re-center dialog, since its size may have changed
		center();
		
		refreshTitle();
		refreshButtons();
	}
	
	private void back() {
		if(configureSensorPane.isVisible()) {
			configureSensorPane.setVisible(false);
			selectSensorTypePane.setVisible(true);
		} else if(broadcastRawSensorDataPane.isVisible()) {
			broadcastRawSensorDataPane.setVisible(false);
			if(selectSensorTypePane.isUsingDefaults()) {
				selectSensorTypePane.setVisible(true);
			} else {
				configureSensorPane.setVisible(true);
			}
		} else if(archiveRawSensorDataPane.isVisible()) {
			archiveRawSensorDataPane.setVisible(false);
			broadcastRawSensorDataPane.setVisible(true);
		} else if(configureSensorArchivePane.isVisible()) {
			configureSensorArchivePane.setVisible(false);
			archiveRawSensorDataPane.setVisible(true);
		} else if(filterPane.isVisible()) {
			filterPane.setVisible(false);
			if(archiveRawSensorDataPane.isArchiveRawSensorData() &&
					!archiveRawSensorDataPane.isUseDefaultArchiveData()) {
				configureSensorArchivePane.setVisible(true);
			} else {
				archiveRawSensorDataPane.setVisible(true);
			}
		} else if(broadcastFilteredDataPane.isVisible()) {
			broadcastFilteredDataPane.setVisible(false);
			filterPane.setVisible(true);
		} else if(archiveFilteredDataPane.isVisible()) {
			archiveFilteredDataPane.setVisible(false);
			broadcastFilteredDataPane.setVisible(true);
		} else if(configureFilteredArchivePane.isVisible()) {
			configureFilteredArchivePane.setVisible(false);
			archiveFilteredDataPane.setVisible(true);
		} else if(finishedPane.isVisible()) {
			finishedPane.setVisible(false);
			
			if(!filterPane.isFilterRawSensorData()) {
				filterPane.setVisible(true);
			} else if(archiveFilteredDataPane.isArchiveFilteredData() &&
					!archiveFilteredDataPane.isUseDefaultArchiveData()) {
				configureFilteredArchivePane.setVisible(true);
			} else {
				archiveFilteredDataPane.setVisible(true);
			}
		}
		
		countSteps();
		
		//re-center dialog, since its size may have changed
		center();
		
		refreshTitle();
		refreshButtons();
	}
	
	private void refreshTitle() {
		if(selectSensorTypePane.isVisible()) {
			setText("Sensor Configuration Wizard: Select Sensor Type");
		} else if(configureSensorPane.isVisible()) {
			setText("Sensor Configuration Wizard: Configure Sensor");
		} else if(broadcastRawSensorDataPane.isVisible()) {
			setText("Sensor Configuration Wizard: Broadcast Raw Sensor Data");
		} else if(archiveRawSensorDataPane.isVisible()) {
			setText("Sensor Configuration Wizard: Archive Raw Sensor Data");
		} else if(configureSensorArchivePane.isVisible()) {
			setText("Sensor Configuration Wizard: Configure Raw Sensor Data Archiver");
		} else if(filterPane.isVisible()) {
			setText("Sensor Configuration Wizard: Filter Raw Sensor Data");
		} else if(broadcastFilteredDataPane.isVisible()) {
			setText("Sensor Configuration Wizard: Broadcast Filtered Data");
		} else if(archiveFilteredDataPane.isVisible()) {
			setText("Sensor Configuration Wizard: Archive Filtered Data");
		} else if(configureFilteredArchivePane.isVisible()) {
			setText("Sensor Configuration Wizard: Configure Filtered Data Archiver");
		} else if(finishedPane.isVisible()) {
			setText("Sensor Configuration Wizard: Complete!");
		}
	}
	
	private void refreshButtons() {
		boolean onFirstPane = selectSensorTypePane.isVisible();
		boolean onLastPane = finishedPane.isVisible();
		
		previousButton.setEnabled(!onFirstPane);
		nextButton.setEnabled(!onLastPane);
		finishButton.setEnabled(onLastPane);
	}
	
	private void refreshPanes() {
		selectSensorTypePane.reset();
		selectSensorTypePane.setVisible(true);
		
		configureSensorPane.reset();
		configureSensorPane.setVisible(false);
		
		archiveRawSensorDataPane.reset();
		archiveRawSensorDataPane.setVisible(false);

		configureSensorArchivePane.reset();
		configureSensorArchivePane.setVisible(false);
		
		filterPane.reset();
		filterPane.setVisible(false);
		
		broadcastFilteredDataPane.reset();
		broadcastFilteredDataPane.setVisible(false);
		
		archiveFilteredDataPane.reset();
		archiveFilteredDataPane.setVisible(false);
		
		configureFilteredArchivePane.setVisible(false);
		broadcastRawSensorDataPane.setVisible(false);
		
		finishedPane.setVisible(false);
	}
	
	private Sensor constructSensor(Filter filter, Writer writer) {
		Sensor sensor = new Sensor();
		
		//Generate a unique ID for the sensor.
		BigInteger id = SensorsConfigurationFactory.generateId();
		sensor.setId(id);
		
		//Make the selected SensorType to the SensorImpl.
		SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
		String sensorImpl = SensorsConfigurationMaps.getInstance().getSensorTypeToImplMap().get(sensorType);
		sensor.setSensorImpl(sensorImpl);
		
		if(broadcastRawSensorDataPane.isBroadcastRawSensorData()) {
			sensor.setDistributeExternally(BooleanEnum.TRUE);
		} else {
			sensor.setDistributeExternally(BooleanEnum.FALSE);
		}
		
		//The user wanted to use default data to configure the sensor.
		if(selectSensorTypePane.isUsingDefaults()) {
			Serializable sensorInputType = SensorsConfigurationFactory.createSensorInputType(sensorType);
			
			SensorInput sensorInput = new SensorInput();
			sensorInput.setType(sensorInputType);
			
			sensor.setName(sensorType.getDisplayName() + " Sensor");
			sensor.setInterval(SensorsConfigurationFactory.DEFAULT_SENSOR_INTERVAL);
			sensor.setSensorInput(sensorInput);
		}
		//The user configured the sensor themselves.
		else {
			String name = configureSensorPane.getName();
			sensor.setName(name);
			
			BigDecimal interval = configureSensorPane.getInterval();
			sensor.setInterval(interval);
			
			SensorInput sensorInput = constructSensorInput();
			sensor.setSensorInput(sensorInput);
		}
		
		if(filter != null) {
			sensor.setFilterInstance(filter.getId());
		}
		if(writer != null) {
			sensor.setWriterInstance(writer.getId());
		}
		return sensor;
	}
	
	private SensorInput constructSensorInput() {
		
		SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
		SensorInput sensorInput = null;
		
		if(sensorType == SensorTypeEnum.SELF_ASSESSMENT) {
			sensorInput = constructSelfAssessmentSensorInput();
			
		} else if(sensorType == SensorTypeEnum.MOUSE_TH_SURROGATE) {
			sensorInput = constructMouseTempHumiditySurrogateSensorInput();
			
		} else if(sensorType == SensorTypeEnum.SINE_WAVE) {
			sensorInput = constructSineWaveSensorInput();
			
		} else if(sensorType == SensorTypeEnum.VHT_MULTISENSE) {
			sensorInput = constructVhtMultisenseSensorInput(); 
			
		} else if(sensorType == SensorTypeEnum.KINECT) {
			sensorInput = constructKinectSensorInput();
			
		} else {
			sensorInput = new SensorInput();
			sensorInput.setType(SensorsConfigurationFactory.createSensorInputType(sensorType));
		}
		return sensorInput;
	}
	
	private SensorInput constructSelfAssessmentSensorInput() {
		BigDecimal rateChangeAmount = configureSensorPane.getRateChangeAmount();
		
		SelfAssessmentSensor selfAssessmentSensor = new SelfAssessmentSensor();
		selfAssessmentSensor.setRateChangeAmount(rateChangeAmount);
		
		SensorInput sensorInput = new SensorInput();
		sensorInput.setType(selfAssessmentSensor);
		return sensorInput;
	}
	
	private SensorInput constructMouseTempHumiditySurrogateSensorInput() {
		BigDecimal temperatureRateChange = configureSensorPane.getTemperatureRateChangeAmount();
		BigDecimal humidityRateChange = configureSensorPane.getHumidityRateChangeAmount();
		
		MouseTempHumiditySurrogateSensor mouseTempHumiditySurrogateSensor = new MouseTempHumiditySurrogateSensor();
		mouseTempHumiditySurrogateSensor.setTemperatureRateChangeAmount(temperatureRateChange);
		mouseTempHumiditySurrogateSensor.setHumidityRateChangeAmount(humidityRateChange);
		
		SensorInput sensorInput = new SensorInput();
		sensorInput.setType(mouseTempHumiditySurrogateSensor);
		return sensorInput;
	}
	
	private SensorInput constructSineWaveSensorInput() {
		BigDecimal amplitude = configureSensorPane.getAmplitude();
		BigDecimal period = configureSensorPane.getPeriod();
		
		SineWaveSensor sineWaveSensor = new SineWaveSensor();
		sineWaveSensor.setAmplitude(amplitude);
		sineWaveSensor.setPeriod(period);
		
		SensorInput sensorInput = new SensorInput();
		sensorInput.setType(sineWaveSensor);
		return sensorInput;
	}
	
	private SensorInput constructVhtMultisenseSensorInput() {
		Long datalessWarningDelay = configureSensorPane.getDatalessWarningDelay();
		String topic = configureSensorPane.getActiveMqTopic();
		String url = configureSensorPane.getActiveMqUrl();
		
		VhtMultisenseSensor vhtMultisenseSensor = new VhtMultisenseSensor();
		vhtMultisenseSensor.setDatalessWarningDelay(datalessWarningDelay);
		
		// VhtMultisenseSensor cannot handle empty AMQ URL String 
		vhtMultisenseSensor.setVhtActiveMqTopic((topic.isEmpty()) ? null : topic);
		vhtMultisenseSensor.setVhtActiveMqUrl((url.isEmpty()) ? null : url);
		
		SensorInput sensorInput = new SensorInput();
		sensorInput.setType(vhtMultisenseSensor);
		return sensorInput;
	}
	
	private SensorInput constructKinectSensorInput() {
		BigDecimal trackingInterval = configureSensorPane.getTrackingInterval();
		BigDecimal colorSampleInterval = configureSensorPane.getColorSampleInterval();
		KinectColorResolutionEnum colorFormat = configureSensorPane.getColorFrameFormat();
		BigDecimal depthSampleInterval = configureSensorPane.getDepthSampleInterval();
		KinectDepthResolutionEnum depthFormat = configureSensorPane.getDepthFrameFormat();
		BooleanEnum nearMode = configureSensorPane.getNearMode();
		
		KinectSensor kinectSensor = new KinectSensor();
		kinectSensor.setColorFrameFormat(colorFormat);
		kinectSensor.setColorSampleInterval(colorSampleInterval);
		kinectSensor.setDepthFrameFormat(depthFormat);
		kinectSensor.setDepthSampleInterval(depthSampleInterval);
		kinectSensor.setNearMode(nearMode);
		kinectSensor.setSkeletonAndFaceTrackingSampleInterval(trackingInterval);
		
		SensorInput sensorInput = new SensorInput();
		sensorInput.setType(kinectSensor);
		return sensorInput;
	}
	
	private Writer constructWriterForSensor() {
		//If the user didn't pair a Writer with the Sensor then we're done!
		if(!archiveRawSensorDataPane.isArchiveRawSensorData()) {
			return null;
		}
		
		Writer writer = new Writer();
		
		BigInteger id = SensorsConfigurationFactory.generateId();
		writer.setId(id);

		SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
		String name = sensorType.getDisplayName() + " Writer";
		writer.setName(name);
		
		SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
		WriterTypeEnum writerType = sensorConfigurationMaps.getSensorTypeToWriterTypeMap().get(sensorType);
		String writerImpl = sensorConfigurationMaps.getWriterTypeToImplMap().get(writerType);
		writer.setWriterImpl(writerImpl);
				
		//The user wanted to pair a writer with the sensor but they wanted to
		//use default data to configure the writer.
		if(archiveRawSensorDataPane.isUseDefaultArchiveData()) {
			Serializable writerInputType = SensorsConfigurationFactory.createWriterInputType(writerType);
			
			WriterInput writerInput = new WriterInput();
			writerInput.setType(writerInputType);
			
			writer.setWriterInput(writerInput);
		}
		//The user paired a writer with the sensor and they configured it.
		else {
			WriterInput writerInput = constructWriterInput(configureSensorArchivePane);
			writer.setWriterInput(writerInput);
		}
		
		return writer;
	}
	
	private WriterInput constructWriterInput(ConfigureArchivePane archivePane) {
		//TODO Judging from the files in config/sensor/SensorCnofigurations/ it
		//looks like the input is always GenericSensorDelimitedWriter except
		//when it is KinectSensorWriter?
		
		SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
		if(sensorType == SensorTypeEnum.KINECT) {
			return constructKinectSensorWriter(archivePane);
		} else {
			return constructGenericSensorDelimitedWriter(archivePane);
		}
	}
	
	private WriterInput constructKinectSensorWriter(ConfigureArchivePane archivePane) {
		String filePrefix = archivePane.getFilePrefix();
		ImageCompressionFormat colorFormat = archivePane.getColorCompression();
		ImageCompressionFormat depthFormat = archivePane.getDepthCompression();
		
		KinectSensorWriter writer = new KinectSensorWriter();
		writer.setFilePrefix(filePrefix);
		writer.setColorCompression(colorFormat);
		writer.setDepthCompression(depthFormat);
		
		WriterInput writerInput = new WriterInput();
		writerInput.setType(writer);
		return writerInput;
	}
	
	private WriterInput constructGenericSensorDelimitedWriter(ConfigureArchivePane archivePane) {
		String filePrefix = archivePane.getFilePrefix();
		String delimiter = archivePane.getDelimiter();
		
		GenericSensorDelimitedWriter writer = new GenericSensorDelimitedWriter();
		writer.setDatumDelimReplacementChar(delimiter);
		writer.setFilePrefix(filePrefix);

		WriterInput writerInput = new WriterInput();
		writerInput.setType(writer);
		return writerInput;
	}
	
	private Filter constructFilter(Writer writer) {
		//If the user didn't make a filter then our job is easy.
		if(!filterPane.isFilterRawSensorData()) {
			return null;
		}
		
		Filter filter = new Filter();
		
		BigInteger id = SensorsConfigurationFactory.generateId();
		filter.setId(id);

		SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
		String name = sensorType.getDisplayName() + " Filter";
		filter.setName(name);
		
		SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
		FilterTypeEnum filterType = sensorConfigurationMaps.getSensorTypeToFilterTypeMap().get(sensorType);
		String filterImpl = sensorConfigurationMaps.getFilterTypeToImplMap().get(filterType);
		filter.setFilterImpl(filterImpl);
		
		//TODO Judging from the files in config/sensor/SensorCnofigurations/ it
		//looks like the input is always NULL.
		filter.setFilterInput(new FilterInput());
		
		if(broadcastFilteredDataPane.isBroadcastRawSensorData()) {
			filter.setDistributeExternally(BooleanEnum.TRUE);
		} else {
			filter.setDistributeExternally(BooleanEnum.FALSE);
		}
		
		if(writer != null) {
			filter.setWriterInstance(writer.getId());
		}
		
		return filter;
	}
	
	private Writer constructWriterForFilter() {
		//If the user didn't pair a Writer with the Filter then we're done!
		if(!filterPane.isFilterRawSensorData()) {
			return null;
		}
		
		Writer writer = new Writer();
		
		BigInteger id = SensorsConfigurationFactory.generateId();
		writer.setId(id);

		SensorTypeEnum sensorType = selectSensorTypePane.getSensorType();
		String name = sensorType.getDisplayName() + " Writer";
		writer.setName(name);
		
		SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
		WriterTypeEnum writerType = sensorConfigurationMaps.getSensorTypeToWriterTypeMap().get(sensorType);
		String writerImpl = sensorConfigurationMaps.getWriterTypeToImplMap().get(writerType);
		writer.setWriterImpl(writerImpl);
				
		//The user wanted to pair a writer with the filter but they wanted to
		//use default data to configure the writer.
		if(archiveFilteredDataPane.isUseDefaultArchiveData()) {
			Serializable writerInputType = SensorsConfigurationFactory.createWriterInputType(writerType);
			
			WriterInput writerInput = new WriterInput();
			writerInput.setType(writerInputType);
			
			writer.setWriterInput(writerInput);
		}
		//The user paired a writer with the filter and they configured it.
		else {
			WriterInput writerInput = constructWriterInput(configureFilteredArchivePane);
			writer.setWriterInput(writerInput);
		}
		
		return writer;
	}
	
	private void countSteps() {
		stepLabel.setText("Step " + currentStep + " of " + totalSteps);
		stepLabel.setVisible(!finishedPane.isVisible());
	}
}
