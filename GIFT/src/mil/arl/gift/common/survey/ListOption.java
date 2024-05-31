/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;

/**
 * An option of a option list
 *
 * @author jleonard
 */
public class ListOption implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private int id;

    private String text;

    private int optionListId;
    
    /** the index used to place this choice among other choices in the question  (smaller values come first) */
    private int sortKey;

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public ListOption() {
    }

    /**
     * Constructor
     *
     * @param id The ID of the list option
     * @param text The text of the list option
     * @param optionListId The ID of the option list this is contained in
     */
    public ListOption(int id, String text, int optionListId) {

        this.id = id;
        this.text = text;
        this.optionListId = optionListId;
    }

    /**
     * Constructor
     *
     * @param id The ID of the list option
     * @param text The text of the list option
     */
    public ListOption(int id, String text) {
        this.id = id;
        this.text = text;
    }

    /**
     * Gets the ID of the list option
     *
     * @return int The ID of the list option
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the list option
     *
     * @param id The ID of the list option
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the text of the list option
     *
     * @return String The text of the list option
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text of the list option
     *
     * @param text The text of the list option
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the ID of the option list this is in
     *
     * @return int The ID of the option list this is in
     */
    public int getOptionListId() {
        return optionListId;
    }

    /**
     * Sets the ID of the option list this is in
     *
     * @param optionListId The ID of the option list this
     * is in
     */
    public void setOptionListId(int optionListId) {
        this.optionListId = optionListId;
    }
    
    /**
     * Gets the sort key.
     * 
     * @return the index used to place this choice among other choices in the question  
     * (smaller values come first).  Default is 0.
     */
    public int getSortKey() {
        return sortKey;
    }

    /**
     * Set the sort key.
     * 
     * @param sortKey the index used to place this choice among other choices in the question  (smaller values come first)
     */
    public void setSortKey(int sortKey) {
        this.sortKey = sortKey;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        
        sb.append("id = ").append(getId());
        sb.append(", text = ").append(getText());
        sb.append(", sortKey = ").append(getSortKey());
        sb.append(", option list id = ").append(getOptionListId());
        
        return sb.toString();
    }
}
