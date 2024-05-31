/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class is used to indicate that the learner has initiated a 'tutor me'
 * conversation.
 * 
 * @author mhoffman
 *
 */
public class TutorMeLearnerTutorAction extends AbstractLearnerTutorAction {

    /**
     * Set attribute
     * 
     * @param learnerAction authored learner action information related to this tutor action.  Can be null if there
     * is no authored information to provide here.
     */
    public TutorMeLearnerTutorAction(generated.dkf.LearnerAction learnerAction){
        super(learnerAction);
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[TutorMeLearnerTutorAction: ");
        sb.append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
