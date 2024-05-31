/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway.installer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import mil.arl.gift.common.io.UserEnvironmentUtil;

import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread responsible for installing GIFT
 * 
 * @author mhoffman
 *
 */
public class InstallThread extends SwingWorker<Void, Void> {
	
    /** logger instance */
	private static Logger logger = LoggerFactory.getLogger(InstallThread.class);	

	/** 
	 * Total percentage contribution for each task 
	 * Note: should total 100
	 */
    private static final int TRAINING_APP_TOTAL_PROGRESS = 5;
	
	private static final int TOTAL_PROGRESS = 100;

	/**
	 * Training Application names.
	 */
	private static final String VBS = "VBS";
	private static final String DE_TESTBED = "DE Testbed";
	private static final String POWERPOINT = "Powerpoint";
	
	private WizardSettings settings;
	private int subProgress = -1;
	private int progress;
	private String subTask;
	
	/** 
	 * Variables for canceling operations 
	 */	
	private boolean isCancelled;
	private boolean cancelDone;
	private List<InstallCancelListener> listeners;
	
	private String failureMessage;
	
	/**
	 * Creates a new InstallThread with the given settings
	 * 
	 * @param settings Install settings selected by the user
	 */
	public InstallThread(WizardSettings settings) {
		this.settings = settings;
		listeners = new ArrayList<InstallCancelListener>();
	}
	
	/**
	 * Gets the progress of the currently running sub task.
	 * 
	 * @return Progress represented as a percentage.
	 */
	public int getSubProgress() {
		return subProgress;
	}
	
	/**
	 * Gets the progress message of the currently running sub task.
	 * 
	 * @return Progress message
	 */
	public String getSubtaskString() {
		return subTask;
	}
	
	/**
	 * Updates the progress of the install operation.
	 * 
	 * @param operation The operation that has just completed.
	 */
	public void updateProgress(int operation) {
	    
	    if((progress + operation) <= 100){
	        progress += operation;
	    }else{
	        progress = 100;
	    }
	    
		setProgress(progress);
	}
	
	/**
	 * Cancels the install thread operation and cleans any existing files.
	 */
	public void cancel() {
		logger.info("User canceled install...cleaning files");
		setSubtaskString("Canceling");
		setSubProgress(-1);
		isCancelled = true;
	}
	
