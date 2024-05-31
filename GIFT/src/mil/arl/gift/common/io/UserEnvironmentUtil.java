/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.common.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles configuring the user's environment variables needed for GIFT. 
 * 
 * @author bzahid
 */
public class UserEnvironmentUtil {
    
    /** logger instance */
	private static Logger logger = LoggerFactory.getLogger(UserEnvironmentUtil.class);
	
    /** 
     * script used to set a windows environment variable.
     * Note: setting the environment variable will not affect the environment during the execution of this application,
     *       nor will it affect any process this application creates.
     */
    private static final String SET_ENV_VAR_BATCH_NAME = "scripts"+File.separator+"install"+File.separator+"setEnvVariable.bat";
    private static final String SET_ENV_VAR_BATCH_NAME_NO_SUFFIX = "scripts"+File.separator+"install"+File.separator+"setEnvVariable";
    private static final String BATCH_SUFFIX = ".bat";
    
    //GIFT environment variables
    public static final String GIFT_PYTHON_HOME = "GIFT_PYTHON_HOME";
    public static final String VBS_HOME         = "VBS_HOME";
    public static final String DE_TESTBED_HOME  = "DE_TESTBED_HOME"; 
    public static final String PPT_HOME         = "POWERPOINT_HOME";
    public static final String VR_ENGAGE_HOME   = "VR_ENGAGE_HOME";
	
    /** 
     * script used to set a windows environment variable.
     * Note: setting the environment variable will not affect the environment during the execution of this application,
     *       nor will it affect any process this application creates.
     */
    private static final String SET_ENV_VAR_CMD = "scripts/install/setEnvVariable.bat";
    
    /** File containing the environment variables. */
	private static File environmentVariablesFile = null;
	
	/** The properties from the environment variables file. */
	private static Properties properties = new Properties();
	
	/** Batch script that sets environment variables. */
	private static File setEnvVarBatchFile = null;
		
	/**
	 * Initialize the properties & environment variables file.
	 */
	static {
		environmentVariablesFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "GIFTEnvironmentVariables.properties");
		
