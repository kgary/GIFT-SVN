/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

import mil.arl.gift.common.gwt.shared.websocket.messages.AbstractWebSocketMessage;
import mil.arl.gift.common.ta.state.RemoveEntity;

/**
 * A message used to notify the client to remove an entity.
 * 
 * @author mhoffman
 *
 */
public class RemoveEntityMessage extends AbstractWebSocketMessage implements Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** contains information about the entity to remove */
    private RemoveEntity removeEntityRequest;
    
    /** identifies the entity to remove in a specific hosted domain session */
    private SessionEntityIdentifier entityToRemove;
    
    /**
     * Default no-arg constructor needed for GWT serialization 
     */
    protected RemoveEntityMessage(){}
    
    /**
     * Set attributes
     * 
     * @param removeEntity contains information about the entity to remove.  Can't be null.
     * @param entityToRemove identifies the entity to remove in a specific hosted domain session.  Can't be null.
     */
    public RemoveEntityMessage(RemoveEntity removeEntity, SessionEntityIdentifier entityToRemove){
        setRemoveEntityRequest(removeEntity);
    }

    /**
     * Return the information about the entity to remove.
     * @return won't be null.
     */
    public RemoveEntity getRemoveEntityRequest() {
        return removeEntityRequest;
    }

    /**
     * Set the information about the entity to remove.
     * @param removeEntityRequest Can't be null.
     */
    private void setRemoveEntityRequest(RemoveEntity removeEntityRequest) {
        if(removeEntityRequest == null){
            throw new IllegalArgumentException("RemoveEntityRequest is null");
        }
        this.removeEntityRequest = removeEntityRequest;
    }
    
    /**
     * Gets the ID that uniquely identifies this entity and its domain knowledge session
     * 
     * @param entityToRemove the entity's session ID. Cannot be null.
     */
    protected void setSessionEntityId(SessionEntityIdentifier entityToRemove) {
        
        if(entityToRemove == null) {
            throw new IllegalArgumentException("The identifier for this entity location update cannot be null.");
        }
        
        this.entityToRemove = entityToRemove;
    }
    
    /**
     * Gets the ID that uniquely identifies this entity and its domain knowledge session
     * 
     * @return the entity's session ID. Will not be null.
     */
    public SessionEntityIdentifier getSessionEntityId() {
        return entityToRemove;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[RemoveEntityMessage: removeEntityRequest = ");
        builder.append(removeEntityRequest);
        builder.append(", entityToRemove = ").append(entityToRemove);
        builder.append("]");
        return builder.toString();
    }
    
    
}
