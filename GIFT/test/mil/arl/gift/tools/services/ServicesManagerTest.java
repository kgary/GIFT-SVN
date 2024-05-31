/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.UUID;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.LMSCourseRecords;
import mil.arl.gift.common.LMSDataRequest;
import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.AbstractSchemaHandler.FileType;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.common.io.UnmarshalledFile;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.lms.impl.common.LmsInvalidCourseRecordException;
import mil.arl.gift.lms.impl.common.LmsInvalidStudentIdException;
import mil.arl.gift.lms.impl.common.LmsIoException;
import mil.arl.gift.tools.authoring.common.conversion.UnsupportedVersionException;
import mil.arl.gift.tools.services.db.DbServicesInterface;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.desktop.DesktopFileServices;
import mil.arl.gift.ums.db.UMSDatabaseException;
import mil.arl.gift.ums.db.survey.SurveyValidationException;

/**
 * This class tests the Services Manager class logic.  Depending on the deployment mode you may need
 * to setup other systems (e.g. Server mode will require the GIFT content management system [Nuxeo] to be 
 * running)
 * 
 * @author mhoffman
 *
 */
public class ServicesManagerTest {

    /** the username to use when in desktop deployment mode */
    private static final String DESKTOP_TEST_USER = "TEST_USER";
    
