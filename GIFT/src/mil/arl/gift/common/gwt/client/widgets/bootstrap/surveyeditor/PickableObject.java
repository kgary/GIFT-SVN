/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

/**
 * This class represents a response in an optional predefined list that an author can choose from.
 * The text is displayed and associated with an id that is hidden from the user. The id can be used
 * to allow GIFT to communicate the selected response to a scenario running in an external application.
 * 
 * @author bzahid
 */
public class PickableObject {

	/** The scenario object id associated with the response text */
	private String objectId = "";
	
	/** The response text */
	private String responseText = null;
	
	/**
	 * Creates a new pickable object.
	 * 
	 * @param responseText The response text.
	 * @param objectId The scenario object id associated with the response text 
	 */
	public PickableObject(String responseText, String objectId) {
		this.responseText = responseText;
		this.objectId = objectId;
	}
	
	/**
	 * Creates a new pickable object with no associated object id.
	 * 
	 * @param responseText The response text.
	 */
	public PickableObject(String responseText) {
		this.responseText = responseText;
	}
	
	/**
	 * Gets the response text. 
	 * 
	 * @return The response text
	 */
	public String getResponse() {
		return responseText;
	}
	
	/**
	 * Sets the response text and clears the scenario object id.
	 * 
	 * @param responseText The response text to set
	 */
	public void setNewResponse(String responseText) {
		this.responseText = responseText;
		objectId = "";
	}
	
	/**
	 * Gets the object id associated with the response text.
	 * 
	 * @return The scenario object id associated with the response text
	 */
	public String getObjectId() {
		return objectId;
	}
	
}
