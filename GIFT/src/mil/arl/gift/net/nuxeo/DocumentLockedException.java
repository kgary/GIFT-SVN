/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.nuxeo;

import java.io.IOException;

/**
 * This exception is thrown when trying to lock a document that is already
 * locked by another user.
 * 
 */
public class DocumentLockedException extends IOException {
    private static final long serialVersionUID = 4678484918777449787L;
    
    public DocumentLockedException(String message) {
        super(message);
    }
}
