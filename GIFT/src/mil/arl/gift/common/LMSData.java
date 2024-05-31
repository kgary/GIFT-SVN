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
import java.util.List;

import mil.arl.gift.common.survey.score.AbstractScale;

/**
 * This class contains all data that can be retrieved from the LMS.
 *
 * @author sharrison
 */
public class LMSData implements Serializable {

    private static final long serialVersionUID = 1L;

    /** The ID of the course record */
    private LMSCourseRecords courseRecords;

    /** The learner state attributes */
    private List<AbstractScale> abstractScales;

    /**
     * Constructor - Default - for gwt serialization.
     */
    public LMSData() {
        this.abstractScales = new ArrayList<AbstractScale>();
    }

    /**
     * @return the courseRecords
     */
    public LMSCourseRecords getCourseRecords() {
        return courseRecords;
    }

    /**
     * @param courseRecords the courseRecords to set
     */
    public void setCourseRecords(LMSCourseRecords courseRecords) {
        this.courseRecords = courseRecords;
    }

    /**
     * @return the abstractScales
     */
    public List<AbstractScale> getAbstractScales() {
        return abstractScales;
    }

    /**
     * @param abstractScales the abstractScales to set
     */
    public void setAbstractScales(List<AbstractScale> abstractScales) {
        this.abstractScales = abstractScales;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[LMSCourseRecord: ");
        sb.append("courseRecords = ").append(getCourseRecords());
        sb.append(", abstractScales = ").append(getAbstractScales());
        sb.append("]");

        return sb.toString();
    }

}
