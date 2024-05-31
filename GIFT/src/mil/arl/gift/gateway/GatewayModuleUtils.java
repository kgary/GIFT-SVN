/**
 * Copyright Dignitas Technologies, LLC
 * 
 * This file and its contents are governed by one or more distribution and
 * copyright statements as described in the LICENSE.txt file distributed with
 * this work.
 */
package mil.arl.gift.gateway;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import mil.arl.gift.common.ConfigurationException;
import mil.arl.gift.common.io.FileFinderUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides utilities to interops for manipulating an application
 *
 * @author jleonard
 */
public class GatewayModuleUtils {

    /** Instance of the logger */
    private static Logger logger = LoggerFactory.getLogger(GatewayModuleUtils.class);

    private static final String LOCAL_WINDOW_UTIL_EXE_FILENAME = "../Training.Apps/Util/WindowUtil.exe";
    private static final String JWS_WINDOW_UTIL_EXE_FILENAME = "Training.Apps/Util/WindowUtil.exe";
    
    /** the exe file to use when running AHK commands */
    private static File windowUtilExeFile;
    
    /** use this string when you want to apply AHK commands based on window title alone. */
    public static final String AHK_CLASS_WILDCARD = "*";
    
    /**
     * Find the Window Util exe file from disk or as a resource (i.e. Java Web Start jar)
     */
    static{
        
        InputStream iStream = FileFinderUtil.getFileByClassLoader(JWS_WINDOW_UTIL_EXE_FILENAME);
        if(iStream == null){
            windowUtilExeFile = new File(LOCAL_WINDOW_UTIL_EXE_FILENAME);
        }else{

            try{
                windowUtilExeFile = File.createTempFile("GIFT-WindowUtil-"+Long.toString(System.nanoTime()), ".exe");
                
                try(OutputStream out = FileUtils.openOutputStream(windowUtilExeFile)){
                        IOUtils.copy(iStream, out);
                }
                
                windowUtilExeFile.deleteOnExit();
                if(logger.isInfoEnabled()){
                    logger.info("Copied WindowUtil.exe to temp file of "+windowUtilExeFile+".");
                }
                
            }catch(Exception e){
                logger.error("Caught exception while trying to write the WindowUtil.exe to the user's temp folder.", e);
                windowUtilExeFile = null;
            }
        }

    }
    
    /**
     * Sets if an application's window should or shouldn't always be on top
     *
     * @param ahkClass The AHK class of the application. If null this method does nothing.<br/>
     * To obtain the AHK Class value use AutoHotKey Spy (AU3_Spy.exe):
     * 1. run AU3_Spy.exe (obtain from AHK download, currently using AutoHotkey104404.zip)
     * 2. with window open (e.g. VBS3), get the 'ahk_class' value from the 'Active Window Info' dialog. This is the value
     * that needs to be provided as the ahkClass parameter.
     * Note: use AHK_CLASS_WILDCARD value when you want to apply AHK commands to first found window based on window title alone.
     * @param windowTitle the title of the application's window.  Can be null or an empty string if the window
     *              search shouldn't use the title and instead use the ahkClass value.
     * @param alwaysOnTop If the always on top should be on or off
     * @throws IllegalArgumentException if there was an issue with one of the parameters provided to this method
     * @throws ConfigurationException if the Window Utility executable was not found on the users computer
     */
    public static void setAlwaysOnTop(String ahkClass, String windowTitle, boolean alwaysOnTop) throws IllegalArgumentException, ConfigurationException{
        
        if(ahkClass == null){
            return;
        }
        
        if((windowTitle == null || windowTitle.isEmpty()) && ahkClass.equals(AHK_CLASS_WILDCARD)){
            throw new IllegalArgumentException("The AHK class value can't be "+AHK_CLASS_WILDCARD+" when the window title is not provided.");
        }else if(windowUtilExeFile == null){
            throw new ConfigurationException("Unable to change whether the application's window is the foreground window or not.",
                    "The Window Util exe was not set.  There should have been an error reported earlier.",
                    null);
        }
        
        try {
            
            String titleParam = windowTitle != null ? windowTitle : "";
            
            if (alwaysOnTop) {
                
                String output = runCommand(windowUtilExeFile.getAbsolutePath(), "setAlwaysOnTop", ahkClass, "On", titleParam);
                   
                if(logger.isInfoEnabled()){
                    logger.info("Setting always on top for '" + ahkClass + "'.  Script output = \n"+output+".");
                }
                
            } else {
                
                String output = runCommand(windowUtilExeFile.getAbsolutePath(), "setAlwaysOnTop", ahkClass, "Off", titleParam);
                                        
                if(logger.isInfoEnabled()){
                    logger.info("Removing always on top for '" + ahkClass + "'.  Script output = \n"+output+".");
                }
            }

        } catch (IOException ex) {
            logger.error("There was an error setting always on top for '" + ahkClass + "'", ex);
        }
    }

    /**
     * Sets if an application's window should or shouldn't always be on top. If null this method does nothing.
     * Note: the window search will ignore window title which may cause issues if more than one
     *      window is opened that has the same AHK Class value.
     *
     * @param ahkClass The AHK class of the application
     * @param alwaysOnTop If the always on top should be on or off
     * @throws IllegalArgumentException if there was an issue with one of the parameters provided to this method
     * @throws ConfigurationException if the Window Utility executable was not found on the users computer 
     */
    public static void setAlwaysOnTop(String ahkClass, boolean alwaysOnTop) throws IllegalArgumentException, ConfigurationException {
        setAlwaysOnTop(ahkClass, null, alwaysOnTop);
    }
    
