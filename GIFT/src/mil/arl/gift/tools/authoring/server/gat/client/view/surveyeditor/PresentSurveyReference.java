/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import generated.course.BooleanEnum;
import generated.course.PresentSurvey;

/**
 * A survey reference for a {@link PresentSurvey} course object that can be modified by the survey composer
 * 
 * @author nroberts
 */
public class PresentSurveyReference extends AbstractSurveyReference {
    
    /** The {@link PresentSurvey} course object containing the reference */
    private PresentSurvey surveyCourseObject;
    
    /**
     * Creates a wrapper around a survey reference for a {@link PresentSurvey} course object
     * 
     * @param surveyCourseObject the survey course object
     */
    public PresentSurveyReference(PresentSurvey surveyCourseObject) {
        
        if(surveyCourseObject == null) {
            throw new IllegalArgumentException("The survey course object cannot be null");
        }
        
        this.surveyCourseObject = surveyCourseObject;
    }

    @Override
    public String getReferencingObjectName() {
        return surveyCourseObject.getTransitionName();
    }

    @Override
    public boolean supportsFullscreen() {
        return true;
    }

    @Override
    public boolean isFullscreen() {
        return BooleanEnum.TRUE.equals(surveyCourseObject.getFullScreen());
    }
}
