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
 * A shape that defines a polyline (i.e. series of lines) following a series of vertices
 * 
 * @author nroberts
 */
public abstract class PolylineShape<T extends AbstractMapPanel> extends AbstractMapShape<T> {

    /** The vertices that this polyline follows */
    private List<AbstractMapCoordinate> vertices;
    
    /**
     * Defines a new polyline shape following the given vertices
     * 
     * @param vertices the vertices of this polyline. Cannot be null.
     */
    public PolylineShape(T mapImpl, List<AbstractMapCoordinate> vertices) {
        super(mapImpl);
        setVertices(vertices);
    }

    /**
     * Gets the vertices that this polyline follows
     * 
     * @return the vertices that this polyline follows. Cannot be null.
     */
    public List<AbstractMapCoordinate> getVertices() {
        return vertices;
    }

    /**
     * Sets the vertices that this polyline follows
     * 
     * @param vertices the vertices that this polyline follows. Cannot be null.
     */
    public void setVertices(List<AbstractMapCoordinate> vertices) {
        
        if(vertices == null) {
            throw new IllegalArgumentException("This polyline's vertices cannot be null.");
            
        } else if(vertices.size() < 2) {
            throw new IllegalArgumentException("This polyline must have at least 2 vertices.");
        }
        
        this.vertices = vertices;
    }
}
