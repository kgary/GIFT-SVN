/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.enums;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import mil.arl.gift.common.EnumException;
import mil.arl.gift.common.EnumerationNotFoundException;

/**
 * This class maintains the base information for a enumeration. Enumeration
 * classes must extend this class.
 * 
 * NOTE: There is a GWT-safe version of this class in 
 * mil/arl/gift/common/gwt/override/mil/arl/gift/common/enums/AbstractEnum.java
 * Any changes to the methods noted below must be reflected in that class too
 *
 * @author mhoffman
 */
public abstract class AbstractEnum implements Serializable, Comparable<AbstractEnum> {

	private static final long serialVersionUID = 1L;

	/** the integer value of this enumeration */
    private int value;

    /** the string name of this enumeration */
    private String name;

    /** the display name of this enumeration */
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
            throw new IllegalArgumentException("The enum name can't be null.");
        }else if(displayName == null){
            throw new IllegalArgumentException("The enum display name can't be null.");
        }

        this.value = value;
        this.name = name;
        this.displayName = displayName;
    }

    /**
     * Return the integer value of this enumeration
     *
     * This method is in the GWT-safe version of this class, any changes to this
     * method must also be made to that class as well. See the class comment
     * for more details
     * 
     * @return the integer value of this enumeration
     */
    public int getValue() {

        return value;
    }

    /**
     * Return the string name of this enumeration
     *
     * This method is in the GWT-safe version of this class, any changes to this
     * method must also be made to that class as well. See the class comment
     * for more details
     * 
     * @return the unique name in this enumeration set
     */
    public String getName() {

        return name;
    }

    /**
     * Return the display name of this enumeration
     *
     * This method is in the GWT-safe version of this class, any changes to this
     * method must also be made to that class as well. See the class comment
     * for more details
     * 
     * @return the display name
     */
    public String getDisplayName() {

        return displayName;
    }

    /**
     * Set the display name of this enumeration
     *
     * This method is in the GWT-safe version of this class, any changes to this
     * method must also be made to that class as well. See the class comment
     * for more details
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
     * This method is in the GWT-safe version of this class, any changes to this
     * method must also be made to that class as well. See the class comment
     * for more details
     *
     * @param <E> The enumeration implementation class
     * @param enumName The name value of the enumeration instance to retrieve.  
     *
     * @param values The values of the enumeration
     * @return The enumeration class instance.  
     *
     * @exception EnumException Description of the Exception
     * @throws EnumerationNotFoundException if enumName does not exist in enum.  Also if the enumName is null.
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
     * This method is in the GWT-safe version of this class, any changes to this
     * method must also be made to that class as well. See the class comment
     * for more details
     * 
     * @param <E> The enumeration implementation class
     * @param enumValue The value of the enumeration instance to retrieve.
     *
     * @param values The values of the enumeration
     * @return The enumeration class instance.
     *
     * @exception EnumException Description of the Exception
     * @throws EnumerationNotFoundException if enumName does not exist in enum.
     */
    public static <E extends AbstractEnum> E valueOf(int enumValue, List<E> values) throws EnumException {

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

        throw new EnumException("Unable to find the enumeration indexed at "+enumValue+" in "+values+".", null);
    }

    /**
     * This is a centralized method of locating an enumeration object using the
     * enumeration class name (fully qualified) and the enumeration instance
     * name.
     *
     * @param <E> The enumeration implementation class
     * @param enumClass The Class of the enumeration.
     * @param enumName The name value of the enumeration instance to retrieve.
     *
     * @return The enumeration class instance.
     *
     * @exception EnumException Description of the Exception
     * @throws EnumerationNotFoundException if enumName does not exist in enum.
     */
    public static <E extends AbstractEnum> E valueOf(Class<E> enumClass,
            String enumName)
            throws EnumException {

        List<E> enumList = VALUES(enumClass);

        return valueOf(enumName, enumList);
    }

    /**
     * This is a centralized method of locating an enumeration object using the
     * enumeration class name (fully qualified) and the enumeration instance
     * value.
     *
     * @param <E> The enumeration implementation class
     * @param enumClass The Class of the enumeration.
     * @param enumValue The value of the enumeration instance to retrieve.
     *
     * @return The enumeration class instance.
     *
     * @exception EnumException Description of the Exception
     * @throws EnumerationNotFoundException if enumName does not exist in enum.
     */
    public static <E extends AbstractEnum> E valueOf(Class<E> enumClass,
            int enumValue)
            throws EnumException {

        List<E> enumList = VALUES(enumClass);

        return valueOf(enumValue, enumList);
    }

    /**
     * Returns a List containing the instances of the specified enumeration
     * class, sorted by value.
     *
     * @param <E> The enumeration implementation class
     * @param c the enumeration class to get the instances of. This class must
     * be a subclass of AbstractEnum
     *
     * @return a List containing the instances of the specified enumeration
     * class, sorted by value
     *
     * @throws AssertionError JAVADOC
     */
    @SuppressWarnings("unchecked")
    public static <E extends AbstractEnum> List<E> VALUES(Class<E> c) {

        assert c != AbstractEnum.class : "Cannot be AbstractEnum itself.";
        assert AbstractEnum.class.isAssignableFrom(c) : "Not assignable from AbstractEnum: " + c.getName();

        Method vMethod;

        try {

            vMethod = c.getMethod("VALUES");
        } catch (NoSuchMethodException e) {
            // Every subclass of AbstractEnum is required to have this method.
            throw new AssertionError(e);
        }

        try {

            return (List<E>) vMethod.invoke(null, new Object[0]);
        } catch (Exception e) {

            // There is no reason why the call should ever fail.
            throw new AssertionError(e);
        }
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
     * This method is in the GWT-safe version of this class, any changes to this
     * method must also be made to that class as well. See the class comment
     * for more details
     * 
     * @return String - string description of this enumeration.
     */
    @Override
    public String toString() {

        return name;
    }
}
