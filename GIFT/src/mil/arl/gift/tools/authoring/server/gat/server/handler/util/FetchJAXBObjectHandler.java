/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.tools.authoring.server.gat.server.GatRpcServiceImpl;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObject;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.FetchJAXBObjectResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;


/**
 * ActionHandler for actions of type GetJAXBObject
 *
 */ 
public class FetchJAXBObjectHandler implements ActionHandler<FetchJAXBObject, FetchJAXBObjectResult> {
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchJAXBObject> getActionType() {
        return FetchJAXBObject.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized FetchJAXBObjectResult execute(FetchJAXBObject action, ExecutionContext context) {
        GatRpcServiceImpl rpcService = new GatRpcServiceImpl();
        return rpcService.getJAXBObject(action.getUserName(), action.getRelativePath(), action.isUseParentAsCourse());
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback( FetchJAXBObject action, FetchJAXBObjectResult result, ExecutionContext context ) 
            throws ActionException {

    }
}
