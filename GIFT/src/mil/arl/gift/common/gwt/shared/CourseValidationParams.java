/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

import java.io.Serializable;

/**
 * Used to configure course validation parameters when requesting validation of a course.
 * 
 * @author mhoffman
 *
 */
public class CourseValidationParams implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * whether these types of checks are allowed at all
     */
    private boolean allowSchemaValidation = true;    
    private boolean allowCourseLogicValidation = true;    
    private boolean allowSurveyValidation = true;
    
    /**
     * whether to ignore any logic that bypasses validation (e.g. caching, last modified date, last validation date)
     * Note: if the associated 'allow' flag is false (e.g. allowCourseLogicValidation), than this flag should be ignored.
     */
    private boolean forceCourseLogicValidation = false;    
    private boolean forceSurveyValidation = false;

    /**
     * Default constructor - see default parameters by exploring class
     */
    public CourseValidationParams(){
        
    }
    
    public boolean isAllowSchemaValidation() {
        return allowSchemaValidation;
    }

    public void setAllowSchemaValidation(boolean allowSchemaValidation) {
        this.allowSchemaValidation = allowSchemaValidation;
    }

    public boolean isAllowCourseLogicValidation() {
        return allowCourseLogicValidation;
    }

    public void setAllowCourseLogicValidation(boolean allowCourseLogicValidation) {
        this.allowCourseLogicValidation = allowCourseLogicValidation;
    }

    public boolean isAllowSurveyValidation() {
        return allowSurveyValidation;
    }

    public void setAllowSurveyValidation(boolean allowSurveyValidation) {
        this.allowSurveyValidation = allowSurveyValidation;
    }

    public boolean isForceCourseLogicValidation() {
        return forceCourseLogicValidation;
    }

    public void setForceCourseLogicValidation(boolean forceCourseLogicValidation) {
        this.forceCourseLogicValidation = forceCourseLogicValidation;
    }

    public boolean isForceSurveyValidation() {
        return forceSurveyValidation;
    }

    public void setForceSurveyValidation(boolean forceSurveyValidation) {
        this.forceSurveyValidation = forceSurveyValidation;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[CourseValidationParams: allowSchemaValidation=");
        builder.append(allowSchemaValidation);
        builder.append(", allowCourseLogicValidation=");
        builder.append(allowCourseLogicValidation);
        builder.append(", allowSurveyValidation=");
        builder.append(allowSurveyValidation);
        builder.append(", forceCourseLogicValidation=");
        builder.append(forceCourseLogicValidation);
        builder.append(", forceSurveyValidation=");
        builder.append(forceSurveyValidation);
        builder.append("]");
        return builder.toString();
    }
    
    
}
