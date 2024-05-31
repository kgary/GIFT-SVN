/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action.dkf;

import java.util.List;

import mil.arl.gift.tools.authoring.server.gat.shared.action.GatServiceResult;

// TODO: Auto-generated Javadoc
/**
 * The Class FetchStrategyHandlerClassNamesResult.
 */
public class FetchStrategyHandlerClassNamesResult extends GatServiceResult {
	
	/** The strategy handler class names. */
	List<String> strategyHandlerClassNames;

    /**
     * Instantiates a new fetch strategy handler class names result.
     */
    public FetchStrategyHandlerClassNamesResult() {
        super();
    }
    
    /**
     * Instantiates a new fetch strategy handler class names result.
     *
     * @param strategyHandlerClassNames the strategy handler class names
     */
    public FetchStrategyHandlerClassNamesResult(List<String> strategyHandlerClassNames) {
       	super();
       	
       	this.strategyHandlerClassNames = strategyHandlerClassNames;
    }

	/**
	 * Gets the strategy handler class names.
	 *
	 * @return the strategy handler class names
	 */
	public List<String> getStrategyHandlerClassNames() {
		return strategyHandlerClassNames;
	}

	/**
	 * Sets the strategy handler class names.
	 *
	 * @param strategyHandlerClassNames the new strategy handler class names
	 */
	public void setStrategyHandlerClassNames(List<String> strategyHandlerClassNames) {
		this.strategyHandlerClassNames = strategyHandlerClassNames;
	}
    
    
}
