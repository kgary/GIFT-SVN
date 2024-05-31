/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

import mil.arl.gift.common.enums.DetonationResultEnum;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;

/**
 * An update that provides the state of detonation within a training application in a domain knowledge session
 *
 * @author nroberts
 */
@SuppressWarnings("serial")
public class DetonationUpdate implements Serializable {

    /** The location of the detonation (required) */
    private AbstractMapCoordinate location;
    
    /** the type of detonation, can be null (optional) */
    private DetonationResultEnum detonationType;
    
    /** location of the entity firing the weapon.  Can be null. (optional) */
    private AbstractMapCoordinate firingEntityLocation;

    /** The force ID associated with the entity firing the weapon */
    private Integer forceId;
    
    /** The ID of the domain session that is hosting the domain knowledge session this detonation is a part of */
    private int hostDomainSessionId;

    /**
     * Default no argument constructor needed for GWT serialization
     */
    protected DetonationUpdate() {}

    /**
     * Creates an update indicating that a detonation occurred at the given location within the given
     * domain knowledge session
     * 
     * @param hostDomainSessionId the ID of the domain session that is hosting
     *        the domain knowledge session this detonation is a part of
     * @param location the location of the detonation. Cannot be null.
     * @param the type of detonation, can be null.
     * @param location of the entity firing the weapon.  Can be null.
     * @param force ID of the entity firing the weapon. Can be null.
     */
    public DetonationUpdate(int hostDomainSessionId, AbstractMapCoordinate location, DetonationResultEnum detonationType, AbstractMapCoordinate firingEntityLocation, Integer forceId) {
        
        this();
        
        if(location == null) {
            throw new IllegalArgumentException("The location of a detonation cannot be null");
        }
        
        this.hostDomainSessionId = hostDomainSessionId;
        this.location = location;
        
        this.detonationType = detonationType;
        this.firingEntityLocation = firingEntityLocation;
        this.forceId = forceId;
    }

    /**
     * Gets the location of the detonation represented by this update
     *
     * @return the detonation's location. Will not be null.
     */
    public AbstractMapCoordinate getLocation() {
        return location;
    }
    
    /**
     * The type of detonation.
     * @return enumerated type of detonation. Can be null.
     */
    public DetonationResultEnum getDetonationType(){
        return detonationType;
    }
    
    /**
     * Gets the ID of the domain session that is hosting the domain knowledge session this detonation is a part of
     *
     * @return the host domain session ID
     */
    public int getHostDomainSessionId() {
        return hostDomainSessionId;
    }
    
    /**
     * Gets the location of the entity firing a weapon to cause this detonation.
     * @return the firing entity location.  Can be null.
     */
    public AbstractMapCoordinate getFiringEntityLocation(){
        return firingEntityLocation;
    }
    
    /**
     * Gets the force ID associated with the entity firing the weapon
     * 
     * @return the force ID. Can be null.
     */
    public Integer getForceId() {
        return forceId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[DetonationUpdate: location = ");
        builder.append(location);
        if(detonationType != null){
            builder.append(", type = ").append(detonationType.getDisplayName());
        }
        if(firingEntityLocation != null){
            builder.append(", firingLocation = ").append(firingEntityLocation);
        }
        if(forceId != null){
            builder.append(", forceId = ").append(forceId);
        }
        builder.append(", hostDomainSessionId = ").append(hostDomainSessionId);
        builder.append("]");
        return builder.toString();
    }
}
