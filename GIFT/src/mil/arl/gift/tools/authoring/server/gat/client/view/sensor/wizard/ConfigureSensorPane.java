/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard;

import generated.sensor.BooleanEnum;
import generated.sensor.KinectColorResolutionEnum;
import generated.sensor.KinectDepthResolutionEnum;
import generated.sensor.KinectSensor;
import generated.sensor.MouseTempHumiditySurrogateSensor;
import generated.sensor.SelfAssessmentSensor;
import generated.sensor.SineWaveSensor;
import generated.sensor.VhtMultisenseSensor;

import java.io.Serializable;
import java.math.BigDecimal;

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.BooleanEnumListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.KinectColorResolutionValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.KinectDepthResolutionValueListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DoubleBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class ConfigureSensorPane.
 */
public class ConfigureSensorPane extends Composite {
	
	/** The ui binder. */
    interface ConfigureSensorPaneUiBinder extends UiBinder<Widget, ConfigureSensorPane> {} 
	private static ConfigureSensorPaneUiBinder uiBinder = GWT.create(ConfigureSensorPaneUiBinder.class);
	
	@UiField protected TextBox sensorNameTextBox;
	@UiField protected DoubleBox intervalDoubleBox;
	private String sensorName;
	private Double intervalValue;
	
	/**
	 * These widgets are used for SelfAssessmentSensor
	 */
	@UiField protected FlowPanel selfAssessmentSensorPanel;
	@UiField protected DoubleBox rateChangeAmountDoubleBox;
	private Double rateChangeAmount;
	
	
	/**
	 * These widgets are used for MouseTempHumiditySurrogateSensor
	 */
	@UiField protected FlowPanel mouseTempHumitySurrogateSensorPanel;
	@UiField protected DoubleBox temperatureRateChangeAmountDoubleBox;
	private Double temperatureRateChange;
	@UiField protected DoubleBox humidityRateChangeAmountDoubleBox;
	private Double humidityRateChange;
	
	/**
	 * These widgets are used for the SineWaveSensor
	 */
	@UiField protected FlowPanel sineWaveSensorPanel;
	@UiField protected DoubleBox amplitudeDoubleBox;
	private Double amplitude;
	@UiField protected DoubleBox periodDoubleBox;
	private Double period;
	
	/**
	 * These widgets are used for the VhtMultisenseSensor
	 */
	@UiField protected FlowPanel vhtMultisenseSensorPanel;
	@UiField protected LongBox datalessWarningDelayLongBox;
	private Long datalessWarningDelay;
	@UiField protected TextBox activeMqUrlTextBox;
	@UiField protected Label activeMqTopicLabel;
	@UiField protected TextBox activeMqTopicTextBox;
	
	/**
	 * These widgets are used for the KinectSensor
	 */
	@UiField protected FlowPanel kinectSensorPanel;
	@UiField protected DoubleBox trackingIntervalDoubleBox;
	private Double trackingInterval;
	@UiField protected DoubleBox colorSampleIntervalDoubleBox;
	private Double colorSampleInterval;
	@UiField protected KinectColorResolutionValueListBox colorFrameFormatListBox;
	@UiField protected DoubleBox depthSampleIntervalDoubleBox;
	private Double depthSampleInterval;
	@UiField protected KinectDepthResolutionValueListBox depthFrameFormatListBox;
	@UiField protected BooleanEnumListBox nearModeListBox;
	
