/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.learner.clusterer;

import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.enums.SensorAttributeNameEnum;

/**
 * This is a generic three state classifier for the learner model.  The generic portion provides a means to set
 * the various enumerated values for each of the 3 'bins' of the classifier.
 * 
 * @author mhoffman
 *
 */
public class GenericThreeStateClassifier extends AbstractThreeStateClassifier {

    /**
     * Class Constructor - set attributes
     * 
     * @param stateAttribute - the state attribute being assigned a value
     * @param lowLevelValue - the state attribute value for when the sensor attribute value is considered in the low state
     * @param mediumLevelValue - the state attribute value for when the sensor attribute value is considered in the medium state
     * @param highLevelValue - the state attribute value for when the sensor attribute value is considered in the high state
     * @param unknownLevelValue - the state attribute value for when the sensor attribute value is considered in the unknown state
     */
    public GenericThreeStateClassifier(LearnerStateAttributeNameEnum stateAttribute, 
            AbstractEnum lowLevelValue, AbstractEnum mediumLevelValue, AbstractEnum highLevelValue, AbstractEnum unknownLevelValue){
        super(stateAttribute, lowLevelValue, mediumLevelValue, highLevelValue, unknownLevelValue);
        
    }
    
    /**
     * Class Constructor - set attributes
     * 
     * @param sensorAttribute - the sensor attribute whose values are being classified
     * @param stateAttribute - the state attribute being assigned a value
     * @param lowLevelValue - the state attribute value for when the sensor attribute value is considered in the low state
     * @param mediumLevelValue - the state attribute value for when the sensor attribute value is considered in the medium state
     * @param highLevelValue - the state attribute value for when the sensor attribute value is considered in the high state
     * @param unknownLevelValue - the state attribute value for when the sensor attribute value is considered in the unknown state
     */
    public GenericThreeStateClassifier(SensorAttributeNameEnum sensorAttribute, LearnerStateAttributeNameEnum stateAttribute, 
            AbstractEnum lowLevelValue, AbstractEnum mediumLevelValue, AbstractEnum highLevelValue, AbstractEnum unknownLevelValue){
        super(sensorAttribute, stateAttribute, lowLevelValue, mediumLevelValue, highLevelValue, unknownLevelValue);
        
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[GenericThreeStateClassifier:");
        sb.append(super.toString());
        sb.append("]");
        return sb.toString();
    }
}
