/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the base score class for a survey element that, when scored, provides
 * one or more scales or attributes.
 *
 * @author mhoffman
 */
public abstract class AbstractScaleScore implements ScoreInterface {

    /** collection of scales for this score */
    private List<AbstractScale> scales;

    /**
     * Class constructor - empty
     */
    public AbstractScaleScore() {

        scales = new ArrayList<>();
    }

    /**
     * Return the collection of scale values
     *
     * @return List<AbstractScale> Gets the collection of scale values.  Can be empty but not null.
     */
    public List<AbstractScale> getScales() {

        return scales;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        sb.append(" scales = {");
        for (AbstractScale pair : scales) {

            sb.append(pair).append(", ");
        }
        sb.append("}");

        return sb.toString();
    }
}
