/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.shared.action;

import mil.arl.gift.common.gwt.client.GenericRpcResponse;
import net.customware.gwt.dispatch.shared.Result;

/**
 * A {@link Result} containing a {@link GenericRpcResponse} responding to a request from the client. This class enforces more robust
 * error handling than {@link GatServiceResult} and supports generic type arguments, allowing new data types to be passed between 
 * the client and server without having to extend {@link GatServiceResult}.
 * <br/><br/>
 * This is essentially a GWT-Dispatch wrapper for {@link GenericRpcResponse}.
 *
 * @author nroberts
 */
public class GenericGatServiceResult<T> implements Result {

	/** The mapping being returned*/
	private GenericRpcResponse<T> response;
		
	/**
	 * Class constructor
	 * For serialization only.
	 */
	@SuppressWarnings("unused")
	private GenericGatServiceResult() {
		
    }
	
	/**
	 * Creates a new result containing the given response
	 * 
	 * @param response the response to contain
	 */
	public GenericGatServiceResult(GenericRpcResponse<T> response){		
		setResponse(response);
	}

	/**
	 * Gets the response contained by this result
	 * 
	 * @return the response contained by this result
	 */
	public GenericRpcResponse<T> getResponse() {
		return response;
	}

	/**
	 * Sets the response this result should contain
	 * 
	 * @param response the response this result should contain
	 */
	public void setResponse(GenericRpcResponse<T> response) {
		
		if(response == null){
			throw new IllegalArgumentException("The response to a GAT service request cannot be null.");
		}
		
		this.response = response;
	}
}
