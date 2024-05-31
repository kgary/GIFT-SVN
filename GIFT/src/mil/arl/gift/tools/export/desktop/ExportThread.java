/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.tools.export.desktop;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.time.FastDateFormat;
import org.ciscavate.cjwizard.WizardSettings;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.PackageUtil;
import mil.arl.gift.common.SurveyCheckRequest;
import mil.arl.gift.common.course.CourseFileValidationException;
import mil.arl.gift.common.course.CourseValidationResults;
import mil.arl.gift.common.course.dkf.DKFValidationException;
import mil.arl.gift.common.db.AbstractHibernateUtil;
import mil.arl.gift.common.db.MysqlCredentialChecker;
import mil.arl.gift.common.io.AbstractFolderProxy;
import mil.arl.gift.common.io.Constants;
import mil.arl.gift.common.io.ContentProxyInterface;
import mil.arl.gift.common.io.DesktopFolderProxy;
import mil.arl.gift.common.io.FileProxy;
import mil.arl.gift.common.io.FileUtil;
import mil.arl.gift.common.io.FileValidationException;
import mil.arl.gift.common.io.ZipUtils;
import mil.arl.gift.common.survey.SurveyContext;
import mil.arl.gift.domain.DomainCourseFileHandler;
import mil.arl.gift.net.api.message.MessageEncodeException;
import mil.arl.gift.net.api.message.codec.json.survey.SurveyContextJSON;
import mil.arl.gift.ums.db.UMSDatabaseManager;
import mil.arl.gift.ums.db.UMSHibernateUtil;
import mil.arl.gift.ums.db.survey.Surveys;

/**
 * Thread responsible for exporting GIFT
 *
 * @author cdettmering
 *
 */
public class ExportThread extends SwingWorker<Void, Void> {

    /** The logger for the class */
	private static Logger logger = LoggerFactory.getLogger(ExportThread.class);

    /**
     * The directory where the the files to zip are staged temporarily. This
     * directory is usually "GIFT/temp".
     */
    private static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

	private static final String XML_SUFFIX = ".xml";

    public static final FastDateFormat BACKUP_FILE_TIME_FORMAT = FastDateFormat.getInstance("MMddyy-HHmmss", null, null);
    
    /** path to the stop derby server batch file */
    private static final String[] STOP_DERBY_SERVER_CMD    = new String[]{"external" + File.separator + "db-derby-10-15.2.0-bin" + File.separator + "bin" + File.separator + "stopNetworkServer.bat"};

	/**
	 * directories and files used for exporting
	 */
	private static final String EXPORT_RESOURCES_DIRECTORY = "data" + File.separator + "exportResources";

	private static final String GIFT_EXPORT_README_FILENAME = "README.txt";
	private static final String GIFT_EXPORT_README_FILENAME_RESOURCE = EXPORT_RESOURCES_DIRECTORY + File.separator + "GIFTExport.README.txt";

	private static final String DOMAIN_EXPORT_README_FILENAME_RESOURCE = EXPORT_RESOURCES_DIRECTORY + File.separator + "DomainExport.README.txt";

    /** name of the hibernate configuration file to use for the export logic that will setup an embedded UMS db connection */
    private static final String EXPORTING_CONFIG_FILE = ".." + File.separator + "data" + File.separator + "exportResources" + File.separator + "exporting.ums.hibernate.cfg.xml";

	/**
	 * Total percentage contribution for each task
	 * Note: should total 100
	 */
	//required steps...
	private static final int RETRIEVE_SETTINGS_TOTAL_PROGRESS = 5;
	private static final int COPY_TEMP_DIRECTORY_TOTAL_PROGRESS = 25;
	private static final int GENERATE_LAUNCH_SCRIPT_TOTAL_PROGRESS = 5;
	private static final int ZIP_OUTPUT_DIRECTORY_TOTAL_PROGRESS = 25;
    private static final int CLEANUP_TOTAL_PROGRESS = 10;

	//If currently configured for MySQL
	private static final int CONFIGURE_DERBY_TOTAL_PROGRESS = 30;
	//OR, exporting domain content only
	private static final int EXPORT_DATABASE_TOTAL_PROGRESS = 30;

	private static final int TOTAL_PROGRESS = 100;

	private WizardSettings settings;
	private File temp = null;
	private int subProgress = -1;
	private int progress;
	private String subTask;
	private int currentFileCount;
	private int totalFileCount;

	/**
	 * Variables for canceling operations
	 */

	/**
	 * Collection of files created during the export process.
	 * This is used for removing files after a failed or successful export.
	 *
	 * Key: file created
	 * Value: boolean whether or not the file should be deleted at the end of a successful export.
	 */
	private Map<File, Boolean> createdFiles;

	/**
	 * Collection of pre-existing files that were renamed during the export process.
	 * This is used for reverting the renames after a failed export or successful export.
	 *
	 * Key: renamed file created
	 * Value: original file
	 */
	private Map<File, File> renamedFiles;

