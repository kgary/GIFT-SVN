package mil.arl.gift.lms.impl.lrs.xapi.append;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.rusticisoftware.tincan.Activity;
import com.rusticisoftware.tincan.Agent;
import com.rusticisoftware.tincan.Context;
import com.rusticisoftware.tincan.Group;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.MarkedTeamMember;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.EchelonEnum;
import mil.arl.gift.common.state.AbstractPerformanceState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.lms.impl.common.LmsXapiActivityException;
import mil.arl.gift.lms.impl.common.LmsXapiAgentException;
import mil.arl.gift.lms.impl.common.LmsXapiAppenderException;
import mil.arl.gift.lms.impl.common.LmsXapiExtensionException;
import mil.arl.gift.lms.impl.common.LmsXapiProfileException;
import mil.arl.gift.lms.impl.lrs.xapi.ContextActivitiesHelper;
import mil.arl.gift.lms.impl.lrs.xapi.PersonaHelper;
import mil.arl.gift.lms.impl.lrs.xapi.TeamHelper;
import mil.arl.gift.lms.impl.lrs.xapi.activity.TeamRoleActivity;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsActivityTypeConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts;
import mil.arl.gift.lms.impl.lrs.xapi.profile.its.ItsContextExtensionConcepts.extensionObjectKeys;
import mil.arl.gift.lms.impl.lrs.xapi.statements.AbstractGiftStatement;

/**
 * Creates Echelon and Team Role Activities + Team Structure extension based on the information
 * found within a Knowledge Session. Constructors are used to modify configuration of the 
 * Team Structure extension + determine relevant Echelon and Team Role Activities to include
 * within xAPI Statement. 
 * 
 * @author Yet Analytics
 *
 */
public class TeamAppender extends AbstractStatementAppender {
    /** name of the appender */
    private static final String appenderName = "Team Appender";
    /** appender description */
    private static final String appenderInfo = "Creates xAPI representations from Team Structure and adds to xAPI Statement";
    /** top level Team from Abstract Knowledge Session */
    private Team rootTeam;
    /** Should the Team Structure Extension contain the full structure or be pruned */
    private boolean fullStructure;
    /** Abstract Knowledge Session that contains Team Structure and Session Member(ship(s)) */
    private AbstractKnowledgeSession knowledgeSession;
    /** Collection of Teams that contain an assigned session member */
    private List<Team> teams;
    /** Collection of Session Members from knowledge session */
    private List<SessionMember> sessionMembers;
    /** Subset of sessionMembers corresponding to `usernames` instance variable. when `usernames` is not provided, same as sessionMembers */
    private List<SessionMember> selectSessionMembers;
    /** Collection of Session Memberships from sessionMembers */
    private List<SessionMembership> sessionMemberships;
    /** Collection of Session Memberships from selectSessionMembers */
    private List<SessionMembership> selectSessionMemberships;
    /** Collection of xAPI Agent(s) created from sessionMemberships */
    private List<Agent> members;
    /** Collection of names used to create selectSessionMembers subset of sessionMembers*/
    private Set<String> usernames;
    /** Performance State used to ensure getAssessedTeamOrgEntities team roles are accounted for */
    private AbstractPerformanceState performanceState;
    /** Collection of Team Echelon Activities to include in xAPI Statement */
    private Set<Activity> refTeamEchelonActivities;
    /** Collection of Agent MBOX IFI(s) created from `usernames` and used within pruning */
    private Set<String> mboxIfis;
    /** Collection of Team Member References used within pruning */
    private Set<MarkedTeamMember> manyRefTeamMember;
    /** Collection of Team(s) which contain Team Member Reference(s); used to ensure all relevant Echelon Activities are included in xAPI Statement */
    private Set<Team> refTeamMemberTeams;
    /** Collection of Team Role Activities used to ensure all relevant Team Role Activities are included in xAPI Statement */
    private Set<Activity> refTeamRoleActivities;
    /** Collection of Team Role Activity identifiers used within pruning */
    private Set<String> refTeamRoleActivityIds;
    /** Echelon xAPI Activity Type */
    private ItsActivityTypeConcepts.TeamEchelon echelonActivityType;
    /** Team Structure Context Extension */
    private ItsContextExtensionConcepts.TeamStructure teamStructureContextExtension;
    /** Team Role xAPI Activity Type */
    private ItsActivityTypeConcepts.TeamRole teamRoleATC;
    /**
     * Initializes Profile Concepts and sets common state.
     * 
     * @param session - knowledge session containing Team Structure and Session Member(ship(s))
     * 
     * @throws LmsXapiAppenderException when unable to initialize a concept or missing knowledge session
     */
    private TeamAppender(AbstractKnowledgeSession session) throws LmsXapiAppenderException {
        super(appenderName, appenderInfo);
        if(session == null) {
            throw new IllegalArgumentException("knowledgeSession can not be null!");
        }
        if(session.getTeamStructure() == null) {
            throw new IllegalArgumentException("Team Appender should not be used when the session does not have a team structure set!");
        }
        try {
            this.echelonActivityType = ItsActivityTypeConcepts.TeamEchelon.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiAppenderException("Unable to initialize Team Echelon Activity Type concept!", e);
        }
        try {
            this.teamRoleATC = ItsActivityTypeConcepts.TeamRole.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiAppenderException("Unable to initialize Team Role Activity Type concept!", e);
        }
        try {
            this.teamStructureContextExtension = ItsContextExtensionConcepts.TeamStructure.getInstance();
        } catch (LmsXapiProfileException e) {
            throw new LmsXapiAppenderException("Unable to initialize Team Structure Context Extension concept!", e);
        }
        this.knowledgeSession = session;
        this.rootTeam = knowledgeSession.getTeamStructure();
        this.sessionMembers = TeamHelper.getSessionMembers(knowledgeSession);
        this.sessionMemberships = TeamHelper.getSessionMemberships(sessionMembers);
        this.members = createGroupMembers(sessionMemberships);
        this.teams = TeamHelper.findRelevantTeams(rootTeam, sessionMemberships);
    }
    
