/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;

import mil.arl.gift.common.io.Constants;

/**
 * An object that wraps the various types of places of interest
 * 
 * @author nroberts
 */
public class PlaceOfInterestWrapper {

    /** The type of place of interest to wrap */
    private Serializable placeOfInterest;
    
    /**
     * Creates a new wrapper that does not yet wrap a type of place of interest
     */
    public PlaceOfInterestWrapper() {
        
    }
    
    /**
     * Creates a new wrapper that wraps the given type of place of interest
     * 
     * @param placeOfInterest the type of place of interest to wrap
     */
    public PlaceOfInterestWrapper(Serializable placeOfInterest) {
        setPlaceOfInterest(placeOfInterest);
    }

    /**
     * Gets the type of place of interest being wrapped
     * 
     * @return the wrapped type of place of interest
     */
    public Serializable getPlaceOfInterest() {
        return placeOfInterest;
    }

    /**
     * Gets the type of place of interest being wrapped
     * 
     * @param placeOfInterest the wrapped type of place of interest
     */
    public void setPlaceOfInterest(Serializable placeOfInterest) {
        this.placeOfInterest = placeOfInterest;
    }
    
    @Override
    public int hashCode(){
        
        String thisName = null;
        if(placeOfInterest instanceof generated.dkf.Point){
            thisName = ((generated.dkf.Point)placeOfInterest).getName();
        }else if(placeOfInterest instanceof generated.dkf.Path){
            thisName = ((generated.dkf.Path)placeOfInterest).getName();
        }else if(placeOfInterest instanceof generated.dkf.Area){
            thisName = ((generated.dkf.Area)placeOfInterest).getName();
        }
        
        if(thisName == null) {
            thisName = Constants.EMPTY;
        }
        
        return 7 * thisName.hashCode();
    }
    
    @Override
    public boolean equals(Object other){
        
        if(other instanceof PlaceOfInterestWrapper){
            // place of interest names are unique in a dkf, therefore just check the name
            
            PlaceOfInterestWrapper otherWrapper = (PlaceOfInterestWrapper)other;
            Serializable otherWrapperPoI = otherWrapper.getPlaceOfInterest();
            String otherName = null, thisName = null;
            if(otherWrapperPoI instanceof generated.dkf.Point && placeOfInterest instanceof generated.dkf.Point){
                otherName = ((generated.dkf.Point)otherWrapperPoI).getName();
                thisName = ((generated.dkf.Point)placeOfInterest).getName();
            }else if(otherWrapperPoI instanceof generated.dkf.Path && placeOfInterest instanceof generated.dkf.Path){
                otherName = ((generated.dkf.Path)otherWrapperPoI).getName();
                thisName = ((generated.dkf.Path)placeOfInterest).getName();
            }else if(otherWrapperPoI instanceof generated.dkf.Area && placeOfInterest instanceof generated.dkf.Area){
                otherName = ((generated.dkf.Area)otherWrapperPoI).getName();
                thisName = ((generated.dkf.Area)placeOfInterest).getName();
            }
            
            return otherName != null && thisName != null && otherName.equalsIgnoreCase(thisName);
        }
        
        return false;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[PlaceOfInterestWrapper: ");
        if(placeOfInterest instanceof generated.dkf.Point){
            
            generated.dkf.Point point = (generated.dkf.Point)placeOfInterest;
            sb.append("placeOfInterest (Point) = ").append(point.getName());
            
        }else if(placeOfInterest instanceof generated.dkf.Path){
            
            generated.dkf.Path path = (generated.dkf.Path)placeOfInterest;
            sb.append("placeOfInterest (Path) = ").append(path.getName());
            
        }else if(placeOfInterest instanceof generated.dkf.Area){
            
            generated.dkf.Area area = (generated.dkf.Area)placeOfInterest;
            sb.append("placeOfInterest (Area) = ").append(area.getName());
            
        }else{
            sb.append("placeOfInterest = UNKNOWN TYPE");
        }
        sb.append("]");
        return sb.toString();
    }
}
