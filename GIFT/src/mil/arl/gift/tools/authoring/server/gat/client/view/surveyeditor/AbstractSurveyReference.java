/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

/**
 * An abstract wrapper used by the survey composer to wrap schema objects that reference surveys. 
 * This class defines some common functionality that is shared between various schema objects, allowing
 * the survey composer to invoke common behavior 
 * 
 * @author nroberts
 */
public abstract class AbstractSurveyReference {
    
    /**
     * Gets the display name of the schema object referencing the survey
     * 
     * @return the display name of the schema object
     */
    abstract public String getReferencingObjectName();
    
    /**
     * Gets whether or not this survey reference supports displaying the survey in full screen mode
     * 
     * @return whether or not this survey reference supports displaying the survey in full screen mode 
     */
    abstract public boolean supportsFullscreen();
    
    /**
     * Gets whether or not the survey should be displayed in full screen mode
     * 
     * @return whether or not the survey should be displayed in full screen mode
     */
    abstract public boolean isFullscreen();
}
