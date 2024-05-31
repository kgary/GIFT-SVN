/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.ert.server.Row;
import mil.arl.gift.common.io.Constants;

/**
 * Used to analyze all branch path history events from a single source (e.g. domain session message log file) 
 * and provide additional information in an ERT report (e.g. duratin within each branch path).
 * 
 * @author mhoffman
 *
 */
public class BranchPathHistoryAggregate extends DomainSessionEvent {
    
    /** the common name of this event, shown in the event type column */
    private static final String eventName = "Time On Task Analysis";

    /** the suffix for the column name for each branch path duration column */
    private static final String BRANCH_DURATION_COL_NAME_SUFFIX = " Branch duration";
    
    /**
     * mapping of unique branch path name to the duration cell for that branch path.
     */
    private HashMap<String, Cell> branchPathDurationCells = new HashMap<>();
    
    /**
     * mapping of unique branch path name to the duration column for that branch path.
     */
    private HashMap<String, EventReportColumn> branchPathDurationCols = new HashMap<>();
    
    /**
     * mapping of unique branch path name to the currently entered path start time (elapsed domain session time).
     * If the branch path is not entered, the branch path will not have an entry in the map.
     */
    private HashMap<String, Double> branchPathCurrentStartTime = new HashMap<>();
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Analyze the branch path history events for duration in each path.
     * 
     * @param branchPathHistoryEvents contains one or more branch path history events to analyze.
     * @param userId the user id that caused the branch path history event
     * @param domainSessionId the session id where the events have taken place.
     * @param participantId participant id from an experiment where the user is anonymous.  Can be null if not in an experiment.
     */
    public BranchPathHistoryAggregate(List<BranchPathHistoryEvent> branchPathHistoryEvents, int userId, int domainSessionId, Integer participantId){
        super(eventName, 0, 0, userId, domainSessionId, null);
        
        parseEvents(branchPathHistoryEvents);
        
        setParticipantId(participantId);
    }
    
    /**
     * Create the branch path duration column for the branch path specified, if it doesn't already exist.
     * 
     * @param branchPathName the branch path name to create the branch path duration column for
     */
    private void createColumnsForBranchPath(String branchPathName){
        
        if(branchPathDurationCells.containsKey(branchPathName)){
            return;
        }
        
        EventReportColumn branchPathDurationCol = new EventReportColumn(branchPathName + BRANCH_DURATION_COL_NAME_SUFFIX);
        branchPathDurationCols.put(branchPathName, branchPathDurationCol);
        columns.add(branchPathDurationCol);
    }
    
    /**
     * Analyze the branch path history events for path durations.
     * 
     * @param branchPathHistoryEvents contains one or more branch path history events to analyze.
     */
    private void parseEvents(List<BranchPathHistoryEvent> branchPathHistoryEvents){
        
        for(int index = 0; index < branchPathHistoryEvents.size(); index++){
            BranchPathHistoryEvent branchPathHistoryEvent = branchPathHistoryEvents.get(index);
                            
            Row row = branchPathHistoryEvent.toRow();
            
            for(Cell cell : row.getCells()){                
                
                if(cell.getColumn().getColumnName().endsWith(BranchPathHistoryEvent.BRANCH_PATH_ENTERED)){
                    //the start of a branch path                    
                    
                    String pathName = cell.getValue();
                    createColumnsForBranchPath(pathName);
                    
                    Double startTime = branchPathCurrentStartTime.get(pathName);
                    if(startTime == null){
                        // check if this branch path assessment has this task transitioning to an active state
                        
                        branchPathCurrentStartTime.put(pathName, branchPathHistoryEvent.getDomainSessionTime());
                        continue;
                    }
                    
                }else if(cell.getColumn().getColumnName().endsWith(BranchPathHistoryEvent.BRANCH_PATH_EXITED)){
                    //the end of a branch path
                    
                    String pathName = cell.getValue();                        
                    Double endTime = branchPathHistoryEvent.getDomainSessionTime();
                    Double startTime = branchPathCurrentStartTime.get(pathName);
                    double pathDuration = endTime - startTime;
                    addBranchPathDurationCell(pathName, pathDuration);
                    
                    // the path is no longer active, remove it so the next active cycle can be calculated independently
                    branchPathCurrentStartTime.remove(pathName);

                }
            }
            
        }
        
    }
    
    /**
     * Add the branch path duration cell information provided.  If this cell already exists the value
     * provided is concatenated to the existing cell value.
     * 
     * @param pathName the name of the branch path to add duration info for
     * @param duration the value to add to that cell for that column (seconds)
     */
    public void addBranchPathDurationCell(String branchPathName, double duration){ 
        
        Cell newBranchPathDurationCell;
        String cellValue = String.valueOf(duration);
        
        Cell currBranchPathDurationCell = branchPathDurationCells.get(branchPathName);
        if(currBranchPathDurationCell == null){
            EventReportColumn branchPathDurationCol = branchPathDurationCols.get(branchPathName);
            newBranchPathDurationCell = new Cell(cellValue, branchPathDurationCol);
            cells.add(newBranchPathDurationCell);
        }else{
            // concatenate values since branch paths might be repeatable in the future
            newBranchPathDurationCell = new Cell(currBranchPathDurationCell.getValue() + Constants.SEMI_COLON + cellValue, currBranchPathDurationCell.getColumn());
            cells.set(cells.indexOf(currBranchPathDurationCell), newBranchPathDurationCell);
        }
        
        branchPathDurationCells.put(branchPathName, newBranchPathDurationCell);   
    }
                
    
    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[BranchPathHistoryAggregate: ");
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
