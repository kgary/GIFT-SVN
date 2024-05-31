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

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;

/**
 * Used to analyze all performance assessment events from a single source (e.g. domain session message log file) 
 * and provide additional information in an ERT report (e.g. time on task for each task).
 * 
 * @author mhoffman
 *
 */
public class TimeOnTaskAggregate extends DomainSessionEvent {
    
    /** used as a delimeter between time on task values in a cell for a task (i.e. a task can be started->finished multiple times */
    private static final String SEMI_COLON = ";";
    
    /** the common name of this event, shown in the event type column */
    private static final String eventName = "Time On Task Analysis";
    
    /** time on task column suffix */
    public static final String TIME_ON_TASK = " TimeOnTask";
    /** total time on task column suffix */
    public static final String TOTAL_TIME_ON_TASK = " TotalTimeOnTask";
    
    /**
     * mapping of unique task name to the time on task cell for that task.
     */
    private HashMap<String, Cell> taskTimeOnTaskCells = new HashMap<>();
    
    /**
     * mapping of unique task name to the total time on task cell for that task.
     */
    private HashMap<String, Cell> taskTotalTimeOnTaskCells = new HashMap<>();
    
    /**
     * mapping of unique task name to the time on task column for that task.
     */
    private HashMap<String, EventReportColumn> taskTimeOnTaskCols = new HashMap<>();
    
    /**
     * mapping of unique task name to the total time on task column for that task.
     */
    private HashMap<String, EventReportColumn> taskTotalTimeOnTaskCols = new HashMap<>();
    
    /**
     * mapping of unique task name to the currently active start time (elapsed domain session time).
     * If the task is not active, the task will not have an entry in the map.
     */
    private HashMap<String, Double> taskCurrentStartTime = new HashMap<>();
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Analyze the performance assessment events for time on task.
     * 
     * @param performanceAssessmentEvents contains one or more performance assessment events to analyze.
     * @param userId the user id that caused the assessments
     * @param domainSessionId the session id where the events have taken place.
     * @param participantId participant id from an experiment where the user is anonymous.  Can be null if not in an experiment.
     */
    public TimeOnTaskAggregate(List<PerformanceAssessmentEvent> performanceAssessmentEvents, int userId, int domainSessionId, Integer participantId){
        super(eventName, 0, 0, userId, domainSessionId, null);
        
        parseEvents(performanceAssessmentEvents);
        
        setParticipantId(participantId);
    }
    
    /**
     * Create the two time on task columns for the task specified, if they don't already exist.
     * 
     * @param taskName the task to create the time on task columns for
     */
    private void createColumnsForTask(String taskName){
        
        if(taskTimeOnTaskCells.containsKey(taskName)){
            return;
        }
        
        EventReportColumn timeOnTaskCol = new EventReportColumn(taskName + TIME_ON_TASK);
        taskTimeOnTaskCols.put(taskName, timeOnTaskCol);
        columns.add(timeOnTaskCol);
        EventReportColumn totalTimeOnTaskCol = new EventReportColumn(taskName + TOTAL_TIME_ON_TASK);
        taskTotalTimeOnTaskCols.put(taskName, totalTimeOnTaskCol);
        columns.add(totalTimeOnTaskCol);
    }
    
