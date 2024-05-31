/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents a learner tutor action that has been completed by the learner on the tutor.
 * 
 * @author mhoffman
 *
 */
public class LearnerTutorAction {
    
    /** the action contents */
    private AbstractLearnerTutorAction actionData;

    /**
     * Class constructor
     * 
     * @param actionData - the report contents.  Can't be null.
     */
    public LearnerTutorAction(AbstractLearnerTutorAction actionData){
        
        if(actionData == null){
            throw new IllegalArgumentException("The actionData can't be null");
        }
        this.actionData = actionData;
    }
    
    /**
     * Get the data about the learner tutor action
     * 
     * @return the action data, won't be null.
     */
    public AbstractLearnerTutorAction getAction(){
        return actionData;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("[LearnerTutorAction: "); 
        sb.append("action = ").append(getAction());
        sb.append("]");

        return sb.toString();
    }
}
