/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.HelpMap;

/**
 * The Class FormFieldFocusEvent.
 */
public class FormFieldFocusEvent extends GenericEvent {

	/** The field. */
	private HelpMap.FormFieldEnum field;
	
	/**
	 * Instantiates a new form field focus event.
	 *
	 * @param field the field
	 */
	public FormFieldFocusEvent(HelpMap.FormFieldEnum field) {
		this.field = field;
	}
	
	/**
	 * Gets the field.
	 *
	 * @return the field
	 */
	public HelpMap.FormFieldEnum getField() {
		return this.field;
	}	
}
