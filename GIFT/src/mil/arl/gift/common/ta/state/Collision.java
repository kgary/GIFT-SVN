/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

/**
 * This game state class contains information about a collision between two objects.
 * 
 * @author mhoffman
 *
 */
public class Collision implements TrainingAppState {
    
    /**
     * a unique identifier for the entity that is issuing the collision information
     */
    private EntityIdentifier issuingEntityID;
    
    /**
     * a unique identifier for the entity which has collided with the issuing entity
     */
    private EntityIdentifier collidingEntityID;
    
    /** the type of collision */
    private Integer collisionType;
    
    /**
     * Types of collisions
     * From: http://faculty.nps.edu/brutzman/vrtp/mil/navy/nps/disEnumerations/JdbeHtmlFiles/pdu/103.htm
     */
    public static final int ELASTIC = 0;
    public static final int INELASTIC = 1;
    public static final int OTHER = 2;
    
    /**
     * Set the attributes
     * 
     * @param issuingEntityID a unique identifier for the entity that is issuing the collision information
     * @param collidingEntityID a unique identifier for the entity which has collided with the issuing entity
     * @param collisionType the type of collision 
     */
    public Collision(EntityIdentifier issuingEntityID, EntityIdentifier collidingEntityID, Integer collisionType){
        this.issuingEntityID = issuingEntityID;
        this.collidingEntityID = collidingEntityID;
        this.collisionType = collisionType;
    }
    
    /**
     * Return the unique identifier for the entity that is issuing the collision information
     * @return EntityIdentifier
     */
    public EntityIdentifier getIssuingEntityID(){
        return issuingEntityID;
    }
    
    /**
     * Return the unique identifier for the entity which has collided with the issuing entity
     * @return EntityIdentifier
     */
    public EntityIdentifier getCollidingEntityID(){
        return collidingEntityID;
    }
    
    /**
     * Return the type of collision 
     * @return Integer
     */
    public Integer getCollisionType(){
        return collisionType;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Collision: ");
        sb.append("issuingEntityID = ").append(getIssuingEntityID());
        sb.append(", collidingEntityID = ").append(getCollidingEntityID());
        sb.append(", collisionType = ").append(getCollisionType());
        sb.append("]");
        return sb.toString();
    }
}
