/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.impl.emotiv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.common.sensor.DoubleValue;

/**
 * This sensor interfaces with the Emotiv EmoComposer application which can produce emotiv events.
 * Those emotiv events are capture by this sensor class and sent to the gift network.
 * 
 * @author mhoffman
 *
 */
public class EmoComposerSensor extends AbstractEmotivSensor {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(EmoComposerSensor.class);
    
    /** use localhost address as default ip address when one is not provided*/
    private static final String DEFAULT_IP_ADDR = "127.0.0.1";
    
    /** the emo composer port from documentation */
    private static final short EMO_COMPOSER_PORT = 1726;
    
    /** information about the data this sensor can produce */    
    protected static EventProducerInformation eventProducerInformation;
    static{
        eventProducerInformation = new EventProducerInformation();
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ST_EXCITEMENT, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.LT_EXCITEMENT, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.ENGAGEMENT, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.MEDITATION, DoubleValue.class));
        eventProducerInformation.addAttribute(new EventProducerAttribute(SensorAttributeNameEnum.FRUSTRATION, DoubleValue.class));
    }
    
    /** the IP address of the machine running the Emo Composer application*/
    private String ipAddr = DEFAULT_IP_ADDR;   
    
    /**
     * use default configuration
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     */
    public EmoComposerSensor(String sensorName){
        super(sensorName, SensorTypeEnum.EMO_COMPOSER);
        setEventProducerInformation(eventProducerInformation);
        
        //affective data is the only data produced by EmoComposer
        useAffectiveSuite = true;
    }
    
    /**
     * Class constructor - configure using the sensor configuration input for this sensor
     * 
     * @param sensorName an authored name for this sensor.  Can't be null or empty.
     * @param configuration the sensor configuration input for this sensor
     */
    public EmoComposerSensor(String sensorName, generated.sensor.EmoComposerSensor configuration){
        this(sensorName);
        
        ipAddr = configuration.getIpAddr();
    }
    
    @Override
    public boolean test(){
        
        logger.debug("Target IP of EmoComposer: ["+ipAddr+"]");

        if (Edk.INSTANCE.EE_EngineRemoteConnect(ipAddr, EMO_COMPOSER_PORT, DEV_ID) != EdkErrorCode.EDK_OK.ToInt()) {            
            throw new ConfigurationException("Failed to test Emotiv sensor", "Cannot connect to EmoComposer on ["+ipAddr+"]", null);
        }
        
        logger.debug("Connected to EmoComposer on ["+ipAddr+"]");
        
        return true;
    }

    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[EmoComposerSensor: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
    

}
