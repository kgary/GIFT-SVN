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
import java.util.List;
import java.util.Set;

/**
 * A scorer for a survey
 *
 * @author jleonard
 */
public class SurveyScorer implements Serializable, HasAttributeScorers {

    private static final long serialVersionUID = 1L;

    private TotalScorer totalScorer;

    private Set<AttributeScorerProperties> attributeScorers = new HashSet<>();

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatibility
     */
    public SurveyScorer() {
    }

    /**
     * Constructor
     *
     * @param totalScorer The total scorer for the survey. Can be null.
     * @param attributeScorers The attribute scorer for the survey. Can be null.
     */
    public SurveyScorer(TotalScorer totalScorer, List<AttributeScorerProperties> attributeScorers) {

        this.totalScorer = totalScorer;
        this.attributeScorers.addAll(attributeScorers);
    }

    /**
     * Gets the total scorer for the survey
     *
     * @return TotalScorerModel The total scorer for the survey. Can be null.
     */
    public TotalScorer getTotalScorer() {

        return totalScorer;
    }

    @Override
    public Set<AttributeScorerProperties> getAttributeScorers() {
        
        return attributeScorers;
    }
    
    @Override
    public String toString(){
        
        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyScorer: ");
        sb.append("totalScorer = ").append(getTotalScorer());
        sb.append(", attributeScorers = ").append(getAttributeScorers());
        sb.append("]");
        return sb.toString();
    }

}
