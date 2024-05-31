/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import mil.arl.gift.common.enums.TrainingApplicationStateEnum;

/**
 * Listener for changes in the state of a training application
 *
 * @author jleonard
 */
public interface TrainingApplicationStateListener {
    
    /**
     * Callback for when changes in the training application occur
     * 
     * @param state The new training application state
     */
    void onTrainingAppStateChanged(TrainingApplicationStateEnum state);
    
}
