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
 * An enumeration for the different types of dialogs
 *
 * @author jleonard
 */
public enum DialogTypeEnum implements IsSerializable {

    INFO_DIALOG(false),
    ERROR_DIALOG(true),
    FATAL_ERROR_DIALOG(true),
    LOADING_DIALOG(false);
    private boolean error;

    /**
     * Default Constructor
     *
     * Required for GWT
     */
    DialogTypeEnum() {
    }

    /**
     * Constructor
     * 
     * @param error If the dialog is an error dialog
     */
    private DialogTypeEnum(boolean error) {
        this.error = error;
    }

    /**
     * Gets if the dialog is an error dialog
     * 
     * @return If the dialog is an error dialog
     */
    public boolean isError() {
        return error;
    }
}
