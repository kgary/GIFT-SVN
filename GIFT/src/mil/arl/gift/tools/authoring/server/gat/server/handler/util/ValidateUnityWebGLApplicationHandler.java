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
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.ValidateUnityWebGLApplication;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Action handler that validates Unity WebGL application files and updates the files if necessary.
 * 
 * @author nroberts
 */
public class ValidateUnityWebGLApplicationHandler 
	implements ActionHandler<ValidateUnityWebGLApplication, GatServiceResult> {
	
	/** Strings to detect if the Unity WebGL application file contains the necessary javascript. */
	private static final String[] VALID_UNITY_WEBGL_KEYS = {
			"function sendAppMessage(msg)", 
			"sendTutorMessage(msg)"
	};
	
	/** Script that allows GIFT to utilize javascript methods in the Unity WebGL application. */
	private static final String UNITY_GIFT_SCRIPT =
			  "    <script type=\"text/javascript\">\n"
			+ "        //Function which sends messages to the unity engine\n"
			+ "        function sendAppMessage(msg) {\n"
			+ "            gameInstance.SendMessage('GiftConnection', 'OnExternalMessageReceived', msg);\n"
			+ "        }\n"
			+ "	       \n"	
		    + "        //Function which sends messages to the parent window.\n"
		    + "        //Called from within the UnityEngine\n"
		    + "        function sendTutorMessage(msg) {\n"
		    + "            parent.postMessage(msg, '*');\n"
		    + "        }\n"
		    + "        \n"
		    + "        //Registers listeners for messages from the parent window\n"
		    + "        window.addEventListener('message', function(event) {\n"
		    + "            sendAppMessage(event.data);\n"
		    + "        }, false);\n"
			+ "    </script>\n";
	
	
	@Override
	public GatServiceResult execute(ValidateUnityWebGLApplication action, ExecutionContext context) 
			throws DispatchException {
		
		long start = System.currentTimeMillis();
		GatServiceResult result = new GatServiceResult();
				
		try {
			
			AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
			FileTreeModel fileModel = fileServices.getRootTree(action.getUserName()).getModelFromRelativePath(action.getFilePath());
			String content = fileServices.readFileToString(action.getUserName(), fileModel);
			
			//check if the unity application's HTML file contains the functions needed to communicate with GIFT and add them if necessary
			if(!content.contains(VALID_UNITY_WEBGL_KEYS[0]) || !content.contains(VALID_UNITY_WEBGL_KEYS[1])) {
				fileServices.updateFileContents(action.getUserName(), fileModel, content.replace("</head>", UNITY_GIFT_SCRIPT + "</head>"));
			}
			
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
		
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.ValidateUnityWebGLApplication", start);
		return result;
	}

	@Override
	public Class<ValidateUnityWebGLApplication> getActionType() {
		return ValidateUnityWebGLApplication.class;
	}
	
	@Override
	public void rollback(ValidateUnityWebGLApplication action, GatServiceResult result, ExecutionContext context)
			throws DispatchException {
		// nothing to roll back		
	}

}
