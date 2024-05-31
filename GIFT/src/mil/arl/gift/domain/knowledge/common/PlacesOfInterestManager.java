/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages the places of interest for a single dkf instance in memory.
 * 
 * @author mhoffman
 *
 */
public class PlacesOfInterestManager {
    
    /** mapping of current places of interest for a dkf */
    private Map<String, PlaceOfInterestInterface> placesOfInterestMap = new HashMap<String, PlaceOfInterestInterface>();
    
    /**
     * Default constructor
     */
    public PlacesOfInterestManager(){
        
    }
    
    /**
     * Add a place of interest.
     * 
     * @param placeOfInterest the place of interest to add 
     */
    public void addPlaceOfInterest(PlaceOfInterestInterface placeOfInterest){
        
        if(placesOfInterestMap.containsKey(placeOfInterest.getName())){
            throw new IllegalArgumentException("Another place of interest named "+placeOfInterest.getName()+" already exists.");
        }
        
        placesOfInterestMap.put(placeOfInterest.getName(), placeOfInterest);

    }
    
    /**
     * Return the place of interest with the given name.
     * 
     * @param name - unique name of a place of interest
     * @return the place of interest with the given name, null if no place of interest was found
     */
    public PlaceOfInterestInterface getPlacesOfInterest(String name){
        return placesOfInterestMap.get(name);
    }
}
