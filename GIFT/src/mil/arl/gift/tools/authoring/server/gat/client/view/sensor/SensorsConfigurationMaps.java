/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor;

import java.util.HashMap;

import mil.arl.gift.common.enums.SensorTypeEnum;

/**
 * The SensorTypeEnum, FilterTypeEnum, and WriterTypeEnum are used exclusively
 * to display human-readable text to the end user allowing them to specify
 * which type of object to use. The backend doesn't use these enums, instead
 * they rely SensorImpl, FilterImpl, and WriterImpl strings which are
 * essentially class paths.
 * 
 * The primary purpose of this class is to create a mapping between the enums
 * (user friendly) to the impls (software friendly).
 * 
 * The secondary purpose of this class is to define the compositional structure
 * between Sensors, Filters, and Writers. For instance, a specific type of
 * Sensor should be paired with a specific type of Filter and Writer and the
 * same is true when it comes to pairing a specific type of Filter with a
 * Writer.
 * @author elafave
 *
 */
public class SensorsConfigurationMaps {

	static private SensorsConfigurationMaps instance;
	
	private HashMap<SensorTypeEnum, String> sensorTypeToImplMap = new HashMap<SensorTypeEnum, String>();

	private HashMap<String, SensorTypeEnum> sensorImplToTypeMap = new HashMap<String, SensorTypeEnum>();
	
	private HashMap<SensorTypeEnum, WriterTypeEnum> sensorTypeToWriterTypeMap = new HashMap<SensorTypeEnum, WriterTypeEnum>();
	
	private HashMap<SensorTypeEnum, FilterTypeEnum> sensorTypeToFilterTypeMap = new HashMap<SensorTypeEnum, FilterTypeEnum>();
	
	private HashMap<WriterTypeEnum, String> writerTypeToImplMap = new HashMap<WriterTypeEnum, String>();

	private HashMap<String, WriterTypeEnum> writerImplToTypeMap = new HashMap<String, WriterTypeEnum>();
	
	private HashMap<FilterTypeEnum, String> filterTypeToImplMap = new HashMap<FilterTypeEnum, String>();

	private HashMap<String, FilterTypeEnum> filterImplToTypeMap = new HashMap<String, FilterTypeEnum>();
	
	private HashMap<FilterTypeEnum, WriterTypeEnum> filterTypeToWriterTypeMap = new HashMap<FilterTypeEnum, WriterTypeEnum>();
	
	static public SensorsConfigurationMaps getInstance() {
		if(instance == null) {
			instance = new SensorsConfigurationMaps();
		}
		return instance;
	}
	
	private SensorsConfigurationMaps() {
		populateSensorTypeImplMaps();
		populateSensorTypeToWriterTypeMap();
		populateSensorTypeToFilterTypeMap();
		populateWriterTypeImplMaps();
		populateFilterTypeImplMaps();
		populateFilterTypeToWriterTypeMap();
	}
	
	/**
	 * @return Mapping from a SensorTypeEnum (user-friendly) to a
	 * SensorImpl (software-friendly).
	 */
	public HashMap<SensorTypeEnum, String> getSensorTypeToImplMap() {
		return sensorTypeToImplMap;
	}
	
	/**
	 * @return Mapping from a SensorImpl (software-friendly) to a
	 * SensorTypeEnum (user-friendly)
	 */
	public HashMap<String, SensorTypeEnum> getSensorImplToTypeMap() {
		return sensorImplToTypeMap;
	}
	
	/**
	 * The SensorTypeEnum to WriterTypeEnum tells you what type of Writer
	 * should be paired with a specific type of Sensor.
	 * @return Mapping from a SensorTypeEnum to a WriterTypeEnum.
	 */
	public HashMap<SensorTypeEnum, WriterTypeEnum> getSensorTypeToWriterTypeMap() {
		return sensorTypeToWriterTypeMap;
	}
	
	/**
	 * The SensorTypeEnum to FilterTypeEnum tells you what type of Filter
	 * should be paired with a specific type of Sensor.
	 * @return Mapping from a SensorTypeEnum to FilterTypeEnum.
	 */
	public HashMap<SensorTypeEnum, FilterTypeEnum> getSensorTypeToFilterTypeMap() {
		return sensorTypeToFilterTypeMap;
	}
	
	/**
	 * @return Mapping from a WriterTypeEnum (user-friendly) to a
	 * WriterImpl (software-friendly).
	 */
	public HashMap<WriterTypeEnum, String> getWriterTypeToImplMap() {
		return writerTypeToImplMap;
	}
	
