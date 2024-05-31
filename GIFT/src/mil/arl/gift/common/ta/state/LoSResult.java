/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.state;

import java.util.List;
import java.util.Map;

import mil.arl.gift.common.util.StringUtils;

/**
 * This class is used to provide LoS query results
 * 
 * @author mhoffman
 *
 */
public class LoSResult implements TrainingAppState {
    
    /** container of the LoS query results mapped by entity identifier (e.g. entity marking) */
    private Map<String, List<VisibilityResult>> entityLoSResults;
    
    /** a unique id assigned to this request which will be provided in the LoS result
     * and can be used to filter other LoS requests. */
    private String requestId;

    /**
     * Class constructor - set attributes
     * 
     * @param entityLoSResults - container of the LoS query results mapped by entity identifier (e.g. entity marking)
     * Won't be null but can be empty.
     * @param requestId - a unique id assigned to this request which will be provided in the LoS result
     * and can be used to filter other LoS requests.  Can't be null or empty.
     */
    public LoSResult(Map<String, List<VisibilityResult>> entityLoSResults, String requestId){
        
        if(StringUtils.isBlank(requestId)){
            throw new IllegalArgumentException("The request id can't be null or empty");
        }else if(entityLoSResults == null){
            throw new IllegalArgumentException("The entity LoS Results can't be null.");
        }
        
        this.requestId = requestId;
        this.entityLoSResults = entityLoSResults;
    }
    
    /**
     * Return the collection of the LoS query results mapped by entity identifier.
     * 
     * @return container of the LoS query results mapped by entity identifier (e.g. entity marking).
     * Won't be null but can be empty.
     */
    public Map<String, List<VisibilityResult>> getEntitiesLoSResults(){
        return entityLoSResults;
    }
    
    /**
     * Return the unique id assigned to this request which will be provided in the LoS result
     * and can be used to filter other LoS requests.
     * @return won't be null or empty.
     */
    public String getRequestId(){
        return requestId;
    }
    
    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[LoSResult: ");     
        sb.append("requestId =").append(requestId);
        sb.append(", locations = {");
        for(String entityMarking : entityLoSResults.keySet()){
            sb.append(entityMarking).append(" : ").append(entityLoSResults.get(entityMarking)).append(",\n");
        }
        sb.append("}");
        
        sb.append("]");

        return sb.toString();
    }
    
    /**
     * Contains information about a line of sight ray result.
     * 
     * @author mhoffman
     *
     */
    public static class VisibilityResult{
        
        /** the index from the {@link LoSRequest} of the target point for a line of sight 
         * ray that has the given visibility percent */
        private int indexOfPointFromRequest;
        
        /** the [0,1.0] value of visibility the ray had */
        private Double visbilityPercent;

        
        /**
         * Set attributes
         * @param indexOfPointFromRequest the index from the {@link LoSRequest} of the target point for a line of sight 
         * ray that has the given visibility percent.  Must be a positive number.
         * @param visbilityPercent the [0,1.0] value of visibility the ray had.  Can't be null.
         */
        public VisibilityResult(int indexOfPointFromRequest, Double visbilityPercent) {
            
            if(indexOfPointFromRequest < 0){
                throw new IllegalArgumentException("The point index must be a positive number");
            }else if(visbilityPercent == null){
                throw new IllegalArgumentException("The visibility percent can't be null");
            }
            this.indexOfPointFromRequest = indexOfPointFromRequest;
            this.visbilityPercent = visbilityPercent;
        }

        /**
         * Return the index from the {@link LoSRequest} of the target point for a line of sight 
         * ray that has the given visibility percent. 
         * @return will be a positive number
         */
        public int getIndexOfPointFromRequest() {
            return indexOfPointFromRequest;
        }

        /**
         * Return the [0,1.0] value of visibility the ray had.
         * @return won't be null.
         */
        public Double getVisbilityPercent() {
            return visbilityPercent;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[VisibilityResult: indexOfPointFromRequest = ");
            builder.append(indexOfPointFromRequest);
            builder.append(", visbilityPercent = ");
            builder.append(visbilityPercent);
            builder.append("]");
            return builder.toString();
        }        
        
    }
}
