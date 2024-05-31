/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.util.HashMap;
import java.util.Map;

import javax.vecmath.Vector3d;

import mil.arl.gift.common.ta.request.VariablesStateRequest.VARIABLE_TYPE;
import mil.arl.gift.common.util.StringUtils;

/**
 * Contains the values of one or more variables mapped by entity.
 * 
 * @author mhoffman
 *
 */
public class VariablesState implements TrainingAppState {

    /**
     * contains variable values mapped by entity
     * key: enumerated variable type
     * value: mapping of unique entity identifier the variable value for that entity
     */
    private Map<VARIABLE_TYPE, Map<String, VariableState>> typeToEntityValue = new HashMap<>();
    
    /**
     * Return the values for a specific variable type for all the entities that were requested.
     * @param variableType the enumerated type of variable to get the values of.
     * @return the mapping of unique entity identifier to the values of that variable for each entity.  Won't be null
     * but can be empty.
     */
    public Map<String, VariableState> getVariableMapForType(VARIABLE_TYPE variableType){
        
        if(variableType == null){
            throw new IllegalArgumentException("The variable type is null");
        }
        
        Map<String, VariableState> entityValueMap = typeToEntityValue.get(variableType);
        if(entityValueMap == null){
            entityValueMap = new HashMap<>();
            typeToEntityValue.put(variableType, entityValueMap);
        }
        
        return entityValueMap;
    }
    
    /**
     * Return the map that contains variable values mapped by entity
     * @return won't be null but can be empty.
     */
    public Map<VARIABLE_TYPE, Map<String, VariableState>> getVariableTypeMap(){
        return typeToEntityValue;
    }        
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[VariablesState: typeToEntityValue = \n");
        builder.append(typeToEntityValue);
        builder.append("]");
        return builder.toString();
    }

    /**
     * Interface used by all variable containers.
     * 
     * @author mhoffman
     *
     */
    public static interface VariableState{}
    
    /**
     * Variable container for a number.
     * @author mhoffman
     *
     */
    public static class VariableNumberState implements VariableState{
        
        /** the variable name */
        private String varName;
        
        /** the variable value */
        private Number varValue;
        
        /**
         * Set attributes 
         * @param varName the variable name, can't be null or empty
         * @param varValue the variable value, Can be null
         */
        public VariableNumberState(String varName, Number varValue){
            
            if(StringUtils.isBlank(varName)){
                throw new IllegalArgumentException("The variable name is null or blank");
            }
            
            this.varName = varName;
            this.varValue = varValue;
        }
        
        /**
         * Return the variable name
         * @return won't be null or empty
         */
        public String getVarName() {
            return varName;
        }

        /**
         * Return the variable value
         * @return can be null if the value couldn't be retrieved
         */
        public Number getVarValue() {
            return varValue;
        }       

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((varName == null) ? 0 : varName.hashCode());
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
            if (!(obj instanceof VariableNumberState)) {
                return false;
            }
            VariableNumberState other = (VariableNumberState) obj;
            if (varName == null) {
                if (other.varName != null) {
                    return false;
                }
            } else if (!varName.equals(other.varName)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[VariableNumberState: varName = ");
            builder.append(varName);
            builder.append(", varValue = ");
            builder.append(varValue);
            builder.append("]");
            return builder.toString();
        }        
        
    }
    
    /**
     * Variable container for the weapon state for a single entity.
     * 
     * @author mhoffman
     *
     */
    public static class WeaponState implements VariableState{
        
        /**
         * whether the weapon safety is enabled or not on the current weapon for this entity
         */
        private Boolean weaponSafetyStatus;
        
        /**
         * whether the entity has a weapon, where having a weapon either means its equipped (in hand) or available
         */
        private Boolean hasWeapon;
        
        /**
         * where the weapon is aiming
         * For VBS this means the vector from the center of the weapon which can be used to determine the muzzle's
         * angle from the horizon 
         */
        private Vector3d weaponAim;
        
        /**
         * Used to identify the entity whose weapon state is being reported
         */
        private String entityMarking;
        
