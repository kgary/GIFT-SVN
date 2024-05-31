/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import java.util.Iterator;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;

import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A {@link PopupPanel} that is used to ask for confirmation before taking some
 * action.
 *
 * @author tflowers
 *
 */
public class ConfirmationOverlayPopup extends OverlayPopup {

    /** The widget that is used to display the prompt text to the user */
    private final HTML prompt = new HTML();

    /** The button the user clicks as confirmation */
    private final Button confirmButton = new Button();

    /** The button the user clicks to deny the action */
    private final Button denyButton = new Button();

    /** Inner most panel containing the confirmation dialog */
    private final HorizontalPanel horizontalPanel = new HorizontalPanel();

    /**
     * Default constructor.
     */
    public ConfirmationOverlayPopup() {
        super();
        add(prompt);
        prompt.getElement().getStyle().setProperty("min-width", "300px");
        
        horizontalPanel.setCellVerticalAlignment(prompt, HasVerticalAlignment.ALIGN_MIDDLE);
        
        super.add(horizontalPanel);

        add(confirmButton);
        confirmButton.setType(ButtonType.PRIMARY);
        setButtonProperties(confirmButton);

        add(denyButton);
        denyButton.setType(ButtonType.DANGER);
        setButtonProperties(denyButton);
    }

    /**
     * Creates the {@link ConfirmationOverlayPopup} with the given text in their
     * corresponding locations.
     *
     * @param promptText The text used to prompt the user for a decision. Null
     *        is treated as blank.
     * @param confirmText The text used to describe the confirm option. Null is
     *        treated as blank.
     * @param denyText The text used to describe the deny option. Null is
     *        treated as blank.
     */
    public ConfirmationOverlayPopup(String promptText, String confirmText, String denyText) {
        this();

        /* Initializes the widgets with the passed text values */
        setPromptText(promptText);
        setConfirmText(confirmText);
        setDenyText(denyText);
    }

    /**
     * Style the button
     * 
     * @param button the button to style
     */
    private void setButtonProperties(Button button) {
        final Style buttonStyle = button.getElement().getStyle();
        buttonStyle.setMarginTop(0, Unit.PX);
        buttonStyle.setMarginRight(4, Unit.PX);
        buttonStyle.setMarginBottom(0, Unit.PX);
        buttonStyle.setMarginLeft(4, Unit.PX);
    }

    /**
     * Setter for the text that is used to prompt the user for their choice.
     *
     * @param promptText The new text to show in the {@link #prompt}. Null is
     *        treated as blank.
     */
    public void setPromptText(String promptText) {
        promptText = promptText != null ? promptText : "";
        prompt.setHTML(SafeHtmlUtils.fromTrustedString(promptText));
    }

    /**
     * Setter for the text that is displayed as the confirm choice.
     *
     * @param confirmText The new text to show in the {@link #confirmButton}.
     *        Null is treated as blank.
     */
    public void setConfirmText(String confirmText) {
        confirmButton.setText(confirmText);
    }

    /**
     * Setter for the text that is displayed as the deny choice.
     *
     * @param denyText The new text to show in the {@link #denyButton}. Null is
     *        treated as blank.
     */
    public void setDenyText(String denyText) {
        denyButton.setText(denyText);
    }

    /**
     * Adds a {@link ClickHandler} to the {@link #confirmButton}.
     *
     * @param handler The {@link ClickHandler} to subscribe to the
     *        {@link #confirmButton}. Can't be null.
     * @return The {@link HandlerRegistration} that can be used to unsubscribe
     *         the provided {@link ClickHandler}. Can't be null.
     */
    public HandlerRegistration addConfirmHandler(ClickHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("The parameter 'handler' cannot be null.");
        }

        return confirmButton.addClickHandler(handler);
    }

    /**
     * Adds a {@link ClickHandler} to the {@link #denyButton}.
     *
     * @param handler The {@link ClickHandler} to subscribe to the
     *        {@link #denyButton}. Can't be null.
     * @return The {@link HandlerRegistration} that can be used to unsubscribe
     *         the provided {@link ClickHandler}. Can't be null.
     */
    public HandlerRegistration addDenyHandler(ClickHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("The parameter 'handler' cannot be null.");
        }

        return denyButton.addClickHandler(handler);
    }

    @Override
    public void add(Widget w) {
        if (w == null) {
            throw new IllegalArgumentException("The parameter 'w' cannot be null.");
        }

        horizontalPanel.add(w);
    }

    @Override
    public void clear() {
        horizontalPanel.clear();
    }

    @Override
    public Iterator<Widget> iterator() {
        return horizontalPanel.iterator();
    }

    @Override
    public boolean remove(Widget w) {
        return horizontalPanel.remove(w);
    }
}
