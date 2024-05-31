/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.io.Serializable;

import mil.arl.gift.common.course.dkf.session.SessionMember.GroupMembership;
import mil.arl.gift.common.util.StringUtils;

/**
 * Used to manage a learners membership to a team knowledge session.
 * 
 * @author mhoffman
 *
 */
public class ManageTeamMembershipRequest implements Serializable {

    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** 
     * the domain session id of a team knowledge session host .
     */
    private int domainSessionIdOfHost;
    
    /**
     * the unique team member name the learner wants to play (or leave) in the team organization of a real time assessment
     */
    private String memberName;
       
    /**
     * the authored name of a team knowledge session that will be created or destroyed by the learner.
     */
    private String nameOfSessionToCreateOrDestroy;
    
    /**
     * the type of action to take for this membership request
     */
    private ACTION_TYPE actionType;
    
    /**
     * collection of session members (team member assignments to learners) to apply ONLY for SET_TEAM_MEMBERS_GROUP_SESSION
     */
    private GroupMembership groupSessionMembership;
    
    /**
     * types of actions that can be performed for team knowledge sessions
     * @author mhoffman
     *
     */
    public enum ACTION_TYPE{
        CREATE_TEAM_SESSION,
        DESTROY_TEAM_SESSION,
        JOIN_TEAM_SESSION,
        LEAVE_TEAM_SESSION,
        ASSIGN_TEAM_MEMBER,
        UNASSIGN_TEAM_MEMBER,
        CHANGE_TEAM_SESSION_NAME,
        SET_TEAM_MEMBERS_GROUP_SESSION // used in RTA lesson level by the Gateway module to assign multiple team members in a single domain session
    }
    
    /**
     * Create a request for hosting a team knowledge session AND assigning team member positions.
     * 
     *  @param domainSessionIdOfHost the domain session id for the domain session creating a team knowledge session
     * @param nameOfSessionToCreate the authored name of a team knowledge session that will be created by the learner.
     * @param groupMembership collection of team member assignments to apply to the session.  Includes the host's team member assignment.
     * @return the resulting new request with the necessary attributes set
     */
    public static ManageTeamMembershipRequest createHostedTeamKnowledgeSessionRequest(int domainSessionIdOfHost, 
            String nameOfSessionToCreate, GroupMembership groupMembership){
     
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setNameOfSessionToCreateOrDestroy(nameOfSessionToCreate);
        request.setSessionMembers(groupMembership);
        request.setActionType(ACTION_TYPE.SET_TEAM_MEMBERS_GROUP_SESSION);
        return request;
    }
    
    /**
     * Create a request for hosting a team knowledge session.
     * 
     * @param domainSessionIdOfHost the domain session id for the domain session creating a team knowledge session
     * @param nameOfSessionToCreate the authored name of a team knowledge session that will be created by the learner.
     * @return the resulting new request with the necessary attributes set
     */
    public static ManageTeamMembershipRequest createHostedTeamKnowledgeSessionRequest(int domainSessionIdOfHost, String nameOfSessionToCreate){
        
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setNameOfSessionToCreateOrDestroy(nameOfSessionToCreate);
        request.setActionType(ACTION_TYPE.CREATE_TEAM_SESSION);
        return request;
    }
    
    /**
     * Create a request for destroying a hosted team knowledge session.
     * 
     * @param domainSessionIdOfHost the domain session id for the domain session destroying a team knowledge session that the domain session
     * id is currently hosting
     */
    public static ManageTeamMembershipRequest createDestroyTeamKnowledgeSessionRequest(int domainSessionIdOfHost){
        
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setActionType(ACTION_TYPE.DESTROY_TEAM_SESSION);
        return request;
    }
    
