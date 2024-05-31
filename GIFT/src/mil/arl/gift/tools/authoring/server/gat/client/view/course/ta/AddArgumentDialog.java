/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.ta;

import generated.course.Nvpair;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.constants.ButtonType;
import org.gwtbootstrap3.client.ui.gwt.FlowPanel;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.ModalDialogBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * A dialog used to add and edit training application arguments
 * 
 * @author nroberts
 */
public class AddArgumentDialog extends ModalDialogBox implements HasValue<Nvpair>{

	private static AddFeedbackDialogUiBinder uiBinder = GWT
			.create(AddFeedbackDialogUiBinder.class);

	interface AddFeedbackDialogUiBinder extends
			UiBinder<Widget, AddArgumentDialog> {
	}
	
	private static final String ADD_ARG_TITLE = "Add Argument";
	private static final String VIEW_ARG_TITLE = "View Argument";
	
	@UiField
	protected TextBox nameTextBox;
	
	@UiField
	protected TextBox valueTextBox;
	
	/** whether this dialog should be in read only mode */
	private boolean isReadOnly = false;
	
	
	private Button confirmButton = new Button("Save Argument");	
	private Button cancelButton = new Button("Cancel");
	
	private Nvpair value = null;

	/**
	 * Creates a new dialog for adding and editing arguments
	 */
	public AddArgumentDialog() {
		setWidget(uiBinder.createAndBindUi(this));
		
		setGlassEnabled(true);
		setWidth("600px");
		
		setText(ADD_ARG_TITLE);		
		
		setEnterButton(confirmButton);
		
		cancelButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		confirmButton.setType(ButtonType.PRIMARY);
		cancelButton.setType(ButtonType.DANGER);
		
		FlowPanel footer = new FlowPanel();
		footer.add(confirmButton);
		footer.add(cancelButton);
		
		setFooterWidget(footer);	
		
		confirmButton.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
			    
			    if(isReadOnly){
			        //in case they changed the DOM to click the button
			        return;
			    }
				
				Nvpair pair = new Nvpair();
				
				if(nameTextBox.getValue() == null || nameTextBox.getValue().isEmpty()){
					
					WarningDialog.alert("Missing value", "Please enter a key name for this argument.");
					return;
				}
				
				if(valueTextBox.getValue() == null || valueTextBox.getValue().isEmpty()){
					
					WarningDialog.alert("Missing value", "Please enter a value for this argument.");
					return;
				}
				
				pair.setName(nameTextBox.getValue());
				pair.setValue(valueTextBox.getValue());
				
				value = pair;
				
				ValueChangeEvent.fire(AddArgumentDialog.this, value);
			}
		});
		
	}
	
	@Override
	public void setValue(Nvpair value){
		
		Nvpair pair = value;
		
		if(pair == null){
			pair = new Nvpair();
		}
		
		nameTextBox.setValue(pair.getName());
		valueTextBox.setValue(pair.getValue());
		
		this.value = pair;
	}
	
	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Nvpair> handler) {
		
		return addHandler(handler, ValueChangeEvent.getType());
	}

	@Override
	public Nvpair getValue() {
		return value;
	}

	@Override
	public void setValue(Nvpair value, boolean arg1) {
		setValue(value);
		
		ValueChangeEvent.fire(this, value);
	}
	
	/**
	 * Set whether the GAT is in read only mode.
	 * 
	 * @param readOnly whether the GAT is in read only mode.  Causes
	 * the dialog to be changed.
	 */
	public void setReadOnly(boolean readOnly){
	    
	    this.isReadOnly = readOnly;
	    refresh();
	}
	
	/**
	 * Applies read only UI logic or undoes the logic if not in read only mode.
	 */
	private void refresh(){
	    
	    if(isReadOnly){
	        confirmButton.setVisible(false);
	        setText(VIEW_ARG_TITLE); 
	        nameTextBox.setEnabled(false);
	        valueTextBox.setEnabled(false);
	    }else{
	        confirmButton.setVisible(true);
	        setText(ADD_ARG_TITLE);  
            nameTextBox.setEnabled(true);
            valueTextBox.setEnabled(true);
	    }
	}
}
