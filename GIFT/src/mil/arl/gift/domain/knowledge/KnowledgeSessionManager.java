/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.DomainSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.KnowledgeSessionCourseInfo;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.ObserverControls;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionType;
import mil.arl.gift.common.course.dkf.session.IndividualKnowledgeSession;
import mil.arl.gift.common.course.dkf.session.KnowledgeSessionsRequest;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipException;
import mil.arl.gift.common.course.dkf.session.ManageTeamMembershipRequest.ACTION_TYPE;
import mil.arl.gift.common.course.dkf.session.Mission;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.session.SessionMember.GroupMembership;
import mil.arl.gift.common.course.dkf.session.SessionMember.IndividualMembership;
import mil.arl.gift.common.course.dkf.session.SessionMember.SessionMembership;
import mil.arl.gift.common.course.dkf.session.TeamKnowledgeSession;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.domain.knowledge.common.AbstractPerformanceAssessmentNode;

/**
 * Manages the active knowledge sessions (i.e. when a DKF is being used in a
 * course) for a domain module.
 *
 * @author mhoffman
 *
 */
public class KnowledgeSessionManager {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(KnowledgeSessionManager.class);

    /** singleton instance of this class */
    private static final KnowledgeSessionManager instance = new KnowledgeSessionManager();

    /**
     * mapping of unique domain session id to the information about a single real time assessment (DKF)
     * which may include team members that have joined the session.
     */
    private Map<Integer, AbstractKnowledgeSession> knowledgeSessionMap = new ConcurrentHashMap<>();

    /**
     * mapping of unique domain session id (for all sessions in a knowledge session) to the domain session
     * id of the host of the knowledge session.  For individual knowledge sessions the key and value are the same
     * id.
     */
    private Map<Integer, Integer> domainSessionIdToHostDomainSessionId = new ConcurrentHashMap<>();
    
    /** the interface used for notifications of knowledge session events */
    private KnowledgeSessionEventListener eventListener;

    /**
     * default
     */
    private KnowledgeSessionManager(){}

    /**
     * Return the singleton instance
     *
     * @return instance of this class
     */
    public static KnowledgeSessionManager getInstance(){
        return instance;
    }
    
    /**
     * Set the event listener for knowledge session events
     * 
     * @param eventListener the listener that wants to be notified of knowledge session events
     */
    public void setEventListener(KnowledgeSessionEventListener eventListener){
        this.eventListener = eventListener;
    }

    /**
     * Add information about the starting of a DKF that has no team
     * organization.
     *
     * @param domainSession The {@link DomainSession} for which this
     *        {@link IndividualKnowledgeSession} is being constructed. Can't be
     *        null.
     * @param courseName the name of the gift course. Can't be null or empty.
     * @param courseObjectName the name of the gift course object that is using
     *        a DKF. Can't be null or empty.
     * @param experimentName the name of the experiment if this course is being
     *        run as an experiment. Can be null if the course is not an
     *        experiment.
     * @param scenarioDescription the description of the scenario. Supports HTML. Can be null or empty.
     * @param team the structure of the team for the session. Can be null, e.g. legacy or individual dkf session
     * @param performanceNodeMap the complete mapping of all task and concept
     *        node ids to their respective names.
     * @param trainingApplicationType the type of training application that is
     *        adding this session.
     * @param mission the mission data for this session. Can be null.
     * @param observerControls the data controlling how this session should be
     *        presented to observer controllers. Can be null.
     * @return the new individual knowledge session created
     */
    public IndividualKnowledgeSession addIndividualKnowledgeSession(DomainSession domainSession, String courseName, 
            String courseObjectName, String experimentName, String scenarioDescription, Team team, 
            Map<Integer, AbstractPerformanceAssessmentNode> performanceNodeMap, 
            TrainingApplicationEnum trainingApplicationType, Mission mission, 
            ObserverControls observerControls) {
        final String courseRuntimeId = domainSession.getDomainRuntimeId();
        final String courseAuthoredId = domainSession.getDomainSourceId();
        final int domainSessionId = domainSession.getDomainSessionId();
        final String learnerUsername = domainSession.getUsername();

        if (knowledgeSessionMap.containsKey(domainSessionId)) {
            throw new IllegalArgumentException("Unable to host an individual knowledge session because the domain session id " + domainSessionId
                    + " is already associated with another knowledge session.");
        }

        KnowledgeSessionCourseInfo courseInfo = new KnowledgeSessionCourseInfo(courseName, courseRuntimeId, courseAuthoredId, experimentName);
        IndividualMembership individualMembership = new IndividualMembership(learnerUsername);
        
        if(team != null){
            // if there is an identified playable member, set it so the learner will be associated with that member.  This is most likely what
            // caused this session to be an individual membership session.
            individualMembership.setTeamMember(team.getFirstPlayableTeamMember()); 
        }
        
        SessionMember sessionMember = new SessionMember(individualMembership, domainSession, domainSessionId);
        IndividualKnowledgeSession iKnowledgeSession = new IndividualKnowledgeSession(courseObjectName, scenarioDescription, courseInfo,
                sessionMember, team, convertPerformanceNodeMap(performanceNodeMap), trainingApplicationType,
                SessionType.ACTIVE, System.currentTimeMillis(), mission, observerControls);

        if (logger.isDebugEnabled()) {
            logger.debug("Creating individual knowledge session of " + iKnowledgeSession);
        }
        knowledgeSessionMap.put(domainSessionId, iKnowledgeSession);

        domainSessionIdToHostDomainSessionId.put(domainSessionId, domainSessionId);

        if(eventListener != null){
            eventListener.sendKnowledgeSessionCreatedMessage(iKnowledgeSession);
        }
        return iKnowledgeSession;
    }

