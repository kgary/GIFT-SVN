/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "surveypageproperty")
public class DbSurveyPageProperty implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private DbSurveyPage surveyPage;

    private DbPropertyKey propertyKey;

    private DbPropertyValue propertyValue;

    /**
     * Default Constructor
     */
    public DbSurveyPageProperty() {
    }

    /**
     * Class constructor
     *
     * @param surveyPage information about the survey page
     * @param propertyKey the key to the property
     * @param propertyValue the property value
     */
    public DbSurveyPageProperty(DbSurveyPage surveyPage, DbPropertyKey propertyKey, DbPropertyValue propertyValue) {

        this.surveyPage = surveyPage;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    //if a survey page property is deleted, don't want the survey page to be deleted
    @Id
    @ManyToOne
    @JoinColumn(name = "surveyPageId_FK")
    public DbSurveyPage getSurveyPage() {

        return surveyPage;
    }

    public void setSurveyPage(DbSurveyPage surveyPage) {

        this.surveyPage = surveyPage;
    }

    @Id
    @ManyToOne
    @JoinColumn(name = "propertyKeyId_FK")
    public DbPropertyKey getPropertyKey() {

        return propertyKey;
    }

    public void setPropertyKey(DbPropertyKey propertyKey) {

        this.propertyKey = propertyKey;
    }

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "propertyValueId_FK", nullable = false)
    public DbPropertyValue getPropertyValue() {

        return propertyValue;
    }

    public void setPropertyValue(DbPropertyValue propertyValue) {

        this.propertyValue = propertyValue;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;
        }

        if (obj instanceof DbSurveyPageProperty) {

            DbSurveyPageProperty property = (DbSurveyPageProperty) obj;

            return getSurveyPage().equals(property.getSurveyPage()) && getPropertyKey().equals(property.getPropertyKey());
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.surveyPage);
        hash = 59 * hash + Objects.hashCode(this.propertyKey);
        return hash;
    }

    /**
     * Return a string representation of this class
     *
     * @return String - a string representation of this class
     */
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyPageProperty:");
        sb.append(", surveyPageId = ").append(getSurveyPage().getSurveyPageId());
        sb.append(", propertyKey = ").append(getPropertyKey());
        sb.append(", propertyValue = ").append(getPropertyValue());
        sb.append("]");

        return sb.toString();
    }
}
