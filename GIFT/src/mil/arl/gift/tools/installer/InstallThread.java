/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.installer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.ciscavate.cjwizard.WizardSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.io.CommonProperties;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.FileTreeModel;
import mil.arl.gift.common.io.PlatformUtils;
import mil.arl.gift.common.io.PlatformUtils.SupportedOSFamilies;
import mil.arl.gift.common.io.ProgressIndicator;
import mil.arl.gift.common.io.UserEnvironmentUtil;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.util.StringUtils;
import mil.arl.gift.gateway.GatewayModuleProperties;
import mil.arl.gift.gateway.installer.TrainingApplicationInstallPage;
import mil.arl.gift.gateway.interop.InteropConfigFileHandler;

/**
 * Thread responsible for installing GIFT
 *
 * @author mhoffman
 *
 */
public class InstallThread extends SwingWorker<Void, Void> {

    /** logger instance */
	private static Logger logger = LoggerFactory.getLogger(InstallThread.class);

    /** the jar(s) to check to determine if GIFT is built or not (I just picked some important jar(s) from the entire list of GIFT jars) */
    private static final File DOMAIN_JAR = new File("bin/gift-domain.jar");
    private static final File GENERATED_JAR = new File("bin/jaxb_generated.jar");

    public static final File MSC_BUILDER_EXE = new File("external/MediaSemantics-CharacterBuilder-CB535.exe");
    public static final File MSC_SERVER_EXE = new File("external/MediaSemantics-CharacterServer-CS535.exe");
    
    /**
     * An enumeration for common command line commands that are supported on multiple execution platforms
     * and must be changed depending on which platform GIFT is running
     * 
     * @author nroberts
     */
    private enum CommonInstallCommands{
        
        /** A command that fully builds GIFT */
        BUILD_GIFT(
                new OSCommand(SupportedOSFamilies.WINDOWS, new String[]{"cmd.exe", "/c", "start", "/MIN", "build.bat", "exitOnError"}),
                new OSCommand(SupportedOSFamilies.UNIX, new String[]{"./build.sh", "exitOnError"})
        ),
        
        /** A command that stops the Derby network server that hosts GIFT's databases */
        STOP_DERBY_SERVER(
                new OSCommand(SupportedOSFamilies.WINDOWS, new String[]{"external" + File.separator + "db-derby-10.15.2.0-bin" + File.separator + "bin" + File.separator + "stopNetworkServer.bat"}),
                new OSCommand(SupportedOSFamilies.UNIX, new String[]{"external" + File.separator + "db-derby-10.15.2.0-bin" + File.separator + "bin" + File.separator + "stopNetworkServer"})
        );
        
        private Map<SupportedOSFamilies, String[]> osToCommandLine = new HashMap<>();
        
        /**
         * Used to enforce operating system to command line mapping restrictions
         * 
         * @param requiredCommand first install command, required
         * @param commands additional install commands, optional
         */
        private CommonInstallCommands(OSCommand requiredCommand, OSCommand... commands) {
            if(requiredCommand == null) {
                throw new IllegalArgumentException("At least one operating system must implement an install command");
            }
            
            /** maps operating system to required command */
            osToCommandLine.put(requiredCommand.getOS(), requiredCommand.getCommandLine());
            
            if(commands != null) {
                for(OSCommand command : commands) {
                	
                	/** maps operating system to additional commands */
                    osToCommandLine.put(command.getOS(), command.getCommandLine());
                }
            }
        }
        
        /**
         * Gets whether this command is supported by the current execution platform (i.e. whether
         * GIFT has a command line for the command that will work on this plaform)
         * 
         * @return whether the command is supported
         */
        public boolean isSupported() {
            SupportedOSFamilies osFamily = PlatformUtils.getFamily();
            return osToCommandLine.containsKey(osFamily);
        }
        
        /**
         * Gets the command line that should be used to invoke this command on the current execution platform
         * 
         * @return the command line to invoke. Can be null if the current execution platform is not supported.
         */
        private String[] getCommandLine() {
            SupportedOSFamilies osFamily = PlatformUtils.getFamily();
            return osToCommandLine.get(osFamily);
        }
    }

	/** commands to execute in the install process */
	private static final String[] INSTALL_MSC_BUILDER_CMD  = new String[]{MSC_BUILDER_EXE.getAbsolutePath(), "/passive"};
	private static final String[] INSTALL_MSC_SERVER_CMD   = new String[]{MSC_SERVER_EXE.getAbsolutePath(), "/passive"};
	private static final String[] GIVE_WRITE_PERMISSIONS   = new String[]{"cscript", "scripts/install/giveWritePermissions.vbs"};
	private static final String[] EXTRACT_UMS_DERBY_DB_CMD = new String[]{"cscript", "scripts/install/extractZip.vbs", "data/derbyDb/DerbyDB.UMS.Backup.Original.zip", "data/derbyDb/"};
    private static final String[] EXTRACT_LMS_DERBY_DB_CMD = new String[]{"cscript", "scripts/install/extractZip.vbs", "data/derbyDb/DerbyDB.LMS.Backup.Original.zip", "data/derbyDb/"};

