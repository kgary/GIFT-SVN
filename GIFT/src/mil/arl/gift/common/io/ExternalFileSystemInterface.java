/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface used by file services for file related logic in a file system (e.g. Windows, Nuxeo)
 * 
 * @author mhoffman
 *
 */
public interface ExternalFileSystemInterface {

    /**
     * Return the input stream that can be used to read the contents of the file.
     * 
     * @return the input stream for the file
     * @throws IOException  if there was a problem getting the input stream
     */
    public InputStream getInputStream() throws IOException;

}