    /**
     * Add information about the starting of a DKF that has team organization
     * and a user is hosting the team session.
     *
     * @param domainSession The {@link DomainSession} for which the
     *        {@link TeamKnowledgeSession} is being constructed. Can't be null.
     * @param nameOfSession the user provided name of the hosted session, used
     *        to help others joining the session. Can't be null or empty.
     * @param courseName the name of the gift course. Can't be null or empty.
     * @param experimentName the name of the experiment if this course is being
     *        run as an experiment. Can be null if the course is not an
     *        experiment.
     * @param scenarioDescription the description of the scenario. Supports HTML. Can be null or empty.
     * @param totalPossibleTeamMembers the number of team members allowed as
     *        defined in the team organization. Must be greater than 0.
     * @param teamData The structure of the team organization from the scenario
     *        used to populate the role information.
     * @param performanceNodeMap the complete mapping of all task and concept
     *        node ids to their respective names.
     * @param trainingApplicationType the type of training application that is
     *        hosting this session.
     * @param mission the mission data for this session. Can be null.
     * @param observerControls the data controlling how this session should be
     *        presented to observer controllers. Can be null.
     * @param groupMembership whether the team knowledge session will be using group membership or individual membership type.
     * @param sessionType the type of active knowledge session. Can't be null.
     * @return the new team knowledge session created
     */
    public TeamKnowledgeSession hostTeamKnowledgeSession(DomainSession domainSession, String nameOfSession, 
            String courseName, String experimentName, String scenarioDescription, int totalPossibleTeamMembers, 
            Team teamData, Map<Integer, AbstractPerformanceAssessmentNode> performanceNodeMap, 
            TrainingApplicationEnum trainingApplicationType, Mission mission, ObserverControls observerControls, boolean groupMembership,
            SessionType sessionType) {

        final int domainSessionIdOfHost = domainSession.getDomainSessionId();
        final String usernameOfHost = domainSession.getUsername();
        final String courseRuntimeId = domainSession.getDomainRuntimeId();
        final String courseAuthoredId = domainSession.getDomainSourceId();

        if(knowledgeSessionMap.containsKey(domainSessionIdOfHost)){
            throw new IllegalArgumentException("Unable to host a team knowledge session because the domain session id "+domainSessionIdOfHost+" is already associated with another knowledge session.");
        }

        List<String> teamRoles = new ArrayList<>();
        for (AbstractTeamUnit unit : teamData.getUnits()) {
            if (unit instanceof TeamMember) {
                if(logger.isDebugEnabled()){
                    logger.debug("found team role - " + unit.getName());
                }
                teamRoles.add(unit.getName());
            }
        }

        KnowledgeSessionCourseInfo courseInfo = new KnowledgeSessionCourseInfo(courseName, courseRuntimeId, courseAuthoredId, experimentName);
        SessionMembership sessionMembership;
        if(groupMembership){
            sessionMembership = new GroupMembership(usernameOfHost);
        }else{
            sessionMembership = new IndividualMembership(usernameOfHost);
        }

        SessionMember sessionMember = new SessionMember(sessionMembership, domainSession, domainSessionIdOfHost);
        TeamKnowledgeSession tKnowledgeSession = new TeamKnowledgeSession(nameOfSession, scenarioDescription, courseInfo,
                totalPossibleTeamMembers, sessionMember, teamRoles, teamData,
                convertPerformanceNodeMap(performanceNodeMap), trainingApplicationType, sessionType,
                System.currentTimeMillis(), mission, observerControls);
        if(logger.isDebugEnabled()){
            logger.debug("Creating team knowledge session of "+tKnowledgeSession);
        }
        knowledgeSessionMap.put(domainSessionIdOfHost, tKnowledgeSession);

        if(eventListener != null){
            eventListener.sendKnowledgeSessionCreatedMessage(tKnowledgeSession);
        }
        return tKnowledgeSession;
    }