    /** the username to use to find and change workspace files in */
    private static final String USER_NAME = DESKTOP_TEST_USER;
    private static final String BROWSER_SESSION_KEY = UUID.randomUUID().toString();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        
        //this variable is usually set by GIFT/scripts/launchProcess.bat for the LMS argument.  That script
        //is used to launch the LMS outside of Junit testing.
        System.setProperty("derby.system.home", "data");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }
    
    @Test
    public void dbServicesTest(){
        
        ServicesManager mgr = ServicesManager.getInstance();
        DbServicesInterface dbServices = mgr.getDbServices();
        
        //
        // retrieve LMS data for user
        //
        
        //GIFT simple LMS db doesn't use username yet, just GIFT user id to identify user
        LMSDataRequest dataRequest = new LMSDataRequest(USER_NAME);        
        int userId = 1;
        
        try {
            LMSCourseRecords lmsCourseRecords = dbServices.getLMSData(userId, dataRequest);
            Assert.assertNotNull("The LMS data retrieval should NOT return null, even if there are no records.", lmsCourseRecords.getRecords());
            
            //not sure what else can be asserted here.  What if this is run and the user doesn't have records yet?

        } catch (LmsIoException | LmsInvalidStudentIdException
                | LmsInvalidCourseRecordException e) {
            System.out.println("Caught exception while trying to retrieve the LMS history for user id of "+userId+".");
            e.printStackTrace();
            Assert.fail("Failed to retrieve LMS history.");
        }
    }

    @Test
    public void fileServicesTest() {
        
        ServicesManager mgr = ServicesManager.getInstance();
        AbstractFileServices fileServices = mgr.getFileServices();
        
        if(!(fileServices instanceof DesktopFileServices) && USER_NAME.equals(DESKTOP_TEST_USER)){
            Assert.fail("This test is being executed with GIFT in the '"+DeploymentModeEnum.SERVER+"' deployment mode.  Please change the username from '"+DESKTOP_TEST_USER+"' to an actual username that has a workspace in Nuxeo");
        }
        
        //
        // getFileTree
        //    retrieve course and DKF files in the domain folder
        //
        System.out.println("Get all workspace folders available to user...");
        FileTreeModel tree = null;
        try{
            tree = fileServices.getFileTree(USER_NAME, AbstractSchemaHandler.COURSE_FILE_EXTENSION, AbstractSchemaHandler.DKF_FILE_EXTENSION);
            
            Assert.assertTrue("The file tree model is not a directory.", tree.isDirectory());
        } catch (DetailedException e) {
            System.out.println("Caught exception while trying to get file tree model.");
            e.printStackTrace();
            Assert.fail("Failed to get file tree model.");
        } catch (ClassCastException classCast){
 
            if("org.nuxeo.ecm.automation.client.model.FileBlob cannot be cast to org.nuxeo.ecm.automation.client.model.OperationRegistry".equals(classCast.getMessage())){
                System.out.println("Caught ClassCastException while trying to get the file tree model.  This normally happens when a non-existant username is used with Nuxeo.  Does '"+USER_NAME+"' exist in Nuxeo workspaces?");
            }
            classCast.printStackTrace();
            Assert.fail("ClassCastException while trying to get the file tree model, most likely due to a bad username of '"+USER_NAME+"'.");
        }
        
        //
        // get a users workspace folder
        //
        System.out.println("Get user's own workspace folder...");
        FileTreeModel directoryModel = null;
        try{
            directoryModel = fileServices.getUsersWorkspace(USER_NAME);
            Assert.assertNotNull("Unable to find the user's "+USER_NAME+" workspace.", directoryModel);
        } catch (DetailedException e) {
            System.out.println("Caught exception while trying to find the user's workspace.");
            e.printStackTrace();
            Assert.fail("Failed to get the user's workspace.");
        }         
        
        //find course file owned by the user
        FileTreeModel courseFile = getRandomFileByType(directoryModel, AbstractSchemaHandler.getFileExtension(FileType.COURSE));
        
        // NOTE: this is only here to prevent an Eclipse build warning because it isn't smart enough to understand the
        // above assertNotNull on 'directoryModel'
        if(directoryModel == null){
            return;
        }
        Assert.assertNotNull("Unable to find a course file (*.course.xml) under '"+directoryModel.getRelativePathFromRoot()+"'.  Do you have any under that location?  If not please manually place a course folder under that directory.", courseFile);
        
        //
        // unmarshal file
        //
        System.out.println("Read existing file...");
        Serializable serializableObject = null;
        try {
            UnmarshalledFile uFile = fileServices.unmarshalFile(USER_NAME, courseFile.getRelativePathFromRoot(true));
            serializableObject = uFile.getUnmarshalled();
            Assert.assertNotNull("Unable to unmashal the course file of '"+courseFile+"'.", serializableObject);
            
            Assert.assertNotNull("There were XML parsing validation issues for the course file of '"+courseFile+"'.  This course was randomly choosen by this test."+
            "  In order to limit the likelihood this test will fail again you could try fixing the following issues in the course before running this test again.\n"+uFile.getValidationEvents(), uFile.getValidationEvents());
            
        } catch (IllegalArgumentException
                | UnsupportedVersionException | IOException e) {
            System.out.println("Caught exception while trying to unmarshal the course file of '"+courseFile+"'.");
            e.printStackTrace();
            Assert.fail("Failed to unmarshal file.");
        }
        
        // 
        // marshal file
        //
        System.out.println("Create new file...");
        String marshalledFileName = "junit-MarshalledFile-"+Long.toString(System.nanoTime())+AbstractSchemaHandler.COURSE_FILE_EXTENSION;
        FileTreeModel marshalledTreeModel = new FileTreeModel(marshalledFileName);
        courseFile.getParentTreeModel().addSubFileOrDirectory(marshalledTreeModel);
        String fullMarshalledFileName = courseFile.getParentTreeModel().getModelFromRelativePath(marshalledFileName).getRelativePathFromRoot(true);
        try {
            fileServices.marshalToFile(USER_NAME, serializableObject, fullMarshalledFileName, null);
        } catch (IllegalArgumentException | IOException e) {
            System.out.println("Caught exception while trying to marshal the course file contents to "+marshalledTreeModel+".");
            e.printStackTrace();
            Assert.fail("Failed to marshal to file.");
        } 
        
        //
        // Lock file
        //
        System.out.println("Lock file...");
        try{
            fileServices.lockFile(USER_NAME, BROWSER_SESSION_KEY, fullMarshalledFileName, false);
            boolean locked = fileServices.isLockedFile(USER_NAME, fullMarshalledFileName);
            Assert.assertTrue("Failed to acquire lock for "+marshalledTreeModel, locked);
        } catch (IllegalArgumentException | DetailedException e) {
            System.out.println("Caught exception while trying to lock the file "+marshalledTreeModel+".");
            e.printStackTrace();
            Assert.fail("Failed to lock file.");
        }
        
        //
        // Is locked
        //
        System.out.println("checked if file is locked...");
        try {
            boolean isLocked = fileServices.isLockedFile(USER_NAME, fullMarshalledFileName);
            Assert.assertTrue("Failed to prove the lock for "+marshalledTreeModel, isLocked);
        } catch (IllegalArgumentException | DetailedException e) {
            System.out.println("Caught exception while trying to check if file is locked on "+marshalledTreeModel+".");
            e.printStackTrace();
            Assert.fail("Failed to check if file is locked.");
        }

        
        //
        // Unlock file
        //
        System.out.println("Unlock file");
        try{
            fileServices.unlockFile(USER_NAME, BROWSER_SESSION_KEY, fullMarshalledFileName);
        }catch(Exception e){
            System.out.println("Caught exception while trying to unlock file "+marshalledTreeModel+".");
            e.printStackTrace();
            Assert.fail("Failed to unlock file.");
        }
        
        //
        // Delete file
        //   that was just marshaled
        //
        System.out.println("Delete file...");
        try{
            boolean deleteSuccess = fileServices.deleteFile(USER_NAME, BROWSER_SESSION_KEY, fullMarshalledFileName, null);
            Assert.assertTrue("Failed to delete "+marshalledTreeModel, deleteSuccess);
        }catch(DetailedException e){
            System.out.println("Caught exception while trying to delete file "+marshalledTreeModel+".");
            e.printStackTrace();
            Assert.fail("Failed to delete file.");
        }
        
        
        //
        // Get all courses (no validation)
        //
        System.out.println("Get all courses...");
        CourseOptionsWrapper wrapperAllCourses = new CourseOptionsWrapper();
        try {
            fileServices.getCourses(USER_NAME, wrapperAllCourses, false, null);
            Assert.assertTrue("Failed to retrieve a single course.", !wrapperAllCourses.domainOptions.isEmpty());
        } catch (IllegalArgumentException  
                | IOException | DetailedException | ProhibitedUserException e) {
            System.out.println("Caught exception while calling getCourses.");
            e.printStackTrace();
            Assert.fail("Failed to successfully get all GIFT courses.");
        }
        
        //
        // Validate specific course
        //
        System.out.println("Validate specific course...");
        Iterator<DomainOption> courseItr = wrapperAllCourses.domainOptions.values().iterator();
        DomainOption firstCourse = courseItr.next();
        CourseOptionsWrapper wrapperSpecificCourses = new CourseOptionsWrapper();
        try {
            fileServices.getCourse(USER_NAME, firstCourse.getDomainId(), wrapperSpecificCourses, true, true, true, null);
            ServicesManager.getInstance().getDbServices().applyLMSRecommendations(USER_NAME, firstCourse);
            Assert.assertTrue("Failed to retrieve the specific course of "+firstCourse+".", !wrapperAllCourses.domainOptions.isEmpty());
        } catch (IllegalArgumentException  
                | SurveyValidationException | LmsIoException
                | LmsInvalidStudentIdException
                | LmsInvalidCourseRecordException | UMSDatabaseException | IOException | DetailedException | ProhibitedUserException e) {
            System.out.println("Caught exception while calling getCourse on "+firstCourse+".");
            e.printStackTrace();
            Assert.fail("Failed to successfully get specific GIFT course.");
        }
        
        System.out.println("DONE");
    }
    
    /**
     * Pseudo-randomly returns a file with the given extension by searching from the specified directory.
     * 
     * @param tree if a folder all descendant files are checked and if a file is found with the specified
     * extension it is returned.  If a file, it will be returned if it's extension matches.  Otherwise null is returned.
     * @param extension the GIFT file extension to search for
     * @return if a file is found it will be returned, otherwise null;
     */
    private FileTreeModel getRandomFileByType(FileTreeModel tree, String extension){
        
        if(!tree.isDirectory() && tree.getFileOrDirectoryName().endsWith(extension)){
            return tree;
        }
        
        //first search children files for an extension match
        for(FileTreeModel child : tree.getSubFilesAndDirectories()){
            
            if(!child.isDirectory() && child.getFileOrDirectoryName().endsWith(extension)){
                //found file with given extension
                return child;
            }
        }
        
        //second search subdirectories
        for(FileTreeModel child : tree.getSubFilesAndDirectories()){
            
            if(child.isDirectory()){
                //search the directory for child files
                FileTreeModel match = getRandomFileByType(child, extension);
                
                if(match != null){
                    return match;
                }
            }
        }
        
        //didn't find a file with the extension in this tree model
        return null;
    }

}
