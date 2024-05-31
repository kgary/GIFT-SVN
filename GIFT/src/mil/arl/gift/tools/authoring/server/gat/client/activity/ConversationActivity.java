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
import mil.arl.gift.tools.authoring.server.gat.client.view.conversation.ConversationView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Activity for starting the conversation authoring tool.
 */
public class ConversationActivity extends AbstractGatActivity {

	/** The logger */
	private static Logger logger = Logger.getLogger(ConversationActivity.class.getName());

	/** A generic mapping of name/value pairs that can be specified when starting the activity/presenter */
    private HashMap<String, String> startParams;
    
	/** The conversation tree presenter. */
	@Inject 
	private ConversationView.Presenter conversationPresenter;
	
	/**
	 * Instantiates a new conversation activity.
	 *
	 * @param startParams - A customizable map of name/value pairs containing startup configuration settings for the activity/presenter.
	 */
	@Inject
	public ConversationActivity(@Assisted HashMap<String, String> startParams) {
	    this.startParams = startParams;
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.Activity#start(com.google.gwt.user.client.ui.AcceptsOneWidget, com.google.gwt.event.shared.EventBus)
	 */
	@Override
	public void start(final AcceptsOneWidget containerWidget, EventBus eventBus) {		
		logger.info("starting EditConversationActivity");		
		
		BsLoadingDialogBox.display("Loading File", "Loading, please wait...");
		
		conversationPresenter.start(containerWidget, startParams);
			
		eventBus.fireEvent(new PlaceChangedEvent("Conversation Authoring Tool"));
	}
		
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#mayStop()
	 */
	@Override
	public String mayStop() {
		return conversationPresenter.confirmStop();
	}
	
	/* (non-Javadoc)
	 * @see com.google.gwt.activity.shared.AbstractActivity#onStop()
	 */
	@Override
	public void onStop() {
		logger.info("stopping EditConversationActivity");
		
		if(conversationPresenter != null){
			conversationPresenter.stop();
		}
	}
}
