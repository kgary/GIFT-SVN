/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.dkf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.course.AbstractActionKnowledge;
import mil.arl.gift.common.course.dkf.transition.PerformanceNodeTransition;
import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.course.strategy.DoNothingStrategy;
import mil.arl.gift.common.course.strategy.StrategySet;
import mil.arl.gift.common.course.transition.AbstractTransition;
import mil.arl.gift.common.course.transition.LearnerStateAttributeTransition;
import mil.arl.gift.common.course.transition.LearnerStateTransition;
import mil.arl.gift.common.course.transition.StateLogicalExpression;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.enums.AssessmentLevelEnum;
import mil.arl.gift.common.enums.LearnerStateAttributeNameEnum;
import mil.arl.gift.common.util.StringUtils;

/**
 * This class contains the strategies and transitions (i.e. actions) of interest during a lesson.
 *
 * @author mhoffman
 *
 */
public class DomainActionKnowledge extends AbstractActionKnowledge {

    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(DomainActionKnowledge.class);

    /** DKF actions element containing action knowledge */
    private generated.dkf.Actions actions;

    /**
     * Class constructor - set attribute(s) and build DKF action knowledge objects.
     *
     * @param actions - DKF actions element containing action knowledge
     * @param placesOfInterest - contains the places of interest for this domain knowledge. Can be null or empty.
     */
    public DomainActionKnowledge(generated.dkf.Actions actions, 
            generated.dkf.PlacesOfInterest placesOfInterest){

        if(actions == null){
            throw new IllegalArgumentException("The actions can't be null");
        }

        this.actions = actions;
        buildDKFActionKnowledge(actions.getInstructionalStrategies().getStrategy(), actions.getStateTransitions().getStateTransition(),
                placesOfInterest);
    }

    /**
     * Return the DKF actions element containing action information (e.g. strategies and state transitions)
     *
     * @return the actions portion of the domain knowledge configuration.
     */
    public generated.dkf.Actions getActions(){
        return actions;
    }

