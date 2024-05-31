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

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.OrderBy;

@Entity
@Table(name = "optionlist")
public class DbOptionList {

    private int optionListId;

    private String name;
    
    private boolean isShared;

    private Set<DbListOption> listOptions;
    
    private Set<String> visibleToUserNames  = new HashSet<String>();
    
    private Set<String> editableToUserNames  = new HashSet<String>();

    /**
     * Default Constructor
     */
    public DbOptionList() {
    }

    /**
     * Class constructor
     *
     * @param name the display name of an options list
     */
    public DbOptionList(String name) {
        
        this.name = name;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "optionListId_PK")
    @TableGenerator(name = "optionListId", table = "optionlistpktb", pkColumnName = "optionListkey", pkColumnValue = "optionListvalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "optionListId")
    public int getOptionListId() {
        
        return optionListId;
    }

    public void setOptionListId(int optionListId) {
        
        this.optionListId = optionListId;
    }
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="OptionListVisibleToUserNames")
    @Column(name="UserName")
    public Set<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(Set<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}
    
    @ElementCollection
    @LazyCollection(LazyCollectionOption.FALSE)
    @CollectionTable(name="OptionListEditableToUserNames")
    @Column(name="UserName")
    public Set<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(Set<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}

    //can't be null
    @Column(nullable = false)
    public String getName() {
        
        return name;
    }

    public void setName(String name) {
        
        this.name = name;
    }
    
    //can't be null
    @Column(nullable = false)
    public boolean getIsShared() {
        
        return isShared;
    }

    public void setIsShared(boolean isShared) {
        
        this.isShared = isShared;
    }

    //created bi-directional 
    //now when option list is deleted, the list options are deleted
    @OneToMany(targetEntity = DbListOption.class, mappedBy = "optionList", cascade = CascadeType.ALL/* , fetch=FetchType.LAZY */)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy(clause = "sortKey")
    public Set<DbListOption> getListOptions() {
        
        return listOptions;
    }

    public void setListOptions(Set<DbListOption> listOptions) {
        
        this.listOptions = listOptions;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            
            return true;
        }
        
        if(obj instanceof DbOptionList) {
            
            DbOptionList optionList = (DbOptionList) obj;
            
            return optionList.getOptionListId() != 0 && optionList.getOptionListId() == this.getOptionListId();
        }
        
        return false;
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 61 * hash + this.optionListId;
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
        sb.append("[OptionList:");
        sb.append(" optionListId = ").append(getOptionListId());
        sb.append(", name = ").append(getName());
        sb.append(", isShared = ").append(getIsShared());
        sb.append(", listOptions = {");
        
        if(getListOptions() != null) {
	        for (DbListOption listOption : getListOptions()) {
	
	            sb.append(listOption).append(", ");
	        }
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
