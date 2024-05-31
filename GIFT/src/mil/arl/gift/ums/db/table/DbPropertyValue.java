/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "propertyvalue")
public class DbPropertyValue {

    private int propertyValueId;

    private String value;

    /**
     * Default Constructor
     */
    public DbPropertyValue() {
    }

    /**
     * Class constructor
     *
     * @param value the value of the property
     */
    public DbPropertyValue(String value) {

        this.value = value;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "propertyValueId_PK")
    @TableGenerator(name = "propertyValueId", table = "propertyvaluepktb", pkColumnName = "propertyValuekey", pkColumnValue = "propertyValuevalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "propertyValueId")
    public int getPropertyValueId() {

        return propertyValueId;
    }

    public void setPropertyValueId(int propertyValueId) {

        this.propertyValueId = propertyValueId;
    }

    //can't be null
    @Column(length = 32000)
    public String getValue() {

        return value;
    }

    public void setValue(String value) {

        this.value = value;
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 11 * hash + Objects.hashCode(value);
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

        final DbPropertyValue other = (DbPropertyValue) obj;

        if (!Objects.equals(this.value, other.value)) {

            return false;
        }

        return true;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DbPropertyValue:");
        sb.append(" propertyValueId = ").append(getPropertyValueId());
        sb.append(", value = ").append(getValue());
        sb.append("]");

        return sb.toString();
    }
}
