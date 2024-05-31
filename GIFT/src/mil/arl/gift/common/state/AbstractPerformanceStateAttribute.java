/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * This class is the base class for the learner state performance state attribute  (e.g. concept "corridor check" assessments).
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractPerformanceStateAttribute implements TemporalStateAttribute {

    /** The user that updated the state attribute */
    private String evaluator;

    /** name of the learner performance state attribute (e.g. "perimeter sweep" for Task "perimeter sweep") */
    private String name;
    
    /** the unique DKF performance node id */
    private int nodeId;
    
    /** the course level unique node id */
    private String nodeCourseId;
    
    /**
     * Defines the importance of the performance assessment node compared to the other nodes.
     * The value may change during the execution of the course and can be used to help filter instructional strategy choices.
     */
    private Integer priority = null;
    
    /**
     * whether to hold the priority value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean priorityHold = false;
    
    /** the state of the task/concept */
    protected PerformanceNodeStateEnum nodeStateEnum = PerformanceNodeStateEnum.UNACTIVATED;
    
    /** how confident is the assessment of the learner on this concept */
    private float confidence;
    
    /**
     * whether to hold the confidence value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean confidenceHold = false;
    
    /** how competent is the learner on this concept */
    private float competence;
    
    /**
     * whether to hold the competence value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean competenceHold = false;
    
    /** the assessment trend of the learner on this concept */
    private float trend;
    
    /**
     * whether to hold the trend value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean trendHold = false;
    
    /** short term assessment level for this performance state */
    private AssessmentLevelEnum shortTerm;
    
    /** the time in milliseconds at which the short term assessment state value was set.  Must be greater than 0. */
    private long shortTermTimestamp;
    
    /** long term assessment level for this performance state */
    private AssessmentLevelEnum longTerm;
    
    /** the time in milliseconds at which the long term assessment state value was set.  Must be greater than 0. */
    private long longTermTimestamp;
    
    /** predicted assessment level for this performance state */
    private AssessmentLevelEnum predicted;
    
    /** the time in milliseconds at which the predicted state value was set.  Must be greater than 0. */
    private long predictedTimestamp;
    
    /** 
     * whether to hold the assessment value 
     * (i.e. GIFT can't automatically change it, requires an external change like the game master) 
     */
    private boolean assessmentHold = false;

    /** 
     * An, optional, observer controller (OC) comment.  This can originate from the GIFT Game master UI.
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.
     */
    private String observerComment;
    
    /**
     * An optional reference to a media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * attached to this performance state by an observer controller (OC). This can originate from the GIFT Game master UI.
     * Note: currently this attribute doesn't persist across subsequent performance assessment messages from the domain module.
     */
    private String observerMedia;
    
    /**
     * An, optional, assessment explanation provided by the assessment logic in GIFT.  E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition} 
     * could provide an explanation of "Vehicle 1 went 37 mph during a 35 mph speed limit". 
     */
    private Set<String> assessmentExplanations;

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

    /** The time the last performance assessment update occurred */
    private long performanceAssessmentTime;

    /**
     * An optional reference to a resource in an external authoritative system associated with the assessment
     */
    private String authoritativeResource;

    /** 
     * Default constructor needed for GWT RPC serialization
     */
    protected AbstractPerformanceStateAttribute() {}
    
    /**
     * Class constructor - set attributes
     * 
     * @param name name of the learner performance state attribute (e.g. "perimeter sweep" for Task "perimeter sweep")
     * @param nodeId unique performance node id (i.e. unique across a DKF)
     * @param nodeCourseId the course level unique node id (i.e. unique across a course)
     * @param shortTerm - the assessment value for short term
     * @param shortTermTimestamp the time in milliseconds at which the short term state value was set.  Must be greater than 0.
     * @param longTerm - the assessment value for long term
     * @param longTermTimestamp the time in milliseconds at which the long term state value was set
     * @param predicted - the assessment value for predicted
     * @param predictedTimestamp the time in milliseconds at which the predicted state value was set
     */
    public AbstractPerformanceStateAttribute(String name, int nodeId, String nodeCourseId,
            AssessmentLevelEnum shortTerm, long shortTermTimestamp,
            AssessmentLevelEnum longTerm, long longTermTimestamp,
            AssessmentLevelEnum predicted, long predictedTimestamp){
        
        this();
        
        if(name == null || name.isEmpty()){
            throw new IllegalArgumentException("The name can't be null or empty.");
        }
        
        if(nodeCourseId == null){
            throw new IllegalArgumentException("The node course id can't be null.");
        }
        
        if(shortTerm == null){
            throw new IllegalArgumentException("The short term assessment can't be null.");
        }
        
        if(longTerm == null){
            throw new IllegalArgumentException("The long term assessment can't be null.");
        }
        
        if(predicted == null){
            throw new IllegalArgumentException("The predicted assessment can't be null.");
        }
        
        if(longTermTimestamp < 1){
            throw new IllegalArgumentException("The long term timestamp value of "+longTermTimestamp+" must be a positive value.");
        }
        
        if(predictedTimestamp < 1){
            throw new IllegalArgumentException("The predicted timestamp value of "+predictedTimestamp+" must be a positive value.");
        }
        
        this.name = name;
        this.nodeId = nodeId;
        this.nodeCourseId = nodeCourseId;
        this.shortTerm = shortTerm;
        this.longTerm = longTerm;
        this.predicted = predicted;
        setShortTermTimestamp(shortTermTimestamp);
        this.longTermTimestamp = longTermTimestamp;
        this.predictedTimestamp = predictedTimestamp;
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param name name of the learner performance state attribute (e.g. "perimeter sweep" for Task "perimeter sweep")
     * @param nodeId unique performance node id
     * @param nodeCourseId the course level unique node id (i.e. unique across a course)
     * @param shortTerm - the assessment value for short term
     * @param longTerm - the assessment value for long term
     * @param predicted - the assessment value for predicted
     */
    public AbstractPerformanceStateAttribute(String name, int nodeId, String nodeCourseId,
            AssessmentLevelEnum shortTerm, AssessmentLevelEnum longTerm, AssessmentLevelEnum predicted){
        this(name, nodeId, nodeCourseId, shortTerm, System.currentTimeMillis(), longTerm, System.currentTimeMillis(), predicted, System.currentTimeMillis());

    }
    
    /**
     * Set the time at which the short term assessment was last changed.
     * 
     * @param time the time in milliseconds at which the short term state value was set.  Must be greater than 0.
     */
    private void setShortTermTimestamp(long time){
        
        if(time < 1){
            throw new IllegalArgumentException("The short term timestamp value of "+time+" must be a positive value.");
        }
        
        this.shortTermTimestamp = time;
    }

    /**
     * Set the priority value for this performance assessment node.
     * 
     * @param priority the priority value. Can be null. Value can not be less
     *        than 1.
     */
    public void setPriority(Integer priority) {
        setPriority(priority, false);
    }

    /**
     * Set the priority value for this performance assessment node.
     * 
     * @param priority the priority value. Can be null. Value can not be less
     *        than 1.
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void setPriority(Integer priority, boolean ignoreHold) {

        if (ignoreHold || !priorityHold) {
            if (priority != null && priority < 1) {
                throw new IllegalArgumentException("The priority must be greater than zero.");
            }

            this.priority = priority;
        }
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
     * Return how confident is the assessment of the learner on this concept
     * 
     * @return the confidence value
     */
    public float getConfidence() {
        return confidence;
    }

    /**
     * Set how confident is the assessment of the learner on this concept
     * 
     * @param confidence the confidence value
     */
    public void setConfidence(float confidence) {
        setConfidence(confidence, false);
    }

    /**
     * Set how confident is the assessment of the learner on this concept
     * 
     * @param confidence the confidence value
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void setConfidence(float confidence, boolean ignoreHold) {

        if (ignoreHold || !confidenceHold) {
            this.confidence = confidence;
        }
    }

    /**
     * Return how competent is the learner on this concept
     * 
     * @return the competence value
     */
    public float getCompetence() {
        return competence;
    }

    /**
     * Set how competent is the learner on this concept
     * 
     * @param competence the competence value
     */
    public void setCompetence(float competence) {
        setCompetence(competence, false);
    }

    /**
     * Set how competent is the learner on this concept
     * 
     * @param competence the competence value
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void setCompetence(float competence, boolean ignoreHold) {

        if (ignoreHold || !competenceHold) {
            this.competence = competence;
        }
    }

    /**
     * Return the assessment trend of the learner on this concept
     * 
     * @return the trend value
     */
    public float getTrend() {
        return trend;
    }

    /**
     * Set the assessment trend of the learner on this concept
     * 
     * @param trend the trend value
     */
    public void setTrend(float trend) {
        setTrend(trend, false);
    }

    /**
     * Set the assessment trend of the learner on this concept
     * 
     * @param trend the trend value
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void setTrend(float trend, boolean ignoreHold) {

        if (ignoreHold || !trendHold) {
            this.trend = trend;
        }
    }

    public String getName() {
        return name;
    }
    
    public int getNodeId(){
        return nodeId;
    }

    public String getNodeCourseId(){
        return nodeCourseId;
    }

    @Override
    public AssessmentLevelEnum getShortTerm() {
        return shortTerm;
    }
    
    @Override
    public long getShortTermTimestamp(){
        return shortTermTimestamp;
    }

    @Override
    public AssessmentLevelEnum getLongTerm() {
        return longTerm;
    }
    
    @Override
    public long getLongTermTimestamp(){
        return longTermTimestamp;
    }

    @Override
    public AssessmentLevelEnum getPredicted() {
        return predicted;
    }
    
    @Override
    public long getPredictedTimestamp(){
        return predictedTimestamp;
    }

    /**
     * Update the short term assessment level
     * 
     * @param level - the new level
     */
    public void updateShortTerm(AssessmentLevelEnum level) {
        updateShortTerm(level, false);
    }

    /**
     * Update the short term assessment level
     * 
     * @param level - the new level
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     */
    public void updateShortTerm(AssessmentLevelEnum level, boolean ignoreHold) {
        updateShortTerm(level, ignoreHold, System.currentTimeMillis());
    }

    /**
     * Update the short term assessment level
     * 
     * @param level - the new level
     * @param ignoreHold - true to update the value regardless of the current
     *        hold state; false to respect the current hold state.
     * @param timestamp - the timestamp to use as the time the assessment was
     *        updated.
     */
    public void updateShortTerm(AssessmentLevelEnum level, boolean ignoreHold, long timestamp) {

        if (ignoreHold || !assessmentHold) {
            shortTerm = level;
            
            setShortTermTimestamp(timestamp);
        }
    }
    
    /**
     * Set the time stamp (epoch) at which the observer started to give an observation (e.g. comment, assessment).<br/>
     * This will over-ride the short term time value.
     * @param observationStartedTime the time at which some observation that is included in this assessment was started
     * by the observer.  Must be greater than 0.
     */
    public void setObservationStartedTime(long observationStartedTime) {
        setShortTermTimestamp(observationStartedTime);
    }

    /**
     * Update the long term assessment level
     * 
     * @param level - the new level
     */
    public void updateLongTerm(AssessmentLevelEnum level){
        longTerm = level;
        longTermTimestamp = System.currentTimeMillis();
    }
    
    /**
     * Update the predicted assessment level
     * 
     * @param level - the new level
     */
    public void updatePredicted(AssessmentLevelEnum level){
        predicted = level;
        predictedTimestamp = System.currentTimeMillis();
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
     * Retrieve the name of the user that updated the state attribute
     * 
     * @return the name of the user that updated the state attribute. Can be null if the username
     *         was never set.
     */
    public String getEvaluator() {
        return evaluator;
    }

    /**
     * Set the name of the user that updated the state attribute
     * 
     * @param evaluator the name of the user that updated the state attribute
     */
    public void setEvaluator(String evaluator) {
        this.evaluator = evaluator;
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
     * attached to this performance state by an observer controller (OC). This can originate from the GIFT Game master UI.<br/>
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
     * attached to this performance state by an observer controller (OC). This can originate from the GIFT Game master UI.<br/>
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
     * Return the collection of unique team organization names of learners whose
     * assessment is represented in this object.
     * 
     * @return the collection of team names and their assessments. Can't be null.
     */
    public Map<String, AssessmentLevelEnum> getAssessedTeamOrgEntities() {
        return assessedTeamOrgEntities;
    }

    /**
     * Set an assessment explanation provided by the assessment logic in GIFT.
     * E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition}
     * might provide an explanation of "Vehicle 1 went 37 mph during a 35 mph
     * speed limit".
     * 
     * @param assessmentExplanation an explanation of the assessment attribute
     *        value in this object. Can be null.
     */
    public void setAssessmentExplanation(Set<String> assessmentExplanation) {
        setAssessmentExplanation(assessmentExplanation, false);
    }
        
    /**
     * Set an assessment explanation provided by the assessment logic in GIFT.
     * E.g. {@see mil.arl.gift.domain.knowledge.condition.SpeedLimitCondition}
     * might provide an explanation of "Vehicle 1 went 37 mph during a 35 mph
     * speed limit".
     * 
     * @param assessmentExplanation an explanation of the assessment attribute
     *        value in this object. Can be null.
     * @param ignoreHold - true to update the value regardless of the current
     *        assessment hold state; false to respect the current hold state.
     */
    public void setAssessmentExplanation(Set<String> assessmentExplanation, boolean ignoreHold) {
        if (ignoreHold || !assessmentHold) {
            this.assessmentExplanations = assessmentExplanation;            
        }
    }

    /**
     * Set the unique team organization names of those learners whose assessment
     * is represented in this object.
     * 
     * @param assessedTeamOrgEntities the names of the assessed learners.
     */
    public void setAssessedTeamOrgEntities(Map<String, AssessmentLevelEnum> assessedTeamOrgEntities) {
        this.assessedTeamOrgEntities.clear();
        if (assessedTeamOrgEntities != null) {
            this.assessedTeamOrgEntities.putAll(assessedTeamOrgEntities);
        }
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
     * Return the time in milliseconds of the last performance assessment
     * update.
     * 
     * @return long time in milliseconds (since epoch)
     */
    public long getPerformanceAssessmentTime() {
        return performanceAssessmentTime;
    }

    /**
     * Sets the latest time of the performance assessment update.
     * 
     * @param performanceAssessmentTime long time in milliseconds (since epoch).
     */
    public void setPerformanceAssessmentTime(long performanceAssessmentTime) {
        this.performanceAssessmentTime = performanceAssessmentTime;
    }

    /**
     * Performs a deep copy of the attribute.
     * 
     * @return a copy of this attribute.
     */
    public abstract AbstractPerformanceStateAttribute deepCopy();

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
    public void setAuthoritativeResource(String authoritativeResource) {
        this.authoritativeResource = authoritativeResource;
    }

    /**
     * Return true if the two objects have the same measurements.
     * 
     * @param attr Another attribute
     * @return boolean - true iff the objects have equal measurements
     */
    public boolean equals(AbstractPerformanceStateAttribute attr) {
        /* should be quicker to detect if one attribute is different vs. making
         * sure all are the same */

        if (this.getShortTerm() == null) {
            if (attr.getShortTerm() != null) {
                return false;
            }
        } else if (!this.getShortTerm().equals(attr.getShortTerm())) {
            return false;
        }

        if (this.getLongTerm() == null) {
            if (attr.getLongTerm() != null) {
                return false;
            }
        } else if (!this.getLongTerm().equals(attr.getLongTerm())) {
            return false;
        }

        if (this.getPredicted() == null) {
            if (attr.getPredicted() != null) {
                return false;
            }
        } else if (!this.getPredicted().equals(attr.getPredicted())) {
            return false;
        }

        return true;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("name = ").append(getName());
        sb.append(", course id = ").append(getNodeCourseId());
        sb.append(", id = ").append(getNodeId());
        sb.append(", scenarioSupport = ").append(isScenarioSupportNode());
        sb.append(", priority = ").append(getPriority());
        sb.append(", nodeState = ").append(getNodeStateEnum());
        sb.append(", confidence = ").append(getConfidence());
        sb.append(", competence = ").append(getCompetence());
        sb.append(", trend = ").append(getTrend());
        sb.append(", short-term = ").append(getShortTerm()); 
        sb.append(", short-term-timestamp = ").append(getShortTermTimestamp());
        sb.append(", long-term = ").append(getLongTerm()); 
        sb.append(", long-term-timestamp = ").append(getLongTermTimestamp());
        sb.append(", predicted = ").append(getPredicted()); 
        sb.append(", predicted-timestamp = ").append(getPredictedTimestamp());
        sb.append(", priorityHold = ").append(isPriorityHold());
        sb.append(", assessmentHold = ").append(isAssessmentHold());
        sb.append(", confidenceHold = ").append(isConfidenceHold());
        sb.append(", competenceHold = ").append(isCompetenceHold());
        sb.append(", trendHold = ").append(isTrendHold());
        sb.append(", evaluator = ").append(getEvaluator());
        sb.append(", observerComment = ").append(getObserverComment());
        sb.append(", observerMedia = ").append(getObserverMedia());
        sb.append(", assessedTeamOrgEntities = ").append(getAssessedTeamOrgEntities());
        sb.append(", performanceAssessmentTime = ").append(getPerformanceAssessmentTime());
        sb.append(", authoritativeResource = ").append(getAuthoritativeResource());

        if (getAssessmentExplanation() != null) {
            sb.append(", assessmentExplanation = ").append(getAssessmentExplanation());
        }

        return sb.toString();
    }
}
