/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.server.messagehandlers;

import mil.arl.gift.net.api.message.DomainSessionMessageInterface;
import mil.arl.gift.tools.dashboard.server.DashboardBrowserWebSession;

/**
 * Interface used to represent message handlers for activemq messages.
 *
 * @author nblomberg
 *
 */
public interface MessageHandlerInterface {

    /**
     * Allow feature/implementation specific handling of activemq messages.
     *
     * @param browserSession The browser session that is requesting the message
     *        to be processed.
     * @param domainMsg The domain session message that was received.
     */
    public void handleMessage(DashboardBrowserWebSession browserSession, DomainSessionMessageInterface domainMsg);

}
