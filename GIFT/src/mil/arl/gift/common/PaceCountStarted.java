/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents the learner tutor action of starting a pace count
 * 
 * @author nroberts
 *
 */
public class PaceCountStarted extends AbstractLearnerTutorAction {

    /**
     * Set attribute
     * 
     * @param learnerAction authored learner action information related to this tutor action.  Can be null if there
     * is no authored information to provide here.
     */
    public PaceCountStarted(generated.dkf.LearnerAction learnerAction){
        super(learnerAction);
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[PaceCountStarted: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
