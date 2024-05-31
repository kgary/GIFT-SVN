/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;

/**
 * The range of a slider
 *
 * @author jleonard
 */
public class SliderRange implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** Enum that is repsonsible for providing a mapping between the GWT ScaleType value
     *  and the GIFT equivalent, representing the type of scale growth
     *  
     * @author cpolynice
     */
    public enum ScaleType {
        /* Linear scale type, ensuring values increase sequentially */
        LINEAR,
        
        /* Logarithmic scale type, ensuring values increase with respect to log */
        LOGARITHMIC
    }

    /** minValue holds the smallest number the slider can be */
    private double minValue;
    
    /** maxValue holds the largest number the slider can be */
    private double maxValue;

    /** stepSize holds the amount by which the slide bar should move */
    private double stepSize;
    
    /** scaleType holds the type of scale that is being used for slider*/
    private ScaleType scaleType;
    
    /**
     * 
     * Default Constructor
     *
     * Required to exist and be public for GWT compatibility
     */
    public SliderRange() {
    }

    /**
     * Constructor
     *
     * @param minValue The minimum value of the slider
     * @param maxValue The maximum value of the slider
     */
    public SliderRange(double minValue, double maxValue) {
        
        if(minValue > maxValue) {
            
            throw new IllegalArgumentException("minValue cannot be greater than maxValue");
        }

        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    /**
     * Gets the minimum value that can be returned by the slider
     *
     * @return double The minimum value that can be returned by the slider
     */
    public double getMinValue() {

        return minValue;
    }

    /**
     * Gets the maximumValue that can be returned by the slider
     *
     * @return double the maximum value that can be returned by the slider
     */
    public double getMaxValue() {

        return maxValue;
    }
    
    /**
     * Gets the stepSize that can be returned by the slider
     *
     * @return double the step size that can be returned by the slider
     */
    public double getStepSize() {

        return stepSize;
    }
    
    /**
     * Gets the scaleType that can be returned by the slider
     *
     * @return String the scale type that can be returned by the slider
     */
    public ScaleType getScaleType() {

        return scaleType;
    }
    
    
    
    /**
     * @param minValue the minValue to set
     */
    public void setMinValue(double minValue) {
        this.minValue = minValue;
    }

    /**
     * @param maxValue the maxValue to set
     */
    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
    }
    
    /**
     * Assures that stepSize if a double where the slider can be moved
     * 
     * @param stepSize the stepSize to set
     */
    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    /**
     * Set the scale type value that indicates the step increment size
     * 
     * @param scaleType the scaleType to set
     */
    public void setScaleType(ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[SliderRange: ");
        sb.append("minValue = ").append(getMinValue());
        sb.append(", maxValue = ").append(getMaxValue());
        sb.append(", stepSize = ").append(getStepSize());
        sb.append(", scaleType = ").append(getScaleType());

        return sb.toString();
    } 
    
}