    /**
     * Creation of Team components (Team Role Activities, Team Echelon Activities, Team Structure Extension) used within
     * Started Knowledge Session xAPI Statement generation and Environment Adaptation xAPI Statement generation when the
     * adaptation does not include a TeamMemberRef.
     * 
     * @param session - knowledge session containing Team Structure and Session Member(ship(s))
     * @param includeFullStructure - determines if the full team structure is included in statements or only relevant items
     * 
     * @throws LmsXapiAppenderException when unable to create Agent's from session members
     */
    public TeamAppender(AbstractKnowledgeSession session, boolean includeFullStructure) throws LmsXapiAppenderException {
        this(session);
        this.fullStructure = includeFullStructure;
        // user names expected to be null before calling getSelectSessionMembers
        this.selectSessionMembers = getSelectSessionMembers();
        this.selectSessionMemberships = TeamHelper.getSessionMemberships(selectSessionMembers);
    }
    
    /**
     * Creation of Team components (Team Role Activities, Team Echelon Activities, Team Structure Extension) used within
     * Assessed Assessment xAPI Statement generation and Environment Adaptation xAPI Statement generation when the TeamMemberRef
     * points to a knowledge session member. 
     * 
     * @param session - knowledge session containing Team Structure and Session Member(ship(s))
     * @param includeFullStructure - determines if the full team structure is included in statements or only relevant items
     * @param relevantUsernames - Set of user names used to filter included team role activities
     * 
     * @throws LmsXapiAppenderException when unable to create Agent's from session members
     */
    public TeamAppender(AbstractKnowledgeSession session, boolean includeFullStructure, Set<String> relevantUsernames) throws LmsXapiAppenderException {
        this(session);
        this.fullStructure = includeFullStructure;
        this.usernames = relevantUsernames;
        // user names must be handled before calling getSelectSessionMembers
        this.selectSessionMembers = getSelectSessionMembers();
        this.selectSessionMemberships = TeamHelper.getSessionMemberships(selectSessionMembers);
    }
    
