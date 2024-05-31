/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import java.util.List;

import net.customware.gwt.dispatch.shared.Action;
 
/**
 * The Class FetchFilesByExtension.
 */
public class FetchFilesByExtension implements Action<FetchFilesByExtensionResult> {
	
	/** The extensions. */
	private List<String> extensions;
	
	/** The user name. */
	private String userName;
    
    /**
     * Instantiates a new fetch files by extension.
     */
    public FetchFilesByExtension() {
        super();
    }

	/**
	 * Gets the extensions.
	 *
	 * @return the extensions
	 */
	public List<String> getExtensions() {
		return extensions;
	}

	/**
	 * Sets the extensions.
	 *
	 * @param extensions the new extensions
	 */
	public void setExtensions(List<String> extensions) {
		this.extensions = extensions;
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
        sb.append("[FetchFilesByExtension: ");
        sb.append("# extensions = ").append((extensions != null ? extensions.size() : "null"));
        if (extensions != null) {
        	sb.append(" : [");
        	for(int i=0; i < extensions.size(); i++) {
        		if (i > 0) {
        			sb.append(", ");
        		}
        		sb.append(extensions.get(i));
        	}
        	sb.append("]");
        }
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
