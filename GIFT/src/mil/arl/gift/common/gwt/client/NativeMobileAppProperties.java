/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.gwt.client;

import com.google.gwt.core.client.JavaScriptObject;
import mil.arl.gift.common.MobileAppProperties;

/**
 * A {@link JavaScriptObject} containing detailed information about a particular instance of the GIFT Mobile App and
 * the device it is currently running on, which is normally provided directly by the mobile app itself to its child iframe.
 * This class is functionally identical to {@link MobileAppProperties} but is used to directly access the values provided by
 * the mobile app via JavaScript calls that can only be used in client-side GWT code. When passing said values to contexts
 * beyond a GWT client, you should first convert this object to a {@link MobileAppProperties} instance using 
 * {@link #toJavaProperties()} and pass that object instead.
 * 
 * @author nroberts
 */
public class NativeMobileAppProperties extends JavaScriptObject{
    
    /*
     * The following properties are also provided by the GIFT Mobile App and can be accessed using methods similar to 
     * the ones below. These properties are not currently used for GIFT's normal interactions with the mobile app, but they
     * can be useful for debugging or information gathering in general.
     * 
     * manufacturer - (String) the name of the device's manufacturer
     * model - (String) the name of the device's hardware model
     * uuid - (String) the UUID that the manufacturer uses to indentify this device
     * serial - (String) the device's serial number
     * isVirtual - (Boolean) whether the device is running virtually in an emulator
     */

    /**
     * Default no-arg constructor. Required by JavaScriptObject policy but should never be called directly.
     */
    protected NativeMobileAppProperties() {}
    
    /**
     * Gets the operating system platform of the mobile device that the mobile app is running on (i.e. Android, iOS, etc.)
     * 
     * @return the operating system platform of the device
     */
    public final native String getPlatform()/*-{
        return this.platform;
    }-*/;
    
    /**
     * Gets the operating system version of the mobile device that the mobile app is running on (e.g. 7.0.0)
     * 
     * @return the operating system version
     */
    public final native String getVersion()/*-{
        return this.version;
    }-*/;
    
    /**
     * Gets the width of the mobile device that the mobile app is running on in <i>physical</i> pixels. This should not be confused with
     * the width of the mobile app's viewport in CSS pixels, as those are dependent on the  
     * <a href='https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio'>device pixel ratio</a>.
     * 
     * @return the width of the device in physical pixels
     */
    public final native Double getScreenWidth()/*-{
        return this.screenWidth;
    }-*/;
    
    /**
     * Gets the height of the mobile device that the mobile app is running on in <i>physical</i> pixels. This should not be confused with
     * the height of the mobile app's viewport in CSS pixels, as those are dependent on the  
     * <a href='https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio'>device pixel ratio</a>.
     * 
     * @return the width of the device in physical pixels
     */
    public final native Double getScreenHeight()/*-{
        return this.screenHeight;
    }-*/;
    
    /**
     * Creates a {@link MobileAppProperties} object that is identical to this set of native properties. This is necessary
     * when said properties need to be used in contexts that do not have access to native JSNI logic, such as server methods
     * and networking logic.
     * 
     * @return a regular Java object that can be passed throughout GIFT without restriction
     */
    public final MobileAppProperties toJavaProperties() {
        
        // Need to wrap the raw JavaScriptObjects into identical created by GWT Java code, since objects created
        // from native JavaScript can't be sent over RPC communications. This is because the objects created
        // by JavaScript aren't actually proper Java objects; they just get cast as Objects by GWT. This prevents them 
        // from being serialized properly for RPCs, since they won't translate to proper Java objects on the server end. 
        // Using GWT Java code to create identical objects avoids this problem by creating proper Java objects that 
        // can be sent over RPCs just fine.
        
        MobileAppProperties commonProperties = new MobileAppProperties();
        
        if(getPlatform() != null) {
            commonProperties.setPlatform(new StringBuilder(getPlatform()).toString());
        }
        
        if(getVersion() != null) {
            commonProperties.setVersion(new StringBuilder(getVersion()).toString());
        }
        
        if(getScreenWidth() != null) {
            /* Round to an integer since some phones give resolution in decimal */
            commonProperties.setScreenWidth(getScreenWidth().intValue());
        }
        
        if(getScreenHeight() != null) {
            /* Round to an integer since some phones give resolution in decimal */
            commonProperties.setScreenHeight(getScreenHeight().intValue());
        }
        
        return commonProperties;
    }
}
