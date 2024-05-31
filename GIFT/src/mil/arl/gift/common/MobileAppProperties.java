/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common;

import java.io.Serializable;

/**
 * A set of detailed information about a particular instance of the GIFT Mobile App and the device it is currently running on
 * 
 * @author nroberts
 */
public class MobileAppProperties implements Serializable {

    private static final long serialVersionUID = 1L;
    
    /** The operating system platform of the mobile device that the mobile app is running on (i.e. Android, iOS, etc.) */
    private String platform;
    
    /** The operating system version of the mobile device that the mobile app is running on (e.g. 7.0.0) */
    private String version;
    
    /** The width of the mobile device that the mobile app is running on in <i>physical</i> pixels. */
    private Integer screenWidth;
    
    /** The height of the mobile device that the mobile app is running on in <i>physical</i> pixels. */
    private Integer screenHeight;
    
    /**
     * Creates an empty set of mobile app properties
     */
    public MobileAppProperties() {}

    /**
     * Gets the operating system platform of the mobile device that the mobile app is running on (i.e. Android, iOS, etc.)
     * 
     * @return the operating system platform of the device
     */
    public final String getPlatform(){
        return this.platform;
    }
    
    /**
     * Sets the operating system platform of the mobile device that the mobile app is running on (i.e. Android, iOS, etc.)
     * 
     * @param platform the operating system platform of the device
     */
    public void setPlatform(String platform) {
        this.platform = platform;
    }
    
    /**
     * Gets the operating system version of the mobile device that the mobile app is running on (e.g. 7.0.0)
     * 
     * @return the operating system version
     */
    public final String getVersion(){
        return this.version;
    }
    
    /**
     * Sets the operating system version of the mobile device that the mobile app is running on (e.g. 7.0.0)
     * 
     * @param version the operating system version
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Gets the width of the mobile device that the mobile app is running on in <i>physical</i> pixels. This should not be confused with
     * the width of the mobile app's viewport in CSS pixels, as those are dependent on the  
     * <a href='https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio'>device pixel ratio</a>.
     * 
     * @return the width of the device in physical pixels
     */
    public final Integer getScreenWidth(){
        return this.screenWidth;
    }
    
    /**
     * Sets the width of the mobile device that the mobile app is running on in <i>physical</i> pixels
     * 
     * @param screenWidth the width of the device in physical pixels
     */
    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }
    
    /**
     * Gets the height of the mobile device that the mobile app is running on in <i>physical</i> pixels. This should not be confused with
     * the height of the mobile app's viewport in CSS pixels, as those are dependent on the  
     * <a href='https://developer.mozilla.org/en-US/docs/Web/API/Window/devicePixelRatio'>device pixel ratio</a>.
     * 
     * @return the width of the device in physical pixels
     */
    public final Integer getScreenHeight(){
        return this.screenHeight;
    }
    
    /**
     * Sets the height of the mobile device that the mobile app is running on in <i>physical</i> pixels
     * 
     * @param screenWidth the height of the device in physical pixels
     */
    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append("[MobileAppProperties : platform = ").append(platform)
                .append(", version = ").append(version)
                .append(", screenWidth = ").append(screenWidth)
                .append(", screenHeight = ").append(screenHeight)
                .append("]")
                .toString();
    }
}
