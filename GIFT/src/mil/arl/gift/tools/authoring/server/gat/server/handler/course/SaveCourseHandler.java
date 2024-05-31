/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.server.handler.course;

import generated.course.Course;
import mil.arl.gift.common.CourseRecord;
import mil.arl.gift.common.metrics.MetricsSenderSingleton;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.authoring.common.util.CommonUtil;
import mil.arl.gift.tools.authoring.server.gat.shared.action.SaveJaxbObjectResult;
import mil.arl.gift.tools.authoring.server.gat.shared.action.course.SaveCourse;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import net.customware.gwt.dispatch.server.ActionHandler;
import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.DispatchException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class SaveCourseHandler.
 */
public class SaveCourseHandler implements ActionHandler<SaveCourse, SaveJaxbObjectResult>{

    /**  instance of the logger. */
    private static Logger logger = LoggerFactory.getLogger(SaveCourseHandler.class);
    
    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#execute(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public SaveJaxbObjectResult execute(SaveCourse action, ExecutionContext ctx)
            throws DispatchException {
        
        long start = System.currentTimeMillis();
        
        if(logger.isInfoEnabled()){
            logger.info("Attempting to save course jaxb object : "+action);
        }

        //If the acquire flag is set to true then we're in one of two cases:
        //1.) We're writing a brand new file that doesn't exist yet.
        //2.) We're overwriting a file via the Save-As functionality.
        //Case 1 is no problem but in case 2 we can only proceed if nobody
        //else has a lock on the file we're overwriting. So lets check for
        //the failure condition of case 2.
        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        String relativePath = action.getPath();
        boolean acquireInsteadOfRenew = action.isAcquireLockInsteadOfRenew();
        Course course = action.getCourse();
        String userName = action.getUserName();
        
        //Note: includes the workspace directory as the parent
        try{
            if(acquireInsteadOfRenew && fileServices.isLockedFile(userName, relativePath)) {
                SaveJaxbObjectResult result = new SaveJaxbObjectResult();
                result.setSuccess(false);
                result.setErrorMsg("Unable to write/marshall Course object to '" + relativePath + "' because that file already exists and it is presently locked.");
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
        String currentVersion = course.getVersion();
        String schemaVersion = CommonUtil.getSchemaVersion(AbstractSchemaHandler.COURSE_SCHEMA_FILE);
        String newVersion = CommonUtil.generateVersionAttribute(currentVersion, schemaVersion);
        course.setVersion(newVersion);
        
        //trim the course name just in case the GAT allowed spaces
        if(course.getName() != null){
            course.setName(course.getName().trim());
        }

        boolean schemaValid;
          
        try {
            FileTreeModel courseFile = FileTreeModel.createFromRawPath(relativePath);
            
            //make sure the course folder exists, if not create a new one in the user's workspace folder
            if(!fileServices.fileExists(userName, courseFile.getParentTreeModel().getRelativePathFromRoot(), true)){
            	
                String userFolder = courseFile.getParentTreeModel().getParentTreeModel().getRelativePathFromRoot();
                String courseFolderName = courseFile.getParentTreeModel().getFileOrDirectoryName();
                fileServices.createFolder(userName, userFolder, courseFolderName, true);
            }

            schemaValid = fileServices.marshalToFile(userName, course, relativePath, null);
        } catch (Exception e) {
            
            logger.error("Unable to write/marshall Course object to '" + relativePath + "'.", e);
            
            SaveJaxbObjectResult result = new SaveJaxbObjectResult();
            result.setSuccess(false);
            if(e instanceof DetailedException){
                result.setErrorMsg("Unable to write/marshall Course object to '" + relativePath + "'.\n\n"+((DetailedException)e).getMessage());
                result.setErrorDetails(((DetailedException)e).getDetails());
            }else{
                result.setErrorMsg("Unable to write/marshall Course object to '" + relativePath + "'.\n\nReason: " + e.toString());
            }
            result.setErrorStackTrace(DetailedException.getFullStackTrace(e));
            return result;
        }
        

        // This is successful if the record is created or if the record already exists.
        // Need to create a course record db row when a course is created (and auto saved the first time). Also
        // need to handle when a course is edited that may have been manually copied into GIFT workspace folder bypassing UI logic.
        CourseRecord courseRecord = ServicesManager.getInstance().getDbServices().createCourseRecordIfNeeded(userName, relativePath, true);
        if (courseRecord == null) {
            SaveJaxbObjectResult result = new SaveJaxbObjectResult();
            result.setSuccess(false);
            result.setErrorMsg("There was a database error trying to create the course record for '" + relativePath + "'.");
            return result;
        }else{
            // make sure a 'course tile' published course exists for all courses that are created.  This way
            // the data for those that take this course can be analyzed using the publish course page.
            ServicesManager.getInstance().getDataCollectionServices().createDefaultDataCollectionItemIfNeeded(userName, courseRecord);
        }

        //Return success!
        SaveJaxbObjectResult result = new SaveJaxbObjectResult();
        result.setNewVersion(newVersion);
        result.setSchemaValid(schemaValid);
        
        MetricsSenderSingleton.getInstance().endTrackingRpc("course.SaveCourse", start);
        return result;
            
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#getActionType()
     */
    @Override
    public Class<SaveCourse> getActionType() {
        return SaveCourse.class;
    }

    /* (non-Javadoc)
     * @see net.customware.gwt.dispatch.server.ActionHandler#rollback(net.customware.gwt.dispatch.shared.Action, net.customware.gwt.dispatch.shared.Result, net.customware.gwt.dispatch.server.ExecutionContext)
     */
    @Override
    public void rollback(SaveCourse arg0, SaveJaxbObjectResult arg1,
            ExecutionContext arg2) throws DispatchException {        
    }
}
