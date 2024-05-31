/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor;

import generated.sensor.BioHarnessSensor;
import generated.sensor.BooleanEnum;
import generated.sensor.ECGDetectionFilterInput;
import generated.sensor.Filter;
import generated.sensor.FilterInput;
import generated.sensor.Filters;
import generated.sensor.GSRDetectionFilterInput;
import generated.sensor.GenericSensorDelimitedWriter;
import generated.sensor.ImageCompressionFormat;
import generated.sensor.KinectColorResolutionEnum;
import generated.sensor.KinectDepthResolutionEnum;
import generated.sensor.KinectSensor;
import generated.sensor.KinectSensorWriter;
import generated.sensor.MouseTempHumiditySensor;
import generated.sensor.MouseTempHumiditySurrogateSensor;
import generated.sensor.OS3DSensor;
import generated.sensor.QSensor;
import generated.sensor.SelfAssessmentSensor;
import generated.sensor.Sensor;
import generated.sensor.SensorInput;
import generated.sensor.Sensors;
import generated.sensor.SensorsConfiguration;
import generated.sensor.SineWaveSensor;
import generated.sensor.VhtMultisenseSensor;
import generated.sensor.Writer;
import generated.sensor.WriterInput;
import generated.sensor.Writers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import mil.arl.gift.common.enums.SensorTypeEnum;

/**
 * This class serves a few purposes:
 * 1.) Sensors, Writers, and Filters all have IDs that must be unique across
 * all three classes (i.e. a Sensor cannot have the same ID as a filter, 
 * writer, or another Sensor). ID generation methods are defined to service
 * this need.
 * 2.) The Sensor, Writer, Filter, Sensor Input Type, Writer Input Type, and
 * Filter Input Type class don't always have the most helpful default 
 * values for their attributes so this class explicity defines default values
 * and methods to construct objects using those default values.
 * 3.) Defines default object composition for Sensor, Writer, and Filter
 * classes (i.e. a sensor of a specific type is paired with a filter of a
 * specific type which is paired with a wrier of a specific type) and provides
 * methods to construct these compositions.
 * @author elafave
 *
 */
public class SensorsConfigurationFactory {
	
	/** Default values for GSRDetectionFilterInput */
	static public final BigInteger DEFAULT_GSR_DETECTION_FILTER_SAMPLING_RATE = BigInteger.valueOf(60);
	static public final float DEFAULT_GSR_DETECTION_FILTER_WINDOW_SIZE = 50f;
	
	/** Default values for ECGDetectionFilterInput */
	static public final BigInteger DEFAULT_ECG_DETECTION_FILTER_SAMPLING_RATE = BigInteger.TEN;
	static public final BigInteger DEFAULT_ECG_DETECTION_FILTER_WINDOW_SIZE = BigInteger.TEN;
	
	/** Default values for GenericSensorDelimitedWriter */
	static public final String DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_DIRECTORY = "output/sensor";
	static public final String DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_FILE_PREFIX = "expertiseSurrogateSensor";
	static public final String DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_REPLACEMENT_CHAR = ",";
	
	/** Default values for KinectSensorWriter */
	static public final ImageCompressionFormat DEFAULT_KINECT_SENSOR_WRITER_COLOR_COMPRESSION = ImageCompressionFormat.LZ_4;
	static public final ImageCompressionFormat DEFAULT_KINECT_SENSOR_WRITER_DEPTH_COMPRESSION = ImageCompressionFormat.LZ_4;
	static public final String DEFAULT_KINECT_SENSOR_WRITER_DIRECTORY = "output/sensor";
	static public final String DEFAULT_KINECT_SENSOR_WRITER_FILE_PREFIX = "kinect";
	
	/** Default values for SineWaveSensor */
	static public final BigDecimal DEFAULT_SINE_WAVE_SENSOR_AMPLITUDE = BigDecimal.valueOf(150);
	static public final BigDecimal DEFAULT_SINE_WAVE_SENSOR_PERIOD = BigDecimal.valueOf(180);
	
	/** Default values for KinectSensor */
	static public final KinectColorResolutionEnum DEFAULT_KINECT_SENSOR_COLOR_FRAME_FORMAT = null;
	static public final BigDecimal DEFAULT_KINECT_SENSOR_COLOR_SAMPLE_INTERVAL = BigDecimal.valueOf(5);
	static public final KinectDepthResolutionEnum DEFAULT_KINECT_SENSOR_DEPTH_FRAME_FORMAT = null;
	static public final BigDecimal DEFAULT_KINECT_SENSOR_DEPTH_SAMPLE_INTERVAL = BigDecimal.valueOf(5);
	static public final BigDecimal DEFAULT_KINECT_SENSOR_TRACKING_SAMPLE_INTERVAL = BigDecimal.valueOf(5);
	static public final BooleanEnum DEFAULT_KINECT_SENSOR_NEAR_MODE = null;
	