	/** key phrase to search for in build output to determine if GIFT failed to build */
	private static final String BUILD_FAILED = "BUILD FAILED";
	
	/** zip files containing the databases that will need to be extracted */
    private static final File UMS_DB_ZIP = new File("data" + File.separator + "derbyDb" + File.separator + "DerbyDB.UMS.Backup.Original.zip");
    private static final File LMS_DB_ZIP = new File("data" + File.separator + "derbyDb" + File.separator + "DerbyDB.LMS.Backup.Original.zip");	
    
    /** location of the extracted databases */
    private static final File UMS_DB_EXTRACT_LOC = new File("data" + File.separator + "derbyDb");
    private static final File LMS_DB_EXTRACT_LOC = new File("data" + File.separator + "derbyDb");  

	/** database directories to check to determine if the databases were installed */
	private static final File UMS_DB_HOME = new File("data" + File.separator + "derbyDb" + File.separator + "GiftUms");
	private static final File LMS_DB_HOME = new File("data" + File.separator + "derbyDb" + File.separator + "GiftLms");

	/** launch GIFT script to place at the root of a GIFT baseline */
	private static final File launchGIFTFile = new File("data" + File.separator + "installResources" + File.separator + "launchGIFT.bat");
	private static final File newLaunchGIFTFile = new File(".." + File.separator + "launchGIFT.bat");

	/** uninstall GIFT script to place at the root of a GIFT baseline */
    private static final File uninstallGIFTFile = new File("data" + File.separator + "installResources" + File.separator + "uninstallGIFT.bat");
    private static final File newUninstallGIFTFile = new File(".." + File.separator + "uninstallGIFT.bat");

	private static final File GIFT_README_FILE = new File("data" + File.separator + "installResources" + File.separator + "GIFTReadme.txt");
	public static final File NEW_GIFT_README_FILE = new File(".." + File.separator + "GIFTReadme.txt");

	/**
	 * Total percentage contribution for each task
	 * Note: should total 100
	 */
	private static final int COPY_SUPPORTING_FILES_TOTAL_PROGRESS = 5;
	private static final int EXTRACT_DATABASE_TOTAL_PROGRESS = 30;
    private static final int BUILD_GIFT_TOTAL_PROGRESS = 40;
    private static final int AVATAR_TOTAL_PROGRESS = 15;
    private static final int TRAINING_APP_TOTAL_PROGRESS = 10;

	private static final int TOTAL_PROGRESS = 100;
	
    /** determines how often a download percent complete will update the progress indicator */
    private static final double UPDATE_THRESHOLD = 0.05;
    
    /** rough file size of the Unity land nav file to download from gt.org downloads */
    private static final long ROUGH_UNITY_LAND_NAV_FILE_SIZE_BYTES = 744000000; // 744 MB

	/**
	 * Training Application names.
	 */
	private static final String VBS = "VBS";
	private static final String DE_TESTBED = "DE Testbed";
	private static final String POWERPOINT = "Powerpoint";
	private static final String WINPYTHON = "WinPython";
	private static final String VR_ENGAGE = "VR-Engage";

	private static final String DE_TESTBED_DIS_INTEROP = "DE Testbed DIS Adapter";
	private static final String DE_TESTBED_XMLRPC_INTEROP = "DE Testbed XMLRPC Adapter";
	private static final String POWERPOINT_INTEROP = "Powerpoint Adapter";
	private static final String DIS_INTEROP = "DIS Adapter 1";
	private static final String VBS_PLUGIN_INTEROP = "VBS Plugin 1";
	private static final String VR_ENGAGE_PLUGIN_INTEROP = "VR-Engage Plugin 1";

	private static final String INTEROP_CONFIG_FILENAME = GatewayModuleProperties.getInstance().getInteropConfig();
	private InteropConfigFileHandler interopFileHandler = null;
	private boolean interopConfigFileUpdate = false;

	/** Default buffer size when downloading files */
    private static final int BUFFER_SIZE = 4096;

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
	private List<InstallCancelListener> listeners;

