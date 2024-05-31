/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

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
public class ExportWizardListener implements WizardListener, ExportCancelListener {
	
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(ExportWizardListener.class);
	
	private ProgressDialog progress;
	private JFrame main;
		
	public ExportWizardListener(JFrame dialog) {
		this.main = dialog;
	}

	@Override
	public void onCanceled(List<WizardPage> path, WizardSettings settings) {
	    
	    int choice = JOptionPane.showConfirmDialog(main, "Are you sure you want to exit the GIFT Export Tutor tool?", "Are you sure?", JOptionPane.YES_NO_OPTION);
	    
	    if(choice == JOptionPane.YES_OPTION){
	        // Exit
	        logger.info("User cancelled export wizard");
	        System.exit(0);
	    }
	}

	@Override
	public void onFinished(List<WizardPage> path, WizardSettings settings) {
		logger.info("Wizard finished, starting export process");
		main.setVisible(false);
		final ExportThread export = new ExportThread(settings);
		export.addCancelListener(this);
		progress = new ProgressDialog(export);
		progress.setVisible(true);
		export.execute();
	}

	@Override
	public void onPageChanged(WizardPage page, List<WizardPage> path) {
		
	}

	@Override
	public void onCancel() {
		logger.info("User cancelled export process");
		if(progress != null) {
			progress.dispose();
			progress = null;
		}
		main.setVisible(true);
	}
	
}
