/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import mil.arl.gift.common.DomainSession;

/**
 * An interface for listening for the status of domain sessions when they start and end
 *
 * @author jleonard
 */
public interface DomainSessionStatusListener {

    /**
     * Callback for when a domain session begins
     *
     * @param domainSession - container for domain session info
     */
    void domainSessionActive(DomainSession domainSession);

    /**
     * Callback for when a domain session ends
     *
     * @param domainSession - container for domain session info
     */
    void domainSessionInactive(DomainSession domainSession);
}
