/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.messages;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import generated.dkf.Strategy;

/**
 * Represents a response to a request for the mapping of strategy names to
 * strategies for a currently running domain session.
 *
 * @author tflowers
 *
 */
public class InitializationMessage implements Serializable {

    /**
     * Unique id to ensure correct serialization and deserialization of this
     * class
     */
    private static final long serialVersionUID = 1L;

    /** The strategies that are associated with at least one transition */
    private List<Strategy> associatedStrategies = new ArrayList<>();

    /** The strategies that are not associated with any transition */
    private List<Strategy> unassociatedStrategies = new ArrayList<>();

    /** The last learner state the web monitor received */
    private KnowledgeSessionState lastLearnerState;

    /** Default constructor that allows the object to be GWT Serializable */
    private InitializationMessage() {
    }

    /**
     * Constructor.
     *
     * @param strategyToAssociationMap The collection of {@link Strategy
     *        strategies} and whether or not they have an associated
     *        {@link StateTransition}. Can't be null.
     * @param lastLearnerState The {@link LearnerState} that was last received
     *        by the web monitor. Can be null if no {@link LearnerState} has
     *        been received yet.
     */
    public InitializationMessage(LinkedHashMap<Strategy, Boolean> strategyToAssociationMap, KnowledgeSessionState lastLearnerState) {
        this();

        if (strategyToAssociationMap == null) {
            throw new IllegalArgumentException("The parameter 'strategyToAssociationMap' cannot be null.");
        }

        if (lastLearnerState == null) {
            throw new IllegalArgumentException("The parameter 'learnerState' cannot be null.");
        }
        
        for (Entry<Strategy, Boolean> entry : strategyToAssociationMap.entrySet()) {
            if (Boolean.TRUE.equals(entry.getValue())) {
                associatedStrategies.add(entry.getKey());
            } else {
                unassociatedStrategies.add(entry.getKey());
            }
        }

        this.lastLearnerState = lastLearnerState;
    }

    /**
     * Retrieves the collection of strategies that have an associated
     * {@link StateTransition}.
     *
     * @return The list of associated {@link Strategy strategies}. Will never be
     *         null.
     */
    public Collection<Strategy> getAssociatedStrategies() {
        return associatedStrategies;
    }

    /**
     * Retrieves the collection of strategies that do not have an associated
     * {@link StateTransition}.
     *
     * @return The list of unassociated {@link Strategy strategies}. Will never
     *         be null.
     */
    public Collection<Strategy> getUnAssociatedStrategies() {
        return unassociatedStrategies;
    }

    /**
     * The getter for the last learner state the web monitor received.
     *
     * @return The {@link LearnerState}. Can be null if no {@link LearnerState}
     *         has been received yet.
     */
    public KnowledgeSessionState getLastLearnerState() {
        return lastLearnerState;
    }

    @Override
    public String toString() {
        return new StringBuilder("[InitializationMessage: ")
                .append("associatedStrategies = ").append(associatedStrategies)
                .append("unassociatedStrategies = ").append(unassociatedStrategies)
                .append(", lastLearnerState = ").append(lastLearnerState)
                .append("]").toString();
    }
}
