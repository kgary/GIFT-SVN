/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import mil.arl.gift.domain.DomainDKFHandler;

/**
 * This class contains information about a line segment with 2 points.
 *
 * @author mhoffman
 *
 */
public class Segment {

    /** segment name */
    private String name;

    /** the world coordinates of the start of this segment */
    private Point startLocation;

    /** the world coordinates of the end of this segment */
    private Point endLocation;

    /** the width of this segment */
    private double width;

    /**
     * The width of the buffer around the segment as a percentage of the its
     * {@link #width}
     */
    private double bufferWidthPercent;

    /**
     * Creates a {@link Segment} from a {@link generated.dkf.Segment}.
     *
     * @param segment The {@link generated.dkf.Segment} to convert. Can't be
     *        null.
     */
    public Segment(generated.dkf.Segment segment) {
        if (segment == null) {
            throw new IllegalArgumentException("The parameter 'segment' cannot be null.");
        }


        width = segment.getWidth().doubleValue();
        bufferWidthPercent = segment.getBufferWidthPercent() != null ? segment.getBufferWidthPercent().doubleValue() : 0;
        startLocation = new Point(segment.getName()
                + " Start", DomainDKFHandler.buildCoordinate(segment.getStart().getCoordinate()));
        endLocation = new Point(segment.getName()
                + " End", DomainDKFHandler.buildCoordinate(segment.getEnd().getCoordinate()));
    }

    /**
     * Class constructor - set attributes
     *
     * @param name a display name for the segment
     * @param startLocation the first point in the segment
     * @param endLocation the second point in the segment
     */
    public Segment(String name, Point startLocation, Point endLocation){

        //
        // Validate inputs
        //

        if(startLocation == null){
            throw new IllegalArgumentException("The start trigger for segment named "+name+" is null");
        }

        if(endLocation == null){
            throw new IllegalArgumentException("The end trigger for segment named "+name+" is null");
        }

        this.name = name;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }


    /**
     * Return the segment name
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Return the start location of the segment
     *
     * @return Waypoint
     */
    public Point getStartLocation() {
        return startLocation;
    }

    /**
     * Return the end location of the segment
     *
     * @return Waypoint
     */
    public Point getEndLocation() {
        return endLocation;
    }

    /**
     * Return the width of this segment
     *
     * @return The value of {@link #width}.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Return the width of the buffer around the segment as a percentage of the
     * its {@link #width}
     * 
     * @return The value of {@link #bufferWidthPercent}.
     */
    public double getBufferWidthPercent() {
        return bufferWidthPercent;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[Segment: ");
        sb.append(" Name = ").append(getName());
        sb.append(", Start = ").append(getStartLocation());
        sb.append(", End = ").append(getEndLocation());
        sb.append(", BufferWidthPercent = ").append(getBufferWidthPercent());
        sb.append(", Width = ").append(getWidth());
        sb.append("]");

        return sb.toString();
    }
}
