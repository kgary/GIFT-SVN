/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.util.StringUtils;

/**
 * Used to specify which courses should be returned by the dashboard's RPC
 * service when the course list is requested
 * 
 * @author tflowers
 *
 */
public class CourseListFilter implements Serializable {

    /**
     * Specifies values for where the courses to populate 
     * the course list should be fetched from.
     * @author tflowers
     *
     */
    public enum CourseSourceOption {
        MY_COURSES("My Courses"),
        SHOWCASE_COURSES("Showcase Courses"),
        SHARED_COURSES("Shared With Me");

        private String displayName;

        CourseSourceOption(String displayName) {
            if(displayName == null) {
                throw new IllegalArgumentException("displayName can't be null");
            }
            
            this.displayName = displayName;
        }

        /**
         * Getter for the display name of the CourseSourceOption
         * @return the value of the display name
         */
        public String getDisplayName() {
            return displayName;
        }
    }

    /** default */
    private static final long serialVersionUID = 1L;

    /**
     * The collection of places to search for the courses.
     */
    private List<CourseSourceOption> courseSourceOptions = new ArrayList<>();

    /**
     * The collection of places to search for the courses.
     * An empty collection indicates that all locations should
     * be searched.
     * @return the collection of course source options, can't be null
     */
    public List<CourseSourceOption> getCourseSourceOptions() {
        return courseSourceOptions;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[CourseListFilter: courseSourceOptions = { ");
        StringUtils.join(", ", getCourseSourceOptions(), sb);
        sb.append(" } ]");
        
        return sb.toString();
    }
}