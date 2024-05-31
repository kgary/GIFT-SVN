/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.tree;

/**
 * An enumeration representing the various types of possible course tree nodes
 * 
 * @author nroberts
 */
public enum TreeNodeEnum {
	
	/** An enumeration for nodes representing course transitions. */
	TRANSITION("Course Object"),
	
	/** An enumeration for nodes representing course end points. */
	COURSE_END("+");
	
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
