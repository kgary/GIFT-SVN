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
@Table(name = "propertykey")
public class DbPropertyKey {

    private int id;

    private String key;

    /**
     * Default Constructor
     */
    public DbPropertyKey() {
    }

    /**
     * Class constructor
     *
     * @param key the key to the property
     */
    public DbPropertyKey(String key) {

        this.key = key;
    }
    
    @Id
    @Column(name = "propertyKeyId", nullable = false)
    @TableGenerator(name = "propertyKeyId", table = "propertykeypktb", pkColumnName = "propertyKeyKey", pkColumnValue = "propertyKeyValue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "propertyKeyId")
    public int getId() {

        return id;
    }

    public void setId(int typeId) {

        this.id = typeId;
    }

    //can't be null
    @Column(name = "propertyKeyValue", nullable = false, length = 64, unique = true)
    public String getKey() {

        return key;
    }

    public void setKey(String key) {

        this.key = key;
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {

            return true;
        }

        if (obj instanceof DbPropertyKey) {

            DbPropertyKey propertyKey = (DbPropertyKey) obj;

            return getKey().equals(propertyKey.getKey());
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[DbPropertyKey:");
        sb.append("id = ").append(getId());
        sb.append(", key = ").append(getKey());
        sb.append("]");

        return sb.toString();
    }
}
