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
 * Exception thrown during document creation indicating a document already exists
 * on the server.
 * @see NuxeoInterface#createDocument(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String) 
 */
public class DocumentExistsException extends IOException {
    private static final long serialVersionUID = 5981986406274716402L;
    public DocumentExistsException(String message) {
        super(message);
    }
}
