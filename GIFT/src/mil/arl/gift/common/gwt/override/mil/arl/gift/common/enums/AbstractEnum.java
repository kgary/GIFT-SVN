/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.io.Serializable;
import java.util.List;
import mil.arl.gift.common.EnumException;
import mil.arl.gift.common.EnumerationNotFoundException;
import mil.arl.gift.common.enums.AbstractEnum;

/**
 * This class maintains the base information for a enumeration. Enumeration
 * classes must extend this class.
 *
 * This class overrides the original Abstract Enum class at
 * mil/arl/gift/common/enums/AbstractEnum.java Any changes made to this class
 * must be also be made to that class as well.
 *
 * @author jleonard
 */
public abstract class AbstractEnum implements Serializable, Comparable<AbstractEnum> {

    //the integer value of this enumeration
    private int value;

    //the string name of this enumeration
    private String name;

    //the display name of this enumeration
    private String displayName;
    
    /**
     * Default constructor - required for GWT.
     */
    public AbstractEnum(){
        
    }

    /**
     * Class constructor
     *
     * @param value - index of this enum in the implementation class'es list of enumerations
     * @param name - the unique name of this enumeration
     * @param displayName - the display name of this enumeration
     */
    public AbstractEnum(int value, String name, String displayName) {
        
        if(name == null){
            throw new IllegalArgumentException("The name can't be null.");
        }else if(displayName == null){
            throw new IllegalArgumentException("The display name can't be null.");
        }
        
        this.value = value;
        this.name = name;
        this.displayName = displayName;
    }

    /**
     * Return the integer value of this enumeration
     *
     * @return int
     */
    public int getValue() {
        return value;
    }

    /**
     * Return the string name of this enumeration
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Return the display name of this enumeration
     *
     * @return String
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Set the display name of this enumeration
     *
     * @param displayName - display name of this enumeration
     */
    protected void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * This is a centralized method of locating an enumeration object using the
     * enumeration class name (fully qualified) and the enumeration instance
     * name.
     *
     * @param enumName The name value of the enumeration instance to retrieve.
     *
     * @return The enumeration class instance.
     *
     * @exception EnumException Description of the Exception
     * @throws EnumerationNotFoundException if enumName does not exist in enum.
     */
    public static <E extends AbstractEnum> E valueOf(String enumName, List<E> values) throws EnumerationNotFoundException {
        
        if(enumName == null){
            throw new EnumerationNotFoundException("Unable to find the enumeration for null in "+values+".", enumName, null);
        }

        // First match to name.
        for (E an_enum : values) {

            if (enumName.equals(an_enum.getName())) {

                return an_enum;
            }
        }

        // Failed to match name, try match to displayName.
        for (E an_enum : values) {

            if (enumName.equals(an_enum.getDisplayName())) {

                return an_enum;
            }
        }

        throw new EnumerationNotFoundException("Unable to find the enumeration named "+enumName+" in "+values+".", enumName, null);
    }

    /**
     * This is a centralized method of locating an enumeration object using the
     * enumeration class name (fully qualified) and the enumeration instance
     * value.
     *
     * @param enumValue The value of the enumeration instance to retrieve.
     *
     * @return The enumeration class instance.
     *
     * @exception EnumException Description of the Exception
     * @throws EnumerationNotFoundException if enumName does not exist in enum.
     */
    public static <E extends AbstractEnum> E valueOf(int enumValue, List<E> values)
            throws EnumException {

        // see if we can directly get the entry first...
        if (enumValue >= 0 && values.size() > enumValue) {
            E anEnum = values.get(enumValue);
            if (enumValue == anEnum.getValue()) {
                return anEnum;
            }
        }

        for (E anEnum : values) {

            if (enumValue == anEnum.getValue()) {

                return anEnum;
            }
        }

        return null;
    }
    
    @Override
    public int compareTo(AbstractEnum otherEnum){
        return getName().compareTo(otherEnum.getName());
    }
    
    @Override
    public boolean equals(Object otherEnum){
        
        if(otherEnum != null && otherEnum instanceof AbstractEnum){
            AbstractEnum otherAbsEnum = (AbstractEnum) otherEnum;
            return this.value == otherAbsEnum.getValue() && this.name.equals(otherAbsEnum.getName()) &&
                    this.displayName.equals(otherAbsEnum.getDisplayName());
        }
        
        return false;
    }
    
    @Override
    public int hashCode(){
        
        int hash = 3;
        hash = 53 * hash + this.value;
        hash += this.name.hashCode();
        hash += this.displayName.hashCode();
        return hash;
    }


    /**
     * Returns a String representation of this enumeration. The String
     * representation matches the value returned by getName().
     *
     * @return String - string description of this enumeration.
     */
    @Override
    public String toString() {

        return name;
    }
}
