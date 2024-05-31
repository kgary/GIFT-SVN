/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.gamemaster;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import generated.dkf.Strategy;
import mil.arl.gift.common.course.dkf.session.AbstractKnowledgeSession;
import mil.arl.gift.common.course.strategy.ExecuteOCStrategy;
import mil.arl.gift.common.gwt.client.util.AbstractProvider;
import mil.arl.gift.common.util.CollectionUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.tools.dashboard.client.gamemaster.StrategyProvider.StrategyReceivedHandler;

/**
 * A singleton class that handles incoming strategy presets and requests.
 * 
 * @author sharrison
 */
public class StrategyProvider extends AbstractProvider<StrategyReceivedHandler> {
    /** The logger for the class */
    private static final Logger logger = Logger.getLogger(StrategyProvider.class.getName());

    /** The instance of the class */
    private static StrategyProvider instance = null;

    /**
     * Singleton constructor
     */
    private StrategyProvider() {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(".ctor()");
        }
    }

    /**
     * Accessor to the provider singleton object. If it doesn't exist yet it
     * will be created.
     * 
     * @return the instance to the provider singleton object.
     */
    public static StrategyProvider getInstance() {
        if (instance == null) {
            instance = new StrategyProvider();
        }

        return instance;
    }

    /**
     * Sets the preset associated and unassociated {@link Strategy strategies}.
     *
     * @param associatedStrategies The {@link Collection} of {@link Strategy
     *        strategies} that have an association with at least one
     *        {@link StateTransition}. Can be null iff there are unassociated
     *        strategies.
     * @param unassociatedStrategies The {@link Collection} of {@link Strategy
     *        strategies} that do not have an association with at least one
     *        {@link StateTransition}. Can be null iff there are associated
     *        strategies.
     * @param domainSessionId the knowledge session domain id.
     */
    public void setPresetStrategies(final Collection<Strategy> associatedStrategies,
            final Collection<Strategy> unassociatedStrategies, final int domainSessionId) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("setPresetStrategies(");
            List<Object> params = Arrays.<Object>asList(associatedStrategies, unassociatedStrategies, domainSessionId);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (associatedStrategies == null && unassociatedStrategies == null) {
            throw new IllegalArgumentException(
                    "The parameters 'associatedStrategies' and 'unassociatedStrategies' cannot both be null.");
        }

        /* Check if the session pushing the entity location update is
         * whitelisted */
        if (RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {

            /* Notify handlers */
            executeHandlers(new SafeHandlerExecution<StrategyReceivedHandler>() {
                @Override
                public void execute(StrategyReceivedHandler handler) {
                    handler.setPresetStrategies(associatedStrategies, unassociatedStrategies, domainSessionId);
                }
            });
        }
    }

    /**
     * New strategies suggestions have been received that are awaiting approval.
     * 
     * @param strategies The {@link Collection} of {@link Strategy} to suggest.
     * @param domainSessionId the knowledge session domain id.
     * @param evaluator The username of the person who created the request
     * @param msgTimestamp The timestamp of the message that contained the
     *        strategies.
     */
    public void addSuggestedStrategy(Map<String, List<Strategy>> strategies, final int domainSessionId,
            final String evaluator, final long msgTimestamp) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("addSuggestedStrategy(");
            List<Object> params = Arrays.<Object>asList(strategies, domainSessionId, evaluator, msgTimestamp);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (strategies == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        }

        /* Check if the session pushing the entity location update is
         * whitelisted */
        if (!RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {
            return;
        }

        /* Aggregate the strategies */
        final Collection<Strategy> aggregatedStrategies = aggregateStrategies(strategies);

        boolean sendToHandlers;
        if (CollectionUtils.isEmpty(aggregatedStrategies)) {
            /* Determine if we should send the empty aggregatedStrategies to the
             * handlers */

            if (CollectionUtils.isEmpty(strategies)) {
                /* Only send the empty strategy set to the handlers if the
                 * original message contained no strategies and we are in
                 * playback mode */
                AbstractKnowledgeSession knowledgeSession = ActiveSessionProvider.getInstance()
                        .getActiveSessionFromDomainSessionId(domainSessionId);
                sendToHandlers = knowledgeSession != null && knowledgeSession.inPastSessionMode();
            } else {
                /* Do not send an empty strategy set to the handlers if the
                 * strategies were 'filtered out' because there were no
                 * activities to perform (e.g. do-nothing) */
                sendToHandlers = false;
            }
        } else {
            /* Always send to the handlers if the aggregatedStrategies is
             * populated */
            sendToHandlers = true;
        }

        /* Notify handlers */
        if (sendToHandlers) {
            executeHandlers(new SafeHandlerExecution<StrategyReceivedHandler>() {
                @Override
                public void execute(StrategyReceivedHandler handler) {
                    handler.addSuggestedStrategy(aggregatedStrategies, domainSessionId, evaluator, msgTimestamp);
                }
            });
        }
    }

    /**
     * New strategies suggestions have been received that have already been
     * applied server-side.
     * 
     * @param strategies The {@link Collection} of {@link Strategy} to suggest.
     * @param domainSessionId the knowledge session domain id.
     * @param evaluator The username of the person who created the request
     * @param msgTimestamp The timestamp of the message that contained the
     *        strategies.
     */
    public void addAppliedStrategies(Map<String, List<Strategy>> strategies, final int domainSessionId,
            final String evaluator, final long msgTimestamp) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("addAppliedStrategies(");
            List<Object> params = Arrays.<Object>asList(strategies, domainSessionId, evaluator, msgTimestamp);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (strategies == null) {
            throw new IllegalArgumentException("The parameter 'strategies' cannot be null.");
        }

        /* Check if the session pushing the entity location update is
         * whitelisted */
        if (!RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {
            return;
        }

        /* Aggregate the strategies */
        final Collection<Strategy> aggregatedStrategies = aggregateStrategies(strategies);

        /* Notify handlers */
        executeHandlers(new SafeHandlerExecution<StrategyReceivedHandler>() {
            @Override
            public void execute(StrategyReceivedHandler handler) {
                handler.addAppliedStrategy(aggregatedStrategies, domainSessionId, evaluator, msgTimestamp);
            }
        });
    }

    /**
     * Aggregate the collection of strategies per reason into a single strategy
     * per reason.  The newly created aggregate strategy instances will
     * be given either the strategy name of the single strategy it was created from or
     * the default with is the reason the strategy was applied as the strategy name.
     * 
     * @param strategies the mapping of reasons to strategies.
     * @return a collection of strategies where each strategy contains all the
     *         activities for each entry in the map.
     */
    public static Collection<Strategy> aggregateStrategies(Map<String, List<Strategy>> strategies) {
        final Collection<Strategy> aggregatedStrategies = new ArrayList<>(strategies.size());
        for (Map.Entry<String, List<Strategy>> reasonToStrategies : strategies.entrySet()) {
            Strategy aggregatedStrategy = new Strategy();

            BigDecimal totalStress = new BigDecimal(0.0);
            BigDecimal totalDifficulty = new BigDecimal(0.0);
            for (Strategy strategy : reasonToStrategies.getValue()) {
                aggregatedStrategy.getStrategyActivities().addAll(strategy.getStrategyActivities());
                
                if(strategy.getStress() != null) {
                    totalStress = totalStress.add(strategy.getStress());
                }
                
                if(strategy.getDifficulty() != null) {
                    totalDifficulty = totalDifficulty.add(strategy.getDifficulty());
                }
            }            

            if (!aggregatedStrategy.getStrategyActivities().isEmpty()) {
                // there is at least one activity in the aggregate strategy, meaning it isn't a do nothing strategy
                
                aggregatedStrategies.add(aggregatedStrategy);
                
                // Note: in the future we may also want a comma delimited list of the strategy names when more than 1, that
                // maybe better than the static reason strings like 'automatically applied by gift'
                if(reasonToStrategies.getValue().size() == 1){
                    // use the specific single strategy name
                    aggregatedStrategy.setName(reasonToStrategies.getValue().get(0).getName());
                }else{
                    // use the reason as the strategy name since there are multiple strategies 
                    // in this new aggregate
                    aggregatedStrategy.setName(reasonToStrategies.getKey());
                }
                
                // use the aggregated strategy stress value
                aggregatedStrategy.setStress(totalStress);
                
                // use the aggregated strategy difficulty value
                aggregatedStrategy.setDifficulty(totalDifficulty);
            }
        }

        return aggregatedStrategies;
    }

    /**
     * Execute an OC strategy. This is a strategy that is solely directed at the
     * OC.
     * 
     * @param strategy the strategy to execute.
     * @param domainSessionId the knowledge session domain id.
     * @param msgTimestamp The timestamp of the message that contained the OC
     *        strategy.
     */
    public void executeOcStrategy(final ExecuteOCStrategy strategy, final int domainSessionId,
            final long msgTimestamp) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder("executeOcStrategy(");
            List<Object> params = Arrays.<Object>asList(strategy, domainSessionId, msgTimestamp);
            StringUtils.join(", ", params, sb);
            logger.fine(sb.append(")").toString());
        }

        if (strategy == null) {
            throw new IllegalArgumentException("The parameter 'strategy' cannot be null.");
        }

        /* Check if the session pushing the OC strategy is whitelisted */
        if (!RegisteredSessionProvider.getInstance().isRegistered(domainSessionId)) {
            return;
        }

        executeHandlers(new SafeHandlerExecution<StrategyReceivedHandler>() {
            @Override
            public void execute(StrategyReceivedHandler handler) {
                handler.executeOcStrategy(strategy, domainSessionId, msgTimestamp);
            }
        });
    }

    /**
     * Handler for listening for incoming strategy presets and requests.
     * 
     * @author sharrison
     */
    public interface StrategyReceivedHandler {
        /**
         * Sets the preset associated and unassociated {@link Strategy
         * strategies}.
         *
         * @param associatedStrategies The {@link Collection} of {@link Strategy
         *        strategies} that have an association with at least one
         *        {@link StateTransition}. Can be null iff there are
         *        unassociated strategies.
         * @param unassociatedStrategies The {@link Collection} of
         *        {@link Strategy strategies} that do not have an association
         *        with at least one {@link StateTransition}. Can be null iff
         *        there are associated strategies.
         * @param domainSessionId the domain session id for the knowledge
         *        session.
         */
        void setPresetStrategies(Collection<Strategy> associatedStrategies, Collection<Strategy> unassociatedStrategies,
                int domainSessionId);

        /**
         * New strategies suggestions have been received.
         * 
         * @param strategies The {@link Collection} of {@link Strategy} to
         *        suggest.
         * @param domainSessionId the domain session id for the knowledge
         *        session.
         * @param evaluator The username of the person who made the request.
         * @param msgTimestamp The timestamp of the message that contained the
         *        strategies.
         */
        void addSuggestedStrategy(Collection<Strategy> strategies, int domainSessionId, String evaluator,
                long msgTimestamp);

        /**
         * New strategies suggestions have been received that have already been
         * applied server-side.
         * 
         * @param strategies The {@link Collection} of {@link Strategy} to
         *        suggest.
         * @param domainSessionId the knowledge session domain id.
         * @param evaluator The username of the person who created the request
         * @param msgTimestamp The timestamp of the message that contained the
         *        strategies.
         */
        void addAppliedStrategy(Collection<Strategy> strategies, int domainSessionId, String evaluator,
                long msgTimestamp);

        /**
         * Execute an OC strategy. This is a strategy that is solely directed at
         * the OC.
         * 
         * @param strategy the strategy to execute.
         * @param domainSessionId the knowledge session domain id.
         * @param msgTimestamp The timestamp of the message that contained the
         *        OC strategy.
         */
        void executeOcStrategy(ExecuteOCStrategy strategy, int domainSessionId, long msgTimestamp);
    }
}
