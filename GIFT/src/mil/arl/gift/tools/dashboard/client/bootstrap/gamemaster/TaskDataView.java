/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import com.google.gwt.user.client.ui.IsWidget;

import mil.arl.gift.tools.dashboard.client.Dashboard.AssessmentSoundType;

/**
 * A view (i.e. a widget) capable of displaying task performance data for a task data provider
 * 
 * @author nroberts
 */
public interface TaskDataView extends IsWidget{

    /**
     * Refreshes any widget that has a timestamp or something else that needs frequent updating.
     */
    public void refreshTimerWidgets();
    
    /**
     * Redraw this panel using the last state received.
     * 
     * @param updateStateOnRedraw flag determining whether to update the concept
     *        states with the current state.
     * @param allowAlertSound flag determining whether the alert sounds are
     *        allowed. Even if this is true, other criteria might prevent the
     *        sound from being played.
     * @param assessmentChanged whether the assessment state has just changed
     * @return an enumerated type of sound that needs to be played for this task
     *         and any descendant concepts where
     *         {@link AssessmentSoundType#POOR_ASSESSMENT} takes precedent. Can
     *         return null;
     */
    public AssessmentSoundType redraw(boolean updateStateOnRedraw, boolean allowAlertSound, boolean assessmentChanged);
    
    /**
     * Update the visibility of the status control icon
     * 
     * @param whether the status control icon should be visible
     */
    public void updateStatusControlIconVisibility(boolean visible);

    /**
     * Updates the visual state of the task data shown by this view to match the provided filter
     * 
     * @return whether all of the task's concepts have been filtered out.
     */
    public boolean applyConceptFilter();

    /**
     * Attempts to find a widget representing the given performance node within this widget and,
     * if such a widget is found, scrolls that widget into view so that the performance node's data
     * is visible
     * 
     * @param performanceNodePath the path to the performance node to scroll into view. Cannot be null.
     */
    public void scrollNodeIntoView(PerformanceNodePath performanceNodePath);
}
