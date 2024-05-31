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

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import mil.arl.gift.tools.authoring.server.gat.client.event.PlaceChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseView;

// TODO: Auto-generated Javadoc
/**
 * The Class CourseActivity.
 */
public class CourseActivity extends AbstractGatActivity {

	/** The logger. */
	private static Logger logger = Logger.getLogger(CourseActivity.class.getName());

	
	/** A generic mapping of name/value pairs that can be specified when starting the activity/presenter */
    private HashMap<String, String> startParams;
    
	/** The edit course presenter. */
	@Inject
	private CourseView.Presenter editCoursePresenter;
	
	/**
	 * Instantiates a new course activity.
	 *
	 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
	 */
	@Inject
	public CourseActivity(@Assisted HashMap<String, String> startParams) {
	    logger.info("constructor - "+startParams);
	    this.startParams = startParams;
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.Activity#start(com.google.gwt.user.client.ui.AcceptsOneWidget, com.google.gwt.event.shared.EventBus)
	 */
	@Override
	public void start(final AcceptsOneWidget containerWidget, EventBus eventBus) {		
		logger.info("starting\n"+startParams);		
		
		editCoursePresenter.start(containerWidget, startParams);	
		eventBus.fireEvent(new PlaceChangedEvent("Course Authoring Tool"));
		logger.info("finished start()");
	}
		
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#mayStop()
	 */
	@Override
	public String mayStop() {
		return editCoursePresenter.confirmStop();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		logger.info("stopping");

		if(editCoursePresenter != null) {
			editCoursePresenter.stop();
		}	
	}
}
