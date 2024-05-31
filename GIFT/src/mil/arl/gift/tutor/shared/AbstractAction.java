/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tutor.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * An action that can be taken.
 *
 * @author jleonard
 */
public abstract class AbstractAction implements IsSerializable {

    private ActionTypeEnum actionType;

    /**
     * Constructor
     *
     * @param actionType The type of action to take
     */
    public AbstractAction(ActionTypeEnum actionType) {
        this.actionType = actionType;
    }

    /**
     * Gets the type of action to take
     *
     * @return ActionTypeEnum The type of action to take
     */
    public ActionTypeEnum getActionType() {
        return actionType;
    }

    @Override
    public String toString() {
        return "actionType = " +actionType;
    }
}
