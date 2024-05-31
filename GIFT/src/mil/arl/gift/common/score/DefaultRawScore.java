/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;

import java.io.Serializable;

/**
 * 
 * Default String-based implementation of a RawScore. 
 * 
 * @author cragusa
 *
 */
public class DefaultRawScore extends AbstractRawScore implements Serializable {

    /** default */
    private static final long serialVersionUID = 1L;
    
    /** value to use to indicate that the score value was overridden, meaning something was changed (e.g. parent concept node assessment) and
     * the score value is not known */
    private static final String OVERRIDDEN_DEFAULT_VALUE = "overridden";
    
    /** string representation of the score */
    private String value;
    
    /**
     * Default constructor - needed for gwt serialization.
     */
    @SuppressWarnings("unused")
    private DefaultRawScore() {

    }
    
    /**
     * Constructor
     * 
     * @param value the value of the RawScore in string form. Null values are illegal.
     * @param unitsLabel a string to be used as a label for the value's units. (e.g. "seconds", "occurrences", etc.). Null values are illegal.
     */
    public DefaultRawScore(String value, String unitsLabel) {
        super(unitsLabel);
        
        if(value == null) {            
            throw new IllegalArgumentException("The value can't be null.");
        }
        
        this.value = value;
    }

    @Override
    public String getValueAsString() {
        return value;
    }    

    @Override
    public void overrideValue() {
        this.value = OVERRIDDEN_DEFAULT_VALUE;
    }
     
    @Override
    public String toDisplayString() { 
        StringBuilder sb = new StringBuilder();
        sb.append(getValueAsString()).append(" ").append(getUnitsLabel());
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("value = ").append(getValueAsString());
        sb.append(" units = ").append(getUnitsLabel());
        return sb.toString();
    }
}
