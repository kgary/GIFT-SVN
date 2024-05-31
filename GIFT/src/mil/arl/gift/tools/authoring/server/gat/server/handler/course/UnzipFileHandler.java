/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import mil.arl.gift.tools.authoring.server.gat.server.FileOperationsManager;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.UnzipFile;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler that unzips an archive file (ZIP) file to extract its contents into a course folder
 * 
 * @author nroberts
 */
public class UnzipFileHandler implements ActionHandler<UnzipFile, GatServiceResult> {

	@Override
	public GatServiceResult execute(UnzipFile action, ExecutionContext context) throws DispatchException {
		
		return FileOperationsManager.getInstance().unzipFile(action);
	}

	@Override
	public Class<UnzipFile> getActionType() {
		return UnzipFile.class;
	}

	@Override
	public void rollback(UnzipFile action, GatServiceResult result, ExecutionContext context) throws DispatchException {
		// nothing to do
	}

}
