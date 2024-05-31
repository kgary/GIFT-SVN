/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.dkf.transition.PerformanceNodeTransition;
import mil.arl.gift.common.course.strategy.StrategySet;
import mil.arl.gift.common.course.transition.AbstractTransition;
import mil.arl.gift.common.course.transition.LearnerStateAttributeTransition;
import mil.arl.gift.common.course.transition.LearnerStateTransition;

/**
 * This is the base class that contains the strategies and transitions (i.e. actions) of interest during a course
 * and/or lesson.
 * 
 * @author mhoffman
 *
 */
public abstract class AbstractActionKnowledge {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(AbstractActionKnowledge.class);
    
    /** map of strategy unique names to activities */
    protected Map<String, StrategySet> strategyNameToActivities;
    
    /** map of unique object identifier (e.g. attribute name enum, performance node name) to transitions of interest for that identifier */
    protected Map<Object, List<LearnerStateTransition>> transitions;
    
    /** collection of all learner state transitions (each can contain a logical expression of state attribute transitions) from the domain knowledge */
    protected List<LearnerStateTransition> globalTransitions;
    
    /**
     * Default constructor - empty
     */
    public AbstractActionKnowledge(){
        
    }
    
    /**
     * Initialize the various collections used by this class, as well as mapping transitions to a unique identifier
     * of the learner state attribute of interest for that transition.
     *  
     * @param strategyNameToActivities - map containing a list of activities for each unique strategy name
     * @param giftLearnerTransitions - all of the learner state transitions
     */
    protected void init(Map<String, StrategySet> strategyNameToActivities, List<LearnerStateTransition> giftLearnerTransitions){
        
        this.strategyNameToActivities = Collections.unmodifiableMap(strategyNameToActivities); 
        
        //
        // Create map of learner state unique identifiers to the logical expressions that include that identifier for
        // easy lookup and evaluation when that state attribute changes.
        //
        
        Map<Object, List<LearnerStateTransition>> tempLearnerStateTransition =  new HashMap<Object, List<LearnerStateTransition>>();
        
        for(LearnerStateTransition learnerTransition : giftLearnerTransitions){
            for(AbstractTransition transition : learnerTransition.getTransitions()){
                
                Object key = null;
                if(transition instanceof LearnerStateAttributeTransition){
                    key = ((LearnerStateAttributeTransition)transition).getAttributeName();
                    
                }else if(transition instanceof PerformanceNodeTransition){
                    key = ((PerformanceNodeTransition)transition).getNodeId();
                    
                }else{
                    logger.error("found unhandled transition of "+transition+" while building domain action knowledge");
                    return;
                }
                
                List<LearnerStateTransition> mappedTransitions = tempLearnerStateTransition.get(key);
                if(mappedTransitions == null){
                    mappedTransitions = new ArrayList<>();
                }
                
                mappedTransitions.add(learnerTransition);
                
                tempLearnerStateTransition.put(key, mappedTransitions);
            }
        }
        
        this.transitions = Collections.unmodifiableMap(tempLearnerStateTransition);
    }
    
    /**
     * Return the unique strategy names.
     * 
     * @return the strategy names.  Can be empty.
     */
    public Set<String> getStrageyNames(){
        return Collections.unmodifiableSet(strategyNameToActivities.keySet());
    }
    
    /**
     * Return the activities for the strategy with the given name
     * 
     * @param name unique name of a strategy for the current domain
     * @return strategy activities. Can return null if a strategy of the given name doesn't exist.
     */
    public StrategySet getStrategyActivities(String name){
        return strategyNameToActivities.get(name);
    }
    
    /**
     * Return the transitions associated with the given key
     * 
     * @param key unique identifier of a transition
     * @return List<LearnerStateTransition>
     */
    public List<LearnerStateTransition> getTransitions(Object key){
        return transitions.get(key);
    }
    
    /**
     * Return the collection of all transitions in the domain knowledge
     * 
     * @return List<LearnerStateTransition>
     */
    public List<LearnerStateTransition> getTransitions(){
        return globalTransitions;
    }
}
