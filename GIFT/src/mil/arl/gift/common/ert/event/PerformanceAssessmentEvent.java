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

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.ConceptAssessment;
import mil.arl.gift.common.IntermediateConceptAssessment;
import mil.arl.gift.common.PerformanceAssessment;
import mil.arl.gift.common.TaskAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class represents a performance assessment event that can be included in an ERT report.  It has the logic to
 * convert a performance assessment object into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class PerformanceAssessmentEvent extends DomainSessionEvent {
    
    /** Column name suffix for the evaluator */
    private static final String EVALUATOR = " evaluator";
    /** Column name suffix for the observer comment */
    private static final String OBSERVER_COMMENT = " observerComment";
    /** Column name suffix for the assessment explanation */
    private static final String ASSESSMENT_EXPLANATION = " assessmentExplanation";
    /** Column name suffix for the confidence */
    private static final String CONFIDENCE = " confidence";
    /** Column name suffix for the competence */
    private static final String COMPETENCE = " competence";
    /** Column name suffix for the trend */
    private static final String TREND = " trend";
    /** Column name suffix for the priority */
    private static final String PRIORITY = " priority";
    /** Column name suffix for the state */
    private static final String STATE = " state";
    /** column name suffix for tasks (to differentiate tasks from concepts) */
    private static final String TASK = " Task";
    /** column name suffix for task state columns */
    public static final String TASK_STATE_SUFFIX = TASK + STATE;
    
    /** mapping of unique task name in the performance assessment message to the cell with the task status value */
    private HashMap<String, Cell> taskStatusCells = new HashMap<>();

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /** the common name of the Performance Assessment event type, shown in the event type column */
    public static final String EventName = MessageTypeEnum.PERFORMANCE_ASSESSMENT.getDisplayName();

    /**
     * Class constructor - set attributes and parse performance assessment event.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param perfAss contains the performance assessment data
     */
    public PerformanceAssessmentEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, PerformanceAssessment perfAss) {
        super(EventName, time, domainSessionMessageEntry, null);
        
        parseEvent(perfAss);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }

    /**
     * Gather information on the columns and cell content for the content of the
     * performance assessment
     * 
     * @param perfAss the performance assessment to parse
     */
    private void parseEvent(PerformanceAssessment perfAss) {

        //
        // add columns (i.e. header) and cells
        //
        for (TaskAssessment task : perfAss.getTasks()) {
            createCommonColumns(task);

            EventReportColumn stateCol = new EventReportColumn(task.getName() + TASK_STATE_SUFFIX);
            columns.add(stateCol);   
            
            final PerformanceNodeStateEnum nodeState = task.getNodeStateEnum();            
            Cell taskStatusCell = new Cell(nodeState != null ? nodeState.getDisplayName() : null, stateCol);
            taskStatusCells.put(task.getName(), taskStatusCell);
            cells.add(taskStatusCell);

            if (task.getConceptAssessments() != null) {
                for (ConceptAssessment conceptAssessment : task.getConceptAssessments()) {
                    parseEvent(conceptAssessment);
                }
            }
        }
    }

    /**
     * Gather information on the columns and cell content for the content of the
     * concept assessment
     * 
     * @param conceptAssessment the concept assessment to parse
     */
    private void parseEvent(ConceptAssessment conceptAssessment) {
        createCommonColumns(conceptAssessment);

        if (conceptAssessment instanceof IntermediateConceptAssessment) {
            IntermediateConceptAssessment icAssessment = (IntermediateConceptAssessment) conceptAssessment;

            if (icAssessment.getConceptAssessments() != null) {
                for (ConceptAssessment concept : icAssessment.getConceptAssessments()) {
                    parseEvent(concept);
                }
            }
        }
    }

    /**
     * Adds the necessary event report columns using the provided
     * {@link AbstractAssessment}.
     * 
     * @param assessment the assessment to use to populate the event report
     *        columns.
     */
    private void createCommonColumns(AbstractAssessment assessment) {
        if (assessment == null) {
            return;
        }
        
        // used to concatenate a suffix to all task metric column names (to differentiate between tasks and concepts)
        boolean isTaskAssessment = assessment instanceof TaskAssessment;

        final String name = assessment.getName();

        EventReportColumn assessmentCol = new EventReportColumn(name);
        columns.add(assessmentCol);
        final AssessmentLevelEnum assessmentLevel = assessment.getAssessmentLevel();
        cells.add(new Cell(assessmentLevel != null ? assessmentLevel.getDisplayName() : null, assessmentCol));

        EventReportColumn observerCommentCol = new EventReportColumn(name + (isTaskAssessment ? TASK : Constants.EMPTY) + OBSERVER_COMMENT);
        columns.add(observerCommentCol);
        cells.add(new Cell(assessment.getObserverComment(), observerCommentCol));
        
        EventReportColumn assessmentExplanationCol = new EventReportColumn(name + (isTaskAssessment ? TASK : Constants.EMPTY) + ASSESSMENT_EXPLANATION);
        columns.add(assessmentExplanationCol);
        cells.add(new Cell(StringUtils.join(Constants.SEMI_COLON, assessment.getAssessmentExplanation()), assessmentExplanationCol));

        EventReportColumn evaluatorCol = new EventReportColumn(name + (isTaskAssessment ? TASK : Constants.EMPTY) + EVALUATOR);
        columns.add(evaluatorCol);
        cells.add(new Cell(assessment.getEvaluator(), evaluatorCol));

        EventReportColumn confidenceCol = new EventReportColumn(name + (isTaskAssessment ? TASK : Constants.EMPTY) + CONFIDENCE);
        columns.add(confidenceCol);
        cells.add(new Cell(Float.toString(assessment.getConfidence()), confidenceCol));

        EventReportColumn competenceCol = new EventReportColumn(name + (isTaskAssessment ? TASK : Constants.EMPTY) + COMPETENCE);
        columns.add(competenceCol);
        cells.add(new Cell(Float.toString(assessment.getCompetence()), competenceCol));

        EventReportColumn trendCol = new EventReportColumn(name + (isTaskAssessment ? TASK : Constants.EMPTY) + TREND);
        columns.add(trendCol);
        cells.add(new Cell(Float.toString(assessment.getTrend()), trendCol));

        EventReportColumn priorityCol = new EventReportColumn(name + (isTaskAssessment ? TASK : Constants.EMPTY) + PRIORITY);
        columns.add(priorityCol);
        final Integer tPriorityMetric = assessment.getPriority();
        cells.add(new Cell(tPriorityMetric != null ? tPriorityMetric.toString() : null, priorityCol));
    }
    
    /**
     * Return the task status cell for the task specified.
     * 
     * @param taskName the task to get the task status cell for.
     * @return the cell that contains this tasks's status value for this performance assessment event.
     */
    public Cell getTaskStatusCell(String taskName){
        return taskStatusCells.get(taskName);
    }
}