    /**
     * Gives an application's window focus
     *
     * @param ahkClass The AHK class of the application.. If null this method does nothing.</br>
     *          Note: use AHK_CLASS_WILDCARD value when you want to apply AHK commands to first found window based on window title alone.
     * @param windowTitle the title of the application's window.  Can be null or an empty string if the window
     *              search shouldn't use the title and instead use the ahkClass value.
     * @throws IllegalArgumentException if there was an issue with one of the parameters provided to this method
     * @throws ConfigurationException if the Window Utility executable was not found on the users computer        
     */
    public static void giveFocus(String ahkClass, String windowTitle) throws IllegalArgumentException, ConfigurationException{
        
        if(ahkClass == null){
            return;
        }
        
        if((windowTitle == null || windowTitle.isEmpty()) && ahkClass.equals(AHK_CLASS_WILDCARD)){
            throw new IllegalArgumentException("The AHK class value can't be "+AHK_CLASS_WILDCARD+" when the window title is not provided.");
        }else if(windowUtilExeFile == null){
            throw new ConfigurationException("Unable to change whether the application's window is the foreground window or not.",
                    "The Window Util exe was not set.  There should have been an error reported earlier.",
                    null);
        }
        
        try {
            
            String titleParam = windowTitle != null ? windowTitle : "";
           
            String output = runCommand(windowUtilExeFile.getAbsolutePath(), "giveFocus", ahkClass, titleParam);

            if(logger.isInfoEnabled()){
                logger.info("Giving focus to '" + ahkClass + "'.  Script output = \n"+output+".");
            }

        } catch (IOException ex) {

            logger.error("There was an error giving focus to '" + ahkClass + "'", ex);
        }
    }

    /**
     * Gives an application's window focus. If null this method does nothing.
     * Note: the window search will ignore window title which may cause issues if more than one
     *      window is opened that has the same AHK Class value.
     *      
     * @param ahkClass The AHK class of the application
     * @throws IllegalArgumentException if there was an issue with one of the parameters provided to this method
     * @throws ConfigurationException if the Window Utility executable was not found on the users computer 
     */
    public static void giveFocus(String ahkClass) throws IllegalArgumentException, ConfigurationException {
        giveFocus(ahkClass, null);
    }
    
    /**
     * Minimizes an application window.
     * 
     * @param ahkClass The AHK class of the application. If null this method does nothing.
     *          Note: use AHK_CLASS_WILDCARD value when you want to apply AHK commands to first found window based on window title alone.
     * @param windowTitle the title of the application's window.  Can be null or an empty string if the window
     *              search shouldn't use the title and instead use the ahkClass value.
     * @throws IllegalArgumentException if there was an issue with one of the parameters provided to this method
     * @throws ConfigurationException if the Window Utility executable was not found on the users computer             
     */
    public static void minimizeWindow(String ahkClass, String windowTitle) throws IllegalArgumentException, ConfigurationException{
        
        if(ahkClass == null){
            return;
        }
        
        if((windowTitle == null || windowTitle.isEmpty()) && ahkClass.equals(AHK_CLASS_WILDCARD)){
            throw new IllegalArgumentException("The AHK class value can't be "+AHK_CLASS_WILDCARD+" when the window title is not provided.");
        }else if(windowUtilExeFile == null){
            throw new ConfigurationException("Unable to change whether the application's window is the foreground window or not.",
                    "The Window Util exe was not set.  There should have been an error reported earlier.",
                    null);
        }
        
        try {
            
            String titleParam = windowTitle != null ? windowTitle : "";
           
            String output = runCommand(windowUtilExeFile.getAbsolutePath(), "minimizeWindow", ahkClass, titleParam);            

            if(logger.isInfoEnabled()){
                logger.info("Minimizing window of '" + ahkClass + "'.  Script output = \n"+output+".");
            }

        } catch (IOException ex) {
            logger.error("There was an error minimizing the window of '" + ahkClass + "'", ex);
        }
    }

    /**
     * Minimizes an application window. If null this method does nothing.
     * Note: the window search will ignore window title which may cause issues if more than one
     *      window is opened that has the same AHK Class value.
     *
     * @param ahkClass The AHK class of the application
     * @throws IllegalArgumentException if there was an issue with one of the parameters provided to this method
     * @throws ConfigurationException if the Window Utility executable was not found on the users computer 
     */
    public static void minimizeWindow(String ahkClass) throws IllegalArgumentException, ConfigurationException {
        minimizeWindow(ahkClass, null);
    }
    
    /**
     * Run the specified command (includes arguments) and return any output from the execution
     * of the command.
     * 
     * @param command the list of command elements
     * @return String the output of executing the command.  Can be empty string but not null.
     * @throws IOException if there was a serve problem executing the process that contains the command or
     *          reading the output of the command (if any).
     */
    private static String runCommand(String... command) throws IOException{
        
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        
        InputStream stdout = process.getInputStream ();
        BufferedReader reader = new BufferedReader (new InputStreamReader(stdout));
        String line;
        StringBuffer outputStringBuffer = new StringBuffer();

        //Note: this while loop will finish once the process has finished, even if some part of the process doesn't write anything
        //      to the output stream.  This was tested by putting a "<sleep milliseconds="100000"/>" in the database-convert.xml.
        while ((line = reader.readLine ()) != null) {
            outputStringBuffer.append(line).append("\n");
        } 
        
        return outputStringBuffer.toString();
    }
}