	/** Default values for MouseTempHumiditySurrogateSensor */
	static public final BigDecimal DEFAULT_MOUSE_TEMP_HUMIDITY_SURROGATE_SENSOR_HUMIDITY_RATE = BigDecimal.valueOf(1);
	static public final BigDecimal DEFAULT_MOUSE_TEMP_HUMIDITY_SURROGATE_SENSOR_TEMPERATURE_RATE = BigDecimal.valueOf(1);
	
	/** Default values for SelfAssessmentSensor */
	static public final BigDecimal DEFAULT_SELF_ASSESSMENT_SENSOR_RATE_CHANGE = BigDecimal.valueOf(0.1);
	
	/** Default values for VHTMultisenseSensor */
	static public final Long DEFAULT_VHT_MULTISENSE_SENSOR_DATALESS_WARNING_DELAY = Long.valueOf(15);
	static public final String DEFAULT_VHT_MULTISENSE_SENSOR_ACTIVE_MQ_URL = null;
	static public final String DEFAULT_VHT_MULTISENSE_SENSOR_ACTIVE_MQ_TOPIC = null;
	
	/** Default values for Filter */
	static public final BooleanEnum DEFAULT_FILTER_DISTRIBUTE_EXTERNALLY = BooleanEnum.TRUE;
	
	/** Default values for Sensor */
	static public final BigDecimal DEFAULT_SENSOR_INTERVAL = BigDecimal.ONE;
	static public final BooleanEnum DEFAULT_SENSOR_DISTRIBUTE_EXTERNALLY = BooleanEnum.FALSE;
	
	
	static private long id = 0;
	
	/**
	 * Resets the ID generator.
	 */
	static public void resetIdGenerator() {
		id = 0;
	}
	
	/**
	 * Configures the ID Generator to guarantee that subsequent calls to
	 * generateId will never return an ID used by a node in the given
	 * sensorsConfiguration.
	 * @param sensorsConfiguration SensorsConfiguration that contains nodes
	 * with IDs that the ID Generator shouldn't generate (duplication).
	 */
	static public void resetIdGenerator(SensorsConfiguration sensorsConfiguration) {
		resetIdGenerator();
		
		if(sensorsConfiguration == null) {
			return;
		}
		
		List<Sensor> sensors = sensorsConfiguration.getSensors().getSensor();
		for(Sensor sensor : sensors) {
			long sensorId = sensor.getId().longValue();
			if(sensorId >= id) {
				id = sensorId;
			}
		}
		
		List<Filter> filters = sensorsConfiguration.getFilters().getFilter();
		for(Filter filter : filters) {
			long filterId = filter.getId().longValue();
			if(filterId >= id) {
				id = filterId;
			}
		}
		
		List<Writer> writers = sensorsConfiguration.getWriters().getWriter();
		for(Writer writer : writers) {
			long writerId = writer.getId().longValue();
			if(writerId >= id) {
				id = writerId;
			}
		}
	}
	
	/**
	 * Generates a unique ID relative to the last time the generator was reset.
	 * @return Unique ID.
	 */
	static public BigInteger generateId() {
		id++;
		return BigInteger.valueOf(id);
	}

	/**
	 * Constructs and initializes a SensorsConfiguration with reasonable
	 * default values.
	 * @return A SensorsConfiguration with reasonable default values.
	 */
	static public SensorsConfiguration createDefaultSensorsConfiguration() {
		Writers writers = new Writers();
		Filters filters = new Filters();
		Sensors sensors = new Sensors();
		
		SensorsConfiguration sensorsConfiguration = new SensorsConfiguration();
		sensorsConfiguration.setFilters(filters);
		sensorsConfiguration.setSensors(sensors);
		sensorsConfiguration.setWriters(writers);
		
		return sensorsConfiguration;
	}	
	
