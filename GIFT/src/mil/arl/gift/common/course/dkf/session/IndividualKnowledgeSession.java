/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.math.BigInteger;
import java.util.Map;

import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.enums.TrainingApplicationEnum;

/**
 * Contains information about a knowledge session designed for an individual, not a team.
 * 
 * @author mhoffman
 *
 */
public class IndividualKnowledgeSession extends AbstractKnowledgeSession{
    
    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** contains the one host member to this individual session */
    private SessionMembers sessionMembers;
    
    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private IndividualKnowledgeSession(){}
    
    /**
     * Set attributes
     * 
     * @param nameOfSession the name of the session, can be useful for showing hosted sessions to join.  Can't be null or empty.
     * @param scenarioDescription the description of the scenario. Supports HTML. Can be null or empty.
     * @param courseInfo contains information about the course for this session. Can't be null.
     * @param hostSessionMember the individual learner hosting their own knowledge session
     * @param team the structure of the team for the session. Can be null, e.g. legacy or individual dkf session
     * @param nodeIdToNameMap the map of all the task/concept node ids to their names
     * @param trainingAppType the training application type of the application in use when this session was created
     * @param sessionType the enumerated type of session.  Can't be null.
     * @param sessionStartTime the epoch start time of this session.
     * @param mission the mission data for this session. Can be null.
     * @param observerControls the data controlling how this session is presented to observer controllers. Can be null.
     */
    public IndividualKnowledgeSession(String nameOfSession, String scenarioDescription, KnowledgeSessionCourseInfo courseInfo, SessionMember hostSessionMember, 
            Team team, Map<BigInteger, String> nodeIdToNameMap, TrainingApplicationEnum trainingAppType, SessionType sessionType, 
            long sessionStartTime, Mission mission, ObserverControls observerControls){
        super(nameOfSession, scenarioDescription, courseInfo, hostSessionMember, team, nodeIdToNameMap, trainingAppType, sessionType, sessionStartTime, mission, observerControls);
        
        sessionMembers = new SessionMembers();
        sessionMembers.addSessionMember(hostSessionMember);
    }    

    @Override
    public SessionMembers getSessionMembers() {
        return sessionMembers;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[IndividualKnowledgeSession: ");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof IndividualKnowledgeSession)) {
            return false;
        }

        return super.equals(obj);
    }

}
