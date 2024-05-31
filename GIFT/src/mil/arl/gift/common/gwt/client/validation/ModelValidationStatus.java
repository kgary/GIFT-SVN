/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.validation;

import java.io.Serializable;

import mil.arl.gift.common.util.StringUtils;

/**
 * A {@link ValidationStatus} for objects in the scenario model.
 * 
 * @author sharrison
 */
public abstract class ModelValidationStatus extends ValidationStatus {

    /**
     * The model object that is the target of the validation status check.
     */
    private Serializable modelObject;

    /**
     * Additional instructions to display to the user if this status is clicked in the
     * {@link ValidationWidget}. This will be displayed under the original message.
     */
    private String additionalInstructions;

    /**
     * Flag to indicate if the additional instructions should be appended to the error message or
     * not
     */
    private boolean showAdditionalInstructions = false;

    /**
     * Constructor.
     * 
     * @param errorMsg The error message to display to the user if the widget is invalid. This
     *        should try to be a unique message for this specific widget.
     */
    public ModelValidationStatus(String errorMsg) {
        super(errorMsg);

        addValidationStatusChangedCallback(new ValidationStatusChangedCallback() {
            @Override
            public void changedValidity(boolean isValid, boolean fireEvents) {
                if (isValid) {
                    setShowAdditionalInstructions(false);
                }
            }
        });
    }

    /**
     * Sets the object that is the target of the validation status check.
     * 
     * @param modelObject The model object that is the target of the validation status check. If
     *        null, no 'jump to' action will be available.
     */
    public void setModelObject(Serializable modelObject) {
        this.modelObject = modelObject;
    }

    /**
     * The model object that is the target of the validation status check.
     * 
     * @return the model object. Can be null.
     */
    public Serializable getModelObject() {
        return modelObject;
    }

    /**
     * Sets the additional instructions to display to the user if this status is clicked in the
     * {@link ValidationWidget}. This will be displayed under the original message.
     * 
     * @param additionalInstructions the message to display
     */
    public void setAdditionalInstructions(String additionalInstructions) {
        this.additionalInstructions = additionalInstructions;
    }

    /**
     * Retrieves the additional instructions to display to the user if this status is clicked in the
     * {@link ValidationWidget}. This will be displayed under the original message.
     * 
     * @return the additional instructions. Can be null or empty.
     */
    public String getAdditionalInstructions() {
        return additionalInstructions;
    }

    /**
     * Flag to indicate if the {@link #additionalInstructions} should be displayed along with the
     * {@link #getErrorMsg() error message}.
     * 
     * @param show true to show the additional instructions; false to only show the
     *        {@link #getErrorMsg() error message}.
     */
    public void setShowAdditionalInstructions(boolean show) {
        this.showAdditionalInstructions = show;
    }

    /**
     * Flag to indicate if the {@link #additionalInstructions} should be displayed along with the
     * {@link #getErrorMsg() error message}.
     * 
     * @return true to show the additional instructions; false to only show the
     *         {@link #getErrorMsg() error message}.
     */
    public boolean isShowAdditionalInstructions() {
        return showAdditionalInstructions;
    }

    /**
     * Jumps to the {@link #modelObject}
     */
    public void jumpToEvent() {
        if (getModelObject() != null) {
            fireJumpToEvent(getModelObject());
        }
    }

    /**
     * Fires the jump event for the provided model object
     * 
     * @param modelObject the model object to jump to
     */
    protected abstract void fireJumpToEvent(Serializable modelObject);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getErrorMsg() == null) ? 0 : getErrorMsg().hashCode());
        result = prime * result + (isValid() ? 1231 : 1237);
        result = prime * result + ((modelObject == null) ? 0 : modelObject.hashCode());
        result = prime * result + ((additionalInstructions == null) ? 0 : additionalInstructions.hashCode());
        result = prime * result + (showAdditionalInstructions ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        // Check that the object to compare is of this type
        if (!(obj instanceof ModelValidationStatus)) {
            return false;
        }

        ModelValidationStatus otherContainer = (ModelValidationStatus) obj;

        // First check the container reference
        if (this == otherContainer) {
            return true;

            /* Then check if the containers have the same model object and error message (excluding
             * blank) */
        } else if (getModelObject() == otherContainer.getModelObject() && StringUtils.isNotBlank(getErrorMsg())
                && StringUtils.equals(getErrorMsg(), otherContainer.getErrorMsg())) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ModelValidationStatus: ");
        sb.append("errorMsg = '").append(getErrorMsg()).append("'");
        sb.append(", valid = ").append(isValid());
        sb.append(", widget = ").append(modelObject != null ? modelObject.getClass().getSimpleName() : null);
        sb.append(", additionalInstructions = '").append(getAdditionalInstructions()).append("'");
        sb.append(", isShowAdditionalInstructions = ").append(isShowAdditionalInstructions());
        sb.append("]");
        return sb.toString();
    }
}
