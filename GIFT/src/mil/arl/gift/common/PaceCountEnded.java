/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents the learner tutor action of ending a pace count
 * 
 * @author nroberts
 *
 */
public class PaceCountEnded extends AbstractLearnerTutorAction {

    /**
     * Set attribute
     * 
     * @param learnerAction authored learner action information related to this tutor action.  Can be null if there
     * is no authored information to provide here.
     */
    public PaceCountEnded(generated.dkf.LearnerAction learnerAction){
        super(learnerAction);
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[PaceCountEnded: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