	/**
	 * Constructs and initializes a GSRDetectionFilterInput with reasonable
	 * default values.
	 * @return A GSRDetectionFilterInput with reasonable default values.
	 */
	static public GSRDetectionFilterInput createGSRDetectionFilterInput() {
		GSRDetectionFilterInput filterInput = new GSRDetectionFilterInput();
		filterInput.setSamplingRateHz(DEFAULT_GSR_DETECTION_FILTER_SAMPLING_RATE);
		filterInput.setWindowSize(DEFAULT_GSR_DETECTION_FILTER_WINDOW_SIZE);
		return filterInput;
	}
	
	/**
	 * Constructs and initializes a ECGDetectionFilterInput with reasonable
	 * default values.
	 * @return A ECGDetectionFilterInput with reasonable default values.
	 */
	static public ECGDetectionFilterInput createECGDetectionFilterInput() {
		ECGDetectionFilterInput filterInput = new ECGDetectionFilterInput();
		filterInput.setSamplingRateHz(DEFAULT_ECG_DETECTION_FILTER_SAMPLING_RATE);
		filterInput.setWindowSize(DEFAULT_ECG_DETECTION_FILTER_WINDOW_SIZE);
		return filterInput;
	}
	
	/**
	 * Constructs and initializes a GenericSensorDelimitedWriter with
	 * reasonable default values.
	 * @return A GenericSensorDelimitedWriter with reasonable default values.
	 */
	static public GenericSensorDelimitedWriter createGenericSensorDelimitedWriter() {
		GenericSensorDelimitedWriter writer = new GenericSensorDelimitedWriter();
		writer.setFilePrefix(DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_FILE_PREFIX);
		writer.setDatumDelimReplacementChar(DEFAULT_GENERIC_SENSOR_DELIMITED_WRITER_REPLACEMENT_CHAR);
		return writer;
	}
	
	/**
	 * Constructs and initializes a KinectSensorWriter with reasonable default
	 * values.
	 * @return A KinectSensorWriter with reasonable default values.
	 */
	static public KinectSensorWriter createKinectSensorWriter() {
		KinectSensorWriter kinectWriter = new KinectSensorWriter();
		kinectWriter.setColorCompression(DEFAULT_KINECT_SENSOR_WRITER_COLOR_COMPRESSION);
		kinectWriter.setDepthCompression(DEFAULT_KINECT_SENSOR_WRITER_DEPTH_COMPRESSION);
		kinectWriter.setFilePrefix(DEFAULT_KINECT_SENSOR_WRITER_FILE_PREFIX);
		return kinectWriter;
	}

	/**
	 * Constructs and initializes a SineWaveSensor with reasonable default
	 * values.
	 * @return A SineWaveSensor with reasonable default values.
	 */
	static public SineWaveSensor createSineWaveSensor() {
		SineWaveSensor sensor = new SineWaveSensor();
		sensor.setAmplitude(DEFAULT_SINE_WAVE_SENSOR_AMPLITUDE);
		sensor.setPeriod(DEFAULT_SINE_WAVE_SENSOR_PERIOD);
		return sensor;
	}
	
	/**
	 * Constructs and initializes a KinectSensor with reasonable default values.
	 * @return A KinectSensor with reasonable default values.
	 */
	static public KinectSensor createKinectSensor() {
		KinectSensor kinectSensor = new KinectSensor();
		kinectSensor.setColorFrameFormat(DEFAULT_KINECT_SENSOR_COLOR_FRAME_FORMAT);
		kinectSensor.setColorSampleInterval(DEFAULT_KINECT_SENSOR_COLOR_SAMPLE_INTERVAL);
		kinectSensor.setDepthFrameFormat(DEFAULT_KINECT_SENSOR_DEPTH_FRAME_FORMAT);
		kinectSensor.setDepthSampleInterval(DEFAULT_KINECT_SENSOR_DEPTH_SAMPLE_INTERVAL);
		kinectSensor.setSkeletonAndFaceTrackingSampleInterval(DEFAULT_KINECT_SENSOR_TRACKING_SAMPLE_INTERVAL);
		kinectSensor.setNearMode(DEFAULT_KINECT_SENSOR_NEAR_MODE);
		return kinectSensor;
	}
	
	/**
	 * Constructs and initializes a BioHarnessSensor with reasonable default values.
	 * @return A BioHarnessSensor with reasonable default values.
	 */
	static public BioHarnessSensor createBioHarnessSensor() {
		BioHarnessSensor sensor = new BioHarnessSensor();
		return sensor;
	}
	