    /**
     * Converts a map of node ids to performance assessment nodes to a map of
     * node ids to their respective node names.
     *
     * @param performanceNodeMap the complete map of all task and concept node
     *        ids to their respective nodes.
     * @return the complete map of all task and concept node ids to their
     *         respective node names.
     */
    private Map<BigInteger, String> convertPerformanceNodeMap(
            Map<Integer, AbstractPerformanceAssessmentNode> performanceNodeMap) {
        Map<BigInteger, String> nodeIdToNameMap = new HashMap<>();

        for (AbstractPerformanceAssessmentNode assessmentNode : performanceNodeMap.values()) {
            nodeIdToNameMap.put(BigInteger.valueOf(assessmentNode.getNodeId()), assessmentNode.getName());
        }

        return nodeIdToNameMap;
    }

    /**
     * Add a new member to a team session.
     *
     * @param domainSessionIdOfHost the domain session id of the gift user
     *        hosting the team session.
     * @param joiningTeamMember The {@link SessionMember} which represents the
     *        member that is joining the specified team session. Can't be null.
     * @param courseId the course id of the joiner. Needed to ensure it matches
     *        the session that is being joined. Cannot be null or empty.
     */
    public void joinTeamSession(int domainSessionIdOfHost, SessionMember joiningTeamMember, String courseId) {

        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(domainSessionIdOfHost);
        if(kSession != null && kSession instanceof TeamKnowledgeSession){

            if(kSession.isSessionRunning()){
                throw new RuntimeException("Unable to join the session named " + kSession.getNameOfSession() +
                        " because it has already been started by the host. ");
            }

            if (courseId == null || courseId.isEmpty()) {
                throw new IllegalArgumentException("Unable to join the session, because the joiner's course is invalid.");
            }

            if (courseId.compareTo(kSession.getCourseSourceId()) != 0) {
                throw new RuntimeException("Unable to join the session because the host course: " + kSession.getCourseSourceId() + ", does not match the joiner's course.");
            }

            TeamKnowledgeSession teamKnowledgeSession = (TeamKnowledgeSession)kSession;
            if(logger.isDebugEnabled()){
                logger.debug("Adding new team member of " + joiningTeamMember + " to team knowledge session of "
                        + teamKnowledgeSession);
            }
            teamKnowledgeSession.joinSession(joiningTeamMember);

            domainSessionIdToHostDomainSessionId.put(joiningTeamMember.getDomainSessionId(), domainSessionIdOfHost);

        }else{
            throw new IllegalArgumentException("The domain session id "+domainSessionIdOfHost+" is not the host of a team knowledge session.");
        }

    }


