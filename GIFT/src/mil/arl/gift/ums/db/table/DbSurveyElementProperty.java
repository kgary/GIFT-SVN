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
@Table(name = "surveyelementproperty")
public class DbSurveyElementProperty implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private DbSurveyElement surveyElement;

    private DbPropertyKey propertyKey;

    private DbPropertyValue propertyValue;

    /**
     * Default Constructor
     */
    public DbSurveyElementProperty() {
    }

    /**
     * Class constructor
     *
     * @param surveyElement information about the survey element
     * @param propertyKey the key for the property
     * @param propertyValue the value of the property
     */
    public DbSurveyElementProperty(DbSurveyElement surveyElement, DbPropertyKey propertyKey, DbPropertyValue propertyValue) {

        this.surveyElement = surveyElement;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    //if a survey element property is deleted, don't want the survey element to be deleted
    @Id
    @ManyToOne
    @JoinColumn(name = "surveyElementId_FK")
    public DbSurveyElement getSurveyElement() {

        return surveyElement;
    }

    public void setSurveyElement(DbSurveyElement surveyElement) {

        this.surveyElement = surveyElement;
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

        if (obj instanceof DbSurveyElementProperty) {

            DbSurveyElementProperty property = (DbSurveyElementProperty) obj;

            return getSurveyElement().equals(property.getSurveyElement()) && getPropertyKey().equals(property.getPropertyKey());
        }

        return false;
    }

    @Override
    public int hashCode() {

        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.surveyElement);
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
        sb.append("[DbSurveyElementProperty:");
        sb.append(" questionId = ").append(getSurveyElement().getQuestionId());
        sb.append(", surveyPageId = ").append(getSurveyElement().getSurveyPage() != null ? getSurveyElement().getSurveyPage().getSurveyPageId() : null);
        sb.append(", propertyKey = ").append(getPropertyKey());
        sb.append(", propertyValue = ").append(getPropertyValue());
        sb.append("]");

        return sb.toString();
    }
}