	/**
	 * Creates a new InstallThread with the given settings
	 *
	 * @param settings Install settings selected by the user
	 */
	public InstallThread(WizardSettings settings) {
		this.settings = settings;
		listeners = new ArrayList<>();
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

	    if(progress + operation <= 100){
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
		if(logger.isInfoEnabled()) {
    		logger.info("User canceled install...cleaning files");
		}
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
     * Check if GIFT is built
     *
     * @return boolean whether or not GIFT is built
     */
    public static boolean isGIFTBuilt(){
        return DOMAIN_JAR.exists() && GENERATED_JAR.exists();
    }

	/**
	 * Installs GIFT using the specified settings
	 *
	 * Try to cancel every so often, this is the preferred best practice to just killing the thread.
	 */
	private void install() {

		if(logger.isInfoEnabled()) {
    		logger.info("Starting install");
		}

		try{

	        try {

	            //
	            // Build GIFT
	            //

	            setSubtaskString("Building GIFT (may take several minutes)...");

	            if(!isGIFTBuilt()){

	                if(CommonInstallCommands.BUILD_GIFT.isSupported()){
    	                String output = executeAndWaitForCommand(CommonInstallCommands.BUILD_GIFT.getCommandLine());
    
    	                //check for "BUILD FAIL"
    	                if(output.contains(BUILD_FAILED)){
    	                    logger.error("The GIFT build failed.  Here is the build output: "+output);
    	                    throw new Exception("GIFT failed to build.");
    	                }
    
    	                if(logger.isInfoEnabled()) {
        	                logger.info("build GIFT output:" + output);
    	                }
	                } else {
	                    throw new Exception("GIFT was unable to build because the current operating system platform has no command line implementation.");
	                }
	            }

	            updateProgress(BUILD_GIFT_TOTAL_PROGRESS);

	        } catch (Exception e) {
	            logger.error("Caught exception while trying to build GIFT.",e);
	            failureMessage = "There was a problem while trying to build GIFT.";
	        }

            if(tryCancel()) return;

	        try{
	            //
	            // Databases
	            //
	            if(logger.isInfoEnabled()) {
    	            logger.info("Handling GIFT Databases.");
	            }
	            handleGIFTDatabases();

	        } catch (Exception e) {
	            logger.error("Caught exception while trying to extract the UMS/LMS databases.",e);
	            failureMessage = "There was a problem while trying to extract the UMS/LMS databases.";
	        }

	        if(tryCancel()) return;

	        /* Perform the below installer tasks only in Windows, since their associated 
	         * applications are not supported in other platforms*/
	        if(SupportedOSFamilies.WINDOWS.equals(PlatformUtils.getFamily())) {
	        
    	        try{
    	            //
    	            // Avatar
    	            //
    	            if(logger.isInfoEnabled()) {
        	            logger.info("Handling Media Semantics.");
    	            }
    	            handleMediaSemantics();
    
    	        } catch (Exception e) {
    	            logger.error("Caught exception while trying to install Media Semantics for GIFT.",e);
    	            failureMessage = "There was a problem while trying to install Media Semantics for GIFT.";
    	        }
    
    	        if(tryCancel()) return;
    
                //
                // Training Applications
                //
    
    	        try{
    	            //VBS
    	            if(logger.isInfoEnabled()) {
        	            logger.info("Checking VBS settings.");
    	            }
    	            handleVBS();
    
    	        } catch (Exception e) {
    	            logger.error("Caught exception while trying to configure VBS for GIFT.",e);
    	            failureMessage = "There was a problem while trying to configre VBS for GIFT.";
    	        }
    
    	        try{
    	            //DE Testbed
    	            if(logger.isInfoEnabled()) {
        	            logger.info("Checking DE Testbed settings.");
    	            }
    	            handleDETestbed();
    
    	        } catch (Exception e) {
    	            logger.error("Caught exception while trying to configure DE Testbed for GIFT.",e);
    	            failureMessage = "There was a problem while trying to configure DE Testbed for GIFT.";
    	        }
    
                try{
                    //
                    // PowerPoint
                    //
                    if(logger.isInfoEnabled()) {
                        logger.info("Checking PowerPoint settings.");
                    }
                    handlePowerPoint();
    
                } catch (Exception e) {
                    logger.error("Caught exception while trying to configure PowerPoint for GIFT.",e);
                    failureMessage = "There was a problem while trying to configure PowerPoint for GIFT.";
                }
    
                try {
                    // Unity GIFT Wrap
                    if (logger.isInfoEnabled()) {
                        logger.info("Downloading Unity project.");
                    }
                    handleUnityDownload();
                } catch (Exception e) {
                    logger.error("Caught exception while trying to download the Unity project for GIFT Wrap.", e);
                    failureMessage = "Caught exception while trying to download the Unity project for GIFT Wrap.";
                }
    
                try{
                    //VR-Engage
                    if(logger.isInfoEnabled()) {
                        logger.info("Checking VR-Engage settings.");
                    }
                    handleVrEngage();
    
                } catch (Exception e) {
                    logger.error("Caught exception while trying to configure VR-Engage for GIFT.",e);
                    failureMessage = "There was a problem while trying to configre VR-Engage for GIFT.";
                }
    
    
                //update interop configuration file
                if(interopConfigFileUpdate){
                    interopFileHandler.updateInteropConfigFile(false);
                }
    
    	        updateProgress(TRAINING_APP_TOTAL_PROGRESS);
    
    	        if(tryCancel()) return;
    
                //WinPython
                if(settings.containsKey(InstallSettings.PYTHON_HOME)) {
                	if (settings.get(InstallSettings.PYTHON_HOME) == null) {
    					if(logger.isInfoEnabled()) {
        					logger.info("Removing Python variable.");
    					}
    					UserEnvironmentUtil.deleteEnvironmentVariable(InstallSettings.PYTHON_HOME);
                	} else {
    					if(logger.isInfoEnabled()) {
        					logger.info("Setting Python variable.");
    					}
    					setEnvironmentVariable(WINPYTHON, InstallSettings.PYTHON_HOME);
                	}
                }
	        }

	        setSubtaskString("Copying Launch script...");

	        //copy launchGIFT.bat to root directory (parent to GIFT directory)
	        if(logger.isInfoEnabled()) {
    	        logger.info("Copying launch script to root directory.");
	        }
	        FileUtils.copyFile(launchGIFTFile, newLaunchGIFTFile);

	        setSubtaskString("Copying Uninstall script...");

	        //copy uninstallGIFT.bat to root directory (parent to GIFT directory)
	        if(logger.isInfoEnabled()) {
    	        logger.info("Copying uninstall script to root directory.");
	        }
            FileUtils.copyFile(uninstallGIFTFile, newUninstallGIFTFile);

	        //copy GIFT Readme to root directory (parent to GIFT directory)
            if(logger.isInfoEnabled()) {
                logger.info("Copying readme to root directory.");
            }
	        FileUtils.copyFile(GIFT_README_FILE, NEW_GIFT_README_FILE);

	        updateProgress(COPY_SUPPORTING_FILES_TOTAL_PROGRESS);

        } catch (Throwable t) {
            logger.error("Caught exception while trying to install.",t);
            failureMessage = "Failed to complete installation due to an exception - '"+t.getMessage()+"'.";
        }

        if(failureMessage != null){
            setSubtaskString("GIFT Installation Failed!  \nCheck install tool log in GIFT/output/logger/tools for more details.");
            if(tryCancel()) return;
        }else{
            setSubtaskString("Installation Completed.");
        }

		setProgress(TOTAL_PROGRESS);
		setSubProgress(TOTAL_PROGRESS);
	}

	/**
	 * Handle configuring VBS for integration with GIFT.
	 *
	 * @throws Exception
	 */
	private void handleVBS() throws Exception{

        if(settings.containsKey(InstallSettings.VBS_HOME)){

            setVBSEnvVariable();

            //make sure to enable interop interfaces in config file
            updateInteropAvailability(DIS_INTEROP, true);
            updateInteropAvailability(VBS_PLUGIN_INTEROP, true);

            if(logger.isInfoEnabled()){
                logger.info("Installing VBS extensions for GIFT.");
            }

            //execute installVBSExtensions.bat to copy over necessary files for GIFT
            @SuppressWarnings("unchecked")
			HashMap<File, File> fileMap = (HashMap<File, File>) settings.get(TrainingApplicationInstallPage.VBS_FILES);
            boolean deleteVbs2Plugin = isPropertyTrue(settings.get(TrainingApplicationInstallPage.DELETE_VBS2_PLUGIN));
            String vbsPath = (String) settings.get(InstallSettings.VBS_HOME);

            if(deleteVbs2Plugin) {
                if(logger.isInfoEnabled()){
                    logger.info("Deleting previous versions of GIFT plugin files.");
                }
            	FileUtils.deleteQuietly(new File(vbsPath + "/plugins/GIFTVBS2Plugin.dll"));
            	FileUtils.deleteQuietly(new File(vbsPath + "/GIFTVBS2Game.bat"));
            }

            for(File source : fileMap.keySet()) {
                if(logger.isInfoEnabled()){
                    logger.info("Copying \"" + source.getPath() + "\" to \"" + fileMap.get(source).getPath() + "\"");
                }
            	if(source.isDirectory()) {
            		FileUtils.copyDirectory(source, fileMap.get(source));
            	} else {
            	    FileUtils.copyFile(source, fileMap.get(source));
            	}
            }

                if(logger.isInfoEnabled()){
                    logger.info("Completed Install of VBS extension for GIFT.");
                }

        }
	}

    /**
     * Handle configuring DE Testbed for integration with GIFT.
     *
     * @throws Exception
     */
	private void handleDETestbed() throws Exception{

        if(settings.containsKey(InstallSettings.DE_TESTBED_HOME)){

            setDETestbedEnvVariable();

            //make sure to enable interop interfaces in config file
            updateInteropAvailability(DE_TESTBED_DIS_INTEROP, true);
            updateInteropAvailability(DE_TESTBED_XMLRPC_INTEROP, true);
        }else{
            //make sure to disable interop interfaces in config file
            updateInteropAvailability(DE_TESTBED_DIS_INTEROP, false);
            updateInteropAvailability(DE_TESTBED_XMLRPC_INTEROP, false);
        }

	}

    /**
     * Handle configuring PowerPoint for integration with GIFT.
     *
     * @throws Exception
     */
	private void handlePowerPoint() throws Exception{

        if(settings.containsKey(InstallSettings.PPT_HOME)){

            setPowerPointEnvVariable();

            //make sure to enable interop interfaces in config file
            updateInteropAvailability(POWERPOINT_INTEROP, true);

        }else{
            //make sure to disable interop interfaces in config file
            updateInteropAvailability(POWERPOINT_INTEROP, false);
        }
	}

    /**
     * Handle downloading any Unity build outputs from gifttutoring.org for GIFT Wrap.
     *
     * @throws Exception if there was a problem downloading any unity build outputs.
     */
    private void handleUnityDownload() throws Exception {

        if (isPropertyTrue(settings.get(InstallSettings.UNITY_LAND_NAV_DOWNLOAD))) {
            setSubtaskString("Downloading Unity Land Nav scenario...");

            /* Build the path for the land nav scenario folder path. */
            String trainingAppsDirectory = CommonProperties.getInstance().getTrainingAppsDirectory();
            String landNavScenarioFolderName = PackageUtil.getLandNavScenario();
            String landNavScenarioFolderPath = Paths.get(trainingAppsDirectory, landNavScenarioFolderName).toString();

            /* Build the land nav scenario folder if it doesn't exist */
            File landNavScenarioFolder = new File(landNavScenarioFolderPath);
            if (!landNavScenarioFolder.exists()) {
                landNavScenarioFolder.mkdirs();
            }

            /* Get the property value of the Unity Land Nav URL and assert that
             * it isn't blank. */
            String unityLandNavURL = InstallerProperties.getInstance().getUnityLandNavDownloadURL();
            if(StringUtils.isBlank(unityLandNavURL)){
                throw new Exception("The Unity Land Nav Build Output URL was not found in the install.properties.");
            }

            File downloadedFile = null;
            try {
                downloadedFile = downloadFile(unityLandNavURL, landNavScenarioFolder, ROUGH_UNITY_LAND_NAV_FILE_SIZE_BYTES);

                /* Handle the case where the file was not downloaded. */
                if (downloadedFile == null) {
                    logger.error("Downloading the Unity Land Nav application from '" + unityLandNavURL + "' failed.");
                    return;
                }

                /* unzip the file and extract its contents */
                setSubtaskString("Unpackaging Unity Land Nav scenario...");
                setSubProgress(0);
                
                // monitor progress of unzip and update installer's progress dialog
                final ProgressIndicator progressIndicator = new ProgressIndicator();
                Thread subprogressUpdator = new Thread(new Runnable() {
                    
                    @Override
                    public void run() {

                        while(!progressIndicator.isComplete()){
                            setSubProgress(progressIndicator.getPercentComplete());
                            
                            try {
                                Thread.sleep(1000);
                            } catch (@SuppressWarnings("unused") InterruptedException e) {
                                // don't care, try again
                            }
                        }
                    }
                }, "Unity Download Unzip Progress updator");
                
                subprogressUpdator.start();
                
                try{
                    ZipUtils.unzipArchive(downloadedFile, landNavScenarioFolder, progressIndicator);
                }finally{
                    progressIndicator.setComplete(true);
                }
                
            } finally {
                /* remove temp files */
                if (downloadedFile != null) {
                    downloadedFile.delete();
                }
            }
        }
    }

    /**
     * Downloads a file from a URL
     *
     * @param fileURL HTTP URL of the file to be downloaded
     * @param saveDir path of the directory to save the file
     * @param fileSizeBytes the amount of bytes expected to be downloaded, used for progress calculations. If
     * less than 1, progress will not be updated.
     * @return the location of the downloaded file including the file name
     * @throws IOException if an I/O exception occurs
     */
    private File downloadFile(String fileURL, File saveDir, long fileSizeBytes) throws IOException {
        final URL url = new URL(fileURL);

        /* Using try-with-resources pattern. All auto-closeables inside the try-with will
         * automatically close after leaving the scope. No need to check them in the finally
         * block. */
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        try (BufferedInputStream inputStream = new BufferedInputStream(httpConn.getInputStream())) {
            int responseCode = httpConn.getResponseCode();

            // always check HTTP response code first
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = Constants.EMPTY;
                String disposition = httpConn.getHeaderField("Content-Disposition");

                if (disposition != null) {
                    // extracts file name from header field
                    int index = disposition.indexOf("filename=");
                    if (index > 0) {
                        fileName = disposition.substring(index + 10, disposition.length() - 1);
                    }
                } else {
                    // extracts file name from URL
                    fileName = FileTreeModel.createFromRawPath(url.getFile()).getFileOrDirectoryName();
                }

                String saveFilePath = saveDir + File.separator + fileName;

                // opens an output stream to save into file
                try (BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(saveFilePath))) {
                    int bytesRead = -1;
                    byte[] buffer = new byte[BUFFER_SIZE];
                    double currentPercent;
                    double lastUpdatePercent = 0;
                    long counter = 0;
                    
                    // sanity check
                    if(fileSizeBytes < 1){
                        fileSizeBytes = 1;
                    }
                    
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        
                        counter += bytesRead;
                        
                        currentPercent = (1.0 * counter / fileSizeBytes);
                        if(currentPercent > (lastUpdatePercent + UPDATE_THRESHOLD)){
                            //send update
                            setSubProgress((int)(currentPercent*100));
                            lastUpdatePercent = currentPercent;
                            //logger.debug("counter = "+counter+", fileSize = "+fileSize+", currentPercent = "+currentPercent+".");
                        }
                        
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    return new File(saveFilePath);
                }
            }
        } finally {
            if (httpConn != null) {
                httpConn.disconnect();
            }
        }

        return null;
    }
    
