/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.validation;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gwt.user.client.ui.Composite;

import mil.arl.gift.common.gwt.client.ErrorDetails;
import mil.arl.gift.common.gwt.client.widgets.bootstrap.DetailsDialogBox;
import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.util.StringUtils;

/**
 * Validation composite used to ensure proper validation handing.
 * 
 * @author sharrison
 */
public abstract class ValidationComposite extends Composite implements HasValidation {

    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(ValidationComposite.class.getName());

    /** Displays any validation errors on the page */
    private ValidationWidget validations;

    /** The validation statuses for this validation composite */
    private Set<ValidationStatus> validationStatusSet;

    /** The set of child {@link ValidationComposite composites} */
    private Set<ValidationComposite> childValidationCompositeSet;

    /** The error dialog for all validation composites */
    private static DetailsDialogBox errorDialog = null;

    /** Flag to indicate if the composite is valid or not. Defaults to valid. */
    private boolean compositeValid = true;

    /**
     * Flag that indicates if validation is active. An example of when to set this to false is if we
     * have parallel composites and only want one branch to be validated at a given time.
     */
    private boolean isValidationActive = true;

    /** Set of callbacks to execute when the validity changes. */
    private final Set<ValidationStatusChangedCallback> validityChangedCallbackSet = new HashSet<>();

    /** Callback to pass to its children to be notified when validity changes */
    private final ValidationStatusChangedCallback validityChangedCallback = new ValidationStatusChangedCallback() {
        @Override
        public void changedValidity(boolean isValid, boolean fireEvents) {
            if (isValid) {
                // changing this composite from invalid to valid
                if (areImmediateChildrenValid()) {
                    setValid(true, fireEvents);
                }
            } else {
                // changing this composite from valid to invalid
                setValid(false, fireEvents);
            }
        }
    };

    /**
     * Add the children that are of type {@link ValidationComposite}.
     * 
     * @param childValidationComposites the set of validation children.
     */
    public abstract void addValidationCompositeChildren(Set<ValidationComposite> childValidationComposites);