    /**
     * Create a request for joining a hosted team knowledge session.
     * 
     * @param domainSessionIdOfHost the domain session id for the domain session hosting a team knowledge session that another domain session
     * wants to join.
     */
    public static ManageTeamMembershipRequest createJoinTeamKnowledgeSessionRequest(int domainSessionIdOfHost){
        
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setActionType(ACTION_TYPE.JOIN_TEAM_SESSION);
        return request;
    }
    
    /**
     * Create a request for leaving a hosted team knowledge session.  This is not for the host of a knowledge session.
     * 
     * @param domainSessionIdOfHost the domain session id for the domain session hosting a team knowledge session that another domain session
     * wants to leave.
     */
    public static ManageTeamMembershipRequest createLeaveTeamKnowledgeSessionRequest(int domainSessionIdOfHost){
        
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setActionType(ACTION_TYPE.LEAVE_TEAM_SESSION);
        return request;
    }
    
    /**
     * Create a request for changing a hosted team knowledge session's name.
     * 
     * @param domainSessionIdOfHost the domain session id for the domain session hosting the team knowledge session whose name
     * will be changed.
     * @param newSessionName the new name to give the session
     */
    public static ManageTeamMembershipRequest createChangeTeamKnowledgeSessionNameRequest(int domainSessionIdOfHost, String newSessionName){
        
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setNameOfSessionToCreateOrDestroy(newSessionName);
        request.setActionType(ACTION_TYPE.CHANGE_TEAM_SESSION_NAME);
        return request;
    }
    
    /**
     * Create a request for assigning a team member position to a learner who has already joined (or is the host) of
     * a hosted team knowledge session.  
     * 
     * @param domainSessionIdOfHost the domain session id for the domain session hosting a team knowledge session where the learner
     * wants to play a team member position.
     * @param teamMemberName the unique team member name the learner wants to play in the team organization of a real time assessment. 
     * Can't be null or empty.
     */
    public static ManageTeamMembershipRequest createAssignTeamMemberTeamKnowledgeSessionRequest(int domainSessionIdOfHost, String teamMemberName){
        
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setTeamMemberName(teamMemberName);
        request.setActionType(ACTION_TYPE.ASSIGN_TEAM_MEMBER);
        return request;
    }
    
    /**
     * Create a request for un-assigning a team member position to a learner who has already joined (or is the host) of
     * a hosted team knowledge session.  
     * 
     * @param domainSessionIdOfHost the domain session id for the domain session hosting a team knowledge session where the learner
     * wants to no longer play a team member position
     * @param teamMemberName the unique team member name the learner wants to no longer play in the team organization of a real time assessment. 
     * Can't be null or empty.
     */
    public static ManageTeamMembershipRequest createUnassignTeamMemberTeamKnowledgeSessionRequest(int domainSessionIdOfHost, String teamMemberName){
        
        ManageTeamMembershipRequest request = new ManageTeamMembershipRequest();
        request.setDomainSessionIdOfHost(domainSessionIdOfHost);
        request.setTeamMemberName(teamMemberName);
        request.setActionType(ACTION_TYPE.UNASSIGN_TEAM_MEMBER);
        return request;
    }
    
    /**
     * Used by factory methods.
     * Required for GWT serialization
     */
    private ManageTeamMembershipRequest(){}

    /**
     * Set the enumerated action type for this request.
     * 
     * @param actionType can't be null
     */
    private void setActionType(ACTION_TYPE actionType){
        
        if(actionType == null){
            throw new IllegalArgumentException("The action type can't be null");
        }
        
        this.actionType = actionType;
    }
    
    /**
     * Set the authored name of a team knowledge session that will be created or destroyed by the learner.
     * 
     * @param nameOfSessionToCreateOrDestroy can't be null or blank.
     */
    private void setNameOfSessionToCreateOrDestroy(String nameOfSessionToCreateOrDestroy){
        
        if(StringUtils.isBlank(nameOfSessionToCreateOrDestroy)){
            throw new IllegalArgumentException("The name of the session to create or destory can't be null or blank");
        }
        
        this.nameOfSessionToCreateOrDestroy = nameOfSessionToCreateOrDestroy;

    }
    
