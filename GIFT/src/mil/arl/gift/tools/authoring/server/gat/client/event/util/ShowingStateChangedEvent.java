/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.util;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog.Showable;

/**
 * The ShowingStateChangedEvent.
 *
 * @author nroberts
 */
public class ShowingStateChangedEvent extends GenericEvent {

	/** The element in which the showing state changed. */
	private Showable element;
	
	/** Whether or not the element is showing. */
	private boolean showing;
	
	/**
	 * Instantiates a new showing state changed event.
	 *
	 * @param element the element in which the showing state changed
	 * @param showing whether or not the element is showing
	 */
	public ShowingStateChangedEvent(Showable element, boolean showing){
		this.element = element;
		this.showing = showing;
	}
	
	/**
	 * Gets the element in which the showing state changed.
	 *
	 * @return the element in which the showing state changed
	 */
	public Showable getElement(){
		return element;
	}

	/**
	 * Checks if the element in which the showing state changed is currently showing.
	 *
	 * @return the showing
	 */
	public boolean isShowing() {
		return showing;
	}
}
