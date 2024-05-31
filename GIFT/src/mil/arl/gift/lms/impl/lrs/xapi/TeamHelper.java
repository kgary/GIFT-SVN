package mil.arl.gift.lms.impl.lrs.xapi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.util.CollectionUtils;

/**
 * Utility class for handling Team Structure and Session Members within a Knowledge Session
 * 
 * @author Yet Analytics
 *
 */
public class TeamHelper {

    /**
     * Parses out all session members from an abstract knowledge session
     * 
     * @param knowledgeSession - abstract knowledge session to parse members from
     * 
     * @return list of all session members from the abstract knowledge session
     */
    public static List<SessionMember> getSessionMembers(AbstractKnowledgeSession knowledgeSession){
        List<SessionMember> sessionMembers = new ArrayList<SessionMember>();
        if(knowledgeSession instanceof IndividualKnowledgeSession) {
            for(Map.Entry<Integer, SessionMember> kv : knowledgeSession.getSessionMembers().getSessionMemberDSIdMap().entrySet()) {
                sessionMembers.add(kv.getValue());
            }
        } else if(knowledgeSession instanceof TeamKnowledgeSession) {
            for(Map.Entry<Integer, SessionMember> kv : ((TeamKnowledgeSession) knowledgeSession).getJoinedMembers().entrySet()) {
                sessionMembers.add(kv.getValue());
            }
            if(knowledgeSession.getSessionType() == SessionType.ACTIVE && !knowledgeSession.inPastSessionMode()) {
                SessionMember host = ((TeamKnowledgeSession) knowledgeSession).getHostSessionMember();
                sessionMembers.add(host);
            }   
        }
        return sessionMembers;
    }
    
    /**
     * Parse out all session memberships from session members
     * 
     * @param sessionMembers list of Session Members
     *  
     * @return list of session memberships
     */
    public static List<SessionMembership> getSessionMemberships(List<SessionMember> sessionMembers) {
        List<SessionMembership> memberships = new ArrayList<SessionMembership>();
        for(SessionMember member : sessionMembers) {
            memberships.add(member.getSessionMembership());
        }
        return memberships;
    }
    
    /**
     * Helper method for returning a possibly empty list of relevant session members for the team. Does not alter the
     * list of passed in sessionMembers.
     * 
     * @param team - Team which may contain roles assigned to session members
     * @param sessionMembers - all session members from a knowledge session
     * 
     * @return the list (possibly empty) of relevant session members.
     */
    public static List<SessionMembership> relevantSessionMembers(Team team, List<SessionMembership> sessionMembers) {
        List<SessionMembership> relevantMembers = new ArrayList<SessionMembership>();
        Set<TeamMember<?>> teamRoles = new HashSet<TeamMember<?>>();
        for(AbstractTeamUnit unit : team.getUnits()) {
            if(unit instanceof TeamMember<?>) {
                TeamMember<?> teamRole = (TeamMember<?>) unit;
                teamRoles.add(teamRole);
            }
        }
        for(SessionMembership member : sessionMembers) {
            TeamMember<?> memberRole = member.getTeamMember();
            if(memberRole != null && teamRoles.contains(memberRole)) {
                relevantMembers.add(member);
            }
        }
        return relevantMembers;
    }
    
    /**
     * determine the team roles that are not assigned to any of the passed in sessionMembers
     * 
     * @param team - Team to derive unassigned roles from
     * @param sessionMembers - all knowledge session members
     * 
     * @return possibly empty list of unassigned team member roles
     */
    public static List<TeamMember<?>> deriveUnassignedRoles(Team team, List<SessionMembership> sessionMembers) {
        List<TeamMember<?>> unassigned = new ArrayList<TeamMember<?>>();
        for(AbstractTeamUnit unit : team.getUnits()) {
            if(unit instanceof TeamMember<?>) {
                TeamMember<?> teamRole = (TeamMember<?>) unit;
                if(teamRole.isPlayable()) {
                    boolean isAssigned = false;
                    for(SessionMembership member : sessionMembers) {
                        TeamMember<?> playerRole = member.getTeamMember();
                        if(playerRole == null) {
                            continue;
                        }
                        if(playerRole.equals(teamRole)) {
                            isAssigned = true;
                            break;
                        }
                    }
                    if(!isAssigned) {
                        unassigned.add(teamRole);
                    }
                } else {
                    unassigned.add(teamRole);
                }
            }
        }
        return unassigned;
    }
    
    /**
     * Recursive search of team units, unassigned is updated in place
     *  
     * @param units - list of team children units
     * @param sessionMembers - all knowledge session members
     * @param unassigned - accumulator of unassigned team member roles
     */
    private static void deriveAllUnassignedRoles(List<AbstractTeamUnit> units, List<SessionMembership> sessionMembers, List<TeamMember<?>> unassigned) {
        for(AbstractTeamUnit unit : units) {
            if(unit instanceof Team) {
                Team team = (Team) unit;
                unassigned.addAll(deriveUnassignedRoles(team, sessionMembers));
                deriveAllUnassignedRoles(team.getUnits(), sessionMembers, unassigned);
            }
        }
    }
    
