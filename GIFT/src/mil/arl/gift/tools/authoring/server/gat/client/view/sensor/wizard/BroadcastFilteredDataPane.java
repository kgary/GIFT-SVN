/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.YesNoListBox;

/**
 * The Class BroadcastFilteredDataPane.
 */
public class BroadcastFilteredDataPane extends Composite {
	
	/** The ui binder. */
    interface BroadcastFilteredDataPaneUiBinder extends UiBinder<Widget, BroadcastFilteredDataPane> {} 
	private static BroadcastFilteredDataPaneUiBinder uiBinder = GWT.create(BroadcastFilteredDataPaneUiBinder.class);
	
	@UiField
	protected YesNoListBox broadcastFilteredDataListBox;
	
    public BroadcastFilteredDataPane() {		
        initWidget(uiBinder.createAndBindUi(this));
        reset();
    }
    
    public void reset() {
    	broadcastFilteredDataListBox.setValue(true);
    }
    
    public boolean isBroadcastRawSensorData() {
    	return broadcastFilteredDataListBox.getValue();
    }
}
