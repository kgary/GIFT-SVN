/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.dkf;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event indicating that a name has been entered for a new strategy to be added.
 *
 * @author nroberts
 */
public class NewStrategyNameEnteredEvent extends GenericEvent {

	/** The scenario. */
	String name;
	
	/**
	 * Instantiates a new event indicating that a name has been entered for a new strategy to be added.
	 *
	 * @param name the name that was entered
	 */
	public NewStrategyNameEnteredEvent(String name){
		this.name = name;
	}
	
	/**
	 * Gets the name that was entered
	 *
	 * @return the name that was entered
	 */
	public String getName(){
		return name;
	}
}
