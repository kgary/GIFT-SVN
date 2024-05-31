/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf;

import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileValidationException;

/**
 * This DKF handler offers simple methods to read and write dkf content
 *  
 * @author mhoffman
 *
 */
public class BasicDKFHandler extends AbstractDKFHandler {

    /**
     * Class constructor 
     * 
     * @param file - the dkf to handle
     * @throws FileValidationException - if the file doesn't exist
     */
    public BasicDKFHandler(FileProxy file) throws FileValidationException {
        super(file, true);

    }

}
