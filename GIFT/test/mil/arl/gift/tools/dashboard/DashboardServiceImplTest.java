/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard;

import java.io.Console;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import mil.arl.gift.common.DomainOption;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.gwt.client.DetailedRpcResponse;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr;
import mil.arl.gift.common.gwt.server.authentication.UserAuthenticationMgr.UserAuthResult;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.LoadedProgressIndicator;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.ProhibitedUserException;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.domain.DomainCourseFileHandler.CourseOptionsWrapper;
import mil.arl.gift.tools.dashboard.server.DashboardHttpSessionData;
import mil.arl.gift.tools.dashboard.server.DashboardServiceImpl;
import mil.arl.gift.tools.dashboard.shared.rpcs.CopyCourseResult;
import mil.arl.gift.tools.dashboard.shared.rpcs.DeleteCourseResult;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.file.AbstractFileServices;



/**
 * This is used to test logic in the DashboardServiceImpl.java 
 * 
 * @author mhoffman
 *
 */

public class DashboardServiceImplTest {
    
    private static Scanner scanner;

    /**
     * Tests the public course to public workspace logic.  It will require you to provide your GIFT
     * username and password as well as select a course in your workspace to copy to the Public workspace.
     */
    @Ignore  //This should not be part of the standard junit as this is tool for publishing showcase courses
    public void publishCourseToPublicTest() {

        System.out.println("This tool will copy a course from your workspace to the Public workspace. "
                + "\nYou must have write access to the Public workspace in order for this to succeed.\n");
        
        System.out.println("Please enter your GIFT username");
        scanner = new Scanner(System.in);
        
        // Default to empty username if one can't be found.
        String username = null;
        if (scanner.hasNextLine()) {
            username = scanner.nextLine();
        }
        
        System.out.println("Please enter your password");
        String password = null;
        Console console = System.console();
         
        if(console != null) {
            password = new String(console.readPassword());
        } else if (scanner.hasNextLine()) {
            password = scanner.nextLine();
        }
        
        System.out.println("Please enter the GIFT user you would like to login as: (if you have permissions to do so) [leave empty to skip]");
        scanner = new Scanner(System.in);
        String loginAsUserName = null;
        if (scanner.hasNextLine()) {
            loginAsUserName = scanner.nextLine();
        }
        
        if(loginAsUserName != null && loginAsUserName.isEmpty()){
            loginAsUserName = null;
        }
        
        System.out.println("Please provide a list of users that will have write access: (comma delimited, no spaces) [leave empty for no one will ]");
        scanner = new Scanner(System.in);
        String editorsStr = null;
        
        if (scanner.hasNextLine()) {
            editorsStr = scanner.nextLine();
        }
        
        HashSet<String> editableToUsernames = null;
        if(editorsStr != null && !editorsStr.isEmpty()){
            String[] usernames = editorsStr.split(Constants.COMMA);
            if(usernames.length > 0){
                editableToUsernames = new HashSet<String>();
                editableToUsernames.addAll(Arrays.asList(usernames));
            }
        }
        
        System.out.println("Please provide a list of users that will have read access: (comma delimited, no spaces) [leave empty to use * ]");
        scanner = new Scanner(System.in);
        String readersStr = null;
        
        if (scanner.hasNextLine()) {
            readersStr = scanner.nextLine();
        }
        
        HashSet<String> visibleToUsernames = null;
        if(readersStr != null && !readersStr.isEmpty()){
            String[] usernames = readersStr.split(Constants.COMMA);
            if(usernames.length > 0){
                visibleToUsernames = new HashSet<String>();
                visibleToUsernames.addAll(Arrays.asList(usernames));
            }
        }
        
        try {
            if(login(username, password, loginAsUserName)) {
                
                if(loginAsUserName != null){
                    username = loginAsUserName;
                }
                
                System.out.println("\nRetrieving courses from "+username+"'s workspace, please wait...\n");
                
                // Show the user a list of courses in their workspace
                CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();
                displayCourses(username, courseWrapper);
                
                // Prompt the user to select a course
                DomainOption selectedCourse = selectCourse(courseWrapper);
                
                System.out.println("\nValidating selected course...");
                if(isCourseValid(selectedCourse, courseWrapper)) {
                    
                    System.out.println("Course is valid.");
                    System.out.println("\nPublishing course, please wait...\n");
                    
                    DashboardServiceImpl dashboard = new DashboardServiceImpl();
                    
                    DashboardHttpSessionData userSession = new DashboardHttpSessionData(username);
                    String browserSession = UUID.randomUUID().toString();
                    DetailedRpcResponse response = dashboard.publishCourseToPublic(userSession, browserSession, selectedCourse.getDomainName(), selectedCourse, editableToUsernames, visibleToUsernames);
                    if(!response.isSuccess()){
                        //ERROR
                        System.out.println("Failed to start the publish course logic."+
                                "\nReason: "+response.getResponse()+
                                "\nError Message: "+response.getErrorMessage());
                        Assert.fail("There was an error while starting the publish course logic");
                    }else{
                        //monitor progress
                        
                        LoadedProgressIndicator<CopyCourseResult> progress = null;
                        CopyCourseResult result = null;
                        do{
                            progress = dashboard.getCopyProgressByUserName(userSession.getUserName());
                            
                            if(progress.getException() == null){                                
                                
                                if (progress.getPayload() != null) {                                
                                    
                                    result = progress.getPayload();
                                    
                                }
                                
                            }else{
                                //ERROR during copy
                                System.out.println("There was an error during publishing."+
                                        "\nReason: "+response.getResponse()+
                                        "\nError Message: "+response.getErrorMessage()+
                                        "\nException: "+progress.getException());
                                Assert.fail("There was an error during the publish course logic");
                            }
                            
                        }while(result == null);
                        
                        if(result.courseAlreadyExists()){
                            System.out.println("There is already a course with the name '"+selectedCourse.getDomainName()+"'.");
                        }else if(!result.isSuccess()){
                            System.out.println("Failed to publish course\n"+result);
                        }else{
                            System.out.println("Course published successfully!");
                        }
                        
                    }
                    
                } else {
                    System.out.println("The course \"" + selectedCourse.getDomainName() + "\" is invalid. "
                            + "Please login to the GIFT Dashboard and check the course.");
                }
                
            } else {
                System.out.println("Failed to login. Incorrect username or password.");
            }
            
        } catch (DetailedException e) {
            System.out.println(e.getReason());
            System.out.println(e.getDetails());
            e.printStackTrace();
            
            Assert.fail("There was an error while performing the publish course logic" );
            
        } catch(Throwable e) {
            System.out.println("An unexpected exception was thrown.");
            e.printStackTrace();
            
            Assert.fail("There was an error while performing the publish course logic" );
        }

    }
    
