/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.validation;

import java.util.Set;

/**
 * Interface for widgets that contain validation. Forces good practice for validation handling.
 * 
 * @author sharrison
 */
public interface HasValidation {
    
    /**
     * Retrieves a complete list of validation status containers for all widgets that will undergo
     * validation.
     * 
     * @param validationStatuses The list of widget validation statuses. This list should be
     *        populated with a {@link ValidationStatus container} for each widget that is checked
     *        for validation.
     */
    public void getValidationStatuses(Set<ValidationStatus> validationStatuses);

    /**
     * Validates the component that is referenced by the provided container's validation object.
     * This method will update the validity flag of the provided container.
     * 
     * @param validationStatus the {@link ValidationStatus} container that is used to populate the
     *        {@link ValidationWidget}. If null, nothing will change.
     */
    public void validate(ValidationStatus validationStatus);
}
