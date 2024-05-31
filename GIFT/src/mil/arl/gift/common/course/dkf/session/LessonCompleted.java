/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

/**
 * Contains information about the end of a lesson, aka DKF or knowledge session.
 * @author mhoffman
 *
 */
public class LessonCompleted {

    /**
     * Enumerated ways a lesson can be completed.
     * Note: must match LessonCompleted.proto
     * @author mhoffman
     *
     */
    public enum LessonCompletedStatusType{
        
        LEGACY_NOT_SPECIFIED,       /* the default for legacy instances that don't have this field (pre July 2021) */
        ERROR,                      /* An error occurred in GIFT and the lesson can no longer continue */
        LESSON_RULE,                /* A rule in the lesson (DKF) caused the lesson to end gracefully */
        LEARNER_ENDED_OBJECT,       /* The learner ended the course prematurely */
        LEARNER_ENDED_COURSE,       /* The learner ended the course object */
        CONTROLLER_ENDED_LESSON,    /* Some external controller outside of the modules ended the lesson (e.g. game master, RTA application) */
        INSTRUCTIONAL_STRATEGY_REQUEST; /* some instructional design ended the course prematurely, e.g. too many failed attempts, need to start lesson over */
    }
    
    /** enumerated status of the lesson being completed */
    private LessonCompletedStatusType statusType;
    
    /**
     * Set attribute
     * @param statusType enumerated status of the lesson being completed, can't be null.
     */
    public LessonCompleted(LessonCompletedStatusType statusType){
        
        if(statusType == null){
            throw new IllegalArgumentException("The statusType is null");
        }
        this.statusType = statusType;        
    }
    
    /**
     * Return the enumerated status of the lesson being completed
     * @return won't be null
     */
    public LessonCompletedStatusType getStatusType(){
        return statusType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[LessonCompleted: statusType = ");
        builder.append(statusType);
        builder.append("]");
        return builder.toString();
    }
    
    
}
