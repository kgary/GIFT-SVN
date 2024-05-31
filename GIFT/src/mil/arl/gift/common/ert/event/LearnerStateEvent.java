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

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.state.AffectiveState;
import mil.arl.gift.common.state.CognitiveState;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.LearnerStateAttribute;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.PerformanceStateAttribute;
import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class represents a learner state event that can be included in an ERT report.  It has the logic to
 * convert a learner state object into cells for a report.   
 * 
 * @author mhoffman
 *
 */
public class LearnerStateEvent extends DomainSessionEvent {

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();

    /** Column name suffix for the short term assessment */
    private static final String SHORT_TERM = " ShortTerm";
    /** Column name suffix for the long term assessment */
    private static final String LONG_TERM = " LongTerm";
    /** Column name suffix for the predicted assessment */
    private static final String PREDICTED = " Predicted";
    /** Column name suffix for the evaluator */
    private static final String EVALUATOR = " evaluator";
    /** Column name suffix for the assessment lock state */
    private static final String ASSESSMENT_HOLD = " assessment locked";
    /** Column name suffix for the confidence */
    private static final String CONFIDENCE = " confidence";
    /** Column name suffix for the confidence lock state */
    private static final String CONFIDENCE_HOLD = CONFIDENCE + " locked";
    /** Column name suffix for the competence */
    private static final String COMPETENCE = " competence";
    /** Column name suffix for the competence lock state */
    private static final String COMPETENCE_HOLD = COMPETENCE + " locked";
    /** Column name suffix for the trend */
    private static final String TREND = " trend";
    /** Column name suffix for the trend lock state */
    private static final String TREND_HOLD = TREND + " locked";
    /** Column name suffix for the priority */
    private static final String PRIORITY = " priority";
    /** Column name suffix for the priority lock state */
    private static final String PRIORITY_HOLD = PRIORITY + " locked";
    /** Column name suffix for the state */
    private static final String STATE = " state";
    /** Column name suffix for the observer comment */
    private static final String OBSERVER_COMMENT = " observer comment";
    /** Column name suffix for the assessment explanation */
    private static final String ASSESSMENT_EXPLANATION = " assessment explanation";

