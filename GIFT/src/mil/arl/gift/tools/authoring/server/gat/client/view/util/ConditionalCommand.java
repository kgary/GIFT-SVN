/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util;

/**
 * A command that executes a conditional expression and returns the result
 * 
 * @author nroberts
 */
public interface ConditionalCommand {

	/** 
	 * Executes a conditional expression and returns the result
	 * 
	 * @return the result of the conditional expression
	 */
	public boolean execute();
}
