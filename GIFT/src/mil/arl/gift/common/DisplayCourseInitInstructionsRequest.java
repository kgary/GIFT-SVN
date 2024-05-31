/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Contains information needed for display course initialization instructions.
 * 
 * @author mhoffman
 *
 */
public class DisplayCourseInitInstructionsRequest implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /**
     * Collection of URLs that can be reached by the tutor client and should be downloaded
     * onto the client's computer for use in the current GIFT course execution.
     * Example: Java Web Start Gateway module JNLP file
     * Can be null or empty.
     * 
     * In order for this list to be passed over GWT RPC calls, we need to ensure that the 
     * internal data representation is a list type that implements Serializable, 
     * such as an ArrayList.
     */
    private ArrayList<String> assetURLs;
    
    /**
     * Enumerated gateway module states related to initializing a course.
     * 
     * NOT_CONNECTED - the gateway module isn't online yet
     * CONNECTED - the gateway module is online and allocated to the domain session
     * READY - the gateway module is online, allocated and configured.  It is ready to be used to communicate with training apps.
     * 
     * @author mhoffman
     *
     */
    public enum GatewayStateEnum{
        NOT_CONNECTED,
        CONNECTED,
        READY
    }
    
    /** the current gateway module state */
    private GatewayStateEnum gatewayState = GatewayStateEnum.NOT_CONNECTED;
    
    /**
     * Default constructor
     */
    public DisplayCourseInitInstructionsRequest(){

    }
    
    /**
     * Set the attribute(s).
     * 
     * @param assetURLs Collection of URLs that can be reached by the tutor client and should be downloaded
     * onto the client's computer for use in the current GIFT course execution.
     */
    public DisplayCourseInitInstructionsRequest(ArrayList<String> assetURLs){
        this.assetURLs = assetURLs;
    }

    /**
     * Add the URL to the collection or asset URLs.
     * 
     * @param url should be a URL that can be reached by the tutor client.  Can't be null or empty string
     */
    public void addAssetURL(String url){
        
        if(url == null || url.isEmpty()){
            throw new IllegalArgumentException("The url can't be null or empty.");
        }else if(assetURLs == null){
            assetURLs = new ArrayList<>();
        }
        
        assetURLs.add(url);
    }
    
    /**
     * Return the collection of asset URLs.
     * 
     * @return can be null or empty.
     */
    public ArrayList<String> getAssetURLs(){
        return assetURLs;
    }
    
    /**
     * Gets the current gateway module state.
     * 
     * @return the current gateway module state
     */
    public GatewayStateEnum getGatewayState(){
        return gatewayState;
    }
    
    /**
     * Set the current gateway module state.
     * 
     * @param gatewayStateEnum the gateway state
     */
    public void setGatewayState(GatewayStateEnum gatewayStateEnum){
        
        if(gatewayStateEnum == null){
            throw new IllegalArgumentException("The gateway state enum can't be null.");
        }
        
        this.gatewayState = gatewayStateEnum;
    }
    
    @Override
    public String toString(){
        
        StringBuffer sb = new StringBuffer();
        sb.append("[DisplayCourseInitInstructionsRequest: ");
        sb.append("gatewayState = ").append(getGatewayState());
        sb.append(", assetURLs = ").append(getAssetURLs());
        sb.append("]");
        return sb.toString();
    }
}
