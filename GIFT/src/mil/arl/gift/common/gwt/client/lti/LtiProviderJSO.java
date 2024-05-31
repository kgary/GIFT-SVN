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
 * A display model representing an LTI providers
 * 
 * @author sharrison
 */
public class LtiProviderJSO extends JavaScriptObject {
	
	/**
	 * A constructor required by GWT to properly initialize the JavaScript object backing this class
	 */
	protected LtiProviderJSO(){}
	
	/**
	 * Gets this provider's name
	 * 
	 * @return the name
	 */
	final public native String getName()/*-{
		return this.name;
	}-*/;
	
	/**
	 * Gets the key needed to identify this provider
	 * 
	 * @return the key
	 */
	final public native String getKey()/*-{
		return this.providerKey;
	}-*/;
	
	/**
	 * Gets the shared secret needed to connect with this provider
	 * 
	 * @return the shared secret
	 */
	final public native String getSharedSecret()/*-{
		return this.providerSharedSecret;
	}-*/;
	
	/**
     * Gets the shared secret needed to connect with this provider
     * 
     * @return the shared secret
     */
    final public native String getProtectClientData()/*-{
        return this.protectClientData;
    }-*/;
    
    final public native void setValues(String name, String providerKey, String providerSharedSecret, String protectClientData)/*-{
        this.name = name;
        this.providerKey = providerKey;
        this.providerSharedSecret = providerSharedSecret;
        this.protectClientData = protectClientData;
    }-*/;

}
