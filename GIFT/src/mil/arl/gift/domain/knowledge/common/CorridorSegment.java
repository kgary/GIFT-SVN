/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

/**
 * This class contains information about a corridor segment that has a width.
 *
 * @author mhoffman
 *
 */
public class CorridorSegment extends Segment{

    /** width of the segment (meters) */
    private double width;

    /** percent of width to give for buffer - used to create another zone around the segment */
    private double bufferWidthPercent = 0;

    private static final String FIRST_PT_NAME = "1";
    private static final String SECOND_PT_NAME = "2";

    /**
     * Class constructor - set attributes
     *
     * @param name the display name of the segment
     * @param startLocation the first point in the segment
     * @param endLocation the second point in the segment
     * @param width the width of the segment (meters)
     */
    public CorridorSegment(String name, Point startLocation, Point endLocation, double width){
        super(name, startLocation, endLocation);

        if(width <= 0){
            throw new IllegalArgumentException("The segment width value "+width+" is not valid and must be greater than zero");
        }

        this.width = width;
    }

    /**
     * Class constructor - set attributes
     *
     * @param name - the name of this segment
     * @param startLocation - the starting location of this segment
     * @param endLocation - the ending location of this segment
     * @param width - the width (meters) of this segment
     * @param bufferWidthPercent - amount of buffer around this corridor
     */
    public CorridorSegment(String name, Point startLocation, Point endLocation, double width, double bufferWidthPercent){
        this(name, startLocation, endLocation, width);

        setBufferWidthPercent(bufferWidthPercent);
    }

    /**
     * Class constructor - set attributes with dkf content
     *
     * @param segment - dkf content for a segment
     * @param waypointManager - used to organize waypoints which can be referenced by
     * name in various parts of the DKF
     */
    public CorridorSegment(Segment segment, PlacesOfInterestManager waypointManager) {
        this(segment.getName(),
                new Point(FIRST_PT_NAME, segment.getStartLocation()),
                new Point(SECOND_PT_NAME, segment.getEndLocation()),
                segment.getWidth());

        setBufferWidthPercent(segment.getBufferWidthPercent());
    }

    /**
     * Set the value of the buffer width percent
     *
     * @param value
     */
    private void setBufferWidthPercent(double value){

        if(value < 0 || value > 100){
            throw new IllegalArgumentException("The buffer width percent value of "+value+" must be a valid percent value between 0 and 100");
        }

        this.bufferWidthPercent = value;
    }

    /**
     * Return the width of the corridor around this segment
     *
     * @return double
     */
    @Override
    public double getWidth() {
        return width;
    }

    /**
     * Return the buffer width percent
     *
     * @return double
     */
    @Override
    public double getBufferWidthPercent(){
        return bufferWidthPercent;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[CorridorSegment: ");
        sb.append(super.toString());
        sb.append(", Width = ").append(getWidth());
        sb.append(", buffer percent = ").append(getBufferWidthPercent());
        sb.append("]");

        return sb.toString();
    }
}
