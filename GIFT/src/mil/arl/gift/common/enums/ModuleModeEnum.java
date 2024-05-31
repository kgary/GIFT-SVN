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
 * Enumeration of different module mode types. These types describe which mode the
 * module is running under.
 * 
 * @author cdettmering
 */
public class ModuleModeEnum extends AbstractEnum {
	
    private static final long serialVersionUID = 1L;
    
    private static List<ModuleModeEnum> enumList = new ArrayList<ModuleModeEnum>(2);
    private static int index = 0;

    /**
     * All of gift is running under a single process, so the module should play nice with other threads.
     */
    public static final ModuleModeEnum LEARNER_MODE = new ModuleModeEnum("LearnerMode", "Learner Mode (Single-process)");
    
    /**
     * This module has its own dedicated process.
     */
    public static final ModuleModeEnum POWER_USER_MODE = new ModuleModeEnum("PowerUserMode", "Power User Mode (Multi-process)");
    
    private ModuleModeEnum(String name, String displayName){
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
    public static ModuleModeEnum valueOf(String name)
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
    public static ModuleModeEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<ModuleModeEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
