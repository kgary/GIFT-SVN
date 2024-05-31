/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import mil.arl.gift.common.state.TaskPerformanceState;
import mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.TaskDataPanel.ViewMode;
import mil.arl.gift.tools.dashboard.shared.messages.TaskStateCache;

/**
 * An inteface used to interact with objects that are capable of storing task performance data so that it
 * can be accessed from different views
 * 
 * @author nroberts
 */
public interface TaskDataProvider extends PerformanceNodeDataProvider<TaskPerformanceState>{
    
    /**
     * Gets the task's cached state information as recorded by the server. Used to retrieve lifetime state information not included in
     * each individual learner state update from the domain.
     * 
     * @return the cached state information. Can be null.
     */
    public TaskStateCache getCachedState();

    /**
     * Sets the view that this data provider should use when displaying task data
     * 
     * @param view the view to use. If null, the default view will be used.
     */
    void setViewMode(ViewMode view);
}
