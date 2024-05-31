/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.EnumerationNotFoundException;


/**
 * Enumeration of the various Training Application states
 * 
 * @author jleonard
 *
 */
public class TrainingApplicationStateEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<TrainingApplicationStateEnum> enumList = new ArrayList<TrainingApplicationStateEnum>(6);
    private static int index = 0;

    public static final TrainingApplicationStateEnum LOADED = new TrainingApplicationStateEnum("Loaded", "Loaded");
    public static final TrainingApplicationStateEnum STOPPED = new TrainingApplicationStateEnum("Stopped", "Stopped");
    public static final TrainingApplicationStateEnum PAUSED = new TrainingApplicationStateEnum("Paused", "Paused");
    public static final TrainingApplicationStateEnum RUNNING = new TrainingApplicationStateEnum("Running", "Running");

    
    private TrainingApplicationStateEnum(String name, String displayName){
    	super(index++, name, displayName);
    	enumList.add(this);
    }
    
    /**
     * Return the enumeration object that has the matching name.
     * @param name The name of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         name is not found.
     */
    public static TrainingApplicationStateEnum valueOf(String name)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(name, VALUES());
    }

    /**
     * Return the enumeration object that has the matching value.
     * @param value The value of the enumeration to find.
     * @return The matching enumeration.
     * @throws EnumerationNotFoundException if an enumeration with a matching
     *         value is not found.
     */
    public static TrainingApplicationStateEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<TrainingApplicationStateEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
    
    /**
     * Return whether the training application state transition is a valid change.
     * 
     * @param from the current state
     * @param too the desired state to change too
     * @return boolean if the state change is a valid transition
     */
    public static boolean isValidTransition(TrainingApplicationStateEnum from, TrainingApplicationStateEnum too){
        
        if(from == null){            
            return too == LOADED || too == STOPPED;
        }else if(from == too){
            return true;
            
        }else if(from == LOADED && (too == STOPPED || too == RUNNING)){
            return true;
        }else if(from == STOPPED && too == null){
            return true;
        }else if(from == PAUSED && (too == STOPPED || too == RUNNING)){
            return true;
        }else if(from == RUNNING && (too == STOPPED || too == PAUSED)){
            return true;
        }
        
        return false;
    }
}
