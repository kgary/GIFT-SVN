/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * This table contains information about subjects in experiments.  Each subject entry
 * can only be in one experiment.  A subject is therefore uniquely identified by
 * the experiment and subject ids together.
 * 
 * @author mhoffman
 *
 */
@Entity
@Table(name="experimentsubject")
public class DbExperimentSubject implements Serializable{
    
    @Embeddable
    public static class DbExperimentSubjectId implements Serializable{        

        private static final long serialVersionUID = -2485218604291463181L;

        //if a subject is deleted, don't want experiment to be deleted
        @ManyToOne
        @JoinColumn(name = "experimentId_FK", insertable = false, updatable = false)
        private DbDataCollection experiment;
        
        /** unique to the experiment instance, but not unique across all experiments */
        @Column(name="subjectId_PK")
        private Integer subjectId;
        
        public static final String EXPERIMENT_PROPERTY = "experimentsubject.experiment";
        public static final String SUBJECT_PROPERTY = "experimentsubject.subjectId";
        
        //required
        public DbExperimentSubjectId(){}
        
        public DbExperimentSubjectId(DbDataCollection experiment, Integer subjectId){
            this.experiment = experiment;
            this.subjectId = subjectId;
        }
        
        public DbDataCollection getExperiment() {
            return experiment;
        }

        public void setExperiment(DbDataCollection experiment) {
            this.experiment = experiment;
        }        
        
        public Integer getSubjectId(){
            return subjectId;
        }
        
        public void setSubjectId(Integer subjectId){
            this.subjectId = subjectId;
        }
        
        @Override
        public int hashCode() {
            int hash = 7;
            hash = 53 * hash + Objects.hashCode(this.subjectId);
            hash = 53 * hash + Objects.hashCode(this.experiment);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {

            if (obj == null) {

                return false;
            }

            if (getClass() != obj.getClass()) {

                return false;
            }

            final DbExperimentSubjectId other = (DbExperimentSubjectId) obj;

            if (!Objects.equals(this.experiment, other.getExperiment())) {

                return false;
            }

            if (this.subjectId != other.getSubjectId()) {

                return false;
            }

            return true;
        }
        
        @Override
        public String toString() {

            StringBuilder sb = new StringBuilder();
            sb.append("[DbExperimentSubjectId:");
            sb.append(" subjectId = ").append(getSubjectId());
            sb.append(", experiment = ").append(getExperiment());
            sb.append("]");

            return sb.toString();
        }
    }

    private static final long serialVersionUID = 8063720719692501214L;

    /** composite key */
    @EmbeddedId
    //@AttributeOverride(name="subjectId", column=@Column(name="SUBJECTID_PK"))
    private DbExperimentSubjectId experimentSubjectId;
    
    /** times defined when a user started the course and when the course was ended */
    private Date startTime;
    private Date endTime;    
    
    /** the domain session message log file name (e.g. domainSession7_uId1_2015-08-21_15-13-17.log) */
    private String messageLogFilename;
    
    //required
    public DbExperimentSubject(){
        
    }  
    
    public DbExperimentSubject(DbDataCollection experiment, Integer subjectId){
        DbExperimentSubjectId id = new DbExperimentSubjectId(experiment, subjectId);
        setExperimentSubjectId(id);
    }
    
    public void setExperimentSubjectId(DbExperimentSubjectId experimentSubjectId){
        this.experimentSubjectId = experimentSubjectId;
    }
    
    public DbExperimentSubjectId getExperimentSubjectId(){
        return experimentSubjectId;
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

    /**
     * Return the domain session message log file name for this subject.
     * This will be null if the UMS message logger never received a domain session message
     * with the experiment id.
     * 
     * @return the domain session message log file name for this subject.  Can be null if never set.
     */
    public String getMessageLogFilename() {
        return messageLogFilename;
    }

    public void setMessageLogFilename(String messageLogFilename) {
        this.messageLogFilename = messageLogFilename;
    }
    
    @Override
    public int hashCode() {
        return getExperimentSubjectId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {

            return false;
        }

        if (getClass() != obj.getClass()) {

            return false;
        }

        final DbExperimentSubject other = (DbExperimentSubject) obj;

        if (!Objects.equals(this.getExperimentSubjectId(), other.getExperimentSubjectId())) {

            return false;
        }

        return true;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DbExperimentSubject:");
        sb.append(" experimentSubjectId = ").append(getExperimentSubjectId());
        sb.append(", startTime = ").append(getStartTime());
        sb.append(", endTime = ").append(getEndTime());
        sb.append(", messageLogFile = ").append(getMessageLogFilename());
        sb.append("]");

        return sb.toString();
    }
}
