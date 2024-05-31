/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.ped.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.transition.LearnerStateTransition;
import mil.arl.gift.common.state.ConceptPerformanceState;
import mil.arl.gift.common.state.LearnerState;
import mil.arl.gift.common.state.PerformanceState;
import mil.arl.gift.common.state.TaskPerformanceState;

/**
 * This class will select the highest prioritized state transition of interest
 * from a list of transitions provided.
 * 
 * @author mhoffman
 *
 */
public class HighestPriorityTransitionSelection implements TransitionSelectionInterface {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(HighestPriorityTransitionSelection.class);

    @Override
    public void filter(LearnerState state, List<LearnerStateTransition> transitions){
        
        if(transitions == null){
            logger.warn("Unable to filter a transitions list that is null.");
            return;
        }else if(state == null){
            logger.warn("The learner state is needed for additional information for filtering, therefore it can't be null.");
            return;
        }
        
        //Note: right now only concept performance assessment nodes have priority values
        
        PerformanceState pState = state.getPerformance();
        Map<Integer, TaskPerformanceState> tasks = pState.getTasks();        
        
        //Build priority map
        // Key: performance node unique id (from DKF)
        // Value: current priority value for the node
        Map<Integer, Integer> nodeIdToPriority = new HashMap<>();
        for( Integer taskKey : tasks.keySet() ) {
            
            TaskPerformanceState taskPerformance = tasks.get(taskKey);
            
            //iterate over the performance for the subtasks (aka concepts)
            for( ConceptPerformanceState conceptPerformance : taskPerformance.getConcepts() ) {
                
                if(conceptPerformance.getState().getPriority() != null){
                    nodeIdToPriority.put(conceptPerformance.getState().getNodeId(), conceptPerformance.getState().getPriority());
                }
            }
        }
        
        if(nodeIdToPriority.isEmpty()){
            //there are no priorities set, therefore no filtering can take place
            logger.info("Unable to find any priority information, therefore nothing can be filtered based on this filter's logic.");
            return;
        }
        
        //Filter transitions by selecting first transition found with highest priority element
        int highestPriorityFound = Integer.MAX_VALUE;
        List<LearnerStateTransition> transitionsToRemove = new ArrayList<>();
        LearnerStateTransition transitionToKeep = null;
        boolean highestUpdated;
        for(LearnerStateTransition transition : transitions){
            
            highestUpdated = false;
            
            Set<Object> keys = transition.getLogicalExpression().getAtrributeKeys();
            for(Object key : keys){
                
                if(key instanceof Integer){
                    
                    Integer nodePriority = nodeIdToPriority.get(key);
                    
                    if(nodePriority != null && nodePriority < highestPriorityFound){
                        highestPriorityFound = nodePriority;
                        highestUpdated = true;
                        
                        //add old transition we thought we would keep to those transitions to be removed
                        if(transitionToKeep != null){
                            logger.info("Removing previously kept transition of "+transitionToKeep+" because a higher priority was found.");
                            transitionsToRemove.add(transitionToKeep);
                        }
                        
                        logger.info("Found new transition to keep of "+transition+" based on filtering logic.");
                        
                        transitionToKeep = transition;
                        break;
                    }
                }
            }
            
            if(!highestUpdated){
                //a new, higher priority, transition was not found in this current transition,
                //therefore remove it from the list
                
                transitionsToRemove.add(transition);
            }
        }        
        
        logger.debug("Filtering "+transitionsToRemove+" transitions from the provided list of "+transitions.size()+" transitions.");
        
        transitions.removeAll(transitionsToRemove);
    }
}
