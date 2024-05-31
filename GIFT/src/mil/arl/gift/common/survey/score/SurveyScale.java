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
 * Contains the attribute, value pairing associated with a survey score scale attribute.
 *
 * @author mhoffman
 */
public class SurveyScale extends AbstractScale {   

    /**
     * Class constructor - set attribute(s)
     * 
     * @param attribute - the attribute associated with the scale 
     * @param value - the attribute value
     * @param rawValue - the raw score value 
     */
    public SurveyScale(LearnerStateAttributeNameEnum attribute, AbstractEnum value, double rawValue) {
        super(attribute, rawValue);
        
        if(value == null){
            throw new IllegalArgumentException("The attribute value can't be null.");
        }
        this.value = value;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[SurveyScale: ");
        sb.append(super.toString());
        sb.append("]");

        return sb.toString();
    }

}
