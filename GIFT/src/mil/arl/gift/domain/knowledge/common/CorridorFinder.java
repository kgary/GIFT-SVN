/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.List;

import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;
import javax.vecmath.Vector3d;

import mil.arl.gift.domain.knowledge.condition.CorridorBoundaryCondition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class helps find the current segment a provided location is in.
 *  
 * @author mhoffman
 *
 */
public class CorridorFinder {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CorridorBoundaryCondition.class);
    private static boolean isDebug = logger.isDebugEnabled();

    /** list of segments that make up this corridor */
    private List<CorridorSegment> segments;
    
    /** the current segment index in the list that the corridor check succeeded */
    private int currentSegmentIndex = 0;
    
    /** the closest segment index in the list - not necessarily the current segment index value */
    private int closestsSegmentIndex = 0;
    
    /** holder for the last segment returned during a buffer corridor location check */
    private Segment previousBufferSegment = null;
    
    /**
     * Class constructor - store corridor segments
     * 
     * @param segments list of segments that make up this corridor
     */
    public CorridorFinder(List<CorridorSegment> segments){
        
        if(segments == null){
            throw new IllegalArgumentException("The segments collection can't be null.");
        }
        this.segments = segments;
    }
    
    /**
     * Get the segment from the current corridor information based on the location provided.  This method adds a buffer
     * area round the closest segment and determines if the location is within that area as well. In addition, if the previous location
     * was not within the buffer or segment boundary and the current location is within the buffer but not the segment boundary,
     * then no segment will be returned until the location is within a segment's boundary.  This logic is meant to help prevent rapid
     * movement between being in and out of a segment by creating a buffer zone.
     * 
     * @param location - the location to check against
     * @return Segment - the current segment of the corridor the location is either in or is in the closest segment's buffer area
     */
    public Segment getSegmentWithBuffer(Point3d location){
        
        //get segment w/o buffer
        CorridorSegment segment = getSegment(location);
        
        if(segment == null){
            
            CorridorSegment closestSegment = segments.get(closestsSegmentIndex);
            double bufferPercent = closestSegment.getBufferWidthPercent();
            if(bufferPercent > 0){
                //if there is a buffer value than check it
                
                double closestDistance = getClosestSegmentDistance(location);
                double bufferSize = closestSegment.getWidth() * bufferPercent/100.0;
                if(closestDistance < (closestSegment.getWidth()/2 + bufferSize)){
                    //the closest segment distance is within the threshold/buffer area which is a percent of the width
                    
                    if(previousBufferSegment != null){
                        //the previous method call found a segment to returned, therefore the closest segment is the current segment
                        //the location is in using buffer area logic
                        segment = closestSegment;
                        
                        if(isDebug){
                            logger.debug("buffer check passed - found location "+location+" to be at a distance of "+closestDistance+" from segment = "+closestSegment.getName());
                        }

                    }else{
                        if(isDebug){
                            logger.debug("buffer check passed - found location "+location+" to be at a distance of "+closestDistance+" segment = "+closestSegment.getName()+", HOWEVER previous locations where outside of the segment boundary");
                        }
                    }
                }else{
                    if(isDebug){
                        logger.debug("buffer check failed - found location "+location+" to be at a distance of "+closestDistance+" from segment = "+closestSegment.getName());
                    }
                }
            }
        }else{
            if(isDebug){
                logger.debug("buffer check not needed - found location "+location+" to be at a distance of "+getDistanceFromSegment(segment, location, true)+" from segment = "+segment.getName());
            }
        }
        
        previousBufferSegment = segment;
        return segment;
    }

    /**
     * Get the segment from the current corridor information based on the new location.
     * 
     * @param location - the location to check against
     * @return CorridorSegment - the current segment of the corridor the location is in
     */
    public CorridorSegment getSegment(Point3d location){
        
        //TODO: implement point-plane distance algorithm
        //
        //for now just do distance from line check
        CorridorSegment segment;
        double distToSegment;
               
        segment = segments.get(currentSegmentIndex);        
        distToSegment = getDistanceFromSegment(segment, location, true);
        
        if(isDebug){
            if(segment != null){
                logger.debug("distance from segment start = "+location.distance(segment.getStartLocation())+" end = "+location.distance(segment.getEndLocation()));
            }
        }
        
        //check if outside current segment corridor
        if(!withinCorridor(distToSegment, segment)){
            
            if(isDebug){
                logger.debug("the location: "+location+" is "+distToSegment+" away from current segment: "+segment+", checking next segment");
            }
            
            //
            //check if transitioned to previous OR next segment neighbors
            //
            segment = getNextSegmentToIndex(currentSegmentIndex);
            distToSegment = getDistanceFromSegment(segment, location, true);
            
            if(withinCorridor(distToSegment, segment)){
                //currently in next segment
                               
                currentSegmentIndex++;
                closestsSegmentIndex = currentSegmentIndex;
                
                if(isDebug){
                    logger.debug("the location: "+location+" is "+distToSegment+" away from next segment: "+segment+", updating current segment to index "+currentSegmentIndex);
                }
                
            }else{
                //not in next segment, check previous
                
                if(isDebug){
                    logger.debug("the location: "+location+" is "+distToSegment+" away from current segment corridor: "+segment+", checking previous segment");
                }
                
                segment = getPreviousSegmentToIndex(currentSegmentIndex);
                distToSegment = getDistanceFromSegment(segment, location, true);
                
                if(withinCorridor(distToSegment, segment)){
                    //currently in previous segment
                    
                    currentSegmentIndex--;
                    closestsSegmentIndex = currentSegmentIndex;
                    
                    if(isDebug){
                        logger.debug("the location: "+location+" is "+distToSegment+" away from previous segment: "+segment+", updating current segment to index "+currentSegmentIndex);
                    }
                    
                }else{
                    
                    //not in current, previous or next segment, 
                    //therefore need to search for closest segment                    
                    ClosestSegmentHelper csHelper = getIndexOfClosestSegment(location);
                    closestsSegmentIndex = csHelper.index;
                    
                    segment = segments.get(closestsSegmentIndex);

                    distToSegment = getClosestSegmentDistance(location);
                    
                    
                    if(isDebug){
                        logger.debug("found the closest segment to be "+segment+" from the location: "+location+" at "+distToSegment+" away");
                    }
                    
                    // finally, make sure currently not in the closest segment
                    if(withinCorridor(distToSegment, segment)){
                        //currently in closest segment
                        
                        if(isDebug){
                            logger.debug("the location: "+location+" satisfies the corridor check for the newly found closest segment");
                        }
                        
                        currentSegmentIndex = closestsSegmentIndex;
                    }else{
                        //exhausted search, determined not in corridor
                        
                        if(isDebug){
                            logger.debug("the location: "+location+" is "+distToSegment+" away from the closest segment: "+segment+", which fails the corridor check");
                        }

                        return null;
                    }
                }
            }
            
        }else{
            //within current segment corridor
            
            if(isDebug){
                logger.debug("the location: "+location+" is "+distToSegment+" away from current segment: "+segment+", which satisfies the corridor check");
            }

        }
        
        return segments.get(currentSegmentIndex); 
    }
    
    /**
     * Return whether the distance provided is within the segments corridor
     * 
     * @param distance
     * @param segment - segment to check its corridor
     * @return boolean - whether or not the distance value is within the segments corridor
     */
    private boolean withinCorridor(double distance, CorridorSegment segment){
        return segment != null ? distance <= segment.getWidth()/2 : false;
    }
    
    /**
     * Return the previous segment in the list using the index provided as the current segment index.
     * 
     * @param index
     * @return CorridorSegment - the previous segment in the list, null if there is no previous segment
     */
    private CorridorSegment getPreviousSegmentToIndex(int index){
        return index > 0 ? segments.get(index - 1) : null;
    }
    
    /**
     * Return the next segment in the list using the index provided as the current segment index.
     * 
     * @param index
     * @return CorridorSegment - the next segment in the list, null if there is no next segment
     */
    private CorridorSegment getNextSegmentToIndex(int index){
        return index < segments.size()-1 ? segments.get(index + 1) : null;
    }
    
    /**
     * Search all of the segments in this corridor for the closest one to the given location.
     * 
     * @param location - location to check against for the closest segment
     * @return int - the index in the list of segments for the closest segment
     */
    private ClosestSegmentHelper getIndexOfClosestSegment(Point3d location){
        
        double distance, minDistance = Double.MAX_VALUE;
        int minSegmentIndex = -1;
        for(int i = 0; i < segments.size(); i++){
            distance = getDistanceFromSegment(segments.get(i), location, false);
            
            if(distance < minDistance){
                minDistance = distance;
                minSegmentIndex = i;
            }
        }
        
        return new ClosestSegmentHelper(minSegmentIndex, minDistance);
    }
    
    /**
     * Returns the distance from the location provided to the current closest segment.  It handles imperfections
     * caused by corridor checks on imperfectly aligned adjoining segments by doing a radius check at those joints.
     * 
     * @param location - location to check against
     * @return double - the distance to the closest segment
     */
    private double getClosestSegmentDistance(Point3d location){
        
        double distToSegment;
        Segment segment = segments.get(closestsSegmentIndex);
        
        //
        // Handle slice case caused by imperfectly aligned adjoining segments - do a radius check 
        //
        if(closestsSegmentIndex == 0){
            //the segment is the first segment, don't radius check the start location
            distToSegment = getDistanceFromSegment(segment, location, true, false);
            
        }else if(closestsSegmentIndex == segments.size()-1){
            //the segment is the last segment, don't radius check the end location
            distToSegment = getDistanceFromSegment(segment, location, false, true);
            
        }else{
            //the segment is a middle segment
            distToSegment = getDistanceFromSegment(segment, location, false);
        }
        
        return distToSegment;
    }

    /**
     * Return the distance from the location to the segment
     * 
     * @param segment - the segment to check against
     * @param location - the location to use a reference
     * @param checkBoundary - whether to use a segments end points as the segments boundary, meaning if the location is
     *               not between the start and end points the distance returned will be infinity.
     * @return double - distance from the location to the segment.
     *          Note: this value can be infinity
     */
    private double getDistanceFromSegment(Segment segment, Point3d location, boolean checkBoundary){
        return getDistanceFromSegment(segment, location, checkBoundary, checkBoundary);
    }
    
    /**
     * Return the distance from the location to the segment
     * 
     * @param segment - the segment to check against
     * @param location - the location to use a reference
     * @param checkStartBoundary - whether to use a segments start point as a segment boundary, meaning if the location is
     *               not between the start and end points the distance returned will be infinity.
     * @param checkEndBoundary - whether to use a segments end point as a segment boundary, meaning if the location is
     *               not between the end and (somewhere near the) start points the distance returned will be infinity.
     * @return double - distance from the location to the segment.
     *          Note: this value can be infinity
     */
    private double getDistanceFromSegment(Segment segment, Point3d location, boolean checkStartBoundary, boolean checkEndBoundary){
        
        if(segment == null){
            return Double.POSITIVE_INFINITY;
        }
        
        Point3d projection = getClosestPointOnSegment(segment, location);
        
        if(checkStartBoundary && projection.equals(segment.getStartLocation())){
            return Double.POSITIVE_INFINITY;
        }else if(checkEndBoundary && projection.equals(segment.getEndLocation())){
            return Double.POSITIVE_INFINITY;
        }
        
        //check distance
        if(segment.getStartLocation().getZ() == 0 && segment.getEndLocation().getZ() == 0){
            //perform 2D calculation - this is here to handle the situation where the location authored was
            //                         created using gift wrap for Unity training application.  In this case
            //                         the third value (elevation) in the Unity AGL coordinate will always be zero.
            //                         However when running the Unity scenario, the entity state messages can
            //                         contain non-zero elevation values.
            return Math.sqrt(Math.pow(projection.getX() - location.getX(), 2) + Math.pow(projection.getY() - location.getY(), 2));
        }else{
            //perform 3D calculation
            return location.distance(projection);
        }
    }
    
    /**
     * Return the closest point on the segment to the location.
     * 
     * @param segment - the segment to return a point on
     * @param location - the location to use as reference for the closest point calculations
     * @return Point3d - the closest point on the segment to the location.
     *         Note: this point can be the start or end point of the segment.
     */
    private Point3d getClosestPointOnSegment(Segment segment, Point3d location){
        
        //
        // Reference: http://stackoverflow.com/questions/849211/shortest-distance-between-a-point-and-a-line-segment
        //
        
        Point3d ssPt = segment.getStartLocation();
        Point3d sePt = segment.getEndLocation();
        
        if(ssPt.getZ() == 0 && sePt.getZ() == 0){
            //perform 2D calculation - this is here to handle the situation where the location authored was
            //                         created using gift wrap for Unity training application.  In this case
            //                         the third value (elevation) in the Unity AGL coordinate will always be zero.
            //                         However when running the Unity scenario, the entity state messages can
            //                         contain non-zero elevation values.
            
            Vector2d StoEVec = new Vector2d(sePt.x - ssPt.x, sePt.y - ssPt.y);
            double l2 = StoEVec.lengthSquared();
            
            if(l2 == 0.0){
                //segment's start and end points are the same
                return segment.getStartLocation();
            }
            
            //Consider the line extending the segment
            //Find the projection of a point onto that line
            Vector2d StoLVec = new Vector2d(location.x - ssPt.x, location.y - ssPt.y);
            
            double t = StoLVec.dot(StoEVec) / l2;
            
            if(t < 0.0){
                //point is beyond the segments start
                return segment.getStartLocation();
            }else if(t > 1.0){
                //point is beyond the segments end
                return segment.getEndLocation();
            }else{
                //projection falls on the segment
                Vector3d StoEVecT = new Vector3d(StoEVec.x * t, StoEVec.y * t, 0.0);
                Point3d projection = new Point3d();
                projection.add(ssPt, StoEVecT);
                return projection;
            }
        }else{
            //perform 3D calculation
            
            Vector3d StoEVec = new Vector3d();
            StoEVec.sub(sePt, ssPt);
            double l2 = StoEVec.lengthSquared();
            
            if(l2 == 0.0){
                //segment's start and end points are the same
                return segment.getStartLocation();
            }
            
            //Consider the line extending the segment
            //Find the projection of a point onto that line
            Vector3d StoLVec = new Vector3d();
            StoLVec.sub(location, ssPt);
            
            double t = StoLVec.dot(StoEVec) / l2;
            
            if(t < 0.0){
                //point is beyond the segments start
                return segment.getStartLocation();
            }else if(t > 1.0){
                //point is beyond the segments end
                return segment.getEndLocation();
            }else{
                //projection falls on the segment
                Vector3d StoEVecT = new Vector3d(StoEVec.x * t, StoEVec.y * t, StoEVec.z * t);
                Point3d projection = new Point3d();
                projection.add(ssPt, StoEVecT);
                return projection;
            }
        }

    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CorridorFinder: ");

        sb.append("Segments = ");
        for(Segment segment : segments){
            sb.append(segment).append(", ");
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    
    
    /**
     * This class contains information about the closest segment.
     * 
     * @author mhoffman
     *
     */
    private class ClosestSegmentHelper{
        
        public final int index;
        @SuppressWarnings("unused")
		public final double distance;
        
        public ClosestSegmentHelper(int index, double distance){
            this.index = index;
            this.distance = distance;
        }
    }
}
