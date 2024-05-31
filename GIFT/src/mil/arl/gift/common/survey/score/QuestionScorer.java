/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * A scorer for a question
 *
 * @author jleonard
 */
public class QuestionScorer implements Serializable, HasAttributeScorers {
    
    private static final long serialVersionUID = 1L;
    
    private boolean totalQuestion;
    
    private Set<AttributeScorerProperties> attributeScorers;

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatability
     */
    public QuestionScorer() {
    }

    /**
     * Constructor
     *
     * @param totalQuestion whether this question should be included with total scoring
     * @param attributeScorers The collection of attribute scorers for the
     * question, can be empty but not null
     */
    public QuestionScorer(boolean totalQuestion, Set<AttributeScorerProperties> attributeScorers) {
        
        this.totalQuestion = totalQuestion;
        setAttributeScorers(attributeScorers);
        
    }

    /**
     * Gets if this question should be included with total scoring
     *
     * @return Boolean If this question should be included with total scoring
     */
    public boolean getTotalQuestion() {
        
        return totalQuestion;
    }
    
    /**
     * Sets if the question should be included with total scoring.
     * @param total True if the question will be included with total scoring, false otherwise.
     */
    public void setTotalQuestion(boolean total) {
        totalQuestion = total;
    }

    @Override
    public Set<AttributeScorerProperties> getAttributeScorers() {
        
        return attributeScorers;
    }

    /**
     * Gets an attribute scorer of a specific attribute
     *
     * @param learnerStateEnum The attribute to get a scorer for
     * @return AttributeScorerModel The scorer for the attribute, null if one
     * does not exist
     */
    public AttributeScorerProperties getAttributeScorer(LearnerStateAttributeNameEnum learnerStateEnum) {
        
        if (learnerStateEnum != null) {
            
            for (AttributeScorerProperties scorerModel : getAttributeScorers()) {
                
                if (learnerStateEnum.equals(scorerModel.getAttributeType())) {
                    
                    return scorerModel;
                }
            }
        }
        
        return null;
    }  
    
    /**
     * Sets the attribute scorer collection
     * 
     * @param attributes The collection of attribute scorers for the
     * question, can be empty but not null
     */
    public void setAttributeScorers(Set<AttributeScorerProperties> attributes){
    	if(attributes == null){
            throw new IllegalArgumentException("The attribute scorers can't be null.");
        }
        this.attributeScorers = new HashSet<AttributeScorerProperties>(attributes);
    }
}
