/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.server;

import mil.arl.gift.tutor.shared.AbstractAction;

/**
 * A listener for actions taken on web pages
 *
 * @author jleonard
 */
public interface ActionListener {

    /**
     * Called when an action has been taken on a web page
     * 
     * @param action The action taken
     */
    void onAction(AbstractAction action);
}
