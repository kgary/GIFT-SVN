/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.monitor;

import mil.arl.gift.tools.remote.HostInfo;

/**
 * An interface for getting updates when a GIFT remote host comes online or goes
 * offline
 *
 * @author jleonard
 */
public interface RemoteClientStatusListener {

    /**
     * Callback when a GIFT Host comes online
     *
     * @param hostInfo The information about the host coming online
     */
    void onRemoteClientOnline(HostInfo hostInfo);

    /**
     * Callback when a GIFT Host goes offline
     *
     * @param hostInfo The information about the host going offline
     */
    void onRemoteClientOffline(HostInfo hostInfo);
}
