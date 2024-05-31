/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared.websocket.responsedata;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * The AbstractMessageResponseData is a container to allow custom data to be
 * passed as the response data in the AsyncMessageResponse class. 
 * 
 * Future classes should extend from the abstract class to
 * encode custom data in the websocket AsyncMessageResponse class.  See the method
 * "setResponseData()" in the AsyncMessageResponse class to set the response data.
 * 
 * @author nblomberg
 *
 */
public abstract class AbstractMessageResponseData implements IsSerializable {

    /** 
     * Constructor - default (required for GWT serialization).
     */
    public AbstractMessageResponseData() {
    }
    
    

   
}
