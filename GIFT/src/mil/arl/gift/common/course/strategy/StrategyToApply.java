/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import java.io.Serializable;
import java.util.Set;

import generated.dkf.Strategy;
import mil.arl.gift.common.util.StringUtils;

/**
 * Describes a {@link Strategy} that should be applied within a domain session.
 * 
 * @author tflowers
 *
 */
public class StrategyToApply implements Serializable {

    /**
     * The version of the class used within the serialization and
     * deserialization logic.
     */
    private static final long serialVersionUID = 1L;

    /** The {@link Strategy} that should be applied. */
    private Strategy strategy;

    /** A description of what caused this Strategy to be applied */
    private String trigger;
    
    /** The username of the person making the request */
    private String evaluator;
    
    /** optional collection of DKF XML node (Task/concept) ids that caused this strategy to be requested. */
    private Set<Integer> nodeIds;

    /**
     * A no argument constructor used to make this class GWT serializable.
     */
    private StrategyToApply() {
    }

    /**
     * Creates a {@link StrategyToApply} from a {@link StrategyToApply} and a
     * {@link String} trigger.
     *
     * @param strategy The {@link Strategy} to apply. Can't be null.
     * @param trigger The {@link String} describing why the {@link Strategy} is
     *        being applied. Can't be null or empty.
     * @param evaluator The username of the person making the request. Can be null
     *        if the request was automatically made by GIFT
     */
    public StrategyToApply(Strategy strategy, String trigger, String evaluator) {
        this();
        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        } else if (StringUtils.isBlank(trigger)) {
            throw new IllegalArgumentException("The parameter 'trigger' cannot be null or empty.");
        }

        this.strategy = strategy;
        this.trigger = trigger;
        this.evaluator = evaluator;
    }

    /**
     * Getter for the {@link Strategy} to apply.
     *
     * @return The value of {@link #strategy}. Can't be null.
     */
    public Strategy getStrategy() {
        return strategy;
    }

    /**
     * Getter for the reason the {@link Strategy} is being applied.
     *
     * @return The value of {@link #trigger}. Can't be null or empty.
     */
    public String getTrigger() {
        return trigger;
    }

    /**
     * Getter for the username of the person making the request.
     * Can be null if it's automatically being applied by GIFT.
     * 
     * @return the evaluator
     */
    public String getEvaluator() {
        return evaluator;
    }
    
    /**
     * Return the optional collection of DKF XML node (Task/concept) ids that caused this strategy to be requested.
     * @return can be null or empty.  
     */
    public Set<Integer> getTaskConceptsAppliedToo() {
        return nodeIds;
    }

    /**
     * Set the optional collection of DKF XML node (Task/concept) ids that caused this strategy to be requested.
     * 
     * @param nodeIds can be null or empty.
     */
    public void setTaskConceptsAppliedToo(Set<Integer> nodeIds) {
        this.nodeIds = nodeIds;
    }

    @Override
    public String toString() {
        return new StringBuilder("[StrategyToApply: ")
                .append("strategy = ").append(strategy)
                .append(", trigger = ").append(trigger)
                .append(", evaluator = ").append(evaluator)
                .append(", taskConceptsAppliedToo = ")
                .append(nodeIds)
                .append("]").toString();
    }
}
