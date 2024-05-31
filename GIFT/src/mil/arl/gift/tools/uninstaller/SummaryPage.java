/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.uninstaller;

import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Shows the user a summary of what will be done during the uninstallation process.
 * 
 * @author mhoffman
 */
public class SummaryPage extends WizardPage {
	
	/** Generated serial */
	private static final long serialVersionUID = -8227208053911011410L;
	
	/** page label values */
	private static final String TITLE = "Summary";
	private static final String DESCRIPTION = "Summary";
	
	/**
	 * Creates a new SummaryPage that will summarize settings.
	 * 
	 * @param settings The settings to summarize.
	 */
	public SummaryPage(WizardSettings settings) {
		super(TITLE, DESCRIPTION);
		setupUi(settings);
	}
	
	
	@Override
	public void rendering(List<WizardPage> path, WizardSettings settings) {
		setNextEnabled(false);
		setFinishEnabled(true);
	}
	
	/**
     * Return whether the property is a boolean with the value of true.
     * 
     * @param value the property object to check
     * @return boolean is the property a boolean with the value of true
     */
    private boolean isPropertyTrue(Object value){
        return value != null && value instanceof Boolean && (boolean)value;
    }
	
    /**
     * Sets up all of the swing components
     */
	private void setupUi(final WizardSettings settings) {
	    
		if(settings != null) {
		    
		    BoxLayout layout = new BoxLayout(this, BoxLayout.Y_AXIS);
		    setLayout(layout);
		    
		    add(Box.createVerticalStrut(10));
		    
		    JLabel summary = new JLabel();
		    summary.setText("<html><b>&nbsp;&nbsp;&nbsp;&nbsp;The following is a summary of the items to be changed on this computer as part<br>&nbsp;&nbsp;&nbsp;&nbsp;of the GIFT uninstall:</b><br></html>");
            add(summary);
            add(Box.createVerticalStrut(5));
            
            String installSummary = "<html><p style=\"margin-left:5px;\"><ul>";

		    //
		    // Training Applications -
		    //
		    
		    //VBS
            if(isPropertyTrue(settings.get(UninstallSettings.VBS_UNINSTALL))){
                installSummary += "<li>VBS GIFT configuration will be removed, along with GIFT files copied to your VBS installation folder.<br><br><b>Note:</b> Please make sure VBS is not running for this to succeed.<br>&nbsp;</li>";
            }
  
            //DE Testbed
            if(isPropertyTrue(settings.get(UninstallSettings.DE_TESTBED_UNINSTALL))){
                installSummary += "<li>DE Testbed GIFT configuration will be removed</li>";
            }

          
            //PowerPoint
            if(isPropertyTrue(settings.get(UninstallSettings.PPT_UNINSTALL))){
                installSummary += "<li>PowerPoint GIFT configuration will be removed</li>";
            }
            
            //VR-Engage
            if(isPropertyTrue(settings.get(UninstallSettings.VR_ENGAGE_UNINSTALL))){
                installSummary += "<li>VR-Engage GIFT configuration will be removed, along with GIFT files copied to your VR-Engage installation folder.<br><br><b>Note:</b> Please make sure VR-Engage is not running for this to succeed.<br>&nbsp;</li>";
            }
		    
            //WinPython
            if(isPropertyTrue(settings.get(UninstallSettings.PYTHON_UNINSTALL))){
                installSummary += "<li>Python GIFT configuration will be removed</li>";
            }
            
            //Delete GIFT
            if(isPropertyTrue(settings.get(UninstallSettings.DELETE_GIFT))){
                installSummary += "<li><font color='red'>GIFT files will be deleted</font><br><br><b>Note:</b> Please make sure you close all files and folders opened in this<br>GIFT instance for this to succeed</li>";
            }
		    
		    installSummary += "</ul></p></html>";
		    JLabel label = new JLabel();
            label.setText(installSummary);
            add(label);
		    
		    JLabel finish = new JLabel();
		    finish.setText("<html><br>&nbsp;&nbsp;&nbsp;&nbsp;Click <b>Finish</b> to start the installation process...<br></html>");
		    add(finish);
		    
		}
	}	
	
}
