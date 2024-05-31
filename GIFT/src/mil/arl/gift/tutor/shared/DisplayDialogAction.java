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
 * An action to display a dialog box
 * 
 * @author jleonard
 */
public class DisplayDialogAction extends AbstractAction implements IsSerializable {

    private DialogInstance dialog;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public DisplayDialogAction() {
        super(ActionTypeEnum.DISPLAY_DIALOG);
    }

    /**
     * Constructor
     * 
     * @param dialog The instance of a dialog to display
     */
    public DisplayDialogAction(DialogInstance dialog) {
        super(ActionTypeEnum.DISPLAY_DIALOG);
        this.dialog = dialog;
    }

    /**
     * Gets the dialog instance to display
     * 
     * @return DialogInstance The dialog instance to display
     */
    public DialogInstance getDialog() {
        return dialog;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DisplayDialogAction [dialog=");
        builder.append(dialog);
        builder.append(", ").append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}
