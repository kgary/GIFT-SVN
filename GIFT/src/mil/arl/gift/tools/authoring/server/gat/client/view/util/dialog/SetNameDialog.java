/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalHeader;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.client.ui.form.error.BasicEditorError;
import org.gwtbootstrap3.client.ui.form.validator.Validator;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.common.gwt.client.event.InputEvent;
import mil.arl.gift.common.gwt.client.event.InputHandler;
import mil.arl.gift.common.util.StringUtils;

/**
 * A dialog used to set the name of some object
 * 
 * @author nroberts
 */
public class SetNameDialog extends TopLevelModal implements HasValue<String> {
    
    /** The logger for this class */
    private final Logger logger = Logger.getLogger(SetNameDialog.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static SetNameDialogUiBinder uiBinder = GWT.create(SetNameDialogUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
    interface SetNameDialogUiBinder extends UiBinder<Widget, SetNameDialog> {
    }

    /** The gwt bootstrap form that contains the widgets with which to perform validation. */
    @UiField
    protected Form form;

    /** The label that displays above the name text box */
    @UiField
    protected HasHTML nameLabel;

    /** The text box that allows the user to input the name value */
    @UiField
    protected TextBox nameTextBox;

    /** The label that display below the name text box to provide additional information */
    @UiField
    protected HTML additionalInfoLabel;

    /** The confirmation button that will trigger the value change event */
    private Button confirmButton = new Button();
    
    /** The cancel button that will exit the dialog without saving any changes */
    private Button cancelButton = new Button("Cancel");

    /** The dialog title */
    protected HTML caption = new HTML();

    /** The container for the contents of the dialog (e.g. labels and the name text box) */
    protected ModalBody body = new ModalBody();

    /** The container for the confirm and cancel button */
    protected ModalFooter footer = new ModalFooter();

    /** The {@link Validator} to process the name text box input */
    private Validator<String> currentValidator = null;

    /** The default text for the confirm button if none is provided */
    private static final String DEFAULT_CONFIRM_TEXT = "Save Name";

    /** The default text for the dialog title if none is provided */
    private static final String DEFAULT_CAPTION_TEXT = "Change Name";

    /**
     * Creates a new dialog. <br\> The confirm button uses the
     * {@link org.gwtbootstrap3.client.ui.constants.ButtonType.PRIMARY} css. <br\> The cancel button
     * uses the {@link org.gwtbootstrap3.client.ui.constants.ButtonType.DANGER} css.
     * 
     * @param captionText the caption text for the dialog. If null, a generic string will be used.
     * @param instructionsHtml the instructions HTML above the text box. If null, a generic string
     *        will be used.
     * @param confirmText the text for the confirm button. If null, a generic string will be used.
     */
    public SetNameDialog(String captionText, String instructionsHtml, String confirmText) {

        super();

        /* Nick: This class used to extend ModalDialogBox instead of TopLevelModal, but we ran into
         * an issue where a CourseObjectModal was stealing focus from this dialog's textbox, which
         * would make the textbox unresponsive in Chrome and Firefox. This problem is the product of
         * some strange functionality in Bootstrap in which modals will attempt to gain focus
         * whenever they are shown, which can potentially rob focus away from any elements that
         * currently have it. To fix this, I basically converted this class to a modal so that it
         * can steal focus back from CourseObjectModal, preventing the issue. I don't like doing
         * this, but after trying many different solutions, this was the only solution that wouldn't
         * affect most of the GAT, since other solutions involve overwriting the behavior of
         * Bootstrap modals, which would affect many areas in the GAT. */

        setDataBackdrop(ModalBackdrop.STATIC);
        setDataKeyboard(false);
        setClosable(false);
        setFade(true);

        setWidth("600px");

        ModalHeader header = new ModalHeader();
        header.setClosable(false);

        Heading heading = new Heading(HeadingSize.H3);
        heading.add(caption);

        header.add(heading);

        setText(captionText);

        add(header);

        body.add(uiBinder.createAndBindUi(this));

        add(body);

        cancelButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        confirmButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                ValueChangeEvent.fire(SetNameDialog.this, nameTextBox.getValue());
            }
        });

        confirmButton.setType(ButtonType.PRIMARY);
        confirmButton.setEnabled(false);
        confirmButton.setText(confirmText != null ? confirmText : DEFAULT_CONFIRM_TEXT);

        cancelButton.setType(ButtonType.DANGER);

