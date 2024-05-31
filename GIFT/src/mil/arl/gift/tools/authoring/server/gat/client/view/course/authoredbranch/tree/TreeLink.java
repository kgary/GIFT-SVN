/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.authoredbranch.tree;

import com.github.gwtd3.api.layout.Link;

/**
 * A JavaScriptObject that allows access to the link properties of a TreeNode
 */
public class TreeLink extends Link {
	
	/**
	 * Class constructor
	 */
	protected TreeLink() {
		super();
	}
	
	/**
	 * Gets the source node id.
	 * 
	 * @return The id of the source node.
	 */
	public final native int getSource() /*-{
		return this.source || -1;
	}-*/;

	/**
	 * Sets the source node id.
	 * 
	 * @param sourceId The id of the source node.
	 */
	public final native void setSource(int sourceId) /*-{
		this.source = sourceId;
	}-*/;
	
	/**
	 * Gets the target node id.
	 * 
	 * @return The id of the target node.
	 */
	public final native int getTarget() /*-{
		return this.target || -1;
	}-*/;

	/**
	 * Sets the target node id.
	 * 
	 * @param targetId The id of the target node.
	 */
	public final native void setTarget(int targetId) /*-{
		this.target = targetId;
	}-*/;
	
	/**
	 * Adds an attribute to the TreeLink
	 * @param name The name of the attribute
	 * @param value The value of the attribute
	 */
	public final native void setAttr(String name, String value) /*-{
		this[name] = value;
	}-*/;
	
}