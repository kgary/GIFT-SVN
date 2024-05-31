/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.team;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the team organization for a real time assessment.
 * 
 * @author mhoffman
 *
 */
public class TeamOrganization {
    
    public static final String DEFAULT_TEAM_OF_ONE_TEAM_NAME = "Team of One";
    public static final String DEFAULT_LEARNER_TEAM_MEMBER_NAME = "learner";

    /** the root of the team structure */
    private Team rootTeam;
    
    /** the team member associated with the learner for this assessment. Can be null. */
    private TeamMember<?> learnerTeamMember;
    
    /**
     * mapping of unique team organization element name to the team unit representing that authored element.
     */
    private Map<String, AbstractTeamUnit> teamElementNameMap = new HashMap<>();
    
    /**
     * Set the root team for the team organization.
     * 
     * @param rootTeam can't be null, must contain at least one team member.
     */
    public TeamOrganization(Team rootTeam){
        
        if(rootTeam == null){
            throw new IllegalArgumentException("The team can't be null");
        }else if(rootTeam.getNumberOfTeamMembers() == 0){
            throw new IllegalArgumentException("There must be at least 1 team member in the team organization");
        }
        
        this.rootTeam = rootTeam;
        
        initialize();
    }
    
    /**
     * Build any helper maps for this team organization.
     */
    private void initialize(){
        
        buildMap(rootTeam);
    }
    
    /**
     * Recursively walk the team hierarchy, adding to the team element map along the way.
     * 
     * @param team the current team level to capture direct descendants of and place in the map. Can't be null.
     */
    private void buildMap(Team team){
        
        for(AbstractTeamUnit teamUnit : team.getUnits()){
            
            if(teamElementNameMap.containsKey(teamUnit.getName())){
                throw new IllegalArgumentException("Found duplicate team element name of '"+teamUnit.getName()+"' in team organization.");
            }
            
            teamElementNameMap.put(teamUnit.getName(), teamUnit);
            
            if(teamUnit instanceof Team){
                buildMap((Team) teamUnit);
            }
        }
    }
    
    /**
     * Return the team element that has the unique team element name.
     * 
     * @param teamElementName unique team element name to use as a lookup key for the corresponding team object.
     * @return the team object mapped to the name provided.  Can be null if there is no mapping.
     */
    public AbstractTeamUnit getTeamElementByName(String teamElementName){
        return teamElementNameMap.get(teamElementName);
    }
    
    /**
     * Set the team member associated with the learner for this assessment.
     * 
     * @param learnerTeamMember can't be null.
     */
    public void setLearnerTeamMember(TeamMember<?> learnerTeamMember){
        
        if(learnerTeamMember == null){
            throw new IllegalArgumentException("The learner team member can't be null");
        }
        
        this.learnerTeamMember = learnerTeamMember;
    }
    
    /**
     * Return the team member associated with the learner for this assessment.
     * 
     * @return can be null if not set yet.
     */
    public TeamMember<?> getLearnerTeamMember(){
        return learnerTeamMember;
    }
    
    /**
     * Return the root team of the team organization.
     * 
     * @return won't be null.
     */
    public Team getRootTeam(){
        return rootTeam;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[TeamOrganization: ");
        builder.append("learnerTeamMember = ");
        builder.append(learnerTeamMember);
        builder.append(", rootTeam = ");
        builder.append(rootTeam);
        builder.append("]");
        return builder.toString();
    }
    
    
}