    public ConfigureSensorPane() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        reset();
        addHandlers();
    }
    
    public void reset() {
    	///////////////////////////////////////////////////////////////////////
    	//Reset Sensor fields.
    	/////////////////////////////////////////////////////////////////////
    	sensorName = "New Unnamed Sensor";
    	sensorNameTextBox.setValue(sensorName);

    	intervalValue = SensorsConfigurationFactory.DEFAULT_SENSOR_INTERVAL.doubleValue();
    	intervalDoubleBox.setValue(intervalValue);
    	
    	///////////////////////////////////////////////////////////////////////
    	//Reset SelfAssessmentSensor fields.
    	///////////////////////////////////////////////////////////////////////
    	rateChangeAmount = SensorsConfigurationFactory.DEFAULT_SELF_ASSESSMENT_SENSOR_RATE_CHANGE.doubleValue();
    	rateChangeAmountDoubleBox.setValue(rateChangeAmount);

    	///////////////////////////////////////////////////////////////////////
    	//Reset MouseTempHumiditySurrogateSensor fields.
    	///////////////////////////////////////////////////////////////////////
    	temperatureRateChange = SensorsConfigurationFactory.DEFAULT_MOUSE_TEMP_HUMIDITY_SURROGATE_SENSOR_TEMPERATURE_RATE.doubleValue();
    	temperatureRateChangeAmountDoubleBox.setValue(temperatureRateChange);
    	
    	humidityRateChange = SensorsConfigurationFactory.DEFAULT_MOUSE_TEMP_HUMIDITY_SURROGATE_SENSOR_HUMIDITY_RATE.doubleValue();
    	humidityRateChangeAmountDoubleBox.setValue(humidityRateChange);

    	///////////////////////////////////////////////////////////////////////
    	//Reset SineWaveSensor fields.
    	///////////////////////////////////////////////////////////////////////
    	amplitude = SensorsConfigurationFactory.DEFAULT_SINE_WAVE_SENSOR_AMPLITUDE.doubleValue();
    	amplitudeDoubleBox.setValue(amplitude);
    	
    	period = SensorsConfigurationFactory.DEFAULT_SINE_WAVE_SENSOR_PERIOD.doubleValue();
    	periodDoubleBox.setValue(period);

    	///////////////////////////////////////////////////////////////////////
    	//Reset VHTMultisenseSensor fields
    	///////////////////////////////////////////////////////////////////////
    	datalessWarningDelay = SensorsConfigurationFactory.DEFAULT_VHT_MULTISENSE_SENSOR_DATALESS_WARNING_DELAY;
    	datalessWarningDelayLongBox.setValue(datalessWarningDelay);
    	activeMqUrlTextBox.setText(SensorsConfigurationFactory.DEFAULT_VHT_MULTISENSE_SENSOR_ACTIVE_MQ_URL);
    	activeMqTopicTextBox.setText(SensorsConfigurationFactory.DEFAULT_VHT_MULTISENSE_SENSOR_ACTIVE_MQ_TOPIC);

    	///////////////////////////////////////////////////////////////////////
    	//Reset KinectSensor fields
    	///////////////////////////////////////////////////////////////////////
    	trackingInterval = SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_TRACKING_SAMPLE_INTERVAL.doubleValue();
    	trackingIntervalDoubleBox.setValue(trackingInterval);
    	
    	colorFrameFormatListBox.setValue(SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_COLOR_FRAME_FORMAT);

    	colorSampleInterval = SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_COLOR_SAMPLE_INTERVAL.doubleValue();
    	colorSampleIntervalDoubleBox.setValue(colorSampleInterval);
    	
    	depthFrameFormatListBox.setValue(SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_DEPTH_FRAME_FORMAT);
    	
    	depthSampleInterval = SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_DEPTH_SAMPLE_INTERVAL.doubleValue();
    	depthSampleIntervalDoubleBox.setValue(depthSampleInterval);

    	nearModeListBox.setValue(SensorsConfigurationFactory.DEFAULT_KINECT_SENSOR_NEAR_MODE);
    }
    
    private void addHandlers() {
    	
    	sensorNameTextBox.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				String value = event.getValue();
				
				if(value == null || value.isEmpty()){
					
					sensorNameTextBox.setValue(sensorName);
					
				} else {
					sensorName = value;
				}
			}
		});
    	
    	ValueChangeHandler<Double> intervalHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					if(intervalDoubleBox.getText().equals("")) {
						// this is an optional field; allow the value to be empty
						intervalDoubleBox.setValue(null);
						intervalValue = null;
					} else {
						// Revert the textfield if the user entered non-digit characters
						intervalDoubleBox.setValue(value);
					}
				} else {
					intervalValue = value;
				}
			}
		};
		intervalDoubleBox.addValueChangeHandler(intervalHandler);
        
        ValueChangeHandler<Double> rateChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					rateChangeAmountDoubleBox.setValue(rateChangeAmount);
				} else {
					rateChangeAmount = value;
				}
			}
		};
		rateChangeAmountDoubleBox.addValueChangeHandler(rateChangeHandler);
        
        ValueChangeHandler<Double> temperatureRateChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					temperatureRateChangeAmountDoubleBox.setValue(temperatureRateChange);
				} else {
					temperatureRateChange = value;
				}
			}
		};
		temperatureRateChangeAmountDoubleBox.addValueChangeHandler(temperatureRateChangeHandler);
        
		ValueChangeHandler<Double> humidityRateChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					humidityRateChangeAmountDoubleBox.setValue(humidityRateChange);
				} else {
					humidityRateChange = value;
				}
			}
		};
		humidityRateChangeAmountDoubleBox.addValueChangeHandler(humidityRateChangeHandler);
		
		ValueChangeHandler<Double> amplitudeChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					amplitudeDoubleBox.setValue(amplitude);
				} else {
					amplitude = value;
				}
			}
		};
		amplitudeDoubleBox.addValueChangeHandler(amplitudeChangeHandler);
		
		ValueChangeHandler<Double> periodChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					periodDoubleBox.setValue(period);
				} else {
					period = value;
				}
			}
		};
		periodDoubleBox.addValueChangeHandler(periodChangeHandler);
		
		ValueChangeHandler<Long> datalessWarningDelayChangeHandler = new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> changeEvent) {
				Long value = changeEvent.getValue();
				if(value == null) {
					if(datalessWarningDelayLongBox.getText().equals("")) {
						// this is an optional field; allow the value to be empty
						datalessWarningDelayLongBox.setValue(null);
						datalessWarningDelay = null;
					} else {
						// Revert the textfield if the user entered non-digit characters
						datalessWarningDelayLongBox.setValue(value);
					}
				} else {
					datalessWarningDelay = value;
				}
			}
		};
		datalessWarningDelayLongBox.addValueChangeHandler(datalessWarningDelayChangeHandler);
		
		ValueChangeHandler<Double> trackingIntervalChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					if(trackingIntervalDoubleBox.getText().equals("")) {
						// this is an optional field; allow the value to be empty
						trackingIntervalDoubleBox.setValue(null);
						trackingInterval = null;
					} else {
						trackingIntervalDoubleBox.setValue(trackingInterval);
					}
				} else {
					trackingInterval = value;
				}
			}
		};
		trackingIntervalDoubleBox.addValueChangeHandler(trackingIntervalChangeHandler);
		
		ValueChangeHandler<Double> colorSampleIntervalChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					if(colorSampleIntervalDoubleBox.getText().equals("")) {
						// this is an optional field; allow the value to be empty
						colorSampleIntervalDoubleBox.setValue(null);
						colorSampleInterval = null;
					} else {
						// Revert the textfield if the user entered non-digit characters
						colorSampleIntervalDoubleBox.setValue(value);
					}
				} else {
					colorSampleInterval = value;
				}
			}
		};
		colorSampleIntervalDoubleBox.addValueChangeHandler(colorSampleIntervalChangeHandler);
		
		ValueChangeHandler<Double> depthSampleIntervalChangeHandler = new ValueChangeHandler<Double>() {
			@Override
			public void onValueChange(ValueChangeEvent<Double> changeEvent) {
				Double value = changeEvent.getValue();
				if(value == null) {
					if(depthSampleIntervalDoubleBox.getText().equals("")) {
						// this is an optional field; allow the value to be empty
						depthSampleIntervalDoubleBox.setValue(null);
						depthSampleInterval = null;
					} else {
						// Revert the textfield if the user entered non-digit characters
						depthSampleIntervalDoubleBox.setValue(value);
					}
				} else {
					depthSampleInterval = value;
				}
			}
		};
		depthSampleIntervalDoubleBox.addValueChangeHandler(depthSampleIntervalChangeHandler);
    }
    
    public void setSensorType(SensorTypeEnum sensorType) {

    	sensorName = sensorType.getDisplayName() + " Sensor";
    	sensorNameTextBox.setValue(sensorName);
    	
    	Serializable input = SensorsConfigurationFactory.createSensorInputType(sensorType);
    	
    	if(input instanceof SelfAssessmentSensor) {
    		setSelfAssessmentVisibility(true);
    	} else {
    		setSelfAssessmentVisibility(false);
    	}

    	if(input instanceof MouseTempHumiditySurrogateSensor) {
    		setMouseTempHumidityVisibility(true);
    	} else {
    		setMouseTempHumidityVisibility(false);
    	}
    	
    	if(input instanceof SineWaveSensor) {
    		setSineWaveVisibility(true);
    	} else {
    		setSineWaveVisibility(false);
    	}
    	
    	if(input instanceof VhtMultisenseSensor) {
    		setMultisenseVisibility(true);
    	} else {
    		setMultisenseVisibility(false);
    	}
    	
    	if(input instanceof KinectSensor) {
    		setKinectVisibility(true);
    	} else {
    		setKinectVisibility(false);
    	}
    }
    
    private void setSelfAssessmentVisibility(boolean visible) {
    	selfAssessmentSensorPanel.setVisible(visible);
    }
    
    private void setMouseTempHumidityVisibility(boolean visible) {
    	mouseTempHumitySurrogateSensorPanel.setVisible(visible);
    }
    
    private void setSineWaveVisibility(boolean visible) {
    	sineWaveSensorPanel.setVisible(visible);
    }
    
    private void setMultisenseVisibility(boolean visible) {
    	vhtMultisenseSensorPanel.setVisible(visible);
    }
    
    private void setKinectVisibility(boolean visible) {
    	kinectSensorPanel.setVisible(visible);
    }
    
    public String getName() {
    	return sensorNameTextBox.getText();
    }
    
    public BigDecimal getInterval() {
    	return (intervalValue == null) ? null : BigDecimal.valueOf(intervalValue);
    }
    
    public BigDecimal getRateChangeAmount() {
    	return BigDecimal.valueOf(rateChangeAmount);
    }
    
    public BigDecimal getTemperatureRateChangeAmount() {
    	return BigDecimal.valueOf(temperatureRateChange);
    }
    
    public BigDecimal getHumidityRateChangeAmount() {
    	return BigDecimal.valueOf(humidityRateChange);
    }
    
    public BigDecimal getAmplitude() {
    	return BigDecimal.valueOf(amplitude);
    }
    
    public BigDecimal getPeriod() {
    	return BigDecimal.valueOf(period);
    }
    
    public Long getDatalessWarningDelay() {
    	return (datalessWarningDelay == null) ? null : Long.valueOf(datalessWarningDelay);
    }
    
    public String getActiveMqUrl() {
    	return activeMqUrlTextBox.getText();
    }
    
    public String getActiveMqTopic() {
    	return activeMqTopicTextBox.getText();
    }
    
    public BigDecimal getTrackingInterval() {
    	return (trackingInterval == null) ? null : BigDecimal.valueOf(trackingInterval);
    }
    
    public BigDecimal getColorSampleInterval() {
    	return (colorSampleInterval == null) ? null : BigDecimal.valueOf(colorSampleInterval);
    }
    
    public KinectColorResolutionEnum getColorFrameFormat() {
    	return colorFrameFormatListBox.getValue();
    }
    
    public BigDecimal getDepthSampleInterval() {
    	return (depthSampleInterval == null) ? null : BigDecimal.valueOf(depthSampleInterval);
    }
    
    public KinectDepthResolutionEnum getDepthFrameFormat() {
    	return depthFrameFormatListBox.getValue();
    }
    
    public BooleanEnum getNearMode() {
    	return nearModeListBox.getValue();
    }
}
