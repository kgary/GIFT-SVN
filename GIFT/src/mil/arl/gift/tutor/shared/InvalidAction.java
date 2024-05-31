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
 * An action that isn't valid
 *
 * @author jleonard
 */
public class InvalidAction extends AbstractAction implements IsSerializable {

    private String message;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public InvalidAction() {
        super(ActionTypeEnum.INVALID);
    }

    /**
     * Constructor
     * 
     * @param message Message why the action is invalid
     */
    public InvalidAction(String message) {
        super(ActionTypeEnum.INVALID);
        this.message = message;
    }

    /**
     * Gets why the action is invalid
     * 
     * @return String Why the action is invalid
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[InvalidAction: message=");
        builder.append(message);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }
    
    
}
