/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import mil.arl.gift.common.UserSession;

/**
 * A callback that handles the result of an experiment subject being created
 * 
 * @author nroberts
 */
public interface ExperimentSubjectCreatedCallback {

	/**
     * Performs an action when an experiment subject has been created. 
     * 
     * Instances overriding this method should return the subject's associated BrowserWebSession.
     * @return the newly created browser web session used to manage the learners browser instance. Can return null if there
     * was a problem creating the session.
     */
    TutorBrowserWebSession onSubjectCreated(UserSession userSession);
    
    /**
     * Handles an error message encountered while creating an experiment subject
     * 
     * @param errorMessage the error message
     * @param additionalInformation additional information to display to the user if needed.  This can be null
     *                              when not needed.
     */
    void onFailure(String errorMessage, String additionalInformation);
}
