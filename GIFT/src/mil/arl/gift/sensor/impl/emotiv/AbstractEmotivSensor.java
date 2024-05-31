/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl.emotiv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorStateEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataCollection;
import mil.arl.gift.sensor.impl.AbstractSensor;
import mil.arl.gift.sensor.impl.emotiv.EmoState.EE_EEG_ContactQuality_t;
import mil.arl.gift.sensor.impl.emotiv.EmoState.EE_InputChannels_t;

/**
 * This is the base class for Emotiv sensor(s).  It contains methods to parse Emotiv sensor states.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractEmotivSensor extends AbstractSensor {  
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractEmotivSensor.class);
    
    //load edk dll
    static{
        System.loadLibrary("edk_utils");
        System.loadLibrary("edk");        
    }
    
    /** the user id to use for the emotiv api */
    protected static final int DEFAULT_USER_ID = 1;

    /** the minimum amount of time (milliseconds) between contact quality checks */
    private static final long CONTACT_QUALITY_CHECK_PERIOD = 1000;
    
    /** Unable to find any Emotiv documentation on what this parameter is used for */
    protected static final String DEV_ID = "Emotiv System"; 
    
    /** error messages*/
    private static final String DISCONNECTED_ERROR = "Emotiv has been disconnected";
    private static final String UNINIT_ERROR = "Emotiv is unitialized";
    private static final String NOT_ACQUIRE_ERROR = "Emotiv can't acquire data";
    private static final String UNKNOWN_ERROR = "Emotiv reporting unknown error";
    private static final String UNHANDLED_ERROR = "Emotiv reporting unhandled error code ";
    private static final String LOW_BATTERY_ERROR = "Emotiv reporting that the battery is low";
    private static final String WEAK_WIRELESS_SIGNAL_ERROR = "Emotiv reporting a poor wireless signal";
    private static final String CONTACT_QUALITY_ERROR = "Emotiv contact quality problem - ";
    
    /** the thread that determines when a new value is calculated */
    private SensorThread sThread = null;
    
    /** the last time the contact quality levels were checked */
    private long lastContactQualityCheck;
    
    /** last collected emotiv attributes */
    protected int lastBatteryLevel = -1;
    protected int lastSignalStrength = 0;
    
    /** flag to indicate whether to capture affective data from emotiv */
    protected boolean useAffectiveSuite = false;
    
    /** the affective attributes */
    float stExcite, ltExcite, engagement, frustration, meditation;

    /**
     * Class constructor
     * 
     * @param sensorName - display name of this sensor, can't be null.
     * @param sensorType The type of sensor. can't be null or empty.
     */
    public AbstractEmotivSensor(String sensorName, SensorTypeEnum sensorType){
        super(sensorName, sensorType);
    }
    
    
    @Override
    public void start(long domainSessionStartTime) throws Exception{
        
        if(sThread == null || sensorState == SensorStateEnum.STOPPED){
            super.start(domainSessionStartTime);
            
            sThread = new SensorThread(getSensorName());
            sThread.start();
            
            lastContactQualityCheck = 0;
            
            logger.info("Sensor started for "+this);
        }
    }
    
    @Override
    public void stop(){
        
        if(sThread != null){
            super.stop();
            sensorState = SensorStateEnum.STOPPED;
            
            try {
                logger.info("Waiting for sensor thread to finish");
                sThread.join();
                logger.info("Sensor thread has finished");
                
            } catch (InterruptedException e) {
                logger.error("Caught exception while trying to wait for sensor thread to finish", e);
            }
            
            logger.info("Sensor stopped for "+this);
        }
    }
    
    /**
     * Handle the emotiv engine state and send the appropriate attributes.
     * 
     * @param eState - contains emotiv state attribute values
     * @param sensorDatas - ordered list of attribute maps based on the number of results returned during a single read of the sensor
     * @param elapsedTime - amount of time that has elapsed since the start of the domain session
     */
    protected void handleState(Pointer eState, List<Map<SensorAttributeNameEnum, AbstractSensorAttributeValue>> sensorDatas, long elapsedTime){
        
        if(useAffectiveSuite){
            
            stExcite = EmoState.INSTANCE.ES_AffectivGetExcitementShortTermScore(eState);
            ltExcite = EmoState.INSTANCE.ES_AffectivGetExcitementLongTermScore(eState);
            engagement = EmoState.INSTANCE.ES_AffectivGetEngagementBoredomScore(eState);
            frustration = EmoState.INSTANCE.ES_AffectivGetFrustrationScore(eState);
            meditation = EmoState.INSTANCE.ES_AffectivGetMeditationScore(eState);
            
            if(sensorDatas.isEmpty()){
                sensorDatas.add(new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>());
            }
            
            //for now... populate every sensor data object with the Affective
            for(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> sData : sensorDatas){
            
                sData.put(SensorAttributeNameEnum.ST_EXCITEMENT, new DoubleValue(SensorAttributeNameEnum.ST_EXCITEMENT, stExcite));
                sData.put(SensorAttributeNameEnum.LT_EXCITEMENT, new DoubleValue(SensorAttributeNameEnum.LT_EXCITEMENT, ltExcite));
                sData.put(SensorAttributeNameEnum.MEDITATION, new DoubleValue(SensorAttributeNameEnum.MEDITATION, meditation));
                sData.put(SensorAttributeNameEnum.FRUSTRATION, new DoubleValue(SensorAttributeNameEnum.FRUSTRATION, frustration));
                sData.put(SensorAttributeNameEnum.ENGAGEMENT, new DoubleValue(SensorAttributeNameEnum.ENGAGEMENT, engagement));
            }
        }
        
        //only send data if there is something to send
        if(!sensorDatas.isEmpty()){ 
            
            // create sensor data objects
            List<SensorData> sDatas = new ArrayList<SensorData>(sensorDatas.size());
            for(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> mapping : sensorDatas){
                sDatas.add(new SensorData(mapping, elapsedTime));
            }
            
            SensorDataCollection data = new SensorDataCollection(sDatas, elapsedTime);
            sendDataEvent(data);
        }
    }
    
    /**
     * This thread is responsible for ticking the sensor which retrieves
     * the sensors next value.  The sensor value is then sent via sensor data event.
     * 
     * @author mhoffman
     *
     */
    private class SensorThread extends Thread{
        
        public SensorThread(String threadName){
            super(threadName);
        }
        
        @Override
        public void run(){
            
            sensorState = SensorStateEnum.RUNNING;
            
            long now;
            long elapsedTime;
            int state = 0, contactState;
            Pointer eEvent          = Edk.INSTANCE.EE_EmoEngineEventCreate();
            Pointer eState          = Edk.INSTANCE.EE_EmoStateCreate();
            IntByReference userID   = new IntByReference(DEFAULT_USER_ID);
            IntByReference batteryLevel   = new IntByReference();
            IntByReference maxBatteryLevel   = new IntByReference();
//            IntByReference contactsQuality = new IntByReference();
            

            //get number of contact channels for later checking contact quality
            int numChannels = EmoState.INSTANCE.ES_GetNumContactQualityChannels(eState);
            
            //TODO: create better way to organize emotiv data for when a single data query returns multiple samples (EEG) 
//            Map<SensorAttributeNameEnum, Double> sensorAttributeToValue = new HashMap<SensorAttributeNameEnum, Double>();
            List<Map<SensorAttributeNameEnum, AbstractSensorAttributeValue>> sDatas = new ArrayList<Map<SensorAttributeNameEnum, AbstractSensorAttributeValue>>();
            
            while(sensorState == SensorStateEnum.RUNNING){
                
                try{
                    //Note: non-blocking call
                    state = Edk.INSTANCE.EE_EngineGetNextEvent(eEvent);
                    
                    // New event needs to be handled
                    if (state == EdkErrorCode.EDK_OK.ToInt()) {

                        int eventType = Edk.INSTANCE.EE_EmoEngineEventGetType(eEvent);
                        Edk.INSTANCE.EE_EmoEngineEventGetUserId(eEvent, userID);

                        // capture the EmoState if it has been updated
                        if (eventType == Edk.EE_Event_t.EE_EmoStateUpdated.ToInt()) {       

                            Edk.INSTANCE.EE_EmoEngineEventGetEmoState(eEvent, eState);
                            
                            // NOTE: decided to capture the time after the data has been collected from the hardware
                            now = System.currentTimeMillis();
                            elapsedTime = now - getDomainSessionStartTime();
                            
//                            sensorAttributeToValue.clear();
                            sDatas.clear();
                            handleState(eState, sDatas, elapsedTime);

                            //
                            // check wireless signal strength - send status out if value is low.  Only send subsequent status values if value changes
                            //                               
                            int signalStrength = EmoState.INSTANCE.ES_GetWirelessSignalStatus(eState);
                            
                            //Good = 2, Bad = 1, No Signal = 0
                            if(signalStrength < 2 && lastSignalStrength != signalStrength){
                                logger.warn("Emotiv wireless signal strength is at level "+signalStrength);
                                createSensorStatus(WEAK_WIRELESS_SIGNAL_ERROR);
                            }
                            lastSignalStrength = signalStrength;

                            //
                            // check battery level - send status out if value is low.  Only send subsequent status values if value changes
                            //                                
                            EmoState.INSTANCE.ES_GetBatteryChargeLevel(eState, batteryLevel, maxBatteryLevel);
                            int level = batteryLevel.getValue();
                            
                            //4 = High or good, {1,0,-1} = considered low or bad
                            //Note: first read of battery level tends to give a large negative number out of emotiv battery level reported range {-1, 4}
                            if(level < 2 && level > -2 && lastBatteryLevel != level){
                                logger.warn("Emotiv battery level is at level "+level);
                                createSensorStatus(LOW_BATTERY_ERROR);
                            }
                            
                            lastBatteryLevel = level;

                            
                            //check contact pad quality 
                            if(now - lastContactQualityCheck > CONTACT_QUALITY_CHECK_PERIOD){

                              //NOTE: while running the sensor module in debug through eclipse Indigo and calling this method in a fast as possible while loop always 
                              //caused the entire sensor module to die almost exactly at 300 EmoState seconds of EmoComposer data events.
                              //However while using the monitor to launch the sensor module there was no problem 
                              //TODO: compare the environment variables and java args of monitor bat files and eclipse for sensor module                               
//                              int size = EmoState.INSTANCE.ES_GetContactQualityFromAllChannels(eState, contacts, numChannels); 
                                
                                StringBuilder contactIssuesMessage = null;
                                for(int i = 0; i < numChannels; i++){
                                    contactState = EmoState.INSTANCE.ES_GetContactQuality(eState, i);
                                    
                                    //refer to EmoState.EE_EEG_ContactQuality_t
                                    if(contactState < 3){
                                        
                                        if(contactIssuesMessage == null){
                                            contactIssuesMessage = new StringBuilder(CONTACT_QUALITY_ERROR);
                                        }
                                        
                                        contactIssuesMessage.append(EE_InputChannels_t.values()[i]).append(" : ").append(EE_EEG_ContactQuality_t.values()[contactState]).append(", ");
                                    }
                                }
                                
                                if(contactIssuesMessage != null){
                                    createSensorStatus(contactIssuesMessage.toString());
                                }
                                
                                lastContactQualityCheck = now;
                            }
                        }else if (eventType == Edk.EE_Event_t.EE_UserAdded.ToInt()) {
                            //
                            // this logic comes from EEGLog.java example and is necessary for capturing EEG data
                            //
                            
                            if (userID != null)
                            {
                                Edk.INSTANCE.EE_DataAcquisitionEnable(userID.getValue(),true);
                            }
                        }

                        

                        
                    }else if (state != EdkErrorCode.EDK_NO_EVENT.ToInt()) {
                        
                        logger.error("Emotiv Engine reporting error.  The error code is "+state);

                        String msg;
                        if(state == EdkErrorCode.EDK_EMOENGINE_DISCONNECTED.ToInt()){
                            msg = DISCONNECTED_ERROR;
                        }else if(state == EdkErrorCode.EDK_EMOENGINE_UNINITIALIZED.ToInt()){
                            msg = UNINIT_ERROR;
                        }else if(state == EdkErrorCode.EDK_CANNOT_ACQUIRE_DATA.ToInt()){
                            msg = NOT_ACQUIRE_ERROR;
                        }else if(state == EdkErrorCode.EDK_UNKNOWN_ERROR.ToInt()){
                            msg = UNKNOWN_ERROR;
                        }else{
                            msg = UNHANDLED_ERROR + state;
                        }
                        
                        createSensorStatus(msg);
                    }
                
                }catch(Exception e){
                    logger.error("Caught exception while collecting data from sensor", e);
                }
                
            }//end while
               
            Edk.INSTANCE.EE_EngineDisconnect();
            Edk.INSTANCE.EE_EmoStateFree(eState);
            Edk.INSTANCE.EE_EmoEngineEventFree(eEvent);
            
            logger.info("Emotiv has been disconnected");
        }
    }
}
