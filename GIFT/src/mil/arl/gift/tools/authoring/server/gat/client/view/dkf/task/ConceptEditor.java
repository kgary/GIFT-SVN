/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task;

import java.io.Serializable;
import java.math.BigInteger;

import generated.dkf.Actions.StateTransitions.StateTransition;
import generated.dkf.Concept;
import generated.dkf.Condition;
import generated.dkf.PerformanceAssessment;
import generated.dkf.PerformanceNode;
import generated.dkf.Strategy;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ReferencesChangedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.concept.ConceptPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies {@link Concept} scenario objects.
 * 
 * @author nroberts
 */
public class ConceptEditor extends AbstractScenarioObjectEditor<Concept> {

    /**
     * The view being modified by this editor's presenter
     */
    private ConceptPanel conceptPanel = new ConceptPanel();

    /**
     * Creates a new editor
     */
    public ConceptEditor() {
    }

    @Override
    protected void editObject(Concept scenarioObject) {

        conceptPanel.edit(scenarioObject);
        conceptPanel.validateAll();

        setWidget(conceptPanel);
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        // create event
        if (event instanceof CreateScenarioObjectEvent) {
            /* create events fire a dirty event, so that will handle re-validating the model
             * validation statuses */

            // delete event
        } else if (event instanceof DeleteScenarioObjectEvent) {
            DeleteScenarioObjectEvent deleteEvent = (DeleteScenarioObjectEvent) event;
            Serializable obj = deleteEvent.getScenarioObject();
            if (obj instanceof Concept || obj instanceof Condition) {
                // deleting a concept or condition might cause validation issues.
                conceptPanel.validateModelValidationStatuses();
            } else if (obj instanceof Strategy) {
                conceptPanel.removeAction((Strategy) obj);
            } else if (obj instanceof StateTransition) {
                conceptPanel.updateReferencedStateTransitions((StateTransition) obj, false);
            }

            // rename event
        } else if (event instanceof RenameScenarioObjectEvent) {
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent) event;
            Serializable scenarioObject = renameEvent.getScenarioObject();
            if (scenarioObject instanceof Strategy) {
                conceptPanel.refreshReferencedActions();
            } else if (scenarioObject instanceof StateTransition) {
                conceptPanel.refreshReferencedStateTransitions();
            } else if (scenarioObject instanceof Concept && doesEventValueAffectEditor(scenarioObject)) {
                conceptPanel.onRename(renameEvent.getNewName());
            }

            // reference changed event
        } else if (event instanceof ReferencesChangedEvent) {
            ReferencesChangedEvent refChangedEvent = (ReferencesChangedEvent) event;
            Serializable source = refChangedEvent.getReferenceChangedSource();

            if (source instanceof Strategy) {
                // Strategy changed its references
                Strategy strategy = (Strategy) source;
                
                // test old value
                if (doesEventValueAffectEditor(refChangedEvent.getOldValue())) {
                    conceptPanel.updateReferencedActions(strategy, false);
                }

                // test new value
                if (doesEventValueAffectEditor(refChangedEvent.getNewValue())) {
                    conceptPanel.updateReferencedActions(strategy, true);
                }

            } else if (source instanceof StateTransition) {
                // State Transition changed its references
                StateTransition transition = (StateTransition) source;
                
                // test old value
                if (doesEventValueAffectEditor(refChangedEvent.getOldValue())) {
                    conceptPanel.updateReferencedStateTransitions(transition, false);
                }

                // test new value
                if (doesEventValueAffectEditor(refChangedEvent.getNewValue())) {
                    conceptPanel.updateReferencedStateTransitions(transition, true);
                }
            }
        }
    }

    /**
     * Determines if the provided value affects this editor.
     * 
     * @param value the value to check.
     * @return true if the value can match the {@link Concept} node id being edited; false
     *         otherwise.
     */
    private boolean doesEventValueAffectEditor(Serializable value) {
        BigInteger nodeId = null;
        if (value instanceof PerformanceNode) {
            nodeId = ((PerformanceNode) value).getNodeId();
        } else if (value instanceof PerformanceAssessment.PerformanceNode) {
            nodeId = ((PerformanceAssessment.PerformanceNode) value).getNodeId();
        } else if (value instanceof Concept) {
            nodeId = ((Concept) value).getNodeId();
        }

        if (nodeId != null) {
            if (nodeId.equals(conceptPanel.getSelectedConceptNodeId())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return conceptPanel;
    }
}