    /**
     * Remove any knowledge session references associated with the domain
     * session id. This should be called when the domain session id is exiting a
     * course object that was using a DKF. This will not remove team members
     * from a team session.
     *
     * @param domainSessionId the domain session id to use as a look up for any
     *        registered knowledge sessions known to this class.
     * @return the knowledge session that was found and removed, or null if there
     * was no knowledge session found for the given domain session ID
     */
    public AbstractKnowledgeSession removeKnowledgeSession(int domainSessionId){

        if(logger.isDebugEnabled()){
            logger.debug("Removing any hosted team or individual knowledge sessions for domain session id "+domainSessionId);
        }
        return knowledgeSessionMap.remove(domainSessionId);
    }

    /**
     * Removes any references of the domain session id from the knowledge
     * sessions This should be called internally for things such as if the
     * domain session terminates abnormally or when the domain session is being
     * cleaned up.
     *
     * @param domainSessionId the domain session id to use as a look up for any
     *        registered knowledge sessions known to this class.
     */
    public void cleanupDomainSession(int domainSessionId) {

        boolean sessionChanged = false;
        // Remove the domain session from any existing team session (non host).
        Integer hostDsId = isMemberOfTeamKnowledgeSession(domainSessionId);
        if (hostDsId != null) {
            try {
                leaveTeamSession(hostDsId, domainSessionId);
                sessionChanged = true;
            } catch (@SuppressWarnings("unused") Exception e) {
                // Ignore any errors here.

            }
        }

        // Remove the knowledge session (if it exists).
        if (removeKnowledgeSession(domainSessionId) != null) {
            sessionChanged = true;
        }

        // Only send the notification out if there was a change
        if (sessionChanged) {
            if(eventListener != null){
                eventListener.notifyTutorKnowledgeSessionsUpdated(domainSessionId);
            }
        }
    }

    /**
     * Remove a learner who joined a team session (and the learner was not the host of the team session).
     *
     * @param domainSessionIdOfHost the domain session id of the host of a team knowledge session.
     * @param domainSessionIdOfLeavingSession the domain session id of the session leaving the team session.
     * @return true if the joined session was found and removed from the team session
     */
    public boolean leaveTeamSession(int domainSessionIdOfHost, int domainSessionIdOfLeavingSession){

        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(domainSessionIdOfHost);
        if(kSession != null && kSession instanceof TeamKnowledgeSession){
            TeamKnowledgeSession teamKnowledgeSession = (TeamKnowledgeSession)kSession;

            if(logger.isDebugEnabled()){
                logger.debug("Removing team member in domain session id "+domainSessionIdOfLeavingSession+" from team knowledge session of "+teamKnowledgeSession);
            }

            if(teamKnowledgeSession.getJoinedMembers().remove(domainSessionIdOfLeavingSession) != null){
                domainSessionIdToHostDomainSessionId.remove(domainSessionIdOfLeavingSession);
                return true;
            }

        }

        return false;
    }

