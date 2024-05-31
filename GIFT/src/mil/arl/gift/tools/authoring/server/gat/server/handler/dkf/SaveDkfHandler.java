/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.dkf;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.Scenario;
import generated.dkf.StartTriggers;
import generated.dkf.Task;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.dkf.SaveDkf;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.XTSPImporter;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;


/**
 * ActionHandler for actions of type SaveDkf.
 */ 
public class SaveDkfHandler implements ActionHandler<SaveDkf, SaveJaxbObjectResult> {

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(SaveDkfHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<SaveDkf> getActionType() {
        return SaveDkf.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized SaveJaxbObjectResult execute(SaveDkf action, ExecutionContext context) {
        long start = System.currentTimeMillis();
        
        if(logger.isInfoEnabled()){
            logger.info("Attempting to save DKF jaxb object : "+action);
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
		    	result.setErrorMsg("Unable to write/marshall DKF object to '" + relativePath + "' because that file already exists and it is presently locked.");
		    	return result;
		    }
		    		    
	    } catch (DetailedException e){
	    	
	    	SaveJaxbObjectResult result = new SaveJaxbObjectResult();
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while attempting to check if '" + relativePath + "' is locked. " + e.getReason());
	    	result.setErrorDetails(e.getDetails());
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    	return result;
	    	
	    }catch (Exception e){
	    	
	    	SaveJaxbObjectResult result = new SaveJaxbObjectResult();
	    	result.setSuccess(false);
	    	result.setErrorMsg("An error occurred while attempting to check if '" + relativePath + "' is locked.");
	    	result.setErrorDetails(e.toString());
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    	return result;
	    }
	    
	    //TODO Without a more sophisticated multi-threaded solution there is a SLIGHT
	    //possibility that somebody else will lock the file right now. That would
	    //of course throw a real wrench into things.
        
        //We have to update the version number every time we save the file. I
	    //would have done this on the client side but the "common" code that
	    //handles the version logic isn't accessible on the client side.
	    Scenario scenario = action.getScenario();
	    
	    //TODO This is a hack that was forced into existence by two related
	    //factors:
	    //
	    //1.) The DKF Editor will NEVER create a Scenario object where
	    //StartTriggers or EndTriggers are set to null. This is because the
	    //TriggerPresenter can only modify existing EndTriggers/StartTriggers
	    //that are provided to it, it cannot create new objects. So if the
	    //DkfPresenter loads a DKF with null for EndTriggers/StartTriggers it
	    //simply constructs a new empty object, sets the object in the DKF,
	    //and passes it into the TriggerPresenter.
	    //2.) The DKF schema allows for the Scenario's End Triggers and a
	    //Task's Start Triggers to be null. If they're not null then the DKF
	    //schema requires they contain at least one trigger.
	    //
	    //As a result the DkfPresenter can create DKFs that aren't schema
	    //valid and saving a schema invalid DKF generates an exception. So our
	    //solution is to manually check for this case and correct it.
	    //
	    //There are better solutions available. If the schema permits having
	    //0 triggers then it should accept an empty EndTriggers/StartTriggers
	    //object rather than requiring null.
	    if(scenario.getEndTriggers() != null && scenario.getEndTriggers().getTrigger().isEmpty()) {
	    	scenario.setEndTriggers(null);
	    }
	    List<Task> tasks = scenario.getAssessment().getTasks().getTask();
	    for(Task task : tasks) {
	    	StartTriggers startTriggers = task.getStartTriggers();
	    	if(startTriggers != null && startTriggers.getTrigger().isEmpty()) {
	    		task.setStartTriggers(null);
	    	}
	    }
	    
  		String currentVersion = scenario.getVersion();
  		String schemaVersion = CommonUtil.getSchemaVersion(AbstractSchemaHandler.DKF_SCHEMA_FILE);
  		String newVersion = CommonUtil.generateVersionAttribute(currentVersion, schemaVersion);
  		scenario.setVersion(newVersion);
    	
  		boolean schemaValid;
		try {
			schemaValid = ServicesManager.getInstance().getFileServices().marshalToFile(userName, scenario, relativePath, null, action.isGIFTWrap());
		} catch (DetailedException e) {
			
			logger.error("Unable to write/marshall DKF object to '" + relativePath + "'.", e);
			
			SaveJaxbObjectResult result = new SaveJaxbObjectResult();
			result.setSuccess(false);
    		result.setErrorMsg("Unable to write/marshall DKF object to '" + relativePath + "'. " + e.getReason());
	    	result.setErrorDetails(e.getDetails());
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
    		return result;
    		
		} catch (Exception e) {
			
			logger.error("Unable to write/marshall DKF object to '" + relativePath + "'.", e);
			
			SaveJaxbObjectResult result = new SaveJaxbObjectResult();
			result.setSuccess(false);
    		result.setErrorMsg("Unable to write/marshall DKF object to '" + relativePath + "'.");
	    	result.setErrorDetails(e.toString());
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
    		return result;
		}
		
		// ONLY IF the DKF has been saved, start the process of exporting to xTSP as well.
	    
		try {
			FileTreeModel directory = ServicesManager.getInstance().getFileServices().getFileTreeByPath(userName, relativePath).getParentTreeModel();
			AbstractFolderProxy folderProxy = ServicesManager.getInstance().getFileServices().getFolder(directory.getRelativePathFromRoot(), userName);
			try {
				List<FileProxy> jsonFiles = folderProxy.listFiles(null, "json");
				if (jsonFiles != null) {
					if (jsonFiles.size() >= 1) {
					    String fileName = action.getScenario().getResources().getSourcePath();
						if (fileName != null) {
							XTSPImporter importer = new XTSPImporter();
							FileProxy jsonFileProxy = folderProxy.getRelativeFile(fileName);
							importer.exportDkfIntoXtsp(action.getUserName(), action.getScenario(), relativePath,
									jsonFileProxy, folderProxy);
							List<DetailedException> errorMessages = importer.getErrorLogList();
							
							SaveJaxbObjectResult result = new SaveJaxbObjectResult();
							result.setErrorMsg("Error");
							result.setErrorDetails("Error Details");
							result.setErrorStackTrace(null);
							result.setExceptionsList(errorMessages);
						}
					};
				}
			} catch (Exception e) {
				logger.error("Unable to write/marshall xTSP file changes to '" + relativePath + "'.", e);
				
				SaveJaxbObjectResult result = new SaveJaxbObjectResult();
				result.setSuccess(false);
	    		result.setErrorMsg("Unable to write/marshall xTSP file changes to '" + relativePath + "'.");
		    	result.setErrorDetails(e.toString());
		    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
	    		return result;
			}
		} catch (IllegalArgumentException e) {
			logger.error("Unable to write/marshall xTSP file changes to '" + relativePath + "'.", e);
			
			SaveJaxbObjectResult result = new SaveJaxbObjectResult();
			result.setSuccess(false);
    		result.setErrorMsg("Unable to write/marshall xTSP file changes to '" + relativePath + "'.");
	    	result.setErrorDetails(e.toString());
	    	result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
    		return result;
		}
		
	    //Return success!
	    SaveJaxbObjectResult result = new SaveJaxbObjectResult();
    	result.setNewVersion(newVersion);
    	result.setSchemaValid(schemaValid);
    	
    	MetricsSenderSingleton.getInstance().endTrackingRpc("dkf.SaveDkf", start);
		return result;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public synchronized void rollback(SaveDkf action, SaveJaxbObjectResult result, ExecutionContext context ) 
            throws ActionException {
    }
}
