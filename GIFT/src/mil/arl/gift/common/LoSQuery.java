/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.List;
import java.util.Set;

import javax.vecmath.Point3d;

import mil.arl.gift.common.ta.request.TrainingAppInfoRequest;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class is used to request one or more line of sight (LoS) queries
 * 
 * @author mhoffman
 *
 */
public class LoSQuery implements TrainingAppInfoRequest{
    
    /** the LoS locations to query for */
    private List<Point3d> locations;
    
    /** a unique id assigned to this request which will be provided in the LoS result
     * and can be used to filter other LoS requests. */
    private String requestId;
    
    /** collection of entities that will be the source of the line of sight ray */
    private Set<String> entities;
    
    /**
     * Set attributes
     * @param locations - list of locations to conduct a LoS query too.  Can't be null or empty.
     * @param requestId - a unique id assigned to this request which will be provided in the LoS result
     * and can be used to filter other LoS requests.  Can't be null or empty.
     */
    private LoSQuery(List<Point3d> locations, String requestId){
        
        if(locations == null || locations.isEmpty()){
            throw new IllegalArgumentException("The list of locations can't be null or empty.");
        }else if(StringUtils.isBlank(requestId)){
            throw new IllegalArgumentException("The request id can't be null or empty");
        }
        
        this.locations = locations;
        this.requestId = requestId;
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param locations - list of locations to conduct a LoS query too.  Can't be null or empty.
     * @param requestId - a unique id assigned to this request which will be provided in the LoS result
     * and can be used to filter other LoS requests.  Can't be null or empty.
     * @param entities the IDs that uniquely identifies the entities from which to query LoS. Can't be null or empty.
     */
    public LoSQuery(List<Point3d> locations, String requestId, Set<String> entities){ 
        this(locations, requestId);
        
        if(CollectionUtils.isEmpty(entities)){
            throw new IllegalArgumentException("The entities can't be null or empty");
        }

        this.entities = entities;
    }
    
    /**
     * Return the collection of locations to conduct a LoS query on
     * 
     * @return won't be null or empty.
     */
    public List<Point3d> getLocations(){
        return locations;
    }
    
    /**
     * Return the unique id assigned to this request which will be provided in the LoS result
     * and can be used to filter other LoS requests.
     * @return won't be null or empty.
     */
    public String getRequestId(){
        return requestId;
    }
    
    /**
     * Return the collection of entities that need their weapon status retrieved
     * @return will not be null or empty.
     */
    public Set<String> getEntities() {
        return entities;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[LoSQuery: ");
        sb.append("requestId =").append(requestId);        

        sb.append(", entities = ");
        sb.append(entities);
        
        sb.append(", locations = {");
        for(Point3d data : locations){
            sb.append(data).append(", ");
        }
        sb.append("}");
        
        sb.append("]");

        return sb.toString();
    }
}
