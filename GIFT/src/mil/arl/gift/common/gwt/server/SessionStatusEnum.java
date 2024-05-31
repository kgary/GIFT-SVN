/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.server;

/**
 * Enumeration of statuses a session can be in
 *
 * @author jleonard
 */
public enum SessionStatusEnum {
    RUNNING,
    STOPPING,
    STOPPED,
    ENDING,
    ENDED;
}
