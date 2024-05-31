/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.MemoryFileServletRequest;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddress;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchContentAddressResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

public class FetchContentAddressHandler implements ActionHandler<FetchContentAddress, FetchContentAddressResult> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchContentAddressHandler.class);
    
    private static final String METRICS_TAG = "course.FetchContentAddress";
    
	@Override
	public FetchContentAddressResult execute(FetchContentAddress action,
			ExecutionContext arg1) throws DispatchException {
	    
	    long start = System.currentTimeMillis();
		AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
	    FetchContentAddressResult result = new FetchContentAddressResult();
	    String filePath = null;
	    String userName = action.getUserName();
	    String imageURL = "";
	    
	    try {

		    AbstractFolderProxy courseFolder = fileServices.getCourseFolder(action.getCourseFolderPath(), userName);
		    		    
		    if(courseFolder.fileExists(action.getContentFilePath())) {
		        filePath = action.getCourseFolderPath() + Constants.FORWARD_SLASH + action.getContentFilePath();		    	
		    } else {
		    	throw new FileNotFoundException("The file (" +action.getContentFilePath() + ") could not be found.");
		    }
		    
		    
	    } catch (Exception e) {
	    	logger.warn("Unable to retrieve the file: ", e);
	    	filePath = null;
	    	result.setErrorMsg("Failed to fetch the URL of the file named '"+action.getCourseFolderPath()+"'.");
	    	result.setErrorDetails("An exception was thrown while checking if the file exists.  The error reads:\n"+e.getMessage());
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    }
	    
	    if(logger.isDebugEnabled()){
	        logger.debug("FetchContentAddressHandler filePath = " + filePath);
	    }
	    
	    if (filePath != null && !filePath.isEmpty() && userName != null && !userName.isEmpty()) {
    	    if (CommonProperties.getInstance().getDeploymentMode().equals(DeploymentModeEnum.SERVER)){
        		
    	        imageURL = CommonProperties.getInstance().getDashboardURL();
                String dashboardPath = CommonProperties.getInstance().getDashboardPath();
                
                imageURL = imageURL + Constants.FORWARD_SLASH + dashboardPath + Constants.FORWARD_SLASH + 
                           CommonProperties.getInstance().getDashboardMemoryFileServletSubPath() + 
                           MemoryFileServletRequest.encode(new MemoryFileServletRequest(filePath, userName));
                       
    	    } else {
    	        
    	        // The url should look something like this: http://<ip>:<port>/workspace/nblomberg/TestCourse/testimage.jpg
    	        String workspaceRelativePath = CommonProperties.getInstance().getWorkspaceDirectoryName() + Constants.FORWARD_SLASH + filePath;
    	        
    	        imageURL = CommonProperties.getInstance().getDomainContentServerAddress() + Constants.FORWARD_SLASH + workspaceRelativePath;
    	    }
    	    
    	    result.setContentURL(imageURL);
            result.setSuccess(true);
             
	    } else {
	        result.setSuccess(false);
	    }
	    
	    
	    if(logger.isDebugEnabled()){
	        logger.debug("FetchContentAddressHandler imageURL = " + imageURL);
	    }
	    
	    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
	    
		return result;
	}

	@Override
	public Class<FetchContentAddress> getActionType() {
		return FetchContentAddress.class;
	}

	@Override
	public void rollback(FetchContentAddress arg0,
			FetchContentAddressResult arg1, ExecutionContext arg2)
			throws DispatchException {}

}
