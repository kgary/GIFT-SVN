/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateMediaSemantics;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateMediaSemanticsResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler that validates media semantics files and updates the files if necessary.
 * 
 * @author bzahid
 */
public class ValidateMediaSemanticsHandler 
	implements ActionHandler<ValidateMediaSemantics, ValidateMediaSemanticsResult> {

	/** File structure example image */
	private static final String AVATAR_FILES_EXAMPLE_IMG = "images/help/mediaSemanticsFilesExample.png";
	
	/** File extension of Avatar min.js file */
	private static final String MIN_JS_FILE_EXTENSION = ".min.js";
	
	/** Strings to detect if the avatar file contains the necessary javascript. */
	private static final String[] VALID_AVATAR_KEYS = {"function receiveMessage(event)", "function onPresentingChange(id, p)"};
	
	/** Script that allows GIFT to utilize media semantics javascript methods. */
	private static final String MEDIA_SEMANTICS_SCRIPT =
			  "    <script type=\"text/javascript\">\n"
			+ "        window.addEventListener(\"message\", receiveMessage, false);\n"
			+ "        function receiveMessage(event) { \n"
			+ "            var payload = JSON.parse(event.data);\n"
			+ "            if (payload.method == \"msSpeak\") {\n"
			+ "                msSpeak(\"Movie1\",payload.key);\n"
			+ "            } else if (payload.method == \"msPlay\") {\n"
			+ "                msPlay(\"Movie1\",payload.key);\n"
			+ "            }\n"
			+ "        }\n"
			+ "        function onPresentingChange(id, p) { \n"
			+ "            if (p == false && id != \"Movie1\") {\n"
			+ "                try {\n"
			+ "                    parent.notifyGIFT();\n"
			+ "                } catch(error) {\n"
			+ "                    parent.postMessage(\"notify\", \"*\");\n"
			+ "                }\n"
			+ "            }\n"
			+ "        }\n"
			+ "    </script>\n";
	
	
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public ValidateMediaSemanticsResult execute(ValidateMediaSemantics action, ExecutionContext context) 
			throws DispatchException {
		
		long start = System.currentTimeMillis();
		ValidateMediaSemanticsResult result = new ValidateMediaSemanticsResult();
		result.setValidFile(true);
				
		try {
			
			AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
			FileTreeModel fileModel = fileServices.getRootTree(action.getUserName()).getModelFromRelativePath(action.getFilePath());
			String content = fileServices.readFileToString(action.getUserName(), fileModel);
			
			if(!content.contains(VALID_AVATAR_KEYS[0]) || !content.contains(VALID_AVATAR_KEYS[1])) {
				if(action.shouldUpdateInvalidFiles()) {
					fileServices.updateFileContents(action.getUserName(), fileModel, content.replace("<head>", "<head>\n" + MEDIA_SEMANTICS_SCRIPT));
				} else {
					result.setSuccess(true);
					result.setValidFile(false);
				}
			}
			
			// find the min.js file
			fileModel = fileServices.getRootTree(action.getUserName()).getModelFromRelativePath(action.getFilePath());
			for(FileTreeModel file : fileModel.getParentTreeModel().getSubFilesAndDirectories()) {
				if(file.getFileOrDirectoryName().endsWith(MIN_JS_FILE_EXTENSION) && !file.getFileOrDirectoryName().startsWith("jquery")) {
					fileModel = file;
					break;
				}
			}
			
			if(!fileModel.getFileOrDirectoryName().endsWith(MIN_JS_FILE_EXTENSION)) {
				throw new DetailedException(
						"A .min.js file was not found. This may prevent the avatar from functioning correctly in the course.",
						"Please make sure a .min.js file is located in the same directory as your avatar html file. " + 
						"Below is an example of the correct file structure: <br/><br/><img src=\"" + AVATAR_FILES_EXAMPLE_IMG+ 
						"\" style=\"padding-left: 50px; display:block;\"alt=\"Example of a Media Semantics project file structure.\">", null);
			}
			
			content = fileServices.readFileToString(action.getUserName(), fileModel);
			
		} catch(DetailedException e) {
			result.setSuccess(false);
			result.setErrorMsg(e.getReason());
			result.setErrorDetails(e.getDetails());
			result.setErrorStackTrace(e.getErrorStackTrace());
			
		} catch(Exception e) {
			result.setSuccess(false);
			result.setErrorMsg("There was a problem updating the file.");
			result.setErrorDetails(e.getMessage());
			result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
		}
		
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.ValidateMediaSemantics", start);
		return result;
	}

	
	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
	 */
	@Override
	public Class<ValidateMediaSemantics> getActionType() {
		return ValidateMediaSemantics.class;
	}

	/* (non-Javadoc)
	 * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
	 */
	@Override
	public void rollback(ValidateMediaSemantics action, ValidateMediaSemanticsResult result, ExecutionContext context)
			throws DispatchException {
		// nothing to roll back		
	}

}
