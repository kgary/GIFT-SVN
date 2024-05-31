/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This class contains information about assessments of a learner for a domain.
 * 
 * @author mhoffman
 *
 */
public class PerformanceAssessment {
    
    /** the tasks being assessed */
    private Map<Integer, TaskAssessment> nameToTask;
    
    /** The username of the person making the request */
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
    
    /**
     * Class constructor - set attributes
     * 
     * @param tasks - root level task assessments of this performance assessment
     */
    public PerformanceAssessment(List<TaskAssessment> tasks){
        
        nameToTask = new HashMap<Integer, TaskAssessment>(tasks.size());
        for(TaskAssessment tAss : tasks){
            nameToTask.put(tAss.getNodeId(), tAss);
        }
    }
    
    /**
     * Class constructor - empty task assessments
     */
    public PerformanceAssessment(){
        this(new ArrayList<TaskAssessment>(0));        
    }
    
    /**
     * Return the task assessments for this performance assessment
     * 
     * @return Collection<TaskAssessment>
     */
    public Collection<TaskAssessment> getTasks(){
        return nameToTask.values();
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
     * Update the performance assessments task assessment
     * 
     * @param assessment - the updated task assessment
     */
    public void updateTaskAssessment(TaskAssessment assessment){
        
        nameToTask.put(assessment.getNodeId(), assessment);
    }   

    @Override
    public int hashCode() {
        return Objects.hash(evaluator, nameToTask, observerComment, observerMedia);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PerformanceAssessment other = (PerformanceAssessment) obj;
        return Objects.equals(evaluator, other.evaluator) && Objects.equals(nameToTask, other.nameToTask)
                && Objects.equals(observerComment, other.observerComment)
                && Objects.equals(observerMedia, other.observerMedia);
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PerformanceAssessment: ");
        sb.append("evaluator = ").append(evaluator);
        sb.append(", observerComment = ").append(observerComment);
        sb.append(", observerMedia = ").append(observerMedia);
        sb.append(", tasks = {");
        for(TaskAssessment task : getTasks()){
            sb.append("\n").append(task).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
