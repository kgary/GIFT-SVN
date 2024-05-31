/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * Result containing the results of a course deletion.
 * 
 * @author bzahid
 */
public class DeleteCourseResult extends RpcResponse {

	/** Whether or not the survey context could not be deleted. */
	private boolean failedToDeleteSurvey = false;	
	
	/** Whether or not the deletion failed because one or more survey elements had a set of responses associated with it */
	private boolean hadSurveyResponses = false;
	
	private DomainOption courseWithIssue = null;
	
	/**
	 * Class constructor.
	 */
	public DeleteCourseResult() {
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
	
	/**
	 * Gets whether or not there was a problem deleting the survey context
	 * 
	 * @return true if there was a problem deleting the survey context, false if it was deleted successfully.
	 */
	public boolean deleteSurveyFailed() {
		return failedToDeleteSurvey;
	}
	
	/**
	 * Sets whether or not the user has permission to delete the survey 
	 * 
	 * @param isAllowedUser true if the user has permission to delete the survey, false otherwise
	 */
	public void setDeleteSurveyFailed(boolean isAllowedUser) {
		this.failedToDeleteSurvey = isAllowedUser;
	}

	/**
	 * The first course that had an issue when deleting.
	 * 
	 * @return can be null if all courses where deleted successfully.
	 */
    public DomainOption getCourseWithIssue() {
        return courseWithIssue;
    }

    /**
     * Set the first course that had an issue when deleting.
     * 
     * @param courseWithIssue a course that had an issue deleting
     */
    public void setCourseWithIssue(DomainOption courseWithIssue) {
        this.courseWithIssue = courseWithIssue;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[DeleteCourseResult: failedToDeleteSurvey=");
        builder.append(failedToDeleteSurvey);
        builder.append(", hadSurveyResponses=");
        builder.append(hadSurveyResponses);
        builder.append(", courseWithIssue=");
        builder.append(courseWithIssue);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }
    
    
}
