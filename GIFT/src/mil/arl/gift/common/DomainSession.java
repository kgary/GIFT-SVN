/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class contains high level information about a domain session
 * 
 * @author mhoffman
 *
 */
public class DomainSession extends UserSession{
    
    /**
     * default
     */
    private static final long serialVersionUID = 1L;

    /** the unique domain session id - across GIFT.  Managed by UMS database. */
    private int id;
    
    /** the course source id which is the path to the authored course 
     * E.g. mhoffman/new course/new course.course.xml */
    private String domainSourceId;
    
    /** the course runtime id which is the path to the course used when taking the course 
     * E.g. mhoffman\2021-02-18_14-25-34\new course\new course.course.xml*/
    private String domainRuntimeId;
    
    /** useful when a domain name (course name) is not known for a domain session message*/
    public static final String UNKNOWN_DOMAIN_NAME = "unknown";
    
    /** used as a prefix to domain session message log file names */
    private static final String PREFIX = "domainSession";
    
    /** used as a prefix to user id in a file name */
    private static final String USER_PREFIX = "_uId";
    
    /** used as a prefix to an experiment id in a file name */
    private static final String EXPERIMENT_PREFIX = "_eId";
    
    /** used as a prefix to a global user id in a file name */
    private static final String GLOBAL_USER_PREFIX = "_guId";
    
    /** 
     * flag used to indicate if the gateway module is needed for this course execution 
     * The gateway module is only needed if an interop implementation class will be possibly used based
     * on the authored course contents.
     * Note: assume the session requires a gateway since most will
     */
    private boolean requiresGateway = true;
    
    /** 
     * whether this domain session only contains gateway training applications and therefore
     * can support a single gateway module connection for all learners in the team knowledge session
     * i.e. this allows other learner's to not need a gateway module of their own
     */
    private boolean hostGatewayAllowed = false;
    
    /**
     * flag used to indicate if the gateway module is connected to this domain session
     */
    private boolean gatewayConnected = false;
    
    /**
     * flag used to indicate if the learner module is connected to this domain session
     */
    private boolean learnerConnected = false;
    
    /**
     * Flag indicating whether or not a tutor topic is needed for the current domain session.
     * This is determined by whether or not the course.xml contains any embedded application urls.
     */
    private boolean requiresTutorTopic = false;
    
    /**
     * (optional) the id of a subject in a published course.  
     * As of 06/2021 all courses are published courses.  This id is managed by the UMS database.
     * It is different than the user id.  A single user identified by a user id can have multiple
     * subject ids for the same course / published course if they take the course multiple times.
     */
    private Integer subjectId;
    
    /**
     * Required for GWT serialization
     */
    @SuppressWarnings("unused")
    private DomainSession(){
        super();
    }

    /**
     * Class constructor - set attributes
     * 
     * @param id - the domain session id
     * @param userId - the user id for this domain session
     * @param domainRuntimeId the course runtime id which is the path to the course used when taking the course.  Can't be null or empty.  If not known use
     * DomainSession.UKNOWN_DOMAIN_NAME. E.g. mhoffman\2021-02-18_14-25-34\new course\new course.course.xml
     * @param domainSourceId the course source id which is the path to the authored course.  Can't be null or empty. E.g. mhoffman/new course/new course.course.xml
     */
    public DomainSession(int id, int userId, String domainRuntimeId, String domainSourceId){
        super(userId);
        this.id = id;
        
        if(domainRuntimeId == null || domainRuntimeId.isEmpty()){
            throw new IllegalArgumentException("The domain runtime id can't be null or empty.");
        }
        
        this.domainRuntimeId = domainRuntimeId;    
        
        if(domainSourceId == null || domainSourceId.isEmpty()){
            throw new IllegalArgumentException("The domain source id can't be null or empty.");
        }
        
        this.domainSourceId = domainSourceId; 
    }
    
    public int getDomainSessionId(){
        return id;
    }
    
    /**
     * Return the course runtime id which is the path to the course used when taking the course.
     * 
     * @return won't be null or empty. E.g. mhoffman\2021-02-18_14-25-34\new course\new course.course.xml
     */
    public String getDomainRuntimeId(){
        return domainRuntimeId;
    } 
    
    /**
     * Return the course source id which is the path to the authored course
     * 
     * @return won't be null or empty. E.g. mhoffman/new course/new course.course.xml
     */
    public String getDomainSourceId(){
        return domainSourceId;
    }
    
    /**
     * Set whether the gateway module is needed for this course execution 
     * The gateway module is only needed if an interop implementation class will be possibly used based
     * on the authored course contents.
     * 
     * @param requiresGateway is the gateway module needed
     */
    public void setRequiresGateway(boolean requiresGateway){
        this.requiresGateway = requiresGateway;
    }
    
    /**
     * Set whether a tutor topic needs to be created for the current domain
     * session. Determined by searching the course.xml files for embedded 
     * training application urls.
     * @param newValue the new value for the requiresTutorTopic flag
     */
    public void setRequiresTutorTopic(boolean newValue) {
    	requiresTutorTopic = newValue;
    }
    
    /**
     * Return whether the gateway module is needed for this course execution 
     * The gateway module is only needed if an interop implementation class will be possibly used based
     * on the authored course contents.
     * 
     * @return boolean is the gateway module needed
     */
    public boolean doesRequireGateway(){
        return requiresGateway;
    }
    /**
     * Return whether a tutor topic needs to be created for the current domain
     * session. Determined by searching the course.xml files for embedded 
     * training application urls.
     * @return true/false for whether a tutor topic is needed for the domain session
     */
    public boolean doesRequiresTutorTopic() {
    	return requiresTutorTopic;
    }
    
