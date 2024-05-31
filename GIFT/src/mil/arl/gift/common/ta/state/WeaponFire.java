/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import javax.vecmath.Vector3d;


/**
 * This class represents a weapon fire event
 * 
 * @author mhoffman
 *
 */
public class WeaponFire implements TrainingAppState {

    /** the unique id of the firing entity */
    private EntityIdentifier firingEntityID;

    /** the unique id of the target entity being fired upon */
    private EntityIdentifier targetEntityID;

    /** the unique id of the munition being fired */
    private EntityIdentifier munitionID;

    /** the unique id for this event */
    private EventIdentifier eventID;

    /** the velocity of the munition at firing */
    private Vector3d velocity;

    /** the location of the weapon fire */
    private Vector3d location;
    
    /** the burst descriptor of the weapon fire */
    private BurstDescriptor burstDescriptor;

    /**
     * Class constructor - set class attributes
     * 
     * @param firingEntityID the unique id of the firing entity
     * @param targetEntityID the unique id of the target entity being fired upon
     * @param munitionID the unique id of the munition being fired
     * @param eventID the unique id for this event 
     * @param velocity  the velocity of the munition at firing 
     * @param location the location of the weapon fire
     * @param burstDescriptor  the burst descriptor of the weapon fire
     */
    public WeaponFire(EntityIdentifier firingEntityID, EntityIdentifier targetEntityID, EntityIdentifier munitionID, EventIdentifier eventID, Vector3d velocity,
            Vector3d location, BurstDescriptor burstDescriptor){
        this.firingEntityID = firingEntityID;
        this.targetEntityID = targetEntityID;
        this.munitionID = munitionID;
        this.eventID = eventID;
        this.velocity = velocity;
        this.location = location;
        this.burstDescriptor = burstDescriptor;
    }

    public EntityIdentifier getFiringEntityID() {
        return firingEntityID;
    }

    public EntityIdentifier getTargetEntityID() {
        return targetEntityID;
    }

    public EntityIdentifier getMunitionID() {
        return munitionID;
    }

    public EventIdentifier getEventID() {
        return eventID;
    }

    public Vector3d getVelocity() {
        return velocity;
    }

    public Vector3d getLocation() {
        return location;
    }
    
    public BurstDescriptor getBurstDescriptor(){
        return burstDescriptor;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[WeaponFire: ");
        sb.append(", Firing Entity ID = ").append(getFiringEntityID());
        sb.append(", Target Entity ID = ").append(getTargetEntityID());
        sb.append(", Event ID = ").append(getEventID());
        sb.append(", Location = ").append(getLocation());
        sb.append(", Velocity = ").append(getVelocity());
        sb.append(", Munition ID = ").append(getMunitionID());
        sb.append(", Burst Descriptor = ").append(getBurstDescriptor());
        sb.append("]");

        return sb.toString();
    }

}
