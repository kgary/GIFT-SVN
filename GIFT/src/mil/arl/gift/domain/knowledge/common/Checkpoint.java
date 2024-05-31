/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.domain.DomainDKFHandler;

/**
 * Contains information about a checkpoint to be assessed during the use of a dkf.
 * 
 * @author mhoffman
 *
 */
public class Checkpoint{
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(Checkpoint.class);

    /** the point associated with this checkpoint */
    private Point point;
    
    /** the elapsed simulation time at which this checkpoint should be reached */
    private double atTime;
    
    /** the amount of time around the atTime value for which the checkpoint can still be achieved successfully.*/
    private double window;
    
    /**
     * Class constructor - set attributes
     * 
     * @param point - the point (i.e. location) for this checkpoint
     * @param atTime - the time at which this checkpoint should be reached by
     * @param window - amount of time around the checkpoint time for which the checkpoint can still be achieved
     */
    public Checkpoint(Point point, double atTime, double window){        
      
        setWindow(window);
        setPoint(point);

        this.atTime = atTime;
    }
    
    /**
     * Class constructor - set attributes from dkf content
     * 
     * @param checkpoint - dkf content for a checkpoint
     * @param placesOfInterestManager - used to organize places of interest which can be referenced by 
     * name in various parts of the DKF
     */
    public Checkpoint(generated.dkf.Checkpoint checkpoint, PlacesOfInterestManager placesOfInterestManager){
        
        
        setPoint(placesOfInterestManager.getPlacesOfInterest(checkpoint.getPoint()));
        
        try{
            this.atTime = (DomainDKFHandler.atTime_df.convertStringToDate(checkpoint.getAtTime()).getTime()) / 1000.0;
        }catch(Exception e){
            logger.error("Caught exception while parsing at time string of "+checkpoint.getAtTime(), e);
            throw new IllegalArgumentException("There was a problem parsing the at time string");
        }
        
        setWindow(checkpoint.getWindowOfTime().doubleValue());
    }
    
    /**
     * Set the window (i.e. amount of time) for this checkpoint
     * 
     * @param window
     */
    private void setWindow(double window){
        
        if(window < 0){
            throw new IllegalArgumentException("checkpoint window value of "+window+" must be greater than zero");
        }  
        
        this.window = window;
    }
    
    /**
     * Set the place of interest (i.e. location) for this checkpoint
     * 
     * @param placeOfInterest
     */
    private void setPoint(PlaceOfInterestInterface placeOfInterest){
        
        if(placeOfInterest == null){
            throw new IllegalArgumentException("The placeOfInterest can't be null");
        }else if(!(placeOfInterest instanceof Point)){
            throw new IllegalArgumentException("The placeOfInterest must be a point");
        }
        
        this.point = (Point) placeOfInterest;
    }
    
    /**
     * Return the point associated with this checkpoint
     * 
     * @return the point
     */
    public Point getPoint(){
        return point;
    }
    
    /**
     * Return the elapsed simulation time at which this checkpoint should be reached
     * 
     * @return double
     */
    public double getAtTime(){
        return atTime;
    }
    
    /**
     * Return the amount of time around the atTime value for which the checkpoint can still be
     * achieved successfully.
     * 
     * @return double
     */
    public double getWindow(){
        return window;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[Checpoint: ");
        sb.append(" point = ").append(getPoint());
        sb.append(", at time = ").append(getAtTime());
        sb.append(", window = ").append(getWindow());
        sb.append("]");
        
        return sb.toString();
    }
}
