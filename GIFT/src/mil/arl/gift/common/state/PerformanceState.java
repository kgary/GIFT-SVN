/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class contains the performance state of a learner state
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class PerformanceState implements Serializable{
    
    /** collection of task performance states (key: task performance node id) */
    private Map<Integer, TaskPerformanceState> tasks;
    
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
     * Default constructor
     */
    public PerformanceState(){
        tasks = new HashMap<Integer, TaskPerformanceState>();
    }

    /**
     * Default constructor
     * 
     * @param tasks the collection of task performance states (key: task performance node id)
     */
    public PerformanceState(Map<Integer, TaskPerformanceState> tasks){
        
        if(tasks == null){
            throw new IllegalArgumentException("The tasks collection can't be null.");
        }
        this.tasks = tasks;
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
     * Return the collection of task performance states (key: task performance node id)
     * 
     * @return tasks. Can't be null.
     */
    public Map<Integer, TaskPerformanceState> getTasks(){
        return tasks;
    }
    
    /**
     * Return true if the two objects have the same measurements.
     * 
     * @param state - the state to compare this state too
     * @return boolean - true iff the objects have equal measurements
     */
    public boolean equals(PerformanceState state){
        
        if(state == null){
            return false;
        }
        
        //check Evaluator
        String otherEvaluator = state.getEvaluator();
        String thisEvaluator = this.getEvaluator();
        
        if (!Objects.equals(thisEvaluator, otherEvaluator)) {
            return false;
        }
        
        //check observerComment
        String otherComment = state.getObserverComment();
        String thisComment = this.getObserverComment();
        
        if (!Objects.equals(thisComment, otherComment)) {
            return false;
        }
                
        //check observerMedia
        String otherMedia = state.getObserverMedia();
        String thisMedia = this.getObserverMedia();
        
        if (!Objects.equals(thisMedia, otherMedia)) {
            return false;
        }
        
        //check tasks
        Map<Integer, TaskPerformanceState> otherTStates = state.getTasks();
        Map<Integer, TaskPerformanceState> thisTStates = this.getTasks();
        
        if(otherTStates.size() != thisTStates.size()){
            return false;
        }
        
        for(Integer key : thisTStates.keySet()){
         
            TaskPerformanceState otherTState = otherTStates.get(key);
            TaskPerformanceState thisTState = otherTStates.get(key);
            
            if(!thisTState.equals(otherTState)){
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Return the time stamp (epoch) at which the observer started to give an observation (e.g. comment, assessment).
     * Looks at all tasks and concepts.
     * @param the time at which some observation that is included in this assessment was started
     * by the observer.  Will be null if not set at any level in this state.
     */
    public Long getObservationStartedTime(){
        
        Long obsStartTime = null;
        for(TaskPerformanceState taskPerfState : tasks.values()){
            
            obsStartTime = taskPerfState.getObservationStartedTime();
            if(obsStartTime != null){
                break;
            }
        }
        
        return obsStartTime;
    }
    
    /**
     * Return whether this performance state has no real information populated at this time.
     * @return true if this is a blank performance state object, i.e. nothing has been provided.
     */
    public boolean isEmpty(){        
        return evaluator == null && observerComment == null && observerMedia == null && (tasks == null || tasks.isEmpty());
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[PerformanceState: ");
        sb.append("evaluator = ").append(evaluator);
        sb.append(", observerComment = ").append(observerComment);
        sb.append(", observerMedia = ").append(observerMedia);
        sb.append(", tasks = {");
        for(TaskPerformanceState state : tasks.values()){
            sb.append("\n").append(state).append(",");
        }
        sb.append("}");
        
        sb.append( "]");
        
        return sb.toString();
    }
}
