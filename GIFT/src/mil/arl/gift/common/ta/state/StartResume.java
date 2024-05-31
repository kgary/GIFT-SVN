/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

/**
 * This is the start/resume simulation event
 * 
 * @author mhoffman
 *
 */
public class StartResume implements TrainingAppState {
       
    /**
     * The real-world time (UTC, milliseconds since midnight Jan 1, 1970) at
     * which the entity is to start/resume in the exercise. This information
     * is used by the simulation applications to start/resume an exercise
     * synchronously.
     */
    private long realWorldTime;

    /**
     * The simulation time (time of day in the simulated world in UTC,
     * milliseconds since midnight Jan 1, 1970) at which the entity will
     * start/resume in the exercise.
     */
    private long simulationTime;

    /**
     * Identifies the specific and unique start/resume request being made by
     * the Simulation Management.
     */
    private long requestID;
        
    /**
     * Class constructor - set class attributes
     * 
     * @param realWorldTime The real-world time (UTC, milliseconds since midnight Jan 1, 1970) at
     * which the entity is to start/resume in the exercise. This information
     * is used by the simulation applications to start/resume an exercise
     * synchronously.
     * @param simulationTime The simulation time (time of day in the simulated world in UTC,
     * milliseconds since midnight Jan 1, 1970) at which the entity will
     * start/resume in the exercise.
     * @param requestID Identifies the specific and unique start/resume request being made by
     * the Simulation Management.
     */
    public StartResume(long realWorldTime, long simulationTime, long requestID){
        this.realWorldTime = realWorldTime;
        this.simulationTime = simulationTime;
        this.requestID = requestID;
    }
    
    
    public long getRealWorldTime() {
        return realWorldTime;
    }

    public long getSimulationTime() {
        return simulationTime;
    }

    public long getRequestID() {
        return requestID;
    }
   
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[StartResume: ");
        sb.append(", Request ID = ").append(getRequestID());
        sb.append(", Real Time = ").append(getRealWorldTime());
        sb.append(", Simulation Time = ").append(getSimulationTime());
        sb.append("]");
        
        return sb.toString();
    }

}
