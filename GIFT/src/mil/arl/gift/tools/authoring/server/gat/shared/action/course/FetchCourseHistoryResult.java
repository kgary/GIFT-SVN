/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import java.util.Date;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;


/**
 * The result of a Fetch Course History {@link FetchCourseHistory}.
 * 
 * @author mhoffman
 *
 */
public class FetchCourseHistoryResult extends GatServiceResult{
	
	/** the date when the course folder was last modified */
	private Date courseFolderLastModifiedDate;
	
	/** the date when the survey context was last modified */
	private Date surveyContextLastModifiedDate;
	
	/**
	 * Class constructor
	 * For serialization only.
	 */
	public FetchCourseHistoryResult() {  }

	/**
	 * Return the date when the course folder was last modified
	 * 
	 * @return can be null if the course folder path was not provided in the fetch request
	 */
    public Date getCourseFolderLastModifiedDate() {
        return courseFolderLastModifiedDate;
    }


    public void setCourseFolderLastModifiedDate(Date courseFolderLastModifiedDate) {
        this.courseFolderLastModifiedDate = courseFolderLastModifiedDate;
    }

    /**
     * Return the date when the course folder was last modified
     * 
     * @return can be null if the survey context id was not provided in the fetch request
     */
    public Date getSurveyContextLastModifiedDate() {
        return surveyContextLastModifiedDate;
    }


    public void setSurveyContextLastModifiedDate(Date surveyContextLastModifiedDate) {
        this.surveyContextLastModifiedDate = surveyContextLastModifiedDate;
    }


    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[FetchCourseHistoryResult: ");
        builder.append("course last modified = ");
        builder.append(courseFolderLastModifiedDate);
        builder.append(", survey context last modified = ");
        builder.append(surveyContextLastModifiedDate);
        builder.append("]");
        return builder.toString();
    }
}
