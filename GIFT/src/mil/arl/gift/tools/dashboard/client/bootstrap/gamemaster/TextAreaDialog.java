/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextArea;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.common.util.StringUtils;

/**
 * A confirmation dialog with a text area.
 * 
 * @author sharrison
 */
public class TextAreaDialog extends ModalDialogBox implements HasValue<String> {

    /** The UiBinder that combines the ui.xml with this java class */
    private static SetNameDialogUiBinder uiBinder = GWT.create(SetNameDialogUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SetNameDialogUiBinder extends UiBinder<Widget, TextAreaDialog> {
    }

    /** The instructional label */
    @UiField
    protected HTML infoLabel;

    /** The error label */
    @UiField
    protected HTML errorLabel;

    /** The text area */
    @UiField
    protected TextArea textArea;

    /** The confirm button */
    private Button confirmButton = new Button("Ok");

    /** The cancel button */
    private Button cancelButton = new Button("Cancel");

    /**
     * Creates a new dialog
     * 
     * @param caption the caption text for the dialog. If null, a generic string
     *        will be used.
     * @param instructions the instruction text above the text box. If null or
     *        blank, no instruction will be given. Supports HTML.
     * @param confirmText the text for the confirm button. If null, a generic
     *        string will be used.
     */
    public TextAreaDialog(String caption, SafeHtml instructions, String confirmText) {

        setWidget(uiBinder.createAndBindUi(this));
        setGlassEnabled(true);

        cancelButton.setType(ButtonType.DANGER);
        confirmButton.setType(ButtonType.PRIMARY);

        FlowPanel footer = new FlowPanel();
        footer.add(confirmButton);
        footer.add(cancelButton);
        setFooterWidget(footer);

        /* Update caption */
        setCaption(caption);

        /* Update instructions */
        if (instructions != null) {
            infoLabel.setHTML(instructions);
        } else {
            infoLabel.removeFromParent();
        }

        /* Update confirm button text */
        if (StringUtils.isNotBlank(confirmText)) {
            confirmButton.setText(confirmText);
        }

        cancelButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        confirmButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                ValueChangeEvent.fire(TextAreaDialog.this, getValue());
                hide();
            }
        });
    }

    /**
     * Updates the caption of the dialog.
     * 
     * @param caption the caption to update. If blank, the default value will be
     *        used.
     */
    public void setCaption(String caption) {
        setText(StringUtils.isNotBlank(caption) ? caption : "Enter Text");
    }

    /**
     * Sets the error message to display if the user entered an invalid value.
     * Supports HTML.
     * 
     * @param message The error message to display if the user entered an
     *        invalid value.
     */
    public void setValidationMessage(String message) {
        errorLabel.setText(message);
    }

    /**
     * Displays or hides the validation error message.
     * 
     * @param show Whether or not to show the error message.
     */
    public void showValidationMessage(boolean show) {
        errorLabel.setVisible(show);
    }

    /**
     * Adds a change handler to fire when the confirm button is clicked.
     * 
     * @param handler The ValueChangeHandler to add.
     */
    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    /**
     * Add a click handler to the cancel button.
     * 
     * @param handler the handler to add.
     * @return the handler registration.
     */
    public HandlerRegistration addCancelHandler(ClickHandler handler) {
        return cancelButton.addClickHandler(handler);
    }

    /**
     * Gets the text in the text area
     * 
     * @return The text in the text area.
     */
    @Override
    public String getValue() {
        return textArea.getValue();
    }

    /**
     * Sets the text in the text area
     * 
     * @param value The text to display in the text area.
     */
    @Override
    public void setValue(final String value) {

        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                textArea.setValue(convertToNewlines(value));
            }
        });

    }

    /**
     * Sets the text in the text area
     * 
     * @param value The text to display in the text area.
     * @param fireEvents Whether or not to fire value change events.
     */
    @Override
    public void setValue(String value, boolean fireEvents) {
        setValue(value);

        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    /**
     * Convert any HTML breaks to text new lines.
     * 
     * @param value the value to convert.
     * @return the converted string
     */
    private String convertToNewlines(String value) {
        value = value.replaceAll("<br>", "\n");
        value = value.replaceAll("<br/>", "\n");
        value = value.replaceAll("<br />", "\n");
        return value;
    }

    @Override
    public void hide() {
        /* Clear the text box */
        textArea.reset();

        super.hide();
    }
}
