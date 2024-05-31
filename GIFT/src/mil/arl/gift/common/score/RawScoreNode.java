/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.score;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.ScoreNodeTypeEnum;
import mil.arl.gift.common.util.CollectionUtils;

/**
 * Class to encapsulate a RawScore with an assessment.
 * 
 * @author cragusa
 */

public class RawScoreNode extends AbstractScoreNode implements Serializable {
    
    /** default */
    private static final long serialVersionUID = -6223920865772189757L;

    /** who is being scored in this node */
    private Set<String> usernames;
        
    /** details of the score */
    private RawScore rawScore;
    
    /**
     * Default constructor -- needed for gwt serialization.
     */
    @SuppressWarnings("unused")
    private RawScoreNode() {

    }
    
    /**
     * Constructor
     * 
     * @param name the name of the node. Null values are illegal.
     * @param rawScore the rawScore for the node. Null values are illegal. Can't be null.
     * @param assessment the assessment value for this node. Can't be null.
     */
    public RawScoreNode(String name, RawScore rawScore, AssessmentLevelEnum assessment) {
        super(name, assessment);
        
        if(rawScore == null) {            
            throw new IllegalArgumentException("The raw score can't be null.");
        }
        this.rawScore = rawScore;
        
        this.usernames = null;
    }
    
    /**
     * Constructor
     * 
     * @param name the name of the node. Null values are illegal.
     * @param rawScore the rawScore for the node. Null values are illegal.
     * @param assessment the assessment value for this node. Can't be null.
     * @param usernames who is being scored at this node.  Can't be null or empty.
     */
    public RawScoreNode(String name, RawScore rawScore, AssessmentLevelEnum assessment, Set<String> usernames) {
        this(name, rawScore, assessment);
        
        if(usernames == null || usernames.isEmpty()){
            throw new IllegalArgumentException("The usernames can't be null or empty");
        }
        
        this.usernames = usernames;
    }
    
    /**
     * Returns a new instance copy of the {@link RawScoreNode} provided.
     * @param original the {@link RawScoreNode} to copy.  If null then null is returned.
     * @return the new {@link RawScoreNode} 
     */
    public static RawScoreNode deepCopy(RawScoreNode original){
        
        if(original == null){
            return null;
        }

        /* For some reason the RawScoreNode constructor that takes the username
         * collection doesn't allow null. Use the other constructor for this
         * case. */
        RawScoreNode copy;
        if (CollectionUtils.isEmpty(original.getUsernames())) {
            copy = new RawScoreNode(original.getName(), original.getRawScore(), original.getAssessment());
        } else {
            copy = new RawScoreNode(original.getName(), original.getRawScore(), original.getAssessment(),
                    new HashSet<>(original.getUsernames())); /* Copy usernames to new set, since UnmodifiableSet cannot be used client-side */
        }

        copy.setParent(original.getParent());
        if(original.getPerformanceNodeId() != null){
            copy.setPerformanceNodeId(original.getPerformanceNodeId());
        }
        
        return copy;
    }
    
    /**
     * Return who is being scored at this node.
     * 
     * @return a READ-ONLY collection of (currently) GIFT usernames.  Can be null but not empty.
     */
    public Set<String> getUsernames(){
        if(usernames == null){
            return null;
        }else{
            return Collections.unmodifiableSet(usernames);
        }
    }
    
    /**
     * Get the RawScore for this node
     * @return the rawScore, won't be null.
     */
    public RawScore getRawScore() {
        return rawScore;
    }
    
    @Override
    protected String toDisplayString(String indent) {
        
        StringBuffer buffer = new StringBuffer();
        
        buffer.append(super.toDisplayString(indent));
        buffer.append(": ").append(rawScore.toDisplayString());
        buffer.append(" [").append(getAssessment().getDisplayName()).append("]");
        
        return buffer.toString();
    }
    
    @Override
    public String toDisplayString() {
        return toDisplayString("");
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
    
    @Override
    public ScoreNodeTypeEnum getType() {
        return ScoreNodeTypeEnum.RAW_SCORE_NODE;
    }    
    
    @Override
    public boolean isValid() {      
        return true; 
        //or should it be this: return (getParent() != null && rawScore != null && assessment != null);
    }
    
    @Override
    public String toString() {
        
        StringBuffer buffer = new StringBuffer();        
        buffer.append(super.toString());
        buffer.append(", users = ").append(getUsernames());
        buffer.append(", ").append(this.getRawScore());
        return buffer.toString();
    }
    
	
}
