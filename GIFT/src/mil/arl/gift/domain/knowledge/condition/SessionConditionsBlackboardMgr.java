/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.ta.state.VariablesState.VariableNumberState;
import mil.arl.gift.common.ta.state.VariablesState.WeaponState;
import mil.arl.gift.common.util.StringUtils;

/**
 * Manages black boards for multiple domain sessions.  A blackboard is used to store shared information
 * for conditions such as entity state and weapon state.
 * @author mhoffman
 *
 */
public class SessionConditionsBlackboardMgr {
    
    /**
     * contains the blackboard instances for each domain session
     * key : unique domain session id
     * value : blackboard instance for that domain session
     */
    private Map<Integer, SessionConditionsBlackboard> dsIdToBlackboard = new HashMap<>();
    
    /** singleton instance of this manager */
    private static SessionConditionsBlackboardMgr instance = null;
    
    /**
     * Return the singleton instance of this manager
     * @return the singleton instance
     */
    public static synchronized SessionConditionsBlackboardMgr getInstance(){
        
        if(instance == null){
            instance = new SessionConditionsBlackboardMgr();
        }
        
        return instance;
    }
    
    /**
     * Default
     */
    private SessionConditionsBlackboardMgr(){}
    
    /**
     * Return the blackboard instance for the specified domain session.  Creates it if it doesn't exist.
     * @param dsId the domain session to get the blackboard for. Can't be null.
     * @return the blackboard for the specified domain session.
     */
    public SessionConditionsBlackboard getSessionBlackboard(Integer dsId){
        
        if(dsId == null){
            throw new IllegalArgumentException("The domain session id can't be null");
        }
        
        SessionConditionsBlackboard bb = dsIdToBlackboard.get(dsId);
        if(bb == null){
            bb = new SessionConditionsBlackboard();
            dsIdToBlackboard.put(dsId, bb);
        }
        
        return bb;
    }
    
    /**
     * Contains shared information (e.g. entity state, weapon state) for condition classes for a single domain session 
     * @author mhoffman
     *
     */
    public static class SessionConditionsBlackboard{

        /** mapping of entity id (e.g. entity marking) to the common state for that actor */
        private Map<String, ConditionEntityState> entityIdToState = new HashMap<>();
        
        /** contains entities (e.g. entity marking) that have a pending weapon state request */
        private Set<String> pendingEntityWeaponStateRequests = new HashSet<>();
        
        /** 
         *  flag used to indicate if a training application is currently available for this session 
         *  This can change as each training app course object is loaded in a course.  It is important
         *  to know this to determine if the training app could be queried for additional information
         *  (e.g. line of sight, weapon safety status)
         */
        private boolean trainingAppAvailable = true;
        
        /**
         * Return the condition state information for the entity identified by the provided value
         * @param entityId an entity identifier unique to the session (e.g. entity marking).  Can't be null or empty.
         * @return the condition state for that entity.  Won't be null.
         */
        public ConditionEntityState getConditionEntityState(String entityId){
            
            if(StringUtils.isBlank(entityId)){
                throw new IllegalArgumentException("The entity id can't be null or blank");
            }
            
            ConditionEntityState state = entityIdToState.get(entityId);
            if(state == null){
                state = new ConditionEntityState();
                entityIdToState.put(entityId, state);
            }
            
            return state;
        }
        
        /**
         * Return whether the provided entity has a pending weapon state request awaiting a result.
         * @param entityMarking identifier of an entity in the session managed by this blackboard
         * that needs to be checked for an outstanding weapon state request.  
         * @return true if the entity was found to have a pending weapon state request.
         */
        public boolean isPendingWeaponStateResult(String entityMarking){
            synchronized(pendingEntityWeaponStateRequests){
                return pendingEntityWeaponStateRequests.contains(entityMarking); 
            }
        }
         
        /**
         * Add the entities provided as having an outstanding weapon state request.
         * 
         * @param entityMarkings identifiers of entities that have an outstanding weapon state request.
         * If null this method does nothing.
         * @return true if any of the entity identifiers provided were not already known to have
         * outstanding weapon state request(s).
         */
        public boolean addPendingWeaponStateResultEntities(Collection<String> entityMarkings){
            if(entityMarkings == null){
                return false;
            }
            
            synchronized(pendingEntityWeaponStateRequests){
                return pendingEntityWeaponStateRequests.addAll(entityMarkings);
            }
        }
        
        /**
         * Remove the entities provided from having a pending weapon state request.
         * 
         * @param entityMarkings identifiers of entities that no longer have an outstanding weapon
         * state request.  If null this method does nothing.
         */
        public void removePendingWeaponStateResultEntities(Collection<String> entityMarkings){
            if(entityMarkings == null){
                return;
            }
            
            synchronized(pendingEntityWeaponStateRequests){
                pendingEntityWeaponStateRequests.removeAll(entityMarkings);
            }
        }        

        /**
         * Return whether the training application is currently available.
         * This can change as each training app course object is loaded in a course.  It is important
         * to know this to determine if the training app could be queried for additional information
         * (e.g. line of sight, weapon safety status)
         * @return true if the training application is currently available.  Default is true.
         */
        public boolean isTrainingAppAvailable() {
            return trainingAppAvailable;
        }

