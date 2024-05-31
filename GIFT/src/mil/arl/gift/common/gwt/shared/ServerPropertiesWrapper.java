/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.shared;

import java.util.Map.Entry;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

/**
 * An native JavaScript object used to wrap a set of {@link ServerProperties} so that they
 * can be encoded to and decoded from JSON
 * 
 * @author nroberts
 */
public class ServerPropertiesWrapper extends JavaScriptObject {

    /** Default no-arg constructor required for classes extending JavaScriptObject */
    protected ServerPropertiesWrapper() {}
    
    /**
     * Creates a new wrapper wrapping the given set of properties
     * 
     * @param origProps the original server properties to wrap. If null, the 
     * returned wrapper will contain no properties
     * @return a wrapper wrapping the given set of properties
     */
    final public static ServerPropertiesWrapper wrap(ServerProperties origProps) {
        
        ServerPropertiesWrapper props = create();
        
        if(origProps != null) {
            for(Entry<String, String> prop : origProps.getAllProperties().entrySet()) {
                props.addProperty(prop.getKey(), prop.getValue());
            }
        }
        
        return props;
    }
    
    final private static native ServerPropertiesWrapper create()/*-{
        return {
            props : [] //define an array to store the property keys
        };
    }-*/;
    
    final private native void addProperty(String property, String value)/*-{
        
        //add the property key to the key array
        this.props.push(property);
        
        //use a property accessor to store the key's value
        this[property] = value;
        
    }-*/;

    /**
     * Unwraps this wrapper to get the original server properties used to create it
     * 
     * @return the original properties. Will be null if no property keys were wrapped.
     */
    final public ServerProperties unwrap() {
        
        JsArrayString props = getProperties();
        if(props != null) {
            
            ServerProperties origProps = new ServerProperties();
            
            for(int i = 0; i < props.length(); i++) {
                String property = props.get(i);
                origProps.addProperty(property, getValue(property));
            }
            
            return origProps;
        }
        
        return null;
    }
    
    final private native JsArrayString getProperties()/*-{
        return this.props; //retrieve the array of property key names
    }-*/;

    
    final private native String getValue(String property)/*-{
        return this[property]; //retrieve the property value using the appropriate accessor
    }-*/;

}