        if(environmentVariablesFile.exists()) {
        	try {
				properties.load(FileUtils.openInputStream(environmentVariablesFile));
			} catch (IOException e) {
				logger.error("Caught exception while attemtping to load properties from " + environmentVariablesFile.getPath(), e);
			}
        }
	}
	
	/**
	 * Gets the environment variable by checking the system variables and the user's environment variables file.
	 * @param variableName The environment variable to retrieve. Cannot be null or empty.
	 * @return The value of the environment variable or null if the variable was not found.
	 */
	public static String getEnvironmentVariable(String variableName) {
		if(variableName == null || variableName.isEmpty()) {
			throw new IllegalArgumentException("The environment variable name cannot be null or empty.");
		}
		
		return (properties.getProperty(variableName) == null) ? 
				System.getenv().get(variableName) : properties.getProperty(variableName);
	}
	
	/**
	 * Removes the environment variable.
	 * 
	 * @param variableName the environment variable to remove.  Cannot be null or empty.
	 * @throws Exception if an error occurred while removing the environment variable.
	 */
	public static void deleteEnvironmentVariable(String variableName) throws Exception{
	    
        if(variableName == null || variableName.isEmpty()) {
            throw new IllegalArgumentException("The environment variable name cannot be null or empty.");
        }
        
        logger.info("Removing "+variableName+" environment variable.");
        
        String[] command;
        properties.remove(variableName);
                
        try {
            // save the properties to the file.
            properties.store(FileUtils.openOutputStream(environmentVariablesFile), "GIFT Environment Variables");
        } catch (Exception e) {
            logger.error("Caught exception while writing properties to the environment variables file.", e);
        }
        
        command = new String[]{SET_ENV_VAR_CMD, variableName, ""};
        
        String output = executeAndWaitForCommand(command);
        logger.info("Remove '"+variableName+"' environment variable script output = '"+output+"'.");
        
        //check removal
        String value = getEnvironmentVariable(variableName);
        logger.info("Checking '"+variableName+"' environment variable value after removal = "+value);
	}
	
	/**
	 * Sets the environment variable.
	 * 
	 * @param variableName The environment variable to set. Cannot be null or empty.
	 * @param variableValue The value of the environment variable to set. Cannot be null or empty.
	 * @param setInMemory Whether or not the variable should be set in memory.
	 * @throws Exception If an error occurred while setting the environment variable.
	 */
	public static void setEnvironmentVariable(String variableName, String variableValue, boolean setInMemory)
			throws Exception {
		
		if(variableName == null || variableName.isEmpty()) {
			throw new IllegalArgumentException("The environment variable name cannot be null or empty.");
		}
		
		if(variableValue == null || variableValue.isEmpty()) {
			throw new IllegalArgumentException("The environment variable value cannot be null or empty.");
		}
		
        logger.info("Setting "+variableName+" environment variable to "+variableValue+".");
		
		String[] command;
		properties.put(variableName, variableValue);
				
        try {
        	// save the properties to the file.
			properties.store(FileUtils.openOutputStream(environmentVariablesFile), "GIFT Environment Variables");
		} catch (Exception e) {
			logger.error("Caught exception while writing properties to the environment variables file.", e);
		}
        
        if(setInMemory) {
	        try{
	            setEnvironmentVariableInMemory(variableName, variableValue);
	        }catch(Exception e){
	            logger.error("Failed to set the environment variable '"+variableName+"' value to '"+variableValue+"' in memory.", e);
	        }
	        
	        if(setEnvVarBatchFile == null){
	            setEnvVarBatchFile = retrieveSetEnvVarScript();
	        }
	        
	        command = new String[]{setEnvVarBatchFile.getAbsolutePath(), variableName, formatFilePath(variableValue)};
        } else {
        	command = new String[]{SET_ENV_VAR_CMD, variableName, formatFilePath(variableValue)};
        }
        
        String output = executeAndWaitForCommand(command);
        logger.info("Set "+variableName+" environment variable script output = '"+output+"'.");
	}
	
	/**
	 * Formats the variable filepath with required escape characters
	 * so it can be read correctly by the process
	 *  
	 * @param path original path of the variable
	 * @return the original path if it does not contain parentheses.
	 * Otherwise, a new path with escape characters
	 */
	private static String formatFilePath(String path){
	    String pathExpressions = ")";
	    if(path.contains(pathExpressions)){
	        StringBuilder newPath = new StringBuilder(path);
	        newPath.insert(path.indexOf(pathExpressions), '^');
	        return newPath.toString();
	    } else {
	        return path;
	    }
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
    private static String executeAndWaitForCommand(String[] command, int...acceptedExitValues) throws IOException, InterruptedException{
        
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
	 * Use java reflection to set the environment variable in the current map of environment variables
	 * given to this process when it started.  This is needed when other code needs to retrieve this environment
	 * variable in this process (e.g. running this installer in Java Web Start at the beginning of a course and
	 * when the course reaches logic to launch the application which needs the value of this variable).  
	 * 
	 * This will not update environment variables in the operating system, just in memory for this process.
	 * 
	 * @param environmentVariableName the name of the environment variable to set
	 * @param environmentVariableValue the value of the environment variable to set
	 * @throws Exception if there was a problem using reflection to update the environment variables in memory.
	 */
    @SuppressWarnings("unchecked")
	private static void setEnvironmentVariableInMemory(String environmentVariableName, String environmentVariableValue) throws Exception {
	    
	    try{
	        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
	        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
	        theEnvironmentField.setAccessible(true);
	        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
	        env.put(environmentVariableName, environmentVariableValue);
	        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
	        theCaseInsensitiveEnvironmentField.setAccessible(true);
	        Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
	        cienv.put(environmentVariableName, environmentVariableValue);
	        
	    }catch (@SuppressWarnings("unused") NoSuchFieldException e){
	        
            Class<?>[] classes = Collections.class.getDeclaredClasses();
            Map<String, String> env = System.getenv();
            for(Class<?> cl : classes) {
                if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
                    Field field = cl.getDeclaredField("m");
                    field.setAccessible(true);
                    Object obj = field.get(env);
                    
                    Map<String, String> map = (Map<String, String>) obj;
                    map.put(environmentVariableName, environmentVariableValue);
                }
            }

	    }

	}
	
	/**
	 * Retrieve 'set environment variable batch' script from classpath (resource) and write to temp file
	 * 
	 * @return File the batch script temporary file.  Note the file will automatically be deleted on JVM exit.
	 * @throws Exception if there was a problem creating the temp file
	 */
	private static File retrieveSetEnvVarScript() throws Exception{
	    
        logger.info("Trying to use 'set environment variable batch' file of "+SET_ENV_VAR_BATCH_NAME+" from classpath." );
	    
        InputStream iStream = FileFinderUtil.getFileByClassLoader(SET_ENV_VAR_BATCH_NAME);
        if(iStream == null){
            throw new IllegalArgumentException("Unable to find 'set environment variable batch' file named "+SET_ENV_VAR_BATCH_NAME);
        }
        
        File tempFile = File.createTempFile(SET_ENV_VAR_BATCH_NAME_NO_SUFFIX, BATCH_SUFFIX, FileUtil.getGIFTTempDirectory());
        try (InputStream in = iStream) {
            Files.copy(in, Paths.get(tempFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        }
        tempFile.deleteOnExit();
        
        return tempFile;
	}
	
	/**
	 * Clears the user's environment variables properties file.
	 */
	public static void clearEnvironmentVariablesFile() {
		if(environmentVariablesFile.exists()) {
			try {
				properties.clear();
				properties.store(FileUtils.openOutputStream(environmentVariablesFile), "GIFT Environment Variables");
			} catch (Exception e) {
				logger.error("Caught exception while trying to clear the environment variables file " + environmentVariablesFile.getPath(), e);
			}
		}
	}
	
	/**
	 * Deletes the user's environment variables properties file.
	 */
	public static void deleteEnvironmentVariablesFile() {
		if(environmentVariablesFile.exists()) {
			try {
				FileUtils.forceDelete(environmentVariablesFile);
			} catch (IOException e) {
				logger.error("Failed to delete the file " + environmentVariablesFile.getPath(), e);
			}
		}
	}
}
