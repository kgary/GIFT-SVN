/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.python;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.io.Constants;

/**
 * Helper class to process streams from Process objects created via Runtime.exec.
 * 
 * @author cragusa
 */
public class ProcessStreamProcessor extends Thread {

	private static Logger logger = LoggerFactory.getLogger(ProcessStreamProcessor.class);

	public enum Type {
		ERROR,
		OUTPUT,
	}
	
	/** User settable identifier for the process - appears in output */
	private String procName = "unspecified";
	
	/** an input stream of the process */
	private InputStream is;
	
	/** The type of input stream - used to control logging by Log4j */
	private Type type;
	
	/** Flag that controls shutdown of this thread */
	private volatile boolean shutdown = false;

	/** 
	 * Constructor
	 * 
	 * @param procName identifier for the associated process
	 * @param is stream of the associated process
	 * @param type the type of stream (ERROR or OUTPUT). This is used internally for log4j
	 * @throws IllegalArgumentException if any of the passed in arguments are null
	 */
	public ProcessStreamProcessor(String procName, InputStream is, Type type) throws IllegalArgumentException {
				
		if(procName == null) {			
			throw new IllegalArgumentException("procName must not be null");
		}
		this.procName = procName;
		
		if(is == null) {
			throw new IllegalArgumentException("is must not be null");
		}
		this.is = is;
		
		if(type == null) {
			throw new IllegalArgumentException("type must not be null");
		}
		this.type = type;
	}
	
	/**
	 * Shuts down the thread
	 */
	public void shutdown() {
		shutdown = true;
	}

	@Override
	public void run() {
		
		try {
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line=null;
			StringBuilder sb = new StringBuilder();
			while ( !shutdown && (line = br.readLine()) != null) {								
				if(type == Type.ERROR) {
				    sb.append(procName).append(" ERROR> ").append(line).append(Constants.NEWLINE);
				} else if (type == Type.OUTPUT && logger.isInfoEnabled()) {	
				    sb.append(procName).append(" OUTPUT> ").append(line).append(Constants.NEWLINE);
				}
			}
			
			if(sb.length() > 0){
    			if(type == Type.ERROR){
    			    logger.error(sb.toString());
    			}else if(type == Type.OUTPUT && logger.isInfoEnabled()){
    			    logger.info(sb.toString());
    			}
			}
		} catch (IOException ioe) {
			
			logger.error("Caught IOException while processing error/output stream from '" + procName + "'", ioe);
		}
	}
}