/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails;
import mil.arl.gift.common.course.dkf.session.KnowledgeAssessmentDetails.KnowledgeSessionVariable;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;

/**
 * Custom parser for branch path history messages.
 * 
 * @author mhoffman
 */
public class KnowledgeAssessmentDetailsEvent extends DomainSessionEvent {

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
    public KnowledgeAssessmentDetailsEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, KnowledgeAssessmentDetails details) {
        super(MessageTypeEnum.KNOWLEDGE_ASSESSMENT_DETAILS.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(details);
    }

    /**
     * Parse the branch path history event and add either a branch path entered or branch path exited cell.
     * 
     * @param branchPathHistory the event to analyze, can't be null.
     */
    private void parseEvent(KnowledgeAssessmentDetails details){
        
        List<String> variableColumns = new ArrayList<>();
        
        for(KnowledgeSessionVariable variable : details.getVariables()) {
            
            variableColumns.add(variable.getName());
            
            /* Variables can be duplicated for multiple actors, so need to account for if
             * the same variable name occurs more than once and increment it */
            int numOccurences = Collections.frequency(variableColumns, variable.getName());
            String colSuffix = "_" + numOccurences;
            
            String valueColName = variable.getName() + colSuffix + "_Value";
            String unitsColName = variable.getName() + colSuffix + "_Units";
            String actorColName = variable.getName() + colSuffix + "_Actor";
            
            EventReportColumn valueCol = new EventReportColumn(valueColName, valueColName);
            columns.add(valueCol);
            cells.add(new Cell(variable.getValue(), valueCol));
            
            EventReportColumn unitsCol = new EventReportColumn(unitsColName, unitsColName);
            columns.add(unitsCol);
            cells.add(new Cell(variable.getUnits(), unitsCol));
            
            EventReportColumn actorCol = new EventReportColumn(actorColName, actorColName);
            columns.add(actorCol);
            cells.add(new Cell(variable.getActor(), actorCol));
        }
    }

    @Override
    public List<EventReportColumn> getColumns() {
        return columns;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[KnowledgeAssessmentDetails: ");
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