        /**
         * Set attribute
         * @param entityMarking Used to identify the entity whose weapon state is being reported, can't be null.
         */
        public WeaponState(String entityMarking){
            
            if(entityMarking == null){
                throw new IllegalArgumentException("The entity marking can't be null");
            }
            
            this.entityMarking = entityMarking;
        }
        
        @Override
        public int hashCode(){
            return entityMarking.hashCode();
        }
        
        @Override
        public boolean equals(Object otherObject){
            
            if(otherObject instanceof WeaponState){
                WeaponState otherWeaponState = (WeaponState)otherObject;

                if(this.weaponSafetyStatus == null && otherWeaponState.weaponSafetyStatus != null){
                    return false;
                }else if(this.weaponSafetyStatus != null && otherWeaponState.weaponSafetyStatus == null){
                    return false;
                }else if(this.weaponSafetyStatus != null && otherWeaponState.weaponSafetyStatus != null && 
                        this.weaponSafetyStatus.booleanValue() != otherWeaponState.weaponSafetyStatus.booleanValue()){
                    return false;
                }
                
                if(this.hasWeapon == null && otherWeaponState.hasWeapon != null){
                    return false;
                }else if(this.hasWeapon != null && otherWeaponState.hasWeapon == null){
                    return false;
                }else if(this.hasWeapon != null && otherWeaponState.hasWeapon != null && 
                        this.hasWeapon.booleanValue() != otherWeaponState.hasWeapon.booleanValue()){
                    return false;
                }
                
                if(this.weaponAim == null && otherWeaponState.weaponAim != null){
                    return false;
                }else if(this.weaponAim != null && otherWeaponState.weaponAim == null){
                    return false;
                }else if(this.weaponAim != null && otherWeaponState.weaponAim != null && 
                        !this.weaponAim.equals(otherWeaponState.weaponAim)){
                    return false;
                }
                
                if(!this.entityMarking.equals(otherWeaponState.entityMarking)){
                    return false;
                }
                
                return true;
            }
            
            return false;
        }

        /**
         * Return whether the weapon safety is enabled or not on the current weapon for this entity
         * @return can be null, true if weapon safety is on.
         */
        public Boolean getWeaponSafetyStatus() {
            return weaponSafetyStatus;
        }

        /**
         * Set whether the weapon safety is enabled or not on the current weapon for this entity
         * @param weaponSafetyStatus can be null, true if weapon safety is on.
         */
        public void setWeaponSafetyStatus(Boolean weaponSafetyStatus) {
            this.weaponSafetyStatus = weaponSafetyStatus;
        }

        /**
         * Return whether the entity has a weapon, where having a weapon either means its equipped (in hand) or available
         * @return can be null, true if weapon is equipped
         */
        public Boolean getHasWeapon() {
            return hasWeapon;
        }

        /**
         * Set whether the entity has a weapon, where having a weapon either means its equipped (in hand) or available
         * @param hasWeapon can be null, true if weapon is equipped
         */
        public void setHasWeapon(Boolean hasWeapon) {
            this.hasWeapon = hasWeapon;
        }

        /**
         * Return where the weapon is aiming
         * For VBS this means the vector from the center of the weapon which can be used to determine the muzzle's
         * angle from the horizon 
         * @return can be null.
         */
        public Vector3d getWeaponAim() {
            return weaponAim;
        }

        /**
         * Set where the weapon is aiming
         * For VBS this means the vector from the center of the weapon which can be used to determine the muzzle's
         * angle from the horizon 
         * @param weaponAim can be null
         */
        public void setWeaponAim(Vector3d weaponAim) {
            this.weaponAim = weaponAim;
        }

        /**
         * Return the entity marking for the entity whose weapon state is being set here
         * @return Used to identify the entity whose weapon state is being reported.  Won't be null or empty.
         */
        public String getEntityMarking() {
            return entityMarking;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[WeaponState: weaponSafetyStatus = ");
            builder.append(weaponSafetyStatus);
            builder.append(", hasWeapon = ");
            builder.append(hasWeapon);
            builder.append(", weaponAim = ");
            builder.append(weaponAim);
            builder.append(", entityMarking = ");
            builder.append(entityMarking);
            builder.append("]");
            return builder.toString();
        }        
        
    }

}