	private boolean isCancelled;
	private boolean cancelDone;
	private List<ExportCancelListener> listeners;

	boolean domainContentOnly = false;

	/**
	 * Creates a new ExportThread with the given settings
	 *
	 * @param settings Export settings selected by the user
	 */
	public ExportThread(WizardSettings settings) {
		this.settings = settings;
		createdFiles = new HashMap<>();
		renamedFiles = new HashMap<>();
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
	 * Updates the progress of the export operation.
	 *
	 * @param operation The operation that has just completed.
	 */
	public void updateProgress(int operation) {
		progress += operation;
		setProgress(progress);
	}

	/**
	 * Cancels the export thread operation and cleans any existing files.
	 */
	public void cancel() {
		if(logger.isInfoEnabled()){
		    logger.info("User canceled export...cleaning files");
		}
		setSubtaskString("Canceling");
		setSubProgress(-1);
		isCancelled = true;
	}

	/**
	 * Adds a cancel listener to the list of listeners.
	 *
	 * @param listener The ExportCancelListener to add.
	 */
	public void addCancelListener(ExportCancelListener listener) {
		listeners.add(listener);
	}

	/**
	 * Exports on a non GUI thread
	 */
	@Override
	public Void doInBackground() {
	    try{
	        export();
	    }catch(Exception e){
	        e.printStackTrace();
	        logger.error("Caught exception while running export.", e);
	    }
		return null;
	}

	/**
     * Sets the sub task progress
     *
     * @param value Progress represented as a percentage (out of 100). Use the
     *        value -1 to indicate that the underlying task is not running. This
     *        differs from the value 0 which indicates that the task is running
     *        but no progress has been made.
     */
	private void setSubProgress(int value) {
        if (value < -1 || value > 100) {
            throw new IllegalArgumentException("The sub progress value of "+value+" must be between -1 and 100 (inclusive).");
        }

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
	 * Begins a file related task involving one or more files.
	 *
	 * @param files collection of files about to perform a task on
	 */
	private void beginFilesTask(Collection<File> files){

	    int cnt = 0;
	    for(File file : files){

	        if(file.isDirectory()){
	            cnt += FileUtil.countFilesInDirectory(file);
	        }else{
	            cnt++;
	        }
	    }

	    totalFileCount = cnt;
	}

	/**
	 * Begins a file related task involving the file.
	 *
	 * @param dir The file to begin the task on.
	 */
	private void beginFileTask(File dir) {

	    if(dir.isDirectory()){
	        totalFileCount = FileUtil.countFilesInDirectory(dir);
	    }else{
	        totalFileCount = 1;
	    }
	}

	/**
	 * Ends a directory task and resets class variables.
	 */
	private void endDirectoryTask() {
        currentFileCount = 0;
		totalFileCount = 0;
	}

	/**
	 * Exports GIFT using the specified settings
	 *
	 * Try to cancel every so often, this is the preferred best practice to just killing the thread.
	 */
	private void export() {

		if(logger.isInfoEnabled()){
		    logger.info("Starting export");
		}

		File outputFile = null;
		StringBuffer exportOutputStringBuffer = new StringBuffer();
		boolean exportCreated = false;

		try {

			//
		    // Retrieve settings
		    //
            if(logger.isInfoEnabled()){
                logger.info("Retrieving settings");
            }
            setSubtaskString("Retrieving settings...");
			String output = (String)settings.get(ExportSettings.getOutputFile());
            outputFile = new File(output);
			DomainContent content = (DomainContent)settings.get(ExportSettings.getDomainContent());
			domainContentOnly = (Boolean)settings.get(ExportSettings.getExportDomainContentOnly());

			////////////////////////////////////////////////////////////////////////////////////////////////
			//
			// Retrieve current UMS hibernate settings
			//
            String configFileName = "ums" + File.separator + "ums.hibernate.cfg.xml";
            Configuration hibernateConfig = new Configuration();
            File hibernateConfigFile = new File(PackageUtil.getConfiguration() + File.separator + configFileName);

            if (!hibernateConfigFile.exists()) {
                throw new FileNotFoundException("The Hibernate config file '" + configFileName + "' could not be found.");
            }

            hibernateConfig.configure(hibernateConfigFile);

            String url = hibernateConfig.getProperty(AbstractHibernateUtil.CONNECTION_URL);
            String user = hibernateConfig.getProperty(AbstractHibernateUtil.USERNAME);
            String password = hibernateConfig.getProperty(AbstractHibernateUtil.PASSWORD);
            String dialect = hibernateConfig.getProperty(AbstractHibernateUtil.DIALECT);
            String driverClass = hibernateConfig.getProperty(AbstractHibernateUtil.DRIVER_CLASS);
            ////////////////////////////////////////////////////////////////////////////////////////////////


            if(logger.isInfoEnabled()){
                logger.info("Exporting to " + outputFile.getAbsolutePath());
            }
			updateProgress(RETRIEVE_SETTINGS_TOTAL_PROGRESS);

			// Copy GIFT into a temp directory with the domain content that was selected.
	 		File giftRoot = new File("..");
			temp = File.createTempFile("GIFT", Long.toString(System.nanoTime()));
	 		temp.delete();

	 		if(!ZipUtils.createDir(temp)){
	 		   throw new Exception("Unable to create directory named "+temp+".");
	 		}

	 		//delete the temp directory even upon success
			createdFiles.put(temp, true);

			//check if user has selected the cancel button
			if(tryCancel()){
			    return;
			}

			if(domainContentOnly){
	            // Export the appropriate database entries (not the entire database)
			    //Note: this has to be done before GIFT is copied to the temporary directory

			    if(logger.isInfoEnabled()){
			        logger.info("Configuring/Exporting Database Entries");
			    }
	            setSubtaskString("Configuring/Exporting Database Entries...");
	           	exportDatabase(content);

	            //check if user has selected the cancel button
	            if(tryCancel()){
	                return;
	            }

	            updateProgress(EXPORT_DATABASE_TOTAL_PROGRESS);
			}

            ////////////////////////////////////////////////////////////////////////////////////////////////
            //
            // Copy appropriate GIFT files into the temp directory
            //
			if(logger.isInfoEnabled()){
			    logger.info("Copying GIFT into temp directory " + temp.getAbsolutePath());
			}
            setSubtaskString("Copying to temporary directory...");

			beginFileTask(giftRoot);
			ExportFileFilter filter = new ExportFileFilter(settings);

			copyDirectory(giftRoot, temp, filter);
			endDirectoryTask();

			//check if user has selected the cancel button
			if(tryCancel()){
			    return;
			}

			// Reset sub progress
			setSubProgress(-1);

			updateProgress(COPY_TEMP_DIRECTORY_TOTAL_PROGRESS);
			////////////////////////////////////////////////////////////////////////////////////////////////

            if(tryCancel()) return;

            if(!domainContentOnly){
                //export entire database

                // Only copy the MySQL database if the MySQL database is configured to be used
                if(dialect.equals(AbstractHibernateUtil.MYSQL_DIALECT)) {
                    // Convert the MySQL database into the derby format before copying
                    if(logger.isInfoEnabled()){
                        logger.info("Converting MySQL database");
                    }
                    setSubtaskString("Converting MySQL database...");
                    convertMySQLDatabase(temp, url, user, password, driverClass);

                    //delete the derby log created when exporting MySQL to Derby
                    new File("derby.log").delete();

                    if(tryCancel()) return;
                }

                configureDerbyDatabase(temp, dialect);

                updateProgress(CONFIGURE_DERBY_TOTAL_PROGRESS);
            }

            //Check if user data should be removed from the GIFT database(s)
            //Note: if exporting domain content only, don't clear UMS user data because the db being
            //      used is the one associated with the GIFT instance running this export tool
            boolean exportUserData = (Boolean)settings.get(ExportSettings.getExportUserData());
            if(!exportUserData && !domainContentOnly){
                clearUMSUserData();
            }
            
            try{
                // stop derby server process that was started when the export tool was started
                executeAndWaitForCommand(STOP_DERBY_SERVER_CMD);
            }catch(@SuppressWarnings("unused") Exception e){
                // don't care, best effort 
            }


            //check if user has selected the cancel button
            if(tryCancel()){
                return;
            }

            // Generate the launch script
            if(logger.isInfoEnabled()){
                logger.info("Copying resource files");
            }
            setSubtaskString("Copying resource files...");
            copyResources(temp, content);

            updateProgress(GENERATE_LAUNCH_SCRIPT_TOTAL_PROGRESS);

            if(outputFile.exists()){
                //delete the old export file since the user has chosen to over-write it in a previous step

                int option = JOptionPane.showConfirmDialog(null,
                        "The file "+outputFile+" will now be over-written.\nDo you want to continue exporting?",
                        "Delete original file?",
                        JOptionPane.YES_NO_OPTION);

                if(option == JOptionPane.NO_OPTION){
                    cancel();
                    tryCancel();
                    return;
                }

                outputFile.delete();
            }

			// Zip into output file
            if(logger.isInfoEnabled()){
                logger.info("Zipping into output file " + outputFile.getAbsolutePath());
            }
			setSubtaskString("Creating zip file...");
			String exec = "scripts" + File.separator + "dev-tools" + File.separator + "export.bat";
            File exportScript = new File(exec);
			String base = temp.getAbsolutePath();
			String outputParam = outputFile.getAbsolutePath();

			//check if user has selected the cancel button
			if(tryCancel()){
			    return;
			}

			//delete the output zip upon failure, not success
	        createdFiles.put(outputFile, false);

            String command = "\"" + exportScript.getPath() + "\" \"" + base + "\" \"" + outputParam + "\"";

            if(logger.isInfoEnabled()){
                logger.info("Executing: " + command);
            }

            ProcessBuilder builder = new ProcessBuilder(command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            // wait until zip is done
            process.waitFor();

            InputStream stdout = process.getInputStream ();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
                String line;
                exportOutputStringBuffer.append("export.bat output:");
                while ((line = reader.readLine()) != null) {
                    exportOutputStringBuffer.append("\n").append(line);
                }
            }


			//check if user has selected the cancel button
			if(tryCancel()){
			    return;
			}

			exportCreated = true;

        } catch (Throwable e) {
            logger.error("Caught exception while trying to export.",e);
        }

		updateProgress(ZIP_OUTPUT_DIRECTORY_TOTAL_PROGRESS);
		setSubtaskString("Cleaning up...");

		//check if user has selected the cancel button
		if(tryCancel()){
		    return;
		}

		beginFilesTask(createdFiles.keySet());
		try{
		    performCleanup(exportCreated);
		}catch(IOException e) {
            logger.error("Caught exception while trying to perform the cleanup.", e);
            JOptionPane.showMessageDialog(null, "An error occurred while trying to perform clean, therefore not all the files created maybe deleted. " +
            		"Please check the log for more details.", "GIFT Cleanup Error", JOptionPane.ERROR_MESSAGE);
        }
		endDirectoryTask();

		//check if user has selected the cancel button
		if(tryCancel()){
		    return;
		}

		//check if zip was created
		if(!exportCreated || outputFile == null || !outputFile.exists()){
		    logger.error("The output file was not created.  Does the path exist and do you have write permissions in "+outputFile+"?  Also, check the export tool log for errors.");

		    if(exportOutputStringBuffer.length() > 0){
		        //show buffer if it has something
		        logger.error(exportOutputStringBuffer.toString());
		    }

		    setSubtaskString("GIFT Export Failed!  \nCheck export tool log in GIFT/output/logger/tools/ for more information.");
		}else{
		    if(logger.isInfoEnabled()){
		        logger.info(exportOutputStringBuffer.toString());
		    }
		    setSubtaskString("GIFT Export Successful!");
		}

		setProgress(TOTAL_PROGRESS);
		setSubProgress(TOTAL_PROGRESS);
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
	 * Clear all user data from the exported UMS database (not the source UMS db).
	 *
	 * @throws IOException if there was a severe problem during the clearing of the user data
	 * @throws ConfigurationException
	 */
	private void clearUMSUserData() throws IOException, ConfigurationException{

	    //start embedded derby UMS db instance using the export temp folder location as the db location
	    ExportingUMSDatabaseManager exportingUMSMgr = new ExportingUMSDatabaseManager();

	    //call UMS db mgr clear user data method to handle erasing the appropriate table's rows
	    exportingUMSMgr.clearUserData();
	    
	    try{
	        exportingUMSMgr.cleanup();
	    }catch(Exception e){
	        logger.error("Caught exception while trying to cleanup the UMS db manager.", e);
	    }
	}

	/**
	 * Copies resource files into the GIFT root directory.
	 *
	 * @param giftRoot The GIFT (exported) root directory
	 * @param content The selected domain folder root level files selected for export
	 * @throws IOException if there was a problem copying files
	 */
	private void copyResources(File giftRoot, DomainContent content) throws IOException {

	    File readmeSrc;
	    File readmeDest;

        try {

    	    if(domainContentOnly){

                //README for Domain only export
                readmeSrc = new File(DOMAIN_EXPORT_README_FILENAME_RESOURCE);
                readmeDest = new File(giftRoot.getAbsolutePath() + File.separator + GIFT_EXPORT_README_FILENAME);

    	    }else{
    	        //README for full GIFT export
    	        readmeSrc = new File(GIFT_EXPORT_README_FILENAME_RESOURCE);
    	        readmeDest = new File(giftRoot.getAbsolutePath() + File.separator + GIFT_EXPORT_README_FILENAME);
    	    }


			FileUtils.copyFile(readmeSrc, readmeDest);

			// Generate a quick list of exported domain content
			PrintWriter readmeWriter = new PrintWriter(new FileOutputStream(readmeDest, true));
			readmeWriter.println();
			readmeWriter.println();
			for(ContentProxyInterface file : content.getExportFiles()) {
				readmeWriter.append(file.getFileId());

				if(file instanceof AbstractFolderProxy){
				    readmeWriter.append(File.separator);
				}

				readmeWriter.println();
			}
			readmeWriter.close();

		} catch(IOException e) {
			logger.error("Caught exception while trying to copy resource files", e);

			throw e;
		}
	}

	/**
	 * This method converts all of the MySQL database entries into Derby files.
	 *
	 * @param giftRoot the root of the GIFT temporary directory where GIFT is being exported too
	 * @param mySqlUrl the url of the MySQL database
	 * @param username a username for the MySQL database
	 * @param password the password for the user
	 * @param driverClass the driver class name used for the database connection (e.g. com.mysql.jdbc.Driver, org.apache.derby.jdbc.ClientDriver)
	 * @throws Exception if there was an issue during the conversion process.
	 */
    private void convertMySQLDatabase(File giftRoot, String mySqlUrl, String username, String password, String driverClass) throws Exception {

        try {

            MysqlCredentialChecker checker = new MysqlCredentialChecker();

            boolean validCreds = checker.checkCredentials(mySqlUrl, username, password, driverClass);

            if(!validCreds) {

                throw new IllegalArgumentException("The MySQL credentials provided are not valid");
            }

            String dir = giftRoot + File.separator + "GIFT";

            //reference these from the root GIFT because they might not be in the temp GIFT
            String antPath = "external" + File.separator + "ant" + File.separator + "bin" + File.separator + "ant.bat";
            String scriptPath = "scripts" + File.separator + "database"+ File.separator + "dbExport" + File.separator + "database-convert.xml";

            String outputPath = dir + File.separator + "output" + File.separator + "dbTemp";

            // Chop off the last slash of the URL so relative paths can be appended as needed
            if (mySqlUrl.endsWith("/")) {

                mySqlUrl = mySqlUrl.substring(0, mySqlUrl.length() - 1);
            }

            //Note: had to build the command this way because, in some instances, the Sql Url 2 forward slashes would be changed to a single backslash
            //      in the database-convert.xml.
            ProcessBuilder builder = new ProcessBuilder(antPath,
                    "-f",
                    scriptPath,
                    "-DmySqlUrl=" + mySqlUrl,
                    "-Dusername=" + username,
                    "-Dpassword=" + password,
                    "-Doutput.dir=" + outputPath);

            List<String> commandParams = builder.command();
            StringBuilder commandsb = new StringBuilder();
            for(String str : commandParams){
                commandsb.append(str).append(" ");
            }

            builder.redirectErrorStream(true);
            Process convertProcess = builder.start();

            InputStream stdout = convertProcess.getInputStream ();
            StringBuffer outputStringBuffer = new StringBuffer();
            try (final BufferedReader reader = new BufferedReader(new InputStreamReader(stdout))) {
                String line;
                outputStringBuffer.append(commandsb.toString()).append(" output:");

                /* Note: this while loop will finish once the process has
                 * finished, even if some part of the process doesn't write
                 * anything to the output stream. This was tested by putting a
                 * "<sleep milliseconds="100000"/>" in the
                 * database-convert.xml. */
                while ((line = reader.readLine()) != null) {
                    outputStringBuffer.append("\n").append(line);
                }

            }

            File newDerbyDatabaseArchive = new File(giftRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "output" + File.separator + "dbTemp" + File.separator + "GiftDatabases.zip");

            if(!newDerbyDatabaseArchive.exists()){
                //ERROR
                logger.error("The convert command failed because the "+newDerbyDatabaseArchive+" file could not be found.  Here is the output captured for debugging purposes:\n"+outputStringBuffer.toString()+".");

                throw new Exception("The convert MySQL database command failed.");
            }

            File oldDerbyDatabaseArchive = new File(giftRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "data" + File.separator + "GiftDatabases.zip");

            FileUtils.copyFile(newDerbyDatabaseArchive, oldDerbyDatabaseArchive);

            File dbTempParent = newDerbyDatabaseArchive.getParentFile();

            if(domainContentOnly){
                FileUtils.deleteDirectory(dbTempParent.getParentFile());
            }else{
                FileUtils.deleteDirectory(dbTempParent);
            }

        } catch (Exception e) {
            logger.error("Caught exception while copying MySQL Database", e);

            throw e;
        }
    }


    /**
     * Makes sure the UMS and LMS hibernate configuration files are setup for using derby.
     *
     * @param giftTempRoot the temp location of the exported GIFT.  This is where to do file manipulations at.
     * @param dbDialect the current hibernate driven database connection dialect (org.hibernate.dialect.MySQLDialect, org.hibernate.dialect.DerbyDialect)
     * @throws Exception if there was a problem configuring for derby
     */
    private void configureDerbyDatabase(File giftTempRoot, String dbDialect) throws Exception{

        //Config Cases:
        //  MySQL + !domainContentOnly - backup current configs, copy derby config as current config.
        //  Derby + !domainContentOnly - nothing to do, configs are the way they should be

        if(!domainContentOnly){

            //location of the derby database data
            File derbyDatabaseDir = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "data" + File.separator + "derbyDB");

            if (dbDialect.equals(AbstractHibernateUtil.MYSQL_DIALECT)) {
                // Backup the MySql configs

                File lmsOriginal = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "config" + File.separator + "lms" + File.separator + "lms.hibernate.cfg.xml");
                File umsOriginal = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "config" + File.separator + "ums" + File.separator + "ums.hibernate.cfg.xml");

                File lmsBackup = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "config" + File.separator + "lms" + File.separator + "lms.hibernate.cfg.xml.Original");
                File umsBackup = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "config" + File.separator + "ums" + File.separator + "ums.hibernate.cfg.xml.Original");

                File lmsDerby = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "config" + File.separator + "lms" + File.separator + "lms.hibernate.cfg.xml.derby");
                File umsDerby = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "config" + File.separator + "ums" + File.separator + "ums.hibernate.cfg.xml.derby");

                FileUtils.copyFile(lmsOriginal, lmsBackup);
                FileUtils.copyFile(umsOriginal, umsBackup);

                // Delete originals
                FileUtils.deleteQuietly(lmsOriginal);
                FileUtils.deleteQuietly(umsOriginal);

                // Rename Derby to originals
                FileUtils.copyFile(lmsDerby, lmsOriginal);
                FileUtils.copyFile(umsDerby, umsOriginal);

                //
                //if the current configuration is for MySQL, then delete any pre-existing derby contents in the GIFT temp location
                //and then extract the derby zip that was newly created with the MySQL content.
                //
                FileUtils.deleteDirectory(derbyDatabaseDir);

                //from the Base GIFT
                File derbyDatabaseArchive = new File("data" + File.separator + "GiftDatabases.zip");

                //in the temp GIFT
                File outputDir = new File(giftTempRoot.getAbsolutePath() + File.separator + "GIFT" + File.separator + "data");

                // Unzip the full, updated Derby databases
                ZipUtils.unzipArchive(derbyDatabaseArchive, outputDir, null);

            }else{

                // If Derby is set to be used but there is no DerbyDb folder,
                // Derby is possibly misconfigured and so an error is thrown
                if (!derbyDatabaseDir.exists()) {
                    throw new FileNotFoundException("The derby database is configured to be used but it does not exist.");
                }
            }
        }
    }
    /**
	 * Exports the necessary database entries for the GIFT export to use.
	 *
	 * @param content The domain content selected for export
     * @throws IllegalArgumentException if the exportResources parameter is null
	 * @throws Exception if there was a severe problem exporting the database content needed for the tutor export
	 */
    private void exportDatabase(DomainContent content) throws Throwable {

        try {

            //Db entries cases:
            //  MySQL full - delete old derby db and extract newly created derby zip
            //  MySQL + domainContentOnly - get survey contexts of courses and dkfs, write as json strings to single file per course and dkf file with similar name
            //  Derby full - nothing to do, the current derby db will be copied as is
            //  Derby + domainContentOnly - get survey contexts of courses and dkfs, write as json strings to single file per course and dkf file with similar name

            if(domainContentOnly){

                //
                // get survey instances needed based on domain content
                //

                List<ContentProxyInterface> createdContentFiles = new ArrayList<>();

                for(ContentProxyInterface file : content.getExportFiles()){

                    if(file instanceof FileProxy){
                        //call function to handle survey refs for course file, and add all image references that the surveys use
                    	exportCourseDBReferences((FileProxy)file, createdContentFiles);
                    }
                }

                //add newly created export files to list of domain content files to export
                content.addExportFiles(createdContentFiles);

            }

        } catch (IOException | DKFValidationException e) {

            logger.error("Caught exception while configuring Derby", e);
            throw e;
        }
    }

    /**
     * This method exports all database references (e.g. survey context, surveys, etc.) to one or more files
     * based on the references made starting at the course file provided.
     *
     * @param courseFile the course file to export all database references for
     * @param createdContentFiles collection of files created as part of the export db references process
     * @throws IllegalArgumentException if the domainResources parameter is null
     * @throws Exception if there was a server error exporting database references for this course
     * @throws FileValidationException if there was a problem with a course file
     */
    private void exportCourseDBReferences(FileProxy courseFile, List<ContentProxyInterface> createdContentFiles) throws Exception, FileValidationException{

        DesktopFolderProxy domainFolderProxy = new DesktopFolderProxy(new File(ExportProperties.getInstance().getDomainDirectory()));
        DomainCourseFileHandler dcfh = new DomainCourseFileHandler(courseFile, domainFolderProxy, true);
        CourseValidationResults validationResults = dcfh.checkCourse(true, null);
        if(validationResults.hasCriticalIssue() || validationResults.hasImportantIssues()){
            throw new CourseFileValidationException("Failed to validation", "There was one or more issues validating", courseFile.getName(), validationResults.getFirstError());
        }

        //create export file based on course file name
        String courseFileName = courseFile.getFileId();
        String courseExportFileName = courseFileName.replace(XML_SUFFIX, FileUtil.SURVEY_REF_EXPORT_SUFFIX);
        File courseExportFile = new File(courseExportFileName);

        if(courseExportFile.exists()){
        	//backup the existing file
        	File backup = new File(courseExportFile.getAbsolutePath() + "."+BACKUP_FILE_TIME_FORMAT.format(new Date())+".EXPORT.BACKUP");
        	FileUtils.copyFile(courseExportFile, backup);
        	renamedFiles.put(backup, courseExportFile);
        }

        //piggy back on the survey validation request logic to gather survey references used in this course
        List<SurveyCheckRequest> surveyChecks = dcfh.buildSurveyValidationRequest();

        //keep track of ids already exported to avoid exporting the same survey context more than once for a course
        List<Integer> exportedSurveyContexts = new ArrayList<>();

        for(SurveyCheckRequest check : surveyChecks){

            int surveyContextId = check.getSurveyContextId();

            if(exportedSurveyContexts.contains(surveyContextId)){
                //already exported this survey context
                continue;
            }

            SurveyContext sContext = Surveys.getSurveyContext(surveyContextId);

            if(sContext == null){
                throw new Exception("There was a problem converting the survey context with id of "+surveyContextId+".");
            }

            //append to the course export file if:
            // 1) the source is not known (for whatever reason)
            // 2) this survey check is a course level survey reference
            boolean append = check.getSourceReference() == null || check.getSourceReference().getFileId().equals(courseFileName);

            //reference to the export file for this survey context
            File exportFile = null;
            if(append){
                exportFile = courseExportFile;
            }else{
                //create export file based on dkf name
                String dkfFileName = check.getSourceReference().getFileId();
                dkfFileName = dkfFileName.replace(XML_SUFFIX, FileUtil.SURVEY_REF_EXPORT_SUFFIX);
                exportFile = new File(dkfFileName);
            }

            if(exportSurveyContext(sContext, exportFile, append)){

                //add new file to content to be exported
                createdContentFiles.add(new FileProxy(exportFile));

                //need to delete this file since it is in the non-temp GIFT directory
                createdFiles.put(exportFile, true);

            }else{
                //ERROR
                throw new Exception("There was a writting the survey context with id of "+surveyContextId+" to "+exportFile+".");
            }

            exportedSurveyContexts.add(surveyContextId);
        }
    }

    /**
     * Exports a survey context to the export file provided.
     *
     * @param sContext Survey context object to export.
     * @param exportFile the file to export (i.e. write) too
     * @param append whether to append the survey context export to the existing export file contents
     * @return True if export was successful, false otherwise.
     */
    private static boolean exportSurveyContext(SurveyContext sContext, File exportFile, boolean append) {

        // Convert to JSON
        SurveyContextJSON json = new SurveyContextJSON();
        JSONObject obj = new JSONObject();

        try {
            json.encode(obj, sContext);

            PrintWriter writer = new PrintWriter(exportFile);

            if(append){
                writer.append(obj.toJSONString());
            }else{
                writer.println(obj.toJSONString());
            }
            writer.close();

        } catch(MessageEncodeException e) {
            logger.error("Caught exception when trying to export survey context", e);
            return false;
        } catch(FileNotFoundException e) {
            logger.error("Caught exception when trying to export survey context", e);
            return false;
        }

        return true;
    }

	/**
     * Recursively copies a source directory to a destination directory.
     *
     * @param src The source directory. If this source directory is a descendant
     *        of {@link #TEMP_DIR}, no action is taken.
     * @param dest The destination directory
     * @param filter The {@link FileFilter} that specifies the rules specifying
     *        which files should be copied from the source directory. A value of
     *        null indicates that all files in the source directory should be
     *        copied.
     * @throws IOException if there was a problem converting either the source
     *         directory or the destination directory to a canonical path.
     */
    private void copyDirectory(File src, File dest, FileFilter filter) throws IOException {

        /* Don't attempt to copy anything from the temp directory */
        if (src.getCanonicalPath().startsWith(TEMP_DIR.getCanonicalPath())) {
            return;
        }

		// If source doesn't exist and is not a directory, return.
		if(!(src.exists() && src.isDirectory())) {
			logger.error("Could not copy " + src.getAbsolutePath() + " source directory doesn't exist or path is not a directory");
			return;
		}

		// If destination doesn't exist, make a directory, if that fails, return.
		if(!dest.exists() && !dest.mkdir()) {
			logger.error("Could not copy " + src.getAbsolutePath() + " destination directory doesn't exist or could not be created");
			return;
		}

		// At this point source and destination should be valid
		File[] files = src.listFiles(filter);
        for (File f : files) {
			if(f.isDirectory()) {
				copyDirectory(f, new File(dest.getAbsolutePath() + File.separator + f.getName()), filter);
			} else if(f.isFile()) {
                try {
					currentFileCount++;
					File d = new File(dest.getAbsolutePath() + File.separator + f.getName());
					setSubProgress((int)((float)currentFileCount / (float)totalFileCount * TOTAL_PROGRESS));
					setProgress(getSubProgress() / (TOTAL_PROGRESS / COPY_TEMP_DIRECTORY_TOTAL_PROGRESS) + RETRIEVE_SETTINGS_TOTAL_PROGRESS);

                    if (logger.isInfoEnabled()) {
					    logger.info("Copying " + f.getAbsolutePath() + " to " + d.getAbsolutePath());
					}
                    FileUtils.copyFile(f, d);

					if(tryCancel()) return;
				} catch(IOException e) {
					logger.error("Caught exception while trying to copy " + f.getAbsolutePath() + " to " + dest.getAbsolutePath() + File.separator + f.getName(), e);
				}
			}
        }
	}

	/**
	 * Recursively deletes directory files in order to display progress to the user.
	 *
	 * @param dir The directory to delete
	 */
	private void deleteDirectory(File dir) {

		if(!(dir.exists() && dir.isDirectory())) {
			logger.error("Could not delete " + dir.getAbsolutePath() + " directory doesn't exist or path is not a directory");
			return;
		}

		File[] files = dir.listFiles();
		for(File f : files) {
			if(f.isDirectory()) {

			    if(f.getName().equals(Constants.SVN)){
			        //don't need to show progress when deleting .svn files because they shouldn't have been copied
			        //to the temp directory anyway.  If it isn't deleted this way than the 'setProgress' below results
			        //in an exception because the progress goes over 100% when deleting files in .svn folders.
	                if(!FileUtils.deleteQuietly(f)) {
	                    logger.error("Could not delete " + f.getAbsolutePath());
	                }
			    }else{
			        deleteDirectory(f);
			    }
			} else if(f.isFile()) {
				currentFileCount++;
				int progressSoFar = RETRIEVE_SETTINGS_TOTAL_PROGRESS + COPY_TEMP_DIRECTORY_TOTAL_PROGRESS + CONFIGURE_DERBY_TOTAL_PROGRESS +
						GENERATE_LAUNCH_SCRIPT_TOTAL_PROGRESS + ZIP_OUTPUT_DIRECTORY_TOTAL_PROGRESS;
				setSubProgress((int)((float)currentFileCount / (float)totalFileCount * TOTAL_PROGRESS));
				setProgress(getSubProgress() / (TOTAL_PROGRESS / CLEANUP_TOTAL_PROGRESS) + progressSoFar);

                if (logger.isInfoEnabled()) {
				    logger.info("Deleting " + f.getAbsolutePath());
				}

				if(!FileUtils.deleteQuietly(f)) {
					logger.error("Could not delete " + f.getAbsolutePath());
				}
			}
		}

		//now that all files have been deleted, delete the directory
		dir.delete();
	}

	/**
	 * Return whether a cancel export operation has taken place.
	 *
	 * @return boolean - true iff the export process is being or has been canceled
	 */
	private boolean tryCancel() {

		if(!isCancelled) {
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
			JOptionPane.showMessageDialog(null, "Export operation cancelled");
		} catch(IOException e) {
			logger.error("Caught exception while trying to cancel", e);
			JOptionPane.showMessageDialog(null, "An error occurred while trying to cancel. Please check the log for more details.", "GIFT Cancel Operation Error", JOptionPane.ERROR_MESSAGE);
		}
		for(ExportCancelListener listener : listeners) {
			listener.onCancel();
		}
	}

	/**
	 * Cleanup the GIFT that performed the export.
	 *
	 * Delete files and directories created during the export process.
	 *
	 * @param successfulExport whether the cleanup is being performed as a result of a successful export or not.
	 * @throws IOException if there was a problem deleting a file
	 */
	private void performCleanup(boolean successfulExport) throws IOException{

        for(File file : createdFiles.keySet()) {

            boolean deleteAlways = createdFiles.get(file);
            if(deleteAlways || !successfulExport){

                if(file.isDirectory()) {
                    deleteDirectory(file);
                } else if(file.isFile()) {
                    FileUtils.deleteQuietly(file);
                }
            }
        }

        for(File renamedFile : renamedFiles.keySet()){
        	renamedFile.renameTo(renamedFiles.get(renamedFile));
        }
	}

	/**
	 * This extension of the UMS hibernate util class allows the export logic to over-ride the connection url
	 * property value(s) in order to redirect the connection to use the exported temporary folder location.
	 *
	 * @author mhoffman
	 *
	 */
	private class ExportingUMSHibernateUtil extends UMSHibernateUtil{

	    /**
	     * Class constructor - use a different hibernate config file setup for embedded UMS database connection
	     * @throws ConfigurationException
	     */
	    public ExportingUMSHibernateUtil() throws ConfigurationException{
	        super(EXPORTING_CONFIG_FILE);

	    }

	    @Override
        protected SessionFactory buildSessionFactory(boolean createSchema) throws ConfigurationException{

	        //use the temporary folder location for the database path
	        config.setProperty(CONNECTION_URL, "jdbc:derby:"+temp.getAbsolutePath()+ File.separator + "GIFT" + File.separator + "data" + File.separator + "derbyDb" + File.separator + "GiftUms");
	        config.setProperty("hibernate.connection.url", "jdbc:derby:"+temp.getAbsolutePath()+ File.separator + "GIFT" + File.separator + "data" + File.separator + "derbyDb" + File.separator + "GiftUms");

	        return super.buildSessionFactory(createSchema);
	    }
	}

	/**
	 * This extension of the UMS database manager allows the export logic to over-ride the default hibernate
	 * util class that is used to configure the hibernate connection.
	 *
	 * @author mhoffman
	 *
	 */
	private class ExportingUMSDatabaseManager extends UMSDatabaseManager{

	    public ExportingUMSDatabaseManager() throws ConfigurationException{
	        super(new ExportingUMSHibernateUtil());
	    }
	}

}
