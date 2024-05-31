/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.PostureEnum;
import mil.arl.gift.tools.map.shared.AbstractMapCoordinate;
import mil.arl.gift.tools.map.shared.SIDC;

/**
 * An update that provides the state of a training application entity in a domain knowledge session
 *
 * @author nroberts
 */
@SuppressWarnings("serial")
public class EntityStateUpdate implements Serializable {

    /** The ID that uniquely identifies this entity and its domain knowledge session */
    private SessionEntityIdentifier sessionEntityId;

    /** The name of this entity's role in the domain knowledge session's team organization, if it represents such a role */
    private String roleName;

    /** The location of the entity */
    private AbstractMapCoordinate location;

    /** The entity type */
    private SIDC sidc;

    /** Entity force identifier */
    private Integer forceId;

    /** The entity's recorded velocity, in meters per second */
    private Double velocity;

    /** The entity's recorded orientation, in degrees clockwise from true north */
    private Double orientation;

    /** The recorded marker assigned to this entity by its training application */
    private String entityMarking;

    /** Whether this entity can be played by a learner */
    private boolean isPlayable;

    /** Information surrounding the learner controlling this entity, if such a learner exists*/
    private LearnerEntityInfo learnerInfo;

    /** the damage value of this entity appearance */
    private DamageEnum damage;
    
    /** the posture value of this entity appearance */
    private PostureEnum posture;

    /** flag used to indicate if this entity is active or deactivated */
    private boolean active = true;

    /**
     * Default no argument constructor needed for GWT serialization
     */
    protected EntityStateUpdate() {}

    /**
     * Creates a new update for an entity at the given location
     *
     * @param sessionEntityId an ID that uniquely identifies this entity and its domain knowledge session. Cannot be null.
     * @param location the location of the entity. Cannot be null.
     * @param sidc the SIDC code representing the entity's military symbology. Cannot be null.
     * @param forceId the entity force identifier. Cannot be null.
     * @param damage the damage value of this entity appearance.  Can't be null.
     * @param posture the posture value of this entity appearance.  Can't be null.
     * @param active flag used to indicate if this entity is active or deactivated
     */
    public EntityStateUpdate(SessionEntityIdentifier sessionEntityId, AbstractMapCoordinate location, SIDC sidc, Integer forceId,
            DamageEnum damage, boolean active) {
        this(sessionEntityId, location, sidc, forceId, null, damage, active);
    }

    /**
     * Creates a new update for an entity being played by a learner at the given location
     *
     * @param sessionEntityId an ID that uniquely identifies this entity and its domain knowledge session. Cannot be null.
     * @param location the location of the entity. Cannot be null.
     * @param sidc the SIDC code representing the entity's military symbology. Cannot be null.
     * @param forceId the entity force identifier. Cannot be null.
     * @param learnerInfo information surrounding the learner controlling this entity. Can be null.
     * @param damage the damage value of this entity appearance.  Can't be null.
     * @param posture the posture value of this entity appearance.  Can't be null.
     * @param active flag used to indicate if this entity is active or deactivated
     */
    public EntityStateUpdate(SessionEntityIdentifier sessionEntityId, AbstractMapCoordinate location, SIDC sidc,
            Integer forceId, LearnerEntityInfo learnerInfo, DamageEnum damage, boolean active) {
        this();

        setLearnerInfo(learnerInfo);
        setSessionEntityId(sessionEntityId);
        setLocation(location);
        setSIDC(sidc);
        setForceId(forceId);
        setDamage(damage);
        setActive(active);
    }

    /**
     * Set the damage state for the entity.
     *
     * @param damage the enumerated damage state.  Can't be null.
     */
    private void setDamage(DamageEnum damage){

        if(damage == null){
            throw new IllegalArgumentException("Damage is null");
        }

        this.damage = damage;
    }

    /**
     * Return the damage state of this entity appearance
     *
     * @return the enumerated damage state.  Will not be null.
     */
    public DamageEnum getDamage(){
        return damage;
    }

    /**
     * Set the posture state for the entity.
     *
     * @param posture the enumerated damage state.  Can't be null.
     */
    public void setPosture(PostureEnum posture){

        if(posture == null){
            throw new IllegalArgumentException("Posture is null");
        }

        this.posture = posture;
    }

    /**
     * Return the posture state of this entity appearance
     *
     * @return the enumerated posture state.  Will not be null.
     */
    public PostureEnum getPosture(){
        return posture;
    }
    
    /**
     * Return whether the entity is active or deactivated.
     *
     * @return true if the entity is active (default), or false if deactivated.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Setter for the flag indicating whether or not the current flag is active.
     *
     * @param active The new value of {@link #active}.
     */
    private void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Gets the location of the entity represented by this update
     *
     * @return the entity's location. Will not be null.
     */
    public AbstractMapCoordinate getLocation() {
        return location;
    }

    /**
     * Sets the location of the entity represented by this update
     *
     * @param location the entity's location. Cannot be null.
     */
    protected void setLocation(AbstractMapCoordinate location) {

        if(location == null) {
            throw new IllegalArgumentException("The location of this entity location update cannot be null.");
        }

        this.location = location;
    }

