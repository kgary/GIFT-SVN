/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.wrap.overlay;

import java.io.Serializable;

import mil.arl.gift.tools.map.client.draw.AbstractMapShape;

/**
 * An object representing a place of interest and the shape used to represent it on a map. This class provides several 
 * methods that can be used to control the interactions between a place of interest and its shape and synchonize their
 * states with one another.
 * 
 * @author nroberts
 *
 * @param <P> the type of place of interest
 * @param <S> the type of shape corresponding associated with the type of place of interest
 */
public abstract class AbstractPlaceOfInterestShape<P extends Serializable, S extends AbstractMapShape<?>> {

    /** The place of interest this shape represents */
    private P placeOfInterest;
    
    /** The map shape used to represent the place of interest on the map */
    private S mapShape;
    
    /**
     * Creates a new place shape for the given place of interest. An appropriate map shape will be created based on 
     * the data provided by the given place of interest.
     * 
     * @param placeOfInterest the place of interest to create a place shape for. Cannot be null.
     */
    protected AbstractPlaceOfInterestShape(P placeOfInterest) {
        this.placeOfInterest = placeOfInterest;
        this.mapShape = createMapShape(placeOfInterest);
        
        if(this.placeOfInterest == null) {
            throw new IllegalArgumentException("The place of interest cannot be null");
        }
        
        if(this.mapShape == null) {
            throw new IllegalArgumentException("The map shape cannot be null");
        }
    }
    
    /**
     * Creates a new place shape for the given map shape. An appropriate place of interest will be created based on 
     * the data provided by the given map shape.
     * 
     * @param shape the map shape to create a place shape for. Cannot be null.
     */
    protected AbstractPlaceOfInterestShape(S shape) {
        this.mapShape = shape;
        this.placeOfInterest = createPlaceOfInterest(shape);
        
        if(this.placeOfInterest == null) {
            throw new IllegalArgumentException("The place of interest cannot be null");
        }
        
        if(this.mapShape == null) {
            throw new IllegalArgumentException("The shape cannot be null");
        }
    }

    /**
     * Gets the place of interest that this place shape represents
     * 
     * @return the place of interest. Cannot be null.
     */
    public P getPlaceOfInterest() {
        return placeOfInterest;
    }

    /**
     * Sets the place of interest that this place shape represents
     * 
     * @param placeOfInterest the place of interest. Cannot be null.
     */
    public void setPlaceOfInterest(P placeOfInterest) {
        
        if(placeOfInterest == null) {
            throw new IllegalArgumentException("The place of interest cannot be null");
        }
        
        this.placeOfInterest = placeOfInterest;
    }

    /**
     * Gets the map shape used to represent this place shape on a map
     * 
     * @return the map shape. Cannot be null.
     */
    public S getMapShape() {
        return mapShape;
    }

    /**
     * Sets the map shape used to represent this place shape on a map
     * 
     * @param shape the map shape. Cannot be null.
     */
    public void setMapShape(S shape) {
        
        if(shape == null) {
            throw new IllegalArgumentException("The map shape cannot be null");
        }
        
        this.mapShape = shape;
    }
    
    /**
     * Creates a map shape based on the data in the given place of interest
     * 
     * @param placeOfInterest the place of interest from which to get the data
     * @return a map shape based on the place of interest's data
     */
    abstract protected S createMapShape(P placeOfInterest);
    
    /**
     * Creates a place of interest based on the data in the given map shape
     * 
     * @param shame the map shape from which to get the data
     * @return a place of interest based on the map shape's data
     */
    abstract protected P createPlaceOfInterest(S shape);
    
    /**
     * Updates this shape's place of interest to match the map shape used to represent it
     */
    abstract public void updatePlaceOfInterest();
    
    /**
     * Updates this shape's map shape to match the place of interest it represents
     */
    abstract public void updateMapShape();
}
