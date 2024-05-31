/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.team;

import java.io.Serializable;

import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.util.StringUtils;

/**
 * A unit (i.e. a sub-team or team member) that can belong to a {@link Team team}.
 *
 * @author nroberts
 */
public abstract class AbstractTeamUnit implements Serializable{

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** The unique name identifying this unit */
    private String name;

    /** The team to which this unit belongs. Null if it is the root team. */
    private Team parentTeam;

    /**
     * the current entity identifier for this team member which comes from
     * entity state messages during real time assessment
     */
    private EntityIdentifier entityIdentifier;

    /**
     * Required for GWT serialization
     */
    public AbstractTeamUnit(){}

    /**
     * Creates a new team unit with the given identifying name
     *
     * @param name the unique name identifying this unit. Cannot be null.
     */
    protected AbstractTeamUnit(String name) {
        setName(name);
    }

    /**
     * Gets the unique name identifying this unit
     *
     * @return this unit's unique name. Cannot be null.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for the name.
     *
     * @param name The new value of {@link #name}. Cannot be null.
     */
    public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("The team unit name cannot be null");
        }

        this.name = name;
    }

    /**
     * Getter for the team.
     *
     * @return The value of {@link #parentTeam}.
     */
    public Team getParentTeam() {
        return parentTeam;
    }

    /**
     * Determines the (zero-based) depth of this unit in the entire team
     * hierarchy.
     * 
     * <pre>
     * For example in this team structure, Member 1.2.1 would have a depth of 2.
     *  
     * Team 1          [d=0]
     * - Member 1.1    [d=1]
     * - Team 1.2      [d=1]
     * -- Member 1.2.1 [d=2]
     * 
     * </pre>
     * 
     * @return the depth level of this unit in the team hierarchy.
     */
    public int getTeamDepth() {
        int depth = 0;

        AbstractTeamUnit teamUnit = this;
        while (teamUnit.getParentTeam() != null) {
            depth++;
            teamUnit = teamUnit.getParentTeam();
        }

        return depth;
    }

    /**
     * Setter for the team.
     *
     * @param team The new value of {@link #parentTeam}.
     */
    void setParentTeam(Team team) {
        this.parentTeam = team;
    }

    /**
     * Return the entity identifier for this team member.
     *
     * @return can be null if the entity identifier hasn't been found yet
     */
    public EntityIdentifier getEntityIdentifier() {
        return entityIdentifier;
    }

    /**
     * Set the entity identifier for this team member.
     *
     * @param entityIdentifier can be null.
     */
    public void setEntityIdentifier(EntityIdentifier entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        AbstractTeamUnit other = (AbstractTeamUnit) obj;

        if (!StringUtils.equals(name, other.name)) {
            return false;
        }

        return true;
    }

    @Override
    public abstract String toString();
}
