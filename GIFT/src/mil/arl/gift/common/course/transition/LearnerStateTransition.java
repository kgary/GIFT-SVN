/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.transition;

import java.util.Collections;
import java.util.List;

import mil.arl.gift.common.course.strategy.AbstractStrategy;
import mil.arl.gift.common.enums.AbstractEnum;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;

/**
 * This is the base class for the transition classes. It contains the previous and current state values that
 * describe a possible transition.
 *
 * @author mhoffman
 *
 */
public class LearnerStateTransition {

    /** A logical expression of learner attribute transitions */
    private StateLogicalExpression stateLogicalExpression;

    /**
     * The ordered list of instructional strategy activities to execute as a result of this state
     * transition. The outer list is each strategy that is referenced by the state transition; the
     * inner list is the collection of activities per strategy.
     */
    private List<List<AbstractStrategy>> strategyActivities;

    /** The number of times this transition has evaluated to true (activated) */
    private int activatedCount = 0;
    
    /** the name of the state transition this class represents */
    private String transitionName;
    
    /** the reason why this pedagogical request is being made. */
    private String reasonForActivation;

    /**
     * Class constructor - set attributes
     *
     * @param transitionName - the name of the state transition this class represents.  Can't be null or empty.
     * @param stateLogicalExpression - a logical expression of learner attribute
     *        transitions to evaluate. Can't be null.
     * @param strategyActivities - The ordered list of instructional strategy
     *        activities to execute as a result of this state transition. The
     *        outer list is each strategy that is referenced by the state
     *        transition; the inner list is the collection of activities per
     *        strategy. Can't be null or empty; can't contain a null inner list.
     */
    public LearnerStateTransition(String transitionName, StateLogicalExpression stateLogicalExpression, List<List<AbstractStrategy>> strategyActivities){

        if(stateLogicalExpression == null){
            throw new IllegalArgumentException("The logical expression can't be null.");
        }else if(strategyActivities == null || strategyActivities.isEmpty()){
            throw new IllegalArgumentException("The strategies can't be null or empty.");
        }else if(StringUtils.isBlank(transitionName)){
            throw new IllegalArgumentException("The transition name can't be null or empty.");
        }

        this.stateLogicalExpression = stateLogicalExpression;
        this.transitionName = transitionName;
        this.reasonForActivation = transitionName;
        this.strategyActivities = Collections.unmodifiableList(strategyActivities);
    }

    /**
     * Return the ordered list of instructional strategy activities to execute as a result of this
     * state transition. The outer list is each strategy that is referenced by the state transition;
     * the inner list is the collection of activities per strategy.
     *
     * @return the collection of activities per strategy. Won't be null or empty.
     */
    private List<List<AbstractStrategy>> getStrategyActivities() {
        return strategyActivities;
    }

    /**
     * Gets the next list of activities to be executed. Every subsequent call to
     * this method will return the next strategy's activities. If this method is
     * called more times than the number of strategies, then the last strategy
     * will be returned each time.
     *
     * @return the collection of activities to execute for the next strategy in
     *         the list. Can't be null.
     */
    public List<AbstractStrategy> getNextActivitySet() {
        int activatedCount = getNextActivatedCount();
        List<AbstractStrategy> nextActivities = getStrategyActivities().get(activatedCount);
        return Collections.unmodifiableList(nextActivities);
    }

    /**
     * Calculates the index to retrieve the next set of activities. If the index is greater than the
     * number of strategies, then the index of the last strategy is returned. Each time this method
     * is called, the index is automatically incremented.
     *
     * @return the calculated index of the next set of strategy activities.
     */
    private int getNextActivatedCount() {
        /* store the current activated count to be used in evaluation later, then increment the
         * activated count for next time */
        final int numTimesActivated = activatedCount++;
        final int strategyCount = getStrategyActivities().size();

        if (numTimesActivated >= strategyCount) {
            // get last strategy in list
            return strategyCount - 1;
        } else {
            return numTimesActivated;
        }
    }

    /**
     * Return the attribute transitions associated with this state transition.
     *
     * @return List<AbstractTransition> Won't be null or empty.
     */
    public List<AbstractTransition> getTransitions(){
        return stateLogicalExpression.getTransitions();
    }

    /**
     * Update this transition with the latest information about an attribute.
     * Note: the previous value can be null if this is the first state attribute value.
     *
     * @param learnerAttributeKey - unique identifier of the state attribute being updated.
     * @param label - optional label that is paired with the learnerAttributeKey, this is usually a course concept.  
     * E.g. 'Knowledge' learner attribute with 'suppress OPFOR with well-aimed fire' course concept key.
     * @param previousValue - the last value of the state attribute.  Can be null if this is the first update.
     * @param previousValueTimestamp the time stamp at which the previous value was set.  Can be 0 if this is the first update.
     * @param currentValue - the current value of the state attribute
     * @param currentValueTimestamp the time stamp at which the current value was set.
     */
    public void update(Object learnerAttributeKey, String label, AbstractEnum previousValue, long previousValueTimestamp,
            AbstractEnum currentValue, long currentValueTimestamp){
        stateLogicalExpression.update(learnerAttributeKey, label, previousValue, previousValueTimestamp, currentValue, currentValueTimestamp);
    }

    /**
     * Determine whether the provided states cause the state logical expression to evaluate
     * to true.  If so it means that this authored state transition has been satisfied.

     * @return true if the transition fired or false if the transition didn't fire.
     */
    public boolean shouldTransition(){
        return stateLogicalExpression.isTrue();
    }

    /**
     * Return the logical expression for this learner state transition that contains the various
     * state attribute's of interest and the transition of values to look for.
     *
     * @return StateLogicalExpression contains the learner state attribute's of interest. Won't be
     *         null.
     */
    public StateLogicalExpression getLogicalExpression(){
        return stateLogicalExpression;
    }
    
    /**
     * The name of the transition.
     * 
     * @return won't be null or empty.
     */
    public String getTransitionName(){
        return transitionName;
    }
    
    /**
     * An explanation as to why the transition was activated.
     * 
     * @return won't be null or empty.
     */
    public String getReasonForActivation(){
        return reasonForActivation;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("[LearnerStateTransition: ");

        sb.append("logicalExpression = ").append(stateLogicalExpression);

        sb.append(", strategy activities = {");

        StringUtils.join(", ", strategyActivities, new Stringifier<List<AbstractStrategy>>() {
            @Override
            public String stringify(List<AbstractStrategy> activityList) {
                return "{" + StringUtils.join(", ", activityList) + "}";
            }
        }, sb);

        sb.append("}");
        sb.append(", evaluated to true= ").append(activatedCount).append(" times");

        sb.append("]");

        return sb.toString();
    }
}