	/**
	 * Constructs and initializes a MouseTempHumiditySensor with reasonable default values.
	 * @return A MouseTempHumiditySensor with reasonable default values.
	 */
	static public MouseTempHumiditySensor createMouseTempHumiditySensor() {
		MouseTempHumiditySensor sensor = new MouseTempHumiditySensor();
		return sensor;
	}
	
	/**
	 * Constructs and initializes a QSensor with reasonable default values.
	 * @return A QSensor with reasonable default values.
	 */
	static public QSensor createQSensor() {
		QSensor sensor = new QSensor();
		return sensor;
	}
	
	/**
	 * Constructs and initializes a OS3DSensor with reasonable default values.
	 * @return A OS3DSensor with reasonable default values.
	 */
	static public OS3DSensor createOS3DSensor() {
		OS3DSensor sensor = new OS3DSensor();
		return sensor;
	}
	
	
	/**
	 * Constructs and initializes a MouseTempHumiditySurrogateSensor with
	 * reasonable default values.
	 * @return A MouseTempHumiditySurrogateSensor with reasonable default values.
	 */
	static public MouseTempHumiditySurrogateSensor createMouseTempHumiditySurrogateSensor() {
		MouseTempHumiditySurrogateSensor sensor = new MouseTempHumiditySurrogateSensor();
		sensor.setHumidityRateChangeAmount(DEFAULT_MOUSE_TEMP_HUMIDITY_SURROGATE_SENSOR_HUMIDITY_RATE);
		sensor.setTemperatureRateChangeAmount(DEFAULT_MOUSE_TEMP_HUMIDITY_SURROGATE_SENSOR_TEMPERATURE_RATE);
		return sensor;
	}
	
	/**
	 * Constructs and initializes a SelfAssessmentSensor with reasonable
	 * default values.
	 * @return A SelfAssessmentSensor with reasonable default values.
	 */
	static public SelfAssessmentSensor createSelfAssessmentSensor() {
		SelfAssessmentSensor selfAssessmentSensor = new SelfAssessmentSensor();
		selfAssessmentSensor.setRateChangeAmount(DEFAULT_SELF_ASSESSMENT_SENSOR_RATE_CHANGE);
		return selfAssessmentSensor;
	}
	
	/**
	 * Constructs and initializes a VhtMultisenseSensor with reasonable default
	 * values.
	 * @return A VhtMultisenseSensor with reasonable default values.
	 */
	static public VhtMultisenseSensor createVhtMultisenseSensor() {
		VhtMultisenseSensor vhtMultisenseSensor = new VhtMultisenseSensor();
		vhtMultisenseSensor.setDatalessWarningDelay(DEFAULT_VHT_MULTISENSE_SENSOR_DATALESS_WARNING_DELAY);
		vhtMultisenseSensor.setVhtActiveMqUrl(DEFAULT_VHT_MULTISENSE_SENSOR_ACTIVE_MQ_URL);
		vhtMultisenseSensor.setVhtActiveMqTopic(DEFAULT_VHT_MULTISENSE_SENSOR_ACTIVE_MQ_TOPIC);
		return vhtMultisenseSensor;
	}
	
	/**
	 * Convenience method to wrap a single Sensor inside a Sensors object.
	 * @param sensor Sensor to wrap in a Sensors object.
	 * @return Sensors object that contains the given sensor.
	 */
	static public Sensors createSensors(Sensor sensor) {
		Sensors sensors = new Sensors();
		sensors.getSensor().add(sensor);
		return sensors;
	}
	
	/**
	 * Convenience method to wrap a single Filter inside a Filters object.
	 * @param filter Filter to wrap in a Filters object.
	 * @return Filters object that contains the given filter.
	 */
	static public Filters createFilters(Filter filter) {
		Filters filters = new Filters();
		filters.getFilter().add(filter);
		return filters;
	}
	
	/**
	 * Convenience method to wrap a single Writer inside a Writers object.
	 * @param writer Writer to wrap in a Writers object.
	 * @return Writers object that contains the given writer.
	 */
	static public Writers createWriters(Writer writer) {
		Writers writers = new Writers();
		writers.getWriter().add(writer);
		return writers;
	}
	
