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
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.experiment.ExperimentUtil.ExperimentStatus;

/**
 * This table contains information about data collections.  Each data collection
 * has a type (experiment or lti).  The results of the data collection are stored in
 * distinct tables that are unique to each type.
 *  
 * If the type of data collection is an experiment, it has
 * a unique set of subjects and each subject entry in an experiment can only 
 * be in one experiment.
 * 
 * Note that due to legacy reasons, the db table is still called 'experiment'.  Ideally
 * the ums table would be renamed to match the updated functionality of the table.
 * 
 * @author mhoffman
 *
 */
@Entity
@Table(name="experiment")
public class DbDataCollection{

    /** the unique id of this data collection instance among all other instances (i.e. Experiments and LTI) */
    private String id;
    
    /** a user given, non-unique, name for the experiment */
    private String name;
    
    /** (optional) information about the experiment */
    private String description;
    
    /** the GIFT username of the author of the experiment */
    private String authorUsername;
    
    /** the current status of this data collection instance (e.g. Paused) */
    private ExperimentStatus status;
    
    /** The type of data collection data set (experiment, lti, etc.) */
    private DataSetType dataSetType;
    
    /** The source course id (optional, depends on the data set type) */
    private String sourceCourseId;
    
    /** The published date of the course */
    private Date publishedDate;
    
    /** 
     * the location of the course folder that contains the course.xml file for the user to run
     * this experiment instance 
     */
    private String courseFolder;
    
    /**
     * the url given to participants to take the experiment, i.e. run the course
     */
    private String url;
    
    /**
     * Only one of the following sets should be used based on the dataSetType of the experiment.
     * If the experiment is an 'experiment' type, then the subjects set has the results of the experiment.
     * If the experiment is an 'lti' typ, then the ltiResults set has the results of the experiment.
     */
    private Set<DbExperimentSubject> subjects;
    private Set<DbDataCollectionResultsLti> ltiResults;
    
    /**
     *  the next subject id to use for the next data collection participant.
     * Subject ids are unique within a data collection instance.
     */
    //TODO: ideally this would be managed in another table in order to use a table's auto-increment logic on save    
    private Integer subjectNextId = 1;
    
    /** the permissions assigned to this data collection instance for sharing purposes */
    private Set<DbDataCollectionPermission> permissions;
    
    /**
     * Default constructor
     */
    public DbDataCollection(){
        
    }
    
    /**
     * Set the unique id of this data collection instance among all other instances (i.e. Experiments and LTI)
     * 
     * @param experimentId the data collection unique id
     */
    public void setId(String experimentId){
        this.id = experimentId;
    }
    
