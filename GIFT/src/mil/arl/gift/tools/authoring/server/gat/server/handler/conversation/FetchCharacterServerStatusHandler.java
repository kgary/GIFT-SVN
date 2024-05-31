/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import mil.arl.gift.common.gwt.server.GiftServletUtils;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.FetchCharacterServerStatus;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handler for checking whether or not Virtual Human or the Media Semantics character server is online
 * 
 * @author bzahid
 */ 
public class FetchCharacterServerStatusHandler implements ActionHandler<FetchCharacterServerStatus, GatServiceResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchCharacterServerStatusHandler.class);
    
    @Override
    public Class<FetchCharacterServerStatus> getActionType() {
        return FetchCharacterServerStatus.class;
    }

    @Override
    public synchronized GatServiceResult execute(FetchCharacterServerStatus action, ExecutionContext context) {
        long start = System.currentTimeMillis();
        logger.info("execute()");
       
        GatServiceResult result = new GatServiceResult();
        
        try {
            result.setSuccess(isVirtualHumanCharacterServerAvailable() || GiftServletUtils.isMediaSemanticsCharacterServerAvailable());
            
        } catch (Exception e) {
            logger.error("Caught exception while connecting to Virtual Human or the Media Semantics Character Server: ", e);
            result.setSuccess(false); 
        }
        
    	MetricsSenderSingleton.getInstance().endTrackingRpc("conversation.FetchCharacterServerStatus", start);
    	
		return result;
    }

    /**
     * Checks whether the virtual human character server is running and reachable.
     * This assumes the GIFT/config/tutor/context/tutor.xml is configured correctly with virtual human
     * character server information.
     * 
     * @return true if the virtual human character server is running and reachable.
     */
    private boolean isVirtualHumanCharacterServerAvailable() {
        
        String result = GiftServletUtils.checkVHCharacterServer("localhost:8088");
        return StringUtils.isNotBlank(result) && Boolean.valueOf(result);
    }

    @Override
    public synchronized void rollback(FetchCharacterServerStatus action, GatServiceResult result, ExecutionContext context ) 
            throws ActionException {
    	// nothing to do
    }
}
