/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * This message represents an entity state.
 *
 * @author mhoffman
 *
 */
public class EntityState implements TrainingAppState {

    /**
     * a unique identifier for the entity
     */
    private EntityIdentifier entityID;

    /**
     * Entity force identifier
     */
    private Integer forceID;

    /**
     * Entity type
     */
    private EntityType entityType;
    
    /**
     * Alternative entity type (optional)
     */
    private EntityType alternativeEntityType;

    /**
     * entity linear velocity (x,y,z)
     */
    private Vector3d linearVel;

    /**
     * entity world coordinates (GCC) (x,y,z)
     */
    private Point3d location;

    /**
     * entity orientation (DIS Euler Angles) (psi, theta, phi)
     */
    private Vector3d orientation;

    /**
     * entity articulation parameters
     */
    private List<ArticulationParameter> artParams;

    /**
     * the entity appearance
     */
    private EntityAppearance appearance;

    /**
     * The entity marking. Can be used in team scenarios to differentiate players
     */
    private EntityMarking entityMarking;
    
    /**
     * (optional) Used to provide the parameters for dead reckoning the position and orientation of the entity. 
     * Dead Reckoning Algorithm in use, Entity Acceleration and Angular velocity shall be included as a part of the dead reckoning parameters.
     */
    private DeadReckoningParameters deadReckoningParameters;

    /**
     * Class constructor - set class attributes
     *
     * @param entityID a unique identifier for the entity
     * @param entityType the type of entity
     * @param forceID Entity force identifier
     * @param linearVel entity linear velocity (x,y,z)
     * @param location entity world coordinates (GCC) (x,y,z)
     * @param orientation  entity orientation (DIS Euler Angles) (psi, theta, phi)
     * @param artParams entity articulation parameters, can be null or empty.
     * @param appearance the entity appearance
     * @param entityMarking the entity marking. Can be used in team scenarios to differentiate players.  Can be null
     * when parsing legacy GIFT messages.
     */
    public EntityState(EntityIdentifier entityID, Integer forceID,
    				   EntityType entityType, Vector3d linearVel,
    				   Point3d location, Vector3d orientation,
    				   List<ArticulationParameter> artParams,
    				   EntityAppearance appearance, EntityMarking entityMarking) {

        this.entityID = entityID;
        this.forceID = forceID;
        this.entityType = entityType;
        this.linearVel = linearVel;
        this.location = location;
        this.orientation = orientation;
        this.artParams = artParams;
        this.appearance = appearance;
        this.entityMarking = entityMarking;
    }

    /**
     * Creates a new {@link EntityState} with the same field values as this
     * {@link EntityState}.
     *
     * @return The copy of this {@link EntityState}. Can't be null.
     */
    private EntityState shallowCopy() {
        EntityState entityState = new EntityState(entityID, forceID, entityType, linearVel, location, orientation, artParams, appearance, entityMarking);
        entityState.setAlternativeEntityType(alternativeEntityType);
        return entityState;
    }

    /**
     * Getter for the unique ID of the entity represented by this
     * {@link EntityState}.
     *
     * @return The value of {@link #entityID}.
     */
    public EntityIdentifier getEntityID() {
        return entityID;
    }

    /**
     * Getter for the ID of the force to which the represented entity belongs.
     *
     * @return The value of {@link #forceID}.
     */
    public Integer getForceID() {
        return forceID;
    }

    /**
     * Getter for the {@link EntityType} of the entity represented by this
     * {@link EntityState}.
     *
     * @return The value of {@link #entityType}.
     */
    public EntityType getEntityType() {
        return entityType;
    }

    /**
     * Getter for the {@link EntityType} of the alternative entity type of
     * this {@link EntityState}.
     * @return the value of the {@link #alternativeEntityType}
     */
    public EntityType getAlternativeEntityType() {
        return alternativeEntityType;
    }

    /**
     * Set the {@link EntityType} of the alternative entity type of
     * this {@link EntityState}.
     * @param alternativeEntityType the alternative entity type
     */
    public void setAlternativeEntityType(EntityType alternativeEntityType) {
        this.alternativeEntityType = alternativeEntityType;
    }

    /**
     * Creates a copy of this {@link EntityState} that contains a different
     * value for {@link #entityType}.
     *
     * @param entityType The new value of {@link #entityType} that should be
     *        used.
     * @return The newly created copy of this {@link EntityState}.
     */
    public EntityState replaceEntityType(EntityType entityType) {
        EntityState toRet = shallowCopy();
        toRet.entityType = entityType;
        return toRet;
    }

    /**
     * Getter for the linear velocity of the represented entity.
     *
     * @return The value of {@link #linearVel}.
     */
    public Vector3d getLinearVelocity() {
        return linearVel;
    }

