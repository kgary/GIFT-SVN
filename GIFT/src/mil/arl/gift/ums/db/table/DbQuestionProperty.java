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
@Table(name = "questionproperty")
public class DbQuestionProperty implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private DbQuestion question;

    private DbPropertyKey propertyKey;

    private DbQuestionPropertyValue propertyValue;

    /**
     * Default Constructor
     */
    public DbQuestionProperty() {
    }

    public DbQuestionProperty(DbQuestion question) {

        this.question = question;
    }

    /**
     * Class constructor
     *
     * @param question the question containing this property
     * @param propertyKey the key to the property
     * @param propertyValue the value of the property
     */
    public DbQuestionProperty(DbQuestion question, DbPropertyKey propertyKey, DbQuestionPropertyValue propertyValue) {

        this.question = question;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    //if a question property is deleted, don't want the question to be deleted
    @Id
    @ManyToOne
    @JoinColumn(name = "questionId_FK")
    public DbQuestion getQuestion() {

        return question;
    }

    public void setQuestion(DbQuestion question) {

        this.question = question;
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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "questionPropertyValueId_FK", nullable = false)
    public DbQuestionPropertyValue getPropertyValue() {

        return propertyValue;
    }

    public void setPropertyValue(DbQuestionPropertyValue propertyValue) {

        this.propertyValue = propertyValue;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            
            return true;
        }
        
        if(obj instanceof DbQuestionProperty) {
            
            DbQuestionProperty property = (DbQuestionProperty) obj;
            
            return getQuestion().equals(property.getQuestion()) && getPropertyKey().equals(property.getPropertyKey());
        }
        
        return false;
    }

    @Override
    public int hashCode() {
        
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.question);
        hash = 59 * hash + Objects.hashCode(this.propertyKey);
        return hash;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionProperty:");
        sb.append(", id = ").append(getQuestion().getQuestionId());
        sb.append(", key = ").append(getPropertyKey());
        sb.append(", value = ").append(getPropertyValue());
        sb.append("]");

        return sb.toString();
    }
}
