/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents the learner tutor action of manually applying an instructional strategy
 * 
 * @author mhoffman
 */
public class ApplyStrategyLearnerAction extends AbstractLearnerTutorAction {

    /**
     * constructor - set attribute
     * @param learnerAction - contains the authored apply strategy learner action which has
     * a reference to the instructional strategy to apply
     */
    public ApplyStrategyLearnerAction(generated.dkf.LearnerAction learnerAction){
        super(learnerAction);
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[ApplyStrategyLearnerAction: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }
}
