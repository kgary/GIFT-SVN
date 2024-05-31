/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.course.strategy.StrategySet;
import mil.arl.gift.common.course.transition.AbstractTransition;
import mil.arl.gift.common.course.transition.LearnerStateAttributeTransition;
import mil.arl.gift.common.course.transition.LearnerStateTransition;
import mil.arl.gift.common.course.transition.StateLogicalExpression;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;

/**
 * This class contains the strategies and transitions (i.e. actions) of interest during a course.
 * 
 * @author mhoffman
 *
 */
public class CourseActionKnowledge extends AbstractActionKnowledge{
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(CourseActionKnowledge.class);

    /**
     * Class constructor - set attributes from the generated class instance
     * 
     * @param actions - generated class object containing values for a course's actions element
     */
    public CourseActionKnowledge(generated.course.Actions actions){
        
        buildCourseActionKnowledge(actions.getInstructionalStrategies().getStrategy(), actions.getStateTransitions().getStateTransition());
    }
    
    /**
     * Create the domain action knowledge instance from the generated class's content provided
     * 
     * @param strategies - the list of strategies from a course XML
     * @param transitions - the list of transitions from a course XML
     */
    private void buildCourseActionKnowledge(List<generated.course.Strategy> strategies, List<generated.course.Actions.StateTransitions.StateTransition> transitions){
        
        //
        // Create GIFT Strategy objects
        //
        
        Map<String, StrategySet> strategyMap = new HashMap<>();
        for(generated.course.Strategy strategy : strategies){
            
            throw new IllegalArgumentException("Found unhandled strategy of "+strategy+" when building domain action knowledge.  An unhandled strategy means nothing will happen if that strategy is selected and that isn't exceptable.");
            
        }//end for
        
        
        //
        // Create GIFT transition objects
        //
        
        AbstractTransition giftTransition = null;
        List<List<AbstractStrategy>> tStrategies;
        globalTransitions = new ArrayList<LearnerStateTransition>();
        for(generated.course.Actions.StateTransitions.StateTransition transition : transitions){
            
            //collect the strategies referenced by this transition
            tStrategies = new ArrayList<List<AbstractStrategy>>();
            if(transition.getStrategyChoices() != null) {
                for(generated.course.StrategyRef strategyRef : transition.getStrategyChoices().getStrategyRef()){

                    if(strategyMap.containsKey(strategyRef.getName())){
                        StrategySet strategySet = strategyMap.get(strategyRef.getName());
                        tStrategies.add(strategySet.getStrategies());
                    }else{
                        throw new IllegalArgumentException("Unable to find the strategy named "+strategyRef.getName()+" referenced by a state transition (index = "+transitions.indexOf(transition)+")");
                    }
                }
            }
            
            //collect the state logical expression
            generated.course.Actions.StateTransitions.StateTransition.LogicalExpression logicalExpression = transition.getLogicalExpression();
            List<AbstractTransition> giftTransitions = new ArrayList<AbstractTransition>();
            for(Object transitionType : logicalExpression.getStateType()){

                if(transitionType instanceof  generated.course.LearnerStateTransitionEnum){
                    //has to be a learner state attribute transition
                    
                    generated.course.LearnerStateTransitionEnum enumTransitionType = (generated.course.LearnerStateTransitionEnum)transitionType;
                    LearnerStateAttributeNameEnum attrNameEnum = LearnerStateAttributeNameEnum.valueOf(enumTransitionType.getAttribute());
                    AbstractEnum prevEnum = AbstractEnum.valueOf(enumTransitionType.getPrevious(), attrNameEnum.getAttributeValues());
                    AbstractEnum currEnum = AbstractEnum.valueOf(enumTransitionType.getCurrent(), attrNameEnum.getAttributeValues());
                    
                    String label = enumTransitionType.getConcept();
                                    
                    giftTransition = new LearnerStateAttributeTransition(attrNameEnum, prevEnum, currEnum, label, logicalExpression.getStateType().size() > 1);
                    
                }else{
                    logger.error("There was a problem in creating a transition because the type of "+transitionType+" is not handled");
                    throw new IllegalArgumentException("There was a problem in creating a transition because the type is not handled.  Check the log for more details.");
                }
                
                giftTransitions.add(giftTransition);
                
            }            
            
            StateLogicalExpression stateLogicalExpression = new StateLogicalExpression(giftTransitions);
            globalTransitions.add(new LearnerStateTransition(transition.getName(), stateLogicalExpression, tStrategies));
            
        }//end for
        
        init(strategyMap, globalTransitions);
    }

}
