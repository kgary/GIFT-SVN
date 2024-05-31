/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.ert.server;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.logger.JSONMessageLogReader;
import mil.arl.gift.common.logger.MessageLogReader;

/** 
 * A jUnit test for parsing log messages
 * 
 * @author bzahid
 */
public class LegacyReportParser {
	
	private static final String LOG1 = PackageUtil.getTestData() + File.separator + "Legacy_System_Message_Log.log";
	private static final String LOG2 = PackageUtil.getTestData() + File.separator + "Legacy_Domain_Session_Log.log";
		
	// Add additional log file paths to this list
	private static final List<String> LOGS = Arrays.asList(LOG1, LOG2);
	
	private static boolean printStackTrace = false;
	boolean fail = false;
	
	@Test
    public void testLegacyReportParser() {
		
		MessageLogReader reader = new JSONMessageLogReader();
		Set<MessageTypeEnum> messageTypes = new HashSet<MessageTypeEnum>();
		List<MessageTypeEnum> definedMessages = new ArrayList<MessageTypeEnum>();
		
		definedMessages.addAll(MessageTypeEnum.VALUES());
		Collections.sort(definedMessages);
		
		for(String log : LOGS) {
			try{
												
				System.out.println("\nParsing " + log);
				reader.parseLog(new FileProxy(new File(log)));
				System.out.println("Finished parsing " + log);
				messageTypes.addAll(reader.getTypesOfMessages());
				
			} catch(Exception e) {
				
				System.err.println(e.getMessage());
				
				if(printStackTrace) {
					e.printStackTrace();
				} else {
					// Print the exception causes if the entire stack trace is not wanted.					
					Throwable cause = e.getCause();
					while(cause != null) {
						System.err.println("   Caused by: " + cause.getMessage());
						cause = cause.getCause();
					}
				}				
				fail = true;
			}
		}
				
		if(messageTypes.size() != definedMessages.size()) {
			
			definedMessages.removeAll(messageTypes);
			System.out.println("\nAll message types must be present in the log file. The following message types were not found:" );
			
			for(MessageTypeEnum msg : definedMessages) {
				System.out.println(msg);
			}
			
			fail("Log files must contain ALL message types. See the console output or junit output file for the list of missing message types.");
		}
		
		if(fail) {
			fail("Failed to parse one or more log files.");
		}
	}
}
