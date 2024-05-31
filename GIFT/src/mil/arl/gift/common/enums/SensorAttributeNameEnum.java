/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.Tuple3dValue;

/**
 * Enumeration of the various Sensor Attribute names
 * 
 * @author mhoffman
 *
 */
public class SensorAttributeNameEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<SensorAttributeNameEnum> enumList = new ArrayList<SensorAttributeNameEnum>();
    private static int index = 0;

    public static final SensorAttributeNameEnum TIME = new SensorAttributeNameEnum("Time", "Time");
    public static final SensorAttributeNameEnum TEMPERATURE = new SensorAttributeNameEnum("Temperature", "Temperature");
    public static final SensorAttributeNameEnum HUMIDITY = new SensorAttributeNameEnum("Humidity", "Humidity");
    public static final SensorAttributeNameEnum AROUSAL = new SensorAttributeNameEnum("Arousal", "Arousal");
    public static final SensorAttributeNameEnum ACCELERATION3D = new SensorAttributeNameEnum("Acceleration3d", "Acceleration 3d", Tuple3dValue.class);
    public static final SensorAttributeNameEnum ACCELERATION_X = new SensorAttributeNameEnum("AccelerationX", "AccelerationX");
    public static final SensorAttributeNameEnum ACCELERATION_Y = new SensorAttributeNameEnum("AccelerationY", "AccelerationY");
    public static final SensorAttributeNameEnum ACCELERATION_Z = new SensorAttributeNameEnum("AccelerationZ", "AccelerationZ");
    
    public static final SensorAttributeNameEnum EDA = new SensorAttributeNameEnum("EDA", "Electrodermal Activity");
    public static final SensorAttributeNameEnum MOTIVATION = new SensorAttributeNameEnum("Motivation", "Motiviation");
    public static final SensorAttributeNameEnum EXPERTISE = new SensorAttributeNameEnum("Expertise", "Expertise");
    
    //
    // ECG
    //
    public static final SensorAttributeNameEnum ECG = new SensorAttributeNameEnum("ECG", "ECG");
    public static final SensorAttributeNameEnum ECG_NUM_HB = new SensorAttributeNameEnum("ECGNumberHeartbeats", "ECG Number of Heartbeats");
    public static final SensorAttributeNameEnum ECG_INIT_INTERBEAT_INTERVAL = new SensorAttributeNameEnum("ECGInitInterbeatInterval", "ECG Initial Interbeat Interval");
    public static final SensorAttributeNameEnum ECG_FINAL_INTERBEAT_INTERVAL = new SensorAttributeNameEnum("ECGFinalInterbeatInterval", "ECG Final Interbeat Interval");
    public static final SensorAttributeNameEnum ECG_AVG_IBI = new SensorAttributeNameEnum("ECGAVGIBI", "ECG Avg IBI");
    
    public static final SensorAttributeNameEnum GSR = new SensorAttributeNameEnum("GSR", "GSR");
    public static final SensorAttributeNameEnum GSR_RESISTANCE = new SensorAttributeNameEnum("GSRResistance", "GSR - Resistance");
    public static final SensorAttributeNameEnum GSR_CONDUCTANCE = new SensorAttributeNameEnum("GSRConductance", "GSR - Conductance");
    public static final SensorAttributeNameEnum GSR_MEAN = new SensorAttributeNameEnum("GSRMean", "GSR Mean");
    public static final SensorAttributeNameEnum GSR_STD = new SensorAttributeNameEnum("GSRStd", "GSR Standard Deviation");
    public static final SensorAttributeNameEnum GSR_FEATURE = new SensorAttributeNameEnum("GSRFeature", "GSR Feature");
    
    //
    // Emotiv 
    //
    
    // Affective suite
    public static final SensorAttributeNameEnum LT_EXCITEMENT = new SensorAttributeNameEnum("LongTermExcitement", "Long Term Excitement");
    public static final SensorAttributeNameEnum ST_EXCITEMENT = new SensorAttributeNameEnum("ShortTermExcitement", "Short Term Excitement");
    public static final SensorAttributeNameEnum MEDITATION = new SensorAttributeNameEnum("Meditation", "Meditation");
    public static final SensorAttributeNameEnum FRUSTRATION = new SensorAttributeNameEnum("Frustration", "Frustration");
    public static final SensorAttributeNameEnum ENGAGEMENT = new SensorAttributeNameEnum("Engagement", "Engagement");
    
    // EEG
    public static final SensorAttributeNameEnum ED_COUNTER = new SensorAttributeNameEnum("ED_COUNTER", "ED_COUNTER");
    public static final SensorAttributeNameEnum ED_INTERPOLATED = new SensorAttributeNameEnum("ED_INTERPOLATED", "ED_INTERPOLATED");
    public static final SensorAttributeNameEnum ED_RAW_CQ = new SensorAttributeNameEnum("ED_RAW_CQ", "ED_RAW_CQ");
    public static final SensorAttributeNameEnum ED_AF3 = new SensorAttributeNameEnum("ED_AF3", "ED_AF3");
    public static final SensorAttributeNameEnum EE_CHAN_F7 = new SensorAttributeNameEnum("EE_CHAN_F7", "EE_CHAN_F7");
    public static final SensorAttributeNameEnum EE_CHAN_F3 = new SensorAttributeNameEnum("EE_CHAN_F3", "EE_CHAN_F3");
    public static final SensorAttributeNameEnum EE_CHAN_FC5 = new SensorAttributeNameEnum("EE_CHAN_FC5", "EE_CHAN_FC5");
    public static final SensorAttributeNameEnum EE_CHAN_T7 = new SensorAttributeNameEnum("EE_CHAN_T7", "EE_CHAN_T7");
    public static final SensorAttributeNameEnum EE_CHAN_P7 = new SensorAttributeNameEnum("EE_CHAN_P7", "EE_CHAN_P7");
    public static final SensorAttributeNameEnum EE_CHAN_O1 = new SensorAttributeNameEnum("EE_CHAN_O1", "EE_CHAN_O1");
    public static final SensorAttributeNameEnum EE_CHAN_O2 = new SensorAttributeNameEnum("EE_CHAN_O2", "EE_CHAN_O2");
    public static final SensorAttributeNameEnum EE_CHAN_P8 = new SensorAttributeNameEnum("EE_CHAN_P8", "EE_CHAN_P8");
    public static final SensorAttributeNameEnum EE_CHAN_T8 = new SensorAttributeNameEnum("EE_CHAN_T8", "EE_CHAN_T8");
    public static final SensorAttributeNameEnum EE_CHAN_FC6 = new SensorAttributeNameEnum("EE_CHAN_FC6", "EE_CHAN_FC6");
    public static final SensorAttributeNameEnum EE_CHAN_F4 = new SensorAttributeNameEnum("EE_CHAN_F4", "EE_CHAN_F4");
    public static final SensorAttributeNameEnum EE_CHAN_F8 = new SensorAttributeNameEnum("EE_CHAN_F8", "EE_CHAN_F8");
    public static final SensorAttributeNameEnum ED_AF4 = new SensorAttributeNameEnum("ED_AF4", "ED_AF4");
    public static final SensorAttributeNameEnum ED_GYROX = new SensorAttributeNameEnum("ED_GYROX", "ED_GYROX");
    public static final SensorAttributeNameEnum ED_GYROY = new SensorAttributeNameEnum("ED_GYROY", "ED_GYROY");
    public static final SensorAttributeNameEnum ED_TIMESTAMP = new SensorAttributeNameEnum("ED_TIMESTAMP", "ED_TIMESTAMP");
    public static final SensorAttributeNameEnum ED_ES_TIMESTAMP = new SensorAttributeNameEnum("ED_ES_TIMESTAMP", "ED_ES_TIMESTAMP");
    public static final SensorAttributeNameEnum ED_FUNC_ID = new SensorAttributeNameEnum("ED_FUNC_ID", "ED_FUNC_ID");
    public static final SensorAttributeNameEnum ED_FUNC_VALUE = new SensorAttributeNameEnum("ED_FUNC_VALUE", "ED_FUNC_VALUE");
    public static final SensorAttributeNameEnum ED_MARKER = new SensorAttributeNameEnum("ED_MARKER", "ED_MARKER");
    public static final SensorAttributeNameEnum ED_SYNC_SIGNAL = new SensorAttributeNameEnum("ED_SYNC_SIGNAL", "ED_SYNC_SIGNAL");

    //
    // GAVAM
    //
    public static final SensorAttributeNameEnum HEAD_POSE = new SensorAttributeNameEnum("HEAD_POSE", "HEAD_POSE");
    public static final SensorAttributeNameEnum HEAD_POSE_ROT = new SensorAttributeNameEnum("HEAD_POSE_ROT_X", "HEAD_POSE_ROT_X");
    
    //
    // VHT Multisense
    //    
    public static final SensorAttributeNameEnum POSTURE_TYPE = new SensorAttributeNameEnum("PostureType", "Posture Type");    
    public static final SensorAttributeNameEnum ACTIVITY_TYPE = new SensorAttributeNameEnum("ActivityType", "Activity Type");    
    public static final SensorAttributeNameEnum ATTENTION_TYPE = new SensorAttributeNameEnum("AttentionType", "Attention Type");
    public static final SensorAttributeNameEnum ATTENTION_VALUE = new SensorAttributeNameEnum("AttentionValue", "Attention Value");    
    public static final SensorAttributeNameEnum ENGAGEMENT_TYPE = new SensorAttributeNameEnum("EngagementType", "Engagement Type");
    public static final SensorAttributeNameEnum ENGAGEMENT_VALUE = new SensorAttributeNameEnum("EngagementValue", "Engagement Value");    
    public static final SensorAttributeNameEnum HORIZONTAL_GAZE = new SensorAttributeNameEnum("HorizontalGaze", "Horizontal Gaze");
    public static final SensorAttributeNameEnum VERTICAL_GAZE = new SensorAttributeNameEnum("VerticalGaze", "Vertical Gaze");
    public static final SensorAttributeNameEnum GAZE_DIRECTION = new SensorAttributeNameEnum("GazeDirection", "Gaze Direction");    
    public static final SensorAttributeNameEnum LEFT_HAND_POSE = new SensorAttributeNameEnum("LEFT_HAND_POSE", "LEFT_HAND_POSE");    
    public static final SensorAttributeNameEnum LEFT_HAND_POSE_ROT = new SensorAttributeNameEnum("LEFT_HAND_POSE_ROT", "LEFT_HAND_POSE_ROT");    
    public static final SensorAttributeNameEnum LEFT_HAND_POSE_TYPE = new SensorAttributeNameEnum("LEFT_HAND_POSE_TYPE", "LEFT_HAND_POSE_TYPE");    
    public static final SensorAttributeNameEnum RIGHT_HAND_POSE = new SensorAttributeNameEnum("RIGHT_HAND_POSE", "RIGHT_HAND_POSE");    
    public static final SensorAttributeNameEnum RIGHT_HAND_POSE_ROT = new SensorAttributeNameEnum("RIGHT_HAND_POSE_ROT", "RIGHT_HAND_POSE_ROT");    
    public static final SensorAttributeNameEnum RIGHT_HAND_POSE_TYPE = new SensorAttributeNameEnum("RIGHT_HAND_POSE_TYPE", "RIGHT_HAND_POSE_TYPE");    

    // OS3D
    public static final SensorAttributeNameEnum OS3D_BITMASK = new SensorAttributeNameEnum("OS3D_BITMASK", "OS3D_BITMAASK");
    public static final SensorAttributeNameEnum OS3D_FRAME_COUNTER = new SensorAttributeNameEnum("OS3D_FRAME_COUNTER", "OS3D_FRAME_COUNTER");
    
    public static final SensorAttributeNameEnum OS3D_RAW_ACC = new SensorAttributeNameEnum("OS3D_RAW_ACC", "OS3D_RAW_ACC");
    public static final SensorAttributeNameEnum OS3D_RAW_GYR = new SensorAttributeNameEnum("OS3D_RAW_GYR", "OS3D_RAW_GYR");
    public static final SensorAttributeNameEnum OS3D_RAW_MAG = new SensorAttributeNameEnum("OS3D_RAW_MAG", "OS3D_RAW_MAG");
    public static final SensorAttributeNameEnum OS3D_RAW_TEMP = new SensorAttributeNameEnum("OS3D_RAW_TEMP", "OS3D_RAW_TEMP");
    
    public static final SensorAttributeNameEnum OS3D_CAL_ACC = new SensorAttributeNameEnum("OS3D_CAL_ACC", "OS3D_CAL_ACC");
    public static final SensorAttributeNameEnum OS3D_CAL_GYR = new SensorAttributeNameEnum("OS3D_CAL_GYR", "OS3D_CAL_GYR");
    public static final SensorAttributeNameEnum OS3D_CAL_MAG = new SensorAttributeNameEnum("OS3D_CAL_MAG", "OS3D_CAL_MAG");
    
    public static final SensorAttributeNameEnum QUAT_X = new SensorAttributeNameEnum("QUAT_X", "QUAT_X");
    public static final SensorAttributeNameEnum QUAT_Y = new SensorAttributeNameEnum("QUAT_Y", "QUAT_Y");
    public static final SensorAttributeNameEnum QUAT_Z = new SensorAttributeNameEnum("QUAT_Z", "QUAT_Z");
    public static final SensorAttributeNameEnum QUAT_W = new SensorAttributeNameEnum("QUAT_W", "QUAT_W");
    
    public static final SensorAttributeNameEnum QUAT_X_REL = new SensorAttributeNameEnum("QUAT_X_REL", "QUAT_X_REL");
    public static final SensorAttributeNameEnum QUAT_Y_REL = new SensorAttributeNameEnum("QUAT_Y_REL", "QUAT_Y_REL");
    public static final SensorAttributeNameEnum QUAT_Z_REL = new SensorAttributeNameEnum("QUAT_Z_REL", "QUAT_Z_REL");
    public static final SensorAttributeNameEnum QUAT_W_REL = new SensorAttributeNameEnum("QUAT_W_REL", "QUAT_W_REL");

    public static final SensorAttributeNameEnum DROPPED_PACKET_COUNT = new SensorAttributeNameEnum("DROPPED_PACKET_COUNT", "DROPPED_PACKET_COUNT");
    
    public static final SensorAttributeNameEnum ROLL_DEG  = new SensorAttributeNameEnum("ROLL_DEG", "ROLL_DEG");
    public static final SensorAttributeNameEnum PITCH_DEG = new SensorAttributeNameEnum("PITCH_DEG", "PITCH_DEG");
    public static final SensorAttributeNameEnum YAW_DEG   = new SensorAttributeNameEnum("YAW_DEG", "YAW_DEG");
    
    public static final SensorAttributeNameEnum ROLL_DEG_REL  = new SensorAttributeNameEnum("ROLL_DEG_REL", "ROLL_DEG_REL");
    public static final SensorAttributeNameEnum PITCH_DEG_REL = new SensorAttributeNameEnum("PITCH_DEG_REL", "PITCH_DEG_REL");
    public static final SensorAttributeNameEnum YAW_DEG_REL   = new SensorAttributeNameEnum("YAW_DEG_REL", "YAW_DEG_REL");
    
    
    
    
    // BioHarness 
    public static final SensorAttributeNameEnum HEART_RATE = new SensorAttributeNameEnum("HEART_RATE", "Heart Rate");
    public static final SensorAttributeNameEnum RESPIRATION_RATE = new SensorAttributeNameEnum("RESPIRATION_RATE", "Respiration Rate");
    public static final SensorAttributeNameEnum POSTURE = new SensorAttributeNameEnum("POSTURE", "Posture");   
    public static final SensorAttributeNameEnum BREATHING_WAVEFORM_SAMPLE = new SensorAttributeNameEnum("BREATHING_WAVEFORM_SAMPLE", "Breathing Waveform Sample");
    public static final SensorAttributeNameEnum ECG_WAVEFORM_SAMPLE = new SensorAttributeNameEnum("ECG_WAVEFORM_SAMPLE", "ECG Waveform Sample");
    
	//
    // Kinect
    //
    public static final SensorAttributeNameEnum HEAD = new SensorAttributeNameEnum("HEAD", "HEAD");
    public static final SensorAttributeNameEnum CENTER_HIP = new SensorAttributeNameEnum("CENTER_HIP", "CENTER_HIP");
    public static final SensorAttributeNameEnum CENTER_SHOULDER = new SensorAttributeNameEnum("CENTER_SHOULDER", "CENTER_SHOULDER");
    public static final SensorAttributeNameEnum LEFT_ANKLE = new SensorAttributeNameEnum("LEFT_ANKLE", "LEFT_ANKLE");
    public static final SensorAttributeNameEnum LEFT_ELBOW = new SensorAttributeNameEnum("LEFT_ELBOW", "LEFT_ELBOW");
    public static final SensorAttributeNameEnum LEFT_FOOT = new SensorAttributeNameEnum("LEFT_FOOT", "LEFT_FOOT");
    public static final SensorAttributeNameEnum LEFT_HAND = new SensorAttributeNameEnum("LEFT_HAND", "LEFT_HAND");
    public static final SensorAttributeNameEnum LEFT_HIP = new SensorAttributeNameEnum("LEFT_HIP", "LEFT_HIP");
    public static final SensorAttributeNameEnum LEFT_KNEE = new SensorAttributeNameEnum("LEFT_KNEE", "LEFT_KNEE");
    public static final SensorAttributeNameEnum LEFT_SHOULDER = new SensorAttributeNameEnum("LEFT_SHOULDER", "LEFT_SHOULDER");
    public static final SensorAttributeNameEnum LEFT_WRIST = new SensorAttributeNameEnum("LEFT_WRIST", "LEFT_WRIST");
    public static final SensorAttributeNameEnum RIGHT_ANKLE = new SensorAttributeNameEnum("RIGHT_ANKLE", "RIGHT_ANKLE");
    public static final SensorAttributeNameEnum RIGHT_ELBOW = new SensorAttributeNameEnum("RIGHT_ELBOW", "RIGHT_ELBOW");
    public static final SensorAttributeNameEnum RIGHT_FOOT = new SensorAttributeNameEnum("RIGHT_FOOT", "RIGHT_FOOT");
    public static final SensorAttributeNameEnum RIGHT_HAND = new SensorAttributeNameEnum("RIGHT_HAND", "RIGHT_HAND");
    public static final SensorAttributeNameEnum RIGHT_HIP = new SensorAttributeNameEnum("RIGHT_HIP", "RIGHT_HIP");
    public static final SensorAttributeNameEnum RIGHT_KNEE = new SensorAttributeNameEnum("RIGHT_KNEE", "RIGHT_KNEE");
    public static final SensorAttributeNameEnum RIGHT_SHOULDER = new SensorAttributeNameEnum("RIGHT_SHOULDER", "RIGHT_SHOULDER");
    public static final SensorAttributeNameEnum RIGHT_WRIST = new SensorAttributeNameEnum("RIGHT_WRIST", "RIGHT_WRIST");
    public static final SensorAttributeNameEnum SPINE = new SensorAttributeNameEnum("SPINE", "SPINE");
    public static final SensorAttributeNameEnum TOP_SKULL = new SensorAttributeNameEnum("TOP_SKULL", "TOP_SKULL");
    public static final SensorAttributeNameEnum TOP_RIGHT_FOREHEAD = new SensorAttributeNameEnum("TOP_RIGHT_FOREHEAD", "TOP_RIGHT_FOREHEAD");
    public static final SensorAttributeNameEnum MIDDLE_TOP_DIP_UPPER_LIP = new SensorAttributeNameEnum("MIDDLE_TOP_DIP_UPPER_LIP", "MIDDLE_TOP_DIP_UPPER_LIP");
    public static final SensorAttributeNameEnum ABOVE_CHIN = new SensorAttributeNameEnum("ABOVE_CHIN", "ABOVE_CHIN");
    public static final SensorAttributeNameEnum BOTTOM_OF_CHIN = new SensorAttributeNameEnum("BOTTOM_OF_CHIN", "BOTTOM_OF_CHIN");
    public static final SensorAttributeNameEnum RIGHT_OF_RIGHT_EYEBROW = new SensorAttributeNameEnum("RIGHT_OF_RIGHT_EYEBROW", "RIGHT_OF_RIGHT_EYEBROW");
    public static final SensorAttributeNameEnum MIDDLE_TOP_OF_RIGHT_EYEBROW = new SensorAttributeNameEnum("MIDDLE_TOP_OF_RIGHT_EYEBROW", "MIDDLE_TOP_OF_RIGHT_EYEBROW");
    public static final SensorAttributeNameEnum LEFT_OF_RIGHT_EYEBROW = new SensorAttributeNameEnum("LEFT_OF_RIGHT_EYEBROW", "LEFT_OF_RIGHT_EYEBROW");
    public static final SensorAttributeNameEnum MIDDLE_BOTTOM_OF_RIGHT_EYEBROW = new SensorAttributeNameEnum("MIDDLE_BOTTOM_OF_RIGHT_EYEBROW", "MIDDLE_BOTTOM_OF_RIGHT_EYEBROW");
    public static final SensorAttributeNameEnum ABOVE_MID_UPPER_RIGHT_EYELID = new SensorAttributeNameEnum("ABOVE_MID_UPPER_RIGHT_EYELID", "ABOVE_MID_UPPER_RIGHT_EYELID");
    public static final SensorAttributeNameEnum OUTER_CORNER_OF_RIGHT_EYE = new SensorAttributeNameEnum("OUTER_CORNER_OF_RIGHT_EYE", "OUTER_CORNER_OF_RIGHT_EYE");
    public static final SensorAttributeNameEnum MIDDLE_TOP_RIGHT_EYELID = new SensorAttributeNameEnum("MIDDLE_TOP_RIGHT_EYELID", "MIDDLE_TOP_RIGHT_EYELID");
    public static final SensorAttributeNameEnum MIDDLE_BOTTOM_RIGHT_EYELID = new SensorAttributeNameEnum("MIDDLE_BOTTOM_RIGHT_EYELID", "MIDDLE_BOTTOM_RIGHT_EYELID");
    public static final SensorAttributeNameEnum INNER_CORNER_RIGHT_EYE = new SensorAttributeNameEnum("INNER_CORNER_RIGHT_EYE", "INNER_CORNER_RIGHT_EYE");
    public static final SensorAttributeNameEnum UNDER_MID_BOTTOM_RIGHT_EYELID = new SensorAttributeNameEnum("UNDER_MID_BOTTOM_RIGHT_EYELID", "UNDER_MID_BOTTOM_RIGHT_EYELID");
    public static final SensorAttributeNameEnum RIGHT_SIDE_OF_CHIN = new SensorAttributeNameEnum("RIGHT_SIDE_OF_CHIN", "RIGHT_SIDE_OF_CHIN");
    public static final SensorAttributeNameEnum OUTSIDE_RIGHT_CORNER_MOUTH = new SensorAttributeNameEnum("OUTSIDE_RIGHT_CORNER_MOUTH", "OUTSIDE_RIGHT_CORNER_MOUTH");
    public static final SensorAttributeNameEnum RIGHT_OF_CHIN = new SensorAttributeNameEnum("RIGHT_OF_CHIN", "RIGHT_OF_CHIN");
    public static final SensorAttributeNameEnum RIGHT_TOP_DIP_UPPER_LIP = new SensorAttributeNameEnum("RIGHT_TOP_DIP_UPPER_LIP", "RIGHT_TOP_DIP_UPPER_LIP");
    public static final SensorAttributeNameEnum TOP_LEFT_FOREHEAD = new SensorAttributeNameEnum("TOP_LEFT_FOREHEAD", "TOP_LEFT_FOREHEAD");
    public static final SensorAttributeNameEnum MIDDLE_TOP_LOWER_LIP = new SensorAttributeNameEnum("MIDDLE_TOP_LOWER_LIP", "MIDDLE_TOP_LOWER_LIP");
    public static final SensorAttributeNameEnum MIDDLE_BOTTOM_LOWER_LIP = new SensorAttributeNameEnum("MIDDLE_BOTTOM_LOWER_LIP", "MIDDLE_BOTTOM_LOWER_LIP");
    public static final SensorAttributeNameEnum LEFT_OF_LEFT_EYEBROW = new SensorAttributeNameEnum("LEFT_OF_LEFT_EYEBROW", "LEFT_OF_LEFT_EYEBROW");
    public static final SensorAttributeNameEnum MIDDLE_TOP_OF_LEFT_EYEBROW = new SensorAttributeNameEnum("MIDDLE_TOP_OF_LEFT_EYEBROW", "MIDDLE_TOP_OF_LEFT_EYEBROW");
    public static final SensorAttributeNameEnum RIGHT_OF_LEFT_EYEBROW = new SensorAttributeNameEnum("RIGHT_OF_LEFT_EYEBROW", "RIGHT_OF_LEFT_EYEBROW");
    public static final SensorAttributeNameEnum MIDDLE_BOTTOM_OF_LEFT_EYEBROW = new SensorAttributeNameEnum("MIDDLE_BOTTOM_OF_LEFT_EYEBROW", "MIDDLE_BOTTOM_OF_LEFT_EYEBROW");
    public static final SensorAttributeNameEnum ABOVE_MID_UPPER_LEFT_EYELID = new SensorAttributeNameEnum("ABOVE_MID_UPPER_LEFT_EYELID", "ABOVE_MID_UPPER_LEFT_EYELID");
    public static final SensorAttributeNameEnum OUTER_CORNER_OF_LEFT_EYE = new SensorAttributeNameEnum("OUTER_CORNER_OF_LEFT_EYE", "OUTER_CORNER_OF_LEFT_EYE");
    public static final SensorAttributeNameEnum MIDDLE_TOP_LEFT_EYELID = new SensorAttributeNameEnum("MIDDLE_TOP_LEFT_EYELID", "MIDDLE_TOP_LEFT_EYELID");
    public static final SensorAttributeNameEnum MIDDLE_BOTTOM_LEFT_EYELID = new SensorAttributeNameEnum("MIDDLE_BOTTOM_LEFT_EYELID", "MIDDLE_BOTTOM_LEFT_EYELID");
    public static final SensorAttributeNameEnum INNER_CORNER_LEFT_EYE = new SensorAttributeNameEnum("INNER_CORNER_LEFT_EYE", "INNER_CORNER_LEFT_EYE");
    public static final SensorAttributeNameEnum UNDER_MID_BOTTOM_LEFT_EYELID = new SensorAttributeNameEnum("UNDER_MID_BOTTOM_LEFT_EYELID", "UNDER_MID_BOTTOM_LEFT_EYELID");
    public static final SensorAttributeNameEnum LEFT_SIDE_OF_CHEEK = new SensorAttributeNameEnum("LEFT_SIDE_OF_CHEEK", "LEFT_SIDE_OF_CHEEK");
    public static final SensorAttributeNameEnum OUTSIDE_LEFT_CORNER_MOUTH = new SensorAttributeNameEnum("OUTSIDE_LEFT_CORNER_MOUTH", "OUTSIDE_LEFT_CORNER_MOUTH");
    public static final SensorAttributeNameEnum LEFT_OF_CHIN = new SensorAttributeNameEnum("LEFT_OF_CHIN", "LEFT_OF_CHIN");
    public static final SensorAttributeNameEnum LEFT_TOP_DIP_UPPER_LIP = new SensorAttributeNameEnum("LEFT_TOP_DIP_UPPER_LIP", "LEFT_TOP_DIP_UPPER_LIP");
    public static final SensorAttributeNameEnum OUTER_TOP_RIGHT_PUPIL = new SensorAttributeNameEnum("OUTER_TOP_RIGHT_PUPIL", "OUTER_TOP_RIGHT_PUPIL");
    public static final SensorAttributeNameEnum OUTER_BOTTOM_RIGHT_PUPIL = new SensorAttributeNameEnum("OUTER_BOTTOM_RIGHT_PUPIL", "OUTER_BOTTOM_RIGHT_PUPIL");
    public static final SensorAttributeNameEnum OUTER_TOP_LEFT_PUPIL = new SensorAttributeNameEnum("OUTER_TOP_LEFT_PUPIL", "OUTER_TOP_LEFT_PUPIL");
    public static final SensorAttributeNameEnum OUTER_BOTTOM_LEFT_PUPIL = new SensorAttributeNameEnum("OUTER_BOTTOM_LEFT_PUPIL", "OUTER_BOTTOM_LEFT_PUPIL");
    public static final SensorAttributeNameEnum INNER_TOP_RIGHT_PUPIL = new SensorAttributeNameEnum("INNER_TOP_RIGHT_PUPIL", "INNER_TOP_RIGHT_PUPIL");
    public static final SensorAttributeNameEnum INNER_BOTTOM_RIGHT_PUPIL = new SensorAttributeNameEnum("INNER_BOTTOM_RIGHT_PUPIL", "INNER_BOTTOM_RIGHT_PUPIL");
    public static final SensorAttributeNameEnum INNER_TOP_LEFT_PUPIL = new SensorAttributeNameEnum("INNER_TOP_LEFT_PUPIL", "INNER_TOP_LEFT_PUPIL");
    public static final SensorAttributeNameEnum INNER_BOTTOM_LEFT_PUPIL = new SensorAttributeNameEnum("INNER_BOTTOM_LEFT_PUPIL", "INNER_BOTTOM_LEFT_PUPIL");
    public static final SensorAttributeNameEnum RIGHT_TOP_UPPER_LIP = new SensorAttributeNameEnum("RIGHT_TOP_UPPER_LIP", "RIGHT_TOP_UPPER_LIP");
    public static final SensorAttributeNameEnum LEFT_TOP_UPPER_LIP = new SensorAttributeNameEnum("LEFT_TOP_UPPER_LIP", "LEFT_TOP_UPPER_LIP");
    public static final SensorAttributeNameEnum RIGHT_BOTTOM_UPPER_LIP = new SensorAttributeNameEnum("RIGHT_BOTTOM_UPPER_LIP", "RIGHT_BOTTOM_UPPER_LIP");
    public static final SensorAttributeNameEnum LEFT_BOTTOM_UPPER_LIP = new SensorAttributeNameEnum("LEFT_BOTTOM_UPPER_LIP", "LEFT_BOTTOM_UPPER_LIP");
    public static final SensorAttributeNameEnum RIGHT_TOP_LOWER_LIP = new SensorAttributeNameEnum("RIGHT_TOP_LOWER_LIP", "RIGHT_TOP_LOWER_LIP");
    public static final SensorAttributeNameEnum LEFT_TOP_LOWER_LIP = new SensorAttributeNameEnum("LEFT_TOP_LOWER_LIP", "LEFT_TOP_LOWER_LIP");
    public static final SensorAttributeNameEnum RIGHT_BOTTOM_LOWER_LIP = new SensorAttributeNameEnum("RIGHT_BOTTOM_LOWER_LIP", "RIGHT_BOTTOM_LOWER_LIP");
    public static final SensorAttributeNameEnum LEFT_BOTTOM_LOWER_LIP = new SensorAttributeNameEnum("LEFT_BOTTOM_LOWER_LIP", "LEFT_BOTTOM_LOWER_LIP");
    public static final SensorAttributeNameEnum MIDDLE_BOTTOM_UPPER_LIP = new SensorAttributeNameEnum("MIDDLE_BOTTOM_UPPER_LIP", "MIDDLE_BOTTOM_UPPER_LIP");
    public static final SensorAttributeNameEnum LEFT_CORNER_MOUTH = new SensorAttributeNameEnum("LEFT_CORNER_MOUTH", "LEFT_CORNER_MOUTH");
    public static final SensorAttributeNameEnum RIGHT_CORNER_MOUTH = new SensorAttributeNameEnum("RIGHT_CORNER_MOUTH", "RIGHT_CORNER_MOUTH");
    public static final SensorAttributeNameEnum BOTTOM_OF_RIGHT_CHEEK = new SensorAttributeNameEnum("BOTTOM_OF_RIGHT_CHEEK", "BOTTOM_OF_RIGHT_CHEEK");
    public static final SensorAttributeNameEnum BOTTOM_OF_LEFT_CHEEK = new SensorAttributeNameEnum("BOTTOM_OF_LEFT_CHEEK", "BOTTOM_OF_LEFT_CHEEK");
    public static final SensorAttributeNameEnum ABOVE_THREE_FOURTH_RIGHT_EYELID = new SensorAttributeNameEnum("ABOVE_THREE_FOURTH_RIGHT_EYELID", "ABOVE_THREE_FOURTH_RIGHT_EYELID");
    public static final SensorAttributeNameEnum ABOVE_THREE_FOURTH_LEFT_EYELID = new SensorAttributeNameEnum("ABOVE_THREE_FOURTH_LEFT_EYELID", "ABOVE_THREE_FOURTH_LEFT_EYELID");
    public static final SensorAttributeNameEnum THREE_FOURTH_TOP_RIGHT_EYELID = new SensorAttributeNameEnum("THREE_FOURTH_TOP_RIGHT_EYELID", "THREE_FOURTH_TOP_RIGHT_EYELID");
    public static final SensorAttributeNameEnum THREE_FOURTH_TOP_LEFT_EYELID = new SensorAttributeNameEnum("THREE_FOURTH_TOP_LEFT_EYELID", "THREE_FOURTH_TOP_LEFT_EYELID");
    public static final SensorAttributeNameEnum THREE_FOURTH_BOTTOM_RIGHT_EYELID = new SensorAttributeNameEnum("THREE_FOURTH_BOTTOM_RIGHT_EYELID", "THREE_FOURTH_BOTTOM_RIGHT_EYELID");
    public static final SensorAttributeNameEnum THREE_FOURTH_BOTTOM_LEFT_EYELID = new SensorAttributeNameEnum("THREE_FOURTH_BOTTOM_LEFT_EYELID", "THREE_FOURTH_BOTTOM_LEFT_EYELID");
    public static final SensorAttributeNameEnum BELOW_THREE_FOURTH_RIGHT_EYELID = new SensorAttributeNameEnum("BELOW_THREE_FOURTH_RIGHT_EYELID", "BELOW_THREE_FOURTH_RIGHT_EYELID");
    public static final SensorAttributeNameEnum BELOW_THREE_FOURTH_LEFT_EYELID = new SensorAttributeNameEnum("BELOW_THREE_FOURTH_LEFT_EYELID", "BELOW_THREE_FOURTH_LEFT_EYELID");
    public static final SensorAttributeNameEnum ABOVE_ONE_FOURTH_RIGHT_EYELID = new SensorAttributeNameEnum("ABOVE_ONE_FOURTH_RIGHT_EYELID", "ABOVE_ONE_FOURTH_RIGHT_EYELID");
    public static final SensorAttributeNameEnum ABOVE_ONE_FOURTH_LEFT_EYELID = new SensorAttributeNameEnum("ABOVE_ONE_FOURTH_LEFT_EYELID", "ABOVE_ONE_FOURTH_LEFT_EYELID");
    public static final SensorAttributeNameEnum ONE_FOURTH_TOP_RIGHT_EYELID = new SensorAttributeNameEnum("ONE_FOURTH_TOP_RIGHT_EYELID", "ONE_FOURTH_TOP_RIGHT_EYELID");
    public static final SensorAttributeNameEnum ONE_FOURTH_TOP_LEFT_EYELID = new SensorAttributeNameEnum("ONE_FOURTH_TOP_LEFT_EYELID", "ONE_FOURTH_TOP_LEFT_EYELID");
    public static final SensorAttributeNameEnum ONE_FOURTH_BOTTOM_RIGHT_EYELID = new SensorAttributeNameEnum("ONE_FOURTH_BOTTOM_RIGHT_EYELID", "ONE_FOURTH_BOTTOM_RIGHT_EYELID");
    public static final SensorAttributeNameEnum ONE_FOURTH_BOTTOM_LEFT_EYELID = new SensorAttributeNameEnum("ONE_FOURTH_BOTTOM_LEFT_EYELID", "ONE_FOURTH_BOTTOM_LEFT_EYELID");
    public static final SensorAttributeNameEnum COLOR_CHANNEL = new SensorAttributeNameEnum("RGB_CHANNEL", "RGB_CHANNEL");
    public static final SensorAttributeNameEnum DEPTH_CHANNEL = new SensorAttributeNameEnum("DEPTH_CHANNEL", "DEPTH_CHANNEL");

    /** the class type for the attribute - useful for filters, learner module and CODEC agreements between attributes and values */
    private Class<? extends AbstractSensorAttributeValue> classType = DoubleValue.class;
    
    public SensorAttributeNameEnum(String name, String displayName){
    	super(index++, name, displayName);
    	enumList.add(this);
    }
    
    /**
     * Class constructor - use this to set a different class type for an attribute.
     * 
     * @param name
     * @param displayName
     * @param classType - the class type for the attribute (default: DoubleValue.class)
     */
    private SensorAttributeNameEnum(String name, String displayName, Class<? extends AbstractSensorAttributeValue> classType){ 
        this(name, displayName);
        
        if(classType == null){
            throw new IllegalArgumentException("The class type can't be null");
        }
        
        this.classType = classType;
    }
    
    /**
     * Return the class type for the attribute.
     * 
     * @return Class<? extends AbstractSensorAttributeValue>
     */
    public Class<? extends AbstractSensorAttributeValue> getClassType(){
        return classType;
    }

    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static SensorAttributeNameEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static SensorAttributeNameEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<SensorAttributeNameEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
