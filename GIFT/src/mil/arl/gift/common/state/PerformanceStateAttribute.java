/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.HashSet;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.util.CollectionUtils;

/**
 * This class contains a learner's performance state attribute measurements (e.g. concept "corridor check" assessments).
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class PerformanceStateAttribute extends AbstractPerformanceStateAttribute {
    
    /** 
     * Default constructor needed for GWT RPC serialization
     */
    @SuppressWarnings("unused")
    private PerformanceStateAttribute() {}
    
    /**
     * Class constructor
     * 
     * @param name - the name of the performance state attribute
     * @param nodeId unique performance node id (i.e. unique across a DKF)
     * @param nodeCourseId the course level unique node id (i.e. unique across a course)
     * @param shortTerm - the short term assessment value 
     * @param shortTermTimestamp the time in milliseconds at which the short term state value was set
     * @param longTerm - the long term assessment value
     * @param longTermTimestamp the time in milliseconds at which the long term state value was set
     * @param predicted - the predicted assessment value
     * @param predictedTimestamp the time in milliseconds at which the predicted state value was set
     */
    public PerformanceStateAttribute(String name, int nodeId, String nodeCourseId, 
            AssessmentLevelEnum shortTerm, long shortTermTimestamp,
            AssessmentLevelEnum longTerm, long longTermTimestamp,
            AssessmentLevelEnum predicted, long predictedTimestamp){
        super(name, nodeId, nodeCourseId, shortTerm, shortTermTimestamp, longTerm, longTermTimestamp, predicted, predictedTimestamp);
    }
    
    /**
     * Class constructor
     * 
     * @param name - the name of the performance state attribute
     * @param nodeId - the unique performance node id
     * @param nodeCourseId the course level unique node id (i.e. unique across a course)
     * @param shortTerm - the short term assessment value 
     * @param longTerm - the long term assessment value
     * @param predicted - the predicted assessment value
     */
    public PerformanceStateAttribute(String name, int nodeId, String nodeCourseId, AssessmentLevelEnum shortTerm, AssessmentLevelEnum longTerm, AssessmentLevelEnum predicted){
        super(name, nodeId, nodeCourseId, shortTerm, longTerm, predicted);
    }
    
    @Override
    public PerformanceStateAttribute deepCopy() {
        PerformanceStateAttribute copy = new PerformanceStateAttribute(getName(), getNodeId(), getNodeCourseId(), getShortTerm(),
                getShortTermTimestamp(), getLongTerm(), getLongTermTimestamp(), getPredicted(),
                getPredictedTimestamp());

        copy.setAssessmentHold(isAssessmentHold());
        copy.setCompetenceHold(isCompetenceHold());
        copy.setConfidenceHold(isConfidenceHold());
        copy.setPriorityHold(isPriorityHold());
        copy.setTrendHold(isTrendHold());

        copy.setCompetence(getCompetence(), true);
        copy.setConfidence(getConfidence(), true);
        copy.setPriority(getPriority(), true);
        copy.setTrend(getTrend(), true);

        copy.setEvaluator(getEvaluator());
        copy.setObserverComment(getObserverComment());
        copy.setObserverMedia(getObserverMedia());
        copy.setScenarioSupportNode(isScenarioSupportNode());
        copy.setNodeStateEnum(getNodeStateEnum());
        copy.setAssessedTeamOrgEntities(getAssessedTeamOrgEntities());
        copy.setPerformanceAssessmentTime(getPerformanceAssessmentTime());

        if (CollectionUtils.isNotEmpty(getAssessmentExplanation())) {
            copy.setAssessmentExplanation(new HashSet<String>(), true);
            copy.getAssessmentExplanation().addAll(getAssessmentExplanation());
        }

        return copy;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PerformanceStateAttribute: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
