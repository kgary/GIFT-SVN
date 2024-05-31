/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

/**
 * This class represents a request for domain options for a user.  It contains information needed to provide a list of domain options 
 * based on characteristics that could be used for selecting a subset of all domains available.
 * 
 * @author mhoffman
 *
 */
public class DomainOptionsRequest {

    /** information about a GIFT web client (e.g. TUI browser) */
    private WebClientInformation info;
    
    /** the LMS User name to use to query for LMS records for a GIFT user */
    private String lmsUserName = null;
    
    /**
     * Class constructor
     * 
     * @param info -information about a GIFT web client (e.g. TUI browser)
     */
    public DomainOptionsRequest(WebClientInformation info){
        setWebClientInformation(info);
    }    

    private void setWebClientInformation(WebClientInformation info){
        
        if(info == null){
            throw new IllegalArgumentException("The client information can't be null");
        }
        
        this.info = info;
    }
    
    /**
     * Set the LMS User name to use to query for LMS records for a GIFT user
     * 
     * @param lmsUserName the LMS User name to use to query for LMS records for a GIFT user
     */
    public void setLMSUserName(String lmsUserName){
        this.lmsUserName = lmsUserName;
    }
    
    /**
     * Set the LMS User name to use to query for LMS records for a GIFT user
     * 
     * @return String the LMS User name to use to query for LMS records for a GIFT user.  Can be null.
     */
    public String getLMSUserName(){
        return lmsUserName;
    }
    
    /**
     * Return the information about a GIFT web client (e.g. TUI browser).
     * 
     * @return WebClientInformation
     */ 
    public WebClientInformation getWebClientInformation(){
        return info;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DomainOptionsRequest: ");
        sb.append("info = ").append(getWebClientInformation());
        sb.append(", LMS username = ").append(getLMSUserName());
        sb.append("]");
        
        return sb.toString();
    }
}
