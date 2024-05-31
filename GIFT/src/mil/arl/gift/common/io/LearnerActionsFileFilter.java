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
 * This filter is used to find learner actions (learnerActions.xml) files.
 * 
 * @author jleonard
 */
public class LearnerActionsFileFilter implements FileFilter {
    
    /** The learner actions file extension */
    public static final String FILE_EXTENSION = "learnerActions.xml";
    
    private static final String SVN = ".svn";

    /**
     * Accepts a file if it is a lesson material file or a directory
     * 
     * @param f The file
     * @return If the file is a domain course file or directory
     */
    @Override
    public boolean accept(File f) {
        
        if(f.getName().endsWith(FILE_EXTENSION)) {
            return true;
        } else if (f.isDirectory() && !f.getName().equals(SVN)) {
            return true;
        } else {
            return false;
        }
    }    
}
