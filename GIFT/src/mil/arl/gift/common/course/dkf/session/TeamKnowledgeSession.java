/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.enums.TrainingApplicationEnum;

/**
 * Contains information about a knowledge session designed for a team.
 *
 * @author mhoffman
 *
 */
public class TeamKnowledgeSession extends AbstractKnowledgeSession{

    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** the number of possible team members that this session can support */
    private int totalPossibleTeamMembers;
    
    /** used to keep track of joined members (including the host) as well as all members */
    private TeamSessionMembers teamSessionMembers;

    /** list of available team roles for this session. */
    private List<String> teamRoles;

    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private TeamKnowledgeSession(){}

    /**
     * Set attributes
     *
     * @param nameOfSession the name of the session, can be useful for showing
     *        hosted sessions to join. Can't be null or empty.
     * @param scenarioDescription the description of the scenario. Supports HTML. Can be null or empty.
     * @param courseInfo contains information about the course for this session.
     *        Can't be null.
     * @param totalPossibleTeamMembers the number of possible team members that
     *        this session can support, must be greater than 0.
     * @param hostSessionMember The {@link SessionMember} that is hosting the
     *        {@link TeamKnowledgeSession}. Can't be null.
     * @param roles the list of available team roles for this session
     * @param team the structure of the team for the session.  Should not be null.
     * @param nodeIdToNameMap the map of all the task/concept node ids to their
     *        names
     * @param trainingAppType the training application type of the application
     *        in use when this session was created
     * @param sessionType the enumerated type of session. Can't be null.
     * @param sessionStartTime the epoch start time of this session.
     * @param mission the mission data for this session. Can be null.
     * @param observerControls the data controlling how this session is
     *        presented to observer controllers. Can be null.
     */
    public TeamKnowledgeSession(String nameOfSession, String scenarioDescription, KnowledgeSessionCourseInfo courseInfo, int totalPossibleTeamMembers,
            SessionMember hostSessionMember, List<String> roles, Team team, Map<BigInteger, String> nodeIdToNameMap,
            TrainingApplicationEnum trainingAppType, SessionType sessionType, long sessionStartTime, Mission mission, ObserverControls observerControls){
        super(nameOfSession, scenarioDescription, courseInfo, hostSessionMember, team, nodeIdToNameMap, trainingAppType,
                sessionType, sessionStartTime, mission, observerControls);

        teamRoles = roles;
        setTotalPossibleTeamMembers(totalPossibleTeamMembers);

        teamSessionMembers = new TeamSessionMembers();
        teamSessionMembers.addSessionMember(hostSessionMember);
    }

    /**
     * Set attributes
     * @param nameOfSession the name of the session, can be useful for showing hosted sessions to join.  Can't be null or empty.
     * @param scenarioDescription the description of the scenario. Supports HTML. Can be null or empty.
     * @param courseInfo contains information about the course for this session. Can't be null.
     * @param totalPossibleTeamMembers the number of possible team members that this session can support, must be greater than 0.
     * @param hostMemberSession information about the learner hosting the team session
     * @param joinedMembers the collection of team members that have already joined this team session.  Can't be null but can be empty.
     * @param roles the list of available team roles for this session
     * @param team the structure of the team for the session. Should not be null.
     * @param nodeIdToNameMap the map of all the task/concept node ids to their names
     * @param trainingAppType the training application type of the application in use when this session was created
     * @param sessionType the enumerated type of session.  Can't be null.
     * @param sessionStartTime the epoch start time of this session.
     * @param mission the mission data for this session. Can be null.
     * @param observerControls the data controlling how this session is presented to observer controllers. Can be null.
     */
    public TeamKnowledgeSession(String nameOfSession, String scenarioDescription, KnowledgeSessionCourseInfo courseInfo, int totalPossibleTeamMembers,
            SessionMember hostMemberSession, Map<Integer, SessionMember> joinedMembers, List<String> roles, Team team,
            Map<BigInteger, String> nodeIdToNameMap, TrainingApplicationEnum trainingAppType, SessionType sessionType,
            long sessionStartTime, Mission mission, ObserverControls observerControls){
        super(nameOfSession, scenarioDescription, courseInfo, hostMemberSession, team, nodeIdToNameMap, trainingAppType, sessionType,
                sessionStartTime, mission, observerControls);

        teamRoles = roles;
        setTotalPossibleTeamMembers(totalPossibleTeamMembers);

        teamSessionMembers = new TeamSessionMembers(joinedMembers);
        teamSessionMembers.addSessionMember(hostMemberSession);
    }

    /**
     * Return the number of possible team members that this session can support
     * @return will be greater than zero
     */
    public int getTotalPossibleTeamMembers() {
        return totalPossibleTeamMembers;
    }

    /**
     * Gets the list of available team roles for this team session.
     *
     * @return List of team roles available (by role name).
     */
    public List<String> getTeamRoles() {
        return teamRoles;
    }

    /**
     * Set the number of possible team members that this session can support
     * @param totalPossibleTeamMembers must be greater than zero
     */
    private void setTotalPossibleTeamMembers(int totalPossibleTeamMembers) {

        if(totalPossibleTeamMembers <= 0){
            throw new IllegalArgumentException("The total possible team members must be a positive number");
        }

        this.totalPossibleTeamMembers = totalPossibleTeamMembers;
    }

    /**
     * Return the mapping of members by their individual, unique domain session IDs that have joined this team session.
     *
     * @return a READ-ONLY mapping of domain session id to information about a team member that joined the team session.
     * Doesn't include the host member.
     */
    public Map<Integer, SessionMember> getJoinedMembers() {
        return Collections.unmodifiableMap(teamSessionMembers.getJoinedMembersByDSId());
    }
    