    /**
     * Handle configuring VR-Engage for integration with GIFT.
     *
     * @throws Exception
     */
    private void handleVrEngage() throws Exception{

        if(settings.containsKey(InstallSettings.VR_ENGAGE_HOME)){

            setVrEngageEnvVariable();

            //make sure to enable interop interfaces in config file
            updateInteropAvailability(DIS_INTEROP, true);
            updateInteropAvailability(VR_ENGAGE_PLUGIN_INTEROP, true);

            if(logger.isInfoEnabled()){
                logger.info("Installing VR-Engage extensions for GIFT.");
            }

            @SuppressWarnings("unchecked")
            HashMap<File, File> fileMap = (HashMap<File, File>) settings.get(TrainingApplicationInstallPage.VR_ENGAGE_FILES);

            for(File source : fileMap.keySet()) {
                if(logger.isInfoEnabled()){
                    logger.info("Copying \"" + source.getPath() + "\" to \"" + fileMap.get(source).getPath() + "\"");
                }
                if(source.isDirectory()) {
                    FileUtils.copyDirectory(source, fileMap.get(source));
                } else {
                    FileUtils.copyFile(source, fileMap.get(source));
                }
            }

                if(logger.isInfoEnabled()){
                    logger.info("Completed Install of VR-Engage extension for GIFT.");
                }

        }
    }

