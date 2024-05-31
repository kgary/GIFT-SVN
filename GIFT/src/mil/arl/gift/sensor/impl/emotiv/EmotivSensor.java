/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl.emotiv;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;

/**
 * This sensor interfaces with the Emotiv sensor which produces Emotiv events.
 * Those Emotiv events are capture by this sensor class and sent to the gift network.
 * 
 * From Emotiv website:
 * The EPOC collects EEG biosignals from 14 sensors around the head, plus 2 reference channels in CMS/DRL configuration.
 * They are all EEG sensors.
 * 
 * The sensor location behind the ears is fitted with a soft rubber comfort pad. 
 * This should remain in place unless the normal Reference sensors do not contact properly.
 * 
 * @author mhoffman
 *
 */
public class EmotivSensor extends AbstractEmotivSensor {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmotivSensor.class);
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_COUNTER, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_INTERPOLATED, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_RAW_CQ, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_AF3, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_F7, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_F3, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_FC5, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_T7, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_P7, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_O1, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_O2, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_P8, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_T8, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_FC6, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_F4, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.EE_CHAN_F8, DoubleValue.class));        
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_AF4, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_GYROX, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_GYROY, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_TIMESTAMP, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_ES_TIMESTAMP, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_FUNC_VALUE, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_MARKER, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ED_SYNC_SIGNAL, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ST_EXCITEMENT, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LT_EXCITEMENT, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ENGAGEMENT, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MEDITATION, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.FRUSTRATION, DoubleValue.class));
    }

    /** name of EEG attributes (in order of collection) to use */
    private static SensorAttributeNameEnum[] EEG_CHANNEL_NAMES = {SensorAttributeNameEnum.ED_COUNTER, SensorAttributeNameEnum.ED_INTERPOLATED, SensorAttributeNameEnum.ED_RAW_CQ, 
        SensorAttributeNameEnum.ED_AF3, SensorAttributeNameEnum.EE_CHAN_F7, SensorAttributeNameEnum.EE_CHAN_F3, SensorAttributeNameEnum.EE_CHAN_FC5, SensorAttributeNameEnum.EE_CHAN_T7, SensorAttributeNameEnum.EE_CHAN_P7, 
        SensorAttributeNameEnum.EE_CHAN_O1, SensorAttributeNameEnum.EE_CHAN_O2, SensorAttributeNameEnum.EE_CHAN_P8, SensorAttributeNameEnum.EE_CHAN_T8, SensorAttributeNameEnum.EE_CHAN_FC6, SensorAttributeNameEnum.EE_CHAN_F4, SensorAttributeNameEnum.EE_CHAN_F8, SensorAttributeNameEnum.ED_AF4,
        SensorAttributeNameEnum.ED_GYROX, SensorAttributeNameEnum.ED_GYROY, SensorAttributeNameEnum.ED_TIMESTAMP, SensorAttributeNameEnum.ED_ES_TIMESTAMP, SensorAttributeNameEnum.ED_FUNC_ID, SensorAttributeNameEnum.ED_FUNC_VALUE, SensorAttributeNameEnum.ED_MARKER, SensorAttributeNameEnum.ED_SYNC_SIGNAL};
    
    Edk.EE_DataChannels_t targetChannelList[] = {
            Edk.EE_DataChannels_t.ED_COUNTER,
            Edk.EE_DataChannels_t.ED_AF3, Edk.EE_DataChannels_t.ED_F7, Edk.EE_DataChannels_t.ED_F3, Edk.EE_DataChannels_t.ED_FC5, Edk.EE_DataChannels_t.ED_T7,
            Edk.EE_DataChannels_t.ED_P7, Edk.EE_DataChannels_t.ED_O1, Edk.EE_DataChannels_t.ED_O2, Edk.EE_DataChannels_t.ED_P8, Edk.EE_DataChannels_t.ED_T8,
            Edk.EE_DataChannels_t.ED_FC6, Edk.EE_DataChannels_t.ED_F4, Edk.EE_DataChannels_t.ED_F8, Edk.EE_DataChannels_t.ED_AF4, Edk.EE_DataChannels_t.ED_GYROX, Edk.EE_DataChannels_t.ED_GYROY, Edk.EE_DataChannels_t.ED_TIMESTAMP,
            Edk.EE_DataChannels_t.ED_FUNC_ID, Edk.EE_DataChannels_t.ED_FUNC_VALUE, Edk.EE_DataChannels_t.ED_MARKER, Edk.EE_DataChannels_t.ED_SYNC_SIGNAL
         }; 
    /** 
     * Sets the size of the data buffer. 
     * The size of the buffer affects how frequent EE_DataUpdateHandle() needs to be called to prevent data loss.
     */
    //NOTE: 
    //Internal sampling rate in the headset is 2048 per sec per channel. 
    //This is filtered to remove mains artifacts and alias frequencies then down-sampled to 128 per sec per channel 
    // http://www.emotiv.com/forum/messages/forum4/topic309/message1698/?phrase_id=111979#message1698
    // Almost equivalent to 7.8+ ms between values
    //NOTE: had issues when using 7.8ms for this value, after getting a single EEG reading (had 4 samples taken) received no more readings
    private static final float SEC_SIZE = 0.05f;
    
    private Pointer hData = Edk.INSTANCE.EE_DataCreate();
    private IntByReference nSamplesTaken   = new IntByReference(0);
    
    /**
     * use default configuration
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public EmotivSensor(String sensorName){
        super(sensorName, SensorTypeEnum.EMOTIV);
        setEventProducerInformation(eventProducerInformation);
    }
    
    /**
     * Class constructor - configure using the sensor configuration input for this sensor
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration the sensor configuration parameters used to configure this sensor instance
     */
    public EmotivSensor(String sensorName, generated.sensor.EmotivSensor configuration){
        this(sensorName);
        
        if(configuration.getUseAffective() != null){
            useAffectiveSuite = new Boolean(configuration.getUseAffective().value());
        }
    }
    
    @Override
    public boolean test(){
        
        if (Edk.INSTANCE.EE_EngineConnect(DEV_ID) != EdkErrorCode.EDK_OK.ToInt()) {            
            throw new ConfigurationException("Emotiv Engine start up failed.", "Unable to connect to the Emotive engine during test", null);
        }
        
        Edk.INSTANCE.EE_DataSetBufferSizeInSec(SEC_SIZE);
        
        logger.debug("Connected to Emotiv");
        
        return true;
    }
    
    @Override
    protected void handleState(Pointer eState, List<Map<SensorAttributeNameEnum, AbstractSensorAttributeValue>> sDatas, long elapsedTime){
        
        Edk.INSTANCE.EE_DataUpdateHandle(0, hData);

        //get number of samples in the data retrieved
        Edk.INSTANCE.EE_DataGetNumberOfSample(hData, nSamplesTaken);

        if (nSamplesTaken != null)
        {
            if (nSamplesTaken.getValue() != 0) {
                
                //System.out.print("Updated: ");
                //System.out.println(nSamplesTaken.getValue());
                
                double[] data = new double[nSamplesTaken.getValue()];
                
                //
                // timing metrics
                //
                //System.out.println(Calendar.getInstance().getTimeInMillis()+": #samples = "+nSamplesTaken.getValue());
                
                for (int sampleIdx=0 ; sampleIdx<nSamplesTaken.getValue() ; ++ sampleIdx) {
                    
                    //the channels of the emotiv for each sample
                    for(int i = 0; i < EEG_CHANNEL_NAMES.length; i++){

                        //get data for channel i from all samples taken
                        Edk.INSTANCE.EE_DataGet(hData, i, data, nSamplesTaken.getValue());
                        
                        if((sDatas.size()-1) < sampleIdx){
                                 sDatas.add(new HashMap<SensorAttributeNameEnum, AbstractSensorAttributeValue>());         
                        }
                        
                        sDatas.get(sampleIdx).put(EEG_CHANNEL_NAMES[i], new DoubleValue(EEG_CHANNEL_NAMES[i], data[sampleIdx]));  
                        
                        //display channel i's data from sample number sampleIdx
                        //System.out.print(data[sampleIdx]);
                        //System.out.print(",");
                    }   
                    //System.out.println();
                }
            }
        }
        
        super.handleState(eState, sDatas, elapsedTime);
    }

    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EmotivSensor: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
