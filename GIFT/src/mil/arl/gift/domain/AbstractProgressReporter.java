/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

import mil.arl.gift.common.AbstractRuntimeParameters;
import mil.arl.gift.common.lti.LtiRuntimeParameters;
import mil.arl.gift.domain.lti.LtiProgressReporter;

/**
 * The AbstractProgressReporter is the base class which allows the course manager to optionally report progress of the
 * learner in the GIFT course.  The class is abstract to allow different mechanisms of where the progress is reported to.
 * The first use case of this is for LTI, such that GIFT can report the progress back as a score value to an LTI tool consumer.  
 * However the intent is that different courses could potentially send the progress to different systems.   
 * 
 * This class also allows the abstraction of the progress reporting logic from the CourseManager.  The course manager then doesn't
 * need implementation specific details of how the progress is reported, but keeps the logic abstracted away from the course manager.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractProgressReporter {
    
    
    /**
     * Constructor -- default
     */
    public AbstractProgressReporter() {
        
    }

    /**
     * Used to report the progress of the course.  The progress is measured between two values, 
     * the current progress and the maxiumum progress that a user can reach in the course.  This is based
     * on the original number of transitions in a course (excluding dynamic transitions that come from adaptive
     * courseflow).  The current progress represents the current transition that the user is on, whereas the maximum 
     * progress represents the final transition.
     * 
     * @param currentProgress The current progress that the learner is in for the course.
     * @param maxProgress The maximum progress that can be reached by the learner.
     * @param isFinalProgressReport whether this is the final notification of progress because the course is ending
     */
    public abstract void reportProgress(int currentProgress, int maxProgress, boolean isFinalProgressReport);
    
    
    /**
     * Static method to create a ProgressReporter object based on the runtime parameters that have been passed into the 
     * domain session.  
     * 
     * @param params The runtime parameters that were passed into the domain session (can be null).
     * @return The created progress reporter that will be used to report progress.  Null can be returned if there 
     * is no progress reporter to be used.
     */
    static AbstractProgressReporter createProgressReporterByRuntimeParams(AbstractRuntimeParameters params) {
        AbstractProgressReporter reporter = null;
        
        if (params != null) {
            if (params instanceof LtiRuntimeParameters) {
                
                // Create and initialize the lti progress reporter class.
                LtiProgressReporter ltiReporter = new LtiProgressReporter();
                ltiReporter.setLtiRuntimeParameters((LtiRuntimeParameters)params);
                reporter = ltiReporter;
            }
        }
        
        return reporter;
    }

}
