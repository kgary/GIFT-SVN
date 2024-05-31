/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.aar;

import java.io.Serializable;

import mil.arl.gift.common.enums.AssessmentLevelEnum;

/**
 * An update describing changes that need to be saved to a score node
 * 
 * @author nroberts
 */
public class ScoreNodeUpdate implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /** The assessment level of the score node */
    private AssessmentLevelEnum assessment;
 
    /** The username of the observer controller that evaluated the score node */
    private String evaluator;
    
    /**
     * An optional reference to a media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * attached to this score node's assessment by an observer controller (OC). This can originate from the GIFT Game master UI.
     */
    private String observerMedia;
    
    /** 
     * An, optional, observer controller (OC) comment associated with this score node's assessment.  
     * This can originate from the GIFT Game master UI.
     */
    private String observerComment;
    
    /**
     * A default, no-argument constructor required by GWT serialization
     */
    private ScoreNodeUpdate() {}
    
    /**
     * Creates a new score node update with the given assessment level
     * 
     * @param assessment the assessment level to give the update. Can be null.
     */
    public ScoreNodeUpdate(AssessmentLevelEnum assessment) {
        this();
        
        setAssessment(assessment);
    }
    
    /**
     * Gets the username of the observer controller who evaluated this score node's assessment
     * 
     * @return the evaluator's username. Can be null.
     */
    public String getEvaluator() {
        return evaluator;
    }

    /**
     * Sets the username of the observer controller who evaluated this score node's assessment
     * 
     * @param evaluator the evaluator's username. Can be null.
     */
    public void setEvaluator(String evaluator) {
        this.evaluator = evaluator;
    }

    /**
     * Gets the reference to a media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * attached to this score node's assessment by an observer controller (OC). This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide non-textual information about something that was witnessed during the lesson.<br/>
     * 
     * @return an observer controller (OC) media file reference. Can be null.
     */
    public String getObserverMedia() {
        return observerMedia;
    }

    /**
     * Sets the reference to a media file (such as a recording [e.g. "recorder/59f3c5bc-03f7-4885-ab88-a2e56ab319e9.wav"]) 
     * attached to this score node's assessment by an observer controller (OC). This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide non-textual information about something that was witnessed during the lesson.<br/>
     * 
     * @param observerMedia an observer controller (OC) media file reference. Can be null.
     */
    public void setObserverMedia(String observerMedia) {
        this.observerMedia = observerMedia;
    }

    /**
     * Return the observer controller (OC) comment associated with this score node's assessment.  
     * This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide some information about something that was witnessed during the lesson.<br/>
     * 
     * @return an observer controller (OC) comment. Can be null.
     */
    public String getObserverComment() {
        return observerComment;
    }

    /**
     * Set the observer controller (OC) comment associated with this score node's assessment.  
     * This can originate from the GIFT Game master UI.<br/>
     * The purpose is to allow the observer to provide some information about something that was witnessed during the lesson.<br/>
     * 
     * @param observerComment an observer controller (OC) comment. Can be null.
     */
    public void setObserverComment(String observerComment) {
        this.observerComment = observerComment;
    }

    /**
     * Gets the assessment level assigned to the score node
     * 
     * @return the assessment level. Can be null.
     */
    public AssessmentLevelEnum getAssessment() {
        return assessment;
    }

    /**
     * Sets the assessment level to assign to the score node
     * 
     * @param assessment the assessment level. Can be null.
     */
    public void setAssessment(AssessmentLevelEnum assessment) {
        this.assessment = assessment;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ScoreNodeUpdate: assessment=");
        builder.append(assessment);
        builder.append(", evaluator=");
        builder.append(evaluator);
        builder.append(", observerMedia=");
        builder.append(observerMedia);
        builder.append(", observerComment=");
        builder.append(observerComment);
        builder.append("]");
        return builder.toString();
    }
    
    
}
