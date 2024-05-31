/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.SurveyPropertyKeyEnum;
import mil.arl.gift.common.io.Constants;

/**
 * The properties of a survey item
 *
 * @author jleonard
 */
public class SurveyItemProperties implements Serializable {

    private static final Logger logger = Logger.getLogger(SurveyItemProperties.class.getName());

    private static final long serialVersionUID = 1L;

    /**
     * The delimiter to use for delimiting lists stored in a property
     */
    public static final String LIST_DELIMITER = ",";

    private final Set<PropertyChangeListener> listeners = new HashSet<PropertyChangeListener>();

    private Map<SurveyPropertyKeyEnum, Serializable> properties = new HashMap<SurveyPropertyKeyEnum, Serializable>();

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SurveyItemProperties() {
    }

    /**
     * Constructor
     *
     * @param properties collection of properties for a survey item
     */
    public SurveyItemProperties(Map<SurveyPropertyKeyEnum, Serializable> properties) {

        if (properties == null) {

            throw new IllegalArgumentException("Cannot copy a null property map");
        }

        this.properties.putAll(properties);
    }

    /**
     * If the property key has a value associated with it
     *
     * @param key The property key
     * @return boolean If the property key has a value associated with it
     */
    public boolean hasProperty(SurveyPropertyKeyEnum key) {

        return properties.containsKey(key);
    }

    /**
     * Gets the number of properties for a question
     *
     * @return int The number of properties for a question
     */
    public int getPropertyCount() {

        return properties.size();
    }

    /**
     * Gets the keys of set properties
     *
     * @return Set<SurveyPropertyKeyEnum> The keys of set properties
     */
    public Set<SurveyPropertyKeyEnum> getKeys() {

        return properties.keySet();
    }
    
    /**
     * Return a list of strings for the property referenced by the key.
     * 
     * @param key used to look up the property value in this instance.  If the key has a delimiter specified
     * than it will be used to split the string property value, otherwise the default list delimiter is used.
     * @return null if the key is null or the value returned for the property is not a string.
     */
    public List<String> getStringListPropertyValue(SurveyPropertyKeyEnum key){
        
        if(key == null){
            return null;
        }
        
        Serializable valueObj = getPropertyValue(key);
        if(valueObj instanceof String){
            
            String valueDelimStr = (String) valueObj;
            
            String delim = key.getListDelimiter() != null ? key.getListDelimiter() : LIST_DELIMITER;
            return decodeListString(valueDelimStr, delim);
            
        }else{
            return null;
        }

    }

    /**
     * Gets the value associated with a property key
     *
     * @param key The property key
     * @return String The value of the property.  Can be null if the property is not found.
     */
    public Serializable getPropertyValue(SurveyPropertyKeyEnum key) {

        Serializable value = properties.get(key);
        return value;
    }

    /**
     * Gets the value associated with the property key as a boolean
     *
     * @param key The property key
     * @return Boolean The boolean value associated with the property key, null
     * if the property is not defined
     */
    public Boolean getBooleanPropertyValue(SurveyPropertyKeyEnum key) {

        String value = (String) getPropertyValue(key);

        return value != null ? Boolean.parseBoolean(value) : null;
    }

    /**
     * Gets the value associated with the property key as a boolean, providing a
     * value even if the value is not defined
     *
     * @param key The property key
     * @param defaultValue The value to return if the property is not set
     * @return boolean The boolean value associated with the property key
     */
    public boolean getBooleanPropertyValue(SurveyPropertyKeyEnum key, boolean defaultValue) {

        Boolean value = getBooleanPropertyValue(key);

        if (value != null) {

            return value;

        } else {

            return defaultValue;
        }
    }

    /**
     * Gets the value associated with the property key as an integer
     *
     * @param key The property key
     * @return Integer The integer value associated with the property key
     */
    public Integer getIntegerPropertyValue(SurveyPropertyKeyEnum key) {

        String value = (String) getPropertyValue(key);

        return (value != null && !value.isEmpty()) ? Integer.parseInt(value) : null;
    }

    /**
     * Gets the value associated with the property key as an integer, providing a
     * value even if the value is not defined
     *
     * @param key The property key
     * @param defaultValue The value to return if the property is not set
     * @return Integer The integer value associated with the property key
     */
    public Integer getIntegerPropertyValue(SurveyPropertyKeyEnum key, Integer defaultValue) {
        
        Integer value = getIntegerPropertyValue(key);
        
        if (value != null) {
            
            return value;
            
        } else {

            return defaultValue;

        }
    }

