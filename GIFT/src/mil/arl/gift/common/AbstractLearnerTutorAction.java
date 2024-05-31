/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.common.course.dkf.team.TeamMember;

/**
 * This is the base class for learner tutor actions (e.g. report completed, used radio) data classes and contains the common attributes.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractLearnerTutorAction {
    
    /** authored learner action information */
    protected generated.dkf.LearnerAction learnerAction;
    
    /** the team member performing this learner action. */
    private TeamMember<?> teamMember;
    
    /**
     * Set attribute.
     * 
     * @param learnerAction authored learner action information.  Can be null.
     */
    public AbstractLearnerTutorAction(generated.dkf.LearnerAction learnerAction){
       setLearnerAction(learnerAction);  
    }
    
    /**
     * Set the team member for the learner performing the learner action.  
     * 
     * @param teamMember can be null when not in a team knowledge session.
     */
    public void setTeamMember(TeamMember<?> teamMember){        
        this.teamMember = teamMember;
    }
    
    /**
     * Return the team member for the learner performing the learner action.  
     * 
     * @return can be null when not in a team knowledge session.
     */
    public TeamMember<?> getTeamMember(){
        return teamMember;
    }
    
    /**
     * Set attribute.
     * 
     * @param learnerAction authored learner action information.  Can be null.
     */
    private void setLearnerAction(generated.dkf.LearnerAction learnerAction){        
        this.learnerAction = learnerAction;
    }
    
    /**
     * Return authored learner action information for this learner action.
     * 
     * @return can be null
     */
    public generated.dkf.LearnerAction getLearnerAction(){
        return learnerAction;
    }

    @Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        sb.append("teamMember = ").append(teamMember);
        sb.append(", learnerAction = ").append(learnerAction);
        return sb.toString();
    }

}
