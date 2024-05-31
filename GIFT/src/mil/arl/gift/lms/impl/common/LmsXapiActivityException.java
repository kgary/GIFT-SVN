/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused when creating or parsing an xAPI Activity.
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiActivityException extends LmsException {

    private static final long serialVersionUID = 1L;
    
    public LmsXapiActivityException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsXapiActivityException(String msg) {
        super(msg);
    }
}
