/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap.surveyeditor;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableLabel;

/**
 * An extension of {@link EditableLabel}. This widget displays text that can be clicked on to open a text 
 * box to that modifies it, however an object can be associated with text that is set programmatically, 
 * such as from a drop-down menu.
 * 
 * @author bzahid
 */
public class ValueEditableLabel extends EditableLabel {

	/** The object associated with the current value */
	PickableObject object = null;	
	
	/** The current value displayed in the html editor */
	String currentValue = null;
	
	/**
	 * Creates a new editable HTML element
	 */
	public ValueEditableLabel() {
		super();
		
		htmlEditor.addValueChangeHandler(new ValueChangeHandler<String>() {

			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				
				if(!event.getValue().equals(currentValue)) {
					if(object != null) {
						// clear the associated object id since the user modified the text.
						object.setNewResponse(event.getValue());
					}
				}				
				currentValue = event.getValue();
			}
			
		});
	}	
	
	/**
	 * Sets the value displayed in the label and the object associated with the value. 
	 * 
	 * @param value The text to display in the label
	 * @param object The object associated with this text
	 */
	public void setValue(String value, PickableObject object) {
		setValue(value, object, false);
	}
	
	/**
	 * Sets the value displayed in the label and the object associated with the value. 
	 * 
	 * @param value The text to display in the label
	 * @param object The object associated with this text
	 * @param fireEvents Whether or not to fire value change events
	 */
	public void setValue(String value, PickableObject object, boolean fireEvents) {
		currentValue = value;
		this.object = object;
		setValue(value, fireEvents);
	}
	
	/**
	 * Gets the object associated with the current value
	 * 
	 * @return The object associated with the current value. Can be null if no object 
	 * was set or if the user edited the value.
	 */
	public PickableObject getValueObject() {
		return object;
	}
	
	/**
	 * Sets the value displayed in the label.
	 * 
	 * @param value The text to display in the label
	 */
	@Override
    public void setValue(String value) {
		
		if(object != null && !value.equals(currentValue)) {
			object.setNewResponse(value);
		}
		
		currentValue = value;
		super.setValue(value);
	}
	
	/**
	 * Sets the value displayed in the label.
	 * 
	 * @param value The text to display in the label
	 * @param fireEvents Whether or not to fire value change events
	 */
	@Override
    public void setValue(String value, boolean fireEvents) {
		
		if(object != null && !value.equals(currentValue)) {
			object.setNewResponse(value);
		}
		
		currentValue = value;
		super.setValue(value, fireEvents);
	}
}
