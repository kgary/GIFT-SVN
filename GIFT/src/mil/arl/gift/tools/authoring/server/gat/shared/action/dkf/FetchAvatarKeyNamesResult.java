/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import java.util.List;
import java.util.Map;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * The Class FetchAvatarKeyNamesResult.
 */
public class FetchAvatarKeyNamesResult extends GatServiceResult {
	
	/** The avatar to key names. */
	Map<String, List<String>> avatarToKeyNames;

    /**
     * Instantiates a new fetch avatar key names result.
     */
    public FetchAvatarKeyNamesResult() {
        super();
    }
    
    /**
     * Instantiates a new fetch avatar key names result.
     *
     * @param avatarToKeyNames the avatar to key names
     */
    public FetchAvatarKeyNamesResult(Map<String, List<String>> avatarToKeyNames) {
        super();
        
        this.avatarToKeyNames = avatarToKeyNames;
    }
    
    /**
     * Gets the avatar to key names.
     *
     * @return the avatar to key names
     */
    public Map<String, List<String>> getAvatarToKeyNames(){
    	return this.avatarToKeyNames;
    }
    
    /**
     * Sets the avatar to key names.
     *
     * @param avatarToKeyNames the avatar to key names
     */
    public void setAvatarToKeyNames(Map<String, List<String>> avatarToKeyNames){
    	this.avatarToKeyNames = avatarToKeyNames;
    }
}
