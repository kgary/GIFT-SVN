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

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import mil.arl.gift.common.ert.EventColumnDisplay;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;

/**
 * This class is responsible for codec logic on an EventColumnDisplay object.
 * 
 * @author mhoffman
 *
 */
public class EventColumnDisplayCodec {
    
    private static final String EVENT_DISPLAY = "Event";
    private static final String IS_ENABLED = "IsEnabled";
    private static final String COLUMN_MAP = "ColumnMap";
    private static final String EVENT_TYPE_NAME = "eventTypeName";
    private static final String EVENT_TYPE_DESC = "eventTypeDescription";

    /**
     * Decode the encoding to a new EventColumnDisplay object.
     * 
     * @param rootObject contains the event column content
     * @return EventColumnDisplay
     */
    public static EventColumnDisplay decode(JSONObject rootObject){
        
        JSONObject object = (JSONObject) rootObject.get(EVENT_DISPLAY);
        
        Object isEnabledObj = object.get(IS_ENABLED);
        if(isEnabledObj == null){
            throw new IllegalArgumentException("The isEnabled value is null or not a boolean");
        }
        
        Object eventTypeNameObj = object.get(EVENT_TYPE_NAME);
        if(eventTypeNameObj == null){
            throw new IllegalArgumentException("The event type name value is null or not a string");
        }
        
        String eventTypeDesc = null;
        Object eventTypeDescObj = object.get(EVENT_TYPE_DESC);
        if(eventTypeDescObj != null){
            eventTypeDesc = (String)eventTypeDescObj;
        }
        
        Map<EventReportColumn, Boolean> columnMap = new HashMap<EventReportColumn, Boolean>();
        JSONArray arrayObj = (JSONArray)object.get(COLUMN_MAP);
        if(arrayObj == null){
            throw new IllegalArgumentException("The column array value is null or not a JSONArray");
        }
        
        for(int i = 0; i < arrayObj.size(); i++){
            
            JSONObject colObj = (JSONObject) arrayObj.get(i);
            if(colObj == null){
                throw new IllegalArgumentException("The column object value is null or not an JSONObject");
            }
            
            EventReportColumn column = EventReportColumnCodec.decode(colObj);
            
            Object isColEnabledObj = colObj.get(IS_ENABLED);
            if(isColEnabledObj == null){
                throw new IllegalArgumentException("The isEnabled value is null or not a boolean");
            }
            
            columnMap.put(column, Boolean.valueOf((String)isColEnabledObj));
        }//end for
        
        
        return new EventColumnDisplay(new EventType((String)eventTypeNameObj, (String)eventTypeNameObj, eventTypeDesc), Boolean.valueOf((String)isEnabledObj), columnMap);
    }
    
    /**
     * Encode the EventColumnDisplay object.
     * 
     * @param parentObject the object to encode values into
     * @param eventColumnDisplay contains the values to encode
     */
    @SuppressWarnings("unchecked")
    public static void encode(JSONObject parentObject, EventColumnDisplay eventColumnDisplay){
        
        JSONObject object = new JSONObject();
        object.put(IS_ENABLED, eventColumnDisplay.isEnabled().toString());
        
        object.put(EVENT_TYPE_NAME, eventColumnDisplay.getEventType().getName());
        
        if(eventColumnDisplay.getEventType().getDescription() != null){
            object.put(EVENT_TYPE_DESC, eventColumnDisplay.getEventType().getDescription());
        }
        
        JSONArray mapObj = new JSONArray();
        Map<EventReportColumn, Boolean> columnMap = eventColumnDisplay.getColumnMap();
        for(EventReportColumn column : columnMap.keySet()){
            
            JSONObject colObj = new JSONObject();
            colObj.put(IS_ENABLED, columnMap.get(column).toString());
            EventReportColumnCodec.encode(colObj, column);            
            mapObj.add(colObj);
        }
        object.put(COLUMN_MAP, mapObj);
        
        parentObject.put(EVENT_DISPLAY, object);
    }
}