    /**
     * Creation of Team components (Team Role Activities, Team Echelon Activities, Team Structure Extension) used within
     * Demonstrated Performance xAPI Statement generation.
     * 
     * @param session - knowledge session containing Team Structure and Session Member(ship(s))
     * @param includeFullStructure - determines if the full team structure is included in statements or only relevant items
     * @param relevantUsername - user name used to filter included team role activity
     * @param performance - performance state to parse assessedTeamOrgEntities from
     * 
     * @throws LmsXapiAppenderException when unable to create Agent's from session members
     */
    public TeamAppender(AbstractKnowledgeSession session, boolean includeFullStructure, List<String> relevantUsernames, AbstractPerformanceState performance) throws LmsXapiAppenderException {
        this(session);
        Set<String> usernames = new HashSet<String>();
        usernames.addAll(relevantUsernames);
        this.fullStructure = includeFullStructure;
        this.usernames = usernames;
        this.performanceState = performance;
        // user names must be handled before calling getSelectSessionMembers
        this.selectSessionMembers = getSelectSessionMembers();
        this.selectSessionMemberships = TeamHelper.getSessionMemberships(selectSessionMembers);
    }
    
    /**
     * Creation of Team components (Team Role Activities, Team Echelon Activities, Team Structure Extension) used within
     * Environment Adaptation xAPI Statement generation when the adaptation contains a TeamMemberRef which does not point
     * to a knowledge session member.
     * 
     * @param session - knowledge session containing Team Structure and Session Member(ship(s))
     * @param includeFullStructure - determines if the full team structure is included in statements or only relevant items
     * @param relevantUsername - user name used to filter included team role activity
     * @param ref - TeamMemberRef that points to an NPC
     * 
     * @throws LmsXapiAppenderException when unable to create Agent's from session members or issue processing TeamMemberRef
     */
    public TeamAppender(AbstractKnowledgeSession session, boolean includeFullStructure, String relevantUsername, MarkedTeamMember ref) throws LmsXapiAppenderException {
        this(session);
        this.fullStructure = includeFullStructure;
        if(ref == null) {
            throw new LmsXapiAppenderException("ref team member can't be null!");
        }
        Set<MarkedTeamMember> refMembers = new HashSet<MarkedTeamMember>(1);
        refMembers.add(ref);
        this.manyRefTeamMember = refMembers;
        // Select Session Member
        Set<String> relevantUsernames = new HashSet<String>(1);
        relevantUsernames.add(relevantUsername);
        this.usernames = relevantUsernames;
        // IFI for top level Actor used within pruning
        Set<String> relevantIFIs = new HashSet<String>(1);
        this.mboxIfis = createMemberMboxIfis(relevantIFIs, usernames);
        // Team the reference belongs to
        this.refTeamMemberTeams = TeamHelper.findRelevantTeams(rootTeam, manyRefTeamMember);
        if(CollectionUtils.isEmpty(refTeamMemberTeams)) {
            throw new LmsXapiAppenderException("Unable to find ref Team Member's Team!");
        }
        // Echelon Activities from relevant Team
        Set<Activity> refEchelonAs = new HashSet<Activity>(1);
        this.refTeamEchelonActivities = createEchelonActivities(refEchelonAs, refTeamMemberTeams, echelonActivityType);
        // Team Role for ref
        Set<Activity> refTeamRoleAs = new HashSet<Activity>(1);
        this.refTeamRoleActivities = createTeamRoleActivities(refTeamRoleAs, manyRefTeamMember, teamRoleATC);
        Set<String> teamRoleAIds = new HashSet<String>(1);
        this.refTeamRoleActivityIds = parseTeamRoleActivityIds(teamRoleAIds, refTeamRoleActivities);
        // user names must be handled before calling getSelectSessionMembers
        this.selectSessionMembers = getSelectSessionMembers();
        this.selectSessionMemberships = TeamHelper.getSessionMemberships(selectSessionMembers);
    }
    
