/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.util.cell;

import java.util.List;

/**
 * An interface used to interact with UI elements that present data in a list of rows that each contain a list of options to chose from. 
 * This interface may be used to set a global list of options to be used by all rows or to set a list of options to be used by one specific 
 * row.
 * 
 * @author nroberts
 * 
 * @param <R> The type of the identifier objects used to distinguish each row (i.e. the type of key to find each row with)
 * @param <O> The type of the options to choose from
 *
 */
public interface HasRowOptions<R, O> {

	/**
	 * Gets the list of options used by the row uniquely identified by the specified identifier object.
	 * 
	 * @param rowIdentifier The identifier object uniquely identifying the row from which to get the list of options
	 * @return the list of options
	 */
	public List<O> getRowOptions(R rowIdentifier);
	
	/**
	 * Sets the list of options used by the row uniquely identified by the specified identifier object.
	 * 
	 * @param rowIdentifier The identifier object uniquely identifying the row that should use the list of options
	 * @param optionList The list of options the row should use
	 */
	public void setRowOptions(R rowIdentifier, List<O> optionList);
	
	/**
	 * Gets the default list of options.
	 * 
	 * @return the default list of options.
	 */
	public List<O> getDefaultOptions();
	
	/**
	 * Sets a default list of options to be used by each row until setRowOptions(R, List<O>) is called on it.
	 * 
	 * @param optionList the default list of options
	 */
	public void setDefaultOptions(List<O> optionList);
}
