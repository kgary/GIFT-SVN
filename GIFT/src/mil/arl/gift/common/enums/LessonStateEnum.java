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
 * Enumeration of the various lesson states
 * 
 * @author jleonard
 *
 */
public class LessonStateEnum extends AbstractEnum {

    private static final long serialVersionUID = 1L;
    
    private static List<LessonStateEnum> enumList = new ArrayList<LessonStateEnum>(6);
    private static int index = 0;

    public static final LessonStateEnum LOADED = new LessonStateEnum("Loaded", "Loaded");
    public static final LessonStateEnum STOPPED = new LessonStateEnum("Stopped", "Stopped");
    public static final LessonStateEnum PAUSED = new LessonStateEnum("Paused", "Paused");
    public static final LessonStateEnum RUNNING = new LessonStateEnum("Running", "Running");

    
    private LessonStateEnum(String name, String displayName){
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
    public static LessonStateEnum valueOf(String name)
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
    public static LessonStateEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<LessonStateEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
