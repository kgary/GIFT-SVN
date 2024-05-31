/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap;

import mil.arl.gift.tools.dashboard.client.bootstrap.ExperimentBuildReportDialogWidget.DataTypeEntry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget used to select data types for an experiment report
 * 
 * @author nroberts
 */
public class ReportDataTypeWidget extends Composite implements HasValue<Boolean>{

	private static ReportDataTypeWidgetUiBinder uiBinder = GWT
			.create(ReportDataTypeWidgetUiBinder.class);

	interface ReportDataTypeWidgetUiBinder extends
			UiBinder<Widget, ReportDataTypeWidget> {
	}
	
	/** The data type this widget represents */
	private DataTypeEntry dataType;
	
	@UiField
	protected CheckBox selectedCheck;

	/**
	 * Creates a new widget representing the given data type
	 * 
	 * @param dataType
	 */
	public ReportDataTypeWidget(DataTypeEntry dataType) {
		initWidget(uiBinder.createAndBindUi(this));
		
		this.dataType = dataType;
		
		selectedCheck.setText(dataType.getDisplayName() != null ? dataType.getDisplayName() : dataType.getName());
		
		if(dataType.getDescription() != null){
			selectedCheck.setTitle(dataType.getDescription());
		}
	}
	
	/**
	 * Gets the data type this widget represents
	 * 
	 * @return the data type
	 */
	public DataTypeEntry getDataTypeEntry(){
		return dataType;
	}

	@Override
	public HandlerRegistration addValueChangeHandler(
			ValueChangeHandler<Boolean> handler) {
		return selectedCheck.addValueChangeHandler(handler);
	}

	@Override
	public Boolean getValue() {		
		return selectedCheck.getValue();
	}

	@Override
	public void setValue(Boolean value) {
		selectedCheck.setValue(value);
	}

	@Override
	public void setValue(Boolean value, boolean fireEvents) {
		selectedCheck.setValue(value, fireEvents);
	}

}
