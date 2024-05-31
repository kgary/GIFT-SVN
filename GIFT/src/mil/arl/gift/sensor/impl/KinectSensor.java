/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl;

import generated.sensor.BooleanEnum;
import generated.sensor.KinectColorResolutionEnum;
import generated.sensor.KinectDepthResolutionEnum;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Point3d;
import mil.arl.gift.common.ImageData;
import mil.arl.gift.common.enums.ImageFormatEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.ImageValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.sensor.SensorData;

import org.jnect.bodymodel.Body;
import org.jnect.bodymodel.PositionedElement;
import org.jnect.core.ColorFrameData;
import org.jnect.core.ColorFrameFormat;
import org.jnect.core.DepthFrameFormat;
import org.jnect.core.FrameData;
import org.jnect.core.FrameListener;
import org.jnect.core.KinectManager;
import org.jnect.core.impl.FaceFeaturesEnum;
import org.jnect.core.impl.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sensor implementation for collecting data from the Microsoft Kinect sensor.
 * 
 * @author jleonard
 */
public class KinectSensor extends AbstractSensor implements FrameListener {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KinectSensor.class);
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.CENTER_HIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.CENTER_SHOULDER, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_ANKLE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_ELBOW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_FOOT, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_HAND, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_HIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_KNEE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_SHOULDER, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_WRIST, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_ANKLE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_ELBOW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_FOOT, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_HAND, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_HIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_KNEE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_SHOULDER, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_WRIST, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.SPINE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.TOP_RIGHT_FOREHEAD, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_TOP_DIP_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_CHIN, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.BOTTOM_OF_CHIN, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_OF_RIGHT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_TOP_OF_RIGHT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_OF_RIGHT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_BOTTOM_OF_RIGHT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_MID_UPPER_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTER_CORNER_OF_RIGHT_EYE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_TOP_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_BOTTOM_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.INNER_CORNER_RIGHT_EYE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.UNDER_MID_BOTTOM_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_SIDE_OF_CHIN, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTSIDE_RIGHT_CORNER_MOUTH, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_OF_CHIN, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_TOP_DIP_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.TOP_LEFT_FOREHEAD, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_TOP_LOWER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_BOTTOM_LOWER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_OF_LEFT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_TOP_OF_LEFT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_OF_LEFT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_BOTTOM_OF_LEFT_EYEBROW, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_MID_UPPER_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTER_CORNER_OF_LEFT_EYE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_TOP_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_BOTTOM_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.INNER_CORNER_LEFT_EYE, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.UNDER_MID_BOTTOM_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_SIDE_OF_CHEEK, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTSIDE_LEFT_CORNER_MOUTH, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_OF_CHIN, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_TOP_DIP_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTER_TOP_RIGHT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTER_BOTTOM_RIGHT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTER_TOP_LEFT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.OUTER_BOTTOM_LEFT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.INNER_TOP_RIGHT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_MID_UPPER_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.INNER_BOTTOM_RIGHT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.INNER_TOP_LEFT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.INNER_BOTTOM_LEFT_PUPIL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_TOP_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_TOP_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_BOTTOM_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_BOTTOM_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_TOP_LOWER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_TOP_LOWER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_BOTTOM_LOWER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_BOTTOM_LOWER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MIDDLE_BOTTOM_UPPER_LIP, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LEFT_CORNER_MOUTH, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.RIGHT_CORNER_MOUTH, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.BOTTOM_OF_RIGHT_CHEEK, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.BOTTOM_OF_LEFT_CHEEK, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_THREE_FOURTH_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_THREE_FOURTH_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.THREE_FOURTH_TOP_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.THREE_FOURTH_TOP_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.THREE_FOURTH_BOTTOM_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.THREE_FOURTH_BOTTOM_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.BELOW_THREE_FOURTH_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.BELOW_THREE_FOURTH_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_ONE_FOURTH_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ABOVE_ONE_FOURTH_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ONE_FOURTH_TOP_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ONE_FOURTH_TOP_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ONE_FOURTH_BOTTOM_RIGHT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ONE_FOURTH_BOTTOM_LEFT_EYELID, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.TOP_SKULL, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.HEAD, Tuple3dValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.COLOR_CHANNEL, ImageValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.DEPTH_CHANNEL, ImageValue.class));        
    }

    private boolean nearMode = false;

    /**
     * The format of the color frame that comes from JNECT
     */
    private ColorFrameFormat colorFrameFormat = null;

    /**
     * The format of the depth frame that comes from JNECT
     */
    private DepthFrameFormat depthFrameFormat = null;

    /**
     * How often to query for skeleton and face data
     */
    private Double faceTrackingSkeletonInterval = null;

    /**
     * The last time skeleton and face data was collected
     */
    private long lastFaceTrackingSkeletonSendTime = 0;

    /**
     * How often to query for color data
     */
    private Double colorSampleInterval = null;

    /**
     * The last time color data was collected
     */
    private long lastColorFrameSendTime = 0;

    /**
     * How often to query for depth data
     */
    private Double depthInterval = null;

    /**
     * The last time depth data was collected
     */
    private long lastDepthSendTime = 0;

    /**
     * If the sensor has detected a skeleton
     */
    private boolean hasSkeleton = false;

    //load FaceTracking dll
    static {
        System.loadLibrary("Microsoft.Kinect.Toolkit");
        System.loadLibrary("Microsoft.Kinect.Toolkit.FaceTracking");
        System.loadLibrary("FaceTrackData");
        System.loadLibrary("FaceTrackLib");
    }
    private final KinectManager kinectManager = KinectManager.INSTANCE;

    /**
     * Configure using the default configuration.
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public KinectSensor(String sensorName) {
        super(sensorName, SensorTypeEnum.KINECT);        
        setEventProducerInformation(eventProducerInformation);
        sensorState = SensorStateEnum.READY;
    }

    /**
     * Class constructor - configure using the sensor configuration input for
     * this sensor
     *
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration parameters to configure this sensor
     */
    public KinectSensor(String sensorName, generated.sensor.KinectSensor configuration) {
        this(sensorName);

        if (configuration.getNearMode() != null) {

            nearMode = configuration.getNearMode() == BooleanEnum.TRUE;
        }

        if (configuration.getColorFrameFormat() != null) {

            if (configuration.getColorFrameFormat() == KinectColorResolutionEnum.RESOLUTION_INFRARED_640_X_480_FPS_30) {

                colorFrameFormat = ColorFrameFormat.RESOLUTION_INFRARED_640_X_480_FPS_30;

            } else if (configuration.getColorFrameFormat() == KinectColorResolutionEnum.RESOLUTION_RAW_BAYER_1280_X_960_FPS_12) {

                colorFrameFormat = ColorFrameFormat.RESOLUTION_RAW_BAYER_1280_X_960_FPS_12;

            } else if (configuration.getColorFrameFormat() == KinectColorResolutionEnum.RESOLUTION_RAW_BAYER_640_X_480_FPS_30) {

                colorFrameFormat = ColorFrameFormat.RESOLUTION_RAW_BAYER_640_X_480_FPS_30;

            } else if (configuration.getColorFrameFormat() == KinectColorResolutionEnum.RESOLUTION_RAW_YUV_640_X_480_FPS_15) {

                colorFrameFormat = ColorFrameFormat.RESOLUTION_RAW_YUV_640_X_480_FPS_15;

            } else if (configuration.getColorFrameFormat() == KinectColorResolutionEnum.RESOLUTION_RGB_1280_X_960_FPS_12) {

                colorFrameFormat = ColorFrameFormat.RESOLUTION_RGB_1280_X_960_FPS_12;

            } else if (configuration.getColorFrameFormat() == KinectColorResolutionEnum.RESOLUTION_RGB_640_X_480_FPS_30) {

                colorFrameFormat = ColorFrameFormat.RESOLUTION_RGB_640_X_480_FPS_30;

            } else if (configuration.getColorFrameFormat() == KinectColorResolutionEnum.RESOLUTION_YUV_640_X_480_FPS_15) {

                colorFrameFormat = ColorFrameFormat.RESOLUTION_YUV_640_X_480_FPS_15;
            }
        }

        if (configuration.getDepthFrameFormat() != null) {

            if (configuration.getDepthFrameFormat() == KinectDepthResolutionEnum.RESOLUTION_640_X_480) {

                depthFrameFormat = DepthFrameFormat.RESOLUTION_640_X_480;

            } else if (configuration.getDepthFrameFormat() == KinectDepthResolutionEnum.RESOLUTION_320_X_240) {

                depthFrameFormat = DepthFrameFormat.RESOLUTION_320_X_240;

            } else if (configuration.getDepthFrameFormat() == KinectDepthResolutionEnum.RESOLUTION_80_X_60) {

                depthFrameFormat = DepthFrameFormat.RESOLUTION_80_X_60;
            }
        }

        if (configuration.getSkeletonAndFaceTrackingSampleInterval() != null) {

            faceTrackingSkeletonInterval = configuration.getSkeletonAndFaceTrackingSampleInterval().doubleValue() * 1000.0;
        }

        if (configuration.getColorSampleInterval() != null) {

            colorSampleInterval = configuration.getColorSampleInterval().doubleValue() * 1000.0;
        }

        if (configuration.getDepthSampleInterval() != null) {

            depthInterval = configuration.getDepthSampleInterval().doubleValue() * 1000.0;
        }
    }
    
    @Override
    public boolean test() {
        
        try {

            kinectManager.startKinect(nearMode, colorFrameFormat, depthFrameFormat);

            kinectManager.startSkeletonTracking();

            kinectManager.stopSkeletonTracking();

            kinectManager.stopKinect();

            return true;

        } catch (RuntimeException e) {
        	
        	createSensorError("There was a problem testing the kinect sensor. Refer to the latest sensor "
        			+ "module log file in GIFT\\\\output\\\\logger\\\\module\\\\ for more information.");
        	logger.error("An error occurred while testing the Kinect sensor.", e);
        	e.printStackTrace();
        	
            return false;
        }
    }

    @Override
    public void start(long domainSessionStartTime) throws Exception {

        if (sensorState == SensorStateEnum.READY) {

            super.start(domainSessionStartTime);

            kinectManager.addFrameListener(this);

            kinectManager.startKinect(nearMode, colorFrameFormat, depthFrameFormat);
            kinectManager.startSkeletonTracking();

            sensorState = SensorStateEnum.RUNNING;

        } else if (sensorState == SensorStateEnum.STOPPED) {

            throw new IllegalStateException("Could not start Kinect sensor, sensor is not ready");

        } else if (sensorState == SensorStateEnum.RUNNING) {

            throw new IllegalStateException("Could not start Kinect sensor, sensor is already running");
        }
    }

    @Override
    public void stop() {
        
        super.stop();
        
        if(sensorState == SensorStateEnum.RUNNING) {

            sensorState = SensorStateEnum.STOPPED;

            kinectManager.removeFrameListener(this);

            kinectManager.stopSkeletonTracking();
            kinectManager.stopKinect();

            logger.info("Kinect Sensor stopped");
            
            sensorState = SensorStateEnum.READY;
        }
    }

    /**
     * Adds a skeleton element to the sensor data map if there is a valid position
     * 
     * @param sensorAttributeToValue Sensor data map to add to
     * @param sensorEnum The enum associated with the position
     * @param positionElement The skeleton position
     */
    private void addSkeletonElement(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue, SensorAttributeNameEnum sensorEnum, PositionedElement position) {
        
        // JNECT's PositionedElement has zeroes when there is no position, we need to ignore those values
        // In the Kinect coordinate space, (0, 0, 0) is the camera, so it is impossible to get this value during use
        if(position != null && position.getX() != 0 && position.getY() != 0 && position.getZ() != 0) {
            
            sensorAttributeToValue.put(sensorEnum, new Tuple3dValue(sensorEnum, new Point3d(position.getX(), position.getY(), position.getZ())));
        }
    }
    
    /**
     * Adds a face feature element to the sensor data map if there is a valid position
     * 
     * @param sensorAttributeToValue Sensor data map to add to
     * @param sensorEnum The enum associated with the position
     * @param positionElement The face feature position
     */
    private void addFaceFeatureElement(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue, SensorAttributeNameEnum sensorEnum, Position position) {

        // JNECT's PositionedElement has zeroes when there is no position, we need to ignore those values
        // In the Kinect coordinate space, (0, 0, 0) is the camera, so it is impossible to get this value during use
        if (position != null && position.getX() != 0 && position.getY() != 0 && position.getZ() != 0) {

            sensorAttributeToValue.put(sensorEnum, new Tuple3dValue(sensorEnum, new Point3d(position.getX(), position.getY(), position.getZ())));
        }
    }

    /**
     * Checks to see if the sensor is ready to collect skeleton and face data
     * 
     * @param timeNow The timestamp of the data
     * @return boolean If the sensor is ready
     */
    private boolean isReadyForSkeletonAndFaceData(long timeNow) {
        
        long timeElapsed = timeNow - lastFaceTrackingSkeletonSendTime;

        if (faceTrackingSkeletonInterval != null) {

            return timeElapsed >= faceTrackingSkeletonInterval;

        } else {

            long interval = getSensorInterval() != null ? (long) (getSensorInterval() * 1000.0) : 0;

            return timeElapsed >= interval;

        }
    }
    
    /**
     * Checks to see if the sensor is ready to collect color data
     * 
     * @param timeNow The timestamp of the data
     * @return boolean If the sensor is ready
     */
    private boolean isReadyForColorFrame(long timeNow) {
        
        long timeElapsed = timeNow - lastColorFrameSendTime;

        if (colorSampleInterval != null) {

            return timeElapsed >= colorSampleInterval;

        } else {

            long interval = getSensorInterval() != null ? (long) (getSensorInterval() * 1000.0) : 0;

            return timeElapsed >= interval;
        }
    }

    /**
     * Checks to see if the sensor is ready to collect depth data
     *
     * @param timeNow The timestamp of the data
     * @return boolean If the sensor is ready
     */
    private boolean isReadyForDepthFrame(long timeNow) {
        
        long timeElapsed = timeNow - lastDepthSendTime;

        if (depthInterval != null) {

            return timeElapsed >= depthInterval;

        } else {

            long interval = getSensorInterval() != null ? (long) (getSensorInterval() * 1000.0) : 0;

            return timeElapsed >= interval;
        }
    }

    /**
     * Callback for when the Kinect has skeleton data
     *
     * Note: This gets called every Kinect frame even when there is no skeleton
     * data
     *
     * @param timeNow The timestamp of the data
     * @param body The tracked body
     * @param faceFeatures The tracked face
     */
    @Override
    public void onSkeletonDataAvailable(long timeNow, Body body, Map<FaceFeaturesEnum, Position> faceFeatures) {

        if (getSensorState() == SensorStateEnum.RUNNING) {

            if (isReadyForSkeletonAndFaceData(timeNow)) {
                
                Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();

                long elapsedTime = timeNow - getDomainSessionStartTime();
                
                if (body != null) {

                    // If the positions are valid, add the body parts to the sensor data map
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.CENTER_HIP, body.getCenterHip());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.CENTER_SHOULDER, body.getCenterShoulder());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_ANKLE, body.getLeftAnkle());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_ELBOW, body.getLeftElbow());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_FOOT, body.getLeftFoot());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_HAND, body.getLeftHand());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_HIP, body.getLeftHip());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_KNEE, body.getLeftKnee());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_SHOULDER, body.getLeftShoulder());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_WRIST, body.getLeftWrist());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_ANKLE, body.getRightAnkle());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_ELBOW, body.getRightElbow());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_FOOT, body.getRightFoot());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_HAND, body.getRightHand());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_HIP, body.getRightHip());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_KNEE, body.getRightKnee());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_SHOULDER, body.getRightShoulder());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_WRIST, body.getRightWrist());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.SPINE, body.getSpine());
                    addSkeletonElement(sensorAttributeToValue, SensorAttributeNameEnum.HEAD, body.getHead());

                    if (!hasSkeleton && !sensorAttributeToValue.isEmpty()) {

                        createSensorStatus("Kinect sensor detected a skeleton and face");
                        hasSkeleton = true;
                    }
                }

                if (hasSkeleton && sensorAttributeToValue.isEmpty()) {

                    createSensorStatus("Kinect sensor no longer detects a skeleton and face");
                    hasSkeleton = false;
                }

                if (faceFeatures != null) {

                    // If the positions are valid, add the facial features to the sensor data map
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.TOP_SKULL, faceFeatures.get(FaceFeaturesEnum.TOP_SKULL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.TOP_RIGHT_FOREHEAD, faceFeatures.get(FaceFeaturesEnum.TOP_RIGHT_FOREHEAD));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_TOP_DIP_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.MIDDLE_TOP_DIP_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ABOVE_CHIN, faceFeatures.get(FaceFeaturesEnum.ABOVE_CHIN));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.BOTTOM_OF_CHIN, faceFeatures.get(FaceFeaturesEnum.BOTTOM_OF_CHIN));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_OF_RIGHT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.RIGHT_OF_RIGHT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_TOP_OF_RIGHT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.MIDDLE_TOP_OF_RIGHT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_OF_RIGHT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.LEFT_OF_RIGHT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_BOTTOM_OF_RIGHT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.MIDDLE_BOTTOM_OF_RIGHT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ABOVE_MID_UPPER_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.ABOVE_MID_UPPER_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTER_CORNER_OF_RIGHT_EYE, faceFeatures.get(FaceFeaturesEnum.OUTER_CORNER_OF_RIGHT_EYE));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_TOP_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.MIDDLE_TOP_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_BOTTOM_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.MIDDLE_BOTTOM_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.INNER_CORNER_RIGHT_EYE, faceFeatures.get(FaceFeaturesEnum.INNER_CORNER_RIGHT_EYE));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.UNDER_MID_BOTTOM_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.UNDER_MID_BOTTOM_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_SIDE_OF_CHIN, faceFeatures.get(FaceFeaturesEnum.RIGHT_SIDE_OF_CHIN));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTSIDE_RIGHT_CORNER_MOUTH, faceFeatures.get(FaceFeaturesEnum.OUTSIDE_RIGHT_CORNER_MOUTH));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_OF_CHIN, faceFeatures.get(FaceFeaturesEnum.RIGHT_OF_CHIN));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_TOP_DIP_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.RIGHT_TOP_DIP_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.TOP_LEFT_FOREHEAD, faceFeatures.get(FaceFeaturesEnum.TOP_LEFT_FOREHEAD));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_TOP_LOWER_LIP, faceFeatures.get(FaceFeaturesEnum.MIDDLE_TOP_LOWER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_BOTTOM_LOWER_LIP, faceFeatures.get(FaceFeaturesEnum.MIDDLE_BOTTOM_LOWER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_OF_LEFT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.LEFT_OF_LEFT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_TOP_OF_LEFT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.MIDDLE_TOP_OF_LEFT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_OF_LEFT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.RIGHT_OF_LEFT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_BOTTOM_OF_LEFT_EYEBROW, faceFeatures.get(FaceFeaturesEnum.MIDDLE_BOTTOM_OF_LEFT_EYEBROW));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ABOVE_MID_UPPER_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.ABOVE_MID_UPPER_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTER_CORNER_OF_LEFT_EYE, faceFeatures.get(FaceFeaturesEnum.OUTER_CORNER_OF_LEFT_EYE));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_TOP_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.MIDDLE_TOP_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_BOTTOM_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.MIDDLE_BOTTOM_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.INNER_CORNER_LEFT_EYE, faceFeatures.get(FaceFeaturesEnum.INNER_CORNER_LEFT_EYE));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.UNDER_MID_BOTTOM_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.UNDER_MID_BOTTOM_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_SIDE_OF_CHEEK, faceFeatures.get(FaceFeaturesEnum.LEFT_SIDE_OF_CHEEK));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTSIDE_LEFT_CORNER_MOUTH, faceFeatures.get(FaceFeaturesEnum.OUTSIDE_LEFT_CORNER_MOUTH));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_OF_CHIN, faceFeatures.get(FaceFeaturesEnum.LEFT_OF_CHIN));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_TOP_DIP_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.LEFT_TOP_DIP_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTER_TOP_RIGHT_PUPIL, faceFeatures.get(FaceFeaturesEnum.OUTER_TOP_RIGHT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTER_BOTTOM_RIGHT_PUPIL, faceFeatures.get(FaceFeaturesEnum.OUTER_BOTTOM_RIGHT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTER_TOP_LEFT_PUPIL, faceFeatures.get(FaceFeaturesEnum.OUTER_TOP_LEFT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.OUTER_BOTTOM_LEFT_PUPIL, faceFeatures.get(FaceFeaturesEnum.OUTER_BOTTOM_LEFT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.INNER_TOP_RIGHT_PUPIL, faceFeatures.get(FaceFeaturesEnum.INNER_TOP_RIGHT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.INNER_BOTTOM_RIGHT_PUPIL, faceFeatures.get(FaceFeaturesEnum.INNER_BOTTOM_RIGHT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.INNER_TOP_LEFT_PUPIL, faceFeatures.get(FaceFeaturesEnum.INNER_TOP_LEFT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.INNER_BOTTOM_LEFT_PUPIL, faceFeatures.get(FaceFeaturesEnum.INNER_BOTTOM_LEFT_PUPIL));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_TOP_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.RIGHT_TOP_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_TOP_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.LEFT_TOP_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_BOTTOM_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.RIGHT_BOTTOM_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_BOTTOM_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.LEFT_BOTTOM_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_TOP_LOWER_LIP, faceFeatures.get(FaceFeaturesEnum.RIGHT_TOP_LOWER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_TOP_LOWER_LIP, faceFeatures.get(FaceFeaturesEnum.LEFT_TOP_LOWER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_BOTTOM_LOWER_LIP, faceFeatures.get(FaceFeaturesEnum.RIGHT_BOTTOM_LOWER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_BOTTOM_LOWER_LIP, faceFeatures.get(FaceFeaturesEnum.LEFT_BOTTOM_LOWER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.MIDDLE_BOTTOM_UPPER_LIP, faceFeatures.get(FaceFeaturesEnum.MIDDLE_BOTTOM_UPPER_LIP));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.LEFT_CORNER_MOUTH, faceFeatures.get(FaceFeaturesEnum.LEFT_CORNER_MOUTH));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.RIGHT_CORNER_MOUTH, faceFeatures.get(FaceFeaturesEnum.RIGHT_CORNER_MOUTH));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.BOTTOM_OF_RIGHT_CHEEK, faceFeatures.get(FaceFeaturesEnum.BOTTOM_OF_RIGHT_CHEEK));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.BOTTOM_OF_LEFT_CHEEK, faceFeatures.get(FaceFeaturesEnum.BOTTOM_OF_LEFT_CHEEK));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ABOVE_THREE_FOURTH_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.ABOVE_THREE_FOURTH_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ABOVE_THREE_FOURTH_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.ABOVE_THREE_FOURTH_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.THREE_FOURTH_TOP_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.THREE_FOURTH_TOP_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.THREE_FOURTH_TOP_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.THREE_FOURTH_TOP_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.THREE_FOURTH_BOTTOM_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.THREE_FOURTH_BOTTOM_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.THREE_FOURTH_BOTTOM_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.THREE_FOURTH_BOTTOM_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.BELOW_THREE_FOURTH_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.BELOW_THREE_FOURTH_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.BELOW_THREE_FOURTH_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.BELOW_THREE_FOURTH_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ABOVE_ONE_FOURTH_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.ABOVE_ONE_FOURTH_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ABOVE_ONE_FOURTH_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.ABOVE_ONE_FOURTH_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ONE_FOURTH_TOP_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.ONE_FOURTH_TOP_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ONE_FOURTH_TOP_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.ONE_FOURTH_TOP_LEFT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ONE_FOURTH_BOTTOM_RIGHT_EYELID, faceFeatures.get(FaceFeaturesEnum.ONE_FOURTH_BOTTOM_RIGHT_EYELID));
                    addFaceFeatureElement(sensorAttributeToValue, SensorAttributeNameEnum.ONE_FOURTH_BOTTOM_LEFT_EYELID, faceFeatures.get(FaceFeaturesEnum.ONE_FOURTH_BOTTOM_LEFT_EYELID));
                }

                if (!sensorAttributeToValue.isEmpty()) {

                    lastFaceTrackingSkeletonSendTime = timeNow;

                    SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
                    sendDataEvent(data);
                }
            }
        }
    }

    /**
     * Callback when there is color data
     * 
     * @param timeNow The timestamp of the data
     * @param frameData The frame data
     */
    @Override
    public void onColorFrameAvailable(long timeNow, FrameData frameData) {

        if (getSensorState() == SensorStateEnum.RUNNING) {

            if (isReadyForColorFrame(timeNow)) {

                Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();

                long elapsedTime = timeNow - getDomainSessionStartTime();

                if (frameData != null) {

                    ColorFrameData colorFrame = (ColorFrameData) frameData;

                    // Convert the Kinect format to a generic image format
                    ImageFormatEnum format = null;

                    if (colorFrame.getColorFormat() == ColorFrameFormat.RESOLUTION_INFRARED_640_X_480_FPS_30) {

                        format = ImageFormatEnum.KINECT_INFRARED;

                    } else if (colorFrame.getColorFormat() == ColorFrameFormat.RESOLUTION_RAW_BAYER_1280_X_960_FPS_12) {

                        format = ImageFormatEnum.BAYER_GRGB;

                    } else if (colorFrame.getColorFormat() == ColorFrameFormat.RESOLUTION_RAW_BAYER_640_X_480_FPS_30) {

                        format = ImageFormatEnum.BAYER_GRGB;

                    } else if (colorFrame.getColorFormat() == ColorFrameFormat.RESOLUTION_RAW_YUV_640_X_480_FPS_15) {

                        format = ImageFormatEnum.YUV_422_UYVY;

                    } else if (colorFrame.getColorFormat() == ColorFrameFormat.RESOLUTION_RGB_1280_X_960_FPS_12) {

                        format = ImageFormatEnum.RGB_8_BGRA;

                    } else if (colorFrame.getColorFormat() == ColorFrameFormat.RESOLUTION_RGB_640_X_480_FPS_30) {

                        format = ImageFormatEnum.RGB_8_BGRA;

                    } else if (colorFrame.getColorFormat() == ColorFrameFormat.RESOLUTION_YUV_640_X_480_FPS_15) {

                        format = ImageFormatEnum.YUV_422_UYVY;
                    }

                    sensorAttributeToValue.put(SensorAttributeNameEnum.COLOR_CHANNEL, new ImageValue(SensorAttributeNameEnum.COLOR_CHANNEL, new ImageData(frameData.getData(), frameData.getWidth(), frameData.getHeight(), format)));

                    lastColorFrameSendTime = timeNow;

                    SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
                    sendDataEvent(data);
                }
            }
        }
    }

    /**
     * Callback when there is depth data
     *
     * @param timeNow The timestamp of the data
     * @param frameData The frame data
     */
    @Override
    public void onDepthFrameAvailable(long timeNow, FrameData frameData) {


        if (getSensorState() == SensorStateEnum.RUNNING) {

            if (isReadyForDepthFrame(timeNow)) {

                Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sensorAttributeToValue = new HashMap<>();

                long elapsedTime = timeNow - getDomainSessionStartTime();

                if (frameData != null) {

                    sensorAttributeToValue.put(SensorAttributeNameEnum.DEPTH_CHANNEL, new ImageValue(SensorAttributeNameEnum.DEPTH_CHANNEL, new ImageData(frameData.getData(), frameData.getWidth(), frameData.getHeight(), ImageFormatEnum.KINECT_DEPTH)));

                    lastDepthSendTime = timeNow;

                    SensorData data = new SensorData(sensorAttributeToValue, elapsedTime);
                    sendDataEvent(data);
                }
            }
        }
    }
}