	/**
	 * Update the 'available' element value in the Gateway module's interop configuration file.
	 *
	 * @param interopImplName the unique name of an interop implementation in the interop configuration file.
	 * @param isAvailable the value to set the 'available' element to
	 */
	private void updateInteropAvailability(String interopImplName, boolean isAvailable){

	    if(interopFileHandler == null){
	        interopFileHandler = new InteropConfigFileHandler(INTEROP_CONFIG_FILENAME);
	    }

	    interopConfigFileUpdate = true;
	    interopFileHandler.setInteropAvailability(interopImplName, isAvailable);
	}

	/**
	 * Handle extracting GIFT databases for the GIFT install.
	 *
	 * @throws Exception
	 */
	private void handleGIFTDatabases() throws Exception{

	    setSubtaskString("Extracting Database(s)...");

        if(isPropertyTrue(settings.get(InstallSettings.EXTRACT_UMS_DATABASE))){
            
            if(CommonInstallCommands.STOP_DERBY_SERVER.isSupported()){

                try {
                    //
                    //Stop any currently running derby server
                    //
                    String stopDerbyOutput = executeAndWaitForCommand(CommonInstallCommands.STOP_DERBY_SERVER.getCommandLine());
                    
                    if(logger.isInfoEnabled()) {
                        logger.info("stopping derby server:" + stopDerbyOutput);
                    }
                    
                } catch (RuntimeException exception) {
                    
                    if(SupportedOSFamilies.UNIX.equals(PlatformUtils.getFamily())) {
                        
                        /* Derby's unix stop script returns an error if Derby is not currently running, 
                         * so report a possible failure but allow the DB extraction to proceed */
                        logger.warn("Failed to stop derby server. It is possible that Derby simply isn't already running.", exception);
                        
                    } else {
                        throw exception;
                    }
                }
            
            } else {
                logger.error("Unable to stop the Derby server to extract the UMS database. There is no command line implementation "
                        + "for the stop Derby server command on the current execution platform.");
                throw new Exception("GIFT was unable to extract the UMS database because the current operating system platform is not supported.");
            }

            //
            //Remove current UMS database directory
            //      
            FileUtils.deleteDirectory(UMS_DB_HOME);

            if(logger.isInfoEnabled()) {
                logger.info("Deleted "+UMS_DB_HOME);
            }

            //
            //Extract UMS derby database
            //
            String extractUMSDBOutput = null;
            
            /* If we are using a headless or UNIX/Linux environment, use ZipUtils to extract the UMS databases. */
            if (PlatformUtils.isHeadless() || PlatformUtils.getFamily().equals(PlatformUtils.SupportedOSFamilies.UNIX)) {
                extractUMSDBOutput = "Headless environment detected. Using platform-agnostic logic to handle unzipping the databases.";
                ZipUtils.unzipArchive(UMS_DB_ZIP, UMS_DB_EXTRACT_LOC, null);
            } else {
                /* Fall-back to the original extraction script used for extracting UMS databases. */
                extractUMSDBOutput = executeAndWaitForCommand(EXTRACT_UMS_DERBY_DB_CMD);
            }
            
            if(logger.isInfoEnabled()) {
                logger.info("Extract UMS database - output of command = "+ extractUMSDBOutput);
            }


            //Check that UMS database directory exists
            if(!UMS_DB_HOME.exists()){
                throw new Exception("Unable to find the extracted UMS database at "+UMS_DB_HOME+".");
            }

        }

        if(isPropertyTrue(settings.get(InstallSettings.EXTRACT_LMS_DATABASE))){

            if(CommonInstallCommands.STOP_DERBY_SERVER.isSupported()){

                try {
                    //
                    //Stop any currently running derby server
                    //
                    String stopDerbyOutput = executeAndWaitForCommand(CommonInstallCommands.STOP_DERBY_SERVER.getCommandLine());
                    
                    if(logger.isInfoEnabled()) {
                        logger.info("stopping derby server:" + stopDerbyOutput);
                    }
                    
                } catch (RuntimeException exception) {
                    
                    if(SupportedOSFamilies.UNIX.equals(PlatformUtils.getFamily())) {
                        
                        /* Derby's unix stop script returns an error if Derby is not currently running, 
                         * so report a possible failure but allow the DB extraction to proceed */
                        logger.warn("Failed to stop derby server. It is possible that Derby isn't running.", exception);
                        
                    } else {
                        throw exception;
                    }
                }
                
            } else {
                logger.error("Unable to stop the Derby server to extract the UMS datavase. There is no command line implementation "
                        + "for the stop Derby server command on the current execution platform.");
                throw new Exception("GIFT was unable to extract the UMS database because the current operating system platform is not supported.");
            }

            //
            //Remove current LMS database directory
            //
            FileUtils.deleteDirectory(LMS_DB_HOME);

            if(logger.isInfoEnabled()) {
                logger.info("Deleted "+LMS_DB_HOME);
            }

            //
            //Extract LMS derby database
            //
            //
            String extractLMSDBOutput = null;
            
            /* If we are using a headless or UNIX/Linux environment, use ZipUtils to extract the LMS databases. */
            if (PlatformUtils.isHeadless() || PlatformUtils.getFamily().equals(PlatformUtils.SupportedOSFamilies.UNIX)) {
                extractLMSDBOutput = "Headless environment detected. Using platform-agnostic logic to handle unzipping the databases.";
                ZipUtils.unzipArchive(LMS_DB_ZIP, LMS_DB_EXTRACT_LOC, null);
            } else {
                /* Fall-back to the original extraction script used for extracting LMS databases. */
                extractLMSDBOutput = executeAndWaitForCommand(EXTRACT_LMS_DERBY_DB_CMD);
            }
            
            if(logger.isInfoEnabled()) {
                logger.info("Extract LMS database - output of command = "+ extractLMSDBOutput);
            }

            //Check that LMS database directory exists
            if(!LMS_DB_HOME.exists()){
                throw new Exception("Unable to find the extracted LMS database at "+LMS_DB_HOME+".");
            }
        }

        updateProgress(EXTRACT_DATABASE_TOTAL_PROGRESS);
	}

