/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared.properties;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The properties of a widget instance
 *
 * @author jleonard
 */
public class WidgetProperties implements IsSerializable {

    private final static String IS_FULLSCREEN_PROPERTY = "IS_FULLSCREEN";

    private final static String HAS_FOCUS_PROPERTY = "HAS_FOCUS";
    
    private final static String SHOULD_UPDATE = "SHOULD_UPDATE";

    private final static String TUTOR_TEST = "TUTOR_TEST";
    
    private final static String IS_DEBUG = "DEBUG";
    
    /** the key to the background property */
    private final static String BACKGROUND = "BACKGROUND";
    
    private HashMap<String, Serializable> keyToSerializable = new HashMap<String, Serializable>();

    private HashMap<String, IsSerializable> keyToIsSerializable = new HashMap<String, IsSerializable>();

    /**
     * Gets the value of a widget's property
     *
     * @param propertyName The property to get the value of
     * @return Object The value of the property
     */
    public Object getPropertyValue(String propertyName) {
        Object property = keyToSerializable.get(propertyName);
        if (property == null) {
            property = keyToIsSerializable.get(propertyName);
        }
        return property;
    }

    /**
     * Gets the value of a widget's string property
     *
     * @param propertyName The string property to get the value of
     * @return String The value of the string property. Can be null.
     */
    public String getStringPropertyValue(String propertyName) {
        Object property = keyToSerializable.get(propertyName);
        if (property == null) {
            property = keyToIsSerializable.get(propertyName);
        }
        return (String) property;
    }

    /**
     * Gets the value of a widget's boolean property
     *
     * @param propertyName The boolean property to get the value of
     * @return Boolean The value of the boolean property.  Can be null.
     */
    public Boolean getBooleanPropertyValue(String propertyName) {
        Object property = keyToSerializable.get(propertyName);
        if (property == null) {
            property = keyToIsSerializable.get(propertyName);
        }
        return (Boolean) property;
    }

    /**
     * Gets the value of a widget's integer property
     *
     * @param propertyName The integer property to get the value of
     * @return Integer The value of the integer property. Can be null.
     */
    public Integer getIntegerPropertyValue(String propertyName) {
        Object property = keyToSerializable.get(propertyName);
        if (property == null) {
            property = keyToIsSerializable.get(propertyName);
        }
        return (Integer) property;
    }

    /**
     * Sets a property of the widget
     *
     * @param propertyName The property to set the value of
     * @param value The value to set the property of
     */
    public void setPropertyValue(String propertyName, Serializable value) {
        keyToSerializable.put(propertyName, value);
    }

    /**
     * Sets a property of the widget
     *
     * @param propertyName The property to set the value of
     * @param value The value to set the property of
     */
    public void setPropertyValue(String propertyName, IsSerializable value) {
        keyToIsSerializable.put(propertyName, value);
    }

    /**
     * Sets if the widget should use the entire browser to display
     *
     * @param isFullscreen If the widget is full screen
     */
    public void setIsFullscreen(boolean isFullscreen) {
        setPropertyValue(IS_FULLSCREEN_PROPERTY, isFullscreen);
    }

    /**
     * Gets if the widget should use the entire browser to display
     *
     * @return boolean If the widget is full screen
     */
    public boolean getIsFullscreen() {
        Boolean isFullscreen = getBooleanPropertyValue(IS_FULLSCREEN_PROPERTY);
        return isFullscreen != null ? isFullscreen : false;
    }

    /**
     * Sets if the widget should be the only one the user can interact with
     *
     * @param hasFocus If the widget has complete focus
     */
    public void setHasFocus(boolean hasFocus) {
        setPropertyValue(HAS_FOCUS_PROPERTY, hasFocus);
    }

    /**
     * Gets if the widget should be the only one the user can interact with
     *
     * @return boolean If the widget has complete focus
     */
    public boolean getHasFocus() {
        Boolean hasFocus = getBooleanPropertyValue(HAS_FOCUS_PROPERTY);
        return hasFocus != null ? hasFocus : false;
    }
    
    /**
     * Sets if the widget should update its internal data.
     * 
     * @param shouldUpdate if the widget should update its internal data
     */
    public void setShouldUpdate(boolean shouldUpdate) {
        setPropertyValue(SHOULD_UPDATE, shouldUpdate);
    }
    
    /**
     * Gets if the widget should update its internal data.
     * 
     * @return if the widget should update its internal data
     */
    public boolean getShouldUpdate() {
        Boolean shouldUpdate = getBooleanPropertyValue(SHOULD_UPDATE);
        return shouldUpdate != null ? shouldUpdate : false;
    }
    
    /**
     * Sets if the widget is currently being displayed on the Tutor Test page.
     * 
     * @param isTutorTest true if the widget is currently being displayed on the Tutor Test page, false otherwise.
     */
    public void setIsTutorTest(boolean isTutorTest) {
        setPropertyValue(TUTOR_TEST, isTutorTest);
    }
    
    /**
     * Gets whether or not the widget is currently being displayed on the Tutor Test page.
     * 
     * @return true if the widget is currently being displayed on the Tutor Test page, false otherwise.
     */
    public boolean isTutorTest() {
        Boolean isTutorTest = getBooleanPropertyValue(TUTOR_TEST);
        return isTutorTest != null ? isTutorTest : false;
    }

    /**
     * Puts the properties of another widget properties into this widget
     * properties
     *
     * @param properties The widget properties to copy from
     */
    public void putAll(WidgetProperties properties) {
        this.keyToSerializable.putAll(properties.keyToSerializable);
        this.keyToIsSerializable.putAll(properties.keyToIsSerializable);
    }
    
    /**
     * Set whether the tutor is in debug mode.  This allows for logic such as color coding survey choices based on scoring
     * 
     * @param debug the value to set
     */
    public void setDebugMode(boolean debug){
        setPropertyValue(IS_DEBUG, debug);
    }
    
    /**
     * Return whether the tutor is in debug mode.  This allows for logic such as color coding survey choices based on scoring
     * 
     * @return default is false
     */
    public boolean isDebugMode(){
        Boolean isDebug = getBooleanPropertyValue(IS_DEBUG);
        return isDebug != null ? isDebug : false;
    }
    
    /**
     * Set the background image path property to the path specified.
     * 
     * @param path the path to an image file (relative to the root of the tutor war).
     * Can be null or invalid path.
     */
    public void setBackgroundImagePath(String path){
        setPropertyValue(BACKGROUND, path);
    }
    
    /**
     * Return the background image path property value.
     * 
     * @return the path to an image file (relative to the root of the tutor war).
     * Can be null or invalid path.
     */
    public String getBackgroundImagePath(){
        return (String) getPropertyValue(BACKGROUND);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[WidgetProperties: properties = {\n");
        
        for(String key : keyToIsSerializable.keySet()){
            sb.append(key).append(" : ").append(keyToIsSerializable.get(key)).append("\n");
        }
        
        for(String key : keyToSerializable.keySet()){
            sb.append(key).append(" : ").append(keyToSerializable.get(key)).append("\n");
        }
        sb.append("}]");
        return sb.toString();
    }
}
