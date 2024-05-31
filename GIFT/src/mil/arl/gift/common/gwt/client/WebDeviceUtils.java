/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

import mil.arl.gift.common.MobileAppProperties;
import mil.arl.gift.common.util.StringUtils;

/**
 * A class containing utility methods that GWT clients can use to interact with the devices on which they are running
 * 
 * @author nroberts
 */
public class WebDeviceUtils {
    
    private static Logger logger = Logger.getLogger(WebDeviceUtils.class.getName());
    
    /** The URL parameter used to tell if a child iframe is embedded within the GIFT Mobile App and obtain the app's properties */
    private static final String MOBILE_APP_EMBEDDED_URL_PARAM = "mobileApp";
    
    /**
     * Gets whether or not this GWT client is running inside the GIFT Mobile App
     * 
     * @return whether this client is running inside the GIFT Mobile App
     */
    public static boolean isMobileAppEmbedded(){
        return getNativeMobileAppProperties() != null;
    }
    
    /**
     * If this GWT client is running inside an the GIFT Mobile App, gets the JavaScript object describing the installed mobile app 
     * and the device it is running on. If this GWT client is NOT running inside the GIFT Mobile app, then this method will
     * always return null.
     * 
     * @return a JavaScript object providing the details of the surrounding GIFT Mobile App install, or null, 
     * if this client is not running inside the mobile app
     */
    private static NativeMobileAppProperties getNativeMobileAppProperties() {
        
        if(isParentWindowTop()) {
            
            // The immediate child iframe of the mobile app has direct access to the mobile app's details via a special
            // property added to its window. This property is explicitly added by the mobile app upon loading said iframe.
            return getMobileAppNative();
            
        } else {
            
            // Other iframes within the first child must have the property shared with them via URL parameters to
            // bypass cross-origin security restrictions
            String encodedDetails = Window.Location.getParameter(MOBILE_APP_EMBEDDED_URL_PARAM);
            
            if(StringUtils.isBlank(encodedDetails)) {

                // No mobile app details have been defined by the query parameter, so this GWT 
                // client is not running inside the GIFT mobile app
                return null;
            } 
            
            // Get the JSON string representing the details from the query string
            String jsonDetails = URL.decodeQueryString(encodedDetails);
            
            try {
                // Get the original JavaScript object from the JSON string
                return JsonUtils.safeEval(jsonDetails);
                
            } catch(Exception e) {
                
                logger.severe("Unable to decode JSON for mobile app details. " + e.toString());
                return null;
            }
        }
    }
    
    /**
     * If this GWT client is running inside an the GIFT Mobile App, gets a set of details describing the installed mobile app 
     * and the device it is running on. If this GWT client is NOT running inside the GIFT Mobile app, then this method will
     * always return null.
     * 
     * @return the details of the surrounding GIFT Mobile App install, or null, if this client is not running inside the mobile app
     */
    public static MobileAppProperties getMobileAppProperties() {
        
        NativeMobileAppProperties nativeProperties = getNativeMobileAppProperties();
        
        if(nativeProperties != null) {
            
            //convert the native JavaScript object to a common Java object that can be used throughout GIFT's client and server code
            return nativeProperties.toJavaProperties();
            
        } else {
            return null;
        }
    }
    
    /**
     * Checks to see if the GIFT Mobile App has explicitly added a property to the surrounding {@link Window} providing details
     * about the GIFT Mobile App and the device it is currently running on. If such a property exists, it will be returned 
     * as a {@link NativeMobileAppDetails} object. This property will only be set for the content window of the
     * GIFT Mobile App's main iframe, so any other windows will always return null.
     * 
     * @return the details of the GIFT Mobile App that this window is embedded within, assuming that this window is the content
     * window of the GIFT Mobile App's main iframe. Null, otherwise.
     */
    private static native NativeMobileAppProperties getMobileAppNative()/*-{
    
        try{
            // Retrieve a property explicitly set by the GIFT Mobile App that tells its main iframe window that it
            // is inside the GIFT Mobile App
            var appProperties = $wnd.mobileApp;
            
            if(appProperties == null && $wnd.isMobileAppEmbedded){
                
                // The surrounding mobile app is an older version that still uses the 'isMobileAppEmbedded' flag, so
                // provide it with a dummy set of properties
                appProperties = {};
            }
            
            return appProperties;
            
        } catch(err){
            return false;
        }
    }-*/;
    
    /**
     * Gets whether the surrounding window's parent window is the topmost frame. 
     * 
     * @return whether the parent window is the topmost frame
     */
    private static native boolean isParentWindowTop()/*-{
        return $wnd.parent === $wnd.top;
    }-*/;
    
    /**
     * Gets a string containing the URL parameter and value needed to tell a child iframe that it is embedded within the GIFT
     * Mobile App and to provide the child iframe with the properties provided by said app. This URL parameter will be 
     * checked whenever {@link #isMobileAppEmbedded()} or {@link #getMobileAppProperties()} are called from within the child iframe.
     * <br/><br/>
     * If the window that invokes this method is not embedded in the mobile app, then this method will simply return null.
     * 
     * @return a string containing the URL parameter and value idenfitying that an iframe is in the GIFT Mobile App, or null,
     * if this window is not embedded within the mobile app
     */
    public static String getMobileAppEmbeddedURLParam() {
        
        NativeMobileAppProperties appDetails = getNativeMobileAppProperties();
        
        if(appDetails == null) {
            
            // No app details are available, so this GWT client is not embedded in the GIFT Mobile App
            return null;
        }
        
        // Encode the GIFT Mobile App's details as a JSON string in the URL query parameter
        return MOBILE_APP_EMBEDDED_URL_PARAM + "=" + URL.encodeQueryString(JsonUtils.stringify(appDetails));
    }
    
}
