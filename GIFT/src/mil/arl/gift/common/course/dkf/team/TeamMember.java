/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.team;

import java.io.Serializable;

/**
 * A member of a {@link Team team} that represents a role to be assigned to a learner in a training application 
 * based on a scenario's hierarchy of team roles (i.e. its team organization).
 * 
 * @author nroberts
 *
 * @param <T> The type of identifying object that is used to determine which team role corresponds to 
 * which learner in a training application
 */
public class TeamMember<T extends Serializable> extends AbstractTeamUnit{


    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** The identifying object that is used to determine which learner this team member role should be applied to */
    private T identifier;
    
    /**
     * This is useful when authors might want to include entities in the team organization that can be assessed 
     * like assessing a vehicle's speed which is driven by a soldier entity played by a learner.
     */
    private boolean isPlayable = true;
    
    /**
     * Required for GWT serialization
     */
    protected TeamMember(){}

    /**
     * Creates a new team member that is identified by the given name and uses the given identifier object 
     * to determine which learner this team member's role should be applied to
     * 
     * @param name the unique name identifying this team member. Cannot be null.
     * @param identifier an identifying object that is used to determine which learner this team member role
     * should be applied to. Cannot be null.
     */
    protected TeamMember(String name, T identifier) {
        super(name);
        
        if(identifier == null) {
            throw new IllegalArgumentException("The identifier object used to assign this team member to a learner cannot be null.");
        }
        
        this.identifier = identifier;
    }
    
    /**
     * Gets the identifying object that is used to determine which learner this team member role should be applied to
     * 
     * @return the identifying object
     */
    public T getIdentifier() {
        return identifier;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (identifier == null ? 0 : identifier.hashCode());
        return result;
    }

    /**
     * Return whether this team member is playable.</br>
     * This is useful when authors might want to include entities in the team organization that can be assessed 
     * like assessing a vehicle's speed which is driven by a soldier entity played by a learner.
     * 
     * @return the default is true.
     */
    public boolean isPlayable() {
        return isPlayable;
    }

    /**
     * Set whether this team member is playable.</br>
     * This is useful when authors might want to include entities in the team organization that can be assessed 
     * like assessing a vehicle's speed which is driven by a soldier entity played by a learner.
     * 
     * @param isPlayable false if a learner should not be able to select this team member.
     */
    public void setPlayable(boolean isPlayable) {
        this.isPlayable = isPlayable;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) 
            return true; 
        if (!super.equals(obj)) 
            return false;
        if (getClass() != obj.getClass()) 
            return false;
        TeamMember<?> other = (TeamMember<?>) obj;
        if (identifier == null) {
            if (other.identifier != null) 
                return false; 
        } else if (!identifier.equals(other.identifier)) 
            return false; 
        return true;
    }
    
   
    
    @Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        sb.append("[TeamMember: ");
        sb.append("name = ").append(getName());
        sb.append(", playable = ").append(isPlayable());
        sb.append(", identifier = ").append(identifier);
        sb.append("]");

        return sb.toString();
    }
}
