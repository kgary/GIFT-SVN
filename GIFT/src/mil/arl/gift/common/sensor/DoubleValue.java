/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.sensor;

import mil.arl.gift.common.enums.SensorAttributeNameEnum;

/**
 * Represents a simple double sensor attribute value
 * 
 * @author mhoffman
 *
 */
public class DoubleValue extends AbstractSensorAttributeValue {
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the name of the sensor attribute
     * @param value - the value of the sensor attribute
     */
    public DoubleValue(SensorAttributeNameEnum name, double value) {
        super(name, value);

    }
    
    @Override
    public Number getNumber(){
        return (Number) value;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append(value);
        return sb.toString();
    }


}
