/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.Map;

import mil.arl.gift.common.AbstractAssessment.PerformanceNodeStateEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains the metrics to update a {@link Task} or {@link Concept} using a provided
 * unique name as the identifier.
 *
 * @author sharrison
 *
 */
@SuppressWarnings("serial")
public class EvaluatorUpdateRequest implements Serializable{

    /** The unique name of the {@link Task} or {@link Concept} being updated */
    private String nodeName;

    /** The username of the person making the request */
    private String evaluator;
    
    /** the epoch time when the evaluator update happened */
    private long timestamp;

    /** The performance metric */
    private AssessmentLevelEnum performanceMetric;
    
    /**
     * whether to hold the assessment value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     */
    private Boolean assessmentHold;

    /** The confidence metric */
    private Float confidenceMetric;

    /**
     * whether to hold the confidence value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     */
    private Boolean confidenceHold;

    /** The competence metric */
    private Float competenceMetric;

    /**
     * whether to hold the competence value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     */
    private Boolean competenceHold;

    /** The trend metric */
    private Float trendMetric;

    /**
     * whether to hold the trend value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     */
    private Boolean trendHold;

    /** The priority metric */
    private Integer priorityMetric;

    /**
     * whether to hold the priority value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     */
    private Boolean priorityHold;

    /** The new state to place the performance node into (only for Tasks) */
    private PerformanceNodeStateEnum state = null;

    /** The reason for the update */
    private String reason;

    /** The (optional) team org entities and their respective assessments */
    private Map<String, AssessmentLevelEnum> teamOrgEntities;
    
    /** 
     * The reference to the media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * associated with this update 
     */
    private String mediaFile;

    /**
     * Default contructor required by GWT's RPC serialization policy
     */
    private EvaluatorUpdateRequest() {}

    /**
     * Class constructor
     *
     * @param nodeName the unique name of the {@link Task} or {@link Concept} being updated. Can be
     *        null, if this update is for the entire scenario.
     * @param evaluator the username of the person making the request. Can be null.
     * @param timestamp epoch time when the update happened which can be different than the message timestamp.
     */
    public EvaluatorUpdateRequest(String nodeName, String evaluator, long timestamp) {
        this();
        
        this.nodeName = nodeName;
        this.evaluator = evaluator;
        setTimestamp(timestamp);
    }

    /**
     * The unique name of the {@link Task} or {@link Concept} being updated.
     *
     * @return the unique name of the {@link Task} or {@link Concept} being updated. Can be null, if this
     * update is for the entire scenario.
     */
    public String getNodeName() {
        return nodeName;
    }

    /**
     * The username of the person making the request.
     *
     * @return the username of the person making the request. Can be null if not set.
     */
    public String getEvaluator() {
        return evaluator;
    }

