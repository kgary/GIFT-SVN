/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import net.customware.gwt.dispatch.shared.Action;
 
// TODO: Auto-generated Javadoc
/**
 * The Class GetDomainDirectoryModel.
 */
public class FetchRootDirectoryModel implements Action<FetchRootDirectoryModelResult> {
	
	/** The extensions to include. */
	private String[] extensionsToInclude;
	
	/** The user name. */
	private String userName;
    
    /** 
     * Default public constructor needed for serialization. 
     */	
	public FetchRootDirectoryModel() {
		 super();
	}

	/**
	 * Gets the extensions to include.
	 *
	 * @return the extensionsToInclude
	 */
	public String[] getExtensionsToInclude() {
		return extensionsToInclude;
	}

	/**
	 * Sets the extensions to include.
	 *
	 * @param extensionsToInclude the extensionsToInclude to set
	 */
	public void setExtensionsToInclude(String[] extensionsToInclude) {
		this.extensionsToInclude = extensionsToInclude;
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
        sb.append("[FetchRootDirectoryModel: ");
        sb.append("extensionsToInclude = ").append(extensionsToInclude);
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
