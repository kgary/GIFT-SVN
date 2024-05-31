/*
* Copyright Dignitas Technologies, LLC
* This file and its contents are governed by one or more distribution and
* copyright statements as described in the LICENSE.txt file distributed with
* this work.
*/            
package mil.arl.gift.common.io;

import java.awt.GraphicsEnvironment;

import mil.arl.gift.common.util.StringUtils;

/**
 * An API that provides utilities for determining details about the execution platform GIFT is running
 * on within the JVM. This can be used to determine information about the operating system that is being
 * used to run GIFT or whether the platform is a headless environment that does not have a GUI.
 * 
 * @author nroberts
 */
public class PlatformUtils {

    /** 
     * A system property that defines what family of operating systems GIFT is running on. This is expected
     * to be provided to the running Java process via GIFT/scripts/util/launchProcess.xml.
     */
    private static final String GIFT_OS_FAMILY_PROPERTY = "gift.os.family";

    /**
     * An enumeration describing the families of operating systems that GIFT supports
     * 
     * @author nroberts
     */
    public enum SupportedOSFamilies{
        
        /** A family for all Microsoft Windows operating systems (e.g. Windows 7, 8, 10, etc.) */
        WINDOWS,
        
        /** A family for Unix-based operating systems (e.g. Linux distributions [CentOS, Ubuntu, etc.] */
        UNIX;
    }
    
    /**
     * Gets the family of operating systems that GIFT is currently running on
     * 
     * @return the operating system family
     */
    public static SupportedOSFamilies getFamily() {
        
        String giftOsFamily = System.getProperty(GIFT_OS_FAMILY_PROPERTY);
        if(StringUtils.isBlank(giftOsFamily)) {
            throw new UnsupportedOperationException("No operating system family was detected. This likely occured because GIFT is either running "
                    + "on an operating system that is not currenly supported or because the \"" + GIFT_OS_FAMILY_PROPERTY + "\" property was not "
                    + "provided to the running Java process by GIFT/scripts/util/launchProcess.xml");
        }
        
        return SupportedOSFamilies.valueOf(giftOsFamily);
    }
    
    /**
     * Gets whether the platform GIFT is executing on is a headless environment that does not have a GUI.
     * This is useful for avoiding UI operations when running on platforms that do not support them.
     * 
     * @return whether the execution platform GIFT is running on is headless.
     */
    public static boolean isHeadless() {
        return GraphicsEnvironment.isHeadless();
    }
}
