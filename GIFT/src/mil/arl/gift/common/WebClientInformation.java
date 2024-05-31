/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import mil.arl.gift.net.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains information about a GIFT web-based client (e.g. TUI browser).
 * 
 * @author mhoffman
 *
 */
public class WebClientInformation {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(WebClientInformation.class);
    
    /** local host represented in IP v6 format */
    private static final String IPV6_LOCALHOST = "0:0:0:0:0:0:0:1";
    
    /** address of the tutor client (e.g. IPv4 address) */
    private String clientAddress;
    
    /** whether the tutor client is embedded within the GIFT Mobile App */
    private MobileAppProperties mobileAppProperties;
    
    /**
     * Default constructor
     */
    public WebClientInformation(){
        
    }

    /**
     * Set the address of the tutor client.
     * 
     * @param clientAddress - can be null because this is optional.
     *                     Note: if the address is IPv6 localhost, it will be replaced with IPv4 address.
     */
    public void setClientAddress(String clientAddress){
        
        String workingClientAddress = clientAddress;
        
        if(clientAddress != null){
            
            //check for IPv6 address
            if(clientAddress.contains(":")){
                
                //check of localhost IPv6
                if(clientAddress.equals(IPV6_LOCALHOST)){
                    
                    //replace with host machines IPv4 address
                    workingClientAddress = Util.getLocalHostAddress().getHostAddress();
                    
                    if(logger.isInfoEnabled()){
                        logger.info("Changing IPv6 localhost address of "+clientAddress+" to IPv4 address of "+workingClientAddress+".");
                    }
                    
                }else{
                    if(logger.isDebugEnabled()){
                        logger.debug("Unable to use IPv6 address of "+clientAddress+" as a client address because GIFT currently doesn't support addresses of that type.");
                    }
                    workingClientAddress = null;
                }
            }
            
        }
        
        this.clientAddress = workingClientAddress;
        
    }
    
    /**
     * Return the address of the tutor client (e.g. IPv4 address).
     * Note: the value may be null if not provided
     * 
     * @return the client address.  Maybe null.
     */
    public String getClientAddress(){
        return clientAddress;
    }
    
    /**
     * Gets whether or not the tutor client is embedded within the GIFT Mobile App
     * 
     * @return whether the client is embedded in the mobile app
     */
    public boolean isMobile() {
        return mobileAppProperties != null;
    }
    
    /**
     * Return whether the tutor client is embedded within the GIFT Mobile App
     * @return can be null
     */
    public MobileAppProperties getMobileAppProperties() {
        return mobileAppProperties;
    }

    /**
     * Sets whether or not the tutor client is embedded within the GIFT Mobile App
     * 
     * @param isMobileClient whether the client is embedded in the mobile app
     */
    public void setMobileAppProperties(MobileAppProperties properties) {
        this.mobileAppProperties = properties;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[WebClientInformation: ");
        sb.append("clientAddress = ").append(getClientAddress());
        sb.append(", mobileAppProperties = ").append(getMobileAppProperties());
        sb.append("]");
        
        return sb.toString();
    }
}
