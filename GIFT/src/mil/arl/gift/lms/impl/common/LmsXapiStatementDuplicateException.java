/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception caused by the creation of an unintended duplicate xAPI Statement; duplication is determined based on xAPI statement id.
 * 
 * In most cases within the instrumentation, the id is generated from xAPI Statement properties. 
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiStatementDuplicateException extends LmsException {

    private static final long serialVersionUID = 1L;
    
    public LmsXapiStatementDuplicateException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsXapiStatementDuplicateException(String msg) {
        super(msg);
    }
}
