/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import generated.dkf.Strategy;

/**
 * The message payload for a {@link MessageTypeEnum#STRATEGY_STATE_UPDATE} type
 * message.
 *
 * @author sharrison
 */
public class StrategyStateUpdate implements Serializable {

    /**
     * A unique ID for identifying this specific version of the class during
     * deserialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The strategies that were applied. Maps reason to the strategies for that
     * reason.
     */
    private Map<String, List<Strategy>> appliedStrategies = new HashMap<>();

    /**
     * The strategies that are awaiting approval before being applied. Maps
     * reason to the strategies for that reason.
     */
    private Map<String, List<Strategy>> pendingStrategies = new HashMap<>();

    /** The username of the person making the request */
    private String evaluator;

    /**
     * Default constructor required for GWT Serialization
     */
    private StrategyStateUpdate() {
    }

    /**
     * Creates a {@link StrategyStateUpdate} from a {@link Strategy}.
     *
     * @param evaluator The username of the person making the request. Can be
     *        null if request was made automatically by GIFT.
     */
    public StrategyStateUpdate(String evaluator) {
        this();

        this.evaluator = evaluator;
    }

    /**
     * Getter for the list of strategies that were applied.
     *
     * @return the list of applied strategies. Will never be null.
     */
    public Map<String, List<Strategy>> getAppliedStrategies() {
        return appliedStrategies;
    }

    /**
     * Getter for the list of strategies that are pending approval.
     *
     * @return the list of strategies pending approval. Will never be null.
     */
    public Map<String, List<Strategy>> getPendingStrategies() {
        return pendingStrategies;
    }

    /**
     * Gets the name of the evaluator making the request. Can be null.
     * 
     * @return the evaluator
     */
    public String getEvaluator() {
        return evaluator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[StrategyStateUpdate: ");

        sb.append("applied strategies = ").append(appliedStrategies);
        sb.append(", pending strategies = ").append(pendingStrategies);
        sb.append(", evaluator = ").append(evaluator);

        return sb.append(']').toString();
    }
}
