/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.util.Map;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This class contains the affective state of a learner state
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public class AffectiveState extends AbstractLearnerState {

    /**
     * Default constructor
     */
    public AffectiveState(){
        super();
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param attributes - collection of affective attributes for this learner state 
     */
    public AffectiveState(Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> attributes){
        super(attributes);
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AffectiveState: ");
        sb.append(super.toString());
        sb.append("]");
        
        return sb.toString();
    }
}
