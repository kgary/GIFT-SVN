/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.tools.authoring.server.gat.server.GatRpcServiceImpl;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFile;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateFileResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * An action handler that validates a GIFT XML file.
 * 
 * @author bzahid
 */
public class ValidateFileHandler implements ActionHandler<ValidateFile, ValidateFileResult> {

	@Override
	public ValidateFileResult execute(ValidateFile action, ExecutionContext context)
			throws DispatchException {
	   GatRpcServiceImpl rpcService = new GatRpcServiceImpl();
	   return rpcService.validateFile(action.getUserName(), action.getFilePath(), null);
	}

	@Override
	public Class<ValidateFile> getActionType() {
		return ValidateFile.class;
	}

	@Override
	public void rollback(ValidateFile action, ValidateFileResult result,
			ExecutionContext context) throws DispatchException {
		
	}

	
}