    /**
     * Removes all instances of the property key
     *
     * @param key The property key
     */
    public void removeProperty(SurveyPropertyKeyEnum key) {

        properties.remove(key);

        notifyPropertyChanged();
    }

    /**
     * Sets the value of a property
     *
     * @param key The property key.  can't be null.
     * @param value The property value.  can be null.
     */
    public void setPropertyValue(SurveyPropertyKeyEnum key, Serializable value) {
        
        if(key == null){
            throw new IllegalArgumentException("The property key can't be null.");
        }

        properties.put(key, value);

        notifyPropertyChanged();
    }

    /**
     * Sets the value of a property as a boolean
     *
     * @param key The property key
     * @param value The property value as a boolean
     */
    public void setBooleanPropertyValue(SurveyPropertyKeyEnum key, boolean value) {

        setPropertyValue(key, Boolean.toString(value));
    }

    /**
     * Sets the boolean value of a property, removes the property if the value
     * is false
     *
     * @param key The property key
     * @param value The property value as a boolean
     */
    public void setBooleanPropertyValueRemoveFalse(SurveyPropertyKeyEnum key, boolean value) {

        if (value) {

            setBooleanPropertyValue(key, value);

        } else {

            removeProperty(key);
        }
    }

    /**
     * Sets the value of a property as an integer
     *
     * @param key The property key
     * @param value The property value as an integer
     */
    public void setIntegerPropertyValue(SurveyPropertyKeyEnum key, int value) {

        setPropertyValue(key, Integer.toString(value));
    }

    /**
     * Adds a listener interested in be notified of when a property changes
     *
     * @param listener The listener interested
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {

        listeners.add(listener);
    }

    /**
     * Removes a listener from being notified of when a property changes
     *
     * @param listener The uninterested listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {

        listeners.remove(listener);
    }

    /**
     * Notifies the listeners that a property has changed
     */
    protected void notifyPropertyChanged() {

        for (PropertyChangeListener listener : listeners) {

            try {

                listener.onPropertyChange();

            } catch (Exception e) {

                logger.log(Level.SEVERE, "Caught an exception while notifying a Property Change Listener that a property had changed", e);
            }
        }
    }

    /**
     * Decodes a string representing a list into the list of strings it contains
     *
     * @param listString The string representation of a string.
     * @return List<String> The string list elements
     */
    public static List<String> decodeListString(String listString) {
        
        if(listString == null || listString.isEmpty()){
            return new ArrayList<String>();
        }

        return decodeListString(listString, LIST_DELIMITER);
    }
    
    /**
     * Decodes a string representing a list into the list of strings it contains
     *
     * @param valueDelimStr The string representation of a string.
     * @param delimiter The regex to split the list with.  If null, the default delimeter will be used {@link #LIST_DELIMITER}
     * @return List<String> The string list elements
     */
    public static List<String> decodeListString(String valueDelimStr, String delimiter) {
        
        if(valueDelimStr == null || valueDelimStr.isEmpty()){
            return new ArrayList<String>();
        }else if(delimiter == null){
            delimiter = LIST_DELIMITER; 
        }
        
        //escape special characters for regular expressions
        if(delimiter.equals(Constants.PIPE)){
        	delimiter = "\\" + delimiter;
        }
               
        String [] tokens = valueDelimStr.split(delimiter);
        List<String> list = new ArrayList<String>(tokens.length);
        Collections.addAll(list, tokens);
        
        //if the string ends with the delimeter the split will not return the correct number of elements,
        //therefore need to add an entry for each delimeter found at the end of the string
        //Examples:
        //     x,,y will return [x,"",y] as it should
        //     x,,y,, will return [x,"",y] but should return [x,"",y,"",""]
        if(valueDelimStr.endsWith(delimiter)){
            
            for(int entriesToAdd = valueDelimStr.length() - valueDelimStr.lastIndexOf(delimiter); entriesToAdd > 0; entriesToAdd--){
                list.add("");
            }
        }
        
        return list;
    }

    /**
     * Encodes a list of string elements in to a string representation of a list
     *
     * @param stringList The list of elements
     * @return String The string representation of the list
     */
    public static String encodeListString(List<String> stringList) {
        return encodeListString(stringList, LIST_DELIMITER);
    }
    
    /**
     * Encodes a list of string elements in to a string representation of a list
     *
     * @param stringList The list of elements
     * @param delimiter a specific delimieter to use between entries in the list
     * @return String The string representation of the list
     */
    public static String encodeListString(List<String> stringList, String delimiter) {

        StringBuilder listStringBuilder = new StringBuilder();
        
        for(int index = 0; index < stringList.size(); index++){
            
            listStringBuilder.append(stringList.get(index));
            
            if((index+1) != stringList.size()){
                listStringBuilder.append(delimiter);
            }
        }

        return listStringBuilder.toString();
    }
    
