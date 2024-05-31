/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.YesNoListBox;

/**
 * The Class ArchiveRawSensorDataPane.
 */
public class ArchiveRawSensorDataPane extends Composite {
	
	/** The ui binder. */
    interface ArchiveRawSensorDataPaneUiBinder extends UiBinder<Widget, ArchiveRawSensorDataPane> {} 
	private static ArchiveRawSensorDataPaneUiBinder uiBinder = GWT.create(ArchiveRawSensorDataPaneUiBinder.class);
	
	@UiField
	protected YesNoListBox archiveRawSensorDataListBox;
	
	@UiField
	protected Label useDefaultArchiveDataLabel;
	
	@UiField
	protected YesNoListBox useDefaultArchiveDataListBox;
	
    public ArchiveRawSensorDataPane() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        reset();
        
        ValueChangeHandler<Boolean> handler = new ValueChangeHandler<Boolean>() {
    		@Override
			public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
				boolean visibility = changeEvent.getValue();
				useDefaultArchiveDataLabel.setVisible(visibility);
				useDefaultArchiveDataListBox.setVisible(visibility);
			}
        };
        archiveRawSensorDataListBox.addValueChangeHandler(handler);
    }
    
    public void reset() {
    	archiveRawSensorDataListBox.setValue(false);
    	
    	useDefaultArchiveDataLabel.setVisible(false);
		useDefaultArchiveDataListBox.setVisible(false);
    	useDefaultArchiveDataListBox.setValue(true);
    }
    
    public boolean isArchiveRawSensorData() {
    	return archiveRawSensorDataListBox.getValue();
    }
    
    public boolean isUseDefaultArchiveData() {
    	return useDefaultArchiveDataListBox.getValue();
    }
}
