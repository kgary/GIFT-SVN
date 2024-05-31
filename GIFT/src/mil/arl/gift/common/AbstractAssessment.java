/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * This is the base class for performance assessment classes
 * 
 * @author mhoffman
 *
 */
public class AbstractAssessment {

    /** name of the item (e.g. concept) being assessed */
    private String name;
    
    /** assessment of the item */
    protected AssessmentLevelEnum assessment;    
    
    /**
     * An, optional, assessment explanation provided by the assessment logic in GIFT.  E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition} 
     * could provide an explanation of "Vehicle 1 went 37 mph during a 35 mph speed limit". 
     */
    private Set<String> assessmentExplanations;
    
    /** 
     * whether to hold the assessment value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean assessmentHold = false;
    
    /** the state of the task/concept */
    protected PerformanceNodeStateEnum nodeStateEnum = PerformanceNodeStateEnum.UNACTIVATED;
    
    /** time stamp (epoch) at which the assessment was last calculated */
    protected long time;
    
    /** 
     * unique performance node id that maps to this assessment  
     * Note: this id is only unique to a single concept hierarchy in a course (e.g. from a dkf), not across all concept hierarchies in a course.
     */
    private int nodeId;
    
    /**
     * a course level unique id for the performance node that maps to this assessment
     * Note: this id is unique across the course
     */
    private UUID courseUUID;
    
    /**
     * Defines the importance of the performance assessment node compared to the other nodes.
     * The value may change during the execution of the course and can be used to help filter instructional strategy choices.
     */
    private Integer priority;
    
    /**
     * whether to hold the priority value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean priorityHold = false;
    
    /** The user updating the assessment */
    private String evaluator;

    /** 
     * An, optional, observer controller (OC) comment.  This can originate from the GIFT Game master UI.
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.
     */
    private String observerComment;
    
    /**
     * An optional reference to a media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * attached to this assessment by an observer controller (OC). This can originate from the GIFT Game master UI.
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.
     */
    private String observerMedia;
    
    /** the optional reference to a resource in an external authoritative system associated with the assessment */
    private String authoritativeResource;

    /**
     * Flag indicating if the assessment contains a child or descendent that
     * requires manual observation
     */
    private boolean containsObservedAssessmentCondition = false;

    /** The default confidence value */
    public static final float DEFAULT_CONFIDENCE = 1.0f;
    /** Minimum confidence level */
    public static final float MIN_CONFIDENCE = 0.0f;
    /** Maximum confidence level */
    public static final float MAX_CONFIDENCE = 1.0f;
    /** How confident is the assessment of the learner on this concept */
    private float confidence = DEFAULT_CONFIDENCE;

    /**
     * whether to hold the confidence value (i.e. GIFT can't automatically
     * change it, requires an external change like the game master)
     */
    private boolean confidenceHold = false;

    /** The default competence value */
    public static final float DEFAULT_COMPETENCE = 1.0f;
    /** Minimum competence level */
    public static final float MIN_COMPETENCE = 0.0f;
    /** Maximum competence level */
    public static final float MAX_COMPETENCE = 1.0f;
    /** How competent is the learner on this concept */
    private float competence = DEFAULT_COMPETENCE;

    /**
     * whether to hold the competence value (i.e. GIFT can't automatically
     * change it, requires an external change like the game master)
     */
    private boolean competenceHold = false;

    /** The default trend value */
    public static final float DEFAULT_TREND = 1.0f;
    /** Minimum trend level */
    public static final float MIN_TREND = -1.0f;
    /** Maximum trend level */
    public static final float MAX_TREND = 1.0f;
    /** The assessment trend of the learner on this concept */
    private float trend = DEFAULT_TREND;

    /**
     * whether to hold the trend value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean trendHold = false;

    /**
     * The unique team organization names of those learners whose assessment is
     * represented in this object.
     */
    private Map<String, AssessmentLevelEnum> assessedTeamOrgEntities = new HashMap<>();
    
    /**
     * flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view. 
     */
    private boolean scenarioSupportNode = false;
    
    /**
     * Used to enumerate the different states of tasks/concepts
     * 
     * @author mhoffman
     *
     */
    public enum PerformanceNodeStateEnum{

