/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A JavaScript <a href='https://developer.mozilla.org/en-US/docs/Web/API/Blob'>Blob</a> object
 * containing file-like access to immutable raw data.
 * 
 * @author nroberts
 */
public class Blob extends JavaScriptObject {

    /** Default no-arg constructor required for classes extending JavaScriptObject */
    protected Blob() {}
    
    /**
     * Gets a URL that can be used within the client where this blob was created in order to access its
     * data in operations that require a URL. Note that URLs created by this method will not work in
     * other clients and should not be shared. 
     * (see <a href='https://developer.mozilla.org/en-US/docs/Web/API/URL/createObjectURL'>URL.createObjectUrl()</a>)
     * 
     * @return a URL that can be used to reference this blob's data.
     */
    public final native String getLocalUrl()/*-{
        
        if(this.localUrl == null){
            this.localUrl = URL.createObjectURL(this);
        }
        
        return this.localUrl;
    }-*/;
    
    /**
     * Used to create an empty blob instance.
     * @return the new Blob
     */
    public final static native Blob create()/*-{
    return new Blob();
    }-*/;

}
