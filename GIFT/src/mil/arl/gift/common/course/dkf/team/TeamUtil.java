/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.team;

import java.util.List;

import javax.vecmath.Point3d;

import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.StringUtils;

/**
 * Utility class for common team methods such as finding team members
 * by start location. 
 * 
 * @author nblomberg
 *
 */
public class TeamUtil {    
    
    /** a distance to use when checking learner starting location for the scenario. */
    public static final double START_RADIUS = 1.0;
    
    /**
     * Constructor - private
     */
    private TeamUtil() {}     
    
    /**
     * Return the first team member in this team to have either a start location near the provided
     * entity state current location or the entity state's entity marking is a match.     * 
     * 
     * @param entityState the current state of an entity to try to find a team member in the team org for.  Can't be null.
     * @param cachedTeamPoint an existing point3d object that the team member coordinates will be converted into
     * This is used so that new point objects don't need to be created in cases where performance needs to be considered
     * @param rootTeam the root team of the team organization, used to search for team members.
     * @return the team member found by either location or entity marking match.
     * Can be null.
     */
    public static TeamMember<?> getTeamMemberByEntityState(EntityState entityState, Point3d cachedTeamPoint, Team rootTeam){
        
        TeamMember<?> foundTeamMember = TeamUtil.getTeamMemberByStartLocation(entityState, TeamUtil.START_RADIUS, cachedTeamPoint,
                rootTeam.getUnits());
        if(foundTeamMember == null && entityState.getEntityMarking() != null){
            foundTeamMember = getTeamMemberByEntityMarking(entityState.getEntityMarking().getEntityMarking(), rootTeam);
        }
        
        return foundTeamMember;
    }
    
    /**
     * Return the first team member in this team that has the entity marking.
     * 
     * @param entityMarking a unique string that identifies this entity in training application state message.  If 
     * null or empty this method returns null.
     * @param rootTeam the root team of the team organization, used to search for team members.
     * @return the team member found by entity marking match.  Can be null.
     */
    public static TeamMember<?> getTeamMemberByEntityMarking(String entityMarking, Team rootTeam){
        
        TeamMember<?> foundTeamMember = null;
        if(StringUtils.isNotBlank(entityMarking)){
            foundTeamMember = rootTeam.getTeamMemberByEntityMarking(entityMarking);
        }
        return foundTeamMember;
    }
   
    /**
     * Return the first team member in this team to have a defined learner start location
     * within the specified threshold distance of the location provided.
     * 
     * @param entityState state information for an entity in a training application to check against all team member learner locations
     * @param thresholdDistance the allowed distance the pre-defined learner start location
     * can be to be considered a match.
     * @param convertedPoint an existing point3d object that the team member coordinates will be converted into
     * This is used so that new point objects don't need to be created in cases where performance needs to be considered
     * @return the team member found with the start location close enough to the location provided.
     * Can be null.
     */
    public static LocatedTeamMember getTeamMemberByStartLocation(EntityState entityState, double thresholdDistance, Point3d convertedPoint,
            List<AbstractTeamUnit> units){
        
        if(entityState == null){
            return null;
        }else if(entityState.getLocation() == null){
            return null;
        }else if(thresholdDistance < 0.0){
            return null;
        }
        
        for(AbstractTeamUnit teamUnit : units){
            
            if(teamUnit instanceof LocatedTeamMember){
                LocatedTeamMember locatedTeamMember = (LocatedTeamMember)teamUnit;
                
                if(locatedTeamMember.getEntityIdentifier() != null &&
                        entityState.getEntityID().equals(locatedTeamMember.getEntityIdentifier())){
                    // the entity state provided matches a team member that was already compared by a learner id (start location or entity marking)
                    // therefore no need to check the start location distance again.
                    return locatedTeamMember;
                }else{
                
                    CoordinateUtil.getInstance().convertIntoPoint(locatedTeamMember.getIdentifier(), convertedPoint);
                    if(locatedTeamMember.getEntityIdentifier() == null && 
                            entityState.getLocation().distance(convertedPoint) < thresholdDistance){
                        // location based identification can only happen once in a scenario since the entity can
                        // move away from the start location and other entities can reach that start location which
                        // would then make it impossible to determine which entity is the learner's based on location alone later on.
                        return locatedTeamMember;
                    }
                }
            }else if(teamUnit instanceof Team){
                LocatedTeamMember foundTeamMember = getTeamMemberByStartLocation(entityState, thresholdDistance, convertedPoint, ((Team)teamUnit).getUnits());
                if(foundTeamMember != null){
                    return foundTeamMember;
                }
            }
        }
        
        return null;
    }
     

}
