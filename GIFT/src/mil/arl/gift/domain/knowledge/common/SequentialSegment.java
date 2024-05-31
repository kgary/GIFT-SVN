/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

/**
 * This class contains information about a line segment whose 2 end points have ordering.
 * In addition the 2 end points have proximity parameters used to determine if the point locations
 * where reached within a specific threshold.
 * 
 * @author mhoffman
 *
 */
public class SequentialSegment extends Segment{
    
    /** allowable distances from each of the 2 points */
    private double startPointProximity;
    private double endPointProximity;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name a display name for the segment
     * @param startLocation the first point in the segment
     * @param endLocation the second point in the segment
     * @param startPointProximity allowable distance from the first point
     * @param endPointProximity allowable distance from the second point
     */
    public SequentialSegment(String name, Point startLocation, Point endLocation, double startPointProximity, double endPointProximity){
        super(name, startLocation, endLocation);
        
        setStartPointProximity(startPointProximity);
        setEndPointProximity(endPointProximity);
    }
    
    /**
     * Class constructor - set attributes for dkf content
     * 
     * @param name - name of the segment
     * @param inside - dkf content for inside point of segment
     * @param outside - dkf content for outside point of segment
     * @param placesOfInterestManager - used to organize places of interest which can be referenced by 
     * name in various parts of the DKF
     */
    public SequentialSegment(String name, generated.dkf.Inside inside, generated.dkf.Outside outside, PlacesOfInterestManager placesOfInterestManager){
        super(name, (Point)placesOfInterestManager.getPlacesOfInterest(inside.getPoint()), (Point)placesOfInterestManager.getPlacesOfInterest(outside.getPoint()));
        
        setStartPointProximity(inside.getProximity().doubleValue());
        setEndPointProximity(outside.getProximity().doubleValue());
    }
    
    /**
     * Set the start point proximity value
     * 
     * @param value must be greater than zero
     */
    private void setStartPointProximity(double value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The segment start point proximity value "+value+" is not valid and must be greater than zero");
        }
        
        this.startPointProximity = value;
    }
    
    /**
     * Set the end point proximity value
     * 
     * @param value must be greater than zero
     */
    private void setEndPointProximity(double value){
        
        if(value <= 0){
            throw new IllegalArgumentException("The segment end point proximity value "+value+" is not valid and must be greater than zero");
        }
        
        this.endPointProximity = value;
    }


    /**
     * Return the allowable proximity distance from the start point
     * 
     * @return will be greater than zero
     */
    public double getStartPointProximity() {
        return startPointProximity;
    }
    
    /**
     * Return the allowable proximity distance from the end point
     * 
     * @return will be greater than zero
     */
    public double getEndPointProximity() {
        return endPointProximity;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CorridorSegment: ");
        sb.append(super.toString());
        sb.append(", startPointProximity = ").append(getStartPointProximity());
        sb.append(", endPointProximity = ").append(getEndPointProximity());
        sb.append("]");
        
        return sb.toString();
    }
}
