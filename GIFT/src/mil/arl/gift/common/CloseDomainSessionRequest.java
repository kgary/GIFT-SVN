/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents a close domain session request.
 * 
 * @author mhoffman
 *
 */
public class CloseDomainSessionRequest {

    /** (optional) a message about why the domain session is closing */
    private String reason;
    
    /**
     * Default constructor
     */
    public CloseDomainSessionRequest(){
        
    }
    
    /**
     * Class constructor - set attribute(s)
     * 
     * @param reason a message about why the domain session is closing
     */
    public CloseDomainSessionRequest(String reason){
        setReason(reason);
    }
    
    /**
     * Set the reason for the close domain session request.
     * 
     * @param reason - a message about why the domain session is closing
     */
    public void setReason(String reason){
        this.reason = reason;
    }
    
    /**
     * Return the reason for the close domain session request.
     * 
     * @return String can be null.
     */
    public String getReason(){
        return reason;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[CloseDomainSessionRequest: ");
        sb.append("reason = ").append(getReason());
        sb.append("]");
        return sb.toString();
    }
}
