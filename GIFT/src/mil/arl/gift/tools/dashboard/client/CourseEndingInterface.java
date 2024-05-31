/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client;

import mil.arl.gift.tools.dashboard.client.UiManager.EndCourseReason;


/**
 * The CourseEndingInterface class defines the functions that are required to implement during the end course process.
 * The general flow for course ending is:
 *     - handleCourseEnding()
 *          The course begins the process of ending & unloading.  The TUI should shutdown the course and send the 'END_COURSE' message back to the dashboard.
 *     - handleCourseEndingTimerExpired() OR handleCourseEnded()
 *          Once the dashboard gets the END_COURSE message, then the handleCourseEnded() method is called.  If the TUI for some reason does not respond, the
 *          timer will expire and the handleCourseEndingTimerExpired() method will be called instead which means we did not receive the END_COURSE message
 *          back from the TUI.
 * 
 * @author nblomberg
 *
 */
public interface CourseEndingInterface {
   
    /**
     * The handler for when the course is in the process of ending.
     */
    public void handleCourseEnding();
    
    /**
     * The handler for when the course has ended because the timer expired, which
     * means that the TUI did not respond to the dashboard in time with the END_COURSE message.
     * In this case, we will need to 'force end' the course and cleanup.
     * 
     * @param reason - the reason the course has ended.
     */
    public void handleCourseEndingTimerExpired(EndCourseReason reason);
    
    /**
     * The handler for when the course has ended.  In this case, the TUI has sent the END_COURSE message
     * which means the TUI has successfully cleaned up and ended the course.
     * 
     * @param reason - the reason the course has ended.
     */
    public void handleCourseEnded(EndCourseReason reason);
   
}
