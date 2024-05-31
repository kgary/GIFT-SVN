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
@Table(name = "surveyproperty")
public class DbSurveyProperty implements Serializable {

    private static final long serialVersionUID = 1L;

    private DbSurvey survey;

    private DbPropertyKey propertyKey;

    private DbPropertyValue propertyValue;

    /**
     * Default Constructor
     */
    public DbSurveyProperty() {
    }

    /**
     * Class constructor
     *
     * @param survey the survey with these properties
     * @param propertyKey the identifier of the property
     * @param propertyValue the value of the property
     */
    public DbSurveyProperty(DbSurvey survey, DbPropertyKey propertyKey, DbPropertyValue propertyValue) {

        this.survey = survey;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    //if a survey property is deleted, don't want the survey to be deleted
    @Id
    @ManyToOne
    @JoinColumn(name = "surveyId_FK")
    public DbSurvey getSurvey() {

        return survey;
    }

    public void setSurvey(DbSurvey survey) {

        this.survey = survey;
    }

    //can't be null
    @Id
    @ManyToOne
    @JoinColumn(name = "propertyKeyId_FK")
    public DbPropertyKey getPropertyKey() {

        return propertyKey;
    }

    public void setPropertyKey(DbPropertyKey propertyKey) {

        this.propertyKey = propertyKey;
    }

    @Id
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.REFRESH})
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

        if (obj instanceof DbSurveyProperty) {

            DbSurveyProperty property = (DbSurveyProperty) obj;

            return getSurvey().equals(property.getSurvey()) && getPropertyKey().equals(property.getPropertyKey());
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.survey);
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
        sb.append("[SurveyProperty:");
        sb.append(", surveyId = ").append(getSurvey().getSurveyId());
        sb.append(", propertyKey = ").append(getPropertyKey());
        sb.append(", propertyValue = ").append(getPropertyValue());
        sb.append("]");

        return sb.toString();
    }
}