    /**
     * Return the unique id of this data collection instance among all other instances (i.e. Experiments and LTI)
     * 
     * @return the unique id of this data collection 
     */
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "experimentId_PK")
    public String getId(){
        return id;
    }

    /**
     * The GIFT user who created this instance.
     * 
     * @return shouldn't be null
     */
    @Column(nullable = false, unique = false)
    public String getAuthorUsername() {
        return authorUsername;
    }

    /**
     * Set the GIFT user who created this instance.
     * 
     * @param authorUsername shouldn't be null
     */
    public void setAuthorUsername(String authorUsername) {
        this.authorUsername = authorUsername;
    }

    /**
     * Return the current status of this data collection instance.
     * 
     * @return the current status
     */
    @Column(name="status") 
    @Enumerated(EnumType.STRING) 
    public ExperimentStatus getStatus() {
        return status;
    }

    /**
     * Set the current status of this data collection instance.
     * 
     * @param status shouldn't be null
     */
    public void setStatus(ExperimentStatus status) {
        this.status = status;
    }

    /**
     * Return the GIFT relative path to the folder where the course is that should
     * be ran for this data collection experience.
     * 
     * @return shouldn't be null
     */
    public String getCourseFolder() {
        return courseFolder;
    }

    /**
     * Set the GIFT relative path to the folder where the course is that should
     * be ran for this data collection experience.
     * @param experimentCourseFolder shouldn't be null.
     */
    public void setCourseFolder(String experimentCourseFolder) {
        this.courseFolder = experimentCourseFolder;
    }

    /**
     * Return the authored name of this data collection.
     * 
     * @return the authored name, won't be null
     */
    public String getName() {
        return name;
    }

    /**
     * Set the authored name of this data collection instance.
     * 
     * @param experimentName the authored name, can't be null.
     */
    @Column(nullable = false, unique = false)
    public void setName(String experimentName) {
        this.name = experimentName;
    }

    /**
     * Return the optional, authored description of this data collection instance.
     *  
     * @return can be null or empty
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the optional, authored description of this data collection instance.
     * 
     * @param description a description of this instance.  Can be null or empty.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Return the published date of this data collection instance
     * 
     * @return the published date of this data collection 
     */
    @Column(name = "publishedDate")
    public Date getPublishedDate(){
        return publishedDate;
    }
    
    /**
    * Set the published date of this data collection instance.
    * 
    * @param publishedDate the published date of this instance. Can be null or empty.
    */
   public void setPublishedDate(Date publishedDate) {
       this.publishedDate = publishedDate;
   }

    /**
     * Return the data collected for an experiment data collection instance.
     * 
     * @return the data collection for an experiment type data collection instance.  Will be empty for LTI instances.
     */
    //created bi-directional 
    //now when Experiment is deleted, the subjects are deleted
    @OneToMany(targetEntity=DbExperimentSubject.class, mappedBy="experimentSubjectId.experiment", cascade=CascadeType.ALL, /*fetch=FetchType.EAGER,*/ orphanRemoval=true)
    @LazyCollection(LazyCollectionOption.TRUE)
    public Set<DbExperimentSubject> getSubjects() {
        return subjects;
    }

    /**
     * Set the data collected for an Experiment data collection instance.
     * 
     * @param subjects the results set containing the set of results to be updated.
     */
    public void setSubjects(Set<DbExperimentSubject> subjects) {
        this.subjects = subjects;
    }
    
    /**
     * Return the data collected for an LTI data collection instance.
     * 
     * @return any data collected.  Can be empty.
     */
    //created bi-directional 
    //now when Experiment is deleted, the data collection results for lti are deleted
    @OneToMany(targetEntity=DbDataCollectionResultsLti.class, mappedBy="dataSetId", cascade=CascadeType.ALL, /*fetch=FetchType.EAGER,*/ orphanRemoval=true)
    @LazyCollection(LazyCollectionOption.TRUE)
    public Set<DbDataCollectionResultsLti> getDataCollectionResultsLti() {
        return this.ltiResults;
    }

    /**
     * Sets the data collection results for LTI type of experiment.  
     * 
     * @param ltiResults The result set containing the set of results to be updated.
     */
    public void setDataCollectionResultsLti(Set<DbDataCollectionResultsLti> ltiResults) {
        this.ltiResults = ltiResults;
    }
    
    /**
     * Return the permissions assigned to this data collection instance
     * 
     * @return can be null or empty if permission where never set because this instance pre-date permissions
     */
    //created bi-directional 
    //now when Experiment is deleted, the data collection permissions are deleted
    @OneToMany(targetEntity=DbDataCollectionPermission.class, mappedBy="dataCollectionId", cascade=CascadeType.ALL, fetch=FetchType.EAGER, orphanRemoval=true)
    public Set<DbDataCollectionPermission> getPermissions(){
        return this.permissions;
    }
    
    /**
     * Set the permissions assigned to this data collection instance
     * @param permissions shouldn't be null or empty.
     */
    public void setPermissions(Set<DbDataCollectionPermission> permissions){
        this.permissions = permissions;
    }

    /**
     * Return the URL for participants to use to reach the data collection experience.
     * 
     * @return the address for participant to use
     */
    public String getUrl() {
        return url;
    }

    /**
     * Set the URL for participants to use to reach the data collection experience.
     * 
     * @param url the address for participant to use
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * Return the next subject id to use for the next data collection participant.
     * Subject ids are unique within a data collection instance.
     * 
     * @return the next subject id
     */
    public Integer getSubjectNextId() {
        return subjectNextId;
    }

    /**
     * Set the next subject id to use for the next data collection participant.
     * Subject ids are unique within a data collection instance.
     * 
     * @param subjectNextId the next subject id.
     */
    public void setSubjectNextId(Integer subjectNextId) {
        this.subjectNextId = subjectNextId;
    }
    
    /**
     * The type of data collection being conducted (e.g. Experiment, LTI)
     * @return the type of data collection.  Can't be null.
     */
    @Column(name="dataSetType") 
    @Enumerated(EnumType.STRING) 
    public DataSetType getDataSetType() {
        return dataSetType;
    }

    /**
     * The type of data collection being conducted (e.g. Experiment, LTI)
     * 
     * @param dataSetType the data collection type
     */
    public void setDataSetType(DataSetType dataSetType) {
        this.dataSetType = dataSetType;
    }
    
    /**
     * Method to determine if the experiment data set type matches the compared type.
     * @param dataSetType the type to compare the experiment data set type with
     * @return true if the experiment data set type matches, false otherwise.
     */
    public boolean isDataSetType(DataSetType dataSetType) {
        return (this.dataSetType.compareTo(dataSetType) == 0);
    }

    /**
     * The source course id (optional, depends on the data set type)
     * 
     * @return the sourceCourseId.  Can be null, e.g. for experiments.
     */
    public String getSourceCourseId() {
        return sourceCourseId;
    }

    /**
     * The source course id (optional, depends on the data set type)
     * 
     * @param sourceCourseId the course id of the course being used for this data collection instance.
     */
    public void setSourceCourseId(String sourceCourseId) {
        this.sourceCourseId = sourceCourseId;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
    
    @Override
    public boolean equals(Object obj) {

        if (obj == null) {

            return false;
        }

        if (getClass() != obj.getClass()) {

            return false;
        }
     
        DbDataCollection other = (DbDataCollection)obj;
        
        return other.getId() != null && other.getId().equals(this.getId());
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DbDataCollection:");
        sb.append(" id = ").append(getId());
        sb.append(" dataSetType = ").append(getDataSetType());
        sb.append(" sourceCourseId = ").append(getSourceCourseId());
        sb.append(", name = ").append(getName());
        sb.append(", author = ").append(getAuthorUsername());
        sb.append(", status = ").append(getStatus());
        sb.append(", url = ").append(getUrl());
        sb.append(", nextSubjectId = ").append(getSubjectNextId());
        sb.append(", courseFolder = ").append(getCourseFolder());
        sb.append(", permissions = {");
        if(permissions != null){
            for(DbDataCollectionPermission permission : permissions){
                sb.append("\n").append(permission);
            }
        }
        sb.append("}");
        sb.append(",\n description = ").append(getDescription());
        sb.append("]");

        return sb.toString();
    }
}
