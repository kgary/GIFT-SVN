/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;

import mil.arl.gift.common.course.InteropsInfo;
import mil.arl.gift.common.course.InteropsInfo.ConditionInfo;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.io.ClassFinderUtil;
import mil.arl.gift.domain.knowledge.condition.AbstractCondition;
import mil.arl.gift.domain.knowledge.condition.autotutor.AutoTutorWebServiceInterfaceCondition;

/**
 * A jUnit test for DomainKnowledgeUtil.java
 * 
 * @author bzahid
 *
 */
public class DomainKnowledgeUtilTest {
	
	@Rule
	public ErrorCollector collector = new ErrorCollector();
	
	/** Stores lists created by functions in DomainKnowledgeUtil.java */
	List<String> fileNames;
	
	@Test
	public void getSIMILEConceptsTest() throws Exception {
		
		/* Note: Produces an error of the path to the config file is invalid */
		File file = new File("data/tests/TC3_Scenario.ixs");	
		assertTrue("Please specify a valid SIMILE config file", file.exists());
		
		fileNames = DomainKnowledgeUtil.getSIMILEConcepts(file.getAbsolutePath(), new FileInputStream(file));
		assertNotNull("getSIMILEConcepts() returned a null list", fileNames);
		
		System.out.println("\nSIMILE Concepts:");
		printList(fileNames);
	}
	
	@Test
	public void getConditionInputChoicesTest() {
		
		Field field;
		Class<?> inputClass;
		List<Class<?>> inputClasses;
		List<Class<?>> conditionClasses;
		List<String> conditionInputList;
		String classPath = "mil.arl.gift.domain.knowledge.condition";
		
		inputClasses = new ArrayList<Class<?>>();
		conditionInputList = new ArrayList<String>();
		
		try {
			
			inputClass = Class.forName("generated.dkf.Input");
			
			PropertyDescriptor p = Introspector.getBeanInfo(
					inputClass, Object.class).getPropertyDescriptors()[0];
			
			field = inputClass.getDeclaredField(p.getName());
			inputClasses = ClassFinderUtil.getChoiceTypes(field);
						
			
			/* Get condition classes from domain.knowledge.condition */
			conditionClasses = ClassFinderUtil.getSubClassesOf(classPath, AbstractCondition.class);
			
			/* For each class, find matching condition input classes and add them to a list  */
			for(Class<?> conditionClass : conditionClasses) {
			    
			    // #4027 - deprecated AutoTutor condition class which will cause DomainKnowledgeUtil.isValidConditionInputParam
			    //         to return false which will then cause this junit test to fail when it shouldn't
			    if(conditionClass.equals(AutoTutorWebServiceInterfaceCondition.class)){
			        continue;
			    }
				
				for(Class<?> input : inputClasses) {
					
					if(DomainKnowledgeUtil.isValidConditionInputParam(
							conditionClass.getCanonicalName(), input.getCanonicalName())){					
					
						conditionInputList.add(input.getCanonicalName());
					}
				}				
				
				System.out.println("\n" + conditionClass.getSimpleName() + " Input Choices: ");
				printList(conditionInputList);
				
				assertTrue("Found no matching input for " + conditionClass.getSimpleName(), 
						conditionInputList.size() >= 1);
				
				conditionInputList.clear();
			}
			
		} catch(Exception e) {
			
			fail("Caught exception: " + e.getMessage());
		}		
	}
	
	/** Function to print the created lists. */
	private void printList(List<String> list) {
		
		/* Prints "-empty" if list is empty */
		if(list.isEmpty()) {
			System.out.println("-empty");
		}
		
		/* Prints the list */
		for(String s : list) {
			System.out.println("- " + s);
		}
	}
	
	@Test
	public void getInteropInfoTest(){
	    
	    InteropsInfo interopsInfo = DomainKnowledgeUtil.getInteropsInfo();
	    Assert.assertTrue("Interops Information gathering failed", interopsInfo != null && interopsInfo.getInteropsInfoMap().size() >= 10);
	}
	
	@Test
	public void getConditionInfosForTrainingAppTest(){
	    
	    Set<ConditionInfo> vbs = DomainKnowledgeUtil.getConditionInfosForTrainingApp(TrainingApplicationEnum.VBS);
	    Assert.assertTrue("Incorrect number of VBS conditions", vbs != null && vbs.size() == 25);
	    
        Set<ConditionInfo> detestbed = DomainKnowledgeUtil.getConditionInfosForTrainingApp(TrainingApplicationEnum.DE_TESTBED);
        Assert.assertTrue("Incorrect number of DE Testbed conditions", detestbed != null && detestbed.size() == 6);
        
        Set<ConditionInfo> ppt = DomainKnowledgeUtil.getConditionInfosForTrainingApp(TrainingApplicationEnum.POWERPOINT);
        Assert.assertTrue("Incorrect number of PowerPoint conditions", ppt != null && ppt.size() == 6);

        Set<ConditionInfo> vrEngage = DomainKnowledgeUtil.getConditionInfosForTrainingApp(TrainingApplicationEnum.VR_ENGAGE);
        Assert.assertTrue("Incorrect number of VR Engage conditions", vrEngage != null && vrEngage.size() == 23);
	}
}
