/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.binder.EventBinder;
import com.google.web.bindery.event.shared.binder.EventHandler;

import mil.arl.gift.tools.authoring.server.gat.client.SharedResources;
import mil.arl.gift.tools.authoring.server.gat.client.event.PlaceChangedEvent;

/**
 * The Class GatHeader.
 */
public class GatHeader extends Composite {

	/**
	 * The Interface GatHeaderUiBinder.
	 */
	interface GatHeaderUiBinder extends UiBinder<Widget, GatHeader> {
	}
	
	/** The ui binder. */
	private static GatHeaderUiBinder uiBinder = GWT
			.create(GatHeaderUiBinder.class);
	
	/**
	 * The Interface GatHeaderEventBinder.
	 */
	interface GatHeaderEventBinder extends EventBinder<GatHeader> {
	}
	
	/** The Constant eventBinder. */
	private static final GatHeaderEventBinder eventBinder = GWT
			.create(GatHeaderEventBinder.class);	

	/**
	 * Instantiates a new gat header.
	 */
	public GatHeader() {
		initWidget(uiBinder.createAndBindUi(this));		
		eventBinder.bindEventHandlers(this, SharedResources.getInstance().getEventBus());
	}
	
	/**
	 * On place changed event.
	 *
	 * @param event the event
	 */
	@EventHandler
	protected void onPlaceChangedEvent(PlaceChangedEvent event) {

	}
}
