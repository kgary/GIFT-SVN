/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.server;

import java.util.List;

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.EventType;
import mil.arl.gift.common.ert.event.AbstractEvent;

/**
 * This is the base class for Event Source Parsers.  An Event Source Parser is responsible for converting
 * raw event sources into events for the ERT.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractEventSourceParser {    
    
    /**
     * Return the types of events in the source.
     * 
     * @return the types of events
     */
    public abstract List<EventType> getTypesOfEvents();
    
    /**
     * Return the events of the given type in the source.
     * 
     * @param eventType the type of event to get instances of
     * @return the events for that type. Order will match how they appear
     * in the log file.
     */
    public abstract List<AbstractEvent> getEventsByType(EventType eventType);
    
    /**
     * Return the default columns that should be in the report for this source.
     * 
     * @return the default columns to include
     */
    public abstract List<EventReportColumn> getDefaultColumns();
    
    /**
     * Return the participant id from an experiment where the user is anonymous.
     * 
     * @return the participant id.  Can be null if not in an experiment or the data set being parsed doesn't contain this information.
     */
    public abstract Integer getParticipantId();
}
