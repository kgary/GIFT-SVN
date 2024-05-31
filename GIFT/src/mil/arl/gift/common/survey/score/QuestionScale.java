/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.survey.score;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * Contains the attribute, value pairing associated with a survey question score scale attribute.
 *
 * @author mhoffman
 */
public class QuestionScale extends AbstractScale {   

    /**
     * Class constructor - set attribute(s)
     * 
     * @param attribute - the attribute associated with the scale 
     * @param rawValue - the raw score value 
     */
    public QuestionScale(LearnerStateAttributeNameEnum attribute, double rawValue) {
        super(attribute, rawValue);
    }
    
    /**
     * Set the attribute value for the scale
     * 
     * @param value enumerated value associated with the attribute 
     */
    public void setValue(AbstractEnum value){        
        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[QuestionScale: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }

}
