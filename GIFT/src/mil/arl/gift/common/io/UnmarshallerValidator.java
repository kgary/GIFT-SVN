/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;

import mil.arl.gift.common.course.AbstractCourseHandler;

/**
 * Unmarshaller validator and listener. Handles validation events that occur during the
 * unmarshalling process.
 * 
 * @author sharrison
 *
 */
public class UnmarshallerValidator extends Unmarshaller.Listener implements ValidationEventHandler {
    // This will keep track of which transition it is trying to unmarshall.
    private Serializable transition;
    
    // The list of validation events to populate.
    private List<ValidationEvent> validationEventList;
    
    // True to throw the first error the validator comes across; false to keep processing.
    private boolean failOnFirstSchemaError;

    /**
     * Constructor.
     * 
     * @param validationEventList the validation event list to populate.
     * @param failOnFirstSchemaError true to throw the first error the validator comes across; false to keep processing.
     */
    public UnmarshallerValidator(List<ValidationEvent> validationEventList, boolean failOnFirstSchemaError) {
        this.validationEventList = validationEventList;
        this.failOnFirstSchemaError = failOnFirstSchemaError;
    }
    
    @Override
    public void beforeUnmarshal(Object target, Object parent) {
        // If the target object is a transition object, save that object for use later on. This will
        // be overwritten when the next transition object is being unmarshalled.
        if (target instanceof Serializable && AbstractCourseHandler.isTransitionObject((Serializable)target)) {
            transition = ((Serializable)target);
        }
    }
    
    @Override
    public boolean handleEvent(ValidationEvent event) {
        // If true, stop unmarshalling and throw error (e.g. return false).
        if (failOnFirstSchemaError) {
            return false;
        }

        // Check if the parent transition is disabled or not. If true, ignore validation event; if
        // false, add validation event to the list.
        if (!AbstractCourseHandler.isTransitionDisabled(transition)) {
            validationEventList.add(event);
        }
        
        // Keep processing.
        return true;
    }
}