    /**
     * Validates the user's credentials
     * 
     * @param username The username
     * @param password The user password
     * @param loginAsUserName used to assume the identify of another GIFT user for debugging purposes.  In most cases this is null.
     * @return true if the credentials are valid, false otherwise
     * @throws DetailedException if there was a problem logging in.
     */
    private static boolean login(String username, String password, String loginAsUserName) throws DetailedException {
        boolean loggedIn = true;
        
        try {
            UserAuthResult reason = UserAuthenticationMgr.getInstance().isValidUser(username, password, loginAsUserName, null);
            if(reason != null && reason.getAuthFailedReason() != null) {
                throw new DetailedException("An error occurred while validating credentials.", reason.getAuthFailedReason(), null);
            }
            
        } catch (Exception e) {
            throw new DetailedException("An error occurred while validating credentials.", e.getMessage(), e);
        }
        
        return loggedIn;
    }
    
    /**
     * Prints a list of course names in the user's workspace
     * 
     * @param username The username
     * @param courseWrapper The CourseOptionsWrapper used to store domain options
     * @throws DetailedException If there was an error retrieving the courses or if there were no available courses to display
     */
    private static void displayCourses(String username, CourseOptionsWrapper courseWrapper) throws DetailedException {
        
        try {
            ServicesManager.getInstance().getFileServices().getCourses(username, 
                    courseWrapper, 
                    false, 
                    null);
            
            if(courseWrapper.domainOptions.isEmpty()) {
                throw new DetailedException("No valid courses were found in the user's workspace", 
                        "Please add a course to your workspace and try again.", null);
            }
            
             Iterator<Map.Entry<String, DomainOption>> it = courseWrapper.domainOptions.entrySet().iterator();
             int i = 1;
             while (it.hasNext()) {
                 Map.Entry<String, DomainOption> entry = it.next();
                 DomainOption domainItem = entry.getValue();
                 
                 //
                 // Remove Public courses (for now) - logic is to only support moving from personal workspace to Public workspace
                 //
                 if(domainItem.getDomainId().startsWith(AbstractFileServices.PUBLIC_WORKSPACE_FOLDER_NAME) || 
                         domainItem.getDomainId().startsWith(AbstractFileServices.PUBLIC_WORKSPACE_FOLDER_NAME, 1)){
                     it.remove();
                     continue;
                 }
                 System.out.println(i + ". " + domainItem.getDomainName());
                 i++;
             }
             
             if(i == 1){
                 //no courses where found
                 Assert.fail("Failed to find any courses in your workspace.");
             }
        } catch(DetailedException e) {
            throw e;
            
        } catch (IllegalArgumentException | FileNotFoundException 
                | ProhibitedUserException e) {
            
            throw new DetailedException("An error occurred while retrieving the courses.", e.getMessage(), e);
        } 
    }
    
