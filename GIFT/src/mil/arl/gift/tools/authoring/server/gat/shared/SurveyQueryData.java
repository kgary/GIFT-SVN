/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.survey.Survey.SurveyTypeEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * The parameters of the query for surveys
 *
 * @author jleonard
 */
public class SurveyQueryData implements IsSerializable {

    private List<String> foldersQuery = null;
    
    /** The user name */
    private String userName;
    
    /** The type of surveys to search for*/
    private SurveyTypeEnum surveyType;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SurveyQueryData() {
    }
    
    /**
     * Constructor
     *
     * @param queryFolders The folders to search for surveys in
     * @param userName The user name.
     */
    public SurveyQueryData(List<String> queryFolders, String userName) {
        this(queryFolders, userName, null);
    }

    /**
     * Constructor
     *
     * @param queryFolders The folders to search for surveys in
     * @param userName The user name.
     * @param surveyType The type of surveys to search for.
     */
    public SurveyQueryData(List<String> queryFolders, String userName, SurveyTypeEnum surveyType) {
        
        foldersQuery = new ArrayList<String>();

        this.foldersQuery.addAll(queryFolders);
        
        this.userName = userName;
        this.surveyType = surveyType;
    }

    /**
     * Gets the list of folders to search for surveys in
     *
     * @return List<String> The list of folders to search for surveys in
     */
    public List<String> getQueryFolders() {

        return this.foldersQuery;
    }

    /**
     * Gets the user name
     * 
     * @return String The user name.
     */
	public String getUserName() {
		return userName;
	}
	
	/**
	 * Gets the type of surveys being searched for
	 * 
	 * @return the survey type to search for
	 */
	public SurveyTypeEnum getSurveyType() {
	    return surveyType;
	}
}
