/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused when updating an xAPI Statement. 
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiAppenderException extends LmsException {

    private static final long serialVersionUID = 1L;

    public LmsXapiAppenderException(String msg) {
        super(msg);
    }
    
    public LmsXapiAppenderException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
