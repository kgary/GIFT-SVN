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
 * A scorer for the total
 *
 * @author jleonard
 */
public class TotalScorer implements Serializable, HasAttributeScorers {

    private static final long serialVersionUID = 1L;

    private Set<AttributeScorerProperties> attributeModels = new HashSet<AttributeScorerProperties>();

    /**
     * Default Constructor
     *
     * Required to exist and be public for GWT compatibility
     */
    public TotalScorer() {
    }

    /**
     * Constructor
     *
     * @param attributeModels The attribute models
     */
    public TotalScorer(List<AttributeScorerProperties> attributeModels) {

        this.attributeModels.addAll(attributeModels);
    }

    @Override
    public Set<AttributeScorerProperties> getAttributeScorers() {

        return attributeModels;
    }
    
    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[TotalScorer: ");
        sb.append(" properties = {");
        for (AttributeScorerProperties properties : attributeModels) {
            sb.append(" ").append(properties).append(",");
        }
        sb.append("}");
        sb.append("]");

        return sb.toString();
    }
}
