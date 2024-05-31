/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.ert.event;

import static mil.arl.gift.common.util.StringUtils.join;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mil.arl.gift.common.course.strategy.AuthorizeStrategiesRequest;
import mil.arl.gift.common.course.strategy.StrategyToApply;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.ert.EventReportColumn;
import mil.arl.gift.common.ert.server.Cell;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.logger.DomainSessionMessageEntry;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;

/**
 * Used to extract important information related to an authorize strategies request message.
 * 
 * @author mhoffman
 *
 */
public class AuthorizeStrategiesRequestEvent extends DomainSessionEvent {
    
    /** column name for the evaluator requesting these strategies be applied */
    private static final String EVALUATOR_COL_NAME = "Evaluator";
       
    /** the column name for the strategies to apply */
    private static final String STRATEGY_NAMES_TO_APPLY_COL_NAME = "StrategyNameToApply";
    
    /** the column name for the reason why the authorize request happened, e.g. the state transition name */
    private static final String TRIGGER_COL_NAME = "Caused by";
    
    /** list of columns specific to this event */
    private List<EventReportColumn> columns = new ArrayList<EventReportColumn>();
    
    /**
     * Class constructor - parse the course state object.
     * 
     * @param time - epoch time at which this event occurred
     * @param domainSessionMessageEntry - general info about the domain session message
     * @param authorizeStratgiesRequest - contains information like the reason the request is happening and the
     * strategies being request to execute
     */
    public AuthorizeStrategiesRequestEvent(long time, DomainSessionMessageEntry domainSessionMessageEntry, AuthorizeStrategiesRequest authorizeStratgiesRequest) {
        super(MessageTypeEnum.AUTHORIZE_STRATEGIES_REQUEST.getDisplayName(), time, domainSessionMessageEntry, null);
        
        parseEvent(authorizeStratgiesRequest);
    }
    
    /**
     * Parse the authorize strategies request event and pull out necessary attributes.
     * 
     * @param authorizeStratgiesRequest contains information like the reason the request is happening and the
     * strategies being request to execute.
     */
    private void parseEvent(AuthorizeStrategiesRequest authorizeStratgiesRequest){
        
        EventReportColumn evaluatorCol = new EventReportColumn(EVALUATOR_COL_NAME, EVALUATOR_COL_NAME);
        columns.add(evaluatorCol);
        cells.add(new Cell(authorizeStratgiesRequest.getEvaluator(), evaluatorCol));
        
        //
        // TRIGGER_COL_NAME - keys in the reasonsToActivity map, semi-colon delimited
        // 
        EventReportColumn triggerCol = new EventReportColumn(TRIGGER_COL_NAME, TRIGGER_COL_NAME);
        columns.add(triggerCol);
        StringBuilder reasonsBuilder = new StringBuilder();
        StringUtils.join(Constants.SEMI_COLON, authorizeStratgiesRequest.getRequests().keySet(), reasonsBuilder);
        cells.add(new Cell(reasonsBuilder.toString(), triggerCol));        
        
        //
        // STRATEGY_NAMES_TO_APPLY_COL_NAME - strategy names for each key in the reasonsToActivity map, 
        //                                    elements in each value's list are comma delimited, each list is semi-colon delimited.
        // 
        EventReportColumn strategiesCol = new EventReportColumn(STRATEGY_NAMES_TO_APPLY_COL_NAME, STRATEGY_NAMES_TO_APPLY_COL_NAME);
        columns.add(strategiesCol);
        StringBuilder cellBuilder = new StringBuilder();
        join("; ", authorizeStratgiesRequest.getRequests().entrySet(), new Stringifier<Map.Entry<String, List<StrategyToApply>>>() {

            @Override
            public String stringify(Entry<String, List<StrategyToApply>> obj) {
                
                StringBuilder listBuilder = new StringBuilder();
                StringUtils.join(", ", obj.getValue(), new Stringifier<StrategyToApply>() {
                    
                    @Override
                    public String stringify(StrategyToApply obj) {
                        return obj.getStrategy().getName();
                    }
                }, listBuilder);

                return listBuilder.toString();
            }

        }, cellBuilder);
        cells.add(new Cell(cellBuilder.toString(), strategiesCol));
    }
    
    @Override
    public List<EventReportColumn> getColumns(){
        return columns;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AuthorizeStrategiesRequestEvent: ");
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
