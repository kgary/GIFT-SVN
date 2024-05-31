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
 * Abstract superclass for RawScore classes.
 * 
 * @author cragusa
 */
public abstract class AbstractRawScore implements RawScore, Serializable {

    /** default */
    private static final long serialVersionUID = 1L;
    
    /** the label for the units of the score value */
    private String unitsLabel;    
    
    /**
     * Default constructor - needed for gwt serialization.
     */
    public AbstractRawScore() {
    }
    
    /**
     * Constructor
     * @param unitsLabel  the label for the units of the score value. should not be null
     */
    protected AbstractRawScore(String unitsLabel) {
        
        if(unitsLabel == null) {  
            throw new IllegalArgumentException("The units label can't be null.");
        }
        
        this.unitsLabel = unitsLabel;
    }

    @Override
    public String getUnitsLabel() {        
        return unitsLabel;
    }
}
