/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteKeyDownEvent;
import org.gwtbootstrap3.extras.summernote.client.event.SummernoteKeyDownHandler;
import org.gwtbootstrap3.extras.summernote.client.ui.Summernote;
import org.gwtbootstrap3.extras.summernote.client.ui.base.Toolbar;
import org.gwtbootstrap3.extras.summernote.client.ui.base.ToolbarButton;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A confirmation dialog with a text area.
 * 
 * @author bzahid
 */
public class TextAreaDialog extends ModalDialogBox implements HasValue<String>{

	private static SetNameDialogUiBinder uiBinder = GWT
			.create(SetNameDialogUiBinder.class);

	/** Tags added automatically by Summernote */
	private final static String SUMMERNOTE_START_TAG = "<p>";
	private final static String SUMMERNOTE_EMPTY_END_TAG = "<br></p>";
	private final static String SUMMERNOTE_END_TAG = "</p>";
	
	interface SetNameDialogUiBinder extends UiBinder<Widget, TextAreaDialog> {
	}
	
	@UiField
	protected Label infoLabel;
	
	@UiField
	protected Label errorLabel;
	
	@UiField(provided=true)
	protected Summernote richTextArea= new Summernote(){
    	
    	@Override
    	protected void onLoad() {
    		super.onLoad();
    		
    		//need to reconfigure the editor or else the blur event doesn't fire properly
    		richTextArea.reconfigure();
    	}
    };
	
	private Button confirmButton = new Button("Ok");	
	private Button cancelButton = new Button("Cancel");
	
	/**
	 * Creates a new dialog
	 * 
	 * @param caption the caption text for the dialog. If null, a generic string will be used.
	 * @param instructions the instruction text above the text box.
	 * @param confirmText the text for the confirm button. If null, a generic string will be used.
	 */
	public TextAreaDialog(String caption, String instructions, String confirmText) {
		
		setWidget(uiBinder.createAndBindUi(this));
		setGlassEnabled(true);
		
		cancelButton.setType(ButtonType.DANGER);
		confirmButton.setType(ButtonType.PRIMARY);
		
		FlowPanel footer = new FlowPanel();
		footer.add(confirmButton);
		footer.add(cancelButton);		
		setFooterWidget(footer);	
		
		if(caption!= null){
			setText(caption);	
		} else {
			setText("Enter Text");	
		}
		
		if(instructions != null){
			infoLabel.setText(instructions);
			
		} else {
			infoLabel.setText("Set Text:");
		}
		
		if(confirmText != null){
			confirmButton.setText(confirmText);
		}
		
		Toolbar defaultToolbar = new Toolbar()
	       .addGroup(ToolbarButton.STYLE)
	       .addGroup(ToolbarButton.BOLD, ToolbarButton.UNDERLINE, ToolbarButton.ITALIC)
	       .addGroup(ToolbarButton.FONT_NAME)
	       .addGroup(ToolbarButton.FONT_SIZE, ToolbarButton.COLOR)
	       .addGroup(ToolbarButton.UL, ToolbarButton.OL, ToolbarButton.PARAGRAPH)
	       .addGroup(ToolbarButton.TABLE)
	       .addGroup(ToolbarButton.LINK, ToolbarButton.VIDEO, ToolbarButton.PICTURE)
	       .addGroup(ToolbarButton.UNDO, ToolbarButton.REDO)
	       .addGroup(ToolbarButton.CODE_VIEW, ToolbarButton.FULL_SCREEN);
		
		richTextArea.setToolbar(defaultToolbar);
		richTextArea.reconfigure();
		
		richTextArea.setDefaultHeight(100);
		
		richTextArea.addSummernoteKeyDownHandler(new SummernoteKeyDownHandler() {
			
			@Override
			public void onSummnernoteKeyDown(SummernoteKeyDownEvent event) {
				
				if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && event.getNativeEvent().getShiftKey()) {
					// nothing to do 
				} else if(event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER && !event.getNativeEvent().getShiftKey()) {
					richTextArea.setCode(richTextArea.getCode().trim());
					confirmButton.click();
				}
			}
		});
		
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
			}
		});
	}
	
	/**
	 * Sets the instruction text
	 * 
	 * @param instructions The message to display above the text area.
	 */
	public void setInstructions(String instructions) {
		infoLabel.setText(instructions);
	}

	/**
	 * Sets the error message to display if the user entered an invalid value. 
	 * 
	 * @param message The error message to display if the user entered an invalid value. 
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
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<String> handler) {
		return addHandler(handler, ValueChangeEvent.getType());
	}

	/**
	 * Gets the text in the text area
	 * 
	 * @return The text in the text area.
	 */
	@Override
	public String getValue() {		
		
		String value = richTextArea.getCode();
		
		if(value.startsWith(SUMMERNOTE_START_TAG) && value.endsWith(SUMMERNOTE_EMPTY_END_TAG)){
			
			//remove the default tags injected by Summernote
			value = value.substring(
					value.indexOf(SUMMERNOTE_START_TAG) + SUMMERNOTE_START_TAG.length(), 
					value.lastIndexOf(SUMMERNOTE_EMPTY_END_TAG)
			);
		} else if(value.startsWith(SUMMERNOTE_START_TAG) && value.endsWith(SUMMERNOTE_END_TAG)){
			
			//remove the default tags injected by Summernote
			value = value.substring(
					value.indexOf(SUMMERNOTE_START_TAG) + SUMMERNOTE_START_TAG.length(), 
					value.lastIndexOf(SUMMERNOTE_END_TAG)
			);
		}
		
		return value;
	}

	/**
	 * Sets the text in the text area
	 * 
	 * @param value The text to display in the text area.
	 */
	@Override
	public void setValue(final String value) {
		
		Scheduler.get().scheduleDeferred(new  ScheduledCommand() {
			
			@Override
			public void execute() {
				richTextArea.setCode(value);
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
		
		if(fireEvents){
			ValueChangeEvent.fire(this, value);
		}
	}
	
	/**
	 * Shows the dialog and sets the focus on the text area.
	 */
	@Override
	public void show(){
		
		super.show();
		
		//Automatically place cursor in text box when shown
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
			
			@Override
			public void execute() {
				
				focusSummernote(richTextArea.getElement());
			}
		});		
	}
	
	@Override
	public void hide(){
		
		//clear the text box's history so the user can't accidentally get a previous value by clicking the 'undo' button
		richTextArea.reset();
		
		super.hide();			
	}

	/*
	 * Focuses on the Summernote editor with the given element.
	 * <br/><br/>
	 * Note: For whatever reason, {@link org.gwtbootstrap3.extras.summernote.client.ui.base.SummernoteBase#setHasFocus(boolean) 
	 * Summernote's setHasFocus(boolean) method} doesn't seem to work properly, so this method exists as a workaround.
	 * 
	 * @param e the element for a Summernote editor
	 */
	/*
	 * Nick - Specifically, I think SummernoteBase.setHasFocus(boolean) is setting the focus on the wrong element. Going into its
	 * Java code reveals that it is using it's getElement() method to get the element it needs to focus on. For most widgets, 
	 * this would be correct, but for the Summernote widget, getElement() actually returns the topmost div for the Summernote editor, 
	 * NOT the actual editable area. In order to set focus on the editable area, we need to use the native Summernote JavaScript API
	 * in order to invoke the 'focus' method on its editor module.
	 */
	private native void focusSummernote(Element e)/*-{
	    $wnd.jQuery(e).summernote('focus');
	}-*/;

}
