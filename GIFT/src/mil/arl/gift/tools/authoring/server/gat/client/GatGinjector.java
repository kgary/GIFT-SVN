/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import net.customware.gwt.dispatch.client.DispatchAsync;
import net.customware.gwt.dispatch.client.gin.StandardDispatchModule;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.inject.client.GinModules;
import com.google.gwt.inject.client.Ginjector;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.ConversationView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseView;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.DkfView;
import mil.arl.gift.tools.authoring.server.gat.client.view.sensor.SensorsConfigurationEditor;

/**
 * Defines the interface for the GatGinjector.
 *
 * @author cragusa
 */
//https://code.google.com/p/google-gin/wiki/GinTutorial#Step_1._Inheriting_the_GIN_module
@GinModules({StandardDispatchModule.class, GatGinModule.class})
public interface GatGinjector extends Ginjector {
	
	/**  The one and only GatGinjector. */
	static final GatGinjector INSTANCE = GWT.create(GatGinjector.class);

	//Activities
	/**
	 * Gets the activity factory.
	 *
	 * @return the activity factory
	 */
	GatActivityMapper.ActivityFactory getActivityFactory();
	
	//Primary Presenters
	/**
	 * Gets the course presenter.
	 *
	 * @return the course presenter
	 */
	CourseView.Presenter getCourse2Presenter();
	
	/**
	 * Gets the dkf presenter.
	 *
	 * @return the dkf presenter
	 */
	DkfView.Presenter getDkf2Presenter();

	/**
	 * Gets the conversation presenter.
	 *
	 * @return the conversation presenter
	 */
	ConversationView.Presenter getConversationPresenter();
	
	//Primary Views
	
	/**
	 * Gets the course view.
	 *
	 * @return the course view
	 */
	CourseView getCourse2View();
	
	/**
	 * Gets the dkf view.
	 *
	 * @return the dkf view
	 */
	DkfView getDkf2View();
	
	/**
	 * Gets the conversation view.
	 *
	 * @return the conversation view
	 */
	ConversationView getConversationView();
	
	/**
	 * Gets the sensor configuration editor.
	 * 
	 * @return the sensor configuration editor.
	 */
	SensorsConfigurationEditor getSensorConfigurationEditor();
	
	//Infrastructure
	/**
	 * Gets the dispatcher.
	 *
	 * @return the dispatcher
	 */
	DispatchAsync getDispatcher();
	
	/**
	 * Gets the event bus.
	 *
	 * @return the event bus
	 */
	EventBus getEventBus();
	
	/**
	 * Gets the place controller.
	 *
	 * @return the place controller
	 */
	PlaceController getPlaceController();
	
	/**
	 * Gets the activity mapper.
	 *
	 * @return the activity mapper
	 */
	ActivityMapper getActivityMapper();
}
