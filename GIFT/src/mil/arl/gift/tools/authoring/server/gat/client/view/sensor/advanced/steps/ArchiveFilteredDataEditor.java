/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps;

import java.io.Serializable;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import generated.sensor.GenericSensorDelimitedWriter;
import generated.sensor.KinectSensorWriter;
import generated.sensor.SensorsConfiguration;
import generated.sensor.Writer;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.WriterTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.GenericSensorDelimitedWriterEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.input.KinectSensorWriterEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.WriterTypeValueListBox;

/**
 * The Class ArchiveFilteredDataEditor.
 */
public class ArchiveFilteredDataEditor extends Composite {
	
	/** The ui binder. */
    interface ArchiveFilteredDataEditorUiBinder extends UiBinder<Widget, ArchiveFilteredDataEditor> {} 
	private static ArchiveFilteredDataEditorUiBinder uiBinder = GWT.create(ArchiveFilteredDataEditorUiBinder.class);
	
	@UiField
	protected HTML caption;
	
	@UiField
	protected WriterTypeValueListBox writerTypeListBox;
	
	@UiField
	protected GenericSensorDelimitedWriterEditor genericSensorDelimitedWriterEditor;
	
	@UiField
	protected KinectSensorWriterEditor kinectSensorWriterEditor;
	
	@UiField
	protected Label initializationWarningLabel;
	
	@UiField
	protected Label referenceWarningLabel;
	
	private Writer writer;
	
    public ArchiveFilteredDataEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
        
		//If the user selects a new writer type then that change needs to
		//cascade to the writer object.
		ValueChangeHandler<WriterTypeEnum> writerTypeHandler = new ValueChangeHandler<WriterTypeEnum>(){
			@Override
			public void onValueChange(ValueChangeEvent<WriterTypeEnum> changeEvent) {
				//Figure out what writer type the user selected.
				WriterTypeEnum writerType = changeEvent.getValue();
				
				//Convert that writerType to a writerImpl and save it in the writer object.
				String writerImpl = SensorsConfigurationMaps.getInstance().getWriterTypeToImplMap().get(writerType);
				writer.setWriterImpl(writerImpl);
				
				//Construct an input object that corresponds to that writer type.
				Serializable writerInput = SensorsConfigurationFactory.createWriterInputType(writerType);
				writer.getWriterInput().setType(writerInput);
				
				//Display an editor for the input object.
				showInputEditor();
				
				//If the warnings were visible then make sure we remove them when
				//the user selects a new writer type and we automatically
				//pair it with a new input.
				initializationWarningLabel.setVisible(false);
				referenceWarningLabel.setVisible(false);
			}
		};
		writerTypeListBox.addValueChangeHandler(writerTypeHandler);
    }
    
    public void setWriter(Writer writer, SensorsConfiguration sensorsConfiguration) {
    	this.writer = writer;
    	
    	//Select the writer type from the list that corresponds to the writer impl. 
    	SensorsConfigurationMaps sensorConfigurationMaps = SensorsConfigurationMaps.getInstance();
    	String writerImpl = writer.getWriterImpl();
    	WriterTypeEnum writerType = sensorConfigurationMaps.getWriterImplToTypeMap().get(writerImpl);
    	writerTypeListBox.setValue(writerType);
    	
    	//Create a default input object that should be used for this type of writer
    	//and grab the actual input object being used. We'll compare them next.
    	Serializable defaultInput = SensorsConfigurationFactory.createWriterInputType(writerType);
    	Serializable actualInput = writer.getWriterInput().getType();
    		
    	//If the expected input type matches with the actual input type then we 
    	//just have to make sure the warning label isn't on screen.
    	if( 	(actualInput == null && defaultInput == null) ||
    			(actualInput != null && defaultInput != null && actualInput.getClass() == defaultInput.getClass())) {
    		initializationWarningLabel.setVisible(false);
    	}
    	//Otherwise we have to replace the invalid existing input with
    	//something valid and warn the user about the change we've made.
    	else {    		
    		writer.getWriterInput().setType(defaultInput);
    		actualInput = defaultInput;
    		
    		initializationWarningLabel.setVisible(true);
    	}
    	
    	//Let the user know if this Writer is referenced by more than one Sensor or Filter.
    	if(SensorsConfigurationUtility.isWriterReferencedMoreThanOnce(sensorsConfiguration, writer.getId())) {
    		referenceWarningLabel.setVisible(true);
    	} else {
    		referenceWarningLabel.setVisible(false);
    	}
    	
    	//Show the appropriate editor for the input object.
    	showInputEditor();
    }
    
    public void setTitle() {
    	caption.setText("Archive Filtered Data");
    }
    
    private void showInputEditor() {
    	Serializable input = writer.getWriterInput().getType();

    	if(input instanceof GenericSensorDelimitedWriter) {
    		GenericSensorDelimitedWriter genericSensorDelimitedWriter = (GenericSensorDelimitedWriter)input;
    		genericSensorDelimitedWriterEditor.setGenericSensorDelimitedWriter(genericSensorDelimitedWriter);
    		genericSensorDelimitedWriterEditor.setVisible(true);
    	} else {
    		genericSensorDelimitedWriterEditor.setVisible(false);
    	}
    	
    	if(input instanceof KinectSensorWriter) {
    		KinectSensorWriter kinectSensorWriter = (KinectSensorWriter)input;
    		kinectSensorWriterEditor.setKinectSensorWriter(kinectSensorWriter);
    		kinectSensorWriterEditor.setVisible(true);
    	} else {
    		kinectSensorWriterEditor.setVisible(false);
    	}
    }
}
