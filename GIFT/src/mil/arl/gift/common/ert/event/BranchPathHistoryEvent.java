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

import mil.arl.gift.common.BranchPathHistory;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;

/**
 * Custom parser for branch path history messages.
 * 
 * @author mhoffman
 */
public class BranchPathHistoryEvent extends DomainSessionEvent {

    /** The name of the column for reporting the name of the branch path chosen */
    public static final String BRANCH_PATH_ENTERED = "BranchPathEntered";

    /** The name of the column for reporting the name of the branch that is being exited */
    public static final String BRANCH_PATH_EXITED = "BranchPathExited";

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();

    /**
     * Class constructor - parse the course state object.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param courseState - the course state for this event
     */
    public BranchPathHistoryEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, BranchPathHistory branchPathHistory) {
        super(MessageTypeEnum.BRANCH_PATH_HISTORY_UPDATE.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(branchPathHistory);
    }

    /**
     * Parse the branch path history event and add either a branch path entered or branch path exited cell.
     * 
     * @param branchPathHistory the event to analyze, can't be null.
     */
    private void parseEvent(BranchPathHistory branchPathHistory){
        
        if (branchPathHistory.isPathEnding()) {
            EventReportColumn branchPathChosenNameCol = new EventReportColumn(BRANCH_PATH_EXITED, BRANCH_PATH_EXITED);
            columns.add(branchPathChosenNameCol);
            cells.add(new Cell(branchPathHistory.getPathName(), branchPathChosenNameCol));
        } else {
            EventReportColumn branchPathChosenNameCol = new EventReportColumn(BRANCH_PATH_ENTERED, BRANCH_PATH_ENTERED);
            columns.add(branchPathChosenNameCol);
            cells.add(new Cell(branchPathHistory.getPathName(), branchPathChosenNameCol));
        }
    }

    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[BranchPathHistoryEvent: ");
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
