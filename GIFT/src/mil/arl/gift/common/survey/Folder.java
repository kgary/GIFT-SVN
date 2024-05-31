/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

public class Folder implements Serializable {

	private static final long serialVersionUID = 3580282183906040724L;

	private int id;
	
	private String name;
	
    private HashSet<String> visibleToUserNames = new HashSet<String>();
    
    private HashSet<String> editableToUserNames = new HashSet<String>();
    
    public Folder() {
    }

    /**
     * Constructor
     *
     * @param id The ID of the option list
     * @param name The name of the option list
     * @param visibleToUserNames User names that can see the option list
     * @param editableToUserNames User names that can edit the option list
     */
    public Folder(int id, String name, Collection<String> visibleToUserNames, Collection<String> editableToUserNames) {

        this.id = id;
        this.name = name;
        
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

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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
}
