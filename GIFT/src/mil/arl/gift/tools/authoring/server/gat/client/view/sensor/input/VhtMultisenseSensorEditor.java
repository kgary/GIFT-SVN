/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.VhtMultisenseSensor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.LongBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class VhtMultisenseSensorEditor.
 */
public class VhtMultisenseSensorEditor extends Composite {
	
	/** The ui binder. */
    interface VhtMultisenseSensorEditorUiBinder extends UiBinder<Widget, VhtMultisenseSensorEditor> {} 
	private static VhtMultisenseSensorEditorUiBinder uiBinder = GWT.create(VhtMultisenseSensorEditorUiBinder.class);
	
	@UiField
	protected LongBox datalessWarningDelayLongBox;
	
	@UiField
	protected TextBox activeMqUrlTextBox;
	
	@UiField
	protected TextBox activeMqTopicTextBox;
	
	private VhtMultisenseSensor vhtMultisenseSensor;
	
    public VhtMultisenseSensorEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        ValueChangeHandler<Long> datalessWarningDelayHandler = new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> changeEvent) {
				Long datalessWarningDelay = changeEvent.getValue();
				if(datalessWarningDelay == null) {
					//Allow the user to delete the text since it is an optional field.
					if(datalessWarningDelayLongBox.getText().equals("")) {
						vhtMultisenseSensor.setDatalessWarningDelay(null);
					} else {
						populateDatalessWarningDelayLongBox();
					}
				} else {
					vhtMultisenseSensor.setDatalessWarningDelay(datalessWarningDelay);
					//If the user enters a decimal it is rounded so refresh the
					//GUI with the rounded value.
					populateDatalessWarningDelayLongBox();
				}
			}
		};
		datalessWarningDelayLongBox.addValueChangeHandler(datalessWarningDelayHandler);
		
		ValueChangeHandler<String> urlHandler = new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> changeEvent) {
				String url = changeEvent.getValue();
				vhtMultisenseSensor.setVhtActiveMqUrl(url);
			}
		};
		activeMqUrlTextBox.addValueChangeHandler(urlHandler);
		
		ValueChangeHandler<String> topicHandler = new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> changeEvent) {
				String topic = changeEvent.getValue();
				if(topic.isEmpty()) {
					vhtMultisenseSensor.setVhtActiveMqTopic(topic);
				}
			}
		};
		activeMqTopicTextBox.addValueChangeHandler(topicHandler);
    }
    
    public void setVhtMultisenseSensor(VhtMultisenseSensor vhtMultisenseSensor) {
    	this.vhtMultisenseSensor = vhtMultisenseSensor;
    	populateDatalessWarningDelayLongBox();
    	activeMqUrlTextBox.setText(vhtMultisenseSensor.getVhtActiveMqUrl());
    	activeMqTopicTextBox.setText(vhtMultisenseSensor.getVhtActiveMqTopic());
    }
    
    private void populateDatalessWarningDelayLongBox() {
    	Long datalessWarningDelay = vhtMultisenseSensor.getDatalessWarningDelay();
    	if(datalessWarningDelay == null) {
    		datalessWarningDelayLongBox.setText("");
    	} else {
    		datalessWarningDelayLongBox.setValue(datalessWarningDelay);
    	}
    }
}
