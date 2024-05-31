/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;


/**
 * Enum to record the different types of user sessions that can exist in GIFT.
 * 
 * @author nblomberg
 *
 */
public enum UserSessionType {

        GIFT_USER,          // Normal user logged into GIFT (corresponds to entries in the GIFTUSER db table).
        EXPERIMENT_USER,    // Anonymous user that is running an experiment.  (corresponds to entries in the EXPERIMENTSUBJECT db table).
        LTI_USER,           // Logged in lti user from a Tool Consumer. (corresponds to entries in the LTIUSERRECORD db table).

}
