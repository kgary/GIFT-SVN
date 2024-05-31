/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import javax.vecmath.Vector3d;

import mil.arl.gift.common.enums.DetonationResultEnum;

/**
 * This message represents a detonation
 *
 * @author mhoffman
 *
 */
public class Detonation implements TrainingAppState {

    /** the unique id of the firing entity */
    private EntityIdentifier firingEntityID;

    /** the unique id of the target entity being fired upon */
    private EntityIdentifier targetEntityID;

    /** the unique id of the munition being fired */
    private EntityIdentifier munitionID;

    /** the unique id for this event */
    private EventIdentifier eventID;

    /** the velocity of the munition at detonation */
    private Vector3d velocity;

    /** the location of the detonation */
    private Vector3d location;

    /** the description of the detonation's burst */
    private BurstDescriptor burstDescriptor;

    /** the type of detonation result */
    private DetonationResultEnum detonationResult;

    /**
     * Class constructor - set class attributes
     *
     * @param firingEntityID the unique id of the firing entity
     * @param targetEntityID the unique id of the target entity being fired upon
     * @param munitionID the unique id of the munition being fired
     * @param eventID the unique id for this event
     * @param velocity the velocity of the munition at detonation
     * @param location the location of the detonation
     * @param burstDescriptor The description of the munition that was
     *        detonated.
     * @param detonationResult the type of detonation result
     */
    public Detonation(EntityIdentifier firingEntityID, EntityIdentifier targetEntityID, EntityIdentifier munitionID, EventIdentifier eventID, Vector3d velocity,
            Vector3d location, BurstDescriptor burstDescriptor, DetonationResultEnum detonationResult) {

        this.firingEntityID = firingEntityID;
        this.targetEntityID = targetEntityID;
        this.munitionID = munitionID;
        this.eventID = eventID;
        this.velocity = velocity;
        this.location = location;
        this.burstDescriptor = burstDescriptor;
        this.detonationResult = detonationResult;
    }

    /**
     * Getter for the unique id of the firing entity
     *
     * @return The value of {@link #firingEntityID}. Can be null.
     */
    public EntityIdentifier getFiringEntityID() {
        return firingEntityID;
    }

    /**
     * Getter for the unique id of the target entity being fired upon
     *
     * @return The value of {@link #targetEntityID}. Can be null.
     */
    public EntityIdentifier getTargetEntityID() {
        return targetEntityID;
    }

    /**
     * Getter for the unique id of the munition being fired.
     *
     * @return The value of {@link #munitionID}. Can be null.
     */
    public EntityIdentifier getMunitionID() {
        return munitionID;
    }

    /**
     * Getter for the unique id for this event.
     *
     * @return The value of {@link #eventID}. Can be null.
     */
    public EventIdentifier getEventID() {
        return eventID;
    }

    /**
     * Getter for the velocity of the munition at detonation.
     *
     * @return The value of {@link #velocity}. Can be null.
     */
    public Vector3d getVelocity() {
        return velocity;
    }

    /**
     * Getter for the location of the detonation
     *
     * @return The value of {@link #location}. Can be null.
     */
    public Vector3d getLocation() {
        return location;
    }

    /**
     * Getter for the description of the detonation's burst.
     *
     * @return The value of {@link #burstDescriptor}. Can be null.
     */
    public BurstDescriptor getBurstDescriptor() {
        return burstDescriptor;
    }

    /**
     * Getter for the type of detonation result.
     *
     * @return The value of {@link #detonationResult}. Can be null.
     */
    public DetonationResultEnum getDetonationResult() {
        return detonationResult;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[Detonation: ");
        sb.append("eventId = ").append(getEventID());
        sb.append(", firingEntityId = ").append(getFiringEntityID());
        sb.append(", targetEntityId = ").append(getTargetEntityID());
        sb.append(", location = ").append(getLocation());
        sb.append(", burstDescriptor = ").append(getBurstDescriptor());
        sb.append(", munitionId = ").append(getMunitionID());
        sb.append(", velocity = ").append(getVelocity());
        sb.append(", result = ").append(getDetonationResult());
        sb.append("]");

        return sb.toString();
    }

}