    /**
     * Class constructor - set attributes and parse learner state
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param state - the learner state content for the event
     */
    public LearnerStateEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, LearnerState state){
        super(MessageTypeEnum.LEARNER_STATE.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(state);
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }

    /**
     * Gather information on the columns and cell content for the content of the
     * learner state
     * 
     * @param state the learner state to parse.
     */
    private void parseEvent(LearnerState state) {

        AffectiveState aState = state.getAffective();
        for (LearnerStateAttribute sAttr : aState.getAttributes().values()) {
            //
            // add columns (i.e. header) and cells
            //
            EventReportColumn st = new EventReportColumn(sAttr.getName() + SHORT_TERM);
            columns.add(st);
            cells.add(new Cell(sAttr.getShortTerm().getDisplayName(), st));

            EventReportColumn lt = new EventReportColumn(sAttr.getName() + LONG_TERM);
            columns.add(lt);
            cells.add(new Cell(sAttr.getLongTerm().getDisplayName(), lt));

            EventReportColumn pred = new EventReportColumn(sAttr.getName() + PREDICTED);
            columns.add(pred);
            cells.add(new Cell(sAttr.getPredicted().getDisplayName(), pred));

        }

        CognitiveState cState = state.getCognitive();
        for (LearnerStateAttribute sAttr : cState.getAttributes().values()) {
            //
            // add columns (i.e. header) and cells
            //
            EventReportColumn st = new EventReportColumn(sAttr.getName() + SHORT_TERM);
            columns.add(st);
            cells.add(new Cell(sAttr.getShortTerm().getDisplayName(), st));

            EventReportColumn lt = new EventReportColumn(sAttr.getName() + LONG_TERM);
            columns.add(lt);
            cells.add(new Cell(sAttr.getLongTerm().getDisplayName(), lt));

            EventReportColumn pred = new EventReportColumn(sAttr.getName() + PREDICTED);
            columns.add(pred);
            cells.add(new Cell(sAttr.getPredicted().getDisplayName(), pred));

        }

        PerformanceState pState = state.getPerformance();
        for (TaskPerformanceState task : pState.getTasks().values()) {
            createCommonColumns(task.getState());

            for (ConceptPerformanceState concept : task.getConcepts()) {
                createCommonColumns(concept.getState());
            }
        }
    }

    /**
     * Adds the necessary event report columns using the provided
     * {@link PerformanceStateAttribute}.
     * 
     * @param attribute the performance state attribute to use to populate the
     *        event report columns.
     */
    private void createCommonColumns(PerformanceStateAttribute attribute) {
        if (attribute == null) {
            return;
        }

        final String name = attribute.getName();

        EventReportColumn shortTermCol = new EventReportColumn(name + SHORT_TERM);
        columns.add(shortTermCol);
        cells.add(new Cell(attribute.getShortTerm().getDisplayName(), shortTermCol));

        EventReportColumn longTermCol = new EventReportColumn(name + LONG_TERM);
        columns.add(longTermCol);
        cells.add(new Cell(attribute.getLongTerm().getDisplayName(), longTermCol));

        EventReportColumn predictedCol = new EventReportColumn(name + PREDICTED);
        columns.add(predictedCol);
        cells.add(new Cell(attribute.getPredicted().getDisplayName(), predictedCol));

        EventReportColumn assessmentHoldCol = new EventReportColumn(name + ASSESSMENT_HOLD);
        columns.add(assessmentHoldCol);
        cells.add(new Cell(Boolean.toString(Boolean.TRUE.equals(attribute.isAssessmentHold())), assessmentHoldCol));

        EventReportColumn confidenceCol = new EventReportColumn(name + CONFIDENCE);
        columns.add(confidenceCol);
        cells.add(new Cell(Float.toString(attribute.getConfidence()), confidenceCol));

        EventReportColumn confidenceHoldCol = new EventReportColumn(name + CONFIDENCE_HOLD);
        columns.add(confidenceHoldCol);
        cells.add(new Cell(Boolean.toString(Boolean.TRUE.equals(attribute.isConfidenceHold())), confidenceHoldCol));

        EventReportColumn competenceCol = new EventReportColumn(name + COMPETENCE);
        columns.add(competenceCol);
        cells.add(new Cell(Float.toString(attribute.getCompetence()), competenceCol));

        EventReportColumn competenceHoldCol = new EventReportColumn(name + COMPETENCE_HOLD);
        columns.add(competenceHoldCol);
        cells.add(new Cell(Boolean.toString(Boolean.TRUE.equals(attribute.isCompetenceHold())), competenceHoldCol));

        EventReportColumn trendCol = new EventReportColumn(name + TREND);
        columns.add(trendCol);
        cells.add(new Cell(Float.toString(attribute.getTrend()), trendCol));

        EventReportColumn trendHoldCol = new EventReportColumn(name + TREND_HOLD);
        columns.add(trendHoldCol);
        cells.add(new Cell(Boolean.toString(Boolean.TRUE.equals(attribute.isTrendHold())), trendHoldCol));

        EventReportColumn priorityCol = new EventReportColumn(name + PRIORITY);
        columns.add(priorityCol);
        final Integer priorityMetric = attribute.getPriority();
        cells.add(new Cell(priorityMetric != null ? priorityMetric.toString() : null, priorityCol));

        EventReportColumn priorityHoldCol = new EventReportColumn(name + PRIORITY_HOLD);
        columns.add(priorityHoldCol);
        cells.add(new Cell(Boolean.toString(Boolean.TRUE.equals(attribute.isPriorityHold())), priorityHoldCol));

        EventReportColumn stateCol = new EventReportColumn(name + STATE);
        columns.add(stateCol);
        final PerformanceNodeStateEnum nodeState = attribute.getNodeStateEnum();
        cells.add(new Cell(nodeState != null ? nodeState.getDisplayName() : null, stateCol));

        EventReportColumn evaluatorCol = new EventReportColumn(name + EVALUATOR);
        columns.add(evaluatorCol);
        cells.add(new Cell(attribute.getEvaluator(), evaluatorCol));

        EventReportColumn observerCommentCol = new EventReportColumn(name + OBSERVER_COMMENT);
        columns.add(observerCommentCol);
        cells.add(new Cell(attribute.getObserverComment(), observerCommentCol));
        
        EventReportColumn assessmentExplanationCol = new EventReportColumn(name + ASSESSMENT_EXPLANATION);
        columns.add(assessmentExplanationCol);
        cells.add(new Cell(StringUtils.join(Constants.SEMI_COLON, attribute.getAssessmentExplanation()), assessmentExplanationCol));
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[LearnerStateEvent: ");
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
