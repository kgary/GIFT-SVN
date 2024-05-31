/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert;

/**
 * Interface for getting the minimum and maximum values of a property
 *
 * @author jleonard
 */
public interface MinMaxProperty {
    
    /**
     * Gets the minimum value of the property
     * 
     * @return Long The minimum value of the property
     */
    Long getMinimum();
    
    /**
     * Gets the maximum value of the property
     * 
     * @return Long The maximum value of the property
     */
    Long getMaximum();
}