        /**
         * Set whether the training application is currently available.
         * This can change as each training app course object is loaded in a course.  It is important
         * to know this to determine if the training app could be queried for additional information
         * (e.g. line of sight, weapon safety status
         * @param trainingAppAvailable true if the training application is currently available.
         */
        public void setTrainingAppAvailable(boolean trainingAppAvailable) {
            this.trainingAppAvailable = trainingAppAvailable;
        }
    }
    
    
    /**
     * Contains shared information about an entity for condition classes (e.g. entity state, weapon state).
     * @author mhoffman
     *
     */
    public static class ConditionEntityState{
        
        /** the latest entity state for an entity */
        private EntityState entityState;
        
        /** the last time the entity state was set */
        private long entityStateUpdatedEpoch = 0;
        
        /** the latest weapon state for the entity */
        private WeaponState weaponState;
        
        /** the last time the weapon state was set */
        private long weaponStateUpdatedEpoch = 0;
        
        /** mapping of unique variable name to the variable value */
        private Map<String, ConditionEntityStateNumberVariable> variablesMap = new HashMap<>();
        
        /**
         * Return the value for a specific variable.
         * @param varName the variable name to retrieve the value for.
         * @return the variable value.  Can be null if either the variable is not found or
         * the value is set to null for that variable.
         */
        public ConditionEntityStateNumberVariable getVariable(String varName){
            return variablesMap.get(varName);
        }
        
        /**
         * Add the variable to the collection.
         * @param varName the name of the variable.  Shouldn't be null or empty to be a meaningful name.
         * @param variable the value of the variable.  Can be null.
         */
        public void addVariable(String varName, ConditionEntityStateNumberVariable variable){
            variablesMap.put(varName, variable);
        }

        /**
         * Return the latest entity state
         * @return can be null if never set
         */
        public EntityState getEntityState() {
            return entityState;
        }

        /**
         * Set the current entity state for the entity
         * @param entityState can be null
         */
        public void setEntityState(EntityState entityState) {
            this.entityState = entityState;
            this.entityStateUpdatedEpoch = System.currentTimeMillis();
        }

        /**
         * Return the latest weapon state for an entity
         * @return can be null if never set
         */
        public WeaponState getWeaponState() {
            return weaponState;
        }

        /**
         * Set the current weapon state for the entity
         * @param weaponState can be null.
         */
        public void setWeaponState(WeaponState weaponState) {
            this.weaponState = weaponState;
            this.weaponStateUpdatedEpoch = System.currentTimeMillis();
        }

        /**
         * Return the time when the last entity state value was set
         * @return default if entity state was never set is 0
         */
        public long getEntityStateUpdatedEpoch() {
            return entityStateUpdatedEpoch;
        }

        /**
         * Return the time when the last weapon state value was set
         * @return default if weapon state was never set is 0.
         */
        public long getWeaponStateUpdatedEpoch() {
            return weaponStateUpdatedEpoch;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ConditionEntityState: entityState = ");
            builder.append(entityState);
            builder.append(", entityStateUpdatedEpoch = ");
            builder.append(entityStateUpdatedEpoch);
            builder.append(", weaponState = ");
            builder.append(weaponState);
            builder.append(", weaponStateUpdatedEpoch = ");
            builder.append(weaponStateUpdatedEpoch);
            builder.append("]");
            return builder.toString();
        }         
    }
    
    /**
     * Contains a number value for a variable.
     * 
     * @author mhoffman
     *
     */
    public static class ConditionEntityStateNumberVariable{
        
        /** the number value for the variable */
        private VariableNumberState numberState;
        
        /** when the variable value was last set */
        private long entityStateUpdatedEpoch;
        
        /**
         * Set the variable value.
         * @param numberState the variable value to set.  Can't be null.
         * @param entityStateUpdatedEpoch when the variable value was set.  If -1 the current epoch time will be used.
         */
        public ConditionEntityStateNumberVariable(VariableNumberState numberState, long entityStateUpdatedEpoch){
            
            if(numberState == null){
                throw new IllegalArgumentException("The number state can't be null");
            }
            
            if(entityStateUpdatedEpoch == -1){
                entityStateUpdatedEpoch = System.currentTimeMillis();
            }            

            this.numberState = numberState;
        }

        /**
         * Return the variable number value.
         * @return the variable number value.  Won't be null.
         */
        public VariableNumberState getNumberVariable() {
            return numberState;
        }

        /**
         * Return the time at which the variable value was set.
         * @return epoch time when the variable value was set.
         */
        public long getEntityStateUpdatedEpoch() {
            return entityStateUpdatedEpoch;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((numberState == null) ? 0 : numberState.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof ConditionEntityStateNumberVariable)) {
                return false;
            }
            ConditionEntityStateNumberVariable other = (ConditionEntityStateNumberVariable) obj;
            if (numberState == null) {
                if (other.numberState != null) {
                    return false;
                }
            } else if (!numberState.equals(other.numberState)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ConditionEntityStateNumberVariable: number = ");
            builder.append(numberState);
            builder.append(", entityStateUpdatedEpoch = ");
            builder.append(entityStateUpdatedEpoch);
            builder.append("]");
            return builder.toString();
        }
        
        
    }
}
