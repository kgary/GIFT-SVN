/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;
 
import mil.arl.gift.common.survey.AbstractQuestion;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GenericGatServiceResult;
import net.customware.gwt.dispatch.shared.Action;
 
/**
 * An action telling the dispatch service to fetch the question export corresponding to a file.
 * 
 * @author nroberts
 */
public class FetchQuestionExport implements Action<GenericGatServiceResult<AbstractQuestion>> {

	/** The Domain-relative path to the file from which to get the question export.*/
    private String domainRelativePath;
	
	/** The user name. */
	private String userName;
    
    /** 
     * Default public constructor. 
     */
    public FetchQuestionExport() {
        super();
    }

	/**
	 * Gets the domain relative path.
	 *
	 * @return the domain relative path
	 */
	public String getRelativePath() {
		return domainRelativePath;
	}

	/**
	 * Sets the domain relative path.
	 *
	 * @param domainRelativePath the new domain relative path
	 */
	public void setRelativePath(String domainRelativePath) {
		this.domainRelativePath = domainRelativePath;
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
}
