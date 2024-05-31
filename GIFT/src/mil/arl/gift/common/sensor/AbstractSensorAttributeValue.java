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
 * This is the base class for a sensor attribute value.  It contains all the getter methods for every type
 * of object that a sensor attribute value can be.  The implementation classes are responsible for overriding
 * any getter methods that they support in order for users of this class to obtain the appropriate class value.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractSensorAttributeValue {
    
    /** the name of this sensor attribute */
    private SensorAttributeNameEnum name;
    
    /** the value of this sensor attribute */
    protected Object value;
    
    /**
     * Class constructor - set attributes.
     * 
     * @param name - the name of the attribute
     * @param value - the value of the attribute
     */
    public AbstractSensorAttributeValue(SensorAttributeNameEnum name, Object value){
        
        if(name == null){
            throw new IllegalArgumentException("The name can't be null");
        }
        
        this.name = name;
        
        if(value == null){
            throw new IllegalArgumentException("The value can't be null");
        }
        
        this.value = value;
    }
    
    /**
     * Return the name of this sensor attribute.
     * 
     * @return SensorAttributeNameEnum
     */
    public SensorAttributeNameEnum getName(){
        return name;
    }
    
    /**
     * Return the value object for this attribute.
     * 
     * @return Object
     */
    public Object getValue(){
        return value;
    }
    
    @Override
    public String toString(){
        
        return "name = " + name;
    }
    
    /**
     * Returns whether or not the value for the attribute is a number (i.e. int, double, etc.) and not
     * a more complex object containing numbers.
     * 
     * @return boolean
     */
    public boolean isNumber(){
        return value instanceof Number;
    }
    
    /**
     * Type specific get methods 
     */
    
    /**
     * Returns the value as a number.
     * 
     * @return Number
     */
    public Number getNumber(){
        throw new UnsupportedOperationException("Unable to return a number");
    }
    
    /**
     * Return the value as a String.
     * 
     * @return String
     */
    public String getString(){
        throw new UnsupportedOperationException("Unable to return a string");
    }
    
    /**
     * Gets the string representation of the all the data for this object
     * 
     * The default behavior is to use the toString method, but if generating
     * the complete string representation of an object is going to be a big
     * task, override this method with the complete functionality and do the
     * lightweight representation in toString
     * 
     * @return String The string representation of this object 
     */
    public String toDataString() {
        
        // For most cases, the toString return value will suffice
        return toString();
    }
}