    /**
     * Prompts the user to select a course
     *  
     * @param courseWrapper The list of courses to select from
     * @return The selected course
     */
    private static DomainOption selectCourse(CourseOptionsWrapper courseWrapper) {
        
        DomainOption selectedCourse = null;
        int courseNum = -1;
        
        while(courseNum == -1) {
            
            boolean invalidInput = true;
            
            while(invalidInput) {
                try {
                    System.out.println("\nEnter the number of the course that should be moved to the Public workspace.");
                    courseNum = scanner.nextInt();
                    invalidInput = false;
                } catch (@SuppressWarnings("unused") Exception e) {
                    System.out.println("Invalid input detected. Please enter a positive integer.");
                }
                
                scanner.nextLine();
            }
            
            System.out.println("Is course #" + courseNum + " correct? (Y/N)");
            
            String response = scanner.nextLine();
            if(!response.equalsIgnoreCase("Y")) {
                courseNum = -1;
                continue;
            }
            
            if(courseNum < 0 || courseNum > courseWrapper.domainOptions.size()) {
                System.out.println("Invalid number entered.");
                courseNum = -1;
                
            } else {
                // Get the selected course
                
                int count = 1;
                Iterator<DomainOption> it = courseWrapper.domainOptions.values().iterator();
                while(it.hasNext()) {
                    if(count == courseNum) {
                        selectedCourse = it.next();
                        break;
                    } else {
                        count += 1;
                        it.next();
                    }
                }
            }   
        }
        
        return selectedCourse;
    }
    
    /**
     * Determines whether or not the course is valid
     * 
     * @param domainOption The course to validate
     * @param courseOptionsWrapper contains the course file handlers for all courses parsed
     * @return true if the course is valid, false otherwise
     */
    private static boolean isCourseValid(DomainOption domainOption, CourseOptionsWrapper courseOptionsWrapper) {
        try {
            DomainCourseFileHandler handler = courseOptionsWrapper.courseFileNameToHandler.get(domainOption.getDomainId());
            CourseValidationResults results = handler.checkCourse(true, null);
            if(results.hasCriticalIssue() || results.hasImportantIssues()){
                System.out.println(results);
                return false;
            }
            
            return true;
        } catch (Throwable t) {
            System.out.println(t);
            return false;
        }
    }
        
    
    /**
     * Copy all of the available courses into a temporary folder and delete the copies 
     * afterwards verifying that the copying and deleting functionality works as expected 
     * by getting the number of available courses and comparing it to the expected number.
     * 
     */
    @Test
    public void copyAndDeleteCourses() {

        //Uses a temporary folder to cleanly copy to and delete from 
        String folderName = "JUnitTestFolder";
        File tempFolder = new File("../Domain/workspace/"+folderName);
        tempFolder.mkdirs();
        
        DashboardServiceImpl dashboard = new DashboardServiceImpl();
        //DashboardHttpSessionData userSession = new DashboardHttpSessionData(folderName);
        LoadedProgressIndicator<CopyCourseResult> progress = new LoadedProgressIndicator<>();

        ArrayList<DomainOption> courses = getCourses();
        int initialSize = courses.size();
        
        try {
            System.out.println("Found "+courses.size()+" courses to copy.");
            
            //Copy courses
            for (DomainOption courseToCopy: courses) {

                System.out.println("Copying course: "+courseToCopy.getDomainId());
                
                String newCourseName = getUniqueCourseName(courseToCopy, courses);
                dashboard.copyCourse(courseToCopy, folderName, "", "DashboardServiceImplTest.BrowserSessionId",
                        newCourseName, null, null, false, courses, progress);
                Assert.assertEquals("The JUnit was not able to successfully copy the course: " + courseToCopy.getDomainName() + 
                        "  Reported error information is\n"+progress.getPayload().getErrorMessage()+"\n"+progress.getPayload().getErrorDetails(),
                        true, progress.getPayload().isSuccess());

            }
            courses = getCourses();
            Assert.assertEquals("Copying did not work as expected. The current number of courses (" + courses.size()
                + ") is not equivalent to twice that of the original number of courses (" + initialSize  + ").",
                initialSize*2, courses.size());
        } finally {
            
            //Delete courses from temporary folder
            
            ArrayList<DomainOption> coursesToDelete = new ArrayList<DomainOption>();
            courses = getCourses();
            System.out.println("There are "+courses.size()+" courses to be considered for deletion.");
            for (DomainOption courseOption : courses) {
                if (courseOption.getDomainId().contains(tempFolder.getName())) {
                    System.out.println("Adding course to delete: "+courseOption.getDomainId());
                    coursesToDelete.add(courseOption);
                } 
            }
            
            deleteCopies(coursesToDelete, folderName, dashboard);
            courses = getCourses();
            Assert.assertEquals("Deleting did not work as expected. The current number of courses (" + courses.size()
                    + ") is not equivalent to the original number of courses (" + initialSize + ").", 
                    initialSize, courses.size());
            tempFolder.delete();
        }
        
    }
    
    
    /**
     * Looks through the Domain/workspace folder to find all the available courses and adds them to an arraylist.
     * 
     * @return available course objects
     */
    private ArrayList<DomainOption> getCourses() {
        
        CourseOptionsWrapper courseWrapper = new CourseOptionsWrapper();
        try {
            ServicesManager.getInstance().getFileServices().getCourses(null,
                    courseWrapper,
                    null,
                    false,
                    null);
            
        } catch (IllegalArgumentException | FileNotFoundException | DetailedException | ProhibitedUserException e) {
            e.printStackTrace();
            Assert.fail("File Services was unable to get existing courses");
        }
        
        ArrayList<DomainOption> courses = new ArrayList<>();
        for (Map.Entry<String, DomainOption> courseOption : courseWrapper.domainOptions.entrySet()) {
            courses.add(courseOption.getValue());
        }
        
        return courses;
    }
    
