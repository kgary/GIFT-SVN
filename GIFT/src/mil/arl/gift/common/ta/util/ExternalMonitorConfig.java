/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ta.util;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * A configuration that can be used to control how training applications that act as external 
 * monitors should handle the data that is shared with them by GIFT
 * 
 * @author nroberts
 */
@SuppressWarnings("serial")
public class ExternalMonitorConfig implements Serializable{

    /**
     * A set of settings that are commonly understood by training applications that act as external
     * monitors. Each setting controls some facet of how said monitors handle the data that GIFT
     * shares with them.
     * 
     * @author nroberts
     */
    public static enum Setting{
        
        /** 
         * A setting that controls whether or not an external monitor should show visual indicators
         * when participants in a training scenario are performing below expectation
         */
        ShowPoorAssessment(true),
        
        /** 
         * A setting that controls whether or not an external monitor should show visual indicators
         * when an observer controller needs to make a performance assessment
         */
        ShowObserverAssessment(true);
        
        /** Whether this setting should be enabled by default in a new configuration */
        private boolean enableByDefault;
        
        /**
         * Creates a new setting
         * 
         * @param enableByDefault whether this setting should be enabled by default in a new configuration
         */
        private Setting(boolean enableByDefault) {
            this.enableByDefault = enableByDefault;
        }
        
        /**
         * Gets whether this setting should be enabled by default in a new configuration
         * 
         * @return whether this setting should be enabled by default
         */
        public boolean getEnableByDefault() {
            return enableByDefault;
        }
    }
    
    /** The settings that this configuration modifies */
    private HashMap<Setting, Boolean> settings = new HashMap<>();
    
    /**
     * Creates a new configuration for an external monitor application
     */
    public ExternalMonitorConfig() {
        
        for(Setting setting : Setting.values()) {
            settings.put(setting, setting.getEnableByDefault());
        }
    }
    
    /**
     * Enables or disables the given setting within this configuration as specified
     * 
     * @param setting the target setting. If null, no action will be performed.
     * @param enabled whether the target setting should be enabled or disabled.
     * @return whether the setting was actually changed from its previous value
     */
    public boolean set(Setting setting, boolean enabled) {
        
        if(setting != null) {
            return settings.put(setting, enabled) != enabled;
        }
        
        return false;
    }
    
    /**
     * Gets all of the settings that this configuration modifies
     * 
     * @return the modified settings. Will not be null.
     */
    public Map<Setting, Boolean> getSettings(){
        return Collections.unmodifiableMap(settings);
    }
}
