/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.Option;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Contains the Gateway module property values.
 * 
 * @author mhoffman
 *
 */
public class GatewayModuleProperties extends AbstractModuleProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "gateway"+File.separator+"gateway.properties";
    
    /**
     * The remote proprties file name.
     * This file is used in both Desktop and Server modes.
     * This file extends the Gateway properties with optionally set remote gateway properties, such as ClientId.
     */
    private static final String REMOTE_PROPERTIES_FILE = "gateway.remote.properties";
    
    private static final long DEFAULT_REMOTE_PRESENT_INSTALLER_TO = 5000;
    
    // From gateway.properties
    private static final String INTEROP_CONFIG = "InteropConfig";
    
    /** singleton instance of this class */
    private static GatewayModuleProperties instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return UMSProperties
     */
    public static synchronized GatewayModuleProperties getInstance(){
        
        if(instance == null){
            instance = new GatewayModuleProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor
     */
    private GatewayModuleProperties(){
        super(PROPERTIES_FILE, REMOTE_PROPERTIES_FILE);
    }
    
    /**
     * Return the path to the interop configuration file in the GIFT/ directory.
     * 
     * @return the relative file path to the interopConfig.xml file
     */
    public String getInteropConfig(){
        return getPropertyValue(INTEROP_CONFIG);
    }
    
    @Override
    public void setCommandLineArgs(String[] args) {

        List<Option> moduleOptionsList = new ArrayList<>();
        super.setCommandLineArgs(moduleOptionsList, args);
    }

    /**
     * Return the timeout in milliseconds used to wait for the domain module to
     * notify a remote gateway module of the interops to configure which
     * causes the gateway module to show the installer dialog. If this timeout
     * is reached the gateway module is killed gracefully.
     * 
     * @return value in milliseconds.
     */
    public static long getRemotePresentInstallerTimeout(){
        /* NOTE: For now, return the default installer timeout class field since the
         * property is no longer a system property returned from the JNLP file. 
         * In the future, the remote zip generation logic will be extended to read/
         * write this property to the gateway.remote.properties file to be read from. 
         */
        return DEFAULT_REMOTE_PRESENT_INSTALLER_TO;
    }
}
