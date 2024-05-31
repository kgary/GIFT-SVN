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

import mil.arl.gift.common.course.dkf.session.LessonCompleted;
import mil.arl.gift.common.course.dkf.session.LessonCompleted.LessonCompletedStatusType;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;

/**
 * Custom parsing logic for lesson completed event.
 * 
 * @author mhoffman
 *
 */
public class LessonCompletedEvent extends DomainSessionEvent {
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /** name of the column that has the lesson completed status value */
    private static final String COMPLETED_STATUS_COL = "Status";
    
    /**
     * Class constructor - set attributes
     * LEGACY - JSON message prior to protobuf conversion July 2021 had no payload
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     */
    public LessonCompletedEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry) {
        super(MessageTypeEnum.LESSON_COMPLETED.getDisplayName(), time, domainSessionMessageEntry, null);
        
    }

    /**
     * Class constructor - set attributes
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param lessonCompleted information about the lesson completed event
     */
    public LessonCompletedEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, LessonCompleted lessonCompleted) {
        super(MessageTypeEnum.LESSON_COMPLETED.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(lessonCompleted);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    /**
     * Parse the lesson completed for values to put in cells.
     * 
     * @param lessonCompleted information about the lesson completed event
     */
    private void parseEvent(LessonCompleted lessonCompleted){
        
        EventReportColumn statusCol = new EventReportColumn(COMPLETED_STATUS_COL, COMPLETED_STATUS_COL);
        columns.add(statusCol);
        LessonCompletedStatusType type = lessonCompleted.getStatusType();
        if(type == null){
            // default
            type = LessonCompletedStatusType.LEGACY_NOT_SPECIFIED;
        }
        cells.add(new Cell(type.name(), statusCol));
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LessonCompletedEvent: ");
        sb.append(super.toString());
        
        sb.append(", columns = {");
        for(EventReportColumn column : columns){
            sb.append(column.toString()).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
