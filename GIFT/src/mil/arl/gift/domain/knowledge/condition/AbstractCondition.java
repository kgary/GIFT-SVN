/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.condition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.lang.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import generated.dkf.PerformanceMetricArguments;
import generated.dkf.UnitsEnumType;
import generated.dkf.ViolationTime;
import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.aar.ScoreNodeUpdate;
import mil.arl.gift.common.coordinate.CoordinateUtil;
import mil.arl.gift.common.coordinate.GDC;
import mil.arl.gift.common.course.dkf.ConditionDescription;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession.SessionMembers;
import mil.arl.gift.common.course.dkf.session.SessionMember;
import mil.arl.gift.common.course.dkf.team.AbstractTeamUnit;
import mil.arl.gift.common.course.dkf.team.Team;
import mil.arl.gift.common.course.dkf.team.TeamMember;
import mil.arl.gift.common.course.dkf.team.TeamOrganization;
import mil.arl.gift.common.course.dkf.team.TeamUtil;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.DamageEnum;
import mil.arl.gift.common.enums.MessageTypeEnum;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.score.DefaultRawScore;
import mil.arl.gift.common.score.RawScoreNode;
import mil.arl.gift.common.ta.state.EntityIdentifier;
import mil.arl.gift.common.ta.state.EntityState;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;
import mil.arl.gift.domain.knowledge.VariablesHandler;
import mil.arl.gift.domain.knowledge.common.ConditionActionInterface;
import mil.arl.gift.domain.knowledge.common.PlacesOfInterestManager;
import mil.arl.gift.domain.knowledge.condition.SessionConditionsBlackboardMgr.SessionConditionsBlackboard;
import mil.arl.gift.domain.knowledge.conversation.ConversationManager.ConversationAssessment;
import mil.arl.gift.domain.knowledge.scoring.AbstractScorer;
import mil.arl.gift.domain.knowledge.scoring.CompletionTimeScorer;
import mil.arl.gift.domain.knowledge.scoring.CountScorer;
import mil.arl.gift.domain.knowledge.scoring.IntegrationScorer;
import mil.arl.gift.domain.knowledge.scoring.OperatorEval;
import mil.arl.gift.net.api.message.DomainSessionMessage;
import mil.arl.gift.net.api.message.Message;

/**
 * This is the base class for all conditions which are associated with metrics and can be evaluated for assessment.
 *
 * @author mhoffman
 *
 */
