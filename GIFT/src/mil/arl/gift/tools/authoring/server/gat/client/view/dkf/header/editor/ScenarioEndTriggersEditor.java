/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.header.editor;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.Concept;
import generated.dkf.LearnerAction;
import generated.dkf.Scenario;
import generated.dkf.Scenario.EndTriggers;
import generated.dkf.Strategy;
import generated.dkf.Task;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.LearnerStartLocationUpdatedEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The {@link AbstractScenarioObjectEditor} that is responsible for displaying the content of a tab
 * that is editing a provided {@link EndTriggers}.
 * 
 * @author tflowers
 *
 */
public class ScenarioEndTriggersEditor extends AbstractScenarioObjectEditor<EndTriggers> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ScenarioEndTriggersEditor.class.getName());

    /** The view being modified by this editor's presenter */
    ScenarioEndTriggersPanel scenarioEndTriggerPanel = new ScenarioEndTriggersPanel();

    /**
     * Instantiates a new end trigger editor.
     */
    public ScenarioEndTriggersEditor() {
    }

    @Override
    protected void editObject(Scenario.EndTriggers endTriggers) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editObject(" + endTriggers + ")");
        }

        scenarioEndTriggerPanel.edit(endTriggers);
        scenarioEndTriggerPanel.validateAll();
        
        setWidget(scenarioEndTriggerPanel);        
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        // delete event
        if (event instanceof DeleteScenarioObjectEvent) {
            DeleteScenarioObjectEvent deleteEvent = (DeleteScenarioObjectEvent) event;
            Serializable obj = deleteEvent.getScenarioObject();
            if (obj instanceof Task || obj instanceof Concept || obj instanceof LearnerAction || obj instanceof Strategy) {
                scenarioEndTriggerPanel.handleTriggerItemDeleted();
            }

            // rename event
        } else if (event instanceof RenameScenarioObjectEvent) {
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent) event;
            Serializable obj = renameEvent.getScenarioObject();
            if (obj instanceof Task || obj instanceof Concept || obj instanceof LearnerAction || obj instanceof Strategy) {
                scenarioEndTriggerPanel.handleTriggerItemRename(obj, renameEvent.getOldName(), renameEvent.getNewName());
            }

            // learner start location updated event
        } else if (event instanceof LearnerStartLocationUpdatedEvent) {
            scenarioEndTriggerPanel.handleLearnerStartLocationUpdate();            
        } else if (event instanceof CreateScenarioObjectEvent) {
            // create event

            CreateScenarioObjectEvent createEvent = (CreateScenarioObjectEvent) event;
            Serializable obj = createEvent.getScenarioObject();
            if(obj instanceof Strategy){
                scenarioEndTriggerPanel.addStrategy((Strategy)obj);
            }
            
        }
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return scenarioEndTriggerPanel;
    }
}