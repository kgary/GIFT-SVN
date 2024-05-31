/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.domain.knowledge.strategy.StrategyHandlerInterface;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchStrategyHandlerClassNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchStrategyHandlerClassNamesResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ActionHandler for actions of type FetchStrategyHandlerClassNames.
 */ 
public class FetchStrategyHandlerClassNamesHandler implements ActionHandler<FetchStrategyHandlerClassNames, FetchStrategyHandlerClassNamesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchStrategyHandlerClassNamesHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchStrategyHandlerClassNames> getActionType() {
        return FetchStrategyHandlerClassNames.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized FetchStrategyHandlerClassNamesResult execute( FetchStrategyHandlerClassNames action, ExecutionContext context ) 
            throws ActionException {
        long start = System.currentTimeMillis();
        logger.info("execute()");
        
        HashSet<String> classNames = new HashSet<String>();
		try{
            String packageName = "mil.arl.gift.domain.knowledge.strategy";
            List<Class<?>> classes = ClassFinderUtil.getSubClassesOf(packageName, StrategyHandlerInterface.class);
            for(Class<?> clazz : classes){
                
            	String className = clazz.getCanonicalName().replace("mil.arl.gift.", "");
            	classNames.add(className);
            }
        
        }catch(Exception e){
            logger.error("Caught exception while trying to populate the list of strategy handlers.", e);
        	FetchStrategyHandlerClassNamesResult result = new FetchStrategyHandlerClassNamesResult();
        	result.setSuccess(false);
        	result.setErrorMsg("Failed to retrieve the list of Strategy Handlers, therefore there won't be any Strategy handlers to choose from when authoring instructional strategies.");
            return result;
        }
        
		ArrayList<String> classNamesAsSortedList = new ArrayList<String>(classNames);
        Collections.sort(classNamesAsSortedList);
		
        FetchStrategyHandlerClassNamesResult result = new FetchStrategyHandlerClassNamesResult();
        result.setStrategyHandlerClassNames(classNamesAsSortedList);
        MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.FetchRootDirectoryModel", start);
		return result;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback( FetchStrategyHandlerClassNames action, FetchStrategyHandlerClassNamesResult result, ExecutionContext context ) 
            throws ActionException {

    }
}
