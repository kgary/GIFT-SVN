/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server;

import java.util.ArrayList;
import java.util.List;
import mil.arl.gift.common.module.AbstractModuleProperties;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;


/**
 * Handles the properties for the web monitor module.  
 * 
 * @author nblomberg
 *
 */
public class WebMonitorModuleProperties extends AbstractModuleProperties {

    /** the properties file name - sharing the existing dashboard.properties file. */
    private static final String PROPERTIES_FILE = "tools/dashboard/dashboard.properties";

    /** singleton instance of this class */
    private static WebMonitorModuleProperties instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return RuntimeToolModuleProperties
     */
    public static synchronized WebMonitorModuleProperties getInstance() {

        if (instance == null) {
            instance = new WebMonitorModuleProperties();
        }

        return instance;
    }

    /**
     * Class constructor
     */
    private WebMonitorModuleProperties() {
        super(PROPERTIES_FILE);
    }

    @Override
    public void setCommandLineArgs(String[] args) {

        List<Option> moduleOptionsList = new ArrayList<>();
        
        OptionBuilder.hasArg();

        setCommandLineArgs(moduleOptionsList, args);        
    }   
    
}
