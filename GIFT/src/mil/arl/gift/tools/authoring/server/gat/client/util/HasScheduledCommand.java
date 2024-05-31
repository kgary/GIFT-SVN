/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.util;

import com.google.gwt.core.client.Scheduler;

/**
 * The Interface HasScheduledCommand.
 */
public interface HasScheduledCommand {
	
	/**
	 * Sets the scheduled command.
	 *
	 * @param command the new scheduled command
	 */
	void setScheduledCommand(Scheduler.ScheduledCommand command);
}
