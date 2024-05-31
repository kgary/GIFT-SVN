/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.activity;

import java.util.HashMap;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.widgets.bootstrap.BsLoadingDialogBox;
import mil.arl.gift.tools.authoring.server.gat.client.event.PlaceChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationEditor;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * The Class SensorsConfigurationActivity.
 */
public class SensorsConfigurationActivity extends AbstractGatActivity {
   
    /** The logger. */
    private static Logger logger = Logger.getLogger(SensorsConfigurationActivity.class.getName());
    
    /** A generic mapping of name/value pairs that can be specified when starting the activity/presenter */
    private HashMap<String, String> startParams;
    
    @Inject
    private SensorsConfigurationEditor sensorConfigurationEditor;
    
    /**
     * Instantiates a new sensor activity.
     *
     * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
     */
    @Inject
    public SensorsConfigurationActivity(@Assisted HashMap<String, String> startParams) {
        this.startParams = startParams;   
    }     
    
    /* (non-Javadoc)
     * @see com.google.gwt.activity.shared.Activity#start(com.google.gwt.user.client.ui.AcceptsOneWidget, com.google.gwt.event.shared.EventBus)
     */
    @Override
    public void start(final AcceptsOneWidget containerWidget, EventBus eventBus) {    	
    	logger.info("starting SensorsConfigurationActivity");    
    	
    	BsLoadingDialogBox.display("Loading File", "Loading, please wait...");

    	sensorConfigurationEditor.start(containerWidget, startParams);
    	
    	eventBus.fireEvent(new PlaceChangedEvent("Sensors Configuration Authoring Tool")); 
    }
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#mayStop()
	 */
	@Override
	public String mayStop() {
		return sensorConfigurationEditor.mayStop();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		logger.info("stopping SensorsConfigurationActivity");
		sensorConfigurationEditor.stop();
	}
}
