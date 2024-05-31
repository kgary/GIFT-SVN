/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import static mil.arl.gift.common.util.StringUtils.join;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * A {@link mil.arl.gift.net.api.message.Messag} payload that is sent by the
 * {@link mil.arl.gift.tools.dashboard.server.WebMonitorModule WebMonitorModule}
 * indicating that a certain set of strategies should be applied.
 *
 * @author tflowers
 *
 */
public class ApplyStrategies implements Serializable {

    /**
     * A unique identifier used for serialization and deserialization purposes.
     */
    private static final long serialVersionUID = 1L;

    /** The names of the strategies that should be executed. */
    private List<StrategyToApply> strategies;

    /** The username of the person making the request */
    private String evaluator;
    
    /** 
     * whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request */
    private boolean scenarioSupport = false;

    /** Default constructor used to make this class GWT serializable */
    private ApplyStrategies() {
    }

    /**
     * Creates an {@link ApplyStrategies} payload with a {@link List} of
     * strategies that should be executed.
     *
     * @param strategies The {@link List} of {@link StrategyToApply} that should be
     *        executed. Can't be null.
     * @param evaluator The username of the person making the request. Can be
     *        null if it's automatically being applied by GIFT
     */
    public ApplyStrategies(List<StrategyToApply> strategies, String evaluator) {
        this();
        if (strategies == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        }

        this.strategies = strategies;
        this.evaluator = evaluator;
    }

    /**
     * Creates an {@link ApplyStrategies} payload with a {@link List} of
     * {@link StrategyToApply} to apply that all have a common trigger.
     *
     * @param strategies The {@link List} of {@link StrategyToApply} that should be
     *        executed. Can't be null.
     * @param trigger The {@link String} describing why each of the strategies
     *        are being recommended. Can't be null or empty.
     * @param evaluator The username of the person making the request. Can be
     *        null if it's automatically being applied by GIFT
     */
    public ApplyStrategies(List<StrategyToApply> strategies, String trigger, String evaluator) {
        this();
        if (strategies == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        } else if (trigger == null) {
            throw new IllegalArgumentException("The parameter 'trigger' cannot be null.");
        }

        this.strategies = strategies;
        this.evaluator = evaluator;
    }

    /**
     * Getter for the activities to execute.
     *
     * @return The {@link List} of {@link StrategyToApply}. Can't be null or modified.
     */
    public List<StrategyToApply> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    /**
     * Getter for the evaluator. Can be null if it's
     * automatically being applied by GIFT
     *
     * @return the evaluator
     */
    public String getEvaluator() {
        return evaluator;
    }

    /**
     * Return whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request
     * @return false by default
     */
    public boolean isScenarioSupport() {
        return scenarioSupport;
    }

    /**
     * Set whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request
     * @param scenarioSupport
     */
    public void setScenarioSupport(boolean scenarioSupport) {
        this.scenarioSupport = scenarioSupport;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ApplyStrategies: ");
        
        if(scenarioSupport){
            // if default wasn't changed than don't care about showing in display string
            sb.append("scenario support = ").append(scenarioSupport).append(", ");
        }

        /* Construct the representation of strategies */
        sb.append("strategies = [");
        join(", ", strategies, sb);
        sb.append(", ").append(evaluator);
        sb.append("]");

        return sb.append("]").toString();
    }
}
