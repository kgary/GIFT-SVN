/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.controlpanel;

import mil.arl.gift.common.module.AbstractModuleProperties;


/**
 * Contains the Control Panel property values.
 *
 * @author bzahid
 */
public class ControlPanelProperties extends AbstractModuleProperties {

    /** the properties file name */
    private static final String PROPERTIES_FILE = "tools/controlpanel/controlpanel.properties";

    /** 
     * Properties
     */

    
    /** singleton instance of this class */
    private static ControlPanelProperties instance = null;

    /**
     * Return the singleton instance of this class
     * @return ControlPanelProperties
     */
    public static synchronized ControlPanelProperties getInstance() {

        if (instance == null) {
            instance = new ControlPanelProperties();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private ControlPanelProperties() {
        super(PROPERTIES_FILE);
    }
    
    @Override
    public void setCommandLineArgs(String[] args) {
       
    }
}
