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
 * An instance of a dialog box
 *
 * @author jleonard
 */
public class DialogInstance implements IsSerializable {

    private String dialogId;
    private DialogTypeEnum dialogType;
    private String title;
    private String message;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    public DialogInstance() {
    }

    /**
     * Constructor
     * 
     * @param dialogId The dialog ID
     * @param dialogType The type of dialog
     * @param title The title of the dialog
     * @param message The message of the dialog
     */
    public DialogInstance(String dialogId, DialogTypeEnum dialogType, String title, String message) {
        this.dialogId = dialogId;
        this.dialogType = dialogType;
        this.title = title;
        this.message = message;
    }

    /**
     * Gets the dialog's ID
     * 
     * @return String The dialog ID
     */
    public String getDialogId() {
        return dialogId;
    }

    /**
     * Gets the dialog's type
     * 
     * @return DialogTypeEnum The dialog's type
     */
    public DialogTypeEnum getType() {
        return dialogType;
    }

    /**
     * Gets the dialog's title
     * 
     * @return String The dialog's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the dialog's message
     * 
     * @return String The dialog's message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "DialogInstance = [ID: " + getDialogId()
                + ", Type: " + getType()
                + ", Title: " + getTitle()
                + ", Message: " + getMessage() + "]";
    }
}