    /**
     * determine the team roles that are not assigned to any of the passed in sessionMembers across the entire
     * team structure
     * 
     * @param rootTeam - Team with potentially children teams
     * @param sessionMembers - all knowledge session members
     * 
     * @return possibly empty list of unassigned team member roles
     */
    public static List<TeamMember<?>> deriveAllUnassignedRoles(Team rootTeam, List<SessionMembership> sessionMembers){
        List<TeamMember<?>> unassigned = deriveUnassignedRoles(rootTeam, sessionMembers);
        deriveAllUnassignedRoles(rootTeam.getUnits(), sessionMembers, unassigned);
        return unassigned;
    }
    
    /**
     * does the team contain any TeamMembers assigned to any of the session members
     * 
     * @param team - team to check membership of
     * @param memberships - session members to check for
     * 
     * @return true if the team contains a role with an assigned member, false otherwise
     */
    public static boolean isRelevantTeam(Team team, List<SessionMembership> memberships) {
        return CollectionUtils.isNotEmpty(relevantSessionMembers(team, memberships));
    }
    
    /**
     * Does the team contain a team unit that is equal to the passed in member
     * 
     * @param team - team to check units of
     * @param member - unit to check for
     * 
     * @return true if found within units, false otherwise
     */
    public static boolean isRelevantTeam(Team team, TeamMember<?> member) {
        return team.getUnits().contains(member);
    }
    
    /**
     * Recursively determine if a team contains a TeamMember assigned to one of the session members
     *  
     * @param relevantTeams - accumulator of relevant teams
     * @param units - Team units to iterate over
     * @param memberships - session members
     */
    private static void findRelevantTeams(List<Team> relevantTeams, List<AbstractTeamUnit> units, List<SessionMembership> memberships) {
        for(AbstractTeamUnit unit : units) {
            if(unit instanceof Team) {
                Team team = (Team) unit;
                if(isRelevantTeam(team, memberships)) {
                    relevantTeams.add(team);
                }
                findRelevantTeams(relevantTeams, team.getUnits(), memberships);
            }
        }
    }
    
    /**
     * find all relevant teams for session members within the abstract knowledge session
     * 
     * @param session - abstract knowledge session containing team structure and members
     * 
     * @return potentially empty list of teams in which a session member is assigned
     */
    public static List<Team> findRelevantTeams(Team rootTeam, List<SessionMembership> memberships) {
        List<Team> relevantTeams = new ArrayList<Team>();
        // top level team
        if(isRelevantTeam(rootTeam, memberships)) {
            relevantTeams.add(rootTeam);
        }
        // subteams
        findRelevantTeams(relevantTeams, rootTeam.getUnits(), memberships);
        return relevantTeams;
    }
    
    /**
     * find all relevant teams from knowledge session for Team Member references
     *  
     * @param rootTeam - Top level team structure
     * @param members - collection of TeamMember to search for
     * 
     * @return possibly empty but never null list of Team
     */
    public static Set<Team> findRelevantTeams(Team rootTeam, Set<MarkedTeamMember> members){
        Set<Team> accum = new HashSet<Team>();
        for(TeamMember<?> member : members) {
            Team relevantTeam = findRelevantTeam(rootTeam, member);
            if(relevantTeam != null) {
                accum.add(relevantTeam);
            }
        }
        return accum;
    }
    
    /**
     * Recursively search for a Team that contains the member
     * 
     * @param units - Collection of AbstractTeamUnit to search
     * @param member - TeamMember to search for
     * 
     * @return The Team that contains the member or null
     */
    private static Team findRelevantTeam(List<AbstractTeamUnit> units, TeamMember<?> member) {
        for(AbstractTeamUnit unit : units) {
            if(unit instanceof Team && isRelevantTeam((Team) unit, member)) {
                return (Team) unit;
            } else if(unit instanceof Team && !isRelevantTeam((Team) unit, member)) {
                Team t = findRelevantTeam(((Team) unit).getUnits(), member);
                if(t != null) {
                    return t;
                }
            }
        }
        return null;
    }
    
    /**
     * Recursively search for a Team (starting with rootTeam) for a Team that contains the member
     * 
     * @param rootTeam - Top level team structure
     * @param member - TeamMember to search for
     * 
     * @return The Team that contains the member or null
     */
    public static Team findRelevantTeam(Team rootTeam, TeamMember<?> member) {
        if(isRelevantTeam(rootTeam, member)) {
            return rootTeam;
        }
        return findRelevantTeam(rootTeam.getUnits(), member);
    }
    
