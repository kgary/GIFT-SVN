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
 * The Class FilterPane.
 */
public class FilterPane extends Composite {
	
	/** The ui binder. */
    interface FilterPanePaneUiBinder extends UiBinder<Widget, FilterPane> {} 
	private static FilterPanePaneUiBinder uiBinder = GWT.create(FilterPanePaneUiBinder.class);
	
	@UiField
	protected YesNoListBox filterRawSensorDataListBox;
	
    public FilterPane() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        reset();
    }
    
    public void reset() {
    	filterRawSensorDataListBox.setValue(true);
    }
    
    public boolean isFilterRawSensorData() {
    	return filterRawSensorDataListBox.getValue();
    }
}
