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
import java.util.HashMap;
import java.util.List;

import mil.arl.gift.common.state.LearnerState;

/**
 * A wrapper around a {@link LearnerState} that contains additional cached state information from a domain knowledge session
 *
 * @author nroberts
 */
@SuppressWarnings("serial")
public class KnowledgeSessionState implements Serializable {

    /** The learner state that this knowledge session state wraps */
    private LearnerState learnerState;

    /** A mapping from each of the knowledge session's tasks to its cached state data */
    private HashMap<Integer, TaskStateCache> cachedTaskStates = new HashMap<>();

    /**
     * The cached strategies that have already been processed for this session
     */
    private List<ProcessedStrategyCache> cachedProcessedStrategies = new ArrayList<>();
    
    /**
     * The cached bookmarks that have already been processed for this session
     */
    private List<ProcessedBookmarkCache> cachedProcessedBookmarks = new ArrayList<>();

    /**
     * Default no-arg constructor needed for GWT serialization
     */
    private KnowledgeSessionState() {}

    /**
     * Creates a new domain knowledge session state wrapping the given learner state
     *
     * @param learnerState the learner state to wrap
     */
    public KnowledgeSessionState(LearnerState learnerState) {
        this();

        this.learnerState = learnerState;
    }

    /**
     * Gets the learner state wrapped by this domain knowledge session state
     *
     * @return the wrapped learner state
     */
    public LearnerState getLearnerState() {
        return learnerState;
    }

    /**
     * Gets the cached state information for the task with the given ID
     *
     * @param taskId the ID of the task to get information for
     * @return the cached state information for the requested task, or null, if no such information is available
     */
    public TaskStateCache getCachedTaskState(Integer taskId) {
        return cachedTaskStates.get(taskId);
    }

    /**
     * Populates the cached state information for the domain knowledge session's tasks
     *
     * @param cachedTaskStates a mapping from each task's ID to its corresponding cached state data
     */
    public void setCachedTaskStates(HashMap<Integer, TaskStateCache> cachedTaskStates) {
        this.cachedTaskStates.clear();
        if (cachedTaskStates != null) {
            this.cachedTaskStates.putAll(cachedTaskStates);
        }
    }

    /**
     * Gets the cached processed strategies
     *
     * @return the list of cached processed strategies
     */
    public List<ProcessedStrategyCache> getCachedProcessedStrategies() {
        return cachedProcessedStrategies;
    }
    
    /**
     * Gets the cached processed bookmarks
     *
     * @return the list of cached processed bookmarks
     */
    public List<ProcessedBookmarkCache> getCachedProcessedBookmarks() {
        return cachedProcessedBookmarks;
    }

    /**
     * Populates the cached processed strategies for the domain knowledge
     *
     * @param cachedProcessedStrategies the list of cached processed strategies.
     *        If null, the list will be cleared.
     */
    public void setCachedProcessedStrategy(List<ProcessedStrategyCache> cachedProcessedStrategies) {
        this.cachedProcessedStrategies.clear();
        if (cachedProcessedStrategies != null) {
            this.cachedProcessedStrategies.addAll(cachedProcessedStrategies);
        }
    }
    
    /**
     * Populates the cached processed bookmarks for the domain knowledge
     *
     * @param cachedProcessedBookmarks the list of cached processed bookmarks.
     *        If null, the list will be cleared.
     */
    public void setCachedProcessedBookmark(List<ProcessedBookmarkCache> cachedProcessedBookmarks) {
        this.cachedProcessedBookmarks.clear();
        if (cachedProcessedBookmarks != null) {
            this.cachedProcessedBookmarks.addAll(cachedProcessedBookmarks);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[KnowledgeSessionState: ");
        sb.append("learnerState = ").append(learnerState);
        sb.append(", cachedTaskStates = ").append(cachedTaskStates);
        sb.append(", cachedProcessedStrategies = ").append(cachedProcessedStrategies);
        sb.append("]");

        return sb.toString();
    }
}
