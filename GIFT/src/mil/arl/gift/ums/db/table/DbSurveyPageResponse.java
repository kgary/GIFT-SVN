/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = "surveypageresponse")
public class DbSurveyPageResponse {

    private int surveyPageResponseId;

    private DbSurveyResponse surveyResponse;

    private DbSurveyPage surveyPage;

    private Date startTime;

    private Date endTime;

    private Set<DbQuestionResponse> questionResponses;

    /**
     * Default Constructor
     */
    public DbSurveyPageResponse() {
    }

    /**
     * Class constructor
     *
     * @param surveyResponse contains the response content
     * @param surveyPage the page for the response
     * @param startTime the time at which the question started being answered
     * @param endTime the time at which the question was done being answered
     * @param questionResponses the responses to the question
     */
    public DbSurveyPageResponse(DbSurveyResponse surveyResponse, DbSurveyPage surveyPage, Date startTime, Date endTime, Set<DbQuestionResponse> questionResponses) {
        this.surveyResponse = surveyResponse;
        this.surveyPage = surveyPage;
        this.startTime = startTime;
        this.endTime = endTime;
        this.questionResponses = questionResponses;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "surveyPageResponseId_PK")
    @TableGenerator(name = "surveyPageResponseId", table = "surveypageresponsepktb", pkColumnName = "surveyPageResponsekey", pkColumnValue = "surveyPageResponsevalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "surveyPageResponseId")
    public int getSurveyPageResponseId() {
        return surveyPageResponseId;
    }

    public void setSurveyPageResponseId(int surveyPageResponseId) {
        this.surveyPageResponseId = surveyPageResponseId;
    }

    //if a survey page response is deleted, don't want the survey response to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyResponseId_FK")
    public DbSurveyResponse getSurveyResponse() {
        return surveyResponse;
    }

    public void setSurveyResponse(DbSurveyResponse surveyResponse) {
        this.surveyResponse = surveyResponse;
    }

    //if a survey page response is deleted, don't want the survey page to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyPageId_FK")
    public DbSurveyPage getSurveyPage() {
        return surveyPage;
    }

    public void setSurveyPage(DbSurveyPage surveyPage) {
        this.surveyPage = surveyPage;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date time) {
        this.startTime = time;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    //created bi-directional 
    //now when a survey page response is deleted, the questionResponses are deleted
    @OneToMany(targetEntity = DbQuestionResponse.class, mappedBy = "surveyPageResponse", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    public Set<DbQuestionResponse> getQuestionResponses() {
        return questionResponses;
    }

    public void setQuestionResponses(Set<DbQuestionResponse> questionResponses) {
        this.questionResponses = questionResponses;
    }

    /**
     * Return a string representation of this class
     *
     * @return String - a string representation of this class
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyPageResponse:");
        sb.append(" id = ").append(getSurveyPageResponseId());
        sb.append(", survey response id = ").append(getSurveyResponse().getSurveyResponseId());
        sb.append(", survey page id = ").append(getSurveyPage().getSurveyPageId());
        sb.append(", start time = ").append(getStartTime());
        sb.append(", end time = ").append(getEndTime());
        sb.append(", question responses = {");
        for (DbQuestionResponse questionResponse : questionResponses) {
            sb.append(questionResponse).append(", ");
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
