/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import mil.arl.gift.common.enums.ArticulationParameterTypeDesignatorEnum;



/**
 * This message represents an articulation parameter.
 *
 * @author dscrane
 *
 */
public class ArticulationParameter implements TrainingAppState { 
    
    private ArticulationParameterTypeDesignatorEnum parameterTypeDesignator;
    private int parameterChange;
    private int partAttachedToID;
    private int parameterType;
    private double parameterValue;
    
    /**
     * Class constructor - set class attributes
     * @param parameterTypeDesignator The articulation parameter type designator.
     * @param parameterChange Indicates a change in the articulation parameter.
     * @param partAttachedToID ID for the part this articulation parameter is attached to.
     * @param parameterType Specifies the type of parameter represented.
     * @param parameterValue The value of the parameter.
     */
    public ArticulationParameter(ArticulationParameterTypeDesignatorEnum parameterTypeDesignator,
                                 int parameterChange,
                                 int partAttachedToID,
                                 int parameterType,
                                 double parameterValue) {

        this.parameterTypeDesignator = parameterTypeDesignator;
        this.parameterChange = parameterChange;
        this.partAttachedToID = partAttachedToID;
        this.parameterType = parameterType;
        this.parameterValue = parameterValue;
    }

    public ArticulationParameterTypeDesignatorEnum getParameterTypeDesignator() {
        
        return this.parameterTypeDesignator;
    }
    
    public int getParameterChange() {
        
        return this.parameterChange;
    }
    
    public int getPartAttachedToID() {
        
        return this.partAttachedToID;
    }
    
    public int getParameterType() {
        
        return this.parameterType;
    }
    
    public double getParameterValue() {
        
        return this.parameterValue;
    }

    /**
     * Decodes the articulation parameter's low bits according to
     * SISO-REF-010-2011.1-RC2 sec 4.8.2 and 4.8.3
     * @return The articulation parameter's field value (type metric)
     */
    public int decodeParameterTypeLowBits() {
        
        return this.getParameterType() & 0x1f;
    }
   
    /**
     * Decodes the articulation parameter's high bits according to
     * SISO-REF-010-2011.1-RC2 sec 4.8.3
     * @return The articulation parameter's index (type class)
     */
    public int decodeParameterTypeHighBits() {

        return this.getParameterType() ^ this.decodeParameterTypeLowBits();
    }
    
    /**
     * Decodes the articulation parameter's value
     * @return Floating point value of the articulation parameter
     */
    public float decodeParameterValue() {
        
        return Float.intBitsToFloat((int)(Double.doubleToRawLongBits(this.getParameterValue()) >>> 32));
    }
    
    @Override
    public String toString() {
        
        StringBuilder sb = new StringBuilder();
        sb.append("[ArticulationParameter: ");
        sb.append("Parameter Type Designator = ").append(getParameterTypeDesignator());
        sb.append(", Parameter Change = ").append(getParameterChange());
        sb.append(", Part Attached To ID = ").append(getPartAttachedToID());
        sb.append(", Parameter Type = ").append(getParameterType());
        sb.append(", Parameter Value = ").append(getParameterValue());
        sb.append("]");
        
        return sb.toString();
    }
}
