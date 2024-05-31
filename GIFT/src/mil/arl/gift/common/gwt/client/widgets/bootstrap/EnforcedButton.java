/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.bootstrap;

import org.gwtbootstrap3.client.ui.constants.IconType;

import com.google.gwt.event.dom.client.ClickHandler;

/**
 * A button that is contained within {@link GenericListEditor#topButtons} or
 * {@link GenericListEditor#bottomButtons}. The purpose of this class is to enforce the parameter
 * requirements.
 * 
 * @author tflowers
 *
 */
public class EnforcedButton extends org.gwtbootstrap3.client.ui.Button {

    /**
     * Constructs a button with all fields populated.
     * 
     * @param iconType The symbol to display within the button. It may be null iff text is not null.
     * @param text The text to display within the button. It may be null iff icon is not null.
     * @param tooltip The text to display when the user hovers over the button. Can be null.
     * @param clickHandler The handler to invoke when the user clicks the button. Can't be null.
     */
    public EnforcedButton(IconType iconType, String text, String tooltip, ClickHandler clickHandler) {
        if (iconType == null && text == null) {
            throw new IllegalArgumentException("Both 'icon' and 'text' cannot be null at the same time.");
        }

        if (clickHandler == null) {
            throw new IllegalArgumentException("The parameter 'callback' can't be null.");
        }

        setIcon(iconType);
        setText(text);
        setTitle(tooltip);
        addClickHandler(clickHandler);
    }
}