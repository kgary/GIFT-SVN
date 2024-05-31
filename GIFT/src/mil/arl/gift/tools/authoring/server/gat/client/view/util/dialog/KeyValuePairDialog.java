/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.ModalHeader;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.constants.HeadingSize;
import org.gwtbootstrap3.client.ui.constants.ModalBackdrop;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHTML;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog used to display key value fields.
 * 
 * @author sharrison
 */
public class KeyValuePairDialog extends TopLevelModal {

	private static KeyValuePairDialogUiBinder uiBinder = GWT
			.create(KeyValuePairDialogUiBinder.class);

	interface KeyValuePairDialogUiBinder extends UiBinder<Widget, KeyValuePairDialog> {
	}
	
	@UiField
    protected HasHTML identifierLabel;
    
    @UiField
    protected TextBox identifierTextBox;
    
    @UiField
    protected HasHTML keyLabel;
    
    @UiField
    protected TextBox keyTextBox;
    
    @UiField
    protected HasHTML valueLabel;
    
    @UiField
    protected TextBox valueTextBox;
	
	private Button confirmButton = new Button();	
	private Button cancelButton = new Button("Cancel");
	
	protected HTML caption = new HTML();
	
	protected ModalBody body = new ModalBody();
	
	protected ModalFooter footer = new ModalFooter();
	
    /**
     * Creates a new dialog
     * 
     * @param captionText the caption text for the dialog. If null, a generic string will be used.
     * @param identifierHtml the instructions HTML above the identifier text box. If null, the
     *            identifier text box will be hidden.
     * @param keyHtml the instructions HTML above the key text box. If null, a generic string will
     *            be used.
     * @param valueHtml the instructions HTML above the value text box. If null, a generic string
     *            will be used.
     * @param confirmText the text for the confirm button. If null, a generic string will be used.
     */
    public KeyValuePairDialog(String captionText, String identifierHtml, String keyHtml, String valueHtml, String confirmText) {

        super();
		
        // Nick: This class used to extend ModalDialogBox instead of TopLevelModal, but we ran into an issue where a CourseObjectModal was
        // stealing focus from this dialog's textbox, which would make the textbox unresponsive in Chrome and Firefox. This problem is the 
        // product of some strange functionality in Bootstrap in which modals will attempt to gain focus whenever they are shown, 
        // which can potentially rob focus away from any elements that currently have it. To fix this, I basically converted this class
        // to a modal so that it can steal focus back from CourseObjectModal, preventing the issue. I don't like doing this, but after
        // trying many different solutions, this was the only solution that wouldn't affect most of the GAT, since other solutions
        // involve overwriting the behavior of Bootstrap modals, which would affect many areas in the GAT.
        
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
        
        if (caption != null) {
            setText(captionText);
        } else {
            setText("Change Values");
        }
        
        add(header);
        
        body.add(uiBinder.createAndBindUi(this));
        
        add(body);
        
        cancelButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                hide();
            }
        });

        confirmButton.setType(ButtonType.PRIMARY);

        if (confirmText != null) {
            confirmButton.setText(confirmText);
        } else {
            confirmButton.setText("Save Name");
        }

        cancelButton.setType(ButtonType.DANGER);
        
        FlowPanel footerPanel = new FlowPanel();
        footerPanel.add(confirmButton);
        footerPanel.add(cancelButton);

        footer.add(footerPanel);

        add(footer);

        if (identifierHtml != null) {
            identifierLabel.setText(identifierHtml);
        } else {
            identifierLabel.setText(null);
            identifierTextBox.setVisible(false);
        }

        keyLabel.setText(keyHtml != null ? keyHtml : "Set Key:");
        valueLabel.setText(valueHtml != null ? valueHtml : "Set Value:");
    }
	
	@Override
	public void show(){		
		super.show();
	}
	
	public void center(){
		show();
	}

    /**
     * Sets the caption text.
     * 
     * @param text the text to set.
     */
    public void setText(String text) {
        caption.setText(text);
    }
    
    /**
     * Sets the confirm button text.
     * 
     * @param text the text to set.
     */
    public void setConfirmButtonText(String text) {
        confirmButton.setText(text);
    }
    
    /**
     * Gets the identifier text in the textbox.
     * 
     * @return the identifier text
     */
    public String getIdentifier() {
        return identifierTextBox.getValue();
    }
    
    /**
     * Sets the identifier text in the textbox.
     * 
     * @param identifier the identifier text to set.
     */
    public void setIdentifier(String identifier) {
        identifierTextBox.setValue(identifier);
    }

    /**
     * Gets the key text in the textbox.
     * 
     * @return the key text
     */
    public String getKey() {
        return keyTextBox.getValue();
    }
    
    /**
     * Sets the key text in the textbox.
     * 
     * @param key the key text to set.
     */
    public void setKey(String key) {
        keyTextBox.setValue(key);
    }

    /**
     * Gets the value text in the textbox.
     * 
     * @return the value text
     */
    public String getValue() {
        return valueTextBox.getValue();
    }
    
    /**
     * Sets the value text in the textbox.
     * 
     * @param value the value text to set.
     */
    public void setValue(String value) {
        valueTextBox.setValue(value);
    }

    /**
     * Clears all textbox fields.
     */
    public void clearTextBoxFields() {
        setIdentifier(null);
        setKey(null);
        setValue(null);
    }
    
    /**
     * Sets the handler for when the confirm button is clicked. The handler needs to be set for the
     * confirm button to be functional.
     */
    public void setConfirmClickHandler(ClickHandler handler) {
        confirmButton.addClickHandler(handler);
    }
}
