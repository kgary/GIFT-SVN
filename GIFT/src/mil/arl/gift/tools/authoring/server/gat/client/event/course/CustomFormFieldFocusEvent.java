/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import mil.arl.gift.tools.authoring.server.gat.client.event.FormFieldFocusEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.HelpMap.FormFieldEnum;

/**
 * The Class CustomFormFieldFocusEvent.
 */
public class CustomFormFieldFocusEvent extends FormFieldFocusEvent {

	/** The custom arg. */
	private String customArg;
	
	/**
	 * Instantiates a new custom form field focus event.
	 *
	 * @param field the field
	 * @param customArg the custom arg
	 */
	public CustomFormFieldFocusEvent(FormFieldEnum field, String customArg) {
		super(field);
		this.customArg = customArg;
	}

	/**
	 * Gets the custom arg.
	 *
	 * @return the custom arg
	 */
	public String getCustomArg() {
		return customArg;
	}
}
