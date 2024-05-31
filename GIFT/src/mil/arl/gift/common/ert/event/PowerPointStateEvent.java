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

import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.ta.state.PowerPointState;
import mil.arl.gift.common.ert.server.Cell;

/**
 * This class represents a PowerPoint state event that can be included in an ERT report.  It has the logic to
 * convert a PowerPoint state object into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class PowerPointStateEvent extends DomainSessionEvent {

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    private static final String SLIDE_INDEX = "Slide Index";
    
    /**
     * Class constructor - parse the event object
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param pptState - the PowerPoint state for this event
     */
    public PowerPointStateEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, PowerPointState pptState) {
        super(MessageTypeEnum.POWERPOINT_STATE.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(pptState);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }
    
    /**
     * Gather information on the columns and cell content for the content of the powerpoint state
     * 
     * @param pptState
     */
    private void parseEvent(PowerPointState pptState){
        
        EventReportColumn slide = new EventReportColumn(SLIDE_INDEX, SLIDE_INDEX);
        columns.add(slide);
        cells.add(new Cell(String.valueOf(pptState.getSlideIndex()), slide));
        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PowerPointStateEvent: ");
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
