/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest.ACTION_TYPE;

/**
 * Exception to handle failures from a manage team membership request.  Not all failures
 * are critical, but we want to present a nice message to the user.  For example if the
 * user tries to select a team role that is already taken, a non technical message
 * should be displayed that informs the user that the role is already taken.
 * 
 * @author nblomberg
 *
 */
public class ManageTeamMembershipException extends Exception {

    /**
     * default serial version id
     */
    private static final long serialVersionUID = 1L;
    
    /** The Membership Request action type that was attempted. */
    ACTION_TYPE action;
    
    /** The friendly user message to display to the user. */
    String friendlyMessage = "";
    

    public ManageTeamMembershipException(ACTION_TYPE action, String friendlyMessage){
        this.friendlyMessage = friendlyMessage;
        this.action = action;
    }
    
   
    /**
     * Get the the membership request action that was attempted that triggered the exception.
     * 
     * @return The membership request action that triggered the exception.
     */
    public ACTION_TYPE getAction() {
        return action;
    }
    

    /**
     * Get a friendly message that can be displayed to the user.
     * 
     * @return The friendly message that can be displayed to the user.
     */
    public String getFriendlyMessage(){
        return friendlyMessage;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ManageTeamMembershipException: ");
        sb.append(" action = ").append(getAction());
        sb.append(", message = ").append(getFriendlyMessage());
        sb.append("]");
        return sb.toString();
    }
}
