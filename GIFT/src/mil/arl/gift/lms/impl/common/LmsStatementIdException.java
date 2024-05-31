/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception when parsing xAPI Statement properties within xAPI Statement id derivation.
 * 
 * @author Yet Analytics
 *
 */
public class LmsStatementIdException extends LmsException {

    private static final long serialVersionUID = 1L;
    
    public LmsStatementIdException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsStatementIdException(String msg) {
        super(msg);
    }
}
