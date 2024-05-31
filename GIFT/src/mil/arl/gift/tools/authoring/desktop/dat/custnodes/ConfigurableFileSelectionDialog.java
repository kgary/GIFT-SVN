/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.io.File;

import mil.arl.gift.common.io.DetailedException;
import mil.arl.gift.tools.authoring.desktop.common.custnodes.AbstractConfigurableFileSelectionDialog;
import mil.arl.gift.tools.authoring.desktop.dat.DAT;

/**
 * This instance of the configurable file selection dialog is used by the DAT.
 * 
 * @author mhoffman
 *
 */
public class ConfigurableFileSelectionDialog extends
        AbstractConfigurableFileSelectionDialog {

    private static final long serialVersionUID = 1L;

    @Override
    public File getCourseFolder() throws DetailedException {
        return DAT.getInstance().getDATForm().updateCourseFolder();
    }

}