    /**
     * Create the domain action knowledge instance from the generated class's content provided
     *
     * @param strategies these are the tactics for strategies requested by the pedagogical module
     * @param transitions contains learner state transitions (both performance, affective and cognitive based) to watch for in a simplistic
     * rule based pedagogy system.
     * @param placesOfInterest - contains the places of interest for this domain knowledge. Can be null or empty.
     */
    private void buildDKFActionKnowledge(List<generated.dkf.Strategy> strategies, 
            List<generated.dkf.Actions.StateTransitions.StateTransition> transitions,
            generated.dkf.PlacesOfInterest placesOfInterest) throws IllegalArgumentException{

        //
        // Create GIFT Strategy objects
        // - Also populate coordinates for places of interest references (e.g. {@link generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo})
        //

        Map<String, StrategySet> strategyMap = new HashMap<>();
        for (generated.dkf.Strategy strategy : strategies) {
            String strategyName = strategy.getName();
            if (strategyMap.containsKey(strategyName)) {
                throw new IllegalArgumentException("Found duplicated strategy name of '" + strategyName
                + "'. Strategy names must be unique.");
            }
            
            //
            // Custom handling for places of interest references - populate coordinates base on place of interest reference
            // 
            if(placesOfInterest != null && !placesOfInterest.getPointOrPathOrArea().isEmpty()){
                for(Serializable activity : strategy.getStrategyActivities()){
                    
                    if(activity instanceof generated.dkf.ScenarioAdaptation){
                        generated.dkf.ScenarioAdaptation scenarioAdaptation = (generated.dkf.ScenarioAdaptation)activity;
                        generated.dkf.EnvironmentAdaptation envAdaptation = scenarioAdaptation.getEnvironmentAdaptation();
                        if(envAdaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs){
                            generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs createBreadcrumbs = 
                                    (generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs)envAdaptation.getType();
                            
                            generated.dkf.EnvironmentAdaptation.CreateBreadcrumbs.LocationInfo locationInfo = 
                                    createBreadcrumbs.getLocationInfo();
                            
                            if(locationInfo.getCoordinate().isEmpty()){
                                // the coordinate(s) needs to be populated from the place of interest reference
                            
                                String poiRef = locationInfo.getPlaceOfInterestRef();
                                for(Serializable pointPathOrArea : placesOfInterest.getPointOrPathOrArea()){
                                    
                                    // only support bread crumbs for point or path
                                    if(pointPathOrArea instanceof generated.dkf.Point){
                                        generated.dkf.Point point = (generated.dkf.Point)pointPathOrArea;
                                        if(StringUtils.equals(point.getName(), poiRef)){
                                            locationInfo.getCoordinate().add(point.getCoordinate());
                                            break;
                                        }
                                    }else if(pointPathOrArea instanceof generated.dkf.Path){
                                        generated.dkf.Path path = (generated.dkf.Path)pointPathOrArea;
                                        if(StringUtils.equals(path.getName(), poiRef)){
                                            
                                            for(generated.dkf.Segment segment : path.getSegment()){
                                             
                                                if(locationInfo.getCoordinate().isEmpty()){
                                                    // only grab the start element of the first segment, the other start elements
                                                    // of the remaining segments will be the same as the end element of the previous segments
                                                    // i.e. each segment connects to the next segment
                                                    locationInfo.getCoordinate().add(segment.getStart().getCoordinate());
                                                }else{
                                                    locationInfo.getCoordinate().add(segment.getEnd().getCoordinate());
                                                }
                                                
                                            }
                                        }
                                        
                                    }
                                }
                                
                                if(locationInfo.getCoordinate().isEmpty()){
                                    //ERROR
                                    throw new IllegalArgumentException("Unable to find the place of interest named '"
                                            + poiRef + "' that is referenced by the strategy named '"
                                            + strategy.getName() + "'.");
    
                                }
                            }
                        }else if(envAdaptation.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects){
                            generated.dkf.EnvironmentAdaptation.HighlightObjects highlight = 
                                    (generated.dkf.EnvironmentAdaptation.HighlightObjects)envAdaptation.getType();
                            
                            if(highlight.getType() instanceof generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo){
                            
                                generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo locationInfo = 
                                        (generated.dkf.EnvironmentAdaptation.HighlightObjects.LocationInfo)highlight.getType();
                                
                                if(locationInfo.getCoordinate() == null){
                                    // the coordinate(s) needs to be populated from the place of interest reference
                                
                                    String poiRef = locationInfo.getPlaceOfInterestRef();
                                    for(Serializable pointPathOrArea : placesOfInterest.getPointOrPathOrArea()){
                                        
                                        if(pointPathOrArea instanceof generated.dkf.Point){
                                            // only support highlight a point
                                            generated.dkf.Point point = (generated.dkf.Point)pointPathOrArea;
                                            if(StringUtils.equals(point.getName(), poiRef)){
                                                locationInfo.setCoordinate(point.getCoordinate());
                                                break;
                                            }
                                        }
                                    }
                                    
                                    if(locationInfo.getCoordinate() == null){
                                        //ERROR
                                        throw new IllegalArgumentException("Unable to find the place of interest named '"
                                                + poiRef + "' that is referenced by the strategy named '"
                                                + strategy.getName() + "'.");
        
                                    }
                                }
                            }
                        
                        }
                    }
                }
            }

            List<AbstractStrategy> abstractStrategies = AbstractStrategy.createActivitiesFrom(strategy);
            StrategySet strategySet = new StrategySet(abstractStrategies);
            
            if(strategy.getStress() != null) {
                strategySet.setStress(strategy.getStress().doubleValue());
            }
            
            if(strategy.getDifficulty() != null) {
                strategySet.setDifficulty(strategy.getDifficulty().doubleValue());
            }
            strategyMap.put(strategyName, strategySet);
        }


        //
        // Create GIFT state transition objects
        //

        AbstractTransition giftTransition = null;
        List<List<AbstractStrategy>> tStrategies;
        globalTransitions = new ArrayList<LearnerStateTransition>();
        for(generated.dkf.Actions.StateTransitions.StateTransition transition : transitions){

            //collect the strategies referenced by this transition
            tStrategies = new ArrayList<List<AbstractStrategy>>();
            if (transition.getStrategyChoices() != null) {
                for (Serializable strategy : transition.getStrategyChoices().getStrategies()) {

                    if (strategy instanceof generated.dkf.StrategyRef) {
                        generated.dkf.StrategyRef strategyRef = (generated.dkf.StrategyRef) strategy;

                        if (strategyMap.containsKey(strategyRef.getName())) {
                            tStrategies.add(strategyMap.get(strategyRef.getName()).getStrategies());
                        } else {
                            throw new IllegalArgumentException("Unable to find the strategy named '"
                                    + strategyRef.getName() + "' that is referenced by the state transition named '"
                                    + transition.getName() + "' (index = " + (transitions.indexOf(transition) + 1)
                                    + ")");
                        }
                    } else if (strategy instanceof generated.dkf.DelayAfterStrategy) {
                        // create a DKF do nothing strategy and common representation of that
                        // strategy to be used downstream
                        // (e.g. pedagogical model that looks at state transitions)
                        generated.dkf.DoNothingInstStrategy doNothingInstStrategy = new generated.dkf.DoNothingInstStrategy();
                        generated.dkf.StrategyHandler handler = new generated.dkf.StrategyHandler();
                        handler.setImpl("domain.knowledge.strategy.DefaultStrategyHandler");
                        doNothingInstStrategy.setStrategyHandler(handler);
                        doNothingInstStrategy.setDelayAfterStrategy((generated.dkf.DelayAfterStrategy) strategy);
                        DoNothingStrategy doNothing = new DoNothingStrategy(UUID.randomUUID().toString(),
                                doNothingInstStrategy);
                        tStrategies.add(Arrays.asList(doNothing));
                    }
                }
            }

            //collect the state logical expression
            generated.dkf.Actions.StateTransitions.StateTransition.LogicalExpression logicalExpression = transition.getLogicalExpression();
            List<AbstractTransition> giftTransitions = new ArrayList<AbstractTransition>();
            for(Object transitionType : logicalExpression.getStateType()){

                if(transitionType instanceof generated.dkf.PerformanceNode){

                    AbstractEnum prevEnum = null;
                    if(((generated.dkf.PerformanceNode)transitionType).getPrevious() != null){
                        prevEnum = AssessmentLevelEnum.valueOf(((generated.dkf.PerformanceNode)transitionType).getPrevious());
                    }

                    AbstractEnum currEnum = null;
                    if(((generated.dkf.PerformanceNode)transitionType).getCurrent() != null){
                        currEnum = AssessmentLevelEnum.valueOf(((generated.dkf.PerformanceNode)transitionType).getCurrent());
                    }

                    giftTransition =
                            new PerformanceNodeTransition(((generated.dkf.PerformanceNode)transitionType).getNodeId().intValue(),
                                                            ((generated.dkf.PerformanceNode)transitionType).getName(),
                                                            prevEnum,
                                                            currEnum);

                }else if(transitionType instanceof  generated.dkf.LearnerStateTransitionEnum){
                    //has to be a learner state attribute transition

                    generated.dkf.LearnerStateTransitionEnum enumTransitionType = (generated.dkf.LearnerStateTransitionEnum)transitionType;
                    LearnerStateAttributeNameEnum attrNameEnum = LearnerStateAttributeNameEnum.valueOf(enumTransitionType.getAttribute());

                    AbstractEnum prevEnum = null;
                    if(enumTransitionType.getPrevious() != null) {
                    	prevEnum = AbstractEnum.valueOf(enumTransitionType.getPrevious(), attrNameEnum.getAttributeValues());
                    }

                    AbstractEnum currEnum = null;
                    if(enumTransitionType.getCurrent() != null) {
                    	currEnum = AbstractEnum.valueOf(enumTransitionType.getCurrent(), attrNameEnum.getAttributeValues());
                    }
                    
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
