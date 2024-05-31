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
 * Exception thrown while trying to perform an operation on a document that requires write
 * permissions for which the user doesn't have.
 */
public class WritePermissionException extends IOException {
    private static final long serialVersionUID = 2672871623270407747L;
    
    public WritePermissionException(String message) {
        super(message);
    }

}
