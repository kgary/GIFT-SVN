/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

@Entity
@Table(name = "category")
public class DbCategory {

    private int categoryId;

    private String name;
    
    private Set<String> visibleToUserNames;
    
    private Set<String> editableToUserNames;

    /**
     * Default Constructor
     */
    public DbCategory() {
    }

    /**
     * Class constructor
     *
     * @param name display name of the category
     */
    public DbCategory(String name) {
    	this.name = name;
    }
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="CategoryVisibleToUserNames")
    @Column(name="UserName")
    public Set<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(Set<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="CategoryEditableToUserNames")
    @Column(name="UserName")
    public Set<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(Set<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}

    /**
     * Class constructor
     *
     * @param questionCategoryId the unique id of a question category
     * @param name display name of the category
     */
    public DbCategory(int questionCategoryId, String name) {
        this.name = name;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "categoryId_PK")
    @TableGenerator(name = "categoryId", table = "categorypktb", pkColumnName = "categoryKey", pkColumnValue = "categoryValue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "categoryId")
    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int questionCategoryId) {
        this.categoryId = questionCategoryId;
    }

    //can't be null
    @Column(nullable = false, unique = true)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            
            return false;
        }
        
        final DbCategory other = (DbCategory) obj;
        
        if (this.categoryId != other.getCategoryId()) {
            
            return false;
        }
        
        return true;
    }

    @Override
    public int hashCode() {
        
        int hash = 7;        
        hash = 89 * hash + this.categoryId;        
        return hash;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Catergory:");
        sb.append(" id = ").append(getCategoryId());
        sb.append(", name = ").append(getName());
        sb.append("]");

        return sb.toString();
    }
}