    /**
     * Set the username of the person making the request.
     *
     * @param evaluator the username of the person making the request. Can be null.
     */
    public void setEvaluator(String evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Retrieve the performance metric.
     *
     * @return the performance metric. Can be null if it was never set (not being updated).
     */
    public AssessmentLevelEnum getPerformanceMetric() {
        return performanceMetric;
    }

    /**
     * Sets the performance metric.
     *
     * @param performanceMetric the updated performance metric. Can be null.
     */
    public void setPerformanceMetric(AssessmentLevelEnum performanceMetric) {
        this.performanceMetric = performanceMetric;
    }

    /**
     * Retrieve the confidence metric.
     *
     * @return the confidence metric. Can be null if it was never set (not being updated).
     * the value must be between {@link #MIN_CONFIDENCE} and {@link #MAX_CONFIDENCE} (inclusive)
     */
    public Float getConfidenceMetric() {
        return confidenceMetric;
    }

    /**
     * Sets the confidence metric.
     *
     * @param confidenceMetric the updated confidence metric. the value must be between {@link AbstractAssessment#MIN_CONFIDENCE}
     *  and {@link AbstractAssessment#MAX_CONFIDENCE} (inclusive)
     */
    public void setConfidenceMetric(float confidenceMetric) {
        this.confidenceMetric = confidenceMetric;
    }

    /**
     * Retrieve the competence metric.
     *
     * @return the competence metric. Can be null if it was never set (not being updated).
     * A value between {@link AbstractAssessment#MIN_COMPETENCE} 
     * and {@link AbstractAssessment#MAX_COMPETENCE} (inclusive)
     */
    public Float getCompetenceMetric() {
        return competenceMetric;
    }

    /**
     * Sets the competence metric.
     *
     * @param competenceMetric the updated competence metric. A value between {@link AbstractAssessment#MIN_COMPETENCE} 
     * and {@link AbstractAssessment#MAX_COMPETENCE} (inclusive)
     */
    public void setCompetenceMetric(float competenceMetric) {
        this.competenceMetric = competenceMetric;
    }

    /**
     * Retrieve the trend metric.
     *
     * @return the trend metric. Can be null if it was never set (not being updated).
     * The value must be between {@link AbstractAssessment#MIN_TREND} and
     *        {@link AbstractAssessment#MAX_TREND} (inclusive).
     */
    public Float getTrendMetric() {
        return trendMetric;
    }

    /**
     * Sets the trend metric.
     *
     * @param trendMetric the updated trend metric. The value must be between {@link AbstractAssessment#MIN_TREND} and
     *        {@link AbstractAssessment#MAX_TREND} (inclusive).
     */
    public void setTrendMetric(float trendMetric) {
        this.trendMetric = trendMetric;
    }

    /**
     * Retrieve the priority metric.
     *
     * @return the priority metric. Can be null if it was never set (not being updated). If not null, the value
     * will not be less than 1.
     */
    public Integer getPriorityMetric() {
        return priorityMetric;
    }

    /**
     * Sets the priority metric.
     *
     * @param priorityMetric the updated priority metric. Can be null. 
     * If the value is less than 1, 1 will be used instead.
     */
    public void setPriorityMetric(Integer priorityMetric) {
        
        if(priorityMetric != null && priorityMetric < 1){
            priorityMetric = 1;
        }
        this.priorityMetric = priorityMetric;
    }

    /**
     * Checks if this request contains at least one updated value.
     *
     * @return true if this request has one or more updated value populated;
     *         false otherwise.
     */
    public boolean hasChanges() {
        
        /* Check for a global bookmark with a non-null reason (whitespace is allowed for global bookmarks) */
        if (getNodeName() == null && getReason() != null) {
            return true;
        }
        
        /* Check for a change in metric values */
        boolean hasMetricChange = getPerformanceMetric() != null || getConfidenceMetric() != null
                || getCompetenceMetric() != null || getTrendMetric() != null || getPriorityMetric() != null;
        if (hasMetricChange) {
            return true;
        }

        /* Check for a change in hold values */
        boolean hasHoldChange = isAssessmentHold() != null || isConfidenceHold() != null || isCompetenceHold() != null
                || isTrendHold() != null || isPriorityHold() != null;
        if (hasHoldChange) {
            return true;
        }

        /* Check for a change in other values (e.g. state, reason, team org
         * entities, etc...) */
        if (getState() != null || StringUtils.isNotBlank(getReason()) || StringUtils.isNotBlank(getMediaFile()) || CollectionUtils.isNotEmpty(teamOrgEntities)) {
            return true;
        }

        return false;
    }

    /**
     * Return whether to hold the trend value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @return true if the assessment is being held. Can be null if it was never set (not being updated).
     */
    public Boolean isAssessmentHold() {
        return assessmentHold;
    }

    /**
     * Set whether to hold the assessment value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @param assessmentHold whether to hold. Can be null.
     */
    public void setAssessmentHold(Boolean assessmentHold) {
        this.assessmentHold = assessmentHold;
    }

    /**
     * Return whether to hold the priority value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @return true if the priority is being held. Can be null if it was never set (not being updated).
     */
    public Boolean isPriorityHold() {
        return priorityHold;
    }

    /**
     * Set whether to hold the priority value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @param priorityHold whether to hold. Can be null.
     */
    public void setPriorityHold(Boolean priorityHold) {
        this.priorityHold = priorityHold;
    }

    /**
     * Return whether to hold the confidence value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @return true if the confidence is being held. Can be null if it was never set (not being updated).
     */
    public Boolean isConfidenceHold() {
        return confidenceHold;
    }

    /**
     * Set whether to hold the confidence value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @param confidenceHold whether to hold. Can be null.
     */
    public void setConfidenceHold(Boolean confidenceHold) {
        this.confidenceHold = confidenceHold;
    }

    /**
     * Return whether to hold the competence value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @return true if the competence is being held. Can be null if it was never set (not being updated).
     */
    public Boolean isCompetenceHold() {
        return competenceHold;
    }

    /**
     * Set whether to hold the competence value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @param competenceHold whether to hold. Can be null.
     */
    public void setCompetenceHold(Boolean competenceHold) {
        this.competenceHold = competenceHold;
    }

    /**
     * Return whether to hold the trend value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @return true if the assessment is being held. Can be null if it was never set (not being updated).
     */
    public Boolean isTrendHold() {
        return trendHold;
    }

    /**
     * Set whether to hold the trend value
     * (i.e. GIFT can't automatically change it, requires an external change like the game master)
     * @param trendHold whether to hold. Can be null.
     */
    public void setTrendHold(Boolean trendHold) {
        this.trendHold = trendHold;
    }

    /**
     * Return the reason for the update request.
     * 
     * @return the reason for the update request. Can be null if not set.
     */
    public String getReason() {
        return reason;
    }

    /**
     * Set the reason for the update request.
     * 
     * @param reason the reason for the update request. Can be null.
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Getter for the requested life cycle state of the described performance
     * node.
     *
     * @return The requested {@link PerformanceNodeStateEnum} of the described
     *         performance node. Can be null.
     */
    public PerformanceNodeStateEnum getState() {
        return state;
    }

    /**
     * Setter for the the requested life cycle state of the described
     * performance node.
     *
     * @param state The new {@link PerformanceNodeStateEnum} of the described
     *        performance node. Can't be null.
     */
    public void setState(PerformanceNodeStateEnum state) {
        if (state == null) {
            throw new IllegalArgumentException("The parameter 'state' cannot be null.");
        }

        this.state = state;
    }

    /**
     * Getter for the team org entities and their respective assessments.
     * 
     * @return the list of team org entities that are responsible for this
     *         update. Should only contain team member names not team names. Can
     *         be null.
     */
    public Map<String, AssessmentLevelEnum> getTeamOrgEntities() {
        return teamOrgEntities;
    }

    /**
     * Setter for the team org entities and their respective assessments.
     * 
     * @param teamOrgEntities the list of team organization entities that are
     *        responsible for this update. Should only contain team member names
     *        not team names. Can be null.
     */
    public void setTeamOrgEntities(Map<String, AssessmentLevelEnum> teamOrgEntities) {
        this.teamOrgEntities = teamOrgEntities;
    }
    
    /**
     * Gets the reference to the media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"])
     * associated with this update
     * 
     * @return the media file reference. Can be null if this update has no associated media.
     */
    public String getMediaFile() {
        return mediaFile;
    }

    /**
     * Sets the reference to the media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"])
     * associated with this update
     * 
     * @param mediaFile the media file reference. Can be null if this update has no associated media.
     */
    public void setMediaFile(String mediaFile) {
        this.mediaFile = mediaFile;
    }

    /**
     * Get the epoch time when the evaluator update happened, i.e. the time at which some observation 
     * was started by the observer.
     * @return epoch time when the update happened which can be different than the message timestamp. Can be zero
     * if not set properly or for legacy messages.  Use the message timestamp as a fall back for timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the epoch time when the evaluator update happened, i.e. the time at which some observation 
     * was started by the observer.
     * @param timestamp epoch time when the update happened which can be different than the message timestamp.
     */
    public void setTimestamp(long timestamp) {
        
        if(timestamp < 1) {
            throw new IllegalArgumentException("The timestamp value of "+timestamp+" must be a positive value.");
        }
        
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[EvaluatorUpdateRequest: ");
        sb.append("nodeName = ").append(getNodeName());
        sb.append(", evaluator = ").append(getEvaluator());
        sb.append(", performanceMetric = ").append(getPerformanceMetric());
        sb.append(", confidenceMetric = ").append(getConfidenceMetric());
        sb.append(", competenceMetric = ").append(getCompetenceMetric());
        sb.append(", trendMetric = ").append(getTrendMetric());
        sb.append(", priorityMetric = ").append(getPriorityMetric());
        sb.append(", priorityHold = ").append(isPriorityHold());
        sb.append(", assessmentHold = ").append(isAssessmentHold());
        sb.append(", confidenceHold = ").append(isConfidenceHold());
        sb.append(", competenceHold = ").append(isCompetenceHold());
        sb.append(", trendHold = ").append(isTrendHold());
        sb.append(", reason = ").append(getReason());
        sb.append(", timestamp = ").append(getTimestamp());
        sb.append(", mediaFile = ").append(getMediaFile());
        sb.append(", state = ").append(getState());
        sb.append(", teamOrgEntities = ").append(teamOrgEntities);
        sb.append("]");

        return sb.toString();
    }
}
