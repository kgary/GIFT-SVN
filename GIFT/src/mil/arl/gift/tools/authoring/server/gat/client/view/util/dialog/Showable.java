/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.dialog;

/**
 * An interface for interacting with UI elements that can be shown and hidden.
 *
 * @author nroberts
 */
public interface Showable {

	/**
	 * Show this element and its content.
	 */
	public void showContent();
	
	/**
	 * Hide this element and its content.
	 */
	public void hideContent();
	
	/**
	 * Show this element and its content.
	 * 
	 * @param fireEvents whether or not to fire events
	 */
	public void showContent(boolean fireEvents);
	
	/**
	 * Hide this element and its content.
	 * 
	 * @param fireEvents whether or not to fire events
	 */
	public void hideContent(boolean fireEvents);
}
