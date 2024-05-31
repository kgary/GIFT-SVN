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
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "listoption", uniqueConstraints = @UniqueConstraint(columnNames = {"optionListId_FK", "sortKey"}))
public class DbListOption {

    private int listOptionId;

    private String text;

    private DbOptionList optionList;

    /** the index used to place this choice among other choices in the question  (smaller values come first) */
    private int sortKey;

    /**
     * Default Constructor
     */
    public DbListOption() {
    }

    /**
     * Class constructor
     *
     * @param text the content of a list option
     * @param optionList the list this option is associated with
     * @param sortKey key used to sort this option in the list
     */
    public DbListOption(String text, DbOptionList optionList, int sortKey) {
        this.text = text;
        this.optionList = optionList;
        this.sortKey = sortKey;
    }

    //primary key, auto-generated sequentially
    @Id
    @Column(name = "listOptionId_PK")
    @TableGenerator(name = "listOptionId", table = "listoptionpktb", pkColumnName = "listOptionkey", pkColumnValue = "listOptionvalue", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "listOptionId")
    public int getListOptionId() {
        return listOptionId;
    }

    public void setListOptionId(int listOptionId) {
        this.listOptionId = listOptionId;
    }

    //can't be null
    @Column(nullable = false)
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    //if a list option is deleted, don't want the option list to be deleted
    @ManyToOne
    @JoinColumn(name = "optionListId_FK", nullable = false)
    public DbOptionList getOptionList() {
        return optionList;
    }

    public void setOptionList(DbOptionList optionList) {
        this.optionList = optionList;
    }

    /**
     * Get the sort key.
     * @return the index used to place this choice among other choices in the question
     * (smaller values come first). Default to 0.
     */
    public int getSortKey() {
        return sortKey;
    }

    /**
     * Set the sort key.
     * @param sortKey the index used to place this choice among other choices in the question  (smaller values come first)
     */
    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if(this == obj) {
            
            return true;
        }
        
        if(obj instanceof DbListOption) {
            
            DbListOption listOption = (DbListOption) obj;
            
            return listOption.getListOptionId() != 0 && listOption.getListOptionId() == getListOptionId();
        }
        
        return false;
    }

    @Override
    public int hashCode() {

        int hash = 7;
        hash = 53 * hash + this.listOptionId;
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
        sb.append("[ListOption:");
        sb.append(" listOptionId = ").append(getListOptionId());
        sb.append(", text = ").append(getText());
        sb.append(", optionListId = ").append(getOptionList() != null ? getOptionList().getOptionListId() : null);
        sb.append(", sortKey = ").append(getSortKey());
        sb.append("]");

        return sb.toString();
    }
}
