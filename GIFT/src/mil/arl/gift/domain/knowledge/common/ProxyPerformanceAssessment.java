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

/**
 * This class contains information about assessments of a learner for a domain.
 * This particular performance assessment class contains the unique ids of the task
 * nodes it contains, as well as observer comment and media for use with global bookmarks.
 * 
 * @author mhoffman
 *
 */
public class ProxyPerformanceAssessment {
    
    /** the tasks being assessed */
    private List<UUID> taskNodeCourseIds;
    
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
     * @param taskNodeCourseIds - root level task assessments of this performance assessment
     */
    public ProxyPerformanceAssessment(List<UUID> taskNodeCourseIds){
        
        if(taskNodeCourseIds == null){
            throw new IllegalArgumentException("The task node ids list can't be null.");
        }
        
        this.taskNodeCourseIds = taskNodeCourseIds;        
    }
    
    /**
     * Return the task assessments for this performance assessment
     * 
     * @return List<UUID>
     */
    public List<UUID> getTasks(){
        return taskNodeCourseIds;
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
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PerformanceAssessment: ");
        sb.append(" evaluator = ").append(this.getEvaluator());
        sb.append(", observerComment = ").append(this.getObserverComment());
        sb.append(", observerMedia = ").append(this.getObserverMedia());
        
        sb.append(", tasks = {");
        for(UUID taskId : getTasks()){
            sb.append(" ").append(taskId).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
