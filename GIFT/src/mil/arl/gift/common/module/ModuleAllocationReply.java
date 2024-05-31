/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.module;

/**
 * This class contains the necessary information for a module to reply to a module allocation request
 * 
 * @author mhoffman
 *
 */
public class ModuleAllocationReply {

    /** info about why the request was denied */
    private String requestDeniedMessage;
    
    /** additional information about why the request was denied */
    private String additionalInformation;

    /** amount of time until the allocation expires */
    private long leaseRenewTime = -1;
    
    /**
     * Class constructor - allocation request denied
     * 
     * @param requestDeniedMessage - info about why the request was denied
     */
    public ModuleAllocationReply(String requestDeniedMessage){
        setRequestDeniedMessage(requestDeniedMessage);
    }
    
    /**
     * Class constructor - allocation request granted
     */
    public ModuleAllocationReply(){

    }
    
    /**
     * Return whether or not the module allocation request has been denied by this reply.
     * 
     * @return boolean - true iff the reply is for a denial of the allocation request.
     */
    public boolean isDenied(){
        return getRequestDeniedMessage() != null;
    }
    
    private void setRequestDeniedMessage(String requestDeniedMessage){
        
        if(requestDeniedMessage == null){
            throw new IllegalArgumentException("The request denied message can't be null");
        }
        
        this.requestDeniedMessage = requestDeniedMessage;
    }
    
    /**
     * Return info about why the request was denied
     * 
     * @return requestDeniedMessage can be null if the request was successful
     */
    public String getRequestDeniedMessage(){
        return requestDeniedMessage;
    }
    
    /**
     * Return additional information about why the request was denied.
     * 
     * @return can be null
     */
    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ModuleAllocationReply: ");
        
        if(requestDeniedMessage != null){
            sb.append("requestDeniedMessage = ").append(requestDeniedMessage);
        }
        
        if(additionalInformation != null){
            sb.append(", additionalInformation = ").append(getAdditionalInformation());
        }
        
        if(leaseRenewTime > 0){
            sb.append("leaseRenewTime = ").append(leaseRenewTime);
        }
        
        sb.append("]");
        
        return sb.toString();
    }
}
