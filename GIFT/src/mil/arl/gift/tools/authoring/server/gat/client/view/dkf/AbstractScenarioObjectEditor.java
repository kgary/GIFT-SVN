/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.dkf;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import mil.arl.gift.common.gwt.client.validation.ValidationStatusChangedCallback;
import mil.arl.gift.tools.authoring.server.gat.client.view.course.AbstractCourseObjectEditor;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorDirtyEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.dkf.events.ScenarioEditorEvent;
import mil.arl.gift.tools.authoring.server.gat.client.view.util.validation.ScenarioValidationComposite;

/**
 * Abstract interface for all sub-editors contained within the Scenario Editor.
 * 
 * @author tflowers
 *
 * @param <T> The type of object the editor is responsible for editing.
 */
public abstract class AbstractScenarioObjectEditor<T extends Serializable> extends AbstractCourseObjectEditor<T> {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(AbstractScenarioObjectEditor.class.getName());

    /**
     * Handles an incoming event and routes it to the appropriate logic if necessary.
     * 
     * @param event The incoming event to handle. Can't be null.
     */
    public void handleEvent(ScenarioEditorEvent event) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("handleEvent(" + event + ")");
        }

        if (event == null) {
            throw new IllegalArgumentException("The parameter 'event' cannot be null.");
        }

        processEvent(event);

        boolean revalidateModelStatuses = false;
        if (event instanceof ScenarioEditorDirtyEvent
                && ((ScenarioEditorDirtyEvent) event).getSourceScenarioObject() != null) {
            /* if the dirty event has a source, validate model validation statuses. */
            revalidateModelStatuses = true;
//        } else if (event instanceof LearnerStartLocationUpdatedEvent) {
//            /* if the learner start location was updated, validate model validation statuses. */
//            revalidateModelStatuses = true;
        }

        /* re-validate the model validation statuses for the editors that have validation composite
         * children */
        if (revalidateModelStatuses) {
            ScenarioValidationComposite child = getValidationCompositeChild();
            if (child != null) {
                child.validateModelValidationStatuses();
            }
        }
    }

    /**
     * Adds a callback to execute when the validity changes.
     * 
     * @param callback the callback to execute when the status validity changes.
     */
    public void addValidationStatusChangedCallback(ValidationStatusChangedCallback callback) {
        ScenarioValidationComposite child = getValidationCompositeChild();
        if (child != null) {
            child.addValidationStatusChangedCallback(callback);
        }
    }

    /**
     * Processes the event for the current editor.
     * 
     * @param event The {@link ScenarioEditorEvent} to process. Can't be null.
     */
    protected abstract void processEvent(ScenarioEditorEvent event);

    /**
     * Gets the {@link ScenarioValidationComposite} child.
     * 
     * @return the child of this editor.
     */
    public abstract ScenarioValidationComposite getValidationCompositeChild();
}