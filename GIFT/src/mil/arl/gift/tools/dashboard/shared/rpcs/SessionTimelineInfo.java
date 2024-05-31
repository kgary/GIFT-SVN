/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.shared.rpcs;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.course.dkf.session.SessionScenarioInfo;
import mil.arl.gift.common.course.strategy.StrategyStateUpdate;
import mil.arl.gift.common.state.LearnerState;

/**
 * Information needed to populate a knowledge session timeline
 * 
 * @author nroberts
 */
public class SessionTimelineInfo implements IsSerializable{
    
    /** A mapping from each timestamp to all of the learner states in the session's log playback */
    private TreeMap<Long, LearnerState> learnerStates = new TreeMap<>();

    /**
     * A mapping from the timestamp of a patched learner state to the
     * performance state names that were patched
     */
    private Map<Long, Set<String>> patchedLearnerStatePerformances = new HashMap<>();

    /** A mapping from each timestamp to all of the applied strategies in the session's log playback */
    private TreeMap<Long, StrategyStateUpdate> strategies = new TreeMap<>();
    
    /** Information about the DKF scenario that was used in the session and its summative assessments */
    private SessionScenarioInfo scenarioInfo;

    /**
     * Creates a new set of timeline information with no learner states or strategies
     */
    public SessionTimelineInfo() {}
    
    /**
     * Gets all of the learner states in the session's log playback
     * 
     * @return a mapping from each timestamp to all of the learner states in the session's log playback. Will not be null.
     */
    public TreeMap<Long, LearnerState> getLearnerStates(){
        return learnerStates;
    }

    /**
     * Gets all of the patched learner states' performance names based on the
     * learner state timestamp.
     * 
     * @return a mapping from the timestamp of a patched learner state to the
     *         performance state names that were patched. Will not be null.
     */
    public Map<Long, Set<String>> getPatchedLearnerStatePerformances() {
        return patchedLearnerStatePerformances;
    }

    /**
     * Gets all of the applied strategies in the session's log playback
     * 
     * @return a mapping from each timestamp to all of the applied strategies in the session's log playback
     */
    public TreeMap<Long, StrategyStateUpdate> getStrategies(){
        return strategies;
    }

    /**
     * Gets information about the DKF scenario that was used in the session and its summative assessments
     * 
     * @return the scenario information. Can be null.
     */
    public SessionScenarioInfo getScenarioInfo() {
        return scenarioInfo;
    }

    /**
     * Sets information about the DKF scenario that was used in the session and its summative assessments
     * 
     * @param scenarioInfo the scenario information. Can be null.
     */
    public void setScenarioInfo(SessionScenarioInfo scenarioInfo) {
        this.scenarioInfo = scenarioInfo;
    }
}
