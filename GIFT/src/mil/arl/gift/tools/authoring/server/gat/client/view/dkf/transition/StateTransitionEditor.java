/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.transition;

import java.io.Serializable;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.Strategy;
import generated.dkf.Task;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies {@link StateTransition} scenario objects.
 * 
 * @author nroberts
 */
public class StateTransitionEditor extends AbstractScenarioObjectEditor<StateTransition> {

    /**
     * The view being modified by this editor's presenter
     */
    private StateTransitionPanel stateTransitionPanel = new StateTransitionPanel();

    /**
     * Creates a new editor
     */
    public StateTransitionEditor() {
    }

    @Override
    protected void editObject(StateTransition scenarioObject) {
        stateTransitionPanel.edit(scenarioObject);
        stateTransitionPanel.validateAll();
        setWidget(stateTransitionPanel);
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        // create event
        if (event instanceof CreateScenarioObjectEvent) {
            CreateScenarioObjectEvent createEvent = (CreateScenarioObjectEvent) event;
            Serializable obj = createEvent.getScenarioObject();
            if (obj instanceof Task || obj instanceof Concept || obj instanceof Strategy) {
                stateTransitionPanel.handleNewItem(obj);
            }

            // delete event
        } else if (event instanceof DeleteScenarioObjectEvent) {
            DeleteScenarioObjectEvent deleteEvent = (DeleteScenarioObjectEvent) event;
            Serializable obj = deleteEvent.getScenarioObject();
            if (obj instanceof Task) {
                stateTransitionPanel.removeReferencedTask((Task) obj);
            } else if (obj instanceof Concept) {
                stateTransitionPanel.removeReferencedConcept((Concept) obj);
            } else if (obj instanceof Strategy) {
                stateTransitionPanel.removeReferencedStrategy((Strategy) obj);
            }

            // rename event
        } else if (event instanceof RenameScenarioObjectEvent) {
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent) event;
            Serializable scenarioObject = renameEvent.getScenarioObject();
            if (scenarioObject instanceof Task) {
                Task task = (Task) scenarioObject;
                stateTransitionPanel.handleTaskRename(task, renameEvent.getOldName(), renameEvent.getNewName());
            } else if (scenarioObject instanceof Concept) {
                Concept concept = (Concept) scenarioObject;
                stateTransitionPanel.handleConceptRename(concept, renameEvent.getOldName(), renameEvent.getNewName());
            } else if (scenarioObject instanceof Strategy) {
                stateTransitionPanel.handleStrategyRename(renameEvent.getOldName(), renameEvent.getNewName());
            }
        }
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return stateTransitionPanel;
    }
}
