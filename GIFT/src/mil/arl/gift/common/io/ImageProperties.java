/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

/**
 * Used to read and access images properties file that defines the paths to various GIFT images.
 * 
 * @author mhoffman
 *
 */
public class ImageProperties extends CommonProperties {

    /** the properties file name*/
    private static final String PROPERTIES_FILE = "images.properties";
    
    //
    //
    // IF YOU ADD A NEW ENTRY HERE, MAKE SURE IT IS ALSO USED IN
    // 1. tutor/build.xml
    // 2. dashboard/build.xml
    // 3. GIFT/config/image.properties
    //
    //
    
    /**
     * The image used on the Dashboard login page, Tutor (TUI) pages, GAT course/survey/conversation-tree preview, error pages 
     */
    public static final String BACKGROUND             = "Background";
    
    /** 
     * the organization image used in places such as the login page
     */
    public static final String ORGANIZATION_IMAGE = "Organization";
    
    /**
     * icon used for things like the system tray (recommended size: 32x32)
     */
    public static final String SYSTEM_ICON_SMALL             = "System_Icon_Small";
    
    /**
     * image used for the loading dialog that shows progress when GIFT applications are starting
     * Recommended size: width = 400, height = 264
     */
    public static final String APP_LOADING = "App_Loading";
    
    /**
     * image used on Experiment/LTI welcome/resume/completed pages, error pages, Simple login page 
     * Recommended size: width = 200, height = 160
     */
    public static final String LOGO = "Logo";
    
    /** singleton instance of this class */
    private static ImageProperties instance = null;

    /**
     * Return the singleton instance of this class
     *
     * @return singleton instance
     */
    public static synchronized ImageProperties getInstance(){

        if(instance == null){
            instance = new ImageProperties();
        }

        return instance;
    }
    
    private ImageProperties(){
        super(PROPERTIES_FILE);
    }

}
