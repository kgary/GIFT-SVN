/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;


/**
 * A response indicating that a call to an RPC method was successful
 * 
 * @author nroberts
 */
public class SuccessfulResponse<T> extends GenericRpcResponse<T> {

	/**
	 * Creates a successful response with no content
	 */
	public SuccessfulResponse(){
		super();
		
		setWasSuccessful(true);
	}
	
	/**
	 * Creates a successful response with the given content
	 */
	public SuccessfulResponse(T content){
		this();
		
		setContent(content);
	}
}
