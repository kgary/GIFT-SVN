/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped;

import java.io.File;

import mil.arl.gift.common.module.AbstractModuleProperties;

/**
 * Contains the Pedagogical module property values.
 * 
 * @author mhoffman
 *
 */
public class PedagogicalModuleProperties extends AbstractModuleProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "ped"+File.separator+"ped.properties";

    /** the key to the EMAP configuration file property */
    private static final String EMAP_CONFIG = "EMAPConfigurationFile";
    
    /** the key to the ICAP configuration file property */
    private static final String ICAP_CONFIG = "ICAPPolicyFile";
    
    /** singleton instance of this class */
    private static PedagogicalModuleProperties instance = null;
    
    /**
     * Return the singleton instance of this class
     * 
     * @return PedagogicalModuleProperties
     */
    public static synchronized PedagogicalModuleProperties getInstance(){
        
        if(instance == null){
            instance = new PedagogicalModuleProperties();
        }
        
        return instance;
    }
    
    /**
     * Class constructor - load properties
     */
    private PedagogicalModuleProperties(){
        super(PROPERTIES_FILE);
    }
    
    /**
     * Return the configuration file for the Engine for Management of Adaptive Pedagogy
     * Pedagogical model.
     * 
     * @return the GIFT/ relative path to the configuration file
     */
    public String getEMAPConfigFileName(){
        return getPropertyValue(EMAP_CONFIG);
    }
    
    /**
     * Return the ICAP policy file for the ICAP pedagogical model.
     * @return the GIFT/ relative path to the policy file
     */
    public String getICAPPolicyFileName(){
        return getPropertyValue(ICAP_CONFIG);
    }

}
