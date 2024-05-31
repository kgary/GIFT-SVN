/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import mil.arl.gift.tools.authoring.server.gat.server.FileOperationsManager;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShow;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.CreateSlideShowResult;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler that convers a PowerPoint file to a series of images
 * 
 * @author bzahid
 */
public class CreateSlideShowHandler implements ActionHandler<CreateSlideShow, CreateSlideShowResult> {

	@Override
	public CreateSlideShowResult execute(CreateSlideShow action, ExecutionContext context) throws DispatchException {
		
		return FileOperationsManager.getInstance().createSlideShow(action.getUsername(), action.getBrowserSessionKey(), action);
	}

	@Override
	public Class<CreateSlideShow> getActionType() {
		return CreateSlideShow.class;
	}

	@Override
	public void rollback(CreateSlideShow action, CreateSlideShowResult result, ExecutionContext context) throws DispatchException {
		// nothing to do
	}

}
