/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import java.util.logging.Logger;

import org.gwtbootstrap3.client.shared.event.ModalShownEvent;
import org.gwtbootstrap3.client.shared.event.ModalShownHandler;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Heading;
import org.gwtbootstrap3.client.ui.Modal;
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

import mil.arl.gift.common.gwt.client.widgets.NumberSpinner;
import mil.arl.gift.common.util.StringUtils;

/**
 * A dialog used to set the number of some object
 * 
 * @author sharrison
 */
public class SetNumberDialog extends Modal implements HasValue<String>{

    /** The logger for this class */
    private static Logger logger = Logger.getLogger(SetNumberDialog.class.getName());
    
    /** The UiBinder that combines the ui.xml with this java class */
    private static SetNumberDialogUiBinder uiBinder = GWT.create(SetNumberDialogUiBinder.class);

    /** Defines the UiBinder that combines the ui.xml with a java class */
	interface SetNumberDialogUiBinder extends UiBinder<Widget, SetNumberDialog> {
	}
	
	/** The label that displays above the number spinner */
	@UiField
	protected HasHTML numberLabel;
	
	/** The widget that allows the user to select a number */
	@UiField
	protected NumberSpinner numberSpinner;
	
	/** The label that displays to the right of the number spinner. Used to indicate a unit of measurement. */
	@UiField
    protected HasHTML unitLabel;
	
	/** The label that display below the number spinner to provide additional information */
	@UiField
	protected HTML additionalInfoLabel;
	
	/** The confirmation button that will trigger the value change event */
	private Button confirmButton = new Button();	
	
	/** The cancel button that will exit the dialog without saving any changes */
	private Button cancelButton = new Button("Cancel");
	
	/** The dialog title */
	protected HTML caption = new HTML();
	
	/** The container for the contents of the dialog (e.g. labels and the number spinner) */
	protected ModalBody body = new ModalBody();
	
	/** The container for the confirm and cancel button */
	protected ModalFooter footer = new ModalFooter();
	
	/** The default text for the confirm button if none is provided */
	private static final String DEFAULT_CONFIRM_TEXT = "Save Number";
	
	/** The default text for the dialog title if none is provided */
    private static final String DEFAULT_CAPTION_TEXT = "Change Number";
	
	/**
	 * Creates a new dialog. <br\>
	 * The confirm button uses the {@link org.gwtbootstrap3.client.ui.constants.ButtonType.PRIMARY} css. <br\>
	 * The cancel button uses the {@link org.gwtbootstrap3.client.ui.constants.ButtonType.DANGER} css.  
	 * 
	 * @param captionText the caption text for the dialog. If null, a generic string will be used.
	 * @param instructionsHtml the instructions HTML above the text box. If null, a generic string will be used.
	 * @param confirmText the text for the confirm button. If null, a generic string will be used.  
	 * @param unitText the text for the unit label to the right of the text box. If null, no unit label will be displayed.
	 */
    public SetNumberDialog(String captionText, String instructionsHtml, String confirmText, String unitText) {
		
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
                String numValue = numberSpinner.getValue() == null ? null : String.valueOf(numberSpinner.getValue());
                ValueChangeEvent.fire(SetNumberDialog.this, numValue);
            }
        });
		
		confirmButton.setType(ButtonType.PRIMARY);
		
		if (unitText != null) {
		    unitLabel.setText(unitText);
		}
		
		numberSpinner.setValue(0);
		numberSpinner.setWidth("75px");
		numberSpinner.setResizeToFitContent(false);
		
		if(confirmText != null){
			confirmButton.setText(confirmText);
		} else {
			confirmButton.setText(DEFAULT_CONFIRM_TEXT);
		}
		
		cancelButton.setType(ButtonType.DANGER);
		
		FlowPanel footerPanel = new FlowPanel();
		footerPanel.add(confirmButton);
		footerPanel.add(cancelButton);
		
		footer.add(footerPanel);
		
		add(footer);
		
		setInstructionsHtml(instructionsHtml);
		
		addDomHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				int key = event.getNativeKeyCode();
				
				if(key == KeyCodes.KEY_ENTER) {
					confirmButton.click();
				}
			}
			
		}, KeyUpEvent.getType());
		
		addShownHandler(new ModalShownHandler() {
			
			@Override
			public void onShown(ModalShownEvent evt) {
				
				numberSpinner.getElement().focus();
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
     * Sets the text that appears on the confirmation button.
     * 
     * @param confirmText the confirmation button text. If null, a default value will be used.
     */
    public void setConfirmButtonText(String confirmText) {
        confirmButton.setText(StringUtils.isBlank(confirmText) ? DEFAULT_CONFIRM_TEXT : confirmText);
    }
	
    /**
     * Sets the label that appears to the right of the text box.
     * 
     * @param unitText the text to be used as a label for a unit of measurement.
     */
    public void setUnitLabel(String unitText) {
        unitLabel.setText(unitText);
    }
	
	/**
	 * Sets the instructions message above the text box.
	 * 
	 * @param instructionsHtml the instructions HTML above the text box. If null, a generic string will be used.
	 */
	public void setInstructionsHtml(String instructionsHtml) {
		if(instructionsHtml != null){
			numberLabel.setHTML(instructionsHtml);
		} else {
			numberLabel.setText("New Number:");
		}
	}
	
	/**
	 * Adds additional information beneath the text box.
	 * 
	 * @param additionalInfoHtml HTML to display below the text box. If null, nothing will be displayed.
	 */
	public void setAdditionalInfo(String additionalInfoHtml) {
		if(additionalInfoHtml != null) {
			additionalInfoLabel.setHTML(additionalInfoHtml);
			additionalInfoLabel.setVisible(true);
		} else {
			additionalInfoLabel.setVisible(false);
		}
	}
	
    /**
     * Sets the step size for the number spinner.
     * 
     * @param stepSize the step size
     */
    public void setStepSize(int stepSize) {
        numberSpinner.setStepSize(stepSize);
    }
    
    /**
     * Sets the minimum value for the number spinner.
     * 
     * @param minValue the minimum value
     */
    public void setMinValue(int minValue) {
        numberSpinner.setMinValue(minValue);
    }
    
    /**
     * Sets the maximum value for the number spinner.
     * 
     * @param maxValue the maximum value
     */
    public void setMaxValue(int maxValue) {
        numberSpinner.setMaxValue(maxValue);
    }

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public String getValue() {		
		return String.valueOf(numberSpinner.getValue());
	}

    @Override
    public void setValue(String value) {
        try {
            numberSpinner.setValue(Integer.parseInt(value));
        } catch (@SuppressWarnings("unused") Exception e) {
            logger.warning("Tried to parse integer from " + value + " but couldn't.");
        }
    }

	@Override
	public void setValue(String value, boolean fireEvents) {
		setValue(value);
		
		if(fireEvents){
			ValueChangeEvent.fire(this, value);
		}
	}

	/**
     * Sets the text of the title of the dialog.
     * 
     * @param text The text to place within the dialog. If null or empty, a default title will be used.
     */
	public void setText(String text){
	    caption.setText(StringUtils.isNotBlank(text) ? text : DEFAULT_CAPTION_TEXT);
	}
}
