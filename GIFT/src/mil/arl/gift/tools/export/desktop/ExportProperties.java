/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import mil.arl.gift.common.io.CommonProperties;

/**
 * This class manages the properties for the export tutor tool.
 * 
 * @author mhoffman
 *
 */
public class ExportProperties extends CommonProperties {

    /** the properties file name */
    private static final String PROPERTIES_FILE = "tools/export/export.properties";
    
    /** singleton instance of this class */
    private static ExportProperties instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return ExportProperties
     */
    public static synchronized ExportProperties getInstance() {

        if (instance == null) {
            instance = new ExportProperties();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private ExportProperties() {
        super(PROPERTIES_FILE);
    }

}
