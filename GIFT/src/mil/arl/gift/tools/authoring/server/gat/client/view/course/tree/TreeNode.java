/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.tree;

import com.github.gwtd3.api.arrays.Array;
import com.github.gwtd3.api.layout.HierarchicalLayout.Node;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * A JavaScriptObject that represents a node in a course tree structure.
 * 
 * @author nroberts
 */
public class TreeNode extends Node {
	
	/** Attribute name used to access node IDs */
	public final static String ID_ATTRIBUTE = "id";
	
	/** Attribute name used to access node types */
	public final static String TYPE_ATTRIBUTE = "type";
	
	/** Attribute name used to access node widths */
	public final static String WIDTH_ATTRIBUTE = "width";
	
	/** Attribute name used to access node links */
	public final static String LINKS_ATTRIBUTE = "links";
	
	/** Attribute name used to access node children */
	public final static String CHILDREN_ATTRIBUTE = "children";
	
	/** Attribute name used to access node x0 coordinates */
	public final static String X0_ATTRIBUTE = "x0";
	
	/** Attribute name used to access node y0 coordinates */
	public final static String Y0_ATTRIBUTE = "y0";
	
	/**
	 * Class constructor
	 */
	protected TreeNode() {
		super();
	}
	
	/**
	 * Gets the node id.
	 * 
	 * @return The node id.
	 */
	public final native int id() /*-{
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::ID_ATTRIBUTE] || -1;
	}-*/;

	/**
	 * Sets the node id.
	 * 
	 * @param id The id of the node.
	 * @return The new id.
	 */
	public final native int id(int id) /*-{
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::ID_ATTRIBUTE] = id;
	}-*/;
	
	/**
	 * Sets an attribute for this node.
	 * 
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public final native void setAttr(String name, JavaScriptObject value) /*-{
		this[name] = value;
	}-*/;

	/**
	 * Sets a String attribute for this node.
	 * 
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public final native String setStrAttr(String name, String value) /*-{
		return this[name] = value;
	}-*/;
	
	/**
	 * Sets an attribute for this node.
	 * 
	 * @param name The name of the attribute.
	 * @param value The value of the attribute.
	 */
	public final native double setAttr(String name, double value) /*-{
		return this[name] = value;
	}-*/;

	/**
	 * Gets an attribute for this node
	 * 
	 * @param name The name of the attribute to get.
	 * @return The value of the attribute as a JavaScriptObject.
	 */
	public final native JavaScriptObject getObjAttr(String name) /*-{
		return this[name];
	}-*/;

	/**
	 * Gets an attribute for this node
	 * 
	 * @param name The name of the attribute to get.
	 * @return The value of the attribute as a double.
	 */
	public final native double getNumAttr(String name) /*-{
		return this[name];
	}-*/;
	
	/**
	 * Sets the type attribute of this node.
	 * 
	 * @param type The type this TreeNode represents. (See {@link TreeNodeEnum})
	 */
	public final native void setType(String type) /*-{
		this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::TYPE_ATTRIBUTE] = type;
	}-*/;
	
	/**
	 * Gets the type attribute of this node.
	 * 
	 * @return A string containing the node type. (See {@link TreeNodeEnum})
	 */
	public final native String getType() /*-{
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::TYPE_ATTRIBUTE];
	}-*/;
	
	/**
	 * Gets the links to assign to this node
	 * 
	 * @param links the links to assign
	 */
	public final native void setLinks(Array<TreeLink> links)/*-{		
		this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::LINKS_ATTRIBUTE] = links;	
	}-*/;
	
	/**
	 * Gets the links assigned to this node
	 * 
	 * @return the links assigned to this node
	 */
	public final native Array<TreeLink> getLinks()/*-{		
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::LINKS_ATTRIBUTE];
	}-*/;
	
	/**
	 * Sets the width to use for this node
	 * 
	 * @param width the width to use for this node
	 */
	public final native void setWidth(double width) /*-{
		this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::WIDTH_ATTRIBUTE] = width;
	}-*/;
	
	/**
	 * Gets this node's width
	 * 
	 * @return this node's width
	 */
	public final native double getWidth() /*-{
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::WIDTH_ATTRIBUTE];
	}-*/;
	
	/**
	 * Gets the children to assign to this node
	 * 
	 * @param children the children to assign
	 */
	public final native void setChildren(Array<TreeNode> children)/*-{		
		this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::CHILDREN_ATTRIBUTE] = children;	
	}-*/;
	
	/**
	 * Gets the children assigned to this node
	 * 
	 * @return the chilren assigned to this node
	 */
	public final native Array<TreeNode> getChildren()/*-{		
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::CHILDREN_ATTRIBUTE];
	}-*/;
	

	/**
	 * Sets the x0 coordinate for this node
	 * 
	 * @param x0 the value to use as the x0 coordinate
	 */
	public final native void x0(double x0) /*-{
		this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::X0_ATTRIBUTE] = x0;
	}-*/;
	
	/**
	 * Gets the x0 coordinate for this node
	 * 
	 * @return this node's x0 coordinate value
	 */
	public final native double x0() /*-{
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::X0_ATTRIBUTE];
	}-*/;
	
	/**
	 * Sets the y0 coordinate for this node
	 * 
	 * @param y0 the value to use as the y0 coordinate
	 */
	public final native void y0(double y0) /*-{
		this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::Y0_ATTRIBUTE] = y0;
	}-*/;
	
	/**
	 * Gets the y0 coordinate for this node
	 * 
	 * @return this node's y0 coordinate value
	 */
	public final native double y0() /*-{
		return this[@mil.arl.gift.tools.authoring.server.gat.client.view.course.tree.TreeNode::Y0_ATTRIBUTE];
	}-*/;
}