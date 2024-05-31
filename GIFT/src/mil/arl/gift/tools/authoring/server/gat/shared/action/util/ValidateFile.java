/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import net.customware.gwt.dispatch.shared.Action;

/**
 * An action that validates a GIFT XML file.
 * 
 * @author bzahid
 */
public class ValidateFile implements Action<ValidateFileResult> {

	/** The domain relative path to the file. */
	private String domainRelativePath;
	
	/** The user name. */
	private String userName;
	
	/**
	 * Default public constructor
	 */
	public ValidateFile() {
		super();
	}
	
	/** Gets the domain relative path to the file. 
	 * 
	 * @ return the domain relative path to the file. 
	 */
	public String getFilePath() {
		return domainRelativePath;
	}
	
	/** 
	 * Sets the domain relative path to the file.
	 * 
	 * @param domainRelativePath the path to the file.
	 */
	public void setRelativePath(String domainRelativePath) {
		this.domainRelativePath = domainRelativePath;
	}
	
	/** Gets the username.
	 * @return the username. 
	 */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Sets the username.
	 * @param userName the username.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ValidateFile: ");
        sb.append("domainRelativePath = ").append(domainRelativePath);
        sb.append(", userName = ").append(userName);
        sb.append("]");

        return sb.toString();
    } 
}
