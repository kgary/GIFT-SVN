/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer.data;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;
import mil.arl.gift.common.sensor.AbstractSensorAttributeValue;
import mil.arl.gift.common.sensor.DoubleValue;
import mil.arl.gift.common.sensor.IntegerValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for normalizing sensor data based attribute ranges.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractNormalizeTranslator extends AbstractSensorTranslator {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractNormalizeTranslator.class);
    
    /** container of unique sensor attribute name to its possible range of values */
    protected Map<SensorAttributeNameEnum, AttributeRange> ranges = new HashMap<SensorAttributeNameEnum, AttributeRange>();
    
    /**
     * Translate the sensor values by normalizing them.
     * 
     * @param attributeToValue - sensor attribute name to it's value
     * @return Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> - translated values
     */
    @Override
    public Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> getValues(Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> attributeToValue){
        
        Map<SensorAttributeNameEnum, AbstractSensorAttributeValue> normalizedVals = new HashMap<>(attributeToValue.size());
        
        for(SensorAttributeNameEnum attribute : attributeToValue.keySet()){
            AbstractSensorAttributeValue valueObj = attributeToValue.get(attribute);
            
            double currValue;
            if(valueObj instanceof DoubleValue){
                currValue = ((DoubleValue)valueObj).getNumber().doubleValue();
            }else if(valueObj instanceof IntegerValue){
                currValue = ((IntegerValue)valueObj).getNumber().intValue();
            }else{
                //don't drop it even though it can't be normalized
                normalizedVals.put(attribute, valueObj);
                continue;
            }
            
            //normalize current value
            AttributeRange aRange = ranges.get(attribute);
            
            if(aRange != null){
                
                if(currValue < aRange.getMin() || currValue > aRange.getMax()){
                    logger.error("Unable to normalize value of "+currValue+" from sensor attribute of "+attribute+" because it falls outside of configured range of "+aRange+".  Therefore the value will not be used.");
                }else{
                    double norm = (currValue - aRange.getMin()) / aRange.getRange();
                    normalizedVals.put(attribute, new DoubleValue(attribute, norm));
                }
            }else{
                logger.error("Unable to normalize sensor attribute named = "+attribute+" because its Attribute Range was not found");
            }
        }
        
        return normalizedVals;
    }
}
