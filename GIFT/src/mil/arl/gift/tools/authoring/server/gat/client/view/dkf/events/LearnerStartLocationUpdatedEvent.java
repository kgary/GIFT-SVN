/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

/**
 * An event that notifies listeners that the learner start location for the scenario has been
 * updated.
 * 
 * @author sharrison
 */
public class LearnerStartLocationUpdatedEvent extends ScenarioEditorEvent {

    /**
     * Constructs an event for notifying listeners that the learner start location for the scenario
     * has been updated.
     */
    public LearnerStartLocationUpdatedEvent() {
    }

    @Override
    public String toString() {
        return new StringBuilder("[LearnerStartLocationUpdatedEvent]").toString();
    }
}
