/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.surveyeditor;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.EditableLabel;

/**
 * Wrapper class for editable label. The purpose of this class is to prevent direct access to the
 * editable label since it is being used incorrectly. E.g. if a question widget has it's own
 * setValue() or setEditable() methods, we don't want to give the developer the option of directly
 * calling those methods from the label, skipping the logic that the question's method implemented.
 * 
 * @author sharrison
 */
public class RestrictedEditableLabel {

    /** The label that is wrapped by this class */
    private EditableLabel label;

    /**
     * Constructor
     * 
     * @param label the editable label that we are trying to prevent direct access to.
     */
    public RestrictedEditableLabel(EditableLabel label) {
        this.label = label;
    }

    /**
     * Adds the value change handler to the editable label.
     * 
     * @param handler the handler to add
     * @return the handler registration
     */
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return label.addValueChangeHandler(handler);
    }

    /**
     * Adds a listener to the label widget that will be notified whenever the Enter key is pressed
     * <br/>
     * <br/>
     * Note: The {@link KeyDownEvent} for the Enter key is event is processed BEFORE the
     * {@link ValueChangeEvent} that updates the widget's value, so to get the entered value, you'll
     * need to wait until the event loop finishes via
     * {@link com.google.gwt.core.client.Scheduler#scheduleDeferred(com.google.gwt.core.client.Scheduler.ScheduledCommand)
     * Scheduler.scheduleDeferred(Command)} and then call {@link #getValue()}. This allows you to
     * perform logic either before or after the value changes when the user hits the Enter key.
     * 
     * @param command the command that will act as the listener and get executed when the Enter key
     *            is pressed
     */
    public void setEnterKeyListener(Command command) {
        label.setEnterKeyListener(command);
    }

    /**
     * Retrieves the value of the editable label.
     * 
     * @return the text value of the label.
     */
    public String getValue() {
        return label.getValue();
    }

    /**
     * Sets the placeholder text to use when no text has been entered
     * 
     * @param placeholder the placeholder text
     */
    public void setPlaceholder(String placeholder) {
        label.setPlaceholder(placeholder);
    }

    /**
     * Retrieves the element style of the editable label.
     * 
     * @return the gwt element style.
     */
    public Style getElementStyle() {
        return label.getElement().getStyle();
    }

    /**
     * Shows/Hides the label component
     * 
     * @param visible true to show the label; false to hide it.
     */
    public void setVisible(boolean visible) {
        label.setVisible(visible);
    }
}
