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
 * Keeps track of the state of GAS
 * 
 * @author cdettmering
 */
public class GASState {
	
	private static Logger logger = LoggerFactory.getLogger(GASState.class);
	
	private boolean started;
	
	/**
	 * Default constructor
	 */
	public GASState() {
	}
	
	/**
	 * Checks if GAS has started.
	 * 
	 * @return If GAS has started
	 */
	public boolean isStarted() {
		return started;
	}
	
	/**
	 * Tell this class that GAS has started.
	 */
	public void start() {
		logger.info("GAS is started.");
		started = true;
	}
}