	/**
	 * Handle the installation and configuration of Media Semantics for the GIFT installer.
	 *
	 * @throws Exception
	 */
	private void handleMediaSemantics() throws Exception{

        //Media Semantics Character
        if(isPropertyTrue(settings.get(InstallSettings.MEDIA_SEMANTICS))){

            setSubtaskString("Installing Media Semantics Character...");

            String output;

            if(logger.isInfoEnabled()) {
                logger.info("Installing MSC builder");
            }

            //execute character builder exe
            output = executeAndWaitForCommand(INSTALL_MSC_BUILDER_CMD);

            if(logger.isInfoEnabled()) {
                logger.info("Installed Media Semantics Character Builder output = '"+output+"'.");
                logger.info("Installing MSC server");
            }

            //upon success, execute character server exe
            output = executeAndWaitForCommand(INSTALL_MSC_SERVER_CMD);

            if(logger.isInfoEnabled()) {
                logger.info("Installed Media Semantics Character Server output = '"+output+"'.");
            }

            //programmatically change permissions of logs directory to write-able for all Users
            File locationA = new File("C:/Program Files (x86)/Character Server/Logs");
            File locationB = new File("C:/Program Files/Character Server/Logs");
            File location = null;
            if(locationA.exists()){
                location = locationA;
            }else if(locationB.exists()){
                location = locationB;
            }

            if(location != null){
                //use location path as arg to vb script

                if(logger.isInfoEnabled()) {
                    logger.info("Attempting to give the Media Semantics Character Server directory of "+location+" write privelages which are necessary for it to work correctly.");
                }
                List<String> argsList = new ArrayList<>(Arrays.asList(GIVE_WRITE_PERMISSIONS));
                argsList.add(location.getAbsolutePath());
                output = executeAndWaitForCommand(argsList.toArray(new String[argsList.size()]));

                if(logger.isInfoEnabled()) {
                    logger.info("Change write permissions output = "+output+".");
                }
            }else{
                logger.warn("Unable to find the Logs directory of the Media Semantics Character Server installation, therefore the Logs directory probably doesn't have write permissions which are needed in order to work correctly.");
            }

        }

        updateProgress(AVATAR_TOTAL_PROGRESS);

	}

