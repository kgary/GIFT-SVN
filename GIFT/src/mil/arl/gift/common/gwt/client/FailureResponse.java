/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;

/**
 * A response indicating that a call to an RPC method resulted in failure
 * 
 * @author nroberts
 */
public class FailureResponse<T> extends GenericRpcResponse<T> {

	/**
	 * Creates a failed response with no content
	 */
	public FailureResponse(){
		super();
		
		setWasSuccessful(false);
	}
	
	/**
	 * Creates a failed response with the given content
	 */
	public FailureResponse(T content){
		this();
		
		setContent(content);
	}
	
	/**
	 * Creates a failed response with the given exception
	 */
	public FailureResponse(DetailedException exception){
		this();
		
		setException(new DetailedExceptionSerializedWrapper(exception));
	}
	
	/**
	 * Creates a failed response with the given content and exception
	 */
	public FailureResponse(T content, DetailedException exception){
		this();
		
		setContent(content);
		setException(new DetailedExceptionSerializedWrapper(exception));
	}
}
