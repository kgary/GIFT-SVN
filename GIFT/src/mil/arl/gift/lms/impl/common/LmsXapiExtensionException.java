/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.lms.impl.common;

/**
 * Represents an exception when generating or parsing an xAPI Statement extension.
 * 
 * @author Yet Analytics
 *
 */
public class LmsXapiExtensionException extends LmsException {

    private static final long serialVersionUID = 1L;
    
    public LmsXapiExtensionException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    public LmsXapiExtensionException(String msg) {
        super(msg);
    }
}