    /**
     * Set whether the gateway module has been connected to the domain session.
     * 
     * @param gatewayConnected is the gateway connected
     */
    public void setGatewayConnected(boolean gatewayConnected){
        this.gatewayConnected = gatewayConnected;
    }
    
    public boolean isGatewayConnected(){
        return this.gatewayConnected;
    }
    
    /**
     * Set whether the learner module has been connected to the domain session.
     * 
     * @param learnerConnected is the learner module connected
     */
    public void setLearnerConnected(boolean learnerConnected){
        this.learnerConnected = learnerConnected;
    }
    
    /**
     * Return whether the learner module was connected to this domain session during the start 
     * of the domain session.
     * 
     * @return true is a good indication that the domain session started successfully
     */
    public boolean isLearnerConnected(){
        return this.learnerConnected;
    }
    
    /**
     * Return whether this domain session only contains gateway training applications and therefore
     * can support a single gateway module connection for all learners in the team knowledge session
     * i.e. this allows other learner's to not need a gateway module of their own
     * @return false by default
     */
    public boolean isHostGatewayAllowed() {
        return hostGatewayAllowed;
    }

    /**
     * Set whether this domain session only contains gateway training applications and therefore
     * can support a single gateway module connection for all learners in the team knowledge session
     * i.e. this allows other learner's to not need a gateway module of their own
     * @param hostGatewayAllowed the value to use when checking for gateway module allocation
     */
    public void setHostGatewayAllowed(boolean hostGatewayAllowed) {
        this.hostGatewayAllowed = hostGatewayAllowed;
    }

    /**
     * Copies data from an existing UserSession object into the domain session.  This is added
     * because a lot of code relies on creating a UserSession (which DomainSession class extends)
     * using only the id value.  This allows the DomainSession to be created using the 'id' and
     * then use a second pass to copy in existing user session details into the domain session where
     * needed.
     * 
     * This allows the UserSession data to be copied into the domain session when creating a domain
     * session from an existing domain session or user session data.
     * 
     * @param userSession the UserSession object data to copy from.
     */
    public void copyFromUserSession(UserSession userSession) {
        if (userSession != null) {
            this.setExperimentId(userSession.getExperimentId());
            this.setSessionType(userSession.getSessionType());
            this.setUsername(userSession.getUsername());
            this.setSessionDetails(userSession.getSessionDetails());
        }
    }
    
    /**
     * Builds the name of the folder in the output directory associated with
     * this domain session, based on this domain session's data. This same name
     * is also used as the file name for the .log file containing the message
     * logs for this domain session.
     * 
     * @return the name of the output folder associated with this domain session. Cannot be null. E.g. domainSession697_uId1
     * E.g. domainSession697_uId1
     */
    public String buildLogFileName() {
        
        if(getSessionDetails() != null && getSessionDetails().getGlobalUserId() != null) {
            return PREFIX + getDomainSessionId() + USER_PREFIX + getUserId() + GLOBAL_USER_PREFIX + getSessionDetails().getGlobalUserId();
        } else if(getExperimentId() != null){
            return PREFIX + getDomainSessionId() + USER_PREFIX + getUserId() + EXPERIMENT_PREFIX + getExperimentId();
        }else{
            return PREFIX + getDomainSessionId() + USER_PREFIX + getUserId();
        }
    }

    /**
     * The id of a subject in a published course.  
     * As of 06/2021 all courses are published courses.  This id is managed by the UMS database.
     * It is different than the user id.  A single user identified by a user id can have multiple
     * subject ids for the same course / published course if they take the course multiple times.
     * @return can be null
     */
    public Integer getSubjectId() {
        return subjectId;
    }

    /**
     * Return the id of a subject in a published course.  
     * As of 06/2021 all courses are published courses.  This id is managed by the UMS database.
     * It is different than the user id.  A single user identified by a user id can have multiple
     * subject ids for the same course / published course if they take the course multiple times.
     * @param subjectId can be null.
     */
    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    @Override
    public boolean equals(Object that){       
        
        if (that != null && that instanceof DomainSession) {
            
            DomainSession other = (DomainSession)that;
            
            if (getDomainSessionId() == other.getDomainSessionId() && super.equals(that)) {
                return true;
            }
                    
        }
        
        return false;
    }
    
    @Override
    public int hashCode(){
        return getDomainSessionId();
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DomainSession: ");
        sb.append(" id = ").append(getDomainSessionId());
        sb.append(", experimentId = ").append(getExperimentId());
        sb.append(", userId = ").append(getUserId());
        sb.append(", subjectId = ").append(getSubjectId());
        sb.append(", username = ").append(getUsername());
        sb.append(", sessionType = ").append(getSessionType());
        sb.append(", sessionDetails = ").append(getSessionDetails());
        sb.append(", globalUserId = ").append(getGlobalUserId());
        sb.append(", domainRuntimeId = ").append(getDomainRuntimeId());
        sb.append(", domainSourceId = ").append(getDomainRuntimeId());
        sb.append(", requiresGateway = ").append(doesRequireGateway());
        sb.append(", hostGatewayAllowed = ").append(isHostGatewayAllowed());
        sb.append(", requiresTutorTopic = ").append(doesRequiresTutorTopic());
        sb.append(", gatewayConnected = ").append(isGatewayConnected());
        sb.append(", learnerConnected = ").append(isLearnerConnected());
        sb.append("]");
        
        return sb.toString();
    }
}
