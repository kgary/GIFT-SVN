/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared.websocket.responsedata;

import mil.arl.gift.common.gwt.client.RpcResponse;

/**
 * The RpcResponseData class is used to help wrap and convert former gwt-rpc calls that relied on 
 * RpcResponse to use the tutor websocket (instead of gwt rpc).  The RpcResponseData class wraps
 * the gwt RpcReponse into the websocket response. This helps re-use the exsting gwt-rpc logic and makes it
 * easier to port the logic to use websockets where needed and re-use the classes.
 * 
 * @author nblomberg
 *
 */
public class RpcResponseData extends AbstractMessageResponseData {

   
    /** The RpcResponse containing the result of the async websocket request. */
    private RpcResponse rpcResponse;
    
    /** 
     * Constructor - default (required for GWT serialization).
     */
    public RpcResponseData() {
    }
    
    /**
     * @return the rpcResponse
     */
    public RpcResponse getRpcResponse() {
        return rpcResponse;
    }


    /**
     * @param rpcResponse the rpcResponse to set
     */
    public void setRpcResponse(RpcResponse rpcResponse) {
        this.rpcResponse = rpcResponse;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[RpcResponseData: ");
        sb.append("rpcResponse = ").append(getRpcResponse());
        sb.append("]");
        
        return sb.toString();
    }
    
    

   
}
