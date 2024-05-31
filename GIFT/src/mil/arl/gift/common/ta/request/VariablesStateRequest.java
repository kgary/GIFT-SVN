/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.request;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * Used to request one or more variables for one or more actors in a training application.
 * @author mhoffman
 *
 */
public class VariablesStateRequest implements TrainingAppInfoRequest {

    /**
     * The supported variable types to request.
     * @author mhoffman
     *
     */
    public enum VARIABLE_TYPE{
        WEAPON_STATE,    // retrieve one or more weapon parameters (e.g. weapon safety)
        VARIABLE,        // a generic way to ask for a variable's value
        ANIMATION_PHASE  // (For VBS) - e.g. retrieve the target 'down' animation value [0,1.0]
    }
    
    /** a unique id assigned to this request which will be provided in the corresponding response/result
     * and can be used to filter other requests. */
    private String requestId;
    
    /**
     * contains the type of variables that need to be queried for, the variable names if needed and
     * who the variable is assigned too.
     * Key: type of variable (e.g. ANIMATION_PHASE)
     * Value: container for the entities the variable applies too and an optional variable name (e.g. "Down" for ANIMATION_PHASE)
     */
    private Map<VARIABLE_TYPE, VariableInfo> typeToVarInfoMap = new HashMap<>();
    
    /**
     * Set the request id
     * @param requestId a unique id assigned to this request which will be provided in the corresponding response/result
     * and can be used to filter other requests.  Can't be null or empty.
     */
    public VariablesStateRequest(String requestId){
        
        if(StringUtils.isBlank(requestId)){
            throw new IllegalArgumentException("The request id is null or empty");
        }
        
        this.requestId = requestId;
    }
    
    /**
     * Return the unique id assigned to this request which will be provided in the corresponding response/result
     * and can be used to filter other requests.
     * @return the unique request id.  Won't be null or empty.
     */
    public String getRequestId() {
        return requestId;
    }
    
    /**
     * Add a request for variable value(s).
     * @param varType the enumerated type of variable being requested. Can't be null.
     * @param varInfo information about the name of the variable and who the variable is associated with. Can't be null.
     */
    public void setTypeVariable(VARIABLE_TYPE varType, VariableInfo varInfo){
        
        if(varType == null){
            throw new IllegalArgumentException("The variable type can't be null");
        }else if(varInfo == null){
            throw new IllegalArgumentException("The variable info can't be null");
        }
        typeToVarInfoMap.put(varType, varInfo);
    }
    
    /**
     * Return the mapping that contains the type of variables that need to be queried for, the variable names if needed and
     * who the variable is assigned too.
     * Key: type of variable (e.g. ANIMATION_PHASE)
     * Value: container for the entities the variable applies too and an optional variable name (e.g. "Down" for ANIMATION_PHASE)
     * @return won't be null but can be empty.
     */
    public Map<VARIABLE_TYPE, VariableInfo> getTypeToVarInfoMap(){
        return typeToVarInfoMap;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[VariableStatesRequest: requestId = ");
        builder.append(requestId);
        builder.append(", typeToVarInfoMap = ");
        builder.append(typeToVarInfoMap);
        builder.append("]");
        return builder.toString();
    }  
    
    /**
     * Contains information about a variable to request the value for and who the variable
     * is associated with.
     * @author mhoffman
     *
     */
    public static class VariableInfo{
        
        /** contains one or more entity identifiers (e.g. entity markings) that the variable
         * is associated with. */
        private Set<String> entityIds;
        
        /** the variable name */
        private String varName;
        
        /**
         * Set who the variable is associated with
         * @param entityIds contains one or more entity identifiers (e.g. entity markings) that the variable
         * is associated with.  Can't be null or empty.
         */
        public VariableInfo(Set<String> entityIds){
            
            if(CollectionUtils.isEmpty(entityIds)){
                throw new IllegalArgumentException("The entity ids can't be empty");
            }
            this.entityIds = entityIds;
        }

        /**
         * Return the variable name
         * @return can be null if the variable name is not needed.  E.g. weapon state variable type requires no variable name.
         */
        public String getVarName() {
            return varName;
        }

        /**
         * Set the variable name (e.g. 'down' to get the VBS target down animation phase)
         * @param varName can be null if the variable name is not needed.  E.g. weapon state variable type requires no variable name.
         */
        public void setVarName(String varName) {
            this.varName = varName;
        }

        /**
         * Return the entity identifiers (e.g. entity markings) that the variable
         * is associated with.
         * @return one or more entity ids.  Won't be null or empty.
         */
        public Set<String> getEntityIds() {
            return entityIds;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[VariableInfo: entityIds = ");
            builder.append(entityIds);
            builder.append(", varName = ");
            builder.append(varName);
            builder.append("]");
            return builder.toString();
        }
        
        
    }
    
}
