/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.io.Serializable;

import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains a unique identifier for an entity
 *
 * @author mhoffman
 *
 */
public class EntityIdentifier implements TrainingAppState, Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** The unique id of an entity */
    private Integer entityID;

    /** The simulation address of this entity */
    private SimulationAddress simAddr;

    /** The entity's role in a team organization, if applicable */
    private String roleName;

    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private EntityIdentifier(){}

    /**
     * Class constructor
     *
     * @param simAddr the simulation address of this entity
     * @param entityID the unique id of an entity
     */
    public EntityIdentifier(SimulationAddress simAddr, Integer entityID){
        this.simAddr = simAddr;
        this.entityID = entityID;
    }

    public SimulationAddress getSimulationAddress(){
        return simAddr;
    }

    public Integer getEntityID(){
        return entityID;
    }

    /**
     * Gets the name of this entity's role in a team organization
     *
     * @return the entity's role name.  Can be null but not empty.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Sets the name of this entity's role in a team organization
     *
     * @param roleName the entity's role name.  Can be null but not empty.
     */
    public void setRoleName(String roleName) {

        if(roleName != null && StringUtils.isBlank(roleName)){
            throw new IllegalArgumentException("The rolename can't be blank.");
        }
        this.roleName = roleName;
    }

    @Override
    public boolean equals(Object other){

        if(other instanceof EntityIdentifier){

            final EntityIdentifier otherEntityId = (EntityIdentifier)other;

            // NOTE: role name should not be used here since it can be null and role name is something
            //       GIFT added and is not part of any standard for uniquely identifying simulation entities
            if(this.getEntityID().intValue() != otherEntityId.getEntityID().intValue()){
                return false;
            } else if (!getSimulationAddress().equals(otherEntityId.getSimulationAddress())) {
                return false;
            }

            return true;
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hashCode = 0;

        hashCode |= getEntityID() << 2;
        hashCode |= getSimulationAddress().getSiteID() << 4;
        hashCode |= getSimulationAddress().getApplicationID() << 6;

        return hashCode;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[EntityIdentifier: ");
        sb.append("Entity ID = ").append(getEntityID());
        if(roleName != null){
            sb.append(", Role Name = ").append(getRoleName());
        }
        sb.append(", Simulation Address = ").append(getSimulationAddress().toString());
        sb.append("]");

        return sb.toString();
    }

}
