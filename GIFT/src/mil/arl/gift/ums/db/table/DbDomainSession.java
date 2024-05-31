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
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="domainsession")
public class DbDomainSession {

    private int sessionId;
    
    /** depending on the user type, one of these should be populated */
    private DbUser user;
    private DbExperimentSubject subject;
    private DbGlobalUser globalUser;
    
    private DbEventFile eventFile;
    private Date startTime;
    private Date endTime;
    
    /** the runtime id of this domain, i.e. the location where the course is executed from 
     * e.g. mhoffman\2020-05-13_11-45-05\Urban Operation - Resource Delivery\Urban Operation - Resource Delivery.course.xml 
     */
    private String domain;
    
    /** identification of the source of this domain, i.e. the course workspace path 
     * e.g. mhoffman/test/test.course.xml  
     */
    private String domainSourceId;

    private Set<DbSensorFile> sensorFiles;
    private Set<DbSurveyResponse> surveyResponses;

    /**
     * Default constructor - required by Hibernate
     */
    public DbDomainSession(){
        
    }
    
    /**
     * Class constructor - should be used when the domain session is not for an experiment
     * 
     * @param user - the user associated with this session
     */
    public DbDomainSession(DbUser user) {
        this.user = user;
    }
    
    /**
     * Class constructor - should be used when the domain session is for an experiment
     * 
     * @param subject - the subject associated with this session
     */
    public DbDomainSession(DbExperimentSubject subject) {
        this.subject = subject;
    }
    
    /**
     * Class constructor - should be used when the domain session is for a global user.
     * 
     * @param globalUser - The global user associated with a session.
     */
    public DbDomainSession(DbGlobalUser globalUser) {
        this.globalUser = globalUser;
    }
    
    @Id
    @Column(name="sessionId_PK")
    @TableGenerator(name="sessionid", table="sessionpktb", pkColumnName="sessionkey", pkColumnValue="sessionvalue", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.TABLE, generator="sessionid")
    public int getSessionId() {
        return sessionId;
    }

    public void setSessionId(int sessionId) {
        this.sessionId = sessionId;
    }
    
    //if a domain session is deleted, don't want user to be deleted
    @ManyToOne
    @JoinColumn(name="userId_FK")
    public DbUser getUser() {
        return user;
    }
    public void setUser(DbUser user) {
        this.user = user;
    }
    
    //if a domain session is deleted, don't want the global user to be deleted
    @ManyToOne
    @JoinColumn(name="globalId_FK")
    public DbGlobalUser getGlobalUser() {
        return globalUser;
    }
    public void setGlobalUser(DbGlobalUser globalUser) {
        this.globalUser = globalUser;
    }
    
    //if a domain session is deleted, don't want subject to be deleted
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
    
    //if a domain session is deleted, the event file is deleted
    @OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    @JoinColumn(name="eventFieldId_FK")
    public DbEventFile getEventFile() {
        return eventFile;
    }
    public void setEventFile(DbEventFile eventFile) {
        this.eventFile = eventFile;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStartTime() {
        return startTime;
    }
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEndTime() {
        return endTime;
    }
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }
    
    public String getDomain() {
        return domain;
    }
    public void setDomain(String domain) {
        this.domain = domain;
    }
    
    public String getDomainSourceId() {
        return domainSourceId;
    }

    public void setDomainSourceId(String domainSourceId) {
        this.domainSourceId = domainSourceId;
    }

    //create bi-directional
    //if a domain session is deleted, delete the sensor files
    @OneToMany(targetEntity=DbSensorFile.class, mappedBy="domainSession", cascade=CascadeType.ALL, fetch=FetchType.EAGER)
    public Set<DbSensorFile> getSensorFiles() {
        return sensorFiles;
    }

    public void setSensorFiles(Set<DbSensorFile> sensorFiles) {
        this.sensorFiles = sensorFiles;
    }
    
    //create bi-directional
    //if a domain session is deleted, delete the survey response
    @OneToMany(targetEntity=DbSurveyResponse.class, mappedBy="domainSession", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    public Set<DbSurveyResponse> getSurveyResponses() {
        return surveyResponses;
    }

    public void setSurveyResponses(Set<DbSurveyResponse> surveyResponses) {
        this.surveyResponses = surveyResponses;
    }
    
    /**
     * Return a string representation of this class
     * 
     * @return String - a string representation of this class
     */
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[DomainSession:");
        sb.append(" session id = ").append(getSessionId());
        
        if(getUser() != null){
            sb.append(", ").append(getUser());
        }else if (getSubject() != null){
            sb.append(", ").append(getSubject());
        }else {
            sb.append(", ").append(getGlobalUser());
        }
        
        sb.append(", runtime = ").append(domain);
        sb.append(", source = ").append(domainSourceId);
        
        sb.append(", ").append(getEventFile());
        sb.append(", start time = ").append(getStartTime());
        sb.append(", end time = ").append(getEndTime());
        
        if(getSensorFiles() != null){
            sb.append(", # sensor files = ").append(getSensorFiles().size());
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    
}