    /**
     * Getter for the location of the represented entity.
     *
     * @return The value of {@link #location}.
     */
    public Point3d getLocation() {
        return location;
    }

    /**
     * Creates a shallow copy of this {@link EntityState} that differs in its
     * {@link #location} value.
     *
     * @param location The new {@link Point3d} value to use for the new
     *        {@link EntityState}'s {@link #location}.
     * @return The newly created {@link EntityState}. Can't be null.
     */
    public EntityState replaceLocation(Point3d location) {
        EntityState toRet = shallowCopy();
        toRet.location = location;
        return toRet;
    }

    /**
     * Getter for the orientation of the entity represented by this
     * {@link EntityState}.
     *
     * @return The value of {@link #orientation}.
     */
    public Vector3d getOrientation() {
        return orientation;
    }

    /**
     * Builds a shallow copy that differs only in its {@link #orientation}.
     *
     * @param orientation The new value to use for {@link #orientation}.
     * @return The newly created {@link EntityState}. Can't be null.
     */
    public EntityState replaceOrientation(Vector3d orientation) {
        EntityState toRet = shallowCopy();
        toRet.orientation = orientation;
        return toRet;
    }

    /**
     * Getter for the articulation parameters of the entity represented by this
     * {@link EntityState}.
     *
     * @return The value of {@link #artParams}.  Can be null or empty.
     */
    public List<ArticulationParameter> getArticulationParameters() {
        return artParams;
    }

    /**
     * Getter for the {@link EntityAppearance} of the entity represented by this
     * {@link EntityState}.
     *
     * @return The value of {@link #appearance}.
     */
    public EntityAppearance getAppearance() {
        return appearance;
    }

    /**
     * Creates a shallow copy of this {@link EntityState} which differs only in
     * its value for {@link #appearance}.
     *
     * @param appearance The new {@link EntityAppearance} value used by the
     *        newly created {@link EntityState}.
     * @return The newly created {@link EntityState}. Can't be null.
     */
    public EntityState replaceAppearance(EntityAppearance appearance) {
        EntityState toRet = shallowCopy();
        toRet.appearance = appearance;
        return toRet;
    }

    /**
     * Can be null for legacy messages.
     *
     * @return the entity marking information for this entity state
     */
    public EntityMarking getEntityMarking(){
        return entityMarking;
    }

    /**
     * Creates a shallow copy of this {@link EntityState} that differs only in
     * the value of its {@link EntityMarking}.
     *
     * @param entityMarking The new {@link EntityMarking} value to use for
     *        {@link #entityMarking}.
     * @return A shallow copy of this {@link EntityState}. Can't be null.
     */
    public EntityState replaceEntityMarking(EntityMarking entityMarking) {
        EntityState toRet = shallowCopy();
        toRet.entityMarking = entityMarking;
        return toRet;
    }

    /**
     * Getter for the dead reckoning parameters 
     * @return can be null, Used to provide the parameters for dead reckoning the position and orientation of the entity. 
     * Dead Reckoning Algorithm in use, Entity Acceleration and Angular velocity shall be included as a part of the dead reckoning parameters.
     */
    public DeadReckoningParameters getDeadReckoningParameters() {
        return deadReckoningParameters;
    }

    /**
     * Setter for the dead reckoning parameters.
     * 
     * @param deadReckoningParameters can be null. Used to provide the parameters for dead reckoning the position and orientation of the entity. 
     * Dead Reckoning Algorithm in use, Entity Acceleration and Angular velocity shall be included as a part of the dead reckoning parameters.
     */
    public void setDeadReckoningParameters(DeadReckoningParameters deadReckoningParameters) {
        this.deadReckoningParameters = deadReckoningParameters;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[EntityStateMessage: ");
        sb.append("Entity ID = ").append(getEntityID().toString());
        sb.append(", Force ID = ").append(getForceID());
        sb.append(", Entity Type = ").append(getEntityType().toString());
        sb.append(", Alternate Entity Type = ").append(getAlternativeEntityType());
        sb.append(", Location = ").append(getLocation().toString());
        sb.append(", Orientation = ").append(getOrientation().toString());
        sb.append(", Linear Velocity = ").append(getLinearVelocity().toString());
        if(artParams != null){
            sb.append(", Articulation Parameters = [");
            for(ArticulationParameter artParam : artParams){
                sb.append(artParam.toString()).append(" ");
            }
            sb.append("]");
        }
        sb.append(", Appearance = ").append(getAppearance());
        sb.append(", Marking = ").append(getEntityMarking());
        sb.append(", Dead Reckoning = ").append(getDeadReckoningParameters());
        sb.append("]");

        return sb.toString();
    }
}
