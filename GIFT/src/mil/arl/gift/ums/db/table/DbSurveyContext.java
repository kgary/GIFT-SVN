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
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name="surveycontext")
public class DbSurveyContext {

	private int surveyContextId;
	private String name;
	
	/** a newer attribute that indicates when the survey context was last modified (this includes its surveys).  Will be null for pre-existing survey contexts */
	private Date lastModified;
	
	private Set<DbSurveyContextSurvey> surveyContextSurveys;
	private Set<DbSurveyResponse> surveyResponses;
    
    private Set<String> visibleToUserNames;
    private Set<String> editableToUserNames;
	
	/**
	 * Default Constructor
	 */
	public DbSurveyContext(){
		
	}
	
	/**
	 * Class constructor 
	 * 
	 * @param name the display name of the survey context
	 */
	public DbSurveyContext(String name) {
		this.name = name;
	}
	
	//primary key, auto-generated sequentially
	@Id
	@Column(name="surveyContextId_PK")
	@TableGenerator(name="surveyContextId", table="surveycontextpktb", pkColumnName="surveyContextkey", pkColumnValue="surveyContextvalue", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.TABLE, generator="surveyContextId")
	public int getSurveyContextId() {
		return surveyContextId;
	}
	public void setSurveyContextId(int surveyContextId) {
		this.surveyContextId = surveyContextId;
	}
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="SurveyContextVisibleToUserNames")
    @Column(name="UserName")
    public Set<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(Set<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="SurveyContextEditableToUserNames")
    @Column(name="UserName")
    public Set<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(Set<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}
	
	//can't be null
	@Column(nullable=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	//created bi-directional 
    //now when SurveyContext is deleted, the SurveyContextSurveys are deleted
	// IMPORTANT - This is now to lazy loading, because the fetch of this is very expensive when scanning across a 
	// survey context with many surveys associated with it.
	// To fetch this data after retrieving the object from the database, look at using:  SurveyContextUtil.getSurveyContextEager() methods.
    @OneToMany(targetEntity=DbSurveyContextSurvey.class, mappedBy="surveyContext", cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.LAZY)
    public Set<DbSurveyContextSurvey> getSurveyContextSurveys() {
        return surveyContextSurveys;
    }
    public void setSurveyContextSurveys(Set<DbSurveyContextSurvey> surveyContextSurveys) {
        this.surveyContextSurveys = surveyContextSurveys;
    }

    //created bi-directional 
    //when the survey context is deleted, the survey responses are deleted
    @OneToMany(targetEntity=DbSurveyResponse.class, mappedBy="surveyContext", cascade=CascadeType.ALL, fetch=FetchType.LAZY)
    public Set<DbSurveyResponse> getSurveyResponses() {
        return surveyResponses;
    }
    public void setSurveyResponses(Set<DbSurveyResponse> surveyResponses) {
        this.surveyResponses = surveyResponses;
    }
    
    /**
     * Return the last modification date of this survey context, including its surveys.
     * 
     * @return will be null for pre-existing survey contexts that haven't been saved using the GAT
     */
    @Column(name = "lastModified", columnDefinition="DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date time) {
        this.lastModified = time;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + this.surveyContextId;
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

        final DbSurveyContext other = (DbSurveyContext) obj;

        if (this.surveyContextId != other.getSurveyContextId()) {

            return false;
        }

        return true;
    }

	/**
	 * Return a string representation of this class
	 * 
	 * @return String - a string representation of this class
	 */
    @Override
	public String toString(){
		
	    StringBuilder sb = new StringBuilder();
	    sb.append("[SurveyContext:");
	    sb.append(" id = ").append(getSurveyContextId());
	    sb.append(", name = ").append(getName());
	    sb.append(", lastModified = ").append(getLastModified());
	    sb.append("]");
		
		return sb.toString();
	}
}