    /**
     * Creation of Team components (Team Role Activities, Team Echelon Activities, Team Structure Extension) used within
     * Environment Adaptation xAPI Statement generation when the adaptation contains 1+ TeamMemberRef which does not point
     * to a knowledge session member.
     * 
     * @param session - knowledge session containing Team Structure and Session Member(ship(s))
     * @param includeFullStructure - determines if the full team structure is included in statements or only relevant items
     * @param relevantUsername - user name used to filter included team role activity
     * @param refs - TeamMemberRef(s) that points to NPC(s)
     * 
     * @throws LmsXapiAppenderException when unable to create Agent's from session members or issue processing TeamMemberRef
     */
    public TeamAppender(AbstractKnowledgeSession session, boolean includeFullStructure, String relevantUsername, Set<MarkedTeamMember> refs) throws LmsXapiAppenderException {
        this(session);
        this.fullStructure = includeFullStructure;
        this.manyRefTeamMember = refs;
        // Select Session Member
        Set<String> relevantUsernames = new HashSet<String>(1);
        relevantUsernames.add(relevantUsername);
        this.usernames = relevantUsernames;
        // IFI for top level Actor used within pruning
        Set<String> relevantIFIs = new HashSet<String>(1);
        this.mboxIfis = createMemberMboxIfis(relevantIFIs, usernames);
        // Team(s) the references belongs to
        this.refTeamMemberTeams = TeamHelper.findRelevantTeams(rootTeam, manyRefTeamMember);
        if(CollectionUtils.isEmpty(refTeamMemberTeams)) {
            throw new LmsXapiAppenderException("Unable to find ref Team Member's Team!");
        }
        // Echelon Activities from relevant Team
        Set<Activity> refEchelonAs = new HashSet<Activity>(1);
        this.refTeamEchelonActivities = createEchelonActivities(refEchelonAs, refTeamMemberTeams, echelonActivityType);
        // Team Role for ref
        Set<Activity> refTeamRoleAs = new HashSet<Activity>(1);
        this.refTeamRoleActivities = createTeamRoleActivities(refTeamRoleAs, manyRefTeamMember, teamRoleATC);
        Set<String> teamRoleAIds = new HashSet<String>();
        this.refTeamRoleActivityIds = parseTeamRoleActivityIds(teamRoleAIds, refTeamRoleActivities);
        // user names must be handled before calling getSelectSessionMembers
        this.selectSessionMembers = getSelectSessionMembers();
        this.selectSessionMemberships = TeamHelper.getSessionMemberships(selectSessionMembers);
    }
    
    /**
     * Creation of Team components (Team Role Activities, Team Echelon Activities, Team Structure Extension) used within
     * Environment Adaptation xAPI Statement generation when the adaptation contains 1+ knowledge session members and 1+ 
     * TeamMemberRef which does not point to a knowledge session member.
     * 
     * @param session - knowledge session containing Team Structure and Session Member(ship(s))
     * @param includeFullStructure - determines if the full team structure is included in statements or only relevant items
     * @param relevantUsernames - user name(s) used to filter included team role activity
     * @param refs - TeamMemberRef(s) that points to NPC(s)
     * 
     * @throws LmsXapiAppenderException when unable to create Agent's from session members or issue processing TeamMemberRef
     */
    public TeamAppender(AbstractKnowledgeSession session, boolean includeFullStructure, Set<String> relevantUsernames, Set<MarkedTeamMember> refs) throws LmsXapiAppenderException {
        this(session);
        this.fullStructure = includeFullStructure;
        this.manyRefTeamMember = refs;
        // Select Session Member
        this.usernames = relevantUsernames;
        // IFI for top relevant agents used within pruning
        Set<String> relevantIFIs = new HashSet<String>();
        this.mboxIfis = createMemberMboxIfis(relevantIFIs, usernames);
        // Team(s) the references belongs to
        this.refTeamMemberTeams = TeamHelper.findRelevantTeams(rootTeam, manyRefTeamMember);
        if(CollectionUtils.isEmpty(refTeamMemberTeams)) {
            throw new LmsXapiAppenderException("Unable to find ref Team Member's Team!");
        }
        // Echelon Activities from relevant Team(s)
        Set<Activity> refEchelonAs = new HashSet<Activity>(1);
        this.refTeamEchelonActivities = createEchelonActivities(refEchelonAs, refTeamMemberTeams, echelonActivityType);
        // Team Roles for refs
        Set<Activity> refTeamRoleAs = new HashSet<Activity>(1);
        this.refTeamRoleActivities = createTeamRoleActivities(refTeamRoleAs, manyRefTeamMember, teamRoleATC);
        Set<String> teamRoleAIds = new HashSet<String>();
        this.refTeamRoleActivityIds = parseTeamRoleActivityIds(teamRoleAIds, refTeamRoleActivities);
        // user names must be handled before calling getSelectSessionMembers
        this.selectSessionMembers = getSelectSessionMembers();
        this.selectSessionMemberships = TeamHelper.getSessionMemberships(selectSessionMembers);
    }
    