	/**
	 * Adds a cancel listener to the list of listeners.
	 * 
	 * @param listener The InstallCancelListener to add.
	 */
	public void addCancelListener(InstallCancelListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Installs on a non GUI thread
	 */
	@Override
	public Void doInBackground() {
	    try{
	        install();
	    }catch(Exception e){
	        e.printStackTrace();
	        logger.error("Caught exception while running install.", e);
	    }
		return null;
	}
	
	/**
	 * Sets the sub task progress
	 * 
	 * @param value Progress represented as a percentage (out of 100)
	 */
	private void setSubProgress(int value) {
		int old = subProgress;
		subProgress = value;
		firePropertyChange("SUBPROGRESS", old, value);
	}
	
	/**
	 * Sets the sub task message
	 * 
	 * @param value The sub task message. This should be a description of what the sub task is doing.
	 */
	private void setSubtaskString(String value) {
		int old = subProgress;
		subTask = value;
		firePropertyChange("SUBTASKSTRING", old, value);
	}
	
	/**
	 * Installs GIFT using the specified settings
	 * 
	 * Try to cancel every so often, this is the preferred best practice to just killing the thread.
	 */
	private void install() {
			
		try {
            
            //
            // Training Applications
            // 
		    
		    //
		    // VBS
		    //
		    try{
            
                
                if(settings.containsKey(TrainingApplicationInstallPage.VBS_HOME)){
                    
                    setVBSEnvVariable();
                    
                    logger.warn("Install VBS extensions is currently not supported for the Gateway installer (it is for the GIFT installer).");    
                }
                
		    }catch(Exception e){
		        logger.error("Caught exception while trying to configure VBS.",e);
		        
		        failureMessage = "Failed to configure VBS for GIFT.";
		    }
		    
		    if(tryCancel()) return;
            
		    //
		    // DE Testbed
		    //
            try{
                setDETestbedEnvVariable();                
            }catch(Exception e){
                logger.error("Caught exception while trying to configure DE Testbed.",e);
                
                failureMessage = "Failed to configure DE Testbed for GIFT.";
            }
            
            if(tryCancel()) return;
            
            //
            // PowerPoint
            //
            try{
                setPowerPointEnvVariable();
                
            }catch(Exception e){
                logger.error("Caught exception while trying to configure PowerPoint.",e);
                
                failureMessage = "Failed to configure PowerPoint for GIFT.";
            }
            
            updateProgress(TRAINING_APP_TOTAL_PROGRESS);
            
            if(tryCancel()) return;

			
        } catch (Exception e) {
            logger.error("Caught a general exception while trying to install.",e);
            
            failureMessage = "Failed to complete installation - unknown reason.";
        }

        if(failureMessage != null){
            setSubtaskString("GIFT Gateway Configuration Failed!"); 
            if(tryCancel()) return;
        }else{
            setSubtaskString("GIFT Gateway Configuration Completed.\nPress OK to continue the course."); 
        }
		
		setProgress(TOTAL_PROGRESS);
		setSubProgress(TOTAL_PROGRESS);
	}
	
	/**
	 * Set the VBS environment variable need for GIFT
	 * 
	 * @throws Exception if there was a critical error in setting the environment variable
	 */
    private void setVBSEnvVariable() throws Exception{
        setEnvironmentVariable(VBS, TrainingApplicationInstallPage.VBS_HOME);        
    }
	
    /**
     * Set the DE Testbed environment variable need for GIFT
     * 
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void setDETestbedEnvVariable() throws Exception{
        setEnvironmentVariable(DE_TESTBED, TrainingApplicationInstallPage.DE_TESTBED_HOME);        
    }
	
    /**
     * Set the PowerPoint environment variable need for GIFT
     * 
     * @throws Exception if there was a critical error in setting the environment variable
     */
	private void setPowerPointEnvVariable() throws Exception{
	    setEnvironmentVariable(POWERPOINT, TrainingApplicationInstallPage.PPT_HOME);        
	}
	
	/**
     * Set the environment variable need for GIFT
     * 
     * @param applicationName the name of the application that needs the environment variable in order
     * to be integrated with GIFT.
     * @param environmentVariable the environment variable name to set.  This is the key in the install wizard's
     * settings map as well.
     * @throws Exception if there was a critical error in setting the environment variable
     */
	private void setEnvironmentVariable(String applicationName, String environmentVariable) throws Exception{
	    
	    if(settings.containsKey(environmentVariable)){
            
            setSubtaskString("Configuring "+applicationName+"..."); 
            
            UserEnvironmentUtil.setEnvironmentVariable(environmentVariable, (String) settings.get(environmentVariable), true);
        }
	}
	
	/**
	 * Return whether a cancel install operation has taken place.
	 *  
	 * @return boolean - true iff the install process is being or has been canceled
	 */
	private boolean tryCancel() {
		
	    //nothing to do if the user hasn't canceled and there wasn't a failure
	    //i.e. return if the install is still running
		if(!isCancelled && failureMessage == null) {
			return false;
		}
		
		if(cancelDone) {
			return true;
		}
		
		doCancel();
		cancelDone = true;
		return true;
	}

	/** 
	 * Performs cancel and cleanup 
	 */
	private void doCancel() {
	    
		try {
		    performCleanup(false);
		    
		    if(failureMessage != null){
		        JOptionPane.showMessageDialog(null, "GIFT Communication Application installation cancelled due to an error.\n\n'"+failureMessage+"'.", "Cancelled", JOptionPane.ERROR_MESSAGE);
		    }else{
		        JOptionPane.showMessageDialog(null, "GIFT Communication Application installation cancelled", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
		    }
		} catch(IOException e) {
			logger.error("Caught exception while trying to cancel", e);
			JOptionPane.showMessageDialog(null, "An error occurred while trying to cancel. Please check the log for more details.", "GIFT Cancel Operation Error", JOptionPane.ERROR_MESSAGE);
		}
		
		for(InstallCancelListener listener : listeners) {
			listener.onCancel(failureMessage);
		}
	}
	
	/**
	 * Delete files and directories created during the install process.
	 * 
	 * @param successfulInstall whether the cleanup is being performed as a result of a successful install or not.
	 * @throws IOException if there was a problem deleting a file
	 */
	private void performCleanup(boolean successfulInstall) throws IOException{
	    

	}
	
	/**
	 * Return a string representation of the arguments provided.
	 * 
	 * @param args collection of string elements to display as a single string
	 * @return String the string representation of the arguments
	 */
	@SuppressWarnings("unused")
    private static String argsToString(List<String> args){
	    
	    StringBuffer sb = new StringBuffer();
	    for(String arg : args){
	        sb.append(arg).append(" ");
	    }
	    return sb.toString();
	}

}
