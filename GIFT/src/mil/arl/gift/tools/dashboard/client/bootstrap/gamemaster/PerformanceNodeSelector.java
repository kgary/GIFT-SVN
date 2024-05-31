/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

/**
 * An interface used to allow widgets to select performance nodes
 * 
 * @author nroberts
 */
public interface PerformanceNodeSelector{
    
    /**
     * Selects the performance node at the given location and timestamp
     * 
     * @param nodePath the location of the performance node within is hierarchy. Cannot be null.
     * @param timestamp the timestamp of the strategy to select. Cannot be null.
     */
    public void selectPerformanceNode(PerformanceNodePath nodePath, long timestamp);
}