    public boolean leaveSession(int domainSessionIdOfLeavingSession){        
        return teamSessionMembers.leaveSession(domainSessionIdOfLeavingSession);
    }

    /**
     * Add a learner to the team session.
     *
     * @param memberToAdd information about a member to add to this team session.  Can't be null.
     */
    public void joinSession(SessionMember memberToAdd) {

        if(memberToAdd == null){
            throw new IllegalArgumentException("The member to add can't be null");
        }else if(getSessionMembers().getNumOfMembers() >= totalPossibleTeamMembers && getSessionType() != SessionType.ACTIVE_PLAYBACK){
            // Note: in active_playback the learner running the course with the playback also joins the session but isn't assigned
            // a team member position.  Therefore this session host shouldn't be counted here.
            throw new IllegalArgumentException("Unable to join the session.  The session is full at "+totalPossibleTeamMembers + " team members.");
        }else if(getSessionMembers().containsMember(memberToAdd.getDomainSessionId())){
            throw new IllegalArgumentException("This domain session ("+memberToAdd.getDomainSessionId()+") has already joined the team knowledge session.");
        }

        teamSessionMembers.joinSession(memberToAdd);
    }    

    @Override
    public SessionMembers getSessionMembers() {
        return teamSessionMembers;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + (getJoinedMembers() == null ? 0 : getJoinedMembers().hashCode());
        result = prime * result + totalPossibleTeamMembers;
        result = prime * result + teamRoles.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof TeamKnowledgeSession)) {
            return false;
        } else if (!super.equals(obj)) {
            return false;
        }

        TeamKnowledgeSession other = (TeamKnowledgeSession) obj;

        if (totalPossibleTeamMembers != other.getTotalPossibleTeamMembers()) {
            return false;
        }

        if (getJoinedMembers() == null) {
            if (other.getJoinedMembers() != null) {
                return false;
            }
        } else if (!getJoinedMembers().equals(other.getJoinedMembers())) {
            return false;
        }

        if (teamRoles == null ) {
            if (other.getTeamRoles() != null) {
                return false;
            }
        } else if (!teamRoles.equals(other.getTeamRoles())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[TeamKnowledgeSession: ");
        builder.append(super.toString());
        builder.append(", totalPossibleTeamMembers = ");
        builder.append(totalPossibleTeamMembers);
        builder.append(",\njoinedMembers = {");
        for(SessionMember teamMember : getJoinedMembers().values()){
            builder.append("\n").append(teamMember);
        }

        builder.append("}");
        builder.append(",\nteamRoles = {");
        for (String role : teamRoles) {
            builder.append("\n").append(role);
        }

        builder.append("}");
        builder.append("]");
        return builder.toString();
    }
    
    /**
     * Contains information about the joined team members to this session as well as
     * all the members that are in this session.
     * 
     * @author mhoffman
     *
     */
    public static class TeamSessionMembers extends SessionMembers{        

        /**
         * default
         */
        private static final long serialVersionUID = 1L;
        
        /**
         * mapping of domain session id to information about a team member that joined the team session
         * Doesn't include the host member.
         */
        private Map<Integer, SessionMember> joinedMembersbyDSId;
        
        /**
         * constructor - required for GWT serialization
         */
        public TeamSessionMembers(){
            joinedMembersbyDSId = new HashMap<>();
        }
        
        /**
         * Constructor - set the joined members
         * @param joinedMembersbyDSId mapping of domain session id to information about a team member that joined the team session
         * Doesn't include the host member.  Can't be null.
         */
        public TeamSessionMembers(Map<Integer, SessionMember> joinedMembersbyDSId){
            
            this();
            
            if(joinedMembersbyDSId == null){
                throw new IllegalArgumentException("The joined members can't be null");
            }
            
            /* Need to ensure that members added by this constructor are also added to the
             * superclass, otherwise scorers may miss usernames */
            for(SessionMember member : joinedMembersbyDSId.values()) {
                joinSession(member);
            }
        }
        
        /**
         * Remove the specified domain session from this team session.
         * 
         * @param domainSessionIdOfLeavingSession domain session id of the joined session that wants
         * to leave.
         * @return true if that domain session was found to be a joined session.
         */
        public boolean leaveSession(int domainSessionIdOfLeavingSession){
            
            // first remove it from the team session as a joined session
            SessionMember sessionMemberLeaving = joinedMembersbyDSId.remove(domainSessionIdOfLeavingSession);
            if(sessionMemberLeaving == null){
                return false;
            }
            
            // remove it from the collection of all members in this session as well
            removeSessionMember(domainSessionIdOfLeavingSession);
            
            return true;
        }
        
        /**
         * Add a learner to the team session.
         *
         * @param memberToAdd information about a member to add to this team session.  Can't be null.
         */
        private void joinSession(SessionMember memberToAdd) {
            this.joinedMembersbyDSId.put(memberToAdd.getDomainSessionId(), memberToAdd);
            super.addSessionMember(memberToAdd);
        }  
        
        /**
         * Return the mapping of members by their individual, unique domain session IDs that have joined this team session.
         *
         * @return mapping of domain session id to information about a team member that joined the team session.
         * Doesn't include the host member.
         */
        public Map<Integer, SessionMember> getJoinedMembersByDSId() {
            return Collections.unmodifiableMap(joinedMembersbyDSId);
        }
        
        @Override
        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append("[TeamSessionMembers:\n");
            for(Integer dsid : joinedMembersbyDSId.keySet()){
                sb.append(dsid).append(":").append(joinedMembersbyDSId.get(dsid)).append("\n");
            }
            sb.append("]");
            return sb.toString();
        }
    }

}