	/**
	 * Creates a reasonable default Sensor and pairs it with the given
	 * filter and writer.
	 * @param sensorTypeEnum The type of Sensor to create.
	 * @param filter Filter to pair with the sensor.
	 * @param writer Writer to pair with the sensor.
	 * @return A Sensor created with reasonable default values and is paired
	 * with the given filter and writer.
	 */
	static public Sensor createSensor(SensorTypeEnum sensorTypeEnum, Filter filter, Writer writer) {
		String sensorImpl = SensorsConfigurationMaps.getInstance().getSensorTypeToImplMap().get(sensorTypeEnum);
		
		SensorInput sensorInput = new SensorInput();
		sensorInput.setType(createSensorInputType(sensorTypeEnum));
		
		Sensor sensor = new Sensor();
		sensor.setId(generateId());
		sensor.setName(sensorTypeEnum.getDisplayName() + " Sensor");
		sensor.setSensorInput(sensorInput);
		sensor.setSensorImpl(sensorImpl);
		sensor.setInterval(DEFAULT_SENSOR_INTERVAL);
		sensor.setDistributeExternally(DEFAULT_SENSOR_DISTRIBUTE_EXTERNALLY);
		if(filter != null) {
			sensor.setFilterInstance(filter.getId());
		}
		if(writer != null) {
			sensor.setWriterInstance(writer.getId());
		}
		return sensor;
	}
	
	/**
	 * Constructs a reasonable default Sensor Input Type to be paired with a
	 * given sensorTypeEnum. The intended usage is as follows:
	 * 
	 * <pre>
	 * {@code
	 * 		Serializable sensorInputType = SensorsConfigurationFactory.createSensorInputType(sensorType);
	 * 		sensor.getSensorInput().setType(sensorInputType);
	 * }
	 * </pre>
	 * @param sensorTypeEnum SensorTypeEnum the Sensor Input Type will be paired with.
	 * @return A reasonable default Sensor Input Type object to pair with a
	 * Sensor of the given SensorTypeEnum.
	 */
	static public Serializable createSensorInputType(SensorTypeEnum sensorTypeEnum) {
		
		if(sensorTypeEnum == SensorTypeEnum.BIOHARNESS) {
			return createBioHarnessSensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.EXPERTISE_SURROGATE) {
			return null;
			
		} else if(sensorTypeEnum == SensorTypeEnum.GSR) {
			return null;
			
		} else if(sensorTypeEnum == SensorTypeEnum.KINECT) {
			return createKinectSensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.MOTIVATION_SURROGATE) {
			return null;
			
		} else if(sensorTypeEnum == SensorTypeEnum.MOUSE_TH) {
			return createMouseTempHumiditySensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.MOUSE_TH_SURROGATE) {
			return createMouseTempHumiditySurrogateSensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.OS3D) {
			return createOS3DSensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.Q) {
			return createQSensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.SELF_ASSESSMENT) {
			return createSelfAssessmentSensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.SINE_WAVE) {
			return createSineWaveSensor();
			
		} else if(sensorTypeEnum == SensorTypeEnum.VHT_MULTISENSE) {
			return createVhtMultisenseSensor();
		}
		
		return null;
	}

	/**
	 * Constructs a reasonable default Filter to be paired with a given Sensor
	 * and to use a given Writer.
	 * 
	 * @param sensorTypeEnum SensorTypeEnum of the Sensor the Filter will be
	 * paired with.
	 * @param writer Writer that the newly created Filter should use.
	 * @return A reasonable default Filter to be paired with a given Sensor and
	 * use the given Writer.
	 */
	static public Filter createFilter(SensorTypeEnum sensorTypeEnum, Writer writer) {
		SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
		FilterTypeEnum filterType = sensorConfigurationMaps.getSensorTypeToFilterTypeMap().get(sensorTypeEnum);
		String filterImpl = sensorConfigurationMaps.getFilterTypeToImplMap().get(filterType);
		
		FilterInput filterInput = createFilterInput(filterType);
		
		Filter filter = new Filter();
		filter.setDistributeExternally(DEFAULT_FILTER_DISTRIBUTE_EXTERNALLY);
		filter.setFilterImpl(filterImpl);
		filter.setFilterInput(filterInput);
		filter.setId(generateId());
		filter.setName("Unnamed Filter");
		filter.setWriterInstance(writer.getId());
		return filter;
	}
	
	/**
	 * Constructs a reasonable default Writer to be paired with a given Filter.
	 * 
	 * @param filterType FilterTypeEnum the Writer will be paired with.
	 * @return A reasonable default Writer object to pair with a Filter of the
	 * given type FilterTypeEnum.
	 */
	static public Writer createWriter(FilterTypeEnum filterType) {
		SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
		WriterTypeEnum writerType = sensorConfigurationMaps.getFilterTypeToWriterTypeMap().get(filterType);
		String writerImpl = sensorConfigurationMaps.getWriterTypeToImplMap().get(writerType);
		
		WriterInput writerInput = createWriterInput(writerType);
		
		Writer writer = new Writer();
		writer.setId(generateId());
		writer.setName("Unnamed Writer");
		writer.setWriterImpl(writerImpl);
		writer.setWriterInput(writerInput);
		return writer;
	}

