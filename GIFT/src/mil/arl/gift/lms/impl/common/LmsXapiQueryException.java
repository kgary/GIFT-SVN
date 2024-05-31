/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception when interacting with an LRS's API.
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiQueryException extends LmsException {
    
    private static final long serialVersionUID = 1L;

    public LmsXapiQueryException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsXapiQueryException(String msg) {
        super(msg);
    }
}
