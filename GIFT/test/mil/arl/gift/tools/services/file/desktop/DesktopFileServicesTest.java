/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.services.file.desktop;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import mil.arl.gift.common.enums.DeploymentModeEnum;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.FileExistsException;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.tools.services.ServicesManager;
import mil.arl.gift.tools.services.ServicesProperties;
import mil.arl.gift.tools.services.file.AbstractFileServices;
import mil.arl.gift.tools.services.file.AbstractFileServices.NameCollisionResolutionBehavior;

/**
 * This tests various methods in the desktop file services class.
 * 
 * @author mhoffman
 *
 */
public class DesktopFileServicesTest {
    
    static final String username = "junit";
    
    static final File WORKSPACE_DIRECTORY = new File(ServicesProperties.getInstance().getWorkspaceDirectory());
    static final File junitWorkspace = new File(WORKSPACE_DIRECTORY + File.separator + username);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
        
        //cleanup - delete junit workspace
        FileUtils.deleteDirectory(junitWorkspace);
    }

    @Test
    public void copyFileTest() {
        
        ServicesManager mgr = ServicesManager.getInstance();
        AbstractFileServices fileServices = mgr.getFileServices();
        if(!(fileServices instanceof DesktopFileServices)){
            Assert.fail("This test is being executed with GIFT in the wrong configuration.  Please change '"+CommonProperties.DEPLOYMENT_MODE+"' to '"+DeploymentModeEnum.DESKTOP.getName()+"' in GIFT/config/common.properties.");
        }
        
        FileTreeModel workspaceTree = null, junitWorkspaceTree = null;
        try{
            workspaceTree = fileServices.getRootTree(username);
            junitWorkspaceTree = fileServices.getUsersWorkspace(username);
        }catch(Exception e){
            System.out.println("Caught exception while trying to get the domain folder tree.");
            e.printStackTrace();
            Assert.fail("Failed to get the domain folder tree.");
        }
        
        //copy junit test data to junit username workspace (Domain/workspaces/junit)
        try {
            FileUtils.deleteDirectory(junitWorkspace);
            FileUtils.copyDirectoryToDirectory(new File("data" + File.separator + "tests" + File.separator + "fileCopyTests"), junitWorkspace);
        } catch (IOException e) {
            System.out.println("Caught exception while trying to copy the test data to the workspace directory.");
            e.printStackTrace();
            Assert.fail("Failed to copy test data while preparing for tests.");
        }
        
        Assert.assertNotNull("Failed to get the domain folder tree.", workspaceTree);
        Assert.assertNotNull("Failed to get the JUnit domain folder tree.", junitWorkspaceTree);
        
        //
        // Copy File - test 1
        // - file to copy and destination are in same directory, the course directory
        //
        try{
            String fileToCopyName = username + "\\fileCopyTests\\test1\\fileToCopy.txt";
            String folderName = username + "\\fileCopyTests\\test1\\";
            
            // test 1.1 - check file exist exception
            try{
                fileServices.copyWorkspaceFile(null, fileToCopyName, folderName, NameCollisionResolutionBehavior.FAIL_ON_COLLISION, null);
                Assert.fail("Failed to properly detect a file name collision when not over-writting an existing file with the same named file in the copy file test (test 1.1).");
            }catch(@SuppressWarnings("unused") FileExistsException e){
                //this should happen since we aren't over-writing
                //nothing to do...                
            }
            
            // test 1.2 - check file is the same exception
            try{
                fileServices.copyWorkspaceFile(null, fileToCopyName, folderName, NameCollisionResolutionBehavior.OVERWRITE, null);
                Assert.fail("Failed to properly detect the file was being copied to itself in the copy file test (test 1.2).");
            }catch(@SuppressWarnings("unused") DetailedException e){
                //this should happen
                //nothing to do...                
            }
            
            // test 1.3 - copy file to new file in same directory
            folderName = username + "\\fileCopyTests\\test1\\copy.txt";
            String filename = fileServices.copyWorkspaceFile(null, fileToCopyName, folderName, NameCollisionResolutionBehavior.OVERWRITE, null);
            
            File checkFile = new File(WORKSPACE_DIRECTORY + File.separator + filename);
            Assert.assertTrue("Failed to over-write an existing file (test 1)", checkFile.exists());

            System.out.println("Test 1: Copied '"+fileToCopyName+"' to '"+folderName+"' resulted in '"+filename+"'.");
            
            
        }catch(Exception e){
            System.out.println("Caught exception while trying to copy a file in the same directory as the course.");
            e.printStackTrace();
            Assert.fail("Failed to copy file (test 1).");
        }
        
        //
        // Copy File - test 2
        // - file to copy is in subdirectory of course directory
        //
        try{
            String fileToCopyName = username + "\\fileCopyTests\\test2\\subfolder\\fileToCopy.txt";
            String folderName = username + "\\fileCopyTests\\test2\\";
            String filename = fileServices.copyWorkspaceFile(null, fileToCopyName, folderName, NameCollisionResolutionBehavior.FAIL_ON_COLLISION, null);
            
            File checkFile = new File(WORKSPACE_DIRECTORY + File.separator + filename);
            Assert.assertTrue("Failed to over-write an existing file (test 2)", checkFile.exists());

            System.out.println("Test 2: Copied '"+fileToCopyName+"' to '"+folderName+"' resulted in '"+filename+"'.");
        }catch(Exception e){
            System.out.println("Caught exception while trying to copy a file in a subdirectory from the course.");
            e.printStackTrace();
            Assert.fail("Failed to copy file (test 2).");
        }
                
        //
        // Copy File - test 3
        // - file to copy is a grandchild of the course folder and a the destination is a great grandchild location
        //
        try{
            String fileToCopyName = username + "\\fileCopyTests\\test3\\fileToCopy.txt";
            String folderName = username  + "\\fileCopyTests\\test3\\a\\";
            String filename = fileServices.copyWorkspaceFile(null, fileToCopyName, folderName, NameCollisionResolutionBehavior.FAIL_ON_COLLISION, null);
            
            File checkFile = new File(WORKSPACE_DIRECTORY + File.separator + filename);
            Assert.assertTrue("Failed to over-write an existing file (test 3)", checkFile.exists());
            
            System.out.println("Test 3: Copied '"+fileToCopyName+"' to '"+folderName+"' resulted in '"+filename+"'.");
        }catch(Exception e){
            System.out.println("Caught exception while trying to copy a file that is a sibling to the subdirectory the course is in.");
            e.printStackTrace();
            Assert.fail("Failed to copy file (test 3).");
        }
        
      //
      // Copy File - test 4
      // - file to copy is a great grandchild of the course folder and a destination is a great grandchild location
      //
      try{
          String fileToCopyName = username + "\\fileCopyTests\\test4\\b\\fileToCopy.txt";
          String folderName = username + "\\fileCopyTests\\test4\\a\\";
          String filename = fileServices.copyWorkspaceFile(null, fileToCopyName, folderName, NameCollisionResolutionBehavior.FAIL_ON_COLLISION, null);
          
          File checkFile = new File(WORKSPACE_DIRECTORY + File.separator + filename);
          Assert.assertTrue("Failed to over-write an existing file (test 4)", checkFile.exists());
          
          System.out.println("Test 4: Copied '"+fileToCopyName+"' to '"+folderName+"' resulted in '"+filename+"'.");
      }catch(Exception e){
          System.out.println("Caught exception while trying to copy a file in a subdirectory of which is a sibling to the subdirectory the course is in.");
          e.printStackTrace();
          Assert.fail("Failed to copy file (test 4).");
      }
    }

}
