/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.course.strategy;

import java.io.Serializable;

import generated.dkf.Strategy;

/**
 * The message payload for a {@link MessageTypeEnum#EXECUTE_OC_STRATEGY} type
 * message. This class contains data on messages that have been requested for
 * execution by the {@link mil.arl.gift.domain.DomainModule DomainModule} to the
 * {@link mil.arl.gift.tools.dashboard.server.WebMonitorModule
 * WebMonitorModule}.
 *
 * @author sharrison
 */
public class ExecuteOCStrategy implements Serializable {

    /**
     * A unique ID for identifying this specific version of the class during
     * deserialization.
     */
    private static final long serialVersionUID = 1L;

    /** The strategy to be executed for the OC */
    private Strategy ocStrategy;

    /** The username of the person making the request */
    private String evaluator;
    
    /** The reason for the strategy being triggered */
    private String reason;
    
    /**
     * whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request
     */
    private boolean scenarioSupport = false;
    
    /**
     * Default constructor required for GWT Serialization
     */
    private ExecuteOCStrategy() {
    }

    /**
     * Creates a {@link ExecuteOCStrategy} from a {@link Strategy}.
     *
     * @param strategy The {@link Strategy} to execute. Can't be null.
     * @param evaluator The username of the person making the request. Can be
     *        null if request was made automatically by GIFT.
     * @param reason The reason the strategy is being triggered. Can be null.
     */
    public ExecuteOCStrategy(Strategy strategy, String evaluator, String reason) {
        this();

        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        this.ocStrategy = strategy;
        this.evaluator = evaluator;
        this.reason = reason;
    }

    /**
     * Getter for a {@link Strategy} that has been requested to be executed for
     * the OC.
     *
     * @return The strategy to execute. Can't be null..
     */
    public Strategy getStrategy() {
        return ocStrategy;
    }

    /**
     * Gets the name of the evaluator making the request. Can be null if it's
     * automatically being applied by GIFT
     * 
     * @return the evaluator
     */
    public String getEvaluator() {
        return evaluator;
    }
    
    /**
     * Gets the reason the strategy is being applied. Can be null.
     * 
     * @return the reason
     */
    public String getReason() {
        return reason;
    }
    
    /**
     * Return whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request
     * @return default is false
     */
    public boolean isScenarioControl() {
        return scenarioSupport;
    }

    /**
     * Set whether this strategy is associated with a scenario element like a task trigger versus a 
     * strategy that is in the set of strategies for the pedagogical module to request
     * @param scenarioSupport the value to use
     */
    public void setScenarioSupport(boolean scenarioSupport) {
        this.scenarioSupport = scenarioSupport;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[ExecuteOCStrategy: ");

        sb.append("strategy = ").append(ocStrategy);
        sb.append(", evaluator = ").append(evaluator);        
        
        if(scenarioSupport){
            // don't add to the display string if the default wasn't changed
            sb.append(", scenario support = ").append(scenarioSupport);
        }
        
        sb.append(", reason = ").append(reason);

        return sb.append(']').toString();
    }
}
