/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.enums.AbstractEnum;

/**
 * A class for getting the properties of a attribute values enumerations
 * 
 * Necessary to provide a reflection-less interface to the AbstractEnum class
 *
 * @author jleonard
 */
public class AttributeValueEnumAccessor implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * The list containing the attribute values, can't be null or empty.
     */
    private List<AbstractEnum> enumList;
    
    /**
     * The list containing the authorable attribute values, can't be null or empty.
     * Must be equal to or less than the enumList size.
     */
    private List<AbstractEnum> enumAuthorableList;
    
    /**
     * The default attribute value.  Can't be null.
     */
    private AbstractEnum defaultValue;
    
    /**
     * Return the 'unknown' value which represents an enum for when a value
     * can't be calculated.  This is something that authoring tools could hide from authors.  Can be null.
     */
    private AbstractEnum unknownValue;
    
    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public AttributeValueEnumAccessor() {
    }

    /**
     * Constructor
     * 
     * @param enumList The list containing the attribute values, can't be null or empty.
     * @param enumAuthorableList The list containing the authorable attribute values, can't be null.
     * Must be equal to or less than the enumList size.
     * @param defaultValue The default attribute value.  Can't be null.
     * @param unknownValue Return the 'unknown' value which represents an enum for when a value
     * can't be calculated.  This is something that authoring tools could hide from authors.  Can be null.
     */
    public AttributeValueEnumAccessor(List<? extends AbstractEnum> enumList, List<? extends AbstractEnum> enumAuthorableList,
            AbstractEnum defaultValue, AbstractEnum unknownValue) {
        
        if(enumList == null || enumList.isEmpty()){
            throw new IllegalArgumentException("The enum list can't be null or empty");
        }else if(enumAuthorableList == null){
            throw new IllegalArgumentException("The enum authorable list can't be null");
        }else if(defaultValue == null){
            throw new IllegalArgumentException("The default value can't be null");
        }
        
        this.enumList = new ArrayList<AbstractEnum>(enumList);
        this.enumAuthorableList = new ArrayList<AbstractEnum>(enumAuthorableList);
        this.defaultValue = defaultValue;
        this.unknownValue = unknownValue;
    }
    
    /**
     * Gets the default value of the attribute
     *
     * @return E The default value of the attribute.  Won't be null.
     */
    public AbstractEnum DEFAULT_VALUE() {        
        return defaultValue;
    }
    
    /**
     * Gets the list of values of the attribute
     * 
     * @return An unmodifiable list of the values of the attribute.  Wont be null or empty.
     */
    public List<AbstractEnum> VALUES() {        
        return enumList;
    }
    
    /**
     * Gets the list of authorable values of the attribute
     * 
     * @return An unmodifiable list of the authorable values of the attribute. Wont be null.
     * Must be equal to or less than the enumList size.
     */
    public List<AbstractEnum> AUTHORABLE_VALUES() {        
        return enumAuthorableList;
    }
    
    /**
     * Return the 'unknown' value which represents an enum for when a value
     * can't be calculated.  This is something that authoring tools could hide from authors.
     * 
     * @return can be null if an 'unknown' enum is not part of the values collection
     */
    public AbstractEnum getUnknownValue(){
        return unknownValue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[AttributeValueEnumAccessor: enumList = ");
        builder.append(enumList);
        builder.append(", enumAuthorableList = ");
        builder.append(enumAuthorableList);
        builder.append(", defaultValue = ");
        builder.append(defaultValue);
        builder.append(", unknownValue = ");
        builder.append(unknownValue);
        builder.append("]");
        return builder.toString();
    }
    
    
}
