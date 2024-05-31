/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception indicating that an xAPI Statement is invalid.
 * 
 * @author Yet Analytics
 *
 */
public class LmsInvalidStatementException extends LmsException {

    private static final long serialVersionUID = 1L;
    
    public LmsInvalidStatementException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsInvalidStatementException(String msg) {
        super(msg);
    }
}
