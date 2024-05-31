/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced;

import generated.sensor.Sensor;
import mil.arl.gift.tools.authoring.server.gat.client.GatClientBundle;
import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;

import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.google.web.bindery.event.shared.binder.EventBinder;

/**
 * The Class SensorList.
 */
public class SensorList extends Composite {
	
	/**
	 * The Interface MyEventBinder.
	 */
	interface MyEventBinder extends EventBinder<SensorList> {
	}
	
	/** The Constant eventBinder. */
	private static final MyEventBinder eventBinder = GWT
			.create(MyEventBinder.class);
	
	/** The event registration. */
	protected HandlerRegistration eventRegistration;
	
	/** The ui binder. */
    interface SensorListUiBinder extends UiBinder<Widget, SensorList> {} 
	private static SensorListUiBinder uiBinder = GWT.create(SensorListUiBinder.class);
	
	/** Provides the sensor data to the dataGrid */
	private ListDataProvider<Sensor> dataProvider = new ListDataProvider<Sensor>();
	
	/** The data grid. */
    @UiField
    public CellTable<Sensor> dataGrid;
    
    /** The name column for the sensor list */
    private Column<Sensor, String> nameColumn = new Column<Sensor, String>(new TextCell()) {
		@Override
		public String getValue(Sensor sensor) {
			return sensor.getName();
		}
    };
    
    private Column<Sensor, String> removeColumn = new Column<Sensor, String>(new ButtonCell(){
    	@Override
    	public void render(com.google.gwt.cell.client.Cell.Context context, 
	            String value, SafeHtmlBuilder sb) {
			
	        SafeHtml html = SafeHtmlUtils.fromTrustedString(new Image(value).toString());
	        sb.append(html);
	    }
    }) {
    	@Override
		public String getValue(Sensor sensor) {
    		return GatClientBundle.INSTANCE.cancel_image().getSafeUri().asString();
		}
    };
       
    /** The selection model. */
    protected SingleSelectionModel<Sensor> selectionModel = new SingleSelectionModel<Sensor>();
    
    public SensorList() {		
        initWidget(uiBinder.createAndBindUi(this));
        
        eventRegistration = eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());

        nameColumn.setSortable(true);
        
        dataGrid.addColumn(nameColumn);
        dataGrid.addColumn(removeColumn);
        dataGrid.setColumnWidth(nameColumn, "100%");        
        dataGrid.setColumnWidth(removeColumn, "0%");
        dataGrid.setSelectionModel(selectionModel);
        
        dataGrid.setEmptyTableWidget(new HTML(""
    			+ "<div style='text-align: center; color:red; font-weight: bold; padding-top: 10px;'>"
    			+ 		"Please add a sensor."
    			+ "</div>"
    	));
        
        dataProvider.addDataDisplay(dataGrid);
    }
    
    public void setSensors(List<Sensor> sensors) {
    	
    	//Why don't we call the setList method?
    	//Nick: because if we then make changes to the data provider's list, those changes will not be propagated to the data grid. The JavaDoc
    	//for ListDataProvider specifically mentions this and recommends using getList() instead.
    	dataProvider.getList().clear();
    	dataProvider.getList().addAll(sensors);
    	
    	if(!dataProvider.getList().isEmpty()){
    		selectionModel.setSelected(sensors.get(0), true);
    	}
    }
    
    public void addSensor(Sensor sensor) {
    	dataProvider.getList().add(sensor);
		selectionModel.setSelected(sensor, true);
    }
    
    public Column<Sensor, String> getRemoveColumn() {
    	return removeColumn;
    }
    
    public Sensor getSelectedSensor() {
    	Sensor selectedSensor = selectionModel.getSelectedObject();
    	return selectedSensor;
    }
    
    public Sensor removeSelectedSensor() {
    	
    	List<Sensor> sensors = dataProvider.getList();
    	
    	//Remove the selected sensor
    	Sensor removedSensor = getSelectedSensor();
    	int index = sensors.indexOf(removedSensor);
    	sensors.remove(removedSensor);
    	
    	if(!sensors.isEmpty()){
    		
	    	//If possible select the sensor that was after the sensor we removed,
	    	//otherwise select the sensor that came before.
	    	if(index < sensors.size()) {
	    		Sensor nextSensor = sensors.get(index);
	    		selectionModel.setSelected(nextSensor, true);
	    	} else {
	    		Sensor previousSensor = sensors.get(index - 1);
	    		selectionModel.setSelected(previousSensor, true);
	    	}
	    	
    	}
    	
    	return removedSensor;
    }
    
    public void addSelectionChangeHandler(SelectionChangeEvent.Handler changeHandler) {
    	selectionModel.addSelectionChangeHandler(changeHandler);
    }
	
	public void refresh(){
		dataProvider.refresh();
	}

}
