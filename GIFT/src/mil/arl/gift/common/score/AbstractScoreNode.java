/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;

import java.io.Serializable;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ScoreNodeTypeEnum;

/**
 * ScoreNode base class with partial implementation.
 * 
 * @author cragusa
 *
 */
public abstract class AbstractScoreNode implements Serializable {

    /** serialization id */
    private static final long serialVersionUID = 2768691717118594903L;

    /** delimeter character(s) to use between the tokens for the path to this node in the hierarchy */
    private static final String WRITE_DELIMETER = "|";
    
    /** used to parse the write delimeter */
    @SuppressWarnings("unused")
    private static final String PARSE_DELIMETER = "\\|";    
    
    /** the display name of this score node */
    private String name;
    
    /** (optional) the parent score node to this node */
    private AbstractScoreNode parent;
    
    /** (optional) the performance node id associated with this score node. */
    private Integer performanceNodeId;

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
    
    /** the assessment value for this node */
    private AssessmentLevelEnum assessment = AssessmentLevelEnum.UNKNOWN;  
    
    /**
     * Default constructor -- needed for gwt serialization.
     */
    protected AbstractScoreNode() {
    }
    
    /**
     * Constructor
     * 
     * @param name the name of this node. Can't be null.
     * @param assessment value for this node.  Can't be null.
     */
    protected AbstractScoreNode(String name, AssessmentLevelEnum assessment) {
        
        if(name == null) {            
            throw new IllegalArgumentException("The name can't be null");
        }
        
        this.name = name;          
        
        setAssessment(assessment);
    } 
    
    /**
     * Set the assessment value for this node
     * @param assessment can't be null
     */
    protected void setAssessment(AssessmentLevelEnum assessment) {
        if(assessment == null) {            
            throw new IllegalArgumentException("The assessment can't be null.");
        }
        
        this.assessment = assessment;
    }
    
    /**
     * Get the assessment for this node.
     * @return The assessment for this node.  Won't be null.  Default is {@link AssessmentLevelEnum.UNKNOWN}
     */
    public AssessmentLevelEnum getAssessment() {
        return assessment;
    }

    /**
     * Gets the name of this node.
     * 
     * @return The name of this node.  Won't be null.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Set the performance node id associated with this score node.
     * 
     * @param performanceNodeId - unique id of a performance node.  Must be greater than 0.
     */
    public void setPerformanceNodeId(Integer performanceNodeId){
        
        if(performanceNodeId == null || performanceNodeId < 0){            
            throw new IllegalArgumentException("The performance node id must be non-negative.");
        }
        
        this.performanceNodeId = performanceNodeId;
    }
    
    /**
     * Return the performance node id associated with this score node.
     * 
     * @return unique id of a performance node.  Will be null if this score is not associated with 
     * a task or concept.  Otherwise will be greater than 0.
     */
    public Integer getPerformanceNodeId(){
        return performanceNodeId;
    }

    /**
     * Get the fully-qualified name of this node.
     * Currently we don't use the fully qualified name for anything. 
     * @return a String containing the full node name, which is a delimited string of all the node names back to the root node.
     */
    public String getFullName() {
        
        StringBuffer fullName = new StringBuffer();
        
        AbstractScoreNode parent = getParent();
           
        if(parent != null) {            
            fullName.append(parent.getFullName()).append(WRITE_DELIMETER).append(name);
        }else {            
            fullName.append(name);
        }
        
        return fullName.toString();
    }
    
    /**
     * Sets a link to the parent node. Used by getFullName to construct the full name.
     * @param parent the parent to this node
     */
    protected void setParent(AbstractScoreNode parent) {
        this.parent = parent;
    }
    
    /**
     * Gets the parent node.
     * @return a reference to the parent node. Value will be null if node is root. 
     * (NOTE: The node is still a root node if it hasn't been added to a parent.)
     */
    public AbstractScoreNode getParent() {
        return parent;
    }
    
    /**
     * Determine if this node is a root node.
     * @return false if this node has a parent, otherwise false.
     */
    public boolean isRoot() {        
        return (parent == null);
    }
    
    /** 
     * Convenience method to determine if this node has children.
     * 
     * @return true if the node either cannot have children, or if the node *can* have children but doesn't have any. Otherwise return false.
     */
    public abstract boolean isLeaf();
    
    /**
     * Return a display name for this score node.
     * 
     * @param indent added to the beginning of the display string for this score node, could be
     * used to indent or prefix the returned string.
     * @return the display string for this score node.
     */
    protected String toDisplayString(String indent) {
        
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(indent).append(getName());
        //buffer.append(prefix).append(", type:").append(this.getType().getDisplayName());
        
        return buffer.toString();        
    }    
        
    /**
     * @return a user-friendly display string.
     */
    public abstract String toDisplayString();
    
    /**
     * Returns the type of this node.
     * 
     * @return the type of the node.
     */
    public abstract ScoreNodeTypeEnum getType();    
    
    /**
     * Validation method for completed nodes.
     * 
     * This is a recursive method.  A node with one or more invalid children is also invalid.
     * 
     * @return true if this node and all its children are valid
     */
    public abstract boolean isValid();    
    
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
    
    @Override
    public String toString() {
        
        StringBuffer sb = new StringBuffer();
        sb.append("name = \"").append(getName());
        sb.append("\", assessment = ").append(getAssessment()); 
        sb.append(", type = \"").append(getType().getDisplayName());
        sb.append("\", id = ").append(getPerformanceNodeId());
        sb.append(", observerMedia = \"").append(observerMedia);
        sb.append("\", observerComment = \"").append(observerComment);
        sb.append("\", evaluator = \"").append(evaluator);
        sb.append("\"");
        
        return sb.toString();
    }
    
}