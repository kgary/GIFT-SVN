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
 * An action to deactivate a session
 *
 * @author jleonard
 */
public class DeactivateAction extends AbstractAction implements IsSerializable {

    private String message;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public DeactivateAction() {
        super(ActionTypeEnum.DEACTIVATE);
    }

    public DeactivateAction(String message) {
        super(ActionTypeEnum.DEACTIVATE);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }    

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[DeactivateAction: message=");
        builder.append(message);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}
