/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.learner.widgets;

import generated.learner.Sensor;
import generated.learner.TrainingAppState;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.enums.SensorTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.learner.LearnerConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.cell.ExtendedSelectionCell;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * Data grid to display data sources (Sensors and Training Applications). There
 * are many different types of Sensors and Training Applications and this grid
 * gives the user the ability to dynamically select which they want.
 * 
 * @author elafave
 *
 */
public class DataSourceDataGrid extends DataGrid<Serializable>{
	
	private ListDataProvider<Serializable> dataProvider = new ListDataProvider<Serializable>();
    
    private ArrayList<String> availableTrainingApplications = new ArrayList<String>();
    
    private ArrayList<String> availableSensors = new ArrayList<String>();
    
    private ExtendedSelectionCell typeCell = new ExtendedSelectionCell();
    
    private SingleSelectionModel<Serializable> selectionModel = new SingleSelectionModel<Serializable>();
    
    private ArrayList<DataSourceModifiedHandler> dataSourceModifiedHandlers = new ArrayList<DataSourceModifiedHandler>();
    
    public DataSourceDataGrid() {
    	populateAvailableChoices();
        
    	Column<Serializable, String> sourceColumn = new Column<Serializable, String>(new TextCell()) {
			@Override
			public String getValue(Serializable dataSource) {
				if(dataSource instanceof Sensor) {
					return "Sensor";
				} else if(dataSource instanceof TrainingAppState) {
					return "Training Application";
				} else {
					return "Error";
				}
			}
        };
        
        Column<Serializable, String> typeColumn = new Column<Serializable, String>(typeCell) {
			@Override
			public String getValue(Serializable dataSource) {
				if(dataSource instanceof Sensor) {
					String type = ((Sensor)dataSource).getType();
					SensorTypeEnum sensorTypeEnum = SensorTypeEnum.valueOf(type);
					return sensorTypeEnum.getDisplayName();
				}
				else if(dataSource instanceof TrainingAppState) {
					String type = ((TrainingAppState)dataSource).getType();
					MessageTypeEnum messageTypeEnum = MessageTypeEnum.valueOf(type);
					return messageTypeEnum.getDisplayName();
				} else {
					return "Error";
				}
			}
        };
        typeColumn.setFieldUpdater(new FieldUpdater<Serializable, String>() {
        	@Override
        	public void update(int index, Serializable dataSource, String value) {
        		if(dataSource instanceof Sensor) {
        			SensorTypeEnum sensorTypeEnum = SensorTypeEnum.valueOf(value);
					String type = sensorTypeEnum.getName();
					((Sensor)dataSource).setType(type);
        		}
        		else if(dataSource instanceof TrainingAppState) {
					MessageTypeEnum messageTypeEnum = MessageTypeEnum.valueOf(value);
					String type = messageTypeEnum.getName();
					((TrainingAppState)dataSource).setType(type);
				}
				
				//Let the interested parties know a data source has been modified.
				for(DataSourceModifiedHandler dataSourceModifiedHandler : dataSourceModifiedHandlers) {
					dataSourceModifiedHandler.onDataSourceModified(dataSource);
				}
        	}        
        });
    	
        addColumn(sourceColumn, "Source");
        setColumnWidth(sourceColumn, "40%");
        addColumn(typeColumn, "Type");
        setColumnWidth(typeColumn, "60%");
        setSelectionModel(selectionModel);
        
        dataProvider.addDataDisplay(this);
    }
    
    /**
     * Register to receive notifications when the user changes the type of a
     * data source.
     * @param handler Handler to callback when a change happens.
     */
    public void addDataSourceModifiedHandler(DataSourceModifiedHandler handler) {
    	dataSourceModifiedHandlers.add(handler);
    }
    
