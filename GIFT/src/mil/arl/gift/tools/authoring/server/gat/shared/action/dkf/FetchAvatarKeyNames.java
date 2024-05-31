/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import java.util.List;

import net.customware.gwt.dispatch.shared.Action;
 
/**
 * The Class FetchAvatarKeyNames.
 */
public class FetchAvatarKeyNames implements Action<FetchAvatarKeyNamesResult> {

	/** The avatar file names. */
	List<String> avatarFileNames;
	
	/** The user name. */
	private String userName;

    /**
     * Instantiates a new fetch avatar key names.
     */
    public FetchAvatarKeyNames() {
        super();
    }
    
    /**
     * Gets the avatars.
     *
     * @return the avatars
     */
    public List<String> getAvatars(){
    	return this.avatarFileNames;
    }
    
    /**
     * Sets the avatar.
     *
     * @param avatarFileNames the new avatar
     */
    public void setAvatar(List<String> avatarFileNames){
    	this.avatarFileNames = avatarFileNames;
    }

	/**
	 * Gets the user name.
	 * @return User name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Sets the user name.
	 * @param userName User name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchAvatarKeyNames: ");
        sb.append("# avatarFileNames = ").append((avatarFileNames != null ? avatarFileNames.size() : "null"));
        if (avatarFileNames != null) {
        	sb.append(" : [");
        	for(int i=0; i < avatarFileNames.size(); i++) {
        		if (i > 0) {
        			sb.append(", ");
        		}
        		sb.append(avatarFileNames.get(i));
        	}
        	sb.append("]");
        }
        sb.append(", userName").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
