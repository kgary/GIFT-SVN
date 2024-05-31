/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.util.Set;

/**
 * An interface for scoring classes that contains attribute scorers that
 * provides a method for getting the scorers
 *
 * @author jleonard
 */
public interface HasAttributeScorers {

    /**
     * Gets the attribute scorers
     *
     * @return The collection of attribute scorers. Can be null.
     */
    public Set<AttributeScorerProperties> getAttributeScorers();
}