    /**
     * Assign an already joined session a team member position.
     *
     * @param domainSessionIdOfHost the domain session id of the host of a team
     *        knowledge session
     * @param domainSessionIdOfJoinedSession the domain session id of a joined
     *        team session that wants to take a specific team member position.
     * @param teamMemberPosition the team member position to assign to the
     *        joined domain session. Can't be null. Use unassignTeamMember
     *        method to remove team member position for a domain session.
     * @return true if the team member was assigned to the joined session.
     * @throws ManageTeamMembershipException if the hosted session could not be
     *         found.
     */
    public boolean assignTeamMember(int domainSessionIdOfHost, int domainSessionIdOfJoinedSession, TeamMember<?> teamMemberPosition) throws ManageTeamMembershipException{

        if(teamMemberPosition == null){
            throw new IllegalArgumentException("The team member position can't be null");
        }

        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(domainSessionIdOfHost);
        if(kSession != null && kSession instanceof TeamKnowledgeSession){
            TeamKnowledgeSession teamKnowledgeSession = (TeamKnowledgeSession)kSession;

            SessionMember sessionMember = null;
            if (domainSessionIdOfHost == domainSessionIdOfJoinedSession) {
                sessionMember = teamKnowledgeSession.getHostSessionMember();
            } else {
                sessionMember = teamKnowledgeSession.getJoinedMembers().get(domainSessionIdOfJoinedSession);
            }

            if(sessionMember == null){
                //must join before being allowed to assign team member position
                String errorMsg = "Unable select role of " + teamMemberPosition.getName() + " because a session could not be found.";
                // This error should be logged.
                logger.error(errorMsg);
                throw new ManageTeamMembershipException(ACTION_TYPE.ASSIGN_TEAM_MEMBER, errorMsg);
            }else if(teamKnowledgeSession.getHostSessionMember().getSessionMembership() instanceof GroupMembership){
                // assigning team members one by one for different domain sessions is not supported when the host is using group membership
                
                String errorMsg = "Unable select role of " + teamMemberPosition.getName() + " because the host session member ship is of type "+GroupMembership.class.getName()+" which doesn't support adding multiple learner sessions.";
                throw new ManageTeamMembershipException(ACTION_TYPE.ASSIGN_TEAM_MEMBER, errorMsg);
            }
            
            // At this point the remaining logic can rely on the host session membership being an individual membership
            IndividualMembership hostIndividualMembership = (IndividualMembership) teamKnowledgeSession.getHostSessionMember().getSessionMembership();

            if (teamMemberPosition.equals(hostIndividualMembership.getTeamMember()) &&
                    domainSessionIdOfHost != domainSessionIdOfJoinedSession) {
                // another learner is trying to be assigned the same role as the host learner
                String errorMsg = "Unable to select role of " + teamMemberPosition.getName() + " because it is already assigned to the host.";
                if (logger.isDebugEnabled()) {
                    logger.debug(errorMsg);
                }
                throw new ManageTeamMembershipException(ACTION_TYPE.ASSIGN_TEAM_MEMBER, errorMsg);
            }

            // Check to see if the other members are not assigned already to the role.
            for (SessionMember member : teamKnowledgeSession.getJoinedMembers().values()) {

                if (teamMemberPosition.equals(member.getSessionMembership().getTeamMember()) && !member.equals(sessionMember)) {
                    String errorMsg = "Unable to select role of " + teamMemberPosition.getName() + " because it is already assigned to '"+member.getSessionMembership().getUsername()+"'.";
                    if (logger.isDebugEnabled()) {
                        logger.debug(errorMsg);
                    }

                    throw new ManageTeamMembershipException(ACTION_TYPE.ASSIGN_TEAM_MEMBER, errorMsg);
                }
            }
            
            IndividualMembership sessionMemberMembership = (IndividualMembership) sessionMember.getSessionMembership();
            TeamMember<?> currentTeamMember = sessionMemberMembership.getTeamMember();
            if(currentTeamMember != null){

                if (logger.isDebugEnabled()) {
                    logger.debug("successfully unassigned domain session id: " + domainSessionIdOfJoinedSession + ", from team member: "+ currentTeamMember);
                }

                // Unassign the team member to the role before assigning to the new one.
                unassignTeamMember(domainSessionIdOfHost,  domainSessionIdOfJoinedSession);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("successfully assigned domain session id: " + domainSessionIdOfJoinedSession + ", to team member: "+ teamMemberPosition);
            }

            sessionMemberMembership.setTeamMember(teamMemberPosition);
            return true;

        }


        String errorMsg = "Unable to select role of " + teamMemberPosition.getName() + ", because the host session could not be found.";
        logger.error(errorMsg);
        throw new ManageTeamMembershipException(ACTION_TYPE.ASSIGN_TEAM_MEMBER, errorMsg);
    }