    /**
     * Determines if a course has a naming conflict between other courses in the provided course list and provides a new name if necessary.
     * 
     * @param courseToRename The course to provide a new name for if any naming conflicts arise. Can't be null.
     * @param coursesToCheck List of courses that need to be checked for potential naming conflicts. Can't be null.
     * @return New course name that is free from naming conflicts with courses in the provided list.
     */
    private String getUniqueCourseName(DomainOption courseToRename, List<DomainOption> coursesToCheck) {
        
        String courseName = courseToRename.getDomainName();
        for (DomainOption domainoption : coursesToCheck) {
            //check if there is a domain id conflict
            if (domainoption.getDomainId().equals(courseToRename.getDomainId())) {
                //if the conflict is with itself, continue
                if (domainoption == courseToRename) {
                    continue;
                } else {
                    Assert.fail("There is an existing domain conflict at " + domainoption.getDomainId());
                }
            }
            if (courseToRename.getDomainName().equalsIgnoreCase(domainoption.getDomainName())) {

                return courseName + " " + UUID.randomUUID().toString();
            }
        }
        return courseName;
        
    }
    
    /**
     * Delete the provided courses from a user's workspace using the dashboard's delete function.
     * 
     * @param coursesList Courses that should be deleted. Can't be empty or null.
     * @param username Checks the access a user has to a course. Can't be empty or null.
     * @param dashboard Used to call dashboard's delete logic. Can't be null.
     */
    private void deleteCopies(List<DomainOption> coursesList, String username, DashboardServiceImpl dashboard) {

        DeleteCourseResult response = new DeleteCourseResult();
        ProgressIndicator deleteProgress = new ProgressIndicator();
        AbstractFileServices fileServices = ServicesManager.getInstance().getFileServices();
        dashboard.deleteCourses(coursesList, username, UUID.randomUUID().toString(), true, true, response, deleteProgress, fileServices);
        
        if (!response.isSuccess()) {
            if(response.getErrorStackTrace() != null){
                System.out.println(response.getErrorStackTrace().toString());
            }
            Assert.fail("Failed to delete course: " + response.getCourseWithIssue() != null ? response.getCourseWithIssue().getDomainName() : "UNKNOWN" + "\nReason: " + response.getResponse()+"\nDetails: "+response.getAdditionalInformation());
            
        }
        
        
    }

}


