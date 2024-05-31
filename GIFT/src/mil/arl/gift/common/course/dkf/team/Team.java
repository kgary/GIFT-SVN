/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.team;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.util.StringUtils;

/**
 * A team that can contain multiple sub-teams and {@link TeamMember team members}. Used to establish a hierarchy of roles
 * in a scenario that can be applied to learner entities in a training application.
 *
 * @author nroberts
 */
public class Team extends AbstractTeamUnit{

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** The units (i.e. sub-teams and team members) that make up this team */
    private List<AbstractTeamUnit> units;

    /**
     * The team member names for all team members in this team (recursively).
     */
    private Set<String> teamMemberNames = new HashSet<>();
    
    /**
     * The team member names that are playable in this team (recursively).
     */
    private Set<String> playableTeamMemberNames = new HashSet<>();

    /**
     * The echelon value of this team. Can be null if unspecified.
     */
    private EchelonEnum echelon;

    /**
     * The team member identifiers for all team members in this team
     * (recursively).
     */
    private Set<Serializable> teamMemberIdentifiers = new HashSet<>();

    /**
     * The {@link Set} of {@link TeamMember} in this {@link Team}. Includes both
     * direct and indirect descendants.
     */
    private Set<TeamMember<?>> teamMembers = new HashSet<>();

    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private Team(){}

    /**
     * Creates a new team that is identified by the given unique name and made up by the
     * given team units (i.e sub-teams and team members)
     *
     * @param name the unique name identifying this team. Cannot be null.
     * @param echelon the echelon value of the team. Can be null if unspecified.
     * @param units the units (i.e. sub-teams and team members) that make up this team. Cannot be null.
     */
    public Team(String name, EchelonEnum echelon, List<AbstractTeamUnit> units) {
        super(name);

        setUnits(units);
        setEchelon(echelon);
        findTeamMembers(teamMembers, teamMemberNames, playableTeamMemberNames, teamMemberIdentifiers);
    }

    /**
     * Find the complete collection of team member names under this team. This
     * includes team members within sub-teams as well.
     *
     * @param teamMembers The {@link Set} of {@link TeamMember} to populate.
     *        Can't be null.
     * @param teamMemberNames The {@link Set} of team member names to populate.
     *        Can't be null.
     * @param playableTeamMemberNames The (@link Set} of team member names that are playable to populate.
     *        Can't be null.
     * @param teamMemberIdentifiers The {@link Set} of {@link Serializable} team
     *        member identifiers to populate. Can't be null.
     */
    private void findTeamMembers(Set<TeamMember<?>> teamMembers, Set<String> teamMemberNames, 
            Set<String> playableTeamMemberNames, Set<Serializable> teamMemberIdentifiers) {
        for (AbstractTeamUnit teamUnit : units) {
            if (teamUnit instanceof TeamMember) {
                final TeamMember<?> teamMember = (TeamMember<?>) teamUnit;
                teamMembers.add(teamMember);
                teamMemberIdentifiers.add(teamMember.getIdentifier());
                teamMemberNames.add(teamMember.getName());
                
                if(teamMember.isPlayable()){
                    playableTeamMemberNames.add(teamMember.getName());
                }
            } else {
                final Team subTeam = (Team) teamUnit;
                subTeam.findTeamMembers(teamMembers, teamMemberNames, playableTeamMemberNames, teamMemberIdentifiers);
            }
        }
    }

    /**
     * Gets the units (i.e. sub-teams and team members) that make up this team
     *
     * @return the units that make up this team. Cannot be null.
     */
    public List<AbstractTeamUnit> getUnits(){
        return Collections.unmodifiableList(units);
    }

    /**
     * Setter for the {@link AbstractTeamUnit} objects that belong to this
     * {@link Team}.
     *
     * @param units The new value of {@link #units}. Can't be null.
     */
    private void setUnits(List<AbstractTeamUnit> units) {
        if (units == null) {
            throw new IllegalArgumentException("The set of child team units cannot be null.");
        }

        this.units = units;
        for (AbstractTeamUnit unit : units) {
            unit.setParentTeam(this);
        }
    }

