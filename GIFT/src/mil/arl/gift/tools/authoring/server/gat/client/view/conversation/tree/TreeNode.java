/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.conversation.tree;

import com.github.gwtd3.api.layout.HierarchicalLayout.Node;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * A JavaScriptObject that represents a node in a conversation tree structure.
 * 
 * @author bzahid
 */
public class TreeNode extends Node {
	
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
		return this.id || -1;
	}-*/;

	/**
	 * Sets the node id.
	 * 
	 * @param id The id of the node.
	 * @return The new id.
	 */
	public final native int id(int id) /*-{
		return this.id = id;
	}-*/;

	/**
	 * Sets the node text.
	 * 
	 * @param text The text to display for this node.
	 */
	public final native void setText(String text) /*-{
		this["name"] = text;
	}-*/;
	
	/**
	 * Gets the node text.
	 * 
	 * @return The node text.
	 */
	public final native String getText() /*-{
		return this["name"];
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
		this["type"] = type;
	}-*/;
	
	/**
	 * Gets the type attribute of this node.
	 * 
	 * @return A string containing the node type. (See {@link TreeNodeEnum})
	 */
	public final native String getType() /*-{
		return this["type"];
	}-*/;
	
	public final native String getAssessmentLevel(int index) /*-{
		if(this["assessments"] == null || this["assessments"].length <= index) {
			return "";
		}
	
		return this["assessments"][index].assessment;
	}-*/;
	
	public final native void addAssessment(String concept, String assessmentLevel, String confidence) /*-{
	
		var newAssessment = [{
				concept: concept,
				assessment: assessmentLevel,
				confidence: confidence
			}];
	
		if(this["assessments"] == null) {
			this["assessments"] = newAssessment;
		} else {
			this.assessments.push(newAssessment[0]);
		}
	
	}-*/;
	
	/**
	 * Removes an assessment from the list.
	 * 
	 * @param index The index of the assessment to delete.
	 */
	public final native void deleteAssessment(int index) /*-{
	
		if(this.assessments == null || this.assessments.length <= index) {
			return;
		} else {
			this.assessments.splice(index, 1);
		}
	
	}-*/;
	
	/**
	 * Sets the assessment level.
	 * 
	 * @param index The index of the assessment to update
	 * @param assessmentLevel The assessment level to set.
	 */
	public final native void setAssessmentLevel(int index, String assessmentLevel) /*-{
	
		if(this["assessments"] == null || this["assessments"].length <= index) {
			var assessments = [{
				assessment: assessmentLevel,
				confidence: 0.9,
				concept: "New Concept"
			}];
			
			if(this["assessments"].length <= index) {
				this.assessments.push(assessments[0]);
			} else {
				this["assessments"] = assessments;
			}
		} else {
			this["assessments"][index].assessment = assessmentLevel;
		}
		
	}-*/;
	
	/**
	 * Gets the confidence value for the assessment at the specified index
	 * 
	 * @param index The index of the assessment to get the confidence value for.
	 * @return The confidence value
	 */
	public final native String getConfidence(int index) /*-{
	
		if(this["assessments"] == null || this["assessments"].length <= index) {
			return 0.9;
		}
	
		var value = this["assessments"][index].confidence;
		if(typeof value == "number") {
			return value.toString();
		}
		
		return value;
	}-*/;
	
	/**
	 * Sets the confidence value for the assessment at the specified index
	 * 
	 * @param index The index of the assessment to set the confidence value for.
	 * @param confidence The confidence value to set.
	 */
	public final native void setConfidence(int index, String confidence) /*-{
	
		if(this["assessments"] == null || this["assessments"].length <= index) {
			var assessments = [{
					assessment: "At Expectation",
					confidence: confidence,
					concept: "New Concept"
				}];
				
			if(this["assessments"].length <= index) {
				this.assessments.push(assessments[0]);
				
			} else {
				this["assessments"] = assessments;
			}
		} else {
			this["assessments"][index].confidence = confidence;
		}
	}-*/;
	
	/**
	 * Gets the concept of the assessment at the specified index
	 * 
	 * @param index The index of the assessment to get the concept for.
	 * @return The assessment concept.
	 */
	public final native String getConcept(int index) /*-{
	
		if(this["assessments"] == null || this["assessments"].length <= index) {
			return "New Concept";
		}
	
		return this["assessments"][index].concept;
	}-*/;
	
	/**
	 * Sets the concept of the assessment at the specified index
	 * 
	 * @param index The index of the assessment to get the concept for.
	 * @param concept The concept name.
	 */
	public final native void setConcept(int index, String concept) /*-{
	
		if(this["assessments"] == null || this["assessments"].length <= index) {
			var assessments = [{
				assessment: "At Expectation",
				confidence: 0.9,
				concept: concept
			}];
			
			if(this["assessments"].length <= index) {
				this.assessments.push(assessments[0]);
				
			} else {
				this["assessments"] = assessments;
			}
		} else {
			this["assessments"][index].concept = concept;
		}
	}-*/;
	
	/**
	 * Removes an assessment from the list.
	 * 
	 * @param index The index of the assessment to remove.
	 */
	public final native void removeAssessment(int index) /*-{
		this.assessments.splice(index, 1);
	}-*/;
	
	/**
	 * Gets whether or not the node has assessments.
	 * 
	 * @return true if there are assessments, false otherwise.
	 */
	public final native boolean hasAssessments() /*-{
		return (this["assessments"] != null && this["assessments"].length > 0);
	}-*/;
}