    /**
     * Set the VBS environment variable need for GIFT
     *
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void setVBSEnvVariable() throws Exception{
        setEnvironmentVariable(VBS, InstallSettings.VBS_HOME);
    }

    /**
     * Set the DE Testbed environment variable need for GIFT
     *
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void setDETestbedEnvVariable() throws Exception{
        setEnvironmentVariable(DE_TESTBED, InstallSettings.DE_TESTBED_HOME);
    }

    /**
     * Set the PowerPoint environment variable need for GIFT
     *
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void setPowerPointEnvVariable() throws Exception{
        setEnvironmentVariable(POWERPOINT, InstallSettings.PPT_HOME);
    }
    
    /**
     * Set the VR-Engage environment variable need for GIFT
     *
     * @throws Exception if there was a critical error in setting the environment variable
     */
    private void setVrEngageEnvVariable() throws Exception{
        setEnvironmentVariable(VR_ENGAGE, InstallSettings.VR_ENGAGE_HOME);
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

            UserEnvironmentUtil.setEnvironmentVariable(environmentVariable, (String) settings.get(environmentVariable), false);
        }
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
        if(PlatformUtils.isHeadless() || PlatformUtils.SupportedOSFamilies.UNIX.equals(PlatformUtils.getFamily())) {
            
            /* If the execution platform is headless or Unix-based, write the process output to StdOut to show progress*/
            builder.redirectOutput(Redirect.INHERIT);
            builder.redirectError(Redirect.INHERIT);
        }
        
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
                JOptionPane.showMessageDialog(null, "GIFT installation cancelled due to an error.\n\n'"+failureMessage+"'."
                        + "\n\n Check install tool log in GIFT/output/logger/tools for more details.", "Cancelled", JOptionPane.ERROR_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null, "GIFT installation cancelled", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
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
	 * A helper class used to represent a operating-system-specific implementation of a command line command 
	 * 
	 * @author nroberts
	 */
	private static class OSCommand {
	    
	    /** The operating system that this command supports */
	    private SupportedOSFamilies os;
	    
	    /** The command line to invoke on the operating system when the command is executed */
	    private String[] commandLine;
	    
	    /**
	     * Creates a new operating system command
	     * 
	     * @param os the operating system that this command supports. Cannot be null.
	     * @param commandLine the arguments that make up the command line. Cannot be null or empty.
	     */
	    private OSCommand(SupportedOSFamilies os, String[] commandLine) {
	        if(os == null) {
	            throw new IllegalArgumentException("The operating system that a command should support cannot be null");
	        }
	        
	        if(commandLine == null || commandLine.length < 1) {
                throw new IllegalArgumentException("The command line of a command cannot be null or empty");
            }
	        
	        this.os = os;
	        this.commandLine = commandLine;
	    }

	    /**
	     * Gets the operating system that this command supports
	     * 
	     * @return the operating system. Cannot be null.
	     */
        public SupportedOSFamilies getOS() {
            return os;
        }

        /**
         * Gets the command line to invoke on the operating system when the command is executed
         * 
         * @return the arguments that make up the command line. Will not be null or empty
         */
        public String[] getCommandLine() {
            return commandLine;
        }
        
        @Override
        public String toString() {
        	StringBuilder builder = new StringBuilder();
        	builder.append("[OSCommand: ");
        	builder.append("OS-Families = ").append(os);
        	builder.append(", Command = ").append(commandLine);
        	builder.append("]");
        	return builder.toString();
        }
	}
}
