/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.PostureEnum;

/**
 * This class contains the entity appearance for an entity
 *
 * @author mhoffman
 *
 */
public class EntityAppearance implements TrainingAppState {

    /** The posture value of this entity appearance */
    private PostureEnum posture;

    /** The damage value of this entity appearance */
    private DamageEnum damage;

    /** Flag used to indicate if this entity is active or deactivated */
    private boolean active = true;

    /**
     * Class constructor
     *
     * @param damage the damage value of this entity appearance.  Can't be null.
     * @param posture The posture value of this entity appearance
     */
    public EntityAppearance(DamageEnum damage, PostureEnum posture){
        setDamage(damage);
        this.posture = posture;
    }

    public EntityAppearance shallowCopy() {
        return new EntityAppearance(damage, posture);
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
     * @return the enumerated damage state.  Won't be null.
     */
    public DamageEnum getDamage(){
        return damage;
    }

    /**
     * Return the posture state of this entity appearance
     *
     * @return the enumerated posture
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
     * Set whether the entity is active or deactivated.
     *
     * @param active true if the entity is active (default), or false if deactivated.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    public EntityAppearance replaceActive(boolean active) {
        EntityAppearance toRet = shallowCopy();
        toRet.active = active;
        return toRet;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[EntityAppearance: ");
        sb.append("damage = ").append(getDamage());
        sb.append(", posture = ").append(getPosture());
        sb.append(", active = ").append(isActive());
        sb.append("]");

        return sb.toString();
    }

}
