/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchAvatarKeyNames;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.FetchAvatarKeyNamesResult;
import mil.arl.gift.tools.services.ServicesManager;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ActionHandler for actions of type FetchAvatarKeyNames.
 */ 
public class FetchAvatarKeyNamesHandler implements ActionHandler<FetchAvatarKeyNames, FetchAvatarKeyNamesResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(FetchAvatarKeyNamesHandler.class);
    
    /** The Constant HTML. */
    private static final String HTML = ".html";
    
    /** The Constant JS. */
    private static final String JS = ".js";
    
    /** The Constant FILES. */
    private static final String FILES   = "_Files";
    
    /** The Constant JS_STARTJSON. */
    private static final String JS_STARTJSON = "{";
    
    /** The Constant JS_ENDJSON. */
    private static final String JS_ENDJSON = "}";
    
    /** The Constant NAME. */
    private static final String NAME    = "name";
    
    /** The Constant CHARACTER. */
    private static final String CHARACTER = "character";
    
    /** The Constant TYPE. */
    private static final String TYPE    = "type";
    
    /** The Constant ONFRAME. */
    private static final String ONFRAME = "onframe";
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<FetchAvatarKeyNames> getActionType() {
        return FetchAvatarKeyNames.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized FetchAvatarKeyNamesResult execute( FetchAvatarKeyNames action, ExecutionContext context ) 
            throws ActionException {
        long start = System.currentTimeMillis();
        logger.info("execute()");
        
        FetchAvatarKeyNamesResult result = new FetchAvatarKeyNamesResult();
        
        try {
                        
            Map<String, List<String>> avatarToKeyNames = getAvatarKeyNames(action.getUserName(), action.getAvatars());
            result.setAvatarToKeyNames(avatarToKeyNames);
            result.setSuccess(true);
        
        } catch (DetailedException e) {
        	
        	result.setSuccess(false);
            result.setErrorMsg(e.getReason());
            result.setErrorDetails(e.getDetails());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            
        } catch (Exception e) {
        
            result.setSuccess(false);
            result.setErrorMsg("An error occurred while populating the avatar speech keys.");
            result.setErrorDetails(e.getMessage());
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
        }

        MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.FetchAvatarKeyNames", start); 
        return result;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback( FetchAvatarKeyNames action, FetchAvatarKeyNamesResult result, ExecutionContext context ) 
            throws ActionException {

    }
    

    
    /**
	 * Gets the key names for all the avatar files specified.
	 *
	 * @param userName Name of the user
	 * @param avatars a list of the paths to each avatar file
	 * @return a map from each avatar file path to its respective key name
	 * @throws Exception the exception
	 */
	static private Map<String, List<String>> getAvatarKeyNames(String userName, List<String> avatars) throws Exception{
		
		Map<String, List<String>> avatarToKeyNames = new HashMap<String, List<String>>();
		
		try{
			
			FileTreeModel directoryModel = ServicesManager.getInstance().getFileServices().getFileTree(userName, HTML, JS);
			
			for(String avatar : avatars){
				
				String fileFolderPath = avatar.replace(HTML, FILES);
				
				String jsFile = avatar.substring(avatar.replace("\\", "/").lastIndexOf("/") + 1).replace(HTML, JS);
				
				FileTreeModel fileModel = directoryModel.getModelFromRelativePath(fileFolderPath + "/" + jsFile);
				
				List<String> keyNames = new ArrayList<String>();
				
                //get contents of the avatar's accompanying .js file into string
				try{
                    String jsStr = ServicesManager.getInstance().getFileServices().readFileToString(userName, fileModel);
                    
                    //strip unwanted text
                    int jsonStartIndex = jsStr.indexOf(JS_STARTJSON);
                    int jsonEndIndex = jsStr.lastIndexOf(JS_ENDJSON);
                    JSONObject jsonObject = new JSONObject(jsStr.substring(jsonStartIndex, jsonEndIndex+1));
    
                    getKeyNames(jsonObject, keyNames);
                    
                    Collections.sort(keyNames);
                    
                    avatarToKeyNames.put(avatar, keyNames);
				}catch(Exception e){
				    logger.warn("Caught exception while trying to get the avatar key names from '"+fileModel.getRelativePathFromRoot(false)+"', therefore it will not be included.", e);
				    throw new DetailedException("The avatar speech keys could not be populated. Please make sure a JavaScript "
				    		+ "file exists at <br/>'" +fileModel.getRelativePathFromRoot(true) + "'<br/> and that the file "
				    		+ "contains predefined methods to make the avatar speak.", e.getMessage(), e.getCause());
				}
			}
                
        } catch(Exception e){
        	logger.warn("Caught exception while looking for avatar key names", e);
        	throw e;
        }
		
		return avatarToKeyNames;
	}

	/**
 	 * Recursively walk the JSON object to find the key name values that references authored characters.
 	 *
 	 * @param object - the json object to parse
 	 * @param names - current list of key names from the json object
 	 */
    static private void getKeyNames(JSONObject object, List<String> names){
        
        if(object == null){
            return;
        }

        @SuppressWarnings("unchecked")
        Iterator<String> iterator = object.keys();
        String type = null, name = null, character = null;
        while(iterator.hasNext()){

            String key = iterator.next();
            
            Object value;
            
            try {
                value = object.get(key);
            } catch (JSONException e) {
                logger.error("Caught exception while retrieving value for key of "+key+" from JSON object.  Skipping this key:value pair.", e);
                continue;
            }
            
            if(key.equals(TYPE)){
                //found a type
                type = (String) value;
                
            }else if(key.equals(NAME)){
                // found name
                name = (String) value;
                
            }else if(key.equals(CHARACTER)){
                // found character
                character = (String) value;
                
            }else if(!key.equals(ONFRAME)){
                //ignore onframe json object which contains a lot of un-needed info here
                
                if(value instanceof JSONObject){
                    
                    try{
                        JSONObject jsonObj = (JSONObject) value;
                        if(jsonObj.keys() != null){
                            getKeyNames(jsonObj, names); 
                        }
                    }catch(@SuppressWarnings("unused") Exception e){}
                }
            }
            
            if(type != null && name != null && character != null){
                //found key name pairing, add name value to list
                names.add(name);
                
                //don't add name again when there are still more objects to iterator over
                name = null;
                type = null;
                character = null;
            }

        }
    }
}
