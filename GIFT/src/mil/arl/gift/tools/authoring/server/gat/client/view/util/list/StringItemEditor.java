/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.list;

import java.util.Set;

import org.gwtbootstrap3.client.ui.TextBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.validation.ValidationComposite;
import mil.arl.gift.common.gwt.client.validation.ValidationStatus;
import mil.arl.gift.common.gwt.client.validation.WidgetValidationStatus;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.list.StringItemEditor.StringWrapper;

/**
 * An {@link ItemEditor} that can be used to edit a list of strings within
 * {@link ItemListEditor}. Accomplishes this through the use of the inner class
 * {@link StringWrapper}.
 *
 * @author tflowers
 *
 */
public class StringItemEditor extends ItemEditor<StringWrapper> {

    /**
     * A class that wraps a {@link String} to make editing a {@link String}
     * within an {@link ItemListEditor} of Strings possible.
     *
     * @author tflowers
     *
     */
    public static class StringWrapper {

        /** The {@link String} being wrapped. */
        private String value = null;

        /**
         * Gets the {@link String} being wrapped.
         *
         * @return The value being wrapped. Can be null.
         */
        public String getValue() {
            return value;
        }

        /**
         * Sets the {@link String} being wrapped.
         *
         * @param value The value being wrapped. Can be null.
         */
        public void setValue(String value) {
            this.value = value;
        }
    }

    /** The binder that combines the ui.xml with this java class */
    private static StringItemEditorUiBinder uiBinder = GWT.create(StringItemEditorUiBinder.class);

    /** The binder that combines the ui.xml with the java class */
    interface StringItemEditorUiBinder extends UiBinder<Widget, StringItemEditor> {
    }

    /** The label used to describe {@link #textBox} */
    @UiField
    protected InlineLabel textBoxLabel;

    /** The text box used to edit the string. */
    @UiField
    protected TextBox textBox;

    /** Validation for ensuring the value isn't blank */
    private final WidgetValidationStatus blankValueValidationStatus;

    /**
     * Creates an instance of the editor
     */
    public StringItemEditor() {
        this("");
    }

    /**
     * Creates an instance of the editor with the given label describing the
     * {@link #textBox}.
     *
     * @param label The text to show next to the {@link #textBox}. Can't be
     *        null.
     */
    public StringItemEditor(String label) {
        if (label == null) {
            throw new IllegalArgumentException("The parameter 'label' cannot be null.");
        }

        initWidget(uiBinder.createAndBindUi(this));
        textBoxLabel.setText(label);

        blankValueValidationStatus = new WidgetValidationStatus(textBox, "The " + label + " value can't be blank.");
    }

    /**
     * Revalidates the value of {@link #textBox} when it changes.
     *
     * @param event The event that contains information about the change in
     *        value of {@link #textBox}.
     */
    @UiHandler("textBox")
    protected void onValueChanged(ValueChangeEvent<String> event) {
        requestValidation(blankValueValidationStatus);
    }

    @Override
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses) {
        validationStatuses.add(blankValueValidationStatus);
    }

    @Override
    public void validate(ValidationStatus validationStatus) {
        if (blankValueValidationStatus.equals(validationStatus)) {
            validationStatus.setValidity(StringUtils.isNotBlank(textBox.getValue()));
        }
    }

    @Override
    protected boolean validate(StringWrapper strWrapper) {
        return StringUtils.isNotBlank(strWrapper.getValue());
    }

    @Override
    protected void populateEditor(StringWrapper obj) {
        textBox.setValue(obj.getValue());
    }

    @Override
    protected void applyEdits(StringWrapper obj) {
        obj.setValue(textBox.getValue());
    }

    @Override
    protected void setReadonly(boolean isReadonly) {
        textBox.setEnabled(!isReadonly);
    }

    @Override
    public void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites) {

    }
}
