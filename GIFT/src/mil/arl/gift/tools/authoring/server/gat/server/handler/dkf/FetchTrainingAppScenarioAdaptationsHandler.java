/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.common.util.DomainKnowledgeUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchTrainingAppScenarioAdaptations;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchTrainingAppScenarioAdaptationsResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The Class FetchTrainingAppScenarioAdaptationsHandler that retrieves the scenario adaptations each training application type supports.
 */
public class FetchTrainingAppScenarioAdaptationsHandler implements ActionHandler<FetchTrainingAppScenarioAdaptations, FetchTrainingAppScenarioAdaptationsResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchTrainingAppScenarioAdaptationsHandler.class);

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public FetchTrainingAppScenarioAdaptationsResult execute(
            FetchTrainingAppScenarioAdaptations action, ExecutionContext context)
                    throws ActionException {
        long start = System.currentTimeMillis();
        
        if(logger.isInfoEnabled()){
            logger.info("execute()");
        }
        
        FetchTrainingAppScenarioAdaptationsResult result = null;
        
        try {            

            Map<TrainingApplicationEnum, Set<Class<?>>> trainingAppScenarioAdaptationsMapCandidate = DomainKnowledgeUtil.getTrainingAppScenarioAdaptationsMap();
            
            // need to convert Class<?> to String
            Map<TrainingApplicationEnum, Set<String>> trainingAppScenarioAdaptationsMap = new HashMap<>();
            for(TrainingApplicationEnum taType : trainingAppScenarioAdaptationsMapCandidate.keySet()) {
                Set<String> clazzes = new HashSet<>();
                for(Class<?> clazz : trainingAppScenarioAdaptationsMapCandidate.get(taType)) {
                    clazzes.add(clazz.getCanonicalName());
                }
                trainingAppScenarioAdaptationsMap.put(taType, clazzes);
            }
        	result = new FetchTrainingAppScenarioAdaptationsResult(trainingAppScenarioAdaptationsMap);
            
        } catch (Exception e) {  
            logger.error("Caught exception while getting the scenario adaptations the training applications support.", e);
            result = new FetchTrainingAppScenarioAdaptationsResult();
            result.setSuccess(false);
            result.setErrorMsg("Failed to get the scenario adaptations the training applications support.");
            result.setErrorDetails(e.toString());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }
        MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.FetchTrainingAppScenarioAdaptations", start);
        return result;
    }


    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchTrainingAppScenarioAdaptations> getActionType() {
        return FetchTrainingAppScenarioAdaptations.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public void rollback(FetchTrainingAppScenarioAdaptations arg0,
            FetchTrainingAppScenarioAdaptationsResult arg1, ExecutionContext arg2)
                    throws ActionException {
    }
}
