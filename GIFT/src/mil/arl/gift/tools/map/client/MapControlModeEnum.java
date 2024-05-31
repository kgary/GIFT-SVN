/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client;

/**
 * An enumeration of the different modes that can be used to handle mouse events on a map
 * 
 * @author nroberts
 */
public enum MapControlModeEnum {
    
    /** The control mode that allows the user to drag the map to pan its view */
    PAN,
    
    /** The control mode that allows the user to draw points */
    DRAW_POINT,
    
    /** The control mode that allows the user to draw polylines */
    DRAW_POLYLINE,
    
    /** The control mode that allows the user to draw polygons */
    DRAW_POLYGON
}
