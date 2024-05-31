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
import java.util.Map;
import java.util.Map.Entry;

import mil.arl.gift.common.EvaluatorUpdateRequest;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;

/**
 * This class represents an evaluator update request event that can be included
 * in an ERT report. It has the logic to convert an evaluator update object into
 * cells for a report.
 * 
 * @author sharrison
 */
public class EvaluatorUpdateRequestEvent extends DomainSessionEvent {

    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();

    /** Column name suffix for the evaluator */
    private static final String EVALUATOR = " change evaluator";
    /** Column name suffix for the assessment short term state */
    private static final String SHORT_TERM = " change ShortTerm";
    /** Column name suffix for the assessment lock state */
    private static final String ASSESSMENT_HOLD = SHORT_TERM + " locked";
    /** Column name suffix for the confidence */
    private static final String CONFIDENCE = " change confidence";
    /** Column name suffix for the confidence lock state */
    private static final String CONFIDENCE_HOLD = CONFIDENCE + " locked";
    /** Column name suffix for the competence */
    private static final String COMPETENCE = " change competence";
    /** Column name suffix for the competence lock state */
    private static final String COMPETENCE_HOLD = COMPETENCE + " locked";
    /** Column name suffix for the trend */
    private static final String TREND = " change trend";
    /** Column name suffix for the trend lock state */
    private static final String TREND_HOLD = TREND + " locked";
    /** Column name suffix for the priority */
    private static final String PRIORITY = " change priority";
    /** Column name suffix for the priority lock state */
    private static final String PRIORITY_HOLD = PRIORITY + " locked";
    /** Column name suffix for the state */
    private static final String STATE = " change state";
    /** Column name suffix for the reason */
    private static final String REASON = " change reason";
    /** Column name suffix for the team org entities */
    private static final String TEAM_ORG_ENTITIES = " change team org entities";
    
    /** 
     * The prefix to use in the column name for global update requests applied to the whole scenario.
     * This will replace the node name that is normally used for performance node updates.
     */
    private static final String GLOBAL_REQUEST_COLUMN_PREFIX = "Session";

