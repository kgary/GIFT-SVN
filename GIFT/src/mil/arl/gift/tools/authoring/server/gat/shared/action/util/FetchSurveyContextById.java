/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import java.math.BigInteger;

import net.customware.gwt.dispatch.shared.Action;

/**
 * Action for retrieving a survey context with a specified id.
 * 
 * @author bzahid
 */
public class FetchSurveyContextById implements Action<FetchSurveyContextByIdResult>{

	/**
	 * The id of the survey context to retrieve.
	 */
	int surveyContextId;
	
	/**
	 * Class constructor.
	 */
	public FetchSurveyContextById() {
		
	}
	
	/**
	 * Creates an action to get the survey context.
	 * 
	 * @param surveyContextId The id of the survey context to get.  Can't be null.
	 */
	public FetchSurveyContextById(BigInteger surveyContextId) {
		this.surveyContextId = surveyContextId.intValue();
	}

	/**
	 * Gets the survey context id.
	 * 
	 * @return the id of the survey context.
	 */
	public int getSurveyContextId() {
		return surveyContextId;
	}

	/**
	 * Sets the survey context id.
	 * 
	 * @param surveyContextId the id of the survey context.
	 */
	public void setSurveyContextId(int surveyContextId) {
		this.surveyContextId = surveyContextId;
	}

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[FetchSurveyContextById: ");
        sb.append("surveyContextId = ").append(surveyContextId);
        sb.append("]");

        return sb.toString();
    } 
}