    /**
     * Updates passed in collection to contain MBOX IFIs created from each user name
     * 
     * @param coll - accumulator to update and return
     * @param relevantUsernames - collection of user names to convert
     * 
     * @return updated collection
     * 
     * @throws LmsXapiAppenderException when unable to create MBOX IFI from a user name
     */
    private static Set<String> createMemberMboxIfis(Set<String> coll, Set<String> relevantUsernames) throws LmsXapiAppenderException {
        for(String userName : relevantUsernames) {
            String mboxIfi;
            try {
                mboxIfi = PersonaHelper.createMboxIFI(userName);
            } catch (LmsXapiAgentException e) {
                throw new LmsXapiAppenderException("Unable to create mbox IFI from relevantUsername: "+userName, e);
            }
            coll.add(mboxIfi);
        }
        return coll;
    }
    
    /**
     * Updates passed in collection to contain Echelon Activities created from each Team that has an echelon
     * 
     * @param coll - accumulator to update and return
     * @param relevantRefTeams - collection of Teams to extract echelon from
     * @param activityType - Echelon Activity Type used to create activities added to accumulator
     * 
     * @return updated collection
     * 
     * @throws LmsXapiAppenderException when unable to create Echelon activity from a non-null echelon
     */
    private static Set<Activity> createEchelonActivities(Set<Activity> coll, Set<Team> relevantRefTeams, ItsActivityTypeConcepts.TeamEchelon activityType) throws LmsXapiAppenderException {
        for(Team refTeam : relevantRefTeams) {
            if(refTeam.getEchelon() != null) {
                Activity refTeamEchelon;
                try {
                    refTeamEchelon = activityType.asActivity(refTeam.getEchelon());
                } catch (LmsXapiActivityException e) {
                    throw new LmsXapiAppenderException("Unable to create Echelon Activity from Echelon enum!", e);
                }
                coll.add(refTeamEchelon);
            }
        }
        return coll;
    }
    
    /**
     * Updates passed in collection to contain Team Role Activities created from each MarkedTeamMember
     * 
     * @param coll - accumulator to update and return
     * @param memberRefs - collection of Team Member References to extract identifier from
     * @param activityType - Team Role Activity Type used to create activities added o accumulator
     * 
     * @return updated collection
     * 
     * @throws LmsXapiAppenderException hen unable to create Team Role activity from MarkedTeamMember
     */
    private static Set<Activity> createTeamRoleActivities(Set<Activity> coll, Set<MarkedTeamMember> memberRefs, ItsActivityTypeConcepts.TeamRole activityType) throws LmsXapiAppenderException {
        for(MarkedTeamMember refMember : memberRefs) {
            Activity refTeamRole;
            try {
                refTeamRole = activityType.asActivity(refMember);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiAppenderException("Unable to create reference Team Role Activity!", e);
            }
            coll.add(refTeamRole);
        }
        return coll;
    }
    
    /**
     * Parses activity ids from collection of activities
     * 
     * @param coll - accumulator of activity ids
     * @param teamRoleActivities - source of activities
     * 
     * @return updated accumulator
     */
    private static Set<String> parseTeamRoleActivityIds(Set<String> coll, Set<Activity> teamRoleActivities){
        for(Activity teamRoleActivity : teamRoleActivities) {
            coll.add(teamRoleActivity.getId().toString());
        }
        return coll;
    }
    