    /**
     * Analyze the performance assessment events for time on task.
     * 
     * @param performanceAssessmentEvents contains one or more performance assessment events to analyze.
     */
    private void parseEvents(List<PerformanceAssessmentEvent> performanceAssessmentEvents){
        
        for(int index = 0; index < performanceAssessmentEvents.size(); index++){
            PerformanceAssessmentEvent performanceAssessmentEvent = performanceAssessmentEvents.get(index);
            
            for(EventReportColumn perfAssEventCol : performanceAssessmentEvent.getColumns()){
                
                if(perfAssEventCol.getColumnName().endsWith(PerformanceAssessmentEvent.TASK_STATE_SUFFIX)){
                    //found the column with a task state
                    
                    String taskName = perfAssEventCol.getColumnName().substring(0, perfAssEventCol.getColumnName().indexOf(PerformanceAssessmentEvent.TASK_STATE_SUFFIX));
                    createColumnsForTask(taskName);
                    
                    Cell taskStatusCell = performanceAssessmentEvent.getTaskStatusCell(taskName);
                    if(taskStatusCell == null){
                        //unable to find this tasks's status cell
                        continue;
                    }
                    
                    Double startTime = taskCurrentStartTime.get(taskName);
                    if(startTime == null){
                        // check if this performance assessment has this task transitioning to an active state
                        
                        if(taskStatusCell.getValue().equals(PerformanceNodeStateEnum.ACTIVE.getDisplayName())){
                            //this task is now active
                            taskCurrentStartTime.put(taskName, performanceAssessmentEvent.getDomainSessionTime());
                            continue;
                        }
                    }else{
                        // check if this performance assessment has this task transitioning to a non-active state
                     
                        if(!taskStatusCell.getValue().equals(PerformanceNodeStateEnum.ACTIVE.getDisplayName())){
                            // this task is NOT active
                            
                            Double endTime = performanceAssessmentEvent.getDomainSessionTime();
                            double timeOnTaskInterval = endTime - startTime;
                            addTimeOnTaskCell(taskName, timeOnTaskInterval);
                            
                            // the task is no longer active, remove it so the next active cycle can be calculated independently
                            taskCurrentStartTime.remove(taskName);
                        }
                    }
                }
            }// end for on performance assessment columns
            
            if(index+1 == performanceAssessmentEvents.size()){
                // this is the last performance assessment, set the time on task aggregate event time to the last performance assessment event
                // in order to have a non-zero value
                setTime(performanceAssessmentEvent.getTime());
                setDomainSessionTime(performanceAssessmentEvent.getDomainSessionTime());
            }
        }
        
        // for tasks that never gracefully ended, show the time as well
        for(String taskName : taskCurrentStartTime.keySet()){
            
            double timeOnTaskInterval = getDomainSessionTime() - taskCurrentStartTime.get(taskName);
            addTimeOnTaskCell(taskName, timeOnTaskInterval);
        }

    }
    
    /**
     * Add the time on task cell information provided.  If this cell already exists the value
     * provided is concatenated to the existing cell value.
     * 
     * @param taskName the name of the task to add time on task info for
     * @param duration the value to add to that cell for that column (seconds)
     */
    public void addTimeOnTaskCell(String taskName, double duration){ 
        
        Cell newTimeOnTaskCell;
        String cellValue = String.valueOf(duration);
        
        Cell currTimeOnTaskCell = taskTimeOnTaskCells.get(taskName);
        if(currTimeOnTaskCell == null){
            EventReportColumn timeOnTaskCol = taskTimeOnTaskCols.get(taskName);
            newTimeOnTaskCell = new Cell(cellValue, timeOnTaskCol);
            cells.add(newTimeOnTaskCell);
        }else{
            // concatenate values since tasks can be repeated
            newTimeOnTaskCell = new Cell(currTimeOnTaskCell.getValue() + SEMI_COLON + cellValue, currTimeOnTaskCell.getColumn());
            cells.set(cells.indexOf(currTimeOnTaskCell), newTimeOnTaskCell);
        }
        
        taskTimeOnTaskCells.put(taskName, newTimeOnTaskCell);   
        
        // Handle time on task total column with its single value
        Cell newTimeOnTaskTotalCell;
        
        Cell currTimeOnTaskTotalCell = taskTotalTimeOnTaskCells.get(taskName);
        if(currTimeOnTaskTotalCell == null){
            EventReportColumn totalTimeOnTaskCol = taskTotalTimeOnTaskCols.get(taskName);
            newTimeOnTaskTotalCell = new Cell(cellValue, totalTimeOnTaskCol);
            cells.add(newTimeOnTaskTotalCell);
        }else{
            double newValue = Double.valueOf(currTimeOnTaskTotalCell.getValue()) + duration;
            String totalCellValue = String.valueOf(newValue);
            newTimeOnTaskTotalCell = new Cell(totalCellValue, currTimeOnTaskTotalCell.getColumn());
            cells.set(cells.indexOf(currTimeOnTaskTotalCell), newTimeOnTaskTotalCell);
        }
        
        taskTotalTimeOnTaskCells.put(taskName, newTimeOnTaskTotalCell); 
    }

    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }

}