        /** The task/concept was never active */
        UNACTIVATED("UNACTIVATED", "unactivated"),
        /** The task/concept is currently being assessed */
        ACTIVE("ACTIVE", "active"),
        /**
         * The concept's conditions never finished but the concept is NOT
         * currently being assessed because the ancestor task is no longer
         * active
         */
        DEACTIVATED("DEACTIVATED", "deactivated"),
        /**
         * The task/concept gracefully finished and is NOT currently being
         * assessed
         */
        FINISHED("FINISHED", "finished");
        
        /** the unique name value for the enum */
        private String name;
        
        /** the display string for the enum */
        private String displayName;
        
        /**
         * Set attributes.
         * 
         * @param name the unique name value for the enum. Can't be null or empty.
         * @param displayName the display string for the enum.  Can't be null or empty.
         */
        private PerformanceNodeStateEnum(String name, String displayName){
            setName(name);
            setDisplayName(displayName);
        }

        /**
         * the unique name value for the enum.
         * 
         * @return won't be null or empty.
         */
        public String getName() {
            return name;
        }

        /**
         * Set the unique name value for the enum
         * 
         * @param name  can't be null or empty.
         */
        private void setName(String name) {
            
            if(StringUtils.isBlank(name)){
                throw new IllegalArgumentException("The name can't be null or empty.");
            }
            
            this.name = name;
        }

        /**
         * the display string for the enum.
         * 
         * @return won't be null or empty.
         */
        public String getDisplayName() {
            return displayName;
        }

        /**
         * Set the display string for the enum.
         * 
         * @param displayName can't be null or empty.
         */
        private void setDisplayName(String displayName) {
            
            if(StringUtils.isBlank(displayName)){
                throw new IllegalArgumentException("The display name can't be null or empty.");
            }
            
            this.displayName = displayName;        
        }
    }
    
    /**
     * Class constructor - set attributes with unique id already generated
     * 
     * @param name - name of the item being assessed
     * @param assessment - assessment of the item
     * @param time - time stamp at which the assessment was last calculated.  Must be greater than 0.
     * @param id - dkf level unique performance node id that maps to this assessment 
     * @param courseUUID - course level unique id for this assessment node
     */
    public AbstractAssessment(String name, AssessmentLevelEnum assessment, long time, int id, UUID courseUUID){
        
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The name can't be empty.");
        }
        
        if(assessment == null){
            throw new IllegalArgumentException("The assessment can't be null.");
        }
        
        if(id < 1){
            throw new IllegalArgumentException("The id must be a positive value.");
        }
        
        if(courseUUID == null){
            throw new IllegalArgumentException("The course UUID can't be null.");
        }
        
        this.name = name;
        this.assessment = assessment;
        setTime(time);
        this.nodeId = id;  
        this.courseUUID = courseUUID;
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
     * Get the state of the performance node (task/concept).
     * 
     * @return won't be null.  The default is {@link PerformanceNodeStateEnum#UNACTIVATED}
     */
    public PerformanceNodeStateEnum getNodeStateEnum() {
        return nodeStateEnum;
    }

    /**
     * Set the state of the performance node (task/concept)
     * 
     * @param nodeStateEnum can't be null.
     */
    public void setNodeStateEnum(PerformanceNodeStateEnum nodeStateEnum) {
        
        if(nodeStateEnum == null){
            throw new IllegalArgumentException("The new node state can't be null.");
        }
        
        this.nodeStateEnum = nodeStateEnum;
    }

    /**
     * Return the name of what's being assessed
     * 
     * @return the name of the performance assessment node.  Won't be null or empty.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the assessment
     * 
     * @return the performance assessment node assessment value.  Won't be null.
     */
    public AssessmentLevelEnum getAssessmentLevel() {
        return assessment;
    }
    
    /**
     * Update the assessment level
     * 
     * @param assessment - a new assessment level.  Can't be null.
     */
    public void updateAssessment(AssessmentLevelEnum assessment){
        updateAssessment(assessment, false);
    }

    /**
     * Update the assessment level
     * 
     * @param assessment - a new assessment level. Can't be null.
     * @param ignoreHold - true to update the assessment regardless of the
     *        current hold state; false to respect the current hold state.
     */
    public void updateAssessment(AssessmentLevelEnum assessment, boolean ignoreHold){
        
        if(ignoreHold || !assessmentHold){
            if(assessment == null){
                throw new IllegalArgumentException("The assessment can't be null.");
            }
            
            this.assessment = assessment;
            setTime(System.currentTimeMillis());
        }
    }

