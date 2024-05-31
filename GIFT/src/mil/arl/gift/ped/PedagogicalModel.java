/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped;

import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.InitializeDomainSessionRequest;
import mil.arl.gift.common.InitializePedagogicalModelRequest;
import mil.arl.gift.common.PedagogicalRequest;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.state.LearnerState;

/**
 * Interface that defines a Pedagogical Model
 * 
 * @author cragusa
 *
 */
public interface PedagogicalModel {
    
    /**
     * Given a learner state object, computes and returns the appropriate pedagogical action(s).
     * 
     * @param state state of the learner
     * @param request the pedagogical request contains pedagogical actions to take
     */
    void getPedagogicalActions(LearnerState state, PedagogicalRequest request);    
    
    /**
     * Initializes the pedagogical model. 
     * 
     * @param initDomainSessionRequest  the domain session request that triggers the initialization.
     */
    void initialize(InitializeDomainSessionRequest initDomainSessionRequest);
    
    /**
     * Initializes the pedagogical model. 
     * 
     * @param initModelRequest  the lesson request that triggers the initialization.
     * @throws DetailedException - if there was initialization issue
     */
    void initialize(InitializePedagogicalModelRequest initModelRequest) throws DetailedException;
    
    /**
     * Handle a course state update.
     * 
     * @param state - update about the state of the course.
     * @return ped request based on the new course state. Can be null or contain no actions to take.
     */
    PedagogicalRequest handleCourseStateUpdate(CourseState state);
    
    /**
     * Notification that the lesson (DKF) has started in a GIFT course. 
     */
    void handleLessonStarted();
}
