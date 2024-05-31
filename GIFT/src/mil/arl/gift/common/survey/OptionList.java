/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * A list of options
 *
 * @author jleonard
 */
public class OptionList implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final String DEFAULT_NAME = "Custom Answer Set";

    private int id;

    private String name = DEFAULT_NAME;
    
    private boolean isShared;

    private List<ListOption> listOptions = new ArrayList<ListOption>();
    
    private HashSet<String> visibleToUserNames = new HashSet<String>();
    
    private HashSet<String> editableToUserNames = new HashSet<String>();

    /**
     * Default Constructor
     *
     * Required by IsSerializable to exist and be public
     */
    public OptionList() {
    }

    /**
     * Constructor
     *
     * @param id The ID of the option list
     * @param name The name of the option list.  If null or empty the default name will be used.
     * @param isShared If the option list is shared across multiple questions
     * @param listOptions The options this list has
     * @param visibleToUserNames User names that can see the option list
     * @param editableToUserNames User names that can edit the option list
     */
    public OptionList(int id, String name, boolean isShared, List<ListOption> listOptions, Collection<String> visibleToUserNames, Collection<String> editableToUserNames) {

        if(name != null && !name.isEmpty()){
            this.name = name;
        }
        
        this.id = id;
        this.isShared = isShared;
        this.listOptions = listOptions;
        
        if(visibleToUserNames != null) {
        	this.visibleToUserNames.addAll(visibleToUserNames);
        }
        
        if(editableToUserNames != null) {
        	this.editableToUserNames.addAll(editableToUserNames);
        }
        
    }

    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the option list
     *
     * @param id The ID of the option list
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the option list
     *
     * @return String The name of the option list.  Won't be null.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the option list
     *
     * @param name The name of the option list.  If null or empty the default name will be used.
     */
    public void setName(String name) {
        
        if(name != null && !name.isEmpty()){
            this.name = name;
        }else{
            this.name = DEFAULT_NAME;
        }
        
    }
    
    /**
     * Gets if the option list is shared across questions
     * 
     * @return boolean If the option list is shared across questions
     */
    public boolean getIsShared() {
        
        return isShared;
    }
    
    /**
     * Sets if the option list is shared across questions
     *
     * @param isShared If the option list is shared across questions
     */
    public void setIsShared(boolean isShared) {

        this.isShared = isShared;
    }

    /**
     * Gets the options of this list
     *
     * @return ArrayList<GwtListOption> The options of this list
     */
    public List<ListOption> getListOptions() {
        return listOptions;
    }

    /**
     * Sets the options of this list
     *
     * @param listOptions The options of this list
     */
    public void setListOptions(List<ListOption> listOptions) {
        this.listOptions = listOptions;
    }

    public HashSet<String> getVisibleToUserNames() {
		return visibleToUserNames;
	}

	public void setVisibleToUserNames(HashSet<String> visibleToUserNames) {
		this.visibleToUserNames = visibleToUserNames;
	}

	public HashSet<String> getEditableToUserNames() {
		return editableToUserNames;
	}

	public void setEditableToUserNames(HashSet<String> editableToUserNames) {
		this.editableToUserNames = editableToUserNames;
	}

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[OptionList: ");
        sb.append("id = ").append(getId());
        sb.append(", name = ").append(getName());
        sb.append(", isShared = ").append(getIsShared());

        sb.append(", list options = {");
        for (ListOption data : getListOptions()) {
            sb.append("{").append(data).append("}, ");
        }
        sb.append("}");

        sb.append("]");

        return sb.toString();
    }
}
