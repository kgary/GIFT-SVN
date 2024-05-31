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
 * Enumeration of the various SIMAN operations
 * 
 * @author jleonard
 *
 */
public class SimanTypeEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<SimanTypeEnum> enumList = new ArrayList<SimanTypeEnum>(6);
    private static int index = 0;

    public static final SimanTypeEnum LOAD = new SimanTypeEnum("Load", "Load");
    public static final SimanTypeEnum START = new SimanTypeEnum("Start", "Start");
    public static final SimanTypeEnum STOP = new SimanTypeEnum("Stop", "Stop");
    public static final SimanTypeEnum PAUSE = new SimanTypeEnum("Pause", "Pause");
    public static final SimanTypeEnum RESUME = new SimanTypeEnum("Resume", "Resume");
    public static final SimanTypeEnum RESTART = new SimanTypeEnum("Restart", "Restart");

    
    private SimanTypeEnum(String name, String displayName){
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
    public static SimanTypeEnum valueOf(String name)
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
    public static SimanTypeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<SimanTypeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
