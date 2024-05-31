/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped;

import java.io.File;
import java.io.FileFilter;

import mil.arl.gift.common.io.AbstractSchemaHandler;

/**
 * This filter is used to find pedagogy configuration files.
 * 
 * @author jleonard
 */
public class PedagogyConfigFileFilter implements FileFilter {
    
    private static final String SVN = ".svn";

    /**
     * Accepts a file if it is a pedagogy configuration file or a directory
     * 
     * @param f The file
     * @return If the file is a pedagogy configuration file or directory
     */
    @Override
    public boolean accept(File f) {
        
        if(f.getName().endsWith(AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION)) {
            return true;
        } else if (f.isDirectory() && !f.getName().equals(SVN)) {
            return true;
        } else {
            return false;
        }
    }    
}
