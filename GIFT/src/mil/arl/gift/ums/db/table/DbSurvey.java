/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OrderBy;

@Entity
@Table(name = "survey")
public class DbSurvey {

    private int surveyId;

    private String name;

    private DbFolder folder;

    private Set<DbSurveyPage> surveyPages;

    private Set<DbSurveyContextSurvey> surveyContextSurveys;

    private Set<DbSurveyProperty> properties;
    
    private Set<String> visibleToUserNames = new HashSet<String>();
    
    private Set<String> editableToUserNames = new HashSet<String>();

    /**
     * Default Constructor
     */
    public DbSurvey() {
    }

    /**
     * Class constructor
     *
     * @param name the display name of the survey
     * @param folder an optional folder that survey can be associated with
     */
    public DbSurvey(String name, DbFolder folder) {

        this.name = name;
        this.folder = folder;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "surveyId_PK")
    @TableGenerator(name = "surveyId", table = "surveypktb", pkColumnName = "surveykey", pkColumnValue = "surveyvalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "surveyId")
    public int getSurveyId() {

        return surveyId;
    }

    public void setSurveyId(int surveyId) {

        this.surveyId = surveyId;
    }
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="SurveyVisibleToUserNames")
    @Column(name="UserName")
    public Set<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(Set<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="SurveyEditableToUserNames")
    @Column(name="UserName")
    public Set<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(Set<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}

    //can't be null
    @Column(nullable = false)
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    //if a survey is deleted, don't want folder to be deleted
    @ManyToOne
    @JoinColumn(name = "folderId_FK")
    public DbFolder getFolder() {

        return folder;
    }

    public void setFolder(DbFolder folder) {

        this.folder = folder;
    }

    //created bi-directional 
    //now when Survey is deleted, the survey pages are deleted
    //NOTE: if you fetch eager here and in SurveyQuestion.java, you get runtime exception - solution was to add LazyCollection
    @OneToMany(targetEntity = DbSurveyPage.class, mappedBy = "survey", cascade = CascadeType.ALL/*, fetch=FetchType.EAGER*/)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy(clause = "pageNumber")
    public Set<DbSurveyPage> getSurveyPages() {

        return surveyPages;
    }

    public void setSurveyPages(Set<DbSurveyPage> surveyPages) {

        this.surveyPages = surveyPages;
    }

    //created bi-directional 
    //now when Survey is deleted, the SurveyContextSurveys are deleted
    //NOTE: if you fetch eager here and in SurveyContextSurvey.java, you get runtime exception - solution was to add LazyCollection
    @OneToMany(targetEntity = DbSurveyContextSurvey.class, mappedBy = "survey", cascade = CascadeType.ALL/*, fetch=FetchType.EAGER*/)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<DbSurveyContextSurvey> getSurveyContextSurveys() {

        return surveyContextSurveys;
    }

    public void setSurveyContextSurveys(Set<DbSurveyContextSurvey> surveyContextSurveys) {

        this.surveyContextSurveys = surveyContextSurveys;
    }

    //created bi-directional 
    //now when the survey question is deleted, the survey question properties are deleted
    @OneToMany(mappedBy = "survey", targetEntity = DbSurveyProperty.class, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<DbSurveyProperty> getProperties() {

        return properties;
    }

    public void setProperties(Set<DbSurveyProperty> properties) {

        this.properties = properties;
    }
    
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;
        }

        if (obj instanceof DbSurvey) {

            DbSurvey survey = (DbSurvey) obj;

            return survey.getSurveyId() == this.getSurveyId();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.surveyId;
        return hash;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Survey:");
        sb.append(" surveyId = ").append(getSurveyId());
        sb.append(", name = ").append(getName());
        sb.append(", folder = ").append(getFolder());
        sb.append("]");

        return sb.toString();
    }
}
