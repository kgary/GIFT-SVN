/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client;

import com.google.gwt.place.shared.PlaceController;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

/**
 * The Class GatPlaceController.
 */
public class GatPlaceController extends PlaceController {
	
	/**
	 * Instantiates a new gat place controller.
	 *
	 * @param eventBus the event bus
	 */
	@Inject
	public GatPlaceController(EventBus eventBus) {
		super(eventBus);
	}
}
