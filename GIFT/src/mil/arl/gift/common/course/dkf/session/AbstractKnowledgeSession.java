/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf.session;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.enums.TrainingApplicationEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * Contains the common information about a knowledge session.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractKnowledgeSession implements Serializable{
    
    /**
     * default
     */
    private static final long serialVersionUID = 1L;
    
    /** the name of the session */
    private String nameOfSession;    

    /** The description of the scenario. Supports HTML. */
    private String scenarioDescription;

    /** the host or owner of this knowledge session */
    private SessionMember hostSessionMember;
    
    /** whether the session is running or not, i.e. the host has started the assessment */
    private boolean sessionRunning = false;

    /** Maps the node id of a Task or Concept to its name */
    private Map<BigInteger, String> nodeIdToNameMap = new HashMap<>();

    /** The training application type that this knowledge session was created for */
    private TrainingApplicationEnum trainingAppType;

    /** Contains the structure of the team for the session. */
    private Team team;
    
    /**
     * Enumerated active knowledge session types
     * @author mhoffman
     *
     */
    public enum SessionType{
        /** a normal, live session, no log file involved*/
        ACTIVE,
        
        /** a normal, live session but the course object is playing a log file */
        ACTIVE_PLAYBACK
    }
    
    /** contains course level metadata for this knowledge session */
    private KnowledgeSessionCourseInfo courseInfo;
    
    /** the enumerated active knowledge session type */
    private SessionType sessionType;
    
    /** whether the session is being view in past session mode (not an active session) or active session mode */
    private boolean inPastSessionMode = false;
    
    /** the epoch start time for this knowledge session */
    private long sessionStartTime;
    
    /** (optional) the epoch session end time for this knowledge session */
    private long sessionEndTime;

    /** (optional) the mission data for this session */
    private Mission mission;

    /** (optional) the data controlling how this session is presented to observer controllers in the Game Master */
    private ObserverControls observerControls;
    
    /** (optional) the unique ID of the service being used to play back this session, if this session is being played back */
    private String playbackId;
    
    /** 
     * The comparator for sessions. Priority:
     * 1. descending start date (recent first)
     * 2. session name
     * 3. session ids
     * 
     */
    public static final Comparator<AbstractKnowledgeSession> defaultSessionComparator = new Comparator<AbstractKnowledgeSession>() {
        @Override
        public int compare(AbstractKnowledgeSession o1, AbstractKnowledgeSession o2) {
            /* Check if either object is null. Non-nulls appear before nulls. */
            if (o1 == null) {
                return o2 != null ? 1 : 0;
            } else if (o2 == null) {
                return -1;
            }
            
            int compare = 0;
            
            if(o1.inPastSessionMode()){
                /* sort by descending start date, i.e. earlier first */
                
                long start1 = o1.getSessionStartTime();
                long start2 = o2.getSessionStartTime();
                if(start1 > start2){
                    // o1 started after o2, therefore o1 should come first
                    compare = -1;
                }else if(start1 < start2){
                    // o1 started before o2, therefore o2 should come first
                    compare = 1;
                }
            }
            
            if(compare == 0){
                
                /* Sort by session name. Non-nulls appear before nulls. */
                String sessionName1 = o1.getNameOfSession();
                String sessionName2 = o2.getNameOfSession();
                if (sessionName1 == null) {
                    return sessionName2 != null ? 1 : 0;
                } else if (sessionName2 == null) {
                    return -1;
                }

                compare = sessionName1.compareToIgnoreCase(sessionName2);
            }
            

            /* If the session names are the same, sort by session ids */
            if (compare == 0) {
                Integer sessionId1 = o1.getHostSessionMember().getDomainSessionId();
                Integer sessionId2 = o2.getHostSessionMember().getDomainSessionId();
                return sessionId1.compareTo(sessionId2);
            }

            return compare;
        }
    };

    /**
     * Required for GWT serialization.
     */
    protected AbstractKnowledgeSession(){}
    
    /**
     * Set attributes - normally used when decoding a message that has this class in the payload.
     * 
     * @param nameOfSession the name of the session, can be useful for showing hosted sessions to join.  Can't be null or empty.
     * @param scenarioDescription the description of the scenario. Supports HTML. Can be null or empty.
     * @param courseInfo contains information about the course for this session. Can't be null.
     * @param hostSessionMember information about the learner hosting the team session
     * @param team the structure of the team for the session. Can be null, e.g. legacy or individual dkf session
     * @param nodeIdToNameMap the map of all the task/concept node ids to their names
     * @param trainingAppType the training application type of the application in use when this session was created
     * @param mission the mission data for this session. Can be null.
     * @param observerControls the data controlling how this session is presented to observer controllers. Can be null.
     */
    public AbstractKnowledgeSession(String nameOfSession, String scenarioDescription, KnowledgeSessionCourseInfo courseInfo, SessionMember hostSessionMember, Team team,
            Map<BigInteger, String> nodeIdToNameMap, TrainingApplicationEnum trainingAppType, SessionType sessionType, long sessionStartTime,
            Mission mission, ObserverControls observerControls) {
        
        setNameOfSession(nameOfSession);
        setScenarioDescription(scenarioDescription);
        setCourseInfo(courseInfo);        
        setHostSessionMember(hostSessionMember);
        setTeamStructure(team);
        setNodeIdToNameMap(nodeIdToNameMap);
        setTrainingAppType(trainingAppType);
        setSessionType(sessionType);
        setSessionStartTime(sessionStartTime);
        setMission(mission);
        setObserverControls(observerControls);
    } 
    
    /**
     * Return information about the current members that are in this session.
     * 
     * @return could contain a single member for sessions that only contain a single playable team member
     * in the team organization, a single member for sessions that only contain a single player in a multi-player
     * possible team organization, or multiple members for a session that supports multi-player team organization.
     */
    public abstract SessionMembers getSessionMembers();
    
    /**
     * Return the enumerated active knowledge session type
     * @return will not be null.
     */
    public SessionType getSessionType() {
        return sessionType;
    }

    /**
     * Set the enumerated active knowledge session type
     * @param sessionType can't be null.
     */
    private void setSessionType(SessionType sessionType) {
        if(sessionType == null){
            throw new IllegalArgumentException("The session type is null");
        }
        this.sessionType = sessionType;
    }
    
    /**
     * Set whether the session is being view in past session mode (not an active session) or active session mode
     * @param value true if in past session mode, false if in active session mode
     */
    public void setInPastSessionMode(boolean value) {
        this.inPastSessionMode = value;
    }
    
    /**
     * Return whether the session is being view in past session mode (not an active session) or active session mode
     * @return default is false (i.e. active session mode).
     */
    public boolean inPastSessionMode() {
        return inPastSessionMode;
    }

    /**
     * Return the epoch time when the knowledge session starts
     * @return the epoch time when this knowledge session started
     */
    public long getSessionStartTime() {
        return sessionStartTime;
    }

    /**
     * Set the epoch time when the knowledge session started
     * @param sessionStartTime the epoch time for the start of this knowledge session
     */
    private void setSessionStartTime(long sessionStartTime) {
        this.sessionStartTime = sessionStartTime;
    }

    /**
     * Return the epoch time when the knowledge session ends
     * @return default to zero if not ended
     */
    public long getSessionEndTime() {
        return sessionEndTime;
    }

    /**
     * Set the epoch time when the knowledge session ends
     * @param sessionEndTime the epoch time for the end of this knowledge session
     */
    public void setSessionEndTime(long sessionEndTime) {
        this.sessionEndTime = sessionEndTime;
    }

    /**
     * Set the course level metadata for this knowledge session
     * @param courseInfo can't be null
     */
    private void setCourseInfo(KnowledgeSessionCourseInfo courseInfo){
        if (courseInfo == null) {
            throw new IllegalArgumentException("The course info can't be null");
        }
        this.courseInfo = courseInfo;
    }
    
    /**
     * Return the information about the host of this knowledge session.
     * 
     * @return information on the knowledge session host.  Won't be null.
     */
    public SessionMember getHostSessionMember() {
        return hostSessionMember;
    }

    /**
     * Sets the host session member
     * 
     * @param hostSessionMember information about the learner hosting the team session
     */
    private void setHostSessionMember(SessionMember hostSessionMember) {
        if (hostSessionMember == null) {
            throw new IllegalArgumentException("The host session member can't be null");
        }
        this.hostSessionMember = hostSessionMember;
    }

    /**
     * Set the name of the course object that contains the dkf. 
     * 
     * @param nameOfSession can't be null or empty.
     */
    public void setNameOfSession(String nameOfSession){
        
        if(StringUtils.isBlank(nameOfSession)){
            throw new IllegalArgumentException("The name of the session is null or blank");
        }
        
        this.nameOfSession = nameOfSession;
    }
    
    /**
     * Return the name of the course object that contains the dkf.
     * @return won't be null or empty.
     */
    public String getNameOfSession(){
        return nameOfSession;
    }

    /**
     * Set the description of the scenario. Supports HTML.
     * 
     * @param scenarioDescription the description of the scenario. Optional.
     */
    public void setScenarioDescription(String scenarioDescription) {
        this.scenarioDescription = scenarioDescription;
    }

    /**
     * Get the description of the scenario.
     * 
     * @return the description of the scenario. Supports HTML. Can be null or empty.
     */
    public String getScenarioDescription() {
        return scenarioDescription;
    }

    /**
     * Return the name of the course being executed
     * 
     * @return won't be null or empty
     */
    public String getCourseName() {
        return courseInfo.getCourseName();
    }


    /**
     * Return the unique id of the course being executed
     * @return won't be null or empty
     */
    public String getCourseRuntimeId() {
        return courseInfo.getCourseRuntimeId();
    }
    
    /**
     * Return the unique id of the course authored being executed
     * @return won't be null or empty
     */
    public String getCourseSourceId(){
        return courseInfo.getCourseSourceId();
    }
    
    /**
     * Return the name of the experiment being executed
     * 
     * @return can be null if the course isn't an experiment
     */
    public String getExperimentName() {
        return courseInfo.getExperimentName();
    }

    /**
     * Set whether the session is running or not, i.e. the host has started the assessment
     * @param running true if the knowledge session has started being assessed
     */
    public void setSessionRunning(boolean running){
        this.sessionRunning = running;
    }
    
    /**
     * Return whether the session is running or not, i.e. the host has started the assessment
     * @return false by default.
     */
    public boolean isSessionRunning(){
        return sessionRunning;
    }

    /**
     * Get the mapping of the task and concept node ids to their respective names.
     * 
     * @return the node id to name map
     */
    public Map<BigInteger, String> getNodeIdToNameMap() {
        return nodeIdToNameMap;
    }

    /**
     * Sets the mapping of the task and concept node ids to their repective names.
     * 
     * @param nodeIdToNameMap the node id to name map. If null, an empty map will be used.
     */
    private void setNodeIdToNameMap(Map<BigInteger, String> nodeIdToNameMap) {
        this.nodeIdToNameMap = nodeIdToNameMap != null ? nodeIdToNameMap : new HashMap<BigInteger, String>();
    }

    /**
     * Get the training application type of the training application that is in use when this
     * session was created.
     * 
     * @return the training application type
     */
    public TrainingApplicationEnum getTrainingAppType() {
        return trainingAppType;
    }

    /**
     * Sets the training application type of the training application that is in use when this
     * session was created.
     * 
     * @param trainingAppType the training application type
     */
    private void setTrainingAppType(TrainingApplicationEnum trainingAppType) {
        this.trainingAppType = trainingAppType;
    }

    /**
     * Returns the structure of the team for this knowledge session. This
     * contains the hierarchy of team members that comprise the team.
     * 
     * @return The team structure data containing the hierarchy of the team. Can be null, e.g. legacy or individual dkf session
     */
    public Team getTeamStructure() {
        return team;
    }

    /**
     * Sets the structure of the team for the session
     * 
     * @param team the structure of the team for the session.  Can be null, e.g. legacy or individual dkf session
     */
    private void setTeamStructure(Team team) {
        this.team = team;
    }
    
    /**
     * Return the course domain session log file this knowledge session is from
     * @return can be null if this knowledge session is not from a log file
     */
    public String getDomainSessionLogFileName() {
        return courseInfo.getDomainSessionLogFileName();
    }
    
    /**
     * Set the course domain session log file this knowledge session is from
     * @param domainSessionLogFileName can be null
     */
    public void setDomainSessionLogFileName(String domainSessionLogFileName) {
        courseInfo.setDomainSessionLogFileName(domainSessionLogFileName);
    }
    
    /**
     * Gets the unique ID of the service being used to play back this session, if this session is being played back
     * 
     * @return the ID of this session's playback service. Can be null.
     */
    public String getPlaybackId() {
        return playbackId;
    }

    /**
     * Sets the unique ID of the service being used to play back this session
     * 
     * @param playbackId the ID of this session's playback service. Can be null.
     */
    public void setPlaybackId(String playbackId) {
        this.playbackId = playbackId;
    }

    /**
     * Gets the mission data for this session.
     * 
     * @return the mission data. Can be null.
     */
    public Mission getMission() {
        return mission;
    }

    /**
     * Sets the mission data for this session.
     * 
     * @param mission the mission data. Can be null.
     */
    public void setMission(Mission mission) {
        this.mission = mission;
    }

    /**
     * Gets the data controlling how this session is presented to observer controllers in the Game Master
     * 
     * @return the observer controls. Can be null.
     */
    public ObserverControls getObserverControls() {
        return observerControls;
    }

    /**
     * Sets the data controlling how this session is presented to observer controllers in the Game Master
     * 
     * @param observerControls the observer controls. Can be null.
     */
    public void setObserverControls(ObserverControls observerControls) {
        this.observerControls = observerControls;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (courseInfo.getCourseName() == null ? 0 : courseInfo.getCourseName().hashCode());
        result = prime * result + (courseInfo.getCourseRuntimeId() == null ? 0 : courseInfo.getCourseRuntimeId().hashCode());
        result = prime * result + (courseInfo.getCourseSourceId() == null ? 0 : courseInfo.getCourseSourceId().hashCode());
        result = prime * result + (courseInfo.getExperimentName() == null ? 0 : courseInfo.getExperimentName().hashCode());
        result = prime * result + (hostSessionMember == null ? 0 : hostSessionMember.hashCode());
        result = prime * result + (nameOfSession == null ? 0 : nameOfSession.hashCode());
        result = prime * result + (scenarioDescription == null ? 0 : scenarioDescription.hashCode());
        result = prime * result + (nodeIdToNameMap == null ? 0 : nodeIdToNameMap.hashCode());
        result = prime * result + (trainingAppType == null ? 0 : trainingAppType.hashCode());
        result = prime * result + (team == null ? 0 : team.hashCode());
        result = prime * result + (sessionRunning ? 1231 : 1237);
        result = prime * result + (int) (sessionStartTime ^ sessionStartTime >>> 32);
        result = prime * result + (getDomainSessionLogFileName() == null ? 0 : getDomainSessionLogFileName().hashCode());
        result = prime * result + (playbackId == null ? 0 : playbackId.hashCode());
        result = prime * result + (mission == null ? 0 : mission.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof AbstractKnowledgeSession)) {
            return false;
        }

        AbstractKnowledgeSession other = (AbstractKnowledgeSession) obj;

        if (!StringUtils.equalsIgnoreCase(courseInfo.getCourseName(), other.getCourseName())) {
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(nameOfSession, other.getNameOfSession())) {
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(scenarioDescription, other.getScenarioDescription())) {
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(courseInfo.getCourseRuntimeId(), other.getCourseRuntimeId())) {
            return false;
        }
        
        if (!StringUtils.equalsIgnoreCase(courseInfo.getCourseSourceId(), other.getCourseSourceId())) {
            return false;
        }

        if (!StringUtils.equalsIgnoreCase(courseInfo.getExperimentName(), other.getExperimentName())) {
            return false;
        }

        if (hostSessionMember == null) {
            if (other.getHostSessionMember() != null) {
                return false;
            }
        } else if (!hostSessionMember.equals(other.getHostSessionMember())) {
            return false;
        }

        if (nodeIdToNameMap == null) {
            if (other.getNodeIdToNameMap() != null) {
                return false;
            }
        } else if (!nodeIdToNameMap.equals(other.getNodeIdToNameMap())) {
            return false;
        }

        if (trainingAppType == null) {
            if (other.getTrainingAppType() != null) {
                return false;
            }
        } else if (!trainingAppType.equals(other.getTrainingAppType())) {
            return false;
        }

        if (team == null) {
            if (other.getTeamStructure() != null) {
                return false;
            }
        } else if (!team.equals(other.getTeamStructure())) {
            return false;
        }

        if (sessionRunning != other.isSessionRunning()) {
            return false;
        }
        
        if(sessionStartTime != other.getSessionStartTime()) {
            return false;
        }
        
        if(getDomainSessionLogFileName() == null) {
            if(other.getDomainSessionLogFileName() != null) {
                return false;
            }
        } else if(!getDomainSessionLogFileName().equals(other.getDomainSessionLogFileName())){
            return false;
        }

        if(playbackId == null) {
            if(other.playbackId != null) {
                return false;
            }
        } else if(!playbackId.equals(other.playbackId)){
            return false;
        }

        if (mission == null) {
            if (other.mission != null) {
                return false;
            }
        } else if (!mission.equals(other.mission)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("nameOfSession = ");
        builder.append(nameOfSession);
        builder.append(", scenarioDescription = ");
        builder.append(scenarioDescription);
        builder.append(", hostSessionMember = ");
        builder.append(hostSessionMember);
        builder.append(", isRunning = ");
        builder.append(sessionRunning);
        builder.append(courseInfo);
        builder.append(", nodeIdToNameMap = ");
        builder.append(nodeIdToNameMap);
        builder.append(", trainingAppType = ");
        builder.append(trainingAppType);
        builder.append(", sessionType = ");
        builder.append(sessionType);
        builder.append(", inPastSessionMode = ");
        builder.append(inPastSessionMode);
        builder.append(", sessionStartTime = ");
        builder.append(sessionStartTime);
        
        if(sessionEndTime > 0){
            builder.append(", sessionEndTime = ");
            builder.append(sessionEndTime);
        }
        builder.append(", team = ").append(this.team);
        builder.append(", playbackId =").append(playbackId);
        builder.append(", mission = ").append(mission);
        builder.append(", observerControls = ").append(observerControls);
        
        return builder.toString();
    }
    
    /**
     * Contains information about members in this session.
     * 
     * @author mhoffman
     *
     */
    public static class SessionMembers implements Serializable{
        
        /**
         * default
         */
        private static final long serialVersionUID = 1L;
        
        /**
         * mapping of domain session id to information about a team member that is in the team session
         * Does include the host member.
         */
        private Map<Integer, SessionMember> dsIdMap = new HashMap<>();
                
        /**
         * Constructor - required for GWT serialization
         */
        public SessionMembers(){
            
        }
        
        /**
         * Return the mapping of domain session id for each user to the session information for that user.
         *  
         * @return a READ-ONLY view of the mapping of domain session id to information about a team member that is in the team session
         * Does include the host member.
         */
        public Map<Integer, SessionMember> getSessionMemberDSIdMap(){
            return Collections.unmodifiableMap(dsIdMap);
        }
        
        /**
         * Add a member to this session.
         * 
         * @param member the member to add.  Can't be null and can't be a member that has already joined this session.
         */
        protected void addSessionMember(SessionMember member){
            
            if(member == null){
                throw new IllegalArgumentException("The member to add can't be null");
            }else if(getSessionMemberDSIdMap().containsKey(member.getDomainSessionId())){
                throw new IllegalArgumentException("Can't add a member to the same knowledge session twice. "+member);
            }
            
            dsIdMap.put(member.getDomainSessionId(), member);
        }
        
        /**
         * Remove a member from this session.
         * 
         * @param domainSessionIdOfLeavingSession the domain session id of the session for the member being removed.
         * @return true if a member was found associated with the given session
         */
        protected boolean removeSessionMember(int domainSessionIdOfLeavingSession){
            return dsIdMap.remove(domainSessionIdOfLeavingSession) != null;
        }
        
        /**
         * Return the number of members in this session.  Includes the host.
         * @return the number of members in the session, both joiners and host.
         */
        public int getNumOfMembers(){
            return dsIdMap.size();
        }
        
        /**
         * Return whether the domain session id provided has already joined the session.
         * @param dsId the domain session id to check
         * @return true if the domain session has already joined the session.
         */
        public boolean containsMember(int dsId){
            return dsIdMap.containsKey(dsId);
        }
    }

    /**
     * Contains course metadata for the knowledge session.
     * 
     * @author mhoffman
     *
     */
    public static class KnowledgeSessionCourseInfo implements Serializable{        
        
        /**
         * default
         */
        private static final long serialVersionUID = 1L;

        /** the name of the session course */
        private String courseName;

        /** the unique id of the course being executed */
        private String courseRuntimeId;
        
        /** the unique id of the course source */
        private String courseSourceId;
        
        /** the name of the session experiment if the course is an experiment */
        private String experimentName;
        
        /** (optional) the course domain session log file this knowledge session is from */
        private String domainSessionLogFileName;
        
        /**
         * Required for GWT serialization.
         */
        protected KnowledgeSessionCourseInfo(){}
        
        /**
         * Set attributes.
         * @param courseName the name of the course being run.  Can't be null or empty.
         * @param courseRuntimeId the unique id of the course being executed.  Can't be null or empty.
         * @param courseSourceId the unique id of the course source being executed.  Can't be null or empty.
         * @param experimentName the name of the experiment if this course is being run as an experiment. Can be null if the course is not an experiment.
         */
        public KnowledgeSessionCourseInfo(String courseName, String courseRuntimeId, String courseSourceId, String experimentName){
            setCourseName(courseName);
            setCourseRuntimeId(courseRuntimeId);
            setCourseSourceId(courseSourceId);
            setExperimentName(experimentName);
        }             

        /**
         * Return the course domain session log file this knowledge session is from
         * @return can be null if not from a log file.
         */
        public String getDomainSessionLogFileName() {
            return domainSessionLogFileName;
        }

        /**
         * Set the course domain session log file this knowledge session is from
         * @param domainSessionLogFileName can be null
         */
        public void setDomainSessionLogFileName(String domainSessionLogFileName) {
            this.domainSessionLogFileName = domainSessionLogFileName;
        }

        /**
         * Set the name of the course being executed
         * 
         * @param courseName can't be null or empty
         */
        private void setCourseName(String courseName) {
            if (StringUtils.isBlank(courseName)) {
                throw new IllegalArgumentException("The course name can't be blank");
            }
            this.courseName = courseName;
        }
        

        /**
         * Set the unique id of the course being executed
         * @param courseRuntimeId can't be null or empty
         */
        private void setCourseRuntimeId(String courseRuntimeId) {
            
            if(StringUtils.isBlank(courseRuntimeId)){
                throw new IllegalArgumentException("The course runtime id can't be null");
            }
            this.courseRuntimeId = courseRuntimeId;
        }  
        
        /**
         * Set the unique id of the authored course
         * @param courseRuntimeId can't be null or empty
         */
        private void setCourseSourceId(String courseSourceId) {
            
            if(StringUtils.isBlank(courseSourceId)){
                throw new IllegalArgumentException("The course source id can't be null");
            }
            this.courseSourceId = courseSourceId;
        }

        /**
         * Set the name of the experiment being executed
         * 
         * @param experimentName can be null if the course isn't an experiment
         */
        private void setExperimentName(String experimentName) {
            this.experimentName = experimentName;
        }
        
        /**
         * Return the name of the course being executed
         * 
         * @return won't be null or empty
         */
        public String getCourseName() {
            return courseName;
        }


        /**
         * Return the unique id of the course being executed
         * @return won't be null or empty
         */
        public String getCourseRuntimeId() {
            return courseRuntimeId;
        }
        
        /**
         * Return the unique id of the course source id
         * @return won't be null or empty
         */
        public String getCourseSourceId() {
            return courseSourceId;
        }
        
        /**
         * Return the name of the experiment being executed
         * 
         * @return can be null if the course isn't an experiment
         */
        public String getExperimentName() {
            return experimentName;
        }
        
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(", courseName = ");
            builder.append(courseName);
            builder.append(", courseRuntimeId = ");
            builder.append(courseRuntimeId);
            builder.append(", courseSourceId = ");
            builder.append(courseSourceId);
            builder.append(", experimentName = ");
            builder.append(experimentName); 
            builder.append(", domainSessionLogFileName = ");
            builder.append(domainSessionLogFileName);
            return builder.toString();
        }
    }
    
    /**
     * Contains data used to control how this session is presented to observer controllers in the Game Master
     * 
     * @author nroberts
     */
    @SuppressWarnings("serial")
    public static class ObserverControls implements Serializable{
        
        /** The URL of the audio file that should be played when notifying the user of good performance */
        private String goodPerformanceAudioUrl;
        
        /** The URL of the audio file that should be played when notifying the user of poor performance */
        private String poorPerformanceAudioUrl;
        
        /** The workspace-relative path to the file containing the audio captured for this session */
        private String capturedAudioPath;
        
        /**
         * Creates a new empty set of observer control data
         */
        public ObserverControls() {}

        /**
         * Gets the URL of the audio file that should be played when notifying the user of good performance
         * 
         * @return the URL of the good performance audio file. Can be null.<br/>
         * This is relative to GIFT/output/domainSession/.<br/>
         * E.g. domainSession697_uId1/crash.mp3
         */
        public String getGoodPerformanceAudioUrl() {
            return goodPerformanceAudioUrl;
        }

        /**
         * Sets the URL of the audio file that should be played when notifying the user of good performance
         * 
         * @param goodAudioUrl the URL of the good performance audio file. Can be null.<br/>
         * This is relative to GIFT/output/domainSession/.<br/>
         * E.g. domainSession697_uId1/crash.mp3
         * @return this observer controls instance. Can be used to chain method calls.
         */
        public ObserverControls setGoodPerformanceAudioUrl(String goodAudioUrl) {
            this.goodPerformanceAudioUrl = goodAudioUrl;
            return this;
        }

        /**
         * Gets the URL of the audio file that should be played when notifying the user of poor performance
         * 
         * @return the URL of the poor performance audio file. Can be null.<br/>
         * This is relative to GIFT/output/domainSession/.<br/>
         * E.g. domainSession697_uId1/beep.mp3
         */
        public String getPoorPerformanceAudioUrl() {
            return poorPerformanceAudioUrl;
        }

        /**
         * Sets the URL of the audio file that should be played when notifying the user of poor performance
         * 
         * @param poorAudioUrl the URL of the poor performance audio file. Can be null.<br/>
         * This is relative to GIFT/output/domainSession/.<br/>
         * E.g. domainSession697_uId1/beep.mp3
         * @return this observer controls instance. Can be used to chain method calls.
         */
        public ObserverControls setPoorPerformanceAudioUrl(String poorAudioUrl) {
            this.poorPerformanceAudioUrl = poorAudioUrl;
            return this;
        }

        /**
         * Gets the path to the file containing the audio captured for this session.
         * 
         * @return the path to the captured audio file. Can be null.<br/>
         * When using this in the Tutor (TUI) during a course, this is a workspace folder
         * relative path (e.g. Public/Urban Operation - JOURNEYMAN Playback/urbanOp_capstone final audio_v4.mp3) <br/>
         * When using this in the Game Master past session UI, this is a
         * domain session output folder (GIFT/output/domainSessions/) relative path (e.g. urbanOp_capstone final audio_v4.mp3)
         */
        public String getCapturedAudioPath() {
            return capturedAudioPath;
        }

        /**
         * Sets the file containing the audio captured for this session
         * 
         * @param capturedAudioPath the captured audio file. Can be null.</br>  
         * When using this in the Tutor (TUI) during a course, this is a workspace folder
         * relative path (e.g. Public/Urban Operation - JOURNEYMAN Playback/urbanOp_capstone final audio_v4.mp3) <br/>
         * When using this in the Game Master past session UI, this is a
         * domain session output folder (GIFT/output/domainSessions/) relative path (e.g. urbanOp_capstone final audio_v4.mp3)
         * @return this observer controls instance. Can be used to chain method calls.
         */
        public ObserverControls setCapturedAudioPath(String capturedAudioPath) {
            this.capturedAudioPath = capturedAudioPath;
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[ObserverControls: goodPerformanceAudioUrl = ");
            builder.append(goodPerformanceAudioUrl);
            builder.append(", poorPerformanceAudioUrl = ");
            builder.append(poorPerformanceAudioUrl);
            builder.append(", capturedAudio = ");
            builder.append(capturedAudioPath);
            builder.append("]");
            return builder.toString();
        }
    }
}
