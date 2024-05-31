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

import generated.dkf.AvailableLearnerActions;
import generated.dkf.Condition;
import generated.dkf.LearnerAction;
import generated.dkf.PaceCountCondition;
import generated.dkf.Strategy;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.CreateScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.DeleteScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.util.ScenarioClientUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * The body of the tab that is used to edit the {@link AvailableLearnerActions} object within the
 * DKF editor.
 *
 * @author tflowers
 *
 */
public class LearnerActionsEditor extends AbstractScenarioObjectEditor<AvailableLearnerActions> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(LearnerActionsEditor.class.getName());

    /** The view being modified by this editor's presenter */
    LearnerActionsPanel learnerActionsPanel = new LearnerActionsPanel();
    
    /** the learner action to jump into editing, can be null if just opening the learner actions editor */
    private LearnerAction learnerActionToEdit = null;
    
    @Override
    protected void editObject(AvailableLearnerActions learnerActions) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editObject(" + learnerActions + ")");
        }

        if (learnerActions == null) {
            throw new IllegalArgumentException("The parameter 'learnerActions' cannot be null.");
        }

        learnerActionsPanel.edit(learnerActions);
        if(learnerActionToEdit != null){
            learnerActionsPanel.edit(learnerActionToEdit);
        }
        learnerActionsPanel.validateAll();

        setWidget(learnerActionsPanel);
    }
    
    /**
     * Open the editor for the learner action specified.
     * @param learnerAction the learner action to edit, must exist in the available learner actions.
     */
    public void editObject(LearnerAction learnerAction){
        
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editObject(" + learnerAction + ")");
        }

        if (learnerAction == null) {
            throw new IllegalArgumentException("The parameter 'learnerAction' cannot be null.");
        }
        
        learnerActionToEdit = learnerAction;
        edit(ScenarioClientUtility.getAvailableLearnerActions());  // will eventually cause editObject(AvailableLaernerActions) above to be called
    }

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        // create event
        if (event instanceof CreateScenarioObjectEvent) {
            CreateScenarioObjectEvent createEvent = (CreateScenarioObjectEvent) event;
            Serializable obj = createEvent.getScenarioObject();
            if (obj instanceof LearnerAction) {
                learnerActionsPanel.rebuildLearnerActionTable();
            }

            // delete event
        } else if (event instanceof DeleteScenarioObjectEvent) {
            DeleteScenarioObjectEvent deleteEvent = (DeleteScenarioObjectEvent) event;
            Serializable obj = deleteEvent.getScenarioObject();
            if (obj instanceof LearnerAction) {
                learnerActionsPanel.rebuildLearnerActionTable();
            } else if (obj instanceof Condition) {
                Condition condition = (Condition) obj;

                /* If a pace count condition was deleted, validate the learner
                 * actions panel since the validity of pace count start and end
                 * require that at least one pace count condition is present in
                 * the Scenario. */
                if (condition.getInput() != null && condition.getInput().getType() instanceof PaceCountCondition) {
                    learnerActionsPanel.validateAll();
                }
            } else if (obj instanceof Strategy) {
                learnerActionsPanel.removeReferencedStrategy((Strategy) obj);
            }

            // rename event
        } else if (event instanceof RenameScenarioObjectEvent) {
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent) event;
            Serializable obj = renameEvent.getScenarioObject();
            if (obj instanceof LearnerAction) {
                learnerActionsPanel.refreshLearnerAction((LearnerAction) obj);
            } else if (obj instanceof Strategy) {
                learnerActionsPanel.handleStrategyRename(renameEvent.getOldName(), renameEvent.getNewName());
            }
        }
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return learnerActionsPanel;
    }
}