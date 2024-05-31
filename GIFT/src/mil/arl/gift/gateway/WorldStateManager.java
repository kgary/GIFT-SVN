/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ta.state.EntityType;
import mil.arl.gift.common.util.StringUtils;

/**
 * This holds simulation data that is received by Gateway plugins, and may need to be tracked for later reference or use.
 * 
 * Currently, this can be used to track an entity's EntityType and force ID, so that this information does not need to be queried
 * every time the entity's state is updated.
 * 
 * In the future, this class can be used to track other data which is received once but needs to be referenced over a period of time.
 * 
 * @author mcambata
 *
 */
public class WorldStateManager {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(WorldStateManager.class);
    
    /** Map associating domain session IDs with WorldStateData */
    private ConcurrentHashMap<Integer, WorldState> domainSessionWorldStates;
    
    /** singleton instance of this class */
    private static WorldStateManager instance = null;
    
    /**
     * Class constructor
     */
    private WorldStateManager() {
        domainSessionWorldStates = new ConcurrentHashMap<Integer, WorldState>();
    }
    
    /**
     * Return the singleton instance of this class
     *
     * @return the singleton instance of this class
     */
    public static WorldStateManager getInstance() {

        if (instance == null) {
            instance = new WorldStateManager();
        }

        return instance;
    }
    
    /**
     * Gets the WorldState with the given domainSessionId, or if one does not already exist, creates it and returns it.
     * 
     * @param domainSessionId the domainSessionId associated with this WorldState.
     * @return the WorldState with the given domainSessionId
     */
    public WorldState getWorldState(int domainSessionId) {
                        
        WorldState worldStateToReturn = domainSessionWorldStates.get(domainSessionId);
        
        if (worldStateToReturn == null) {
            // This case only occurs if the worldState was set to null at some point, which should not happen.
            if (logger.isDebugEnabled()) {
                logger.debug("The world state in WorldStateManager with domainSessionId " + domainSessionId + " was null. It has been re-initialized.");
            }
            worldStateToReturn = new WorldState();
            domainSessionWorldStates.put(domainSessionId, worldStateToReturn);
        }
        
        return worldStateToReturn;
    }
    
    /**
     * Removes the WorldState with the given domainSessionId, if one exists.
     * 
     * @param domainSessionId a domainSessionId associated with a WorldState.
     */
    public void clearWorldState(int domainSessionId) {
        if (logger.isInfoEnabled()) {
            logger.info("Clearing the WorldState for domainSessionId " + domainSessionId + ".");
        }
        domainSessionWorldStates.remove(domainSessionId);
    }
    
    /**
     * Returns true if the chosen WorldState's entityTypeAndForceIdMap contains a key with the given entityId value.
     * 
     * @param domainSessionId the domainSessionId of the world state to check
     * @param entityId the integer ID of the entity to check.
     * @return true if a type and force ID have been defined for the given entity. False otherwise, 
     *          or if the specified domainSessionId has no corresponding WorldState.
     */
    public boolean entityTypeIsDefinedForId(int domainSessionId, int entityId) {
        WorldState worldState = domainSessionWorldStates.get(domainSessionId);
        
        if (worldState == null) {
            return false;
        } else {
            return worldState.entityTypeIsDefinedForId(entityId);
        }
    }
    
    /**
     * Gets the EntityTypeAndForceID for the given entity ID in the chosen WorldState.
     * Returns null if no such entity ID has been added to the map, or
     * the chosen domainSessionId has no corresponding WorldState.
     * 
     * @param domainSessionId the domain session ID of the world state to check
     * @param entityId the entity ID to check
     * @return the EntityTypeAndForceID for the given entity ID,
     * or null if that entity ID is not in the map,
     * or if the specified domainSessionId has no corresponding WorldState.
     */
    public EntityTypeAndForceId getEntityTypeAndForceIdFor(int domainSessionId, int entityId) {
        WorldState worldState = domainSessionWorldStates.get(domainSessionId);
        if (worldState == null) {
            return null;
        } else {
            return worldState.getEntityTypeAndForceIdFor(entityId);
        }
    }
    
