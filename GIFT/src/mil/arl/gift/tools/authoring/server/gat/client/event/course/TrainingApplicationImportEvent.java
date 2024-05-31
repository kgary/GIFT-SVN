/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course;

import com.google.web.bindery.event.shared.binder.GenericEvent;

import generated.course.TrainingApplication;

/**
 * An event fired whenever a training application is loaded from GIFT Wrap.
 * 
 * @author sharrison
 */
public class TrainingApplicationImportEvent extends GenericEvent {

    /** The training application that was imported */
    private TrainingApplication trainingApplication;

    /**
     * Instantiates an event fired whenever a training application is imported from GIFT Wrap.
     * 
     * @param trainingApplication the training application that was imported.
     */
    public TrainingApplicationImportEvent(TrainingApplication trainingApplication) {
        this.trainingApplication = trainingApplication;
    }

    /**
     * Gets the training application that was imported.
     * 
     * @return the training application
     */
    public TrainingApplication getTrainingApplication() {
        return trainingApplication;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[TrainingApplicationImportEvent: ");
        builder.append("training appplication = ").append(getTrainingApplication());
        return builder.append("]").toString();
    }

}
