/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped.common;

import java.util.List;

import mil.arl.gift.common.course.transition.LearnerStateTransition;
import mil.arl.gift.common.state.LearnerState;

/**
 * This is the interface that all transition selection classes should implement.  A transition selection
 * class has logic to filter the list of transitions that have been detected to be satisfied by the
 * latest learner state.
 * 
 * @author mhoffman
 *
 */
public interface TransitionSelectionInterface {

    /**
     * Filter the collection of transitions that have been detected to be satisfied by the latest learner state.
     * 
     * @param state the latest learner state information
     * @param transitions collection of learner state transitions that are satisfied by the learner state.
     */
    public void filter(LearnerState state, List<LearnerStateTransition> transitions);
}
