/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.dashboard.client.bootstrap.gamemaster.aar;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * An <a href='https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/Error'>error</a>
 * that can be thrown by a video element when the browser is unable to process video data passed into it.
 * 
 * @author nroberts
 */
public class VideoError extends JavaScriptObject {

    /* A no-arg constructor required by JavaScriptObject */
    protected VideoError() {}
    
    /**
     * Gets the name of the error type. Depending on the error and the browser,
     * this may be undefined (or null, in Java code).
     * 
     * @return the error name. Can be null.
     */
    public final native String name()/*-{
        return this.name;
    }-*/;
    
    /**
     * Gets the error message.
     * 
     * @return the error message. Can be null.
     */
    public final native String message()/*-{
        return this.message;
    }-*/;
}
