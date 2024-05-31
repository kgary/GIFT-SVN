/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.ciscavate.cjwizard.WizardListener;
import org.ciscavate.cjwizard.WizardPage;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Listens to important events fired by the wizard.
 * 
 * @author cdettmering
 */
public class InstallWizardListener implements WizardListener, InstallCancelListener {
	
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(InstallWizardListener.class);
	
	private ProgressDialog progress;
	private JFrame main;
		
	public InstallWizardListener(JFrame dialog) {
		this.main = dialog;
	}

	@Override
	public void onCanceled(List<WizardPage> path, WizardSettings settings) {
	    
	    int choice = JOptionPane.showConfirmDialog(null, 
	            "Are you sure you want to cancel the install?",
	            "Are you sure?", 
	            JOptionPane.YES_NO_OPTION);
	    
	    if(choice == JOptionPane.YES_OPTION){
	        // Exit
	        logger.info("User cancelled install wizard");
	        System.exit(0);
	    }
	}

	@Override
	public void onFinished(List<WizardPage> path, WizardSettings settings) {
		logger.info("Wizard finished, starting install process");
		main.setVisible(false);
		final InstallThread install = new InstallThread(settings);
		install.addCancelListener(this);
		progress = new ProgressDialog(install);
		progress.setVisible(true);
		install.execute();
	}

	@Override
	public void onPageChanged(WizardPage page, List<WizardPage> path) {

	}

	@Override
	public void onCancel(String failureMessage) {
	    
        if(failureMessage != null){
            logger.error("The install process failed with message of '"+failureMessage+"'.");
        }else{
            logger.info("User cancelled install process");
        }
        
		if(progress != null) {
			progress.dispose();
			progress = null;
		}
		main.setVisible(true);
	}
	
}
