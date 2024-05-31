/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.util.StringUtils;

/**
 * Contains information about a path (list of points).
 *
 * @author mhoffman
 *
 */
public class Path implements PlaceOfInterestInterface {

    /** the display name for this path */
    private String name;

    /** the list of segments for this path */
    private List<Segment> segments;

    /**
     * Set attributes
     *
     * @param name the display name for this path. Can't be null or empty.
     * @param segments the {@link List} of {@link Segment} for this path. Can't
     *        be null or empty.
     */
    public Path(String name, List<Segment> segments) {

        if(StringUtils.isBlank(name)){
            throw new IllegalArgumentException("The name is null or empty.");
        } else if (segments == null || segments.isEmpty()) {
            throw new IllegalArgumentException("The segments are null or empty.");
        }

        this.name = name;
        this.segments = segments;
    }

    /**
     * Class constructor - set attributes using generated class's object for a path
     *
     * @param path - dkf content for a path
     */
    public Path(generated.dkf.Path path){
        this(path.getName(), buildSegmentsFromXmlObjects(path.getSegment()));
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Converts a {@link List} of {@link generated.dkf.Segment} to a
     * {@link List} of {@link Segment}
     *
     * @param segments The {@link List} to convert from. Can be null.
     * @return The {@link List} to convert to. Can't be null.
     */
    private static List<Segment> buildSegmentsFromXmlObjects(List<generated.dkf.Segment> segments) {
        List<Segment> toRet = new ArrayList<>();
        if (segments == null) {
            return toRet;
        }

        for (generated.dkf.Segment segment : segments) {
            toRet.add(new Segment(segment));
        }

        return toRet;
    }

    /**
     * Return the {@link List} of {@link Segment} for this {@link Path}.
     *
     * @return The {@link List} of {@link Segment}. Can't be null or empty.
     */
    public List<Segment> getSegment() {
        return segments;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[Path: ");
        sb.append(" Name = ").append(getName());
        sb.append(", location = ").append(super.toString());
        sb.append("]");

        return sb.toString();
    }

}
