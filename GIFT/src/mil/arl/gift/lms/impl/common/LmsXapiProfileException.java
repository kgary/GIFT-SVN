/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception related to xAPI Profile.
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiProfileException extends LmsException {
    
    private static final long serialVersionUID = 1L;

    public LmsXapiProfileException(String msg) {
        super(msg);
    }
    
    public LmsXapiProfileException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
