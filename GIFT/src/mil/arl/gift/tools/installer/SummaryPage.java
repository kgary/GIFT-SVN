/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;

import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;

/**
 * Shows the user a summary of what will be done during the installation process.
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
		    summary.setText("<html><b>&nbsp;&nbsp;&nbsp;&nbsp;The following is a summary of the installation that will be performed:</b><br></html>");
            add(summary);
            add(Box.createVerticalStrut(5));
            
            String installSummary = "<html><p style=\"margin-left:5px;\"><ul>";
		    
		    //build GIFT -
		    if(InstallThread.isGIFTBuilt()){
		        installSummary += "<li>GIFT has already been built for you.</li>";
		    }else{
		        installSummary += "<li>GIFT source code will be built.</li>";
		    }
		    
		    //Database extraction - 
		    boolean umsDb = isPropertyTrue(settings.get(InstallSettings.EXTRACT_UMS_DATABASE));
		    boolean lmsDb = isPropertyTrue(settings.get(InstallSettings.EXTRACT_LMS_DATABASE));
		    if(umsDb && lmsDb){
		        installSummary += "<li>GIFT UMS and LMS databases will be installed.</li>";
		    }else if(lmsDb){
	            installSummary += "<li>GIFT LMS databases will be installed.</li>";
		    }else if(umsDb){
                installSummary += "<li>GIFT UMS databases will be installed.</li>";
		    }else{
		        installSummary += "<li>No changes will be made to the existing GIFT databases.</li>";
		    }
		    
		    //
		    // Avatar
		    //
		    
		    //Media Semantics Character
		    if(isPropertyTrue(settings.get(InstallSettings.MEDIA_SEMANTICS))){
	              installSummary += "<li style=\"margin-bottom: 10px;\">Media Semantics Character will be installed.<br><br>" +
	              		"<b>Note:</b> please read the GIFT Media Semantics Character Server instructions for more information.</li>";
		    }
		    
		    //
		    // Training Applications -
		    //
		    
		    //VBS
		    if(settings.containsKey(InstallSettings.VBS_HOME)){
		        installSummary += "<li style=\"margin-bottom: 10px;\">VBS will be configured to work with GIFT.<br><br>" +
		        		"<b>Note:</b> please read the GIFT install instructions for additional VBS scenario setup procedures,<br>" +
		        		"including how to install additional VBS terrain and model files needed for VBS scenarios<br>" +
		        		"referenced by GIFT courses.<br><br>"+
		        		"<b>Note:</b> make sure that VBS is not running before starting the GIFT install.</li>";
		    }
		    
            //DE Testbed
            if(settings.containsKey(InstallSettings.DE_TESTBED_HOME)){
                installSummary += "<li>The DE Testbed will be configured to work with GIFT.</li>";
            }else{
                installSummary += "<li>Disabling the Gateway configuration for DE Testbed.</li>";
            }
            
            //PowerPoint
            if(settings.containsKey(InstallSettings.PPT_HOME)){
                installSummary += "<li>PowerPoint will be configured to work with GIFT.</li>";
            }else{
                installSummary += "<li>Disabling the Gateway configuration for PowerPoint.</li>";
            }

            // Unity Land Nav GIFT Wrap
            if(isPropertyTrue(settings.get(InstallSettings.UNITY_LAND_NAV_DOWNLOAD))){
                installSummary += "<li>The Unity Land Nav scenario will be downloaded for GIFT Wrap.</li>";
            }
            
            //VR-Engage
            if(settings.containsKey(InstallSettings.VR_ENGAGE_HOME)){
                installSummary += "<li style=\"margin-bottom: 10px;\">VR-Engage will be configured to work with GIFT.<br><br>" +
                        "<b>Note:</b> make sure that VR-Engage is not running before starting the GIFT install.</li>";
            }
		    
            //WinPython
            if(settings.containsKey(InstallSettings.PYTHON_HOME)){
                installSummary += "<li>Python will be configured to work with GIFT.</li>";
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
