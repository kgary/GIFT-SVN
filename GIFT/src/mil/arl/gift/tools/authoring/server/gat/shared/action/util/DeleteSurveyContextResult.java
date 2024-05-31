/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Result containing the results of a survey context deletion.
 * 
 * @author nroberts
 */
public class DeleteSurveyContextResult extends GatServiceResult {
	
	/** Whether or not the deletion failed because one or more survey elements had a set of responses associated with it */
	private boolean hadSurveyResponses = false;
	
	/**
	 * Class constructor.
	 */
	public DeleteSurveyContextResult() {
		super();
	}

	/**
	 * Gets whether or not the deletion failed because one or more survey elements had a set of responses associated with it
	 * 
	 * @return whether the delete failed due to survey responses
	 */
	public boolean hadSurveyResponses() {
		return hadSurveyResponses;
	}

	/**
	 * Sets whether or not the deletion failed because one or more survey elements had a set of responses associated with it
	 * 
	 * @param hadSurveyResponses whether the delete failed due to survey responses
	 */
	public void setHadSurveyResponses(boolean hadSurveyResponses) {
		this.hadSurveyResponses = hadSurveyResponses;
	}
}
