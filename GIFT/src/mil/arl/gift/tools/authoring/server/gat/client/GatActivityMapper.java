/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import java.util.HashMap;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.inject.Inject;

import mil.arl.gift.tools.authoring.server.gat.client.activity.ConversationActivity;
import mil.arl.gift.tools.authoring.server.gat.client.activity.CourseActivity;
import mil.arl.gift.tools.authoring.server.gat.client.activity.DkfActivity;
import mil.arl.gift.tools.authoring.server.gat.client.activity.LearnerConfigurationActivity;
import mil.arl.gift.tools.authoring.server.gat.client.activity.MetadataActivity;
import mil.arl.gift.tools.authoring.server.gat.client.activity.PedagogyConfigurationActivity;
import mil.arl.gift.tools.authoring.server.gat.client.activity.SensorsConfigurationActivity;
import mil.arl.gift.tools.authoring.server.gat.client.place.ConversationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.CoursePlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.DkfPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.LearnerConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.MetadataPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.PedagogyConfigurationPlace;
import mil.arl.gift.tools.authoring.server.gat.client.place.SensorsConfigurationPlace;

/**
 * This class maps activities to places.
 * @author iapostolos and cragusa
 *
 */
public class GatActivityMapper implements ActivityMapper {
		
	//http://stackoverflow.com/questions/9924695/using-gin-in-gwt-activities
	//http://stackoverflow.com/questions/8976250/how-to-use-guices-assistedinject
	/**
	 * A factory for creating Activity objects.
	 */
	public interface ActivityFactory {
		
		/**
		 * Creates a new Activity object.
		 *
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 * @return the course activity
		 */
		CourseActivity createCourseActivity(HashMap<String, String> startParams);
		
		/**
		 * Creates a new Activity object.
		 *
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 * 
		 * @return the dkf activity
		 */
		DkfActivity createDkfActivity(HashMap<String, String> startParams);

		/**
		 * Creates a new Activity object.
		 *
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 * 
		 * @return the conversation activity
		 */
		ConversationActivity createConversationActivity(HashMap<String, String> startParams);
		
		/**
		 * Creates a new Activity object.
		 * 
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 *
		 * @return the sensors configuration activity
		 */
		SensorsConfigurationActivity createSensorsConfigurationActivity(HashMap<String, String> startParams);
		
		/**
		 * Creates a new Activity object.
		 *
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 * @return the learner configuration activity
		 */
		LearnerConfigurationActivity createLearnerConfigurationActivity(HashMap<String, String> startParams);
		
		/**
		 * Creates a new Activity object.
		 *
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 * 
		 * @return the metadata activity
		 */
		MetadataActivity createMetadataActivity(HashMap<String, String> startParams);
		
		/**
		 * Creates a new Activity object.
		 *
		 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
		 * 
		 * @return the pedagogy configuration activity
		 */
		PedagogyConfigurationActivity createPedagogyConfigurationActivity(HashMap<String, String> startParams);
	}	

	/** The activity factory. */
	@Inject
	private ActivityFactory activityFactory;

	/**
	 * GatActivityMapper associates each Place with its corresponding
	 * {@link Activity}.
	 */
	@Inject
	public GatActivityMapper() {		
		super();
	}
	
	/**
	 * Map each Place to its corresponding Activity.
	 *
	 * @param place the place
	 * @return the activity
	 */
	@Override
	public Activity getActivity(Place place) {
		
		if(place instanceof CoursePlace) {
			
			CoursePlace coursePlace = ((CoursePlace) place);
			return activityFactory.createCourseActivity(coursePlace.getStartParams());
			
		} else if(place instanceof DkfPlace) {
			
		    DkfPlace dkfPlace = (DkfPlace)place;
			return activityFactory.createDkfActivity(dkfPlace.getStartParams());		
			
		} else if(place instanceof ConversationPlace) {
			
		    ConversationPlace conversationPlace = (ConversationPlace)place;
			return activityFactory.createConversationActivity(conversationPlace.getStartParams());		
			
		} else if(place instanceof SensorsConfigurationPlace) {
			
		    SensorsConfigurationPlace sensorPlace = ((SensorsConfigurationPlace) place);
			return activityFactory.createSensorsConfigurationActivity(sensorPlace.getStartParams());
			
		} else if(place instanceof LearnerConfigurationPlace) {
			
			LearnerConfigurationPlace learnerPlace = ((LearnerConfigurationPlace)place);
			return activityFactory.createLearnerConfigurationActivity(learnerPlace.getStartParams());
			
		} else if(place instanceof MetadataPlace) {
			
		    MetadataPlace metadataPlace = ((MetadataPlace)place);
			return activityFactory.createMetadataActivity(metadataPlace.getStartParams());
			
		} else if(place instanceof PedagogyConfigurationPlace) {
			
		    PedagogyConfigurationPlace pedPlace = ((PedagogyConfigurationPlace)place);
			return activityFactory.createPedagogyConfigurationActivity(pedPlace.getStartParams());
		}

		return null;
	}
}
