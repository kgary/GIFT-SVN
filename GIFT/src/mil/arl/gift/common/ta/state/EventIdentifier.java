/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;


/**
 * This class contains a unique identifier for an event
 * 
 * @author mhoffman
 *
 */
public class EventIdentifier implements TrainingAppState {

    /** the event's unique id */
    private int eventID;
    
    /** the simulation address of this event */
    private SimulationAddress simAddr;

    /**
     * Class constructor 
     * 
     * @param simAddr the simulation address of this event
     * @param eventID the event's unique id 
     */
    public EventIdentifier(SimulationAddress simAddr, int eventID){
        this.simAddr = simAddr;
        this.eventID = eventID;
    }

    public SimulationAddress getSimulationAddress(){
        return simAddr;
    }

    public int getEventID(){
        return eventID;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[EventIdentifier: ");
        sb.append("Event ID = ").append(getEventID());
        sb.append(", Simulation Address = ").append(getSimulationAddress().toString());
        sb.append("]");

        return sb.toString();
    }

}
