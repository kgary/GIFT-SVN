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

import mil.arl.gift.common.CourseState;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;

/**
 * Custom parsing logic for the course state message.
 * 
 * @author mhoffman
 *
 */
public class CourseStateEvent extends DomainSessionEvent {
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    private static final String NEXT_COURSE_OBJECT_TYPE = "NextCourseObjectType";
    private static final String NEXT_ADAPTIVE_PHASE = "NextAdaptivePhase";

    /**
     * Class constructor - parse the course state object.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param courseState - the course state for this event
     */
    public CourseStateEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, CourseState courseState) {
        super(MessageTypeEnum.COURSE_STATE.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(courseState);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    /**
     * Parse the course state for values to put in cells.
     * 
     * @param courseState contains information such as the name of the next course object and the next adaptive
     * courseflow phase.
     */
    private void parseEvent(CourseState courseState){
        
        EventReportColumn nextCourseObjectCol = new EventReportColumn(NEXT_COURSE_OBJECT_TYPE, NEXT_COURSE_OBJECT_TYPE);
        columns.add(nextCourseObjectCol);
        cells.add(new Cell(courseState.getNextTransitionImplementation(), nextCourseObjectCol));
        
        EventReportColumn nextAdaptivePhaseCol = new EventReportColumn(NEXT_ADAPTIVE_PHASE, NEXT_ADAPTIVE_PHASE);
        columns.add(nextAdaptivePhaseCol);
        String nextQuadrantName = null;
        if(courseState.getNextQuadrant() != null){
            nextQuadrantName = courseState.getNextQuadrant().getDisplayName();
        }
        cells.add(new Cell(nextQuadrantName, nextAdaptivePhaseCol));
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CourseStateEvent: ");
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
