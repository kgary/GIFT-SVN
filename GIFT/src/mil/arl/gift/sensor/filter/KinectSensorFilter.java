/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.sensor.filter;

import java.util.HashMap;
import java.util.Map;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.Tuple3dValue;
import mil.arl.gift.sensor.AbstractSensorData;
import mil.arl.gift.sensor.SensorData;
import mil.arl.gift.sensor.SensorDataCollection;
import mil.arl.gift.sensor.SensorDataEvent;
import mil.arl.gift.sensor.SensorManager;
import mil.arl.gift.sensor.impl.AbstractSensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for filtering sensor data and producing a filtered event.
 * 
 * @author jleonard
 */
public class KinectSensorFilter extends AbstractSensorFilter {
	
	/** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KinectSensorFilter.class);
    
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
    }
    
	/**
	 * Empty constructor
	 */
	public KinectSensorFilter(){
        setEventProducerInformation(eventProducerInformation);			
    }

	@Override
	public void filterSensorFilterData(SensorFilterEvent sensorFilterEvent){
		//nothing to do yet...
	}
	
	@Override
    public void start(long domainSessionStartTime) throws Exception{
        
	    logger.info("Start called for "+this);
	    super.start(domainSessionStartTime);
	}
	
    @Override
    public void filterSensorData(SensorDataEvent sensorDataEvent) {

        AbstractSensor sensor = sensorDataEvent.getSensor();

        if (logger.isInfoEnabled()) {

            logger.info("Received sensor data event to filter and create a message for - " + sensorDataEvent);
        }

        //convert attributes to object map
        AbstractSensorData sData = sensorDataEvent.getData();

        if (sData instanceof SensorDataCollection) {

            for (SensorData sensorData : ((SensorDataCollection) sData).getSensorDatas()) {

                filterSensorData(sensorData, sensor);
            }

        } else if (sData instanceof SensorData) {

            filterSensorData((SensorData) sData, sensor);

        } else {

            logger.error("Unable to handle sensor data event of type " + sData);
        }
    }
    
    private void filterSensorData(SensorData sensorData, AbstractSensor sensor) {

        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> filteredSensorData = new HashMap<>();
        
        // Cull out all but a few interesting attributes to prevent flooding the monitor
        
        AbstractSensorAttributeValue head = sensorData.getSensorAttributeToValue().get(SensorAttributeNameEnum.HEAD);
        
        if(head != null) {
            
            filteredSensorData.put(SensorAttributeNameEnum.HEAD, head);
        }
        
        AbstractSensorAttributeValue topSkull = sensorData.getSensorAttributeToValue().get(SensorAttributeNameEnum.TOP_SKULL);
        
        if(topSkull != null) {
        
            filteredSensorData.put(SensorAttributeNameEnum.TOP_SKULL, topSkull);
        }
        
        AbstractSensorAttributeValue rightHand = sensorData.getSensorAttributeToValue().get(SensorAttributeNameEnum.RIGHT_HAND);
        
        if(rightHand != null) {
        
            filteredSensorData.put(SensorAttributeNameEnum.RIGHT_HAND, rightHand);
        }
        
        AbstractSensorAttributeValue leftHand = sensorData.getSensorAttributeToValue().get(SensorAttributeNameEnum.LEFT_HAND);
        
        if(leftHand != null) {
        
            filteredSensorData.put(SensorAttributeNameEnum.LEFT_HAND, leftHand);
        }
        
        AbstractSensorAttributeValue centerShoulder = sensorData.getSensorAttributeToValue().get(SensorAttributeNameEnum.CENTER_SHOULDER);
        
        if(centerShoulder != null) {
        
            filteredSensorData.put(SensorAttributeNameEnum.CENTER_SHOULDER, centerShoulder);
        }
        
        AbstractSensorAttributeValue leftShoulder = sensorData.getSensorAttributeToValue().get(SensorAttributeNameEnum.LEFT_SHOULDER);
        
        if(leftShoulder != null) {
        
            filteredSensorData.put(SensorAttributeNameEnum.LEFT_SHOULDER, leftShoulder);
        }
        
        AbstractSensorAttributeValue rightShoulder = sensorData.getSensorAttributeToValue().get(SensorAttributeNameEnum.RIGHT_SHOULDER);
        
        if(rightShoulder != null) {
        
            filteredSensorData.put(SensorAttributeNameEnum.RIGHT_SHOULDER, rightShoulder);
        }

        SensorFilterData fData = new SensorFilterData(filteredSensorData, sensorData.getElapsedTime());
        SensorManager.getInstance().createSensorFilterDataEvent(this, fData, sensor);
    }

    @Override
    protected void writerFileCreated(String fileName){        
        //TODO: as of now our UMS db doesn't support filter data file referencing
    }
	
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[KinectSensorFilter:");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }	
}
