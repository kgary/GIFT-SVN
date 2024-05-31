/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;

import mil.arl.gift.common.io.DetailedExceptionSerializedWrapper;

/**
 * An abstract representation of a response returned by an RPC method. Compared to
 * {@link mil.arl.gift.common.gwt.client.RpcResponse RpcResponse}, this class uses some more up-to-date error-reporting features
 * in order to report information back to end users. Namely, it enforces the use of {@link DetailedException DetailedExceptions}
 * when reporting failure messages so that end users can more easily report errors to developers. This class also supports generic
 * type arguments, making it much more flexible than {@link mil.arl.gift.common.gwt.client.RpcResponse RpcResponse} in terms
 * of wrapping successful return values.
 *
 * @author nroberts
 */
public class GenericRpcResponse<T> implements IsSerializable {

	/** The content of the response */
	private T content;

	/** Whether or not the operation that returned this response was successful */
	private boolean wasSuccessful;

	/** An optional detailed exception giving the user more details about a failed operation */
	private DetailedExceptionSerializedWrapper exception;

	/**
	 * Creates a new, empty RPC response
	 */
	public GenericRpcResponse(){

	}

	/**
	 * Gets content of the response
	 *
	 * @return the content
	 */
	public T getContent() {
		return content;
	}

	/**
	 * Sets content of the response
	 *
	 * @param content the content to set
	 */
	public void setContent(T content) {
		this.content = content;
	}

	/**
	 * Gets whether or not the operation that returned this response was successful
	 *
	 * @return whether or not the operation that returned this response was successful
	 */
	public boolean getWasSuccessful() {
		return wasSuccessful;
	}

	/**
	 * Sets whether or not the operation that returned this response was successful
	 *
	 * @param wasSuccessful the wasSuccessful to set
	 */
	public void setWasSuccessful(boolean wasSuccessful) {
		this.wasSuccessful = wasSuccessful;
	}

	/**
	 * Gets a detailed exception giving the user more details about a failed operation
	 *
	 * @return the exception
	 */
	public DetailedExceptionSerializedWrapper getException() {
		return exception;
	}

	/**
	 * Sets a detailed exception giving the user more details about a failed operation
	 *
	 * @param exception the exception to set
	 */
	public void setException(DetailedExceptionSerializedWrapper exception) {
		this.exception = exception;
	}


}
