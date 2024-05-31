/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.coordinate;

import java.io.Serializable;

/**
 * This is the base class for GIFT coordinate representations.  It provides helper methods for coordinate conversions.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractCoordinate implements Serializable{    
    
    /**
     * The enumerated coordinate types 
     * @author mhoffman
     *
     */
    public enum CoordinateType{
        GCC,
        GDC,
        AGL
    }

    /**
     * Constructor (for gwt serialization)
     */
    public AbstractCoordinate() {}
    
    @Override
    public abstract String toString();
}