    /**
     * If the supplied dataSource is a Sensor or Training Application then it
     * is added to the data grid. If instructed, it will also be auto-selected.
     * @param dataSource The Sensor or Training Application to add.
     * @param select True if the row should be auto-selected, false otherwise.
     * @return True if the data source could be added, false otherwise.
     */
    public boolean addDataSource(Serializable dataSource, boolean select) {
    	//Tell the type cell which options it should give the user based on
    	//what type of data source it is.
    	//
    	//NOTE: The ExtendedSelectionCell seems to keep track of the options on
    	//a per row basis. However, when I delete a row I never tell the
    	//ExtendedSelectionCell it can forget about that row. I suspect this
    	//results in a memory leak but based on my schedule it wasn't
    	//significant enough for me to further investigate.
    	if(dataSource instanceof Sensor) {
    		typeCell.setRowOptions(dataSource, availableSensors);
    	} else if(dataSource instanceof TrainingAppState){
    		typeCell.setRowOptions(dataSource, availableTrainingApplications);
    	} else {
    		return false;
    	}
    	
    	//Add the new data source to the grid.
    	dataProvider.getList().add(dataSource);
    	
    	//Make sure the newly added item is selected.
    	if(select) {
    		selectionModel.setSelected(dataSource, true);
    	}
    	
    	return true;
    }
    
    /**
     * Removes all of the data sources and replaces them with the supplied
     * dataSources
     * @param dataSources Collection of Sensors and Training Applications to
     * display in the data grid.
     */
    public void setDataSources(List<Serializable> dataSources) {
    	dataProvider.getList().clear();

    	boolean emptyList = true;
    	for(Serializable dataSource : dataSources) {
    		emptyList = !addDataSource(dataSource, emptyList);
    	}
    }
    
    /**
     * 
     * @return The selected Sensor or Training Application from the data grid.
     */
    public Serializable getSelectedDataSource() {
    	Serializable dataSource = selectionModel.getSelectedObject();
    	return dataSource;
    }
    
    /**
     * Removes the selected data source from the grid and if possible auto
     * selects the next available data source.
     * @return The Sensor or Training Application that was removed, if the grid
     * was already empty then null is returned.
     */
    public Serializable removeSelectedDataSource() {
    	//If nothing is selected then we can't remove anything.
    	Serializable dataSource = getSelectedDataSource();
    	if(dataSource == null) {
    		return null;
    	}
    	
    	//Remove the selected item.
    	List<Serializable> dataSources = dataProvider.getList();
    	int index = dataSources.indexOf(dataSource);
    	dataSources.remove(dataSource);
    	
    	//If possible select the data source that was after the data source we
    	//removed, otherwise select the data source that came before.
    	if(index < dataSources.size()) {
    		Serializable nextDataSource = dataSources.get(index);
    		selectionModel.setSelected(nextDataSource, true);
    	} else {
    		Serializable previousDataSource = dataSources.get(index - 1);
    		selectionModel.setSelected(previousDataSource, true);
    	}
    	
    	return dataSource;
    }
    
    /**
     * 
     * @return Collection of data sources displayed in this grid. Note that
     * modifying this collection will not cause the data grid to be modified.
     */
    public ArrayList<Serializable> getDataSources() {
    	//I return this list in its own ArrayList so people are forced to go
    	//through our API for adding/removing elements.
    	ArrayList<Serializable> dataSources = new ArrayList<Serializable>();
    	dataSources.addAll(dataProvider.getList());
    	return dataSources;
    }
    
    private void populateAvailableChoices() {
    	HashSet<MessageTypeEnum> trainingApplicationTypes = LearnerConfigurationMaps.getInstance().getTrainingApplicationTypes();
    	for(MessageTypeEnum trainingApplicationType : trainingApplicationTypes) {
    		availableTrainingApplications.add(trainingApplicationType.getDisplayName());
    	}
        Collections.sort(availableTrainingApplications);
        
        HashSet<SensorTypeEnum> sensorTypes = LearnerConfigurationMaps.getInstance().getSensorTypes();
    	for(SensorTypeEnum sensorType : sensorTypes) {
    		availableSensors.add(sensorType.getDisplayName());
    	}
        Collections.sort(availableSensors);
    }
}
