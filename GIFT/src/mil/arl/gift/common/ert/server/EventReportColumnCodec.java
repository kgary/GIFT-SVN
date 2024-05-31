/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.ert.ColumnProperties;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.TimeProperties;

import org.json.simple.JSONObject;

/**
 * This class is responsible for codec logic on an EventReportColumn object.
 * 
 * @author mhoffman
 *
 */
public class EventReportColumnCodec {
    
    private static final String COL_NAME = "name";
    private static final String DISPLAY = "display";
    private static final String ENABLED = "enabled";
    private static final String PROPERTIES = "properties";

    private static final String PROPERTY_CLASS = "propertyClass";
    private static final String FILTER_BEFORE = "filterBefore";
    private static final String FILTER_AFTER = "filterAfter";

    /** Maps the implementation class names to the correct codecs */
    private static Map<String, Class<?>> implClassMap = new HashMap<String, Class<?>>();

    static {
        implClassMap.put(TimeProperties.class.getName(), TimeProperties.class);
    }

    /**
     * Decode the encoding to a new EventReportColumn object.
     * 
     * @param object the object to decode
     * @return EventReportColumn
     */
    public static EventReportColumn decode(JSONObject object) {

        Object nameJSON = object.get(COL_NAME);
        Object displayJSON = object.get(DISPLAY);

        if (nameJSON == null) {
            
            throw new IllegalArgumentException("The column name is null or not a string");
            
        } else if (displayJSON == null) {
            
            throw new IllegalArgumentException("The column display name is null or not a string");
        }

        ColumnProperties properties = null;

        JSONObject propertiesJSON = (JSONObject) object.get(PROPERTIES);

        if (propertiesJSON != null) {

            String propertyClassName = (String) propertiesJSON.get(PROPERTY_CLASS);

            if (propertyClassName == null) {

                throw new IllegalArgumentException("The property class is null or not a string");
            }

            Class<?> propertyClass = implClassMap.get(propertyClassName);

            if (propertyClass == null) {

                throw new IllegalArgumentException("The property class does not map to a class");
            }

            if (propertyClass == TimeProperties.class) {

                Long filterBeforeLong = (Long) propertiesJSON.get(FILTER_BEFORE);

                Long filterAfterLong = (Long) propertiesJSON.get(FILTER_AFTER);

                properties = new TimeProperties(filterBeforeLong, filterAfterLong);
            }
        }
        
        EventReportColumn column;
        if(properties == null) {

            column = new EventReportColumn((String)displayJSON, (String)nameJSON);
        
        } else {
            
            column =  new EventReportColumn((String)displayJSON, (String)nameJSON, properties);
        }
        
        if(object.containsKey(ENABLED)){
            // legacy encodings won't have this attribute
            boolean enabled = (Boolean)object.get(ENABLED);
            column.setEnabled(enabled);
        }
        
        return column;
    }
    
    /**
     * Encode the EventReportColumn object.
     * 
     * @param object where to encode data into
     * @param eventReportColumn the data to encode
     */
    @SuppressWarnings("unchecked")
    public static void encode(JSONObject object, EventReportColumn eventReportColumn) {

        object.put(COL_NAME, eventReportColumn.getColumnName());
        object.put(DISPLAY, eventReportColumn.getDisplayName());
        object.put(ENABLED, eventReportColumn.isEnabled());

        if (eventReportColumn.getProperties() != null) {

            JSONObject propertiesObj = new JSONObject();

            propertiesObj.put(PROPERTY_CLASS, eventReportColumn.getProperties().getClass().getName());

            if (eventReportColumn.getProperties() instanceof TimeProperties) {

                TimeProperties properties = (TimeProperties) eventReportColumn.getProperties();

                if (properties.getFilterBeforeTime() != null) {

                    propertiesObj.put(FILTER_BEFORE, properties.getFilterBeforeTime());
                }

                if (properties.getFilterAfterTime() != null) {

                    propertiesObj.put(FILTER_AFTER, properties.getFilterAfterTime());
                }
            }

            object.put(PROPERTIES, propertiesObj);
        }
    }
}
