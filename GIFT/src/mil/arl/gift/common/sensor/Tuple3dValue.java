/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.sensor;

import javax.vecmath.Tuple3d;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;

/**
 * This class represents a javax.vecmath.Tuple3d sensor attribute value.
 * 
 * @author mhoffman
 *
 */
public class Tuple3dValue extends AbstractSensorAttributeValue {

    /**
     * Class constructor - set attributes
     * 
     * @param name - the name of the attribute
     * @param tuple3d - the value of the attribute
     */
    public Tuple3dValue(SensorAttributeNameEnum name, Tuple3d tuple3d) {
        super(name, tuple3d);
    }
    
    public double getX(){
        return getTuple3d().getX();
    }
    
    public double getY(){
        return getTuple3d().getY();
    }
    
    public double getZ(){
        return getTuple3d().getZ();
    }

    public Tuple3d getTuple3d(){
        return (Tuple3d)value;
    }    

    @Override
    public boolean isNumber() {
        return false;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append(getX()).append(", ").append(getY()).append(", ").append(getZ());
        return sb.toString();
    }
}
