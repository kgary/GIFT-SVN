/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services;

import mil.arl.gift.common.io.CommonProperties;

/**
 * The properties for the GIFT services.
 * 
 * @author mhoffman
 *
 */
public class ServicesProperties extends CommonProperties {
    
    /** the properties file name */
    private static final String PROPERTIES_FILE = "tools/services/services.properties";
    
    /** The file name of the folder where public files are stored */
    public static final String PUBLIC_FOLDER_NAME = "Public";
    
    /** The REST endpoint that should be written to when exporting XTSP files modified by GIFT */
    public static final String XTSP_REST_ENDPOINT_ADDRESS = "xtspRestEndpointAddress";
    
    /** The REST function used to save XTSP files modified by GIFT */
    public static final String EXPORT_XTSP_REST_FUNCTION = "exportXtspRestFunction";
    
    /** The REST function used to import XTSP files into GIFT */
    public static final String IMPORT_XTSP_REST_FUNCTION = "importXtspRestFunction";

    /** singleton instance of this class */
    private static ServicesProperties instance = null;

    /**
     * Return the singleton instance of this class
     * @return ServicesProperties
     */
    public static synchronized ServicesProperties getInstance() {

        if (instance == null) {
            instance = new ServicesProperties();
        }

        return instance;
    }

    /**
     * Class constructor - use services properties files
     */
    private ServicesProperties() {
        super(PROPERTIES_FILE);
    }
}
