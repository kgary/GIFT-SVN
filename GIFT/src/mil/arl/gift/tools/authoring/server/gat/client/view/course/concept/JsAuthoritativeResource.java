/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.server.gat.client.view.course.concept;

import com.google.gwt.core.client.JavaScriptObject;

import generated.course.AuthoritativeResource;

/**
 * A native JavaScript representation of a {@link AuthoritativeResource}. This is used to pass course concepts between GAT editors.
 * 
 * @author nroberts
 */
public class JsAuthoritativeResource extends JavaScriptObject {

    /** No arg constructor required by {@link JavaScriptObject} */
    protected JsAuthoritativeResource() {}
    
    /**
     * Creates a JavaScript object representing the given resource
     * 
     * @param node the resource to represent. If null, null will be returned.
     * @return an equivalent JavaScript object representing the same resource.
     */
    public static JsAuthoritativeResource create(AuthoritativeResource resource) {
        
        if(resource == null) {
            return null;
        }
        
        return create(resource.getId());
    }
    
    private static native JsAuthoritativeResource create(String id)/*-{
        return {
            id : id
        }
    }-*/;
    
    private final native String getId()/*-{
        return this.id;
    }-*/;

    /**
     * Gets the original resource that this JavaScript object was created from. Note that the
     * returned resource is technically a different object created using the same underlying data.
     * 
     * @return the original course concept node. Cannot be null.
     */
    public final AuthoritativeResource getOriginalResource() {
        
        AuthoritativeResource orig = new AuthoritativeResource();
        orig.setId(getId());
        
        return orig;
    }

}
