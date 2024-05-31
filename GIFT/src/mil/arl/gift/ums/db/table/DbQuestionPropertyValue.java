/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "questionpropertyvalue")
public class DbQuestionPropertyValue {

    private int propertyId;

    private String stringValue;

    private DbOptionList optionListValue;

    /**
     * Default Constructor
     */
    public DbQuestionPropertyValue() {
    }

    /**
     * Class constructor
     *
     * @param value the value for the property
     */
    public DbQuestionPropertyValue(String value) {

        this.stringValue = value;
    }
    
    public DbQuestionPropertyValue(DbOptionList value) {

        this.optionListValue = value;
    }
    
    public DbQuestionPropertyValue(String stringValue, DbOptionList optionListValue) {

        this.stringValue = stringValue;
        this.optionListValue = optionListValue;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "questionPropertyValueId_PK")
    @TableGenerator(name = "questionPropertyValueId", table = "questionpropertyvaluepktb", pkColumnName = "questionPropertyValueKey", pkColumnValue = "questionPropertyValueValue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "questionPropertyValueId")
    public int getId() {

        return propertyId;
    }

    public void setId(int propertyId) {

        this.propertyId = propertyId;
    }

    @Column(length = 32000)
    public String getStringValue() {

        return stringValue;
    }

    public void setStringValue(String stringValue) {

        this.stringValue = stringValue;
    }

    @ManyToOne
    @JoinColumn(name = "optionListId_FK")
    public DbOptionList getOptionListValue() {

        return optionListValue;
    }

    public void setOptionListValue(DbOptionList optionListValue) {

        this.optionListValue = optionListValue;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionPropertyValue:");
        sb.append(" id = ").append(getId());
        sb.append(", string = ").append(getStringValue());
        sb.append(", optionList = ").append(getOptionListValue());
        sb.append("]");

        return sb.toString();
    }
}
