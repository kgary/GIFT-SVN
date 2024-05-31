/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import mil.arl.gift.tools.authoring.common.CommonProperties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A jUnit test for CommonUtil.java
 * 
 * @author bzahid
 *
 */
public class CommonUtilTest {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CommonUtilTest.class);

	/** Stores list created by functions in CommonUtil.java */
	List<String> fileNames;
	
	/** Clears the list after every test method */
	@After
	public void clear() {
	    
	    if(fileNames != null){
	        fileNames.clear();
	    }
	}
	
	@Test
	public void getFileNamesTest() {
		
	    try{
	        File workspaceDirectory = new File(CommonProperties.getInstance().getWorkspaceDirectory());
	        fileNames = CommonUtil.getRelativeFileNamesByExtensions(workspaceDirectory, "");
	        assertNotNull("getDomainRelativeFileNamesByExtensions() returned a null list", fileNames);
		
	        System.out.println("\nDomain Files:");
		
	        printList();
	    }catch(Exception e){
	        logger.error("Caught exception while trying to get domain relative file names.", e);
	        e.printStackTrace();
	        Assert.fail("Failed to get domain relative file names by extentions.");
	    }
	}

	@Test
	public void getLessonFilesTest() {
		
	    try{
	        File workspaceDirectory = new File(CommonProperties.getInstance().getWorkspaceDirectory());
    		fileNames = CommonUtil.getLessonMaterialFiles(workspaceDirectory);
    		assertNotNull("getLessonMaterialFiles() returned a null list", fileNames);
    		
    		System.out.println("\nLesson Material Files:");
    		
    		printList();
	    }catch(Exception e){
	        logger.error("Caught exception while trying to get lesson material files.", e);
            e.printStackTrace();
            Assert.fail("Failed to get lesson material files.");
	    }
	}
	
	@Test
	public void getLearnerActionsTest() {
		
	    try{
	        File workspaceDirectory = new File(CommonProperties.getInstance().getWorkspaceDirectory());
    		fileNames = CommonUtil.getLearnerActionsFiles(workspaceDirectory);
    		assertNotNull("getLearnerActionsFiles() returned a null list", fileNames);
    		
    		System.out.println("\nLearner Actions Files:");
    		
    		printList();
	      }catch(Exception e){
              logger.error("Caught exception while trying to get learner action files.", e);
              e.printStackTrace();
              Assert.fail("Failed to get learner action files.");
	      }
	}
	
	@Ignore
	public void getSIMILEConfigTest() {
		
	    try{
	        File workspaceDirectory = new File(CommonProperties.getInstance().getWorkspaceDirectory());
    		fileNames = CommonUtil.getSIMILEConfigFiles(workspaceDirectory);
    		assertNotNull("getSIMLEConfigFiles() returned a null list", fileNames);
    		
    		System.out.println("\nSIMILE Config Files:");
    		
    		printList();
	      }catch(Exception e){
              e.printStackTrace();
              Assert.fail("Failed to get domain relative file names by extentions.");
	      }
	}
	
	@Test
	public void getDKFsTest() {
		
	    try{
	        File workspaceDirectory = new File(CommonProperties.getInstance().getWorkspaceDirectory());
    		fileNames = CommonUtil.getDKFs(workspaceDirectory);
    		assertNotNull("getDKFs() returned a null list", fileNames);
    		
    		System.out.println("\nDKF List:");
    		
    		printList();
	    }catch(Exception e){
            e.printStackTrace();
            Assert.fail("Failed to get domain relative file names by extentions.");  
	    }
	}
	
	/** Function to print the fileNames list. */
	private void printList() {
		
		/* Prints "-empty" if the list is empty */
		if(fileNames.isEmpty()) {
			System.out.println("-empty");
		}
		
		/* Prints the list */
		for(String s : fileNames) {
			System.out.println(s);
		}
	}
}