    /**
     * Associates an EntityTypeAndForceID with the given entity ID, IF
     * a WorldState exists for the chosen domainSessionId.
     * 
     * @param domainSessionId the domain session ID of the world state to check
     * @param entityId the ID of the entity to set
     * @param newEntityTypeAndForceId the EntityTypeAndForceId value to set
     * @throws Exception if newEntityType or newForceId have illegal argument values,
     *          or if an exception occurs when putting a value into the entityTypeAndForceIdMap.
     */
    public void setEntityTypeAndForceId(int domainSessionId, int entityId, EntityType newEntityType, int newForceId) throws Exception {
        WorldState worldState = domainSessionWorldStates.get(domainSessionId);
        if (worldState != null) {
            worldState.setEntityTypeAndForceId(entityId, newEntityType, newForceId);
        }
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[WorldStateManager: ");
        sb.append("domainSessionWorldStates.dsIds = {");
        StringUtils.join(",", domainSessionWorldStates.keySet(), sb);
        sb.append("}]");
        return sb.toString();
    }
    
    /**
     * Holds the data used and accessed by WorldStateManager. The manager itself contains a map of data to domain sessions,
     * and each entry in that map will contain an instance of WorldStateData.
     * 
     * @author mcambata
     *
     */
    private class WorldState {
        
        /** Map associating entity IDs with DIS type and force ID */
        private ConcurrentHashMap<Integer, EntityTypeAndForceId> entityTypeAndForceIdMap;
        
        /**
         * Class constructor
         */
        public WorldState() {
            entityTypeAndForceIdMap = new ConcurrentHashMap<Integer, EntityTypeAndForceId>();
        }
        
        /**
         * Returns true if the entityTypeAndForceIdMap contains a key with the given entityId value.
         * 
         * @param entityId the integer ID of the entity to check.
         * @return true if a type and force ID have been defined for the given entity. False otherwise.
         */
        public boolean entityTypeIsDefinedForId(int entityId) {
            return entityTypeAndForceIdMap.containsKey(entityId);
        }
        
        /**
         * Gets the EntityTypeAndForceID for the given entity ID.
         * Returns null if no such entity ID has been added to the map.
         * 
         * @param entityId the entity ID to check
         * @return the EntityTypeAndForceID for the given entity ID,
         * or null if that entity ID is not in the map.
         */
        public EntityTypeAndForceId getEntityTypeAndForceIdFor(int entityId) {
            return entityTypeAndForceIdMap.get(entityId);
        }
        
        /**
         * Associates an EntityTypeAndForceID with the given entity ID.
         * 
         * @param entityId the ID of the entity to set
         * @param newEntityTypeAndForceId the EntityTypeAndForceId value to set
         * @throws Exception if newEntityType or newForceId have illegal argument values,
         *          or if an exception occurs when putting a value into the entityTypeAndForceIdMap.
         */
        public void setEntityTypeAndForceId(int entityId, EntityType newEntityType, int newForceId) throws Exception {
            EntityTypeAndForceId newEntityTypeAndForceId = new EntityTypeAndForceId(newEntityType, newForceId);
            entityTypeAndForceIdMap.put(entityId, newEntityTypeAndForceId);
        }
        
        @Override
        public String toString(){
            
            StringBuilder sb = new StringBuilder();
            sb.append("[WorldState: ");
            sb.append("entityTypeAndForceIdMap.size = ");
            sb.append(entityTypeAndForceIdMap.size());
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Holds an entity type and force ID. This data is necessary to determine what icon will be displayed for an entity.
     * 
     * @author mcambata
     *
     */
    public class EntityTypeAndForceId {
        
        /** The minimum value that forceId can hold */
        private static final int MIN_FORCE_ID_VALUE = 0;
        
        /** The maximum value that forceId can hold */
        private static final int MAX_FORCE_ID_VALUE = 3;
        
        /**
         * The DIS EntityType data being stored
         */
        private EntityType type;
        
        /**
         * An integer that corresponds to a DIS force ID enum
         */
        private int forceId;
        
        /**
         * Constructor. Stores the defined entity type and force ID for later reference.
         * 
         * @param newType a DIS EntityType. Cannot be null.
         * @param newForceId a DIS forceID. Must be between the values specified by MIN_FORCE_ID_VALUE and MAX_FORCE_ID_VALUE, inclusive.
         * @throws IllegalArgumentException if newType is null or newForceId is outside the range specified.
         */
        public EntityTypeAndForceId(EntityType newType, int newForceId) throws IllegalArgumentException {
            
            if (newType == null) {
                throw new IllegalArgumentException("EntityType newType cannot equal null.");
            }
            
            if (newForceId < MIN_FORCE_ID_VALUE || newForceId > MAX_FORCE_ID_VALUE) {
                throw new IllegalArgumentException("newForceId cannot be less than" + MIN_FORCE_ID_VALUE + " or greater than " + MAX_FORCE_ID_VALUE);
            }
            
            this.type = newType;
            this.forceId = newForceId;
        }
        
        /**
         * Gets the type of the EntityTypeAndForceId
         * @return the EntityType value of this object's type.
         */
        public EntityType getType() {
            return type;
        }
        
        /**
         * Gets the force ID of the EntityTypeAndForceId
         * @return the int value of this object's forceId.
         */
        public int getForceId() {
            return forceId;
        }
                
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("[EntityTypeAndForceId: ");
            sb.append("type = ");
            sb.append(getType());
            sb.append(", forceId = ");
            sb.append(getForceId());
            sb.append("]");
            return sb.toString();
        }
    }
}
