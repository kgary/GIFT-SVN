/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.transition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.dkf.transition.PerformanceNodeTransition;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains a logical expression of learner state attribute transition(s).  The expression
 * should be provided learner states and can be evaluated at any given point. 
 * 
 * Note: currently all state attributes transitions are AND'ed together.  Eventually other operators will be supported.
 * 
 * @author mhoffman
 *
 */
public class StateLogicalExpression {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(StateLogicalExpression.class);

    /** list of all transitions in this expression */
    private List<AbstractTransition> transitions;
    
    /** quick reference to attribute transitions based on attribute key (e.g. performance node id, attribute name enum) */
    private Map<Object, List<AbstractTransition>> transitionMap =  new HashMap<Object, List<AbstractTransition>>();
    
    /**
     * Class constructor - set attribute
     * 
     * @param transitions - list of all transitions in this expression
     */
    public StateLogicalExpression(List<AbstractTransition> transitions){
        
        setTransitions(transitions);        
        buildMap();
    }
    
    private void setTransitions(List<AbstractTransition> transitions){
        
        if(transitions == null || transitions.isEmpty()){
            throw new IllegalArgumentException("The transitions list must contain at least one element.");
        }
        
        this.transitions = transitions;
    }
    
    /**
     * Build a quick reference map for when a learner state attribute is updated.  This allows only
     * those interested attribute transitions to be evaluated.
     */
    private void buildMap(){
        
        for(AbstractTransition transition : transitions){
            
            Object key = null;
            if(transition instanceof LearnerStateAttributeTransition){
                key = ((LearnerStateAttributeTransition)transition).getAttributeName();
                
            }else if(transition instanceof PerformanceNodeTransition){
                key = ((PerformanceNodeTransition)transition).getNodeId();
                
            }else{
                logger.error("found unhandled transition of "+transition+" while building transition map.");
                return;
            }
            
            List<AbstractTransition> mappedTransitions = transitionMap.get(key);
            if(mappedTransitions == null){
                mappedTransitions = new ArrayList<>();
            }
            
            mappedTransitions.add(transition);
            
            transitionMap.put(key, mappedTransitions);
        }
    }
    
    /**
     * Provide the various transitions of this logical expression the previous and current state values specified.
     * 
     * @param learnerAttributeKey - unique identifier of the attribute (e.g. performance node is the node id)
     * @param label - optional label that is paired with the learnerAttributeKey, this is usually a course concept.  
     * E.g. 'Knowledge' learner attribute with 'suppress OPFOR with well-aimed fire' course concept key.
     * @param previousValue - the previous attribute value.  Can be null if this is the first update.
     * @param previousValueTimestamp the time stamp at which the previous value was set.  Can be 0 if this is the first update.
     * @param currentValue - the current attribute value
     * @param currentValueTimestamp the time stamp at which the current value was set.
     */
    public void update(Object learnerAttributeKey, String label, AbstractEnum previousValue, long previousValueTimestamp, 
            AbstractEnum currentValue, long currentValueTimestamp){
        
        if(transitionMap.get(learnerAttributeKey) != null){
            
            // look at each transition expression in the state transition
            for(AbstractTransition transition : transitionMap.get(learnerAttributeKey)){
                
                if(StringUtils.equalsIgnoreCase(transition.getLabel(), label)) {
                    // the labels match - which can be null == null
                    transition.shouldTransition(previousValue, previousValueTimestamp, currentValue, currentValueTimestamp);
                }
            }
        }

    }
    
    /**
     * Return whether or not this expression is currently evaluating to true.
     * 
     * @return boolean
     */
    public boolean isTrue(){
        
        if(transitions.size() == 1) {
            
            /* For single transition, just check if the transition itself is satisfied */
            return transitions.get(0).isSatisfiedAndReset();
        
        } else {
        
            /* For multiple transitions, we need to consider both if a transition is currently satisfied
             * OR if it was previously satisfied and hasn't changed state since.
             * 
             * This distinction is important, since not considering whether a state was previously
             * satisfied can cause bugs where state transitions don't fire when intended (see #5528).
             * 
             * Consider if a state transition is authored to only proceed if the user presses 2 buttons.
             * When button 1 is pressed, we can't just leave isSatisfied true for the button 1 transition, 
             * since another state transition that only watches button 1 would fire constantly. What we 
             * can check instead, though is whether button 1 was pressed beforehand. This is what the 
             * isStillNotionallySatisfied() check accomplishes.
             * 
             * Even with this check, though, we need to make sure at least one transition is currently 
             * satisfied, otherwise the state transition watching both buttons would fire constantly 
             * once both had been pressed at any point. The atLeastOneCurrentlySatisfied 
             * check handles this .
             */
            boolean atLeastOneCurrentlySatisfied = false;
            for(AbstractTransition transition : transitions){
                
                boolean currentlySatisfied = transition.isSatisfied();
                if(!currentlySatisfied
                        && !transition.isStillNotionallySatisfied()){
                    return false;
                }
                
                atLeastOneCurrentlySatisfied |= currentlySatisfied;
            }
            
            if(!atLeastOneCurrentlySatisfied) {
                
                /* All transitions were already satisfied before, this expression
                 * is not actively satisfied  */
                return false;
            }
            
            return true;
        }
    }
    
    /**
     * Return the list of transitions involved in this logical expression.
     * 
     * @return List<AbstractTransition>
     */
    public List<AbstractTransition> getTransitions(){
        return transitions;
    }
    
    /**
     * Return the collection of attribute keys (e.g. performance node id) for the attributes
     * used by this state logical expression.
     * 
     * @return Set<Object> the collection of keys
     */
    public Set<Object> getAtrributeKeys(){
        return transitionMap.keySet();
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[StateLogicalExpression: ");
        
        sb.append("transitions = {");
        for(AbstractTransition transition : transitions){
            sb.append(" ").append(transition).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }
}
