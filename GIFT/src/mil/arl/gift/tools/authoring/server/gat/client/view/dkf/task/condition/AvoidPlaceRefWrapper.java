/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;
import generated.dkf.AreaRef;
import generated.dkf.PointRef;

/**
 * A wrapper used to allow the author to switch between {@link PointRef}'s and {@link AreaRef}'s when defining the places
 * that the learner should avoid for an AvoidLocationCondition.
 * 
 * @author nroberts
 */
public class AvoidPlaceRefWrapper {
    
    /** The reference to the place to be avoided */
    private Serializable placeRef;
    
    /**
     * Creates a new wrapper that does not yet wrap a place reference
     */
    protected AvoidPlaceRefWrapper() {}
    
    /**
     * Creates a wrapper around the given point reference
     * 
     * @param ref a reference to a point to wrap
     */
    protected AvoidPlaceRefWrapper(PointRef ref) {
        this.setPlaceRef(ref);
    }
    
    /**
     * Creates a wrapper around the given area reference
     * 
     * @param ref a reference to an area to wrap
     */
    protected AvoidPlaceRefWrapper(AreaRef ref) {
        this.setPlaceRef(ref);
    }
    
    /**
     * Sets the place reference that this wrapper should wrap
     * 
     * @param placeRef a point reference to wrap
     */
    public void setPlaceRef(PointRef placeRef) {
        this.placeRef = placeRef;
    }
    
    /**
     * Sets the place reference that this wrapper should wrap
     * 
     * @param placeRef an area reference to wrap
     */
    public void setPlaceRef(AreaRef placeRef) {
        this.placeRef = placeRef;
    }

    /**
     * Gets the reference to the place to be avoided
     * 
     * @return the place reference. Can be null or an instance of {@link PointRef} or {@link AreaRef}.
     */
    public Serializable getPlaceRef() {
        return placeRef;
    }
    
}
