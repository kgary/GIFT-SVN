/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

import mil.arl.gift.common.io.PlatformUtils;
import mil.arl.gift.common.io.PlatformUtils.SupportedOSFamilies;

/**
 * This class contains tools used to handle management of scripts within GIFT
 *
 * @author jleonard
 */
public class ScriptTools {
    
    /**
     * Returns the full pathname to the launch script.
     *
     * @return String The path to the launch script.
     */
    public static String generateScriptFilePathName() {
        
        /* Invoke the proper script depending on the operating system family*/
        SupportedOSFamilies osFamily = PlatformUtils.getFamily();
        switch(osFamily) {
            case WINDOWS:
                return System.getProperty(LaunchConstants.USER_DIR) + LaunchConstants.WIN_LAUNCH_SCRIPT_REL_PATH;
                
            case UNIX:
                return System.getProperty(LaunchConstants.USER_DIR) + LaunchConstants.UNIX_LAUNCH_SCRIPT_REL_PATH;
                
            default:
                throw new UnsupportedOperationException("Could not get launch script path because no launch script exists for " + osFamily);
        }
    }    
    
    private ScriptTools() {
        
    }
    
}
