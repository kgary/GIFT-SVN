/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.websocket;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A {@link JavaScriptObject} implementation of the native JavaScript 
 * <a href="https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent">Close Event</a> used for WebSockets.
 * 
 * @author nroberts
 */
public class CloseEvent extends JavaScriptObject {

	/** Required for all classes extending {@link JavaScriptObject} */
	protected CloseEvent(){}
	
	/**
	 * Returns an integer containing the close code send by the server. See https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent 
	 * for a list of the possible codes that can be returned by this method.
	 * <br/><br/>
	 * Utility methods for interpreting these codes can be found in 
	 * {@link mil.arl.gift.common.gwt.shared.websocket.CloseEventCodes CloseEventCodes}.
	 * 
	 * @return an integer containing the close code send by the server
	 */
	final public native int getCode()/*-{
		return this.code;
	}-*/;
	
	/**
	 * Returns a string indicating the reason the server closed the connection. This is specific to the particular server and sub-protocol.
	 * 
	 * @return a string indicating the reason the server closed the connection
	 */
	final public native String getReason()/*-{
		return this.reason;
	}-*/;
	
	/**
	 * Returns a boolean that indicates whether or not the connection was cleanly closed.
	 * 
	 * @return whether or not the connection was cleanly closed
	 */
	final public native Boolean wasClean()/*-{
		return this.wasClean;
	}-*/;

}
