/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.installer;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
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
 * @author mhoffman
 */
public class InstallWizardListener implements WizardListener, InstallCancelListener {
	
	/** Logger instance */
	private static Logger logger = LoggerFactory.getLogger(InstallWizardListener.class);
	
	/** used to show installation progress */
	private ProgressDialog progress;
	
	/** the install wizard main dialog shown before the progress dialog */
	private JFrame main;
	
	/** used to notify whether the installation logic completed (after progress dialog) or was canceled */
	private InstallFinishedCallback installFinishedCallback;
		
	/**
	 * Set attributes for use later.
	 * 
	 * @param dialog the dialog containing the installation user interface components and what this
	 * listener will be monitoring. Can not be null.
	 * @param installFinishedCallback used for notification of events related to the how the installation
	 * wizard ended.  Can't be null.
	 */
	public InstallWizardListener(JFrame dialog, InstallFinishedCallback installFinishedCallback) {
	    
	    if(dialog == null){
	        throw new IllegalArgumentException("The dialog can't be null.");
	    }
		this.main = dialog;
		
        if(installFinishedCallback == null){
            throw new IllegalArgumentException("The callback can't be null.");
        }
		this.installFinishedCallback = installFinishedCallback;
	}

	// user is attempting to close the install wizard
	@Override
	public void onCanceled(List<WizardPage> path, WizardSettings settings) {
	    
	    int choice = JOptionPane.showConfirmDialog(null, 
	            "Are you sure you want to cancel?\nThis will prematurely end the GIFT course you are about to run.",
	            "Are you sure?", 
	            JOptionPane.YES_NO_OPTION);
	    
	    if(choice == JOptionPane.YES_OPTION){
	        logger.warn("User cancelled install wizard");	        
	        installFinishedCallback.canceled();
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
		progress.addWindowListener(new WindowListener() {
            
            @Override
            public void windowOpened(WindowEvent e) {
                
            }
            
            @Override
            public void windowIconified(WindowEvent e) {
                
            }
            
            @Override
            public void windowDeiconified(WindowEvent e) {
                
            }
            
            @Override
            public void windowDeactivated(WindowEvent e) {
                
            }
            
            @Override
            public void windowClosing(WindowEvent e) {
                                
            }
            
            @Override
            public void windowClosed(WindowEvent e) {
                
                if(!main.isVisible()){
                    main.dispose();
                }
                
                installFinishedCallback.completed();
                
            }
            
            @Override
            public void windowActivated(WindowEvent e) {                

            }
        });
		install.execute();
	}

	@Override
	public void onPageChanged(WizardPage page, List<WizardPage> path) {

	}

	//user is canceling the progress dialog and should be returned to the install wizard last page
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
	
	/**
	 * Used for notification of installation logic finished events.
	 * 
	 * @author mhoffman
	 *
	 */
	public interface InstallFinishedCallback{
	    
	    /**
	     * The installation logic completed successfully
	     */
	    public void completed();
	    
	    /**
	     * The installation logic was canceled by the user
	     */
	    public void canceled();
	}
}
