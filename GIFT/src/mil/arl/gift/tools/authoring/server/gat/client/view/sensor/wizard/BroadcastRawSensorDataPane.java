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
 * The Class BroadcastRawSensorDataPane.
 */
public class BroadcastRawSensorDataPane extends Composite {
	
	/** The ui binder. */
    interface BroadcastRawSensorDataPaneUiBinder extends UiBinder<Widget, BroadcastRawSensorDataPane> {} 
	private static BroadcastRawSensorDataPaneUiBinder uiBinder = GWT.create(BroadcastRawSensorDataPaneUiBinder.class);
	
	@UiField
	protected YesNoListBox broadcastRawSensorDataListBox;
	
    public BroadcastRawSensorDataPane() {		
        initWidget(uiBinder.createAndBindUi(this));
    }
    
    public boolean isBroadcastRawSensorData() {
    	return broadcastRawSensorDataListBox.getValue();
    }
}
