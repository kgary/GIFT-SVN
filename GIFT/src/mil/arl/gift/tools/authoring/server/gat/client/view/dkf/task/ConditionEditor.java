/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task;

import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.Condition;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.CourseConceptUtility;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.AbstractScenarioObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.RenameScenarioObjectEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.task.condition.ConditionPanel;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * An editor that modifies {@link Condition} scenario objects.
 * 
 * @author nroberts
 */
public class ConditionEditor extends AbstractScenarioObjectEditor<Condition> {
	
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ConditionEditor.class.getName());

    /** The view being modified by this editor's presenter */
    private final ConditionPanel conditionPanel = new ConditionPanel();

	/**
	 * Creates a new editor
	 */
    public ConditionEditor() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("ConditionEditor()");
        }
	}

    @Override
    protected void editObject(Condition condition) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("editObject(" + condition + ")");
        }

        if (condition == null) {
            throw new IllegalArgumentException("The parameter 'condition' cannot be null.");
        }

        // validation is handled within edit
        conditionPanel.edit(condition);
        
        setWidget(conditionPanel);
	}

    @Override
    protected void processEvent(ScenarioEditorEvent event) {
        
        if(event instanceof RenameScenarioObjectEvent){
            // Check if a concept changed course concept status
            
            RenameScenarioObjectEvent renameEvent = (RenameScenarioObjectEvent)event;
            if(renameEvent.getScenarioObject() instanceof generated.dkf.Concept){
                
                generated.course.ConceptNode oldNode = CourseConceptUtility.getConceptWithName(renameEvent.getOldName());
                generated.course.ConceptNode newNode = CourseConceptUtility.getConceptWithName(renameEvent.getNewName());
                if(newNode == null && oldNode == null){
                    // was NOT a course concept and did NOT become a course concept
                    return;
                }else{
                    // either became a course concept or turned into a non-course concept
                    conditionPanel.onCourseConceptChanged((generated.dkf.Concept)renameEvent.getScenarioObject());
                }
            }          
        }else if(event instanceof ScenarioEditorDirtyEvent){
                        
            ScenarioEditorDirtyEvent dirtyEvent = (ScenarioEditorDirtyEvent)event;
            if(dirtyEvent.getSourceScenarioObject() instanceof generated.dkf.Concept){
                conditionPanel.onCourseConceptChanged((generated.dkf.Concept)dirtyEvent.getSourceScenarioObject());  
            }
        }
    }

    @Override
    public ScenarioValidationComposite getValidationCompositeChild() {
        return conditionPanel;
    }
}