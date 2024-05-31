/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.util;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileExistsException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.net.nuxeo.QuotaExceededException;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.MoveDomainFileToWorkspaceLocation;
import mil.arl.gift.tools.authoring.server.gat.shared.action.util.MoveDomainFileToWorkspaceLocationResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;


/**
 * An action handler that moves a file from the Domain folder into a location in the workspace folder
 */ 
public class MoveDomainFileToWorkspaceLocationHandler implements ActionHandler<MoveDomainFileToWorkspaceLocation, MoveDomainFileToWorkspaceLocationResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(MoveDomainFileToWorkspaceLocationHandler.class);
    
    @Override
	public Class<MoveDomainFileToWorkspaceLocation> getActionType() {
        return MoveDomainFileToWorkspaceLocation.class;
    }
    
    @Override
    public synchronized MoveDomainFileToWorkspaceLocationResult execute( MoveDomainFileToWorkspaceLocation action, ExecutionContext context ) 
            throws ActionException {
        long start = System.currentTimeMillis();
        
        if(logger.isInfoEnabled()){
            logger.info("starting move domain file to workspace operation on "+action);
        }
		
        MoveDomainFileToWorkspaceLocationResult result = new MoveDomainFileToWorkspaceLocationResult();     
        
		try{
			
			String username = action.getUsername();
			String domainFilePath = action.getDomainFilePath();		        
		    String workspaceLocationPath = action.getWorkspaceLocation();
		     
		    //get file tree model for the domain file
		    String domainFile = domainFilePath;

		    //move the domain file to the target workspace location
		    String movedFilePath = null;
		    
		    try{
		        NameCollisionResolutionBehavior collisionResolution = action.getOverwriteExisiting() ? NameCollisionResolutionBehavior.OVERWRITE : NameCollisionResolutionBehavior.FAIL_ON_COLLISION;
		    	movedFilePath = ServicesManager.getInstance().getFileServices().copyDomainFile(username, domainFile, workspaceLocationPath, collisionResolution, null);
		    
		    } catch(@SuppressWarnings("unused") FileExistsException e){
		    	result.addMoveFailure(domainFilePath, workspaceLocationPath);
		    }
		    
		    //delete the original file
		    File originalFile = new File(CommonProperties.getInstance().getDomainDirectory() + File.separator + domainFilePath);
		    
		    if(!originalFile.getParent().equals(CommonProperties.getInstance().getDomainDirectory())) {
		    	// delete the unique folder that was generated for this file
		    	FileUtils.deleteDirectory(originalFile.getParentFile());
		    } else {
		    	originalFile.delete();
		    }		    
		    
		    if(movedFilePath != null){
		    	
			    FileTreeModel movedLocationModel = FileTreeModel.createFromRawPath(movedFilePath);
			    FileTreeModel temp = new FileTreeModel(movedLocationModel.getFileOrDirectoryName()); //this is a hack that'll break if this logic gets more complex.
			    result.setMovedLocationModel(temp);
			    result.setSuccess(true);
			    
		    } else {		    			    	
		    	throw new Exception("Could not move " + domainFilePath + " to " + workspaceLocationPath + " because a file with that"
		    			+ "name already exists at that location.");
		    }   
		    
		}catch(QuotaExceededException qe){
		    
            logger.error("Caught exception while trying to copy a file.", qe);
            result.setErrorStackTrace(qe.getErrorStackTrace());
            result.setSuccess(false);
            result.setErrorMsg("You are running low on disk space. Please delete some files from your workspace before attempting to upload this file again.");
    		result.setErrorDetails(qe.getDetails());
        }catch(Exception e){
        	
            logger.error("Caught exception while trying to copy a file.", e);
            result.setSuccess(false);
            result.setErrorMsg("could not copy a file.");
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            result.setErrorDetails(e.getMessage());
        }        
		MetricsSenderSingleton.getInstance().endTrackingRpc("util.MoveDomainFileToWorkspaceLocation", start);
        return result;
    }
    
    @Override
    public synchronized void rollback( MoveDomainFileToWorkspaceLocation action, MoveDomainFileToWorkspaceLocationResult result, ExecutionContext context ) 
            throws ActionException {

    }
}
