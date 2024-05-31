/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.lti;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A display model representing an LTI consumer
 * 
 * @author nroberts
 */
public class LtiConsumer extends JavaScriptObject {
	
	/**
	 * A contructor required by GWT to properly initialize the JavaScript object backing this class
	 */
	protected LtiConsumer(){}
	
	/**
	 * Gets this consumer's name
	 * 
	 * @return the name
	 */
	final public native String getName()/*-{
		return this.name;
	}-*/;
	
	/**
	 * Gets the key needed to identify this consumer
	 * 
	 * @return the key
	 */
	final public native String getKey()/*-{
		return this.consumerKey;
	}-*/;
	
	/**
	 * Gets the shared secret needed to connect with this consumer
	 * 
	 * @return the shared secret
	 */
	final public native String getSharedSecret()/*-{
		return this.consumerSharedSecret;
	}-*/;

}
