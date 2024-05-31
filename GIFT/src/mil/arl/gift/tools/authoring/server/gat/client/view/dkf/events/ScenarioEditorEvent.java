/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * An event class representing an event that is raised within the ScenarioEditor
 * 
 * @author tflowers
 *
 */
public abstract class ScenarioEditorEvent extends GenericEvent {
    @Override
    public abstract String toString();
}
