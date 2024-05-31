/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.util.Collection;

import mil.arl.gift.common.util.StringUtils;

/**
 * The Initialize Interoperability Connections is used to initialize/enable specific interop interfaces for a
 * lesson/domain-session.
 * 
 * @author mhoffman
 *
 */
public class InitializeInteropConnections{
    
    /** the interop interface class implementation names  */
    private Collection<String> interops = null;
    
    /** 
     * the address of the domain server (currently jetty) that hosts domain content 
     * (e.g. http://10.1.21.123:8885)
     */
    private String domainServerAddress = null;
    
    /** (optional) the name of the observer controller requesting these interop connections via the Game Master interface */
    private String requestingObserver;
    
    /** whether a playback of a domain session log is currently happening which could have implications
     * on how this interop plugin handles messages going in/out. */
    private boolean isPlayback = false;
    
    /**
     * Class constructor - set the necessary attributes
     * 
     * @param domainServerAddress the address of the domain server (currently jetty) that hosts domain content
     * Can't be null or an empty string. (e.g. http://10.1.21.123:8885)
     * @param interops the interop interface class implementation names.  Can't be null.  Currently
     * can't be empty.
     */
    public InitializeInteropConnections(String domainServerAddress, Collection<String> interops){
        this(domainServerAddress, interops, null);
    }
    
    /**
     * Class constructor - set the necessary attributes
     * 
     * @param domainServerAddress the address of the domain server (currently jetty) that hosts domain content
     * Can't be null or an empty string. (e.g. http://10.1.21.123:8885)
     * @param interops the interop interface class implementation names.  Can't be null.  Can be empty if the requesting
     * observer is provided to turn off currently connected connections.
     * @param requestingObserver the username of the observer controller requesting these interop connections 
     * via the Game Master interface. Can be null, if this request is not being made by an observer controller.
     */
    public InitializeInteropConnections(String domainServerAddress, Collection<String> interops, String requestingObserver){
        
        if(interops == null || (StringUtils.isBlank(requestingObserver) && interops.isEmpty())){
            throw new IllegalArgumentException("The interops can't be null and can only be empty if the requesting observer is provided.");
        }
        
        this.interops = interops;
        
        if(domainServerAddress == null || domainServerAddress.isEmpty()){
            throw new IllegalArgumentException("The domain server address of '"+domainServerAddress+"' is not valid.");
        }
        
        this.domainServerAddress = domainServerAddress;
        this.requestingObserver = requestingObserver;
    }
    
    /**
     * Return the container of interop interfaces
     * 
     * @return the gateway module interop plugin classes to enable.  Won't be null.  Can be empty if the requesting
     * observer is provided to turn off currently connected connections. E.g. gateway.interop.vbsplugin.VBSPluginInterface
     */
    public Collection<String> getInterops(){
    	return interops;
    }	
    
    /**
     * Return the the address of the domain server (currently jetty) that hosts domain content
     * (e.g. http://10.1.21.123:8885)
     * 
     * @return  Won't be null or an empty string. (e.g. http://10.1.21.123:8885)
     */
    public String getDomainServerAddress(){
        return domainServerAddress;
    }

    /**
     * Gets the name of the observer controller requesting these interop connections via the Game Master interface
     * 
     * @return the requesting observer. Can be null, if interop connections are not being initialized from the
     * Game Master interface.
     */
	public String getRequestingObserver() {
        return requestingObserver;
    }

    /**
     * Return whether a playback of a domain session log is currently happening which could have implications
     * on how interop plugins handle messages going in/out.
     * @return the current playback flag value
     */
    public boolean isPlayback() {
        return isPlayback;
    }

    /**
     * Set whether a playback of a domain session log is currently happening which could have implications
     * on how interop plugins handle messages going in/out.
     * @param isPlayback true if currently performing playback
     */
    public void setPlayback(boolean isPlayback) {
        this.isPlayback = isPlayback;
    }

    @Override
    public String toString(){

		StringBuffer sb = new StringBuffer();
		sb.append("[InitializeInteropConnections: ");
		sb.append("domainServerAddress = ").append(getDomainServerAddress());
		
		sb.append(", interops = {");
		for(String implName : interops){
		    sb.append(implName).append(", ");
		}
		sb.append("}");
		
		sb.append(", requestingObserver = ").append(getRequestingObserver());
		sb.append(", playback = ").append(isPlayback());
		
		sb.append("]");

		return sb.toString();
    }
}
