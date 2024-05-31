/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.validation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.Command;

import mil.arl.gift.common.util.StringUtils;

/**
 * An abstract container class for validation statuses.
 * 
 * @author sharrison
 */
public abstract class ValidationStatus {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ValidationStatus.class.getName());

    /** Set of callbacks to execute when the validity changes. */
    private final Set<ValidationStatusChangedCallback> statusChangedCallbacks = new HashSet<>();

    /** The error message to display to the user if the widget is invalid. */
    private String errorMsg;

    /**
     * The collection of commands that should be invoked when the value of {@link #errorMsg}
     * changes.
     */
    private Collection<Command> errorMsgChangedCallbacks = new HashSet<>();

    /** Flag to indicate if the widget is valid or not. Defaults to valid. */
    private boolean valid = true;

    /**
     * Constructor.<br>
     * <br>
     * 
     * @param errorMsg The error message to display to the user if the widget is invalid. This
     *        should try to be a unique message for this specific widget.
     */
    public ValidationStatus(String errorMsg) {
        if (StringUtils.isBlank(errorMsg)) {
            throw new IllegalArgumentException("The parameter 'errorMsg' cannot be blank.");
        }

        this.errorMsg = errorMsg;
    }

    /**
     * The error message to display to the user if the widget is invalid.
     * 
     * @return the error message. Can't be blank.
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Adds a callback to execute when the validity changes.
     * 
     * @param callback the callback to execute when the status validity changes. Can't be null.
     */
    public void addValidationStatusChangedCallback(ValidationStatusChangedCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        statusChangedCallbacks.add(callback);
    }

    /**
     * Returns the valid status of the widget.
     * 
     * @return true if the validation status is valid; false otherwise.
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Sets the validity of the widget as valid.
     */
    public void setValid() {
        setValidity(true, false);
    }

    /**
     * Sets the validity of the widget as invalid.
     */
    public void setInvalid() {
        setValidity(false, false);
    }

    /**
     * Sets the validity of the widget using the provided flag.
     * 
     * @param isValid true to mark the widget as valid; false to mark it as invalid.
     */
    public void setValidity(boolean isValid) {
        setValidity(isValid, false);
    }

    /**
     * Sets the validity of the widget using the provided flag.
     * 
     * @param isValid true to mark the widget as valid; false to mark it as
     *        invalid.
     * @param fireEvents true to force the change callback to fire; false will
     *        only fire the change callback if the validity changes.
     */
    public void setValidity(boolean isValid, boolean fireEvents) {
        final boolean oldValidity = isValid();

        this.valid = isValid;

        // validity changed, notify listeners
        if (fireEvents || oldValidity != isValid) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("setValidity(" + isValid + ") for " + this);
            }

            for (ValidationStatusChangedCallback callback : statusChangedCallbacks) {
                callback.changedValidity(isValid(), fireEvents);
            }
        }
    }

    /**
     * Updates the value of the error message that is displayed while validation is failing.
     * 
     * @param errorMsg The message text that should now be used when displaying the error message.
     *        Can't be null.
     */
    public void setErrorMessage(String errorMsg) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setErrorMessage(\"" + errorMsg + "\")");
        }

        if (StringUtils.isBlank(errorMsg)) {
            throw new IllegalArgumentException("The parameter 'errorMsg' cannot be blank.");
        }

        if (StringUtils.equals(this.errorMsg, errorMsg)) {
            return;
        }

        for (Command command : errorMsgChangedCallbacks) {
            try {
                command.execute();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "There was an exception while invoking an error message changed command", e);
            }
        }

        this.errorMsg = errorMsg;
    }

    /**
     * Adds an error message change command to this {@link WidgetValidationStatus}.
     * 
     * @param command The command to be invoked when the value of the {@link #errorMsg} has changed.
     *        Can't be null.
     */
    public void addErrorMessageChangeCommand(Command command) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("addErrorMessageChangeCommand(" + command + ")");
        }

        if (command == null) {
            throw new IllegalArgumentException("The parameter 'command' cannot be null.");
        }

        errorMsgChangedCallbacks.add(command);
    }

    /**
     * Removes an error message change command to this {@link WidgetValidationStatus}.
     * 
     * @param command The command that should no longer be invoked when the value of the
     *        {@link #errorMsg} has changed. Can't be null.
     */
    public void removeErrorMessageChangeCommand(Command command) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("removeErrorMessageChangeCommand(" + command + ")");
        }

        if (command == null) {
            throw new IllegalArgumentException("The parameter 'command' cannot be null.");
        }

        errorMsgChangedCallbacks.remove(command);
    }
}
