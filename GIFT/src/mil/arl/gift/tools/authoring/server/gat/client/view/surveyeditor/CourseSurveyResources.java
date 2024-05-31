/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import generated.course.Course;
import mil.arl.gift.tools.authoring.server.gat.client.util.GatClientCourseUtil;

/**
 * Survey resources for a {@link Course} whose survey context can be modified by the survey composer
 * 
 * @author nroberts
 */
public class CourseSurveyResources extends AbstractSurveyResources {
    
    /** The {@link Course} defining the survey resources */
    private Course course;
    
    /**
     * Creates a set of survey resources defined by the given course
     * 
     * @param course the course defining the survey resources
     */
    public CourseSurveyResources(Course course) {
        
        if(course == null) {
            throw new IllegalArgumentException("The course cannot be null");
        }
        
        this.course = course;
    }

    @Override
    public int getSurveyContextId() {
        
        if(course != null 
                && course.getSurveyContext() != null) {
            
            return course.getSurveyContext().intValue();
        }
        
        return -1;
    }

    @Override
    public boolean hasSharedQuestionBank() {
        return GatClientCourseUtil.hasSharedQuestionBank(course);
    }

}
