/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.tools.dashboard.client.bootstrap.webmonitor.message;

import mil.arl.gift.tools.dashboard.shared.messages.monitor.AbstractMessageUpdate;

/**
 * A panel that displays system messages that are not tied to specific domain
 * sessions within GIFT
 * 
 * @author nroberts
 */
public class SystemMessageDisplayPanel extends AbstractMessageDisplayPanel{
    
    /**
     * Creates a new panel to display system messages
     */
    public SystemMessageDisplayPanel() {
        super();
    }

    @Override
    protected boolean isAccepted(AbstractMessageUpdate update) {
        return update.getDomainSessionId() == null;
    }

    @Override
    public Integer getDomainSessionId() {
        
        /* No domain session is monitored by this panel */
        return null;
    }
}
