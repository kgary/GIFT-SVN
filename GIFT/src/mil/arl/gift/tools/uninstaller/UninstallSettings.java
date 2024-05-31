/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.uninstaller;

import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage;
import mil.arl.gift.gateway.uninstaller.TrainingApplicationUninstallPage;

/**
 * Unique setting names used by the wizard
 * 
 * @author mhoffman
 */
public class UninstallSettings {
	
    /**
     * The various setting keys
     */
    public static final String VBS_UNINSTALL                 = TrainingApplicationUninstallPage.VBS_UNINSTALL;
    public static final String DE_TESTBED_UNINSTALL          = TrainingApplicationUninstallPage.DE_TESTBED_UNINSTALL; 
    public static final String PPT_UNINSTALL                 = TrainingApplicationUninstallPage.PPT_UNINSTALL;
    public static final String VR_ENGAGE_UNINSTALL           = TrainingApplicationUninstallPage.VR_ENGAGE_UNINSTALL;
    public static final String VBS_HOME                 = TrainingApplicationInstallPage.VBS_HOME;
    public static final String DE_TESTBED_HOME          = TrainingApplicationInstallPage.DE_TESTBED_HOME; 
    public static final String PPT_HOME                 = TrainingApplicationInstallPage.PPT_HOME;
    public static final String VR_ENGAGE_HOME           = TrainingApplicationInstallPage.VR_ENGAGE_HOME;
    public static final String MEDIA_SEMANTICS          = "MEDIA_SEMANTICS";
    public static final String PYTHON_HOME	        = "GIFT_PYTHON_HOME";
    public static final String PYTHON_UNINSTALL      = "PYTHON_UNINSTALL";
    public static final String DELETE_GIFT            = "DELETE_GIFT";
}