    /**
     * Return the authored name of a team knowledge session that will be created or destroyed by the learner.
     * @return won't be null or empty
     */
    public String getNameOfSessionToCreateOrDestroy(){
        return nameOfSessionToCreateOrDestroy;
    }
    
    /**
     * Set the domain session id of the host of a already created team knowledge session.
     * 
     * @param domainSessionIdOfHost must be a valid domain session id
     */
    private void setDomainSessionIdOfHost(int domainSessionIdOfHost){
        
        if(domainSessionIdOfHost <= 0){
            throw new IllegalArgumentException("The host domain session id must be a postivie number");
        }
        
        this.domainSessionIdOfHost = domainSessionIdOfHost;

    }

    /**
     * Return the domain session id of the host of a already created team knowledge session.
     * @return will be 0 
     */
    public int getDomainSessionIdOfHost() {
        return domainSessionIdOfHost;
    }
    
    /**
     * Set the unique team member name the learner wants to play (or leave) in the team organization of a real time assessment
     * 
     * @param teamMemberName can't be null or empty.  This is not the gift username.
     */
    private void setTeamMemberName(String teamMemberName){
        
        if(StringUtils.isBlank(teamMemberName)){
            throw new IllegalArgumentException("The member name can't be null");
        }
        
        this.memberName = teamMemberName;
    }

    /**
     * Return the unique team member name the learner wants to play (or leave) in the team organization of a real time assessment
     * @return wont be null or empty. This is not the gift username.
     */
    public String getTeamMemberName() {
        return memberName;
    }

    /**
     * Return the enumerated type of action the learner wants to perform on a team knowledge session.
     * 
     * @return the team membership action type to apply for this request.  Won't be null.
     */
    public ACTION_TYPE getActionType() {
        return actionType;
    }
    
    /**
     * Set the collection of session members (team member assignments to learners) to apply ONLY for SET_TEAM_MEMBERS_GROUP_SESSION
     * @param groupSessionMembership can't be null and can't be empty.
     */
    private void setSessionMembers(GroupMembership groupSessionMembership){
        
        if(groupSessionMembership == null){
            throw new IllegalArgumentException("The sessionMembers can't be null");
        }else if(groupSessionMembership.getMembers().isEmpty()){
            throw new IllegalArgumentException("The sessionMembers must be one or greater");
        }
        
        this.groupSessionMembership = groupSessionMembership;
    }
    
    /**
     * Return the collection of session members (team member assignments to learners) to apply ONLY for SET_TEAM_MEMBERS_GROUP_SESSION
     * @return can be null if set to a different action type other than SET_TEAM_MEMBERS_GROUP_SESSION.  Otherwise will
     * not be null and will not be empty.
     */
    public GroupMembership getGroupSessionMembership(){
        return groupSessionMembership;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ManageTeamMembershipRequest: ");
        builder.append("actionType = ");
        builder.append(actionType);
        switch(actionType){
        case CREATE_TEAM_SESSION:
            builder.append(", nameOfSession = ");
            builder.append(nameOfSessionToCreateOrDestroy);
            break;
        case SET_TEAM_MEMBERS_GROUP_SESSION:
            builder.append(",\nsessions = ");
            builder.append(groupSessionMembership);
            builder.append("\n");
            break;
        case ASSIGN_TEAM_MEMBER:
        case UNASSIGN_TEAM_MEMBER:
            builder.append(", memberName = ");
            builder.append(memberName);
            break;
        case JOIN_TEAM_SESSION:
        case LEAVE_TEAM_SESSION:
        case DESTROY_TEAM_SESSION:
            break;
		default:
			break;
        }

        builder.append(", domainSessionIdOfHost = ");
        builder.append(domainSessionIdOfHost);

        builder.append("]");
        return builder.toString();
    }

}
