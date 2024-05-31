/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.team;

import mil.arl.gift.common.coordinate.AbstractCoordinate;

/**
 * A {@link TeamMember team member} whose assigned learner is identified by its starting location 
 * in a training application.
 * 
 * @author nroberts
 */
public class LocatedTeamMember extends TeamMember<AbstractCoordinate>{
    
    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private LocatedTeamMember(){}

    /**
     * Creates a new team member that is identified by the given name and assigned to the learner 
     * with the given starting location in a training application.
     * 
     * @param name the unique name identifying this team member. Cannot be null.
     * @param identifier the starting location of the learner that this team member's role should 
     * be assigned to. Cannot be null.
     */
    public LocatedTeamMember(String name, AbstractCoordinate identifier) {
        super(name, identifier);
    }

}
