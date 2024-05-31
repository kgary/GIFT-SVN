/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.nuxeo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.nuxeo.ecm.automation.client.model.Document;
import org.nuxeo.ecm.automation.client.model.Documents;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.net.nuxeo.NuxeoInterface.GroupEntityType;
import mil.arl.gift.net.nuxeo.NuxeoInterface.UserEntityType;
import mil.arl.gift.net.nuxeo.NuxeoInterface.UserEntityType.Properties;

/**
 * Tests NuxeoInterface methods
 * 
 * 
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NuxeoInterfaceTest {
    private static final String adminUser = "Administrator";
    private static final String testUser1 = "test1"; // used for general tests
    private static final String testUser2 = "test2"; // used for create and delete
    private static final String testUser3 = "test3"; // non-existent user

    private static final String NUXEO_URL = CommonProperties.getInstance().getCMSURL();
    private static final String NUXEO_SECRET_KEY = CommonProperties.getInstance().getCMSSecretKey();
    
    private static org.apache.log4j.Logger logger;
    
    private static NuxeoInterface nuxeoInterface;
    @BeforeClass
    public static void setup() {

        if (NUXEO_URL == null) {
            Assert.fail(
                    "The nuxeo URL was not set.  Therefore Nuxeo API tests can't be run.  If you don't want to test Nuxeo API calls you can ignore this junit failure.  Otherwise please set the CMS_URL property value in GIFT/config/common.properties.");
        }

        if(NUXEO_SECRET_KEY == null || NUXEO_SECRET_KEY.equalsIgnoreCase("yourSecretKeyGoesHere")){
            Assert.fail("The nuxeo secret key was not set.  Therefore Nuxeo API tests can't be run.  If you don't want to test Nuxeo API calls you can ignore this junit failure.  Otherwise please set the CMS_SECRET_KEY property value in GIFT/config/common.properties.");
        }
        
        nuxeoInterface = new NuxeoInterface(NUXEO_URL, NUXEO_SECRET_KEY);
        
        if(NUXEO_SECRET_KEY == null){
            Assert.fail("The nuxeo secret key was not set.  Therefore Nuxeo API tests can't be run.  If you don't want to test Nuxeo API calls you can ignore this junit failure.  Otherwise please set the CMS_SECRET_KEY property value in GIFT/config/common.properties.");
        }
        
        ConsoleAppender console = new ConsoleAppender(); //create appender
        //configure the appender
        String PATTERN = "%d{ABSOLUTE} %5p %c{1}:%L - %m%n";
        console.setLayout(new PatternLayout(PATTERN)); 
        console.setThreshold(org.apache.log4j.Level.INFO);
        console.activateOptions();
        //add appender to any Logger (here is root)
        logger = org.apache.log4j.Logger.getRootLogger();
        logger.addAppender(console);
        
        boolean userExists = false;
        try{
            userExists = nuxeoInterface.userExists(testUser1);
        }catch(IOException e){
            logger.fatal("Error checking if test user exists", e);
        }
        
        if(!userExists){
            //create test user which will be deleted in a later test in this class
            Properties props = new Properties("First", "Last", testUser1, "test2@example.com", "GIFT", "password");
            UserEntityType user = new UserEntityType(testUser1, props);
            try {
                nuxeoInterface.createNewUser(user);
            } catch (IOException ex) {
                logger.fatal("Error creating test user", ex);
            }
        }
    }
    
    @AfterClass
    public static void cleanup() {
        nuxeoInterface.deleteUser(testUser1);
    }
    
    //this requires a test folder structure to work with.  Implement junit in the future, not important right now, very low priority.
    @Ignore("this requires a test folder structure to work with.  Implement junit in the future, not important right now, very low priority.")
    @Test
    public void testRenameDocument(){
        
        String workspacePath = "testUser1/new";
        String currentName = "new.NEW.course.xml";
        String newName = "new.course.xml";

        try {
            nuxeoInterface.renameDocument(workspacePath, currentName, newName, "mhoffman");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    //this method is useful to systematically change everyones default disk quota to another value
    //and should not be part of normal junit operations.
    @Ignore("this method is useful to systematically change everyones default disk quota to another value and should not be part of normal junit operations.")
    @Test
    public void testIncreaseDiskQuota() throws IOException{
        
        long quotaBytes = 1073741824; //1 GB;
        String EVERYONE_GROUP = "members";
        GroupEntityType group = nuxeoInterface.getGroup(EVERYONE_GROUP, adminUser);
        for(String username : group.getMemberUsers()){    
//            System.out.println(username);
            
            try{
                nuxeoInterface.setWorkspaceQuota(username, quotaBytes, username);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    
    @Test
    public void testGetUser() {
        try{
            UserEntityType user = nuxeoInterface.getUser(testUser1);
            Assert.assertNotNull("Failed to get user "+testUser1+".", user);
            Assert.assertEquals("The user has the wrong id", testUser1, user.getId());
        }catch(Exception e){
            logger.error("Exception while retrieving "+testUser1+" user object.", e);
            Assert.fail("Failed to retrieve "+testUser1+" because of an exception.");
        }
        
        try{
            System.out.println("WARNING!!! testing for non-existant user next... expect an error and possible a stack trace to appear.");
            UserEntityType user = nuxeoInterface.getUser(testUser3);
            Assert.assertNull("The user "+testUser3+" should not exist.", user);
        }catch(@SuppressWarnings("unused") Exception e){
            //expected, there is no testUser3
        }
        System.out.println("END OF NON-EXISTANT USER TEST... the test for non-existant user is now over, continue to analyze output of junit.");
        
    }
    
    @Test
    public void testGetGroup(){
        
       final String EVERYONE_GROUP = "members";
       GroupEntityType group = nuxeoInterface.getGroup(EVERYONE_GROUP, adminUser);
       Assert.assertNotNull("The members group is null", group);
    }
    
    @Test
    public void testUserExists() {
        boolean userExists = true;
        try {
            userExists = nuxeoInterface.userExists(testUser1);
            Assert.assertTrue("The user "+testUser1+" doesn't exist", userExists);
            userExists = nuxeoInterface.userExists(testUser3);
        } catch (IOException ex) {
            logger.error("Error calling userExists", ex);
        }
        Assert.assertFalse("The user "+testUser1+" still exists", userExists);
    }
    
    @Test
    public void testCreateDeleteUser() {
        Properties props = new Properties("First", "Last", testUser2, "test1@example.com", "GIFT", "password");
        UserEntityType user = new UserEntityType(testUser2, props);
        boolean error;
        try {
            error = !nuxeoInterface.createNewUser(user);
        } catch (IOException ex) {
            logger.error("Error creating new user", ex);
            error = true;
        }
        Assert.assertFalse("There was an error creating the user "+user+".", error);
        if (!error) {
            user = nuxeoInterface.getUser(testUser2);
            Assert.assertNotNull("The user "+testUser2+" was not found.", user);
            Assert.assertEquals("The user has the wrong id", testUser2, user.getId());
        }
        
        boolean deleted = nuxeoInterface.deleteUser(testUser2);
        Assert.assertTrue("The user could not be deleted", deleted);
    }
    
    @Ignore("This is already tested by testCreateUser")
    @Test
    public void testCreateWorkspace() {
        Properties props = new Properties("First", "Last", testUser1, "testUser1@example.com", "GIFT", "password");
        UserEntityType user = new UserEntityType(testUser1, props);
        boolean error = false;
        try {
            nuxeoInterface.createUserWorkspace(user);
        } catch (IOException ex) {
            error = true;
            logger.error("Error creating user workspace", ex);
        }
        Assert.assertFalse("There was an error creating the workspace", error);
    }
    
    @Test
    public void testGetUserRootFolder() {
        Document rootFolderDocument = null;
        try {
            rootFolderDocument = nuxeoInterface.getUserRootFolder(testUser1);
        } catch (IOException ex) {
            logger.error("Error getting user root folder", ex);
        }
        Assert.assertNotNull("The root folder document is null", rootFolderDocument);
    }
    
    @Test
    public void testGetUserPrivateFolder() {
        Document privateFolderDocument = null;
        try {
            privateFolderDocument = nuxeoInterface.getUserPrivateFolder(testUser1);
        } catch (IOException ex) {
            logger.error("Error getting userver private folder", ex);
        }
        Assert.assertNotNull("The private folder document is null", privateFolderDocument);
    }
    
    @Test
    public void testGetDocumentsByExtension() {
        
        try{
            Documents documents = nuxeoInterface.getDocumentsByPath("/default-domain/workspaces/"+testUser1, null, AbstractSchemaHandler.COURSE_FILE_EXTENSION, testUser1);
            if(documents != null){
                Iterator<Document> docsItr = documents.iterator();
                while(docsItr.hasNext()){
                    System.out.println(docsItr.next().getTitle());
                }
            }
        }catch (IOException e){
            logger.error("Error getting documents by path", e);
            Assert.fail("Failed to get documents by path because on an exception");
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testDocumentLockUnlock() {
        String filenameToUpload = "../Domain/README.txt";
        File fileToUpload = new File(filenameToUpload);
        String newFolderPath = testUser1;
//        System.out.println("Creating new document at " + newFolderPath +" with content of "+fileToUpload);
        try {
            org.nuxeo.ecm.automation.client.model.Document createdDocument = 
                    nuxeoInterface.createDocument(newFolderPath, fileToUpload.getName(), newFolderPath, new FileInputStream(fileToUpload), testUser1);
            boolean isLocked = nuxeoInterface.lockDocument(testUser1, createdDocument);
            Assert.assertTrue("The document is not locked after requesting to lock it", isLocked);
            Document retrieved = nuxeoInterface.getDocumentByName(newFolderPath, fileToUpload.getName(), testUser1);
            Assert.assertTrue("The document is not locked, retrieved by document name", retrieved.isLocked());
            boolean isUnlocked = nuxeoInterface.unlockDocument(testUser1, retrieved);
            Assert.assertTrue("The document is not unlocked after requesting to unlock it", isUnlocked);
            retrieved = nuxeoInterface.getDocumentByName(newFolderPath, fileToUpload.getName(), testUser1);
            Assert.assertFalse("The document is not unlocked, retrieved by document name", retrieved.isLocked());
            nuxeoInterface.deleteDocument(createdDocument, testUser1);
        } catch (IOException ex) {
            logger.error("Error testing document lock/unlock", ex);
        }
    }
    
    @Test
    public void testSetUserWorkspacePermissions() {
        try {
            nuxeoInterface.setUserWorkspacePermissions(null, testUser1);
        } catch (IOException ex) {
            logger.error("Error testing setUserWorkspacePermissions", ex);
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testCreateDeleteDocument() {
        String filePathToUpload = "../Domain/README.txt";
        File fileToUpload = new File(filePathToUpload);
        String newFolderPath = testUser1;
        String shortFilename = fileToUpload.getName();
        
//        System.out.println("Creating new document at " + newFolderPath +" with content of "+fileToUpload);
        org.nuxeo.ecm.automation.client.model.Document createdDocument = null;

        // create initial document
        try (FileInputStream fis = new FileInputStream(fileToUpload)) {
            createdDocument = nuxeoInterface.createDocument(newFolderPath, shortFilename, newFolderPath, fis, testUser1);
        } catch (FileNotFoundException ex) {
            logger.error("Local file not found: " + shortFilename, ex);
            return;
        } catch (IOException e) {
            logger.error("Error creating file", e);
        }
                
        Assert.assertNotNull("Failed to create document", createdDocument);
            
        // Test over-write same named document
        boolean updatedDoc = true;
        try (FileInputStream fis = new FileInputStream(fileToUpload)) {
            createdDocument = nuxeoInterface.createDocument(newFolderPath, shortFilename, newFolderPath, fis, testUser1);
        } catch (FileNotFoundException ex) {
            logger.error("File not found: " + shortFilename, ex);
            updatedDoc = false;
        } catch (@SuppressWarnings("unused") DocumentExistsException e) {
            logger.error("Failed to over-write document with the same name");
            updatedDoc = false;
        } catch (IOException e) {
            logger.error("Error trying to over-write existing document", e);
            updatedDoc = false;
        }
        Assert.assertTrue("Failed to update the document", updatedDoc);

        // clean up
        if (createdDocument != null) {
            try {
                nuxeoInterface.deleteDocument(createdDocument, testUser1);        
            } catch (IOException ex) {
                logger.error("Error deleting document: " + createdDocument.getPath(), ex);
            }
        }
    }
    
    @Test
    public void testFolderSize() {
        boolean error = false;
        try {
            nuxeoInterface.getFolderSizeByPath("Public", testUser1);
        } catch (IOException ex) {
            error = true;
            logger.error("Error getting folder size", ex);
        }
        Assert.assertFalse("Failed to get the folder size", error);
    }
    
    @Test
    public void testSetQuota() {
        boolean error = false;
        try {
            //checking the return value
            Assert.assertTrue("Failed to set the user's workspace quota", nuxeoInterface.setWorkspaceQuota(testUser1, 100000, testUser1));
        } catch (IOException ex) {
            error = true;
            logger.error("Error setting quota", ex);
        }
        //checking if an exception happened (meaning we werent able to check the return value)
        Assert.assertFalse("There was an error setting the user's workspace quota", error);
        
        // Test permissions, testUser1 should not be able to change permissions on Public workspace folder
        boolean success = false;
        try {
            nuxeoInterface.setWorkspaceQuota("Public", -1, testUser1);
        } catch (@SuppressWarnings("unused") IOException ex) {
            //excepted behavior
            success = true;
            logger.info("Test setQuota - successfully prevented user from changing quota when they lacked permissions to do so.");
        }
        //should be an error cause the test user doesn't have permissions
        Assert.assertTrue("Failed to set the public workspace quota", success);
    }
    
    @Test
    public void testGetRemainingQuota() {
        try {
            nuxeoInterface.getRemainingWorkspaceQuota(testUser1, testUser1);
        } catch (IOException ex) {
            logger.error("Error getting remaining quota", ex);
            Assert.fail("Failed to get remaining workspace quota due to exception");
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test
    public void testQuotaLimit() {
        String filePathToUpload = "../Domain/README.txt";
        File fileToUpload = new File(filePathToUpload);
        String fileDestination = testUser1+"/temp";
        String shortFilename = "temp.txt";
        // Create temp workspace
        try {
            nuxeoInterface.createWorkspaceFolder(testUser1, "temp", testUser1, false);
        } catch (IOException ex) {
            logger.error("Error creating temp workspace", ex);
            return;
        }
        
        // set a low quota limit
        int quotaMax = 10;
        try {
            Assert.assertTrue("Failed to set a low quota limit", nuxeoInterface.setWorkspaceQuota(fileDestination, quotaMax, testUser1));
        } catch (IOException ex) {
            logger.error("Error setting quota", ex);
            Assert.fail("Failed to set workspace quota because of an exception");
        }

        boolean quotaExceeded = false;
        // document create should fail
        try (FileInputStream fis = new FileInputStream(fileToUpload)) {
            Document createdDocument = nuxeoInterface.createDocument(fileDestination, shortFilename, fileDestination, fis, testUser1);
            nuxeoInterface.deleteDocument(createdDocument, testUser1);
        } catch (FileNotFoundException ex) {
            logger.error("Local file not found: " + shortFilename, ex);
        } catch (@SuppressWarnings("unused") QuotaExceededException e) {
            //this is what should happen
            quotaExceeded = true;
        } catch (IOException e) {
            logger.error("Error creating file", e);
        } 
        Assert.assertTrue("Failed to detect the disk quota was exceeded", quotaExceeded);
        
        // cleanup
        try {
            nuxeoInterface.setWorkspaceQuota("test1/temp", -1, testUser1);
            nuxeoInterface.deleteWorkspaceFolder("test1/temp", null, testUser1);
        } catch (IOException ex) {
            logger.warn("Error cleaning up workspace", ex);
        }
        
    }
    
    @Test
    public void testGetDocs() {
        try {
            nuxeoInterface.getDocumentsByPath("/default-domain/workspaces/Public/RapidMiner Demo", null, null, testUser1);
            nuxeoInterface.getDocumentsByPath("/default-domain/workspaces/Public/RapidMiner Demo", null, AbstractSchemaHandler.COURSE_FILE_EXTENSION, testUser1);
            
            // all files and folder should be returned
            Documents documents = nuxeoInterface.getDocumentsAndFoldersByPath("/default-domain/workspaces/Public/RapidMiner Demo", null, testUser1);
            Assert.assertTrue("Incorrect number of RapidMiner Demo course folder documents retrieved", documents != null && documents.size() == 11);
//            for (Document doc : documents.list()) {
//                System.out.println(doc.getTitle());
//            }
//            System.out.println("--------");
            // only course file and folder should be returned
            documents = nuxeoInterface.getDocumentsAndFoldersByPath("/default-domain/workspaces/Public/RapidMiner Demo", AbstractSchemaHandler.COURSE_FILE_EXTENSION, testUser1);
            Assert.assertTrue("Incorrect number of RapidMinder Demo course folder course.xml documents retrieved", documents != null && documents.size() == 2);
//            for (Document doc : documents.list()) {
//                System.out.println(doc.getTitle());
//            }
            
            // some random file does not exist
            Assert.assertFalse("Failed to detect that a file doesn't exist", nuxeoInterface.documentExists("", "doesnotexist", false, testUser1));
            
            // Public workspace directory exists
            Assert.assertTrue("Failed to detect that a directory doesn't exist", nuxeoInterface.documentExists("", "Public", true, testUser1));
            
            // test file exists
            Assert.assertTrue("Failed to detect that a file exist", nuxeoInterface.documentExists("/default-domain/workspaces/Public/RapidMiner Demo/", "RapidMinerDemo.course.xml", false, testUser1));
        } catch (IOException ex) {
            logger.error("Error testing get documents", ex);
        }
    }
    
    /**
     * Test deleting a folder and all its contents
     */
    @Test
    public void testCreateDeleteFolder() {
        String filePathToUpload = "../Domain/README.txt";
        File fileToUpload = new File(filePathToUpload);
        String fileDestination = testUser1+"/temp";
        String shortFilename = "temp.txt";
        // Create workspace folder
        try {
            nuxeoInterface.createWorkspaceFolder("test1", "temp", testUser1, false);
            boolean duplicateFailed = false;
            try {
                // this one should fail
                nuxeoInterface.createWorkspaceFolder("test1", "temp", testUser1, false);
            } catch (@SuppressWarnings("unused") DocumentExistsException e) {
//                logger.info("Successfully prevented creating folder with duplicate name");
                duplicateFailed = true;
            }
            Assert.assertTrue("Failed to properly detect a duplicate workspace folder", duplicateFailed);
        } catch (IOException ex) {
            logger.error("Error creating temp workspace", ex);
            return;
        }
        
        // upload temp file
        try (FileInputStream fis = new FileInputStream(fileToUpload)) {
            nuxeoInterface.createDocument(fileDestination, shortFilename, fileDestination, fis, testUser1);
        } catch (FileNotFoundException ex) {
            logger.error("Local file not found: " + shortFilename, ex);
        } catch (@SuppressWarnings("unused") QuotaExceededException e) {
            System.err.println("Quota exceeded");
        } catch (IOException e) {
            logger.error("Error creating file", e);
        } 
        
        // delete folder and all contents
        try {
            nuxeoInterface.deleteWorkspaceFolder("test1/temp", null, testUser1);
            Assert.assertFalse("Failed to delete a workspace folder", nuxeoInterface.documentExists("", "test1/temp/"+shortFilename, false, testUser1));
        } catch (IOException ex) {
            logger.warn("Error cleaning up workspace", ex);
        }
    }
    
    @Test
    public void testExport() {
        Document document;
        try {
            document = nuxeoInterface.getDocumentByName("Public/Hello World", testUser1);
        } catch (IOException ex) {
            logger.error("Error geting document for export", ex);
            return;
        }
        try {
            File exportFile = new File("testExport.zip");
            nuxeoInterface.exportFolder(document.getId(), exportFile, testUser1, null);
            Assert.assertTrue("Failed to export a file", exportFile.exists());
        } catch (IOException ex) {
            logger.error("Failed to export folder", ex);
        }
    }
}