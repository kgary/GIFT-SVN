/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.remote;

/**
 * Constants related to launching modules, either locally or remotely
 * 
 * @author cragusa
 */
public interface LaunchConstants {
    
    /** Prefix used with GIFT host messages */
    String REMOTE_HEARTBEAT_PREFIX = "GIFT HOST:";
    
    /** System property name for the user directory */
    String USER_DIR = "user.dir";
           
    /** Relative path to the launch script on a Windows gift host */
    String WIN_LAUNCH_SCRIPT_REL_PATH   = "/scripts/util/launchProcess.bat";
    
    /** Relative path to the launch script on a Linux gift host */
    String UNIX_LAUNCH_SCRIPT_REL_PATH   = "/scripts/util/launchProcess";
    
    /** Launch command to launch the UMS module */
    String LAUNCH_UMS     = "start ums";

    /** Launch command to launch the LMS module */
    String LAUNCH_LMS     = "start lms";

    /** Launch command to launch the Pedagogical module */
    String LAUNCH_PED     = "start ped";

    /** Launch command to launch the Learner module */
    String LAUNCH_LEARNER = "start learner";

    /** Launch command to launch the Sensor module */
    String LAUNCH_SENSOR  = "start sensor";

    /** Launch command to launch the Domain module */
    String LAUNCH_DOMAIN  = "start domain";

    /** Launch command to launch the Gateway module */
    String LAUNCH_GATEWAY = "start gateway";

    /** Launch command to launch the Tutor module */
    String LAUNCH_TUTOR   = "start tutor";
    
    /** Launch command to launch the DKF Authoring Tool (DAT) */
    String LAUNCH_DAT     = "start dat";
    
    /** Launch command to launch the Sensor Configuration Authoring Tool (SCAT) */
    String LAUNCH_SCAT     = "start scat";
    
    /** Launch command to launch the Learner Configuration Authoring Tool (LCAT) */
    String LAUNCH_LCAT     = "start lcat";
    
    /** Launch command to launch the Course Authoring Tool (CAT) */
    String LAUNCH_CAT     = "start cat";
    
    /** Launch command to launch the Metadata Authoring Tool (MAT) */
    String LAUNCH_MAT     = "start mat";
    
    /** Launch command to launch the Pedagogy Configuration Authoring Tool (PCAT) */
    String LAUNCH_PCAT    = "start pcat";
    
    /** Launch command to launch the Export Tutor tool */
    String LAUNCH_EXPORT_TOOL    = "start export";
    
    /** Launch command to launch the Import tool */
    String LAUNCH_IMPORT_TOOL    = "start import";
    
    String WILDCARD_ADDRESS = "0.0.0.0";
    
    /** Launch command prefix to launch a webpage on the client */
    String LAUNCH_WEBPAGE_COMMAND = "launchWebpage";
    
    /** Array of launch commands for commands typically run on a GIFT Server */
    String[] LAUNCH_SERVER_ALL_COMMAND_ARRAY = new String[] {
            LAUNCH_UMS,
            LAUNCH_LMS,
            LAUNCH_PED,
            LAUNCH_LEARNER,
            LAUNCH_TUTOR
        };    
    
    /** Array of launch commands for commands typically run on a GIFT Learner Station */
    String[] LAUNCH_LEARNER_ALL_COMMAND_ARRAY = new String[] {
            LAUNCH_SENSOR,
            LAUNCH_DOMAIN,
            LAUNCH_GATEWAY                                      
        };
    
}
