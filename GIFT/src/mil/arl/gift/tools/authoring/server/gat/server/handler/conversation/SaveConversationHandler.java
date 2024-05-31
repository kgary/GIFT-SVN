/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.conversation;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.conversation.Conversation;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.conversation.SaveConversation;
import mil.arl.gift.tools.authoring.server.gat.shared.model.conversation.ConversationHelper;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;


/**
 * Handler for saving a conversation tree file
 */ 
public class SaveConversationHandler implements ActionHandler<SaveConversation, SaveJaxbObjectResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(SaveConversationHandler.class);
    
    @Override
    public Class<SaveConversation> getActionType() {
        return SaveConversation.class;
    }

    @Override
    public synchronized SaveJaxbObjectResult execute(SaveConversation action, ExecutionContext context) {
        long start = System.currentTimeMillis();
        
        
        if(logger.isInfoEnabled()){
            logger.info("Attempting to save conversation jaxb object : "+action);
        }
        
        //If the acquire flag is set to true then we're in one of two cases:
	    //1.) We're writing a brand new file that doesn't exist yet.
	    //2.) We're overwriting a file via the Save-As functionality.
	    //Case 1 is no problem but in case 2 we can only proceed if nobody
	    //else has a lock on the file we're overwriting. So lets check for
	    //the failure condition of case 2.
        
        String relativePath = action.getPath();
	    boolean acquireInsteadOfRenew = action.isAcquireLockInsteadOfRenew();
		String userName = action.getUserName();

	    try{
	        
		  if(acquireInsteadOfRenew && ServicesManager.getInstance().getFileServices().isLockedFile(userName, relativePath)) {
		    	SaveJaxbObjectResult result = new SaveJaxbObjectResult();
		    	result.setSuccess(false);
		    	result.setErrorMsg("Unable to write/marshall Conversation object to '" + relativePath + "' because that file already exists and it is presently locked.");
		    	return result;
		    }
	    } catch (Exception e){
	    	
	    	SaveJaxbObjectResult result = new SaveJaxbObjectResult();
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while attempting to check if '" + relativePath + "' is locked.");
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    	return result;
	    }
	    
	    //TODO Without a more sophisticated multi-threaded solution there is a SLIGHT
	    //possibility that somebody else will lock the file right now. That would
	    //of course throw a real wrench into things.
        
        //We have to update the version number every time we save the file. I
	    //would have done this on the client side but the "common" code that
	    //handles the version logic isn't accessible on the client side.
	    JSONObject jsonObj = null;
	    try {
			jsonObj = new JSONObject(action.getConversationJSONStr());
			jsonObj.put(ConversationHelper.TREE_KEY, new JSONObject(action.getConversationTreeJSONStr()));
		} catch (JSONException e1) {
			SaveJaxbObjectResult result = new SaveJaxbObjectResult();
	    	result.setSuccess(false);
	    	result.setErrorDetails(e1.toString());
	    	result.setErrorMsg("An error occurred while converting the JSON Object");
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e1));
	    	return result;
		}
	    
	    Conversation conversationObj = ConversationUtil.fromJSON(jsonObj);	    
  		String currentVersion = conversationObj.getVersion();
  		String schemaVersion = CommonUtil.getSchemaVersion(AbstractSchemaHandler.CONVERSATION_TREE_SCHEMA_FILE);
  		String newVersion = CommonUtil.generateVersionAttribute(currentVersion, schemaVersion);
  		conversationObj.setVersion(newVersion); 
    	
  		boolean schemaValid;
		try {
			schemaValid = ServicesManager.getInstance().getFileServices().marshalToFile(userName, conversationObj, relativePath, null, action.isGIFTWrap());
		} catch (Exception e) {
			
			logger.error("Unable to write/marshall Conversation object to '" + relativePath + "'.", e);
			
			SaveJaxbObjectResult result = new SaveJaxbObjectResult();
			result.setSuccess(false);
    		result.setErrorMsg("Unable to write/marshall Conversation object to '" + relativePath + "'. Reason: " + e.toString());
    		result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
    		return result;
		}
    	
	    //Return success
	    SaveJaxbObjectResult result = new SaveJaxbObjectResult();
    	result.setNewVersion(newVersion);
    	result.setSchemaValid(schemaValid);
    	
    	MetricsSenderSingleton.getInstance().endTrackingRpc("conversation.SaveConversation", start);
		return result;
    }

    @Override
    public synchronized void rollback(SaveConversation action, SaveJaxbObjectResult result, ExecutionContext context ) 
            throws ActionException {
    }
}