	/**
	 * @return Mapping from a WriterImpl (software-friendly) to a
	 * WriterTypeEnum (user-friendly)
	 */
	public HashMap<String, WriterTypeEnum> getWriterImplToTypeMap() {
		return writerImplToTypeMap;
	}
	
	/**
	 * @return Mapping from a FilterTypeEnum (user-friendly) to a
	 * FilterImpl (software-friendly).
	 */
	public HashMap<FilterTypeEnum, String> getFilterTypeToImplMap() {
		return filterTypeToImplMap;
	}
	
	/**
	 * @return Mapping from a FilterImpl (software-friendly) to a
	 * FilterTypeEnum (user-friendly)
	 */
	public HashMap<String, FilterTypeEnum> getFilterImplToTypeMap() {
		return filterImplToTypeMap;
	}
	
	/**
	 * The FilterTypeEnum to WriterTypeEnum tells you what type of Writer
	 * should be paired with a specific type of Filter.
	 * @return Mapping from a FilterTypeEnum to WriterTypeEnum.
	 */
	public HashMap<FilterTypeEnum, WriterTypeEnum> getFilterTypeToWriterTypeMap() {
		return filterTypeToWriterTypeMap;
	}
	
	private void populateSensorTypeImplMaps() {
		pairSensorTypeWithImpl(SensorTypeEnum.BIOHARNESS, "sensor.impl.zephyr.BioHarnessSensor");
		
		/**
		 * The emotive stuff has been commented out for now because they're
		 * "restricted". In the future we may figure out a way to dynamically
		 * support them if they're available but that is a lower priority.
		 * 
		 * pairSensorTypeWithImpl(SensorTypeEnum.EMO_COMPOSER, "########I_DONT_KNOW_WHAT_CLASS_PATH_TO_USE########");
		 * pairSensorTypeWithImpl(SensorTypeEnum.EMOTIV, "########I_DONT_KNOW_WHAT_CLASS_PATH_TO_USE########");
		 * 
		 */
		
		pairSensorTypeWithImpl(SensorTypeEnum.EXPERTISE_SURROGATE, "sensor.impl.ExpertiseSurrogateSensor");
		//pairSensorTypeWithImpl(SensorTypeEnum.GSR, "########I_DONT_KNOW_WHAT_CLASS_PATH_TO_USE########");
		pairSensorTypeWithImpl(SensorTypeEnum.KINECT, "sensor.impl.KinectSensor");
		pairSensorTypeWithImpl(SensorTypeEnum.MOTIVATION_SURROGATE, "sensor.impl.MotivationSurrogateSensor");
		//pairSensorTypeWithImpl(SensorTypeEnum.MOUSE_EVENT, "########I_DONT_KNOW_WHAT_CLASS_PATH_TO_USE########");
		pairSensorTypeWithImpl(SensorTypeEnum.MOUSE_TH, "sensor.impl.MouseTempHumiditySensor");
		pairSensorTypeWithImpl(SensorTypeEnum.MOUSE_TH_SURROGATE, "sensor.impl.MouseTempHumiditySurrogateSensor");
		//pairSensorTypeWithImpl(SensorTypeEnum.OS3D, "########I_DONT_KNOW_WHAT_CLASS_PATH_TO_USE########");
		pairSensorTypeWithImpl(SensorTypeEnum.Q, "sensor.impl.QSensor");
		pairSensorTypeWithImpl(SensorTypeEnum.SELF_ASSESSMENT, "sensor.impl.SelfAssessmentSensor");
		pairSensorTypeWithImpl(SensorTypeEnum.SINE_WAVE, "sensor.impl.SineWaveSensor");
		//pairSensorTypeWithImpl(SensorTypeEnum.TEMP_HUMIDITY, "########I_DONT_KNOW_WHAT_CLASS_PATH_TO_USE########");
		pairSensorTypeWithImpl(SensorTypeEnum.VHT_MULTISENSE, "sensor.impl.VhtMultisenseSensor");
	}
	
	private void pairSensorTypeWithImpl(SensorTypeEnum sensorTypeEnum, String sensorImpl) {
		sensorTypeToImplMap.put(sensorTypeEnum, sensorImpl);
		sensorImplToTypeMap.put(sensorImpl, sensorTypeEnum);
	}
	
