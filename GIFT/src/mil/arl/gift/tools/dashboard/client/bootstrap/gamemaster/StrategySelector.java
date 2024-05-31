/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

/**
 * An interface used to allow widgets to select strategies
 * 
 * @author nroberts
 */
public interface StrategySelector{
    
    /**
     * Selects the strategy with the given timestamp
     * 
     * @param name the name of the strategy to select
     * @param timestamp the timestamp of the strategy to select
     */
    public void selectStrategy(String name, Long timestamp);
}