    /**
     * Unassign an already joined session and assigned team member position.
     *
     * @param domainSessionIdOfHost the domain session id of the host of a team knowledge session
     * @param domainSessionIdOfJoinedSession the domain session id of a joined team session that wants to no
     * longer be assigned to a specific team member position.
     * @return true if the joined session is no longer assigned to any team member position
     * @throws ManageTeamMembershipException if the hosted session could not be found
     */
    public boolean unassignTeamMember(int domainSessionIdOfHost, int domainSessionIdOfJoinedSession) throws ManageTeamMembershipException{

        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(domainSessionIdOfHost);
        if(kSession != null && kSession instanceof TeamKnowledgeSession){
            TeamKnowledgeSession teamKnowledgeSession = (TeamKnowledgeSession)kSession;

            SessionMember sessionMember = null;
            if (domainSessionIdOfHost == domainSessionIdOfJoinedSession) {
                sessionMember = teamKnowledgeSession.getHostSessionMember();
            } else {
                sessionMember = teamKnowledgeSession.getJoinedMembers().get(domainSessionIdOfJoinedSession);
            }

            if(sessionMember == null){
                //session isn't a member so nothing to unassign
                return true;
            }else if(teamKnowledgeSession.getHostSessionMember().getSessionMembership() instanceof GroupMembership){
                // unassigning team members one by one for different domain sessions is not supported when the host is using group membership
                
                String errorMsg = "Unable to unassign role for session member of domain session "+domainSessionIdOfJoinedSession+" because the host session member ship is of type "+GroupMembership.class.getName()+" which doesn't support adding multiple learner sessions.";
                throw new ManageTeamMembershipException(ACTION_TYPE.ASSIGN_TEAM_MEMBER, errorMsg);
            }

            IndividualMembership sessionMemberMembership = (IndividualMembership) sessionMember.getSessionMembership();
            TeamMember<?> teamMember = sessionMemberMembership.getTeamMember();
            if(teamMember == null){
                //not currently assigned a team member position so nothing to unassign
                return true;
            }else{
                // unassign
                if (logger.isDebugEnabled()) {
                    logger.debug("successfully unassigned domain session id: " + domainSessionIdOfJoinedSession + ", old team info was: "+ teamMember);
                }
                sessionMemberMembership.setTeamMember(null);
                return true;
            }
        }

        String errorMsg = "Unable to unassign team member because domain id: " + domainSessionIdOfJoinedSession + ", could not find a hosted session.";
        logger.error(errorMsg);
        throw new ManageTeamMembershipException(ACTION_TYPE.UNASSIGN_TEAM_MEMBER, errorMsg);
    }

    /**
     * Change the name of a team session.
     *
     * @param domainSessionIdOfHost the domain session id of the host of a team knowledge session.
     * @param newSessionName the new name to give the team knowledge session
     * @return true if the team session was found and had its name changed
     */
    public boolean changeTeamSessionName(int domainSessionIdOfHost, String newSessionName){

        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(domainSessionIdOfHost);
        if(kSession != null && kSession instanceof TeamKnowledgeSession){
            TeamKnowledgeSession teamKnowledgeSession = (TeamKnowledgeSession)kSession;

            if(logger.isDebugEnabled()){
                logger.debug("Changing the name of the team knowledge session "+teamKnowledgeSession);
            }

            teamKnowledgeSession.setNameOfSession(newSessionName);

            return true;
        }

        return false;
    }

    /**
     * Return the current collection of knowledge sessions, both for individual and teams.
     *
     * @return mapping of unique domain session id to the information about a single real time assessment (DKF)
     * which may include team members that have joined the session.
     */
    public Map<Integer, AbstractKnowledgeSession> getKnowledgeSessions(){
        return knowledgeSessionMap;
    }

