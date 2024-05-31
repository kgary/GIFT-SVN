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
 * Enumeration of the various Sensor States
 * 
 * @author mhoffman
 *
 */
public class SensorStateEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<SensorStateEnum> enumList = new ArrayList<SensorStateEnum>(3);
    private static int index = 0;

    public static final SensorStateEnum READY = new SensorStateEnum("READY", "Ready");
    public static final SensorStateEnum RUNNING = new SensorStateEnum("RUNNING", "Running");
    public static final SensorStateEnum STOPPED = new SensorStateEnum("STOPPED", "Stopped");
    
    private SensorStateEnum(String name, String displayName){
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
    public static SensorStateEnum valueOf(String name)
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
    public static SensorStateEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<SensorStateEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
