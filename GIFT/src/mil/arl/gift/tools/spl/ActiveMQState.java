/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.spl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Keeps track of the state of ActiveMQ
 * 
 * @author cdettmering
 */
public class ActiveMQState {
	
	private static Logger logger = LoggerFactory.getLogger(ActiveMQState.class);
	
	private boolean started;
	
	/**
	 * Default constructor
	 */
	public ActiveMQState() {
	}
	
	/**
	 * Checks if ActiveMQ has started.
	 * 
	 * @return If ActiveMQ has started
	 */
	public boolean isStarted() {
		return started;
	}
	
	/**
	 * Tell this class that ActiveMQ has started.
	 */
	public void start() {
		logger.info("ActiveMQ is started.");
		started = true;
	}
}
