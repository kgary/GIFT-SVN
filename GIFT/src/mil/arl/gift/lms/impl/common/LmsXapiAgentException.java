/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused when creating or handling an xAPI Agent or Group.
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiAgentException extends LmsException {
    
    private static final long serialVersionUID = 1L;
    
    public LmsXapiAgentException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsXapiAgentException(String msg) {
        super(msg);
    }

}
