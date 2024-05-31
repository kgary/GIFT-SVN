/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

/**
 * Interface for being notified when a property changes
 *
 * @author jleonard
 */
public interface PropertyChangeListener {

    /**
     * Callback for when a property has changed, used to notify that views
     * should be redrawn
     */
    void onPropertyChange();
}
