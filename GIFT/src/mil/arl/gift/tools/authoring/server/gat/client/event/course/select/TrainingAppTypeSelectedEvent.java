/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.event.course.select;

import mil.arl.gift.common.enums.TrainingApplicationEnum;

import com.google.web.bindery.event.shared.binder.GenericEvent;

/**
 * Event data model for when a training application type is selected/set
 * in the external application course object GAT editor.
 * 
 * @author mhoffman
 *
 */
public class TrainingAppTypeSelectedEvent extends GenericEvent {

    private TrainingApplicationEnum trainingAppType;
    
    /**
     * Set the event attribute(s)
     * 
     * @param trainingAppType the enumerated type of training application selected for this event
     */
    public TrainingAppTypeSelectedEvent(TrainingApplicationEnum trainingAppType){
        this.trainingAppType = trainingAppType;
    }
    
    public TrainingApplicationEnum getTrainingAppType(){
        return trainingAppType;
    }
}
