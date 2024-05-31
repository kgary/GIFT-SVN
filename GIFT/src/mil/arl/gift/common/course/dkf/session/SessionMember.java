/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import mil.arl.gift.common.UserSession;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * Contains information about a learner being assessed in real time.
 *
 * @author mhoffman
 *
 */
public class SessionMember implements Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** contains user identifiable information for this session member */
    private UserSession userSession;
    
    /** the unique domain session id for the course this learner is in */
    private int domainSessionId;

    /** information about the type of membership this session member is using */
    private SessionMembership sessionMembership;

    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private SessionMember() {
    }

    /**
     * Set attributes .
     *
     * @param sessionMembership information about the type of membership this session member is using. Can't be null.
     * @param userSession user identifiable information for the session member. Can't be null.
     * @param domainSessionId the unique domain session id for the course this
     *        learner is in. Must be positive.
     */
    public SessionMember(SessionMembership sessionMembership, UserSession userSession, int domainSessionId) {

        setSessionMembership(sessionMembership);
        setUserSession(userSession);
        setDomainSessionId(domainSessionId);
    }

    /**
     * Set the information about the type of membership this session member is using
     * @param sessionMembership can't be null.
     */
    private void setSessionMembership(SessionMembership sessionMembership){

        if(sessionMembership == null){
            throw new IllegalArgumentException("Session membership can't be null");
        }

        this.sessionMembership = sessionMembership;
    }

    /**
     * Return the information about the type of membership this session member is using
     * @return won't be null.
     */
    public SessionMembership getSessionMembership(){
        return sessionMembership;
    }
    
    /**
     * Set the user identifiable information for this session member
     * @param userSession information about this member.  Can't be null.
     */
    public void setUserSession(UserSession userSession){
        
        if(userSession == null){
            throw new IllegalArgumentException("The user session can't be null");
        }
        
        this.userSession = userSession;
    }
    
    /**
     * Return the user identifiable information for this session member
     * @return user session information for this member. Won't be null.
     */
    public UserSession getUserSession(){
        return userSession;
    }
    
    /**
     * Return the unique domain session id for the course this learner is in
     *
     * @return domain session id, will be greater than 0
     */
    public int getDomainSessionId() {
        return domainSessionId;
    }

    /**
     * Set the unique domain session id for the course this learner is in
     *
     * @param domainSessionId the domain session id
     */
    private void setDomainSessionId(int domainSessionId) {
        if (domainSessionId <= 0) {
            throw new IllegalArgumentException("The domain session id must be a positive number");
        }

        this.domainSessionId = domainSessionId;
    }

    @Override
    public int hashCode() {
        return userSession.hashCode() + this.domainSessionId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof SessionMember)) {
            return false;
        }

        SessionMember other = (SessionMember) obj;

        if (!other.getSessionMembership().equals(other.getSessionMembership())) {
            return false;
        }

        if (!userSession.equals(other.getUserSession())) {
            return false;
        }
        
        if (domainSessionId != other.getDomainSessionId()) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[SessionMember: ");
        builder.append("userSession = ");
        builder.append(userSession);
        builder.append(", domainSessionId = ");
        builder.append(domainSessionId);
        builder.append(", membership = ");
        builder.append(sessionMembership);

        builder.append("]");
        return builder.toString();
    }

    /**
     * Used to group one or more individuals into a single session member.  This
     * is currently only used in RTA lesson level mode when the team membership is
     * defined through the Gateway module with a single external system request.
     *
     * @author mhoffman
     *
     */
    public static class GroupMembership implements SessionMembership, Serializable{

        /**
         * default
         */
        private static final long serialVersionUID = 1L;
        
        /** 
         * The username of the user hosting the team knowledge session. A consistent username is needed to ensure
         * that session data is not filtered out in Game Master.
         */
        private String username;

        /**
         * collection of individual in this group
         */
        private Set<IndividualMembership> members;
        
        /**
         * Private, no-argument constructor needed for GWT serialization
         */
        private GroupMembership(){}

        /**
         * Create empty members collection.
         * 
         * @param username the username of the user hosting the team knowledge session. If null, the username
         * of the first member will be used as the host username of the group to support decoding legacy messages
         * that use an older version of this class.
         */
        public GroupMembership(String username){
            this();
            
            this.username = username;
            members = new HashSet<>();
        }

        /**
         * Return the collection of individuals in this group membership.
         *
         * @return won't be null but can be empty if no individuals have been added.
         */
        public Set<IndividualMembership> getMembers(){
            return members;
        }

        @Override
        public String getUsername() {
            
            if(username != null) {
                return username;
            }
            
            /* if a legacy message without a username was used to generate this instance, 
             * then use the first member's username as the host username as a fallback */
            Iterator<IndividualMembership> itr = members.iterator();
            if(itr.hasNext()){
                IndividualMembership individualMembership = itr.next();
                return individualMembership.getUsername();
            }

            return null;
        }

        @Override
        public TeamMember<?> getTeamMember() {

            Iterator<IndividualMembership> itr = members.iterator();
            if(itr.hasNext()){
                IndividualMembership individualMembership = itr.next();
                return individualMembership.getTeamMember();
            }
            return null;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (username == null ? 0 : username.hashCode());
            for(IndividualMembership iMembership : members){
                result = prime * result + iMembership.hashCode();
            }
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof GroupMembership)) {
                return false;
            }

            GroupMembership other = (GroupMembership) obj;
            
            if (!StringUtils.equalsIgnoreCase(username, other.getUsername())) {
                return false;
            }

            if (!CollectionUtils.equals(this.getMembers(), other.getMembers())) {
                return false;
            }

            return true;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[GroupMembership: username = ")
                .append(username)
                .append(", members={\n");
            StringUtils.join(",\n", members, builder);
            builder.append("}\n");
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * Represents a single individual in a knowledge session.  An individual has a username
     * that identifies the user in GIFT and possibly an external system.
     *
     * @author mhoffman
     *
     */
    public static class IndividualMembership implements SessionMembership, Serializable{

        /**
         * default
         */
        private static final long serialVersionUID = 1L;

        /** the username of a learner being assessed in a knowledge session */
        private String username;

        /** The selected team member position for this session member.
         *  Null if a team member position hasn't been associated with this user.
         */
        private TeamMember<?> teamMember;

        /**
         * Required for serialization.
         */
        @SuppressWarnings("unused")
        private IndividualMembership(){}

        /**
         * Set attribute(s)
         *
         * @param username the username of a learner being assessed in a knowledge session. Can't be null or empty.
         */
        public IndividualMembership(String username){
            setUsername(username);
        }

        @Override
        public String getUsername() {
            return username;
        }

        /**
         * Set the username of a learner being assessed in a knowledge session.
         *
         * @param memberName Can't be null or empty.
         */
        public void setUsername(String memberName) {

            if(StringUtils.isBlank(memberName)){
                throw new IllegalArgumentException("The member name can't be null or empty");
            }
            this.username = memberName;
        }

        @Override
        public TeamMember<?> getTeamMember() {
            return teamMember;
        }

        /**
         * Set the selected team member position for this session member.
         *
         * @param teamMember can be null if un-assigning.
         */
        public void setTeamMember(TeamMember<?> teamMember) {
            this.teamMember = teamMember;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else if (obj == null) {
                return false;
            } else if (!(obj instanceof IndividualMembership)) {
                return false;
            }

            IndividualMembership other = (IndividualMembership) obj;

            if (!StringUtils.equalsIgnoreCase(username, other.getUsername())) {
                return false;
            }

            return true;
        }


        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (username == null ? 0 : username.hashCode());
            return result;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[IndividualMembership: memberName = ");
            builder.append(username);
            builder.append(", teamMember = ");
            builder.append(teamMember);
            builder.append("]");
            return builder.toString();
        }
    }

    /**
     * Interface used by membership classes.
     * @author mhoffman
     *
     */
    public interface SessionMembership{

        /**
         * Return the username of a learner being assessed in a knowledge session.
         * This is not the team member name.
         * In legacy versions of GroupMembership, this value is the first IndividualMembership in the group.
         *
         * @return the username of the learner. Can be null if a message containing a
         * legacy version of GroupMembership with no members is decoded.
         */
        public String getUsername();

        /**
         * The selected team member position for this session member.
         * In GroupMembership this value is the first IndividualMembership in the group.
         *
         * @return can be null if the learner hasn't selected (or been assigned) a
         *         team member position yet.
         */
        public TeamMember<?> getTeamMember();
    }
}
