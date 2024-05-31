/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a Pedagogical Request from the Pedagogical Module.
 * 
 * @author mhoffman
 *
 */
public class PedagogicalRequest implements Serializable{
    
    /**
     *  default
     */
    private static final long serialVersionUID = 1L;
    
    /** 
     * a map of unique reasons why to a collection of pedagogical request(s) for that reason
     */
    private Map<String, List<AbstractPedagogicalRequest>> requestsMap;
    
    /**
     * Default constructor - creates empty map.
     */
    public PedagogicalRequest(){
        requestsMap = new HashMap<>();
    }
    
	/**
	 * Class constructor
	 * 
     * @param requests - a map of unique reasons why to a collection of pedagogical request(s) for that reason.  Can't be null.
	 */
	public PedagogicalRequest(Map<String, List<AbstractPedagogicalRequest>> requests){	
	    
	    if(requests == null){
	        throw new IllegalArgumentException("The requests can't be null.");
	    }
	    
		this.requestsMap = requests;
	}

	/**
	 * Return the map of unique reasons why to a collection of pedagogical request(s) for that reason
	 * 
	 * @return can be empty but not null.
	 */
	public Map<String, List<AbstractPedagogicalRequest>> getRequests(){
		return requestsMap;
	}
	
	/**
	 * Add a collection of requests for the reason provided.
	 * 
	 * @param reasonToAdd a reason for the requests that are being added.  If this reason is already
	 * represented in the map, the requests to add will be added to the list (i.e. the provided list
	 * doesn't replace any existing list for that reason)
	 * @param requestsToAdd the requests to add for that specific reason provided.
	 */
	public void addRequests(String reasonToAdd, List<AbstractPedagogicalRequest> requestsToAdd){
	                
        List<AbstractPedagogicalRequest> requests = requestsMap.get(reasonToAdd);
        if(requests == null){
            requests = new ArrayList<>();
            requestsMap.put(reasonToAdd, requests);
        }
        
        requests.addAll(requestsToAdd);

	}
	
	   /**
     * Add a single request for the reason provided.
     * 
     * @param reasonToAdd a reason for the request being added.  If this reason is already
     * represented in the map, the request to add will be added to the list (i.e. the provided request
     * doesn't replace any existing list for that reason)
     * @param requestToAdd the request to add for that specific reason provided.
     */
	public void addRequest(String reasonToAdd, AbstractPedagogicalRequest requestToAdd){
	    
        List<AbstractPedagogicalRequest> requests = requestsMap.get(reasonToAdd);
        if(requests == null){
            requests = new ArrayList<>();
            requestsMap.put(reasonToAdd, requests);
        }
        
        requests.add(requestToAdd);
	}
		
	@Override
	public String toString(){
		
	    StringBuffer sb = new StringBuffer();
	    sb.append("[PedagogicalRequest: ");
		
		sb.append(", requests = {");
		for(String reason : getRequests().keySet()){
		    sb.append("\n").append(reason).append(" - ");
    		for(AbstractPedagogicalRequest request : getRequests().get(reason)){
    		    sb.append(request.toString()).append(",");
    		}
		}
		sb.append("}");
		
		sb.append("]");
		
		return sb.toString();
	}
}
