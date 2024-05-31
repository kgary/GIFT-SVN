/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain;

/**
 * An interface for getting the outcome of an asynchronous action
 *
 * @author jleonard
 */
public interface AsyncActionCallback {

    /**
     * Callback for when the action has a failure
     *
     * @param e The exception that occurred
     */
    void onFailure(Exception e);

    /**
     * Callback for when the action is successful
     */
    void onSuccess();
}
