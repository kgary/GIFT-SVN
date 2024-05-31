/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.course;

import generated.course.TrainingApplicationWrapper;
import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

/**
 * Response containing the imported training application from Gift Wrap
 * 
 * @author bzahid
 */
public class ImportTrainingApplicationObjectResult extends GatServiceResult {

    /** The training application wrapper from the imported object */
    private TrainingApplicationWrapper taWrapper;

    /** The flag that indicates if the import found a naming conflict */
    private boolean foundConflict = false;

    /**
     * No-arg constructor. Needed for serialization.
     */
    public ImportTrainingApplicationObjectResult() {
    }

    /**
     * Sets the training application wrapper
     * 
     * @param taWrapper the training application wrapper
     */
    public void setTaWrapper(TrainingApplicationWrapper taWrapper) {
        this.taWrapper = taWrapper;
    }

    /**
     * Gets the training application wrapper
     * 
     * @return the training application wrapper
     */
    public TrainingApplicationWrapper getTaWrapper() {
        return taWrapper;
    }

    /**
     * Sets the flag that indicates if the import found a naming conflict
     * 
     * @param foundConflict true to indicate that a conflict was found; false otherwise.
     */
    public void setFoundConflict(boolean foundConflict) {
        this.foundConflict = foundConflict;
    }

    /**
     * Gets the flag that indicating if the import found a naming conflict
     * 
     * @return true to indicate that a conflict was found; false otherwise.
     */
    public boolean isFoundConflict() {
        return foundConflict;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[ImportTrainingApplicationObjectResult: ");
        sb.append(super.toString());
        sb.append(", taWrapper = ").append(getTaWrapper());
        sb.append(", foundConflict = ").append(isFoundConflict());
        sb.append("]");

        return sb.toString();
    }
}
