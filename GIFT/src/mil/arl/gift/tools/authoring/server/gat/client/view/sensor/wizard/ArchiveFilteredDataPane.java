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
 * The Class ArchiveFilteredDataPane.
 */
public class ArchiveFilteredDataPane extends Composite {
	
	/** The ui binder. */
    interface ArchiveFilteredDataPaneUiBinder extends UiBinder<Widget, ArchiveFilteredDataPane> {} 
	private static ArchiveFilteredDataPaneUiBinder uiBinder = GWT.create(ArchiveFilteredDataPaneUiBinder.class);
	
	@UiField
	protected YesNoListBox archiveFilteredDataListBox;
	
	@UiField
	protected Label useDefaultArchiveDataLabel;
	
	@UiField
	protected YesNoListBox useDefaultArchiveDataListBox;
	
    public ArchiveFilteredDataPane() {		
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
        archiveFilteredDataListBox.addValueChangeHandler(handler);
    }
    
    public void reset() {
    	archiveFilteredDataListBox.setValue(true);
        useDefaultArchiveDataListBox.setValue(true);
    }
    
    public boolean isArchiveFilteredData() {
    	return archiveFilteredDataListBox.getValue();
    }
    
    public boolean isUseDefaultArchiveData() {
    	return useDefaultArchiveDataListBox.getValue();
    }
}
