/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.List;

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;

/**
 * Event that captures the number of attempts taken by the same experiment participant.
 * 
 * @author mhoffman
 *
 */
public class ParticipantAttemptCnt extends DomainSessionEvent {
    
    /** list of columns specific to this event */
    private static List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    static{
        columns.add(EventReportColumn.ATTEMPT_COL);
    }
    
    /** name of this event */
    private static final String eventName = "Participant Attempt Cnt";
    
    /** the single cell for this event that captures and keeps track of attempt count */
    private Cell attemptCell = new Cell("1", EventReportColumn.ATTEMPT_COL);

    /**
     * Set attributes and build attempt cell.
     * 
     * @param userId the generated user id for the experiment participant
     * @param domainSessionId the session id where the events have taken place.
     * @param participantId the unique participant id within an experiment.
     */
    public ParticipantAttemptCnt(int userId, int dsId, int participantId){
        super(eventName, 0, 0, userId, dsId, null);

        cells.add(attemptCell);
        
        setParticipantId(participantId);
    }
    
    /**
     * Increment the number of attempts this participant has started for an experiment
     */
    public void incrementAttemptCnt(){

        String currValStr = attemptCell.getValue();
        Integer currVal = Integer.valueOf(currValStr);
        setAttemptCell(currVal + 1);
    }    
    
    /**
     * Set the experiment attempt value to the value provided.
     * 
     * @param value the number of attempts this participant has stared for an experiment.
     */
    private void setAttemptCell(Integer value){
        
        Cell newAttemptCell = new Cell(value.toString(), EventReportColumn.ATTEMPT_COL);
        cells.set(cells.indexOf(attemptCell), newAttemptCell);
        
        attemptCell = newAttemptCell;
    }
    
    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }
}