    /**
     * Class constructor - set attributes and parse evaluator update events.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session
     *        message
     * @param updateRequest - contains the evaluator update request data. Can't
     *        be null.
     */
    public EvaluatorUpdateRequestEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry,
            EvaluatorUpdateRequest updateRequest) {
        super(MessageTypeEnum.EVALUATOR_UPDATE_REQUEST.getDisplayName(), time, domainSessionMessageEntry, null);

        parseEvent(updateRequest);
    }

    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }

    /**
     * Gather information on the columns and cell content for the content of the
     * update request
     * 
     * @param updateRequest - contains the evaluator update request data. Can't
     *        be null.
     */
    private void parseEvent(EvaluatorUpdateRequest updateRequest) {
        final String name = updateRequest.getNodeName() != null 
                ? updateRequest.getNodeName()
                : GLOBAL_REQUEST_COLUMN_PREFIX;

        //
        // add columns (i.e. header) and cells
        //
        EventReportColumn evaluatorCol = new EventReportColumn(name + EVALUATOR);
        columns.add(evaluatorCol);
        cells.add(new Cell(updateRequest.getEvaluator(), evaluatorCol));

        EventReportColumn assessmentCol = new EventReportColumn(name + SHORT_TERM);
        columns.add(assessmentCol);
        final AssessmentLevelEnum performanceMetric = updateRequest.getPerformanceMetric();
        cells.add(new Cell(performanceMetric != null ? performanceMetric.getDisplayName() : null, assessmentCol));

        EventReportColumn assessmentHoldCol = new EventReportColumn(name + ASSESSMENT_HOLD);
        columns.add(assessmentHoldCol);
        final Boolean assessmentHold = updateRequest.isAssessmentHold();
        cells.add(new Cell(assessmentHold != null ? assessmentHold.toString() : null, assessmentHoldCol));

        EventReportColumn confidenceCol = new EventReportColumn(name + CONFIDENCE);
        columns.add(confidenceCol);
        final Float confidenceMetric = updateRequest.getConfidenceMetric();
        cells.add(new Cell(confidenceMetric != null ? confidenceMetric.toString() : null, confidenceCol));

        EventReportColumn confidenceHoldCol = new EventReportColumn(name + CONFIDENCE_HOLD);
        columns.add(confidenceHoldCol);
        final Boolean confidenceHold = updateRequest.isConfidenceHold();
        cells.add(new Cell(confidenceHold != null ? confidenceHold.toString() : null, confidenceHoldCol));

        EventReportColumn competenceCol = new EventReportColumn(name + COMPETENCE);
        columns.add(competenceCol);
        final Float competenceMetric = updateRequest.getCompetenceMetric();
        cells.add(new Cell(competenceMetric != null ? competenceMetric.toString() : null, competenceCol));

        EventReportColumn competenceHoldCol = new EventReportColumn(name + COMPETENCE_HOLD);
        columns.add(competenceHoldCol);
        final Boolean competenceHold = updateRequest.isCompetenceHold();
        cells.add(new Cell(competenceHold != null ? competenceHold.toString() : null, competenceHoldCol));

        EventReportColumn trendCol = new EventReportColumn(name + TREND);
        columns.add(trendCol);
        final Float trendMetric = updateRequest.getTrendMetric();
        cells.add(new Cell(trendMetric != null ? trendMetric.toString() : null, trendCol));

        EventReportColumn trendHoldCol = new EventReportColumn(name + TREND_HOLD);
        columns.add(trendHoldCol);
        final Boolean trendHold = updateRequest.isTrendHold();
        cells.add(new Cell(trendHold != null ? trendHold.toString() : null, trendHoldCol));

        EventReportColumn priorityCol = new EventReportColumn(name + PRIORITY);
        columns.add(priorityCol);
        final Integer priorityMetric = updateRequest.getPriorityMetric();
        cells.add(new Cell(priorityMetric != null ? priorityMetric.toString() : null, priorityCol));

        EventReportColumn priorityHoldCol = new EventReportColumn(name + PRIORITY_HOLD);
        columns.add(priorityHoldCol);
        final Boolean priorityHold = updateRequest.isPriorityHold();
        cells.add(new Cell(priorityHold != null ? priorityHold.toString() : null, priorityHoldCol));

        /* Only report the state if it exists. This is a workaround to prevent
         * inserting a state column for each concept since they are not allowed
         * to be updated manually and therefor will always be null here. */
        if (updateRequest.getState() != null) {
            EventReportColumn stateCol = new EventReportColumn(name + STATE);
            columns.add(stateCol);
            cells.add(new Cell(updateRequest.getState().getDisplayName(), stateCol));
        }

        EventReportColumn reasonCol = new EventReportColumn(name + REASON);
        columns.add(reasonCol);
        cells.add(new Cell(updateRequest.getReason(), reasonCol));

        EventReportColumn teamOrgEntitiesCol = new EventReportColumn(name + TEAM_ORG_ENTITIES);
        columns.add(teamOrgEntitiesCol);
        final Map<String, AssessmentLevelEnum> teamOrgEntities = updateRequest.getTeamOrgEntities();
        final StringBuilder teamEntitySb = new StringBuilder();
        if (CollectionUtils.isNotEmpty(teamOrgEntities)) {
            StringUtils.join(", ", teamOrgEntities.entrySet(), new Stringifier<Entry<String, AssessmentLevelEnum>>() {
                @Override
                public String stringify(Entry<String, AssessmentLevelEnum> obj) {
                    return obj.getKey() + "=" + obj.getValue().getName();
                }
            }, teamEntitySb);
        }
        cells.add(new Cell(teamEntitySb.toString(), teamOrgEntitiesCol));
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[EvaluatorUpdateRequestEvent: ");
        sb.append(super.toString());

        sb.append(", columns = {");
        for (EventReportColumn column : columns) {
            sb.append(column.toString()).append(", ");
        }
        sb.append("}");

        sb.append("]");
        return sb.toString();
    }
}