	private void populateSensorTypeToWriterTypeMap() {
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.BIOHARNESS, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.EXPERTISE_SURROGATE, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.GSR, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.KINECT, WriterTypeEnum.KINECT_SENSOR_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.MOTIVATION_SURROGATE, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.MOUSE_TH, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.MOUSE_TH_SURROGATE, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.OS3D, WriterTypeEnum.SCIENTIFIC_NOTATION_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.Q, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.SELF_ASSESSMENT, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.SINE_WAVE, WriterTypeEnum.GENERIC_WRITER);
		sensorTypeToWriterTypeMap.put(SensorTypeEnum.VHT_MULTISENSE, WriterTypeEnum.GENERIC_WRITER);
	}
	
	private void populateSensorTypeToFilterTypeMap() {
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.BIOHARNESS, FilterTypeEnum.BIOHARNESS_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.EXPERTISE_SURROGATE, FilterTypeEnum.GENERIC_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.GSR, FilterTypeEnum.GSR_DETECTION_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.KINECT, FilterTypeEnum.KINECT_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.MOTIVATION_SURROGATE, FilterTypeEnum.GENERIC_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.MOUSE_TH, FilterTypeEnum.GENERIC_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.MOUSE_TH_SURROGATE, FilterTypeEnum.GENERIC_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.OS3D, FilterTypeEnum.GENERIC_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.Q, FilterTypeEnum.GENERIC_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.SELF_ASSESSMENT, FilterTypeEnum.GENERIC_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.SINE_WAVE, FilterTypeEnum.SINE_WAVE_SENSOR_FILTER);
		sensorTypeToFilterTypeMap.put(SensorTypeEnum.VHT_MULTISENSE, FilterTypeEnum.GENERIC_SENSOR_FILTER);
	}
	
	private void populateWriterTypeImplMaps() {
		pairWriterTypeWithImpl(WriterTypeEnum.GENERIC_WRITER, "sensor.writer.GenericSensorDelimitedWriter");
		pairWriterTypeWithImpl(WriterTypeEnum.KINECT_SENSOR_WRITER, "sensor.writer.KinectSensorWriter");
		pairWriterTypeWithImpl(WriterTypeEnum.SCIENTIFIC_NOTATION_WRITER, "sensor.writer.ScientificNotationSensorDelimitedWriter");
	}
	
	private void pairWriterTypeWithImpl(WriterTypeEnum writerType, String writerImpl) {
		writerTypeToImplMap.put(writerType, writerImpl);
		writerImplToTypeMap.put(writerImpl, writerType);
	}
	
	private void populateFilterTypeImplMaps() {
		pairFilterTypeWithImpl(FilterTypeEnum.BIOHARNESS_FILTER, "sensor.filter.BioharnessFilter");
		pairFilterTypeWithImpl(FilterTypeEnum.EMOTIV_SENSOR_FILTER, "sensor.filter.EmotivSensorFilter");
		pairFilterTypeWithImpl(FilterTypeEnum.GSR_DETECTION_FILTER, "sensor.filter.GSRDetectionFilter");
		pairFilterTypeWithImpl(FilterTypeEnum.GENERIC_SENSOR_FILTER, "sensor.filter.GenericSensorFilter");
		pairFilterTypeWithImpl(FilterTypeEnum.KINECT_SENSOR_FILTER, "sensor.filter.KinectSensorFilter");
		pairFilterTypeWithImpl(FilterTypeEnum.QRS_FROM_ECG_FILTER, "sensor.filter.QrsFromEcgFilter");
		pairFilterTypeWithImpl(FilterTypeEnum.SINE_WAVE_SENSOR_FILTER, "sensor.filter.SineWaveSensorFilter");
	}
	
	private void populateFilterTypeToWriterTypeMap() {
		filterTypeToWriterTypeMap.put(FilterTypeEnum.BIOHARNESS_FILTER, WriterTypeEnum.GENERIC_WRITER);
		filterTypeToWriterTypeMap.put(FilterTypeEnum.EMOTIV_SENSOR_FILTER, WriterTypeEnum.GENERIC_WRITER);
		filterTypeToWriterTypeMap.put(FilterTypeEnum.GSR_DETECTION_FILTER, WriterTypeEnum.GENERIC_WRITER);
		filterTypeToWriterTypeMap.put(FilterTypeEnum.GENERIC_SENSOR_FILTER, WriterTypeEnum.GENERIC_WRITER);
		filterTypeToWriterTypeMap.put(FilterTypeEnum.KINECT_SENSOR_FILTER, WriterTypeEnum.KINECT_SENSOR_WRITER);
		filterTypeToWriterTypeMap.put(FilterTypeEnum.QRS_FROM_ECG_FILTER, WriterTypeEnum.GENERIC_WRITER);
		filterTypeToWriterTypeMap.put(FilterTypeEnum.SINE_WAVE_SENSOR_FILTER, WriterTypeEnum.GENERIC_WRITER);
	}
	
	private void pairFilterTypeWithImpl(FilterTypeEnum filterType, String filterImpl) {
		filterTypeToImplMap.put(filterType, filterImpl);
		filterImplToTypeMap.put(filterImpl, filterType);
	}
}
