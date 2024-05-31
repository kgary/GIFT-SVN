/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;

/**
 * Websocket message for receiving the module status from the server to the client
 * and commanding a new status from the client to the server
 * 
 * @author efernando
 *
 */
@SuppressWarnings("serial")
public class ModuleStatusMessage implements Serializable {

    /** The module's name */
    private String moduleName;

    /** True if the module is online */
    private boolean isOnline;
    
    public ModuleStatusMessage() {
    }
    
    public ModuleStatusMessage(String moduleName, boolean isOnline) {
        this.moduleName = moduleName;
        this.isOnline = isOnline;
    }
    
    /**
     * Returns the module's name
     * @return the module's name
     */
    public String getModuleName() {
        return moduleName;
    }
    
    /**
     * Returns the online status
     * @return true if the module is online
     */
    public boolean getIsOnline() {
        return isOnline;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[ModuleStatusMessage: ");
        sb.append("moduleName = ").append(moduleName);
        sb.append(", isOnline = ").append(isOnline);
        sb.append("]");
        
        return sb.toString();
    }
}
