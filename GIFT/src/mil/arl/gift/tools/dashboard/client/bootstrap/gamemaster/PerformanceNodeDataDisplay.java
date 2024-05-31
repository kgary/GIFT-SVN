/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

/**
 * An interface representing widgets that contain performance node data and display it to the user. This
 * interface is used to perform some common operations that are shared by different types of widgets
 * that display performance node data.
 * 
 * @author nroberts
 */
public interface PerformanceNodeDataDisplay {

    /**
     * Updates the visual state of the data shown by this item to match the
     * filter.
     * 
     * @return whether all of the subconcepts have been filtered out.
     */
    public boolean applyConceptFilter();
    
    /**
     * Gets whether the performance node represented by this widget or any of its children require the observer
     * controller to provide an assessment. This is used to determine the priority with which performance
     * node widgets are rendered to the user.
     * 
     * @return whether the performance node represented by this widget requires an observed assessment
     */
    public boolean requiresObservedAssessment();
}
