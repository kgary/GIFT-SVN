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
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="surveyresponse")
public class DbSurveyResponse {

    private int surveyResponseId;

    /** depending on the user type, one of these should be populated */
    private DbUser user;
    private DbExperimentSubject subject;
    private DbGlobalUser globalUser;

    private DbSurvey survey;

    private Date startTime;

    private Date endTime;

    private DbSurveyContext surveyContext;

    private DbDomainSession domainSession;

    private Set<DbSurveyPageResponse> pageResponses;

    /**
     * Default constructor - required by Hibernate
     */
    public DbSurveyResponse() {
    }
    
    /**
     * Class constructor 
     *
     * @param subject - the user associated with this session
     * @param survey the survey given
     * @param surveyContext the survey context for the survey
     * @param domainSession the domain session the survey was given
     */
    public DbSurveyResponse(DbSurvey survey, DbSurveyContext surveyContext, DbDomainSession domainSession) {
        this.survey = survey;
        this.surveyContext = surveyContext;
        this.domainSession = domainSession;
    }


    @Id
    @Column(name = "surveyResponseId_PK")
    @TableGenerator(name = "surveyResponseid", table = "surveyresponsepktb", pkColumnName = "surveyResponsekey", pkColumnValue = "surveyResponsevalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "surveyResponseid")
    public int getSurveyResponseId() {
        return surveyResponseId;
    }

    public void setSurveyResponseId(int surveyResponseId) {
        this.surveyResponseId = surveyResponseId;
    }

    //if a survey response is deleted, don't want user to be deleted
    @ManyToOne
    @JoinColumn(name = "userId_FK")
    public DbUser getUser() {
        return user;
    }

    public void setUser(DbUser user) {
        this.user = user;
    }
    
    
    @ManyToOne
    @JoinColumn(name = "globalId_FK")
    public DbGlobalUser getGlobalUser() {
        return globalUser;
    }

    public void setGlobalUser(DbGlobalUser globalUser) {
        this.globalUser = globalUser;
    }
    
    
    //if a survey response is deleted, don't want subject to be deleted
    @ManyToOne
    @JoinColumns({
        @JoinColumn(name="experimentId_FK"),
        @JoinColumn(name="subjectId_FK")
    })
    public DbExperimentSubject getSubject() {
        return subject;
    }
    public void setSubject(DbExperimentSubject subject) {
        this.subject = subject;
    }

    //if a survey response is deleted, don't want survey to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyId_FK")
    public DbSurvey getSurvey() {
        return survey;
    }

    public void setSurvey(DbSurvey survey) {
        this.survey = survey;
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

    //if a survey response is deleted, don't want the survey context to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyContextId_FK")
    public DbSurveyContext getSurveyContext() {
        return surveyContext;
    }

    public void setSurveyContext(DbSurveyContext surveyContext) {
        this.surveyContext = surveyContext;
    }

    //if a survey response is deleted, don't want domain session to be deleted
    @ManyToOne
    @JoinColumn(name = "domainSessionId_FK")
    public DbDomainSession getDomainSession() {
        return domainSession;
    }

    public void setDomainSession(DbDomainSession domainSession) {
        this.domainSession = domainSession;
    }

    //created bi-directional 
    //now when survey response is deleted, the survey page responses are deleted
    @OneToMany(targetEntity = DbSurveyPageResponse.class, mappedBy = "surveyResponse", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Set<DbSurveyPageResponse> getSurveyPageResponses() {
        return pageResponses;
    }

    public void setSurveyPageResponses(Set<DbSurveyPageResponse> pageResponses) {
        this.pageResponses = pageResponses;
    }

    /**
     * Return a string representation of this class
     *
     * @return String - a string representation of this class
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyResponse:");
        sb.append(" id = ").append(getSurveyResponseId());
        
        if(getUser() != null){
            sb.append(", ").append(getUser());
        }else if (getSubject() != null) {
            sb.append(", ").append(getSubject());
        } else {
            sb.append(", ").append(getGlobalUser());
        }
        
        sb.append(", start time = ").append(getStartTime());
        sb.append(", end time = ").append(getEndTime());
        sb.append(", surveyContext = ").append(getSurveyContext());
        sb.append(", survey = ").append(getSurvey());
        sb.append(", domainSession = ").append(getDomainSession());
        sb.append(", survey page responses = {");
        for(DbSurveyPageResponse surveyPageResponse : getSurveyPageResponses()) {
            sb.append(surveyPageResponse).append(", ");
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }
}