    /**
     * Update the priority value.
     * 
     * @param priority the priority value. Can be null. Value can not be less
     *        than 1.
     */
    public void updatePriority(Integer priority) {
        updatePriority(priority, false);
    }

    /**
     * Update the priority value.
     * 
     * @param priority the priority value. Can be null. Value can not be less
     *        than 1.
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void updatePriority(Integer priority, boolean ignoreHold) {

        if (ignoreHold || !priorityHold) {
            if (priority != null && priority < 1) {
                throw new IllegalArgumentException("The priority must be greater than zero.");
            }

            this.priority = priority;
        }
    }

    /**
     * Set the time at which the assessment was set.
     * @param time the time in milliseconds since epoch, must be greater than 0.
     */
    private void setTime(long time){
        if(time < 1){
            throw new IllegalArgumentException("The time must be a postive value.");
        }
        this.time = time;
    }

    /**
     * Return the time at which the assessment was set
     * 
     * @return the time in milliseconds since epoch, will be greater than 0.
     */
    public long getTime() {
        return time;
    }

    /**
     * Set the time stamp (epoch) at which the observer started to give an observation (e.g. comment, assessment).<br/>
     * This will over-ride the assessment time value.
     * @param observationStartedTime the time at which some observation that is included in this assessment was started
     * by the observer. must be greater than 0.
     */
    public void setObservationStartedTime(long observationStartedTime) {
        setTime(observationStartedTime);
    }

    /**
     * Return the unique performance node id (w/in a concept hierarchy) that maps to this assessment  
     * Note: this id is only unique to a single concept hierarchy in a course, not across all concept hierarchies in a course.
     * 
     * @return the unique node id from the dkf
     */
    public int getNodeId(){
        return nodeId;
    }
    
