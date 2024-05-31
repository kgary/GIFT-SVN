/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import javax.swing.tree.DefaultMutableTreeNode;

/**
 * Node that also holds a checked or unchecked state.
 * 
 * @author cdettmering
 *
 */
public class CheckBoxNode extends DefaultMutableTreeNode {
	
	/** Generated serial */
	private static final long serialVersionUID = 4000145090879681465L;
	
	/** checked or unchecked state */
	private boolean selected;
	
	/**
	 * Creates a new unchecked CheckBoxNode
	 */
	public CheckBoxNode() {
		super();
	}
	
	/**
	 * Creates a new unchecked CheckBoxNode, with no parent, no children, but
	 * allows children, and initializes it with the specified userObject.
	 * 
	 * @param userObject Object to initialize the CheckBoxNode
	 */
	public CheckBoxNode(Object userObject) {
		super(userObject);
	}
	
	/**
	 * Creates a new unchecked CheckBoxNode, with no parent, no children, but
	 * allows children if specified, and initializes it with the specified userObject.
	 * 
	 * @param userObject Object to initialize the CheckBoxNode
	 * @param allowsChildren Should the node allow children?
	 */
	public CheckBoxNode(Object userObject, boolean allowsChildren) {
		super(userObject, allowsChildren);
	}
	
	/**
	 * Checks if the node is checked or not.
	 * 
	 * @return True if the node is checked, false if the node is unchecked.
	 */
	public boolean isSelected() {
		return selected;
	}
	
	/**
	 * Sets the checked state of the node
	 * 
	 * @param select True to check the node, false to uncheck the node.
	 */
	public void setSelected(boolean select) {
		selected = select;
	}

}
