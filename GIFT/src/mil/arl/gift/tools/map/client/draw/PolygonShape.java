/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.map.client.draw;

import java.util.List;

import mil.arl.gift.tools.map.client.AbstractMapPanel;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * A shape that defines an polygon comprised of a series of vertices
 * 
 * @author nroberts
 */
public abstract class PolygonShape<T extends AbstractMapPanel> extends AbstractMapShape<T> {

    /** The vertices that this polygon is comprised of */
    private List<AbstractMapCoordinate> vertices;
    
    /**
     * Defines a new polygon shape comprised of the given vertices
     * 
     * @param vertices the vertices of this polygon. Cannot be null.
     */
    public PolygonShape(T mapImpl, List<AbstractMapCoordinate> vertices) {
        super(mapImpl);
        setVertices(vertices);
    }

    /**
     * Gets the vertices that comprise this polygon
     * 
     * @return the vertices that comprise this polygon. Cannot be null.
     */
    public List<AbstractMapCoordinate> getVertices() {
        return vertices;
    }

    /**
     * Sets the vertices that comprise this polygon
     * 
     * @param vertices the vertices that comprise this polygon. Cannot be null.
     */
    public void setVertices(List<AbstractMapCoordinate> vertices) {
        
        if(vertices == null) {
            throw new IllegalArgumentException("This path's vertices cannot be null.");
            
        } else if(vertices.size() < 2) {
            throw new IllegalArgumentException("This path must have at least 3 vertices.");
        }
        
        this.vertices = vertices;
    }
}
