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
 * This class contains the various enumerated damage types
 *
 * @author mhoffman
 *
 */
public class DamageEnum extends AbstractEnum {

    /** contains list of enum instances */
    private static List<DamageEnum> enumList = new ArrayList<>(2);
    
    /** index of last enum to be created */
    private static int index = 0;

    /** no damage enum */
    public static final DamageEnum HEALTHY = new DamageEnum("Healthy", "Healthy");
    
    /** small amount of damage enum */
    public static final DamageEnum SLIGHT_DAMAGE = new DamageEnum("SlightDamage", "Slight Damage");
    
    /** medium amount of damage enum */
    public static final DamageEnum MODERATE_DAMAGE = new DamageEnum("ModerateDamage", "Moderate Damage");
    
    /** dead damage enum */
    public static final DamageEnum DESTROYED = new DamageEnum("Destroyed", "Destroyed");
    
    /** default id */
    private static final long serialVersionUID = 1L;

    /**
     * default - required for GWT serialization
     */
    private DamageEnum() {
    }

    /**
     * Create enumeration
     * @param name the unique name of this enumeration instance among this enumeration type
     * @param displayName the display name of this enumeration instance
     */
    private DamageEnum(String name, String displayName){
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
    public static DamageEnum valueOf(String name)
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
    public static DamageEnum valueOf(int value)
        throws EnumerationNotFoundException {
        return AbstractEnum.valueOf(value, VALUES());
    }

    /**
     * Returns a List of the currently defined enumerations.
     * @return a List of the currently defined enumerations.
     */
    public static final List<DamageEnum> VALUES() {
        return Collections.unmodifiableList(enumList);
    }
}
