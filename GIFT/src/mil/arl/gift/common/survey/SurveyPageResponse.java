/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A response to a survey page
 *
 * @author jleonard
 */
public class SurveyPageResponse implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int surveyPageResponseId;

    private int surveyResponseId;

    private SurveyPage surveyPage;

    private Date startTime;

    private Date endTime;

    private List<AbstractQuestionResponse> questionResponses = new ArrayList<AbstractQuestionResponse>();

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public SurveyPageResponse() {
    }

    /**
     * Constructor
     *
     * @param surveyPageResponseId The ID of the survey page response
     * @param surveyResponseId The ID of the survey response this page is in
     * @param surveyPage The survey this response is for
     * @param startTime The time the survey page response was started
     * @param endTime The time the survey page response was completed
     * @param questionResponses The list of responses to questions on the page
     */
    public SurveyPageResponse(int surveyPageResponseId, int surveyResponseId, SurveyPage surveyPage, Date startTime, Date endTime, List<AbstractQuestionResponse> questionResponses) {
        this.surveyPageResponseId = surveyPageResponseId;
        this.surveyResponseId = surveyResponseId;
        this.surveyPage = surveyPage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.questionResponses = questionResponses;
    }

    /**
     * Gets the ID of the survey page response
     *
     * @return int The ID of the survey page response
     */
    public int getSurveyPageResponseId() {
        return surveyPageResponseId;
    }

    /**
     * Gets the ID of the survey response this page is in
     *
     * @return int The ID of the survey response this page is in
     */
    public int getSurveyResponseId() {
        return surveyResponseId;
    }

    /**
     * Gets the survey page this response is for
     *
     * @return surveyPage The survey page this response is for
     */
    public SurveyPage getSurveyPage() {
        return surveyPage;
    }

    /**
     * Sets the survey page this response is for
     *
     * @param surveyPage The survey page this response is for
     */
    public void setSurveyPage(SurveyPage surveyPage) {
        this.surveyPage = surveyPage;
    }

    /**
     * Gets the time the survey page response was started
     *
     * @return Date The time the survey page response was started
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the time the survey page response was started
     *
     * @param startTime The time the survey page response was started
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the time the survey page response was completed
     *
     * @return Date The time the survey page response was completed
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the time the survey page response was completed
     *
     * @param endTime The time the survey page response was completed
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the list of responses to questions on the survey page
     *
     * @return List<GwtQuestionResponse> The list of responses to questions on
     * the survey page
     */
    public List<AbstractQuestionResponse> getQuestionResponses() {
        return questionResponses;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyPageResponse:");
        sb.append(" id = ").append(getSurveyPageResponseId());
        sb.append(", survey response id = ").append(getSurveyResponseId());
        sb.append(", survey page = ").append(getSurveyPage());
        sb.append(", start time = ").append(getStartTime());
        sb.append(", end time = ").append(getEndTime());
        sb.append(", question responses = {");
        for (AbstractQuestionResponse questionResponse : getQuestionResponses()) {
            sb.append(questionResponse).append(", ");
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
