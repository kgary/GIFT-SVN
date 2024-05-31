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
import java.util.Map;
import java.util.Map.Entry;

import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.common.util.StringUtils.Stringifier;

/**
 * The message payload for a {@link mil.arl.gift.common.enums.MessageTypeEnum#AUTHORIZE_STRATEGIES_REQUEST} type
 * message. This class contains data on messages that have been requested for
 * execution by the {@link mil.arl.gift.domain.DomainModule DomainModule} to the
 * {@link mil.arl.gift.tools.dashboard.server.WebMonitorModule
 * WebMonitorModule}.
 *
 * @author tflowers
 *
 */
public class AuthorizeStrategiesRequest implements Serializable {

    /**
     * A unique ID for identifying this specific version of the class during
     * deserialization.
     */
    private static final long serialVersionUID = 1L;

    /** The collection of strategies for which execution is requested */
    private Map<String, List<StrategyToApply>> reasonsToActivity;
    
    /** The username of the person making the request */
    private String evaluator;

    /**
     * Default constructor required for GWT Serialization
     */
    private AuthorizeStrategiesRequest() {
    }

    /**
     * Creates a {@link AuthorizeStrategiesRequest} from a {@link List} of
     * {@link StrategyToApply}.
     *
     * @param strategies The {@link List} of {@link StrategyToApply} to execute. Can't
     *        be null but can be empty.
     * @param evaluator The username of the person making the request. Can be null
     *        if request was made automatically by GIFT.
     */
    public AuthorizeStrategiesRequest(Map<String, List<StrategyToApply>> strategies, String evaluator) {
        this();

        if (strategies == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        }

        this.reasonsToActivity = strategies;
        this.evaluator = evaluator;
    }

    /**
     * Getter for a mapping of request reasons to the {@link List} of
     * {@link StrategyToApply} that has been requested for that reason.
     *
     * @return The {@link Map} of reason to strategies. Can't be null but can be
     *         empty. Can't be modified.
     */
    public Map<String, List<StrategyToApply>> getRequests() {
        return Collections.unmodifiableMap(reasonsToActivity);
    }

    /**
     * Gets the name of the evaluator making the request. 
     * 
     * @return the evaluator, Can be null if it's automatically being applied by GIFT.
     */
    public String getEvaluator() {
        return evaluator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[AuthorizeStrategiesRequest: ");

        sb.append("strategies = [");
        join(", ", reasonsToActivity.entrySet(), new Stringifier<Map.Entry<String, List<StrategyToApply>>>() {

            @Override
            public String stringify(Entry<String, List<StrategyToApply>> obj) {
                
                StringBuilder sb = new StringBuilder();
                sb.append(obj.getKey()).append(Constants.EQUALS).append("{");
                StringUtils.join(", ", obj.getValue(), new Stringifier<StrategyToApply>() {
                    
                    @Override
                    public String stringify(StrategyToApply obj) {
                        return obj.getStrategy().getName();
                    }
                }, sb);
                sb.append("}");

                return sb.toString();
            }

        }, sb);
        sb.append(", evaluator = ").append(evaluator);
        sb.append(']');

        return sb.append(']').toString();
    }
}