    /**
     * Parses all non-null team roles from sessionMemberships
     * 
     * @return possibly empty collection of TeamMember<?>
     */
    public static List<TeamMember<?>> relevantTeamRoles(List<SessionMembership> memberships) {
        List<TeamMember<?>> coll = new ArrayList<TeamMember<?>>();
        for(SessionMembership membership : memberships) {
            TeamMember<?> position = membership.getTeamMember();
            if(position != null) {
                coll.add(position);
            }
        }
        return coll;
    }
    
    /**
     * Search for TeamMember with entityMarking as its identifier
     * 
     * @param entityMarking - TeamMember identity to search for
     * @param members - Collection to search through
     * 
     * @return The TeamMember if found, null otherwise
     */
    public static TeamMember<?> findMatchingMember(String entityMarking, List<TeamMember<?>> members) {
        for(TeamMember<?> member : members) {
            if(member.getIdentifier().equals(entityMarking)) {
                return member;
            }
        }
        return null;
    }
    
    /**
     * Search for TeamMember within collection of Session Memberships
     * 
     * @param member - TeamMember to search for
     * @param memberships - Collection to search through
     * 
     * @return SessionMembership containing the TeamMember or null if not found
     */
    public static SessionMembership findMatchingSessionMembership(TeamMember<?> member, List<SessionMembership> memberships) {
        for(SessionMembership membership : memberships) {
            if(membership.getTeamMember() != null && 
                    (membership.getTeamMember().equals(member) ||
                            membership.getTeamMember().getName().equals(member.getName()))) {
                return membership;
            }
        }
        return null;
    }
    
    /**
     * Determines if the TeamMember ref points to the actor based on their session membership
     * 
     * @param actorSlug - Session Membership user name to search for
     * @param knowledgeSession - Knowledge Session to parse Session Memberships from
     * @param ref - TeamMember created from Environment Adaptation TeamMemberRef
     * 
     * @return true if ref corresponds to the actor, false otherwise
     */
    public static boolean isActorTeamMemberRef(String actorSlug, AbstractKnowledgeSession knowledgeSession, TeamMember<?> ref) {
        // Knowledge session required to determine alignment
        if(knowledgeSession == null || ref == null) {
            return false;
        }
        // Knowledge session memberships
        List<SessionMembership> sessionMemberships = getSessionMemberships(getSessionMembers(knowledgeSession));
        SessionMembership actorMembership = null;
        // Actor's membership
        for(SessionMembership membership : sessionMemberships) {
            if(membership.getUsername().equals(actorSlug)) {
                actorMembership = membership;
                break;
            }
        }
        // No membership for actor, not team member ref
        if(actorMembership == null) {
            return false;
        }
        TeamMember<?> actorMember = actorMembership.getTeamMember();
        // Actor is unassigned, not team member ref
        if(actorMember == null) {
            return false;
        }
        // Compare actor's TeamMember identifier to entityMarking from team member ref
        return actorMember.equals(ref);        
    }
    
    /**
     * Determines if the TeamMember ref points to any session member within the knowledge session.
     * 
     * @param knowledgeSession - Knowledge Session to parse Session Memberships from
     * @param ref - TeamMember created from Environment Adaptation TeamMemberRef
     * 
     * @return true if ref corresponds to a session membership, false otherwise
     */
    public static boolean isRefSessionMember(AbstractKnowledgeSession knowledgeSession, TeamMember<?> ref) {
        // Knowledge session required to determine alignment
        if(knowledgeSession == null) {
            return false;
        }
        // Knowledge session memberships
        List<SessionMembership> sessionMemberships = getSessionMemberships(getSessionMembers(knowledgeSession));
        List<TeamMember<?>> teamRoles = relevantTeamRoles(sessionMemberships);
        // Check to see if refEntityMarking found within session member team roles
        for(TeamMember<?> teamRole : teamRoles) {
            if(teamRole.equals(ref)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determines the user name associated with the TeamMember ref if the association exists within the knowledge session
     * 
     * @param knowledgeSession - Knowledge Session to parse Session Memberships from
     * @param ref - TeamMember created from Environment Adaptation TeamMemberRef
     * 
     * @return associated user name or null if not found
     */
    public static String refUsername(AbstractKnowledgeSession knowledgeSession, TeamMember<?> ref) {
        // Knowledge session required to determine alignment
        if(knowledgeSession == null) {
            return null;
        }
        // Knowledge session memberships
        List<SessionMembership> sessionMemberships = getSessionMemberships(getSessionMembers(knowledgeSession));
        for(SessionMembership membership : sessionMemberships) {
            TeamMember<?> position = membership.getTeamMember();
            if(position != null && position.equals(ref)) {
                return membership.getUsername();
            }
        }
        return null;
    }
}
