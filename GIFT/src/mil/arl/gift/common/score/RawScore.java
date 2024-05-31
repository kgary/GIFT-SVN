/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;

/**
 * 
 * Defines the minimal interface for a RawScore.
 * 
 * @author cragusa
 */
public interface RawScore {
    
    /**
     * Get the value of the RawScore in string form.
     * @return the value in string form
     */
    String getValueAsString();
    
    /**
     * Get the units label of the RawScore.
     * @return the units label
     */
    String getUnitsLabel();
    
    /**
     * Get a user-friendly display string for this RawScore.
     * @return the display string.
     */
    String toDisplayString();
    
    /**
     * Notification that the score value is being overridden.  The new value is not known but
     * it can't be the initial value because the initial value is usually indicative of an event
     * never happening which is in itself a possible scored value.  A score being overridden usually
     * means that the parent to this score, usually a concept's graded score node, was changed manually
     * but no additional details where given at the raw score level under that node.  In most circumstances 
     * the implementation for this method will result in a score value such as 'overridden'.
     */
    void overrideValue();
    
}