    /**
     * Return a filtered collection of knowledge sessions based on the request.
     *
     * @param request contains filter options for knowledge sessions known to this class.
     * @return mapping of unique domain session id to the information about a single real time assessment (DKF)
     * which may include team members that have joined the session.
     */
    public Map<Integer, AbstractKnowledgeSession> getKnowledgeSessions(KnowledgeSessionsRequest request){

        if(request == null ||
                request.isFullTeamSessions() && request.isIndividualSessions() && request.isRunningSessions() && (request.getCourseIds() == null || request.getCourseIds().isEmpty())){
            // treat this as a get all knowledge sessions
            return getKnowledgeSessions();
        }

        Map<Integer, AbstractKnowledgeSession> filteredMap = new HashMap<>();
        Iterator<Integer> keyItr = knowledgeSessionMap.keySet().iterator();
        while(keyItr.hasNext()){
            Integer key = keyItr.next();
            AbstractKnowledgeSession aKnowledgeSession = knowledgeSessionMap.get(key);

            if(aKnowledgeSession instanceof TeamKnowledgeSession){

                TeamKnowledgeSession teamSession = (TeamKnowledgeSession)aKnowledgeSession;
                if(!request.isFullTeamSessions() && teamSession.getJoinedMembers().size() >= teamSession.getTotalPossibleTeamMembers()){
                    //filter out full team sessions
                    continue;
                }
            }else if(!request.isIndividualSessions()){
                //filter out individual sessions
                continue;
            }

            if(!request.isRunningSessions() && aKnowledgeSession.isSessionRunning()){
                //filter out running sessions
                continue;
            }

            if(request.getCourseIds() != null && !request.getCourseIds().isEmpty() && !request.getCourseIds().contains(aKnowledgeSession.getCourseSourceId())){
                //filter out by course id
                continue;
            }

            filteredMap.put(key, aKnowledgeSession);
        }
        return filteredMap;
    }

    /**
     * Return whether the domain session is the host of a team session.
     *
     * @param dsId the domain session to check
     * @return true if the domain session is the host of a team session
     */
    public boolean isHostOfTeamKnowledgeSession(int dsId){

        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(dsId);
        return kSession != null && kSession instanceof TeamKnowledgeSession;
    }

    /**
     * Return whether the domain session id is a member (non host) of a team session.
     *
     * @param dsId the domain session to check
     * @return the domain session id of the host session (if found), null otherwise
     */
    public Integer isMemberOfTeamKnowledgeSession(int dsId) {
        Integer hostDsId = null;

        Iterator<Integer> keyItr = knowledgeSessionMap.keySet().iterator();
        while(keyItr.hasNext()){
            Integer key = keyItr.next();
            AbstractKnowledgeSession aKnowledgeSession = knowledgeSessionMap.get(key);
            if (aKnowledgeSession != null && aKnowledgeSession instanceof TeamKnowledgeSession) {
                TeamKnowledgeSession teamKnowledgeSession = (TeamKnowledgeSession)aKnowledgeSession;

                if (teamKnowledgeSession.getJoinedMembers().get(dsId) != null) {
                    hostDsId = key;
                    break;
                }
            }
        }
        return hostDsId;
    }

    /**
     * Return information about the team members that have joined a team session.
     *
     * @param domainSessionIdOfHost the domain session id of a team session host.
     * @return the team members for a team session mapped by domain session id.  Will be null if the domain session id provided
     * isn't mapped to a team session.  Will be empty if the team session has no members that have joined yet.
     * The host will not be in this collection.
     */
    public Map<Integer, SessionMember> getTeamKnowledgeSessionsMembersForHost(int domainSessionIdOfHost){

        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(domainSessionIdOfHost);
        if(kSession != null && kSession instanceof TeamKnowledgeSession){
            TeamKnowledgeSession teamKnowledgeSession = (TeamKnowledgeSession)kSession;
            return teamKnowledgeSession.getJoinedMembers();
        }

        return null;
    }
    
    /**
     * Return information about the members that are in this session.
     * 
     * @param domainSessionIdOfHost the domain session id of a team session host to get the session members for.
     * @return the session member information.  Will be null if the domain session id is not that of a session host at this time.
     */
    public SessionMembers getKnowledgeSessionMembers(int domainSessionIdOfHost){
        
        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(domainSessionIdOfHost);
        if(kSession != null){
            return kSession.getSessionMembers();
        }
        
        return null;
    }

