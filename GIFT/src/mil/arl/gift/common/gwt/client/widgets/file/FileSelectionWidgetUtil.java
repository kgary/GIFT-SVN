/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.widgets.file;

import java.util.logging.Logger;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

import mil.arl.gift.common.gwt.shared.DocumentUtil;
import mil.arl.gift.common.gwt.client.widgets.file.FileSelectionView.FileSelectionCallback;
import mil.arl.gift.common.io.Constants;

/**
 * A utility for handling file selection widget logic common across the GIFT gwt or bootstrap file selection widgets.
 * 
 * @author mhoffman
 *
 */
public class FileSelectionWidgetUtil {
    
    /** The logger. */
    private static Logger logger = Logger.getLogger(FileSelectionWidgetUtil.class.getName());

    /**
     * Handle a response to an upload to the server.
     * 
     * @param eventResult contains the server's response including the name of the uploaded file.  Can be null.
     * @param fileUploadCallback used to provide success/failure information about the response.  If null than
     * this method doesn't do anything.
     */
    public static void handleFileUploadResponse(String eventResult, FileSelectionCallback fileUploadCallback){
        
        try{           
            logger.info("the result of the file upload :\n"+eventResult);
            
            if(fileUploadCallback == null){
                logger.info("not doing anything with the result of uploading the file to the server because a callback was not provided.");
                return;
            }
            
            boolean xmlEncoded = false;
            JSONObject jsonObject = null;
            
            if(eventResult != null){
                
                // check if the result is xml encoded                
                if(eventResult.startsWith("<pre")) {
                    
                    eventResult = eventResult.substring(eventResult.indexOf('>') + 1, eventResult.lastIndexOf("</pre"));
                    xmlEncoded = true;
                    
                } else {
                	// the result will be an html error page. 
                	
                	int index = eventResult.lastIndexOf("Powered by Jetty");
                	if(index != -1) {
                		// trim off the excess line breaks	
                		index += eventResult.substring(index).indexOf("<br");
                		eventResult = eventResult.substring(0, index);
                	}
                	
                	eventResult = "<span class=\"detailsHtml\">" + eventResult + "</span>";
                	
                	fileUploadCallback.onFailure(eventResult);
                	return;
                }
                
                JSONValue jsonValue = JSONParser.parseStrict(eventResult);
                jsonObject = jsonValue.isObject();
            }
            
            if (jsonObject != null) {
                
                JSONValue resultValue = jsonObject.get(Constants.FILE_SERVLET_RESULT_KEY);
                JSONString result = resultValue.isString();

                if (result != null && result.stringValue().equals(Constants.FILE_SERVLET_RESULT_SUCCESS)) {
                        
                    //
                    // file name uploaded (required)
                    //
                    JSONValue fileValue = jsonObject.get(Constants.FILE_SERVLET_FILE_KEY);
                    JSONString file = fileValue.isString();
                    String fileStr = file.stringValue();
                    
                    //
                    // Path to file (optional)
                    //
                    JSONValue servletPathValue = jsonObject.get(Constants.FILE_SERVLET_SERVLET_PATH);
                    String servletPathStr = null;                            
                    if(servletPathValue != null){
                        JSONString servletPath = servletPathValue.isString();  
                        servletPathStr = servletPath.stringValue();
                    }
                    
                    // decode the file name -
                    // the file name could have been xml encoded but the actual file on disk
                    // is not encoded therefore to access the file we need the true file name
                    // 
                    if(xmlEncoded){
                        fileStr = DocumentUtil.unescapeHTML(fileStr);
                        servletPathStr = DocumentUtil.unescapeHTML(servletPathStr);
                    }
                    
                    logger.info("on client file upload to be called: "+fileStr+", "+servletPathStr);
                    fileUploadCallback.onClientFileUploaded(fileStr, servletPathStr);                    
                    
                } else if (result != null && result.stringValue().equals(Constants.FILE_SERVLET_RESULT_FAILURE)) {
                    //there was a server error with the upload

                    JSONValue errorValue = jsonObject.get(Constants.FILE_SERVLET_ERROR);
                    JSONString error = errorValue.isString();
                    
                    if (error != null) {

                        JSONValue messageValue = jsonObject.get(Constants.FILE_SERVLET_MESSAGE);
                        JSONString message = messageValue.isString();
                        
                        if(error.stringValue().equals(Constants.FILE_SERVLET_FILE_ALREADY_EXISTS)) {
                            fileUploadCallback.onFailure("The file already exists on the server, rename the file and reupload.");
                            
                        } else {

                            if (message != null) {
                                fileUploadCallback.onFailure("An error of type '" + error + "' occurred on the server. " + message.stringValue());
                                
                            } else {
                                fileUploadCallback.onFailure("An error of type '" + error + "' occurred on the server.");
                            }
                        }

                    } else {                           
                        fileUploadCallback.onFailure("There was a failure on the server but the error is null.");
                        
                    }

                } else if (result != null) {                        
                    fileUploadCallback.onFailure("Unknown result: '" + result.stringValue() + "'");
                    
                } else {                       
                    fileUploadCallback.onFailure("The result returned from the server is null");
                }
                
            } else {
                fileUploadCallback.onFailure("The response returned from the server is null");
            }  
            
        } catch(Throwable e){
            
            if(fileUploadCallback != null){
                fileUploadCallback.onFailure("An exception was thrown when handling the response to the file upload that reads:\n"+e.toString());
            }
        }
    }
}
