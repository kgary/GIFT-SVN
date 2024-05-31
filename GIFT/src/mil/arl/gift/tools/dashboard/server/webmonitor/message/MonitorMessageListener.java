/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.webmonitor.message;

import mil.arl.gift.net.api.message.Message;

/**
 * For registering with the Monitor module to receive network messages.
 *
 * @author jleonard
 */
public interface MonitorMessageListener {
    
    /**
     * The callback for when a message is received by the Monitor module
     * 
     * @param msg The message received by the Monitor module
     */
    void handleMessage(Message msg);
}
