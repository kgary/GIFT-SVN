/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.File;
import java.io.FileFilter;

/**
 * This filter is used to find event source files.
 * 
 * @author jleonard
 */
public class EventSourceFileFilter implements FileFilter {
    
    /** The message log file extension */
    private static final String MESSAGE_LOG_FILE_EXTENSION = ".log";

    /** The protobuf message log file extension */
    private static final String PROTOBUF_LOG_FILE_EXTENSION = ".protobuf.bin";

    private static final String SENSOR_DATA_FILE_EXTENSION = ".csv";
    
    private static final String SVN = ".svn";

    /**
     * Accepts a file if it is an event source file or a directory
     * 
     * @param f The file
     * @return If the file is an event source file or directory
     */
    @Override
    public boolean accept(File f) {
        
        if (f.getName().endsWith(MESSAGE_LOG_FILE_EXTENSION) || f.getName().endsWith(PROTOBUF_LOG_FILE_EXTENSION)
                || f.getName().endsWith(SENSOR_DATA_FILE_EXTENSION)) {
            return true;
        } else if (f.isDirectory() && !f.getName().equals(SVN)) {
            return true;
        } else {
            return false;
        }
    }    
}