    /**
     * Check the string list for duplicate string.
     * 
     * @param stringList the list to check.  Can be empty.  Can contain null and empty strings.
     * @return the first duplicate string found in the list.  Null if there are no duplicates.
     */
    public static String findDuplicateEntry(List<String> stringList){
        
        String duplicateString = null;
        for(int i = 0; i < stringList.size() && duplicateString == null; i++){
            
            String objectIdA = stringList.get(i);
            for(int j = i+1; j < stringList.size(); j++){
                
                String objectIdB = stringList.get(j);
                if(objectIdA != null && !objectIdA.isEmpty() && objectIdB != null && 
                        objectIdA.equals(objectIdB)){
                    //ERROR - found match
                    duplicateString = objectIdA;
                    break;
                }
            }
        }
        
        return duplicateString;
    }

    /**
     * Decodes a string representation of a list of integers in to a list of
     * integers
     *
     * @param listString The string representation of a list of integers
     * @return List<Integer> The list of integers from the string
     */
    public static List<Integer> decodeIntegerListString(String listString) {

        List<Integer> integersList = new ArrayList<Integer>();

        List<String> stringList = decodeListString(listString);

        for (String integerString : stringList) {

            integersList.add(Integer.decode(integerString));
        }

        return integersList;
    }

    /**
     * Encodes a list of integers into a string representation
     *
     * @param integerList The list of integers
     * @return String The string representation of the list
     */
    public static String encodeIntegerListString(List<Integer> integerList) {

        List<String> stringList = new ArrayList<String>();

        for (Integer integer : integerList) {

            stringList.add(Integer.toString(integer));
        }

        return encodeListString(stringList);
    }

    /**
     * Decodes a string representation of a list of doubles in to a list of
     * doubles
     *
     * @param listString The string representation of a list of doubles
     * @return List<Double> The list of doubles from the string
     */
    public static List<Double> decodeDoubleListString(String listString) {

        List<Double> doublesList = new ArrayList<Double>();

        List<String> stringList = decodeListString(listString);

        for (String doubleString : stringList) {

            doublesList.add(Double.parseDouble(doubleString));
        }

        return doublesList;
    }

    /**
     * Encodes a list of doubles into a string representation
     *
     * @param doubleList The list of doubles
     * @return String The string representation of the list
     */
    public static String encodeDoubleListString(List<Double> doubleList) {

        List<String> stringList = new ArrayList<String>();

        for (Double doubleVal : doubleList) {

            stringList.add(Double.toString(doubleVal));
        }

        return encodeListString(stringList);
    }

    /**
     * Decodes a list of enumerations represented as a String
     *
     * @param <E> The enumeration class
     * @param listString The string representation
     * @param allEnums The list of enumerations of the enumeration class
     * @return List<E> The list of enumerations in the string representation
     */
    public static <E extends AbstractEnum> List<E> decodeEnumListString(String listString, List<E> allEnums) {

        List<String> enumStringList = decodeListString(listString);

        List<E> enumList = new ArrayList<E>();

        for (String enumString : enumStringList) {

            E enumValue = AbstractEnum.valueOf(enumString, allEnums);

            if (enumValue != null) {

                enumList.add(enumValue);
            }
        }

        return enumList;
    }

    /**
     * Encodes a list of enumerations in to a String representation
     *
     * @param <E> The enumeration class
     * @param enumList The list of enumerations to convert
     * @return String The string representation of the list
     */
    public static <E extends AbstractEnum> String encodeEnumListString(List<E> enumList) {

        List<String> enumStringList = new ArrayList<String>();

        for (E enumValue : enumList) {

            enumStringList.add(enumValue.getName());
        }

        return encodeListString(enumStringList);
    }
    
    @Override
    public String toString() {
   
        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyItemProperties: ");
        for (SurveyPropertyKeyEnum key : properties.keySet()) {

            sb.append(key).append(": ").append(properties.get(key)).append(", ");
        }
        sb.append("]");
        return sb.toString();
        
    }

    /**
     * Copies the survey item properties into a different survey item properties object.
     * Note: this doesn't consider properties that have been removed, i.e. if this object
     * has had properties removed but the provided property object still has them it won't remove
     * them from the provided property object.
     * 
     * @param props - The survey item properties that will have the new items copied into.
     */
    public void copyInto(SurveyItemProperties props) {
        
        for (SurveyPropertyKeyEnum key : getKeys()) {
            Serializable value = getPropertyValue(key);
            
            props.setPropertyValue(key, value);
        }
        
    }
}
