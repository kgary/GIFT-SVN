/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

/**
 * For registering with the Monitor Module to receive a callback when a domain
 * session is selected to be monitored or ignored.
 *
 * @author jleonard
 */
public interface DomainSessionMonitorListener {

    /**
     * The callback for when a when a domain session is being monitored
     *
     * @param domainSessionId The ID of the domain session to be monitored
     */
    void monitorDomainSession(int domainSessionId);

    /**
     * The callback for when a domain session is no longer being monitored
     *
     * @param domainSessionId The ID of the domain session no longer being
     * monitored
     */
    void ignoreDomainSession(int domainSessionId);
}
