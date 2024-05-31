/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.team;
 
/**
 * A {@link TeamMember team member} whose assigned learner is identified by a unique marker 
 * given to an entity in a training application.
 * 
 * @author nroberts
 */
public class MarkedTeamMember extends TeamMember<String>{
    
    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private MarkedTeamMember(){}

    /**
     * Creates a new team member that is identified by the given name and assigned to the learner 
     * with the given entity marker in a training application.
     * 
     * @param name the unique name identifying this team member. Cannot be null.
     * @param identifier the entity marker of the learner that this team member's role should 
     * be assigned to. Cannot be null.
     */
    public MarkedTeamMember(String name, String identifier) {
        super(name, identifier);
    }

}
