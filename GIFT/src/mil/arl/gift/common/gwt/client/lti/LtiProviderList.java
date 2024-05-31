/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.lti;

import com.github.gwtd3.api.arrays.Array;
import com.google.gwt.core.client.JavaScriptObject;

/**
 * A list of {@link LtiProviderJSO LTI providers}
 * 
 * @author sharrison
 */
public class LtiProviderList extends JavaScriptObject {
	
	/**
	 * A constructor required by GWT to properly initialize the JavaScript object backing this class
	 */
	protected LtiProviderList(){}
	
	final public native Array<LtiProviderJSO> getProviders()/*-{
		return this.trusted_providers;
	}-*/;

}
