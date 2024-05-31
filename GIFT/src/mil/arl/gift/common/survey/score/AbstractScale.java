/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.util.Date;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * Contains the attribute, value pairing associated with a survey score scale attribute.
 *
 * @author mhoffman
 */
public abstract class AbstractScale {

    /** scale attribute */
    private LearnerStateAttributeNameEnum attribute;

    /** (raw) value associated with the scale (e.g. 0.78, 51) */
    private double rawValue;    

    /** enumerated value associated with the attribute */
    protected AbstractEnum value;
    
    /** The time at which the scale was computed */
    private Date timeStamp = new Date();
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param attribute - the attribute associated with the scale
     * @param value - the raw score value.  This could be the total points earned for this attribute from a scored
     * survey.  Can be useful for ERT report generation, showing when reviewing survey results.
     */
    public AbstractScale(LearnerStateAttributeNameEnum attribute, double value){
        
        if(attribute == null){
            throw new IllegalArgumentException("The attribute can't be null.");
        }
        this.attribute = attribute;
        
        this.rawValue = value;
    }
    
    /**
     * Return the scale attribute name
     * 
     * @return LearnerStateAttributeNameEnum
     */
    public LearnerStateAttributeNameEnum getAttribute() {
        return attribute;
    }

    /**
     * Return the (raw) value associated with the scale (e.g. 0.78, 51)
     * 
     * @return double
     */
    public double getRawValue() {
        return rawValue;
    }    
    
    /**
     * Return the attribute value for the scale
     * 
     * @return AbstractEnum
     */
    public AbstractEnum getValue(){
        return value;
    }

    /**
     * Getter for the timeStamp field. Represents the time that the 
     * scale was computed.
     * @return the value of the timeStamp field
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * Setter for the timeStamp field. Represents the time that the 
     * scale was computed.
     * @param timeStamp the new value of the timeStamp field, can't be null
     */
    public void setTimeStamp(Date timeStamp) {
        if(timeStamp == null) {
            throw new IllegalArgumentException("timeStamp can't be null");
        }
        
        this.timeStamp = timeStamp;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("attribute = ").append(getAttribute());
        sb.append(", value = ").append(getValue());
        sb.append(", rawValue = ").append(getRawValue());

        return sb.toString();
    }
}
