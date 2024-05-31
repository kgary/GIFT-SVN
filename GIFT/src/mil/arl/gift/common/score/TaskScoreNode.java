/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * The graded score node representation for a task that allows for additional attributes beyond
 * the basic graded score node.
 * @author mhoffman
 *
 */
public class TaskScoreNode extends GradedScoreNode {
    
    /**
     * default
     */
    private static final long serialVersionUID = 1L;    
    
    /** the min value for difficulty - also defined in dkf.xsd */
    public static final Double MIN_DIFFICULTY = Double.valueOf(1.0);
    
    /** the max value for difficulty - also defined in dkf.xsd */
    public static final Double MAX_DIFFICULTY = Double.valueOf(3.0);
    
    /** the min value for stress of a task - also defined in dkf.xsd */
    public static final Double MIN_STRESS = Double.valueOf(0.0);
    
    /** the max value for stress of a task - also defined in dkf.xsd */
    public static final Double MAX_STRESS = Double.valueOf(1.0);
    
    /** the difficulty value for this task, optional or between {@link #MIN_DIFFICULTY} and {@link #MAX_DIFFICULTY} */
    private Double difficulty;
    
    /** an explanation on the reason for the difficulty value */
    private String difficultyReason;
    
    /** the stress value for this task, optional or between {@link #MIN_STRESS} and {@link #MAX_STRESS} */
    private Double stress;
    
    /** an explanation on the reason for the stress value */
    private String stressReason;
    
    /**
     * Default constructor - needed for gwt serialization.
     */
    @SuppressWarnings("unused")
    private TaskScoreNode() { 
        super();
    }
    
    /**
     * Set attributes 
     * @param name - the name of the task node.
     * @param assessmentLevel - the assessment level given to the score node.  Can't be null. 
     */
    public TaskScoreNode(String name, AssessmentLevelEnum assessmentLevel) {
        super(name, assessmentLevel);
    }
    
    /**
     * Set attribute
     * @param name - the name of the task node.
     */
    public TaskScoreNode(String name) {
        super(name);
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
            }else if(difficulty >  MAX_DIFFICULTY) {
                difficulty = MAX_DIFFICULTY;
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
    
    /**
     * Creates a new instance of the {@link GradedScoreNode} provided.
     * @param original the {@link GradedScoreNode} to copy.  If null, null is returned.  Can
     * be a {@link TaskScoreNode}.
     * @return the new deep copy of the provided GradedScoreNode.
     */
    public static GradedScoreNode deepCopy(GradedScoreNode original){
        
        if(original == null) {
            return null;
        }
        
        if(original instanceof TaskScoreNode) {
            
            TaskScoreNode copy = new TaskScoreNode(original.getName(), original.getAssessment());
            copy.setParent(original.getParent());
            if(original.getPerformanceNodeId() != null){
                copy.setPerformanceNodeId(original.getPerformanceNodeId());
            }
            
            GradedScoreNode.deepCopyChildren(original, copy);
            
            // Unique to this class...
            copy.setDifficulty(((TaskScoreNode)original).getDifficulty());
            copy.setDifficultyReason(((TaskScoreNode)original).getDifficultyReason());
            copy.setStress(((TaskScoreNode)original).getStress());
            copy.setStressReason(((TaskScoreNode)original).getStressReason());
            
            return copy;
        }else {
            return GradedScoreNode.deepCopy(original);
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[TaskScoreNode: ");
        builder.append(super.toString());
        builder.append(", difficulty = ");
        builder.append(difficulty);
        builder.append(", difficulty-reason = ");
        builder.append(difficultyReason);
        builder.append(", stress = ");
        builder.append(stress);
        builder.append(", stress-reason = ");
        builder.append(stressReason);
        builder.append("]");
        return builder.toString();
    }

    
}
