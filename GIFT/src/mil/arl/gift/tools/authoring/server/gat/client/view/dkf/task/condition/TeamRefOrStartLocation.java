/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition;

import java.io.Serializable;

import generated.dkf.StartLocation;

/**
 * A wrapper item used to edit both {@link StartLocation}s and team member references in the same list editor
 * 
 * @author nroberts
 */
public class TeamRefOrStartLocation {
    
    /** The team member reference or start location this item wraps */
    private Serializable refOrLocation;
    
    /**
     * Creates a new wrapper with no team member reference or start location (i.e. represents a null entry in the list)
     */
    public TeamRefOrStartLocation() {}

    /**
     * Creates a wrapper around the given team member reference
     * 
     * @param teamRef the name of the team member being referenced. Can be null.
     */
    public TeamRefOrStartLocation(String teamRef) {
        this();
        setRefOrLocation(teamRef);
    }
    
    /**
     * Creates a wrapper around the given start location
     * 
     * @param startLocation an entity's starting location. Can be null.
     */
    public TeamRefOrStartLocation(StartLocation startLocation) {
        this();
        setRefOrLocation(startLocation);
    }
    
    /**
     * Wraps this item around the given team member reference
     * 
     * @param teamRef the name of the team member being referenced. Can be null.
     */
    public void setRefOrLocation(String teamRef) {
        refOrLocation = teamRef;
    }
    
    /**
     * Wraps this item around the given start location
     * 
     * @param startLocation an entity's starting location. Can be null.
     */
    public void setRefOrLocation(StartLocation startLocation) {
        refOrLocation = startLocation;
    }
    
    /**
     * Gets the team member reference or start location that this item wraps
     * 
     * @return the wrapped team member reference or start location. Can be null.
     */
    public Serializable getRefOrLocation() {
        return refOrLocation;
    }
}