    /**
     * If no user names are set, defaults to true.
     * 
     * @param member - SessionMember to check
     * 
     * @return is the session members user name found within the set user names
     */
    private boolean isSelectSessionMember(SessionMember member) {
        // Not set, all are relevant
        if(usernames == null) {
            return true;
        }
        // Parse out username from SessionMember
        String userSessionName = member.getUserSession().getUsername();
        String sessionMemberName = member.getSessionMembership().getUsername();
        // determine relevance
        if(userSessionName != null && sessionMemberName != null) {
            return (usernames.contains(userSessionName) || usernames.contains(sessionMemberName));
        } else if(userSessionName == null && sessionMemberName != null) {
            return usernames.contains(sessionMemberName);
        } else if (userSessionName != null && sessionMemberName == null) {
            return usernames.contains(userSessionName);
        } else {
            return false;
        }
    }
    
    /**
     * Filter all session members to only those whose user name is contained within user names
     * 
     * @return subset of sessionMembers
     */
    private List<SessionMember> getSelectSessionMembers(){
        List<SessionMember> coll = new ArrayList<SessionMember>();
        for(SessionMember member : sessionMembers) {
            if(isSelectSessionMember(member)) {
                coll.add(member);
            }
        }
        return coll;
    }
    
    /**
     * Creates collection of xAPI Agents from session members
     * 
     * @return list of Agents to use as context.team group members
     * 
     * @throws LmsXapiAppenderException when unable to create xAPI Agent from session member
     */
    private static List<Agent> createGroupMembers(List<SessionMembership> memberships) throws LmsXapiAppenderException {
        List<Agent> members = new ArrayList<Agent>();
        for(SessionMembership sessionMembership : memberships) {
            String username = sessionMembership.getUsername();
            if(username != null) {
                try {
                    members.add(PersonaHelper.createMboxAgent(username));
                } catch (LmsXapiAgentException e) {
                    throw new LmsXapiAppenderException("Unable to create mbox Agent from username!", e);
                }
            }
        }
        return members;
    }
    
    /**
     * Create Anonymous Group from relevant teams and members assigned to those teams
     * 
     * @param relevantTeams - list of teams that have an assigned session member
     * @param members - list of session members as agents
     * 
     * @return Anonymous Group with compound name and session members
     */
    private Group createGroupFromRelevantTeams() {
        Group anonGroup = new Group();
        // Create name from comp of relevant team names
        List<String> teamNames = new ArrayList<String>();
        for(Team team : teams) {
            teamNames.add(team.getName());
        }
        if(CollectionUtils.isNotEmpty(teamNames)) {
            String compoundTeamName = StringUtils.join(teamNames, "|");
            anonGroup.setName(compoundTeamName);
        }
        anonGroup.setMembers(members);
        return anonGroup;
    }
    
    /**
     * Parses out all non-null echelon enums from team structure
     * 
     * @param echelons - accumulator
     * @param units - AbstractTeamUnits to parse
     */
    private static void parseEchelons(Set<EchelonEnum> echelons, List<AbstractTeamUnit> units) {
        for(AbstractTeamUnit unit : units) {
            if(unit instanceof Team) {
                Team team = (Team) unit;
                EchelonEnum echelon = team.getEchelon();
                if(echelon != null) {
                    echelons.add(echelon);
                }
                parseEchelons(echelons, team.getUnits());
            }
        }
    }
    
    /**
     * Creates collection of Echelon Eums from team structure
     * 
     * @return collection of EchelonEnum
     */
    private Set<EchelonEnum> getEchelons() {
        Set<EchelonEnum> coll = new HashSet<EchelonEnum>();
        if(fullStructure) {
            // All echelon found within team structure
            EchelonEnum rootEchelon = rootTeam.getEchelon();
            if(rootEchelon != null) {
                coll.add(rootEchelon);
            }
            parseEchelons(coll, rootTeam.getUnits());
        } else {
            // echelon found within relevant teams
            for(Team team : teams) {
                EchelonEnum echelon = team.getEchelon();
                if(echelon != null) {
                    coll.add(echelon);
                }
            }
        }
        return coll;
    }
    
