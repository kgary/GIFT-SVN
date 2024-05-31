/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import generated.dkf.LearnerAction;

/**
 * Represents the Assess My location learner action event normally caused by the learner pressing
 * the corresponding learner action button in the TUI.
 * 
 * @author mhoffman
 *
 */
public class AssessMyLocationTutorAction extends AbstractLearnerTutorAction {

    /**
     * Set attribute.
     * 
     * @param learnerAction authored learner action information related to this tutor action.  Can be null if there
     * is no authored information to provide here.
     */
    public AssessMyLocationTutorAction(LearnerAction learnerAction) {
        super(learnerAction);

    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[AssessMyLocationTutorAction: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
