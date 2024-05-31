/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
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
@Table(name = "surveypage")
public class DbSurveyPage {

    private int surveyPageId;

    private String name;

    private DbSurvey survey;

    private int pageNumber;

    private Set<DbSurveyElement> surveyElements;

    private Set<DbSurveyPageProperty> properties;

    /**
     * Default Constructor
     */
    public DbSurveyPage() {
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "surveyPageId_PK")
    @TableGenerator(name = "surveyPageId", table = "surveypagepktb", pkColumnName = "surveyPagekey", pkColumnValue = "surveyPagevalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "surveyPageId")
    public int getSurveyPageId() {

        return surveyPageId;
    }

    public void setSurveyPageId(int surveyPageId) {

        this.surveyPageId = surveyPageId;
    }

    //can't be null
    @Column(nullable = false)
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    //if a survey page is deleted, don't want survey to be deleted
    @ManyToOne
    @JoinColumn(name = "surveyId_FK")
    public DbSurvey getSurvey() {

        return survey;
    }

    public void setSurvey(DbSurvey survey) {

        this.survey = survey;
    }

    public int getPageNumber() {

        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {

        this.pageNumber = pageNumber;
    }

    //created bi-directional 
    //now when SurveyPage is deleted, the survey elements are deleted
    //NOTE: if you fetch eager here and in Survey.java, you get runtime exception - solution was to add LazyCollection
    @OneToMany(targetEntity = DbSurveyElement.class, mappedBy = "surveyPage", cascade = CascadeType.ALL/*, fetch=FetchType.EAGER*/)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy(clause = "elementNumber")
    public Set<DbSurveyElement> getSurveyElements() {

        return surveyElements;
    }

    public void setSurveyElements(Set<DbSurveyElement> surveyElements) {

        this.surveyElements = surveyElements;
    }

    //created bi-directional 
    //now when the survey page is deleted, the survey page properties are deleted
    @OneToMany(mappedBy = "surveyPage", targetEntity = DbSurveyPageProperty.class, cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    public Set<DbSurveyPageProperty> getProperties() {

        return properties;
    }

    public void setProperties(Set<DbSurveyPageProperty> properties) {

        this.properties = properties;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;
        }

        if (obj instanceof DbSurveyPage) {

            DbSurveyPage surveyPage = (DbSurveyPage) obj;

            return surveyPage.getSurveyPageId() != 0 && surveyPage.getSurveyPageId() == this.getSurveyPageId();
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + this.surveyPageId;
        return hash;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DbSurveyPage:");
        sb.append(" surveyPageId = ").append(getSurveyPageId());
        sb.append(", name = ").append(getName());
        sb.append(", survey = ").append(getSurvey());
        sb.append(", pageNumber = ").append(getPageNumber());
        sb.append("]");

        return sb.toString();
    }
}