    /**
     * Creates set of Team Role Activities from assessed team org entities.
     * 
     * @return possibly empty but not null set of activities
     * 
     * @throws LmsXapiAppenderException when unable to create activity from assessed team org entities entry
     */
    private Set<Activity> assessedTeamRoleActivities() throws LmsXapiAppenderException {
        Set<Activity> teamOrgEntityActivities = new HashSet<Activity>();
        if(performanceState != null && performanceState.getState() != null && performanceState.getState().getAssessedTeamOrgEntities() != null) {
            for(Map.Entry<String, AssessmentLevelEnum> kv : performanceState.getState().getAssessedTeamOrgEntities().entrySet()) {
                // Marked Team Member ref creation
                String teamRoleName = kv.getKey();
                MarkedTeamMember teamRole = new MarkedTeamMember(teamRoleName, teamRoleName);
                TeamRoleActivity teamRoleActivity;
                try {
                    teamRoleActivity = teamRoleATC.asActivity(teamRole);
                } catch (LmsXapiActivityException e) {
                    throw new LmsXapiAppenderException("Unable to create Team Role Activity Id from string key within Assessed Team Org Entities!", e);
                }
                teamOrgEntityActivities.add(teamRoleActivity);
            }
        }
        return teamOrgEntityActivities;
    }
    
