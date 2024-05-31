/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mil.arl.gift.common.io.AbstractSchemaHandler;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.tools.authoring.common.conversion.AbstractConversionWizardUtil;

import org.junit.Assert;
import org.junit.Test;

public class ConversionWizardTest {

	private static List<File> xmlFiles = new ArrayList<File>();
	private static List<File> convertedFiles = new ArrayList<File>();
	private static List<File> failedToConvertFiles = new ArrayList<File>();
	private static List<File> notConvertedFiles = new ArrayList<File>();
	
	private File currentFile = null;
	
	/**
	 * Used to test conversion wizards, will convert every possible file in the \data\conversionWizard folder
	 * including course, dkf, learner config, metadata, pedagogical config, traning app extension, and sensor config files
	 */
	@Test
	public void convertXMLFiles(){
		try{
			File conversionDataDirectory = new File("data//conversionWizard");
			System.out.println("retrieving xml files from " + conversionDataDirectory.getAbsolutePath());
			getXMLFilesInDirectory(conversionDataDirectory);
			if(xmlFiles == null || xmlFiles.size() == 0){
				System.out.println("Empty");
			}
			else{
				System.out.println("There are " + xmlFiles.size() + " xml files in the list");
			}
			//for each xml file in the directory, decide which type of file it is and perform the appropriate conversion
			for(File file : xmlFiles){
				try{
					currentFile = file;
					if(file.getName().endsWith(AbstractSchemaHandler.COURSE_FILE_EXTENSION)){
						AbstractConversionWizardUtil.getConversionUtil(new FileProxy(file), true).convertCourse(new FileProxy(file), false, true);
						convertedFiles.add(file);
						System.out.println("Successfully converted " + file.getAbsolutePath());
					}
					else if(file.getName().endsWith(AbstractSchemaHandler.DKF_FILE_EXTENSION)){
						AbstractConversionWizardUtil.getConversionUtil(new FileProxy(file), true).convertScenario(new FileProxy(file), false, true);
						convertedFiles.add(file);
						System.out.println("Successfully converted " + file.getAbsolutePath());
					}
					else if(file.getName().endsWith(AbstractSchemaHandler.LEARNER_CONFIG_FILE_EXTENSION)){
						AbstractConversionWizardUtil.getConversionUtil(new FileProxy(file), true).convertLearnerConfiguration(new FileProxy(file), false, true);
						convertedFiles.add(file);
						System.out.println("Successfully converted " + file.getAbsolutePath());
					}
					else if(file.getName().endsWith(AbstractSchemaHandler.METADATA_FILE_EXTENSION)){
						AbstractConversionWizardUtil.getConversionUtil(new FileProxy(file), true).convertMetadata(new FileProxy(file), false, true);
						convertedFiles.add(file);
						System.out.println("Successfully converted " + file.getAbsolutePath());
					}
					else if(file.getName().endsWith(AbstractSchemaHandler.SENSOR_CONFIG_FILE_EXTENSION)){
						AbstractConversionWizardUtil.getConversionUtil(new FileProxy(file), true).convertSensorConfiguration(new FileProxy(file), false, true);
						convertedFiles.add(file);
						System.out.println("Successfully converted " + file.getAbsolutePath());
					}
					else if(file.getName().endsWith(AbstractSchemaHandler.EMAP_PEDAGOGICAL_CONFIG_FILE_EXTENSION)){
						AbstractConversionWizardUtil.getConversionUtil(new FileProxy(file), true).convertEMAPConfiguration(new FileProxy(file), false, true);
						convertedFiles.add(file);
						System.out.println("Successfully converted " + file.getAbsolutePath());
					}
					else if(file.getName().endsWith(AbstractSchemaHandler.TRAINING_APP_FILE_EXTENSION)){
						AbstractConversionWizardUtil.getConversionUtil(new FileProxy(file), true).convertTrainingApplicationRef(new FileProxy(file), false, true);
						convertedFiles.add(file);
						System.out.println("Successfully converted " + file.getAbsolutePath());
					}
					else{
						notConvertedFiles.add(file);
					}
					//catch exceptions on each individual file on why they could not be converted, and add failed file to a list
				}catch(Exception e){
					failedToConvertFiles.add(currentFile);
					System.out.println("problem converting " + currentFile.getAbsolutePath());
					e.printStackTrace();
				}
			}
			
			System.out.println("\n\nSuccessfully converted " + convertedFiles.size() + " file(s)\nFailed to Convert " + failedToConvertFiles.size() + " file(s)");
			System.out.println("\n\nFiles not attempted to be converted: ");
			for(File file : notConvertedFiles){
				System.out.println(file.getAbsolutePath());
			}
			if(!failedToConvertFiles.isEmpty()){
				Assert.fail("One or more files failed to convert");
			}
		}catch(Exception e){
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	/**
	 * Recursively retrieves all the files in the given directory that end with a .xml extension
	 * 
	 * @param dir - The directory to seach within
	 */
	private void getXMLFilesInDirectory(File dir){
		List<File> allFiles = Arrays.asList(dir.listFiles());
		for(File file : allFiles){
			if(file.getName().endsWith(".xml")){
				xmlFiles.add(file);
			}
			else if(file.isDirectory()){
				getXMLFilesInDirectory(file);
			}
		}
		
	}
}
