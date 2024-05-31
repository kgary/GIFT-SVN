/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.wizard;

import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.SensorTypeValueListBox;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.widgets.YesNoListBox;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class SelectSensorTypePane.
 */
public class SelectSensorTypePane extends Composite {
	
	/** The ui binder. */
    interface SelectSensorTypePaneUiBinder extends UiBinder<Widget, SelectSensorTypePane> {} 
	private static SelectSensorTypePaneUiBinder uiBinder = GWT.create(SelectSensorTypePaneUiBinder.class);
	
	@UiField
	protected SensorTypeValueListBox sensorTypeListBox;
	
	@UiField
	protected YesNoListBox useDefaultsListBox;
	
    public SelectSensorTypePane() {		
        initWidget(uiBinder.createAndBindUi(this));
        reset();
    }
    
    public SensorTypeEnum getSensorType() {
    	return sensorTypeListBox.getValue();
    }
    
    public boolean isUsingDefaults() {
    	return useDefaultsListBox.getValue();
    }
    
    public void reset() {
    	sensorTypeListBox.setValue(SensorTypeEnum.BIOHARNESS);
    	useDefaultsListBox.setValue(true);
    }
}
