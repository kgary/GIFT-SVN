/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.model.conversation;

/**
 * This class represents conversation tree node types.
 * 
 * @author bzahid
 */
public enum TreeNodeEnum {

	CHOICE_NODE("choice", "Choice"),
	MESSAGE_NODE("message", "Message"),
	QUESTION_NODE("question", "Question"),
	END_NODE("end", "END"),
	ADD_NODE("add", "ADD");
	
	private final String displayName;
	private final String name;
	
	private TreeNodeEnum(String name, String displayName) {
		this.name = name;
		this.displayName = displayName;
	}
		
    /**
     * Gets the display name of the node type.
     * 
     * @return A title case string that identifies the TreeNodeEnum.
     */
    public String getDisplayName() {
    	return displayName;
    }
    
    /**
     * Gets the name of the node type.
     * 
     * @return A string that identifies the TreeNodeEnum.
     */
    public String getName() {
    	return name;
    }
    
    /**
     * Identifies the TreeNodeEnum with the given displayName.
     * 
     * @param name the name or displayName of a TreeNodeEnum
     * @return TreeNodeEnum with the given name or displayName, or null if no matching TreeNodeEnum was found
     */
   	public static TreeNodeEnum fromName(String name) {
    	TreeNodeEnum [] treeNodeEnums = TreeNodeEnum.values();
        for(TreeNodeEnum treeNodeEnum : treeNodeEnums) {
        	if(treeNodeEnum.getName().equalsIgnoreCase(name)) {
        		return treeNodeEnum;
        	}
        }
        return null;
    }
}
