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
 * A list of {@link LtiConsumer LTI consumers}
 * 
 * @author nroberts
 */
public class LtiConsumerList extends JavaScriptObject {
	
	/**
	 * A contructor required by GWT to properly initialize the JavaScript object backing this class
	 */
	protected LtiConsumerList(){}
	
	final public native Array<LtiConsumer> getConsumers()/*-{
		return this.trusted_consumers;
	}-*/;

}
