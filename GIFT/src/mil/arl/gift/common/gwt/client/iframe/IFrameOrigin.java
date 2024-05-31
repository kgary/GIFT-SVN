/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client.iframe;


/**
 * The IFrameOrigin class encapsulates the data needed to identify what the 'origin' is.
 * In this case, we specify the url of the origin, the iframe 'id' which is the id that the iframe
 * will have in the html document, as well as a 'key' which can be used to identify the origin.
 * 
 * @author nblomberg
 *
 */
public class IFrameOrigin {

    // The url for the origin.
    private String originUrl = "";
    // The iframe id which is the 'id' that the iframe will have in the html document.
    private String iFrameId = "";
    // A unique key or identifier of the origin that will be used if the origins get put into a map structure.
    private String originKey = "";
   
    /**
     * Contructor
     */
    public IFrameOrigin() {
        
    }
    
    /**
     * Constructor 
     * @param url - url of the origin
     * @param frameId - iFrameId of that the origin will have in the html document.
     * @param key - Unique identifier that can be used to identify the origin.
     */
    public IFrameOrigin(String url, String frameId, String key) {
        this();
        
        originUrl = url;
        iFrameId = frameId;
        originKey = key;
        
    }
    
    /**
     * Accessor to retrieve the origin url.
     * @return String - string of the url for the origin.
     */
    public String getOriginUrl() {
        return originUrl;
    }

    /**
     * Acccessor to set the origin url.
     * @param originUrl - the url for the origin. (example: "http://localhost:8080")
     */
    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }
    
    /**
     * Set the original Url by first removing the end of the Url provided.
     * e.g. http://localhost:8080/sas origin value will be http://localhost:8080
     * e.g. localhost:8080/ert origin value will be localhost:8080
     * e.g. gifttutor.org/gat origin value will be gifttutor.org
     * 
     * @param fullURL the url to get the origin url from
     */
    public void setOriginUrlFromFullUrl(String fullURL){
        
        int afterHttpIndex = 0;
        if(fullURL.contains("//")){
            afterHttpIndex = fullURL.indexOf("//")+2;
        }

        int endOriginUrlIndex = fullURL.indexOf("/", afterHttpIndex);
        if(endOriginUrlIndex == -1){
            setOriginUrl(fullURL);
        }else{
            setOriginUrl(fullURL.substring(0, endOriginUrlIndex));
        }
    }

    /**
     * Accessor to get the iFrameId of the origin.
     * @return String - The iFrameId of the origin.
     */
    public String getIFrameId() {
        return iFrameId;
    }

    /**
     * Accessor to set the iFrameId of the origin.
     * @param originFrameId - The iFrameId of the origin.  Should not be null.  This is the frame id that will identify the frame tag in the html document.
     */
    public void setIFrameId(String originFrameId) {
        this.iFrameId = originFrameId;
    }
    
    /**
     * Accessor to get the origin key.
     * @return String - The unique identifier of the origin.
     */
    public String getOriginKey() {
        return originKey;
    }

    /**
     * Accessor to set the origin key.
     * @param key - An identifier which can be used to specify the origin (if there are multiples).
     */
    public void setOriginKey(String key) {
        this.originKey = key;
    }
      

    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        sb.append("[IFrameOrigin: ");
        sb.append("originKey = ").append(getOriginKey());
        sb.append(", originUrl = ").append(getOriginUrl());
        sb.append(", originFrameId = ").append(getIFrameId());
        sb.append("]");

        return sb.toString();
    }
    
    
    
}