    /**
     * Return the current team member position for the learner in the domain session.
     *
     * @param domainSessionId the domain session for a learner.  This can also be the host domain session id.
     * @return the team member currently assigned to the learner in the domain session.  Can be null if the
     * learner hasn't been associated with a team member position yet or the learner hasn't joined a team session.
     */
    public TeamMember<?> getTeamMemberForDomainSession(int domainSessionId){

        AbstractKnowledgeSession aKnowledgeSession = null;

        Integer hostDomainSessionId = domainSessionIdToHostDomainSessionId.get(domainSessionId);
        if(hostDomainSessionId == null){
            // the domain session is hosting or isn't known about
            
            aKnowledgeSession = knowledgeSessionMap.get(domainSessionId);
            if(aKnowledgeSession == null){
                return null;
            }
            SessionMember hostSessionMember = aKnowledgeSession.getHostSessionMember();
            if(hostSessionMember.getDomainSessionId() != domainSessionId){
                // the provided domain session id is not the host and not associated with another hosted domain session
                return null;
            }else if(hostSessionMember.getSessionMembership() instanceof GroupMembership){
                // a single domain session is hosting a group of individuals (learners) with no single host
                return null;
            }else{
                // an individual session membership
                return hostSessionMember.getSessionMembership().getTeamMember();
            }

        }else{
            // the domain session is not hosting, get the hosters knowledge session
            aKnowledgeSession = knowledgeSessionMap.get(hostDomainSessionId);
        }

        if(aKnowledgeSession == null){
            return null;
        }

        if(aKnowledgeSession instanceof IndividualKnowledgeSession){
            // individual knowledge sessions means the domain session id provided should be the host of the session,
            // i.e. the ids should match.  If they don't then the requested domain session id is not part of the knowledge
            // session and the map is incorrect.
            IndividualKnowledgeSession iKnowledgeSession = (IndividualKnowledgeSession)aKnowledgeSession;
            SessionMember sessionMember = iKnowledgeSession.getHostSessionMember();
            return sessionMember.getDomainSessionId() == domainSessionId ? sessionMember.getSessionMembership().getTeamMember() : null;

        }else if(aKnowledgeSession instanceof TeamKnowledgeSession){

            TeamKnowledgeSession tKnowledgeSession = (TeamKnowledgeSession)aKnowledgeSession;
            SessionMember sessionMember = tKnowledgeSession.getJoinedMembers().get(domainSessionId);
            if(sessionMember != null){
                return sessionMember.getSessionMembership().getTeamMember();
            }
        }

        return null;
    }

    /**
     * Validates the knowledge session (by host domain session id) prior to starting the session
     * to ensure that it can be properly started.
     *
     * @param hostDomainSessionId The domain session id of the host (to get the knowledge session)
     * @return null if the session is valid and ready to be started, otherwise it will contain an error
     * message about why the session isnt valid.
     */
    public String validateKnowledgeSessionForStart(int hostDomainSessionId) {

        // Session is valid for starting if all users have selected a role.
        AbstractKnowledgeSession kSession = knowledgeSessionMap.get(hostDomainSessionId);
        if(kSession != null && kSession instanceof TeamKnowledgeSession){
            TeamKnowledgeSession tKnowledgeSession = (TeamKnowledgeSession)kSession;

            if(kSession.getSessionType() != SessionType.ACTIVE_PLAYBACK){
                // Check the host - must be set and assigned a team member
                // EXCEPT when the host is playing back a session, in which case the host isn't an assigned team member in the team org.
                SessionMember hostMember = tKnowledgeSession.getHostSessionMember();
                if (hostMember == null){
                    return "The session's host user has not been set";
                }else if(hostMember.getSessionMembership().getTeamMember() == null){
                    return "The session's host user '"+hostMember.getSessionMembership().getUsername()+"' has not been assiged to a team member position.";
                }
            }

            // Check the members - any joined member must be assigned a team member
            for (SessionMember joinedMember : tKnowledgeSession.getJoinedMembers().values()) {

                if (joinedMember == null){
                    return "There is a joined member known to this knowledge session that is now null.";
                }else if(joinedMember.getSessionMembership().getTeamMember() == null){
                    return "The joined member '"+joinedMember.getSessionMembership().getUsername()+"' has not been assigned to a team member position.";
                }
            }

        } else {
            return "Failed to find a registered knowledge session with the domain session id "+hostDomainSessionId+" among "+knowledgeSessionMap.size()+" currently registered knowledge session(s).";
        }

        // Only valid if we get here
        return null;
    }

}
