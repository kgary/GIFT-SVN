/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.client;

/**
 * A simple callback for handling success or failures for a particular action
 * 
 * @author nblomberg
 *
 */
public interface SuccessFailCallback {
    
    /**
     * Handler for when the action succeeds
     */
    void onSuccess();
    
    /**
     * Handler for when the action fails
     */
    void onFailure();

}
