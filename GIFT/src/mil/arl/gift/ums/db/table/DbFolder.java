/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ums.db.table;

import java.util.HashSet;
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
@Table(name = "folder")
public class DbFolder {

    private int folderId;

    private String name;
    
    private Set<String> visibleToUserNames = new HashSet<String>();
    
    private Set<String> editableToUserNames = new HashSet<String>();

    /**
     * Default Constructor
     */
    public DbFolder() {
    }

    /**
     * Class constructor
     *
     * @param name display name for the folder
     */
    public DbFolder(String name) {
        
        this.name = name;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "folderId_PK")
    @TableGenerator(name = "folderId", table = "folderpktb", pkColumnName = "folderkey", pkColumnValue = "foldervalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "folderId")
    public int getFolderId() {
        
        return folderId;
    }

    public void setFolderId(int folderId) {
        
        this.folderId = folderId;
    }
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="FolderVisibleToUserNames")
    @Column(name="UserName")
    public Set<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(Set<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="FolderEditableToUserNames")
    @Column(name="UserName")
    public Set<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(Set<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
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
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[Folder:");
        sb.append(" id = ").append(getFolderId());
        sb.append(", name = ").append(getName());
        sb.append("]");

        return sb.toString();
    }
}
