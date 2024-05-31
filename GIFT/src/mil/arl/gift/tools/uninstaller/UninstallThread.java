/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.uninstaller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import mil.arl.gift.common.io.UserEnvironmentUtil;
import mil.arl.gift.common.util.JOptionPaneUtil;
import mil.arl.gift.gateway.uninstaller.TrainingApplicationUninstallPage;

import org.apache.commons.io.FileUtils;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread responsible for uninstalling GIFT
 * 
 * @author mhoffman
 *
 */
public class UninstallThread extends SwingWorker<Void, Void> {
	
    /** logger instance */
	private static Logger logger = LoggerFactory.getLogger(UninstallThread.class);	
	
	/** commands to execute in the install process */
	private static final String[] STOP_DERBY_SERVER_CMD    = new String[]{"external" + File.separator + "db-derby-10-15.2.0-bin" + File.separator + "bin" + File.separator + "stopNetworkServer.bat"};
	
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
	private static final String PYTHON = "Python";
	private static final String VR_ENGAGE = "VR-Engage";
	
	private String failureMessage;

	private WizardSettings settings;
	private int subProgress = -1;
	private int progress;
	private String subTask;
	
	/** 
	 * Variables for canceling operations 
	 */	
	private boolean isCancelled;
	private boolean cancelDone;
	private List<UninstallCancelListener> listeners;
	
