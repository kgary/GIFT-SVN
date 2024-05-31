/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import mil.arl.gift.common.io.UserEnvironmentUtil;
import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage;

/**
 * Unique setting names used by the wizard
 * 
 * @author mhoffman
 */
public class InstallSettings {
	
    /**
     * The various setting keys
     */
    public static final String EXTRACT_UMS_DATABASE     = "EXTRACT_UMS_DATABASE";
    public static final String EXTRACT_LMS_DATABASE     = "EXTRACT_LMS_DATABASE";
    public static final String VBS_HOME                 = TrainingApplicationInstallPage.VBS_HOME;
    public static final String DE_TESTBED_HOME          = TrainingApplicationInstallPage.DE_TESTBED_HOME; 
    public static final String PPT_HOME                 = TrainingApplicationInstallPage.PPT_HOME;
    public static final String UNITY_LAND_NAV_DOWNLOAD           = TrainingApplicationInstallPage.UNITY_LAND_NAV_DOWNLOAD;
    public static final String MEDIA_SEMANTICS          = "MEDIA_SEMANTICS";
    public static final String PYTHON_HOME	            = UserEnvironmentUtil.GIFT_PYTHON_HOME;
    public static final String VR_ENGAGE_HOME                 = TrainingApplicationInstallPage.VR_ENGAGE_HOME;
}
