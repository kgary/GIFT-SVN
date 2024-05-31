/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.sensor.advanced.steps;

import generated.sensor.BooleanEnum;
import generated.sensor.Filter;
import generated.sensor.SensorsConfiguration;
import generated.sensor.Writer;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.FilterTypeEnum;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationFactory;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationMaps;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationUtility;

import java.math.BigInteger;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * The Class TransmitFilteredDataEditor.
 */
public class TransmitFilteredDataEditor extends Composite {
	
	/** The ui binder. */
    interface TransmitFilteredDataEditorUiBinder extends UiBinder<Widget, TransmitFilteredDataEditor> {} 
	private static TransmitFilteredDataEditorUiBinder uiBinder = GWT.create(TransmitFilteredDataEditorUiBinder.class);
	
	@UiField
	protected HTML caption;
	
	@UiField
	protected CheckBox distributeExternallyCheckBox;
	
	@UiField
	protected CheckBox writerCheckBox;
	
	private Filter filter;
	
	private SensorsConfiguration sensorsConfiguration;
	
    public TransmitFilteredDataEditor() {		
        initWidget(uiBinder.createAndBindUi(this));
		
		ValueChangeHandler<Boolean> distributeExternallyHandler = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
				if(changeEvent.getValue()) {
					filter.setDistributeExternally(BooleanEnum.TRUE);
				} else {
					filter.setDistributeExternally(BooleanEnum.FALSE);
				}
			}
		};
		distributeExternallyCheckBox.addValueChangeHandler(distributeExternallyHandler);
		
		ValueChangeHandler<Boolean> writerHandler = new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> changeEvent) {
				if(changeEvent.getValue()) {
					//Construct a new Writer appropriate for the filterImpl.
					String filterImpl = filter.getFilterImpl();
					FilterTypeEnum filterType = SensorsConfigurationMaps.getInstance().getFilterImplToTypeMap().get(filterImpl);
					Writer writer = SensorsConfigurationFactory.createWriter(filterType);
					
					//Add the writer to the filter.
					sensorsConfiguration.getWriters().getWriter().add(writer);
					filter.setWriterInstance(writer.getId());
				} else {
					//Remove the filter's reference to its writer.
					filter.setWriterInstance(null);
					
					//As a result the writer may become a dead node and
					//therefore need to be removed from the sensors
					//configuration.
					SensorsConfigurationUtility.removeUnreferencedWriters(sensorsConfiguration);
				}
			}
		};
		writerCheckBox.addValueChangeHandler(writerHandler);
    }
    
    public void setFilter(Filter filter, SensorsConfiguration sensorsConfiguration) {
    	this.filter = filter;
    	this.sensorsConfiguration = sensorsConfiguration;
    	
    	BooleanEnum distributeExternally = filter.getDistributeExternally();
    	if(distributeExternally == null || distributeExternally == BooleanEnum.FALSE) {
    		distributeExternallyCheckBox.setValue(false);
    	} else {
    		distributeExternallyCheckBox.setValue(true);
    	}
    	
    	BigInteger writerInstance = filter.getWriterInstance();
    	if(writerInstance == null) {
    		writerCheckBox.setValue(false);
    	} else {
    		writerCheckBox.setValue(true);
    	}
    }
    
    public void addWriterValueChangeHandler(ValueChangeHandler<Boolean> handler) {
    	writerCheckBox.addValueChangeHandler(handler);
    }
    
    public void setTitle() {
    	caption.setText("Transmit Filtered Data");
    }
}