    /**
     * Gets the ID that uniquely identifies this entity and its domain knowledge session
     *
     * @param sessionEntityId the entity's session ID. Cannot be null.
     */
    protected void setSessionEntityId(SessionEntityIdentifier sessionEntityId) {

        if(sessionEntityId == null) {
            throw new IllegalArgumentException("The identifier for this entity location update cannot be null.");
        }

        this.sessionEntityId = sessionEntityId;
    }

    /**
     * Gets the ID that uniquely identifies this entity and its domain knowledge session
     *
     * @return the entity's session ID. Will not be null.
     */
    public SessionEntityIdentifier getSessionEntityId() {
        return sessionEntityId;
    }

    /**
     * Gets the name of this entity's role in the domain knowledge session's team organization, if it represents such a role
     *
     * @return the entity's role name. Can be null.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Sets the name of this entity's role in the domain knowledge session's
     * team organization, if it represents such a role
     *
     * @param roleName the entity's role name. Can be null.
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    /**
     * Sets the SIDC code representing the entity's military symbology
     *
     * @param sidc the SIDC code. Cannot be null.
     */
    private void setSIDC(SIDC sidc) {
        if (sidc == null) {
            throw new IllegalArgumentException("The parameter 'sidc' cannot be null.");
        }

        this.sidc = sidc;
    }

    /**
     * Gets the SIDC code representing the entity's military symbology
     *
     * @return the SIDC code. Will not be null.
     */
    public SIDC getSIDC() {
        return sidc;
    }

    /**
     * Sets the entity force identifier.
     *
     * @param forceId the entity force identifier. Cannot be null.
     */
    private void setForceId(Integer forceId) {
        if (forceId == null) {
            throw new IllegalArgumentException("The parameter 'forceId' cannot be null.");
        }

        this.forceId = forceId;
    }

    /**
     * Gets the force identifier of the entity represented by this update
     *
     * @return the entity's force identifier. Will not be null.
     */
    public Integer getForceId() {
        return forceId;
    }

    /**
     * Gets whether this entity can be played by a learner
     *
     * @return whether this entity can be played by a learner
     */
    public boolean isPlayable() {
        return isPlayable;
    }

    /**
     * Sets whether this entity can be played by a learner
     *
     * @param isPlayable whether this entity can be played by a learner
     */
    public void setPlayable(boolean isPlayable) {
        this.isPlayable = isPlayable;
    }

    /**
     * Gets this entity's recorded velocity, in meters per second
     *
     * @return the recorded velocity. Can be null.
     */
    public Double getVelocity() {
        return velocity;
    }

    /**
     * Sets this entity's recorded velocity, in meters per second
     *
     * @param velocity the velocity to record. Can be null.
     */
    public void setVelocity(Double velocity) {
        this.velocity = velocity;
    }

    /**
     * Gets this entity's recorded orientation, in degrees clockwise from true north
     *
     * @return the recorded orientation. Can be null.
     */
    public Double getOrientation() {
        return orientation;
    }

    /**
     * Sets this entity's recorded orientation, in degrees clockwise from true north
     *
     * @param orientation the orientation to record. Can be null.
     */
    public void setOrientation(Double orientation) {
        this.orientation = orientation;
    }

    /**
     * Gets this entity's recorded marker within its associated training application
     *
     * @return the recorded entity marker. Can be null.
     */
    public String getEntityMarking() {
        return entityMarking;
    }

    /**
     * Sets this entity's recorded marker within its associated training
     * application
     *
     * @param entityMarking the entity marker to record. Can be null.
     */
    public void setEntityMarking(String entityMarking) {
        this.entityMarking = entityMarking;
    }

    /**
     * Gets the information surrounding the learner controlling this entity. A value of null indicates
     * that this entity is not being controlled by a learner.
     *
     * @return the learner info. Can be null, if this entity is not being controlled by a learner.
     */
    public LearnerEntityInfo getLearnerInfo() {
        return learnerInfo;
    }

    /**
     * Sets the information surrounding the learner controlling this entity. A value of null indicates
     * that this entity is not being controlled by a learner.
     *
     * @param learnerInfo the learner info. Can be null, if this entity is not being controlled by a learner.
     */
    public void setLearnerInfo(LearnerEntityInfo learnerInfo) {
        this.learnerInfo = learnerInfo;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[EntityStateUpdate:  sessionEntityId = ");
        builder.append(sessionEntityId);
        builder.append(", roleName = ");
        builder.append(roleName);
        builder.append(", location = ");
        builder.append(location);
        builder.append(", sidc = ");
        builder.append(sidc);
        builder.append(", forceId = ");
        builder.append(forceId);
        builder.append(", velocity = ");
        builder.append(velocity);
        builder.append(", orientation = ");
        builder.append(orientation);
        builder.append(", entityMarking = ");
        builder.append(entityMarking);
        builder.append(", isPlayable = ");
        builder.append(isPlayable);
        builder.append(", learnerInfo = ");
        builder.append(learnerInfo);
        builder.append(", damage = ");
        builder.append(damage);
        builder.append(", active = ");
        builder.append(active);
        builder.append("]");
        return builder.toString();
    }


}