    /**
     * Getter for all {@link TeamMember} objects of this {@link Team}. Includes
     * both direct and indirect descendants.
     *
     * @return The {@link Set} of {@link TeamMember}. Can't be null or modified.
     */
    public Set<TeamMember<?>> getTeamMembers() {
        return Collections.unmodifiableSet(teamMembers);
    }

    /**
     * Returns the team member names for all team members in this team
     * (recursively).
     *
     * @return The {@link Set} of team member names. Can't be null or modified.
     */
    public Set<String> getTeamMemberNames() {
        return Collections.unmodifiableSet(teamMemberNames);
    }
    
    /**
     * Returns the team member names for all team members that are playable in this team
     * (recursively).
     *
     * @return The {@link Set} of playable team member names. Can't be null or modified.
     */
    public Set<String> getPlayableTeamMemberNames(){
        return Collections.unmodifiableSet(playableTeamMemberNames);
    }

    /**
     * Gets the echelon value of this team. Can be null if unspecified.
     *
     * @return the echelon value of the team
     */
    public EchelonEnum getEchelon() {
        return echelon;
    }

    /**
     * Sets the echelon value for this team. Can be null if unspecified.
     *
     * @param echelon the echelon to set
     */
    private void setEchelon(EchelonEnum echelon) {
        this.echelon = echelon;
    }

    /**
     * Returns the team member identifiers for all team members in this team
     * (recursively).
     *
     * @return The {@link Set} of team member identifiers. Can't be null or
     *         modified.
     */
    public Set<Serializable> getTeamMemberIdentifiers() {
        return Collections.unmodifiableSet(teamMemberIdentifiers);
    }

    /**
     * Return the total number of team members under this team. This includes
     * team members within sub-teams as well.
     *
     * @return the total number of team members found under this team level.  Includes both playable and non-playable.
     */
    public int getNumberOfTeamMembers() {
        return teamMemberNames.size();
    }
    
    /**
     * Return the total number of team members under this team that are playable.
     * This includes team members within sub-teams as well.
     * 
     * @return the total number of team members found under this team level that are
     * marked as playable.
     */
    public int getNumberOfPlayableTeamMembers(){
        return playableTeamMemberNames.size();
    }

    /**
     * Return the first playable team member found in the team. This is useful when
     * there is only one playable team member and that team member should be the learner being assessed.
     *
     * @return the first playable team member found in this team.  Can be null if there are no playable team members..
     */
    public TeamMember<?> getFirstPlayableTeamMember(){

        for(AbstractTeamUnit teamUnit : units){

            if(teamUnit instanceof TeamMember){
                if(((TeamMember<?>)teamUnit).isPlayable()){
                    return (TeamMember<?>) teamUnit;
                }
            }else{
                TeamMember<?> teamMember = ((Team)teamUnit).getFirstPlayableTeamMember();
                if(teamMember != null){
                    return teamMember;
                }
            }
        }

        return null;
    }

    /**
     * Return the team unit object for the given name.
     *
     * @param teamElementName the name to find in this team hierarchy.
     * @return the team unit object whose name matches the name provided (ignores case sensitivity).
     * If the name provided is null or empty, null is returned.
     */
    public AbstractTeamUnit getTeamElement(String teamElementName){

        if(StringUtils.isBlank(teamElementName)){
            return null;
        }else if(this.getName().equalsIgnoreCase(teamElementName)){
            return this;
        }

        for(AbstractTeamUnit teamUnit : units){

            if(teamUnit.getName().equalsIgnoreCase(teamElementName)){
                return teamUnit;
            }else if(teamUnit instanceof Team){
                AbstractTeamUnit foundElement = ((Team)teamUnit).getTeamElement(teamElementName);
                if(foundElement != null){
                    return foundElement;
                }
            }
        }

        return null;
    }

