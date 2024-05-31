/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.common.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import mil.arl.gift.common.io.DetailedException;

/**
 * A jUnit test for CourseUtil.java
 * 
 * @author bzahid
 *
 */
public class CourseUtilTest {
	
	/** Stores the list returned by the functions in CourseUtil.java */
	List<String> values;
		
	@Test
	public void interopTest() {
		
		try {
			values = CourseUtil.getInteropImplementations();
		} catch (DetailedException e) {
			throw e;
		}
		
		assertNotNull("getInteropImplementations() failed to retrieve subclasses"
				+ "of \"mil.arl.gift.gateway.interop\"", values);
		
		System.out.println("\nAvailable Plugins:");
		
		/* Prints "-empty" if the list is empty */
		if(values.isEmpty()) {
			System.out.println("- empty");
		}
		
		/* Prints the list */
		for(int i = 0; i < values.size(); i++) {
			System.out.println("- " + values.get(i));
		}
	}
}
