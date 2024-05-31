/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused within processing GIFT data into xAPI Statement(s).
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiProcessorException extends LmsException {
    private static final long serialVersionUID = 1L;

    public LmsXapiProcessorException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsXapiProcessorException(String msg) {
        super(msg);
    }
}