    /**
     * Logic for determining if a member item should be removed from the team unit extension component
     * 
     * @param memberItem - JSON node from a team unit item
     * @param isEntityMappingRelevant - indicates if a performance state is influencing the pruning
     * @param teamOrgEntityIdSet - collection of ids from performance state
     * 
     * @return true when the member item should be removed, false otherwise
     */
    private boolean shouldPruneNode(JsonNode memberItem, Set<String> teamOrgEntityIdSet) {
        // Performance state handling if set
        if(CollectionUtils.isNotEmpty(teamOrgEntityIdSet)) {
            // pruning based on assessedTeamOrgEntities
            JsonNode identifier = memberItem.get(extensionObjectKeys.IDENTIFIER.getValue());
            if(!teamOrgEntityIdSet.contains(identifier.asText())) {
                return true;
            }
        } else if(manyRefTeamMember != null) {
            // Checking for assigned corresponding to relevant user mbox ifi
            JsonNode assigned = memberItem.get(extensionObjectKeys.ASSIGNED.getValue());
            // Checking for npc reference team role
            JsonNode identifier = memberItem.get(extensionObjectKeys.IDENTIFIER.getValue());
            if(assigned != null) {
                // Compare to relevant session member mbox ifi
                String assignedIfi = assigned.asText();
                if(!mboxIfis.contains(assignedIfi)) {
                    return true;
                }
            } else {
                // Compare to relevant team role activities
                String teamRoleId = identifier.asText();
                if(!refTeamRoleActivityIds.contains(teamRoleId)) {
                    return true;
                }
            }
        } else {
            // Checking to see if assigned to a session member
            JsonNode assigned = memberItem.get(extensionObjectKeys.ASSIGNED.getValue());
            if(assigned == null) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Removes team units without members, possibly removes items with unassigned / non-playable roles based on
     * assessed team org entities within performance state. When performance state is not set or team org entities
     * is empty, remove items with only unassigned / non-playable members.
     * 
     * @param ext - full TeamStructure to pair down to only relevant items
     */
    private void pruneTeamStructure(ArrayNode ext) throws LmsXapiAppenderException {
        // is performance state relevant
        Set<String> teamOrgEntityIdSet = new HashSet<String>();
        int arraycopysize;
        for(Activity teamRoleActivity : assessedTeamRoleActivities()) {
            teamOrgEntityIdSet.add(teamRoleActivity.getId().toString());
        }
        // iteration over team unit extension items
        int extSize = ext.size();
        List<Integer> idxToPrune = new ArrayList<Integer>();
        for(int i = 0; i < extSize; i++) {
            JsonNode node = ext.get(i);
            // does the item contain members
            JsonNode membersNode = node.get(extensionObjectKeys.MEMBERS.getValue());
            if(membersNode != null) {
                // members + iteration setup
                ArrayNode arrayNode = (ArrayNode) membersNode;
                List<Integer> subIdxToPrune = new ArrayList<Integer>();
                int arrayNodeSize = arrayNode.size();
                // iterate over members
                for(int j = 0; j < arrayNodeSize; j++) {
                    JsonNode memberItem = arrayNode.get(j);
                    if(shouldPruneNode(memberItem, teamOrgEntityIdSet)) {
                        subIdxToPrune.add(j);
                    }
                }
                // Prune non-relevant members from copy to determine if parent node should be pruned
                int nPruned = 0;
                ArrayNode arrayNodeCopy = arrayNode.deepCopy();
                for(Integer subIdx : subIdxToPrune) {
                    arrayNodeCopy.remove(subIdx - nPruned);
                    nPruned++;
                }
                arraycopysize = arrayNodeCopy.size();
                if(arraycopysize == 0) {
                    idxToPrune.add(i);
                }
            } else {
                // remove node without members
                idxToPrune.add(i);
            }
        }
        // prune
        int nPruned = 0;
        for(Integer idx : idxToPrune) {
            ext.remove(idx - nPruned);
            nPruned++;
        }
    }
    
    @Override
    public AbstractGiftStatement appendToStatement(AbstractGiftStatement statement) throws LmsXapiAppenderException {
        Context ctx = statement.getContext();
        // Compound Team
        ctx.setTeam(createGroupFromRelevantTeams());
        // Relevant Echelon activities
        Set<String> relevantTeamEchelonIds = new HashSet<String>();
        for(EchelonEnum echelon : getEchelons()) {
            Activity a;
            try {
                a = echelonActivityType.asActivity(echelon);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiAppenderException("Unable to create Echelon Activity from Echelon enum!", e);
            }
            ContextActivitiesHelper.addGroupingActivity(a, ctx);
            relevantTeamEchelonIds.add(a.getId().toString());
        }
        // Relevant Team role activities
        Set<String> relevantTeamRoleIds = new HashSet<String>();
        for(TeamMember<?> position : TeamHelper.relevantTeamRoles(selectSessionMemberships)) {
            Activity a;
            try {
                a = teamRoleATC.asActivity(position);
            } catch (LmsXapiActivityException e) {
                throw new LmsXapiAppenderException("Unable to create Team Role Activity!", e);
            }
            ContextActivitiesHelper.addCategoryActivity(a, ctx);
            relevantTeamRoleIds.add(a.getId().toString());
        }
        // Team Role activities based on assessed team org entities
        for(Activity assessedTeamRoleActivity : assessedTeamRoleActivities()) {
            if(!relevantTeamRoleIds.contains(assessedTeamRoleActivity.getId().toString())) {
                ContextActivitiesHelper.addCategoryActivity(assessedTeamRoleActivity, ctx);
            }
        }
        // Team Role Activity + Team Echelon Activity from Team Member References
        if(CollectionUtils.isNotEmpty(manyRefTeamMember)) {
            for(Activity teamRoleActivity : refTeamRoleActivities) {
                if(!relevantTeamRoleIds.contains(teamRoleActivity.getId().toString())) {
                    ContextActivitiesHelper.addCategoryActivity(teamRoleActivity, ctx);
                }
            }
            for(Activity teamEchelonActivity : refTeamEchelonActivities) {
                if(!relevantTeamEchelonIds.contains(teamEchelonActivity.getId().toString())) {
                    ContextActivitiesHelper.addGroupingActivity(teamEchelonActivity, ctx);
                }
            }
        }
        // Team Structure context extension
        ArrayNode ext;
        try {
            ext = teamStructureContextExtension.createExtensionColl(rootTeam, sessionMemberships);
        } catch (LmsXapiExtensionException e) {
            throw new LmsXapiAppenderException("Unable to create Team Structure Extension!", e);
        }
        // full structure vs relevant elements from the structure
        if(fullStructure) {
            teamStructureContextExtension.addToContext(ctx, ext);
        } else {
            pruneTeamStructure(ext);
            if(ext.size() != 0) {
                teamStructureContextExtension.addToContext(ctx, ext);
            }
        }
        statement.setContext(ctx);
        return statement;
    }
}
