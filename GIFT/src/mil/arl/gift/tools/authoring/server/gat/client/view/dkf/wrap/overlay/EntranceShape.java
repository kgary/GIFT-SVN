/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import generated.dkf.Entrance;
import generated.dkf.Point;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.PlacesOfInterestOverlay;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.WrapPanel;
import mil.arl.gift.tools.map.client.draw.PolylineShape;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * An object representing an entrance and the shape used to represent it on a map. This class provides several 
 * methods that can be used to control the interactions between an entranceand its shape and synchonize their
 * states with one another.
 * 
 * @author nroberts
 */
public class EntranceShape {

    /** The entrance this shape represents*/
    private Entrance entrance;
    
    /** The map shape used to represent the entrance on the map */
    private PolylineShape<?> mapShape;
    
    /**
     * Creates a new entrance shape for the given entrance. An appropriate map shape will be created based on 
     * the data provided by the given entrance.
     * 
     * @param entrance the entrance to create a entrance shape for. Cannot be null.
     */
    public EntranceShape(Entrance entrance) {
        
        if(entrance == null) {
            throw new IllegalArgumentException("The entrance for this shape cannot be null.");
        }
        
        this.entrance = entrance;
        
        List<AbstractMapCoordinate> vertices = getPoints();
        
        mapShape = WrapPanel.getInstance().getCurrentMap().createPolyline(vertices);
        mapShape.setName(entrance.getName());

        Point point = getOutsidePoint();
        
        if(point != null) {
            mapShape.setColor(point.getColorHexRGBA());
        }
    }
    
    /**
     * Gets the coordinates of the entrance's start and end points and returns them as a list of coordinates representing
     * a path for a polyline.
     * 
     * @return the coordinates corresponding to this entrance's start and end points
     */
    private List<AbstractMapCoordinate> getPoints(){
        
        List<AbstractMapCoordinate> points = new ArrayList<>();
        
        Point outsidePoint = getOutsidePoint();
        
        if(outsidePoint != null) {
            points.add(PlacesOfInterestOverlay.toMapCoordinate(outsidePoint.getCoordinate()));
        }
        
        Point insidePoint = getInsidePoint();
        
        if(insidePoint != null) {
            points.add(PlacesOfInterestOverlay.toMapCoordinate(insidePoint.getCoordinate()));
        }
        
        return points;
    }
    
    /**
     * Gets the point of interest being used as the entrance's outside point
     * 
     * @return the outside point of interest, or null, if the entrance references an invalid point
     */
    public Point getOutsidePoint() {
        
        if(entrance.getOutside() != null) {
            
            Serializable point = ScenarioClientUtility.getPlaceOfInterestWithName(entrance.getOutside().getPoint());
            
            if(point instanceof Point) {
                return (Point) point;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the point of interest being used as the entrance's inside point
     * 
     * @return the inside point of interest, or null, if the entrance references an invalid point
     */
    public Point getInsidePoint() {
        
        if(entrance.getInside() != null) {
            
            Serializable point = ScenarioClientUtility.getPlaceOfInterestWithName(entrance.getInside().getPoint());
            
            if(point instanceof Point) {
                return (Point) point;
            }
        }
        
        return null;
    }
    
    /**
     * Updates this shape's map shape to match the entrance it represents
     */
    public void updateMapShape() {
        
        List<AbstractMapCoordinate> vertices = getPoints();
        
        mapShape.setVertices(vertices);
        mapShape.setName(entrance.getName());

        Point point = getOutsidePoint();
        
        if(point != null) {
            mapShape.setColor(point.getColorHexRGBA());
        }
    }
    
    /**
     * Gets the entrance this shape represents
     * 
     * @return the entrance
     */
    public Entrance getEntrance() {
        return entrance;
    }
    
    /**
     * Gets the map shape used to represent the entrance on a map
     * 
     * @return the map shape
     */
    public PolylineShape<?> getMapShape(){
        return mapShape;
    }
}
