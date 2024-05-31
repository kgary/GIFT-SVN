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

import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;

/**
 * Used to extract important attributes from an Execute OC strategy message.
 * 
 * @author mhoffman
 *
 */
public class ExecuteOCStrategyEvent extends DomainSessionEvent {
    
    /** column name for the optional evaluator making the request */
    private static final String EVALUATOR_COL_NAME = "Evaluator";
    
    /** column name for the name of the strategy being applied on the observer */
    private static final String STRATEGY_NAME_TO_APPLY_COL_NAME = "StrategyNameToApply";
    
    /** column name for the reason the strategy is being applied */
    private static final String TRIGGER_COL_NAME = "Caused by";
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Class constructor - parse the course state object.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param applyStrategies - the apply strategies for this event
     */
    public ExecuteOCStrategyEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, ExecuteOCStrategy executeOCStrategy) {
        super(MessageTypeEnum.EXECUTE_OC_STRATEGY.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(executeOCStrategy);
    }
    
    /**
     * Parse the execute OC strategy event and pull out necessary attributes.
     * 
     * @param executeOCStrategy contains information like the evaluator requesting these strategies be 
     * applied and the strategy to apply.
     */
    private void parseEvent(ExecuteOCStrategy executeOCStrategy){
        
        EventReportColumn evaluatorCol = new EventReportColumn(EVALUATOR_COL_NAME, EVALUATOR_COL_NAME);
        columns.add(evaluatorCol);
        cells.add(new Cell(executeOCStrategy.getEvaluator(), evaluatorCol));
        
        EventReportColumn strategyNamesCol = new EventReportColumn(STRATEGY_NAME_TO_APPLY_COL_NAME, STRATEGY_NAME_TO_APPLY_COL_NAME);
        columns.add(strategyNamesCol);
        cells.add(new Cell(executeOCStrategy.getStrategy().getName(), strategyNamesCol));
        
        EventReportColumn triggerCol = new EventReportColumn(TRIGGER_COL_NAME, TRIGGER_COL_NAME);
        columns.add(triggerCol);
        cells.add(new Cell(executeOCStrategy.getReason(), triggerCol));
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ExecuteOCStrategyEvent: ");
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
