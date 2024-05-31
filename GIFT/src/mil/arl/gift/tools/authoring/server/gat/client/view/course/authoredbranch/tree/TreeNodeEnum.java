/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree;

/**
 * An enumeration representing the various types of possible branch tree nodes
 * 
 * @author nroberts
 */
public enum TreeNodeEnum {

	/** An enumeration for the node representing the start of the branch. Generally, there should only every be one node with this type. */
	BRANCH_POINT("Authored Branch"),
	
	/** An enumeration for the point where a path begins */
	PATH_START("Path Start"),
	
	/** An enumeration for nodes representing path transitions. */
	TRANSITION("Course Object"),
	
	/** An enumeration for nodes representing path end points. */
	PATH_END("+"),
	
	/** An enumeration for nodes representing course end points. */
	COURSE_END("Course End");
	
	/** The display name corresponding to this type of node */
	private final String displayName;
	
	/** Creates a new node type with the given display name*/
	private TreeNodeEnum(String displayName) {
		this.displayName = displayName;
	}
		
    /**
     * Gets this node type's display name
     * 
     * @return the display name
     */
    public String getDisplayName() {
    	return displayName;
    }
}