    /**
     * This should be called if a composite's children list was modified.
     */
    protected void childListModified() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("childListModified()");
        }

        // clear any validation errors
        clearValidations();

        // refresh validation sets
        validationStatusSet = null;
        childValidationCompositeSet = null;

        // reapply validation to children
        initValidationComposite(validations);
    }

    /**
     * Sets the validation widget to contain the error messages. This does not guarantee real-time
     * validation. The composite must implement proper validation logic.<br>
     * <br>
     * This should be called <b>after</b> all validation statuses have been initialized.
     * 
     * @param validationWidget the validation widget. Can't be null.
     */
    public void initValidationComposite(ValidationWidget validationWidget) {
        if (validationWidget == null) {
            throw new IllegalArgumentException("The parameter 'validationWidget' cannot be null.");
        }

        this.validations = validationWidget;

        // initialize validation composite children
        for (ValidationComposite child : getChildren()) {
            child.initValidationComposite(validationWidget);
        }

        // initialize validation status children.
        getValidationStatuses();
    }

    /**
     * Adds a callback to execute when the validity changes.
     * 
     * @param callback the callback to execute when the status validity changes. Can't be null.
     */
    public void addValidationStatusChangedCallback(final ValidationStatusChangedCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("The parameter 'callback' cannot be null.");
        }

        // duplicate callback
        if (validityChangedCallbackSet.contains(callback)) {
            return;
        }

        validityChangedCallbackSet.add(callback);
    }

    /**
     * Checks the immediate validation status children and validation composite children to see if
     * they are all valid.
     * 
     * @return true if all the children (status and composite) are valid; false otherwise.
     */
    private boolean areImmediateChildrenValid() {
        // check validity of immediate validation status children
        for (ValidationStatus status : getValidationStatuses()) {
            if (!status.isValid()) {
                return false;
            }
        }

        // check validity of immediate validation composite children
        for (ValidationComposite child : getChildren()) {
            if (!child.isValid()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Retrieve the direct {@link ValidationStatus validation statuses} of this
     * {@link ValidationComposite}.
     * 
     * @return the validation statuses. Can't be null. Can be empty.
     */
    private Set<ValidationStatus> getValidationStatuses() {
        if (validationStatusSet == null) {
            validationStatusSet = new HashSet<ValidationStatus>();
            getValidationStatuses(validationStatusSet);

            // check for null and add validity changed callback
            for (ValidationStatus status : validationStatusSet) {
                if (status == null) {
                    throw new UnsupportedOperationException(
                            this.getClass() + " contains a null validation status. This is not allowed.");
                }
                status.addValidationStatusChangedCallback(validityChangedCallback);
            }
        }

        return validationStatusSet;
    }

    /**
     * Retrieve the direct children of this {@link ValidationComposite}.
     * 
     * @return the validation children. Can't be null. Can be empty.
     */
    protected Set<ValidationComposite> getChildren() {
        if (childValidationCompositeSet == null) {
            childValidationCompositeSet = new HashSet<ValidationComposite>();
            addValidationCompositeChildren(childValidationCompositeSet);

            // check for null and add validity changed callback
            for (ValidationComposite child : childValidationCompositeSet) {
                if (child == null) {
                    throw new UnsupportedOperationException(
                            this.getClass() + " contains a null child. This is not allowed.");
                }

                child.addValidationStatusChangedCallback(validityChangedCallback);
            }
        }

        return childValidationCompositeSet;
    }

    /**
     * Sets the flag that indicates if validation is active. If deactivated, this composite will not
     * undergo any validations or update the validation widget.
     * 
     * @param active true to mark the composite as active; false otherwise.
     */
    public void setActive(boolean active) {
        this.isValidationActive = active;
    }

    /**
     * Retrieves the active flag.
     * 
     * @return true if the composite is active (allows validation to occur); false otherwise.
     */
    public boolean isActive() {
        return isValidationActive;
    }

    /**
     * Returns the valid status of the composite.
     * 
     * @return true if the composite is valid; false otherwise.
     */
    public boolean isValid() {
        return compositeValid;
    }

    /**
     * Sets the validity of this composite.
     * 
     * @param isValid true to mark this composite as valid; false to mark it as invalid.
     * @param fireEvents true to force the change callback to fire; false will
     *        only fire the change callback if the validity changes.
     */
    private void setValid(boolean isValid, boolean fireEvents) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("setValid(" + isValid + ") - composite is " + (isValid() ? "valid" : "invalid"));
        }

        // check if the value is changing
        if (fireEvents || isValid() != isValid) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Setting " + this.getClass() + " to " + (isValid ? "valid" : "invalid"));
            }

            this.compositeValid = isValid;
            for (ValidationStatusChangedCallback callback : validityChangedCallbackSet) {
                callback.changedValidity(isValid, fireEvents);
            }
        }
    }

    /**
     * Requests that validation be run. This should be called when a page element is modified. Does
     * not trigger a dirty event.
     * 
     * @param validationStatuses the {@link ValidationStatus} container that is used to populate the
     *        {@link ValidationWidget}.
     * @throws UnsupportedOperationException if {@link #validations} was never set.
     */
    protected void requestValidation(ValidationStatus... validationStatuses) throws UnsupportedOperationException {
        if (!isActive()) {
            /* Not active, so ignore validation */
            return;
        } else if (!isValidationWidgetSet()) {
            /* Active, but validation widget hasn't been set */
            throw attemptedToValidateWithoutWidget("Attempted to perform requestValidation().");
        } else if (validationStatuses == null || validationStatuses.length == 0) {
            /* Nothing to validate */
            return;
        }

        for (ValidationStatus validationStatus : validationStatuses) {
            validate(validationStatus);
            updateValidationStatus(validationStatus);
        }
    }

    /**
     * Requests that validation be run. This should be called when a page element is modified.
     * Triggers a dirty event to be sent if the provided source object is not null.
     * 
     * @param sourceScenarioObject the source scenario object that will be fired in the dirty event. Can't be null.
     * @param validationStatuses the {@link ValidationStatus} container that is used to populate the
     *        {@link ValidationWidget}.
     * @throws UnsupportedOperationException if {@link #validations} was never set.
     */
    protected void requestValidationAndFireDirtyEvent(Serializable sourceScenarioObject,
            ValidationStatus... validationStatuses) throws UnsupportedOperationException {
        if (!isActive()) {
            /* Not active, so ignore validation */
            return;
        } else if (!isValidationWidgetSet()) {
            /* Active, but validation widget hasn't been set */
            throw attemptedToValidateWithoutWidget("Attempted to perform requestValidationAndFireDirtyEvent().");
        }

        if (sourceScenarioObject == null) {
            throw new IllegalArgumentException("The parameter 'sourceScenarioObject' cannot be null.");
        }

        fireDirtyEvent(sourceScenarioObject);
        requestValidation(validationStatuses);        
    }

    /**
     * Updates the {@link ValidationWidget} with the validated {@link ValidationStatus}.
     * 
     * @param validationStatus the validation status to update the {@link ValidationWidget}.
     * @throws UnsupportedOperationException if {@link #validations} was never set.
     */
    protected void updateValidationStatus(ValidationStatus validationStatus) throws UnsupportedOperationException {
        if (!isActive()) {
            /* Not active, so ignore update */
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("updateValidationStatus() - " + this.getClass() + " is inactive.");
            }
            return;
        } else if (!isValidationWidgetSet()) {
            /* Active, but validation widget hasn't been set */
            throw attemptedToValidateWithoutWidget("Attempted to perform updateValidationStatus().");
        }

        validations.updateValidationStatus(validationStatus);
    }

    /**
     * Validates only the {@link ModelValidationStatus model validation statuses}.
     * 
     * @throws UnsupportedOperationException if {@link #validations} was never set.
     */
    public void validateModelValidationStatuses() throws UnsupportedOperationException {
        if (!isActive()) {
            /* Not active, so ignore validation */
            return;
        } else if (!isValidationWidgetSet()) {
            /* Active, but validation widget hasn't been set */
            throw attemptedToValidateWithoutWidget("Attempted to perform validateModelValidationStatuses().");
        }

        // validate and update model validation statuses for this composite
        for (ValidationStatus validationStatus : getValidationStatuses()) {
            if (validationStatus instanceof ModelValidationStatus) {
                validate(validationStatus);
                updateValidationStatus(validationStatus);
            }
        }

        // validate and update status for this composite's children
        for (ValidationComposite child : getChildren()) {
            child.validateModelValidationStatuses();
        }
    }

    /**
     * Validates all the components, but bypasses firing the dirty event. <b>This should only be
     * called on component population.</b>
     * 
     * @return boolean that indicates the the validity of this composite. True if all statuses were
     *         valid; false is at least one status was invalid.
     * @throws UnsupportedOperationException if attempting to update a validation status, but
     *         {@link #validations} was never set.
     */
    public boolean validateAll() throws UnsupportedOperationException {
        if (!isActive()) {
            return true;
        }

        boolean valid = true;

        // validate and update status for this composite
        for (ValidationStatus validationStatus : getValidationStatuses()) {
            // update validity
            validate(validationStatus);

            // update valid flag
            valid &= validationStatus.isValid();

            // Update validation widget; handles a null Scenario Validation Widget internally
            updateValidationStatus(validationStatus);
        }

        // validate and update status for this composite's children
        for (ValidationComposite child : getChildren()) {
            valid &= child.validateAll();
        }

        return valid;
    }

    /**
     * Validates all the components.
     * 
     * @param sourceScenarioObject the source scenario object that will be fired in the dirty event. Can't be null.
     * @return boolean that indicates the the validity of this composite. True if all statuses were
     *         valid; false is at least one status was invalid.
     * @throws UnsupportedOperationException if attempting to update a validation status, but
     *         {@link #validations} was never set.
     */
    public boolean validateAllAndFireDirtyEvent(Serializable sourceScenarioObject)
            throws UnsupportedOperationException {
        if (sourceScenarioObject == null) {
            throw new IllegalArgumentException("The parameter 'sourceScenarioObject' cannot be null.");
        }

        fireDirtyEvent(sourceScenarioObject);
        boolean valid = validateAll();

        return valid;
    }

    /**
     * Clears the {@link #validations validation widget} of all invalid widgets from this composite.
     * 
     * @throws UnsupportedOperationException if attempting to update a validation status, but
     *         {@link #validations} was never set.
     */
    public void clearValidations() throws UnsupportedOperationException {
        /* Note: It doesn't matter if the composite is inactive in order to clear validations. */

        // clear this composite's validations (clears by marking them as valid)
        Set<ValidationStatus> statuses = new HashSet<ValidationStatus>();
        getValidationStatuses(statuses);
        for (ValidationStatus validationStatus : statuses) {
            if (!validationStatus.isValid()) {
                validationStatus.setValid();
                validations.updateValidationStatus(validationStatus);
            }
        }

        // clear this composite's child validations (clears by marking them as valid)
        for (ValidationComposite child : getChildren()) {
            child.clearValidations();
        }
    }

    /**
     * Checks if the validation widget is set for this composite.
     * 
     * @return true if the validation widget is set; false otherwise.
     */
    public boolean isValidationWidgetSet() {
        return validations != null;
    }

    /**
     * Fires a dirty event.
     * 
     * @param sourceObject the object that was the origin of the dirty event.
     *        Set to null if the event should be untraceable or will not be
     *        needed for further validation later on.
     */
    protected abstract void fireDirtyEvent(Serializable sourceObject);

    /**
     * Display an error dialog to the user if a widget attempted to validate
     * without having a {@link ValidationWidget} set.
     * 
     * @param errorMsg the method specific error message to be displayed for
     *        additional information inside the error details.
     * @return the generated {@link UnsupportedOperationException}
     */
    private UnsupportedOperationException attemptedToValidateWithoutWidget(String errorMsg) {

        final String simpleClassName = this.getClass().getSimpleName();
        final String customMsg = StringUtils.isNotBlank(errorMsg) ? errorMsg + " " : "";
        final String reason = customMsg + "The validation widget has not been set for " + simpleClassName + ".";

        final UnsupportedOperationException throwable = new UnsupportedOperationException(reason);

        /* If the dialog is not currently being displayed, create a new dialog
         * and show it */
        if (errorDialog == null || !errorDialog.isShowing()) {
            StringBuilder details = new StringBuilder(reason);
            details.append(" There are 3 ways to resolve this: <ol>");
            details.append("<li>Add this ").append(simpleClassName)
                    .append(" to its parent's addValidationCompositeChildren() method. One of the ")
                    .append(simpleClassName)
                    .append("'s ancestors should be calling initValidationComposite(ValidationWidget) and that will automatically propagate the validation widget to all declared validation children.</li>");
            details.append("<li>Call 'initValidationComposite(ValidationWidget)' at the end of this ")
                    .append(simpleClassName)
                    .append("'s constructor to directly set a new validation widget. This solution is typically only used for top-level one-off widgets (e.g. container panels).</li>");
            details.append("<li>Call 'setActive(false)' to ignore validation for this ").append(simpleClassName)
                    .append(".</li>");
            details.append("</ol>");

            ErrorDetails errorDetails = new ErrorDetails(details.toString(),
                    DetailedException.getFullStackTrace(throwable));

            errorDialog = new DetailsDialogBox(reason, errorDetails, null) {
                @Override
                public void startDownload() {
                    /* Cannot implement downloading in the common package. The
                     * download button is being removed from this dialog
                     * below. */
                }
            };
            errorDialog.setText("Attempt to Validate Failed");
            errorDialog.removeDownloadButton();
            errorDialog.center();
        }

        return throwable;
    }
}