	/**
	 * Creates a new UninstallThread with the given settings
	 * 
	 * @param settings uninstall settings selected by the user
	 */
	public UninstallThread(WizardSettings settings) {
		this.settings = settings;
		listeners = new ArrayList<UninstallCancelListener>();
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
	 * Updates the progress of the uninstall operation.
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
	 * Cancels the uninstall thread operation.
	 */
	public void cancel() {
	    
	    if(logger.isInfoEnabled()) {
	        logger.info("User canceled uninstall");
	    }
	    
		setSubtaskString("Canceling");
		setSubProgress(-1);
		isCancelled = true;
	}
	
	/**
	 * Adds a cancel listener to the list of listeners.
	 * 
	 * @param listener The UninstallCancelListener to add.
	 */
	public void addCancelListener(UninstallCancelListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Uninstalls on a non GUI thread
	 */
	@Override
	public Void doInBackground() {
	    try{
	        uninstall();
	    }catch(Exception e){
	        e.printStackTrace();
	        logger.error("Caught exception while running uninstall.", e);
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
	 * Uninstalls GIFT using the specified settings
	 * 
	 * Try to cancel every so often, this is the preferred best practice to just killing the thread.
	 */
	private void uninstall() {
	    
	    if(logger.isInfoEnabled()) {
	        logger.info("Starting uninstall");
	    }
		
		try{
		    
            File rootFile = new File("bin");
            final String rootFileName = rootFile.getAbsoluteFile().getParentFile().getParentFile().getCanonicalPath();
		    if(isPropertyTrue(settings.get(UninstallSettings.DELETE_GIFT))){
		        //confirm with user they want to delete GIFT files
		        int choice = JOptionPaneUtil.showConfirmDialog("Are you sure you want to delete this instance of GIFT from this computer?\n\nThis can't be undone!\n\n(The folder "+rootFileName+"\nwill be deleted shortly after the uninstall application is closed)", 
		                "Delete GIFT?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		        
		        if(choice == JOptionPane.CANCEL_OPTION){
		            cancel();		            
		        }
		    }

            if(tryCancel()) return;
	            
	        try{
	            //
	            // Avatar
	            //
	            handleMediaSemantics();            
	            
	        } catch (Exception e) {
	            logger.error("Caught exception while trying to uninstall Media Semantics for GIFT.",e);            
	            failureMessage = "There was a problem while trying to uninstall Media Semantics for GIFT.";
	        }
	            
	        if(tryCancel()) return;
	        
            //
            // Training Applications
            // 
	        
	        try{      	           
	            //VBS
	            handleVBS();
	            
	        } catch (Exception e) {
	            logger.error("Caught exception while trying to remove the VBS configuration that GIFT created.",e);	            
	            failureMessage = "There was a problem while trying to remove the VBS configuration that GIFT created.";
	        }
	        
	        try{
	            //DE Testbed
	            handleDETestbed();	            
	            
	        } catch (Exception e) {
	            logger.error("Caught exception while trying to remove the DE Testbed configuration that GIFT created.",e);	            
	            failureMessage = "There was a problem while trying to remove the DE Testbed configuration that GIFT created.";
	        }
	        
            try{
                //
                // PowerPoint
                //
                handlePowerPoint(); 
                
            } catch (Exception e) {
                logger.error("Caught exception while trying to remove the PowerPoint configuration that GIFT created.",e);              
                failureMessage = "There was a problem while trying to remove the PowerPoint configuration that GIFT created.";
            }
            
            try{                   
                //VR-Engage
                handleVrEngage();
                
            } catch (Exception e) {
                logger.error("Caught exception while trying to remove the VR-Engage configuration that GIFT created.",e);             
                failureMessage = "There was a problem while trying to remove the VR-Engage configuration that GIFT created.";
            }
	            
	        updateProgress(TRAINING_APP_TOTAL_PROGRESS);
	        
	        if(tryCancel()) return;

            //Python
            if(isPropertyTrue(settings.get(UninstallSettings.PYTHON_UNINSTALL))) {
            	 removeEnvironmentVariable(PYTHON, UninstallSettings.PYTHON_HOME);  
            }
            
            //delete GIFT files
            if(isPropertyTrue(settings.get(UninstallSettings.DELETE_GIFT))){

                // stop derby server so the derby db files can be removed
                String stopDerbyOutput = executeAndWaitForCommand(STOP_DERBY_SERVER_CMD);
                
                if(logger.isInfoEnabled()) {
                    logger.info("stopping derby server:" + stopDerbyOutput);
                }
                
                //
                // deleted GIFT on exit
                //
                if(logger.isInfoEnabled()) {
                    logger.info("Deleting GIFT directory of '"+rootFileName+"'.");
                }
                
                //copy the script that will perform the deletion to the temp folder so that the delete operation
                //doesn't run into a 'cannot delete because this file is in use' type error
                File tempFile = File.createTempFile("DELETE_GIFT", ".bat");
                FileUtils.copyFile(new File("scripts" + File.separator + "install" + File.separator + "deleteGIFT.bat"), tempFile);
                
                Runtime.getRuntime().addShutdownHook(new Thread("Delete GIFT Shutdownhook") {

                    @Override
                    public void run() {
                        try {
                            String[] command = {"cmd.exe", "/c", "start", "/MIN", tempFile.getCanonicalPath(), rootFileName};
                            
                            ProcessBuilder builder = new ProcessBuilder(command);
                            builder.directory(tempFile.getParentFile());  // need to set the working directory outside of the GIFT folder structure being deleted, 
                                                                          // otherwise the GIFT folder would remain after the delete operation
                            builder.start();
                        } catch (IOException e) {
                            logger.error("Caught exception while trying to delete GIFT files", e);
                        }
                    }
               });
                

            }

			
        } catch (Exception e) {
            logger.error("Caught exception while trying to uninstall.",e);            
            failureMessage = "Failed to complete uninstall due to an exception - '"+e.getMessage()+"'.";
        }

        if(failureMessage != null){
            setSubtaskString("GIFT Uninstall Failed!  \nCheck uninstall tool log in GIFT/output/logger/tools for more details."); 
            if(tryCancel()) return;
        }else{
            setSubtaskString("Uninstall Completed."); 
        }
		
		setProgress(TOTAL_PROGRESS);
		setSubProgress(TOTAL_PROGRESS);
	}
	
	/**
	 * Handle removing the VBS configuration created by GIFT.
	 * 
	 * @throws Exception
	 */
	private void handleVBS() throws Exception{
	    
        if(isPropertyTrue(settings.get(UninstallSettings.VBS_UNINSTALL))){            
            
            //delete GIFT files from VBS install directory
            String vbsPath = UserEnvironmentUtil.getEnvironmentVariable(UninstallSettings.VBS_HOME);
            if(vbsPath != null){
                
                File vbsPathFile = new File(vbsPath);
                if(vbsPathFile.exists()){
                
                    @SuppressWarnings("unchecked")
                    List<String> files = (List<String>) settings.get(TrainingApplicationUninstallPage.VBS_FILES);
                    for(String filename : files) {
                        
                        if(logger.isInfoEnabled()) {
                            logger.info("Removing \"" + filename + "\" from \"" + vbsPathFile.getAbsolutePath() + "\"");
                        }
                        
                        File file = new File(vbsPathFile.getAbsolutePath() + File.separator + filename);
                        if(file.isDirectory()) {
                            FileUtils.deleteQuietly(file);
                        } else {
                            FileUtils.deleteQuietly(file);
                        }
                    }
                }
            }
            
            removeVBSEnvVariable();
        }

	}
	
	/**
     * Handle removing the VR-Engage configuration created by GIFT.
     * 
     * @throws Exception
     */
    private void handleVrEngage() throws Exception{
        
        if(isPropertyTrue(settings.get(UninstallSettings.VR_ENGAGE_UNINSTALL))){            
            
            //delete GIFT files from VR-Engage install directory
            String vrEngagePath = UserEnvironmentUtil.getEnvironmentVariable(UninstallSettings.VR_ENGAGE_HOME);
            if(vrEngagePath != null){
                
                File vrEngagePathFile = new File(vrEngagePath);
                if(vrEngagePathFile.exists()){
                
                    @SuppressWarnings("unchecked")
                    List<String> files = (List<String>) settings.get(TrainingApplicationUninstallPage.VR_ENGAGE_FILES);
                    for(String filename : files) {
                        
                        if(logger.isInfoEnabled()) {
                            logger.info("Removing \"" + filename + "\" from \"" + vrEngagePathFile.getAbsolutePath() + "\"");
                        }
                        
                        File file = new File(vrEngagePathFile.getAbsolutePath() + File.separator + filename);
                        if(file.isDirectory()) {
                            FileUtils.deleteQuietly(file);
                        } else {
                            FileUtils.deleteQuietly(file);
                        }
                    }
                }
            }
            
            removeVrEngageEnvVariable();
        }

    }
	
    /**
     * Handle removing the DE Testbed configuration created by GIFT.
     * 
     * @throws Exception
     */
	private void handleDETestbed() throws Exception{
	    
        if(isPropertyTrue(settings.get(UninstallSettings.DE_TESTBED_UNINSTALL))){            
            removeDETestbedEnvVariable();
        }
	    
	}
	
    /**
     * Handle removing the PowerPoint configuration created by GIFT.
     * 
     * @throws Exception
     */
	private void handlePowerPoint() throws Exception{
	    
	    if(isPropertyTrue(settings.get(UninstallSettings.PPT_UNINSTALL))){	        
	        removePowerPointEnvVariable();
	    }

	}	

            
	/**
	 * Handle the installation and configuration of Media Semantics for the GIFT installer.
	 * 
	 * @throws Exception
	 */
	private void handleMediaSemantics() throws Exception{
            
//        //Media Semantics Character
//        if(isPropertyTrue(settings.get(InstallSettings.MEDIA_SEMANTICS))){
//            
//            setSubtaskString("Installing Media Semantics Character..."); 
//            
//            String output;
//            
//            logger.info("Installing MSC builder");
//            
//            //execute character builder exe
//            output = executeAndWaitForCommand(INSTALL_MSC_BUILDER_CMD);
//            
//            logger.info("Installed Media Semantics Character Builder output = '"+output+"'.");
//            
//            logger.info("Installing MSC server");
//            
//            //upon success, execute character server exe
//            output = executeAndWaitForCommand(INSTALL_MSC_SERVER_CMD);
//            
//            logger.info("Installed Media Semantics Character Server output = '"+output+"'.");
//            
//            //programmatically change permissions of logs directory to write-able for all Users             
//            File locationA = new File("C:/Program Files (x86)/Character Server/Logs");
//            File locationB = new File("C:/Program Files/Character Server/Logs");
//            File location = null;
//            if(locationA.exists()){
//                location = locationA;
//            }else if(locationB.exists()){
//                location = locationB;
//            }  
//            
//            if(location != null){
//                //use location path as arg to vb script
//                
//                logger.info("Attempting to give the Media Semantics Character Server directory of "+location+" write privelages which are necessary for it to work correctly.");
//                List<String> argsList = new ArrayList<>(Arrays.asList(GIVE_WRITE_PERMISSIONS));
//                argsList.add(location.getAbsolutePath());
//                output = executeAndWaitForCommand(argsList.toArray(new String[argsList.size()]));
//                
//                logger.info("Change write permissions output = "+output+".");
//            }else{
//                logger.warn("Unable to find the Logs directory of the Media Semantics Character Server installation, therefore the Logs directory probably doesn't have write permissions which are needed in order to work correctly.");
//            }
//            
//        }
//        
//        updateProgress(AVATAR_TOTAL_PROGRESS);

	}
	
    /**
     * Set the VBS environment variable need for GIFT
     * 
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void removeVBSEnvVariable() throws Exception{
        removeEnvironmentVariable(VBS, UninstallSettings.VBS_HOME);        
    }
    
    /**
     * Set the VR-Engage environment variable need for GIFT
     * 
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void removeVrEngageEnvVariable() throws Exception{
        removeEnvironmentVariable(VR_ENGAGE, UninstallSettings.VR_ENGAGE_HOME);        
    }
    
    /**
     * Set the DE Testbed environment variable need for GIFT
     * 
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void removeDETestbedEnvVariable() throws Exception{
        removeEnvironmentVariable(DE_TESTBED, UninstallSettings.DE_TESTBED_HOME);        
    }
    
    /**
     * Set the PowerPoint environment variable need for GIFT
     * 
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void removePowerPointEnvVariable() throws Exception{
        removeEnvironmentVariable(POWERPOINT, UninstallSettings.PPT_HOME);      
    }
    
    /**
     * Remove the environment variable
     * 
     * @param applicationName the name of the application that needs the environment variable in order
     * to be integrated with GIFT.
     * @param environmentVariable the environment variable name to remove.  
     * @throws Exception if there was a critical error in removing the environment variable
     */
    private void removeEnvironmentVariable(String applicationName, String environmentVariable) throws Exception{
            
        setSubtaskString("Removing configuration for "+applicationName+"..."); 
        
        UserEnvironmentUtil.deleteEnvironmentVariable(environmentVariable);
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
     * Execute the provided command then capture and return the output (both input and error streams).
     *  
     * @param command the command to execute (split into arguments)
     * @param acceptedExitValues list of optional accepted exit values for the command being executed
     * @return String the IO output of executing the command
     * @throws IOException if there was an IO problem starting the command or reading the output stream
     * @throws InterruptedException if there was a problem waiting for the command to finish
     */
    public static String executeAndWaitForCommand(String[] command, int...acceptedExitValues) throws IOException, InterruptedException{
        
        ProcessBuilder builder = new ProcessBuilder(command);

        builder.redirectErrorStream(true);
        Process process = builder.start();          
        
        // wait until build is done
        int exitValue = process.waitFor();
        
        boolean badExitValue = exitValue != 0;
        
        if(acceptedExitValues != null){
            //check expected exit values
            
            for(int acceptedExitValue : acceptedExitValues){
                
                if(exitValue == acceptedExitValue){
                    badExitValue = false;
                    break;
                }
            }
        }
        
        InputStream stdout = process.getInputStream ();
        BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
        String line;
        StringBuffer outputStringBuffer = new StringBuffer();
        while ((line = reader.readLine ()) != null) {
            outputStringBuffer.append("\n").append(line);
        }         
        
        if(badExitValue){
            StringBuilder commandStr = new StringBuilder();
            for(String token : command){
                
                if(commandStr.length() > 0){
                    //add space after each command element
                    commandStr.append(" ");
                }
                
                commandStr.append(token);
            }
            throw new RuntimeException("The process exited with value of "+exitValue+" for command of '"+commandStr.toString()+"' with output of '"+outputStringBuffer.toString()+"'.");
        }
        
        return outputStringBuffer.toString();
    }
	
	/**
	 * Return whether a cancel uninstall operation has taken place.
	 *  
	 * @return boolean - true iff the uninstall process is being or has been canceled
	 */
	private boolean tryCancel() {
		
        //nothing to do if the user hasn't canceled and there wasn't a failure
        //i.e. return if the uninstall is still running
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
                JOptionPane.showMessageDialog(null, "GIFT uninstall cancelled due to an error.\n\n'"+failureMessage+"'."
                        + "\n\n Check uninstall tool log in GIFT/output/logger/tools for more details.", "Cancelled", JOptionPane.ERROR_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null, "GIFT uninstallation cancelled", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            }
		} catch(IOException e) {
			logger.error("Caught exception while trying to cancel", e);
			JOptionPane.showMessageDialog(null, "An error occurred while trying to cancel. Please check the log for more details.", "GIFT Cancel Operation Error", JOptionPane.ERROR_MESSAGE);
		}
        
		for(UninstallCancelListener listener : listeners) {
            listener.onCancel(failureMessage);
		}

	}
	
	/**
	 * Delete files and directories created during the uninstall process.
	 * 
	 * @param successfulUninstall whether the cleanup is being performed as a result of a successful uninstall or not.
	 * @throws IOException if there was a problem deleting a file
	 */
	private void performCleanup(boolean successfulUninstall) throws IOException{
	    

	}

}
