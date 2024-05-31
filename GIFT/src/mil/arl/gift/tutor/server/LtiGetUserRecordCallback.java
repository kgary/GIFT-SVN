/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import mil.arl.gift.common.lti.LtiUserRecord;

/**
 * The LtiGetUserRecordCallback is used to return the LtiUserRecord from an asynchronous
 * request to fetch the lti user record.
 * 
 * @author nblomberg
 *
 */
public interface LtiGetUserRecordCallback {

    /**
     * Called when the request is successful.  The LtiUserRecord is returned upon success, but can also be null.
     * @param record The record that was found.  Null means the record was not found.
     */
    void onSuccess(LtiUserRecord record);
    
    /**
     * Handles an error message encountered while fetching the lti user record.
     * 
     * @param errorMessage the error message
     * @param additionalInformation additional information to display to the user if needed.  This can be null
     *                              when not needed.
     */
    void onFailure(String errorMessage, String additionalInformation);
}
