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
@Table(name = "questiontype")
public class DbQuestionType {

    private int id;

    private String key;

    private String name;

    /**
     * Default Constructor
     */
    public DbQuestionType() {
    }

    /**
     * Class constructor
     *
     * @param key the display name for the question type
     */
    public DbQuestionType(String key) {

        this.key = key;
    }

    public DbQuestionType(String key, String name) {

        this.key = key;
        this.name = name;
    }

    @Id
    @Column(name = "typeId", nullable = false)
    @TableGenerator(name = "questionTypeId", table = "questiontypepktb", pkColumnName = "questionTypekey", pkColumnValue = "questionTypevalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "questionTypeId")
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
        sb.append("[QuestionType:");
        sb.append("id = ").append(getId());
        sb.append(", key = ").append(getKey());
        sb.append(", name = ").append(getName());
        sb.append("]");

        return sb.toString();
    }
}
