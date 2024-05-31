/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.io.Serializable;

/**
 * This message represents a request to remove an entity from an exercise.
 * 
 * @author mhoffman
 *
 */
public class RemoveEntity implements TrainingAppState, Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** the originating entity */
    private EntityIdentifier originatingId;
    
    /** the intended receiving entity */
    private EntityIdentifier receivingId;
    
    /** the specific and unique entity removal request being made by the simulation manager */
    private int requestId;
    
    /**
     * Set attributes
     * @param originatingId the originating entity, can't be null
     * @param receivingId the intended receiving entity, can't be null
     * @param requestId the specific and unique entity removal request being made by the simulation manager
     */
    public RemoveEntity(EntityIdentifier originatingId, EntityIdentifier receivingId, int requestId){
        setOriginatingId(originatingId);
        setReceivingId(receivingId);
        setRequestId(requestId);
    }

    /**
     * Set the originating entity id.
     * @return the originating entity, can't be null
     */
    public EntityIdentifier getOriginatingId() {
        return originatingId;
    }

    /**
     * Return the originating entity id.
     * @param originatingId  the originating entity, can't be null
     */
    private void setOriginatingId(EntityIdentifier originatingId) {
        
        if(originatingId == null){
            throw new IllegalArgumentException("The originating id is null");
        }
        this.originatingId = originatingId;
    }

    /**
     * Return the intended receiving entity
     * @return the intended receiving entity, won't be null.
     */
    public EntityIdentifier getReceivingId() {
        return receivingId;
    }

    /**
     * Set the intended receiving entity
     * @param receivingId the intended receiving entity, can't be null
     */
    private void setReceivingId(EntityIdentifier receivingId) {
        
        if(receivingId == null){
            throw new IllegalArgumentException("The receiving id is null");
        }
        this.receivingId = receivingId;
    }

    /**
     * Return the specific and unique entity removal request being made by the simulation manager
     * @return the specific and unique entity removal request being made by the simulation manager
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Set the specific and unique entity removal request being made by the simulation manager
     * @param requestId the specific and unique entity removal request being made by the simulation manager
     */
    private void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[RemoveEntity: originatingId = ");
        builder.append(originatingId);
        builder.append(", receivingId = ");
        builder.append(receivingId);
        builder.append(", requestId = ");
        builder.append(requestId);
        builder.append("]");
        return builder.toString();
    }
    
    
}
