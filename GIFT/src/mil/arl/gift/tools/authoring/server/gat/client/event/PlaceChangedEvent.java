/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * The Class PlaceChangedEvent.
 */
public class PlaceChangedEvent extends GenericEvent {

	/** The place name. */
	private String placeName = "";
	
	/**
	 * Instantiates a new place changed event.
	 *
	 * @param placeName the place name
	 */
	public PlaceChangedEvent(String placeName) {
		if(placeName != null) {
			this.placeName = placeName;
		}
	}

	/**
	 * Gets the place name.
	 *
	 * @return the place name
	 */
	public String getPlaceName() {
		return placeName;
	}
}
