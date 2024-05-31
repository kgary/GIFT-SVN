/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.List;

import mil.arl.gift.common.coordinate.AbstractCoordinate;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.domain.DomainDKFHandler;

/**
 * Represents a closed area by defining the ordered list of vertices.
 * 
 * @author mhoffman
 *
 */
public class Area implements PlaceOfInterestInterface {
    
    /** the display name of the area */
    private String name;
    
    /** the points that make up the perimeter of the area */
    private List<AbstractCoordinate> points;
    
    /**
     * Set attributes
     * 
     * @param name the display name for this area.  Can't be null or empty.
     * @param points the list of points for this area
     */
    public Area(String name, List<AbstractCoordinate> points){
        
        if(StringUtils.isBlank(name)){
            throw new IllegalArgumentException("The name is null or empty.");
        }else if(points == null || points.isEmpty()){
            throw new IllegalArgumentException("The points is null or empty.");
        }
        
        this.name = name;
        this.points = points;
    }
    
    /**
     * Class constructor - set attributes using generated class's object for a area
     * 
     * @param area - dkf content for an area
     */
    public Area(generated.dkf.Area area){
        this(area.getName(), DomainDKFHandler.buildCoordinatesFromCoordinates(area.getCoordinate()));
    }

    @Override
    public String getName() {
        return name;
    }
    
    /**
     * Return the list of points for this area.
     * 
     * @return the list of points.  Can't be null or empty.
     */
    public List<AbstractCoordinate> getPoints(){
        return points;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Area: ");
        sb.append(" Name = ").append(getName());
        sb.append(", location = ").append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }

}