    /**
     * Return the team unit object for the given entity marking.
     *
     * @param entityMarking the entity marking to find in this team hierarchy.
     * @return the team unit object whose entity marking matches the one
     *         provided (case sensitive). If the entity marking is null or
     *         empty, null is returned.
     */
    public TeamMember<?> getTeamElementByEntityMarking(String entityMarking) {
        if (StringUtils.isBlank(entityMarking)) {
            return null;
        }

        for (AbstractTeamUnit teamUnit : units) {
            if (teamUnit instanceof TeamMember<?>) {
                TeamMember<?> member = (TeamMember<?>) teamUnit;
                if (member.getIdentifier().equals(entityMarking)) {
                    return member;
                }
            } else if (teamUnit instanceof Team) {
                Team team = (Team) teamUnit;
                TeamMember<?> possibleResult = team.getTeamElementByEntityMarking(entityMarking);
                if (possibleResult != null) {
                    return possibleResult;
                }
            }
        }

        return null;
    }

    /**
     * Return the first team member in this team to have an entity marking that
     * matches the one provided.
     *
     * @param entityMarking the entity marking to check against all team member
     *        entity marking values.
     * @return the team member found with the same entity marking as the one
     *         provided. Can be null.
     */
    public MarkedTeamMember getTeamMemberByEntityMarking(String entityMarking) {

        if (entityMarking == null) {
            return null;
        }

        for (AbstractTeamUnit teamUnit : units) {

            if (teamUnit instanceof MarkedTeamMember) {
                MarkedTeamMember markedTeamMember = (MarkedTeamMember) teamUnit;
                if (entityMarking.equalsIgnoreCase(markedTeamMember.getIdentifier())) {
                    return markedTeamMember;
                }
            } else if (teamUnit instanceof Team) {
                MarkedTeamMember foundTeamMember = ((Team) teamUnit).getTeamMemberByEntityMarking(entityMarking);
                if (foundTeamMember != null) {
                    return foundTeamMember;
                }
            }
        }

        return null;
    }

    /**
     * Searches for an {@link AbstractTeamUnit} within this {@link Team}
     * identified by a provided {@link EntityIdentifier}.
     *
     * @param entityId The {@link EntityIdentifier} of the
     *        {@link AbstractTeamUnit} to find. Can't be null.
     * @return The {@link AbstractTeamUnit} within this {@link Team} that is
     *         identified by the provided identifier. Can be null if no matching
     *         {@link AbstractTeamUnit} was found. Can be this {@link Team}
     *         object if the {@link EntityIdentifier} matches.
     */
    public AbstractTeamUnit getTeamElementByEntityId(EntityIdentifier entityId) {
        if (entityId == null) {
            throw new IllegalArgumentException("The parameter 'entityId' cannot be null.");
        }

        if (entityId.equals(getEntityIdentifier())) {
            return this;
        }

        for (AbstractTeamUnit subUnit : units) {
            if (subUnit instanceof TeamMember<?>) {
                TeamMember<?> member = (TeamMember<?>) subUnit;
                if (entityId.equals(member.getEntityIdentifier())) {
                    return member;
                }
            } else if (subUnit instanceof Team) {
                Team subTeam = (Team) subUnit;
                AbstractTeamUnit possibleResult = subTeam.getTeamElementByEntityId(entityId);
                if (possibleResult != null) {
                    return possibleResult;
                }
            }
        }

        return null;
    }

    /**
     * Return whether this team has at least one playable team member somewhere as a descendant
     * to this team.
     *
     * @return true if there is at least one playable team member under this team.
     */
    public boolean hasPlayableTeamMember(){

        for(AbstractTeamUnit teamUnit : units){

            if(teamUnit instanceof TeamMember<?>){
                TeamMember<?> teamMember = (TeamMember<?>)teamUnit;
                if(teamMember.isPlayable()){
                    return true;
                }
            }else if(teamUnit instanceof Team){
                if(((Team)teamUnit).hasPlayableTeamMember()){
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[Team: ");
        sb.append("name = ").append(getName());
        sb.append(", echelon = ").append(getEchelon() != null ? getEchelon().getDisplayName() : "none");
        sb.append(", units = ").append(units);
        sb.append("]");

        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (units == null ? 0 : units.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        Team other = (Team) obj;
        if (units == null) {
            if (other.units != null)
                return false;
        } else if (!units.equals(other.units))
            return false;
        return true;
    }
}
