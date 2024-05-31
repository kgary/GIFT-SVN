/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused when creating a v2 UUID within xAPI instrumentation.
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiUUIDException extends LmsException {

    private static final long serialVersionUID = 1L;
    
    public LmsXapiUUIDException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsXapiUUIDException(String msg) {
        super(msg);
    }
}