        FlowPanel footerPanel = new FlowPanel();
        footerPanel.add(confirmButton);
        footerPanel.add(cancelButton);

        footer.add(footerPanel);

        add(footer);

        setInstructionsHtml(instructionsHtml);

        nameTextBox.addDomHandler(new InputHandler() {
            @Override
            public void onInput(InputEvent event) {
                confirmButton.setEnabled(form.validate());
            }
        }, InputEvent.getType());

        addDomHandler(new KeyUpHandler() {

            @Override
            public void onKeyUp(KeyUpEvent event) {
                int key = event.getNativeKeyCode();

                if (key == KeyCodes.KEY_ENTER) {
                    confirmButton.click();
                }
            }

        }, KeyUpEvent.getType());

        addShownHandler(new ModalShownHandler() {

            @Override
            public void onShown(ModalShownEvent evt) {

                nameTextBox.getElement().focus();

                if (nameTextBox.getText() != null) {
                    nameTextBox.setCursorPos(nameTextBox.getText().length());
                }
            }
        });

    }

    /**
     * Displays an overwrite option
     * 
     * @param overwriteText The overwrite button text. If null, a generic string will be used.
     * @param overwriteHandler Click handler to execute when the overwrite button is clicked
     */
    public void showOverwriteOption(String overwriteText, ClickHandler overwriteHandler) {

        Button overwriteButton = new Button();
        FlowPanel footerPanel = new FlowPanel();

        footer.clear();
        overwriteButton.setText(overwriteText == null ? "Overwrite" : overwriteText);
        overwriteButton.addClickHandler(overwriteHandler);
        overwriteButton.setType(ButtonType.WARNING);

        footerPanel.add(overwriteButton);
        footerPanel.add(confirmButton);
        footerPanel.add(cancelButton);
        footer.add(footerPanel);
    }

    /**
     * Sets the instructions message above the text box.
     * 
     * @param instructionsHtml the instructions HTML above the text box. If null, a generic string
     *        will be used.
     */
    public void setInstructionsHtml(String instructionsHtml) {
        if (instructionsHtml != null) {
            nameLabel.setHTML(instructionsHtml);
        } else {
            nameLabel.setText("New Name:");
        }
    }

    /**
     * Adds additional information beneath the text box.
     * 
     * @param additionalInfoHtml HTML to display below the text box. If null, nothing will be
     *        displayed.
     */
    public void setAdditionalInfo(String additionalInfoHtml) {
        if (additionalInfoHtml != null) {
            additionalInfoLabel.setHTML(additionalInfoHtml);
            additionalInfoLabel.setVisible(true);
        } else {
            additionalInfoLabel.setVisible(false);
        }
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    @Override
    public String getValue() {
        return nameTextBox.getValue();
    }

    @Override
    public void setValue(String value) {
        setValue(value, false);
    }

    @Override
    public void setValue(String value, boolean fireEvents) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setValue(" + value + ", " + fireEvents + ")");
        }

        nameTextBox.setValue(value);
        confirmButton.setEnabled(form.validate(fireEvents));

        if (fireEvents) {
            ValueChangeEvent.fire(this, value);
        }
    }

    @Override
    public void show() {
        super.show();
    }

    /** Display this dialog to the user */
    public void center() {
        show();
    }

    /**
     * Sets the text of the title of the dialog.
     * 
     * @param text The text to place within the dialog. If null or empty, a default title will be used.
     * Does support HTML.
     */
    public void setText(String text) {
        caption.setHTML(StringUtils.isNotBlank(text) ? text : DEFAULT_CAPTION_TEXT);
    }

    /**
     * Sets the {@link Validator} for the name text box.
     * 
     * @param validator the {@link Validator} to check the user input for the name text box field.
     *        If null, the textbox will have no validator.
     */
    public void setValidator(Validator<String> validator) {
        if (currentValidator != null) {
            nameTextBox.removeValidator(currentValidator);
        }

        if (validator != null) {
            nameTextBox.addValidator(validator);
        }

        currentValidator = validator;
    }

    /**
     * Generates the {@link BasicEditorError} object for the text box component using the provided
     * value and error message.
     * 
     * @param value the value that failed validation.
     * @param errorMsg the error message to display to the user.
     * @return a {@link BasicEditorError} to display to the user to indicate why validation failed.
     */
    public BasicEditorError generateEditorError(String value, String errorMsg) {
        return new BasicEditorError(nameTextBox, value, errorMsg);
    }
}