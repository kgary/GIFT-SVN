/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.domain.knowledge.common;

import java.util.List;
import java.util.UUID;

import mil.arl.gift.common.AbstractAssessment;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains information about an assessment of a domain's task.
 * This particular instance contains only the unique ids of the child concepts associated with this
 * task node.  
 *
 * @author mhoffman
 *
 */
public class ProxyTaskAssessment extends AbstractAssessment {     
    
    /** the min value for difficulty */
    public static final Double MIN_DIFFICULTY = TaskScoreNode.MIN_DIFFICULTY;
    
    /** the max value for difficulty */
    public static final Double MAX_DIFFICULTY = TaskScoreNode.MAX_DIFFICULTY;
    
    /** the min value for stress */
    public static final Double MIN_STRESS = TaskScoreNode.MIN_STRESS;
    
    /** the max value for stress */
    public static final Double MAX_STRESS = TaskScoreNode.MAX_STRESS;
    
    /** the concepts associated with this task */
    private List<UUID> proxyConceptIds; 
    
    /** the initial difficulty rating for this task */
    private Double initDifficulty;
    
    /** the difficulty value for this task, optional or between {@link #MIN_DIFFICULTY} and {@link #MAX_DIFFICULTY} */
    private Double difficulty;    
    
    /** an explanation on the reason for the difficulty value */
    private String difficultyReason;
    
    /** the stress value for this task, optional or between {@link #MIN_STRESS} and {@link #MAX_STRESS} */
    private Double stress;
    
    /** an explanation on the reason for the stress value */
    private String stressReason;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the name of the task being assessed.
     * @param assessment - the task assessment value
     * @param time - the time at which this assessment was calculated
     * @param proxyConceptIds - the task's list of concepts
     * @param nodeId - the unique performance node id of this task
     * @param courseUUID - course level unique id for this assessment node
     */
    public ProxyTaskAssessment(String name, AssessmentLevelEnum assessment, long time, 
            List<UUID> proxyConceptIds, int nodeId, UUID courseUUID){
        super(name, assessment, time, nodeId, courseUUID);
        
        this.proxyConceptIds = proxyConceptIds;
    }    
    
    /**
     * Return the list of concept assessments associated with this task assessment
     * 
     * @return List<UUID>
     */
    public List<UUID> getConcepts(){
        return proxyConceptIds;
    }

    
    /**
     * Return true if the two objects have the same measurements.
     * 
     * @param that Another task assessment
     * @return boolean - true iff the objects have equal measurements
     */
    public boolean equals(ProxyTaskAssessment that){
        
        if(!super.equals(that)){
            return false;
        }
            
        //check concepts
        List<UUID> thatConceptIds = that.getConcepts();
        List<UUID> thisConceptIds = this.getConcepts();        
        
        if(!thisConceptIds.containsAll(thatConceptIds)) {
            return false;
        }else if(this.difficulty != that.difficulty) {
            return false;
        }else if(!StringUtils.equals(this.difficultyReason, that.difficultyReason)) {
            return false;
        }else if(this.stress != that.stress) {
            return false;
        }else if(!StringUtils.equals(this.stressReason, that.stressReason)) {
            return false;
        }
        
        return true;
    } 
    
    /**
     * Return the initial difficulty of this task.  
     * @return null if the difficulty was never set, otherwise a value between {{@link #MIN_DIFFICULTY} 
     * and {@link #MAX_DIFFICULTY}
     */
    public Double getInitialDifficulty() {
        return initDifficulty;
    }   
    
    /**
     * Return the difficulty value for this task.  The value is related to the set of information that 
     * was used to grade this node (e.g. data set for the duration the task was active).
     * @return null if not set, or a value between {@link #MIN_DIFFICULTY} and {@link #MAX_DIFFICULTY}.
     */
    public Double getDifficulty() {
        return difficulty;
    }

    /**
     * Set the difficulty value of this task.  The value is related to the set of information that 
     * was used to grade this node (e.g. data set for the duration the task was active).
     * @param difficulty can be null to indicate the value is not set, otherwise a value between {@link #MIN_DIFFICULTY}
     * and {@link #MAX_DIFFICULTY} is used.  If outside of those bounds the value will be changed to {@link #MIN_DIFFICULTY}
     * or {@link #MAX_DIFFICULTY}.
     */
    public void setDifficulty(Double difficulty) {
        
        if(difficulty != null) {
            if(difficulty < MIN_DIFFICULTY) {
                difficulty = MIN_DIFFICULTY;
            }else if(difficulty > MAX_DIFFICULTY) {
                difficulty = MAX_DIFFICULTY;
            }
        }
        
        if(this.difficulty == null) {
            this.initDifficulty = difficulty;
        }
        
        this.difficulty = difficulty;
    }

    /**
     * Return the reason for the difficulty value.
     * @return can be null or empty
     */
    public String getDifficultyReason() {
        return difficultyReason;
    }

    /**
     * Set the reason for the difficulty value.
     * @param difficultyReason can be null or empty.
     */
    public void setDifficultyReason(String difficultyReason) {
        this.difficultyReason = difficultyReason;
    }

    /**
     * Return the stress value for this task.  The value is related to the set of information that 
     * was used to grade this node (e.g. data set for the duration the task was active).
     * @return null if not set, or a value between {@link #MIN_STRESS} and {@link #MAX_STRESS}.
     */
    public Double getStress() {
        return stress;
    }

    /**
     * Set the stress value for this task.  The value is related to the set of information
     * that was used to grade this node (e.g. data set for the duration the task was active).
     * 
     * @param stress can be null to indicate the value is not set, otherwise a value between {@link #MIN_STRESS} 
     * and {@link #MAX_STRESS} is used.  If outside of those bounds the value will be changed to {@link #MIN_STRESS} 
     * or {@link #MAX_STRESS};
     */
    public void setStress(Double stress) {
        
        if(stress != null) {
            if(stress < MIN_STRESS) {
                stress = MIN_STRESS;
            }else if(stress > MAX_STRESS) {
                stress = MAX_STRESS;
            }
        }
        this.stress = stress;
    }

    /**
     * Return a reason for the stress value.
     * @return can be null or empty.
     */
    public String getStressReason() {
        return stressReason;
    }

    /**
     * Set the reason for the stress value.
     * @param stressReason can be null or empty.
     */
    public void setStressReason(String stressReason) {
        this.stressReason = stressReason;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[TaskAssessment: ");
        sb.append(super.toString());
        sb.append(", difficulty = ").append(difficulty);
        sb.append(", difficulty-reason = ").append(difficultyReason);
        sb.append(", stress = ").append(stress);
        sb.append(", stress-reason = ").append(stressReason);
        
        sb.append(", concepts = {");

        for(UUID conceptId : getConcepts()){
            sb.append(" ").append(conceptId).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
