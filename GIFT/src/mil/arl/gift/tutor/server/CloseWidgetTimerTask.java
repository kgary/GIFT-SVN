/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import java.util.TimerTask;
import mil.arl.gift.tutor.shared.CloseAction;

/**
 * A timer task for executing a Close action on a widget
 *
 * @author jleonard
 */
public class CloseWidgetTimerTask extends TimerTask {

    private final DomainWebState webState;
    private final String id;

    /**
     * Constructor
     * 
     * @param webState The domain web state to send the close action to
     * @param id The ID of the widget instance
     */
    public CloseWidgetTimerTask(DomainWebState webState, String id) {
        this.webState = webState;
        this.id = id;
    }

    @Override
    public void run() {
        webState.doServerAction(new CloseAction(id));
    }
}
