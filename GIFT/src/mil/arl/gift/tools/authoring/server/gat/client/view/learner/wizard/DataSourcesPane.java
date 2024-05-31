/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.wizard;

import generated.learner.Sensor;
import generated.learner.TrainingAppState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets.DataSourceDataGrid;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.WarningDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 * GUI that contains the DataSourceDataGrid and buttons to add/remove data
 * sources to the data grid.
 * @author elafave
 */
public class DataSourcesPane extends Composite {
	
	/** The ui binder. */
    interface DataSourcesPaneUiBinder extends UiBinder<Widget, DataSourcesPane> {} 
	private static DataSourcesPaneUiBinder uiBinder = GWT.create(DataSourcesPaneUiBinder.class);
	
	@UiField
	protected DataSourceDataGrid dataSourceDataGrid;
	
	@UiField
	protected Button addSensorButton;
	
	@UiField
	protected Button addTrainingApplicationButton;
	
	@UiField
	protected Button removeDataSourceButton;
	
    public DataSourcesPane() {		
        initWidget(uiBinder.createAndBindUi(this));
        reset();
        
        ClickHandler addSensorHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				Sensor sensor = new Sensor();
				sensor.setType(SensorTypeEnum.BIOHARNESS.getName());
				
				dataSourceDataGrid.addDataSource(sensor, true);
			}
        };
        addSensorButton.addClickHandler(addSensorHandler);
    
        ClickHandler addTrainingApplicationClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				TrainingAppState trainingAppState = new TrainingAppState();
				trainingAppState.setType(MessageTypeEnum.COLLISION.getName());

				dataSourceDataGrid.addDataSource(trainingAppState, true);
			}
        };
        addTrainingApplicationButton.addClickHandler(addTrainingApplicationClickHandler);
        
        ClickHandler removeDataSourceClickHandler = new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				if(dataSourceDataGrid.getDataSources().size() == 1) {
					WarningDialog.warning("Data source needed", "You must have at least one data source therefore it cannot be removed.");
					return;
				}
				
				Serializable removedDataSource = dataSourceDataGrid.removeSelectedDataSource();
				if(removedDataSource == null) {
					WarningDialog.warning("Missing selection", "Please select the data source to remove before requesting it be removed.");
					return;
				}
			}
        };
        removeDataSourceButton.addClickHandler(removeDataSourceClickHandler);
    }
    
    public ArrayList<Serializable> getDataSources() {
    	return dataSourceDataGrid.getDataSources();
    }
    
    /**
     * Replaces the collection of data sources with a new, single data source.
     */
    public void reset() {
    	LearnerConfigurationMaps learnerConfigurationMaps = LearnerConfigurationMaps.getInstance();
    	HashSet<SensorTypeEnum> acceptableSensorTypes = learnerConfigurationMaps.getSensorTypes();
    	SensorTypeEnum sensorType = acceptableSensorTypes.iterator().next();
    	
    	Sensor sensor = new Sensor();
    	sensor.setType(sensorType.getName());
    	
    	ArrayList<Serializable> dataSources = new ArrayList<Serializable>();
    	dataSources.add(sensor);
    	
    	dataSourceDataGrid.setDataSources(dataSources);
    }

	/**
	 * Redraws this widget, particularly its table. 
	 */
	public void redraw() {
		dataSourceDataGrid.redraw();
	}
}
