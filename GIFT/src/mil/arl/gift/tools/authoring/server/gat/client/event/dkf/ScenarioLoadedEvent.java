/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.dkf;

import generated.dkf.Scenario;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event indicating that a scenario was loaded.
 *
 * @author nroberts
 */
public class ScenarioLoadedEvent extends GenericEvent {

	/** The scenario. */
	Scenario scenario;
	
	/**
	 * Instantiates a new event indicating that a scenario was loaded.
	 *
	 * @param scenario the scenario that was loaded
	 */
	public ScenarioLoadedEvent(Scenario scenario){
		this.scenario = scenario;
	}
	
	/**
	 * Gets the scenario that was loaded.
	 *
	 * @return the scenario that was loaded
	 */
	public Scenario getScenario(){
		return scenario;
	}
}
