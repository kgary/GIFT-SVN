/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused when handling a DomainSession within xAPI instrumentation.
 * 
 * @author Yet Analytics
 *
 */
public class LmsDomainSessionException extends LmsException {

    private static final long serialVersionUID = 1L;
    
    public LmsDomainSessionException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsDomainSessionException(String msg) {
        super(msg);
    }
}
