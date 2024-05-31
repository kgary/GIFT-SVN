/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.experiment.ExperimentUtil.DataSetType;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchLtiProperties;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.FetchLtiPropertiesResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.experiment.DataCollectionServicesInterface;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

/**
 * Fetches the LTI properties for a course based on a specific user who may or may not be authorized to view the
 * properties for the course.
 * 
 * @author nblomberg
 *
 */
public class FetchLtiPropertiesHandler implements ActionHandler<FetchLtiProperties, FetchLtiPropertiesResult> {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(FetchLtiPropertiesHandler.class);
    
    private static final String METRICS_TAG = "course.FetchLtiProperties";
    
	@Override
	public FetchLtiPropertiesResult execute(FetchLtiProperties action,
			ExecutionContext arg1) throws DispatchException {
	    
	    long start = System.currentTimeMillis();
		
	    FetchLtiPropertiesResult result = new FetchLtiPropertiesResult();
	    
	    String relativePath = action.getCourseFilePath();
	    String userName = action.getUserName();
	    
	    
	    FileTreeModel fileModel;
        try{
            FileTreeModel directoryModel = ServicesManager.getInstance().getFileServices().getFileTree(userName, AbstractSchemaHandler.getFileType(relativePath));
            
            fileModel = directoryModel.getModelFromRelativePath(relativePath);
           
            if (fileModel != null) {
                // LTI properties for Public courses are now allowed, so the user can view the properties and embed a
                // public course into a Tool Consumer.  However, Public courses should NOT be allowed to have a data set created
                // for them, so the user cannot do data collection on public courses at this time.   But in the GAT editor,
                // it is allowed to embed any accessible course into an LTI tool consumer.
                CourseRecord record = ServicesManager.getInstance().getDbServices().getCourseByPath(relativePath);
                if (record != null) {
                    
                    // Successful and the user has permissions to the course, so the lti panel should show the lti properties
                    // for the course.
                    result.setSuccess(true);
                    result.setCourseId(record.getCourseId());
                    DataCollectionServicesInterface expServices = ServicesManager.getInstance().getDataCollectionServices();
                    // IGNORE - course data type because all courses will have that data set and currently that shouldn't influence placing barriers for changing the course
                    result.setHasDataSets(expServices.doesCourseHaveDataCollectionDataSets(userName, relativePath, DataSetType.COURSE_DATA));
                    
                } else {

                    result.setSuccess(false);
                    result.setCourseId(null);
                }
            } else {
                result.setSuccess(false);
                result.setErrorMsg("An error occurred fetching the lti properties.");
                result.setErrorDetails("An error occurred while trying to get lti properties for '" + relativePath + "' on the server.");
            }
            
            
        } catch(Exception e){
            
            logger.error("FetchLtiPropertiesHandler - An error occurred while getting the file tree for '" + relativePath + "'.", e);
            
            
            result.setSuccess(false);
            
            if(e instanceof DetailedException){
                result.setErrorMsg(((DetailedException) e).getReason());
                result.setErrorDetails(((DetailedException) e).getDetails());
                result.setErrorStackTrace(((DetailedException) e).getErrorStackTrace());
                
            } else {
                result.setErrorMsg("An error occurred while trying to locate '" + relativePath + "' on the server.");
                result.setErrorDetails(e.toString());
                result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            }
            
            return result;
        }
        
	    logger.debug("FetchLtiPropertiesHandler result = " + result);
	    MetricsSenderSingleton.getInstance().endTrackingRpc(METRICS_TAG, start);
	    
		return result;
	}

	@Override
	public Class<FetchLtiProperties> getActionType() {
		return FetchLtiProperties.class;
	}

	@Override
	public void rollback(FetchLtiProperties arg0,
	        FetchLtiPropertiesResult arg1, ExecutionContext arg2)
			throws DispatchException {}

}