    /**
     * Return the unique performance node id across an entire course that maps to this assessment.
     * 
     * @return the unique node id for a domain module instance
     */
    public UUID getCourseNodeId(){
        return courseUUID;
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
     * @param confidence the value must be between {@link #MIN_CONFIDENCE} and
     *        {@link #MAX_CONFIDENCE} (inclusive)
     */
    public void updateConfidence(float confidence) {
        updateConfidence(confidence, false);
    }

    /**
     * Set how confident is the assessment of the learner on this concept
     * 
     * @param confidence the value must be between {@link #MIN_CONFIDENCE} and {@link #MAX_CONFIDENCE} (inclusive)
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void updateConfidence(float confidence, boolean ignoreHold) {  
        
        if(ignoreHold || !confidenceHold){
            if(confidence < MIN_CONFIDENCE || confidence > MAX_CONFIDENCE){
                throw new IllegalArgumentException("The confidence value of "+confidence+" is not between "+MIN_CONFIDENCE+" and "+MAX_CONFIDENCE+" (inclusive)");
            }
            
            this.confidence = confidence;
        }
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
     * @param competence the value must be between {@link #MIN_COMPETENCE} and
     *        {@link #MAX_COMPETENCE} (inclusive)
     */
    public void updateCompetence(float competence) {
        updateCompetence(competence, false);
    }

    /**
     * Set how competent is the learner on this concept
     * 
     * @param competence the value must be between {@link #MIN_COMPETENCE} and
     *        {@link #MAX_COMPETENCE} (inclusive)
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void updateCompetence(float competence, boolean ignoreHold) {

        if (ignoreHold || !competenceHold) {
            if (competence < MIN_COMPETENCE || competence > MAX_COMPETENCE) {
                throw new IllegalArgumentException("The competence value of " + competence + " is not between "
                        + MIN_COMPETENCE + " and " + MAX_COMPETENCE + " (inclusive)");
            }

            this.competence = competence;
        }
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
     * Set the assessment trend of the learner on this concept
     * 
     * @param trend the value must be between {@link #MIN_TREND} and
     *        {@link #MAX_TREND} (inclusive)
     */
    public void updateTrend(float trend) {
        updateTrend(trend, false);
    }

    /**
     * Set the assessment trend of the learner on this concept
     * 
     * @param trend the value must be between {@link #MIN_TREND} and
     *        {@link #MAX_TREND} (inclusive)
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void updateTrend(float trend, boolean ignoreHold) {

        if (ignoreHold || !trendHold) {

            if (trend < MIN_TREND || trend > MAX_TREND) {
                throw new IllegalArgumentException("The trend value of " + trend + " is not between " + MIN_TREND
                        + " and " + MAX_TREND + " (inclusive)");
            }

            this.trend = trend;
        }
    }

    /**
     * Retrieve the name of the user sending the assessment
     * 
     * @return the name of the user sending the assessment. Can be null if the username was never
     *         set and GIFT created the assessment automatically.
     */
    public String getEvaluator() {
        return evaluator;
    }

    /**
     * Set the name of the user sending the assessment
     * 
     * @param evaluator the name of the user sending the assessment.  Use null to indicate GIFT 
     * created the assessment automatically.
     */
    public void setEvaluator(String evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Retrieve the flag indicating if the assessment contains a child or descendent that requires
     * manual observation
     * 
     * @return true if the assessment contains such a child; false otherwise.
     */
    public boolean isContainsObservedAssessmentCondition() {
        return containsObservedAssessmentCondition;
    }

    /**
     * Set the flag indicating if the assessment contains a child or descendent that requires manual
     * observation
     * 
     * @param containsObservedAssessmentCondition true if the assessment contains such a child;
     *        false otherwise.
     */
    public void setContainsObservedAssessmentCondition(boolean containsObservedAssessmentCondition) {
        this.containsObservedAssessmentCondition = containsObservedAssessmentCondition;
    }    

    
    /**
     * Return whether to hold the trend value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @return true if the assessment is being held
     */
    public boolean isAssessmentHold() {
        return assessmentHold;
    }

    /**
     * Set whether to hold the assessment value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @param assessmentHold whether to hold
     */
    public void setAssessmentHold(boolean assessmentHold) {
        this.assessmentHold = assessmentHold;
    }

    /**
     * Return whether to hold the priority value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @return true if the priority is being held
     */
    public boolean isPriorityHold() {
        return priorityHold;
    }

    /**
     * Set whether to hold the priority value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @param priorityHold whether to hold
     */
    public void setPriorityHold(boolean priorityHold) {
        this.priorityHold = priorityHold;
    }

    /**
     * Return whether to hold the confidence value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @return true if the confidence is being held
     */
    public boolean isConfidenceHold() {
        return confidenceHold;
    }

    /**
     * Set whether to hold the confidence value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @param confidenceHold whether to hold
     */
    public void setConfidenceHold(boolean confidenceHold) {
        this.confidenceHold = confidenceHold;
    }

    /**
     * Return whether to hold the competence value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @return true if the competence is being held
     */
    public boolean isCompetenceHold() {
        return competenceHold;
    }

    /**
     * Set whether to hold the competence value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @param competenceHold whether to hold
     */
    public void setCompetenceHold(boolean competenceHold) {
        this.competenceHold = competenceHold;
    }

    /**
     * Return whether to hold the trend value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @return true if the assessment is being held
     */
    public boolean isTrendHold() {
        return trendHold;
    }

    /**
     * Set whether to hold the trend value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     * @param trendHold whether to hold
     */
    public void setTrendHold(boolean trendHold) {
        this.trendHold = trendHold;
    }

    /**
     * Return the observer controller (OC) comment.  This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide some information about something that was witnessed during the lesson.<br/>
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.<br/>
     * Note: Make sure this is called after any assessment update, which also updates this field.
     * 
     * @return an observer controller (OC) comment. Can be null.
     */
    public String getObserverComment() {
        return observerComment;
    }

    /**
     * Set the observer controller (OC) comment.  This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide some information about something that was witnessed during the lesson.<br/>
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.<br/>
     * Note: Make sure this is called after any assessment update, which also updates this field.
     * 
     * @param observerComment an observer controller (OC) comment. Can be null.
     */
    public void setObserverComment(String observerComment) {
        this.observerComment = observerComment;
    }
    
    /**
     * Gets the reference to a media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * attached to this assessment by an observer controller (OC). This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide non-textual information about something that was witnessed during the lesson.<br/>
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.
     * 
     * @return an observer controller (OC) media file reference. Can be null.
     */
    public String getObserverMedia() {
        return observerMedia;
    }

    /**
     * Sets the reference to a media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * attached to this assessment by an observer controller (OC). This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide non-textual information about something that was witnessed during the lesson.<br/>
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.
     * 
     * @param observerComment an observer controller (OC) media file reference. Can be null.
     */
    public void setObserverMedia(String observerMedia) {
        this.observerMedia = observerMedia;
    }

    /**
     * Return an assessment explanation provided by the assessment logic in GIFT.  E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition} 
     * might provide an explanation of "Vehicle 1 went 37 mph during a 35 mph speed limit". 
     * 
     * @return an explanation of the assessment attribute value in this object.  Can be null.
     */
    public Set<String> getAssessmentExplanation() {
        return assessmentExplanations;
    }

    /**
     * Set an assessment explanation provided by the assessment logic in GIFT.  E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition} 
     * might provide an explanation of "Vehicle 1 went 37 mph during a 35 mph speed limit". 
     * 
     * @param assessmentExplanation an explanation of the assessment attribute value in this object.  Can be null.
     */
    public void setAssessmentExplanation(Set<String> assessmentExplanation) {
        
        if(!assessmentHold){
            this.assessmentExplanations = assessmentExplanation;            
        }
    }
    
    /**
     * Add the assessment explanation to the collection for this assessment instance.
     * 
     * @param assessmentExplanation explains the assessment contained in this object.  If this explanation
     * is null or empty it will not be added.
     */
    public void addAssessmentExplanation(String assessmentExplanation){
        
        if(!assessmentHold){
            
            if(StringUtils.isNotBlank(assessmentExplanation)){
                if(assessmentExplanations == null){
                    this.assessmentExplanations = new HashSet<String>(1);
                }
                this.assessmentExplanations.add(assessmentExplanation);
            }
        }
    }

    /**
     * Add a unique team organization name of a learner whose assessment is
     * represented in this object
     * 
     * @param teamOrgName If null or empty the value will not be added.
     * @param assessment If null, {@link AssessmentLevelEnum#UNKNOWN} will be
     *        used instead.
     */
    public void addAssessedTeamOrgEntry(String teamOrgName, AssessmentLevelEnum assessment) {

        if (StringUtils.isNotBlank(teamOrgName)) {
            assessedTeamOrgEntities.put(teamOrgName, assessment);
        }
    }

    /**
     * Add a collection of unique team organization names of learners who
     * assessment is represented in this object.
     * 
     * @param teamOrgNames if null or empty this method does nothing. If any of
     *        the assessments are null, {@link AssessmentLevelEnum#UNKNOWN} will
     *        be used instead.
     */
    public void addAssessedTeamOrgEntries(Map<String, AssessmentLevelEnum> teamOrgNames) {

        if (CollectionUtils.isNotEmpty(teamOrgNames)) {
            for (Entry<String, AssessmentLevelEnum> entry : teamOrgNames.entrySet()) {
                addAssessedTeamOrgEntry(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Return the collection of unique team organization names of learners whose
     * assessment is represented in this object.
     * 
     * @return the collection of unique team organization names of learners
     *         whose assessment is represented in this object. Can't be null.
     */
    public Map<String, AssessmentLevelEnum> getAssessedTeamOrgEntities() {
        return assessedTeamOrgEntities;
    }

    /**
     * Return the flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view.
     * @return default is false
     */
    public boolean isScenarioSupportNode() {
        return scenarioSupportNode;
    }

    /**
     * Set the flag indication whether this task/concept is for scenario support such as a timer used
     * to fire some strategy.  Ideally these tasks/concepts would not be shown to an instructor/observer-controller
     * view.
     * @param scenarioSupportNode true if this task/concept should be hidden from the OC
     */
    public void setScenarioSupportNode(boolean scenarioSupportNode) {
        this.scenarioSupportNode = scenarioSupportNode;
    }
    
    /**
     * Clears any observer-specific metadata from the assessment and all descendants,
     * namely any comments that were added by an observer.
     *
     * @param assessment the assessment that should have its and its descendants'
     *        observer metadata cleared out.
     */
    public void clearObserverMetadata() {

        /* Clear 'reason' from assessment */
        setObserverComment(null);

        /* Perform clear on children */
        List<ConceptAssessment> childConcepts = null;
        if (this instanceof TaskAssessment) {
            TaskAssessment tAssessment = (TaskAssessment) this;
            childConcepts = tAssessment.getConceptAssessments();
        } else if (this instanceof IntermediateConceptAssessment) {
            IntermediateConceptAssessment icAssessment = (IntermediateConceptAssessment) this;
            childConcepts = icAssessment.getConceptAssessments();
        }

        /* Perform clear on children */
        if (childConcepts != null) {
            for (ConceptAssessment concept : childConcepts) {
                concept.clearObserverMetadata();
            }
        }
    }
    
    /**
     * Gets the optional reference to a resource in an external authoritative system associated with the assessment
     * 
     * @return the authoritative resource reference. Can be null.
     */
    public String getAuthoritativeResource() {
        return authoritativeResource;
    }

    /**
     * Sets the optional reference to a resource in an external authoritative system associated with the assessment
     * 
     * @param authoritativeResource the authoritative resource reference. Can be null.
     */
    public void setAuthoritativeResource(String resourceId) {
        this.authoritativeResource = resourceId;
    }

    @Override
    public boolean equals(Object otherAssessment){        
        return otherAssessment != null && otherAssessment instanceof AbstractAssessment &&
                this.getAssessmentLevel() == ((AbstractAssessment)otherAssessment).getAssessmentLevel() && 
                this.courseUUID.equals(((AbstractAssessment)otherAssessment).getCourseNodeId()) &&
                this.name.equals(((AbstractAssessment)otherAssessment).getName()) &&
                this.getConfidence() == ((AbstractAssessment)otherAssessment).getConfidence() &&
                this.getCompetence() == ((AbstractAssessment)otherAssessment).getCompetence() &&
                this.getTrend() == ((AbstractAssessment)otherAssessment).getTrend() && 
                StringUtils.equalsIgnoreCase(this.getEvaluator(), ((AbstractAssessment)otherAssessment).getEvaluator()) &&
                StringUtils.equalsIgnoreCase(this.getObserverComment(), ((AbstractAssessment)otherAssessment).getObserverComment()) &&
                isContainsObservedAssessmentCondition() == ((AbstractAssessment)otherAssessment).isContainsObservedAssessmentCondition() &&
                CollectionUtils.equals(this.getAssessmentExplanation(), ((AbstractAssessment)otherAssessment).getAssessmentExplanation()) &&
                CollectionUtils.equals(this.getAssessedTeamOrgEntities(), ((AbstractAssessment)otherAssessment).getAssessedTeamOrgEntities());
    }
    
    @Override
    public int hashCode(){
    	
    	// Start with prime number
    	int hash = 349;
    	int mult = 157;

    	if(this != null) {
	    	// Take another prime as multiplier, add members used in equals
	    	
    		hash = mult * hash + this.getAssessmentLevel().hashCode();
	    	hash = mult * hash + this.getCourseNodeId().hashCode();
	    	hash = mult * hash + this.getName().hashCode();
	    	hash = mult * hash + this.getEvaluator().hashCode();
            hash = mult * hash + (containsObservedAssessmentCondition ? 1231 : 1237);
    	}
    	
    	return hash;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("name  = ").append(getName());
        sb.append(", id = ").append(getNodeId());
        sb.append(", course id = ").append(getCourseNodeId().toString());
        sb.append(", scenarioSupport = ").append(isScenarioSupportNode());
        sb.append(", priority = ").append(getPriority());
        sb.append(", assessment = ").append(getAssessmentLevel());

        if (getAssessmentExplanation() != null) {
            sb.append(", assessmentExplanation = ").append(getAssessmentExplanation());
        }

        if (getAssessedTeamOrgEntities() != null) {
            sb.append(", assessedTeamOrgEntities = ").append(getAssessedTeamOrgEntities());
        }

        sb.append(", nodeState = ").append(getNodeStateEnum());
        sb.append(", confidence = ").append(getConfidence());
        sb.append(", competence = ").append(getCompetence());
        sb.append(", trend = ").append(getTrend());
        sb.append(", time = ").append(getTime());
        sb.append(", evaluator = ").append(getEvaluator());
        sb.append(", priorityHold = ").append(isPriorityHold());
        sb.append(", assessmentHold = ").append(isAssessmentHold());
        sb.append(", confidenceHold = ").append(isConfidenceHold());
        sb.append(", competenceHold = ").append(isCompetenceHold());
        sb.append(", trendHold = ").append(isTrendHold());
        sb.append(", observerComment = ").append(getObserverComment());
        sb.append(", observerMedia = ").append(getObserverMedia());
        sb.append(", authoritativeResource = ").append(getAuthoritativeResource());
        sb.append(", contains observed assessment condition = ").append(isContainsObservedAssessmentCondition());
        sb.append("]");
        
        return sb.toString();
    }
}
