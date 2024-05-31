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
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "surveyelementtype")
public class DbSurveyElementType {

    private int id;

    private String key;

    private String name;

    /**
     * Default Constructor
     */
    public DbSurveyElementType() {
    }

    /**
     * Class constructor
     *
     * @param key the unique key for a survey element
     */
    public DbSurveyElementType(String key) {

        this.key = key;
    }

    public DbSurveyElementType(String key, String name) {

        this.key = key;
        this.name = name;
    }

    @Id
    @Column(name = "typeId", nullable = false)
    @TableGenerator(name = "surveyElementTypeId", table = "surveyelementtypepktb", pkColumnName = "surveyElementTypekey", pkColumnValue = "surveyElementTypevalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "surveyElementTypeId")
    public int getId() {

        return id;
    }

    public void setId(int typeId) {

        this.id = typeId;
    }

    //can't be null
    @Column(name = "typeKey", nullable = false, length = 32, unique = true)
    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    @Column(nullable = false, unique = true)
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DbSurveyElementType:");
        sb.append("id = ").append(getId());
        sb.append(", key = ").append(getKey());
        sb.append(", name = ").append(getName());
        sb.append("]");

        return sb.toString();
    }
}
