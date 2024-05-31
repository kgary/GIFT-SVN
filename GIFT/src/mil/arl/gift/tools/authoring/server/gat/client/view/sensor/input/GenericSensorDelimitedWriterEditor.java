/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input;

import generated.sensor.GenericSensorDelimitedWriter;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class GenericSensorDelimitedWriterEditor.
 */
public class GenericSensorDelimitedWriterEditor extends Composite {
	
	/** The ui binder. */
    interface GenericSensorDelimitedWriterEditorUiBinder extends UiBinder<Widget, GenericSensorDelimitedWriterEditor> {} 
	private static GenericSensorDelimitedWriterEditorUiBinder uiBinder = GWT.create(GenericSensorDelimitedWriterEditorUiBinder.class);
		
	@UiField
	protected TextBox filePrefixTextBox;
	
	@UiField
	protected TextBox delimiterTextBox;
	
	private GenericSensorDelimitedWriter genericSensorDelimitedWriter;
	
    public GenericSensorDelimitedWriterEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
		
		ValueChangeHandler<String> prefixHandler = new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> changeEvent) {
				String prefix = changeEvent.getValue();
				if(prefix.equals("")) {
					genericSensorDelimitedWriter.setFilePrefix(null);
				} else {
					genericSensorDelimitedWriter.setFilePrefix(prefix);
				}
			}
		};
		filePrefixTextBox.addValueChangeHandler(prefixHandler);
		
		ValueChangeHandler<String> delimiterHandler = new ValueChangeHandler<String>(){
			@Override
			public void onValueChange(ValueChangeEvent<String> changeEvent) {
				String delimiter = changeEvent.getValue();
				if(delimiter.equals("")) {
					genericSensorDelimitedWriter.setDatumDelimReplacementChar(null);
				} else {
					genericSensorDelimitedWriter.setDatumDelimReplacementChar(delimiter);
				}
			}
		};
		delimiterTextBox.addValueChangeHandler(delimiterHandler);
    }
    
    public void setGenericSensorDelimitedWriter(GenericSensorDelimitedWriter genericSensorDelimitedWriter) {
    	this.genericSensorDelimitedWriter = genericSensorDelimitedWriter;
    	filePrefixTextBox.setText(genericSensorDelimitedWriter.getFilePrefix());
    	delimiterTextBox.setText(genericSensorDelimitedWriter.getDatumDelimReplacementChar());
    }
}
