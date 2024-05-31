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

import mil.arl.gift.common.course.strategy.ApplyStrategies;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;

/**
 * Used to extract important information from an apply strategies message.
 * 
 * @author mhoffman
 *
 */
public class ApplyStrategyEvent extends DomainSessionEvent {
    
    /** column name for the evaluator requesting these strategies be applied */
    private static final String EVALUATOR_COL_NAME = "Evaluator";
    
    /** column name for the strategies to apply */
    private static final String STRATEGY_NAMES_TO_APPLY_COL_NAME = "StrategyNameToApply";
    
    /** column name for the reason the strategies should be applied */
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
    public ApplyStrategyEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, ApplyStrategies applyStrategies) {
        super(MessageTypeEnum.APPLY_STRATEGIES.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(applyStrategies);
    }
    
    /**
     * Parse the apply strategies event and pull out necessary attributes.
     * 
     * @param applyStrategies contains information like the evaluator requesting these strategies be 
     * applied and the strategies to apply.
     */
    private void parseEvent(ApplyStrategies applyStrategies){
        
        EventReportColumn evaluatorCol = new EventReportColumn(EVALUATOR_COL_NAME, EVALUATOR_COL_NAME);
        columns.add(evaluatorCol);
        cells.add(new Cell(applyStrategies.getEvaluator(), evaluatorCol));
        
        EventReportColumn strategyNamesCol = new EventReportColumn(STRATEGY_NAMES_TO_APPLY_COL_NAME, STRATEGY_NAMES_TO_APPLY_COL_NAME);
        columns.add(strategyNamesCol);
        StringBuilder strategyNamesBuilder = new StringBuilder();
        StringUtils.join(Constants.SEMI_COLON, applyStrategies.getStrategies(), new Stringifier<StrategyToApply>() {
            @Override
            public String stringify(StrategyToApply strategyToApply) {
                return strategyToApply.getStrategy().getName();
            }
        }, strategyNamesBuilder);
        cells.add(new Cell(strategyNamesBuilder.toString(), strategyNamesCol));
        
        if(!applyStrategies.getStrategies().isEmpty()){
            // currently the same trigger string is copied to all strategies in an apply strategies object.
            EventReportColumn triggerCol = new EventReportColumn(TRIGGER_COL_NAME, TRIGGER_COL_NAME);
            columns.add(triggerCol);
            cells.add(new Cell(applyStrategies.getStrategies().get(0).getTrigger(), triggerCol));
        }
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ApplyStrategyEvent: ");
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
