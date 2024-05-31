/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.state;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This is the base class for learner state class that don't require a unique learner state implementation or hierarchy.  This class 
 * contains learner state attributes.
 * 
 * @author mhoffman
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractLearnerState implements Serializable {

    /** collection of attributes for this learner state */
    protected Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> attributes;
    
    /**
     * Class constructor
     */
    public AbstractLearnerState(){
        attributes = new HashMap<LearnerStateAttributeNameEnum, LearnerStateAttribute>();
    }
    
    /**
     * Class constructor - set attributes
     * 
     * @param attributes collection of attributes for this learner state. Can't be null.
     */
    public AbstractLearnerState(Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> attributes){
        if(attributes == null){
            throw new IllegalArgumentException("The attribute map can't be null");
        }
        this.attributes = attributes;
    }
    
    /**
     * Return the collection of attributes for this learner state
     * @return won't be null.
     */
    public Map<LearnerStateAttributeNameEnum, LearnerStateAttribute> getAttributes(){
        return attributes;
    }
    
    /**
     * Return whether this state has no real information populated at this time.
     * @return true if this is a blank state object, i.e. nothing has been provided.
     */
    public boolean isEmpty(){        
        return attributes == null || attributes.isEmpty();
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[AbstractLearnerState: ");

        sb.append(", attributes = {");
        for(LearnerStateAttribute attribute : attributes.values()){
            sb.append(attribute).append(", ");
        }
        sb.append("}");
        
        sb.append("]");
        
        return sb.toString();
    }
}
