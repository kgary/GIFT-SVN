/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchConditionImplNamesResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class FetchConditionImplNamesHandler.
 */
public class FetchConditionImplNamesHandler implements ActionHandler<FetchConditionImplNames, FetchConditionImplNamesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchConditionImplNamesHandler.class);

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public FetchConditionImplNamesResult execute(
            FetchConditionImplNames action, ExecutionContext context)
                    throws ActionException {
        long start = System.currentTimeMillis();
        logger.info("execute()");
        
        FetchConditionImplNamesResult result = new FetchConditionImplNamesResult();
        
        List<String> classNames = new ArrayList<String>();
        
        try {            
            List<Class<?>> classes = ClassFinderUtil.getSubClassesOf("mil.arl.gift.domain.knowledge.condition", AbstractCondition.class);           
            for(Class<?>clazz : classes) {
                classNames.add(clazz.getName());
            } 
            
            Collections.sort(classNames);
            result.setClassNames(classNames);
            
        } catch (ClassNotFoundException | IOException | IllegalStateException  e) {  
            logger.error("Caught exception while getting list of condition implementations. ", e);
            result.setSuccess(false);
            result.setErrorMsg("Failed to retrieve the list of condition implementation classes, therefore there won't be any to select from when authoring a condition.");
            result.setErrorDetails(e.toString());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }
        MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.FetchConditionImplNames", start);
        return result;
    }


    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchConditionImplNames> getActionType() {
        return FetchConditionImplNames.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public void rollback(FetchConditionImplNames arg0,
            FetchConditionImplNamesResult arg1, ExecutionContext arg2)
                    throws ActionException {
    }
}
