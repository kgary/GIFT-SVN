/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

/**
 * A callback interface when an action has been completed
 * 
 * @author jleonard
 */
public interface AsyncResponseCallback {

    /**
     * Callback when an action has been completed
     * 
     * @param success If the action was successful or not
     * @param response The result of the action
     * @param additionalInformation additional information to display to the user if needed.  This can be null
     *                              when not needed.
     */
    void notify(boolean success, String response, String additionalInformation);
}
