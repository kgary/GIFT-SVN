/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.authoring.desktop.dat.custnodes;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import mil.arl.gift.tools.authoring.desktop.common.XMLAuthoringToolSelectionDialog;

/**
 * This custom dialog provides the list of available scripts local to the auto tutor web service.
 * 
 * @author mhoffman
 *
 */
public class AutoTutorScriptSelectionDialog extends
        XMLAuthoringToolSelectionDialog {

    private static final long serialVersionUID = 1L;

    /** title of the dialog */
    private static final String TITLE = "AutoTutor Script Selection";
    
    /** information label shown on the dialog */
    private static final String LABEL = "Please select an existing AutoTutor (AT) script (SKO) that contains the necessary\n" +
    		"information for an Auto Tutor session in GIFT.\n\n" +
    		"Use the AutoTutor Scripting Authoring Tool (ASAT) to author an AT script which is available as either a\n" +
    		"desktop application (GIFT\\external\\ASATForGIFT.zip) OR\n" +
    		"web-based interface (http://asat.gifttutoring.org/?helpOn=1).";   
    
    /** there are no default values for this dialog */
    private static final List<SelectionItem> DEFAULT_VALUES = new ArrayList<>(0);
    
    /**
     * Default constructor
     */
    public AutoTutorScriptSelectionDialog(){
        super(TITLE, LABEL, DEFAULT_VALUES, false);
    }
    
    @Override
    public Object[] getCustomValues() {
            
        //show error message dialog
        JOptionPane.showMessageDialog(this,
                "As of 8/18 this is no longer supported by AutoTutor ACE server.",
                "No Auto Tutor scripts available",
                JOptionPane.ERROR_MESSAGE);
        
        return null;

    }

    @Override
    public void addUserEntry(String value) {
        // not supported for this dialog
    }

}
