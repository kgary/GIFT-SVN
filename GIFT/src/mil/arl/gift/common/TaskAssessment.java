/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.List;
import java.util.UUID;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains information about an assessment of a domain's task.
 *
 * @author mhoffman
 *
 */
public class TaskAssessment extends AbstractAssessment {        
    
    /** the concepts associated with this task */
    private List<ConceptAssessment> conceptAssessments;
    
    /** the difficulty value for this task, optional or between {@link #TaskScoreNode.MIN_DIFFICULTY} and {@link #TaskScoreNode.MAX_DIFFICULTY} */
    private Double difficulty;    
    
    /** an explanation on the reason for the difficulty value */
    private String difficultyReason;
    
    /** the stress value for this task, optional or between {@link #TaskScoreNode.MIN_STRESS} and {@link #TaskScoreNode.MAX_STRESS} */
    private Double stress;
    
    /** an explanation on the reason for the stress value */
    private String stressReason;
    
    /**
     * Class constructor - set attributes
     * 
     * @param name - the name of the task being assessed.
     * @param assessment - the task assessment value
     * @param time - the time at which this assessment was calculated
     * @param conceptAssessments - the task's collection of concepts
     * @param nodeId - the unique performance node id of this task
     * @param courseUUID - the course level unique id for this assessment node
     */
    public TaskAssessment(String name, AssessmentLevelEnum assessment, long time, 
            List<ConceptAssessment> conceptAssessments, int nodeId, UUID courseUUID){
        super(name, assessment, time, nodeId, courseUUID);
        
        this.conceptAssessments = conceptAssessments;
    }
    
    /**
     * The child concept assessments for this task.
     * 
     * @return the concepts directly under this task.
     */
    public List<ConceptAssessment> getConceptAssessments(){
        return conceptAssessments;
    }
    
    /**
     * Update the time associated with this assessment
     */
    public void updated(){
        this.time = System.currentTimeMillis();
    }
    
    @Override
    public boolean equals(Object otherTaskAssessment){
        
        if(otherTaskAssessment == null){
            return false;
        }else if(!super.equals(otherTaskAssessment)){
            return false;
        }else if(!(otherTaskAssessment instanceof TaskAssessment)){
            return false;
        }
            
        //check concepts
        List<ConceptAssessment> thatConcepts = ((TaskAssessment)otherTaskAssessment).getConceptAssessments();
        List<ConceptAssessment> thisConcepts = this.getConceptAssessments();        
        
        if(!thisConcepts.equals(thatConcepts)) {
            return false;
        }else if(this.difficulty != ((TaskAssessment)otherTaskAssessment).difficulty) {
            return false;
        }else if(!StringUtils.equals(this.difficultyReason, ((TaskAssessment)otherTaskAssessment).difficultyReason)) {
            return false;
        }else if(this.stress != ((TaskAssessment)otherTaskAssessment).stress) {
            return false;
        }else if(!StringUtils.equals(this.stressReason, ((TaskAssessment)otherTaskAssessment).stressReason)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Return the difficulty value for this task.  The value is related to the set of information that 
     * was used to grade this node (e.g. data set for the duration the task was active).
     * @return null if not set, or a value between {@link #TaskScoreNode.MIN_DIFFICULTY} and {@link #TaskScoreNode.MAX_DIFFICULTY}.
     */
    public Double getDifficulty() {
        return difficulty;
    }

    /**
     * Set the difficulty value of this task.  The value is related to the set of information that 
     * was used to grade this node (e.g. data set for the duration the task was active).
     * @param difficulty can be null to indicate the value is not set, otherwise a value between {@link #TaskScoreNode.MIN_DIFFICULTY}
     * and {@link #TaskScoreNode.MAX_DIFFICULTY} is used.  If outside of those bounds the value will be changed to {@link #TaskScoreNode.MIN_DIFFICULTY}
     * or {@link #TaskScoreNode.MAX_DIFFICULTY}.
     */
    public void setDifficulty(Double difficulty) {
        
        if(difficulty != null) {
            if(difficulty < TaskScoreNode.MIN_DIFFICULTY) {
                difficulty = TaskScoreNode.MIN_DIFFICULTY;
            }else if(difficulty > TaskScoreNode.MAX_DIFFICULTY) {
                difficulty = TaskScoreNode.MAX_DIFFICULTY;
            }
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
     * @return null if not set, or a value between {@link #TaskScoreNode.MIN_STRESS} and {@link #TaskScoreNode.MAX_STRESS}.
     */
    public Double getStress() {
        return stress;
    }

    /**
     * Set the stress value for this task.  The value is related to the set of information
     * that was used to grade this node (e.g. data set for the duration the task was active).
     * 
     * @param stress can be null to indicate the value is not set, otherwise a value between {@link #TaskScoreNode.MIN_STRESS} 
     * and {@link #TaskScoreNode.MAX_STRESS} is used.  If outside of those bounds the value will be changed to {@link #TaskScoreNode.MIN_STRESS} 
     * or {@link #TaskScoreNode.MAX_STRESS};
     */
    public void setStress(Double stress) {
        
        if(stress != null) {
            if(stress < TaskScoreNode.MIN_STRESS) {
                stress = TaskScoreNode.MIN_STRESS;
            }else if(stress > TaskScoreNode.MAX_STRESS) {
                stress = TaskScoreNode.MAX_STRESS;
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

        for(ConceptAssessment conceptAssessment : getConceptAssessments()){
            sb.append("\n").append(conceptAssessment).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