public abstract class AbstractCondition{

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractCondition.class);

    /** name of the stale violators timer */
    private static final String STALE_VIOLATORS_TIMER_NAME = "Stale violators timer";

    /**
     * used in {@link StringUtils} to create a string of the team members provided.
     */
    protected final static Stringifier<TeamMember<?>> TEAM_MEMBER_STRINGIFIER = new Stringifier<TeamMember<?>>() {
        @Override
        public String stringify(TeamMember<?> teamMember) {
           return teamMember != null ? teamMember.getName() : LEARNER;
        }
     };
     
     /**
      * Used to artifically (i.e. not an authored overall assessment) create a violation 
      * time scorer to track when the condition is actively being scored (e.g. condition violation).
      * Only needed if the author hasn't specified an overall assessment type of Violation Time.
      */
     private static final ViolationTime SCORING_EVENT_ACTIVE_TIME_CONFIG;
     static{
         SCORING_EVENT_ACTIVE_TIME_CONFIG = new ViolationTime();
         SCORING_EVENT_ACTIVE_TIME_CONFIG.setName("Actively Violating");
         SCORING_EVENT_ACTIVE_TIME_CONFIG.setUnits(UnitsEnumType.HH_MM_SS);
         
         generated.dkf.Evaluators evaluators = new generated.dkf.Evaluators();
         SCORING_EVENT_ACTIVE_TIME_CONFIG.setEvaluators(evaluators);
     }

     /** the delimiter to use between team member names */
     protected final static String TEAM_MEMBER_STRINGIFIER_DELIM = ", ";

    /** unique id for this condition instance */
    protected UUID conditionInstanceID = UUID.randomUUID();

    /** date formatter used for time stamps */
    //NOTE: set the time zone to GMT since getTime uses that time zone
    protected static FastDateFormat fdf = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss", TimeZone.getDefault(), Locale.getDefault());

    /** the current assessment level of this condition */
    private AssessmentLevelEnum currentAssessment = AssessmentLevelEnum.UNKNOWN;

    /** the default assessment for all conditions */
    private AssessmentLevelEnum defaultAssessment = AssessmentLevelEnum.UNKNOWN;

    /**
     * an assessment explanation provided by the assessment logic in GIFT.  E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition}
     * might provide an explanation of "Vehicle 1 went 37 mph during a 35 mph speed limit".
     */
    protected String assessmentExplanation = null;

    /** how confident is the assessment of the learner on this concept */
    protected float confidence = AbstractAssessment.DEFAULT_CONFIDENCE;

    /** how competent is the learner on this concept */
    protected float competence = AbstractAssessment.DEFAULT_COMPETENCE;

    /** the assessment trend of the learner on this concept */
    protected float trend = AbstractAssessment.DEFAULT_TREND;

    /**
     * Defines the importance of the condition compared to the other conditions.
     * The value may change during the execution of the course.
     */
    protected Integer priority;

    /** this is the interface which needs to be called to notify the metric of condition actions (e.g. the condition has an assessment) 
     * Will be null before {@link Concept} calls {@link #initialize(ConditionActionInterface)} and when {@link Concept}
     * is being cleaned up. */
    protected ConditionActionInterface conditionActionInterface;

    /** the time at which this condition was initialized */
    protected Date initializedAtTime = null;

    /** the time at which this condition was started */
    protected Date startAtTime = null;

    /** the time at which the condition completed */
    protected Date completedAtTime = null;

    /**
     * The authored team organization for this assessment.  Contains the latest entity identifiers
     * and the authored attributes to identify team members.
     * Can be null if teams or identifying the learner amongst other players are not important.
     */
    private TeamOrganization teamOrganization = null;

    /**
     * Used to filter message traffic based on specific team members being assessed in this condition.
     * Can be null if this type of filtering is not needed or the condition will rely on the learner
     * team member attribute being set in the team organization.
     */
    private generated.dkf.TeamMemberRefs teamMembersBeingAssessed = null;    
    
    /** contains the READ-ONLY entity markings of the team members that are being assessed in this condition */
    private Set<String> assessedMarkings = null;

    /** used to organize places of interest which can be referenced by name in various parts of the DKF */
    protected PlacesOfInterestManager placesOfInterestManager = null;

    /**
     * Used to handle retrieval of variables that have
     * been provided during course execution
     */
    protected VariablesHandler varsHandler;

    /**
     * the course folder that contains all assets for the course that contains the DKF of which
     * uses this condition. This is the either the workspace folder or the runtime folder depending
     * on the source of instantiating this condition (workspace folder = validation, not running a course object with a DKF).
     * Can be null.
     */
    protected AbstractFolderProxy courseFolder;
    
    /**
     * the folder where the output for a domain session instance is being written too. This could
     * include domain session log, video files, sensor data.
     * Will be null if not in a domain session, i.e. validating a DKF could be done outside of a domain session.
     */
    protected DesktopFolderProxy outputFolder;

    /**
     * mapping of authored real time scoring rules that will replace the default logic in a condition<br/>
     * Key: scorer class (only support one of each scorer class per condition)<br/>
     * Value: instance of that scorer class with authored rules
     */
    protected Map<Class<? extends AbstractScorer>, AbstractScorer> realTimeScorers = new HashMap<>();

    /**
     * mapping of authored overall scoring rules that will be used to grade the learner at the end
     * of a real time assessment (not during the real time assessment)
     * Key: scorer class (only support one of each scorer class per condition)
     * Value: instance of that scorer class with authored rules
     */
    protected Map<Class<? extends AbstractScorer>, Map<TeamMember<?>, AbstractScorer>> scorers = new HashMap<>();

    /**
     * for scheduling a task for creating assessments out of synch with
     * incoming simulation messages
     * Note: can be null if a timer is not needed for this condition.
     */
    private SchedulableTimer resetTimer = null;

    /**
     * for scheduling a task for checking for stale violators of a condition.
     * Note: can be null if a timer is not needed for this condition.
     */
    private SchedulableTimer staleViolatorsTimer = null;

    /**
     * the number of milliseconds to wait before resetting the assessment value for this condition
     * to the default assessment value.
     * This is used with the reset timer logic.
     */
    private Long resetAssessmentDelay = null;

    /** amount of milliseconds needed to consider a violation expired */
    private static final long STALE_VIOLATION_DURATION = 10000;

    /** amount of milliseconds between checking for stale violations */
    private static final long STALE_VIOLATION_CHECK_DURATION = 5000;

    /** a display name for the learner when there is only one learner in the knowledge session */
    protected static final String LEARNER = "learner";
    
    /** instance of the black board for the domain session this condition is in
     * Should be set once a domain session message is received */
    protected SessionConditionsBlackboard blackboard;

    /**
     * mapping of entity state entity identifier to the last time that entity violated the condition
     * This is needed because when using applications like VBS the entity state messages could just stop
     * when an entity has mounted a vehicle and GIFT will never know that the entity is no longer violating
     * a condition.
     */
    private Map<EntityIdentifier, Long> violatorTimeMap = new HashMap<>();

    /**
     * A mapping of entities to violators whose violation is caused by the
     * entity. This map is used so that if on of the violators dependencies
     * becomes stale, they can be removed from the list of violators.
     */
    private final Map<EntityIdentifier, Set<EntityIdentifier>> entityToDependentViolators = new HashMap<>();

    /** timer task used to check for stale violators */
    private StaleViolatorsTimeTask staleViolatorsTimeTask = null;

    /** Point that is cached for performance reasons and is used when looking up team locations */
    private Point3d cachedTeamPoint = new Point3d();

    /**
     * A mapping of {@link TeamMember} to its corresponding metadata. This is
     * used to cache information related to a specific entity represented by
     * {@link EntityState} update messages. This is useful for
     * {@link AbstractCondition} objects which need to remember the current
     * state of active entities within the scenario. Entities that become
     * 'stale' (stop broadcasting updates) are routinely purged by the
     * {@link #staleViolatorsTimer}. To prevent an entity from becoming stale,
     * push all of its {@link EntityState} updates into the
     * {@link AssessedEntityMetadata#updateMetadata(EntityState)} method.
     */
    protected final Map<EntityIdentifier, AssessedEntityMetadata> entityMetadataLookup = new HashMap<>();
    
    /** The optional arguments to be passed into the algorithm that handles this condition's performance metrics */
    private PerformanceMetricArguments performanceArguments;

    /**
     * Default constructor - doesn't use the reset assessment timer logic
     */
    public AbstractCondition(){}

    /**
     * Class constructor - doesn't use the reset assessment timer logic.
     *
     * @param defaultAssessment the default assessment value to use as the
     *        current assessment value for this condition now and if the
     *        resetDelay is set later, after each assessment reset timer event
     *        fires. The value can't be null.
     */
    public AbstractCondition(AssessmentLevelEnum defaultAssessment) {
        this(defaultAssessment, null);
    }

    /**
     * Class constructor - uses the reset assessment timer logic to reset this condition's assessment value
     * to the default value provided after the delay specified.  The reset logic will only fire after the
     * condition implementation class calls the 'updateAssessment' method in this class.
     *
     * @param defaultAssessment the default assessment value to use as the current
     * assessment value for this condition now and after each assessment reset timer event
     * fires.  The value can't be null.
     * @param resetAssessmentDelay the number of milliseconds to wait before resetting the assessment
     * value for this condition to the default assessment value.  If the value is less than 1, the reset
     * timer is not used as this is an indication the condition doesn't want it's assessment value automatically
     * changed.
     */
    public AbstractCondition(AssessmentLevelEnum defaultAssessment, Long resetAssessmentDelay){

        if(defaultAssessment == null){
            throw new IllegalArgumentException("The default assessment can't be null.");
        }

        this.defaultAssessment = defaultAssessment;
        this.currentAssessment = defaultAssessment;

        setResetAssessmentDelay(resetAssessmentDelay);
    }
    
    /**
     * Set the black board instance this condition should use based on the domain session the message is from.
     * 
     * @param message if a domain session message than use the domain session id to set the black board instance
     * for this condition, otherwise this method does nothing.
     */
    protected void setBlackboard(Message message){
        if(blackboard != null){
            return;
        }
        
        if(message instanceof DomainSessionMessage){
            DomainSessionMessage dsMsg = (DomainSessionMessage)message;
            blackboard = SessionConditionsBlackboardMgr.getInstance().getSessionBlackboard(dsMsg.getDomainSessionId());
        }
    }
    
    /**
     * Return the course level unique id of this condition created randomly amongst conditions.
     * Can be used to identify conditions under a concept.
     * @return the unique id of this condition instance.
     */
    public UUID getId(){
        return conditionInstanceID;
    }
    
    /**
     * Return the team member's being assessed by this condition instance.  This has no indication
     * about whether these team members are being played by learners or not.
     * @return normally contains one or more references to team members
     * in the team organization hirerachy.  Can be null.
     */
    protected generated.dkf.TeamMemberRefs getTeamMembersBeingAssessed(){
        return teamMembersBeingAssessed;
    }
    
    /**
     * Set the team member's being assessed by this condition instance.  This has no indication
     * about whether these team members are being played by learners or not.
     * 
     * @param teamMembersBeingAssessed normally contains one or more references to team members
     * in the team organization hierarchy.  Can be null.
     */
    protected void setTeamMembersBeingAssessed(generated.dkf.TeamMemberRefs teamMembersBeingAssessed){
        this.teamMembersBeingAssessed = teamMembersBeingAssessed;
    }

    /**
     * Set the assessment reset delay time value.
     * @param resetAssessmentDelay the number of milliseconds to wait before resetting the assessment
     * value for this condition to the default assessment value.  If the value is less than 1, the reset
     * timer is not used as this is an indication the condition doesn't want it's assessment value automatically
     * changed.
     */
    protected void setResetAssessmentDelay(Long resetAssessmentDelay){

        if(resetAssessmentDelay != null && resetAssessmentDelay > 0){
            this.resetAssessmentDelay = resetAssessmentDelay;
        }else{
            this.resetAssessmentDelay = null;
        }
    }

    /**
     * Set the places of interest manager which is used to organize places of interest which can be referenced by
     * name in various parts of the DKF.
     *
     * @param placesOfInterestManager can be null
     */
    public void setPlacesOfInterestManager(PlacesOfInterestManager placesOfInterestManager){
        this.placesOfInterestManager = placesOfInterestManager;
    }

    /**
     * Set the variable handler used to handle retrieval of assessment variables that have
     * been provided during course execution.
     *
     * @param conversationVarHandler can't be null.
     */
    public void setVarsHandler(VariablesHandler varsHandler){

        if(varsHandler == null){
            throw new IllegalArgumentException("The conversation variable handler can't be null.");
        }

        this.varsHandler = varsHandler;
    }

    /**
     * Set the course folder that contains all assets for the course that contains the DKF of which
     * uses this condition.  
     *
     * @param courseFolder can't be null.  This is the either the workspace folder or the runtime folder depending
     * on the source of instantiating this condition (workspace folder = validation, not running a course object with a DKF).
     */
    public void setCourseFolder(AbstractFolderProxy courseFolder){

        if(courseFolder == null){
            throw new IllegalArgumentException("The course folder can't be null.");
        }

        this.courseFolder = courseFolder;
    }
    
    /**
     * Set the folder where the output for a domain session instance is being written too. This could
     * include domain session log, video files, sensor data.  Currently the output folder is always a desktop folder
     * and never resides in a content management system (e.g. Nuxeo).      
     * @param outputFolder can't provide null here.  If null then just don't call this method.  The outputFolder is
     * null when, for example, validating a DKF could be done outside of a domain session.
     */
    public void setOutputFolder(DesktopFolderProxy outputFolder) {
        
        if(outputFolder == null) {
            throw new IllegalArgumentException("The output folder can't be null.");
        }

        this.outputFolder = outputFolder;
    }

    /**
     * Return the current assessment level for this condition
     *
     * @return the current assessment level, won't be null but can be UKNOWN type.
     */
    public AssessmentLevelEnum getAssessment(){
        return currentAssessment;
    }

    /**
     * Return how confident is the assessment of the learner on this concept
     *
     * @return a value between {@link #MIN_CONFIDENCE} and {@link #MAX_CONFIDENCE} (inclusive)
     * The default value is {@link #DEFAULT_CONFIDENCE}
     */
    public float getConfidence() {
        return confidence;
    }

    /**
     * Set how confident is the assessment of the learner on this concept
     *
     * @param newConfidence the value must be between {@link #MIN_CONFIDENCE} and {@link #MAX_CONFIDENCE} (inclusive)
     */
    public void updateConfidence(float newConfidence) {

        if(newConfidence < AbstractAssessment.MIN_CONFIDENCE || newConfidence > AbstractAssessment.MAX_CONFIDENCE){
            throw new IllegalArgumentException("The confidence value of "+newConfidence+" is not between "+AbstractAssessment.MIN_CONFIDENCE+" and "+AbstractAssessment.MAX_CONFIDENCE+" (inclusive)");
        }

        this.confidence = newConfidence;
    }

    /**
     * Return how competent is the learner on this concept
     *
     * @return a value between {@link #MIN_COMPETENCE} and {@link #MAX_COMPETENCE} (inclusive)
     * The default value is {@link #DEFAULT_COMPETENCE}
     */
    public float getCompetence() {
        return competence;
    }

    /**
     * Set how competent is the learner on this concept
     *
     * @param newCompetence the value must be between {@link #MIN_COMPETENCE} and {@link #MAX_COMPETENCE} (inclusive)
     */
    public void updateCompetence(float newCompetence) {

        if(newCompetence < AbstractAssessment.MIN_COMPETENCE|| newCompetence > AbstractAssessment.MAX_COMPETENCE){
            throw new IllegalArgumentException("The competence value of "+newCompetence+" is not between "+AbstractAssessment.MIN_COMPETENCE+" and "+AbstractAssessment.MAX_COMPETENCE+" (inclusive)");
        }

        this.competence = newCompetence;
    }

    /**
     * Return the assessment trend of the learner on this concept
     *
     * @return a value between {@link #MIN_TREND} and {@link #MAX_TREND} (inclusive)
     * The default value is {@link #DEFAULT_TREND}
     */
    public float getTrend() {
        return trend;
    }

    /**
     * Set  the assessment trend of the learner on this concept
     *
     * @param trend the value must be between {@link #MIN_TREND} and {@link #MAX_TREND} (inclusive)
     */
    public void updateTrend(float trend) {

        if(trend < AbstractAssessment.MIN_TREND|| trend > AbstractAssessment.MAX_TREND){
            throw new IllegalArgumentException("The trend value of "+trend+" is not between "+AbstractAssessment.MIN_TREND+" and "+AbstractAssessment.MAX_TREND+" (inclusive)");
        }

        this.trend = trend;
    }

    /**
     * Return the priority value for this performance assessment node.
     *
     * @return Integer the priority value.  Can be null.  Value will not be less than 1.
     */
    public Integer getPriority(){
        return priority;
    }

    /**
     * Set the priority value for this performance assessment node.
     *
     * @param priority the priority value.  Can be null.  Value can not be less than 1.
     */
    public void updatePriority(Integer priority){

        if(priority != null && priority < 1){
            throw new IllegalArgumentException("The priority must be greater than zero.");
        }

        this.priority = priority;
    }

    /**
     * Send the current assessment up the task/concept hierachy for possible use in a performance assessment message.
     */
    protected void sendAssessmentEvent(){
        if(conditionActionInterface != null){
            conditionActionInterface.conditionAssessmentCreated(this);
        }
    }

    /**
     * Set the authored team organization for this assessment to be used by the condition classes.
     * Contains the latest entity identifiers and the authored attributes to identify team members.
     *
     * @param teamOrganization Can be null if teams or identifying the learner amongst other players are not important.
     */
    public void setTeamOrganization(TeamOrganization teamOrganization){
        this.teamOrganization = teamOrganization;   
        
        // when the scorers don't contain an integration scorer, add one that will be used to track
        // the total time this condition is violating.  This default scorer is useful for Observed Assessment
        // conditions in order to make sure the condition passes the dkf course concept validation checks that
        // require at least one overall assessment scorer.
        if(!scorers.containsKey(IntegrationScorer.class)){
            // add one to the null team member here which also covers the case of the condition not being associated
            // with a team member.
            // The for loop later in this method will make sure all team members have a mapped instance each 
            // if there are team members defined.  
            AbstractScorer scoringEventActiveScorer = createScorer(SCORING_EVENT_ACTIVE_TIME_CONFIG);
            scoringEventActiveScorer.setInternalUseOnly(true);

            Map<TeamMember<?>, AbstractScorer> teamMemberScorers = new HashMap<>();
            teamMemberScorers.put(null, scoringEventActiveScorer);
            scorers.put(IntegrationScorer.class, teamMemberScorers);
        }
        
        // no additional configuration is needed if this condition is not associated with specific team members
        if(teamMembersBeingAssessed == null || teamMembersBeingAssessed.getTeamMemberRef().isEmpty()){
            return;
        }
        
        // make sure there are integration scorers for all team members being assessed in this condition
        // to, at a minimum, determine whether a specific team member is actively violating the condition.        
        // copy overall assessment scorers that are not assigned to specific team members
        // and assign one copy to each team member being assessed
        // - this can only be done once users have been assigned to team member positions in the team org.
        for(Map<TeamMember<?>, AbstractScorer> teamMemberScorers : scorers.values()){
            
            AbstractScorer scorer = teamMemberScorers.get(null);                
            for(String teamMemberRef : teamMembersBeingAssessed.getTeamMemberRef()){
                
                TeamMember<?> teamMember = getTeamMember(teamMemberRef);
                if(teamMember != null){
                    
                    if(teamMemberScorers.get(teamMember) != null){
                        // already have a scorer for this team member for this scorer type
                        continue;
                    }
                    
                    AbstractScorer scorerCopy = null;
                    if(scorer instanceof IntegrationScorer){
                        scorerCopy = IntegrationScorer.deepCopy((IntegrationScorer) scorer);
                    }else if(scorer instanceof CountScorer){
                        scorerCopy = CountScorer.deepCopy((CountScorer) scorer);
                    }else if(scorer instanceof CompletionTimeScorer){
                        scorerCopy = CompletionTimeScorer.deepCopy((CompletionTimeScorer) scorer);
                    }
                    
                    if(scorerCopy != null){
                        teamMemberScorers.put(teamMember, scorerCopy);
                    }
                }
            }
        }
    }

    /**
     * Return the team member for the learner running this assessment.
     * The value can be null if the learner's team member has not been identified or the training application doesn't need to identify the
     * learner amongst other players.
     *
     * @return the learner's team member. Can be null.
     */
    public TeamMember<?> getLearnerTeamMember(){
        return teamOrganization != null ? teamOrganization.getLearnerTeamMember() : null;
    }

    /**
     * Return the team organization names that are assessed by this condition.
     * Used to filter message traffic based on specific team members being assessed in this condition.
     * @return Can be null if this type of filtering is not needed or the condition will rely on the learner
     * team member attribute being set in the team organization.
     */
    public generated.dkf.TeamMemberRefs getTeamOrgRefs(){
        return teamMembersBeingAssessed;
    }
    
    /**
     * Returns a read only set of the entity markings for the team members being assessed
     * by this condition instance.
     * @return a read only set containing the entity markings for the team members being assessed
     */
    protected Set<String> getAssessedEntityMarkings(){
        
        if(assessedMarkings == null && teamMembersBeingAssessed != null){
            Set<String> localSet = new HashSet<>();
            for(String teamMemberRef : teamMembersBeingAssessed.getTeamMemberRef()){
                TeamMember<?> member = getTeamMember(teamMemberRef);
                if(member != null && member.getIdentifier() instanceof String){
                    localSet.add((String)member.getIdentifier());
                }
            }
            
            assessedMarkings = Collections.unmodifiableSet(localSet);
        }
        
        return assessedMarkings;
    }

    /**
     * Return the team member with the specified unique authored name.
     *
     * @param teamMemberName the unique authored team element name to look for in the team organization
     * @return the found team member with the given authored name.  Can be null if a team element in the team organization
     * could not be found with the name or the team element was a team and not a team member.
     */
    public TeamMember<?> getTeamMember(String teamMemberName){
        TeamMember<?> teamMember = null;
        AbstractTeamUnit teamUnit = teamOrganization != null ? teamOrganization.getTeamElementByName(teamMemberName) : null;
        if(teamUnit != null && teamUnit instanceof TeamMember<?>){
             teamMember = (TeamMember<?>) teamUnit;
        }
        return teamMember;
    }

    /**
     * Whether the provided entity identifier matches one of the interested team
     * members for this condition's assessment.
     * Note: this currently just checks the entity id and not the simulation address information.
     *
     * @param anEntityIdentifier the identifier information to compare against the identifier known to
     * the important team member(s) for this condition.
     * @return the condition's team member instance for the entity id provided, null if the entity id is not for
     * a condition's referenced team member.
     */
    public TeamMember<?> isConditionAssessedTeamMember(EntityIdentifier anEntityIdentifier){

        if(anEntityIdentifier == null){
            return null;
        }else if(teamMembersBeingAssessed == null){
            // The condition hasn't specified specific team members to check for, therefore fall back
            // to the original logic and check if provided entity identifier is for the learner of this real time assessment
            TeamMember<?> learnerTeamMember = getLearnerTeamMember();
            if(learnerTeamMember != null && learnerTeamMember.getEntityIdentifier() != null &&
                    anEntityIdentifier != null && anEntityIdentifier.getEntityID() == learnerTeamMember.getEntityIdentifier().getEntityID()){
                return learnerTeamMember;
            }
        }else if(teamOrganization != null){
            // The condition has specified specific team members to check for, determine if the
            // provided entity identifier is for one of those team members
            for(String teamMemberName : teamMembersBeingAssessed.getTeamMemberRef()){
                TeamMember<?> teamMember = getTeamMember(teamMemberName);

                if(teamMember != null && teamMember.getEntityIdentifier() != null && anEntityIdentifier.getEntityID().equals(teamMember.getEntityIdentifier().getEntityID())){
                    return teamMember;
                }
            }
        }

        return null;
    }
    
    /**
     * Return whether the team member is one that this condition is assessing.
     * @param teamMemberCandidate the team member to check if it is one of the team members
     * this condition is responsible for assessing
     * @return true if the provided team member is one that this condition is assessing.
     */
    public boolean isConditionAssessedTeamMember(TeamMember<?> teamMemberCandidate){
        
        if(teamMemberCandidate != null && teamMembersBeingAssessed != null){
             return teamMembersBeingAssessed.getTeamMemberRef().contains(teamMemberCandidate.getName());
        }
        
        return false;
        
    }


    /**
     * Return the team member in the team org for the entity represented by the entity state.
     *
     * @param es an entity state to get the team member for in the team org.
     * @return the team member in the team org for the entity represented by the entity state.  Will be null
     * if there is no team org or a team member couldn't be found by start location or entity marking checks.
     */
    public TeamMember<?> getTeamMemberFromTeamOrg(EntityState es){

        TeamMember<?> foundTeamMember = null;
        if(teamOrganization != null){
            foundTeamMember = TeamUtil.getTeamMemberByEntityState(es, cachedTeamPoint, teamOrganization.getRootTeam());
        }

        return foundTeamMember;
    }
    
    /**
     * Return the team member in the team org for the entity represented by the entity identifier
     *
     * @param id an entity ID to get the team member for in the team org.
     * @return the team member in the team org for the entity represented by the entity ID.  Will be null
     * if there is no team org or a team member couldn't be found by start location or entity marking checks.
     */
    public TeamMember<?> getTeamMemberFromTeamOrg(EntityIdentifier id){

        TeamMember<?> foundTeamMember = null;
        if(teamOrganization != null){
            AbstractTeamUnit teamUnit = teamOrganization.getRootTeam().getTeamElementByEntityId(id); 
            if(teamUnit instanceof TeamMember<?>) {
                foundTeamMember = (TeamMember<?>) teamUnit;
            }
        }

        return foundTeamMember;
    }
    
    /**
     * Return the team member in the team org for the entity marking provided.
     * @param entityMarking a unique string that identifies this entity in training application state message.  If 
     * null or empty this method returns null.
     * @return the team member found by entity marking match.  Can be null.
     */
    public TeamMember<?> getTeamMemberFromTeamOrg(String entityMarking){
        TeamMember<?> foundTeamMember = null;
        if(teamOrganization != null){
            foundTeamMember = TeamUtil.getTeamMemberByEntityMarking(entityMarking, teamOrganization.getRootTeam());
        }

        return foundTeamMember;
    }

    /**
     * Checks whether the condition references at least one team member in the team organization.
     *
     * @return the reason the refs are invalid<br/>
     * 1. null if the team org has not been set in this condition (which also means it isn't defined
     * in the dkf) <br/>
     * 2. null if the team org has zero members <br/>
     * 3. null if the team org has one or more members and the condition references one or more team members <br/>
     * 4. a message if the team org has one or more members and the condition references zero team members <br/>
     * 5. a message if the condition references team organization entries that aren't in the team org.<br/>
     */
    public String hasValidTeamMemberRefs(){

        if(teamOrganization != null){
            // a team org has been provided for this DKF instance

            Team rootTeam = teamOrganization.getRootTeam();
            int totalMembers = rootTeam.getNumberOfTeamMembers();
            if(totalMembers > 0){

                if(teamMembersBeingAssessed == null || teamMembersBeingAssessed.getTeamMemberRef().isEmpty()){
                    // there is a team org with at least one team member but the condition doesn't
                    // reference any team members when it should in order to return true for this method.
                    return "missing a team organization reference";
                }
            }

        }

        // make sure condition's team member refs are all in the team org
        if(teamMembersBeingAssessed != null && !teamMembersBeingAssessed.getTeamMemberRef().isEmpty()){

            if(teamOrganization == null){
                return "found a team organization reference but the team organization is empty.";
            }

            for(String teamMemberRef : teamMembersBeingAssessed.getTeamMemberRef()){

                if(teamOrganization.getTeamElementByName(teamMemberRef) == null){
                    return "found a team organization reference of '"+teamMemberRef+"' that isn't in the team organization.";
                }
            }
        }


        return null;
    }

    /**
     * Update the condition's current assessment level to the value provided.
     * Note: this should be called every time the condition reports an assessment value (other than null) to it's parent concept.
     *
     * @param newAssessmentLevel a new assessment for this condition
     */
    public void updateAssessment(AssessmentLevelEnum newAssessmentLevel){

        if(newAssessmentLevel != null && getAssessment() != newAssessmentLevel){

            if(logger.isDebugEnabled()){
                logger.debug("Updating assessment of "+this+" to "+newAssessmentLevel);
            }

            currentAssessment = newAssessmentLevel;
        }

        //schedule an assessment reset because the assessment value changed
        scheduleAssessmentReset();
    }
    
    /**
     * Update the condition's current assessment level to the value provided and, if needed,
     * force the assessment to roll up to the parent concept to change its assessment
     *
     * @param newAssessmentLevel a new assessment for this condition
     * @param forceRollup whether the assessment should be forced to roll up to the parent concept
     * to change its assessment
     */
    public void updateAssessment(AssessmentLevelEnum newAssessmentLevel, boolean forceRollup){
        
        updateAssessment(newAssessmentLevel);
        
        if(forceRollup) {
            if(conditionActionInterface != null){
                conditionActionInterface.conditionAssessmentCreated(this);
            }
        }
    }

    /**
     * Method that is executed when the concept's assessment is updated externally (not from the
     * condition assessments).
     */
    public void assessmentUpdatedExternally() {
        // do nothing by default
    }

    /**
     * Initialize the condition
     *
     * @param conditionActionInterface - the interface used to notify when condition actions happen. Can't be null.
     */
    public void initialize(ConditionActionInterface conditionActionInterface){

        if(logger.isInfoEnabled()){
            logger.info("Initializing "+this);
        }

        if(conditionActionInterface == null){
            throw new IllegalArgumentException("The condition action interface can't be null.");
        }

        this.conditionActionInterface = conditionActionInterface;

        this.initializedAtTime = new Date();
    }

    /**
     * Schedule a reset assessment timer task some time in the future.
     * If there is an event already schedule, it is canceled first.  If the
     * reset assessment delay value was not set correctly this method will not
     * schedule a timer task.
     */
    protected void scheduleAssessmentReset(){

        if(resetTimer != null){
            try{
                resetTimer.cancel();
            }catch(Throwable t){
                logger.warn("There was a problem canceling the assessment reset timer for the condition of "+this, t);
            }

            resetTimer = null;
        }

        if(resetAssessmentDelay != null){
            resetTimer = new SchedulableTimer("Reset Assessment Delay timer");
            resetTimer.scheduleTask(new ResetTimerTask(), resetAssessmentDelay);
        }
    }

    /**
     * Start checking the condition.  This means the metric has been started.
     */
    public void start(){

        if(logger.isInfoEnabled()){
            logger.info("Starting "+this);
        }

        startAtTime = new Date();

        completedAtTime = null; //reset this to handle when this condition is started after being completed
    }

    /**
     * Stop checking the condition.  This means the metric is stopping.
     */
    public void stop(){

        if(logger.isInfoEnabled()){
            logger.info("Stopping "+this);
        }

        //cancel timer - has no affect if the timer task has already been fired
        if(resetTimer != null){
            try{
                resetTimer.cancel();
            }catch(Throwable t){
                logger.warn("There was a problem canceling the assessment reset timer for the condition of "+this+".", t);
            }
        }

        if(staleViolatorsTimer != null){
            try{
                staleViolatorsTimer.cancel();
            }catch(Throwable t){
                logger.warn("There was a problem canceling the stale violators timer for the condition of "+this+".", t);
            }
        }
    }

    /**
     * Notification that the condition is wanting to start a scoring event
     * (e.g. Enemy have been eliminated, Learner has broke cover and concealment)
     * @param teamMembersBeingScored the team member's that are causing this scoring event. Can be empty.
     */
    protected void scoringEventStarted(TeamMember<?>... teamMembersBeingScored){
        scoringEventStarted(1, teamMembersBeingScored);
    }

    /**
     * Return whether a scoring event has started and has not ended by calling {@link #scoringEventEnded()}.
     * This is useful for determining whether an event is a new event or a continuation of a previously assessed
     * or scored event.
     *
     * @param teamMembersBeingScored the team member's that are causing this scoring event. Can be empty.  If empty (null),
     * than only the team (or not specific to an single team member) scoring attributes will be updated.
     * @return true if a scoring event is currently active and hasn't ended.
     */
    protected boolean isScoringEventActive(TeamMember<?>... teamMembersBeingScored){
        
        Map<TeamMember<?>, AbstractScorer> teamMemberScorers = scorers.get(IntegrationScorer.class);
        
        if(teamMembersBeingScored.length == 0){

            if(teamMemberScorers == null || teamMemberScorers.isEmpty()){
                // no overall assessment duration scorer was authored and one has not been artificially
                // added yet to track this conditions active scoring event, need to add that now
                
                AbstractScorer scoringEventActiveScorer = createScorer(SCORING_EVENT_ACTIVE_TIME_CONFIG);
                scoringEventActiveScorer.setInternalUseOnly(true);

                teamMemberScorers = new HashMap<>();
                teamMemberScorers.put(null, scoringEventActiveScorer);
                scorers.put(IntegrationScorer.class, teamMemberScorers);
            }
            
            IntegrationScorer scoringEventActiveScorer = (IntegrationScorer)teamMemberScorers.get(null);
            return scoringEventActiveScorer != null && scoringEventActiveScorer.hasStarted();
        }else{
            // check each team member provided to see if the integration scorer is active for all
            
            if(teamMemberScorers == null || teamMemberScorers.isEmpty()){
                // ERROR - there are no integration scorers but the caller is providing team members being assessed/scored
                //         This means setTeamMembersBeingAssessed method was not called.
                logger.warn("Unable to determine whether the condition's scoring event is active for one or more team members "+
                        "because the team members being assessed was not set.  Setting that will make sure duration based scorers "+
                        "exists for all specified team members which is essential for tracking whether a team member is causing a "+
                        "event at the current moment.\n"+toString());
                return false;
            }
            
            for(TeamMember<?> teamMember : teamMembersBeingScored){
                
                IntegrationScorer scoringEventActiveScorer = (IntegrationScorer)teamMemberScorers.get(teamMember);
                if(scoringEventActiveScorer == null){
                    logger.warn("Unable to determine whether the condition's scoring event is active for "+teamMember+
                            " because there is no duration scorer for that team member.  Is this team member one of the "+
                            "team member's being assessed by this condition?\n"+toString());
                    return false;
                }else if(!scoringEventActiveScorer.hasStarted()){
                    if(logger.isDebugEnabled()){
                        logger.debug("A duration based scoring event is not currently active for "+teamMember+
                                ".  Therefore returning false for isScoringEventActive.");
                    }
                    
                    return false;
                }
            }
            
            return true;
        }
    }

    /**
     * Notification that the condition is wanting to start a scoring event
     *
     * @param numberOfEvents - the number of scoring events to score that occurred at one point in time
     * @param teamMembersBeingScored the team member's that are causing this scoring event. Can be empty/null to
     * indicate that all team members being assessed by this condition should be scored (change made based on #5196).
     */
    protected void scoringEventStarted(int numberOfEvents, TeamMember<?>... teamMembersBeingScored){

        if(numberOfEvents < 1){
            throw new IllegalArgumentException("The number of events when the scoring event started must be greater than zero");
        }

        if(logger.isDebugEnabled()){
            logger.debug(this+" is being notified of "+numberOfEvents+" scoring event(s)");
        }
        
        // the set of team members to update scores for
        Set<TeamMember<?>> membersToScore = null;
        
        if(teamMembersBeingScored != null && teamMembersBeingScored.length > 0) {
            // use the provided team members
            membersToScore = new HashSet<>(Arrays.asList(teamMembersBeingScored));
            
            // Always update null team member association to cover both the case when no team members where
            // specified for this condition AND as a summary of all team members being assessed in this condition.
            // Doesn't hurt if this logic is called for each concurrent team member starting a scoring event.
            membersToScore.add(null); 
        }

        //
        // start time interval
        //
        IntegrationScorer integrationScorer;
        
        // ... for overall scoring
        // Keep track of per team member events if possible
        Map<TeamMember<?>, AbstractScorer> integrationScorers = scorers.get(IntegrationScorer.class);
        if(integrationScorers != null){
            
            if(membersToScore == null) {
                // use the condition's assessed team members, already includes the 'null' entry
                membersToScore = integrationScorers.keySet();
            }
            
            for(TeamMember<?> teamMemberBeingScored : membersToScore){
                integrationScorer = (IntegrationScorer)integrationScorers.get(teamMemberBeingScored);
                if(integrationScorer != null){
        
                    if(!integrationScorer.hasStarted()){
                        integrationScorer.start();
                    }
                }
            }                
        }

        // ... for authored real time assessment
        // Doesn't keep track of per team member events.
        integrationScorer = (IntegrationScorer)realTimeScorers.get(IntegrationScorer.class);
        if(integrationScorer != null){

            if(!integrationScorer.hasStarted()){
                integrationScorer.start();
            }
        }

        //
        // increment number of violations
        //
        
        CountScorer countScorer;

        // ... for overall scoring
        // Keep track of per team member events if possible
        Map<TeamMember<?>, AbstractScorer> countScorers = scorers.get(CountScorer.class);
        if(countScorers != null){
            
            if(membersToScore == null) {
                // use the condition's assessed team members, already includes the 'null' entry
                membersToScore = countScorers.keySet();
            }
                    
            for(TeamMember<?> teamMemberBeingScored : membersToScore){
                countScorer = (CountScorer)countScorers.get(teamMemberBeingScored);
                if(countScorer != null){
                    countScorer.add(numberOfEvents);
                }
            }
            
        }

        // ... for authored real time assessment
        // Doesn't keep track of per team member events.
        countScorer = (CountScorer)realTimeScorers.get(CountScorer.class);
        if(countScorer != null){
            countScorer.add(numberOfEvents);
        }
    }

    /**
     * Notification that the condition is no longer encountering a scoring event
     * @param teamMembersBeingScored the team member's that are causing this scoring event. Can be empty.  When empty
     * this should signal that there are no active scoring events at this time.
     */
    protected void scoringEventEnded(TeamMember<?>...teamMembersBeingScored){

        //
        // stop time interval
        //
        IntegrationScorer integrationScorer;

        // ... for overall scoring
        // Keep track of per team member events if possible
        Map<TeamMember<?>, AbstractScorer> integrationScorers = scorers.get(IntegrationScorer.class);
        if(integrationScorers != null){
            
            if(teamMembersBeingScored != null && teamMembersBeingScored.length > 0){
                for(TeamMember<?> teamMemberBeingScored : teamMembersBeingScored){
                    integrationScorer = (IntegrationScorer)integrationScorers.get(teamMemberBeingScored);
                    if(integrationScorer != null){
                        integrationScorer.stop();
                    }
                }
            }else{
            
                // update null team member association to cover both the case when no team members where
                // specified for this condition AND as a summary of all team members being assessed in this condition.
                integrationScorer = (IntegrationScorer)integrationScorers.get(null);
                if(integrationScorer != null){
                    integrationScorer.stop();
                }
            }

        }

        // ... for authored real time assessment
        // Doesn't keep track of per team member events.
        if(teamMembersBeingScored != null && teamMembersBeingScored.length == 0){
            // need an empty parameter to signal that all team member's being assessed are not
            // involved in scored events right now
            integrationScorer = (IntegrationScorer)realTimeScorers.get(IntegrationScorer.class);
            if(integrationScorer != null){
                integrationScorer.stop();
            }
        }
    }

    /**
     * Return whether this condition has completed
     *
     * @return boolean
     */
    public boolean hasCompleted(){

        return completedAtTime != null;
    }

    /**
     * Notification that the condition has completed
     */
    protected void conditionCompleted(){

        if(completedAtTime == null){
            completedAtTime = new Date();

            if(logger.isDebugEnabled()){
                logger.debug("Condition completed - "+this+".");
            }

            //set time to complete condition
            // Keep track of per team member events if possible
            Map<TeamMember<?>, AbstractScorer> completionTimeScorers = scorers.get(CompletionTimeScorer.class);
            if(completionTimeScorers != null){
                
                for(AbstractScorer scorer : completionTimeScorers.values()){
                    
                    if(scorer instanceof CompletionTimeScorer){
                        Calendar timeToComplete = Calendar.getInstance();
                        timeToComplete.setTimeInMillis(completedAtTime.getTime() - startAtTime.getTime());
                        ((CompletionTimeScorer)scorer).setTimeToComplete(timeToComplete);
                    }
                }

            }

            if(conditionActionInterface != null){
                conditionActionInterface.conditionCompleted(this);
            }

        }else{
            logger.warn("The condition has called condition completed more than once - "+this+".");
        }
    }

    /**
     * Return an assessment level based on authored real time assessment rules that override
     * the default condition class logic for assessment.
     *
     * @return an assessment value if any of the authored real time assessment rules evaluate
     * to true.  Null will be returned if there are no authored real time assessment rules.
     * Note that the lowest assessment value will be returned if there are multiple rules
     * that provide an assessment.
     */
    protected AssessmentLevelEnum getAuthoredRealTimeAssessment(){

        AssessmentLevelEnum lowestScore = null;
        for(AbstractScorer scorer : realTimeScorers.values()){

            AssessmentLevelEnum assessment = scorer.getAssessment();
            if(lowestScore == null || assessment == AssessmentLevelEnum.BELOW_EXPECTATION){
                lowestScore = assessment;
            }else if(assessment == AssessmentLevelEnum.UNKNOWN || assessment == lowestScore){
                continue;
            }else if(assessment == AssessmentLevelEnum.AT_EXPECTATION && lowestScore == AssessmentLevelEnum.ABOVE_EXPECTATION){
                lowestScore = assessment;
            }

            if(lowestScore == AssessmentLevelEnum.BELOW_EXPECTATION){
                break;
            }
        }

        return lowestScore;
    }

    /**
     * Add authored real time assessment rules for this condition to track.  These rules over ride
     * the default condition class logic for assessment values.
     *
     * @param realTimeAssessmentRules can be null if not authored.
     */
    protected void addRealTimeAssessmentRules(generated.dkf.RealTimeAssessmentRules realTimeAssessmentRules){

        if(realTimeAssessmentRules == null){
            return;
        }

        if(realTimeAssessmentRules.getCount() != null){
            AbstractScorer scorer = createScorer(realTimeAssessmentRules.getCount());
            realTimeScorers.put(scorer.getClass(), scorer);
        }

        if(realTimeAssessmentRules.getViolationTime() != null){
            AbstractScorer scorer = createScorer(realTimeAssessmentRules.getViolationTime());
            realTimeScorers.put(scorer.getClass(), scorer);
        }
    }

    /**
     * Instantiate a new scorer objects based on the authored scoring object provided.
     *
     * @param scoringType one of {@link @generated.dkf.Count}, {@link @generated.dkf.ViolationTime} or
     * {@link @generated.dkf.CompletionTime}
     *
     * @return the new scorer instance.
     */
    private AbstractScorer createScorer(Serializable scoringType){

        List<OperatorEval> evaluators = new ArrayList<>();

        if(scoringType instanceof generated.dkf.Count){

            generated.dkf.Count count = (generated.dkf.Count)scoringType;
            UnitsEnumType units = UnitsEnumType.fromValue(count.getUnits().value());

            //convert evaluator into operator eval
            evaluators = handleEvaluators(count.getEvaluators().getEvaluator(), count.getName(), units);

            return new CountScorer(count.getName(), units, evaluators);

        }else if(scoringType instanceof generated.dkf.ViolationTime){

            generated.dkf.ViolationTime violation = (generated.dkf.ViolationTime)scoringType;
            UnitsEnumType units = violation.getUnits();

            //convert evaluator into operator eval
            evaluators = handleEvaluators(violation.getEvaluators().getEvaluator(), violation.getName(), units);

            return new IntegrationScorer(violation.getName(), units, evaluators);

        }else if(scoringType instanceof generated.dkf.CompletionTime){

            generated.dkf.CompletionTime completion = (generated.dkf.CompletionTime)scoringType;
            UnitsEnumType units = UnitsEnumType.fromValue(completion.getUnits().value());

            //convert evaluator into operator eval
            evaluators = handleEvaluators(completion.getEvaluators().getEvaluator(), completion.getName(), units);

            return new CompletionTimeScorer(completion.getName(), units, evaluators);

        }else{
            throw new IllegalArgumentException("Found unhandled scoring type in "+scoringType);
        }
    }

    /**
     * Add the dkf contents of scorers to this condition
     *
     * @param scoring - dkf content containing scorers
     * @param memberBeingScored the team member to associate this scorer too.  Can be null.
     */
    public void addScorers(generated.dkf.Scoring scoring, TeamMember<?> memberBeingScored){

        for(Serializable scoringType : scoring.getType()){
            addScorer(createScorer(scoringType), memberBeingScored);
        }
    }

    /**
     * Handle the generated class's evaluators content by converting it into operator eval instances
     * and checking for duplicate logic.
     *
     * @param evaluators - evaluation logic
     * @param countName - the name of the rule given by the author
     * @param units -  the units for the scoring rule (e.g. "hh:mm:ss")
     * @return the logical expressions for the evaluator
     */
    private List<OperatorEval> handleEvaluators(List<generated.dkf.Evaluator> evaluators, String countName, UnitsEnumType units){

        List<OperatorEval> opEvaluators = new ArrayList<>();

        for(generated.dkf.Evaluator evaluator : evaluators){
            opEvaluators.add(new OperatorEval(evaluator, units));
        }

        //check for duplicates
        for(int i = 0; i < opEvaluators.size(); i++){
            for(int j = i+1; j < opEvaluators.size(); j++){

                OperatorEval aOpEval = opEvaluators.get(i);
                OperatorEval bOpEval = opEvaluators.get(j);

                if(aOpEval.equals(bOpEval)){
                    //ERROR
                    throw new IllegalArgumentException("Found duplicate scoring rules of: "+aOpEval+" and "+bOpEval+" in "+countName);
                }
            }
        }

        return opEvaluators;
    }

    /**
     * Handle the assessments of a conversation between the learner and GIFT.
     *
     * @param assessments contains concept assessment information for choices the learner has made in the conversation.
     */
    public void handleConversationAssessment(List<ConversationAssessment> assessments){
        //nothing to do
    }

    /**
     * Add a scorer for this condition
     *
     * @param scorer - the scorer to add to this condition
     * @param memberBeingScored the team member to associate this scorer too.  Can be null.
     */
    public void addScorer(AbstractScorer scorer, TeamMember<?> memberBeingScored){
        
        Map<TeamMember<?>, AbstractScorer> scoringByTeamMember = scorers.get(scorer.getClass());
        if(scoringByTeamMember == null){
            scoringByTeamMember = new HashMap<>();
            scorers.put(scorer.getClass(), scoringByTeamMember);
        }
        
        if(scoringByTeamMember.containsKey(memberBeingScored)){
          throw new IllegalArgumentException("The condition: "+this+" already contains a scorer of class type "+scorer.getClass()+" assigned to the team member "+memberBeingScored);

        }

        scoringByTeamMember.put(memberBeingScored, scorer);

        if(logger.isDebugEnabled()){
            logger.debug("Added scorer: "+scorer+" to condition");
        }
    }

    /**
     * Get the scores for this condition
     *
     * @param conditionScorers - where to place any calculated overall assessment scores.  Can't be null.  
     * Should be empty. Might not have any objects added to it.
     */
    public void getScores(List<RawScoreNode> conditionScores){
        getScores(conditionScores, null);
    }
    
    /**
     * Get the scores for this condition
     *
     * @param conditionScorers - where to place any calculated overall assessment scores.  Can't be null.  
     * Should be empty. Might not have any objects added to it.
     * @param ocAssessmentLevel - an optional score update assigned by an observer controller. If non-null,
     * the score's provided assessment will either:
     *   (a) replace the condition's current assessment value in any generated scores, or
     *   (b) if no scores are generated by any scorers, be placed in a completely new score.
     * If the observer controller has entered any comments or recorded any media associated with the
     * assessment, it will be saved to the generated scores.
     */
    public void getScores(List<RawScoreNode> conditionScores, ScoreNodeUpdate ocAssessmentLevel){
        
        if(conditionScores == null){
            throw new IllegalArgumentException("The condition scores list can't be null.");
        }
        
        if(conditionActionInterface == null){
            logger.warn("Unable to calculate the score(s) for this condition because the condition action interface is null.\n"+this);
            return;
        }
        //
        // Gather the user information for the team members that were assessed by this condition
        //
        final SessionMembers sessionMembers = conditionActionInterface.getSessionMembers();
        final Set<String> usernames = new HashSet<>();
        
        // mapping of unique team member name in the team organization to the unique GIFT username assigned that team member
        Map<String, String> teamMemberRefToUserName = new HashMap<>();
        boolean isSinglePlayerCondition = false;
        if (sessionMembers != null) {
            Map<Integer, SessionMember> dsIdMemberMap = sessionMembers.getSessionMemberDSIdMap();
            if (teamMembersBeingAssessed != null && !teamMembersBeingAssessed.getTeamMemberRef().isEmpty()) {
                /* this condition is specific to certain team members */

                for (String teamMemberRef : teamMembersBeingAssessed.getTeamMemberRef()) {
                    for (SessionMember member : dsIdMemberMap.values()) {

                        /* when the session member's team member is null it
                         * means this is a single player DKF (or pre-teams DKF)
                         * therefore the team member was never assigned and the
                         * team member ref is most likely the default 'learner'
                         * value */
                        TeamMember<?> teamMember = member.getSessionMembership().getTeamMember();
                        boolean isSinglePlayer = dsIdMemberMap.size() == 1 && teamMember == null;
                        if (isSinglePlayer || 
                                (teamMember != null && StringUtils.equals(teamMemberRef, teamMember.getName()))) {
                            // found the session member for this team member

                            if (StringUtils.isNotBlank(member.getSessionMembership().getUsername())) {
                                usernames.add(member.getSessionMembership().getUsername());
                                teamMemberRefToUserName.put(teamMemberRef, member.getSessionMembership().getUsername());
                            }

                            break;
                        }

                    }
                }

                isSinglePlayerCondition = usernames.size() == 1;
            } else {
                /* the condition isn't specific to team members, use all
                 * learners */
                for (SessionMember member : dsIdMemberMap.values()) {
                    if (StringUtils.isNotBlank(member.getSessionMembership().getUsername())) {
                        usernames.add(member.getSessionMembership().getUsername());
                    }
                }
            }
        }
        // convert condition scorers into score nodes
        for(Map<TeamMember<?>, AbstractScorer> memberScoring : scorers.values()){

            for(TeamMember<?> teamMember : memberScoring.keySet()){
                
                AbstractScorer scorer = memberScoring.get(teamMember);
                scorer.cleanup();
                if(scorer.isInternalUseOnly()){
                    // don't include internal scorers as public scores for this condition
                    continue;
                }
                
                DefaultRawScore defaultRawScore = new DefaultRawScore(scorer.getRawScore(), scorer.getUnits().value());
                
                RawScoreNode rawScoreNode;
                if(teamMember == null){ 
                    // this is a scorer for all team members referenced by this condition - or all learners when the condition references no team members   
                    
                    if(isSinglePlayerCondition){
                        // ignore the null teamMember mapping when there is only a single player because this scorer
                        // and the scorer for the single team member will have the same scores.  We don't want the same
                        // score included twice for a single player.
                        continue;
                    }else{
                        
                        if(usernames.isEmpty()) {
                            
                            /* This can happen if a condition is assessing a team member that was not actually
                             * played by a learner, since such a team member would have no corresponding session 
                             * member in the team knowledge session.
                             * 
                             * If this happens, then avoid scoring, since we can't do so meaningfully */
                            return;
                        }
                        
                        AssessmentLevelEnum assessment = ocAssessmentLevel != null ? ocAssessmentLevel.getAssessment() : scorer.getAssessment();
                        
                        rawScoreNode = new RawScoreNode(scorer.getName(), defaultRawScore, assessment, usernames);
                        
                        if(ocAssessmentLevel != null) {
                            rawScoreNode.setEvaluator(ocAssessmentLevel.getEvaluator());
                            rawScoreNode.setObserverComment(ocAssessmentLevel.getObserverComment());
                            rawScoreNode.setObserverMedia(ocAssessmentLevel.getObserverMedia());
                        }
                    }
    
                }else{
                    // scorer is assigned to a specific team member, use the username assigned to that team member
                    Set<String> teamMemberUsername = new HashSet<>();
                    String username = teamMemberRefToUserName.get(teamMember.getName());
                    if(StringUtils.isBlank(username)){
                        // no learner played this team member role that was assessed, therefore there is no learner
                        // to assign this score too.
                        continue;
                    }
                    teamMemberUsername.add(username);
                    
                    AssessmentLevelEnum assessment = ocAssessmentLevel != null ? ocAssessmentLevel.getAssessment() : scorer.getAssessment();
                    
                    rawScoreNode = new RawScoreNode(scorer.getName(), defaultRawScore, assessment, teamMemberUsername);
                    
                    if(ocAssessmentLevel != null) {
                        rawScoreNode.setEvaluator(ocAssessmentLevel.getEvaluator());
                        rawScoreNode.setObserverComment(ocAssessmentLevel.getObserverComment());
                        rawScoreNode.setObserverMedia(ocAssessmentLevel.getObserverMedia());
                    }
                }
                
                conditionScores.add(rawScoreNode);

            }
        }
        
        if(conditionScores.isEmpty() && ocAssessmentLevel != null){
            
            /* Observer controller has provided an overall assessment but there are no scorers for this 
             * node, so we need to create a score from scratch */
            DefaultRawScore teamRawScore = new DefaultRawScore("1", " manual assessment");
            
            RawScoreNode teamScore = new RawScoreNode("OC Assessment", teamRawScore, ocAssessmentLevel.getAssessment(), usernames);
            teamScore.setEvaluator(ocAssessmentLevel.getEvaluator());
            teamScore.setObserverComment(ocAssessmentLevel.getObserverComment());
            teamScore.setObserverMedia(ocAssessmentLevel.getObserverMedia());
            
            conditionScores.add(teamScore);
            
            /* In addition to the team score, we need to provide a score for each individual in the team */
            for(String username : usernames) {
                
                DefaultRawScore individualRawScore = new DefaultRawScore("1", " manual assessment");
                
                Set<String> teamMemberUsername = new HashSet<>();
                teamMemberUsername.add(username);
                
                RawScoreNode individualScore = new RawScoreNode("OC Assessment", individualRawScore, ocAssessmentLevel.getAssessment(), teamMemberUsername);
                individualScore.setEvaluator(ocAssessmentLevel.getEvaluator());
                individualScore.setObserverComment(ocAssessmentLevel.getObserverComment());
                individualScore.setObserverMedia(ocAssessmentLevel.getObserverMedia());
                
                conditionScores.add(individualScore);
            }
        }
    }

    /**
     * Return whether or not this condition class has at least one scorer that can
     * provide an overall assessment for this condition.
     *
     * @return true if there is one or more overall assessment scorers
     */
    public boolean hasScorers(){
        return !scorers.isEmpty();
    }

    /**
     * Process the training application game state message received.  The message usually comes
     * from the Tutor or the Gateway modules.
     *
     * @param message - the training application game state message to handle
     * @return true iff the condition has changed an assessment value ({@link AbstractCondition#currentAssessment},
     *           {@link AbstractCondition#confidence}, {@link AbstractCondition#competence}) as a result of this training application
     *           state message.  False indicates that nothing has changed.  It is acceptable to return true as an indication
     *           that the an assessment event has happened even if the values are the same as the last reported values.
     *           For example, every time a user presses 'button A' a Below assessment value
     *           is returned for a Condition.  Therefore the first and second time that 'button A' is pressed
     *           are different events but with the same back-to-back reported assessment value.
     */
    public abstract boolean handleTrainingAppGameState(Message message);

    /**
     * Return a description of what the condition can assess.
     *
     * @return provides additional information about the condition like what types of assessments it is
     * responsible for.
     */
    public abstract ConditionDescription getDescription();

    /**
     * This method is used to inform the parent performance assessment node would like additional assessing (if available)
     * and that the condition should execute further or different assessments in regards to the
     * knowledge/interface it has at it's disposal.  For instance, a condition may want to perform additional assessment
     * logic such as re-evaluating its previous assessments or using a new algorithm to potentially provide a different
     * assessment value for the parent performance assessment node.
     */
    public void assessCondition(){
        //by default, a condition doesn't have any other logic to assess other than the logic it is already using to assess
        //incoming simulation state messages.
    }

    /**
     * Return the list of GIFT message types this condition is interested in receiving updates for.
     *
     * @return List<MessageTypeEnum> - should not be null or an empty list
     */
    public abstract List<MessageTypeEnum> getSimulationInterests();

    /**
     * Return whether the condition calls {@link #conditionCompleted()} to indicate that the
     * condition is no longer assessing.
     * @return true if the condition ever calls {@link #conditionCompleted()}, false otherwise.
     */
    public abstract boolean canComplete();

    /**
     * Return the unique set of learner actions needed to be shown to the learner for
     * this condition to assess the learner. E.g. Radio, Spot Report, Start Pace Count buttons on the TUI.
     *
     * @return can be null or empty if the condition doesn't need learner actions for assessment.
     */
    public abstract Set<generated.dkf.LearnerActionEnumType> getLearnerActionsNeeded();

    /**
     * Return the unique set of overall assessment types this condition uses to help
     * populate a structured review (AAR).
     *
     * @return the set of overall assessment types the condition can populate it its life cycle.
     * Entries include: {@link generated.dkf.Count}, {@link generated.dkf.ViolationTime},
     * {@link generated.dkf.CompletionTime}.  May return null or empty set.
     */
    public abstract Set<Class<?>> getOverallAssessmenTypes();

    /**
     * Reset the assessment of this condition to the default assessment value if provided.  This will
     * cause the parent concept to be notified of the assessment value change event, therefore the value
     * will not change if the default value is the same as the current assessment value.
     */
    protected void resetAssessmentEvent(){

        //the existing assessment must be different than the default to fire a new assessment event
        if(defaultAssessment != null && getAssessment() != defaultAssessment){

            if(logger.isInfoEnabled()){
                logger.info("Resetting assessment from "+getAssessment()+" to the condition's default assessment of "+defaultAssessment+".");
            }
            assessmentExplanation = null;  // clear the explanation since the assessment is being changed to the default with no specific reason why
                                           // this tends to be called based on a timer being fired (e.g. RulesOfEngagementCondition)
            updateAssessment(defaultAssessment);
            if(conditionActionInterface != null){
                conditionActionInterface.conditionAssessmentCreated(this);
            }
        }
    }

    /**
     * Return the collection of team members that are being assessed on this condition and are currently violating
     * this condition.
     *
     * @return a new Set with the team members from the team organization that are being assessed and violating this condition.  Can be empty
     * but not null if there are no violators right now.
     * Note: null is a valid entry in the set and represents the learner when there are no other learners.
     */
    protected Set<TeamMember<?>> buildViolatorsInfo(){

        Set<TeamMember<?>> violators = new HashSet<>();
        synchronized (violatorTimeMap) {
            for(EntityIdentifier entityId : violatorTimeMap.keySet()){
                TeamMember<?> violator = isConditionAssessedTeamMember(entityId);
                violators.add(violator);
            }
        }

        return violators;
    }

    /**
     * Add this entity to the collection of violators for this condition.  If the entity
     * is already in the violators collection it will be updated with the current system time
     * as a refresh event.
     *
     * @param teamMember the team organization team member that is violating this condition.  Null can be used to represent
     * the learner when dealing with mobile app where there is a single learner and no team organization.
     * @param entityId entity state entity identifier that corresponds to an entity violating this condition.
     * Null can be used to represent the learner when dealing with mobile app where there is a single learner
     * and no team organization.
     */
    protected void addViolator(TeamMember<?> teamMember, EntityIdentifier entityId){
        addViolatorWithDependencies(teamMember, entityId, null);
    }

    /**
     * Adds a violator and also specifies its dependencies.
     *
     * @param teamMember the team organization team member that is violating
     *        this condition. Null can be used to represent the learner when
     *        dealing with mobile app where there is a single learner and no
     *        team organization.
     * @param entityId entity state entity identifier that corresponds to an
     *        entity violating this condition. Null can be used to represent the
     *        learner when dealing with mobile app where there is a single
     *        learner and no team organization.
     * @param dependencies The entities on which this violation depends. If any
     *        of the entities cease to exist, this violation will cease to
     *        exist. Null is treated as an empty collection.
     */
    protected void addViolatorWithDependencies(TeamMember<?> teamMember, EntityIdentifier entityId, Collection<EntityIdentifier> dependencies) {

        synchronized (violatorTimeMap) {
            violatorTimeMap.put(entityId, System.currentTimeMillis());

            /* Update the dependency lookup */
            if (dependencies != null) {
                synchronized (entityToDependentViolators) {
                    for (EntityIdentifier dependency : dependencies) {
                        final Collection<EntityIdentifier> dependentViolators = entityToDependentViolators
                                .computeIfAbsent(dependency, key -> ConcurrentHashMap.newKeySet());
                        dependentViolators.add(entityId);
                    }
                }
            }
        }

        if(staleViolatorsTimeTask == null){
            //start the timer task because it hasn't been started before now for this condition

            if(staleViolatorsTimer == null){
                staleViolatorsTimer = new SchedulableTimer(STALE_VIOLATORS_TIMER_NAME);
            }

            staleViolatorsTimeTask = new StaleViolatorsTimeTask();
            staleViolatorsTimer.scheduleTask(staleViolatorsTimeTask, STALE_VIOLATION_CHECK_DURATION);
        }
    }

    /**
     * The entity is no longer violating the condition.
     *
     * @param entityId the entity to remove from the violators collection.
     * Null can be used to represent the learner when dealing with mobile app where there is a single learner
     * and no team organization.
     * @return true if the entity was in the violation collection
     */
    protected boolean removeViolator(EntityIdentifier entityId){

        synchronized (violatorTimeMap) {
            return violatorTimeMap.remove(entityId) != null;
        }
    }

    /**
     * Remove all violators being tracked by this condition.  This is useful for when the condition
     * has been completed (e.g. all targets have been eliminated) and the act of completing causes the
     * assessment to change.  It is that assessment change which should result in a change in assessment explanation
     * and that explanation should be based on the current violators, which can't exist if the condition has
     * been completed.  After all this is real time assessment information (currently) and not over all assessment
     * information.
     */
    protected void removeAllViolators(){
        synchronized (violatorTimeMap) {
            violatorTimeMap.clear();
        }
    }

    /**
     * Check the violators for stale violations based on when the last violation
     * by that entity took place.
     */
    private void checkStaleViolators() {

        synchronized (violatorTimeMap) {
            final long now = System.currentTimeMillis();
            Set<EntityIdentifier> removedViolators = null;

            /* Remove all the violators who are themselves stale. */
            Iterator<EntityIdentifier> violatorIdIter = violatorTimeMap.keySet().iterator();
            while (violatorIdIter.hasNext()) {
                final EntityIdentifier violatorId = violatorIdIter.next();

                long violationTime = violatorTimeMap.get(violatorId);
                if (now - violationTime > STALE_VIOLATION_DURATION) {
                    if (removedViolators == null) {
                        removedViolators = new HashSet<>();
                    }

                    removedViolators.add(violatorId);

                    /* clean up the metadata for entities that are no longer
                     * violators */
                    entityMetadataLookup.remove(violatorId);

                    /* Removes the stale entity from the violator map */
                    violatorIdIter.remove();
                }
            }

            /* Determine if the dependencies of any violators have gone stale */
            synchronized (entityToDependentViolators) {
                final Iterator<Entry<EntityIdentifier, Set<EntityIdentifier>>> dependenciesIter = entityToDependentViolators
                        .entrySet().iterator();

                synchronized (entityMetadataLookup) {
                    Set<EntityIdentifier> candidatesForRemoval = null;
                    Set<EntityIdentifier> persistentViolators = null;
                    while (dependenciesIter.hasNext()) {
                        final Entry<EntityIdentifier, Set<EntityIdentifier>> dependenciesEntry = dependenciesIter
                                .next();
                        final EntityIdentifier dependencyId = dependenciesEntry.getKey();

                        final AssessedEntityMetadata dependencyMetadata = entityMetadataLookup.get(dependencyId);

                        /* Determine if the current dependency has already been
                         * removed or is stale itself. Either case indicates
                         * that it no longer exists within the scenario. */
                        final boolean alreadyStale = removedViolators != null
                                && removedViolators.contains(dependencyId);
                        final boolean isStale = alreadyStale
                                || now - dependencyMetadata.getLastTimeStamp() > STALE_VIOLATION_DURATION;

                        final Set<EntityIdentifier> dependentViolators = dependenciesEntry.getValue();
                        if (isStale) {
                            if (logger.isInfoEnabled()) {
                                logger.info("The entity " + dependencyId + " has been determined to be stale.");
                            }

                            /* Since this dependency is stale, all violations
                             * that were dependent on this entity should be
                             * removed as well. */
                            if (removedViolators == null) {
                                removedViolators = new HashSet<>();
                            }

                            /* Create the candidates for removal set if it has
                             * not already been created. */
                            if (candidatesForRemoval == null) {
                                candidatesForRemoval = new HashSet<>();
                            }

                            /* Add each violator who is dependent on the
                             * currently identified stale entity to the set of
                             * candidates for removal. */
                            for (EntityIdentifier violatorId : dependentViolators) {
                                candidatesForRemoval.add(violatorId);
                            }

                            /* Since this dependency no longer exists, remove
                             * its entry from the dependency map and remove it
                             * from the entityMetadataLookup map as well. */
                            dependenciesIter.remove();

                            /* Since we have determined that this entity is
                             * stale, remove its metadata */
                            entityMetadataLookup.remove(dependencyId);
                        } else {
                            if (persistentViolators == null) {
                                persistentViolators = new HashSet<>();
                            }

                            /* Since this dependency has been identified as not
                             * stale, all violations which depend on it are
                             * guaranteed to not be removed. */
                            for (EntityIdentifier violatorId : dependentViolators) {
                                persistentViolators.add(violatorId);
                            }
                        }
                    }

                    /* Determine which of the dependent violators will actually
                     * be removed. */
                    if (candidatesForRemoval != null) {
                        /* Don't remove any violators which have been determined
                         * to be persisted. */
                        if (persistentViolators != null) {
                            candidatesForRemoval.removeAll(persistentViolators);
                        }

                        /* If there are still any candidates for removal, remove
                         * them from the violators map and add them to the set
                         * of removed violators. */
                        if (CollectionUtils.isNotEmpty(candidatesForRemoval)) {
                            if (removedViolators == null) {
                                removedViolators = new HashSet<>();
                            }

                            for (EntityIdentifier violatorId : candidatesForRemoval) {
                                if (violatorTimeMap.remove(violatorId) != null) {
                                    removedViolators.add(violatorId);
                                }
                            }
                        }
                    }

                    /* Ensure that all violators that are being removed are not
                     * still in the violation dependency map. */
                    if (removedViolators != null) {
                        for (Collection<EntityIdentifier> dependentViolators : entityToDependentViolators.values()) {
                            dependentViolators.removeAll(removedViolators);
                        }
                    }
                }
            }
            
            // convert to team members
            Set<TeamMember<?>> removedMemberViolators = null;
            if(removedViolators != null){
                removedMemberViolators = new HashSet<>();
                for(EntityIdentifier eId : removedViolators){
                    TeamMember<?> teamMember = isConditionAssessedTeamMember(eId);
                    if(teamMember != null){
                        removedMemberViolators.add(teamMember);
                    }
                }
            }

            // notify the condition
            violatorUpdated(removedMemberViolators);
        }

        // reschedule the check
        staleViolatorsTimeTask = new StaleViolatorsTimeTask();

        if (staleViolatorsTimer == null) {
            staleViolatorsTimer = new SchedulableTimer(STALE_VIOLATORS_TIMER_NAME);
        }

        staleViolatorsTimer.scheduleTask(staleViolatorsTimeTask, STALE_VIOLATION_CHECK_DURATION);
    }

    /**
     * Return how many violators are known to this condition right now.
     *
     * @return the number of current violators to this condition that haven't become stale yet.
     */
    protected int getViolatorSize(){
        return violatorTimeMap.size();
    }

    /**
     * This method should be over-ridden by condition classes that add/remove violators and
     * are interested if the violator event becomes stale for any of the violator entities.
     *
     * @param removedViolators collection of entity's that were stale and therefore removed from the collection
     * of existing violators.  The list will be null if no violators were removed.
     */
    protected void violatorUpdated(Set<TeamMember<?>> removedViolators) {
        // nothing to do by default
    }

    /**
     * Return an assessment explanation provided by the assessment logic in GIFT.  E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition}
     * might provide an explanation of "Vehicle 1 went 37 mph during a 35 mph speed limit".
     *
     * @return an explanation of the assessment attribute value in this object.  Can be null.
     */
    public String getAssessmentExplanation(){
        return assessmentExplanation;
    }

    /**
     * Return the collection of team organization members that are being assessed by this condition (i.e.
     * a subset of teamMemberRefs) and are causing the current assessment level for this condition.
     *
     * @return if null than there are no team members defined, no team members are causing the current assessment
     * level or the condition class has not been developed to keep track of unique violators.
     */
    public Set<String> getViolatorTeamOrgEntries(){

        Set<TeamMember<?>> violators = buildViolatorsInfo();
        if(CollectionUtils.isNotEmpty(violators)){

            Set<String> teamOrgEntries = new HashSet<>();
            for(TeamMember<?> violator : violators){

                String name;
                if(violator == null){
                    // null means the single learner in a knowledge session
                    name = LEARNER;
                }else{
                    name = StringUtils.isNotBlank(violator.getName()) ? violator.getName() : LEARNER;
                }

                teamOrgEntries.add(name);
            }
            return teamOrgEntries;
        }

        return null;
    }
    
    /**
     * Gets the arguments to be passed into the algorithm that handles this condition's performance metrics
     * 
     * @return the performance metric algorithm arguments. Can be null.
     */
    public PerformanceMetricArguments getPerformanceArguments() {
        return performanceArguments;
    }

    /**
     * Sets the arguments to be passed into the algorithm that handles this condition's performance metrics
     * 
     * @param performanceArguments the performance metric algorithm arguments. Can be null.
     */
    public void setPerformanceArguments(PerformanceMetricArguments performanceArguments) {
        this.performanceArguments = performanceArguments;
    }

    @Override
    public String toString(){

        StringBuffer sb = new StringBuffer();
        sb.append("assessment = ").append(currentAssessment.toString());
        sb.append(", assessmentExplanation = ").append(getAssessmentExplanation());
        sb.append(", confidence = ").append(getConfidence());
        sb.append(", competence = ").append(getCompetence());
        sb.append(", trend = ").append(getTrend());
        sb.append(", priority = ").append(getPriority());

        if(initializedAtTime != null){
            sb.append(", initialized at = ").append(fdf.format(initializedAtTime));
        }

        if(completedAtTime != null){
            sb.append(", completed At time = ").append(fdf.format(completedAtTime));
        }

        if(teamMembersBeingAssessed != null){
            sb.append(", teamMemberRefs = {");
            for(String ref : teamMembersBeingAssessed.getTeamMemberRef()){
                sb.append("\n").append(ref).append(",");
            }
            sb.append("}");
        }

        if(!violatorTimeMap.isEmpty()){
            sb.append(", violators = {");
            for(EntityIdentifier eId : violatorTimeMap.keySet()){
                sb.append("\n").append(eId).append(",");
            }
            sb.append("}");
        }

        if(!realTimeScorers.isEmpty()){
            sb.append(", real time assessment = {");
            for(AbstractScorer scorer : realTimeScorers.values()){
                sb.append("\n").append(scorer).append(",");
            }
            sb.append("}");
        }

        sb.append(", scorers = {");
        for(Map<TeamMember<?>, AbstractScorer> teamMembersScorer : scorers.values()){
            sb.append("\n");
            for(TeamMember<?> teamMember : teamMembersScorer.keySet()){
                
                if(teamMember != null){
                    sb.append(teamMember.getName()).append(":");
                }
                
                sb.append(teamMembersScorer.get(teamMember)).append(",");

            }
        }
        sb.append("}");

        return sb.toString();
    }   

    /**
     * This class is responsible for scheduling a timer task.
     *
     * @author mhoffman
     *
     */
    protected class SchedulableTimer extends Timer {

        /**
         * Constructor
         * @param name the name of the timer thread
         */
        public SchedulableTimer(String name){
            super(name);
        }

        public void scheduleTask(TimerTask timerTask, long delay) {
            schedule(timerTask, delay);

            if(logger.isInfoEnabled()){
                logger.info("Scheduled reset timer for "+delay+" ms from now.");
            }
        }

      }

    /**
     * This class is the timer task which runs at the appropriately schedule date.  It will
     * cause a new assessment of this condition to be created.
     *
     * @author mhoffman
     *
     */
    private class ResetTimerTask extends TimerTask{

        @Override
        public void run() {

            if(logger.isInfoEnabled()){
                logger.info("Assessment reset timer fired.");
            }
            resetAssessmentEvent();
        }

        @Override
        public String toString(){
            return "ResetTimerTask";
        }
    }

    /**
     * This class is the timer task which runs at the appropriately scheduled date.  It will
     * cause a check for stale violators in the violators collection.
     *
     * @author mhoffman
     *
     */
    private class StaleViolatorsTimeTask extends TimerTask{

        @Override
        public void run() {

            if (logger.isInfoEnabled()) {
                logger.info("Stale violators timer fired.");
            }

            try {
                checkStaleViolators();
            } catch (Throwable t) {
                logger.error("There was a problem while for checking for stale violators", t);
            }
        }

        @Override
        public String toString(){
            return "StaleViolatorsTimeTask";
        }
    }
    

    /**
     * Used to track per team member information for assessment purposes.
     *
     * @author mhoffman
     *
     */
     class AssessmentWrapper{

        /** last entity state update received for a team member */
        private EntityState entityState;

        /** epoch time for when the last assessment value was set */
        private Long lastAssessmentUpdate;

        /** the last assessment value calculated for this team member */
        private AssessmentLevelEnum assessment;
        
        /** the previous to last assessment value calculated for this team member */
        private AssessmentLevelEnum lastAssessment;

        /** the team member being assessed */
        private TeamMember<?> teamMember;

        /** epoch time for when the last assessment check was performed */
        private Long lastAssessmentCheck;

        /**
         * Set attribute
         *
         * @param teamMember the team member being assessed.  Can't be null.
         */
        public AssessmentWrapper(TeamMember<?> teamMember){

            if(teamMember == null){
                throw new IllegalArgumentException("The team member is null.");
            }

            this.teamMember = teamMember;
        }

        /**
         * Return the team member being assessed
         * @return won't be null.
         */
        public TeamMember<?> getTeamMember(){
            return teamMember;
        }

        /**
         * Set the time at which the last assessment check was performed for this team member
         * to the current epoch time.
         */
        public void updateLastAssessmentCheck(){
            lastAssessmentCheck = System.currentTimeMillis();
        }

        /**
         * Return the epoch time for when the last assessment check was performed for this team member
         * @return can be null if {@link #updateLastAssessmentCheck()} was never called.
         */
        public Long getLastAssessmentCheck(){
            return lastAssessmentCheck;
        }

        /**
         * Set the latest entity state for this team member.
         *
         * @param entityState can't be null
         */
        public void setEntityState(EntityState entityState){
            if(entityState == null){
                return;
            }

            this.entityState = entityState;
        }

        /**
         * Set the current assessment for this team member for this condition.
         *
         * @param assessment can't be null.
         */
        public void updateAssessment(AssessmentLevelEnum assessment){
            if(assessment == null){
                return;
            }

            this.lastAssessment = this.assessment;
            this.assessment = assessment;
            this.lastAssessmentUpdate = System.currentTimeMillis();
        }

        /**
         * Return the current assessment for this team member for this condition.
         *
         * @return will be null if {@link #updateAssessment(AssessmentLevelEnum)} has not been called.
         */
        public AssessmentLevelEnum getAssessment(){
            return assessment;
        }
        
        /**
         * Return the last assessment for this team member for this condition.
         * 
         * @return will be null if {@link #updateAssessment(AssessmentLevelEnum)} has not been called TWICE.
         */
        public AssessmentLevelEnum getLastAssessment(){
            return lastAssessment;
        }

        /**
         * Return the epoch time at which the assessment for this team member for this condition
         * was last set.
         * @return will be null if {@link #updateAssessment(AssessmentLevelEnum)} has not been called.
         */
        public Long getLastAssessmentTime(){
            return lastAssessmentUpdate;
        }

        /**
         * Return the last entity state received for this team member.
         * @return will be null if {@link #setEntityState(EntityState)} was not called.
         */
        public EntityState getEntityState(){
            return entityState;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("[AssessmentWrapper: ");
            builder.append("lastAssessmentUpdate = ");
            builder.append(getLastAssessmentTime());
            builder.append(", assessment = ");
            builder.append(getAssessment());
            builder.append(", teamMember = ");
            builder.append(getTeamMember());
            builder.append(", lastAssessmentCheck = ");
            builder.append(getLastAssessmentCheck());
            builder.append(", entityState = ");
            builder.append(getEntityState());
            builder.append("]");
            return builder.toString();
        }

    }

    /**
     * Represents an aggregation of the data needed for each assessed team
     * member in order to properly assess this condition. There should only be
     * one instance of this class per entity in the scenario. This class should
     * only be created for entities whose state is represented by an
     * {@link EntityState} payload and for conditions which require coordinates
     * expressed in both {@link GCC} and {@link GDC} systems.
     *
     * @author tflowers
     *
     */
    protected class AssessedEntityMetadata {

        /** The last known GCC location of the assessed entity. */
        private final Point3d lastGccLocation = new Point3d();

        /** The last known GDC location of the assessed entity. */
        private final GDC lastGdcLocation = new GDC();
        
        /** the last known orientation of the assessed entity. */
        private final Vector3d lastOrientationEulerAngles = new Vector3d();

        /** The epoch time for when an entity state was last received. */
        private long lastTimeStamp = 0;
        
        /** whether the entity is healthy */
        private boolean isHealthy = true;
        
        /** the entity's last damage value */
        private DamageEnum damage;

        /**
         * Updates the internal metadata based on a provided
         * {@link EntityState}.
         *
         * @param es The {@link EntityState} from which to extract metadata.
         *        Can't be null.
         */
        public void updateMetadata(EntityState es) {
            this.lastTimeStamp = System.currentTimeMillis();
            lastGccLocation.set(es.getLocation());
            CoordinateUtil.getInstance().convertFromGCCToGDC(es.getLocation(), lastGdcLocation);
            lastOrientationEulerAngles.set(es.getOrientation());
            damage = es.getAppearance().getDamage();
            isHealthy = es.getAppearance().getDamage() == DamageEnum.HEALTHY;
        }
        
        /**
         * Return the damage value for this entity.
         * 
         * @return won't be null.
         */
        public DamageEnum getDamage(){
            return damage;
        }
        
        /**
         * Return true if the entity is healthy (i.e. not damaged)
         * @return true if the entity is healthy, false if it has any damage.
         */
        public boolean isHealthy(){
            return isHealthy;
        }
        
        /**
         * Return the the last known orientation of the assessed entity.
         * @return the value of {@link #lastOrientationEulerAngles}
         */
        public Vector3d getLastOrientation(){
            return lastOrientationEulerAngles;
        }

        /**
         * Getter for the lastGccLocation.
         *
         * @return The value of {@link #lastGccLocation}.
         */
        public Point3d getLastGccLocation() {
            return lastGccLocation;
        }

        /**
         * Getter for the lastGdcLocation.
         *
         * @return The value of {@link #lastGdcLocation}.
         */
        public GDC getLastGdcLocation() {
            return lastGdcLocation;
        }

        /**
         * Getter for the lastTimeStamp.
         *
         * @return The value of {@link #lastTimeStamp}.
         */
        public long getLastTimeStamp() {
            return lastTimeStamp;
        }

        @Override
        public String toString() {
            return new StringBuilder("[AssessedEntityMetadata: ")
                    .append("lastGccLocation = ").append(lastGccLocation)
                    .append(", lastGdcLocation = ").append(lastGdcLocation)
                    .append(", lastOrientationEuler = ").append(lastOrientationEulerAngles)
                    .append(", lastTimeStamp = ").append(lastTimeStamp)
                    .append(']').toString();
        }
    }
}
