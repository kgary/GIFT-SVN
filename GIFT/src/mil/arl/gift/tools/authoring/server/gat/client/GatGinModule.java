/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import mil.arl.gift.tools.authoring.server.gat.client.presenter.conversation.ConversationPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.course.CoursePresenter;
import mil.arl.gift.tools.authoring.server.gat.client.presenter.dkf.DkfPresenter;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.ConversationView;
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.ConversationViewImpl;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseView;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseViewImpl;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.DkfView;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.DkfViewImpl;

/**
 * Configures dependency injections for gin.
 * (i.e. specifies which concrete classes should be used to satisfy each
 * interface) 
 *
 * @author cragusa
 */
public class GatGinModule extends AbstractGinModule {
	
	/* (non-Javadoc)
	 * @see com.google.gwt.inject.client.AbstractGinModule#configure()
	 */
	@Override
	protected void configure() {

		//Activities (Assisted using Factory)
		install(new GinFactoryModuleBuilder().build(GatActivityMapper.ActivityFactory.class));
		
		//Primary Presenters
		bind(DkfView.Presenter.class).to(DkfPresenter.class);
		bind(CourseView.Presenter.class).to(CoursePresenter.class);
		bind(ConversationView.Presenter.class).to(ConversationPresenter.class);

		//Primary Views (all should be singletons)
		bind(DkfView.class).to(DkfViewImpl.class).in(Singleton.class);
		bind(CourseView.class).to(CourseViewImpl.class).in(Singleton.class);
		bind(ConversationView.class).to(ConversationViewImpl.class).in(Singleton.class);
		
		//Infrastructure
		bind(EventBus.class).to(SimpleEventBus.class).in(Singleton.class);
		bind(PlaceController.class).to(GatPlaceController.class).in(Singleton.class);
		bind(ActivityMapper.class).to(GatActivityMapper.class).in(Singleton.class);
		//NOTE: DispatchAsync comes from StandardDispatchModule (not this module)
	}
}