	/**
	 * Constructs a reasonable default Writer to be paired with a given Sensor.
	 * 
	 * @param sensorTypeEnum SensorTypeEnum the Writer will be paired with.
	 * @return A reasonable default Writer object to pair with a Sensor of the
	 * given type SensorTypeEnum.
	 */
	static public Writer createWriter(SensorTypeEnum sensorTypeEnum) {
		SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
		WriterTypeEnum writerType = sensorConfigurationMaps.getSensorTypeToWriterTypeMap().get(sensorTypeEnum);
		String writerImpl = sensorConfigurationMaps.getWriterTypeToImplMap().get(writerType);
		
		WriterInput writerInput = createWriterInput(writerType);
		
		Writer writer = new Writer();
		writer.setId(generateId());
		writer.setName("Unnamed Writer");
		writer.setWriterImpl(writerImpl);
		writer.setWriterInput(writerInput);
		return writer;
	}
	
	/**
	 * Constructs a reasonable default WriterInput to be paired with a given
	 * writerType.
	 * 
	 * @param writerType WriterTypeEnum the WriterInput will be paired with.
	 * @return A reasonable default WriterInput object to pair with a Writer of
	 * the given WriterTypeEnum.
	 */
	static public WriterInput createWriterInput(WriterTypeEnum writerType) {
		Serializable writerInputType = createWriterInputType(writerType);
		
		WriterInput writerInput = new WriterInput();
		writerInput.setType(writerInputType);
		
		return writerInput;
	}
	
	/**
	 * Constructs a reasonable default Writer Input Type to be paired with a
	 * given writerType. The intended usage is as follows:
	 * 
	 * <pre>
	 * {@code
	 * 		Serializable writerInputType = createWriterInputType(writerType);
	 * 		writer.getWriterInput().setType(writerInputType);
	 * }
	 * </pre>
	 * @param writerType WriterTypeEnum the Writer Input Type will be paired with.
	 * @return A reasonable default Writer Input Type object to pair with a
	 * Writer of the given WriterTypeEnum.
	 */
	static public Serializable createWriterInputType(WriterTypeEnum writerType) {
		//TODO Judging from the files in config/sensor/SensorCnofigurations/ it
		//looks like the input is always GenericSensorDelimitedWriter except
		//when it is KinectSensorWriter?
		if(writerType.equals(WriterTypeEnum.KINECT_SENSOR_WRITER)) {
			return createKinectSensorWriter();
		} else {
			return createGenericSensorDelimitedWriter();
		}
	}
	
	/**
	 * Constructs a reasonable default FilterInput to be paired with a given
	 * filterType.
	 * 
	 * @param filterType FilterTypeEnum the FilterInput will be paired with.
	 * @return A reasonable default FilterInput object to pair with a Filter of
	 * the given FilterTypeEnum.
	 */
	static public FilterInput createFilterInput(FilterTypeEnum filterType) {
		Serializable filterInputType = createFilterInputType(filterType);
		
		FilterInput filterInput = new FilterInput();
		filterInput.setType(filterInputType);
		
		return filterInput;
	}
	
	/**
	 * Constructs a reasonable default Filter Input Type to be paired with a
	 * given filterType. The intended usage is as follows:
	 * 
	 * <pre>
	 * {@code
	 * 		Serializable filterInputType = SensorsConfigurationFactory.createFilterInputType(filterType);
	 * 		filter.getFilterInput().setType(filterInputType);
	 * }
	 * </pre>
	 * @param filterType FilterTypeEnum the Filter Input Type will be paired with.
	 * @return A reasonable default Filter Input Type object to pair with a
	 * Filter of the given FilterTypeEnum.
	 */
	static public Serializable createFilterInputType(FilterTypeEnum filterType) {
		//TODO This mapping needs to be verified.
		if(filterType.equals(FilterTypeEnum.GSR_DETECTION_FILTER)) {
			return createGSRDetectionFilterInput();
		} else if(filterType.equals(FilterTypeEnum.QRS_FROM_ECG_FILTER)) {
			return createECGDetectionFilterInput();
		}
		return null;
	}
	
}
