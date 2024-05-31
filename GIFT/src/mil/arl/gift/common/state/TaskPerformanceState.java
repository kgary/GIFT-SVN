/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import mil.arl.gift.common.score.TaskScoreNode;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;


/**
 * This class contains the learner state performance state measurements for a task.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class TaskPerformanceState extends AbstractPerformanceState {    
    
    /** the min difficulty value for easy */
    public static final Double EASY_DIFFICULTY = Double.valueOf(1.0);
    
    /** the min difficulty value for medium */
    public static final Double MED_DIFFICULTY = Double.valueOf(2.0);
    
    /** the min difficulty value for hard */
    public static final Double HARD_DIFFICULTY = Double.valueOf(3.0);
    
    /** the performance state(s) of concepts associated with this task */
    private List<ConceptPerformanceState> concepts;
    
    /** the difficulty value for this task, optional or between {@link #TaskScoreNode.MIN_DIFFICULTY} and {@link #TaskScoreNode.MAX_DIFFICULTY} */
    private Double difficulty;    
    
    /** an explanation on the reason for the difficulty value */
    private String difficultyReason;
    
    /** the stress value for this task, optional or between {@link #TaskScoreNode.MIN_STRESS} and {@link #TaskScoreNode.MAX_STRESS} */
    private Double stress;
    
    /** an explanation on the reason for the stress value */
    private String stressReason;
    
    /**
     * Default constructor
     */
    public TaskPerformanceState(){
        super(null);
        concepts = new ArrayList<ConceptPerformanceState>();
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param state - the performance state for a task
     * @param concepts - the performance states for the concepts of this task
     */
    public TaskPerformanceState(PerformanceStateAttribute state, List<ConceptPerformanceState> concepts){
        super(state);
        this.concepts = concepts;
    }

    /**
     * Return the concept performance states for the task
     * 
     * @return the concepts under this task. Can be null if never set and can be empty.
     */
    public List<ConceptPerformanceState> getConcepts() {
        return concepts;
    }    
    
    @Override
    public int hashCode() {
        return Objects.hash(concepts, difficulty, difficultyReason, stress, stressReason);
    }

    @Override
    public boolean equals(Object otherState){
        
        if(!super.equals(otherState)){
            return false;
        }else if(!(otherState instanceof TaskPerformanceState)) {
            return false;
        }
        
        TaskPerformanceState state = (TaskPerformanceState)otherState;
        
        //check concepts
        List<ConceptPerformanceState> otherCStates = state.getConcepts();
        List<ConceptPerformanceState> thisCStates = this.getConcepts();
        
        if(otherCStates.size() != thisCStates.size()){
            return false;
        }
        
        if(!CollectionUtils.equals(thisCStates, otherCStates)) {
            return false;
        }else if(this.difficulty != null && !this.difficulty.equals(state.difficulty)) {
            return false;
        }else if(state.difficulty != null && this.difficulty == null) {
            return false;
        }else if(!StringUtils.equals(this.difficultyReason, state.difficultyReason)) {
            return false;
        }else if(this.stress != null && !this.stress.equals(state.stress)) {
            return false;
        }else if(state.stress != null && this.stress == null) {
            return false;
        }else if(!StringUtils.equals(this.stressReason, state.stressReason)) {
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
     * Return the difficulty value for this task as a string.  
     * @return null if the difficulty value is null or one of  {easy,medium,hard} depending on the difficulty value
     * compared to {@link #EASY_DIFFICULTY}, {@link #MED_DIFFICULTY}, {@link #HARD_DIFFICULTY}.
     */
    public String getDifficultyAsString() {
        
        if(difficulty == null) {
            return null;
        }else if(difficulty >= HARD_DIFFICULTY) {
            return "Hard";
        }else if(difficulty >= MED_DIFFICULTY) {
            return "Medium";
        }else {
            return "Easy";
        }
    }

    /**
     * Set the difficulty value of this task.  The value is related to the set of information that 
     * was used to grade this node (e.g. data set for the duration the task was active).
     * @param difficulty can be null to indicate the value is not set, otherwise a value between {@link #TaskScoreNode.MIN_DIFFICULTY}
     * and {@link #MAX_DIFFICULTY} is used.  If outside of those bounds the value will be changed to {@link #TaskScoreNode.MIN_DIFFICULTY}
     * or {@link #MAX_DIFFICULTY}.
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
     * and {@link #MAX_STRESS} is used.  If outside of those bounds the value will be changed to {@link #TaskScoreNode.MIN_STRESS} 
     * or {@link #MAX_STRESS};
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
    public Long getObservationStartedTime(){
        
        Long obsStartTime = super.getObservationStartedTime();
        if(obsStartTime == null){

            // check descendants
            for(ConceptPerformanceState conceptPerfState : concepts){
                
                obsStartTime = conceptPerfState.getObservationStartedTime();
                if(obsStartTime != null){
                    break;
                }
            }
        }
        
        return obsStartTime;
    }

    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[TaskPerformanceState:");
        sb.append(super.toString());
        sb.append(", difficulty = ").append(difficulty);
        sb.append(", difficulty-reason = ").append(difficultyReason);
        sb.append(", stress = ").append(stress);
        sb.append(", stress-reason = ").append(stressReason);
        
        sb.append(", concepts = {");
        for(ConceptPerformanceState state : concepts){
            sb.append(state).append(", ");
        }
        sb.append("}");

        sb.append("]");
        
        return sb.toString();
    }
    
}
