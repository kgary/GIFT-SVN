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
 * Enumeration of the various module states.
 * 
 * @author mhoffman
 *
 */
public class ModuleStateEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<ModuleStateEnum> enumList = new ArrayList<ModuleStateEnum>(2);
    private static int index = 0;

    /** The state is not known or not provided */
    public static final ModuleStateEnum UNKNOWN = new ModuleStateEnum("Unknown", "Unknown");
    
    /** The module is behaving normally */
    public static final ModuleStateEnum NORMAL = new ModuleStateEnum("Normal", "Normal");
    
    /** The module is overloaded at this time */
    public static final ModuleStateEnum OVERLOADED = new ModuleStateEnum("Overloaded", "Overloaded");
    
    /** The module has reached a critical failure state and is no longer executing properly */
    public static final ModuleStateEnum FAILED = new ModuleStateEnum("Failed", "Failed");

    
    private ModuleStateEnum(String name, String displayName){
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
    public static ModuleStateEnum valueOf(String name)
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
    public static ModuleStateEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<ModuleStateEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
