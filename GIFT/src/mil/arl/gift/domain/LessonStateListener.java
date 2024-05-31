/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import mil.arl.gift.common.enums.LessonStateEnum;

/**
 * This is the interface for those listeners that want to be notified about changes in lesson state.
 * 
 * @author mhoffman
 *
 */
public interface LessonStateListener {

    /**
     * Callback for when changes in the lesson state occur
     * 
     * @param state The new lesson state
     */
    void onLessonStateChanged(LessonStateEnum state);
}
