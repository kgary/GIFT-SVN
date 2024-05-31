/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * Interface for actions that can be taken as feedback
 *
 * @author jleonard
 */
public interface FeedbackAction {

    /**
     * Returns if this feedback has audio that plays
     *
     * @return boolean If this feedback has audio that plays
     */
    boolean hasAudio();
}
