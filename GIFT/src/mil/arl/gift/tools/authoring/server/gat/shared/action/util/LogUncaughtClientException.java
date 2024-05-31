/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.util;

import net.customware.gwt.dispatch.shared.Action;

/**
 * The Class LogUncaughtClientException.
 */
public class LogUncaughtClientException implements Action<LogUncaughtClientExceptionResult> {

	/** The log entry. */
	private String logEntry;
		
	/**
	 * Instantiates a new log uncaught client exception.
	 *
	 * @param logEntry the log entry
	 */
	public LogUncaughtClientException(String logEntry) {
		super();
		this.logEntry = logEntry;
	}

	/**
	 * Instantiates a new log uncaught client exception.
	 */
	public LogUncaughtClientException() {
		super();
	}

	/**
	 * Gets the log entry.
	 *
	 * @return the log entry
	 */
	public String getLogEntry() {
		return logEntry;
	}

	/**
	 * Sets the log entry.
	 *
	 * @param logEntry the new log entry
	 */
	public void setLogEntry(String logEntry) {
		this.logEntry = logEntry;
	}
	
    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[LogUncaughtClientException: ");
        sb.append("logEntry = ").append(logEntry);
        sb.append("]");

        return sb.toString();
    } 
}
