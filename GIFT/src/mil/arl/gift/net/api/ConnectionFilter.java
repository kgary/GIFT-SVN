/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.net.api;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mil.arl.gift.common.module.ModuleStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to filter out potential module connections.
 * 
 * @author mhoffman
 *
 */
public class ConnectionFilter {
    
    /** instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(ConnectionFilter.class);

    /** Required address or addresses to match for this filter (optional) 
     *  Note this can be a a list of addresses, such as addresses for localhost. As long as the ip
     *  matches one of the required addresses, it will be considered accepted by the connection filter.  In that sense
     *  the required address list could be thought of a type of whitelist.  The required address list can be
     *  empty which means there is no required addresses (but ignored addresses may be considered).
     */
    private List<String> requiredAddress = new ArrayList<>(0);
    
    /** list of addresses to ignore, i.e. filter out, (optional)*/
    private List<String> ignoreAddresses = new ArrayList<>(0);
    
    /** the status of a module that must be the selected module (optional) */
    private ModuleStatus requiredModule = null;
    
    /**
     * Default constructor
     */
    public ConnectionFilter(){
        
    }
    
    /**
     * Construct this instance with the attributes of the provided filter instance.
     * @param filterToCopy if null the constructed filter will contain only default values, if any.
     */
    public ConnectionFilter(ConnectionFilter filterToCopy){
        
        if(filterToCopy != null){
            addAllIgnoreAddresses(filterToCopy.getIgnoreAddresses());
            addAllRequiredAddresses(filterToCopy.getRequiredAddress());
            setRequiredModule(filterToCopy.getRequiredModule());
        }
    }
    
    /**
     * Return whether or not the address passes the filter parameters.
     * 
     * @param moduleStatus - the address to check.  If null, this method will return false.
     * @return true iff the address passes the filter check
     */
    public boolean accept(ModuleStatus moduleStatus){
        
        boolean accept = true;
        
        if(moduleStatus != null){
            String address = moduleStatus.getQueueName();
            
            if(requiredAddress != null  && !requiredAddress.isEmpty()){
                //check required address
                
                
                if(!isAddressInRequiredList(address)){
                    //the required address is specified but the provided address doesn't match
                    logger.info("Not accepting address of "+address+" because it doesn't match the required address for "+this);
                    accept = false;
                }
                
            }
            
            if(accept && isIgnoreAddress(address)){
                //make sure the address isn't on the ignore list
                
                logger.info("Not accepting address of "+address+" because it is in the ignore list for "+this);
                accept = false;
            }
            
            if(requiredModule != null){
                accept = moduleStatus.equals(requiredModule);
            }
            
        }else{
            logger.info("Not accepting null moduleStatus for "+this);
            accept = false;
        }

        return accept;
    }
    
    public boolean isIgnoreAddress(String address){
        
        if(ignoreAddresses != null){
            
            for(String ignoreAddress : ignoreAddresses){
                
                if(ignoreAddress != null && ignoreAddress.equalsIgnoreCase(address)){
                    //the provided address is in the list of addresses to ignore
                    return true;
                }
            }

        }

        return false;
    }
    
    /**
     * Adds a required address to the list of addresses that are considered
     * required for this filter.
     * 
     * @param requiredAddress - ip address to be added, normally an IPv4 address.  Can't be null.
     */
    public void addRequiredAddress(String requiredAddress) {
        
        if(requiredAddress == null){
            throw new IllegalArgumentException("The required address to add can't be null.");
        }
        
        if(!this.requiredAddress.contains(requiredAddress)){
            logger.debug("Adding address to ConnectionFilter: " + requiredAddress);
            this.requiredAddress.add(requiredAddress);
        }
    }
    
    /**
     * Add a collection of addresses to the required address list.
     * 
     * @param requiredAddresses - list of addresses, normally an IPv4 address (cannot be null).
     */
    public void addAllRequiredAddresses(List<String> requiredAddresses){
        
        for(String address : requiredAddresses){
            addRequiredAddress(address);
        }
        
    }
    
    /**
     * Add a collection of InetAddresses to the required address list.
     * 
     * @param requiredAddresses - list of addresses, normally an IPv4 address (cannot be null).
     */
    public void addAllRequiredAddresses(Collection<InetAddress> requiredAddresses) {
        for(InetAddress address : requiredAddresses) {
            addRequiredAddress(address.getHostAddress());
        }
        
    }
    
    /**
     * Return the required address to match for this filter.
     * 
     * @return String - can be null if this optional parameter was not provided
     */
    public List<String> getRequiredAddress(){
        return requiredAddress;
    }
    
    /**
     * Return the required module information.
     * 
     * @return a module status for the module to filter on when selecting a module.  Can be null.
     */
    public ModuleStatus getRequiredModule(){
        return requiredModule;
    }
    
    /**
     * Set the required module information.
     * 
     * @param requiredModule the status of a module that must be used
     */
    public void setRequiredModule(ModuleStatus requiredModule){
        this.requiredModule = requiredModule;
    }

    /**
     * Add an address to ignore to the current list.
     * 
     * @param address - the address to add
     */
    public void addIgnoreAddress(String address){
        
        if(!ignoreAddresses.contains(address)){
            ignoreAddresses.add(address);
        }
    }
    
    /**
     * Add a collection of addresses to the ignore address list.
     * 
     * @param addresses - collection of addresses to ignore
     */
    public void addAllIgnoreAddresses(Collection<String> addresses){
        
        for(String address : addresses){
            addIgnoreAddress(address);
        }
    }

    /**
     * Checks to see if an address is in the required list.  Note that it compares
     * the addressStr to see if it contains an ip that is in our required list.
     * For example, a topic of "Gateway_Queue:10.1.1.1:Inbox" can be passed into this
     * function as an 'address'.  The entire string will be scanned to see if it contains
     * an ip address that is in the requiredAddress list.
     * 
     * @param addressStr - the address to check.  If null, this method will return false.  
     *                     This can be an ip address such as "10.1.1.1" or a topic address such as "Gateway_Queue:10.1.1.1:Inbox"
     * @return - True if the address is contained in the list of required addresses.
     */
    public boolean isAddressInRequiredList(String addressStr) {
        boolean isFound = false;
        
        if (addressStr != null && !addressStr.isEmpty()) {
            for (String address : requiredAddress) {
                if (addressStr.contains(address)) {
                    isFound = true;
                    break;
                }
            }
        }
        
        
        return isFound;
    }
    
    /**
     * Return the collection of addresses to ignore.
     * 
     * @return List<String> - collection of addresses.  Can be empty.
     */
    public List<String> getIgnoreAddresses(){
        return ignoreAddresses;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[ConnectionFilter: ");
        sb.append("required Module = ").append(getRequiredModule());
        sb.append(", required Address = {");
        for (String address : getRequiredAddress()) {
            sb.append(" ").append(address).append(",");
        }
        
        sb.append(", ignore addresses = {");
        for(String address : getIgnoreAddresses()){
            sb.append(" ").append(address).append(",");
        }
        sb.append("}");
        
        sb.append("]");
        return sb.toString();
    }

    
}
