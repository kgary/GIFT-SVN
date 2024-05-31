/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

/**
 * This class represents a stop/freeze simulation event
 * Note: most of the attributes in this class were taken from the DIS StopFreeze PDU.
 * 
 * @author mhoffman
 *
 */
public class StopFreeze implements TrainingAppState {

	/**
     * The real-world time (UTC, milliseconds since midnight Jan 1, 1970) at
     * which the entity is to start/resume in the exercise. This information
     * is used by the simulation applications to start/resume an exercise
     * synchronously.
     */
    private long realWorldTime;

    /** Indicates the reason that an entity or exercise was stopped/frozen. */
    /**
        0    Other
        1   Recess
        2   Termination
        3   System Failure
        4   Security Violation
        5   Entity Reconstitution
        6   Stop for reset
        7   Stop for restart
        8   Abort Training Return to Tactical Operation
     */
    private Integer reason;
    
    /** RECESS = paused training application */
    public static final int RECESS = 1;
    
    public static final int TERMINATION = 2;
    public static final int SYSTEM_FAIL = 3;
    public static final int STOP_FOR_RESET = 6;
    public static final int STOP_FOR_RESTART = 7;

    /**
     * Indicates the internal behavior of the simulation and its appearance
     * while frozen to the other participants of the exercise.
     */
    /**
        0  Run internal simulation clock
        1   Transmit PDUs.
        2   Update simulation models of other entities via received PDUs.
        3   Continue Transmit PDU
        4   Cease Update Simulation Models of Other Entities Via Received PDUs
        5   Continue Update Simulation Models of Other Entities Via Received PDUs
     */
    private Integer frozenBehavior;

    /**
     * Identifies the specific and unique stop/freeze request being made by
     * the Simulation Management.
     */
    private long requestID;

    /**
     * Class constructor - set class attributes
     * 
     * @param realWorldTime  The real-world time (UTC, milliseconds since midnight Jan 1, 1970) at
     * which the entity is to start/resume in the exercise. This information
     * is used by the simulation applications to start/resume an exercise
     * synchronously.
     * @param reason Indicates the reason that an entity or exercise was stopped/frozen.
     * @param frozenBehavior Indicates the internal behavior of the simulation and its appearance
     * while frozen to the other participants of the exercise.
     * @param requestID Identifies the specific and unique stop/freeze request being made by
     * the Simulation Management.
     */
    public StopFreeze(long realWorldTime, Integer reason, Integer frozenBehavior, long requestID){
        this.realWorldTime = realWorldTime;
        this.reason = reason;
        this.frozenBehavior = frozenBehavior;
        this.requestID = requestID;
    }


    public long getRealWorldTime() {
        return realWorldTime;
    }

    public Integer getReason(){
        return reason;
    }

    public Integer getFrozenBehavior(){
        return frozenBehavior;
    }

    public long getRequestID() {
        return requestID;
    }

    /**
     * Return whether or not the StopFreeze specifies that the training application has paused.
     * 
     * @return boolean
     */
    public boolean isPaused(){
        return getReason() == RECESS;
    }
    
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[StopFreeze: ");
        sb.append(", Request ID = ").append(getRequestID());
        sb.append(", Real Time = ").append(getRealWorldTime());
        sb.append(", Reason = ").append(getReason());
        sb.append(", Frozen Behavior = ").append(getFrozenBehavior());
        sb.append("]");

        return sb.toString();
    }
}
