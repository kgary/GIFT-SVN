/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.surveyeditor;

import net.customware.gwt.dispatch.shared.Action;

/**
 * An action for importing a Qualtrics survey
 * 
 * @author bzahid
 */
public class ImportQsf implements Action<QualtricsImportResult> {

	private String username;
	
	private String qsfContent;
	
    /**
     * No-arg constructor. Needed for serialization.
     */
    public ImportQsf() {
    	
    }

    /**
     * Creates a new action to import a qualtrics survey
     * 
     * @param username the username of the user making the request
     * @param qsfContent the content of the qsf export
     */
    public ImportQsf(String username, String qsfContent) {
    	this.username = username;
    	this.qsfContent = qsfContent;
    }
    
    /**
     * Gets the username of the user making the request
     * 
     * @return the username
     */
	public String getUsername() {
		return username;
	}

	/**
	 * Gets the content of the qsf export
	 * 
	 * @return the content of the qsf export
	 */
	public String getQsfContent() {
		return qsfContent;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ImportQsf: ");
        sb.append("username = ").append(username);
        sb.append(", qsfContent = ").append(qsfContent);
        sb.append("]");

        return sb.toString();
    } 
}
